/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface PolicyResponse {
  createdAt?: string;
  createdBy?: string;
  escalationMinutes: number;
  eventSubType?: string;
  eventType?: string;
  policyCode?: string;
  policyId: number;
  receiverGroup?: string;
  severity?: string;
  thresholdCount: number;
  updatedAt?: string;
  updatedBy?: string;
  useYn?: string;
  version: number;
  windowSeconds: number;
}
