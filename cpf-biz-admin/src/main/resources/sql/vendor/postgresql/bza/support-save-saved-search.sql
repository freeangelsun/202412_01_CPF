INSERT INTO bza_saved_search (
    owner_login_id, screen_code, search_name, criteria_json,
    shared_yn, use_yn, created_by, updated_by
) VALUES (
    :ownerLoginId, :screenCode, :searchName, :criteriaJson,
    :sharedYn, 'Y', :requestUser, :requestUser
)
ON CONFLICT (owner_login_id, screen_code, search_name) DO UPDATE SET
    criteria_json = EXCLUDED.criteria_json, shared_yn = EXCLUDED.shared_yn, use_yn = 'Y',
    updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP
