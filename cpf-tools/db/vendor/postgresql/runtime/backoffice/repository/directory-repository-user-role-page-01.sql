SELECT ur.user_role_id AS userRoleId,u.admin_login_id AS loginId,ur.admin_user_id AS adminUserId,ur.role_code AS roleCode,r.role_name AS roleName,
       ur.valid_from AS validFrom,ur.valid_to AS validTo,ur.primary_yn AS primaryYn,ur.grant_reason AS grantReason,ur.operation_id AS operationId,ur.version_no AS versionNo
  FROM mbw_user_role ur JOIN mbw_admin_user u ON u.admin_user_id=ur.admin_user_id JOIN mbw_role r ON r.role_code=ur.role_code
 WHERE (:loginId IS NULL OR u.admin_login_id=:loginId) AND (ur.valid_from IS NULL OR ur.valid_from<=:effectiveAt) AND (ur.valid_to IS NULL OR ur.valid_to>:effectiveAt)
 ORDER BY u.admin_login_id,ur.primary_yn DESC,ur.created_at DESC LIMIT :limit OFFSET :offset
