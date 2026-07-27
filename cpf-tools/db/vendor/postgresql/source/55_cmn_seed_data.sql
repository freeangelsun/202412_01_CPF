-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=55_cmn_seed_data.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=cmnDB
-- CPF_USE_LOGICAL_DATABASE=cmnDB
MERGE INTO cmn_sample_item tgt
USING (VALUES
  ('CMN-SAMPLE-001', 'CPF CMN 기본 샘플', 'DATABASE', 'ACTIVE', 'connection migration crud search offset slice cursor', NULL, 10, 0, 'CMN_SAMPLE', 'CMN_SAMPLE'),
  ('CMN-SAMPLE-002', 'CPF CMN 비활성 샘플', 'VALIDATION', 'INACTIVE', 'validation duplicate optimistic-lock rollback', NULL, 20, 0, 'CMN_SAMPLE', 'CMN_SAMPLE')
) AS src(sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by)
ON (tgt.sample_key = src.sample_key)
WHEN MATCHED THEN UPDATE SET
  tgt.item_name = src.item_name,
  tgt.category_code = src.category_code,
  tgt.status_code = src.status_code,
  tgt.searchable_text = src.searchable_text,
  tgt.owner_reference = src.owner_reference,
  tgt.sort_order = src.sort_order,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by)
VALUES (src.sample_key, src.item_name, src.category_code, src.status_code, src.searchable_text, src.owner_reference, src.sort_order, src.version_no, src.created_by, src.updated_by);
