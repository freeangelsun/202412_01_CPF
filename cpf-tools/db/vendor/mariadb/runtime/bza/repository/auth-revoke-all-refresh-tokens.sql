UPDATE bza_refresh_token
SET revoked_yn = 'Y', revoked_at = CURRENT_TIMESTAMP, updated_by = 'BZA_AUTH', updated_at = CURRENT_TIMESTAMP
WHERE admin_user_id = :adminUserId AND revoked_yn = 'N'
