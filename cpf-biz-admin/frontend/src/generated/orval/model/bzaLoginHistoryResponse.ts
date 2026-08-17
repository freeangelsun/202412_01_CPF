/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BzaLoginHistoryResponse {
  clientIp?: string;
  createdAt?: string;
  failureReason?: string;
  historyId: number;
  loginId?: string;
  moduleId?: string;
  operatorId?: number;
  instanceId?: string;
  successYn?: string;
  transactionId?: string;
  userAgent?: string;
  wasId?: string;
}
