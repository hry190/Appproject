package com.jueqiao.jianghu.auth

import com.google.gson.annotations.SerializedName

enum class VerificationPurpose {
    REGISTER,
    RESET_PASSWORD,
    GUARDIAN_CONSENT,
}

enum class AgeBand(val apiValue: String, val label: String) {
    Under14("UNDER_14", "未满14岁"),
    Teen("AGE_14_TO_17", "14–17岁"),
    Adult("ADULT", "18岁及以上"),
}

data class VerificationCodeRequest(
    val phone: String,
    val purpose: String,
    @SerializedName("client_request_id") val clientRequestId: String,
)

data class VerificationCodeAccepted(
    val accepted: Boolean,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("retry_after") val retryAfter: Int,
    @SerializedName("request_id") val requestId: String,
)

data class PasswordLoginRequest(
    val phone: String,
    val password: String,
    @SerializedName("device_name") val deviceName: String = "Android",
)

data class RegisterRequest(
    val phone: String,
    @SerializedName("verification_code") val verificationCode: String,
    val password: String,
    @SerializedName("age_band") val ageBand: String,
    @SerializedName("terms_version") val termsVersion: String,
    @SerializedName("privacy_version") val privacyVersion: String,
    @SerializedName("guardian_consent_token") val guardianConsentToken: String? = null,
    @SerializedName("device_name") val deviceName: String = "Android",
    @SerializedName("client_request_id") val clientRequestId: String,
)

data class GuardianConsentRequest(
    @SerializedName("child_phone") val childPhone: String,
    @SerializedName("guardian_phone") val guardianPhone: String,
    @SerializedName("verification_code") val verificationCode: String,
    @SerializedName("terms_version") val termsVersion: String,
    @SerializedName("privacy_version") val privacyVersion: String,
)

data class GuardianConsentResponse(
    @SerializedName("guardian_consent_token") val guardianConsentToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
)

data class PasswordResetRequest(
    val phone: String,
    @SerializedName("verification_code") val verificationCode: String,
    @SerializedName("new_password") val newPassword: String,
)

data class PasswordResetResponse(val status: String)

data class LogoutRequest(
    @SerializedName("refresh_token") val refreshToken: String,
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("device_name") val deviceName: String = "Android",
)

data class UserDto(
    val id: String,
    val nickname: String,
    @SerializedName("phone_masked") val phoneMasked: String,
    val status: String,
    @SerializedName("age_band") val ageBand: String,
    @SerializedName("guardian_status") val guardianStatus: String,
)

data class TokenPairDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("refresh_expires_in") val refreshExpiresIn: Int,
)

data class AuthResponseDto(
    val user: UserDto,
    val tokens: TokenPairDto,
    @SerializedName("next_action") val nextAction: String,
)

data class UserSettingsDto(
    @SerializedName("message_enabled") val messageEnabled: Boolean,
    @SerializedName("learning_reminder") val learningReminder: Boolean,
    @SerializedName("work_updates") val workUpdates: Boolean,
    @SerializedName("service_messages") val serviceMessages: Boolean,
    @SerializedName("quiet_hours") val quietHours: Boolean,
    @SerializedName("auto_save") val autoSave: Boolean,
    @SerializedName("wifi_only") val wifiOnly: Boolean,
    @SerializedName("haptic_feedback") val hapticFeedback: Boolean,
    @SerializedName("large_text") val largeText: Boolean,
    @SerializedName("sound_enabled") val soundEnabled: Boolean,
    @SerializedName("music_volume") val musicVolume: Float,
    @SerializedName("effect_volume") val effectVolume: Float,
    @SerializedName("high_contrast") val highContrast: Boolean,
    @SerializedName("read_aloud") val readAloud: Boolean,
    @SerializedName("subtitles_enabled") val subtitlesEnabled: Boolean,
    @SerializedName("personalization_enabled") val personalizationEnabled: Boolean,
    @SerializedName("rest_reminder") val restReminder: Boolean,
    @SerializedName("updated_at") val updatedAt: String,
)

data class UserSettingsPatchDto(
    @SerializedName("message_enabled") val messageEnabled: Boolean? = null,
    @SerializedName("learning_reminder") val learningReminder: Boolean? = null,
    @SerializedName("work_updates") val workUpdates: Boolean? = null,
    @SerializedName("service_messages") val serviceMessages: Boolean? = null,
    @SerializedName("quiet_hours") val quietHours: Boolean? = null,
    @SerializedName("auto_save") val autoSave: Boolean? = null,
    @SerializedName("wifi_only") val wifiOnly: Boolean? = null,
    @SerializedName("haptic_feedback") val hapticFeedback: Boolean? = null,
    @SerializedName("large_text") val largeText: Boolean? = null,
    @SerializedName("sound_enabled") val soundEnabled: Boolean? = null,
    @SerializedName("music_volume") val musicVolume: Float? = null,
    @SerializedName("effect_volume") val effectVolume: Float? = null,
    @SerializedName("high_contrast") val highContrast: Boolean? = null,
    @SerializedName("read_aloud") val readAloud: Boolean? = null,
    @SerializedName("subtitles_enabled") val subtitlesEnabled: Boolean? = null,
    @SerializedName("personalization_enabled") val personalizationEnabled: Boolean? = null,
    @SerializedName("rest_reminder") val restReminder: Boolean? = null,
)

data class FeedbackRequestDto(
    val category: String = "GENERAL",
    val message: String,
)

data class FeedbackResponseDto(
    val id: String,
    val category: String,
    val message: String,
    val status: String,
    @SerializedName("created_at") val createdAt: String,
)

data class BlacklistEntryDto(
    @SerializedName("user_id") val userId: String,
    val nickname: String,
    @SerializedName("blocked_at") val blockedAt: String,
)

data class SessionDto(
    val id: String,
    @SerializedName("device_name") val deviceName: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("last_seen_at") val lastSeenAt: String,
    @SerializedName("expires_at") val expiresAt: String,
)

internal data class ApiErrorBody(
    val code: String? = null,
    val message: String? = null,
    @SerializedName("request_id") val requestId: String? = null,
    @SerializedName("retry_after") val retryAfter: Int? = null,
)

internal data class ApiErrorEnvelope(val error: ApiErrorBody? = null)

class AuthApiException(
    val statusCode: Int,
    val code: String,
    override val message: String,
    val retryAfter: Int? = null,
    val requestId: String? = null,
) : Exception(message)
