CREATE TABLE cpf_bff_credential_vault (
    handle_id VARCHAR2(64 CHAR) NOT NULL,
    key_id VARCHAR2(100 CHAR) NOT NULL,
    access_iv RAW(32) NOT NULL,
    access_cipher_text BLOB NOT NULL,
    refresh_iv RAW(32),
    refresh_cipher_text BLOB,
    access_expires_at TIMESTAMP(6) NOT NULL,
    refresh_expires_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version_no NUMBER(19) DEFAULT 1 NOT NULL,
    CONSTRAINT pk_cpf_bff_credential_vault PRIMARY KEY (handle_id),
    CONSTRAINT ck_cpf_bff_credential_version CHECK (version_no > 0),
    CONSTRAINT ck_cpf_bff_credential_expiry CHECK (refresh_expires_at >= access_expires_at)
);
CREATE INDEX idx_cpf_bff_credential_expiry ON cpf_bff_credential_vault (refresh_expires_at);
CREATE INDEX idx_cpf_bff_credential_key ON cpf_bff_credential_vault (key_id, updated_at);
