-- V70 BZA action-level permission hardening
MERGE INTO bza_permission tgt USING (VALUES
 ('BZA_ADMIN','BZA_AUTHORIZATION','SIMULATE','API','GET','/api/bza/backoffice/permissions/effective',NULL,'ALL','ALL','Y','Y','SYSTEM','SYSTEM'),
 ('BZA_ADMIN','BZA_EMPLOYEE','PII_RAW','API','POST','/api/bza/backoffice/employees/*/contacts/raw',NULL,'ALL','ALL','Y','Y','SYSTEM','SYSTEM'),
 ('BZA_OPERATOR','BZA_AUTHORIZATION','SIMULATE','API','GET','/api/bza/backoffice/permissions/effective',NULL,'ALL','ORGANIZATION','Y','Y','SYSTEM','SYSTEM')
) AS src(role_code,menu_code,button_code,permission_type,http_method,api_pattern,domain_code,environment_code,data_scope,allow_yn,use_yn,created_by,updated_by)
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET http_method=src.http_method,api_pattern=src.api_pattern,data_scope=src.data_scope,allow_yn=src.allow_yn,use_yn='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT(role_code,menu_code,button_code,permission_type,http_method,api_pattern,domain_code,environment_code,data_scope,allow_yn,use_yn,created_by,updated_by)
VALUES(src.role_code,src.menu_code,src.button_code,src.permission_type,src.http_method,src.api_pattern,src.domain_code,src.environment_code,src.data_scope,src.allow_yn,src.use_yn,src.created_by,src.updated_by);
