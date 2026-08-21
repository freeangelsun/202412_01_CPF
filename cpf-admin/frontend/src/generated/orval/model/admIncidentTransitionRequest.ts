/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmIncidentTransitionRequest {
  reason: string;
  status: string;
}
