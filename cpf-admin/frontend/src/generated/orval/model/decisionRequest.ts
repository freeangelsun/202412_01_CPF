/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface DecisionRequest {
  action: string;
  breakGlass?: boolean;
  idempotencyKey: string;
  reason: string;
}
