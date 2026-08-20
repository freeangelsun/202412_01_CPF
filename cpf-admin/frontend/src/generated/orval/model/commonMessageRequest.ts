/** ADM message create/update input. The authenticated operator is resolved from the server session; requestUser is not accepted. */
export interface CommonMessageRequest {
  messageId?: number;
  messageCode?: string;
  messageKey?: string;
  locale: string;
  messageFormatType?: "FIXED" | "INDEXED";
  externalMessage?: string;
  internalMessage?: string;
  messageValue?: string;
  parameterCount?: number;
  parameterSample?: string;
  description?: string;
  useYn?: "Y" | "N";
  reason: string;
}
