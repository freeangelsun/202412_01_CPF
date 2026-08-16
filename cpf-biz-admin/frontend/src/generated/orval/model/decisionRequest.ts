/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface DecisionRequest {
  action?: string;
  comment?: string;
  idempotencyKey?: string;
  reason?: string;
}
