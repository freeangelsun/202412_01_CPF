SELECT e.employee_no AS employeeNo,
       a.organization_code AS organizationCode,
       a.position_code AS positionCode, a.job_title_code AS jobTitleCode
  FROM mbw_employee e
  LEFT JOIN mbw_employee_assignment a
    ON a.employee_no=e.employee_no AND a.primary_yn='Y'
   AND a.effective_from <= :at AND (a.effective_to IS NULL OR a.effective_to > :at)
 WHERE e.employee_no=:targetCode AND e.use_yn='Y' AND e.employment_status IN ('EMPLOYED','SECONDMENT','DISPATCHED')
