SELECT TOKEN_HASH AS tokenHash,
       ENV_FINGERPRINT AS environmentFingerprint,
       STATUS AS status,
       OPERATION_ID AS operationId,
       EXPIRES_AT AS expiresAt,
       CLAIMED_AT AS claimedAt,
       CLAIM_OWNER_ID AS claimOwnerId,
       CLAIM_EXPIRES_AT AS claimExpiresAt,
       COMPLETED_AT AS completedAt,
       ADMIN_USER_ID AS adminUserId,
       FAILURE_CODE AS failureCode,
       CLEANUP_STATUS AS cleanupStatus,
       CLEANUP_FAILURE_CODE AS cleanupFailureCode,
       CLEANUP_UPDATED_AT AS cleanupUpdatedAt
  FROM MBW_BOOTSTRAP_APPROVAL
 WHERE OPERATION_ID = :value
