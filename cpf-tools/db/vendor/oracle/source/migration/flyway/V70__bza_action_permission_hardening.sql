-- V70 BZA action-level permission hardening
MERGE INTO bza_permission tgt USING (
 SELECT 'BZA_ADMIN' role_code,'BZA_AUTHORIZATION' menu_code,'SIMULATE' button_code,'API' permission_type,'GET' http_method,'/api/bza/backoffice/permissions/effective' api_pattern,NULL domain_code,'ALL' environment_code,'ALL' data_scope,'Y' allow_yn,'Y' use_yn,'SYSTEM' created_by,'SYSTEM' updated_by FROM dual UNION ALL
 SELECT 'BZA_ADMIN','BZA_EMPLOYEE','PII_RAW','API','POST','/api/bza/backoffice/employees/*/contacts/raw',NULL,'ALL','ALL','Y','Y','SYSTEM','SYSTEM' FROM dual UNION ALL
 SELECT 'BZA_OPERATOR','BZA_AUTHORIZATION','SIMULATE','API','GET','/api/bza/backoffice/permissions/effective',NULL,'ALL','ORGANIZATION','Y','Y','SYSTEM','SYSTEM' FROM dual
) src ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method,tgt.api_pattern=src.api_pattern,tgt.data_scope=src.data_scope,tgt.allow_yn=src.allow_yn,tgt.use_yn='Y',tgt.updated_by='SYSTEM',tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT(role_code,menu_code,button_code,permission_type,http_method,api_pattern,domain_code,environment_code,data_scope,allow_yn,use_yn,created_by,updated_by)
VALUES(src.role_code,src.menu_code,src.button_code,src.permission_type,src.http_method,src.api_pattern,src.domain_code,src.environment_code,src.data_scope,src.allow_yn,src.use_yn,src.created_by,src.updated_by);
