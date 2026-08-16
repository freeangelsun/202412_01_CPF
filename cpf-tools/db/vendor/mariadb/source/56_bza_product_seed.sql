-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=mariadb; source=56_bza_product_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=bzaDB
INSERT INTO BZA_ROLE (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by) VALUES ('BZA_ADMIN', '업무 관리자', 'Y', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_OPERATOR', '업무 운영자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', '업무 결재자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_VIEWER', '업무 조회자', 'N', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM') ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), write_allowed_yn = VALUES(write_allowed_yn), data_scope = VALUES(data_scope), use_yn = VALUES(use_yn), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);
INSERT INTO BZA_MENU (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by) VALUES ('BZA_DASHBOARD', '업무 관리자 대시보드', NULL, 'BZA', '/bza', 'dashboard', 'ALL', '/api/bza/dashboard', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_ORGANIZATION', '조직 관리', NULL, 'BZA', '/bza/organizations', 'organization', 'ALL', '/api/bza/organizations', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_EMPLOYEE', '직원·소속 관리', NULL, 'BZA', '/bza/employees', 'employee', 'ALL', '/api/bza/employees', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_AUTHORIZATION', '업무 권한 관리', NULL, 'BZA', '/bza/authorization', 'shield', 'ALL', '/api/bza/authorization', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVAL', '업무 결재 관리', NULL, 'BZA', '/bza/approvals', 'approval', 'ALL', '/api/bza/approvals', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_AUDIT', '업무 감사 조회', NULL, 'BZA', '/bza/audits', 'audit', 'ALL', '/api/bza/audits', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_ATTACHMENT', '첨부 관리', NULL, 'BZA', '/bza/attachments', 'attachment', 'ALL', '/api/bza/attachments', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_SETTING', '업무 관리자 설정', NULL, 'BZA', '/bza/settings', 'setting', 'ALL', '/api/bza/settings', 80, 'Y', 'SYSTEM', 'SYSTEM') ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), parent_menu_code = VALUES(parent_menu_code), module_code = VALUES(module_code), route_path = VALUES(route_path), icon_code = VALUES(icon_code), environment_code = VALUES(environment_code), api_path = VALUES(api_path), sort_order = VALUES(sort_order), use_yn = VALUES(use_yn), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);
INSERT INTO BZA_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) SELECT 'BZA_ADMIN', menu_code, 'ALL', 'API', '*', CONCAT(api_path, '/**'),
       NULL, environment_code, 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM BZA_MENU
WHERE use_yn = 'Y' ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type), http_method = VALUES(http_method), api_pattern = VALUES(api_pattern), environment_code = VALUES(environment_code), data_scope = VALUES(data_scope), allow_yn = VALUES(allow_yn), use_yn = VALUES(use_yn), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);
INSERT INTO BZA_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES ('BZA_OPERATOR', 'BZA_DASHBOARD', 'READ', 'API', 'GET', '/api/bza/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_OPERATOR', 'BZA_ORGANIZATION', 'READ', 'API', 'GET', '/api/bza/organizations/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_OPERATOR', 'BZA_EMPLOYEE', 'READ', 'API', 'GET', '/api/bza/employees/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', 'BZA_APPROVAL', 'READ', 'API', 'GET', '/api/bza/approvals/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', 'BZA_APPROVAL', 'DECIDE', 'API', 'POST', '/api/bza/approvals/*/decisions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_VIEWER', 'BZA_DASHBOARD', 'READ', 'API', 'GET', '/api/bza/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_VIEWER', 'BZA_AUDIT', 'READ', 'API', 'GET', '/api/bza/audits/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM') ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type), http_method = VALUES(http_method), api_pattern = VALUES(api_pattern), environment_code = VALUES(environment_code), data_scope = VALUES(data_scope), allow_yn = VALUES(allow_yn), use_yn = VALUES(use_yn), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);
INSERT INTO BZA_PROJECT_SETTING (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES ('BZA.APPROVAL.SELF_APPROVAL_ALLOWED', 'N', '기본 자기승인 차단 정책', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.APPROVAL.DEFAULT_DUE_HOURS', '24', '기본 결재 SLA 시간', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.APPROVAL.REQUIRE_PAYLOAD_HASH', 'Y', '결재 대상 Payload 변조 검증용 SHA-256 사용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.AUDIT.HASH_CHAIN_ENABLED', 'Y', '업무 감사 로그 hash-chain 검증 사용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.ATTACHMENT.SECURITY_SCAN_REQUIRED', 'Y', '첨부 보안검사 완료 후 사용 허용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.ATTACHMENT.DEFAULT_RETENTION_DAYS', '365', '첨부 기본 보존일수', 'Y', 'SYSTEM', 'SYSTEM') ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), description = VALUES(description), use_yn = VALUES(use_yn), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);
INSERT INTO BZA_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES ('BZA_ADMIN', 'BZA_AUTHORIZATION', 'SIMULATE', 'API', 'GET', '/api/bza/backoffice/permissions/effective', NULL, 'ALL', 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_ADMIN', 'BZA_EMPLOYEE', 'PII_RAW', 'API', 'POST', '/api/bza/backoffice/employees/*/contacts/raw', NULL, 'ALL', 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_OPERATOR', 'BZA_AUTHORIZATION', 'SIMULATE', 'API', 'GET', '/api/bza/backoffice/permissions/effective', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', 'BZA_APPROVAL', 'DECIDE', 'API', 'POST', '/api/bza/backoffice/approvals/*/actions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', 'BZA_APPROVAL', 'DECIDE', 'API', 'POST', '/api/bza/approvals/*/decisions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM') ON DUPLICATE KEY UPDATE http_method = VALUES(http_method), api_pattern = VALUES(api_pattern), domain_code = VALUES(domain_code), data_scope = VALUES(data_scope), allow_yn = VALUES(allow_yn), use_yn = VALUES(use_yn), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;
