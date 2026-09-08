import type {
  LearningOverview,
  LearningRoute,
  LessonReadEventAccepted,
  MigrationEvidenceSubmitted,
  TeachingEvidenceAccepted,
  TrialAttemptAccepted,
} from "./index";

export class LearningApiError extends Error {
  constructor(public readonly status: number, message: string) {
    super(message);
    this.name = "LearningApiError";
  }
}

export class LearningApiClient {
  constructor(private readonly baseUrl = "") {}

  private async request<T>(path: string, init?: RequestInit): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      ...init,
      headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
    });
    if (!response.ok) {
      throw new LearningApiError(response.status, await response.text());
    }
    return (await response.json()) as T;
  }

  getOverview(mode?: "empty" | "content"): Promise<LearningOverview> {
    return this.request(`/v1/learning/overview${mode ? `?mode=${mode}` : ""}`);
  }

  route(query: string): Promise<LearningRoute> {
    return this.request(`/v1/learning/route?q=${encodeURIComponent(query)}`);
  }

  readLesson(lessonId: string): Promise<Record<string, unknown>> {
    return this.request(`/v1/lessons/${lessonId}`);
  }

  recordLessonRead(lessonId: string, idempotencyKey: string): Promise<LessonReadEventAccepted> {
    return this.request(`/v1/lessons/${lessonId}/read-events`, {
      method: "POST",
      headers: { "Idempotency-Key": idempotencyKey },
    });
  }

  submitTrial(trialId: string, payload: unknown, idempotencyKey: string): Promise<TrialAttemptAccepted> {
    return this.request(`/v1/trials/${trialId}/attempts`, {
      method: "POST",
      headers: { "Idempotency-Key": idempotencyKey },
      body: JSON.stringify(payload),
    });
  }

  submitMigrationEvidence(
    lessonId: string,
    payload: unknown,
    idempotencyKey: string,
  ): Promise<MigrationEvidenceSubmitted> {
    return this.request(`/v1/lessons/${lessonId}/migration-evidence`, {
      method: "POST",
      headers: { "Idempotency-Key": idempotencyKey },
      body: JSON.stringify(payload),
    });
  }

  submitTeachingEvidence(
    lessonId: string,
    payload: unknown,
    idempotencyKey: string,
  ): Promise<TeachingEvidenceAccepted> {
    return this.request(`/v1/lessons/${lessonId}/teaching-evidence`, {
      method: "POST",
      headers: { "Idempotency-Key": idempotencyKey },
      body: JSON.stringify(payload),
    });
  }
}
