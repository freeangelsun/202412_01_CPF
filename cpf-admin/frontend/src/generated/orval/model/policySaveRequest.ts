/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface PolicySaveRequest {
  approvalRequestId?: string;
  escalationMinutes: number;
  eventSubType?: string;
  eventType?: string;
  expectedVersion: number;
  idempotencyKey?: string;
  policyCode?: string;
  reason?: string;
  receiverGroup?: string;
  severity?: string;
  thresholdCount: number;
  useYn?: string;
  windowSeconds: number;
}
