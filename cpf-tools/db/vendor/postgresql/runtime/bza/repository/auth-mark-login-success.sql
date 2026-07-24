UPDATE bza_admin_user
SET login_fail_count = 0, last_login_at = CURRENT_TIMESTAMP, updated_by = 'BZA_AUTH', updated_at = CURRENT_TIMESTAMP
WHERE admin_user_id = :adminUserId
