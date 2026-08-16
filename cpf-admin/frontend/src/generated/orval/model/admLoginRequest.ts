/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmLoginRequest {
  operatorId?: string;
  otpCode?: string;
  password?: string;
}
