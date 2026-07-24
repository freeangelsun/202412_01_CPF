MERGE INTO bza_role target
USING (
    SELECT :roleCode role_code, :roleName role_name, :writeAllowedYn write_allowed_yn,
           :dataScope data_scope, :useYn use_yn, :requestUser request_user
    FROM dual
) source
ON (target.role_code = source.role_code)
WHEN MATCHED THEN UPDATE SET
    target.role_name = source.role_name, target.write_allowed_yn = source.write_allowed_yn,
    target.data_scope = source.data_scope, target.use_yn = source.use_yn,
    target.updated_by = source.request_user, target.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by
) VALUES (
    source.role_code, source.role_name, source.write_allowed_yn, source.data_scope,
    source.use_yn, source.request_user, source.request_user
)
