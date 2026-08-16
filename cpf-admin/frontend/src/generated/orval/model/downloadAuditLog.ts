/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface DownloadAuditLog {
  adminId?: string;
  completedAt?: string;
  downloadId?: number;
  downloadType?: string;
  includeSensitiveYn?: string;
  maskedYn?: string;
  reason?: string;
  requestedAt?: string;
  rowCount?: number;
  status?: string;
}
