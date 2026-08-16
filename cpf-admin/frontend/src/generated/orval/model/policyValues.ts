/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface PolicyValues {
  activeYn?: string;
  dbLogEnabledYn?: string;
  description?: string;
  errorStackCaptureMode?: string;
  errorStackLogYn?: string;
  fieldAllowlist?: string;
  fileLogEnabledYn?: string;
  headerAllowlist?: string;
  logLevel?: string;
  maskingPolicyKey?: string;
  maxHeaderBytes: number;
  maxQueryBytes: number;
  maxRequestBodyBytes: number;
  maxResponseBodyBytes: number;
  maxStackBytes: number;
  policyChecksum?: string;
  policyKey?: string;
  policyName?: string;
  priority: number;
  queryAllowlist?: string;
  queryCaptureMode?: string;
  requestBodyCaptureMode?: string;
  requestBodyLogYn?: string;
  requestHeaderCaptureMode?: string;
  responseBodyCaptureMode?: string;
  responseBodyLogYn?: string;
  responseHeaderCaptureMode?: string;
  retentionDays: number;
  samplingRate?: number;
  targetId?: string;
  targetType?: string;
  user?: string;
}
