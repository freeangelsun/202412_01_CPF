/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmLogPolicyOverrideRequest {
  approvedBy?: string;
  dbLogEnabledYn?: string;
  effectiveEndAt?: string;
  effectiveStartAt?: string;
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
  policyId?: number;
  queryAllowlist?: string;
  queryCaptureMode?: string;
  reason?: string;
  requestBodyCaptureMode?: string;
  requestHeaderCaptureMode?: string;
  responseBodyCaptureMode?: string;
  responseHeaderCaptureMode?: string;
  targetId?: string;
  targetType?: string;
}
