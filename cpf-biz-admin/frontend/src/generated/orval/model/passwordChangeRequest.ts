/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface PasswordChangeRequest {
  currentPassword?: string;
  newPassword?: string;
  newPasswordConfirm?: string;
}
