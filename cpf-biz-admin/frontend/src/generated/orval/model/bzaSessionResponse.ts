/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BzaSessionResponse {
  createdAt?: string;
  expiresAt?: string;
  loginDomain?: string;
  loginId?: string;
  operatorId: number;
  revokedYn?: string;
  sessionId: number;
  transactionId?: string;
  updatedAt?: string;
}
