UPDATE MBW_BOOTSTRAP_APPROVAL
   SET STATUS = 'CLAIMED', OPERATION_ID = :operationId, CLAIM_OWNER_ID = :claimOwnerId,
       CLAIMED_AT = :claimedAt, CLAIM_EXPIRES_AT = :claimExpiresAt,
       UPDATED_AT = :claimedAt
 WHERE TOKEN_HASH = :tokenHash
   AND ENV_FINGERPRINT = :environmentFingerprint
   AND EXPIRES_AT > :claimedAt
   AND (
        STATUS = 'APPROVED'
        OR (STATUS = 'CLAIMED' AND OPERATION_ID = :operationId AND CLAIM_EXPIRES_AT <= :claimedAt)
   )
