-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=55_cmn_seed_data.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=cmnDB
-- CPF_USE_LOGICAL_DATABASE=cmnDB
MERGE INTO cmn_sample_item tgt
USING (
  SELECT 'CMN-SAMPLE-001' AS sample_key, 'CPF CMN 기본 샘플' AS item_name, 'DATABASE' AS category_code, 'ACTIVE' AS status_code, 'connection migration crud search offset slice cursor' AS searchable_text, NULL AS owner_reference, 10 AS sort_order, 0 AS version_no, 'CMN_SAMPLE' AS created_by, 'CMN_SAMPLE' AS updated_by FROM dual
  UNION ALL
  SELECT 'CMN-SAMPLE-002' AS sample_key, 'CPF CMN 비활성 샘플' AS item_name, 'VALIDATION' AS category_code, 'INACTIVE' AS status_code, 'validation duplicate optimistic-lock rollback' AS searchable_text, NULL AS owner_reference, 20 AS sort_order, 0 AS version_no, 'CMN_SAMPLE' AS created_by, 'CMN_SAMPLE' AS updated_by FROM dual
) src
ON (tgt.sample_key = src.sample_key)
WHEN MATCHED THEN UPDATE SET
  tgt.item_name = src.item_name,
  tgt.category_code = src.category_code,
  tgt.status_code = src.status_code,
  tgt.searchable_text = src.searchable_text,
  tgt.owner_reference = src.owner_reference,
  tgt.sort_order = src.sort_order,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by)
VALUES (src.sample_key, src.item_name, src.category_code, src.status_code, src.searchable_text, src.owner_reference, src.sort_order, src.version_no, src.created_by, src.updated_by);
