from __future__ import annotations

import uuid

from fastapi import APIRouter, Depends, Header, Query, Response, status

from app.api.dependencies import (
    get_creation_service,
    get_creation_export_service,
    get_current_user,
    get_image_generation_service,
)
from app.domains.creations.contracts import (
    CreationChangeLogListPublic,
    CreationConversationGenerate,
    CreationConversationGenerationPublic,
    CreationConversationMessageCreate,
    CreationConversationPublic,
    CreationConversationResultAction,
    CreationConversationStart,
    CreationExportCreate,
    CreationExportJobListPublic,
    CreationExportJobPublic,
    CreationIntentAnalysisPublic,
    CreationIntentAnalyze,
    CreationMethodPublic,
    CreationMethodPut,
    CreationProjectCreate,
    CreationProjectListPublic,
    CreationProjectPatch,
    CreationProjectPublic,
    CreationSealCheckPublic,
    CreationSealCheckPut,
    CreationStageEventListPublic,
    CreationStageTransition,
    CreationStageTransitionPublic,
    CreationSubmissionCreate,
    ConferenceCategorySuggestionListPublic,
    CreationTestIssuePublic,
    CreationTestIssueResolve,
    CreationTestRecordCreate,
    CreationTestRecordListPublic,
    CreationTestRecordPublic,
    CreationToolCallDecision,
    CreationToolCallListPublic,
    CreationToolCallPropose,
    CreationToolCallPublic,
    CreationVersionCreate,
    CreationVersionDiffPublic,
    CreationVersionListPublic,
    CreationVersionPublic,
    LearningCardPublic,
    LearningCardPut,
    ProvenanceManifestPublic,
    ProvenanceManifestPut,
    PublicationPublic,
    ImageGenerationCreate,
    ImageGenerationJobListPublic,
    ImageGenerationJobPublic,
    ImageGenerationRetry,
)
from app.domains.creations.image_generation_service import ImageGenerationService
from app.domains.creations.export_service import CreationExportService
from app.domains.creations.models import CreationProjectStatus
from app.domains.creations.service import CreationService
from app.models import User


router = APIRouter(prefix="/v1", tags=["creations"])


@router.post(
    "/creation-projects",
    response_model=CreationProjectPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_project(
    payload: CreationProjectCreate,
    idempotency_key: str | None = Header(
        default=None,
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationProjectPublic:
    return service.create_project(user, payload, idempotency_key)


@router.post(
    "/creation-intents:analyze",
    response_model=CreationIntentAnalysisPublic,
    status_code=status.HTTP_201_CREATED,
)
def analyze_creation_intent(
    payload: CreationIntentAnalyze,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationIntentAnalysisPublic:
    return service.analyze_intent(user, payload)


@router.post(
    "/creation-conversations:start",
    response_model=CreationConversationPublic,
    status_code=status.HTTP_201_CREATED,
)
def start_creation_conversation(
    payload: CreationConversationStart,
    idempotency_key: str = Header(
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationConversationPublic:
    return service.start_conversation(user, payload, idempotency_key)


@router.get("/me/creation-projects", response_model=CreationProjectListPublic)
def list_projects(
    project_status: CreationProjectStatus | None = Query(default=None, alias="status"),
    cursor: str | None = Query(default=None, max_length=120),
    limit: int = Query(default=20, ge=1, le=50),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationProjectListPublic:
    return service.list_projects(
        user,
        status=project_status,
        cursor=cursor,
        limit=limit,
    )


@router.get(
    "/creation-projects/{project_id}", response_model=CreationProjectPublic
)
def get_project(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationProjectPublic:
    return service.get_project(user, project_id)


@router.get(
    "/creation-projects/{project_id}/conversation",
    response_model=CreationConversationPublic,
)
def get_creation_conversation(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationConversationPublic:
    return service.get_conversation(user, project_id)


@router.post(
    "/creation-projects/{project_id}/conversation:resume",
    response_model=CreationConversationPublic,
)
def resume_creation_conversation(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationConversationPublic:
    return service.resume_conversation(user, project_id)


@router.post(
    "/creation-projects/{project_id}/conversation/messages",
    response_model=CreationConversationPublic,
    status_code=status.HTTP_201_CREATED,
)
def add_creation_conversation_message(
    project_id: uuid.UUID,
    payload: CreationConversationMessageCreate,
    idempotency_key: str = Header(
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationConversationPublic:
    return service.add_conversation_message(
        user, project_id, payload, idempotency_key
    )


@router.post(
    "/creation-projects/{project_id}/conversation/suggestions/{message_id}:accept",
    response_model=CreationConversationPublic,
)
def accept_creation_conversation_suggestion(
    project_id: uuid.UUID,
    message_id: uuid.UUID,
    payload: CreationConversationResultAction,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationConversationPublic:
    return service.accept_conversation_suggestion(
        user, project_id, message_id, payload.expected_revision
    )


@router.post(
    "/creation-projects/{project_id}/conversation:return",
    response_model=CreationConversationPublic,
)
def return_creation_conversation(
    project_id: uuid.UUID,
    payload: CreationConversationResultAction,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationConversationPublic:
    return service.return_conversation_to_dialogue(user, project_id, payload)


@router.post(
    "/creation-projects/{project_id}/conversation:save-result",
    response_model=CreationConversationPublic,
)
def save_creation_conversation_result(
    project_id: uuid.UUID,
    payload: CreationConversationResultAction,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationConversationPublic:
    return service.save_conversation_result(user, project_id, payload)


@router.post(
    "/creation-projects/{project_id}/conversation:generate",
    response_model=CreationConversationGenerationPublic,
    status_code=status.HTTP_202_ACCEPTED,
)
def generate_from_creation_conversation(
    project_id: uuid.UUID,
    payload: CreationConversationGenerate,
    idempotency_key: str = Header(
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
    generation_service: ImageGenerationService = Depends(get_image_generation_service),
) -> CreationConversationGenerationPublic:
    draft, private_prompt, project, has_job = service.prepare_conversation_generation(
        user, project_id, payload, idempotency_key
    )
    conversation = service.get_conversation(user, project_id)
    if has_job and conversation.active_generation_job_id is not None:
        job = generation_service.get_job(user, conversation.active_generation_job_id)
    else:
        try:
            job = generation_service.create_job(
                user,
                project_id,
                ImageGenerationCreate(
                    parent_version_id=draft.id,
                    prompt=private_prompt,
                    size="PORTRAIT",
                    quality="MEDIUM",
                    expected_project_revision=project.row_version,
                    user_confirmed_generation=True,
                ),
                f"conversation:{idempotency_key}",
            )
            conversation = service.attach_conversation_generation(
                user, project_id, job.id
            )
        except Exception:
            service.mark_conversation_generation_failed(user, project_id)
            raise
    return CreationConversationGenerationPublic(
        conversation=conversation,
        generation=job,
    )


@router.patch(
    "/creation-projects/{project_id}", response_model=CreationProjectPublic
)
def update_project(
    project_id: uuid.UUID,
    payload: CreationProjectPatch,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationProjectPublic:
    return service.update_project(user, project_id, payload)


@router.get(
    "/creation-projects/{project_id}/method",
    response_model=CreationMethodPublic,
)
def get_creation_method(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationMethodPublic:
    return service.get_current_method(user, project_id)


@router.put(
    "/creation-projects/{project_id}/method",
    response_model=CreationMethodPublic,
)
def put_creation_method(
    project_id: uuid.UUID,
    payload: CreationMethodPut,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationMethodPublic:
    return service.put_method(user, project_id, payload)


@router.post(
    "/creation-projects/{project_id}/stage-transitions",
    response_model=CreationStageTransitionPublic,
    status_code=status.HTTP_201_CREATED,
)
def transition_creation_stage(
    project_id: uuid.UUID,
    payload: CreationStageTransition,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationStageTransitionPublic:
    return service.transition_stage(user, project_id, payload)


@router.get(
    "/creation-projects/{project_id}/stage-events",
    response_model=CreationStageEventListPublic,
)
def list_creation_stage_events(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationStageEventListPublic:
    return service.list_stage_events(user, project_id)


@router.post(
    "/creation-projects/{project_id}/tool-calls",
    response_model=CreationToolCallPublic,
    status_code=status.HTTP_201_CREATED,
)
def propose_creation_tool_call(
    project_id: uuid.UUID,
    payload: CreationToolCallPropose,
    idempotency_key: str = Header(
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationToolCallPublic:
    return service.propose_tool_call(user, project_id, payload, idempotency_key)


@router.get(
    "/creation-projects/{project_id}/tool-calls",
    response_model=CreationToolCallListPublic,
)
def list_creation_tool_calls(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationToolCallListPublic:
    return service.list_tool_calls(user, project_id)


@router.post(
    "/creation-tool-calls/{tool_call_id}/decision",
    response_model=CreationToolCallPublic,
)
def decide_creation_tool_call(
    tool_call_id: uuid.UUID,
    payload: CreationToolCallDecision,
    idempotency_key: str = Header(
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationToolCallPublic:
    return service.decide_tool_call(user, tool_call_id, payload, idempotency_key)


@router.post(
    "/creation-projects/{project_id}/test-records",
    response_model=CreationTestRecordPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_creation_test_record(
    project_id: uuid.UUID,
    payload: CreationTestRecordCreate,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationTestRecordPublic:
    return service.create_test_record(user, project_id, payload)


@router.get(
    "/creation-projects/{project_id}/test-records",
    response_model=CreationTestRecordListPublic,
)
def list_creation_test_records(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationTestRecordListPublic:
    return service.list_test_records(user, project_id)


@router.post(
    "/creation-test-issues/{issue_id}/resolve",
    response_model=CreationTestIssuePublic,
)
def resolve_creation_test_issue(
    issue_id: uuid.UUID,
    payload: CreationTestIssueResolve,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationTestIssuePublic:
    return service.resolve_test_issue(user, issue_id, payload)


@router.delete(
    "/creation-projects/{project_id}",
    status_code=status.HTTP_204_NO_CONTENT,
)
def delete_project(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> Response:
    service.delete_project(user, project_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/creation-projects/{project_id}/versions",
    response_model=CreationVersionPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_version(
    project_id: uuid.UUID,
    payload: CreationVersionCreate,
    idempotency_key: str | None = Header(
        default=None,
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationVersionPublic:
    return service.create_version(user, project_id, payload, idempotency_key)


@router.post(
    "/creation-projects/{project_id}/image-generations",
    response_model=ImageGenerationJobPublic,
    status_code=status.HTTP_202_ACCEPTED,
)
def create_image_generation(
    project_id: uuid.UUID,
    payload: ImageGenerationCreate,
    idempotency_key: str = Header(
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: ImageGenerationService = Depends(get_image_generation_service),
) -> ImageGenerationJobPublic:
    return service.create_job(user, project_id, payload, idempotency_key)


@router.get(
    "/creation-projects/{project_id}/image-generations",
    response_model=ImageGenerationJobListPublic,
)
def list_image_generations(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: ImageGenerationService = Depends(get_image_generation_service),
) -> ImageGenerationJobListPublic:
    return service.list_jobs(user, project_id)


@router.get(
    "/image-generation-jobs/{job_id}",
    response_model=ImageGenerationJobPublic,
)
def get_image_generation(
    job_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: ImageGenerationService = Depends(get_image_generation_service),
) -> ImageGenerationJobPublic:
    return service.get_job(user, job_id)


@router.post(
    "/image-generation-jobs/{job_id}/retry",
    response_model=ImageGenerationJobPublic,
    status_code=status.HTTP_202_ACCEPTED,
)
def retry_image_generation(
    job_id: uuid.UUID,
    payload: ImageGenerationRetry,
    idempotency_key: str = Header(
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: ImageGenerationService = Depends(get_image_generation_service),
) -> ImageGenerationJobPublic:
    return service.retry_job(user, job_id, payload, idempotency_key)


@router.get(
    "/creation-projects/{project_id}/versions",
    response_model=CreationVersionListPublic,
)
def list_versions(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationVersionListPublic:
    return service.list_versions(user, project_id)


@router.get(
    "/creation-projects/{project_id}/change-logs",
    response_model=CreationChangeLogListPublic,
)
def list_change_logs(
    project_id: uuid.UUID,
    limit: int = Query(default=50, ge=1, le=100),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationChangeLogListPublic:
    return service.list_change_logs(user, project_id, limit=limit)


@router.get("/creation-versions/{version_id}", response_model=CreationVersionPublic)
def get_version(
    version_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationVersionPublic:
    return service.get_version(user, version_id)


@router.get(
    "/creation-versions/{version_id}/diff",
    response_model=CreationVersionDiffPublic,
)
def compare_versions(
    version_id: uuid.UUID,
    base_version_id: uuid.UUID | None = Query(default=None),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationVersionDiffPublic:
    return service.compare_versions(user, version_id, base_version_id)


@router.post(
    "/creation-versions/{version_id}/exports",
    response_model=CreationExportJobPublic,
    status_code=status.HTTP_202_ACCEPTED,
)
def create_creation_export(
    version_id: uuid.UUID,
    payload: CreationExportCreate,
    idempotency_key: str = Header(
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: CreationExportService = Depends(get_creation_export_service),
) -> CreationExportJobPublic:
    return service.create_job(user, version_id, payload, idempotency_key)


@router.get(
    "/creation-versions/{version_id}/exports",
    response_model=CreationExportJobListPublic,
)
def list_creation_exports(
    version_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationExportService = Depends(get_creation_export_service),
) -> CreationExportJobListPublic:
    return service.list_jobs(user, version_id)


@router.get(
    "/creation-export-jobs/{job_id}",
    response_model=CreationExportJobPublic,
)
def get_creation_export(
    job_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationExportService = Depends(get_creation_export_service),
) -> CreationExportJobPublic:
    return service.get_job(user, job_id)


@router.put(
    "/creation-versions/{version_id}/learning-card",
    response_model=LearningCardPublic,
)
def put_learning_card(
    version_id: uuid.UUID,
    payload: LearningCardPut,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> LearningCardPublic:
    return service.put_learning_card(user, version_id, payload)


@router.get(
    "/creation-versions/{version_id}/learning-card",
    response_model=LearningCardPublic,
)
def get_learning_card(
    version_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> LearningCardPublic:
    return service.get_learning_card(user, version_id)


@router.put(
    "/creation-versions/{version_id}/seal-check",
    response_model=CreationSealCheckPublic,
)
def put_seal_check(
    version_id: uuid.UUID,
    payload: CreationSealCheckPut,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationSealCheckPublic:
    return service.put_seal_check(user, version_id, payload)


@router.get(
    "/creation-versions/{version_id}/seal-check",
    response_model=CreationSealCheckPublic,
)
def get_seal_check(
    version_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationSealCheckPublic:
    return service.get_seal_check(user, version_id)


@router.put(
    "/creation-versions/{version_id}/provenance-manifest",
    response_model=ProvenanceManifestPublic,
)
def put_provenance_manifest(
    version_id: uuid.UUID,
    payload: ProvenanceManifestPut,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> ProvenanceManifestPublic:
    return service.put_provenance_manifest(user, version_id, payload)


@router.get(
    "/creation-versions/{version_id}/provenance-manifest",
    response_model=ProvenanceManifestPublic,
)
def get_provenance_manifest(
    version_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> ProvenanceManifestPublic:
    return service.get_provenance_manifest(user, version_id)


@router.post(
    "/creation-projects/{project_id}/submissions",
    response_model=PublicationPublic,
    status_code=status.HTTP_201_CREATED,
)
def submit_creation(
    project_id: uuid.UUID,
    payload: CreationSubmissionCreate,
    idempotency_key: str = Header(
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> PublicationPublic:
    return service.submit(user, project_id, payload, idempotency_key)


@router.get(
    "/creation-projects/{project_id}/conference-category-suggestions",
    response_model=ConferenceCategorySuggestionListPublic,
)
def suggest_conference_categories(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> ConferenceCategorySuggestionListPublic:
    return service.suggest_conference_categories(user, project_id)
