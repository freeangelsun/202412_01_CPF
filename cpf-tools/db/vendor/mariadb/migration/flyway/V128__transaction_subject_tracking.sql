-- Protected Subject -> transactionId canonical index. Raw subject identifiers are never stored.
CREATE TABLE OPS_TRANSACTION_SUBJECT (
    transaction_id VARCHAR(128) NOT NULL,
    subject_role VARCHAR(32) DEFAULT 'ACTOR' NOT NULL,
    subject_type VARCHAR(32) NOT NULL,
    subject_search_key VARCHAR(64) NOT NULL,
    subject_masked_value VARCHAR(256) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    trust_level VARCHAR(20) DEFAULT 'CLAIMED' NOT NULL,
    search_key_version VARCHAR(64) NOT NULL,
    first_seen_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_seen_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_OPS_TRANSACTION_SUBJECT PRIMARY KEY (transaction_id, subject_role, subject_type, subject_search_key),
    CONSTRAINT ck_ops_transaction_subject_role CHECK (subject_role IN ('ACTOR','RELATED','BENEFICIARY','OWNER','TARGET')),
    CONSTRAINT ck_ops_transaction_subject_type CHECK (subject_type IN ('CUSTOMER_NO','CUSTOMER_ID','MEMBER_NO','LOGIN_ID')),
    CONSTRAINT ck_ops_transaction_subject_trust CHECK (trust_level IN ('UNVERIFIED','CLAIMED','TRUSTED','VERIFIED'))
) ENGINE=InnoDB;
CREATE INDEX ix_ops_transaction_subject_search ON OPS_TRANSACTION_SUBJECT (subject_role, subject_type, subject_search_key, first_seen_at);
CREATE INDEX ix_ops_transaction_subject_transaction ON OPS_TRANSACTION_SUBJECT (transaction_id, first_seen_at);
CREATE INDEX ix_ops_transaction_subject_retention ON OPS_TRANSACTION_SUBJECT (last_seen_at, transaction_id);
