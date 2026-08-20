UPDATE mbw_admin_user
SET login_fail_count = 0, version_no = version_no + 1,
    last_login_at = CURRENT_TIMESTAMP, updated_by = 'MBW_AUTH', updated_at = CURRENT_TIMESTAMP
WHERE admin_user_id = :adminUserId
