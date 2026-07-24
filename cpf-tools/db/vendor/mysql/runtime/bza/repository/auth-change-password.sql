UPDATE bza_admin_user
SET password_hash = :newHash, password_change_required_yn = 'N',
    password_expire_at = DATE_ADD(NOW(), INTERVAL 90 DAY),
    login_fail_count = 0, lock_yn = 'N', updated_by = :updatedBy, updated_at = NOW()
WHERE admin_user_id = :adminUserId AND password_hash = :previousHash AND use_yn = 'Y'
