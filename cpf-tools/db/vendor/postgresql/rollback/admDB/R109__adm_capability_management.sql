DELETE FROM adm_role_api_permission WHERE api_permission_id='API_CAPABILITY_FLEET_READ';
DELETE FROM adm_role_button WHERE button_id='CAPABILITY_FLEET_READ';
DELETE FROM adm_api_permission WHERE api_permission_id='API_CAPABILITY_FLEET_READ';
DELETE FROM adm_role_menu WHERE menu_id='CAPABILITY_FLEET';
DELETE FROM adm_button WHERE button_id='CAPABILITY_FLEET_READ';
DELETE FROM adm_menu WHERE menu_id='CAPABILITY_FLEET';
