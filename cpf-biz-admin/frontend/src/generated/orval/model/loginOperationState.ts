/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface LoginOperationState {
  adminUserId: number;
  failureCode?: string;
  failureMessage?: string;
  loginId?: string;
  operationId?: string;
  requestHash?: string;
  resultAccessTokenEnc?: string;
  resultExpiresAt?: string;
  resultRefreshExpiresAt?: string;
  resultRefreshTokenEnc?: string;
  status?: string;
}
