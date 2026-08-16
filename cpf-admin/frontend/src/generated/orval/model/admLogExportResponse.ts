/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmLogExportResponse {
  downloadUrl?: string;
  expiresAt?: string;
  exportId?: string;
  fileName?: string;
  maskedContent?: string;
  status?: string;
  watermark?: string;
}
