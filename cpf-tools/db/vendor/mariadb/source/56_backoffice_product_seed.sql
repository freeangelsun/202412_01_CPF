-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=mariadb; source=56_backoffice_product_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=mbwDB
USE mbwDB;
INSERT INTO MBW_ROLE (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by)
VALUES ('MBW_ADMIN', '업무 관리자', 'Y', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_OPERATOR', '업무 운영자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_APPROVER', '업무 결재자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_VIEWER', '업무 조회자', 'N', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name), write_allowed_yn=VALUES(write_allowed_yn), data_scope=VALUES(data_scope), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_MENU (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by)
VALUES ('MBW_DASHBOARD', '업무 관리자 대시보드', NULL, 'MBW', '/backoffice', 'dashboard', 'ALL', '/api/v1/backoffice/dashboard', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_ORGANIZATION', '조직 관리', NULL, 'MBW', '/backoffice/organizations', 'organization', 'ALL', '/api/v1/backoffice/organizations', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_EMPLOYEE', '직원·소속 관리', NULL, 'MBW', '/backoffice/employees', 'employee', 'ALL', '/api/v1/backoffice/employees', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_AUTHORIZATION', '업무 권한 관리', NULL, 'MBW', '/backoffice/authorization', 'shield', 'ALL', '/api/v1/backoffice/authorization', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_APPROVAL', '업무 결재 관리', NULL, 'MBW', '/backoffice/approvals', 'approval', 'ALL', '/api/v1/backoffice/approvals', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_AUDIT', '업무 감사 조회', NULL, 'MBW', '/backoffice/audits', 'audit', 'ALL', '/api/v1/backoffice/audits', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_ATTACHMENT', '첨부 관리', NULL, 'MBW', '/backoffice/attachments', 'attachment', 'ALL', '/api/v1/backoffice/attachments', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_SETTING', '업무 관리자 설정', NULL, 'MBW', '/backoffice/settings', 'setting', 'ALL', '/api/v1/backoffice/settings', 80, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), parent_menu_code=VALUES(parent_menu_code), module_code=VALUES(module_code), route_path=VALUES(route_path), icon_code=VALUES(icon_code), environment_code=VALUES(environment_code), api_path=VALUES(api_path), sort_order=VALUES(sort_order), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
SELECT 'MBW_ADMIN', menu_code, 'ALL', 'API', '*', CONCAT(api_path, '/**'),
       NULL, environment_code, 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM MBW_MENU
WHERE use_yn = 'Y'
ON DUPLICATE KEY UPDATE http_method=VALUES(http_method), api_pattern=VALUES(api_pattern), data_scope=VALUES(data_scope), allow_yn=VALUES(allow_yn), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
VALUES ('MBW_OPERATOR', 'MBW_DASHBOARD', 'READ', 'API', 'GET', '/api/v1/backoffice/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_OPERATOR', 'MBW_ORGANIZATION', 'READ', 'API', 'GET', '/api/v1/backoffice/organizations/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_OPERATOR', 'MBW_EMPLOYEE', 'READ', 'API', 'GET', '/api/v1/backoffice/employees/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_APPROVER', 'MBW_APPROVAL', 'READ', 'API', 'GET', '/api/v1/backoffice/approvals/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_APPROVER', 'MBW_APPROVAL', 'DECIDE', 'API', 'POST', '/api/v1/backoffice/approvals/*/decisions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_VIEWER', 'MBW_DASHBOARD', 'READ', 'API', 'GET', '/api/v1/backoffice/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_VIEWER', 'MBW_AUDIT', 'READ', 'API', 'GET', '/api/v1/backoffice/audits/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE http_method=VALUES(http_method), api_pattern=VALUES(api_pattern), data_scope=VALUES(data_scope), allow_yn=VALUES(allow_yn), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_PROJECT_SETTING (setting_key, setting_value, description, use_yn, created_by, updated_by)
VALUES ('MBW.APPROVAL.SELF_APPROVAL_ALLOWED', 'N', '기본 자기승인 차단 정책', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW.APPROVAL.DEFAULT_DUE_HOURS', '24', '기본 결재 SLA 시간', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW.APPROVAL.REQUIRE_PAYLOAD_HASH', 'Y', '결재 대상 Payload 변조 검증용 SHA-256 사용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW.AUDIT.HASH_CHAIN_ENABLED', 'Y', '업무 감사 로그 hash-chain 검증 사용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW.ATTACHMENT.SECURITY_SCAN_REQUIRED', 'Y', '첨부 보안검사 완료 후 사용 허용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW.ATTACHMENT.DEFAULT_RETENTION_DAYS', '365', '첨부 기본 보존일수', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value), description=VALUES(description), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
VALUES ('MBW_ADMIN', 'MBW_AUTHORIZATION', 'SIMULATE', 'API', 'GET', '/api/v1/backoffice/permissions/effective', NULL, 'ALL', 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_ADMIN', 'MBW_EMPLOYEE', 'PII_RAW', 'API', 'POST', '/api/v1/backoffice/employees/*/contacts/raw', NULL, 'ALL', 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_OPERATOR', 'MBW_AUTHORIZATION', 'SIMULATE', 'API', 'GET', '/api/v1/backoffice/permissions/effective', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_APPROVER', 'MBW_APPROVAL', 'DECIDE', 'API', 'POST', '/api/v1/backoffice/approvals/*/decisions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE http_method=VALUES(http_method), api_pattern=VALUES(api_pattern), domain_code=VALUES(domain_code), data_scope=VALUES(data_scope), allow_yn=VALUES(allow_yn), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
