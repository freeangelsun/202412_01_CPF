-- QA-B3-004: currentize V92 ADM incident lifecycle tables to canonical ADM_* ownership.
BEGIN;
ALTER TABLE cpf_incident_policy RENAME TO adm_incident_policy;
ALTER TABLE cpf_maintenance_window RENAME TO adm_maintenance_window;
ALTER TABLE cpf_incident RENAME TO adm_incident_lifecycle;
ALTER TABLE cpf_incident_signal RENAME TO adm_incident_signal;
ALTER TABLE cpf_incident_timeline RENAME TO adm_incident_timeline;
ALTER TABLE cpf_incident_command RENAME TO adm_incident_command;
COMMIT;
