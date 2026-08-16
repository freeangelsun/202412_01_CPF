/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmIntegrationDataQualityCorrectionApprovalRequestRequest {
  corrected: Record<string, unknown>;
  expectedVersion: number;
  idempotencyKey: string;
  reason: string;
}
