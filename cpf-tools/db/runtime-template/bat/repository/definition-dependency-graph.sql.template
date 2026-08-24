SELECT d.job_id,d.related_job_id
FROM BAT_JOB_DEPENDENCY d
JOIN BAT_JOB_DEFINITION_VERSION v
  ON v.job_id = d.job_id
 AND v.definition_version = d.definition_version
WHERE v.definition_state IN ('DRAFT','VALIDATED','APPROVAL','PUBLISHED')
