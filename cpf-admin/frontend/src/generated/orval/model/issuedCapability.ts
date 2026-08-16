/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface IssuedCapability {
  approvedAt?: string;
  expiresAt?: string;
  nonce?: string;
  proof?: string;
}
