/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmOperatorPasswordResetRequest {
  forceChange: boolean;
  newPassword?: string;
  reason?: string;
}
