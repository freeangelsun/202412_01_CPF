-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=55_cmn_seed_data.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB

-- CPF_LOGICAL_DATABASE=referenceFixture
MERGE INTO REF_CMN_SAMPLE_ITEM tgt USING (
SELECT 'CMN-SAMPLE-001' sample_key, 'CPF CMN 기본 샘플' item_name, 'DATABASE' category_code, 'ACTIVE' status_code, 'connection migration crud search offset slice cursor' searchable_text, NULL owner_reference, 10 sort_order, 0 version_no, 'CMN_SAMPLE' created_by, 'CMN_SAMPLE' updated_by FROM dual
UNION ALL
SELECT 'CMN-SAMPLE-002' sample_key, 'CPF CMN 비활성 샘플' item_name, 'VALIDATION' category_code, 'INACTIVE' status_code, 'validation duplicate optimistic-lock rollback' searchable_text, NULL owner_reference, 20 sort_order, 0 version_no, 'CMN_SAMPLE' created_by, 'CMN_SAMPLE' updated_by FROM dual
) src ON (tgt.sample_key = src.sample_key)
WHEN MATCHED THEN UPDATE SET tgt.item_name = src.item_name, tgt.category_code = src.category_code, tgt.status_code = src.status_code, tgt.searchable_text = src.searchable_text, tgt.owner_reference = src.owner_reference, tgt.sort_order = src.sort_order, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by) VALUES (src.sample_key, src.item_name, src.category_code, src.status_code, src.searchable_text, src.owner_reference, src.sort_order, src.version_no, src.created_by, src.updated_by);
