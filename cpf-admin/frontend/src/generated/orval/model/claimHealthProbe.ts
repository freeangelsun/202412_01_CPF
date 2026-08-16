/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface ClaimHealthProbe {
  gatewayInstanceId?: string;
  instanceId?: string;
  leaseSeconds: number;
  serverGroupId?: string;
}
