/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface SavedSearchRequest {
  criteriaJson?: string;
  reason?: string;
  screenCode?: string;
  searchName?: string;
  sharedYn?: string;
}
