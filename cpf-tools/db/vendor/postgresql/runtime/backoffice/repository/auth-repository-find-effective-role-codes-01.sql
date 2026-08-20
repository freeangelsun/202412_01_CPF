SELECT ur.role_code
  FROM mbw_user_role ur
  JOIN mbw_role r ON r.role_code = ur.role_code AND r.use_yn = 'Y'
 WHERE ur.admin_user_id = :adminUserId
   AND (ur.valid_from IS NULL OR ur.valid_from <= CURRENT_TIMESTAMP(3))
   AND (ur.valid_to IS NULL OR ur.valid_to > CURRENT_TIMESTAMP(3))
 ORDER BY ur.primary_yn DESC, ur.role_code
