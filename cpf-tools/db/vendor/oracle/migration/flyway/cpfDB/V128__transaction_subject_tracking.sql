-- Protected Subject -> transactionId canonical index. Raw subject identifiers are never stored.
CREATE TABLE OPS_TRANSACTION_SUBJECT (
    transaction_id VARCHAR2(128 CHAR) NOT NULL,
    subject_role VARCHAR2(32 CHAR) DEFAULT 'ACTOR' NOT NULL,
    subject_type VARCHAR2(32 CHAR) NOT NULL,
    subject_search_key VARCHAR2(64 CHAR) NOT NULL,
    subject_masked_value VARCHAR2(256 CHAR) NOT NULL,
    source_type VARCHAR2(40 CHAR) NOT NULL,
    trust_level VARCHAR2(20 CHAR) DEFAULT 'CLAIMED' NOT NULL,
    search_key_version VARCHAR2(64 CHAR) NOT NULL,
    first_seen_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_seen_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_OPS_TRANSACTION_SUBJECT PRIMARY KEY (transaction_id, subject_role, subject_type, subject_search_key),
    CONSTRAINT ck_ops_transaction_subject_role CHECK (subject_role IN ('ACTOR','RELATED','BENEFICIARY','OWNER','TARGET')),
    CONSTRAINT ck_ops_transaction_subject_type CHECK (subject_type IN ('CUSTOMER_NO','CUSTOMER_ID','MEMBER_NO','LOGIN_ID')),
    CONSTRAINT ck_ops_transaction_subject_trust CHECK (trust_level IN ('UNVERIFIED','CLAIMED','TRUSTED','VERIFIED'))
);
CREATE INDEX ix_ops_transaction_subject_search ON OPS_TRANSACTION_SUBJECT (subject_role, subject_type, subject_search_key, first_seen_at);
CREATE INDEX ix_ops_transaction_subject_transaction ON OPS_TRANSACTION_SUBJECT (transaction_id, first_seen_at);
CREATE INDEX ix_ops_transaction_subject_retention ON OPS_TRANSACTION_SUBJECT (last_seen_at, transaction_id);
