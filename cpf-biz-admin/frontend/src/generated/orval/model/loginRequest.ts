/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface LoginRequest {
  loginId?: string;
  operationId?: string;
  password?: string;
}
