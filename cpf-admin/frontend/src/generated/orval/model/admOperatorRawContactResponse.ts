/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmOperatorRawContactResponse {
  mobileNo?: string;
  officePhoneNo?: string;
  operatorId?: string;
  rawViewAllowed: boolean;
  transactionId?: string;
}
