/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface SessionRow {
  expiresAt?: string;
  issuedAt?: string;
  operatorId?: string;
  passwordChangeRequired: boolean;
}
