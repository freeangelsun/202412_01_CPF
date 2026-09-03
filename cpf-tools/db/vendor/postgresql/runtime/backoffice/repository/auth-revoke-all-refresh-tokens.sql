UPDATE MBW_REFRESH_TOKEN
SET revoked_yn = 'Y', revoked_at = CURRENT_TIMESTAMP, updated_by = 'MBW_AUTH', updated_at = CURRENT_TIMESTAMP
WHERE admin_user_id = :adminUserId AND revoked_yn = 'N'
