INSERT INTO bza_admin_user (
    admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn,
    password_change_required_yn, created_by, updated_by
) VALUES (
    :loginId, :adminName, :passwordHash, :roleCode, :useYn, :lockYn,
    :passwordChangeRequiredYn, :requestUser, :requestUser
)
ON DUPLICATE KEY UPDATE
    admin_name = VALUES(admin_name), password_hash = COALESCE(VALUES(password_hash), password_hash),
    role_code = VALUES(role_code), use_yn = VALUES(use_yn), lock_yn = VALUES(lock_yn),
    password_change_required_yn = VALUES(password_change_required_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP
