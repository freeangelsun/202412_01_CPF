UPDATE MBW_BOOTSTRAP_APPROVAL
   SET CLEANUP_STATUS = :cleanupStatus,
       CLEANUP_FAILURE_CODE = :failureCode,
       CLEANUP_UPDATED_AT = :updatedAt,
       UPDATED_AT = :updatedAt
 WHERE TOKEN_HASH = :tokenHash
