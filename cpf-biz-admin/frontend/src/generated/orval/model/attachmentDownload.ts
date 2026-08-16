/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AttachmentDownload {
  checksumSha256?: string;
  content?: Array<number>;
  contentType?: string;
  fileName?: string;
}
