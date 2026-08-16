/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BzaSessionRevokeResponse {
  revoked: boolean;
  sessionId: number;
}
