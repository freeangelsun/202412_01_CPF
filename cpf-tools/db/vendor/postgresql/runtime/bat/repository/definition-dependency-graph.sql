SELECT d.job_id,d.related_job_id
FROM bat_job_dependency d
JOIN bat_job_definition_version v
  ON v.job_id = d.job_id
 AND v.definition_version = d.definition_version
WHERE v.definition_state IN ('DRAFT','VALIDATED','APPROVAL','PUBLISHED')
