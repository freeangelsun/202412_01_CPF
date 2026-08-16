DELETE FROM adm_role_api_permission WHERE api_permission_id LIKE 'API_RUNTIME_CONTROL_%';
DELETE FROM adm_role_button WHERE button_id LIKE 'RUNTIME_CONTROL_%';
DELETE FROM adm_api_permission WHERE api_permission_id LIKE 'API_RUNTIME_CONTROL_%';
DELETE FROM adm_role_menu WHERE menu_id='RUNTIME_CONTROL';
DELETE FROM adm_button WHERE menu_id='RUNTIME_CONTROL';
DELETE FROM adm_menu WHERE menu_id='RUNTIME_CONTROL';
