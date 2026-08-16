/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AttachmentSecurityRequest {
  dataClassification?: string;
  quarantineYn?: string;
  reason?: string;
  retentionUntil?: string;
  scanStatus?: string;
  useYn?: string;
}
