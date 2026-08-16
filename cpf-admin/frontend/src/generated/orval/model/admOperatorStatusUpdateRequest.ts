/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmOperatorStatusUpdateRequest {
  accountStatus?: string;
  expectedVersion?: number;
  reason?: string;
}
