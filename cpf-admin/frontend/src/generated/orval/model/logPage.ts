/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface LogPage {
  cursor?: number;
  hasMore: boolean;
  items?: Array<Record<string, unknown>>;
  nextCursor?: number;
  pageSize: number;
  total: number;
}
