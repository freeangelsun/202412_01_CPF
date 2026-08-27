-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=61_adm_gateway_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_READ' AS button_id, 'GATEWAY_DASHBOARD' AS menu_id, 'READ' AS action_code, 'Gateway 운영 조회' AS button_name, 'GET' AS http_method, '/adm/api/gateway-registry/**' AS api_pattern, 10 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_GROUP_WRITE' AS button_id, 'GATEWAY_GROUPS' AS menu_id, 'WRITE' AS action_code, 'Server Group 저장' AS button_name, 'POST' AS http_method, '/adm/api/gateway-registry/server-groups' AS api_pattern, 20 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_GROUP_DELETE' AS button_id, 'GATEWAY_GROUPS' AS menu_id, 'DELETE' AS action_code, 'Server Group 폐기' AS button_name, 'DELETE' AS http_method, '/adm/api/gateway-registry/server-groups/*' AS api_pattern, 30 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_ROUTE_WRITE' AS button_id, 'GATEWAY_ROUTES' AS menu_id, 'WRITE' AS action_code, 'Gateway Binding 저장' AS button_name, 'POST' AS http_method, '/adm/api/gateway-registry/bindings' AS api_pattern, 40 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_ROUTE_STATE' AS button_id, 'GATEWAY_ROUTES' AS menu_id, 'CONTROL' AS action_code, 'Gateway Binding 상태 변경' AS button_name, 'POST' AS http_method, '/adm/api/gateway-registry/bindings/*/state' AS api_pattern, 50 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_ROUTE_DELETE' AS button_id, 'GATEWAY_ROUTES' AS menu_id, 'DELETE' AS action_code, 'Gateway Binding 폐기' AS button_name, 'DELETE' AS http_method, '/adm/api/gateway-registry/bindings/*' AS api_pattern, 60 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_CONNECTION_TEST' AS button_id, 'GATEWAY_HEALTH' AS menu_id, 'TEST' AS action_code, 'Gateway 연결시험 요청' AS button_name, 'POST' AS http_method, '/adm/api/gateway-registry/bindings/*/connection-tests' AS api_pattern, 70 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_TEST_CONTROL' AS button_id, 'GATEWAY_HEALTH' AS menu_id, 'CONTROL' AS action_code, 'Gateway 연결시험 취소·재검증' AS button_name, 'POST' AS http_method, '/adm/api/gateway-registry/connection-test-operations/*/**' AS api_pattern, 80 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_DASHBOARD' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_SERVERS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_GROUPS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_ROUTES' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_SECURITY' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_HEALTH' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_TRANSACTIONS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_LOG_POLICY' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_APPLY_STATUS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_DASHBOARD' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_SERVERS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_GROUPS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_ROUTES' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_SECURITY' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_HEALTH' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_TRANSACTIONS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_LOG_POLICY' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_APPLY_STATUS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_DASHBOARD' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_SERVERS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_GROUPS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_ROUTES' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_SECURITY' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_HEALTH' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_TRANSACTIONS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_LOG_POLICY' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_APPLY_STATUS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_DASHBOARD' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_SERVERS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_GROUPS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_ROUTES' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_SECURITY' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_HEALTH' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_TRANSACTIONS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_LOG_POLICY' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_APPLY_STATUS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_READ' AS api_permission_id, 'GATEWAY' AS api_group_code, 'GET' AS http_method, '/adm/api/gateway-registry/**' AS api_path, 'Gateway 운영 조회' AS api_name, 'READ' AS permission_code, 'GATEWAY_DASHBOARD' AS menu_id, 'GATEWAY_READ' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_GROUP_WRITE' AS api_permission_id, 'GATEWAY' AS api_group_code, 'POST' AS http_method, '/adm/api/gateway-registry/server-groups' AS api_path, 'Server Group 저장' AS api_name, 'WRITE' AS permission_code, 'GATEWAY_GROUPS' AS menu_id, 'GATEWAY_GROUP_WRITE' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_GROUP_DELETE' AS api_permission_id, 'GATEWAY' AS api_group_code, 'DELETE' AS http_method, '/adm/api/gateway-registry/server-groups/*' AS api_path, 'Server Group 폐기' AS api_name, 'DELETE' AS permission_code, 'GATEWAY_GROUPS' AS menu_id, 'GATEWAY_GROUP_DELETE' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_ROUTE_WRITE' AS api_permission_id, 'GATEWAY' AS api_group_code, 'POST' AS http_method, '/adm/api/gateway-registry/bindings' AS api_path, 'Gateway Binding 저장' AS api_name, 'WRITE' AS permission_code, 'GATEWAY_ROUTES' AS menu_id, 'GATEWAY_ROUTE_WRITE' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_ROUTE_STATE' AS api_permission_id, 'GATEWAY' AS api_group_code, 'POST' AS http_method, '/adm/api/gateway-registry/bindings/*/state' AS api_path, 'Gateway Binding 상태 변경' AS api_name, 'CONTROL' AS permission_code, 'GATEWAY_ROUTES' AS menu_id, 'GATEWAY_ROUTE_STATE' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_ROUTE_DELETE' AS api_permission_id, 'GATEWAY' AS api_group_code, 'DELETE' AS http_method, '/adm/api/gateway-registry/bindings/*' AS api_path, 'Gateway Binding 폐기' AS api_name, 'DELETE' AS permission_code, 'GATEWAY_ROUTES' AS menu_id, 'GATEWAY_ROUTE_DELETE' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_CONNECTION_TEST' AS api_permission_id, 'GATEWAY' AS api_group_code, 'POST' AS http_method, '/adm/api/gateway-registry/bindings/*/connection-tests' AS api_path, 'Gateway 연결시험 요청' AS api_name, 'TEST' AS permission_code, 'GATEWAY_HEALTH' AS menu_id, 'GATEWAY_CONNECTION_TEST' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_TEST_CONTROL' AS api_permission_id, 'GATEWAY' AS api_group_code, 'POST' AS http_method, '/adm/api/gateway-registry/connection-test-operations/*/**' AS api_path, 'Gateway 연결시험 취소·재검증' AS api_name, 'CONTROL' AS permission_code, 'GATEWAY_HEALTH' AS menu_id, 'GATEWAY_TEST_CONTROL' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_READ' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_GROUP_WRITE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_GROUP_DELETE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_ROUTE_WRITE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_ROUTE_STATE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_ROUTE_DELETE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_CONNECTION_TEST' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_TEST_CONTROL' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_READ' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_GROUP_WRITE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_GROUP_DELETE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_ROUTE_WRITE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_ROUTE_STATE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_ROUTE_DELETE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_CONNECTION_TEST' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_TEST_CONTROL' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_READ' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_GROUP_WRITE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_GROUP_DELETE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_ROUTE_WRITE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_ROUTE_STATE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_ROUTE_DELETE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_CONNECTION_TEST' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_TEST_CONTROL' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_READ' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_GROUP_WRITE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_GROUP_DELETE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_ROUTE_WRITE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_ROUTE_STATE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_ROUTE_DELETE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_CONNECTION_TEST' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_TEST_CONTROL' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_READ' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_GROUP_WRITE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_GROUP_DELETE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_ROUTE_WRITE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_ROUTE_STATE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_ROUTE_DELETE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_CONNECTION_TEST' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_TEST_CONTROL' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_READ' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_GROUP_WRITE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_GROUP_DELETE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_ROUTE_WRITE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_ROUTE_STATE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_ROUTE_DELETE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_CONNECTION_TEST' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_TEST_CONTROL' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_READ' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_GROUP_WRITE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_GROUP_DELETE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_ROUTE_WRITE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_ROUTE_STATE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_ROUTE_DELETE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_CONNECTION_TEST' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_TEST_CONTROL' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_READ' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_GROUP_WRITE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_GROUP_DELETE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_ROUTE_WRITE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_ROUTE_STATE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_ROUTE_DELETE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_CONNECTION_TEST' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_TEST_CONTROL' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
