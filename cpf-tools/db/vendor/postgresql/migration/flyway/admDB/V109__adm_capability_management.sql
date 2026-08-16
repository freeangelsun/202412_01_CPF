-- CPF Capability Fleet common management surface
INSERT INTO adm_menu (menu_id,parent_menu_id,menu_name,menu_path,sort_order,use_yn,created_by,updated_by)
VALUES ('CAPABILITY_FLEET',NULL,'CPF Capability','/adm#capabilities',15,'Y','SYSTEM','SYSTEM')
ON CONFLICT (menu_id) DO UPDATE SET menu_name=EXCLUDED.menu_name,menu_path=EXCLUDED.menu_path,sort_order=EXCLUDED.sort_order,use_yn=EXCLUDED.use_yn,updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_button (button_id,menu_id,action_code,button_name,http_method,api_pattern,sort_order,use_yn,created_by,updated_by)
VALUES ('CAPABILITY_FLEET_READ','CAPABILITY_FLEET','READ','CPF Capability 조회','GET','/adm/api/capability-management/**',10,'Y','SYSTEM','SYSTEM')
ON CONFLICT (button_id) DO UPDATE SET menu_id=EXCLUDED.menu_id,action_code=EXCLUDED.action_code,button_name=EXCLUDED.button_name,http_method=EXCLUDED.http_method,api_pattern=EXCLUDED.api_pattern,sort_order=EXCLUDED.sort_order,use_yn=EXCLUDED.use_yn,updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (role_id,menu_id,read_yn,write_yn,delete_yn,created_by,updated_by)
SELECT role_id,'CAPABILITY_FLEET','Y','N','N','SYSTEM','SYSTEM' FROM adm_role
WHERE role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_VIEWER')
ON CONFLICT (role_id,menu_id) DO UPDATE SET read_yn='Y',write_yn='N',delete_yn='N',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_api_permission (api_permission_id,api_group_code,http_method,api_path,api_name,permission_code,menu_id,button_id,use_yn,created_by,updated_by)
VALUES ('API_CAPABILITY_FLEET_READ','CAPABILITY_FLEET','GET','/adm/api/capability-management/**','CPF Capability 조회','READ','CAPABILITY_FLEET','CAPABILITY_FLEET_READ','Y','SYSTEM','SYSTEM')
ON CONFLICT (api_permission_id) DO UPDATE SET api_group_code=EXCLUDED.api_group_code,http_method=EXCLUDED.http_method,api_path=EXCLUDED.api_path,api_name=EXCLUDED.api_name,permission_code=EXCLUDED.permission_code,menu_id=EXCLUDED.menu_id,button_id=EXCLUDED.button_id,use_yn=EXCLUDED.use_yn,updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (role_id,button_id,allow_yn,created_by,updated_by)
SELECT role_id,'CAPABILITY_FLEET_READ','Y','SYSTEM','SYSTEM' FROM adm_role
WHERE role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_VIEWER')
ON CONFLICT (role_id,button_id) DO UPDATE SET allow_yn='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_role_api_permission (role_id,api_permission_id,allow_yn,created_by,updated_by)
SELECT role_id,'API_CAPABILITY_FLEET_READ','Y','SYSTEM','SYSTEM' FROM adm_role
WHERE role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_VIEWER')
ON CONFLICT (role_id,api_permission_id) DO UPDATE SET allow_yn='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;
