/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface LoginCommitResult {
  refreshExpireAt?: string;
  replayed: boolean;
  resultAccessTokenEnc?: string;
  resultRefreshTokenEnc?: string;
}
