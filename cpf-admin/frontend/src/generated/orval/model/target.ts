/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Target {
  endpointCode?: string;
  instanceId?: string;
  serviceId?: string;
  version: number;
}
