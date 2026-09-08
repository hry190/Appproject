export type LearningState = "UNSEEN" | "DISCOVERED" | "LEARNED" | "MASTERED" | "TEACHING";

export interface LearningBook {
  manual_page_id: string;
  page_no: number;
  style_no: number;
  title: string;
  volume_no: number;
  volume_title: string;
  state: LearningState;
  state_label: string;
  evidence_count: number;
  review_due: boolean;
  updated_at: string | null;
}

export interface BackMountainRecommendation {
  volume_no: number;
  lesson_id: string;
  reason: string;
  available: boolean;
}

export interface LearningOverview {
  recommended_lesson_id: string | null;
  books: LearningBook[];
  back_mountain: BackMountainRecommendation | null;
}

export interface LearningRouteMatch {
  lesson_id: string;
  volume_no: number;
  volume_title: string;
  title: string;
  state: LearningState;
  available: boolean;
  prerequisites: string[];
  recommended_reason: string;
  match_source: string;
}

export interface LearningRoute {
  query: string;
  matches: LearningRouteMatch[];
}

export interface LessonReadEventAccepted {
  event_id: string;
  lesson_id: string;
  state: LearningState;
  changed: false;
  processed_at: string;
}

export interface TrialAttemptAccepted {
  attempt_id: string;
  trial_id: string;
  trial_version_id: string;
  result: "PASSED" | "FAILED";
  score: number;
  max_score: number;
  passed: boolean;
  feedback_codes: string[];
  progress_changes: Array<{
    manual_page_id: string;
    previous_state: LearningState;
    current_state: LearningState;
    changed: boolean;
    evidence_id: string | null;
  }>;
  evidence_awards: unknown[];
  mistake: { id: string; knowledge_point_code: string; reason_code: string; status: string } | null;
  processed_at: string;
}

export interface MigrationEvidenceSubmitted {
  evidence_id: string;
  lesson_id: string;
  creation_version_id: string;
  validation_status: "VALID" | "PENDING_REVIEW" | "REVOKED";
  current_state: LearningState;
  processed_at: string;
}

export interface TeachingEvidenceAccepted {
  evidence_id: string;
  lesson_id: string;
  validation_status: "VALID";
  current_state: LearningState;
  changed: boolean;
  processed_at: string;
}
