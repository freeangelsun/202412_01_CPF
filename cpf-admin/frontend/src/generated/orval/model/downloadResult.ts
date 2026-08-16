/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface DownloadResult {
  content?: Array<number>;
  contentType?: string;
  downloadId?: number;
  fileName?: string;
  maskedYn?: string;
  rowCount: number;
}
