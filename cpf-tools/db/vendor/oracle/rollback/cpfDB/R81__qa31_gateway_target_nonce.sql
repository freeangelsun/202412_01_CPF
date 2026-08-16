DROP TABLE cpf_gateway_control_security_audit;
-- Data-destructive rollback. Export nonce state before execution.
DROP TABLE cpf_gateway_control_nonce;
ALTER TABLE cpf_gateway_binding DROP COLUMN target_path;
