INSERT INTO bza_role (
    role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by
) VALUES (
    :roleCode, :roleName, :writeAllowedYn, :dataScope, :useYn, :requestUser, :requestUser
)
ON CONFLICT (role_code) DO UPDATE SET
    role_name = EXCLUDED.role_name, write_allowed_yn = EXCLUDED.write_allowed_yn,
    data_scope = EXCLUDED.data_scope, use_yn = EXCLUDED.use_yn,
    updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP
