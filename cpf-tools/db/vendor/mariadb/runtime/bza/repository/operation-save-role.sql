INSERT INTO bza_role (
    role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by
) VALUES (
    :roleCode, :roleName, :writeAllowedYn, :dataScope, :useYn, :requestUser, :requestUser
)
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name), write_allowed_yn = VALUES(write_allowed_yn),
    data_scope = VALUES(data_scope), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP
