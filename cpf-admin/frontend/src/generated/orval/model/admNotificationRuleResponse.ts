/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmNotificationRuleResponse {
  channelCode?: string;
  createdAt?: string;
  createdBy?: string;
  eventSubType?: string;
  eventType?: string;
  receiverGroup?: string;
  ruleId: number;
  severity?: string;
  templateCode?: string;
  updatedAt?: string;
  updatedBy?: string;
  useYn?: string;
}
