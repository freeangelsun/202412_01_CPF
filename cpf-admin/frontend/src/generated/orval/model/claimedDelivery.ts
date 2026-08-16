import type { AdmNotificationRuleResponse } from './admNotificationRuleResponse';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface ClaimedDelivery {
  attemptCount: number;
  deliveryId: number;
  maxAttempts: number;
  operationId?: string;
  payloadBody?: string;
  receiver?: string;
  requestUser?: string;
  rule?: AdmNotificationRuleResponse;
  targetId?: string;
  targetType?: string;
  version: number;
}
