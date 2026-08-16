/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface OrganizationRequest {
  effectiveFrom?: string;
  effectiveTo?: string;
  expectedVersion?: number;
  organizationCode?: string;
  organizationName?: string;
  organizationType?: string;
  parentOrganizationCode?: string;
  reason?: string;
  requestUser?: string;
  sortOrder?: number;
  useYn?: string;
}
