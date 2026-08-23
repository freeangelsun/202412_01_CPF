DROP TRIGGER IF EXISTS trg_gw_binding_key_hash_compat ON GW_BINDING;
DROP FUNCTION IF EXISTS cpf_gw_binding_key_hash_compat();

ALTER TABLE GW_BINDING ADD COLUMN IF NOT EXISTS binding_key_hash CHAR(64);
ALTER TABLE GW_BINDING DROP CONSTRAINT IF EXISTS uk_cpf_gwy_binding_key;
DROP INDEX IF EXISTS uq_gwy_binding_key;

UPDATE GW_BINDING
   SET binding_key_hash = encode(sha256(convert_to(concat(
       encode(sha256(convert_to(COALESCE(NULLIF(BTRIM(environment_code), ''), 'DEFAULT'), 'UTF8')), 'hex'),
       encode(sha256(convert_to(COALESCE(NULLIF(BTRIM(host_pattern), ''), '*'), 'UTF8')), 'hex'),
       encode(sha256(convert_to(BTRIM(path_pattern), 'UTF8')), 'hex'),
       encode(sha256(convert_to(COALESCE(NULLIF(BTRIM(http_method), ''), '*'), 'UTF8')), 'hex'),
       encode(sha256(convert_to(COALESCE(NULLIF(BTRIM(api_version), ''), 'v1'), 'UTF8')), 'hex'),
       encode(sha256(convert_to(COALESCE(NULLIF(BTRIM(route_version), ''), '1'), 'UTF8')), 'hex')
   ), 'UTF8')), 'hex');

ALTER TABLE GW_BINDING ALTER COLUMN binding_key_hash SET NOT NULL;
ALTER TABLE GW_BINDING ADD CONSTRAINT uk_cpf_gwy_binding_key UNIQUE (binding_key_hash);

ALTER TABLE CMN_RESPONSE_CODE ADD COLUMN IF NOT EXISTS http_status INTEGER;
UPDATE CMN_RESPONSE_CODE
   SET http_status = CASE response_code
       WHEN 'SCPF000000' THEN 200
       WHEN 'ECPF010001' THEN 400
       WHEN 'ECPF010002' THEN 404
       WHEN 'ECPF010003' THEN 409
       WHEN 'ECPF010004' THEN 400
       WHEN 'ECPF010005' THEN 401
       WHEN 'ECPF010006' THEN 403
       WHEN 'ECPF020001' THEN 400
       WHEN 'ECPF030001' THEN 502
       WHEN 'ECPF900001' THEN 400
       WHEN 'ECPF900002' THEN 500
       WHEN 'ECPF900003' THEN 500
       WHEN 'ECPF900004' THEN 400
       WHEN 'ECPF900005' THEN 403
       WHEN 'ECPF990000' THEN 500
       WHEN 'ECPF990001' THEN 500
       WHEN 'SMBW000000' THEN 200
       WHEN 'EMBW010001' THEN 400
       WHEN 'EMBW010002' THEN 403
       WHEN 'EEDU010001' THEN 409
       WHEN 'ECPF030002' THEN 504
       WHEN 'ECPF030003' THEN 503
       WHEN 'ECPF030004' THEN 202
       WHEN 'ECPF020002' THEN 409
       WHEN 'ECPF020003' THEN 409
       WHEN 'ECPF040001' THEN 423
       WHEN 'ECPF040002' THEN 403
       WHEN 'ECPF020004' THEN 403
       WHEN 'ECPF020005' THEN 409
       WHEN 'ECPF020006' THEN 409
       WHEN 'ECPF020007' THEN 409
       WHEN 'ECPF040003' THEN 423
       WHEN 'ECPF040004' THEN 403
       WHEN 'ECPF050001' THEN 403
       WHEN 'ECPF050002' THEN 400
       ELSE CASE
           WHEN http_status BETWEEN 100 AND 599 THEN http_status
           WHEN result_type = 'S' THEN 200
           ELSE 500
       END
   END;
ALTER TABLE CMN_RESPONSE_CODE ALTER COLUMN http_status SET NOT NULL;
