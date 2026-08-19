import type { AdmRuntimeInventoryResponse } from './admRuntimeInventoryResponse';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRuntimeInventoryPageResponse {
  hasNext: boolean;
  items?: Array<AdmRuntimeInventoryResponse>;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
