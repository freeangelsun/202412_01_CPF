SELECT admin_user_id AS adminUserId, admin_login_id AS loginId, create_operation_id AS operationId
FROM mbw_admin_user
WHERE admin_login_id = :loginId
