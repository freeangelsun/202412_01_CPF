/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmMenuPermissionUpdateRequest {
  deleteYn?: string;
  readYn?: string;
  reason?: string;
  writeYn?: string;
}
