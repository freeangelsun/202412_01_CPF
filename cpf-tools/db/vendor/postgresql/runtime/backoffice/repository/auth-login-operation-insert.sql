INSERT INTO MBW_LOGIN_OPERATION (operation_id, admin_user_id, admin_login_id, request_hash, operation_status)
VALUES (:operationId, :adminUserId, :loginId, :requestHash, 'PROCESSING')
