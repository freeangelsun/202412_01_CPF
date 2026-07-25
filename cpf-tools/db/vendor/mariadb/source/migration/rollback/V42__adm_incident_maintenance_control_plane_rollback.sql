-- Rollback R9 ADM control-plane tables. Execute only after exporting required audit evidence.
DROP TABLE IF EXISTS adm_maintenance_action;
DROP TABLE IF EXISTS adm_incident;
