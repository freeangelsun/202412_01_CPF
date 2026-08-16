/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface JobTitleRequest {
  expectedVersion?: number;
  jobTitleCode?: string;
  jobTitleName?: string;
  managerYn?: string;
  reason?: string;
  useYn?: string;
}
