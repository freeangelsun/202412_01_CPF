/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmNotificationRuleRequest {
  channelCode?: string;
  eventSubType?: string;
  eventType?: string;
  reason?: string;
  receiverGroup?: string;
  severity?: string;
  templateCode?: string;
  useYn?: string;
}
