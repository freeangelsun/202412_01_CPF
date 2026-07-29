INSERT INTO bza_user_role (
    admin_user_id, role_code, valid_from, primary_yn, grant_reason,
    operation_id, version_no, created_by, updated_by
)
SELECT admin_user_id, :roleCode, CURRENT_TIMESTAMP(3), 'Y', 'INITIAL_ROLE',
       CONCAT('INIT:', admin_user_id, ':', :roleCode), 0, :actor, :actor
FROM bza_admin_user
WHERE admin_login_id = :loginId
ON CONFLICT (operation_id) DO UPDATE
SET updated_by = EXCLUDED.updated_by
