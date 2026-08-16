/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface MenuDeleteResult {
  deleted: boolean;
  deletedVersion: number;
  menuCode?: string;
  operatorId?: string;
}
