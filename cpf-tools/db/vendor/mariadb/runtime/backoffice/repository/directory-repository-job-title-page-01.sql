SELECT job_title_code AS jobTitleCode,job_title_name AS jobTitleName,manager_yn AS managerYn,use_yn AS useYn,version_no AS versionNo,updated_at AS updatedAt
FROM mbw_job_title ORDER BY job_title_code LIMIT :limit OFFSET :offset
