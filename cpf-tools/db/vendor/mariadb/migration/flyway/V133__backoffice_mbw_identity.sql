-- CPF_LOGICAL_DATABASE=mbwDB
-- Append-only migration: legacy BZA physical tables become canonical MBW Backoffice tables.
RENAME TABLE bza_admin_user TO MBW_ADMIN_USER;
RENAME TABLE bza_approval_delegation TO MBW_APPROVAL_DELEGATION;
RENAME TABLE bza_approval_document TO MBW_APPROVAL_DOCUMENT;
RENAME TABLE bza_approval_history TO MBW_APPROVAL_HISTORY;
RENAME TABLE bza_approval_line TO MBW_APPROVAL_LINE;
RENAME TABLE bza_approval_participant TO MBW_APPROVAL_PARTICIPANT;
RENAME TABLE bza_approval_policy TO MBW_APPROVAL_POLICY;
RENAME TABLE bza_approval_policy_step TO MBW_APPROVAL_POLICY_STEP;
RENAME TABLE bza_attachment TO MBW_ATTACHMENT;
RENAME TABLE bza_audit_chain_lock TO MBW_AUDIT_CHAIN_LOCK;
RENAME TABLE bza_bootstrap_approval TO MBW_BOOTSTRAP_APPROVAL;
RENAME TABLE bza_business_audit TO MBW_BUSINESS_AUDIT;
RENAME TABLE bza_download_audit TO MBW_DOWNLOAD_AUDIT;
RENAME TABLE bza_employee TO MBW_EMPLOYEE;
RENAME TABLE bza_employee_assignment TO MBW_EMPLOYEE_ASSIGNMENT;
RENAME TABLE bza_job_title TO MBW_JOB_TITLE;
RENAME TABLE bza_login_history TO MBW_LOGIN_HISTORY;
RENAME TABLE bza_login_operation TO MBW_LOGIN_OPERATION;
RENAME TABLE bza_menu TO MBW_MENU;
RENAME TABLE bza_notification TO MBW_NOTIFICATION;
RENAME TABLE bza_organization TO MBW_ORGANIZATION;
RENAME TABLE bza_organization_responsibility TO MBW_ORGANIZATION_RESPONSIBILITY;
RENAME TABLE bza_permission TO MBW_PERMISSION;
RENAME TABLE bza_position TO MBW_POSITION;
RENAME TABLE bza_project_setting TO MBW_PROJECT_SETTING;
RENAME TABLE bza_refresh_token TO MBW_REFRESH_TOKEN;
RENAME TABLE bza_role TO MBW_ROLE;
RENAME TABLE bza_saved_search TO MBW_SAVED_SEARCH;
RENAME TABLE bza_user_role TO MBW_USER_ROLE;

-- Currentize login/runtime identity columns to the canonical Backoffice contract.
ALTER TABLE MBW_LOGIN_HISTORY CHANGE COLUMN module_id system_code VARCHAR(20) NULL;
ALTER TABLE MBW_LOGIN_HISTORY CHANGE COLUMN was_id application_name VARCHAR(200) NULL;
ALTER TABLE MBW_LOGIN_HISTORY CHANGE COLUMN server_instance_id instance_id VARCHAR(200) NULL;
ALTER TABLE MBW_LOGIN_HISTORY MODIFY COLUMN login_domain VARCHAR(30) NOT NULL DEFAULT 'MBW';
UPDATE MBW_LOGIN_HISTORY SET login_domain = 'MBW' WHERE login_domain = 'BZA';
UPDATE MBW_LOGIN_HISTORY SET system_code = 'MBW' WHERE system_code IS NULL OR system_code = '' OR system_code = 'BZA';
UPDATE MBW_LOGIN_HISTORY SET application_name = 'cpf-backoffice' WHERE application_name IS NULL OR application_name = '' OR application_name IN ('cpf-biz-admin', 'BZA');

-- Currentize Backoffice logical codes without losing existing user-role grants.
INSERT INTO MBW_ROLE (role_code, role_name, write_allowed_yn, data_scope, use_yn, version_no, created_by, created_at, updated_by, updated_at)
SELECT CASE role_code WHEN 'BZA_ADMIN' THEN 'MBW_ADMIN' WHEN 'BZA_OPERATOR' THEN 'MBW_OPERATOR' WHEN 'BZA_APPROVER' THEN 'MBW_APPROVER' WHEN 'BZA_VIEWER' THEN 'MBW_VIEWER' WHEN 'BZA_MANAGER' THEN 'MBW_MANAGER' ELSE role_code END, role_name, write_allowed_yn, data_scope, use_yn, version_no, created_by, created_at, updated_by, updated_at
FROM MBW_ROLE old_role
WHERE role_code IN ('BZA_ADMIN', 'BZA_OPERATOR', 'BZA_APPROVER', 'BZA_VIEWER', 'BZA_MANAGER')
  AND NOT EXISTS (SELECT 1 FROM MBW_ROLE new_role WHERE new_role.role_code = CASE role_code WHEN 'BZA_ADMIN' THEN 'MBW_ADMIN' WHEN 'BZA_OPERATOR' THEN 'MBW_OPERATOR' WHEN 'BZA_APPROVER' THEN 'MBW_APPROVER' WHEN 'BZA_VIEWER' THEN 'MBW_VIEWER' WHEN 'BZA_MANAGER' THEN 'MBW_MANAGER' ELSE role_code END);
UPDATE MBW_USER_ROLE SET role_code = CASE role_code WHEN 'BZA_ADMIN' THEN 'MBW_ADMIN' WHEN 'BZA_OPERATOR' THEN 'MBW_OPERATOR' WHEN 'BZA_APPROVER' THEN 'MBW_APPROVER' WHEN 'BZA_VIEWER' THEN 'MBW_VIEWER' WHEN 'BZA_MANAGER' THEN 'MBW_MANAGER' ELSE role_code END WHERE role_code IN ('BZA_ADMIN', 'BZA_OPERATOR', 'BZA_APPROVER', 'BZA_VIEWER', 'BZA_MANAGER');
UPDATE MBW_ADMIN_USER SET role_code = CASE role_code WHEN 'BZA_ADMIN' THEN 'MBW_ADMIN' WHEN 'BZA_OPERATOR' THEN 'MBW_OPERATOR' WHEN 'BZA_APPROVER' THEN 'MBW_APPROVER' WHEN 'BZA_VIEWER' THEN 'MBW_VIEWER' WHEN 'BZA_MANAGER' THEN 'MBW_MANAGER' ELSE role_code END WHERE role_code IN ('BZA_ADMIN', 'BZA_OPERATOR', 'BZA_APPROVER', 'BZA_VIEWER', 'BZA_MANAGER');
UPDATE MBW_PERMISSION SET role_code = CASE role_code WHEN 'BZA_ADMIN' THEN 'MBW_ADMIN' WHEN 'BZA_OPERATOR' THEN 'MBW_OPERATOR' WHEN 'BZA_APPROVER' THEN 'MBW_APPROVER' WHEN 'BZA_VIEWER' THEN 'MBW_VIEWER' WHEN 'BZA_MANAGER' THEN 'MBW_MANAGER' ELSE role_code END WHERE role_code IN ('BZA_ADMIN', 'BZA_OPERATOR', 'BZA_APPROVER', 'BZA_VIEWER', 'BZA_MANAGER');
DELETE FROM MBW_ROLE WHERE role_code IN ('BZA_ADMIN', 'BZA_OPERATOR', 'BZA_APPROVER', 'BZA_VIEWER', 'BZA_MANAGER');
UPDATE MBW_MENU SET menu_code = REPLACE(menu_code, 'BZA_', 'MBW_') WHERE menu_code LIKE 'BZA_%';
UPDATE MBW_MENU SET parent_menu_code = REPLACE(parent_menu_code, 'BZA_', 'MBW_') WHERE parent_menu_code LIKE 'BZA_%';
UPDATE MBW_MENU SET module_code = 'MBW' WHERE module_code = 'BZA';
UPDATE MBW_MENU SET route_path = REPLACE(route_path, '/bza', '/backoffice') WHERE route_path LIKE '/bza%';
UPDATE MBW_MENU SET api_path = REPLACE(api_path, '/api/bza', '/api/v1/backoffice') WHERE api_path LIKE '/api/bza%';
UPDATE MBW_PERMISSION SET menu_code = REPLACE(menu_code, 'BZA_', 'MBW_') WHERE menu_code LIKE 'BZA_%';
UPDATE MBW_PERMISSION SET api_pattern = REPLACE(api_pattern, '/api/bza', '/api/v1/backoffice') WHERE api_pattern LIKE '/api/bza%';
