-- CPF_DESTRUCTIVE_ROLLBACK_APPROVAL_REQUIRED
-- Execute only after backup/export hash, retention expiry, approval and reconciliation completion are recorded.
drop table CPF_BATCH_APPROVED_LAUNCH_R82_ARCHIVE;
drop table CPF_BATCH_EXECUTION_LINK_R82_ARCHIVE;
drop table CPF_BATCH_EXECUTION_CONTROL_R82_ARCHIVE;
