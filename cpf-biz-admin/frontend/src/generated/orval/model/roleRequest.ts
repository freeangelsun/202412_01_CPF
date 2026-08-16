/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface RoleRequest {
  dataScope?: string;
  expectedVersion?: number;
  reason?: string;
  requestUser?: string;
  roleCode?: string;
  roleName?: string;
  useYn?: string;
  writeAllowedYn?: string;
}
