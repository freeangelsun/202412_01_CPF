SELECT COUNT(*)
  FROM mbw_user_role ur
  JOIN mbw_admin_user u ON u.admin_user_id=ur.admin_user_id
 WHERE u.admin_login_id=:loginId
   AND ur.valid_from<=CURRENT_TIMESTAMP
   AND (ur.valid_to IS NULL OR ur.valid_to>CURRENT_TIMESTAMP)
