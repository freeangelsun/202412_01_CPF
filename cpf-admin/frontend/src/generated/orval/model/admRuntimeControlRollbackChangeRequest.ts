/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRuntimeControlRollbackChangeRequest {
  operationId?: string;
  reason?: string;
}
