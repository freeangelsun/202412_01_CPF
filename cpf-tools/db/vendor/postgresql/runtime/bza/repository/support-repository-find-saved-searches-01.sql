SELECT saved_search_id AS savedSearchId, screen_code AS screenCode,
       search_name AS searchName, criteria_json AS criteriaJson,
       shared_yn AS sharedYn, created_by AS createdBy, updated_at AS updatedAt
  FROM bza_saved_search
 WHERE use_yn = 'Y'
   AND (:screenCode IS NULL OR screen_code = :screenCode)
   AND (owner_login_id = :loginId OR shared_yn = 'Y')
 ORDER BY screen_code, search_name
