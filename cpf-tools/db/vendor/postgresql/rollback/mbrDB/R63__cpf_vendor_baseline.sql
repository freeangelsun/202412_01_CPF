-- Exact rollback of CPF postgresql initial baseline for mbrDB
DROP TABLE IF EXISTS mbr_sample_item CASCADE;
DROP TABLE IF EXISTS mbr_member_role_operation CASCADE;
DROP TABLE IF EXISTS mbr_member_role_history CASCADE;
DROP TABLE IF EXISTS mbr_member_role CASCADE;
DROP TABLE IF EXISTS mbr_member_no_sequence CASCADE;
DROP TABLE IF EXISTS mbr_member_no_issue_history CASCADE;
DROP TABLE IF EXISTS mbr_member_login_history CASCADE;
DROP TABLE IF EXISTS mbr_member CASCADE;
