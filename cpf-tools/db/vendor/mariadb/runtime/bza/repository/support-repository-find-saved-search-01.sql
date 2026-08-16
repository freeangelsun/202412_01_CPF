SELECT saved_search_id AS savedSearchId, owner_login_id AS ownerLoginId,
       screen_code AS screenCode, search_name AS searchName,
       criteria_json AS criteriaJson, shared_yn AS sharedYn, use_yn AS useYn
  FROM bza_saved_search
 WHERE owner_login_id = :loginId AND screen_code = :screenCode AND search_name = :searchName
