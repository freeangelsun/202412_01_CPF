-- CPF Capability Fleet common management surface
MERGE INTO adm_menu t USING (SELECT 'CAPABILITY_FLEET' menu_id, CAST(NULL AS VARCHAR2(100)) parent_menu_id, 'CPF Capability' menu_name, '/adm#capabilities' menu_path, 15 sort_order, 'Y' use_yn FROM dual) s ON (t.menu_id=s.menu_id)
WHEN MATCHED THEN UPDATE SET t.menu_name=s.menu_name,t.menu_path=s.menu_path,t.sort_order=s.sort_order,t.use_yn=s.use_yn,t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_id,parent_menu_id,menu_name,menu_path,sort_order,use_yn,created_by,updated_by) VALUES (s.menu_id,s.parent_menu_id,s.menu_name,s.menu_path,s.sort_order,s.use_yn,'SYSTEM','SYSTEM');

MERGE INTO adm_button t USING (SELECT 'CAPABILITY_FLEET_READ' button_id,'CAPABILITY_FLEET' menu_id,'READ' action_code,'CPF Capability 조회' button_name,'GET' http_method,'/adm/api/capability-management/**' api_pattern,10 sort_order FROM dual) s ON (t.button_id=s.button_id)
WHEN MATCHED THEN UPDATE SET t.menu_id=s.menu_id,t.action_code=s.action_code,t.button_name=s.button_name,t.http_method=s.http_method,t.api_pattern=s.api_pattern,t.sort_order=s.sort_order,t.use_yn='Y',t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id,menu_id,action_code,button_name,http_method,api_pattern,sort_order,use_yn,created_by,updated_by) VALUES (s.button_id,s.menu_id,s.action_code,s.button_name,s.http_method,s.api_pattern,s.sort_order,'Y','SYSTEM','SYSTEM');

MERGE INTO adm_role_menu t USING (SELECT role_id,'CAPABILITY_FLEET' menu_id,'Y' read_yn,'N' write_yn,'N' delete_yn FROM adm_role WHERE role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_VIEWER')) s ON (t.role_id=s.role_id AND t.menu_id=s.menu_id)
WHEN MATCHED THEN UPDATE SET t.read_yn='Y',t.write_yn='N',t.delete_yn='N',t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id,menu_id,read_yn,write_yn,delete_yn,created_by,updated_by) VALUES (s.role_id,s.menu_id,s.read_yn,s.write_yn,s.delete_yn,'SYSTEM','SYSTEM');

MERGE INTO adm_api_permission t USING (SELECT 'API_CAPABILITY_FLEET_READ' api_permission_id,'CAPABILITY_FLEET' api_group_code,'GET' http_method,'/adm/api/capability-management/**' api_path,'CPF Capability 조회' api_name,'READ' permission_code,'CAPABILITY_FLEET' menu_id,'CAPABILITY_FLEET_READ' button_id FROM dual) s ON (t.api_permission_id=s.api_permission_id)
WHEN MATCHED THEN UPDATE SET t.api_group_code=s.api_group_code,t.http_method=s.http_method,t.api_path=s.api_path,t.api_name=s.api_name,t.permission_code=s.permission_code,t.menu_id=s.menu_id,t.button_id=s.button_id,t.use_yn='Y',t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id,api_group_code,http_method,api_path,api_name,permission_code,menu_id,button_id,use_yn,created_by,updated_by) VALUES (s.api_permission_id,s.api_group_code,s.http_method,s.api_path,s.api_name,s.permission_code,s.menu_id,s.button_id,'Y','SYSTEM','SYSTEM');

MERGE INTO adm_role_button t USING (SELECT role_id,'CAPABILITY_FLEET_READ' button_id,'Y' allow_yn FROM adm_role WHERE role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_VIEWER')) s ON (t.role_id=s.role_id AND t.button_id=s.button_id) WHEN MATCHED THEN UPDATE SET t.allow_yn='Y',t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP WHEN NOT MATCHED THEN INSERT (role_id,button_id,allow_yn,created_by,updated_by) VALUES (s.role_id,s.button_id,s.allow_yn,'SYSTEM','SYSTEM');

MERGE INTO adm_role_api_permission t USING (SELECT role_id,'API_CAPABILITY_FLEET_READ' api_permission_id,'Y' allow_yn FROM adm_role WHERE role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_VIEWER')) s ON (t.role_id=s.role_id AND t.api_permission_id=s.api_permission_id) WHEN MATCHED THEN UPDATE SET t.allow_yn='Y',t.updated_by='SYSTEM',t.updated_at=CURRENT_TIMESTAMP WHEN NOT MATCHED THEN INSERT (role_id,api_permission_id,allow_yn,created_by,updated_by) VALUES (s.role_id,s.api_permission_id,s.allow_yn,'SYSTEM','SYSTEM');
