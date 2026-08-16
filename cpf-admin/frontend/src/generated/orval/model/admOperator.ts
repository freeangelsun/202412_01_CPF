/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmOperator {
  accountStatus?: string;
  createdAt?: string;
  locked: boolean;
  mobileNo?: string;
  officePhoneNo?: string;
  operatorId?: string;
  operatorName?: string;
  passwordChangeRequired: boolean;
  passwordExpired: boolean;
  rawViewAllowed: boolean;
  roleIds?: Array<string>;
  updatedAt?: string;
  versionNo: number;
}
