"""Contest creation acceptance over public HTTP APIs only; never edits business tables."""
from __future__ import annotations

import argparse
import json
from pathlib import Path
import time
from urllib.parse import urlparse
import uuid

import httpx


class CreationAcceptance:
    def __init__(self, client: httpx.Client) -> None:
        self.client = client
        self.events: list[dict] = []

    def call(self, method, path, headers=None, body=None, expected=200):
        response = self.client.request(method, path, headers=headers, json=body)
        self.events.append(
            {"method": method, "path": path, "status": response.status_code, "expected": expected}
        )
        assert response.status_code == expected, f"{method} {path}: {response.text}"
        return response.json() if response.content else None

    def register(self, phone: str, age_band: str) -> dict[str, str]:
        self.call(
            "POST",
            "/v1/auth/verification-codes",
            body={"phone": phone, "purpose": "REGISTER"},
            expected=202,
        )
        result = self.call(
            "POST",
            "/v1/auth/register",
            body={
                "phone": phone,
                "verification_code": "123456",
                "password": "AcceptancePass8!",
                "age_band": age_band,
                "terms_version": "2026-08",
                "privacy_version": "2026-08",
            },
            expected=201,
        )
        return {"Authorization": f"Bearer {result['tokens']['access_token']}"}

    def transition(self, headers, project_id, start, end, revision):
        result = self.call(
            "POST",
            f"/v1/creation-projects/{project_id}/stage-transitions",
            headers,
            {
                "from_stage": start,
                "to_stage": end,
                "reason": "竞赛验收：完成当前创作任务",
                "expected_revision": revision,
            },
            expected=201,
        )
        assert result["current_stage"] == end
        return result["project_revision"]

    def run(self, student_phone: str, teacher_phone: str) -> dict:
        teacher = self.register(teacher_phone, "ADULT")
        student = self.register(student_phone, "AGE_14_TO_17")

        classroom = self.call(
            "POST", "/v1/classrooms", teacher, {"name": "机巧江湖竞赛演示班"}, expected=201
        )
        joined = self.call(
            "POST", "/v1/classrooms:join", student, {"join_code": classroom["join_code"]}
        )
        assert joined["id"] == classroom["id"] and joined["can_submit"] is True

        # 提出创意。
        project = self.call(
            "POST",
            "/v1/creation-projects",
            student,
            {
                "title": "会开花的节水机关",
                "description": "用荷花机关提醒大家按需取水",
                "media_type": "ILLUSTRATION",
                "default_visibility": "CLASSROOM",
            },
            expected=201,
        )
        project_id = project["id"]

        # 确认工法。
        method = self.call(
            "PUT",
            f"/v1/creation-projects/{project_id}/method",
            student,
            {
                "name": "观察—拆解—组合",
                "goal": "让同学一眼看懂节水机关怎样工作",
                "audience": ["同学", "老师"],
                "format": "图文机关说明",
                "steps": ["画出水流", "加入荷花机关", "标注操作方法"],
                "expected_revision": project["row_version"],
            },
        )
        revision = method["project_revision"]
        revision = self.transition(student, project_id, "IDEATION", "DRAFT", revision)

        # 保存首个真实版本。
        first_version = self.call(
            "POST",
            f"/v1/creation-projects/{project_id}/versions",
            {**student, "Idempotency-Key": f"version-{uuid.uuid4()}"},
            {
                "parent_version_id": None,
                "layers": [
                    {
                        "layer_id": "idea-title",
                        "kind": "TEXT",
                        "name": "创意说明",
                        "z_index": 0,
                        "text_content": "轻按荷叶，荷花展开并提示本次取水量",
                    }
                ],
                "canvas_width": 1024,
                "canvas_height": 1024,
                "change_summary": "保存首版草图说明",
            },
            expected=201,
        )
        current = self.call("GET", f"/v1/creation-projects/{project_id}", student)
        revision = self.transition(
            student, project_id, "DRAFT", "PRODUCTION", current["row_version"]
        )

        # 经过明确确认后生成图片，并等待本地比赛 worker 写入新版本。
        generation = self.call(
            "POST",
            f"/v1/creation-projects/{project_id}/image-generations",
            {**student, "Idempotency-Key": f"image-{uuid.uuid4()}"},
            {
                "parent_version_id": first_version["id"],
                "prompt": "儿童科普插画，荷花形节水机关，绿色与米色，结构清楚",
                "size": "SQUARE",
                "quality": "MEDIUM",
                "expected_project_revision": revision,
                "user_confirmed_generation": True,
            },
            expected=202,
        )
        for _ in range(100):
            generation = self.call("GET", f"/v1/image-generation-jobs/{generation['id']}", student)
            if generation["status"] in {"COMPLETED", "FAILED", "REJECTED"}:
                break
            time.sleep(0.1)
        assert generation["status"] == "COMPLETED", generation
        generated_version_id = generation["output_version_id"]
        assert generated_version_id
        media_path = urlparse(generation["output_asset"]["original_url"]).path
        media_response = self.client.get(media_path)
        self.events.append(
            {
                "method": "GET",
                "path": media_path,
                "status": media_response.status_code,
                "expected": 200,
            }
        )
        assert media_response.status_code == 200
        assert media_response.headers["content-type"].startswith("image/png")
        assert media_response.content.startswith(b"\x89PNG\r\n\x1a\n")

        # 创作教练先提出将读取什么，再由学生确认。
        coach = self.call(
            "POST",
            f"/v1/creation-projects/{project_id}/tool-calls",
            {**student, "Idempotency-Key": f"coach-{uuid.uuid4()}"},
            {"kind": "COACH_REVIEW", "prompt": "请检查画面是否能让同学看懂节水机关"},
            expected=201,
        )
        coach = self.call(
            "POST",
            f"/v1/creation-tool-calls/{coach['id']}/decision",
            {**student, "Idempotency-Key": f"coach-decision-{uuid.uuid4()}"},
            {"approve": True, "expected_revision": coach["row_version"]},
        )
        assert coach["status"] == "COMPLETED"

        current = self.call("GET", f"/v1/creation-projects/{project_id}", student)
        revision = self.transition(
            student, project_id, "PRODUCTION", "TEST", current["row_version"]
        )
        test_record = self.call(
            "POST",
            f"/v1/creation-projects/{project_id}/test-records",
            student,
            {
                "creation_version_id": generated_version_id,
                "scenario": "请同学不看说明说出怎样操作以及为什么能节水",
                "result": "PASSED",
                "notes": "同学能说出按荷叶取水和荷花提示水量",
                "findings": [],
            },
            expected=201,
        )
        assert test_record["result"] == "PASSED"
        revision = self.transition(student, project_id, "TEST", "SEAL", revision)

        prefix = f"/v1/creation-versions/{generated_version_id}"
        learning = self.call(
            "PUT",
            prefix + "/learning-card",
            student,
            {
                "manual_page_ids": [],
                "method_summary": "先画水流，再用荷花开合表现取水反馈",
                "unresolved_questions": [],
                "questions_confirmed": True,
            },
        )
        assert learning["status"] == "COMPLETE"
        generated_provenance = self.call(
            "GET", prefix + "/provenance-manifest", student
        )
        provenance = self.call(
            "PUT",
            prefix + "/provenance-manifest",
            student,
            {
                "human_contribution_summary": "本人提出主题、设计机关并完成文字说明",
                "ai_assistance_used": True,
                "ai_contribution_summary": "根据本人确认的描述生成一层辅助画面",
                "aigc_label_declared": True,
                "unresolved_rights": False,
                "row_version": generated_provenance["row_version"],
                "items": [
                    {
                        "item_type": "HUMAN_CONTRIBUTION",
                        "contribution_type": "构思与说明",
                        "description": "本人完成主题、机关逻辑和测试",
                        "license_type": "ORIGINAL",
                    },
                    {
                        "item_type": "AI_CONTRIBUTION",
                        "contribution_type": "辅助画面",
                        "description": "按本人确认的描述生成并由本人选择",
                        "license_type": "NOT_APPLICABLE",
                        "ai_provider": generation["provider_ref"],
                        "ai_model": generation["model_ref"],
                        "ai_tool_action": "生成辅助画面",
                        "prompt_summary": generation["prompt_summary"],
                        "output_asset_id": generation["output_asset"]["id"],
                        "user_modified": True,
                    },
                ],
            },
        )
        assert provenance["status"] == "COMPLETE"
        seal = self.call(
            "PUT",
            prefix + "/seal-check",
            student,
            {
                "work_description": "用荷花开合提示取水量的儿童节水机关图解",
                "learning_reflection": "学会了用动作变化表达看不见的水量",
                "next_improvement": "下一版会让水流箭头更简洁",
                "identity_privacy_confirmed": True,
                "contact_privacy_confirmed": True,
                "portrait_rights_confirmed": True,
            },
        )
        assert seal["status"] == "COMPLETE"

        submission = self.call(
            "POST",
            f"/v1/creation-projects/{project_id}/submissions",
            {**student, "Idempotency-Key": f"submit-{uuid.uuid4()}"},
            {
                "creation_version_id": generated_version_id,
                "visibility": "CLASSROOM",
                "target_classroom_id": classroom["id"],
            },
            expected=201,
        )
        assert submission["status"] == "PENDING_CHECK"
        assert submission["classroom_id"] == classroom["id"]

        # 留在已提交状态，后续由 Android UI 的“继续创作”触发正式版本接口。
        archived = self.call("GET", f"/v1/creation-projects/{project_id}", student)
        versions = self.call("GET", f"/v1/creation-projects/{project_id}/versions", student)
        source = next(item for item in versions["items"] if item["id"] == generated_version_id)
        assert archived["current_stage"] == "SEAL"
        assert archived["latest_publication"]["id"] == submission["id"]

        return {
            "project_id": project_id,
            "classroom_id": classroom["id"],
            "submitted_version_id": generated_version_id,
            "version_count_before_ui_continue": len(versions["items"]),
            "latest_version_number_before_ui_continue": source["version_number"],
            "publication_id": submission["id"],
        }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:8011")
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--student-phone", default="13990802001")
    parser.add_argument("--teacher-phone", default="13990802002")
    args = parser.parse_args()
    parsed = urlparse(args.base_url)
    if parsed.scheme != "http" or parsed.hostname not in {"127.0.0.1", "localhost"}:
        parser.error("Only the local disposable contest service is supported")
    with httpx.Client(base_url=args.base_url, timeout=30) as client:
        acceptance = CreationAcceptance(client)
        result = acceptance.run(args.student_phone, args.teacher_phone)
    evidence = {"result": result, "requests": acceptance.events}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(evidence, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"PASS: {len(acceptance.events)} HTTP checks; evidence: {args.output}")


if __name__ == "__main__":
    main()
