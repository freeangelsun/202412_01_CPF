CREATE TABLE cpf_bff_credential_vault (
    handle_id VARCHAR(64) PRIMARY KEY,
    key_id VARCHAR(100) NOT NULL,
    access_iv BYTEA NOT NULL,
    access_cipher_text BYTEA NOT NULL,
    refresh_iv BYTEA NULL,
    refresh_cipher_text BYTEA NULL,
    access_expires_at TIMESTAMP(6) NOT NULL,
    refresh_expires_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version_no BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT ck_cpf_bff_credential_version CHECK (version_no > 0),
    CONSTRAINT ck_cpf_bff_credential_expiry CHECK (refresh_expires_at >= access_expires_at)
);
CREATE INDEX idx_cpf_bff_credential_expiry ON cpf_bff_credential_vault (refresh_expires_at);
CREATE INDEX idx_cpf_bff_credential_key ON cpf_bff_credential_vault (key_id, updated_at);
