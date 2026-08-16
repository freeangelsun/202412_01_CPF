/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface DownloadRequest {
  downloadType?: string;
  fromDate?: string;
  includeSensitive?: boolean;
  jobId?: string;
  limit?: number;
  reason?: string;
  targetType?: string;
  toDate?: string;
  traceId?: string;
  transactionId?: string;
}
