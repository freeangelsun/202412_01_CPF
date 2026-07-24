MERGE INTO bza_permission target
USING (
    SELECT :roleCode role_code, :menuCode menu_code, :buttonCode button_code,
           :permissionType permission_type, :httpMethod http_method, :apiPattern api_pattern,
           :domainCode domain_code, :environmentCode environment_code, :dataScope data_scope,
           :allowYn allow_yn, :useYn use_yn, :requestUser request_user
    FROM dual
) source
ON (target.role_code = source.role_code
    AND target.menu_code = source.menu_code
    AND target.button_code = source.button_code)
WHEN MATCHED THEN UPDATE SET
    target.permission_type = source.permission_type, target.http_method = source.http_method,
    target.api_pattern = source.api_pattern, target.domain_code = source.domain_code,
    target.environment_code = source.environment_code, target.data_scope = source.data_scope,
    target.allow_yn = source.allow_yn, target.use_yn = source.use_yn,
    target.updated_by = source.request_user, target.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    role_code, menu_code, button_code, permission_type, http_method, api_pattern,
    domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by
) VALUES (
    source.role_code, source.menu_code, source.button_code, source.permission_type,
    source.http_method, source.api_pattern, source.domain_code, source.environment_code,
    source.data_scope, source.allow_yn, source.use_yn, source.request_user, source.request_user
)
