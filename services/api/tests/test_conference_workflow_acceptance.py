from fastapi.testclient import TestClient
import pytest

from scripts.accept_conference_workflow import ConferenceAcceptance


def test_creation_publication_feedback_revision_uses_only_http(client: TestClient):
    acceptance = ConferenceAcceptance(client, "dev-internal-worker-token-change-me-123456")
    setup = acceptance.prepare("13990801001", "13990801002")
    result = acceptance.complete(setup)
    assert result["published_version"] != result["adopted_version"]


@pytest.mark.parametrize("initialize_controls", [False, True])
def test_minor_community_submission_respects_guardian_controls(client: TestClient, initialize_controls):
    acceptance = ConferenceAcceptance(client, "dev-internal-worker-token-change-me-123456")
    assert acceptance.prepare("13990802001", "13990802002", "AGE_14_TO_17",
                              initialize_controls=initialize_controls)["minor_restricted"]
