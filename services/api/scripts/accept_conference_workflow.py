"""Local-only acceptance using HTTP contracts; never inserts business rows directly."""
from __future__ import annotations

import argparse
import json
from pathlib import Path
from urllib.parse import urlparse
import uuid

import httpx


class ConferenceAcceptance:
    def __init__(self, client, internal_token: str) -> None:
        self.client = client
        self.internal = {"X-Internal-Token": internal_token}
        self.events: list[dict] = []
        self.key = str(uuid.uuid4())

    def call(self, method, path, headers=None, body=None, expected=200, code=None):
        response = self.client.request(method, path, headers=headers, json=body)
        self.events.append({"method": method, "path": path, "status": response.status_code,
                            "expected": expected})
        assert response.status_code == expected, f"{method} {path}: {response.text}"
        data = response.json()
        if code:
            assert data["error"]["code"] == code, data
        return data

    def register(self, phone, age_band="ADULT"):
        self.call("POST", "/v1/auth/verification-codes", body={
            "phone": phone, "purpose": "REGISTER"}, expected=202)
        data = self.call("POST", "/v1/auth/register", body={
            "phone": phone, "verification_code": "123456", "password": "AcceptancePass8!",
            "age_band": age_band, "terms_version": "2026-08", "privacy_version": "2026-08",
        }, expected=201)
        return {"Authorization": f"Bearer {data['tokens']['access_token']}"}

    def version(self, author, project, parent=None):
        return self.call("POST", f"/v1/creation-projects/{project}/versions", author, {
            "parent_version_id": parent,
            "layers": [{"layer_id": "title", "kind": "TEXT", "name": "结构说明", "z_index": 0,
                        "text_content": "缩短传动轴，减少晃动" if parent else "机关结构示意"}],
            "canvas_width": 800, "canvas_height": 600,
            "change_summary": "根据评招改善结构" if parent else "完成首版结构图",
            "modification_reason": "采纳同门的结构建议" if parent else None,
        }, expected=201)

    def prepare(self, author_phone, reviewer_phone, age_band="ADULT", initialize_controls=False):
        author = self.register(author_phone, age_band)
        reviewer = self.register(reviewer_phone)
        project = self.call("POST", "/v1/creation-projects", author, {
            "title": "双账号验收·机关结构图", "media_type": "ILLUSTRATION",
            "default_visibility": "PRIVATE"}, expected=201)["id"]
        version = self.version(author, project)
        submission_path = f"/v1/creation-projects/{project}/submissions"
        submission_body = {"creation_version_id": version["id"], "visibility": "COMMUNITY"}
        submit_headers = {**author, "Idempotency-Key": self.key}
        self.call("POST", submission_path, submit_headers, submission_body, 409,
                  "SUBMISSION_STAGE_INVALID")
        current = self.call("GET", f"/v1/creation-projects/{project}", author)
        method = self.call("PUT", f"/v1/creation-projects/{project}/method", author, {
            "name": "结构图创作", "goal": "准确说明机关结构", "audience": ["同门"],
            "format": "图文", "steps": ["构思", "制作", "测试"],
            "expected_revision": current["row_version"],
        })
        revision = method["project_revision"]
        for start, end in [("IDEATION", "DRAFT"), ("DRAFT", "PRODUCTION"),
                           ("PRODUCTION", "TEST")]:
            revision = self.call("POST", f"/v1/creation-projects/{project}/stage-transitions",
                                 author, {"from_stage": start, "to_stage": end,
                                          "reason": "完成当前阶段验收",
                                          "expected_revision": revision}, 201)["project_revision"]
        self.call("POST", f"/v1/creation-projects/{project}/test-records", author, {
            "creation_version_id": version["id"], "scenario": "检查机关结构说明是否清楚",
            "result": "PASSED", "notes": "首版结构能被正确理解", "findings": [],
        }, 201)
        self.call("POST", f"/v1/creation-projects/{project}/stage-transitions", author, {
            "from_stage": "TEST", "to_stage": "SEAL", "reason": "测试通过并填写说明",
            "expected_revision": revision}, 201)
        self.call("POST", submission_path, submit_headers, submission_body, 409,
                  "SUBMISSION_INCOMPLETE")
        prefix = f"/v1/creation-versions/{version['id']}"
        self.call("PUT", prefix + "/learning-card", author, {
            "manual_page_ids": [], "method_summary": "先拆解结构再绘制示意图",
            "unresolved_questions": [], "questions_confirmed": True})
        self.call("PUT", prefix + "/provenance-manifest", author, {
            "human_contribution_summary": "本人构思并编写结构说明", "ai_assistance_used": False,
            "aigc_label_declared": False, "unresolved_rights": False,
            "items": [{"item_type": "HUMAN_CONTRIBUTION", "contribution_type": "构思",
                       "description": "本人构图", "license_type": "ORIGINAL"}]})
        self.call("PUT", prefix + "/seal-check", author, {
            "work_description": "介绍机关结构的图文作品", "learning_reflection": "学会拆分结构",
            "next_improvement": "根据反馈改善稳定性", "identity_privacy_confirmed": True,
            "contact_privacy_confirmed": True, "portrait_rights_confirmed": True})
        if age_band != "ADULT":
            if initialize_controls:
                self.call("GET", "/v1/settings/guardian-controls", author)
            self.call("POST", submission_path, submit_headers, submission_body, 403,
                      "GUARDIAN_VISIBILITY_RESTRICTED")
            return {"minor_restricted": True}
        publication = self.call("POST", submission_path, submit_headers, submission_body, 201)
        assert self.call("POST", submission_path, submit_headers, submission_body, 201) == publication
        pub = publication["id"]
        for suffix in ["learning-card", "provenance-manifest", "seal-check"]:
            assert self.call("GET", prefix + "/" + suffix, author)["status"] == "LOCKED"
        self.call("GET", f"/v1/conference/publications/{pub}", reviewer, expected=404)
        case = self.call("GET", f"/v1/publications/{pub}/moderation-case", author)
        routed = self.call("POST", f"/v1/internal/moderation-cases/{case['id']}/route", self.internal)
        self.call("POST", f"/v1/internal/moderation-cases/{case['id']}/decision", self.internal, {
            "decision": "PUBLISH", "risk_level": "LOW", "reviewer_reference": "local-acceptance",
            "row_version": routed["row_version"]})
        work = self.call("GET", f"/v1/conference/publications/{pub}", reviewer)
        assert work["version_number"] == 1 and not work["is_owner"]
        assert self.call("GET", f"/v1/conference/publications/{pub}", author)["is_owner"]
        return {"author": author, "reviewer": reviewer, "project": project,
                "publication": pub, "version": version}

    def complete(self, setup):
        author, reviewer = setup["author"], setup["reviewer"]
        pub, project, version = setup["publication"], setup["project"], setup["version"]
        path = f"/v1/conference/publications/{pub}/reviews"
        suggestion = {"template": "SUGGESTION", "content": "建议缩短传动轴以减少晃动。"}
        self.call("POST", path, author, suggestion, 403, "SELF_REVIEW_FORBIDDEN")
        review = self.call("POST", path, reviewer, suggestion, 201)
        decision = f"/v1/conference/reviews/{review['id']}/decision"
        adoption = f"/v1/conference/reviews/{review['id']}/adoption"
        self.call("POST", decision, reviewer, {"action": "ACCEPT", "row_version": 1}, 403)
        accepted = self.call("POST", decision, author, {"action": "ACCEPT", "row_version": 1})
        self.call("POST", adoption, author, {"creation_version_id": version["id"],
                  "summary": "仍指向参会旧版本", "row_version": accepted["row_version"]},
                  409, "INVALID_REVIEW_ADOPTION_VERSION")
        revised = self.version(author, project, version["id"])
        assert revised["version_number"] == 2
        assert self.call("GET", f"/v1/creation-projects/{project}", author)["current_stage"] == "PRODUCTION"
        self.call("POST", f"/v1/creation-projects/{project}/submissions",
                  {**author, "Idempotency-Key": self.key + "-v2"},
                  {"creation_version_id": revised["id"], "visibility": "COMMUNITY"},
                  409, "SUBMISSION_STAGE_INVALID")
        body = {"creation_version_id": revised["id"], "summary": "缩短传动轴并改善结构稳定性",
                "row_version": accepted["row_version"]}
        self.call("POST", adoption, reviewer, body, 403)
        linked = self.call("POST", adoption, author, body)
        assert linked["adopted_in_creation_version_id"] == revised["id"]
        replay = self.call("POST", adoption, author, body)
        assert replay == linked, {key: [linked[key], replay[key]] for key in linked if linked[key] != replay[key]}
        # Publishing V1 must not expose the unreviewed V2.
        assert self.call("GET", f"/v1/conference/publications/{pub}", reviewer)["creation_version_id"] == version["id"]
        versions = self.call("GET", f"/v1/creation-projects/{project}/versions", author)["items"]
        assert next(v for v in versions if v["id"] == version["id"])["layers"][0]["text_content"] == "机关结构示意"
        letters = self.call("GET", "/v1/conference/letters", reviewer)["items"]
        matches = [v for v in letters if v["title"] == "你的建议已形成新版本" and v["navigation_id"] == pub]
        assert len(matches) == 1 and matches[0]["navigation_target"] == "CONFERENCE_WORK"
        for headers, field in [(author, "reviews_received"), (reviewer, "reviews_authored")]:
            exported = self.call("GET", "/v1/account/export", headers)
            review_export = next(v for v in exported["conference_activity"][field] if v["id"] == review["id"])
            assert review_export["adopted_in_creation_version_id"] == revised["id"]
        return {"publication_id": pub, "project_id": project, "review_id": review["id"],
                "published_version": version["id"], "adopted_version": revised["id"]}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:8011")
    parser.add_argument("--prepare-only", action="store_true")
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--author-phone", default="13990801001")
    parser.add_argument("--reviewer-phone", default="13990801002")
    args = parser.parse_args()
    parsed = urlparse(args.base_url)
    if parsed.scheme != "http" or parsed.hostname not in {"127.0.0.1", "localhost"}:
        parser.error("Only a local disposable development server is supported")
    with httpx.Client(base_url=args.base_url, timeout=30) as client:
        run = ConferenceAcceptance(client, "dev-internal-worker-token-change-me-123456")
        setup = run.prepare(args.author_phone, args.reviewer_phone)
        result = {"project_id": setup["project"], "publication_id": setup["publication"]}
        if not args.prepare_only:
            result = run.complete(setup)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps({"mode": "prepare" if args.prepare_only else "complete",
                                          "result": result, "requests": run.events},
                                         ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"PASS: {len(run.events)} HTTP checks; evidence: {args.output}")


if __name__ == "__main__":
    main()
