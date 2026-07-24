MERGE INTO bza_saved_search AS target
USING (
    SELECT :ownerLoginId owner_login_id, :screenCode screen_code, :searchName search_name,
           :criteriaJson criteria_json, :sharedYn shared_yn, :requestUser request_user
) AS source
ON (target.owner_login_id = source.owner_login_id
    AND target.screen_code = source.screen_code
    AND target.search_name = source.search_name)
WHEN MATCHED THEN UPDATE SET
    target.criteria_json = source.criteria_json, target.shared_yn = source.shared_yn,
    target.use_yn = 'Y', target.updated_by = source.request_user,
    target.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    owner_login_id, screen_code, search_name, criteria_json,
    shared_yn, use_yn, created_by, updated_by
) VALUES (
    source.owner_login_id, source.screen_code, source.search_name, source.criteria_json,
    source.shared_yn, 'Y', source.request_user, source.request_user
);
