/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmLogExportRequest {
  action?: string;
  format?: string;
  logId?: string;
  reason?: string;
  requestedBy?: string;
}
