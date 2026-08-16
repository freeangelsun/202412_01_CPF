/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface MenuDeleteRequest {
  expectedVersion?: number;
  operationId?: string;
  reason?: string;
}
