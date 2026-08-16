/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmOperatorRoleUpdateRequest {
  reason?: string;
  roleIds?: Array<string>;
}
