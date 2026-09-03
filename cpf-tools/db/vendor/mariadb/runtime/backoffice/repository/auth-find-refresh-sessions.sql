SELECT refresh_token_id AS sessionId, login_domain AS loginDomain,
       transaction_id AS transactionId, expire_at AS expiresAt,
       revoked_yn AS revokedYn, revoked_at AS revokedAt,
       created_at AS createdAt, updated_at AS updatedAt
FROM MBW_REFRESH_TOKEN
WHERE admin_user_id = :adminUserId
ORDER BY refresh_token_id DESC
LIMIT :limit
