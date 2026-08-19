import type { AdmManagedServerResponse } from './admManagedServerResponse';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmManagedServerPageResponse {
  hasNext: boolean;
  items?: Array<AdmManagedServerResponse>;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
