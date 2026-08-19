/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmManagedServerResponse {
  activeRuntimeCount: number;
  description?: string;
  displayName?: string;
  enabled: boolean;
  environment?: string;
  hostname?: string;
  location?: string;
  managedServerId?: string;
  managementIdentity?: string;
  registeredAt?: string;
  rowVersion: number;
  runtimeCount: number;
  serverGroup?: string;
  serverName?: string;
  status?: string;
  tagsJson?: string;
  updatedAt?: string;
  zone?: string;
}
