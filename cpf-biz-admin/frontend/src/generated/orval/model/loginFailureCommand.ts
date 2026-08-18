/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface LoginFailureCommand {
  adminUserId?: number;
  clientIp?: string;
  increaseFailCount: boolean;
  instanceId?: string;
  loginId?: string;
  moduleId?: string;
  reason?: string;
  transactionId?: string;
  userAgent?: string;
  wasId?: string;
}
