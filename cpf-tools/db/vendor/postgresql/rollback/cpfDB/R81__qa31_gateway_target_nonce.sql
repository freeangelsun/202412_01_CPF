DROP TABLE IF EXISTS cpf_gateway_control_security_audit;
-- Data-destructive rollback. Export nonce state before execution.
DROP TABLE IF EXISTS cpf_gateway_control_nonce;
ALTER TABLE cpf_gateway_binding DROP COLUMN IF EXISTS target_path;
