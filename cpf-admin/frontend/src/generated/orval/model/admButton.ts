/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmButton {
  actionCode?: string;
  apiPattern?: string;
  buttonId?: string;
  buttonName?: string;
  createdAt?: string;
  httpMethod?: string;
  menuId?: string;
  sortOrder: number;
  updatedAt?: string;
  useYn?: string;
}
