UPDATE MBW_SAVED_SEARCH
   SET use_yn = 'N', updated_by = :requestUser, updated_at = CURRENT_TIMESTAMP
 WHERE saved_search_id = :savedSearchId AND owner_login_id = :loginId AND use_yn = 'Y'
