SELECT parameter_id AS parameterId,
       center_cut_job_id AS centerCutJobId,
       parameter_key AS parameterKey,
       CASE
           WHEN encrypted_yn = 'Y' THEN '[MASKED]'
           ELSE parameter_value
       END AS parameterValue,
       encrypted_yn AS encryptedYn,
       use_yn AS useYn,
       created_at AS createdAt,
       updated_at AS updatedAt
FROM bat_center_cut_parameter
WHERE center_cut_job_id = ?
ORDER BY parameter_key
