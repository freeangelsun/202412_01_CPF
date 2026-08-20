SELECT responsibility_id AS responsibilityId,organization_code AS organizationCode,responsibility_type AS responsibilityType,employee_no AS employeeNo,
       effective_from AS effectiveFrom,effective_to AS effectiveTo,priority_no AS priorityNo,use_yn AS useYn,version_no AS versionNo,updated_at AS updatedAt
  FROM mbw_organization_responsibility WHERE (:organizationCode IS NULL OR organization_code=:organizationCode) AND effective_from<=:effectiveAt AND (effective_to IS NULL OR effective_to>:effectiveAt)
 ORDER BY organization_code,responsibility_type,priority_no,effective_from DESC LIMIT :limit OFFSET :offset
