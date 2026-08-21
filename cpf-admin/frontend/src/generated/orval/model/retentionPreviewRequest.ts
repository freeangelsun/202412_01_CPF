/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface RetentionPreviewRequest {
  action: string;
  cutoff: string;
  legalHold: boolean;
  limit: number;
  reason: string;
  target: string;
}
