-- CPF_LOGICAL_DATABASE=batDB
-- Rollback removes only V77 additive artifacts; safety relaxations remain compatible.
DROP TABLE bat_execution_result_detail;
DROP TABLE bat_job_runtime_projection_outbox;
DROP TABLE bat_job_runtime_projection;
ALTER TABLE bat_job DROP CONSTRAINT fk_bat_job_published_definition;
ALTER TABLE bat_execution DROP COLUMN definition_checksum, DROP COLUMN definition_version;
ALTER TABLE bat_schedule DROP COLUMN definition_checksum, DROP COLUMN definition_version;
ALTER TABLE bat_job DROP COLUMN definition_published_at, DROP COLUMN executor_reference, DROP COLUMN published_definition_checksum, DROP COLUMN published_definition_version;
