SELECT refresh_token_id AS sessionId, login_domain AS loginDomain,
       transaction_global_id AS transactionGlobalId, expire_at AS expiresAt,
       revoked_yn AS revokedYn, revoked_at AS revokedAt,
       created_at AS createdAt, updated_at AS updatedAt
FROM bza_refresh_token
WHERE admin_user_id = :adminUserId
ORDER BY refresh_token_id DESC
OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY
