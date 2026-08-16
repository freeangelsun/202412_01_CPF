/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmIpAllowlistRequest {
  description?: string;
  ipPattern?: string;
  reason?: string;
  useYn?: string;
}
