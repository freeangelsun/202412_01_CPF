/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmCenterCutActionRequest {
  approvalRequestId?: string;
  idempotencyKey?: string;
  reason?: string;
}
