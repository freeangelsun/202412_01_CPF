/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface DecisionCommand {
  action?: string;
  idempotencyKey?: string;
  reason?: string;
}
