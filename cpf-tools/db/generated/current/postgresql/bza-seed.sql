-- GENERATED FILE. DO NOT EDIT.
-- Source: cpf-tools/db/canonical/seed-model.json
-- Vendor: postgresql
-- Role: BZA_DB

INSERT INTO BZA_ROLE (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by)
VALUES ('BZA_ADMIN', '업무 관리자', 'Y', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_OPERATOR', '업무 운영자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', '업무 결재자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_VIEWER', '업무 조회자', 'N', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (role_code) DO UPDATE SET role_name=EXCLUDED.role_name, write_allowed_yn=EXCLUDED.write_allowed_yn, data_scope=EXCLUDED.data_scope, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_MENU (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by)
VALUES ('BZA_DASHBOARD', '업무 관리자 대시보드', NULL, 'BZA', '/bza', 'dashboard', 'ALL', '/api/bza/dashboard', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_ORGANIZATION', '조직 관리', NULL, 'BZA', '/bza/organizations', 'organization', 'ALL', '/api/bza/organizations', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_EMPLOYEE', '직원·소속 관리', NULL, 'BZA', '/bza/employees', 'employee', 'ALL', '/api/bza/employees', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_AUTHORIZATION', '업무 권한 관리', NULL, 'BZA', '/bza/authorization', 'shield', 'ALL', '/api/bza/authorization', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVAL', '업무 결재 관리', NULL, 'BZA', '/bza/approvals', 'approval', 'ALL', '/api/bza/approvals', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_AUDIT', '업무 감사 조회', NULL, 'BZA', '/bza/audits', 'audit', 'ALL', '/api/bza/audits', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_ATTACHMENT', '첨부 관리', NULL, 'BZA', '/bza/attachments', 'attachment', 'ALL', '/api/bza/attachments', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_SETTING', '업무 관리자 설정', NULL, 'BZA', '/bza/settings', 'setting', 'ALL', '/api/bza/settings', 80, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (menu_code) DO UPDATE SET menu_name=EXCLUDED.menu_name, parent_menu_code=EXCLUDED.parent_menu_code, module_code=EXCLUDED.module_code, route_path=EXCLUDED.route_path, icon_code=EXCLUDED.icon_code, environment_code=EXCLUDED.environment_code, api_path=EXCLUDED.api_path, sort_order=EXCLUDED.sort_order, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
SELECT 'BZA_ADMIN', menu_code, 'ALL', 'API', '*', CONCAT(api_path, '/**'),
       NULL, environment_code, 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM BZA_MENU
WHERE use_yn = 'Y'
ON CONFLICT (role_code, menu_code, button_code, permission_type, environment_code) DO UPDATE SET permission_type=EXCLUDED.permission_type, http_method=EXCLUDED.http_method, api_pattern=EXCLUDED.api_pattern, environment_code=EXCLUDED.environment_code, data_scope=EXCLUDED.data_scope, allow_yn=EXCLUDED.allow_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
VALUES ('BZA_OPERATOR', 'BZA_DASHBOARD', 'READ', 'API', 'GET', '/api/bza/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_OPERATOR', 'BZA_ORGANIZATION', 'READ', 'API', 'GET', '/api/bza/organizations/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_OPERATOR', 'BZA_EMPLOYEE', 'READ', 'API', 'GET', '/api/bza/employees/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', 'BZA_APPROVAL', 'READ', 'API', 'GET', '/api/bza/approvals/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', 'BZA_APPROVAL', 'DECIDE', 'API', 'POST', '/api/bza/approvals/*/decisions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_VIEWER', 'BZA_DASHBOARD', 'READ', 'API', 'GET', '/api/bza/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_VIEWER', 'BZA_AUDIT', 'READ', 'API', 'GET', '/api/bza/audits/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (role_code, menu_code, button_code, permission_type, environment_code) DO UPDATE SET permission_type=EXCLUDED.permission_type, http_method=EXCLUDED.http_method, api_pattern=EXCLUDED.api_pattern, environment_code=EXCLUDED.environment_code, data_scope=EXCLUDED.data_scope, allow_yn=EXCLUDED.allow_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_PROJECT_SETTING (setting_key, setting_value, description, use_yn, created_by, updated_by)
VALUES ('BZA.APPROVAL.SELF_APPROVAL_ALLOWED', 'N', '기본 자기승인 차단 정책', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.APPROVAL.DEFAULT_DUE_HOURS', '24', '기본 결재 SLA 시간', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.APPROVAL.REQUIRE_PAYLOAD_HASH', 'Y', '결재 대상 Payload 변조 검증용 SHA-256 사용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.AUDIT.HASH_CHAIN_ENABLED', 'Y', '업무 감사 로그 hash-chain 검증 사용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.ATTACHMENT.SECURITY_SCAN_REQUIRED', 'Y', '첨부 보안검사 완료 후 사용 허용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA.ATTACHMENT.DEFAULT_RETENTION_DAYS', '365', '첨부 기본 보존일수', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (setting_key) DO UPDATE SET setting_value=EXCLUDED.setting_value, description=EXCLUDED.description, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
VALUES ('BZA_ADMIN', 'BZA_AUTHORIZATION', 'SIMULATE', 'API', 'GET', '/api/bza/backoffice/permissions/effective', NULL, 'ALL', 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_ADMIN', 'BZA_EMPLOYEE', 'PII_RAW', 'API', 'POST', '/api/bza/backoffice/employees/*/contacts/raw', NULL, 'ALL', 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_OPERATOR', 'BZA_AUTHORIZATION', 'SIMULATE', 'API', 'GET', '/api/bza/backoffice/permissions/effective', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', 'BZA_APPROVAL', 'DECIDE', 'API', 'POST', '/api/bza/backoffice/approvals/*/actions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_APPROVER', 'BZA_APPROVAL', 'DECIDE', 'API', 'POST', '/api/bza/approvals/*/decisions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (role_code, menu_code, button_code, permission_type, environment_code) DO UPDATE SET http_method=EXCLUDED.http_method, api_pattern=EXCLUDED.api_pattern, domain_code=EXCLUDED.domain_code, data_scope=EXCLUDED.data_scope, allow_yn=EXCLUDED.allow_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BZA_ORGANIZATION (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by)
VALUES ('SAMPLE_ROOT', NULL, '샘플 본부', 'COMPANY', 10, CURRENT_TIMESTAMP(3), NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SAMPLE_DEV', 'SAMPLE_ROOT', '샘플 개발부', 'DEPARTMENT', 20, CURRENT_TIMESTAMP(3), NULL, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (organization_code) DO UPDATE SET parent_organization_code=EXCLUDED.parent_organization_code, organization_name=EXCLUDED.organization_name, organization_type=EXCLUDED.organization_type, sort_order=EXCLUDED.sort_order, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_POSITION (position_code, position_name, rank_order, use_yn, created_by, updated_by)
VALUES ('SAMPLE_P1', '샘플 일반', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SAMPLE_P2', '샘플 책임', 20, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (position_code) DO UPDATE SET position_name=EXCLUDED.position_name, rank_order=EXCLUDED.rank_order, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_JOB_TITLE (job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by)
VALUES ('SAMPLE_MEMBER', '샘플 구성원', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('SAMPLE_MANAGER', '샘플 부서장', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (job_title_code) DO UPDATE SET job_title_name=EXCLUDED.job_title_name, manager_yn=EXCLUDED.manager_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_EMPLOYEE (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, manager_employee_no, employment_status, join_date, leave_date, email, mobile_no, use_yn, created_by, updated_by)
VALUES ('SAMPLE0001', NULL, 'SAMPLE_DEV', '샘플 결재자', 'SAMPLE_P2', 'SAMPLE_MANAGER', NULL,
     'ACTIVE', CURRENT_DATE, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SAMPLE0002', NULL, 'SAMPLE_DEV', '샘플 요청자', 'SAMPLE_P1', 'SAMPLE_MEMBER', 'SAMPLE0001',
     'ACTIVE', CURRENT_DATE, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (admin_user_id) DO UPDATE SET organization_code=EXCLUDED.organization_code, employee_name=EXCLUDED.employee_name, position_code=EXCLUDED.position_code, job_title_code=EXCLUDED.job_title_code, manager_employee_no=EXCLUDED.manager_employee_no, employment_status=EXCLUDED.employment_status, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_EMPLOYEE_ASSIGNMENT (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by)
SELECT v.employee_no, v.organization_code, v.position_code, v.job_title_code, 'PRIMARY', 'Y', CURRENT_TIMESTAMP(3), NULL, 'SYSTEM', 'SYSTEM'
FROM (
    SELECT 'SAMPLE0001' employee_no, 'SAMPLE_DEV' organization_code, 'SAMPLE_P2' position_code, 'SAMPLE_MANAGER' job_title_code
    UNION ALL
    SELECT 'SAMPLE0002', 'SAMPLE_DEV', 'SAMPLE_P1', 'SAMPLE_MEMBER'
) v
WHERE NOT EXISTS (
    SELECT 1 FROM BZA_EMPLOYEE_ASSIGNMENT a
    WHERE a.employee_no = v.employee_no AND a.organization_code = v.organization_code
      AND a.primary_yn = 'Y' AND a.effective_to IS NULL
);

INSERT INTO BZA_ORGANIZATION_RESPONSIBILITY (organization_code, responsibility_type, employee_no, effective_from, effective_to, priority_no, use_yn, created_by, updated_by)
SELECT 'SAMPLE_DEV', 'MANAGER', 'SAMPLE0001', CURRENT_TIMESTAMP(3), NULL, 1, 'Y', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM BZA_ORGANIZATION_RESPONSIBILITY
    WHERE organization_code = 'SAMPLE_DEV' AND responsibility_type = 'MANAGER'
      AND employee_no = 'SAMPLE0001' AND use_yn = 'Y' AND effective_to IS NULL
);

INSERT INTO BZA_APPROVAL_POLICY (policy_code, policy_version, policy_name, business_domain, approval_type, effective_from, effective_to, enabled_yn, self_approval_allowed_yn, description, created_by, updated_by)
VALUES (
    'SAMPLE_STANDARD_APPROVAL', 1, '샘플 표준 결재', 'SAMPLE', 'STANDARD',
    CURRENT_TIMESTAMP(3), NULL, 'Y', 'N',
    'Generator/업무관리자 결재 연동을 검증하기 위한 Optional Sample 정책', 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (policy_code, policy_version) DO UPDATE SET policy_name=EXCLUDED.policy_name, enabled_yn=EXCLUDED.enabled_yn, self_approval_allowed_yn=EXCLUDED.self_approval_allowed_yn, description=EXCLUDED.description, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_APPROVAL_POLICY_STEP (policy_code, policy_version, step_no, step_type, target_type, target_code, decision_rule, required_count, required_yn, sort_order, created_by, updated_by)
VALUES (
    'SAMPLE_STANDARD_APPROVAL', 1, 1, 'APPROVAL', 'ORG_MANAGER', 'SAMPLE_DEV',
    'ALL', NULL, 'Y', 10, 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (policy_code, policy_version, step_no, target_type, target_code) DO UPDATE SET step_type=EXCLUDED.step_type, target_type=EXCLUDED.target_type, target_code=EXCLUDED.target_code, decision_rule=EXCLUDED.decision_rule, required_count=EXCLUDED.required_count, required_yn=EXCLUDED.required_yn, sort_order=EXCLUDED.sort_order, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_ADMIN_USER (admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn, login_fail_count, password_change_required_yn, password_expire_at, last_login_at, created_by, updated_by)
VALUES (
    'bza-admin', '업무 관리자 샘플', NULL, 'BZA_MANAGER', 'Y', 'N',
    0, 'Y', NULL, NULL, 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (admin_login_id) DO UPDATE SET admin_name=EXCLUDED.admin_name, role_code=EXCLUDED.role_code, use_yn=EXCLUDED.use_yn, lock_yn=EXCLUDED.lock_yn, login_fail_count=EXCLUDED.login_fail_count, password_change_required_yn=EXCLUDED.password_change_required_yn, password_expire_at=EXCLUDED.password_expire_at, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BZA_LOGIN_HISTORY (admin_user_id, login_domain, admin_login_id, login_result, failure_reason, client_ip, user_agent, transaction_id, module_id, was_id, server_instance_id, created_by, updated_by)
SELECT admin_user_id, 'BZA', 'bza-admin', 'SUCCESS', NULL, '127.0.0.1', 'SQL-SEED',
       '20260715120000000BZAbzaAP010000001', 'BZA', 'bzaAP01', 'local-bza:seed', 'SYSTEM', 'SYSTEM'
FROM BZA_ADMIN_USER
WHERE admin_login_id = 'bza-admin'
  AND NOT EXISTS (
      SELECT 1
      FROM BZA_LOGIN_HISTORY
      WHERE admin_login_id = 'bza-admin'
        AND transaction_id = '20260715120000000BZAbzaAP010000001'
  );

INSERT INTO BZA_MENU (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by)
VALUES ('DASHBOARD', '업무 대시보드', 'BZA', '/bza', '/api/bza/dashboard', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('USER', '백오피스 사용자', 'BZA', '/bza#users', '/api/bza/admin-users', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ORGANIZATION', '조직 관리', 'BZA', '/bza#organizations', '/api/bza/backoffice/organizations', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('EMPLOYEE', '직원 관리', 'BZA', '/bza#employees', '/api/bza/backoffice/employees', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ROLE', '역할 관리', 'BZA', '/bza#roles', '/api/bza/roles', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MENU', '메뉴 관리', 'BZA', '/bza#menus', '/api/bza/menus', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION', '권한 관리', 'BZA', '/bza#permissions', '/api/bza/permissions', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('APPROVAL', '결재 관리', 'BZA', '/bza#approvals', '/api/bza/backoffice/approvals', 80, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SETTING', '업무 설정', 'BZA', '/bza#settings', '/api/bza/settings', 120, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD', '다운로드 감사', 'BZA', '/bza#downloads', '/api/bza/downloads', 130, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT', '업무 감사', 'BZA', '/bza#audits', '/api/bza/backoffice/audits', 140, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION', '업무 알림', 'BZA', '/bza#notifications', '/api/bza/notifications', 150, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ATTACHMENT', '첨부파일', 'BZA', '/bza#attachments', '/api/bza/attachments', 160, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SAVED_SEARCH', '저장 검색', 'BZA', '/bza#savedSearches', '/api/bza/saved-searches', 170, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (menu_code) DO UPDATE SET menu_name=EXCLUDED.menu_name, api_path=EXCLUDED.api_path, sort_order=EXCLUDED.sort_order, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BZA_ROLE (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by)
VALUES (
    'BZA_MANAGER', '업무 관리자', 'Y', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (role_code) DO UPDATE SET role_name=EXCLUDED.role_name, write_allowed_yn=EXCLUDED.write_allowed_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BZA_USER_ROLE (admin_user_id, role_code, valid_from, valid_to, primary_yn, grant_reason, operation_id, created_by, updated_by)
SELECT admin_user_id, 'BZA_MANAGER', CURRENT_TIMESTAMP(3), NULL, 'Y',
       'CPF_TEST_SEED', 'CPF-TEST-BZA-ROLE-MANAGER-0001', 'SYSTEM', 'SYSTEM'
FROM BZA_ADMIN_USER
WHERE admin_login_id = 'bza-admin'
ON CONFLICT (operation_id) DO UPDATE SET valid_to=NULL, primary_yn='Y', grant_reason=EXCLUDED.grant_reason, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by)
VALUES ('BZA_MANAGER', 'DASHBOARD', 'READ', 'API', 'GET', '/api/bza/dashboard', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'USER', 'READ', 'API', 'GET', '/api/bza/admin-users/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'USER', 'WRITE', 'API', 'POST', '/api/bza/admin-users', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ORGANIZATION', 'READ', 'API', 'GET', '/api/bza/backoffice/organizations/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ORGANIZATION', 'WRITE', 'API', 'POST', '/api/bza/backoffice/organizations', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'EMPLOYEE', 'READ', 'API', 'GET', '/api/bza/backoffice/employees/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'EMPLOYEE', 'WRITE', 'API', 'POST', '/api/bza/backoffice/employees', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ROLE', 'READ', 'API', 'GET', '/api/bza/roles/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ROLE', 'WRITE', 'API', 'POST', '/api/bza/roles', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'MENU', 'READ', 'API', 'GET', '/api/bza/menus/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'MENU', 'WRITE', 'API', 'POST', '/api/bza/menus', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'PERMISSION', 'READ', 'API', 'GET', '/api/bza/permissions/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'PERMISSION', 'WRITE', 'API', 'POST', '/api/bza/permissions/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'APPROVAL', 'READ', 'API', 'GET', '/api/bza/backoffice/approvals/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'APPROVAL', 'WRITE', 'API', 'POST', '/api/bza/backoffice/approvals/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'SETTING', 'READ', 'API', 'GET', '/api/bza/settings/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'DOWNLOAD', 'READ', 'API', 'GET', '/api/bza/downloads/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'AUDIT', 'READ', 'API', 'GET', '/api/bza/backoffice/audits/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'NOTIFICATION', 'READ', 'API', 'GET', '/api/bza/notifications/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'NOTIFICATION', 'WRITE', 'API', 'POST', '/api/bza/notifications/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ATTACHMENT', 'READ', 'API', 'GET', '/api/bza/attachments', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ATTACHMENT', 'WRITE', 'API', 'POST', '/api/bza/attachments', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ATTACHMENT', 'DOWNLOAD', 'API', 'GET', '/api/bza/attachments/*/download', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'SAVED_SEARCH', 'READ', 'API', 'GET', '/api/bza/saved-searches/**', 'OWN', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'SAVED_SEARCH', 'WRITE', 'API', 'POST', '/api/bza/saved-searches/**', 'OWN', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (role_code, menu_code, button_code, permission_type, environment_code) DO UPDATE SET allow_yn=EXCLUDED.allow_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BZA_PROJECT_SETTING (setting_key, setting_value, description, use_yn, created_by, updated_by)
VALUES (
    'DOWNLOAD.MASKING.ENABLED', 'Y', '업무 다운로드 마스킹 사용 여부', 'Y', 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (setting_key) DO UPDATE SET setting_value=EXCLUDED.setting_value, description=EXCLUDED.description, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BZA_ORGANIZATION (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by)
VALUES ('HQ', NULL, '본사', 'COMPANY', 10, CURRENT_TIMESTAMP(3), NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPS', 'HQ', '업무운영팀', 'DEPARTMENT', 20, CURRENT_TIMESTAMP(3), NULL, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (organization_code) DO UPDATE SET parent_organization_code=EXCLUDED.parent_organization_code, organization_name=EXCLUDED.organization_name, organization_type=EXCLUDED.organization_type, sort_order=EXCLUDED.sort_order, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BZA_POSITION (position_code, position_name, rank_order, use_yn, created_by, updated_by)
VALUES ('P3', '책임', 30, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (position_code) DO UPDATE SET position_name=EXCLUDED.position_name, rank_order=EXCLUDED.rank_order, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_JOB_TITLE (job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by)
VALUES ('OPERATOR', '업무담당자', 'N', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (job_title_code) DO UPDATE SET job_title_name=EXCLUDED.job_title_name, manager_yn=EXCLUDED.manager_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_EMPLOYEE (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, employment_status, join_date, email, use_yn, created_by, updated_by)
SELECT 'EMP001', admin_user_id, 'OPS', '업무 담당자', 'P3', 'OPERATOR', 'ACTIVE', CURRENT_DATE,
       'operator@example.com', 'Y', 'SYSTEM', 'SYSTEM'
FROM BZA_ADMIN_USER WHERE admin_login_id = 'bza-admin'
ON CONFLICT (admin_user_id) DO UPDATE SET admin_user_id=EXCLUDED.admin_user_id, organization_code=EXCLUDED.organization_code, employee_name=EXCLUDED.employee_name, position_code=EXCLUDED.position_code, job_title_code=EXCLUDED.job_title_code, employment_status=EXCLUDED.employment_status, email=EXCLUDED.email, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BZA_EMPLOYEE_ASSIGNMENT (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by)
VALUES (
    'EMP001', 'OPS', 'P3', 'OPERATOR', 'PRIMARY', 'Y', CURRENT_TIMESTAMP(3), NULL, 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (employee_no, assignment_type, primary_yn) DO UPDATE SET organization_code=EXCLUDED.organization_code, position_code=EXCLUDED.position_code, job_title_code=EXCLUDED.job_title_code, primary_yn=EXCLUDED.primary_yn, effective_to=NULL, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BZA_NOTIFICATION (recipient_login_id, notification_type, title, message_body, reference_type, reference_id, read_yn, use_yn, created_by, updated_by)
SELECT 'bza-admin', 'APPROVAL', '결재 대기 알림', '기준정보 변경 요청 결재를 확인하세요.',
       'APPROVAL', 'BZA-SAMPLE-001', 'N', 'Y', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM BZA_NOTIFICATION
     WHERE recipient_login_id = 'bza-admin'
       AND reference_type = 'APPROVAL'
       AND reference_id = 'BZA-SAMPLE-001'
);

INSERT INTO BZA_SAVED_SEARCH (owner_login_id, screen_code, search_name, criteria_json, shared_yn, use_yn, created_by, updated_by)
VALUES (
    'bza-admin', 'APPROVAL', '진행 중 결재', '{"approvalStatus":"IN_REVIEW"}',
    'N', 'Y', 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (owner_login_id, screen_code, search_name) DO UPDATE SET criteria_json=EXCLUDED.criteria_json, shared_yn=EXCLUDED.shared_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
