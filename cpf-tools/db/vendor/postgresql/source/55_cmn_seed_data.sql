-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=55_cmn_seed_data.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cmnDB
INSERT INTO cmn_sample_item (sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by) VALUES ('CMN-SAMPLE-001', 'CPF CMN 기본 샘플', 'DATABASE', 'ACTIVE', 'connection migration crud search offset slice cursor', NULL, 10, 0, 'CMN_SAMPLE', 'CMN_SAMPLE'),
    ('CMN-SAMPLE-002', 'CPF CMN 비활성 샘플', 'VALIDATION', 'INACTIVE', 'validation duplicate optimistic-lock rollback', NULL, 20, 0, 'CMN_SAMPLE', 'CMN_SAMPLE') ON CONFLICT (sample_key) DO UPDATE SET item_name = EXCLUDED.item_name, category_code = EXCLUDED.category_code, status_code = EXCLUDED.status_code, searchable_text = EXCLUDED.searchable_text, owner_reference = EXCLUDED.owner_reference, sort_order = EXCLUDED.sort_order, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
