SELECT p.menu_code AS menuCode, p.button_code AS buttonCode
  FROM bza_permission p
  JOIN bza_role r ON r.role_code = p.role_code AND r.use_yn = 'Y'
  JOIN bza_menu m ON m.menu_code = p.menu_code AND m.use_yn = 'Y'
 WHERE p.role_code IN (:roleCodes)
   AND p.use_yn = 'Y'
   AND p.environment_code IN ('ALL', :environmentCode)
   AND m.environment_code IN ('ALL', :environmentCode)
 GROUP BY p.menu_code, p.button_code
HAVING SUM(CASE WHEN p.allow_yn = 'N' THEN 1 ELSE 0 END) = 0
   AND SUM(CASE WHEN p.allow_yn = 'Y' THEN 1 ELSE 0 END) > 0
 ORDER BY p.menu_code, p.button_code
