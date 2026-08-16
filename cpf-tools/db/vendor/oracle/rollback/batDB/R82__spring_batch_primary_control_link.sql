-- CPF R82 safe rollback: operational/audit data is preserved for reconciliation.
-- Destructive cleanup is intentionally separated under destructive/ and requires explicit approval.
alter table CPF_BATCH_APPROVED_LAUNCH rename to CPF_BATCH_APPROVED_LAUNCH_R82_ARCHIVE;
alter table CPF_BATCH_EXECUTION_LINK rename to CPF_BATCH_EXECUTION_LINK_R82_ARCHIVE;
alter table CPF_BATCH_EXECUTION_CONTROL rename to CPF_BATCH_EXECUTION_CONTROL_R82_ARCHIVE;
