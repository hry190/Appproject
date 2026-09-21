"""Accept the conversation-driven creation flow through public HTTP APIs only."""
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

    def register(self, phone: str) -> dict[str, str]:
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
                "age_band": "AGE_14_TO_17",
                "terms_version": "2026-08",
                "privacy_version": "2026-08",
            },
            expected=201,
        )
        return {"Authorization": f"Bearer {result['tokens']['access_token']}"}

    def wait_for_image(self, headers: dict[str, str], job: dict) -> dict:
        for _ in range(120):
            job = self.call("GET", f"/v1/image-generation-jobs/{job['id']}", headers)
            if job["status"] in {"COMPLETED", "FAILED", "REJECTED"}:
                break
            time.sleep(0.1)
        assert job["status"] == "COMPLETED", job
        assert job["output_version_id"] and job["output_asset"]["original_url"]
        media_path = urlparse(job["output_asset"]["original_url"]).path
        response = self.client.get(media_path)
        self.events.append(
            {"method": "GET", "path": "signed generated image", "status": response.status_code, "expected": 200}
        )
        assert response.status_code == 200
        assert response.headers["content-type"].startswith("image/")
        return job

    def run(self, student_phone: str) -> dict:
        student = self.register(student_phone)

        # 图一：只提交一个想法；后端同时记录来源与第一条教练分析。
        start_key = f"conversation-start-{uuid.uuid4()}"
        conversation = self.call(
            "POST",
            "/v1/creation-conversations:start",
            {**student, "Idempotency-Key": start_key},
            {"idea": "画一只在荷塘修理木鸟的小熊猫"},
            expected=201,
        )
        project_id = conversation["project"]["id"]
        assert [item["role"] for item in conversation["messages"]] == ["STUDENT", "COACH"]
        assert conversation["messages"][-1]["decision"] == "PENDING"

        replay = self.call(
            "POST",
            "/v1/creation-conversations:start",
            {**student, "Idempotency-Key": start_key},
            {"idea": "画一只在荷塘修理木鸟的小熊猫"},
            expected=201,
        )
        assert replay["project"]["id"] == project_id

        # 图三：拒绝无需额外按钮，直接发新要求；旧建议会被真实标记为已替代。
        conversation = self.call(
            "POST",
            f"/v1/creation-projects/{project_id}/conversation/messages",
            {**student, "Idempotency-Key": f"message-{uuid.uuid4()}"},
            {"text": "背景改成傍晚，木鸟要更可爱"},
            expected=201,
        )
        assert any(item["decision"] == "REPLACED" for item in conversation["messages"])
        suggestion = conversation["messages"][-1]
        conversation = self.call(
            "POST",
            f"/v1/creation-projects/{project_id}/conversation/suggestions/{suggestion['id']}:accept",
            student,
            {"expected_revision": conversation["row_version"]},
        )
        assert any(item["decision"] == "ACCEPTED" for item in conversation["messages"])

        # 保存上传草稿会由服务端整理内部方案并发起真实生成，不向客户端回传内部提示词。
        queued = self.call(
            "POST",
            f"/v1/creation-projects/{project_id}/conversation:generate",
            {**student, "Idempotency-Key": f"generate-{uuid.uuid4()}"},
            {"expected_revision": conversation["row_version"], "user_confirmed_generation": True},
            expected=202,
        )
        assert queued["generation"]["prompt_summary"] == "创作方案已由教练整理"
        first_job = self.wait_for_image(student, queued["generation"])
        conversation = self.call(
            "GET", f"/v1/creation-projects/{project_id}/conversation", student
        )
        assert conversation["status"] == "RESULT_READY"
        project = self.call("GET", f"/v1/creation-projects/{project_id}", student)
        assert project["current_stage"] == "SEAL"
        tests = self.call("GET", f"/v1/creation-projects/{project_id}/test-records", student)
        assert tests["items"][0]["result"] == "PASSED"

        # 不满意时回到同一对话修改，并再次经过完整生成与检查。
        conversation = self.call(
            "POST",
            f"/v1/creation-projects/{project_id}/conversation:return",
            student,
            {"expected_revision": conversation["row_version"]},
        )
        conversation = self.call(
            "POST",
            f"/v1/creation-projects/{project_id}/conversation/messages",
            {**student, "Idempotency-Key": f"message-{uuid.uuid4()}"},
            {"text": "天空改成浅紫色，保留傍晚的感觉"},
            expected=201,
        )
        queued = self.call(
            "POST",
            f"/v1/creation-projects/{project_id}/conversation:generate",
            {**student, "Idempotency-Key": f"generate-{uuid.uuid4()}"},
            {"expected_revision": conversation["row_version"], "user_confirmed_generation": True},
            expected=202,
        )
        final_job = self.wait_for_image(student, queued["generation"])
        conversation = self.call(
            "GET", f"/v1/creation-projects/{project_id}/conversation", student
        )
        saved = self.call(
            "POST",
            f"/v1/creation-projects/{project_id}/conversation:save-result",
            student,
            {"expected_revision": conversation["row_version"]},
        )
        assert saved["status"] == "SAVED"
        assert saved["result_version_id"] in saved["saved_version_ids"]

        # 创作档案“继续创作”使用同一恢复接口，消息、草稿和结果都必须还在。
        resumed = self.call(
            "POST", f"/v1/creation-projects/{project_id}/conversation:resume", student
        )
        assert resumed["messages"] == saved["messages"]
        assert resumed["saved_version_ids"] == saved["saved_version_ids"]
        versions = self.call("GET", f"/v1/creation-projects/{project_id}/versions", student)
        version_ids = {item["id"] for item in versions["items"]}
        assert set(resumed["draft_version_ids"]).issubset(version_ids)
        assert set(resumed["saved_version_ids"]).issubset(version_ids)

        return {
            "project_id": project_id,
            "first_result_version_id": first_job["output_version_id"],
            "saved_result_version_id": final_job["output_version_id"],
            "conversation_message_count": len(resumed["messages"]),
            "draft_count": len(resumed["draft_version_ids"]),
            "saved_count": len(resumed["saved_version_ids"]),
        }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:8011")
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--student-phone", default="13990802001")
    parser.add_argument("--teacher-phone", default="13990802002", help=argparse.SUPPRESS)
    args = parser.parse_args()
    parsed = urlparse(args.base_url)
    if parsed.scheme != "http" or parsed.hostname not in {"127.0.0.1", "localhost"}:
        parser.error("Only the local disposable contest service is supported")
    with httpx.Client(base_url=args.base_url, timeout=30) as client:
        acceptance = CreationAcceptance(client)
        result = acceptance.run(args.student_phone)
    evidence = {"result": result, "requests": acceptance.events}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(evidence, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"PASS: {len(acceptance.events)} HTTP checks; evidence: {args.output}")


if __name__ == "__main__":
    main()
