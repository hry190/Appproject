package com.jueqiao.jianghu.luggage

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement
import com.google.gson.JsonObject

data class SignedMediaDto(
    @SerializedName("asset_id") val assetId: String,
    val url: String,
    @SerializedName("expires_at") val expiresAt: String,
)

data class CurrentTitleDto(val code: String, val name: String)

data class BadgeDto(
    val id: String? = null,
    val code: String,
    val name: String,
    val description: String? = null,
    @SerializedName("earned_at") val earnedAt: String,
)

data class LuggageProfileDto(
    val nickname: String,
    val avatar: SignedMediaDto?,
    @SerializedName("age_band") val ageBand: String,
    @SerializedName("class_label") val classLabel: String?,
    @SerializedName("anonymous_id") val anonymousId: String,
    @SerializedName("current_title") val currentTitle: CurrentTitleDto?,
    val badges: List<BadgeDto>,
)

data class LearningWeekDto(
    val timezone: String,
    @SerializedName("starts_at") val startsAt: String,
    @SerializedName("ends_at_exclusive") val endsAtExclusive: String,
    @SerializedName("practice_count") val practiceCount: Int,
)

data class EvidenceCounterDto(
    val count: Int,
    @SerializedName("latest_at") val latestAt: String?,
    @SerializedName("display_summary") val displaySummary: String,
)

data class EvidenceCountersDto(
    val wisdom: EvidenceCounterDto,
    val craft: EvidenceCounterDto,
    val chivalry: EvidenceCounterDto,
)

data class LearningStatsDto(
    val week: LearningWeekDto,
    @SerializedName("lifetime_practice_count") val lifetimePracticeCount: Int,
    @SerializedName("lifetime_practice_days") val lifetimePracticeDays: Int,
    @SerializedName("distinct_trials_passed") val distinctTrialsPassed: Int,
    val evidence: EvidenceCountersDto,
)

data class LuggageManualDto(
    val id: String,
    val volume: Int,
    @SerializedName("style_no") val styleNo: Int,
    val title: String,
    val state: String,
    @SerializedName("state_label") val stateLabel: String,
    @SerializedName("latest_evidence_summary") val latestEvidenceSummary: String?,
    @SerializedName("updated_at") val updatedAt: String?,
)

data class LuggageManualSectionDto(
    val total: Int,
    val obtained: Int,
    @SerializedName("counts_by_state") val countsByState: Map<String, Int>,
    val items: List<LuggageManualDto>,
    @SerializedName("empty_reason") val emptyReason: String?,
    @SerializedName("detail_url") val detailUrl: String,
)

data class LuggageMistakeDto(
    val id: String,
    @SerializedName("knowledge_point") val knowledgePoint: String,
    val status: String,
    @SerializedName("manual_page_id") val manualPageId: String,
    @SerializedName("retry_url") val retryUrl: String?,
)

data class LuggageMistakeSectionDto(
    @SerializedName("pending_count") val pendingCount: Int,
    val items: List<LuggageMistakeDto>,
    @SerializedName("empty_reason") val emptyReason: String?,
    @SerializedName("detail_url") val detailUrl: String,
)

data class LuggageCreationDto(
    @SerializedName("project_id") val projectId: String,
    val title: String,
    @SerializedName("display_status") val displayStatus: String,
    @SerializedName("current_version") val currentVersion: Int,
    val thumbnail: SignedMediaDto?,
    @SerializedName("can_revise") val canRevise: Boolean,
    @SerializedName("return_reason") val returnReason: String?,
    @SerializedName("updated_at") val updatedAt: String,
)

data class LuggageCreationSectionDto(
    @SerializedName("counts_by_status") val countsByStatus: Map<String, Int>,
    val items: List<LuggageCreationDto>,
    @SerializedName("empty_reason") val emptyReason: String?,
    @SerializedName("detail_url") val detailUrl: String,
) {
    val total: Int get() = countsByStatus.values.sum()
}

data class LuggagePrivacyDto(
    @SerializedName("guardian_controls_active") val guardianControlsActive: Boolean,
    @SerializedName("pending_appeal_count") val pendingAppealCount: Int,
    @SerializedName("privacy_settings_url") val privacySettingsUrl: String,
)

data class LuggageDataDto(
    val profile: LuggageProfileDto,
    val stats: LearningStatsDto,
    val manuals: LuggageManualSectionDto,
    val mistakes: LuggageMistakeSectionDto,
    val creations: LuggageCreationSectionDto,
    val privacy: LuggagePrivacyDto,
)

data class LuggageMetaDto(
    @SerializedName("generated_at") val generatedAt: String,
    @SerializedName("snapshot_version") val snapshotVersion: Long,
    val etag: String,
)

data class LuggageResponseDto(val data: LuggageDataDto, val meta: LuggageMetaDto)

sealed interface LuggageHttpResult {
    data class Fresh(val body: LuggageResponseDto, val etag: String) : LuggageHttpResult
    data object NotModified : LuggageHttpResult
}

data class EvidenceItemDto(
    val id: String,
    val category: String,
    @SerializedName("evidence_type") val evidenceType: String,
    @SerializedName("manual_page_id") val manualPageId: String?,
    @SerializedName("manual_title") val manualTitle: String?,
    val summary: String,
    @SerializedName("validation_status") val validationStatus: String,
    @SerializedName("created_at") val createdAt: String,
)

data class EvidenceListDto(
    val total: Int,
    val items: List<EvidenceItemDto>,
    @SerializedName("next_cursor") val nextCursor: String?,
)

data class ManualPageDto(
    val id: String,
    @SerializedName("page_no") val pageNo: Int,
    @SerializedName("style_no") val styleNo: Int,
    val title: String,
    @SerializedName("volume_no") val volumeNo: Int,
    @SerializedName("volume_title") val volumeTitle: String,
    @SerializedName("core_logic") val coreLogic: String,
    @SerializedName("progress_state") val progressState: String,
    @SerializedName("progress_label") val progressLabel: String,
    @SerializedName("is_favorite") val isFavorite: Boolean,
)

data class ManualPageListDto(
    val total: Int,
    val items: List<ManualPageDto>,
    @SerializedName("next_cursor") val nextCursor: String?,
)

data class ManualProgressRequirementDto(
    val state: String,
    val label: String,
    val requirement: String,
)

data class ManualDetailDto(
    val id: String,
    @SerializedName("page_no") val pageNo: Int,
    @SerializedName("style_no") val styleNo: Int,
    val title: String,
    @SerializedName("volume_no") val volumeNo: Int,
    @SerializedName("volume_title") val volumeTitle: String,
    @SerializedName("core_logic") val coreLogic: String,
    @SerializedName("life_hook") val lifeHook: String,
    @SerializedName("interaction_evidence") val interactionEvidence: String,
    @SerializedName("progress_state") val progressState: String,
    @SerializedName("progress_label") val progressLabel: String,
    @SerializedName("is_favorite") val isFavorite: Boolean,
    @SerializedName("progress_requirements") val progressRequirements: List<ManualProgressRequirementDto>,
    val evidence: List<EvidenceItemDto>,
)

data class ManualProgressTransitionDto(
    @SerializedName("previous_state") val previousState: String,
    @SerializedName("current_state") val currentState: String,
    @SerializedName("trigger_event") val triggerEvent: String,
    @SerializedName("rule_version") val ruleVersion: String,
    @SerializedName("evidence_summary") val evidenceSummary: String?,
    @SerializedName("occurred_at") val occurredAt: String,
)

data class ManualLearningHistoryDto(
    @SerializedName("manual_page_id") val manualPageId: String,
    @SerializedName("current_state") val currentState: String,
    @SerializedName("discovered_at") val discoveredAt: String?,
    @SerializedName("learned_at") val learnedAt: String?,
    @SerializedName("mastered_at") val masteredAt: String?,
    @SerializedName("teaching_at") val teachingAt: String?,
    val transitions: List<ManualProgressTransitionDto>,
    val evidence: List<EvidenceItemDto>,
)

data class ManualDetailBundle(
    val manual: ManualDetailDto,
    val history: ManualLearningHistoryDto,
    val evidence: List<EvidenceItemDto>,
)

data class MistakeItemDto(
    val id: String,
    @SerializedName("trial_id") val trialId: String,
    @SerializedName("manual_page_id") val manualPageId: String,
    @SerializedName("manual_title") val manualTitle: String,
    @SerializedName("knowledge_point_code") val knowledgePointCode: String,
    @SerializedName("error_reason_summary") val errorReasonSummary: String,
    val status: String,
    @SerializedName("failure_count") val failureCount: Int,
    @SerializedName("successful_retries") val successfulRetries: Int,
    @SerializedName("next_review_at") val nextReviewAt: String?,
    @SerializedName("retry_url") val retryUrl: String,
)

data class MistakeListDto(
    val total: Int,
    val items: List<MistakeItemDto>,
    @SerializedName("next_cursor") val nextCursor: String?,
)

data class RemediationRecordDto(
    val id: String,
    @SerializedName("attempt_id") val attemptId: String,
    val result: String,
    val reflection: String?,
    @SerializedName("occurred_at") val occurredAt: String,
)

data class MistakeDetailDto(
    val id: String,
    @SerializedName("trial_id") val trialId: String,
    @SerializedName("manual_page_id") val manualPageId: String,
    @SerializedName("manual_title") val manualTitle: String,
    @SerializedName("knowledge_point_code") val knowledgePointCode: String,
    @SerializedName("error_reason_code") val errorReasonCode: String,
    @SerializedName("error_reason_summary") val errorReasonSummary: String,
    val status: String,
    @SerializedName("failure_count") val failureCount: Int,
    @SerializedName("successful_retries") val successfulRetries: Int,
    @SerializedName("next_review_at") val nextReviewAt: String?,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("retry_url") val retryUrl: String,
    @SerializedName("first_attempt_id") val firstAttemptId: String,
    @SerializedName("latest_attempt_id") val latestAttemptId: String,
    @SerializedName("original_answer_payload") val originalAnswerPayload: JsonElement,
    @SerializedName("consolidated_at") val consolidatedAt: String?,
    @SerializedName("remediation_records") val remediationRecords: List<RemediationRecordDto>,
)

data class RetrySessionDto(
    val id: String,
    @SerializedName("mistake_id") val mistakeId: String,
    @SerializedName("trial_id") val trialId: String,
    @SerializedName("trial_version_id") val trialVersionId: String,
    @SerializedName("expires_at") val expiresAt: String,
    @SerializedName("submit_url") val submitUrl: String,
)

data class TrialVersionDto(
    val id: String,
    val version: Int,
    val prompt: String,
    @SerializedName("prediction_prompt") val predictionPrompt: String,
    @SerializedName("answer_schema") val answerSchema: JsonObject,
    @SerializedName("prediction_required") val predictionRequired: Boolean,
    @SerializedName("explanation_required") val explanationRequired: Boolean,
    @SerializedName("min_explanation_length") val minExplanationLength: Int,
)

data class TrialDto(
    val id: String,
    val title: String,
    @SerializedName("knowledge_point_code") val knowledgePointCode: String,
    @SerializedName("manual_page_id") val manualPageId: String,
    @SerializedName("current_version") val currentVersion: TrialVersionDto,
)

data class TrialAttemptRequestDto(
    @SerializedName("trial_version_id") val trialVersionId: String,
    @SerializedName("prediction_payload") val predictionPayload: Map<String, String>?,
    @SerializedName("answer_payload") val answerPayload: Map<String, String>,
    val explanation: String?,
    @SerializedName("remediation_context_id") val remediationContextId: String,
    @SerializedName("client_request_id") val clientRequestId: String,
)

data class TrialAttemptResultDto(
    @SerializedName("attempt_id") val attemptId: String,
    val passed: Boolean,
    val score: Double,
    @SerializedName("max_score") val maxScore: Double,
    @SerializedName("feedback_codes") val feedbackCodes: List<String>,
)

data class PublicationDto(
    val id: String,
    val status: String,
    @SerializedName("return_reason_summary") val returnReasonSummary: String?,
    @SerializedName("row_version") val rowVersion: Int,
)

data class ModerationCaseDto(
    val id: String,
    @SerializedName("publication_id") val publicationId: String,
    @SerializedName("publication_status") val publicationStatus: String,
    val status: String,
    @SerializedName("public_reason_summary") val publicReasonSummary: String?,
    @SerializedName("revision_suggestion") val revisionSuggestion: String?,
    @SerializedName("can_appeal") val canAppeal: Boolean,
)

data class ModerationAppealDto(
    val id: String,
    @SerializedName("moderation_case_id") val moderationCaseId: String,
    val reason: String,
    val status: String,
)

data class CreationProjectDto(
    val id: String,
    val title: String,
    val description: String?,
    @SerializedName("media_type") val mediaType: String,
    val status: String,
    @SerializedName("default_visibility") val defaultVisibility: String,
    @SerializedName("current_version_number") val currentVersionNumber: Int?,
    @SerializedName("display_status") val displayStatus: String,
    @SerializedName("latest_publication") val latestPublication: PublicationDto?,
    @SerializedName("row_version") val rowVersion: Int,
    @SerializedName("updated_at") val updatedAt: String,
)

data class CreationProjectListDto(
    val total: Int,
    val items: List<CreationProjectDto>,
    @SerializedName("next_cursor") val nextCursor: String?,
)

data class CreationLayerDto(
    @SerializedName("layer_id") val layerId: String,
    val kind: String,
    val name: String,
    @SerializedName("z_index") val zIndex: Int,
    val visible: Boolean,
    @SerializedName("asset_id") val assetId: String?,
    @SerializedName("text_content") val textContent: String?,
    val aigc: Boolean,
)

data class CreationVersionDto(
    val id: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("version_number") val versionNumber: Int,
    val layers: List<CreationLayerDto>,
    @SerializedName("change_summary") val changeSummary: String,
    @SerializedName("modification_reason") val modificationReason: String?,
    @SerializedName("created_at") val createdAt: String,
)

data class CreationVersionListDto(val items: List<CreationVersionDto>)

data class CreationVersionCreateDto(
    @SerializedName("parent_version_id") val parentVersionId: String? = null,
    val layers: List<CreationLayerDto>,
    @SerializedName("canvas_width") val canvasWidth: Int,
    @SerializedName("canvas_height") val canvasHeight: Int,
    @SerializedName("preview_asset_id") val previewAssetId: String? = null,
    @SerializedName("change_summary") val changeSummary: String,
    @SerializedName("modification_reason") val modificationReason: String? = null,
)

data class LearningCardDto(
    @SerializedName("creation_version_id") val creationVersionId: String,
    @SerializedName("manual_page_ids") val manualPageIds: List<String>,
    @SerializedName("method_summary") val methodSummary: String,
    @SerializedName("unresolved_questions") val unresolvedQuestions: List<String>,
    val status: String,
)

data class ProvenanceItemDto(
    @SerializedName("item_type") val itemType: String,
    @SerializedName("contribution_type") val contributionType: String,
    val description: String,
    @SerializedName("license_type") val licenseType: String,
    @SerializedName("source_url") val sourceUrl: String?,
    @SerializedName("ai_provider") val aiProvider: String?,
)

data class ProvenanceManifestDto(
    @SerializedName("creation_version_id") val creationVersionId: String,
    @SerializedName("human_contribution_summary") val humanContributionSummary: String,
    @SerializedName("ai_assistance_used") val aiAssistanceUsed: Boolean,
    @SerializedName("ai_contribution_summary") val aiContributionSummary: String?,
    @SerializedName("aigc_label_declared") val aigcLabelDeclared: Boolean,
    @SerializedName("unresolved_rights") val unresolvedRights: Boolean,
    val status: String,
    val items: List<ProvenanceItemDto>,
)

data class CreationDetailBundle(
    val project: CreationProjectDto,
    val versions: List<CreationVersionDto>,
    val learningCard: LearningCardDto?,
    val provenance: ProvenanceManifestDto?,
    val moderationCase: ModerationCaseDto?,
)

data class PrivacySettingsDto(
    @SerializedName("default_work_visibility") val defaultWorkVisibility: String,
    @SerializedName("learning_card_public") val learningCardPublic: Boolean,
    @SerializedName("aigc_export_mark_enabled") val aigcExportMarkEnabled: Boolean,
    @SerializedName("profile_discovery_enabled") val profileDiscoveryEnabled: Boolean,
    @SerializedName("guardian_controls_active") val guardianControlsActive: Boolean,
    @SerializedName("row_version") val rowVersion: Int,
    @SerializedName("updated_at") val updatedAt: String,
)

data class PrivacySettingsPatchDto(
    @SerializedName("default_work_visibility") val defaultWorkVisibility: String? = null,
    @SerializedName("learning_card_public") val learningCardPublic: Boolean? = null,
    @SerializedName("aigc_export_mark_enabled") val aigcExportMarkEnabled: Boolean? = null,
    @SerializedName("profile_discovery_enabled") val profileDiscoveryEnabled: Boolean? = null,
    @SerializedName("row_version") val rowVersion: Int,
)

data class DataRightsRequestDto(
    val id: String,
    @SerializedName("request_type") val requestType: String,
    val status: String,
    val reason: String?,
    @SerializedName("created_at") val createdAt: String,
)
