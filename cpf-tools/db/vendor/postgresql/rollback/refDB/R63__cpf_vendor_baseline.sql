-- Exact rollback of CPF postgresql initial baseline for refDB
DROP TABLE IF EXISTS ref_sample_item CASCADE;
DROP TABLE IF EXISTS ref_center_cut_sample_target CASCADE;
DROP TABLE IF EXISTS ref_center_cut_sample_result CASCADE;
