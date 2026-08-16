/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface ResponsibilityRequest {
  effectiveFrom?: string;
  effectiveTo?: string;
  employeeNo?: string;
  expectedVersion?: number;
  organizationCode?: string;
  priorityNo?: number;
  reason?: string;
  responsibilityId?: number;
  responsibilityType?: string;
  useYn?: string;
}
