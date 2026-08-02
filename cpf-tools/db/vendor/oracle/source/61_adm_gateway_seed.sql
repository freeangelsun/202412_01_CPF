-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=61_adm_gateway_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=admDB
MERGE INTO adm_menu tgt USING (
SELECT 'GATEWAY_DASHBOARD' menu_id, NULL parent_menu_id, 'Gateway 대시보드' menu_name, '/adm#gateway-dashboard' menu_path, 300 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_SERVERS' menu_id, 'GATEWAY_DASHBOARD' parent_menu_id, 'Gateway 연동 서버' menu_name, '/adm#gateway-servers' menu_path, 301 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_GROUPS' menu_id, 'GATEWAY_DASHBOARD' parent_menu_id, 'Gateway 서버 그룹' menu_name, '/adm#gateway-groups' menu_path, 302 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_ROUTES' menu_id, 'GATEWAY_DASHBOARD' parent_menu_id, 'Gateway 경로·라우팅' menu_name, '/adm#gateway-routes' menu_path, 303 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_SECURITY' menu_id, 'GATEWAY_DASHBOARD' parent_menu_id, 'Gateway 보안·제한' menu_name, '/adm#gateway-security' menu_path, 304 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_HEALTH' menu_id, 'GATEWAY_DASHBOARD' parent_menu_id, 'Gateway Health·연결시험' menu_name, '/adm#gateway-health' menu_path, 305 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_TRANSACTIONS' menu_id, 'GATEWAY_DASHBOARD' parent_menu_id, 'Gateway 거래 조회' menu_name, '/adm#gateway-transactions' menu_path, 306 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_LOG_POLICY' menu_id, 'GATEWAY_DASHBOARD' parent_menu_id, 'Gateway 로그 정책' menu_name, '/adm#gateway-log-policies' menu_path, 307 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_APPLY_STATUS' menu_id, 'GATEWAY_DASHBOARD' parent_menu_id, 'Gateway 적용 상태·이력' menu_name, '/adm#gateway-apply-status' menu_path, 308 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.menu_id = src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.parent_menu_id = src.parent_menu_id, tgt.menu_name = src.menu_name, tgt.menu_path = src.menu_path, tgt.sort_order = src.sort_order, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = src.updated_at
WHEN NOT MATCHED THEN INSERT (menu_id, parent_menu_id, menu_name, menu_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_id, src.parent_menu_id, src.menu_name, src.menu_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO adm_button tgt USING (
SELECT 'GATEWAY_READ' button_id, 'GATEWAY_DASHBOARD' menu_id, 'READ' action_code, 'Gateway 운영 조회' button_name, 'GET' http_method, '/adm/api/gateway-registry/**' api_pattern, 10 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_GROUP_WRITE' button_id, 'GATEWAY_GROUPS' menu_id, 'WRITE' action_code, 'Server Group 저장' button_name, 'POST' http_method, '/adm/api/gateway-registry/server-groups' api_pattern, 20 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_GROUP_DELETE' button_id, 'GATEWAY_GROUPS' menu_id, 'DELETE' action_code, 'Server Group 폐기' button_name, 'DELETE' http_method, '/adm/api/gateway-registry/server-groups/*' api_pattern, 30 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_ROUTE_WRITE' button_id, 'GATEWAY_ROUTES' menu_id, 'WRITE' action_code, 'Gateway Binding 저장' button_name, 'POST' http_method, '/adm/api/gateway-registry/bindings' api_pattern, 40 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_ROUTE_STATE' button_id, 'GATEWAY_ROUTES' menu_id, 'CONTROL' action_code, 'Gateway Binding 상태 변경' button_name, 'POST' http_method, '/adm/api/gateway-registry/bindings/*/state' api_pattern, 50 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_ROUTE_DELETE' button_id, 'GATEWAY_ROUTES' menu_id, 'DELETE' action_code, 'Gateway Binding 폐기' button_name, 'DELETE' http_method, '/adm/api/gateway-registry/bindings/*' api_pattern, 60 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_CONNECTION_TEST' button_id, 'GATEWAY_HEALTH' menu_id, 'TEST' action_code, 'Gateway 연결시험 요청' button_name, 'POST' http_method, '/adm/api/gateway-registry/bindings/*/connection-tests' api_pattern, 70 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'GATEWAY_TEST_CONTROL' button_id, 'GATEWAY_HEALTH' menu_id, 'CONTROL' action_code, 'Gateway 연결시험 취소·재검증' button_name, 'POST' http_method, '/adm/api/gateway-registry/connection-test-operations/*/**' api_pattern, 80 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.button_id = src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id = src.menu_id, tgt.action_code = src.action_code, tgt.button_name = src.button_name, tgt.http_method = src.http_method, tgt.api_pattern = src.api_pattern, tgt.sort_order = src.sort_order, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = src.updated_at
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO adm_role_menu tgt USING (
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_DASHBOARD' menu_id, 'Y' read_yn, 'Y' write_yn, 'Y' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_SERVERS' menu_id, 'Y' read_yn, 'Y' write_yn, 'Y' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_GROUPS' menu_id, 'Y' read_yn, 'Y' write_yn, 'Y' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_ROUTES' menu_id, 'Y' read_yn, 'Y' write_yn, 'Y' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_SECURITY' menu_id, 'Y' read_yn, 'Y' write_yn, 'Y' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_HEALTH' menu_id, 'Y' read_yn, 'Y' write_yn, 'Y' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_TRANSACTIONS' menu_id, 'Y' read_yn, 'Y' write_yn, 'Y' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_LOG_POLICY' menu_id, 'Y' read_yn, 'Y' write_yn, 'Y' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_APPLY_STATUS' menu_id, 'Y' read_yn, 'Y' write_yn, 'Y' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_DASHBOARD' menu_id, 'Y' read_yn, 'Y' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_SERVERS' menu_id, 'Y' read_yn, 'Y' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_GROUPS' menu_id, 'Y' read_yn, 'Y' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_ROUTES' menu_id, 'Y' read_yn, 'Y' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_SECURITY' menu_id, 'Y' read_yn, 'Y' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_HEALTH' menu_id, 'Y' read_yn, 'Y' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_TRANSACTIONS' menu_id, 'Y' read_yn, 'Y' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_LOG_POLICY' menu_id, 'Y' read_yn, 'Y' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_APPLY_STATUS' menu_id, 'Y' read_yn, 'Y' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_DASHBOARD' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_SERVERS' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_GROUPS' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_ROUTES' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_SECURITY' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_HEALTH' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_TRANSACTIONS' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_LOG_POLICY' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_APPLY_STATUS' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_DASHBOARD' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_SERVERS' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_GROUPS' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_ROUTES' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_SECURITY' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_HEALTH' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_TRANSACTIONS' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_LOG_POLICY' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_APPLY_STATUS' menu_id, 'Y' read_yn, 'N' write_yn, 'N' delete_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.role_id = src.role_id AND tgt.menu_id = src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn = src.read_yn, tgt.write_yn = src.write_yn, tgt.delete_yn = src.delete_yn, tgt.updated_by = src.updated_by, tgt.updated_at = src.updated_at
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO adm_api_permission tgt USING (
SELECT 'API_GATEWAY_READ' api_permission_id, 'GATEWAY' api_group_code, 'GET' http_method, '/adm/api/gateway-registry/**' api_path, 'Gateway 운영 조회' api_name, 'READ' permission_code, 'GATEWAY_DASHBOARD' menu_id, 'GATEWAY_READ' button_id, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'API_GATEWAY_GROUP_WRITE' api_permission_id, 'GATEWAY' api_group_code, 'POST' http_method, '/adm/api/gateway-registry/server-groups' api_path, 'Server Group 저장' api_name, 'WRITE' permission_code, 'GATEWAY_GROUPS' menu_id, 'GATEWAY_GROUP_WRITE' button_id, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'API_GATEWAY_GROUP_DELETE' api_permission_id, 'GATEWAY' api_group_code, 'DELETE' http_method, '/adm/api/gateway-registry/server-groups/*' api_path, 'Server Group 폐기' api_name, 'DELETE' permission_code, 'GATEWAY_GROUPS' menu_id, 'GATEWAY_GROUP_DELETE' button_id, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'API_GATEWAY_ROUTE_WRITE' api_permission_id, 'GATEWAY' api_group_code, 'POST' http_method, '/adm/api/gateway-registry/bindings' api_path, 'Gateway Binding 저장' api_name, 'WRITE' permission_code, 'GATEWAY_ROUTES' menu_id, 'GATEWAY_ROUTE_WRITE' button_id, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'API_GATEWAY_ROUTE_STATE' api_permission_id, 'GATEWAY' api_group_code, 'POST' http_method, '/adm/api/gateway-registry/bindings/*/state' api_path, 'Gateway Binding 상태 변경' api_name, 'CONTROL' permission_code, 'GATEWAY_ROUTES' menu_id, 'GATEWAY_ROUTE_STATE' button_id, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'API_GATEWAY_ROUTE_DELETE' api_permission_id, 'GATEWAY' api_group_code, 'DELETE' http_method, '/adm/api/gateway-registry/bindings/*' api_path, 'Gateway Binding 폐기' api_name, 'DELETE' permission_code, 'GATEWAY_ROUTES' menu_id, 'GATEWAY_ROUTE_DELETE' button_id, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'API_GATEWAY_CONNECTION_TEST' api_permission_id, 'GATEWAY' api_group_code, 'POST' http_method, '/adm/api/gateway-registry/bindings/*/connection-tests' api_path, 'Gateway 연결시험 요청' api_name, 'TEST' permission_code, 'GATEWAY_HEALTH' menu_id, 'GATEWAY_CONNECTION_TEST' button_id, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'API_GATEWAY_TEST_CONTROL' api_permission_id, 'GATEWAY' api_group_code, 'POST' http_method, '/adm/api/gateway-registry/connection-test-operations/*/**' api_path, 'Gateway 연결시험 취소·재검증' api_name, 'CONTROL' permission_code, 'GATEWAY_HEALTH' menu_id, 'GATEWAY_TEST_CONTROL' button_id, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.api_permission_id = src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code = src.api_group_code, tgt.http_method = src.http_method, tgt.api_path = src.api_path, tgt.api_name = src.api_name, tgt.permission_code = src.permission_code, tgt.menu_id = src.menu_id, tgt.button_id = src.button_id, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = src.updated_at
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO adm_role_button tgt USING (
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_READ' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_GROUP_WRITE' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_GROUP_DELETE' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_ROUTE_WRITE' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_ROUTE_STATE' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_ROUTE_DELETE' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_CONNECTION_TEST' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'GATEWAY_TEST_CONTROL' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_READ' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_GROUP_WRITE' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_GROUP_DELETE' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_ROUTE_WRITE' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_ROUTE_STATE' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_ROUTE_DELETE' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_CONNECTION_TEST' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'GATEWAY_TEST_CONTROL' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_READ' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_GROUP_WRITE' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_GROUP_DELETE' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_ROUTE_WRITE' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_ROUTE_STATE' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_ROUTE_DELETE' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_CONNECTION_TEST' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'GATEWAY_TEST_CONTROL' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_READ' button_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_GROUP_WRITE' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_GROUP_DELETE' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_ROUTE_WRITE' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_ROUTE_STATE' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_ROUTE_DELETE' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_CONNECTION_TEST' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'GATEWAY_TEST_CONTROL' button_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.role_id = src.role_id AND tgt.button_id = src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn = src.allow_yn, tgt.updated_by = src.updated_by, tgt.updated_at = src.updated_at
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO adm_role_api_permission tgt USING (
SELECT 'ADM_ADMIN' role_id, 'API_GATEWAY_READ' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'API_GATEWAY_GROUP_WRITE' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'API_GATEWAY_GROUP_DELETE' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'API_GATEWAY_ROUTE_WRITE' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'API_GATEWAY_ROUTE_STATE' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'API_GATEWAY_ROUTE_DELETE' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'API_GATEWAY_CONNECTION_TEST' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' role_id, 'API_GATEWAY_TEST_CONTROL' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'API_GATEWAY_READ' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'API_GATEWAY_GROUP_WRITE' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'API_GATEWAY_GROUP_DELETE' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'API_GATEWAY_ROUTE_WRITE' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'API_GATEWAY_ROUTE_STATE' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'API_GATEWAY_ROUTE_DELETE' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'API_GATEWAY_CONNECTION_TEST' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' role_id, 'API_GATEWAY_TEST_CONTROL' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'API_GATEWAY_READ' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'API_GATEWAY_GROUP_WRITE' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'API_GATEWAY_GROUP_DELETE' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'API_GATEWAY_ROUTE_WRITE' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'API_GATEWAY_ROUTE_STATE' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'API_GATEWAY_ROUTE_DELETE' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'API_GATEWAY_CONNECTION_TEST' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' role_id, 'API_GATEWAY_TEST_CONTROL' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'API_GATEWAY_READ' api_permission_id, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'API_GATEWAY_GROUP_WRITE' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'API_GATEWAY_GROUP_DELETE' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'API_GATEWAY_ROUTE_WRITE' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'API_GATEWAY_ROUTE_STATE' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'API_GATEWAY_ROUTE_DELETE' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'API_GATEWAY_CONNECTION_TEST' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' role_id, 'API_GATEWAY_TEST_CONTROL' api_permission_id, 'N' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.role_id = src.role_id AND tgt.api_permission_id = src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn = src.allow_yn, tgt.updated_by = src.updated_by, tgt.updated_at = src.updated_at
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
