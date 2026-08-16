import type { AdmApprovalDirectoryEntry } from './admApprovalDirectoryEntry';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Resolved {
  entry?: AdmApprovalDirectoryEntry;
  stepNo: number;
  targetCode?: string;
  targetType?: string;
}
