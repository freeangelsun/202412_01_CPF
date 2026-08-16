SELECT CONCAT(menu_code, ':', button_code) AS button_key
FROM bza_permission
WHERE role_code = :roleCode AND allow_yn = 'Y' AND use_yn = 'Y'
ORDER BY menu_code, button_code
