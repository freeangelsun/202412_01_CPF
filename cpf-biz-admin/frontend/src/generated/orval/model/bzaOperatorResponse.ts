/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BzaOperatorResponse {
  accountStatus?: string;
  buttons?: Array<string>;
  failCount: number;
  lastLoginAt?: string;
  lockYn?: string;
  loginId?: string;
  menus?: Array<string>;
  operatorId: number;
  operatorName?: string;
  passwordChangeRequiredYn?: string;
  passwordExpireAt?: string;
  roleCode?: string;
  useYn?: string;
}
