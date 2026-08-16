/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmIntegrationDataQualityReplayRequest {
  expectedVersion: number;
  idempotencyKey: string;
  reason: string;
}
