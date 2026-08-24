SELECT c.center_cut_job_id AS centerCutJobId,
       c.batch_job_id AS batchJobId,
       c.center_cut_job_name AS centerCutJobName,
       c.provider_key AS providerKey,
       c.handler_key AS handlerKey,
       c.chunk_size AS chunkSize,
       c.retry_limit AS retryLimit,
       c.use_yn AS useYn,
       c.description AS description,
       c.created_at AS createdAt,
       c.updated_at AS updatedAt,
       j.job_name AS batchJobName,
       j.job_type AS batchJobType
FROM BAT_CENTER_CUT_JOB c
LEFT JOIN BAT_JOB j ON j.job_id = c.batch_job_id
ORDER BY c.center_cut_job_id
