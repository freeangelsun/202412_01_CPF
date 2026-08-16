MERGE INTO bza_admin_user target
USING (
    SELECT :loginId admin_login_id, :adminName admin_name, :passwordHash password_hash,
           :roleCode role_code, :useYn use_yn, :lockYn lock_yn,
           :passwordChangeRequiredYn password_change_required_yn, :requestUser request_user
    FROM dual
) source
ON (target.admin_login_id = source.admin_login_id)
WHEN MATCHED THEN UPDATE SET
    target.admin_name = source.admin_name,
    target.password_hash = COALESCE(source.password_hash, target.password_hash),
    target.role_code = source.role_code, target.use_yn = source.use_yn, target.lock_yn = source.lock_yn,
    target.password_change_required_yn = source.password_change_required_yn,
    target.updated_by = source.request_user, target.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn,
    password_change_required_yn, created_by, updated_by
) VALUES (
    source.admin_login_id, source.admin_name, source.password_hash, source.role_code,
    source.use_yn, source.lock_yn, source.password_change_required_yn,
    source.request_user, source.request_user
)
