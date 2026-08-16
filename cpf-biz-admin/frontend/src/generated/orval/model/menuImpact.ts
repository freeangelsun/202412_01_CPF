/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface MenuImpact {
  deletable: boolean;
  descendantCount: number;
  menuCode?: string;
  permissionCount: number;
  routePath?: string;
}
