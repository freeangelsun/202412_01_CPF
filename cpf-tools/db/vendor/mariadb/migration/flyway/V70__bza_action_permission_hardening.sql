-- V70 BZA action-level permission hardening
-- Combined create/update endpoints keep WRITE; dangerous and approval actions are explicit.
INSERT INTO bza_permission(role_code,menu_code,button_code,permission_type,http_method,api_pattern,domain_code,environment_code,data_scope,allow_yn,use_yn,created_by,updated_by) VALUES
 ('BZA_ADMIN','BZA_AUTHORIZATION','SIMULATE','API','GET','/api/bza/backoffice/permissions/effective',NULL,'ALL','ALL','Y','Y','SYSTEM','SYSTEM'),
 ('BZA_ADMIN','BZA_EMPLOYEE','PII_RAW','API','POST','/api/bza/backoffice/employees/*/contacts/raw',NULL,'ALL','ALL','Y','Y','SYSTEM','SYSTEM'),
 ('BZA_OPERATOR','BZA_AUTHORIZATION','SIMULATE','API','GET','/api/bza/backoffice/permissions/effective',NULL,'ALL','ORGANIZATION','Y','Y','SYSTEM','SYSTEM'),
 ('BZA_APPROVER','BZA_APPROVAL','DECIDE','API','POST','/api/bza/backoffice/approvals/*/actions',NULL,'ALL','ORGANIZATION','Y','Y','SYSTEM','SYSTEM'),
 ('BZA_APPROVER','BZA_APPROVAL','DECIDE','API','POST','/api/bza/approvals/*/decisions',NULL,'ALL','ORGANIZATION','Y','Y','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE http_method=VALUES(http_method),api_pattern=VALUES(api_pattern),domain_code=VALUES(domain_code),data_scope=VALUES(data_scope),allow_yn=VALUES(allow_yn),use_yn='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;
