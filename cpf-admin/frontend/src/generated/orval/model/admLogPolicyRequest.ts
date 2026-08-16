/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmLogPolicyRequest {
  activeYn?: string;
  dbLogEnabledYn?: string;
  description?: string;
  errorStackCaptureMode?: string;
  fieldAllowlist?: string;
  fileLogEnabledYn?: string;
  headerAllowlist?: string;
  logLevel?: string;
  maskingPolicyKey?: string;
  maxHeaderBytes?: number;
  maxQueryBytes?: number;
  maxRequestBodyBytes?: number;
  maxResponseBodyBytes?: number;
  maxStackBytes?: number;
  policyKey?: string;
  policyName?: string;
  priority?: number;
  queryAllowlist?: string;
  queryCaptureMode?: string;
  reason?: string;
  requestBodyCaptureMode?: string;
  requestHeaderCaptureMode?: string;
  responseBodyCaptureMode?: string;
  responseHeaderCaptureMode?: string;
  retentionDays?: number;
  samplingRate?: number;
  targetId?: string;
  targetType?: string;
}
