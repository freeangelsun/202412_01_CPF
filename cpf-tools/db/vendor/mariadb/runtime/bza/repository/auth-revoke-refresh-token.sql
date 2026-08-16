UPDATE bza_refresh_token
SET revoked_yn = 'Y', revoked_at = CURRENT_TIMESTAMP, updated_by = 'BZA_AUTH', updated_at = CURRENT_TIMESTAMP
WHERE refresh_token_hash = :refreshTokenHash AND revoked_yn = 'N' AND expire_at > CURRENT_TIMESTAMP
