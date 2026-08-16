/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface ApprovalMutationResult {
  body?: Record<string, unknown>;
  created: boolean;
  replayed: boolean;
}
