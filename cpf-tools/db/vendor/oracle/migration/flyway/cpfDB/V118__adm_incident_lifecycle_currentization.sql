-- QA-B3-004: currentize V92 ADM incident lifecycle tables to canonical ADM_* ownership.
RENAME cpf_incident_policy TO adm_incident_policy;
RENAME cpf_maintenance_window TO adm_maintenance_window;
RENAME cpf_incident TO adm_incident_lifecycle;
RENAME cpf_incident_signal TO adm_incident_signal;
RENAME cpf_incident_timeline TO adm_incident_timeline;
RENAME cpf_incident_command TO adm_incident_command;
