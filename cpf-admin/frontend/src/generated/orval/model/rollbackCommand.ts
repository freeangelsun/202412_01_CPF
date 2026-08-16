/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface RollbackCommand {
  clientIp?: string;
  operatorId?: string;
  reason?: string;
  rollbackToken?: string;
  rowOperationId?: string;
}
