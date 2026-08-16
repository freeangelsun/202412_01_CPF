/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface QuerySpec {
  args?: Array<unknown>;
  sql?: string;
}
