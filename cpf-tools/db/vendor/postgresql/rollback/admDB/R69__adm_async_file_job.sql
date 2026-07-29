-- R69 ADM File Job rollback (postgresql)
DELETE FROM adm_role_api_permission WHERE api_permission_id LIKE 'API_FILE_JOB_%' OR api_permission_id IN ('API_CACHE_EVICT_KEY','API_CACHE_EVICT_NAMESPACE','API_CACHE_RECONCILE');
DELETE FROM adm_api_permission WHERE api_permission_id LIKE 'API_FILE_JOB_%' OR api_permission_id IN ('API_CACHE_EVICT_KEY','API_CACHE_EVICT_NAMESPACE','API_CACHE_RECONCILE');
DELETE FROM adm_role_button WHERE button_id LIKE 'FILE_JOB_%' OR button_id IN ('CACHE_EVICT_KEY','CACHE_EVICT_NAMESPACE','CACHE_RECONCILE');
DELETE FROM adm_role_menu WHERE menu_id='FILE_JOB';
DELETE FROM adm_button WHERE button_id LIKE 'FILE_JOB_%' OR button_id IN ('CACHE_EVICT_KEY','CACHE_EVICT_NAMESPACE','CACHE_RECONCILE');
DELETE FROM adm_menu WHERE menu_id='FILE_JOB';
DROP TABLE IF EXISTS adm_file_job_row;
DROP TABLE IF EXISTS adm_file_job;
