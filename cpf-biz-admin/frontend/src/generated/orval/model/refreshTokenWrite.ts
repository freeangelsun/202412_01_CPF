/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface RefreshTokenWrite {
  adminUserId: number;
  expireAt?: string;
  loginDomain?: string;
  loginOperationId?: string;
  refreshTokenHash?: string;
  transactionId?: string;
}
