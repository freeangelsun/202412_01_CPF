/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmBatchLockReleaseRequest {
  approvalRequestId?: string;
  expectedVersion?: number;
  idempotencyKey?: string;
  lockKey?: string;
  reason?: string;
}
