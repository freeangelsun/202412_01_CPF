/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRuntimeInventoryResponse {
  applicationName?: string;
  applicationRole?: string;
  artifactVersion?: string;
  capabilities?: Record<string, string>;
  cpfVersion?: string;
  environment?: string;
  instanceId?: string;
  javaVersion?: string;
  lastSeenAt?: string;
  managedServerId?: string;
  runtimeHostname?: string;
  serverName?: string;
  serviceId?: string;
  startedAt?: string;
  status?: string;
  systemCode?: string;
  zone?: string;
}
