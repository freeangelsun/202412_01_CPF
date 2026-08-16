/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmChannelPolicySaveRequest {
  active: boolean;
  allowed: boolean;
  authenticationRequired: boolean;
  callerChannelCode: string;
  effectiveFrom?: string;
  effectiveTo?: string;
  maxTps: number;
  originalChannelCode: string;
  reason: string;
  requestType: string;
  signatureRequired: boolean;
  standardExecutionId: string;
}
