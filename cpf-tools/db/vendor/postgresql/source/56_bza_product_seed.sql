-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=56_bza_product_seed.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=bzaDB
-- CPF_USE_LOGICAL_DATABASE=bzaDB
MERGE INTO bza_role tgt
USING (VALUES
  ('BZA_ADMIN', '업무 관리자', 'Y', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_OPERATOR', '업무 운영자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_APPROVER', '업무 결재자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_VIEWER', '업무 조회자', 'N', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by)
ON (tgt.role_code = src.role_code)
WHEN MATCHED THEN UPDATE SET
  tgt.role_name = src.role_name,
  tgt.write_allowed_yn = src.write_allowed_yn,
  tgt.data_scope = src.data_scope,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by)
VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_menu tgt
USING (VALUES
  ('BZA_DASHBOARD', '업무 관리자 대시보드', NULL, 'BZA', '/bza', 'dashboard', 'ALL', '/api/bza/dashboard', 10, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_ORGANIZATION', '조직 관리', NULL, 'BZA', '/bza/organizations', 'organization', 'ALL', '/api/bza/organizations', 20, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_EMPLOYEE', '직원·소속 관리', NULL, 'BZA', '/bza/employees', 'employee', 'ALL', '/api/bza/employees', 30, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_AUTHORIZATION', '업무 권한 관리', NULL, 'BZA', '/bza/authorization', 'shield', 'ALL', '/api/bza/authorization', 40, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_APPROVAL', '업무 결재 관리', NULL, 'BZA', '/bza/approvals', 'approval', 'ALL', '/api/bza/approvals', 50, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_AUDIT', '업무 감사 조회', NULL, 'BZA', '/bza/audits', 'audit', 'ALL', '/api/bza/audits', 60, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_ATTACHMENT', '첨부 관리', NULL, 'BZA', '/bza/attachments', 'attachment', 'ALL', '/api/bza/attachments', 70, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_SETTING', '업무 관리자 설정', NULL, 'BZA', '/bza/settings', 'setting', 'ALL', '/api/bza/settings', 80, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by)
ON (tgt.menu_code = src.menu_code)
WHEN MATCHED THEN UPDATE SET
  tgt.menu_name = src.menu_name,
  tgt.parent_menu_code = src.parent_menu_code,
  tgt.module_code = src.module_code,
  tgt.route_path = src.route_path,
  tgt.icon_code = src.icon_code,
  tgt.environment_code = src.environment_code,
  tgt.api_path = src.api_path,
  tgt.sort_order = src.sort_order,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by)
VALUES (src.menu_code, src.menu_name, src.parent_menu_code, src.module_code, src.route_path, src.icon_code, src.environment_code, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_permission tgt
USING (
  SELECT 'BZA_ADMIN' AS role_code, menu_code AS menu_code, 'ALL' AS button_code, 'API' AS permission_type, '*' AS http_method, (api_path || '/**') AS api_pattern, NULL AS domain_code, environment_code AS environment_code, 'ALL' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM bza_menu
  WHERE use_yn = 'Y'
) src
ON (tgt.role_code = src.role_code AND tgt.menu_code = src.menu_code AND tgt.button_code = src.button_code AND tgt.permission_type = src.permission_type AND tgt.environment_code = src.environment_code)
WHEN MATCHED THEN UPDATE SET
  tgt.http_method = src.http_method,
  tgt.api_pattern = src.api_pattern,
  tgt.data_scope = src.data_scope,
  tgt.allow_yn = src.allow_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_permission tgt
USING (VALUES
  ('BZA_OPERATOR', 'BZA_DASHBOARD', 'READ', 'API', 'GET', '/api/bza/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_OPERATOR', 'BZA_ORGANIZATION', 'READ', 'API', 'GET', '/api/bza/organizations/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_OPERATOR', 'BZA_EMPLOYEE', 'READ', 'API', 'GET', '/api/bza/employees/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_APPROVER', 'BZA_APPROVAL', 'READ', 'API', 'GET', '/api/bza/approvals/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_APPROVER', 'BZA_APPROVAL', 'DECIDE', 'API', 'POST', '/api/bza/approvals/*/decisions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_VIEWER', 'BZA_DASHBOARD', 'READ', 'API', 'GET', '/api/bza/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_VIEWER', 'BZA_AUDIT', 'READ', 'API', 'GET', '/api/bza/audits/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
ON (tgt.role_code = src.role_code AND tgt.menu_code = src.menu_code AND tgt.button_code = src.button_code AND tgt.permission_type = src.permission_type AND tgt.environment_code = src.environment_code)
WHEN MATCHED THEN UPDATE SET
  tgt.http_method = src.http_method,
  tgt.api_pattern = src.api_pattern,
  tgt.data_scope = src.data_scope,
  tgt.allow_yn = src.allow_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_project_setting tgt
USING (VALUES
  ('BZA.APPROVAL.SELF_APPROVAL_ALLOWED', 'N', '기본 자기승인 차단 정책', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA.APPROVAL.DEFAULT_DUE_HOURS', '24', '기본 결재 SLA 시간', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA.APPROVAL.REQUIRE_PAYLOAD_HASH', 'Y', '결재 대상 Payload 변조 검증용 SHA-256 사용', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA.AUDIT.HASH_CHAIN_ENABLED', 'Y', '업무 감사 로그 hash-chain 검증 사용', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA.ATTACHMENT.SECURITY_SCAN_REQUIRED', 'Y', '첨부 보안검사 완료 후 사용 허용', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA.ATTACHMENT.DEFAULT_RETENTION_DAYS', '365', '첨부 기본 보존일수', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(setting_key, setting_value, description, use_yn, created_by, updated_by)
ON (tgt.setting_key = src.setting_key)
WHEN MATCHED THEN UPDATE SET
  tgt.setting_value = src.setting_value,
  tgt.description = src.description,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by)
VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);

-- AUTO-GENERATED canonical action additions (CPF V70)
-- V70 BZA action-level permission hardening
MERGE INTO bza_permission tgt USING (VALUES
 ('BZA_ADMIN','BZA_AUTHORIZATION','SIMULATE','API','GET','/api/bza/backoffice/permissions/effective',NULL,'ALL','ALL','Y','Y','SYSTEM','SYSTEM'),
 ('BZA_ADMIN','BZA_EMPLOYEE','PII_RAW','API','POST','/api/bza/backoffice/employees/*/contacts/raw',NULL,'ALL','ALL','Y','Y','SYSTEM','SYSTEM'),
 ('BZA_OPERATOR','BZA_AUTHORIZATION','SIMULATE','API','GET','/api/bza/backoffice/permissions/effective',NULL,'ALL','ORGANIZATION','Y','Y','SYSTEM','SYSTEM'),
 ('BZA_APPROVER','BZA_APPROVAL','DECIDE','API','POST','/api/bza/backoffice/approvals/*/actions',NULL,'ALL','ORGANIZATION','Y','Y','SYSTEM','SYSTEM'),
 ('BZA_APPROVER','BZA_APPROVAL','DECIDE','API','POST','/api/bza/approvals/*/decisions',NULL,'ALL','ORGANIZATION','Y','Y','SYSTEM','SYSTEM')
) AS src(role_code,menu_code,button_code,permission_type,http_method,api_pattern,domain_code,environment_code,data_scope,allow_yn,use_yn,created_by,updated_by)
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET http_method=src.http_method,api_pattern=src.api_pattern,domain_code=src.domain_code,data_scope=src.data_scope,allow_yn=src.allow_yn,use_yn='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT(role_code,menu_code,button_code,permission_type,http_method,api_pattern,domain_code,environment_code,data_scope,allow_yn,use_yn,created_by,updated_by)
VALUES(src.role_code,src.menu_code,src.button_code,src.permission_type,src.http_method,src.api_pattern,src.domain_code,src.environment_code,src.data_scope,src.allow_yn,src.use_yn,src.created_by,src.updated_by);
