/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Row {
  businessKey?: string;
  errorCode?: string;
  jobId?: string;
  message?: string;
  payload?: Record<string, string>;
  rollbackToken?: string;
  rowNumber: number;
  state?: string;
}
