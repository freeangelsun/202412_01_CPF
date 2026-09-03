SELECT COUNT(*)
  FROM MBW_USER_ROLE ur
  JOIN MBW_ADMIN_USER u ON u.admin_user_id=ur.admin_user_id
 WHERE u.admin_login_id=:loginId
   AND ur.valid_from<=CURRENT_TIMESTAMP
   AND (ur.valid_to IS NULL OR ur.valid_to>CURRENT_TIMESTAMP)
