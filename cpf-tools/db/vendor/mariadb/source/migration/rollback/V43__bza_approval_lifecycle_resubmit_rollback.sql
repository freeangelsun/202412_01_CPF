-- CPF R9 rollback: remove MBW resubmit linkage only; approval history remains immutable.
ALTER TABLE mbw_approval_document DROP FOREIGN KEY fk_mbw_approval_document_resubmit;
DROP INDEX IF EXISTS ix_mbw_approval_document_resubmit ON mbw_approval_document;
ALTER TABLE mbw_approval_document DROP COLUMN IF EXISTS resubmitted_from_approval_id;
