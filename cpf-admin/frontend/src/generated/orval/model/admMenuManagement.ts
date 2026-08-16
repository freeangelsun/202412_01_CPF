/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmMenuManagement {
  createdAt?: string;
  menuId?: string;
  menuName?: string;
  menuPath?: string;
  parentMenuId?: string;
  sortOrder: number;
  updatedAt?: string;
  useYn?: string;
}
