UPDATE bza_admin_user
SET create_operation_id = :operationId, updated_by = 'BOOTSTRAP'
WHERE admin_login_id = :loginId AND create_operation_id IS NULL;
