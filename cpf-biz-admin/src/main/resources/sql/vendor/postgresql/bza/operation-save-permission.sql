INSERT INTO bza_permission (
    role_code, menu_code, button_code, permission_type, http_method, api_pattern,
    domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by
) VALUES (
    :roleCode, :menuCode, :buttonCode, :permissionType, :httpMethod, :apiPattern,
    :domainCode, :environmentCode, :dataScope, :allowYn, :useYn, :requestUser, :requestUser
)
ON CONFLICT (role_code, menu_code, button_code) DO UPDATE SET
    permission_type = EXCLUDED.permission_type, http_method = EXCLUDED.http_method,
    api_pattern = EXCLUDED.api_pattern, domain_code = EXCLUDED.domain_code,
    environment_code = EXCLUDED.environment_code, data_scope = EXCLUDED.data_scope,
    allow_yn = EXCLUDED.allow_yn, use_yn = EXCLUDED.use_yn,
    updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP
