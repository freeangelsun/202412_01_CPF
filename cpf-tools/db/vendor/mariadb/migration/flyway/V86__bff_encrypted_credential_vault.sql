CREATE TABLE cpf_bff_credential_vault (
    handle_id VARCHAR(64) NOT NULL,
    key_id VARCHAR(100) NOT NULL,
    access_iv VARBINARY(32) NOT NULL,
    access_cipher_text LONGBLOB NOT NULL,
    refresh_iv VARBINARY(32) NULL,
    refresh_cipher_text LONGBLOB NULL,
    access_expires_at DATETIME(6) NOT NULL,
    refresh_expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version_no BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT pk_cpf_bff_credential_vault PRIMARY KEY (handle_id),
    CONSTRAINT ck_cpf_bff_credential_version CHECK (version_no > 0),
    CONSTRAINT ck_cpf_bff_credential_expiry CHECK (refresh_expires_at >= access_expires_at)
) ENGINE=InnoDB;
CREATE INDEX idx_cpf_bff_credential_expiry ON cpf_bff_credential_vault (refresh_expires_at);
CREATE INDEX idx_cpf_bff_credential_key ON cpf_bff_credential_vault (key_id, updated_at);
