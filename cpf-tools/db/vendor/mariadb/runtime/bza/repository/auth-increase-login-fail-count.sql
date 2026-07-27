UPDATE bza_admin_user
SET login_fail_count = login_fail_count + 1,
    lock_yn = CASE WHEN login_fail_count + 1 >= 5 THEN 'Y' ELSE lock_yn END,
    account_status = CASE WHEN login_fail_count + 1 >= 5 THEN 'LOCKED' ELSE account_status END,
    version_no = version_no + 1,
    updated_by = 'BZA_AUTH', updated_at = NOW()
WHERE admin_user_id = :adminUserId
