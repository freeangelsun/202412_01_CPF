SELECT ur.user_role_id AS userRoleId,u.admin_login_id AS loginId,ur.role_code AS roleCode,ur.operation_id AS operationId,ur.primary_yn AS primaryYn,ur.valid_from AS validFrom,ur.valid_to AS validTo
FROM bza_user_role ur JOIN bza_admin_user u ON u.admin_user_id=ur.admin_user_id WHERE ur.operation_id=:operationId
