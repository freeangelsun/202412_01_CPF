import type { BzaOperatorRow } from './bzaOperatorRow';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface LoginSuccessCommand {
  clientIp?: string;
  moduleId?: string;
  operationId?: string;
  operator?: BzaOperatorRow;
  previousPasswordHash?: string;
  refreshExpireAt?: string;
  refreshTokenHash?: string;
  requestHash?: string;
  resultAccessTokenEnc?: string;
  resultExpireAt?: string;
  resultRefreshTokenEnc?: string;
  instanceId?: string;
  transactionId?: string;
  upgradedPasswordHash?: string;
  userAgent?: string;
  wasId?: string;
}
