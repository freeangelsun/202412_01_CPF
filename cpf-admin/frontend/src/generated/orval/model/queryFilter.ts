/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface QueryFilter {
  args?: Array<unknown>;
  sql?: string;
}
