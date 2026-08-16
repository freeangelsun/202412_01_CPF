/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface IncidentActionRequest {
  approvalRequestId?: string;
  expectedVersion: number;
  idempotencyKey?: string;
  reason?: string;
}
