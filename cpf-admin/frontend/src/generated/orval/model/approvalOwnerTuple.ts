/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface ApprovalOwnerTuple {
  actionType?: string;
  ownerCommand?: string;
  ownerModule?: string;
  targetType?: string;
}
