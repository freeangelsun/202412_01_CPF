/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface RetentionVersionedReasonRequest {
  expectedVersion: number;
  reason: string;
}
