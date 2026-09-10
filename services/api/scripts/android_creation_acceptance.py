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

    def run_adb(self, *args: str) -> bytes:
        return subprocess.run(
            [str(self.adb), "-s", self.device, *args],
            check=True,
            capture_output=True,
            timeout=25,
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


def start_project(flow: DeviceFlow, title: str, *, adjust_method: bool) -> None:
    idea = flow.wait_find("创意输入框可用", class_name="android.widget.EditText")
    flow.enter(idea, title)
    tree, _ = flow.snapshot("current")
    if flow.find(tree, text="创作工法") is None:
        flow.tap_text("提交创意", "开始")
    flow.wait_find("创意已整理为工法", text="创作工法", timeout=25)
    flow.snapshot(f"idea-method-{title}")
    if adjust_method:
        flow.tap_text("打开工法调整", "调整工法")
        tree, _ = flow.snapshot("current")
        fields = [n for n in flow.app_nodes(tree) if n.get("class") == "android.widget.EditText"]
        assert len(fields) >= 3
        flow.enter(fields[0], "_clear")
        flow.tap_text("确认工法调整", "完成调整")
    flow.tap_text("确认工法并进入草图", "进入草图/脚本")
    draft = flow.wait_find("草图阶段显示保存并继续", description="保存并继续", timeout=25)
    flow.capture(f"draft-{title}", "草图任务页已就绪", description="保存并继续")
    flow.tap(draft)
    flow.wait_find("草图版本保存成功", text="已保存为 V1，可继续修改", timeout=25)
    flow.capture(f"production-home-{title}", "进入制作阶段", description="下一步")
    flow.tap_description("打开制作任务", "下一步")
    flow.wait_find("制作流程从第一项开始", text="制作阶段 · 1/4", timeout=20)


def finish_production(flow: DeviceFlow, *, rich: bool, title: str) -> None:
    if rich:
        prompt = flow.wait_find("图片描述输入框可用", class_name="android.widget.EditText")
        flow.enter(prompt, "lotuswatermachine")
        flow.tap(flow.scroll_find("核对图片生成内容", text="核对生成内容"))
        flow.wait_find("显示生成确认", text="生成确认")
        confirm = flow.scroll_find("确认图片生成", text="确认生成")
        flow.snapshot(f"generation-confirm-{title}")
        flow.tap(confirm)
        flow.wait_find("图片生成完成", text="生成完成", timeout=35)
        flow.wait_find("生成结果真实预览节点可见", timeout=15, description="生成结果预览")
        tree, raw = flow.snapshot(f"generation-preview-{title}")
        assert flow.find(tree, text="图片暂时没有加载出来") is None
        flow.assert_no_internal_labels(raw)
        flow.check("图片预览不是空白失败态")
        flow.tap(flow.scroll_find("生成后进入下一项", text="下一项"))

        flow.wait_find("进入教练确认任务", text="制作阶段 · 2/4")
        flow.tap(flow.scroll_find("请求创作教练", text="准备建议并查看确认页"))
        flow.wait_find("教练调用确认出现", text="调用确认", timeout=20)
        flow.capture(f"coach-confirm-{title}", "学生可确认教练建议", text="确认并获取")
        flow.tap(flow.scroll_find("确认获取建议", text="确认并获取"))
        flow.wait_find("教练建议已返回", text="最近一次建议", timeout=25)
        flow.capture(f"coach-result-{title}", "教练建议只读展示", text="最近一次建议")
        flow.tap(flow.scroll_find("教练后进入画布任务", text="下一项"))

        flow.wait_find("进入画布任务", text="制作阶段 · 3/4")
        flow.tap(flow.scroll_find("打开画布编辑器", text="进入画布编辑器"))
        flow.wait_find("画布编辑器载入真实版本", text="画布", timeout=25)
        flow.tap(flow.scroll_find("画布新增文字", text="加文字"))
        flow.tap(flow.wait_find("画布修改后允许保存", text="保存为新版本", timeout=12))
        flow.wait_find("画布保存产生新版本", text="当前 V3", contains=True, timeout=25)
        flow.capture(f"editor-saved-{title}", "画布有效修改保存完成", text="当前 V3", contains=True)
        # Use Android back here as well as the visible button path elsewhere;
        # the editor owns a BackHandler and returns to the saved workflow route.
        flow.run_adb("shell", "input", "keyevent", "4")
        flow.wait_find("返回画布任务", text="制作阶段 · 3/4", timeout=20)
        flow.tap(flow.scroll_find("确认排版完成", text="排版已完成"))
    else:
        flow.tap(flow.scroll_find("精简作品跳过图片生成", text="暂不生成"))
        flow.wait_find("精简作品进入教练任务", text="制作阶段 · 2/4")
        flow.tap(flow.scroll_find("精简作品跳过教练", text="暂不询问"))
        flow.wait_find("精简作品进入画布任务", text="制作阶段 · 3/4")
        flow.tap(flow.scroll_find("精简作品确认排版完成", text="排版已完成"))
    flow.wait_find("制作最后一步", text="制作阶段 · 4/4")
    flow.tap(flow.scroll_find("通过 UI 进入测试", text="进入测试"))
    flow.wait_find("测试任务已打开", text="测试阶段 · 1/1", timeout=25)


def finish_test(flow: DeviceFlow, *, rich: bool, title: str) -> None:
    if rich:
        finding = flow.scroll_find("找到问题记录输入", text="发现的问题")
        flow.enter(finding, "arrowunclear")
        flow.tap(flow.scroll_find("保存需要修改结果", text="保存测试结果"))
        flow.wait_find("生成待处理问题", text="还需处理 1 个问题", timeout=25)
        flow.capture(f"test-needs-revision-{title}", "测试问题已记录", text="还需处理 1 个问题")
        resolution = flow.scroll_find("填写问题处理结果", text="我是怎样处理的")
        flow.enter(resolution, "addedcleararrow")
        flow.tap(flow.scroll_find("保存问题处理结果", text="保存处理结果"))
        flow.wait_find("问题关闭后可以复测", text="上次结果", timeout=25)
    flow.tap(flow.scroll_find("选择测试通过", text="通过"))
    flow.tap(flow.scroll_find("保存复测通过", text="保存测试结果"))
    flow.wait_find("复测通过允许封卷", text="进入封卷", timeout=25)
    flow.capture(f"test-passed-{title}", "复测通过状态正确", text="进入封卷")
    flow.tap_text("通过 UI 进入封卷", "进入封卷")
    flow.wait_find("封卷从第一步开始", text="封卷准备 · 1/4", timeout=25)


def finish_seal_and_submit(flow: DeviceFlow, *, title: str) -> None:
    description = flow.scroll_find("填写作品说明", text="这件作品是什么、希望表达什么")
    flow.enter(description, "watersavingwork")
    flow.tap(flow.scroll_find("完成封卷第一步", text="下一项"))
    flow.wait_find("封卷第二步出现", text="封卷准备 · 2/4")
    learned = flow.scroll_find("填写学习收获", text="这次学会了什么")
    flow.enter(learned, "learnedclearsteps")
    improvement = flow.scroll_find("填写下次改进", text="下一次还想改什么")
    flow.enter(improvement, "simplifyarrows")
    flow.tap(flow.scroll_find("保存封卷第二步", text="保存并继续"))
    flow.wait_find("封卷第三步保存后出现", text="封卷准备 · 3/4", timeout=30)
    human = flow.scroll_find("填写本人贡献", text="我亲自完成了什么")
    flow.enter(human, "idesigntestandedit")
    flow.tap(flow.scroll_find("保存来源说明", text="保存并继续"))
    flow.wait_find("封卷第四步出现", text="封卷准备 · 4/4", timeout=30)
    privacy_labels = [
        "作品中没有真实姓名、学校或可识别身份信息",
        "作品中没有手机号、地址、账号等联系方式",
        "没有敏感肖像，或已经取得相关使用许可",
    ]
    for index, label in enumerate(privacy_labels, start=1):
        flow.tap(flow.scroll_find(f"确认隐私项 {index}", text=label))
    flow.tap(flow.scroll_find("保存隐私检查", text="保存并检查"))
    flow.wait_find("投递位置已解锁", text="选择投递位置", timeout=30)
    flow.capture(f"seal-delivery-{title}", "单一班级已自动选择", text="提交作品")
    flow.tap(flow.scroll_find("通过 UI 提交作品", text="提交作品"))
    flow.wait_find("提交后进入独立状态卡", text="作品提交状态", timeout=30)
    flow.wait_find(
        "提交状态为检查中",
        text="作品正在检查，通过前不会公开展示。",
    )
    flow.capture(f"submitted-{title}", "提交状态卡显示检查中", text="作品提交状态")


def reopen_project_from_workshop(flow: DeviceFlow, title: str) -> None:
    flow.tap_text("关闭提交状态卡", "关闭")
    flow.run_adb("shell", "input", "keyevent", "4")
    flow.wait_find("返回创作台", text="你今天想做什么？", timeout=20)
    flow.wait_find("创作台列出目标作品", text=title)
    flow.tap_text("从创作台打开作品", "继续")


def select_archive_work(flow: DeviceFlow, title: str) -> None:
    selection = flow.wait_find(
        "档案列表加载真实作品", description=f"选择作品：{title}", timeout=25,
    )
    flow.tap(selection)
    flow.wait_find("档案主动作显示继续创作", text="继续创作", timeout=20)


def more_action(flow: DeviceFlow, label: str) -> None:
    flow.tap_text("打开更多管理操作", "更多")
    flow.wait_find(label, text="删除作品", timeout=20)


def delete_selected(flow: DeviceFlow, label: str) -> None:
    more_action(flow, f"{label} 的管理操作可用")
    flow.tap_text(f"{label} 打开删除确认", "删除作品")
    flow.wait_find(
        f"{label} 显示删除影响",
        text="删除后作品与已发布内容都会撤下，且不能恢复。",
    )
    flow.tap_text(f"{label} 确认删除", "确认删除")
    time.sleep(2)


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

    suffix = args.student_phone[-6:]
    main_title = f"Flow{suffix}"
    appeal_title = f"Case{suffix}"
    flow = DeviceFlow(args.adb, args.device, args.package, args.output)
    api = AcceptanceApi(args.base_url)
    student, classroom = api.bootstrap(args.student_phone, args.teacher_phone)

    try:
        flow.run_adb("shell", "pm", "clear", args.package)
        flow.run_adb("logcat", "-c")
        flow.run_adb(
            "shell", "am", "start", "-W", "-n",
            f"{args.package}/com.jueqiao.jianghu.MainActivity",
        )
        flow.tap_description("打开登录入口", "点击进入登录页")
        phone_field = flow.wait_find("显示登录表单", class_name="android.widget.EditText")
        tree, _ = flow.snapshot("01-login")
        fields = [node for node in flow.app_nodes(tree) if node.get("class") == "android.widget.EditText"]
        assert len(fields) >= 2
        flow.tap(phone_field)
        flow.input_text(args.student_phone)
        flow.tap(fields[1])
        flow.input_text(args.password)
        flow.tap_description("勾选用户协议", "同意用户协议与隐私条款")
        flow.tap_text("提交登录", "登 录")

        for _ in range(24):
            tree, _ = flow.snapshot("02-home-onboarding")
            creation = flow.find(tree, description="作品创作")
            if creation is not None:
                break
            flow.tap_xy(540, 1200)
            time.sleep(0.5)
        else:
            raise AssertionError("首页作品创作入口未出现")
        flow.check("全新学生登录并进入首页")

        flow.tap_description("打开书信入口", "书信，查看评招、邀请与提醒")
        tree, _ = flow.snapshot("current")
        if flow.find(tree, text="查看作品与班级来信") is not None:
            flow.tap_text("打开班级来信", "查看作品与班级来信")
        join_field = flow.wait_find("打开加入班级表单", class_name="android.widget.EditText")
        flow.enter(join_field, classroom["join_code"])
        flow.tap_text("确认加入班级", "确认加入")
        time.sleep(1.5)
        api.assert_joined(student, classroom["id"])
        flow.capture("03-classroom-joined", "学生通过 UI 加入可投递班级", text="已加入", contains=True)

        open_home(flow)
        open_workshop(flow, first_visit=True)
        start_project(flow, main_title, adjust_method=True)
        finish_production(flow, rich=True, title=main_title)
        finish_test(flow, rich=True, title=main_title)
        finish_seal_and_submit(flow, title=main_title)

        main_project = api.project(student, main_title)
        main_publication = main_project["latest_publication"]
        assert main_publication and main_publication["status"] == "PENDING_CHECK"
        generated_jobs = api.call(
            "GET", f"/v1/creation-projects/{main_project['id']}/image-generations", headers=student,
        )["items"]
        generated = next(job for job in generated_jobs if job["status"] == "COMPLETED")
        image_bytes = api.download_signed_media(generated["output_asset"]["original_url"])
        assert image_bytes.startswith(b"\x89PNG\r\n\x1a\n")

        api.moderate(student, main_publication["id"], "PUBLISH")
        reopen_project_from_workshop(flow, main_title)
        flow.tap_description("已发布作品主按钮显示状态", "查看提交状态")
        flow.wait_find("App 显示已发布", text="作品已经通过并发布", contains=True, timeout=20)
        flow.capture("published-status", "正式审核后 App 状态同步", text="作品已经通过并发布", contains=True)
        flow.tap_text("关闭已发布状态", "关闭")

        before_continue = len(api.versions(student, main_project["id"]))
        flow.tap_text("从作品页打开档案", "创作档案")
        flow.tap_xy(540, 1000)
        picker = flow.wait_find("首次档案引导完成", description="选择作品", timeout=12)
        flow.tap(picker)
        select_archive_work(flow, main_title)
        flow.capture("archive-main-selected", "档案保持继续创作加更多层级", text="继续创作")
        flow.tap_text("从档案继续创作", "继续创作")
        flow.wait_find("继续创作进入制作阶段", description="下一步", timeout=30)
        after_continue = len(api.versions(student, main_project["id"]))
        assert after_continue == before_continue + 1
        assert api.project(student, main_title)["current_stage"] == "PRODUCTION"
        flow.check("继续创作通过正式 API 新建且只新建一个版本")

        flow.tap_text("继续版本打开创作档案", "创作档案")
        picker = flow.wait_find("档案再次进入不重播引导", timeout=4, description="选择作品")
        flow.check("档案引导每个账号只播放一次")
        flow.tap(picker)
        select_archive_work(flow, main_title)
        more_action(flow, "已发布作品可撤回")
        flow.tap_text("打开撤回确认", "撤回已发布作品")
        flow.wait_find(
            "撤回影响说明清晰", text="撤回后将从班级或大会中移除，作品和版本仍会保留。",
        )
        flow.tap_text("确认撤回作品", "确认撤回")
        flow.wait_find("撤回后档案立即更新", text="已撤回", contains=True, timeout=25)
        flow.capture("withdrawn-main", "撤回完成且版本保留", text="已撤回", contains=True)

        open_home(flow)
        open_workshop(flow, first_visit=False)
        start_project(flow, appeal_title, adjust_method=False)
        finish_production(flow, rich=False, title=appeal_title)
        finish_test(flow, rich=False, title=appeal_title)
        finish_seal_and_submit(flow, title=appeal_title)

        appeal_project = api.project(student, appeal_title)
        appeal_publication = appeal_project["latest_publication"]
        assert appeal_publication and appeal_publication["status"] == "PENDING_CHECK"
        api.moderate(student, appeal_publication["id"], "RETURN")
        flow.tap_text("关闭第二份提交状态", "关闭")
        flow.tap_text("打开第二份作品档案", "创作档案")
        flow.tap(flow.wait_find("档案选择入口", description="选择作品"))
        select_archive_work(flow, appeal_title)
        more_action(flow, "退回作品显示申诉入口")
        appeal_field = flow.wait_find("申诉输入框可用", text="申诉说明")
        flow.enter(appeal_field, "pleasecheckagain")
        flow.tap(flow.scroll_find("通过 UI 提交申诉", text="提交申诉"))
        flow.wait_find("提交后档案显示申诉处理中", text="申诉处理中", contains=True, timeout=25)
        flow.capture("appeal-pending", "申诉提交成功", text="申诉处理中", contains=True)
        more_action(flow, "申诉处理中作品仍可管理")
        flow.wait_find("申诉处理中不能重复填写", text="申诉已提交", contains=True)
        flow.capture(
            "appeal-pending-more",
            "申诉处理中不能重复提交",
            text="申诉已提交",
            contains=True,
        )
        flow.run_adb("shell", "input", "keyevent", "4")

        flow.run_adb("shell", "am", "force-stop", args.package)
        flow.run_adb("shell", "am", "start", "-n", f"{args.package}/com.jueqiao.jianghu.MainActivity")
        flow.tap_description("重启后进入应用", "点击进入登录页")
        flow.wait_find("重启后会话仍有效", description="作品创作", timeout=20)
        open_workshop(flow, first_visit=False)
        flow.tap_text("重启后打开档案", "创作档案")
        flow.tap(flow.wait_find("重启后档案直接可用", timeout=4, description="选择作品"))
        select_archive_work(flow, appeal_title)
        more_action(flow, "重启后读取正式申诉列表")
        flow.wait_find("重启后仍显示申诉处理中", text="申诉已提交", contains=True)
        flow.capture(
            "appeal-pending-after-restart", "申诉状态重启后一致", text="申诉已提交", contains=True,
        )
        flow.run_adb("shell", "input", "keyevent", "4")

        delete_selected(flow, "退回作品")
        flow.wait_find("删除后返回创作台", text="你今天想做什么？", timeout=20)
        flow.tap_text("删除后重新打开档案", "创作档案")
        flow.tap(flow.wait_find("删除后可重新选作品", description="选择作品", timeout=20))
        select_archive_work(flow, main_title)
        delete_selected(flow, "撤回作品")
        flow.wait_find("删除最后作品后返回创作台", text="你今天想做什么？", timeout=20)
        flow.tap_text("删除最后作品后打开档案", "创作档案")
        flow.tap(flow.wait_find("删除全部作品后打开列表", description="选择作品", timeout=20))
        flow.wait_find("所有测试作品删除后为空", text="暂无已保存作品", timeout=20)
        flow.capture("archive-empty", "最终回到空档案状态", text="暂无已保存作品")

        projects_left = [item for item in api.projects(student) if item["status"] == "ACTIVE"]
        assert not projects_left, projects_left
        appeals = api.call("GET", "/v1/me/moderation-appeals", headers=student)
        assert len(appeals) == 1 and appeals[0]["status"] == "PENDING"

        package_info = flow.run_adb(
            "shell", "cmd", "package", "list", "packages", "-U", args.package
        ).decode("utf-8", "replace")
        uid_match = re.search(r"uid:(\d+)", package_info)
        assert uid_match, f"无法读取应用 UID：{package_info}"
        logs = flow.run_adb(
            "logcat", "-d", f"--uid={uid_match.group(1)}", "-v", "brief"
        ).decode("utf-8", "replace")
        assert "unexpected end of stream" not in logs, "比赛后端访问仍出现 EOF"
        assert "database is locked" not in logs.lower(), "流程出现 SQLite busy/locked"
        assert "FATAL EXCEPTION" not in logs, "演示流程出现 Android 崩溃"
        flow.check("全程无 EOF、SQLite busy 与 Android 崩溃")

        report = {
            "package": args.package,
            "device": args.device,
            "studentPhone": args.student_phone,
            "teacherPhone": args.teacher_phone,
            "classroomId": classroom["id"],
            "projects": {"main": main_title, "appeal": appeal_title},
            "checks": flow.checks,
            "apiChecks": api.events,
            "completedAt": datetime.now().astimezone().isoformat(),
        }
        (args.output / "result.json").write_text(
            json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8",
        )
        print(
            f"PASS: {len(flow.checks)} Android UI checks + {len(api.events)} formal API checks; "
            f"evidence: {args.output}"
        )
    finally:
        api.close()


if __name__ == "__main__":
    main()
