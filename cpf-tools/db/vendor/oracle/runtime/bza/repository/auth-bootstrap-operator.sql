INSERT INTO bza_admin_user (
    admin_login_id, admin_name, password_hash, role_code, account_status, version_no, create_operation_id, use_yn, lock_yn,
    login_fail_count, password_change_required_yn, password_expire_at, created_by, updated_by
) VALUES (
    :loginId, :operatorName, :passwordHash, :roleCode, 'ACTIVE', 0, :operationId, 'Y', 'N',
    0, 'Y', :passwordExpireAt, 'BOOTSTRAP', 'BOOTSTRAP'
);
