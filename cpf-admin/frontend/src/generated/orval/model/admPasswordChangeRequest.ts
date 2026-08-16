/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmPasswordChangeRequest {
  currentPassword?: string;
  newPassword?: string;
  newPasswordConfirm?: string;
  reason?: string;
}
