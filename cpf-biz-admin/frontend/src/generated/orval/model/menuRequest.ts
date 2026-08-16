/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface MenuRequest {
  apiPath?: string;
  environmentCode?: string;
  expectedVersion?: number;
  iconCode?: string;
  menuCode?: string;
  menuName?: string;
  moduleCode?: string;
  parentMenuCode?: string;
  reason?: string;
  requestUser?: string;
  routePath?: string;
  sortOrder?: number;
  useYn?: string;
}
