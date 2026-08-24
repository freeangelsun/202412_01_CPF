SELECT *
FROM BAT_JOB_RELATION
WHERE (? IS NULL OR job_id = ? OR related_job_id = ?)
ORDER BY relation_id
