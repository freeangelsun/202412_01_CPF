-- R40 remove CPF Saga runtime. Data destructive; execute only approved rollback.
DROP TABLE IF EXISTS cpf_saga_manual_action;
DROP TABLE IF EXISTS cpf_saga_step_execution;
DROP TABLE IF EXISTS cpf_saga_execution;
