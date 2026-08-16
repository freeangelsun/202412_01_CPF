-- CPF Gateway menu/button/API permission canonical seed
-- QA30 D047: route/menu/API/permission parity

MERGE INTO adm_menu t USING (
 SELECT 'GATEWAY_DASHBOARD' menu_id,NULL,'Gateway 대시보드','/adm#gateway-dashboard','300','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_SERVERS' menu_id,'GATEWAY_DASHBOARD','Gateway 연동 서버','/adm#gateway-servers','301','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_GROUPS' menu_id,'GATEWAY_DASHBOARD','Gateway 서버 그룹','/adm#gateway-groups','302','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_ROUTES' menu_id,'GATEWAY_DASHBOARD','Gateway 경로·라우팅','/adm#gateway-routes','303','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_SECURITY' menu_id,'GATEWAY_DASHBOARD','Gateway 보안·제한','/adm#gateway-security','304','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_HEALTH' menu_id,'GATEWAY_DASHBOARD','Gateway Health·연결시험','/adm#gateway-health','305','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_TRANSACTIONS' menu_id,'GATEWAY_DASHBOARD','Gateway 거래 조회','/adm#gateway-transactions','306','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_LOG_POLICY' menu_id,'GATEWAY_DASHBOARD','Gateway 로그 정책','/adm#gateway-log-policies','307','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_APPLY_STATUS' menu_id,'GATEWAY_DASHBOARD','Gateway 적용 상태·이력','/adm#gateway-apply-status','308','Y' FROM dual
) s ON (t.menu_id=s.menu_id)
WHEN MATCHED THEN UPDATE SET t.parent_menu_id=s.parent_menu_id,t.menu_name=s.menu_name,t.menu_path=s.menu_path,t.sort_order=s.sort_order,t.use_yn=s.use_yn,t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_id,parent_menu_id,menu_name,menu_path,sort_order,use_yn,created_by,updated_by) VALUES (s.menu_id,s.parent_menu_id,s.menu_name,s.menu_path,s.sort_order,s.use_yn,'SYSTEM','SYSTEM');

MERGE INTO adm_button t USING (
 SELECT 'GATEWAY_READ' button_id,'GATEWAY_DASHBOARD','READ','Gateway 운영 조회','GET','/adm/api/gateway-registry/**','10','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_GROUP_WRITE' button_id,'GATEWAY_GROUPS','WRITE','Server Group 저장','POST','/adm/api/gateway-registry/server-groups','20','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_GROUP_DELETE' button_id,'GATEWAY_GROUPS','DELETE','Server Group 폐기','DELETE','/adm/api/gateway-registry/server-groups/*','30','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_ROUTE_WRITE' button_id,'GATEWAY_ROUTES','WRITE','Gateway Binding 저장','POST','/adm/api/gateway-registry/bindings','40','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_ROUTE_STATE' button_id,'GATEWAY_ROUTES','CONTROL','Gateway Binding 상태 변경','POST','/adm/api/gateway-registry/bindings/*/state','50','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_ROUTE_DELETE' button_id,'GATEWAY_ROUTES','DELETE','Gateway Binding 폐기','DELETE','/adm/api/gateway-registry/bindings/*','60','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_CONNECTION_TEST' button_id,'GATEWAY_HEALTH','TEST','Gateway 연결시험 요청','POST','/adm/api/gateway-registry/bindings/*/connection-tests','70','Y' FROM dual UNION ALL
 SELECT 'GATEWAY_TEST_CONTROL' button_id,'GATEWAY_HEALTH','CONTROL','Gateway 연결시험 취소·재검증','POST','/adm/api/gateway-registry/connection-test-operations/*/**','80','Y' FROM dual
) s ON (t.button_id=s.button_id)
WHEN MATCHED THEN UPDATE SET t.menu_id=s.menu_id,t.action_code=s.action_code,t.button_name=s.button_name,t.http_method=s.http_method,t.api_pattern=s.api_pattern,t.sort_order=s.sort_order,t.use_yn=s.use_yn,t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id,menu_id,action_code,button_name,http_method,api_pattern,sort_order,use_yn,created_by,updated_by) VALUES (s.button_id,s.menu_id,s.action_code,s.button_name,s.http_method,s.api_pattern,s.sort_order,s.use_yn,'SYSTEM','SYSTEM');

MERGE INTO adm_role_menu t USING (
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_DASHBOARD','Y','Y','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_SERVERS','Y','Y','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_GROUPS','Y','Y','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_ROUTES','Y','Y','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_SECURITY','Y','Y','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_HEALTH','Y','Y','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_TRANSACTIONS','Y','Y','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_LOG_POLICY','Y','Y','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_APPLY_STATUS','Y','Y','Y' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_DASHBOARD','Y','Y','N' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_SERVERS','Y','Y','N' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_GROUPS','Y','Y','N' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_ROUTES','Y','Y','N' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_SECURITY','Y','Y','N' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_HEALTH','Y','Y','N' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_TRANSACTIONS','Y','Y','N' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_LOG_POLICY','Y','Y','N' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_APPLY_STATUS','Y','Y','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_DASHBOARD','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_SERVERS','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_GROUPS','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_ROUTES','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_SECURITY','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_HEALTH','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_TRANSACTIONS','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_LOG_POLICY','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_APPLY_STATUS','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_DASHBOARD','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_SERVERS','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_GROUPS','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_ROUTES','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_SECURITY','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_HEALTH','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_TRANSACTIONS','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_LOG_POLICY','Y','N','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_APPLY_STATUS','Y','N','N' FROM dual
) s ON (t.role_id=s.role_id AND t.menu_id=s.menu_id)
WHEN MATCHED THEN UPDATE SET t.read_yn=s.read_yn,t.write_yn=s.write_yn,t.delete_yn=s.delete_yn,t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id,menu_id,read_yn,write_yn,delete_yn,created_by,updated_by) VALUES (s.role_id,s.menu_id,s.read_yn,s.write_yn,s.delete_yn,'SYSTEM','SYSTEM');

MERGE INTO adm_api_permission t USING (
 SELECT 'API_GATEWAY_READ' api_permission_id,'GATEWAY','GET','/adm/api/gateway-registry/**','Gateway 운영 조회','READ','GATEWAY_DASHBOARD','GATEWAY_READ','Y' FROM dual UNION ALL
 SELECT 'API_GATEWAY_GROUP_WRITE' api_permission_id,'GATEWAY','POST','/adm/api/gateway-registry/server-groups','Server Group 저장','WRITE','GATEWAY_GROUPS','GATEWAY_GROUP_WRITE','Y' FROM dual UNION ALL
 SELECT 'API_GATEWAY_GROUP_DELETE' api_permission_id,'GATEWAY','DELETE','/adm/api/gateway-registry/server-groups/*','Server Group 폐기','DELETE','GATEWAY_GROUPS','GATEWAY_GROUP_DELETE','Y' FROM dual UNION ALL
 SELECT 'API_GATEWAY_ROUTE_WRITE' api_permission_id,'GATEWAY','POST','/adm/api/gateway-registry/bindings','Gateway Binding 저장','WRITE','GATEWAY_ROUTES','GATEWAY_ROUTE_WRITE','Y' FROM dual UNION ALL
 SELECT 'API_GATEWAY_ROUTE_STATE' api_permission_id,'GATEWAY','POST','/adm/api/gateway-registry/bindings/*/state','Gateway Binding 상태 변경','CONTROL','GATEWAY_ROUTES','GATEWAY_ROUTE_STATE','Y' FROM dual UNION ALL
 SELECT 'API_GATEWAY_ROUTE_DELETE' api_permission_id,'GATEWAY','DELETE','/adm/api/gateway-registry/bindings/*','Gateway Binding 폐기','DELETE','GATEWAY_ROUTES','GATEWAY_ROUTE_DELETE','Y' FROM dual UNION ALL
 SELECT 'API_GATEWAY_CONNECTION_TEST' api_permission_id,'GATEWAY','POST','/adm/api/gateway-registry/bindings/*/connection-tests','Gateway 연결시험 요청','TEST','GATEWAY_HEALTH','GATEWAY_CONNECTION_TEST','Y' FROM dual UNION ALL
 SELECT 'API_GATEWAY_TEST_CONTROL' api_permission_id,'GATEWAY','POST','/adm/api/gateway-registry/connection-test-operations/*/**','Gateway 연결시험 취소·재검증','CONTROL','GATEWAY_HEALTH','GATEWAY_TEST_CONTROL','Y' FROM dual
) s ON (t.api_permission_id=s.api_permission_id)
WHEN MATCHED THEN UPDATE SET t.api_group_code=s.api_group_code,t.http_method=s.http_method,t.api_path=s.api_path,t.api_name=s.api_name,t.permission_code=s.permission_code,t.menu_id=s.menu_id,t.button_id=s.button_id,t.use_yn=s.use_yn,t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id,api_group_code,http_method,api_path,api_name,permission_code,menu_id,button_id,use_yn,created_by,updated_by) VALUES (s.api_permission_id,s.api_group_code,s.http_method,s.api_path,s.api_name,s.permission_code,s.menu_id,s.button_id,s.use_yn,'SYSTEM','SYSTEM');

MERGE INTO adm_role_button t USING (
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_READ','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_GROUP_WRITE','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_GROUP_DELETE','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_ROUTE_WRITE','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_ROUTE_STATE','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_ROUTE_DELETE','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_CONNECTION_TEST','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'GATEWAY_TEST_CONTROL','Y' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_READ','Y' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_GROUP_WRITE','Y' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_GROUP_DELETE','N' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_ROUTE_WRITE','Y' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_ROUTE_STATE','Y' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_ROUTE_DELETE','N' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_CONNECTION_TEST','Y' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'GATEWAY_TEST_CONTROL','Y' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_READ','Y' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_GROUP_WRITE','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_GROUP_DELETE','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_ROUTE_WRITE','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_ROUTE_STATE','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_ROUTE_DELETE','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_CONNECTION_TEST','Y' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'GATEWAY_TEST_CONTROL','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_READ','Y' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_GROUP_WRITE','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_GROUP_DELETE','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_ROUTE_WRITE','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_ROUTE_STATE','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_ROUTE_DELETE','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_CONNECTION_TEST','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'GATEWAY_TEST_CONTROL','N' FROM dual
) s ON (t.role_id=s.role_id AND t.button_id=s.button_id)
WHEN MATCHED THEN UPDATE SET t.allow_yn=s.allow_yn,t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id,button_id,allow_yn,created_by,updated_by) VALUES (s.role_id,s.button_id,s.allow_yn,'SYSTEM','SYSTEM');

MERGE INTO adm_role_api_permission t USING (
 SELECT 'ADM_ADMIN' role_id,'API_GATEWAY_READ','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'API_GATEWAY_GROUP_WRITE','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'API_GATEWAY_GROUP_DELETE','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'API_GATEWAY_ROUTE_WRITE','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'API_GATEWAY_ROUTE_STATE','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'API_GATEWAY_ROUTE_DELETE','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'API_GATEWAY_CONNECTION_TEST','Y' FROM dual UNION ALL
 SELECT 'ADM_ADMIN' role_id,'API_GATEWAY_TEST_CONTROL','Y' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'API_GATEWAY_READ','Y' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'API_GATEWAY_GROUP_WRITE','Y' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'API_GATEWAY_GROUP_DELETE','N' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'API_GATEWAY_ROUTE_WRITE','Y' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'API_GATEWAY_ROUTE_STATE','Y' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'API_GATEWAY_ROUTE_DELETE','N' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'API_GATEWAY_CONNECTION_TEST','Y' FROM dual UNION ALL
 SELECT 'ADM_DEV_OPERATOR' role_id,'API_GATEWAY_TEST_CONTROL','Y' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'API_GATEWAY_READ','Y' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'API_GATEWAY_GROUP_WRITE','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'API_GATEWAY_GROUP_DELETE','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'API_GATEWAY_ROUTE_WRITE','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'API_GATEWAY_ROUTE_STATE','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'API_GATEWAY_ROUTE_DELETE','N' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'API_GATEWAY_CONNECTION_TEST','Y' FROM dual UNION ALL
 SELECT 'ADM_OPERATOR' role_id,'API_GATEWAY_TEST_CONTROL','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'API_GATEWAY_READ','Y' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'API_GATEWAY_GROUP_WRITE','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'API_GATEWAY_GROUP_DELETE','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'API_GATEWAY_ROUTE_WRITE','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'API_GATEWAY_ROUTE_STATE','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'API_GATEWAY_ROUTE_DELETE','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'API_GATEWAY_CONNECTION_TEST','N' FROM dual UNION ALL
 SELECT 'ADM_VIEWER' role_id,'API_GATEWAY_TEST_CONTROL','N' FROM dual
) s ON (t.role_id=s.role_id AND t.api_permission_id=s.api_permission_id)
WHEN MATCHED THEN UPDATE SET t.allow_yn=s.allow_yn,t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id,api_permission_id,allow_yn,created_by,updated_by) VALUES (s.role_id,s.api_permission_id,s.allow_yn,'SYSTEM','SYSTEM');
