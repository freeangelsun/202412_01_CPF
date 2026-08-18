/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmChannelPolicySaveRequest {
  active: boolean;
  allowed: boolean;
  authenticationRequired: boolean;
  callerChannel: string;
  effectiveFrom?: string;
  effectiveTo?: string;
  maxTps: number;
  operationId: string;
  reason: string;
  signatureRequired: boolean;
}
