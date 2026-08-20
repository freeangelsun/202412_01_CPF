-- V70 MBW action-level permission hardening
-- Combined create/update endpoints keep WRITE; dangerous and approval actions are explicit.
INSERT INTO mbw_permission(role_code,menu_code,button_code,permission_type,http_method,api_pattern,domain_code,environment_code,data_scope,allow_yn,use_yn,created_by,updated_by) VALUES
 ('MBW_ADMIN','MBW_AUTHORIZATION','SIMULATE','API','GET','/api/v1/backoffice/backoffice/permissions/effective',NULL,'ALL','ALL','Y','Y','SYSTEM','SYSTEM'),
 ('MBW_ADMIN','MBW_EMPLOYEE','PII_RAW','API','POST','/api/v1/backoffice/backoffice/employees/*/contacts/raw',NULL,'ALL','ALL','Y','Y','SYSTEM','SYSTEM'),
 ('MBW_OPERATOR','MBW_AUTHORIZATION','SIMULATE','API','GET','/api/v1/backoffice/backoffice/permissions/effective',NULL,'ALL','ORGANIZATION','Y','Y','SYSTEM','SYSTEM'),
 ('MBW_APPROVER','MBW_APPROVAL','DECIDE','API','POST','/api/v1/backoffice/backoffice/approvals/*/actions',NULL,'ALL','ORGANIZATION','Y','Y','SYSTEM','SYSTEM'),
 ('MBW_APPROVER','MBW_APPROVAL','DECIDE','API','POST','/api/v1/backoffice/approvals/*/decisions',NULL,'ALL','ORGANIZATION','Y','Y','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE http_method=VALUES(http_method),api_pattern=VALUES(api_pattern),domain_code=VALUES(domain_code),data_scope=VALUES(data_scope),allow_yn=VALUES(allow_yn),use_yn='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;
