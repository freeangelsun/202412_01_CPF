import type { AdmApprovalExecutionStatus } from './admApprovalExecutionStatus';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmApprovedOperationResult {
  maskedMessage?: string;
  resultCode?: string;
  status?: AdmApprovalExecutionStatus;
}
