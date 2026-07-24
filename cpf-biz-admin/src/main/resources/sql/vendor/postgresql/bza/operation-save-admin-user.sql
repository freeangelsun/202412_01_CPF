INSERT INTO bza_admin_user (
    admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn,
    password_change_required_yn, created_by, updated_by
) VALUES (
    :loginId, :adminName, :passwordHash, :roleCode, :useYn, :lockYn,
    :passwordChangeRequiredYn, :requestUser, :requestUser
)
ON CONFLICT (admin_login_id) DO UPDATE SET
    admin_name = EXCLUDED.admin_name,
    password_hash = COALESCE(EXCLUDED.password_hash, bza_admin_user.password_hash),
    role_code = EXCLUDED.role_code, use_yn = EXCLUDED.use_yn, lock_yn = EXCLUDED.lock_yn,
    password_change_required_yn = EXCLUDED.password_change_required_yn,
    updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP
