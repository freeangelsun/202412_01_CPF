/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmFileJobRowResponse {
  businessKey?: string;
  errorCode?: string;
  message?: string;
  rowNumber: number;
  state?: string;
}
