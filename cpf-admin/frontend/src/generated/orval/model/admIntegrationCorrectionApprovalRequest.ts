/** Generated from CPF OpenAPI. */
export interface AdmIntegrationCorrectionApprovalRequest {
  expectedVersion: number;
  idempotencyKey: string;
  reason: string;
  corrected: Record<string, unknown>;
}
