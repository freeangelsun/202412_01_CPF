SELECT e.employee_no AS employeeNo, a.organization_code AS organizationCode,
       a.position_code AS positionCode, a.job_title_code AS jobTitleCode
  FROM mbw_user_role ur
  JOIN mbw_admin_user u ON u.admin_user_id=ur.admin_user_id
  JOIN mbw_employee e ON e.admin_user_id=u.admin_user_id
  LEFT JOIN mbw_employee_assignment a
    ON a.employee_no=e.employee_no AND a.primary_yn='Y'
   AND a.effective_from <= :at AND (a.effective_to IS NULL OR a.effective_to > :at)
 WHERE ur.role_code=:targetCode
   AND (ur.valid_from IS NULL OR ur.valid_from <= :at)
   AND (ur.valid_to IS NULL OR ur.valid_to > :at)
   AND u.use_yn='Y' AND e.use_yn='Y' AND e.employment_status IN ('EMPLOYED','SECONDMENT','DISPATCHED')
