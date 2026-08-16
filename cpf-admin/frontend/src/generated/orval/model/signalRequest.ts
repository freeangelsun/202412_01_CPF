/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface SignalRequest {
  correlationId?: string;
  idempotencyKey?: string;
  occurredAt?: string;
  policyCode?: string;
  sourceId?: string;
  sourceType?: string;
  summary?: string;
  title?: string;
  transactionId?: string;
}
