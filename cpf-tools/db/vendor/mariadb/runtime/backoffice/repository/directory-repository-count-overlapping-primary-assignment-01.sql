SELECT COUNT(*) FROM mbw_employee_assignment WHERE employee_no=:employeeNo AND primary_yn='Y' AND (:exclude IS NULL OR assignment_id<>:exclude)
 AND (effective_to IS NULL OR effective_to>:from) AND (:to IS NULL OR effective_from<:to)
