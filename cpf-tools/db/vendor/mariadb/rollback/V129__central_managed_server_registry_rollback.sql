ALTER TABLE OPS_SERVICE_INSTANCE DROP FOREIGN KEY fk_cpf_service_instance_managed_server;
DROP INDEX ix_cpf_service_instance_managed_server ON OPS_SERVICE_INSTANCE;
ALTER TABLE OPS_SERVICE_INSTANCE
    DROP COLUMN started_at,
    DROP COLUMN application_version,
    DROP COLUMN cpf_version,
    DROP COLUMN java_version,
    DROP COLUMN process_id,
    DROP COLUMN runtime_hostname,
    DROP COLUMN application_role,
    DROP COLUMN application_name,
    DROP COLUMN system_code,
    DROP COLUMN managed_server_id;
DROP TABLE OPS_MANAGED_SERVER;
