/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmBatchOperationRequest {
  approvalRequestId?: string;
  expectedVersion?: number;
  idempotencyKey?: string;
  jobParameters?: string;
  reason?: string;
}
