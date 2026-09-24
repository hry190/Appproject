"""Archive contracts: all pages, complete project-scoped history, and real deletion."""
from fastapi.testclient import TestClient

from test_creations_api import register, seeded_client  # noqa: F401


def test_archive_pages_preserve_media_types_and_deleted_works_disappear(seeded_client: TestClient):
    headers = register(seeded_client, "13990912011")
    expected = {}
    for index in range(23):
        media_type = ["ILLUSTRATION", "COMIC", "MIXED_MEDIA", "VIDEO"][index % 4]
        response = seeded_client.post("/v1/creation-projects", headers=headers, json={
            "title": "带有视频和小游戏字样的图文标题", "media_type": media_type,
        })
        assert response.status_code == 201, response.text
        expected[response.json()["id"]] = media_type

    first = seeded_client.get("/v1/me/creation-projects?limit=20", headers=headers).json()
    assert first["total"] == 23 and len(first["items"]) == 20
    second = seeded_client.get("/v1/me/creation-projects", headers=headers,
                               params={"cursor": first["next_cursor"], "limit": 20}).json()
    assert second["next_cursor"] is None and len(second["items"]) == 3
    assert {item["id"]: item["media_type"] for item in first["items"] + second["items"]} == expected

    deleted_id = second["items"][-1]["id"]
    response = seeded_client.delete(f"/v1/creation-projects/{deleted_id}", headers=headers)
    assert response.status_code == 204, response.text
    remaining = seeded_client.get("/v1/me/creation-projects?limit=50", headers=headers).json()
    assert remaining["total"] == 22
    assert deleted_id not in {item["id"] for item in remaining["items"]}


def test_archive_history_and_resume_preserve_every_message_of_only_the_selected_work(seeded_client: TestClient):
    headers = register(seeded_client, "13990912012")

    def start(key: str, idea: str):
        response = seeded_client.post("/v1/creation-conversations:start",
            headers={**headers, "Idempotency-Key": key}, json={"idea": idea})
        assert response.status_code == 201, response.text
        return response.json()

    conversation = start("archive-long-history", "画一只荷塘边的小熊猫")
    other = start("archive-other-history", "另外一件作品，只画竹林")
    project_id = conversation["project"]["id"]
    original_message_id = conversation["messages"][0]["id"]
    for index in range(15):
        response = seeded_client.post(f"/v1/creation-projects/{project_id}/conversation/messages",
            headers={**headers, "Idempotency-Key": f"archive-message-{index}"},
            json={"text": f"第 {index} 次修改，保留荷花旁的文字"})
        assert response.status_code == 201, response.text
        conversation = response.json()

    read = seeded_client.get(f"/v1/creation-projects/{project_id}/conversation", headers=headers).json()
    resumed = seeded_client.post(f"/v1/creation-projects/{project_id}/conversation:resume", headers=headers).json()
    assert len(read["messages"]) == 32
    assert read["messages"][0]["id"] == original_message_id
    assert read["messages"] == conversation["messages"] == resumed["messages"]
    assert all(item["project_id"] == project_id for item in read["messages"])
    assert not ({item["id"] for item in read["messages"]} & {item["id"] for item in other["messages"]})
    assert resumed["project"]["id"] == project_id
