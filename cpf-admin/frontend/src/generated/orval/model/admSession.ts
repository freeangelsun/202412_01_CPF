/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmSession {
  expiresAt?: string;
  issuedAt?: string;
  operatorId?: string;
  passwordChangeRequired: boolean;
  roleIds?: Array<string>;
  token?: string;
}
