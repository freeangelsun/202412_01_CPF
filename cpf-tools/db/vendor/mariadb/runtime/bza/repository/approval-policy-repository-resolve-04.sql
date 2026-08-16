SELECT e.employee_no AS employeeNo, r.organization_code AS organizationCode,
       a.position_code AS positionCode, a.job_title_code AS jobTitleCode
  FROM bza_organization_responsibility r
  JOIN bza_employee e ON e.employee_no=r.employee_no
  LEFT JOIN bza_employee_assignment a
    ON a.employee_no=e.employee_no AND a.primary_yn='Y'
   AND a.effective_from <= :at AND (a.effective_to IS NULL OR a.effective_to > :at)
 WHERE r.organization_code=:targetCode
   AND r.responsibility_type IN ('MANAGER','APPROVAL_OWNER','ACTING')
   AND r.effective_from <= :at AND (r.effective_to IS NULL OR r.effective_to > :at)
   AND e.use_yn='Y' AND e.employment_status IN ('EMPLOYED','SECONDMENT','DISPATCHED')
