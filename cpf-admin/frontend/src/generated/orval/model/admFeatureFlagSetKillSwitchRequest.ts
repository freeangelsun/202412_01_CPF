/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmFeatureFlagSetKillSwitchRequest {
  enabled: boolean;
  reason?: string;
}
