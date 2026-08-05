/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmFeatureFlagRequestOverrideRequest {
  expiresAt?: string;
  flagKey?: string;
  reason?: string;
  value?: string;
  valueType?: string;
}
