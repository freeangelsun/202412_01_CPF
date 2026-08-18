/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BzaLoginHistoryResponse {
  clientIp?: string;
  createdAt?: string;
  failureReason?: string;
  historyId: number;
  instanceId?: string;
  loginId?: string;
  moduleId?: string;
  operatorId?: number;
  successYn?: string;
  transactionId?: string;
  userAgent?: string;
  wasId?: string;
}
