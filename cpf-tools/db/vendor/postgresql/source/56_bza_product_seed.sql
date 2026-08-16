-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=56_bza_product_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=bzaDB
INSERT INTO BZA_ROLE (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by) VALUES ('BZA_ADMIN', '업무 관리자', 'Y', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_OPERATOR', '업무 운영자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', '업무 결재자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_VIEWER', '업무 조회자', 'N', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (role_code) DO UPDATE SET role_name = EXCLUDED.role_name, write_allowed_yn = EXCLUDED.write_allowed_yn, data_scope = EXCLUDED.data_scope, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BZA_MENU (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by) VALUES ('BZA_DASHBOARD', '업무 관리자 대시보드', NULL, 'BZA', '/bza', 'dashboard', 'ALL', '/api/bza/dashboard', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_ORGANIZATION', '조직 관리', NULL, 'BZA', '/bza/organizations', 'organization', 'ALL', '/api/bza/organizations', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_EMPLOYEE', '직원·소속 관리', NULL, 'BZA', '/bza/employees', 'employee', 'ALL', '/api/bza/employees', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_AUTHORIZATION', '업무 권한 관리', NULL, 'BZA', '/bza/authorization', 'shield', 'ALL', '/api/bza/authorization', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVAL', '업무 결재 관리', NULL, 'BZA', '/bza/approvals', 'approval', 'ALL', '/api/bza/approvals', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_AUDIT', '업무 감사 조회', NULL, 'BZA', '/bza/audits', 'audit', 'ALL', '/api/bza/audits', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_ATTACHMENT', '첨부 관리', NULL, 'BZA', '/bza/attachments', 'attachment', 'ALL', '/api/bza/attachments', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_SETTING', '업무 관리자 설정', NULL, 'BZA', '/bza/settings', 'setting', 'ALL', '/api/bza/settings', 80, 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (menu_code) DO UPDATE SET menu_name = EXCLUDED.menu_name, parent_menu_code = EXCLUDED.parent_menu_code, module_code = EXCLUDED.module_code, route_path = EXCLUDED.route_path, icon_code = EXCLUDED.icon_code, environment_code = EXCLUDED.environment_code, api_path = EXCLUDED.api_path, sort_order = EXCLUDED.sort_order, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BZA_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) SELECT 'BZA_ADMIN', menu_code, 'ALL', 'API', '*', (api_path || '/**'),
       NULL, environment_code, 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM BZA_MENU
WHERE use_yn = 'Y' ON CONFLICT (role_code, menu_code, button_code, permission_type, environment_code) DO UPDATE SET permission_type = EXCLUDED.permission_type, http_method = EXCLUDED.http_method, api_pattern = EXCLUDED.api_pattern, environment_code = EXCLUDED.environment_code, data_scope = EXCLUDED.data_scope, allow_yn = EXCLUDED.allow_yn, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BZA_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES ('BZA_OPERATOR', 'BZA_DASHBOARD', 'READ', 'API', 'GET', '/api/bza/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_OPERATOR', 'BZA_ORGANIZATION', 'READ', 'API', 'GET', '/api/bza/organizations/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_OPERATOR', 'BZA_EMPLOYEE', 'READ', 'API', 'GET', '/api/bza/employees/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', 'BZA_APPROVAL', 'READ', 'API', 'GET', '/api/bza/approvals/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', 'BZA_APPROVAL', 'DECIDE', 'API', 'POST', '/api/bza/approvals/*/decisions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_VIEWER', 'BZA_DASHBOARD', 'READ', 'API', 'GET', '/api/bza/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_VIEWER', 'BZA_AUDIT', 'READ', 'API', 'GET', '/api/bza/audits/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (role_code, menu_code, button_code, permission_type, environment_code) DO UPDATE SET permission_type = EXCLUDED.permission_type, http_method = EXCLUDED.http_method, api_pattern = EXCLUDED.api_pattern, environment_code = EXCLUDED.environment_code, data_scope = EXCLUDED.data_scope, allow_yn = EXCLUDED.allow_yn, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BZA_PROJECT_SETTING (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES ('BZA.APPROVAL.SELF_APPROVAL_ALLOWED', 'N', '기본 자기승인 차단 정책', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.APPROVAL.DEFAULT_DUE_HOURS', '24', '기본 결재 SLA 시간', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.APPROVAL.REQUIRE_PAYLOAD_HASH', 'Y', '결재 대상 Payload 변조 검증용 SHA-256 사용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.AUDIT.HASH_CHAIN_ENABLED', 'Y', '업무 감사 로그 hash-chain 검증 사용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.ATTACHMENT.SECURITY_SCAN_REQUIRED', 'Y', '첨부 보안검사 완료 후 사용 허용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.ATTACHMENT.DEFAULT_RETENTION_DAYS', '365', '첨부 기본 보존일수', 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (setting_key) DO UPDATE SET setting_value = EXCLUDED.setting_value, description = EXCLUDED.description, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BZA_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES ('BZA_ADMIN', 'BZA_AUTHORIZATION', 'SIMULATE', 'API', 'GET', '/api/bza/backoffice/permissions/effective', NULL, 'ALL', 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_ADMIN', 'BZA_EMPLOYEE', 'PII_RAW', 'API', 'POST', '/api/bza/backoffice/employees/*/contacts/raw', NULL, 'ALL', 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_OPERATOR', 'BZA_AUTHORIZATION', 'SIMULATE', 'API', 'GET', '/api/bza/backoffice/permissions/effective', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', 'BZA_APPROVAL', 'DECIDE', 'API', 'POST', '/api/bza/backoffice/approvals/*/actions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', 'BZA_APPROVAL', 'DECIDE', 'API', 'POST', '/api/bza/approvals/*/decisions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (role_code, menu_code, button_code, permission_type, environment_code) DO UPDATE SET http_method = EXCLUDED.http_method, api_pattern = EXCLUDED.api_pattern, domain_code = EXCLUDED.domain_code, data_scope = EXCLUDED.data_scope, allow_yn = EXCLUDED.allow_yn, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
