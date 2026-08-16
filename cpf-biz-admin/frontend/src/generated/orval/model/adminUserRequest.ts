/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdminUserRequest {
  accountStatus?: string;
  adminName?: string;
  expectedVersion?: number;
  lockYn?: string;
  loginId?: string;
  passwordChangeRequiredYn?: string;
  rawPassword?: string;
  reason?: string;
  requestUser?: string;
  roleCode?: string;
  useYn?: string;
}
