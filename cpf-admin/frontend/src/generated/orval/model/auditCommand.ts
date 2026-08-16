/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AuditCommand {
  actionType?: string;
  beforeData?: string;
  clientIp?: string;
  operatorId?: string;
  reason?: string;
  targetId?: string;
  targetType?: string;
  traceId?: string;
  transactionId?: string;
}
