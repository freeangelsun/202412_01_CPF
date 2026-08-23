DROP TRIGGER IF EXISTS trg_gw_binding_key_hash_compat_bi;
DROP TRIGGER IF EXISTS trg_gw_binding_key_hash_compat_bu;

SET @cpf_sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'GW_BINDING' AND column_name = 'binding_key_hash') = 0,
    'ALTER TABLE GW_BINDING ADD COLUMN binding_key_hash CHAR(64) NULL COMMENT ''Route Match Key SHA-256''',
    'SELECT 1'
);
PREPARE cpf_stmt FROM @cpf_sql;
EXECUTE cpf_stmt;
DEALLOCATE PREPARE cpf_stmt;

SET @cpf_sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
      WHERE table_schema = DATABASE() AND table_name = 'GW_BINDING' AND index_name = 'uk_cpf_gwy_binding_key') > 0,
    'ALTER TABLE GW_BINDING DROP INDEX uk_cpf_gwy_binding_key',
    'SELECT 1'
);
PREPARE cpf_stmt FROM @cpf_sql;
EXECUTE cpf_stmt;
DEALLOCATE PREPARE cpf_stmt;

SET @cpf_sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
      WHERE table_schema = DATABASE() AND table_name = 'GW_BINDING' AND index_name = 'uq_gwy_binding_key') > 0,
    'ALTER TABLE GW_BINDING DROP INDEX uq_gwy_binding_key',
    'SELECT 1'
);
PREPARE cpf_stmt FROM @cpf_sql;
EXECUTE cpf_stmt;
DEALLOCATE PREPARE cpf_stmt;

UPDATE GW_BINDING
   SET binding_key_hash = LOWER(SHA2(CONCAT(
       LOWER(SHA2(COALESCE(NULLIF(TRIM(environment_code), ''), 'DEFAULT'), 256)),
       LOWER(SHA2(COALESCE(NULLIF(TRIM(host_pattern), ''), '*'), 256)),
       LOWER(SHA2(TRIM(path_pattern), 256)),
       LOWER(SHA2(COALESCE(NULLIF(TRIM(http_method), ''), '*'), 256)),
       LOWER(SHA2(COALESCE(NULLIF(TRIM(api_version), ''), 'v1'), 256)),
       LOWER(SHA2(COALESCE(NULLIF(TRIM(route_version), ''), '1'), 256))
   ), 256));

ALTER TABLE GW_BINDING MODIFY binding_key_hash CHAR(64) NOT NULL;
ALTER TABLE GW_BINDING ADD CONSTRAINT uk_cpf_gwy_binding_key UNIQUE (binding_key_hash);

SET @cpf_sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'CMN_RESPONSE_CODE' AND column_name = 'http_status') = 0,
    'ALTER TABLE CMN_RESPONSE_CODE ADD COLUMN http_status INT NULL AFTER sequence_no',
    'SELECT 1'
);
PREPARE cpf_stmt FROM @cpf_sql;
EXECUTE cpf_stmt;
DEALLOCATE PREPARE cpf_stmt;

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

ALTER TABLE CMN_RESPONSE_CODE MODIFY http_status INT NOT NULL;
SET @cpf_sql = NULL;
