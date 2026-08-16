INSERT INTO bza_permission (
    role_code, menu_code, button_code, permission_type, http_method, api_pattern,
    domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by
) VALUES (
    :roleCode, :menuCode, :buttonCode, :permissionType, :httpMethod, :apiPattern,
    :domainCode, :environmentCode, :dataScope, :allowYn, :useYn, :requestUser, :requestUser
)
ON DUPLICATE KEY UPDATE
    permission_type = VALUES(permission_type), http_method = VALUES(http_method),
    api_pattern = VALUES(api_pattern), domain_code = VALUES(domain_code),
    environment_code = VALUES(environment_code), data_scope = VALUES(data_scope),
    allow_yn = VALUES(allow_yn), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP
