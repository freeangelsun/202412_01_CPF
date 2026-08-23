-- GENERATED FILE. DO NOT EDIT.
-- Source: cpf-tools/db/canonical/seed-model.json
-- Vendor: oracle
-- Role: CUSTOMER_BUSINESS_DB

MERGE INTO MBW_ROLE tgt
USING (SELECT 'MBW_ADMIN' AS role_code, '업무 관리자' AS role_name, 'Y' AS write_allowed_yn, 'ALL' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_OPERATOR' AS role_code, '업무 운영자' AS role_name, 'Y' AS write_allowed_yn, 'ORGANIZATION' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_APPROVER' AS role_code, '업무 결재자' AS role_name, 'Y' AS write_allowed_yn, 'ORGANIZATION' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_VIEWER' AS role_code, '업무 조회자' AS role_name, 'N' AS write_allowed_yn, 'ORGANIZATION' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code)
WHEN MATCHED THEN UPDATE SET tgt.role_name=src.role_name, tgt.write_allowed_yn=src.write_allowed_yn, tgt.data_scope=src.data_scope, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by) VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_MENU tgt
USING (SELECT 'MBW_DASHBOARD' AS menu_code, '업무 관리자 대시보드' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice' AS route_path, 'dashboard' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/dashboard' AS api_path, 10 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_ORGANIZATION' AS menu_code, '조직 관리' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/organizations' AS route_path, 'organization' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/organizations' AS api_path, 20 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_EMPLOYEE' AS menu_code, '직원·소속 관리' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/employees' AS route_path, 'employee' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/employees' AS api_path, 30 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_AUTHORIZATION' AS menu_code, '업무 권한 관리' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/authorization' AS route_path, 'shield' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/authorization' AS api_path, 40 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_APPROVAL' AS menu_code, '업무 결재 관리' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/approvals' AS route_path, 'approval' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/approvals' AS api_path, 50 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_AUDIT' AS menu_code, '업무 감사 조회' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/audits' AS route_path, 'audit' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/audits' AS api_path, 60 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_ATTACHMENT' AS menu_code, '첨부 관리' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/attachments' AS route_path, 'attachment' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/attachments' AS api_path, 70 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_SETTING' AS menu_code, '업무 관리자 설정' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/settings' AS route_path, 'setting' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/settings' AS api_path, 80 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.parent_menu_code=src.parent_menu_code, tgt.module_code=src.module_code, tgt.route_path=src.route_path, tgt.icon_code=src.icon_code, tgt.environment_code=src.environment_code, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.parent_menu_code, src.module_code, src.route_path, src.icon_code, src.environment_code, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_ADMIN' AS role_code, menu_code AS menu_code, 'ALL' AS button_code, 'API' AS permission_type, '*' AS http_method, CONCAT(api_path, '/**') AS api_pattern, NULL AS domain_code, environment_code AS environment_code, 'ALL' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM MBW_MENU
WHERE use_yn = 'Y') src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.permission_type=src.permission_type, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.environment_code=src.environment_code, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_OPERATOR' AS role_code, 'MBW_DASHBOARD' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/dashboard/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_OPERATOR' AS role_code, 'MBW_ORGANIZATION' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/organizations/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_OPERATOR' AS role_code, 'MBW_EMPLOYEE' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/employees/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_APPROVER' AS role_code, 'MBW_APPROVAL' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/approvals/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_APPROVER' AS role_code, 'MBW_APPROVAL' AS menu_code, 'DECIDE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/approvals/*/decisions' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_VIEWER' AS role_code, 'MBW_DASHBOARD' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/dashboard/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_VIEWER' AS role_code, 'MBW_AUDIT' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/audits/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.permission_type=src.permission_type, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.environment_code=src.environment_code, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_PROJECT_SETTING tgt
USING (SELECT 'MBW.APPROVAL.SELF_APPROVAL_ALLOWED' AS setting_key, 'N' AS setting_value, '기본 자기승인 차단 정책' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW.APPROVAL.DEFAULT_DUE_HOURS' AS setting_key, '24' AS setting_value, '기본 결재 SLA 시간' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW.APPROVAL.REQUIRE_PAYLOAD_HASH' AS setting_key, 'Y' AS setting_value, '결재 대상 Payload 변조 검증용 SHA-256 사용' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW.AUDIT.HASH_CHAIN_ENABLED' AS setting_key, 'Y' AS setting_value, '업무 감사 로그 hash-chain 검증 사용' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW.ATTACHMENT.SECURITY_SCAN_REQUIRED' AS setting_key, 'Y' AS setting_value, '첨부 보안검사 완료 후 사용 허용' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW.ATTACHMENT.DEFAULT_RETENTION_DAYS' AS setting_key, '365' AS setting_value, '첨부 기본 보존일수' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.setting_key=src.setting_key)
WHEN MATCHED THEN UPDATE SET tgt.setting_value=src.setting_value, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_ADMIN' AS role_code, 'MBW_AUTHORIZATION' AS menu_code, 'SIMULATE' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/permissions/effective' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ALL' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_ADMIN' AS role_code, 'MBW_EMPLOYEE' AS menu_code, 'PII_RAW' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/employees/*/contacts/raw' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ALL' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_OPERATOR' AS role_code, 'MBW_AUTHORIZATION' AS menu_code, 'SIMULATE' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/permissions/effective' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_APPROVER' AS role_code, 'MBW_APPROVAL' AS menu_code, 'DECIDE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/approvals/*/decisions' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.domain_code=src.domain_code, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_ORGANIZATION tgt
USING (SELECT 'SAMPLE_ROOT' AS organization_code, NULL AS parent_organization_code, '샘플 본부' AS organization_name, 'COMPANY' AS organization_type, 10 AS sort_order, SYSTIMESTAMP AS effective_from, NULL AS effective_to, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'SAMPLE_DEV' AS organization_code, 'SAMPLE_ROOT' AS parent_organization_code, '샘플 개발부' AS organization_name, 'DEPARTMENT' AS organization_type, 20 AS sort_order, SYSTIMESTAMP AS effective_from, NULL AS effective_to, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.organization_code=src.organization_code)
WHEN MATCHED THEN UPDATE SET tgt.parent_organization_code=src.parent_organization_code, tgt.organization_name=src.organization_name, tgt.organization_type=src.organization_type, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by) VALUES (src.organization_code, src.parent_organization_code, src.organization_name, src.organization_type, src.sort_order, src.effective_from, src.effective_to, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_POSITION tgt
USING (SELECT 'SAMPLE_P1' AS position_code, '샘플 일반' AS position_name, 10 AS rank_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'SAMPLE_P2' AS position_code, '샘플 책임' AS position_name, 20 AS rank_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.position_code=src.position_code)
WHEN MATCHED THEN UPDATE SET tgt.position_name=src.position_name, tgt.rank_order=src.rank_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (position_code, position_name, rank_order, use_yn, created_by, updated_by) VALUES (src.position_code, src.position_name, src.rank_order, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_JOB_TITLE tgt
USING (SELECT 'SAMPLE_MEMBER' AS job_title_code, '샘플 구성원' AS job_title_name, 'N' AS manager_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'SAMPLE_MANAGER' AS job_title_code, '샘플 부서장' AS job_title_name, 'Y' AS manager_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.job_title_code=src.job_title_code)
WHEN MATCHED THEN UPDATE SET tgt.job_title_name=src.job_title_name, tgt.manager_yn=src.manager_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by) VALUES (src.job_title_code, src.job_title_name, src.manager_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_EMPLOYEE tgt
USING (SELECT 'SAMPLE0001' AS employee_no, NULL AS admin_user_id, 'SAMPLE_DEV' AS organization_code, '샘플 결재자' AS employee_name, 'SAMPLE_P2' AS position_code, 'SAMPLE_MANAGER' AS job_title_code, NULL AS manager_employee_no, 'ACTIVE' AS employment_status, CURRENT_DATE AS join_date, NULL AS leave_date, NULL AS email, NULL AS mobile_no, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'SAMPLE0002' AS employee_no, NULL AS admin_user_id, 'SAMPLE_DEV' AS organization_code, '샘플 요청자' AS employee_name, 'SAMPLE_P1' AS position_code, 'SAMPLE_MEMBER' AS job_title_code, 'SAMPLE0001' AS manager_employee_no, 'ACTIVE' AS employment_status, CURRENT_DATE AS join_date, NULL AS leave_date, NULL AS email, NULL AS mobile_no, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.admin_user_id=src.admin_user_id)
WHEN MATCHED THEN UPDATE SET tgt.organization_code=src.organization_code, tgt.employee_name=src.employee_name, tgt.position_code=src.position_code, tgt.job_title_code=src.job_title_code, tgt.manager_employee_no=src.manager_employee_no, tgt.employment_status=src.employment_status, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, manager_employee_no, employment_status, join_date, leave_date, email, mobile_no, use_yn, created_by, updated_by) VALUES (src.employee_no, src.admin_user_id, src.organization_code, src.employee_name, src.position_code, src.job_title_code, src.manager_employee_no, src.employment_status, src.join_date, src.leave_date, src.email, src.mobile_no, src.use_yn, src.created_by, src.updated_by);

INSERT INTO MBW_EMPLOYEE_ASSIGNMENT (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by)
SELECT v.employee_no, v.organization_code, v.position_code, v.job_title_code, 'PRIMARY', 'Y', SYSTIMESTAMP, NULL, 'SYSTEM', 'SYSTEM'
FROM (
    SELECT 'SAMPLE0001' employee_no, 'SAMPLE_DEV' organization_code, 'SAMPLE_P2' position_code, 'SAMPLE_MANAGER' job_title_code
    UNION ALL
    SELECT 'SAMPLE0002', 'SAMPLE_DEV', 'SAMPLE_P1', 'SAMPLE_MEMBER'
) v
WHERE NOT EXISTS (
    SELECT 1 FROM MBW_EMPLOYEE_ASSIGNMENT a
    WHERE a.employee_no = v.employee_no AND a.organization_code = v.organization_code
      AND a.primary_yn = 'Y' AND a.effective_to IS NULL
);

INSERT INTO MBW_ORGANIZATION_RESPONSIBILITY (organization_code, responsibility_type, employee_no, effective_from, effective_to, priority_no, use_yn, created_by, updated_by)
SELECT 'SAMPLE_DEV', 'MANAGER', 'SAMPLE0001', SYSTIMESTAMP, NULL, 1, 'Y', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM MBW_ORGANIZATION_RESPONSIBILITY
    WHERE organization_code = 'SAMPLE_DEV' AND responsibility_type = 'MANAGER'
      AND employee_no = 'SAMPLE0001' AND use_yn = 'Y' AND effective_to IS NULL
);

MERGE INTO MBW_APPROVAL_POLICY tgt
USING (SELECT 'SAMPLE_STANDARD_APPROVAL' AS policy_code, 1 AS policy_version, '샘플 표준 결재' AS policy_name, 'SAMPLE' AS business_domain, 'STANDARD' AS approval_type, SYSTIMESTAMP AS effective_from, NULL AS effective_to, 'Y' AS enabled_yn, 'N' AS self_approval_allowed_yn, 'Generator/업무관리자 결재 연동을 검증하기 위한 Optional Sample 정책' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.policy_code=src.policy_code AND tgt.policy_version=src.policy_version)
WHEN MATCHED THEN UPDATE SET tgt.policy_name=src.policy_name, tgt.enabled_yn=src.enabled_yn, tgt.self_approval_allowed_yn=src.self_approval_allowed_yn, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (policy_code, policy_version, policy_name, business_domain, approval_type, effective_from, effective_to, enabled_yn, self_approval_allowed_yn, description, created_by, updated_by) VALUES (src.policy_code, src.policy_version, src.policy_name, src.business_domain, src.approval_type, src.effective_from, src.effective_to, src.enabled_yn, src.self_approval_allowed_yn, src.description, src.created_by, src.updated_by);

MERGE INTO MBW_APPROVAL_POLICY_STEP tgt
USING (SELECT 'SAMPLE_STANDARD_APPROVAL' AS policy_code, 1 AS policy_version, 1 AS step_no, 'APPROVAL' AS step_type, 'ORG_MANAGER' AS target_type, 'SAMPLE_DEV' AS target_code, 'ALL' AS decision_rule, NULL AS required_count, 'Y' AS required_yn, 10 AS sort_order, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.policy_code=src.policy_code AND tgt.policy_version=src.policy_version AND tgt.step_no=src.step_no AND tgt.target_type=src.target_type AND tgt.target_code=src.target_code)
WHEN MATCHED THEN UPDATE SET tgt.step_type=src.step_type, tgt.target_type=src.target_type, tgt.target_code=src.target_code, tgt.decision_rule=src.decision_rule, tgt.required_count=src.required_count, tgt.required_yn=src.required_yn, tgt.sort_order=src.sort_order, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (policy_code, policy_version, step_no, step_type, target_type, target_code, decision_rule, required_count, required_yn, sort_order, created_by, updated_by) VALUES (src.policy_code, src.policy_version, src.step_no, src.step_type, src.target_type, src.target_code, src.decision_rule, src.required_count, src.required_yn, src.sort_order, src.created_by, src.updated_by);

MERGE INTO MBW_ADMIN_USER tgt
USING (SELECT 'mbw-admin' AS admin_login_id, '업무 관리자 샘플' AS admin_name, NULL AS password_hash, 'MBW_MANAGER' AS role_code, 'Y' AS use_yn, 'N' AS lock_yn, 0 AS login_fail_count, 'Y' AS password_change_required_yn, NULL AS password_expire_at, NULL AS last_login_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.admin_login_id=src.admin_login_id)
WHEN MATCHED THEN UPDATE SET tgt.admin_name=src.admin_name, tgt.role_code=src.role_code, tgt.use_yn=src.use_yn, tgt.lock_yn=src.lock_yn, tgt.login_fail_count=src.login_fail_count, tgt.password_change_required_yn=src.password_change_required_yn, tgt.password_expire_at=src.password_expire_at, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn, login_fail_count, password_change_required_yn, password_expire_at, last_login_at, created_by, updated_by) VALUES (src.admin_login_id, src.admin_name, src.password_hash, src.role_code, src.use_yn, src.lock_yn, src.login_fail_count, src.password_change_required_yn, src.password_expire_at, src.last_login_at, src.created_by, src.updated_by);

INSERT INTO MBW_LOGIN_HISTORY (admin_user_id, login_domain, admin_login_id, login_result, failure_reason, client_ip, user_agent, transaction_id, system_code, application_name, instance_id, created_by, updated_by)
SELECT admin_user_id, 'MBW', 'mbw-admin', 'SUCCESS', NULL, '127.0.0.1', 'SQL-SEED',
       '20260715120000000MBWmbwAP010000001', 'MBW', 'mbwAP01', 'MBW-SEED-01', 'SYSTEM', 'SYSTEM'
FROM MBW_ADMIN_USER
WHERE admin_login_id = 'mbw-admin'
  AND NOT EXISTS (
      SELECT 1
      FROM MBW_LOGIN_HISTORY
      WHERE admin_login_id = 'mbw-admin'
        AND transaction_id = '20260715120000000MBWmbwAP010000001'
  );

MERGE INTO MBW_MENU tgt
USING (SELECT 'DASHBOARD' AS menu_code, '업무 대시보드' AS menu_name, 'MBW' AS module_code, '/backoffice' AS route_path, '/api/v1/backoffice/dashboard' AS api_path, 10 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'USER' AS menu_code, '백오피스 사용자' AS menu_name, 'MBW' AS module_code, '/backoffice#users' AS route_path, '/api/v1/backoffice/admin-users' AS api_path, 20 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'ORGANIZATION' AS menu_code, '조직 관리' AS menu_name, 'MBW' AS module_code, '/backoffice#organizations' AS route_path, '/api/v1/backoffice/organizations' AS api_path, 30 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'EMPLOYEE' AS menu_code, '직원 관리' AS menu_name, 'MBW' AS module_code, '/backoffice#employees' AS route_path, '/api/v1/backoffice/employees' AS api_path, 40 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'ROLE' AS menu_code, '역할 관리' AS menu_name, 'MBW' AS module_code, '/backoffice#roles' AS route_path, '/api/v1/backoffice/roles' AS api_path, 50 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MENU' AS menu_code, '메뉴 관리' AS menu_name, 'MBW' AS module_code, '/backoffice#menus' AS route_path, '/api/v1/backoffice/menus' AS api_path, 60 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'PERMISSION' AS menu_code, '권한 관리' AS menu_name, 'MBW' AS module_code, '/backoffice#permissions' AS route_path, '/api/v1/backoffice/permissions' AS api_path, 70 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'APPROVAL' AS menu_code, '결재 관리' AS menu_name, 'MBW' AS module_code, '/backoffice#approvals' AS route_path, '/api/v1/backoffice/approvals' AS api_path, 80 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'SETTING' AS menu_code, '업무 설정' AS menu_name, 'MBW' AS module_code, '/backoffice#settings' AS route_path, '/api/v1/backoffice/settings' AS api_path, 120 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'DOWNLOAD' AS menu_code, '다운로드 감사' AS menu_name, 'MBW' AS module_code, '/backoffice#downloads' AS route_path, '/api/v1/backoffice/downloads' AS api_path, 130 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'AUDIT' AS menu_code, '업무 감사' AS menu_name, 'MBW' AS module_code, '/backoffice#audits' AS route_path, '/api/v1/backoffice/audits' AS api_path, 140 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'NOTIFICATION' AS menu_code, '업무 알림' AS menu_name, 'MBW' AS module_code, '/backoffice#notifications' AS route_path, '/api/v1/backoffice/notifications' AS api_path, 150 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'ATTACHMENT' AS menu_code, '첨부파일' AS menu_name, 'MBW' AS module_code, '/backoffice#attachments' AS route_path, '/api/v1/backoffice/attachments' AS api_path, 160 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'SAVED_SEARCH' AS menu_code, '저장 검색' AS menu_name, 'MBW' AS module_code, '/backoffice#savedSearches' AS route_path, '/api/v1/backoffice/saved-searches' AS api_path, 170 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_ROLE tgt
USING (SELECT 'MBW_MANAGER' AS role_code, '업무 관리자' AS role_name, 'Y' AS write_allowed_yn, 'ALL' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code)
WHEN MATCHED THEN UPDATE SET tgt.role_name=src.role_name, tgt.write_allowed_yn=src.write_allowed_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by) VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_USER_ROLE tgt
USING (SELECT admin_user_id AS admin_user_id, 'MBW_MANAGER' AS role_code, SYSTIMESTAMP AS valid_from, NULL AS valid_to, 'Y' AS primary_yn, 'CPF_TEST_SEED' AS grant_reason, 'CPF-TEST-MBW-ROLE-MANAGER-0001' AS operation_id, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM MBW_ADMIN_USER
WHERE admin_login_id = 'mbw-admin') src
ON (tgt.operation_id=src.operation_id)
WHEN MATCHED THEN UPDATE SET tgt.valid_to=NULL, tgt.primary_yn='Y', tgt.grant_reason=src.grant_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (admin_user_id, role_code, valid_from, valid_to, primary_yn, grant_reason, operation_id, created_by, updated_by) VALUES (src.admin_user_id, src.role_code, src.valid_from, src.valid_to, src.primary_yn, src.grant_reason, src.operation_id, src.created_by, src.updated_by);

MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'DASHBOARD' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/dashboard' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'USER' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/admin-users/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'USER' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/admin-users' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'ORGANIZATION' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/organizations/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'ORGANIZATION' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/organizations' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'EMPLOYEE' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/employees/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'EMPLOYEE' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/employees' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'ROLE' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/roles/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'ROLE' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/roles' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'MENU' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/menus/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'MENU' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/menus' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'PERMISSION' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/permissions/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'PERMISSION' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/permissions/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'APPROVAL' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/approvals/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'APPROVAL' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/approvals/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'SETTING' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/settings/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'DOWNLOAD' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/downloads/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'AUDIT' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/audits/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'NOTIFICATION' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/notifications/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'NOTIFICATION' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/notifications/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'ATTACHMENT' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/attachments' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'ATTACHMENT' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/attachments' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'ATTACHMENT' AS menu_code, 'DOWNLOAD' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/attachments/*/download' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'SAVED_SEARCH' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/saved-searches/**' AS api_pattern, 'OWN' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'MBW_MANAGER' AS role_code, 'SAVED_SEARCH' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/saved-searches/**' AS api_pattern, 'OWN' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);

MERGE INTO MBW_PROJECT_SETTING tgt
USING (SELECT 'DOWNLOAD.MASKING.ENABLED' AS setting_key, 'Y' AS setting_value, '업무 다운로드 마스킹 사용 여부' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.setting_key=src.setting_key)
WHEN MATCHED THEN UPDATE SET tgt.setting_value=src.setting_value, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_ORGANIZATION tgt
USING (SELECT 'HQ' AS organization_code, NULL AS parent_organization_code, '본사' AS organization_name, 'COMPANY' AS organization_type, 10 AS sort_order, SYSTIMESTAMP AS effective_from, NULL AS effective_to, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'OPS' AS organization_code, 'HQ' AS parent_organization_code, '업무운영팀' AS organization_name, 'DEPARTMENT' AS organization_type, 20 AS sort_order, SYSTIMESTAMP AS effective_from, NULL AS effective_to, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.organization_code=src.organization_code)
WHEN MATCHED THEN UPDATE SET tgt.parent_organization_code=src.parent_organization_code, tgt.organization_name=src.organization_name, tgt.organization_type=src.organization_type, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by) VALUES (src.organization_code, src.parent_organization_code, src.organization_name, src.organization_type, src.sort_order, src.effective_from, src.effective_to, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_POSITION tgt
USING (SELECT 'P3' AS position_code, '책임' AS position_name, 30 AS rank_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.position_code=src.position_code)
WHEN MATCHED THEN UPDATE SET tgt.position_name=src.position_name, tgt.rank_order=src.rank_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (position_code, position_name, rank_order, use_yn, created_by, updated_by) VALUES (src.position_code, src.position_name, src.rank_order, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_JOB_TITLE tgt
USING (SELECT 'OPERATOR' AS job_title_code, '업무담당자' AS job_title_name, 'N' AS manager_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.job_title_code=src.job_title_code)
WHEN MATCHED THEN UPDATE SET tgt.job_title_name=src.job_title_name, tgt.manager_yn=src.manager_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by) VALUES (src.job_title_code, src.job_title_name, src.manager_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_EMPLOYEE tgt
USING (SELECT 'EMP001' AS employee_no, admin_user_id AS admin_user_id, 'OPS' AS organization_code, '업무 담당자' AS employee_name, 'P3' AS position_code, 'OPERATOR' AS job_title_code, 'ACTIVE' AS employment_status, CURRENT_DATE AS join_date, 'operator@example.com' AS email, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM MBW_ADMIN_USER WHERE admin_login_id = 'mbw-admin') src
ON (tgt.admin_user_id=src.admin_user_id)
WHEN MATCHED THEN UPDATE SET tgt.admin_user_id=src.admin_user_id, tgt.organization_code=src.organization_code, tgt.employee_name=src.employee_name, tgt.position_code=src.position_code, tgt.job_title_code=src.job_title_code, tgt.employment_status=src.employment_status, tgt.email=src.email, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, employment_status, join_date, email, use_yn, created_by, updated_by) VALUES (src.employee_no, src.admin_user_id, src.organization_code, src.employee_name, src.position_code, src.job_title_code, src.employment_status, src.join_date, src.email, src.use_yn, src.created_by, src.updated_by);

MERGE INTO MBW_EMPLOYEE_ASSIGNMENT tgt
USING (SELECT 'EMP001' AS employee_no, 'OPS' AS organization_code, 'P3' AS position_code, 'OPERATOR' AS job_title_code, 'PRIMARY' AS assignment_type, 'Y' AS primary_yn, SYSTIMESTAMP AS effective_from, NULL AS effective_to, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.employee_no=src.employee_no AND tgt.assignment_type=src.assignment_type AND tgt.primary_yn=src.primary_yn)
WHEN MATCHED THEN UPDATE SET tgt.organization_code=src.organization_code, tgt.position_code=src.position_code, tgt.job_title_code=src.job_title_code, tgt.primary_yn=src.primary_yn, tgt.effective_to=NULL, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by) VALUES (src.employee_no, src.organization_code, src.position_code, src.job_title_code, src.assignment_type, src.primary_yn, src.effective_from, src.effective_to, src.created_by, src.updated_by);

INSERT INTO MBW_NOTIFICATION (recipient_login_id, notification_type, title, message_body, reference_type, reference_id, read_yn, use_yn, created_by, updated_by)
SELECT 'mbw-admin', 'APPROVAL', '결재 대기 알림', '기준정보 변경 요청 결재를 확인하세요.',
       'APPROVAL', 'MBW-SAMPLE-001', 'N', 'Y', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM MBW_NOTIFICATION
     WHERE recipient_login_id = 'mbw-admin'
       AND reference_type = 'APPROVAL'
       AND reference_id = 'MBW-SAMPLE-001'
);

MERGE INTO MBW_SAVED_SEARCH tgt
USING (SELECT 'mbw-admin' AS owner_login_id, 'APPROVAL' AS screen_code, '진행 중 결재' AS search_name, '{"approvalStatus":"IN_REVIEW"}' AS criteria_json, 'N' AS shared_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.owner_login_id=src.owner_login_id AND tgt.screen_code=src.screen_code AND tgt.search_name=src.search_name)
WHEN MATCHED THEN UPDATE SET tgt.criteria_json=src.criteria_json, tgt.shared_yn=src.shared_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (owner_login_id, screen_code, search_name, criteria_json, shared_yn, use_yn, created_by, updated_by) VALUES (src.owner_login_id, src.screen_code, src.search_name, src.criteria_json, src.shared_yn, src.use_yn, src.created_by, src.updated_by);
