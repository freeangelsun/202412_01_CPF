BEGIN;
ALTER TABLE adm_incident_policy RENAME TO cpf_incident_policy;
ALTER TABLE adm_maintenance_window RENAME TO cpf_maintenance_window;
ALTER TABLE adm_incident_lifecycle RENAME TO cpf_incident;
ALTER TABLE adm_incident_signal RENAME TO cpf_incident_signal;
ALTER TABLE adm_incident_timeline RENAME TO cpf_incident_timeline;
ALTER TABLE adm_incident_command RENAME TO cpf_incident_command;
COMMIT;
