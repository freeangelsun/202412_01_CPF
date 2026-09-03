SELECT e.employee_no AS employeeNo
  FROM MBW_ADMIN_USER u
  JOIN MBW_EMPLOYEE e ON e.admin_user_id = u.admin_user_id
 WHERE u.admin_login_id = :loginId
   AND u.use_yn = 'Y'
   AND e.use_yn = 'Y'
