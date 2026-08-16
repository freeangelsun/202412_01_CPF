/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmApprovedOperationCommand {
  actionType?: string;
  approvalRequestId: number;
  approvedBy?: string;
  commandRequestId?: string;
  fenceToken: number;
  leaseOwner?: string;
  ownerCommand?: string;
  ownerModule?: string;
  payloadHash?: string;
  payloadSnapshot?: string;
  reason?: string;
  requestedBy?: string;
  targetId?: string;
  targetType?: string;
  transactionId?: string;
}
