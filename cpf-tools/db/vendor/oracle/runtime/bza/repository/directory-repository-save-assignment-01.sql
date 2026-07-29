INSERT INTO bza_employee_assignment(employee_no,organization_code,position_code,job_title_code,assignment_type,primary_yn,effective_from,effective_to,version_no,created_by,updated_by)
VALUES(:employeeNo,:organizationCode,:positionCode,:jobTitleCode,:assignmentType,:primaryYn,:effectiveFrom,:effectiveTo,0,:operatorId,:operatorId)
