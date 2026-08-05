/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmSecretRotateRequest {
  key?: string;
  provider?: string;
  reason?: string;
}
