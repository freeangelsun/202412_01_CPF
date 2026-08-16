/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface ChangeResult {
  after?: Record<string, unknown>;
  before?: Record<string, unknown>;
  reason?: string;
}
