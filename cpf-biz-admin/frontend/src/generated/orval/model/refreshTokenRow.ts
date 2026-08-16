/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface RefreshTokenRow {
  adminUserId: number;
  expiresAt?: string;
  loginDomain?: string;
  loginId?: string;
  refreshTokenHash?: string;
  revoked: boolean;
  transactionId?: string;
}
