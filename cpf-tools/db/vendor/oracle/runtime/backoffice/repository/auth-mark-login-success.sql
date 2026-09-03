UPDATE MBW_ADMIN_USER
SET login_fail_count = 0, last_login_at = CURRENT_TIMESTAMP, updated_by = 'MBW_AUTH', updated_at = CURRENT_TIMESTAMP
WHERE admin_user_id = :adminUserId
