ALTER TABLE GW_BINDING ALTER COLUMN binding_key_hash DROP NOT NULL;

CREATE OR REPLACE FUNCTION cpf_gw_binding_key_hash_compat() RETURNS trigger AS $$
BEGIN
    NEW.binding_key_hash = encode(sha256(convert_to(concat(
        encode(sha256(convert_to(COALESCE(NULLIF(BTRIM(NEW.environment_code), ''), 'DEFAULT'), 'UTF8')), 'hex'),
        encode(sha256(convert_to(COALESCE(NULLIF(BTRIM(NEW.host_pattern), ''), '*'), 'UTF8')), 'hex'),
        encode(sha256(convert_to(BTRIM(NEW.path_pattern), 'UTF8')), 'hex'),
        encode(sha256(convert_to(COALESCE(NULLIF(BTRIM(NEW.http_method), ''), '*'), 'UTF8')), 'hex'),
        encode(sha256(convert_to(COALESCE(NULLIF(BTRIM(NEW.api_version), ''), 'v1'), 'UTF8')), 'hex'),
        encode(sha256(convert_to(COALESCE(NULLIF(BTRIM(NEW.route_version), ''), '1'), 'UTF8')), 'hex')
    ), 'UTF8')), 'hex');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_gw_binding_key_hash_compat ON GW_BINDING;
CREATE TRIGGER trg_gw_binding_key_hash_compat
BEFORE INSERT OR UPDATE OF environment_code, host_pattern, path_pattern, http_method, api_version, route_version
ON GW_BINDING FOR EACH ROW EXECUTE FUNCTION cpf_gw_binding_key_hash_compat();

ALTER TABLE CMN_RESPONSE_CODE ALTER COLUMN http_status DROP NOT NULL;
