INSERT INTO bza_refresh_token (
    admin_user_id,
    login_domain,
    refresh_token_hash,
    transaction_id,
    expire_at,
    revoked_yn,
    created_by,
    updated_by
)
VALUES (
    :adminUserId,
    :loginDomain,
    :refreshTokenHash,
    :transactionId,
    :expireAt,
    'N',
    'BZA_AUTH',
    'BZA_AUTH'
)
