/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BzaOperatorRow {
  accountStatus?: string;
  adminName?: string;
  adminUserId: number;
  buttons?: Array<string>;
  lastLoginAt?: string;
  lockYn?: string;
  loginFailCount: number;
  loginId?: string;
  menus?: Array<string>;
  passwordChangeRequiredYn?: string;
  passwordExpireAt?: string;
  passwordHash?: string;
  roleCode?: string;
  useYn?: string;
}
