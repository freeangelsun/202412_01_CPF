-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=56_bza_product_seed.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=bzaDB
-- CPF_USE_LOGICAL_DATABASE=bzaDB
MERGE INTO bza_role tgt
USING (
  SELECT 'BZA_ADMIN' AS role_code, '업무 관리자' AS role_name, 'Y' AS write_allowed_yn, 'ALL' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_OPERATOR' AS role_code, '업무 운영자' AS role_name, 'Y' AS write_allowed_yn, 'ORGANIZATION' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_APPROVER' AS role_code, '업무 결재자' AS role_name, 'Y' AS write_allowed_yn, 'ORGANIZATION' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_VIEWER' AS role_code, '업무 조회자' AS role_name, 'N' AS write_allowed_yn, 'ORGANIZATION' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.role_code = src.role_code)
WHEN MATCHED THEN UPDATE SET
  tgt.role_name = src.role_name,
  tgt.write_allowed_yn = src.write_allowed_yn,
  tgt.data_scope = src.data_scope,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by)
VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_menu tgt
USING (
  SELECT 'BZA_DASHBOARD' AS menu_code, '업무 관리자 대시보드' AS menu_name, NULL AS parent_menu_code, 'BZA' AS module_code, '/bza' AS route_path, 'dashboard' AS icon_code, 'ALL' AS environment_code, '/api/bza/dashboard' AS api_path, 10 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_ORGANIZATION' AS menu_code, '조직 관리' AS menu_name, NULL AS parent_menu_code, 'BZA' AS module_code, '/bza/organizations' AS route_path, 'organization' AS icon_code, 'ALL' AS environment_code, '/api/bza/organizations' AS api_path, 20 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_EMPLOYEE' AS menu_code, '직원·소속 관리' AS menu_name, NULL AS parent_menu_code, 'BZA' AS module_code, '/bza/employees' AS route_path, 'employee' AS icon_code, 'ALL' AS environment_code, '/api/bza/employees' AS api_path, 30 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_AUTHORIZATION' AS menu_code, '업무 권한 관리' AS menu_name, NULL AS parent_menu_code, 'BZA' AS module_code, '/bza/authorization' AS route_path, 'shield' AS icon_code, 'ALL' AS environment_code, '/api/bza/authorization' AS api_path, 40 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_APPROVAL' AS menu_code, '업무 결재 관리' AS menu_name, NULL AS parent_menu_code, 'BZA' AS module_code, '/bza/approvals' AS route_path, 'approval' AS icon_code, 'ALL' AS environment_code, '/api/bza/approvals' AS api_path, 50 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_AUDIT' AS menu_code, '업무 감사 조회' AS menu_name, NULL AS parent_menu_code, 'BZA' AS module_code, '/bza/audits' AS route_path, 'audit' AS icon_code, 'ALL' AS environment_code, '/api/bza/audits' AS api_path, 60 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_ATTACHMENT' AS menu_code, '첨부 관리' AS menu_name, NULL AS parent_menu_code, 'BZA' AS module_code, '/bza/attachments' AS route_path, 'attachment' AS icon_code, 'ALL' AS environment_code, '/api/bza/attachments' AS api_path, 70 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_SETTING' AS menu_code, '업무 관리자 설정' AS menu_name, NULL AS parent_menu_code, 'BZA' AS module_code, '/bza/settings' AS route_path, 'setting' AS icon_code, 'ALL' AS environment_code, '/api/bza/settings' AS api_path, 80 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
  tgt.updated_at = CURRENT_TIMESTAMP(3)
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
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_permission tgt
USING (
  SELECT 'BZA_OPERATOR' AS role_code, 'BZA_DASHBOARD' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/bza/dashboard/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_OPERATOR' AS role_code, 'BZA_ORGANIZATION' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/bza/organizations/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_OPERATOR' AS role_code, 'BZA_EMPLOYEE' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/bza/employees/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_APPROVER' AS role_code, 'BZA_APPROVAL' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/bza/approvals/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_APPROVER' AS role_code, 'BZA_APPROVAL' AS menu_code, 'DECIDE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/bza/approvals/*/decisions' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_VIEWER' AS role_code, 'BZA_DASHBOARD' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/bza/dashboard/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA_VIEWER' AS role_code, 'BZA_AUDIT' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/bza/audits/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.role_code = src.role_code AND tgt.menu_code = src.menu_code AND tgt.button_code = src.button_code AND tgt.permission_type = src.permission_type AND tgt.environment_code = src.environment_code)
WHEN MATCHED THEN UPDATE SET
  tgt.http_method = src.http_method,
  tgt.api_pattern = src.api_pattern,
  tgt.data_scope = src.data_scope,
  tgt.allow_yn = src.allow_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_project_setting tgt
USING (
  SELECT 'BZA.APPROVAL.SELF_APPROVAL_ALLOWED' AS setting_key, 'N' AS setting_value, '기본 자기승인 차단 정책' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA.APPROVAL.DEFAULT_DUE_HOURS' AS setting_key, '24' AS setting_value, '기본 결재 SLA 시간' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA.APPROVAL.REQUIRE_PAYLOAD_HASH' AS setting_key, 'Y' AS setting_value, '결재 대상 Payload 변조 검증용 SHA-256 사용' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA.AUDIT.HASH_CHAIN_ENABLED' AS setting_key, 'Y' AS setting_value, '업무 감사 로그 hash-chain 검증 사용' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA.ATTACHMENT.SECURITY_SCAN_REQUIRED' AS setting_key, 'Y' AS setting_value, '첨부 보안검사 완료 후 사용 허용' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BZA.ATTACHMENT.DEFAULT_RETENTION_DAYS' AS setting_key, '365' AS setting_value, '첨부 기본 보존일수' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.setting_key = src.setting_key)
WHEN MATCHED THEN UPDATE SET
  tgt.setting_value = src.setting_value,
  tgt.description = src.description,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by)
VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);
