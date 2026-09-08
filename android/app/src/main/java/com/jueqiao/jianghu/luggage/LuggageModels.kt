package com.jueqiao.jianghu.luggage

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement
import com.google.gson.JsonObject

data class CapabilitiesDto(
    val profile: Boolean,
    @SerializedName("manual_catalog") val manualCatalog: Boolean,
    @SerializedName("manual_favorites") val manualFavorites: Boolean,
    @SerializedName("luggage_snapshot") val luggageSnapshot: String,
    @SerializedName("learning_progress") val learningProgress: Boolean,
    val mistakes: Boolean,
    val creations: Boolean,
    @SerializedName("media_uploads") val mediaUploads: Boolean,
    val conference: Boolean,
)

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

data class LearningBookDto(
    @SerializedName("manual_page_id") val manualPageId: String,
    @SerializedName("page_no") val pageNo: Int,
    @SerializedName("style_no") val styleNo: Int,
    val title: String,
    @SerializedName("volume_no") val volumeNo: Int,
    @SerializedName("volume_title") val volumeTitle: String,
    val state: String,
    @SerializedName("state_label") val stateLabel: String,
    @SerializedName("evidence_count") val evidenceCount: Int,
    @SerializedName("review_due") val reviewDue: Boolean,
    @SerializedName("updated_at") val updatedAt: String?,
)

data class BackMountainRecommendationDto(
    @SerializedName("volume_no") val volumeNo: Int,
    @SerializedName("lesson_id") val lessonId: String,
    val reason: String,
    val available: Boolean,
)

data class LearningOverviewDto(
    @SerializedName("recommended_lesson_id") val recommendedLessonId: String?,
    val books: List<LearningBookDto>,
    @SerializedName("back_mountain") val backMountain: BackMountainRecommendationDto?,
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
    @SerializedName("trial_id") val trialId: String? = null,
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
    @SerializedName("remediation_context_id") val remediationContextId: String? = null,
    @SerializedName("client_request_id") val clientRequestId: String,
)

data class LessonReadEventAcceptedDto(
    @SerializedName("event_id") val eventId: String,
    @SerializedName("lesson_id") val lessonId: String,
    val state: String,
    val changed: Boolean,
    @SerializedName("processed_at") val processedAt: String,
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
    @SerializedName("project_id") val projectId: String,
    @SerializedName("creation_version_id") val creationVersionId: String,
    val status: String,
    val visibility: String,
    @SerializedName("classroom_id") val classroomId: String?,
    @SerializedName("return_reason_summary") val returnReasonSummary: String?,
    @SerializedName("submitted_at") val submittedAt: String,
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
    @SerializedName("source_intent_id") val sourceIntentId: String?,
    @SerializedName("derivative_authorization_id") val derivativeAuthorizationId: String?,
    @SerializedName("current_stage") val currentStage: String,
    @SerializedName("display_status") val displayStatus: String,
    @SerializedName("latest_publication") val latestPublication: PublicationDto?,
    @SerializedName("row_version") val rowVersion: Int,
    @SerializedName("updated_at") val updatedAt: String,
)

data class CreationProjectCreateDto(
    val title: String,
    val description: String? = null,
    @SerializedName("intent_id") val intentId: String? = null,
    @SerializedName("derivative_authorization_id") val derivativeAuthorizationId: String? = null,
    @SerializedName("media_type") val mediaType: String = "ILLUSTRATION",
    @SerializedName("default_visibility") val defaultVisibility: String = "PRIVATE",
)

data class CreationProjectPatchDto(
    val title: String? = null,
    val description: String? = null,
    @SerializedName("default_visibility") val defaultVisibility: String? = null,
    @SerializedName("row_version") val rowVersion: Int,
)

data class ConferenceWorkDto(
    @SerializedName("publication_id") val publicationId: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("creation_version_id") val creationVersionId: String,
    val title: String,
    val description: String?,
    @SerializedName("media_type") val mediaType: String,
    @SerializedName("author_nickname") val authorNickname: String,
    @SerializedName("is_owner") val isOwner: Boolean,
    @SerializedName("version_number") val versionNumber: Int,
    @SerializedName("published_at") val publishedAt: String,
    @SerializedName("preview_url") val previewUrl: String?,
    @SerializedName("ai_assisted") val aiAssisted: Boolean,
    @SerializedName("learning_summary") val learningSummary: String?,
    @SerializedName("related_manuals") val relatedManuals: List<ConferenceRelatedManualDto>,
    @SerializedName("learning_card") val learningCard: ConferenceLearningCardSummaryDto?,
    val provenance: ConferenceProvenanceSummaryDto?,
)

data class ConferenceRelatedManualDto(
    @SerializedName("manual_page_id") val manualPageId: String,
    @SerializedName("page_no") val pageNo: Int,
    val title: String,
)

data class ConferenceLearningCardSummaryDto(
    @SerializedName("method_summary") val methodSummary: String,
    @SerializedName("unresolved_questions") val unresolvedQuestions: List<String>,
)

data class ConferenceProvenanceSummaryDto(
    @SerializedName("human_contribution_summary") val humanContributionSummary: String,
    @SerializedName("ai_assistance_used") val aiAssistanceUsed: Boolean,
    @SerializedName("ai_contribution_summary") val aiContributionSummary: String?,
    @SerializedName("aigc_label_declared") val aigcLabelDeclared: Boolean,
    @SerializedName("source_count") val sourceCount: Int,
)

data class ConferenceFeedDto(
    val items: List<ConferenceWorkDto>,
    @SerializedName("next_cursor") val nextCursor: String?,
)

data class ConferenceReviewCreateDto(
    val template: String,
    val content: String,
)

data class ConferenceReviewDecisionDto(
    val action: String,
    val reply: String? = null,
    @SerializedName("row_version") val rowVersion: Int,
)

data class ConferenceReviewDto(
    val id: String,
    @SerializedName("publication_id") val publicationId: String,
    @SerializedName("reviewer_nickname") val reviewerNickname: String,
    val template: String,
    val content: String,
    val status: String,
    @SerializedName("moderation_status") val moderationStatus: String,
    @SerializedName("author_reply") val authorReply: String?,
    @SerializedName("handled_at") val handledAt: String?,
    @SerializedName("adopted_in_creation_version_id") val adoptedInCreationVersionId: String?,
    @SerializedName("adoption_summary") val adoptionSummary: String?,
    @SerializedName("adopted_at") val adoptedAt: String?,
    @SerializedName("row_version") val rowVersion: Int,
    @SerializedName("created_at") val createdAt: String,
)

data class ConferenceReviewAdoptionCreateDto(
    @SerializedName("creation_version_id") val creationVersionId: String,
    val summary: String,
    @SerializedName("row_version") val rowVersion: Int,
)

data class ConferenceReviewReportCreateDto(
    val reason: String,
    val details: String? = null,
)

data class ConferenceReviewReportDto(
    val id: String,
    @SerializedName("review_id") val reviewId: String,
    val reason: String,
    val status: String,
    @SerializedName("resolution_summary") val resolutionSummary: String?,
    @SerializedName("row_version") val rowVersion: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("resolved_at") val resolvedAt: String?,
)

data class ConferenceReviewListDto(val items: List<ConferenceReviewDto>)

data class ConferenceCollectionDto(
    @SerializedName("publication_id") val publicationId: String,
    @SerializedName("saved_at") val savedAt: String,
    val work: ConferenceWorkDto,
)

data class ConferenceCollectionListDto(val items: List<ConferenceCollectionDto>)

data class ConferenceDerivativeRequestCreateDto(
    @SerializedName("source_publication_id") val sourcePublicationId: String,
    @SerializedName("requested_use") val requestedUse: String,
)

data class ConferenceDerivativeDecisionDto(
    val decision: String,
    val note: String? = null,
)

data class ConferenceDerivativeAuthorizationDto(
    val id: String,
    @SerializedName("request_id") val requestId: String,
    @SerializedName("source_publication_id") val sourcePublicationId: String,
    @SerializedName("source_creation_version_id") val sourceCreationVersionId: String,
    @SerializedName("authorization_version") val authorizationVersion: String,
    val status: String,
)

data class ConferenceDerivativeRequestDto(
    val id: String,
    @SerializedName("source_publication_id") val sourcePublicationId: String,
    @SerializedName("source_title") val sourceTitle: String,
    @SerializedName("source_author_nickname") val sourceAuthorNickname: String,
    @SerializedName("requested_use") val requestedUse: String,
    val status: String,
    @SerializedName("author_decision_note") val authorDecisionNote: String?,
    val authorization: ConferenceDerivativeAuthorizationDto?,
)

data class ConferenceDerivativeRequestListDto(
    val items: List<ConferenceDerivativeRequestDto>,
)

data class ConferenceMatchQueueDto(
    @SerializedName("queue_id") val queueId: String?,
    val status: String,
    @SerializedName("manual_page_id") val manualPageId: String?,
    @SerializedName("match_id") val matchId: String?,
    @SerializedName("joined_at") val joinedAt: String?,
    @SerializedName("expires_at") val expiresAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
)

data class ConferenceMatchJoinDto(
    @SerializedName("manual_page_id") val manualPageId: String,
)

data class ConferenceMatchReportCreateDto(
    val reason: String,
    val details: String? = null,
)

data class ConferenceMatchQuestionDto(
    val id: String,
    val position: Int,
    val kind: String,
    val prompt: String,
)

data class ConferenceMatchAnswerCreateDto(
    @SerializedName("question_id") val questionId: String,
    val answer: String,
    val reason: String,
)

data class ConferenceMatchAnswerDto(
    val id: String,
    @SerializedName("question_id") val questionId: String,
    val answer: String,
    val reason: String,
    @SerializedName("submitted_at") val submittedAt: String,
)

data class ConferenceMatchProgressDto(
    val answered: Int,
    val total: Int,
    val complete: Boolean,
)

data class ConferenceMatchDetailDto(
    @SerializedName("match_id") val matchId: String,
    @SerializedName("manual_page_id") val manualPageId: String,
    val status: String,
    val questions: List<ConferenceMatchQuestionDto>,
    @SerializedName("my_answers") val myAnswers: List<ConferenceMatchAnswerDto>,
    @SerializedName("my_progress") val myProgress: ConferenceMatchProgressDto,
    @SerializedName("opponent_progress") val opponentProgress: ConferenceMatchProgressDto,
    @SerializedName("matched_at") val matchedAt: String,
)

data class ConferenceMatchAnswerSubmittedDto(
    val answer: ConferenceMatchAnswerDto,
    @SerializedName("my_progress") val myProgress: ConferenceMatchProgressDto,
    @SerializedName("opponent_progress") val opponentProgress: ConferenceMatchProgressDto,
    @SerializedName("match_status") val matchStatus: String,
)

data class ConferenceMatchEvaluationCreateDto(
    val kind: String,
    val score: Double,
    @SerializedName("dimension_scores") val dimensionScores: Map<String, Double> = emptyMap(),
    val summary: String,
    val strengths: List<String> = emptyList(),
    val improvements: List<String> = emptyList(),
)

data class ConferenceMatchEvaluationDto(
    val id: String,
    val kind: String,
    val score: Double,
    @SerializedName("dimension_scores") val dimensionScores: Map<String, Double>,
    val summary: String,
    val strengths: List<String>,
    val improvements: List<String>,
    @SerializedName("evaluator_reference") val evaluatorReference: String?,
    @SerializedName("created_at") val createdAt: String,
)

data class ConferenceMatchReflectionCreateDto(
    val learned: String,
    @SerializedName("next_improvement") val nextImprovement: String,
)

data class ConferenceMatchReflectionDto(
    val id: String,
    @SerializedName("match_id") val matchId: String,
    val learned: String,
    @SerializedName("next_improvement") val nextImprovement: String,
    @SerializedName("created_at") val createdAt: String,
)

data class ConferenceMatchParticipantResultDto(
    val perspective: String,
    val score: Double?,
    val evaluations: List<ConferenceMatchEvaluationDto>,
)

data class ConferenceMatchResultDto(
    @SerializedName("match_id") val matchId: String,
    val status: String,
    @SerializedName("end_reason") val endReason: String?,
    val outcome: String,
    val participants: List<ConferenceMatchParticipantResultDto>,
    @SerializedName("my_reflection") val myReflection: ConferenceMatchReflectionDto?,
    @SerializedName("ended_at") val endedAt: String?,
)

data class ConferenceMatchReportDto(
    val id: String,
    @SerializedName("match_id") val matchId: String,
    val reason: String,
    val status: String,
    @SerializedName("resolution_summary") val resolutionSummary: String?,
    @SerializedName("row_version") val rowVersion: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("resolved_at") val resolvedAt: String?,
)

data class ConferenceLetterDto(
    val id: String,
    val category: String,
    val title: String,
    val body: String,
    @SerializedName("action_type") val actionType: String?,
    @SerializedName("action_id") val actionId: String?,
    @SerializedName("navigation_target") val navigationTarget: String?,
    @SerializedName("navigation_id") val navigationId: String?,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("read_at") val readAt: String?,
    @SerializedName("created_at") val createdAt: String,
)

data class ConferenceLetterListDto(
    val items: List<ConferenceLetterDto>,
    @SerializedName("unread_count") val unreadCount: Int,
)

data class CreationIntentAnalyzeDto(
    val text: String,
    @SerializedName("attachment_refs") val attachmentRefs: List<String> = emptyList(),
    @SerializedName("attachment_asset_ids") val attachmentAssetIds: List<String> = emptyList(),
    @SerializedName("manual_page_ids") val manualPageIds: List<String> = emptyList(),
    @SerializedName("resource_links") val resourceLinks: List<String> = emptyList(),
)

data class CreationMethodDraftDto(
    val name: String,
    val goal: String,
    val audience: List<String>,
    val format: String,
    val steps: List<String>,
    @SerializedName("resource_links") val resourceLinks: List<String>,
    @SerializedName("source_asset_ids") val sourceAssetIds: List<String>,
    @SerializedName("manual_page_ids") val manualPageIds: List<String>,
    @SerializedName("recommended_media_type") val recommendedMediaType: String,
)

data class CreationIntentAnalysisDto(
    @SerializedName("intent_id") val intentId: String,
    @SerializedName("analysis_id") val analysisId: String,
    @SerializedName("schema_version") val schemaVersion: String,
    @SerializedName("method_draft") val methodDraft: CreationMethodDraftDto,
    val questions: List<String>,
    @SerializedName("safety_flags") val safetyFlags: List<String>,
    val confidence: String,
    @SerializedName("expires_at") val expiresAt: String,
)

data class CreationMethodPutDto(
    val name: String,
    val goal: String,
    val audience: List<String>,
    val format: String,
    val steps: List<String>,
    @SerializedName("resource_links") val resourceLinks: List<String>,
    @SerializedName("source_asset_ids") val sourceAssetIds: List<String>,
    @SerializedName("manual_page_ids") val manualPageIds: List<String>,
    @SerializedName("expected_revision") val expectedRevision: Int,
)

data class CreationMethodDto(
    val id: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("version_number") val versionNumber: Int,
    val name: String,
    val goal: String,
    val audience: List<String>,
    val format: String,
    val steps: List<String>,
    @SerializedName("resource_links") val resourceLinks: List<String>,
    @SerializedName("source_asset_ids") val sourceAssetIds: List<String>,
    @SerializedName("manual_page_ids") val manualPageIds: List<String>,
    @SerializedName("project_revision") val projectRevision: Int,
    @SerializedName("created_at") val createdAt: String,
)

data class MediaUploadIntentCreateDto(
    val purpose: String = "CREATION_LAYER",
    val filename: String,
    @SerializedName("declared_mime") val declaredMime: String,
    @SerializedName("byte_size") val byteSize: Int,
    val sha256: String,
)

data class MediaUploadIntentDto(
    val id: String,
    val status: String,
    val purpose: String,
    @SerializedName("upload_url") val uploadUrl: String,
    val method: String,
    @SerializedName("required_headers") val requiredHeaders: Map<String, String>,
    @SerializedName("expires_at") val expiresAt: String,
)

data class MediaUploadCompleteDto(
    @SerializedName("byte_size") val byteSize: Int,
    val sha256: String,
)

data class MediaAssetDto(
    val id: String,
    val purpose: String,
    @SerializedName("original_filename") val originalFilename: String,
    val status: String,
    @SerializedName("actual_mime") val actualMime: String?,
    @SerializedName("byte_size") val byteSize: Int,
    val sha256: String,
    val width: Int?,
    val height: Int?,
    @SerializedName("metadata_stripped") val metadataStripped: Boolean?,
    @SerializedName("aigc_detected") val aigcDetected: Boolean?,
    @SerializedName("rejection_code") val rejectionCode: String?,
    @SerializedName("rejection_summary") val rejectionSummary: String?,
    @SerializedName("original_url") val originalUrl: String?,
    @SerializedName("url_expires_at") val urlExpiresAt: String?,
    @SerializedName("row_version") val rowVersion: Int,
)

data class CreationStageTransitionDto(
    @SerializedName("from_stage") val fromStage: String,
    @SerializedName("to_stage") val toStage: String,
    val reason: String,
    @SerializedName("expected_revision") val expectedRevision: Int,
)

data class CreationStageEventDto(
    val id: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("from_stage") val fromStage: String,
    @SerializedName("to_stage") val toStage: String,
    val reason: String,
    @SerializedName("actor_user_id") val actorUserId: String,
    @SerializedName("created_at") val createdAt: String,
)

data class CreationStageEventListDto(val items: List<CreationStageEventDto>)

data class CreationStageTransitionResultDto(
    @SerializedName("current_stage") val currentStage: String,
    @SerializedName("project_revision") val projectRevision: Int,
    val event: CreationStageEventDto,
)

data class CreationToolCallProposeDto(
    val kind: String = "COACH_REVIEW",
    val prompt: String,
)

data class CreationToolCallDecisionDto(
    val approve: Boolean,
    val reason: String? = null,
    @SerializedName("expected_revision") val expectedRevision: Int,
)

data class CreationToolCallDto(
    val id: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("creation_version_id") val creationVersionId: String,
    val kind: String,
    val status: String,
    @SerializedName("input_snapshot") val inputSnapshot: JsonObject,
    @SerializedName("prompt_summary") val promptSummary: String,
    @SerializedName("effect_summary") val effectSummary: String,
    @SerializedName("external_data_shared") val externalDataShared: Boolean,
    @SerializedName("output_snapshot") val outputSnapshot: JsonObject?,
    @SerializedName("executor_ref") val executorRef: String?,
    @SerializedName("row_version") val rowVersion: Int,
    @SerializedName("proposed_at") val proposedAt: String,
    @SerializedName("expires_at") val expiresAt: String,
    @SerializedName("decided_at") val decidedAt: String?,
    @SerializedName("completed_at") val completedAt: String?,
)

data class CreationToolCallListDto(val items: List<CreationToolCallDto>)

data class CreationTestFindingCreateDto(
    val severity: String,
    val description: String,
)

data class CreationTestRecordCreateDto(
    @SerializedName("creation_version_id") val creationVersionId: String,
    val scenario: String,
    val result: String,
    val notes: String,
    val findings: List<CreationTestFindingCreateDto>,
)

data class CreationTestIssueDto(
    val id: String,
    @SerializedName("test_record_id") val testRecordId: String,
    @SerializedName("project_id") val projectId: String,
    val severity: String,
    val description: String,
    val status: String,
    @SerializedName("resolution_summary") val resolutionSummary: String?,
    @SerializedName("row_version") val rowVersion: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("resolved_at") val resolvedAt: String?,
)

data class CreationTestRecordDto(
    val id: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("creation_version_id") val creationVersionId: String,
    val scenario: String,
    val result: String,
    val notes: String,
    val issues: List<CreationTestIssueDto>,
    @SerializedName("created_at") val createdAt: String,
)

data class CreationTestRecordListDto(val items: List<CreationTestRecordDto>)

data class CreationTestIssueResolveDto(
    @SerializedName("resolution_summary") val resolutionSummary: String,
    @SerializedName("expected_revision") val expectedRevision: Int,
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
    @SerializedName("offset_x") val offsetX: Float = 0f,
    @SerializedName("offset_y") val offsetY: Float = 0f,
    val scale: Float = 1f,
    @SerializedName("rotation_degrees") val rotationDegrees: Float = 0f,
    val opacity: Float = 1f,
    @SerializedName("crop_inset") val cropInset: Float = 0f,
    @SerializedName("font_size") val fontSize: Int? = null,
    @SerializedName("text_color") val textColor: String? = null,
)

data class CreationVersionDto(
    val id: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("version_number") val versionNumber: Int,
    @SerializedName("parent_version_id") val parentVersionId: String?,
    val layers: List<CreationLayerDto>,
    @SerializedName("canvas_width") val canvasWidth: Int,
    @SerializedName("canvas_height") val canvasHeight: Int,
    @SerializedName("preview_asset_id") val previewAssetId: String?,
    @SerializedName("change_summary") val changeSummary: String,
    @SerializedName("modification_reason") val modificationReason: String?,
    @SerializedName("created_at") val createdAt: String,
)

data class CreationVersionListDto(val items: List<CreationVersionDto>)

data class CreationLayerDiffDto(
    @SerializedName("layer_id") val layerId: String,
    val name: String,
    val kind: String,
    @SerializedName("changed_fields") val changedFields: List<String>,
)

data class CreationVersionDiffDto(
    @SerializedName("base_version_id") val baseVersionId: String,
    @SerializedName("base_version_number") val baseVersionNumber: Int,
    @SerializedName("target_version_id") val targetVersionId: String,
    @SerializedName("target_version_number") val targetVersionNumber: Int,
    @SerializedName("canvas_changed") val canvasChanged: Boolean,
    @SerializedName("added_layers") val addedLayers: List<CreationLayerDiffDto>,
    @SerializedName("removed_layers") val removedLayers: List<CreationLayerDiffDto>,
    @SerializedName("modified_layers") val modifiedLayers: List<CreationLayerDiffDto>,
)

data class ImageGenerationCreateDto(
    @SerializedName("parent_version_id") val parentVersionId: String,
    val prompt: String,
    val size: String,
    val quality: String,
    @SerializedName("expected_project_revision") val expectedProjectRevision: Int,
    @SerializedName("user_confirmed_generation") val userConfirmedGeneration: Boolean = true,
)

data class ImageGenerationRetryDto(
    @SerializedName("expected_revision") val expectedRevision: Int,
)

data class ImageGenerationJobDto(
    val id: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("parent_version_id") val parentVersionId: String,
    @SerializedName("prompt_summary") val promptSummary: String,
    val size: String,
    val quality: String,
    val status: String,
    @SerializedName("progress_percent") val progressPercent: Int,
    @SerializedName("provider_ref") val providerRef: String,
    @SerializedName("model_ref") val modelRef: String,
    @SerializedName("external_data_shared") val externalDataShared: Boolean,
    @SerializedName("output_asset") val outputAsset: MediaAssetDto?,
    @SerializedName("output_version_id") val outputVersionId: String?,
    @SerializedName("error_code") val errorCode: String?,
    @SerializedName("error_summary") val errorSummary: String?,
    val retryable: Boolean,
    @SerializedName("retry_count") val retryCount: Int,
    @SerializedName("row_version") val rowVersion: Int,
)

data class ImageGenerationJobListDto(
    val enabled: Boolean,
    @SerializedName("provider_ref") val providerRef: String,
    @SerializedName("model_ref") val modelRef: String,
    @SerializedName("external_data_shared") val externalDataShared: Boolean,
    @SerializedName("daily_limit") val dailyLimit: Int,
    @SerializedName("daily_used") val dailyUsed: Int,
    @SerializedName("daily_remaining") val dailyRemaining: Int,
    val items: List<ImageGenerationJobDto>,
)

data class CreationExportCreateDto(
    val format: String,
    @SerializedName("output_scale") val outputScale: Int,
    @SerializedName("user_confirmed_export") val userConfirmedExport: Boolean = true,
)

data class CreationExportJobDto(
    val id: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("creation_version_id") val creationVersionId: String,
    @SerializedName("version_number") val versionNumber: Int,
    val format: String,
    @SerializedName("output_scale") val outputScale: Int,
    val status: String,
    @SerializedName("progress_percent") val progressPercent: Int,
    @SerializedName("output_asset") val outputAsset: MediaAssetDto?,
    @SerializedName("error_code") val errorCode: String?,
    @SerializedName("error_summary") val errorSummary: String?,
    @SerializedName("row_version") val rowVersion: Int,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("completed_at") val completedAt: String?,
)

data class CreationExportJobListDto(val items: List<CreationExportJobDto>)

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
    @SerializedName("questions_confirmed") val questionsConfirmed: Boolean,
    val status: String,
    @SerializedName("row_version") val rowVersion: Int,
    @SerializedName("locked_at") val lockedAt: String?,
)

data class LearningCardPutDto(
    @SerializedName("manual_page_ids") val manualPageIds: List<String>,
    @SerializedName("method_summary") val methodSummary: String,
    @SerializedName("unresolved_questions") val unresolvedQuestions: List<String>,
    @SerializedName("questions_confirmed") val questionsConfirmed: Boolean,
    @SerializedName("row_version") val rowVersion: Int? = null,
)

data class MigrationEvidenceCreateDto(
    @SerializedName("creation_version_id") val creationVersionId: String,
    @SerializedName("used_lessons") val usedLessons: List<String>,
    @SerializedName("revision_reason") val revisionReason: String,
)

data class MigrationEvidenceSubmittedDto(
    @SerializedName("evidence_id") val evidenceId: String,
    @SerializedName("lesson_id") val lessonId: String,
    @SerializedName("creation_version_id") val creationVersionId: String,
    @SerializedName("validation_status") val validationStatus: String,
    @SerializedName("current_state") val currentState: String,
    @SerializedName("processed_at") val processedAt: String,
)

data class ProvenanceItemDto(
    val id: String,
    @SerializedName("item_type") val itemType: String,
    @SerializedName("contribution_type") val contributionType: String,
    val description: String,
    @SerializedName("license_type") val licenseType: String,
    @SerializedName("source_url") val sourceUrl: String?,
    @SerializedName("source_author") val sourceAuthor: String?,
    @SerializedName("ai_provider") val aiProvider: String?,
    @SerializedName("ai_model") val aiModel: String?,
    @SerializedName("ai_tool_action") val aiToolAction: String?,
    @SerializedName("prompt_summary") val promptSummary: String?,
    @SerializedName("user_modified") val userModified: Boolean?,
)

data class ProvenanceItemInputDto(
    @SerializedName("item_type") val itemType: String,
    @SerializedName("contribution_type") val contributionType: String,
    val description: String,
    @SerializedName("source_url") val sourceUrl: String? = null,
    @SerializedName("source_author") val sourceAuthor: String? = null,
    @SerializedName("license_type") val licenseType: String,
    @SerializedName("ai_provider") val aiProvider: String? = null,
    @SerializedName("ai_model") val aiModel: String? = null,
    @SerializedName("ai_tool_action") val aiToolAction: String? = null,
    @SerializedName("prompt_summary") val promptSummary: String? = null,
    @SerializedName("user_modified") val userModified: Boolean? = null,
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
    @SerializedName("row_version") val rowVersion: Int,
    @SerializedName("locked_at") val lockedAt: String?,
)

data class ProvenanceManifestPutDto(
    @SerializedName("human_contribution_summary") val humanContributionSummary: String,
    @SerializedName("ai_assistance_used") val aiAssistanceUsed: Boolean,
    @SerializedName("ai_contribution_summary") val aiContributionSummary: String? = null,
    @SerializedName("aigc_label_declared") val aigcLabelDeclared: Boolean,
    @SerializedName("unresolved_rights") val unresolvedRights: Boolean,
    val items: List<ProvenanceItemInputDto>,
    @SerializedName("row_version") val rowVersion: Int? = null,
)

data class CreationSealCheckDto(
    @SerializedName("creation_version_id") val creationVersionId: String,
    @SerializedName("work_description") val workDescription: String,
    @SerializedName("learning_reflection") val learningReflection: String,
    @SerializedName("next_improvement") val nextImprovement: String,
    @SerializedName("identity_privacy_confirmed") val identityPrivacyConfirmed: Boolean,
    @SerializedName("contact_privacy_confirmed") val contactPrivacyConfirmed: Boolean,
    @SerializedName("portrait_rights_confirmed") val portraitRightsConfirmed: Boolean,
    val status: String,
    @SerializedName("row_version") val rowVersion: Int,
    @SerializedName("locked_at") val lockedAt: String?,
)

data class CreationSealCheckPutDto(
    @SerializedName("work_description") val workDescription: String,
    @SerializedName("learning_reflection") val learningReflection: String,
    @SerializedName("next_improvement") val nextImprovement: String,
    @SerializedName("identity_privacy_confirmed") val identityPrivacyConfirmed: Boolean,
    @SerializedName("contact_privacy_confirmed") val contactPrivacyConfirmed: Boolean,
    @SerializedName("portrait_rights_confirmed") val portraitRightsConfirmed: Boolean,
    @SerializedName("row_version") val rowVersion: Int? = null,
)

data class CreationSubmissionCreateDto(
    @SerializedName("creation_version_id") val creationVersionId: String,
    val visibility: String,
    @SerializedName("target_classroom_id") val targetClassroomId: String? = null,
)

data class ClassroomDto(
    val id: String,
    val name: String,
    val role: String,
    @SerializedName("teacher_nickname") val teacherNickname: String,
    @SerializedName("member_count") val memberCount: Int,
    @SerializedName("can_submit") val canSubmit: Boolean,
    @SerializedName("joined_at") val joinedAt: String?,
    @SerializedName("created_at") val createdAt: String,
)

data class ClassroomListDto(val items: List<ClassroomDto>)

data class ClassroomCreatedDto(
    val id: String,
    val name: String,
    @SerializedName("join_code") val joinCode: String,
    val role: String,
    @SerializedName("member_count") val memberCount: Int,
    @SerializedName("created_at") val createdAt: String,
)

data class PublicationFeedItemDto(
    @SerializedName("publication_id") val publicationId: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("creation_version_id") val creationVersionId: String,
    val title: String,
    val description: String?,
    @SerializedName("media_type") val mediaType: String,
    val visibility: String,
    val channel: String?,
    @SerializedName("classroom_id") val classroomId: String?,
    @SerializedName("classroom_name") val classroomName: String?,
    @SerializedName("author_nickname") val authorNickname: String,
    @SerializedName("published_at") val publishedAt: String,
    @SerializedName("preview_url") val previewUrl: String?,
    @SerializedName("preview_mime_type") val previewMimeType: String?,
    @SerializedName("preview_width") val previewWidth: Int?,
    @SerializedName("preview_height") val previewHeight: Int?,
    @SerializedName("preview_url_expires_at") val previewUrlExpiresAt: String?,
    @SerializedName("ai_assisted") val aiAssisted: Boolean,
    @SerializedName("learning_summary") val learningSummary: String?,
)

data class PublicationFeedPageDto(
    val items: List<PublicationFeedItemDto>,
    @SerializedName("next_cursor") val nextCursor: String?,
)

data class CreationDetailBundle(
    val project: CreationProjectDto,
    val versions: List<CreationVersionDto>,
    val method: CreationMethodDto?,
    val stageEvents: List<CreationStageEventDto>,
    val toolCalls: List<CreationToolCallDto>,
    val testRecords: List<CreationTestRecordDto>,
    val imageGenerations: ImageGenerationJobListDto,
    val learningCard: LearningCardDto?,
    val provenance: ProvenanceManifestDto?,
    val sealCheck: CreationSealCheckDto?,
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
