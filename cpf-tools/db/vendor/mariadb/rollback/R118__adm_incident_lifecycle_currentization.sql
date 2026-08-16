-- QA-B3-004 rollback: restore V92 physical names without changing data.
USE cpfDB;

RENAME TABLE
    adm_incident_policy TO cpf_incident_policy,
    adm_maintenance_window TO cpf_maintenance_window,
    adm_incident_lifecycle TO cpf_incident,
    adm_incident_signal TO cpf_incident_signal,
    adm_incident_timeline TO cpf_incident_timeline,
    adm_incident_command TO cpf_incident_command;
