/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BzaLogoutResponse {
  loggedOut: boolean;
  loginDomain?: string;
}
