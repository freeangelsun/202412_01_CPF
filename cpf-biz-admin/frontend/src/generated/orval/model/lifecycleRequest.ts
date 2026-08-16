/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface LifecycleRequest {
  comment?: string;
  idempotencyKey?: string;
  reason?: string;
}
