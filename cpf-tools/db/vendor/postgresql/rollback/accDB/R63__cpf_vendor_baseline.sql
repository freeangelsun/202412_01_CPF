-- Exact rollback of CPF postgresql initial baseline for accDB
DROP TABLE IF EXISTS acc_account_change_log CASCADE;
DROP TABLE IF EXISTS acc_account CASCADE;
