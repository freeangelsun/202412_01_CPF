/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRoleSaveRequest {
  description?: string;
  reason?: string;
  roleId?: string;
  roleName?: string;
  roleType?: string;
  useYn?: string;
}
