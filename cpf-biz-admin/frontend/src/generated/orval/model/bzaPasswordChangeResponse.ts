/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BzaPasswordChangeResponse {
  changed: boolean;
  loginId?: string;
  refreshTokensRevoked: boolean;
}
