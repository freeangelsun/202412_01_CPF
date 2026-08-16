/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface DeleteTarget {
  id?: string;
  targetType?: string;
  version: number;
}
