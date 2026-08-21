/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmIncidentCreateRequest {
  reason: string;
  severity: string;
  sourceId?: string;
  sourceType?: string;
  summary?: string;
  title: string;
}
