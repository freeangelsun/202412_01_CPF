/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BzaEmployeeRawContactResponse {
  email?: string;
  employeeNo?: string;
  mobileNo?: string;
  officePhoneNo?: string;
  rawViewAllowed: boolean;
  transactionId?: string;
}
