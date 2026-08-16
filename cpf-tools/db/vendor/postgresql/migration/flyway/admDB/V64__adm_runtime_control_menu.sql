-- Runtime Change Center 메뉴·권한
INSERT INTO adm_menu (menu_id,parent_menu_id,menu_name,menu_path,sort_order,use_yn,created_by,updated_by)
VALUES ('RUNTIME_CONTROL',NULL,'Runtime Change Center','/adm#runtimeControl',78,'Y','SYSTEM','SYSTEM')
ON CONFLICT (menu_id) DO UPDATE SET menu_name=EXCLUDED.menu_name,menu_path=EXCLUDED.menu_path,sort_order=EXCLUDED.sort_order,use_yn=EXCLUDED.use_yn,updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_button (button_id,menu_id,action_code,button_name,http_method,api_pattern,sort_order,use_yn,created_by,updated_by)
VALUES
 ('RUNTIME_CONTROL_READ','RUNTIME_CONTROL','READ','Runtime 상태 조회','GET','/adm/api/runtime-control/**',10,'Y','SYSTEM','SYSTEM'),
 ('RUNTIME_CONTROL_PREVIEW','RUNTIME_CONTROL','PREVIEW','Runtime 변경 Preview','POST','/adm/api/runtime-control/preview-*',20,'Y','SYSTEM','SYSTEM'),
 ('RUNTIME_CONTROL_WRITE','RUNTIME_CONTROL','WRITE','Runtime 변경 생성','POST','/adm/api/runtime-control/changes',30,'Y','SYSTEM','SYSTEM'),
 ('RUNTIME_CONTROL_CONTROL','RUNTIME_CONTROL','CONTROL','Runtime 취소·Rollback','POST','/adm/api/runtime-control/changes/*/**',40,'Y','SYSTEM','SYSTEM'),
 ('RUNTIME_CONTROL_GROUP_WRITE','RUNTIME_CONTROL','GROUP_WRITE','Runtime Group 변경','POST','/adm/api/runtime-control/groups/**',50,'Y','SYSTEM','SYSTEM'),
 ('RUNTIME_CONTROL_GROUP_DELETE','RUNTIME_CONTROL','GROUP_DELETE','Runtime Group 삭제','DELETE','/adm/api/runtime-control/groups/*',60,'Y','SYSTEM','SYSTEM')
ON CONFLICT (button_id) DO UPDATE SET menu_id=EXCLUDED.menu_id,action_code=EXCLUDED.action_code,button_name=EXCLUDED.button_name,http_method=EXCLUDED.http_method,api_pattern=EXCLUDED.api_pattern,sort_order=EXCLUDED.sort_order,use_yn=EXCLUDED.use_yn,updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (role_id,menu_id,read_yn,write_yn,delete_yn,created_by,updated_by)
SELECT role_id,'RUNTIME_CONTROL','Y',CASE WHEN role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR') THEN 'Y' ELSE 'N' END,
       CASE WHEN role_id='ADM_ADMIN' THEN 'Y' ELSE 'N' END,'SYSTEM','SYSTEM'
FROM adm_role WHERE role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR')
ON CONFLICT (role_id,menu_id) DO UPDATE SET read_yn=EXCLUDED.read_yn,write_yn=EXCLUDED.write_yn,delete_yn=EXCLUDED.delete_yn,updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_api_permission (api_permission_id,api_group_code,http_method,api_path,api_name,permission_code,menu_id,button_id,use_yn,created_by,updated_by)
VALUES
 ('API_RUNTIME_CONTROL_READ','RUNTIME_CONTROL','GET','/adm/api/runtime-control/**','Runtime Control 조회','READ','RUNTIME_CONTROL','RUNTIME_CONTROL_READ','Y','SYSTEM','SYSTEM'),
 ('API_RUNTIME_CONTROL_PREVIEW','RUNTIME_CONTROL','POST','/adm/api/runtime-control/preview-*','Runtime Change Preview','PREVIEW','RUNTIME_CONTROL','RUNTIME_CONTROL_PREVIEW','Y','SYSTEM','SYSTEM'),
 ('API_RUNTIME_CONTROL_WRITE','RUNTIME_CONTROL','POST','/adm/api/runtime-control/changes','Runtime Change 생성','WRITE','RUNTIME_CONTROL','RUNTIME_CONTROL_WRITE','Y','SYSTEM','SYSTEM'),
 ('API_RUNTIME_CONTROL_CANCEL','RUNTIME_CONTROL','POST','/adm/api/runtime-control/changes/*/cancel','Runtime Change 취소','CONTROL','RUNTIME_CONTROL','RUNTIME_CONTROL_CONTROL','Y','SYSTEM','SYSTEM'),
 ('API_RUNTIME_CONTROL_ROLLBACK','RUNTIME_CONTROL','POST','/adm/api/runtime-control/changes/*/rollback','Runtime Change Rollback','CONTROL','RUNTIME_CONTROL','RUNTIME_CONTROL_CONTROL','Y','SYSTEM','SYSTEM'),
 ('API_RUNTIME_CONTROL_GROUP_WRITE','RUNTIME_CONTROL','POST','/adm/api/runtime-control/groups/**','Runtime Group 변경','GROUP_WRITE','RUNTIME_CONTROL','RUNTIME_CONTROL_GROUP_WRITE','Y','SYSTEM','SYSTEM'),
 ('API_RUNTIME_CONTROL_GROUP_DELETE','RUNTIME_CONTROL','DELETE','/adm/api/runtime-control/groups/*','Runtime Group 삭제','GROUP_DELETE','RUNTIME_CONTROL','RUNTIME_CONTROL_GROUP_DELETE','Y','SYSTEM','SYSTEM')
ON CONFLICT (api_permission_id) DO UPDATE SET api_group_code=EXCLUDED.api_group_code,http_method=EXCLUDED.http_method,api_path=EXCLUDED.api_path,api_name=EXCLUDED.api_name,permission_code=EXCLUDED.permission_code,menu_id=EXCLUDED.menu_id,button_id=EXCLUDED.button_id,use_yn=EXCLUDED.use_yn,updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (role_id,button_id,allow_yn,created_by,updated_by)
SELECT r.role_id,b.button_id,CASE WHEN b.action_code='READ' THEN 'Y' WHEN b.action_code='GROUP_DELETE' AND r.role_id='ADM_ADMIN' THEN 'Y' WHEN b.action_code<>'GROUP_DELETE' AND r.role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR') THEN 'Y' ELSE 'N' END,'SYSTEM','SYSTEM'
FROM adm_role r JOIN adm_button b ON b.menu_id='RUNTIME_CONTROL'
WHERE r.role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR')
ON CONFLICT (role_id,button_id) DO UPDATE SET allow_yn=EXCLUDED.allow_yn,updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_role_api_permission (role_id,api_permission_id,allow_yn,created_by,updated_by)
SELECT r.role_id,p.api_permission_id,CASE WHEN p.permission_code='READ' THEN 'Y' WHEN p.permission_code='GROUP_DELETE' AND r.role_id='ADM_ADMIN' THEN 'Y' WHEN p.permission_code<>'GROUP_DELETE' AND r.role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR') THEN 'Y' ELSE 'N' END,'SYSTEM','SYSTEM'
FROM adm_role r JOIN adm_api_permission p ON p.menu_id='RUNTIME_CONTROL'
WHERE r.role_id IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR')
ON CONFLICT (role_id,api_permission_id) DO UPDATE SET allow_yn=EXCLUDED.allow_yn,updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;
