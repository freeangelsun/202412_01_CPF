/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface EmployeeRequest {
  adminUserId?: number;
  clearEmail: boolean;
  clearMobileNo: boolean;
  clearOfficePhoneNo: boolean;
  email?: string;
  employeeName?: string;
  employeeNo?: string;
  employmentStatus?: string;
  expectedVersion?: number;
  jobTitleCode?: string;
  joinDate?: string;
  leaveDate?: string;
  managerEmployeeNo?: string;
  mobileNo?: string;
  officePhoneNo?: string;
  organizationCode?: string;
  positionCode?: string;
  reason?: string;
  requestUser?: string;
  useYn?: string;
}
