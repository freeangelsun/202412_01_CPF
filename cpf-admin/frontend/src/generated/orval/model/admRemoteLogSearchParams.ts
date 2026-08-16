/** Generated from OpenAPI query/header parameters. */
export interface AdmRemoteLogSearchParams {
  environment?: string;
  module?: string;
  service?: string;
  instance?: string;
  logType?: string;
  fileName?: string;
  standardTransactionId?: string;
  standardBatchId?: string;
  transactionId?: string;
  segmentId?: string;
  jobInstanceId?: string;
  jobExecutionId?: string;
  stepExecutionId?: string;
  schedulerId?: string;
  modifiedFrom?: string;
  modifiedTo?: string;
  minSize?: number;
  maxSize?: number;
  compressed?: boolean;
  active?: boolean;
  limit?: number;
}
