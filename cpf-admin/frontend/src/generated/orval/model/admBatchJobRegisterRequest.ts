/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmBatchJobRegisterRequest {
  description?: string;
  jobId?: string;
  jobName?: string;
  jobType?: string;
  reason?: string;
}
