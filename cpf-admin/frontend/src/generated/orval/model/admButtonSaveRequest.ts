/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmButtonSaveRequest {
  actionCode?: string;
  apiPattern?: string;
  buttonId?: string;
  buttonName?: string;
  httpMethod?: string;
  menuId?: string;
  reason?: string;
  sortOrder?: number;
  useYn?: string;
}
