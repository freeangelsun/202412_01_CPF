/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmApiPermission {
  apiGroupCode?: string;
  apiName?: string;
  apiPath?: string;
  apiPermissionId?: string;
  buttonId?: string;
  createdAt?: string;
  httpMethod?: string;
  menuId?: string;
  permissionCode?: string;
  updatedAt?: string;
  useYn?: string;
}
