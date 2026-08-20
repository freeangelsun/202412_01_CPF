SELECT assignment_id AS assignmentId,employee_no AS employeeNo,organization_code AS organizationCode,position_code AS positionCode,job_title_code AS jobTitleCode,
       assignment_type AS assignmentType,primary_yn AS primaryYn,effective_from AS effectiveFrom,effective_to AS effectiveTo,version_no AS versionNo,updated_at AS updatedAt
  FROM mbw_employee_assignment WHERE (:employeeNo IS NULL OR employee_no=:employeeNo) AND (:organizationCode IS NULL OR organization_code=:organizationCode)
   AND effective_from<=:effectiveAt AND (effective_to IS NULL OR effective_to>:effectiveAt)
 ORDER BY employee_no,primary_yn DESC,effective_from DESC,assignment_id DESC OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
