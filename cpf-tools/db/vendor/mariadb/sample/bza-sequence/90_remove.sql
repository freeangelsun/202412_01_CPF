-- Optional BZA sequence customization sample rollback.
DELETE FROM bza_permission WHERE menu_code='BZA_SEQUENCE_SAMPLE';
DELETE FROM bza_menu WHERE menu_code='BZA_SEQUENCE_SAMPLE';
DROP TABLE IF EXISTS bza_sequence_sample_issue;
DROP TABLE IF EXISTS bza_sequence_sample_rule;
