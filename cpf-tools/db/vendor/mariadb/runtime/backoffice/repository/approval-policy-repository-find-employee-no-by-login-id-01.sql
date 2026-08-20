SELECT e.employee_no AS employeeNo
  FROM mbw_admin_user u
  JOIN mbw_employee e ON e.admin_user_id=u.admin_user_id
 WHERE u.admin_login_id=:loginId AND u.use_yn='Y' AND e.use_yn='Y'
