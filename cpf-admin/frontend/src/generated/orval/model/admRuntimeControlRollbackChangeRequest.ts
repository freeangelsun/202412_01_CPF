/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRuntimeControlRollbackChangeRequest {
  commandId?: string;
  reason?: string;
}
