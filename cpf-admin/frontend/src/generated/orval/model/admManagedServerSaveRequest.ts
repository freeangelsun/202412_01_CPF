/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmManagedServerSaveRequest {
  description?: string;
  displayName?: string;
  environment?: string;
  expectedVersion?: number;
  hostname?: string;
  location?: string;
  managedServerId?: string;
  managementIdentity?: string;
  reason?: string;
  serverGroup?: string;
  serverName?: string;
  tagsJson?: string;
  zone?: string;
}
