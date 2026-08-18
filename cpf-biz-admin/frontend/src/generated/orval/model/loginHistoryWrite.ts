/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface LoginHistoryWrite {
  adminLoginId?: string;
  adminUserId?: number;
  clientIp?: string;
  failureReason?: string;
  instanceId?: string;
  loginDomain?: string;
  loginResult?: string;
  moduleId?: string;
  transactionId?: string;
  userAgent?: string;
  wasId?: string;
}
