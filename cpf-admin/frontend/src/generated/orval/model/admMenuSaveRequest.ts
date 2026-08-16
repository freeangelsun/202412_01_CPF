/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmMenuSaveRequest {
  menuId?: string;
  menuName?: string;
  menuPath?: string;
  parentMenuId?: string;
  reason?: string;
  sortOrder?: number;
  useYn?: string;
}
