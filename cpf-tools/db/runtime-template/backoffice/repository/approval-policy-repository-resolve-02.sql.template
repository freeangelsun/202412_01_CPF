SELECT e.employee_no AS employeeNo, a.organization_code AS organizationCode,
       a.position_code AS positionCode, a.job_title_code AS jobTitleCode
  FROM mbw_employee_assignment a
  JOIN mbw_employee e ON e.employee_no=a.employee_no
 WHERE a.organization_code=:targetCode
   AND a.effective_from <= :at AND (a.effective_to IS NULL OR a.effective_to > :at)
   AND e.use_yn='Y' AND e.employment_status IN ('EMPLOYED','SECONDMENT','DISPATCHED')
