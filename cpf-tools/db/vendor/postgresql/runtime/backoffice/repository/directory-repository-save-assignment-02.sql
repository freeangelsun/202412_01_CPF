UPDATE MBW_EMPLOYEE_ASSIGNMENT SET employee_no=:employeeNo,organization_code=:organizationCode,position_code=:positionCode,job_title_code=:jobTitleCode,assignment_type=:assignmentType,
   primary_yn=:primaryYn,effective_from=:effectiveFrom,effective_to=:effectiveTo,version_no=version_no+1,updated_by=:operatorId,updated_at=CURRENT_TIMESTAMP(3)
 WHERE assignment_id=:assignmentId AND version_no=:expectedVersion
