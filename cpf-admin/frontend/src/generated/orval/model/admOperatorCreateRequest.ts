/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmOperatorCreateRequest {
  mobileNo?: string;
  officePhoneNo?: string;
  operationId?: string;
  operatorId?: string;
  operatorName?: string;
  password?: string;
  reason?: string;
  roleIds?: Array<string>;
}
