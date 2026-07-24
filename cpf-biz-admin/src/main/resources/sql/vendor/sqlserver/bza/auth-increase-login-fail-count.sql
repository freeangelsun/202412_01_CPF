UPDATE bza_admin_user
SET login_fail_count = login_fail_count + 1,
    lock_yn = CASE WHEN login_fail_count + 1 >= 5 THEN 'Y' ELSE lock_yn END,
    updated_by = 'BZA_AUTH', updated_at = CURRENT_TIMESTAMP
WHERE admin_user_id = :adminUserId
