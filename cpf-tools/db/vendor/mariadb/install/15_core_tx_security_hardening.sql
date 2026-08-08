CREATE TABLE IF NOT EXISTS cpf_xa_recovery (
 transaction_id VARCHAR(80) NOT NULL PRIMARY KEY, attempt_id VARCHAR(120) NOT NULL, outcome VARCHAR(30) NOT NULL,
 fencing_token BIGINT NOT NULL DEFAULT 0, detail VARCHAR(1000), updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 INDEX ix_cpf_xa_recovery_outcome(outcome,updated_at)
);
CREATE TABLE IF NOT EXISTS cpf_tamper_audit_head (
 head_id INT NOT NULL PRIMARY KEY, sequence_no BIGINT NOT NULL, current_hash VARCHAR(64) NOT NULL, version_no BIGINT NOT NULL DEFAULT 0,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), CONSTRAINT ck_cpf_tamper_audit_head_id CHECK(head_id=1)
);
INSERT IGNORE INTO cpf_tamper_audit_head(head_id,sequence_no,current_hash,version_no) VALUES(1,0,'GENESIS',0);
CREATE TABLE IF NOT EXISTS cpf_tamper_audit (
 sequence_no BIGINT NOT NULL PRIMARY KEY, transaction_id VARCHAR(80) NOT NULL, actor_id VARCHAR(160), action_code VARCHAR(160) NOT NULL,
 payload_hash VARCHAR(64) NOT NULL, previous_hash VARCHAR(64) NOT NULL, current_hash VARCHAR(64) NOT NULL,
 key_id VARCHAR(200) NOT NULL, key_version VARCHAR(100), algorithm VARCHAR(100) NOT NULL, certificate_id VARCHAR(200),
 signature_value LONGBLOB NOT NULL, occurred_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_cpf_tamper_audit_current_hash(current_hash), INDEX ix_cpf_tamper_audit_tx(transaction_id,sequence_no)
);

CREATE TABLE IF NOT EXISTS cpf_ref_tcc_reservation (
 transaction_id VARCHAR(80) NOT NULL, branch_id VARCHAR(120) NOT NULL, idempotency_key VARCHAR(160) NOT NULL,
 account_id VARCHAR(160) NOT NULL, amount DECIMAL(38,8) NOT NULL, state VARCHAR(30) NOT NULL, deadline_at DATETIME(3) NOT NULL,
 fencing_token BIGINT NOT NULL DEFAULT 0, review_reason VARCHAR(500), updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 PRIMARY KEY(transaction_id,branch_id), UNIQUE KEY uk_cpf_ref_tcc_idem(idempotency_key), INDEX ix_cpf_ref_tcc_recovery(state,deadline_at,updated_at)
);
