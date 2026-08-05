/** ADM notification rule input. The authenticated operator is resolved from the server session; requestUser is not accepted. */
export interface AdmNotificationRuleRequest {
  eventType: string;
  eventSubType?: string;
  channelCode?: string;
  templateCode?: string;
  severity?: "TRACE" | "DEBUG" | "INFO" | "WARN" | "ERROR" | "CRITICAL";
  receiverGroup?: string;
  useYn?: "Y" | "N";
  reason: string;
}
