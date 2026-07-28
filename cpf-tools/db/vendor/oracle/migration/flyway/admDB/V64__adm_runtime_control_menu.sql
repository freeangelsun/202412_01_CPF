-- Runtime Change Center 메뉴·권한
MERGE INTO adm_menu t USING (
 SELECT 'RUNTIME_CONTROL' menu_id, CAST(NULL AS VARCHAR2(100)) parent_menu_id, 'Runtime Change Center' menu_name,
        '/adm#runtimeControl' menu_path, 78 sort_order, 'Y' use_yn FROM dual
) s ON (t.menu_id=s.menu_id)
WHEN MATCHED THEN UPDATE SET t.menu_name=s.menu_name,t.menu_path=s.menu_path,t.sort_order=s.sort_order,t.use_yn=s.use_yn,t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_id,parent_menu_id,menu_name,menu_path,sort_order,use_yn,created_by,updated_by)
VALUES (s.menu_id,s.parent_menu_id,s.menu_name,s.menu_path,s.sort_order,s.use_yn,'SYSTEM','SYSTEM');

MERGE INTO adm_button t USING (
 SELECT 'RUNTIME_CONTROL_READ' button_id,'RUNTIME_CONTROL' menu_id,'READ' action_code,'Runtime 상태 조회' button_name,'GET' http_method,'/adm/api/runtime-control/**' api_pattern,10 sort_order FROM dual UNION ALL
 SELECT 'RUNTIME_CONTROL_PREVIEW','RUNTIME_CONTROL','PREVIEW','Runtime 변경 Preview','POST','/adm/api/runtime-control/preview-*',20 FROM dual UNION ALL
 SELECT 'RUNTIME_CONTROL_WRITE','RUNTIME_CONTROL','WRITE','Runtime 변경 생성','POST','/adm/api/runtime-control/changes',30 FROM dual UNION ALL
 SELECT 'RUNTIME_CONTROL_CONTROL','RUNTIME_CONTROL','CONTROL','Runtime 취소·Rollback','POST','/adm/api/runtime-control/changes/*/**',40 FROM dual UNION ALL
 SELECT 'RUNTIME_CONTROL_GROUP_WRITE','RUNTIME_CONTROL','GROUP_WRITE','Runtime Group 변경','POST','/adm/api/runtime-control/groups/**',50 FROM dual UNION ALL
 SELECT 'RUNTIME_CONTROL_GROUP_DELETE','RUNTIME_CONTROL','GROUP_DELETE','Runtime Group 삭제','DELETE','/adm/api/runtime-control/groups/*',60 FROM dual
) s ON (t.button_id=s.button_id)
WHEN MATCHED THEN UPDATE SET t.menu_id=s.menu_id,t.action_code=s.action_code,t.button_name=s.button_name,t.http_method=s.http_method,t.api_pattern=s.api_pattern,t.sort_order=s.sort_order,t.use_yn='Y',t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id,menu_id,action_code,button_name,http_method,api_pattern,sort_order,use_yn,created_by,updated_by)
VALUES (s.button_id,s.menu_id,s.action_code,s.button_name,s.http_method,s.api_pattern,s.sort_order,'Y','SYSTEM','SYSTEM');

MERGE INTO adm_role_menu t USING (
 SELECT role_id,'RUNTIME_CONTROL' menu_id,'Y' read_yn,
        CASE WHEN role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR') THEN 'Y' ELSE 'N' END write_yn,
        CASE WHEN role_id='ADM_ADMIN' THEN 'Y' ELSE 'N' END delete_yn
 FROM adm_role WHERE role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR')
) s ON (t.role_id=s.role_id AND t.menu_id=s.menu_id)
WHEN MATCHED THEN UPDATE SET t.read_yn=s.read_yn,t.write_yn=s.write_yn,t.delete_yn=s.delete_yn,t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id,menu_id,read_yn,write_yn,delete_yn,created_by,updated_by)
VALUES (s.role_id,s.menu_id,s.read_yn,s.write_yn,s.delete_yn,'SYSTEM','SYSTEM');

MERGE INTO adm_api_permission t USING (
 SELECT 'API_RUNTIME_CONTROL_READ' api_permission_id,'RUNTIME_CONTROL' api_group_code,'GET' http_method,'/adm/api/runtime-control/**' api_path,'Runtime Control 조회' api_name,'READ' permission_code,'RUNTIME_CONTROL' menu_id,'RUNTIME_CONTROL_READ' button_id FROM dual UNION ALL
 SELECT 'API_RUNTIME_CONTROL_PREVIEW','RUNTIME_CONTROL','POST','/adm/api/runtime-control/preview-*','Runtime Change Preview','PREVIEW','RUNTIME_CONTROL','RUNTIME_CONTROL_PREVIEW' FROM dual UNION ALL
 SELECT 'API_RUNTIME_CONTROL_WRITE','RUNTIME_CONTROL','POST','/adm/api/runtime-control/changes','Runtime Change 생성','WRITE','RUNTIME_CONTROL','RUNTIME_CONTROL_WRITE' FROM dual UNION ALL
 SELECT 'API_RUNTIME_CONTROL_CANCEL','RUNTIME_CONTROL','POST','/adm/api/runtime-control/changes/*/cancel','Runtime Change 취소','CONTROL','RUNTIME_CONTROL','RUNTIME_CONTROL_CONTROL' FROM dual UNION ALL
 SELECT 'API_RUNTIME_CONTROL_ROLLBACK','RUNTIME_CONTROL','POST','/adm/api/runtime-control/changes/*/rollback','Runtime Change Rollback','CONTROL','RUNTIME_CONTROL','RUNTIME_CONTROL_CONTROL' FROM dual UNION ALL
 SELECT 'API_RUNTIME_CONTROL_GROUP_WRITE','RUNTIME_CONTROL','POST','/adm/api/runtime-control/groups/**','Runtime Group 변경','GROUP_WRITE','RUNTIME_CONTROL','RUNTIME_CONTROL_GROUP_WRITE' FROM dual UNION ALL
 SELECT 'API_RUNTIME_CONTROL_GROUP_DELETE','RUNTIME_CONTROL','DELETE','/adm/api/runtime-control/groups/*','Runtime Group 삭제','GROUP_DELETE','RUNTIME_CONTROL','RUNTIME_CONTROL_GROUP_DELETE' FROM dual
) s ON (t.api_permission_id=s.api_permission_id)
WHEN MATCHED THEN UPDATE SET t.api_group_code=s.api_group_code,t.http_method=s.http_method,t.api_path=s.api_path,t.api_name=s.api_name,t.permission_code=s.permission_code,t.menu_id=s.menu_id,t.button_id=s.button_id,t.use_yn='Y',t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id,api_group_code,http_method,api_path,api_name,permission_code,menu_id,button_id,use_yn,created_by,updated_by)
VALUES (s.api_permission_id,s.api_group_code,s.http_method,s.api_path,s.api_name,s.permission_code,s.menu_id,s.button_id,'Y','SYSTEM','SYSTEM');

MERGE INTO adm_role_button t USING (
 SELECT r.role_id,b.button_id,CASE WHEN b.action_code='READ' THEN 'Y' WHEN b.action_code='GROUP_DELETE' AND r.role_id='ADM_ADMIN' THEN 'Y' WHEN b.action_code<>'GROUP_DELETE' AND r.role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR') THEN 'Y' ELSE 'N' END allow_yn
 FROM adm_role r JOIN adm_button b ON b.menu_id='RUNTIME_CONTROL'
 WHERE r.role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR')
) s ON (t.role_id=s.role_id AND t.button_id=s.button_id)
WHEN MATCHED THEN UPDATE SET t.allow_yn=s.allow_yn,t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id,button_id,allow_yn,created_by,updated_by) VALUES (s.role_id,s.button_id,s.allow_yn,'SYSTEM','SYSTEM');

MERGE INTO adm_role_api_permission t USING (
 SELECT r.role_id,p.api_permission_id,CASE WHEN p.permission_code='READ' THEN 'Y' WHEN p.permission_code='GROUP_DELETE' AND r.role_id='ADM_ADMIN' THEN 'Y' WHEN p.permission_code<>'GROUP_DELETE' AND r.role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR') THEN 'Y' ELSE 'N' END allow_yn
 FROM adm_role r JOIN adm_api_permission p ON p.menu_id='RUNTIME_CONTROL'
 WHERE r.role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR')
) s ON (t.role_id=s.role_id AND t.api_permission_id=s.api_permission_id)
WHEN MATCHED THEN UPDATE SET t.allow_yn=s.allow_yn,t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id,api_permission_id,allow_yn,created_by,updated_by) VALUES (s.role_id,s.api_permission_id,s.allow_yn,'SYSTEM','SYSTEM');
