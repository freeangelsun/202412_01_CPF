UPDATE bza_admin_user
SET login_fail_count = 0, last_login_at = NOW(), updated_by = 'BZA_AUTH', updated_at = NOW()
WHERE admin_user_id = :adminUserId
