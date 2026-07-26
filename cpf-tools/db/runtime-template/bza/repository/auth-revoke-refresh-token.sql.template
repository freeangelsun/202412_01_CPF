UPDATE bza_refresh_token
SET revoked_yn = 'Y', revoked_at = NOW(), updated_by = 'BZA_AUTH', updated_at = NOW()
WHERE refresh_token_hash = :refreshTokenHash AND revoked_yn = 'N' AND expire_at > NOW()
