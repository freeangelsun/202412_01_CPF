-- CPF_LOGICAL_DATABASE=mbwDB
-- Rollback V133 Backoffice MBW identity migration.
USE mbwDB;
-- Currentize Backoffice logical codes without losing existing user-role grants.
INSERT INTO MBW_ROLE (role_code, role_name, write_allowed_yn, data_scope, use_yn, version_no, created_by, created_at, updated_by, updated_at)
SELECT CASE role_code WHEN 'MBW_ADMIN' THEN 'BZA_ADMIN' WHEN 'MBW_OPERATOR' THEN 'BZA_OPERATOR' WHEN 'MBW_APPROVER' THEN 'BZA_APPROVER' WHEN 'MBW_VIEWER' THEN 'BZA_VIEWER' WHEN 'MBW_MANAGER' THEN 'BZA_MANAGER' ELSE role_code END, role_name, write_allowed_yn, data_scope, use_yn, version_no, created_by, created_at, updated_by, updated_at
FROM MBW_ROLE old_role
WHERE role_code IN ('MBW_ADMIN', 'MBW_OPERATOR', 'MBW_APPROVER', 'MBW_VIEWER', 'MBW_MANAGER')
  AND NOT EXISTS (SELECT 1 FROM MBW_ROLE new_role WHERE new_role.role_code = CASE role_code WHEN 'MBW_ADMIN' THEN 'BZA_ADMIN' WHEN 'MBW_OPERATOR' THEN 'BZA_OPERATOR' WHEN 'MBW_APPROVER' THEN 'BZA_APPROVER' WHEN 'MBW_VIEWER' THEN 'BZA_VIEWER' WHEN 'MBW_MANAGER' THEN 'BZA_MANAGER' ELSE role_code END);
UPDATE MBW_USER_ROLE SET role_code = CASE role_code WHEN 'MBW_ADMIN' THEN 'BZA_ADMIN' WHEN 'MBW_OPERATOR' THEN 'BZA_OPERATOR' WHEN 'MBW_APPROVER' THEN 'BZA_APPROVER' WHEN 'MBW_VIEWER' THEN 'BZA_VIEWER' WHEN 'MBW_MANAGER' THEN 'BZA_MANAGER' ELSE role_code END WHERE role_code IN ('MBW_ADMIN', 'MBW_OPERATOR', 'MBW_APPROVER', 'MBW_VIEWER', 'MBW_MANAGER');
UPDATE MBW_ADMIN_USER SET role_code = CASE role_code WHEN 'MBW_ADMIN' THEN 'BZA_ADMIN' WHEN 'MBW_OPERATOR' THEN 'BZA_OPERATOR' WHEN 'MBW_APPROVER' THEN 'BZA_APPROVER' WHEN 'MBW_VIEWER' THEN 'BZA_VIEWER' WHEN 'MBW_MANAGER' THEN 'BZA_MANAGER' ELSE role_code END WHERE role_code IN ('MBW_ADMIN', 'MBW_OPERATOR', 'MBW_APPROVER', 'MBW_VIEWER', 'MBW_MANAGER');
UPDATE MBW_PERMISSION SET role_code = CASE role_code WHEN 'MBW_ADMIN' THEN 'BZA_ADMIN' WHEN 'MBW_OPERATOR' THEN 'BZA_OPERATOR' WHEN 'MBW_APPROVER' THEN 'BZA_APPROVER' WHEN 'MBW_VIEWER' THEN 'BZA_VIEWER' WHEN 'MBW_MANAGER' THEN 'BZA_MANAGER' ELSE role_code END WHERE role_code IN ('MBW_ADMIN', 'MBW_OPERATOR', 'MBW_APPROVER', 'MBW_VIEWER', 'MBW_MANAGER');
DELETE FROM MBW_ROLE WHERE role_code IN ('MBW_ADMIN', 'MBW_OPERATOR', 'MBW_APPROVER', 'MBW_VIEWER', 'MBW_MANAGER');
UPDATE MBW_MENU SET menu_code = REPLACE(menu_code, 'MBW_', 'BZA_') WHERE menu_code LIKE 'MBW_%';
UPDATE MBW_MENU SET parent_menu_code = REPLACE(parent_menu_code, 'MBW_', 'BZA_') WHERE parent_menu_code LIKE 'MBW_%';
UPDATE MBW_MENU SET module_code = 'BZA' WHERE module_code = 'MBW';
UPDATE MBW_MENU SET route_path = REPLACE(route_path, '/backoffice', '/bza') WHERE route_path LIKE '/backoffice%';
UPDATE MBW_MENU SET api_path = REPLACE(api_path, '/api/v1/backoffice', '/api/bza') WHERE api_path LIKE '/api/v1/backoffice%';
UPDATE MBW_PERMISSION SET menu_code = REPLACE(menu_code, 'MBW_', 'BZA_') WHERE menu_code LIKE 'MBW_%';
UPDATE MBW_PERMISSION SET api_pattern = REPLACE(api_pattern, '/api/v1/backoffice', '/api/bza') WHERE api_pattern LIKE '/api/v1/backoffice%';

-- Restore the legacy login/runtime identity contract before shrinking/renaming columns.
UPDATE MBW_LOGIN_HISTORY SET login_domain = 'BZA' WHERE login_domain = 'MBW';
UPDATE MBW_LOGIN_HISTORY SET system_code = 'BZA' WHERE system_code = 'MBW' OR system_code IS NULL OR system_code = '';
UPDATE MBW_LOGIN_HISTORY
SET application_name = LEFT(COALESCE(NULLIF(instance_id, ''), NULLIF(application_name, ''), 'BZA0001'), 7);
ALTER TABLE MBW_LOGIN_HISTORY MODIFY COLUMN login_domain VARCHAR(30) NOT NULL DEFAULT 'BZA';
ALTER TABLE MBW_LOGIN_HISTORY CHANGE COLUMN instance_id server_instance_id VARCHAR(200) NULL;
ALTER TABLE MBW_LOGIN_HISTORY CHANGE COLUMN application_name was_id VARCHAR(7) NULL;
ALTER TABLE MBW_LOGIN_HISTORY CHANGE COLUMN system_code module_id VARCHAR(3) NULL;

-- Reverse physical table names.
RENAME TABLE MBW_USER_ROLE TO bza_user_role;
RENAME TABLE MBW_SAVED_SEARCH TO bza_saved_search;
RENAME TABLE MBW_ROLE TO bza_role;
RENAME TABLE MBW_REFRESH_TOKEN TO bza_refresh_token;
RENAME TABLE MBW_PROJECT_SETTING TO bza_project_setting;
RENAME TABLE MBW_POSITION TO bza_position;
RENAME TABLE MBW_PERMISSION TO bza_permission;
RENAME TABLE MBW_ORGANIZATION_RESPONSIBILITY TO bza_organization_responsibility;
RENAME TABLE MBW_ORGANIZATION TO bza_organization;
RENAME TABLE MBW_NOTIFICATION TO bza_notification;
RENAME TABLE MBW_MENU TO bza_menu;
RENAME TABLE MBW_LOGIN_OPERATION TO bza_login_operation;
RENAME TABLE MBW_LOGIN_HISTORY TO bza_login_history;
RENAME TABLE MBW_JOB_TITLE TO bza_job_title;
RENAME TABLE MBW_EMPLOYEE_ASSIGNMENT TO bza_employee_assignment;
RENAME TABLE MBW_EMPLOYEE TO bza_employee;
RENAME TABLE MBW_DOWNLOAD_AUDIT TO bza_download_audit;
RENAME TABLE MBW_BUSINESS_AUDIT TO bza_business_audit;
RENAME TABLE MBW_BOOTSTRAP_APPROVAL TO bza_bootstrap_approval;
RENAME TABLE MBW_AUDIT_CHAIN_LOCK TO bza_audit_chain_lock;
RENAME TABLE MBW_ATTACHMENT TO bza_attachment;
RENAME TABLE MBW_APPROVAL_POLICY_STEP TO bza_approval_policy_step;
RENAME TABLE MBW_APPROVAL_POLICY TO bza_approval_policy;
RENAME TABLE MBW_APPROVAL_PARTICIPANT TO bza_approval_participant;
RENAME TABLE MBW_APPROVAL_LINE TO bza_approval_line;
RENAME TABLE MBW_APPROVAL_HISTORY TO bza_approval_history;
RENAME TABLE MBW_APPROVAL_DOCUMENT TO bza_approval_document;
RENAME TABLE MBW_APPROVAL_DELEGATION TO bza_approval_delegation;
RENAME TABLE MBW_ADMIN_USER TO bza_admin_user;
