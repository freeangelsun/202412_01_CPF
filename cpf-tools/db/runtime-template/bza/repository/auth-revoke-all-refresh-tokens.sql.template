UPDATE bza_refresh_token
SET revoked_yn = 'Y', revoked_at = NOW(), updated_by = 'BZA_AUTH', updated_at = NOW()
WHERE admin_user_id = :adminUserId AND revoked_yn = 'N'
