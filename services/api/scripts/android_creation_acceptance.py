"""Complete contest creation acceptance via Android UI and formal review APIs.

Student-owned stage changes are triggered only by visible Android controls. HTTP
is limited to fresh-account setup, read-only assertions, and reviewer actions.
"""
from __future__ import annotations

import argparse
from datetime import datetime
import json
import os
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET
from urllib.parse import urlsplit

import httpx


ROOT = Path(__file__).resolve().parents[3]
DEFAULT_ADB = Path(os.environ["LOCALAPPDATA"]) / "Android/Sdk/platform-tools/adb.exe"
BOUNDS = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")
INTERNAL_TOKEN = "dev-internal-worker-token-change-me-123456"


class AcceptanceApi:
    def __init__(self, base_url: str) -> None:
        self.client = httpx.Client(
            base_url=base_url,
            timeout=30,
            trust_env=False,
            limits=httpx.Limits(keepalive_expiry=10),
        )
        self.events: list[dict] = []

    def close(self) -> None:
        self.client.close()

    def call(self, method: str, path: str, *, headers=None, body=None, expected=200):
        response = self.client.request(method, path, headers=headers, json=body)
        self.events.append(
            {"method": method, "path": path, "status": response.status_code, "expected": expected}
        )
        assert response.status_code == expected, f"{method} {path}: {response.text}"
        return response.json() if response.content else None

    def register(self, phone: str, age_band: str) -> dict[str, str]:
        self.call(
            "POST", "/v1/auth/verification-codes",
            body={"phone": phone, "purpose": "REGISTER"}, expected=202,
        )
        result = self.call(
            "POST", "/v1/auth/register", expected=201,
            body={
                "phone": phone,
                "verification_code": "123456",
                "password": "AcceptancePass8!",
                "age_band": age_band,
                "terms_version": "2026-08",
                "privacy_version": "2026-08",
            },
        )
        return {"Authorization": f"Bearer {result['tokens']['access_token']}"}

    def bootstrap(self, student_phone: str, teacher_phone: str) -> tuple[dict[str, str], dict]:
        teacher = self.register(teacher_phone, "ADULT")
        student = self.register(student_phone, "AGE_14_TO_17")
        classroom = self.call(
            "POST", "/v1/classrooms", headers=teacher,
            body={"name": "机巧江湖 UI 验收班"}, expected=201,
        )
        return student, classroom

    def projects(self, student: dict[str, str]) -> list[dict]:
        return self.call("GET", "/v1/me/creation-projects?limit=50", headers=student)["items"]

    def project(self, student: dict[str, str], title: str) -> dict:
        return next(item for item in self.projects(student) if item["title"] == title)

    def versions(self, student: dict[str, str], project_id: str) -> list[dict]:
        return self.call("GET", f"/v1/creation-projects/{project_id}/versions", headers=student)["items"]

    def assert_joined(self, student: dict[str, str], classroom_id: str) -> None:
        items = self.call("GET", "/v1/me/classrooms", headers=student)["items"]
        assert any(item["id"] == classroom_id and item["can_submit"] for item in items)

    def download_signed_media(self, public_url: str) -> bytes:
        parsed = urlsplit(public_url)
        assert parsed.path.startswith("/v1/media-downloads/")
        local_path = parsed.path + (f"?{parsed.query}" if parsed.query else "")
        response = self.client.get(local_path, headers={"Connection": "close"})
        self.events.append(
            {
                "method": "GET",
                "path": "signed generated image",
                "status": response.status_code,
                "expected": 200,
            }
        )
        assert response.status_code == 200
        return response.content

    def moderate(self, student: dict[str, str], publication_id: str, decision: str) -> dict:
        case = self.call(
            "GET", f"/v1/publications/{publication_id}/moderation-case", headers=student,
        )
        internal = {"X-Internal-Token": INTERNAL_TOKEN}
        routed = self.call(
            "POST", f"/v1/internal/moderation-cases/{case['id']}/route", headers=internal,
        )
        body = {
            "decision": decision,
            "risk_level": "LOW" if decision == "PUBLISH" else "MEDIUM",
            "reviewer_reference": "contest-reviewer",
            "row_version": routed["row_version"],
        }
        if decision == "RETURN":
            body.update(
                reason_code="PRIVACY_CLUE",
                reason_summary="请确认画面中没有可识别的个人信息。",
                revision_suggestion="请检查并修改后重新提交。",
            )
        result = self.call(
            "POST", f"/v1/internal/moderation-cases/{case['id']}/decision",
            headers=internal, body=body,
        )
        expected = "PUBLISHED" if decision == "PUBLISH" else "RETURNED"
        assert result["publication_status"] == expected
        return result


class DeviceFlow:
    def __init__(self, adb: Path, device: str, package: str, output: Path) -> None:
        self.adb = adb
        self.device = device
        self.package = package
        self.output = output
        self.checks: list[dict] = []

    def check(self, label: str) -> None:
        self.checks.append({"check": label, "result": "pass"})

    def run_adb(self, *args: str, timeout: int = 25) -> bytes:
        return subprocess.run(
            [str(self.adb), "-s", self.device, *args],
            check=True,
            capture_output=True,
            timeout=timeout,
        ).stdout

    def snapshot(self, name: str) -> tuple[ET.Element, bytes]:
        self.output.mkdir(parents=True, exist_ok=True)
        remote = "/sdcard/creation-acceptance.xml"
        self.run_adb("shell", "uiautomator", "dump", remote)
        raw = self.run_adb("shell", "cat", remote)
        tree = ET.fromstring(raw)
        (self.output / f"{name}.xml").write_bytes(raw)
        (self.output / f"{name}.png").write_bytes(self.run_adb("exec-out", "screencap", "-p"))
        return tree, raw

    @staticmethod
    def nodes(tree: ET.Element) -> list[ET.Element]:
        return list(tree.iter("node"))

    def app_nodes(self, tree: ET.Element) -> list[ET.Element]:
        return [node for node in self.nodes(tree) if node.get("package") == self.package]

    def find(self, tree: ET.Element, *, text: str | None = None,
             description: str | None = None, class_name: str | None = None,
             contains: bool = False) -> ET.Element | None:
        for node in self.app_nodes(tree):
            if class_name and node.get("class") != class_name:
                continue
            if text is not None:
                actual = node.get("text", "")
                if (text not in actual) if contains else (text != actual):
                    continue
            if description is not None:
                actual = node.get("content-desc", "")
                if (description not in actual) if contains else (description != actual):
                    continue
            return node
        return None

    def find_tappable(self, tree: ET.Element, *, text: str | None = None,
                      description: str | None = None, contains: bool = False) -> ET.Element | None:
        parent_of = {child: parent for parent in tree.iter() for child in parent}
        for node in self.app_nodes(tree):
            if text is not None:
                actual = node.get("text", "")
                if (text not in actual) if contains else (text != actual):
                    continue
            if description is not None:
                actual = node.get("content-desc", "")
                if (description not in actual) if contains else (description != actual):
                    continue
            target: ET.Element | None = node
            while target is not None:
                if target.get("clickable") == "true" and target.get("enabled") == "true":
                    return target
                target = parent_of.get(target)
        return None

    def wait_tappable(self, label: str, timeout: float = 18, **selector) -> ET.Element:
        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline:
            try:
                tree, _ = self.snapshot("current")
                found = self.find_tappable(tree, **selector)
                if found is not None:
                    self.check(label)
                    return found
            except (subprocess.CalledProcessError, subprocess.TimeoutExpired, ET.ParseError):
                pass
            time.sleep(0.35)
        raise AssertionError(f"Timed out waiting for tappable {label}: {selector}")

    def wait_find(self, label: str, timeout: float = 18, *, record: bool = True,
                  **selector) -> ET.Element:
        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline:
            try:
                tree, _ = self.snapshot("current")
                found = self.find(tree, **selector)
                if found is not None:
                    if record:
                        self.check(label)
                    return found
            except (subprocess.CalledProcessError, subprocess.TimeoutExpired, ET.ParseError):
                pass
            time.sleep(0.35)
        raise AssertionError(f"Timed out waiting for {label}: {selector}")

    def tap(self, node: ET.Element) -> None:
        match = BOUNDS.fullmatch(node.get("bounds", ""))
        assert match, node.attrib
        left, top, right, bottom = map(int, match.groups())
        self.tap_xy((left + right) // 2, (top + bottom) // 2)

    def tap_xy(self, x: int, y: int) -> None:
        self.run_adb("shell", "input", "tap", str(x), str(y))

    def tap_text(self, label: str, text: str, *, timeout: float = 18,
                 contains: bool = False) -> None:
        self.tap(self.wait_tappable(label, timeout=timeout, text=text, contains=contains))

    def tap_description(self, label: str, description: str, *, timeout: float = 18) -> None:
        self.tap(self.wait_tappable(label, timeout=timeout, description=description))

    def input_text(self, value: str) -> None:
        assert re.fullmatch(r"[A-Za-z0-9!._-]+", value), value
        self.run_adb("shell", "input", "text", value)

    def enter(self, node: ET.Element, value: str) -> None:
        self.tap(node)
        self.input_text(value)
        # The contest emulator uses a hardware keyboard, so there is no visual
        # IME to dismiss. Sending Escape/Back here can close the current Compose
        # dialog or navigate away from the page instead of merely ending input.
        time.sleep(0.35)

    def swipe_up(self, short: bool = False) -> None:
        start = 1850 if short else 2000
        end = 1200 if short else 850
        self.run_adb("shell", "input", "swipe", "540", str(start), "540", str(end), "420")
        time.sleep(0.45)

    def scroll_find(self, label: str, *, text: str | None = None,
                    description: str | None = None, max_swipes: int = 6) -> ET.Element:
        for _ in range(max_swipes + 1):
            tree, _ = self.snapshot("current")
            found = self.find_tappable(tree, text=text, description=description)
            if found is not None:
                self.check(label)
                return found
            self.swipe_up(short=True)
        raise AssertionError(f"Could not scroll to {label}")

    def capture(self, name: str, label: str, **selector) -> ET.Element:
        tree, raw = self.snapshot(name)
        found = self.find(tree, **selector)
        assert found is not None, f"{label}: {selector}"
        self.assert_no_internal_labels(raw)
        self.check(label)
        return found

    def assert_no_internal_labels(self, raw: bytes) -> None:
        decoded = raw.decode("utf-8", "replace")
        forbidden = [
            "DRAFT", "PRODUCTION", "TEST", "SEAL", "SQUARE", "MEDIUM",
            "development", "rules-coach", "gpt-image", "AIGC", "来源谱",
            "执行器名", "模型名", "worker", "JSON", "数据库", "接口字段",
        ]
        found = [item for item in forbidden if item in decoded]
        assert not found, f"Internal labels visible in UI tree: {found}"


def open_home(flow: DeviceFlow) -> None:
    for _ in range(6):
        tree, _ = flow.snapshot("current")
        if flow.find(tree, description="作品创作") is not None:
            return
        flow.run_adb("shell", "input", "keyevent", "4")
        time.sleep(0.7)
    raise AssertionError("无法返回首页")


def open_workshop(flow: DeviceFlow, *, first_visit: bool) -> None:
    flow.tap_description("打开作品创作入口", "作品创作")
    if first_visit:
        for _ in range(5):
            tree, _ = flow.snapshot("creation-entry-guide")
            workshop = flow.find(tree, description="工坊")
            if workshop is not None and workshop.get("enabled") == "true":
                break
            flow.tap_xy(540, 1200)
            time.sleep(0.65)
        else:
            raise AssertionError("首次创作引导没有显示工坊入口")
    else:
        workshop = flow.wait_find("创作引导完成后直接显示工坊", timeout=3, description="工坊")
        _, raw = flow.snapshot("creation-entry-revisit")
        assert "快去工坊里头看看吧" not in raw.decode("utf-8", "replace")
        flow.check("创作入口引导每个账号只播放一次")
    flow.tap(workshop)
    flow.wait_find("进入创作台", text="你今天想做什么？")


def create_conversation_work(flow: DeviceFlow, title: str) -> None:
    idea = flow.wait_find("创意输入框可用", class_name="android.widget.EditText")
    flow.enter(idea, title)
    flow.tap_text("提交最初想法", "开始")
    flow.wait_find("直接进入教练对话", text="我明白了", contains=True, timeout=30)
    flow.capture("04-first-coach-analysis", "首条教练分析已显示", text="我明白了", contains=True)
    flow.tap_description("采纳教练建议", "采纳教练建议并继续")
    flow.wait_find("采纳后继续分析", text="好，我们采用这个方向", contains=True, timeout=25)

    message = flow.wait_find(
        "沟通输入框可用", description="给熊猫教练发送修改想法"
    )
    flow.enter(message, "purpleevening")
    flow.tap_description("发送新的修改要求", "发送消息")
    flow.wait_find("教练按新要求重做建议", text="我理解你希望", contains=True, timeout=25)
    flow.capture("05-revised-dialogue", "拒绝旧方向后通过输入完成调整", text="我理解你希望", contains=True)

    flow.tap_description("保存对话草稿并开始创作", "保存上传草稿")
    flow.wait_find("真实生成结果可以预览", description="本次创作的作品预览", timeout=45)
    flow.capture("06-first-result", "第一版生成结果已显示", description="本次创作的作品预览")

    message = flow.wait_find(
        "生成结果页可直接继续沟通", description="给熊猫教练发送修改想法"
    )
    flow.enter(message, "brightersky")
    flow.tap_description("从结果页直接发送第二次修改", "发送消息")
    flow.wait_find("第二次建议返回", text="我理解你希望", contains=True, timeout=25)
    flow.tap_description("保存第二版草稿并创作", "保存上传草稿")
    flow.wait_find("第二版真实结果可预览", description="本次创作的作品预览", timeout=45)
    flow.capture("07-final-result", "第二版生成结果已显示", description="本次创作的作品预览")
    flow.tap_description("确认保存作品", "保存作品")
    flow.wait_find("保存后主动作进入档案", text="查看创作档案", timeout=25)
    flow.capture("08-work-saved", "作品已保存且不自动提交", text="查看创作档案")


def open_archive_and_select(flow: DeviceFlow, title: str) -> None:
    flow.tap_text("打开创作档案", "查看创作档案")
    for _ in range(6):
        tree, _ = flow.snapshot("current")
        picker = flow.find_tappable(tree, description="选择作品")
        if picker is not None:
            flow.tap(picker)
            break
        flow.tap_xy(540, 1200)
        time.sleep(0.55)
    else:
        raise AssertionError("创作档案选择作品入口未出现")
    select_archive_work(flow, title)


def select_archive_work(flow: DeviceFlow, title: str) -> None:
    selection = flow.wait_find(
        "档案列表加载真实作品",
        description=f"选择作品：{title}",
        timeout=25,
    )
    flow.tap(selection)
    flow.wait_find("档案主动作显示继续创作", text="继续创作", timeout=20)


def inspect_archive_lotus(flow: DeviceFlow) -> None:
    entries = [
        ("查看原创记录", "09-archive-original", "我的最初想法"),
        ("查看修改版本记录", "10-archive-versions", "确认作品"),
        ("查看创作教练记录", "11-archive-coach", "教练"),
    ]
    for description, screenshot, expected_text in entries:
        flow.tap_description(f"打开{description}", description)
        flow.wait_find(f"{description}显示真实内容", text=expected_text, contains=True, timeout=20)
        flow.capture(screenshot, f"{description}内容与新流程一致", text=expected_text, contains=True)
        flow.tap_description(f"关闭{description}", "关闭当前档案记录")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--device", default="emulator-5554")
    parser.add_argument("--package", default="com.jueqiao.jianghu.demo")
    parser.add_argument("--student-phone", required=True)
    parser.add_argument("--teacher-phone", required=True)
    parser.add_argument("--password", default="AcceptancePass8!")
    parser.add_argument("--base-url", default="http://127.0.0.1:8011")
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--adb", type=Path, default=DEFAULT_ADB)
    args = parser.parse_args()

    title = f"Flow{args.student_phone[-6:]}"
    flow = DeviceFlow(args.adb, args.device, args.package, args.output)
    api = AcceptanceApi(args.base_url)
    student, _classroom = api.bootstrap(args.student_phone, args.teacher_phone)

    try:
        flow.run_adb("shell", "pm", "clear", args.package)
        flow.run_adb("logcat", "-c")
        flow.run_adb(
            "shell", "am", "start", "-W", "-n",
            f"{args.package}/com.jueqiao.jianghu.MainActivity",
            timeout=60,
        )
        flow.tap_description("打开登录入口", "点击进入登录页")
        tree, _ = flow.snapshot("01-login")
        fields = [node for node in flow.app_nodes(tree) if node.get("class") == "android.widget.EditText"]
        assert len(fields) >= 2
        flow.enter(fields[0], args.student_phone)
        flow.enter(fields[1], args.password)
        flow.tap_description("勾选用户协议", "同意用户协议与隐私条款")
        flow.tap_text("提交登录", "登 录")

        for _ in range(24):
            tree, _ = flow.snapshot("02-home-onboarding")
            if flow.find(tree, description="作品创作") is not None:
                break
            flow.tap_xy(540, 1200)
            time.sleep(0.5)
        else:
            raise AssertionError("首页作品创作入口未出现")
        flow.check("全新学生登录并进入首页")

        open_workshop(flow, first_visit=True)
        create_conversation_work(flow, title)

        project = api.project(student, title)
        conversation = api.call(
            "GET", f"/v1/creation-projects/{project['id']}/conversation", headers=student,
        )
        assert conversation["status"] == "SAVED"
        assert len(conversation["draft_version_ids"]) == 2
        assert len(conversation["saved_version_ids"]) == 1
        assert any(item["decision"] == "REPLACED" for item in conversation["messages"])
        assert project["latest_publication"] is None
        tests = api.call(
            "GET", f"/v1/creation-projects/{project['id']}/test-records", headers=student,
        )["items"]
        assert len(tests) == 2 and all(item["result"] == "PASSED" for item in tests)
        jobs = api.call(
            "GET", f"/v1/creation-projects/{project['id']}/image-generations", headers=student,
        )["items"]
        completed = [job for job in jobs if job["status"] == "COMPLETED"]
        assert len(completed) == 2
        image_bytes = api.download_signed_media(completed[0]["output_asset"]["original_url"])
        assert image_bytes.startswith(b"\x89PNG\r\n\x1a\n")

        versions_before_continue = len(api.versions(student, project["id"]))
        messages_before_continue = len(conversation["messages"])
        open_archive_and_select(flow, title)
        inspect_archive_lotus(flow)
        flow.tap_text("从档案继续创作", "继续创作")
        flow.wait_find("恢复保存后的结果预览", description="本次创作的作品预览", timeout=30)
        flow.capture("12-archive-continue", "继续创作恢复原对话与结果", description="本次创作的作品预览")
        resumed = api.call(
            "GET", f"/v1/creation-projects/{project['id']}/conversation", headers=student,
        )
        assert len(api.versions(student, project["id"])) == versions_before_continue
        assert len(resumed["messages"]) == messages_before_continue
        flow.check("继续创作不重复创建版本且完整恢复对话")

        flow.run_adb("shell", "am", "force-stop", args.package)
        flow.run_adb(
            "shell", "am", "start", "-W", "-n",
            f"{args.package}/com.jueqiao.jianghu.MainActivity",
            timeout=60,
        )
        flow.tap_description("重启后进入应用", "点击进入登录页")
        flow.wait_find("重启后会话仍有效", description="作品创作", timeout=25)
        open_workshop(flow, first_visit=False)
        flow.wait_find("重启后最近作品仍存在", text=title, timeout=20)
        flow.tap_text("重启后继续作品", "继续")
        flow.wait_find("重启后恢复生成结果", description="本次创作的作品预览", timeout=30)
        flow.capture("13-restart-resume", "重启后创作状态保持一致", description="本次创作的作品预览")

        package_info = flow.run_adb(
            "shell", "cmd", "package", "list", "packages", "-U", args.package
        ).decode("utf-8", "replace")
        uid_match = re.search(r"uid:(\d+)", package_info)
        assert uid_match, f"无法读取应用 UID：{package_info}"
        logs = flow.run_adb(
            "logcat", "-d", f"--uid={uid_match.group(1)}", "-v", "brief"
        ).decode("utf-8", "replace")
        assert "unexpected end of stream" not in logs
        assert "database is locked" not in logs.lower()
        assert "FATAL EXCEPTION" not in logs
        flow.check("全程无连接中断、数据库忙与 Android 崩溃")

        report = {
            "package": args.package,
            "device": args.device,
            "studentPhone": args.student_phone,
            "projectId": project["id"],
            "projectTitle": title,
            "checks": flow.checks,
            "apiChecks": api.events,
            "completedAt": datetime.now().astimezone().isoformat(),
        }
        (args.output / "result.json").write_text(
            json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8",
        )
        print(
            f"PASS: {len(flow.checks)} Android UI checks + {len(api.events)} API checks; "
            f"evidence: {args.output}"
        )
    finally:
        api.close()


if __name__ == "__main__":
    main()
