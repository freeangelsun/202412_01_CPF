-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=56_bza_product_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=bzaDB
MERGE INTO bza_role tgt USING (
SELECT 'BZA_ADMIN' role_code, '업무 관리자' role_name, 'Y' write_allowed_yn, 'ALL' data_scope, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_OPERATOR' role_code, '업무 운영자' role_name, 'Y' write_allowed_yn, 'ORGANIZATION' data_scope, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_APPROVER' role_code, '업무 결재자' role_name, 'Y' write_allowed_yn, 'ORGANIZATION' data_scope, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_VIEWER' role_code, '업무 조회자' role_name, 'N' write_allowed_yn, 'ORGANIZATION' data_scope, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.role_code = src.role_code)
WHEN MATCHED THEN UPDATE SET tgt.role_name = src.role_name, tgt.write_allowed_yn = src.write_allowed_yn, tgt.data_scope = src.data_scope, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by) VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_menu tgt USING (
SELECT 'BZA_DASHBOARD' menu_code, '업무 관리자 대시보드' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza' route_path, 'dashboard' icon_code, 'ALL' environment_code, '/api/bza/dashboard' api_path, 10 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_ORGANIZATION' menu_code, '조직 관리' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/organizations' route_path, 'organization' icon_code, 'ALL' environment_code, '/api/bza/organizations' api_path, 20 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_EMPLOYEE' menu_code, '직원·소속 관리' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/employees' route_path, 'employee' icon_code, 'ALL' environment_code, '/api/bza/employees' api_path, 30 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_AUTHORIZATION' menu_code, '업무 권한 관리' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/authorization' route_path, 'shield' icon_code, 'ALL' environment_code, '/api/bza/authorization' api_path, 40 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_APPROVAL' menu_code, '업무 결재 관리' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/approvals' route_path, 'approval' icon_code, 'ALL' environment_code, '/api/bza/approvals' api_path, 50 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_AUDIT' menu_code, '업무 감사 조회' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/audits' route_path, 'audit' icon_code, 'ALL' environment_code, '/api/bza/audits' api_path, 60 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_ATTACHMENT' menu_code, '첨부 관리' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/attachments' route_path, 'attachment' icon_code, 'ALL' environment_code, '/api/bza/attachments' api_path, 70 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_SETTING' menu_code, '업무 관리자 설정' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/settings' route_path, 'setting' icon_code, 'ALL' environment_code, '/api/bza/settings' api_path, 80 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.menu_code = src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name = src.menu_name, tgt.parent_menu_code = src.parent_menu_code, tgt.module_code = src.module_code, tgt.route_path = src.route_path, tgt.icon_code = src.icon_code, tgt.environment_code = src.environment_code, tgt.api_path = src.api_path, tgt.sort_order = src.sort_order, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.parent_menu_code, src.module_code, src.route_path, src.icon_code, src.environment_code, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_permission tgt USING (
SELECT 'BZA_ADMIN' role_code, menu_code menu_code, 'ALL' button_code, 'API' permission_type, '*' http_method, (api_path || '/**') api_pattern, NULL domain_code, environment_code environment_code, 'ALL' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM bza_menu
WHERE use_yn = 'Y'
) src ON (tgt.role_code = src.role_code AND tgt.menu_code = src.menu_code AND tgt.button_code = src.button_code AND tgt.permission_type = src.permission_type AND tgt.environment_code = src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.permission_type = src.permission_type, tgt.http_method = src.http_method, tgt.api_pattern = src.api_pattern, tgt.environment_code = src.environment_code, tgt.data_scope = src.data_scope, tgt.allow_yn = src.allow_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_permission tgt USING (
SELECT 'BZA_OPERATOR' role_code, 'BZA_DASHBOARD' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/dashboard/**' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_OPERATOR' role_code, 'BZA_ORGANIZATION' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/organizations/**' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_OPERATOR' role_code, 'BZA_EMPLOYEE' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/employees/**' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_APPROVER' role_code, 'BZA_APPROVAL' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/approvals/**' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_APPROVER' role_code, 'BZA_APPROVAL' menu_code, 'DECIDE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/approvals/*/decisions' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_VIEWER' role_code, 'BZA_DASHBOARD' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/dashboard/**' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_VIEWER' role_code, 'BZA_AUDIT' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/audits/**' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.role_code = src.role_code AND tgt.menu_code = src.menu_code AND tgt.button_code = src.button_code AND tgt.permission_type = src.permission_type AND tgt.environment_code = src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.permission_type = src.permission_type, tgt.http_method = src.http_method, tgt.api_pattern = src.api_pattern, tgt.environment_code = src.environment_code, tgt.data_scope = src.data_scope, tgt.allow_yn = src.allow_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_project_setting tgt USING (
SELECT 'BZA.APPROVAL.SELF_APPROVAL_ALLOWED' setting_key, 'N' setting_value, '기본 자기승인 차단 정책' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA.APPROVAL.DEFAULT_DUE_HOURS' setting_key, '24' setting_value, '기본 결재 SLA 시간' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA.APPROVAL.REQUIRE_PAYLOAD_HASH' setting_key, 'Y' setting_value, '결재 대상 Payload 변조 검증용 SHA-256 사용' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA.AUDIT.HASH_CHAIN_ENABLED' setting_key, 'Y' setting_value, '업무 감사 로그 hash-chain 검증 사용' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA.ATTACHMENT.SECURITY_SCAN_REQUIRED' setting_key, 'Y' setting_value, '첨부 보안검사 완료 후 사용 허용' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA.ATTACHMENT.DEFAULT_RETENTION_DAYS' setting_key, '365' setting_value, '첨부 기본 보존일수' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.setting_key = src.setting_key)
WHEN MATCHED THEN UPDATE SET tgt.setting_value = src.setting_value, tgt.description = src.description, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_permission tgt USING (
SELECT 'BZA_ADMIN' role_code, 'BZA_AUTHORIZATION' menu_code, 'SIMULATE' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/backoffice/permissions/effective' api_pattern, NULL domain_code, 'ALL' environment_code, 'ALL' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_ADMIN' role_code, 'BZA_EMPLOYEE' menu_code, 'PII_RAW' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/backoffice/employees/*/contacts/raw' api_pattern, NULL domain_code, 'ALL' environment_code, 'ALL' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_OPERATOR' role_code, 'BZA_AUTHORIZATION' menu_code, 'SIMULATE' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/backoffice/permissions/effective' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_APPROVER' role_code, 'BZA_APPROVAL' menu_code, 'DECIDE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/backoffice/approvals/*/actions' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_APPROVER' role_code, 'BZA_APPROVAL' menu_code, 'DECIDE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/approvals/*/decisions' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.role_code = src.role_code AND tgt.menu_code = src.menu_code AND tgt.button_code = src.button_code AND tgt.permission_type = src.permission_type AND tgt.environment_code = src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method = src.http_method, tgt.api_pattern = src.api_pattern, tgt.domain_code = src.domain_code, tgt.data_scope = src.data_scope, tgt.allow_yn = src.allow_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
