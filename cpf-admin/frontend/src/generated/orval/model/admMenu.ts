/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmMenu {
  deleteAllowed: boolean;
  menuId?: string;
  menuName?: string;
  parentMenuId?: string;
  path?: string;
  readAllowed: boolean;
  sortOrder: number;
  writeAllowed: boolean;
}
