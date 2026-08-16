import type { CpfDataRow } from './cpfDataRow';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface TransactionMetaPage {
  available: boolean;
  items?: Array<CpfDataRow>;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
