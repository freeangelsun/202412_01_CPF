/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmBreakGlassOpenRequest {
  reason: string;
  scopeType: string;
  scopeValue: string;
  ttlMinutes: number;
}
