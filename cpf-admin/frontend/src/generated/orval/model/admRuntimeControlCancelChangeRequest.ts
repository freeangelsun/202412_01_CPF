/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRuntimeControlCancelChangeRequest {
  operationId?: string;
  reason?: string;
}
