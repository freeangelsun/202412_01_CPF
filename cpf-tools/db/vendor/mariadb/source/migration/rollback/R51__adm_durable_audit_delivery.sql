-- R12 rollback: relay를 중지하고 미전달 건을 보존/백업한 뒤 수행하십시오.
DELETE FROM adm_role_api_permission WHERE API_PERMISSION_ID='API_AUDIT_LOG_RETRY';
DELETE FROM adm_api_permission WHERE API_PERMISSION_ID='API_AUDIT_LOG_RETRY';
DELETE FROM adm_role_button WHERE BUTTON_ID='AUDIT_LOG_RETRY';
DELETE FROM adm_button WHERE BUTTON_ID='AUDIT_LOG_RETRY';
DROP TABLE IF EXISTS adm_audit_delivery;
