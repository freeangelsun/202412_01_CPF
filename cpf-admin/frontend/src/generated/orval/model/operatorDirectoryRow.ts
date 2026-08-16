/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface OperatorDirectoryRow {
  accountStatus?: string;
  createdAt?: string;
  locked: boolean;
  mobileNo?: string;
  officePhoneNo?: string;
  operatorId?: string;
  operatorName?: string;
  passwordChangeRequired: boolean;
  passwordChangedAt?: string;
  updatedAt?: string;
  versionNo: number;
}
