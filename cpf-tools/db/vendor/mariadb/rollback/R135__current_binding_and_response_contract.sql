ALTER TABLE GW_BINDING MODIFY binding_key_hash CHAR(64) NULL;

DROP TRIGGER IF EXISTS trg_gw_binding_key_hash_compat_bi;
CREATE TRIGGER trg_gw_binding_key_hash_compat_bi
BEFORE INSERT ON GW_BINDING
FOR EACH ROW SET NEW.binding_key_hash = LOWER(SHA2(CONCAT(
    LOWER(SHA2(COALESCE(NULLIF(TRIM(NEW.environment_code), ''), 'DEFAULT'), 256)),
    LOWER(SHA2(COALESCE(NULLIF(TRIM(NEW.host_pattern), ''), '*'), 256)),
    LOWER(SHA2(TRIM(NEW.path_pattern), 256)),
    LOWER(SHA2(COALESCE(NULLIF(TRIM(NEW.http_method), ''), '*'), 256)),
    LOWER(SHA2(COALESCE(NULLIF(TRIM(NEW.api_version), ''), 'v1'), 256)),
    LOWER(SHA2(COALESCE(NULLIF(TRIM(NEW.route_version), ''), '1'), 256))
), 256));

DROP TRIGGER IF EXISTS trg_gw_binding_key_hash_compat_bu;
CREATE TRIGGER trg_gw_binding_key_hash_compat_bu
BEFORE UPDATE ON GW_BINDING
FOR EACH ROW SET NEW.binding_key_hash = LOWER(SHA2(CONCAT(
    LOWER(SHA2(COALESCE(NULLIF(TRIM(NEW.environment_code), ''), 'DEFAULT'), 256)),
    LOWER(SHA2(COALESCE(NULLIF(TRIM(NEW.host_pattern), ''), '*'), 256)),
    LOWER(SHA2(TRIM(NEW.path_pattern), 256)),
    LOWER(SHA2(COALESCE(NULLIF(TRIM(NEW.http_method), ''), '*'), 256)),
    LOWER(SHA2(COALESCE(NULLIF(TRIM(NEW.api_version), ''), 'v1'), 256)),
    LOWER(SHA2(COALESCE(NULLIF(TRIM(NEW.route_version), ''), '1'), 256))
), 256));

ALTER TABLE CMN_RESPONSE_CODE MODIFY http_status INT NULL;
