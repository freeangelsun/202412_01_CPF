/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmSessionSummaryResponse {
  clientIp?: string;
  createdAt?: string;
  expiresAt?: string;
  issuedAt?: string;
  operatorId?: string;
  revoked: boolean;
  roleIds?: Array<string>;
  sessionId?: string;
  updatedAt?: string;
  userAgent?: string;
}
