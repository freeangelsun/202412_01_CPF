-- CPF_LOGICAL_DATABASE=mbwDB
-- One environment-scoped row owns each Backoffice role/menu/action permission identity.
-- Existing duplicate identities fail closed while the unique key is created.
USE mbwDB;
ALTER TABLE MBW_PERMISSION
    ADD CONSTRAINT uk_mbw_permission_scope
    UNIQUE (role_code, menu_code, button_code, permission_type, environment_code);
