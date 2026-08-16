/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface ApplyCommand {
  clientIp?: string;
  operatorId?: string;
  reason?: string;
  rowOperationId?: string;
  values?: Record<string, string>;
}
