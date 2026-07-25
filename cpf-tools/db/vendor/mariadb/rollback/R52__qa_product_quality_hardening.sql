-- R13 rollback.
-- version_no/role_type/grant_reason columns are intentionally retained because they are backward-compatible
-- and may pre-exist in development DBs due to pre-GA schema drift. Destructive rollback must not remove unknown owner data.

USE mbrDB;
DROP TABLE IF EXISTS mbr_member_role_operation;
DROP TABLE IF EXISTS mbr_member_no_issue_history;
DROP TABLE IF EXISTS mbr_member_no_sequence;

USE admDB;
ALTER TABLE adm_download_audit_log
    DROP COLUMN IF EXISTS CSV_POLICY_VERSION;
