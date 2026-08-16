/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface ApprovalState {
  adminUserId?: number;
  claimExpiresAt?: string;
  claimOwnerId?: string;
  claimedAt?: string;
  cleanupFailureCode?: string;
  cleanupStatus?: string;
  cleanupUpdatedAt?: string;
  completedAt?: string;
  environmentFingerprint?: string;
  expiresAt?: string;
  failureCode?: string;
  operationId?: string;
  status?: string;
  tokenHash?: string;
}
