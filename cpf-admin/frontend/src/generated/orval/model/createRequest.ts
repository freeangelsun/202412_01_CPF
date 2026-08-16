/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface CreateRequest {
  actionType: string;
  expireAt?: string;
  ownerCommand: string;
  ownerModule: string;
  payloadSnapshot: string;
  policyCode?: string;
  policyVersion?: number;
  reason: string;
  requestKey: string;
  targetId: string;
  targetType: string;
}
