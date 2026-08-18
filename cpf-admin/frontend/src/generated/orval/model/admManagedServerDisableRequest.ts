/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmManagedServerDisableRequest {
  expectedVersion: number;
  reason?: string;
}
