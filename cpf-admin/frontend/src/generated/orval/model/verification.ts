/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Verification {
  calculatedHash?: string;
  storedHash?: string;
  valid: boolean;
}
