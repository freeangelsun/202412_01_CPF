import type { AdmNotificationRuleResponse } from './admNotificationRuleResponse';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmNotificationTestSendResponse {
  deliveryId: number;
  deliveryStatus?: string;
  providerVerification?: string;
  rule?: AdmNotificationRuleResponse;
}
