import type { InstanceCommand } from './instanceCommand';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmServiceRegistryChangeInstanceStateRequest {
  command?: InstanceCommand;
  expectedVersion?: number;
  operationId?: string;
  reason?: string;
}
