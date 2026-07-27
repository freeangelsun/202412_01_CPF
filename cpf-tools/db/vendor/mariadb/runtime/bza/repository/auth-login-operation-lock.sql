SELECT operation_id AS operationId, admin_user_id AS adminUserId, admin_login_id AS loginId, operation_status AS status FROM bza_login_operation WHERE operation_id = :operationId FOR UPDATE
