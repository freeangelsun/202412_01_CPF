-- CPF_LOGICAL_DATABASE=batDB
-- Rollback removes only V77 additive artifacts; safety relaxations remain compatible.
DROP TABLE bat_execution_result_detail CASCADE CONSTRAINTS;
DROP TABLE bat_job_runtime_projection_outbox CASCADE CONSTRAINTS;
DROP TABLE bat_job_runtime_projection CASCADE CONSTRAINTS;
ALTER TABLE bat_job DROP CONSTRAINT fk_bat_job_published_definition;
ALTER TABLE bat_execution DROP (definition_checksum, definition_version);
ALTER TABLE bat_schedule DROP (definition_checksum, definition_version);
ALTER TABLE bat_job DROP (definition_published_at, executor_reference, published_definition_checksum, published_definition_version);
