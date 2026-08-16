-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=mariadb; source=55_cmn_seed_data.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB

-- CPF_LOGICAL_DATABASE=referenceFixture
INSERT INTO REF_CMN_SAMPLE_ITEM (sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by) VALUES ('CMN-SAMPLE-001', 'CPF CMN 기본 샘플', 'DATABASE', 'ACTIVE', 'connection migration crud search offset slice cursor', NULL, 10, 0, 'CMN_SAMPLE', 'CMN_SAMPLE'),
    ('CMN-SAMPLE-002', 'CPF CMN 비활성 샘플', 'VALIDATION', 'INACTIVE', 'validation duplicate optimistic-lock rollback', NULL, 20, 0, 'CMN_SAMPLE', 'CMN_SAMPLE') ON DUPLICATE KEY UPDATE item_name = VALUES(item_name), category_code = VALUES(category_code), status_code = VALUES(status_code), searchable_text = VALUES(searchable_text), owner_reference = VALUES(owner_reference), sort_order = VALUES(sort_order), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);
