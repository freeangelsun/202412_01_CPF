-- CPF Oracle provisioning template. Execute as a privileged deployment account.
-- Oracle user/schema creation is driven by the install profile and deployment credential policy.
SELECT SYS_CONTEXT('USERENV','CURRENT_SCHEMA') AS current_schema FROM dual;
