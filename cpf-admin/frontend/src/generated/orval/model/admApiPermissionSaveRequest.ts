/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmApiPermissionSaveRequest {
  apiGroupCode?: string;
  apiName?: string;
  apiPath?: string;
  apiPermissionId?: string;
  buttonId?: string;
  httpMethod?: string;
  menuId?: string;
  permissionCode?: string;
  reason?: string;
  useYn?: string;
}
