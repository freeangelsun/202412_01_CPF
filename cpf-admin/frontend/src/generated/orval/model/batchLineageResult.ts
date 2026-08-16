/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BatchLineageResult {
  applicable: boolean;
  failureType?: string;
  queryFailed: boolean;
  rows?: Array<Record<string, unknown>>;
}
