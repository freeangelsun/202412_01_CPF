-- CPF R9 rollback: remove BZA resubmit linkage only; approval history remains immutable.
ALTER TABLE bza_approval_document DROP FOREIGN KEY fk_bza_approval_document_resubmit;
DROP INDEX IF EXISTS ix_bza_approval_document_resubmit ON bza_approval_document;
ALTER TABLE bza_approval_document DROP COLUMN IF EXISTS resubmitted_from_approval_id;
