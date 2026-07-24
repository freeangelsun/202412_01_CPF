INSERT INTO bza_admin_user (
    admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn,
    login_fail_count, password_change_required_yn, password_expire_at, created_by, updated_by
)
SELECT :loginId, :operatorName, :passwordHash, :roleCode, 'Y', 'N', 0, 'Y',
       DATEADD(DAY, 90, CURRENT_TIMESTAMP), 'BOOTSTRAP', 'BOOTSTRAP'
WHERE NOT EXISTS (SELECT 1 FROM bza_admin_user WHERE admin_login_id = :loginId)
