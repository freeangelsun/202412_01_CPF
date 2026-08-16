/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmOperatorContactUpdateRequest {
  clearMobileNo: boolean;
  clearOfficePhoneNo: boolean;
  expectedVersion?: number;
  mobileNo?: string;
  officePhoneNo?: string;
  reason?: string;
}
