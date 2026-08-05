import type { UnknownResolution } from './unknownResolution';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmFileJobResolveUnknownRequest {
  approvalId?: string;
  businessKey?: string;
  reason?: string;
  resolution?: UnknownResolution;
  rollbackToken?: string;
  rowNumber: number;
}
