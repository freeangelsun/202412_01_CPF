-- CPF R9: BZA approval immutable resubmit linkage
ALTER TABLE bza_approval_document
    ADD COLUMN IF NOT EXISTS resubmitted_from_approval_id BIGINT NULL
        COMMENT '재상신 원본 결재 ID; 원본 Snapshot은 변경하지 않음' AFTER attachment_group_id;

CREATE INDEX IF NOT EXISTS ix_bza_approval_document_resubmit
    ON bza_approval_document(resubmitted_from_approval_id);

SET @cpf_fk_exists := (
    SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = DATABASE()
       AND CONSTRAINT_NAME = 'fk_bza_approval_document_resubmit'
);
SET @cpf_fk_sql := IF(@cpf_fk_exists = 0,
    'ALTER TABLE bza_approval_document ADD CONSTRAINT fk_bza_approval_document_resubmit FOREIGN KEY (resubmitted_from_approval_id) REFERENCES bza_approval_document(approval_id)',
    'SELECT 1');
PREPARE cpf_stmt FROM @cpf_fk_sql;
EXECUTE cpf_stmt;
DEALLOCATE PREPARE cpf_stmt;
