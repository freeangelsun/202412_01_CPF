MERGE INTO bza_user_role target
USING (
    SELECT admin_user_id,
           :roleCode role_code,
           'INIT:' || TO_CHAR(admin_user_id) || ':' || :roleCode operation_id,
           :actor actor
    FROM bza_admin_user
    WHERE admin_login_id = :loginId
) source
ON (target.operation_id = source.operation_id)
WHEN MATCHED THEN UPDATE SET
    target.updated_by = source.actor
WHEN NOT MATCHED THEN INSERT (
    admin_user_id, role_code, valid_from, primary_yn, grant_reason,
    operation_id, version_no, created_by, updated_by
) VALUES (
    source.admin_user_id, source.role_code, CURRENT_TIMESTAMP(3), 'Y', 'INITIAL_ROLE',
    source.operation_id, 0, source.actor, source.actor
)
