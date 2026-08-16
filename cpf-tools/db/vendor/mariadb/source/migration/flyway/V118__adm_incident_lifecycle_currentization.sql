-- QA-B3-004: currentize V92 ADM incident lifecycle tables to canonical ADM_* ownership.
-- Historical V92 remains immutable; this migration only renames the existing tables and preserves data/identity/FK relationships.
USE cpfDB;

RENAME TABLE
    cpf_incident_policy TO adm_incident_policy,
    cpf_maintenance_window TO adm_maintenance_window,
    cpf_incident TO adm_incident_lifecycle,
    cpf_incident_signal TO adm_incident_signal,
    cpf_incident_timeline TO adm_incident_timeline,
    cpf_incident_command TO adm_incident_command;
