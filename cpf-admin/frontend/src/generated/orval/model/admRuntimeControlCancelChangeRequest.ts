/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRuntimeControlCancelChangeRequest {
  commandId?: string;
  reason?: string;
}
