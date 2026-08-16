import type { AdmFileJobState } from './admFileJobState';
import type { AdmFileJobType } from './admFileJobType';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Job {
  appliedBy?: string;
  approvalId?: string;
  clientIp?: string;
  controlBy?: string;
  controlReason?: string;
  controlUpdatedAt?: string;
  createdAt?: string;
  dryRun: boolean;
  errorCode?: string;
  errorMessage?: string;
  failedRows: number;
  fencingToken: number;
  format?: string;
  jobId?: string;
  jobType?: AdmFileJobType;
  leaseOwner?: string;
  leaseUntil?: string;
  operationId?: string;
  reason?: string;
  requestHash?: string;
  requestedBy?: string;
  resolvedBy?: string;
  resultPath?: string;
  resultSha256?: string;
  retentionUntil?: string;
  rollbackSupported: boolean;
  sourcePath?: string;
  sourceSha256?: string;
  state?: AdmFileJobState;
  successRows: number;
  templateCode?: string;
  templateVersion: number;
  totalRows: number;
  updatedAt?: string;
}
