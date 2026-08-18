-- GENERATED FILE. DO NOT EDIT.
-- Source: cpf-tools/db/canonical/seed-model.json
-- Vendor: oracle
-- Role: REFERENCE_FIXTURE

MERGE INTO REF_CMN_SAMPLE_ITEM tgt
USING (SELECT 'CMN-SAMPLE-001' AS sample_key, 'CPF CMN 기본 샘플' AS item_name, 'DATABASE' AS category_code, 'ACTIVE' AS status_code, 'connection migration crud search offset slice cursor' AS searchable_text, NULL AS owner_reference, 10 AS sort_order, 0 AS version_no, 'CMN_SAMPLE' AS created_by, 'CMN_SAMPLE' AS updated_by FROM dual
UNION ALL
SELECT 'CMN-SAMPLE-002' AS sample_key, 'CPF CMN 비활성 샘플' AS item_name, 'VALIDATION' AS category_code, 'INACTIVE' AS status_code, 'validation duplicate optimistic-lock rollback' AS searchable_text, NULL AS owner_reference, 20 AS sort_order, 0 AS version_no, 'CMN_SAMPLE' AS created_by, 'CMN_SAMPLE' AS updated_by FROM dual) src
ON (tgt.sample_key=src.sample_key)
WHEN MATCHED THEN UPDATE SET tgt.item_name=src.item_name, tgt.category_code=src.category_code, tgt.status_code=src.status_code, tgt.searchable_text=src.searchable_text, tgt.owner_reference=src.owner_reference, tgt.sort_order=src.sort_order, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by) VALUES (src.sample_key, src.item_name, src.category_code, src.status_code, src.searchable_text, src.owner_reference, src.sort_order, src.version_no, src.created_by, src.updated_by);

MERGE INTO REF_CMN_SAMPLE_ITEM tgt
USING (SELECT 101 AS sample_item_id, 'CMN-TEST-101' AS sample_key, '표준 헤더 단건 조회' AS item_name, 'HEADER' AS category_code, 'ACTIVE' AS status_code, 'header single query' AS searchable_text, 'CPF-TEST-101' AS owner_reference, 101 AS sort_order, 0 AS version_no, 'CMN_TEST' AS created_by, 'CMN_TEST' AS updated_by FROM dual
UNION ALL
SELECT 102 AS sample_item_id, 'CMN-TEST-102' AS sample_key, '거래 로그 목록 조회' AS item_name, 'LOG' AS category_code, 'ACTIVE' AS status_code, 'transaction log list' AS searchable_text, 'CPF-TEST-102' AS owner_reference, 102 AS sort_order, 0 AS version_no, 'CMN_TEST' AS created_by, 'CMN_TEST' AS updated_by FROM dual
UNION ALL
SELECT 103 AS sample_item_id, 'CMN-TEST-103' AS sample_key, 'offset 페이징 조회' AS item_name, 'QUERY' AS category_code, 'ACTIVE' AS status_code, 'offset page' AS searchable_text, 'CPF-TEST-103' AS owner_reference, 103 AS sort_order, 0 AS version_no, 'CMN_TEST' AS created_by, 'CMN_TEST' AS updated_by FROM dual
UNION ALL
SELECT 104 AS sample_item_id, 'CMN-TEST-104' AS sample_key, 'keyset 페이징 조회' AS item_name, 'QUERY' AS category_code, 'ACTIVE' AS status_code, 'keyset cursor' AS searchable_text, 'CPF-TEST-104' AS owner_reference, 104 AS sort_order, 0 AS version_no, 'CMN_TEST' AS created_by, 'CMN_TEST' AS updated_by FROM dual
UNION ALL
SELECT 105 AS sample_item_id, 'CMN-TEST-105' AS sample_key, '검색 조건 정규화' AS item_name, 'QUERY' AS category_code, 'INACTIVE' AS status_code, 'search validation' AS searchable_text, 'CPF-TEST-105' AS owner_reference, 105 AS sort_order, 0 AS version_no, 'CMN_TEST' AS created_by, 'CMN_TEST' AS updated_by FROM dual
UNION ALL
SELECT 106 AS sample_item_id, 'CMN-TEST-106' AS sample_key, '정렬 allowlist' AS item_name, 'QUERY' AS category_code, 'ACTIVE' AS status_code, 'stable sort allowlist' AS searchable_text, 'CPF-TEST-106' AS owner_reference, 106 AS sort_order, 0 AS version_no, 'CMN_TEST' AS created_by, 'CMN_TEST' AS updated_by FROM dual
UNION ALL
SELECT 107 AS sample_item_id, 'CMN-TEST-107' AS sample_key, '낙관적 잠금 충돌' AS item_name, 'LOCK' AS category_code, 'ACTIVE' AS status_code, 'optimistic lock version' AS searchable_text, 'CPF-TEST-107' AS owner_reference, 107 AS sort_order, 0 AS version_no, 'CMN_TEST' AS created_by, 'CMN_TEST' AS updated_by FROM dual
UNION ALL
SELECT 108 AS sample_item_id, 'CMN-TEST-108' AS sample_key, 'Transaction rollback' AS item_name, 'TRANSACTION' AS category_code, 'ACTIVE' AS status_code, 'transaction rollback' AS searchable_text, 'CPF-TEST-108' AS owner_reference, 108 AS sort_order, 0 AS version_no, 'CMN_TEST' AS created_by, 'CMN_TEST' AS updated_by FROM dual) src
ON (tgt.sample_item_id=src.sample_item_id)
WHEN MATCHED THEN UPDATE SET tgt.sample_key=src.sample_key, tgt.item_name=src.item_name, tgt.category_code=src.category_code, tgt.status_code=src.status_code, tgt.searchable_text=src.searchable_text, tgt.owner_reference=src.owner_reference, tgt.sort_order=src.sort_order, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (sample_item_id, sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by) VALUES (src.sample_item_id, src.sample_key, src.item_name, src.category_code, src.status_code, src.searchable_text, src.owner_reference, src.sort_order, src.version_no, src.created_by, src.updated_by);

DELETE FROM REF_SAMPLE_ITEM WHERE sample_item_id BETWEEN 90001 AND 90008;

DELETE FROM REF_SAMPLE_ITEM WHERE sample_item_id BETWEEN 91000 AND 91999;

MERGE INTO REF_SAMPLE_ITEM tgt
USING (SELECT 90001 AS sample_item_id, 'REF-MAPPER-90001' AS sample_key, '단건 조회 샘플' AS item_name, 'SINGLE' AS category_code, 'ACTIVE' AS status_code, 'single' AS searchable_text, 'REF-90001' AS owner_reference, 90001 AS sort_order, 0 AS version_no, 'N' AS deleted_yn, 'MAPPER_TEST' AS created_by, '2026-06-01 09:00:00.000' AS created_at, 'MAPPER_TEST' AS updated_by, '2026-06-01 09:00:00.000' AS updated_at FROM dual
UNION ALL
SELECT 90002 AS sample_item_id, 'REF-MAPPER-90002' AS sample_key, '목록 조회 샘플' AS item_name, 'LIST' AS category_code, 'ACTIVE' AS status_code, 'list' AS searchable_text, 'REF-90002' AS owner_reference, 90002 AS sort_order, 0 AS version_no, 'N' AS deleted_yn, 'MAPPER_TEST' AS created_by, '2026-06-02 09:00:00.000' AS created_at, 'MAPPER_TEST' AS updated_by, '2026-06-02 09:00:00.000' AS updated_at FROM dual
UNION ALL
SELECT 90003 AS sample_item_id, 'REF-MAPPER-90003' AS sample_key, '검색 조회 샘플' AS item_name, 'SEARCH' AS category_code, 'ACTIVE' AS status_code, 'search' AS searchable_text, 'REF-90003' AS owner_reference, 90003 AS sort_order, 0 AS version_no, 'N' AS deleted_yn, 'MAPPER_TEST' AS created_by, '2026-06-03 09:00:00.000' AS created_at, 'MAPPER_TEST' AS updated_by, '2026-06-03 09:00:00.000' AS updated_at FROM dual
UNION ALL
SELECT 90004 AS sample_item_id, 'REF-MAPPER-90004' AS sample_key, '정렬 조회 샘플' AS item_name, 'SORT' AS category_code, 'ACTIVE' AS status_code, 'sort' AS searchable_text, 'REF-90004' AS owner_reference, 90004 AS sort_order, 0 AS version_no, 'N' AS deleted_yn, 'MAPPER_TEST' AS created_by, '2026-06-04 09:00:00.000' AS created_at, 'MAPPER_TEST' AS updated_by, '2026-06-04 09:00:00.000' AS updated_at FROM dual
UNION ALL
SELECT 90005 AS sample_item_id, 'REF-MAPPER-90005' AS sample_key, '페이지 조회 샘플' AS item_name, 'PAGE' AS category_code, 'ACTIVE' AS status_code, 'page' AS searchable_text, 'REF-90005' AS owner_reference, 90005 AS sort_order, 0 AS version_no, 'N' AS deleted_yn, 'MAPPER_TEST' AS created_by, '2026-06-05 09:00:00.000' AS created_at, 'MAPPER_TEST' AS updated_by, '2026-06-05 09:00:00.000' AS updated_at FROM dual
UNION ALL
SELECT 90006 AS sample_item_id, 'REF-MAPPER-90006' AS sample_key, '비활성 조회 샘플' AS item_name, 'LIST' AS category_code, 'INACTIVE' AS status_code, 'inactive' AS searchable_text, 'REF-90006' AS owner_reference, 90006 AS sort_order, 0 AS version_no, 'N' AS deleted_yn, 'MAPPER_TEST' AS created_by, '2026-06-06 09:00:00.000' AS created_at, 'MAPPER_TEST' AS updated_by, '2026-06-06 09:00:00.000' AS updated_at FROM dual
UNION ALL
SELECT 90007 AS sample_item_id, 'REF-MAPPER-90007' AS sample_key, 'Validation 조회 샘플' AS item_name, 'VALIDATION' AS category_code, 'INACTIVE' AS status_code, 'validation' AS searchable_text, 'REF-90007' AS owner_reference, 90007 AS sort_order, 0 AS version_no, 'N' AS deleted_yn, 'MAPPER_TEST' AS created_by, '2026-06-07 09:00:00.000' AS created_at, 'MAPPER_TEST' AS updated_by, '2026-06-07 09:00:00.000' AS updated_at FROM dual
UNION ALL
SELECT 90008 AS sample_item_id, 'REF-MAPPER-90008' AS sample_key, 'Keyset 조회 샘플' AS item_name, 'KEYSET' AS category_code, 'ACTIVE' AS status_code, 'keyset' AS searchable_text, 'REF-90008' AS owner_reference, 90008 AS sort_order, 0 AS version_no, 'N' AS deleted_yn, 'MAPPER_TEST' AS created_by, '2026-06-08 09:00:00.000' AS created_at, 'MAPPER_TEST' AS updated_by, '2026-06-08 09:00:00.000' AS updated_at FROM dual) src
ON (tgt.sample_item_id=src.sample_item_id)
WHEN MATCHED THEN UPDATE SET tgt.sample_key=src.sample_key, tgt.item_name=src.item_name, tgt.category_code=src.category_code, tgt.status_code=src.status_code, tgt.searchable_text=src.searchable_text, tgt.owner_reference=src.owner_reference, tgt.sort_order=src.sort_order, tgt.updated_by=src.updated_by, tgt.updated_at=src.updated_at
WHEN NOT MATCHED THEN INSERT (sample_item_id, sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, deleted_yn, created_by, created_at, updated_by, updated_at) VALUES (src.sample_item_id, src.sample_key, src.item_name, src.category_code, src.status_code, src.searchable_text, src.owner_reference, src.sort_order, src.version_no, src.deleted_yn, src.created_by, src.created_at, src.updated_by, src.updated_at);

DELETE FROM REF_CENTER_CUT_SAMPLE_RESULT WHERE center_cut_job_id = 'CPF_REF_CENTER_CUT_SAMPLE_JOB';

MERGE INTO REF_CENTER_CUT_SAMPLE_TARGET tgt
USING (SELECT 'REF-CENTER-CUT-001' AS target_id, 'CPF_REF_CENTER_CUT_SAMPLE_JOB' AS center_cut_job_id, 'REF-ORDER-20260702-001' AS business_key, '2026-07-02' AS business_date, '{"amount":1000,"forceFail":false}' AS target_payload, 'READY' AS status_code, 0 AS retry_count, '20260702110000000REFlocal010000001' AS transaction_id, 'SEG-REF-CENTER-ROOT' AS parent_segment_id, NULL AS transaction_segment_id, NULL AS started_at, NULL AS completed_at, NULL AS last_error_message, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'REF-CENTER-CUT-002' AS target_id, 'CPF_REF_CENTER_CUT_SAMPLE_JOB' AS center_cut_job_id, 'REF-ORDER-20260702-002' AS business_key, '2026-07-02' AS business_date, '{"amount":2000,"forceFail":false}' AS target_payload, 'READY' AS status_code, 0 AS retry_count, '20260702110000000REFlocal010000001' AS transaction_id, 'SEG-REF-CENTER-ROOT' AS parent_segment_id, NULL AS transaction_segment_id, NULL AS started_at, NULL AS completed_at, NULL AS last_error_message, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'REF-CENTER-CUT-003' AS target_id, 'CPF_REF_CENTER_CUT_SAMPLE_JOB' AS center_cut_job_id, 'REF-ORDER-20260702-003' AS business_key, '2026-07-02' AS business_date, '{"amount":3000,"forceFail":true}' AS target_payload, 'READY' AS status_code, 0 AS retry_count, '20260702110000000REFlocal010000001' AS transaction_id, 'SEG-REF-CENTER-ROOT' AS parent_segment_id, NULL AS transaction_segment_id, NULL AS started_at, NULL AS completed_at, NULL AS last_error_message, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
UNION ALL
SELECT 'REF-CENTER-CUT-004' AS target_id, 'CPF_REF_CENTER_CUT_SAMPLE_JOB' AS center_cut_job_id, 'REF-ORDER-20260702-004' AS business_key, '2026-07-02' AS business_date, '{"amount":4000,"forceFail":false}' AS target_payload, 'READY' AS status_code, 0 AS retry_count, '20260702110000000REFlocal010000001' AS transaction_id, 'SEG-REF-CENTER-ROOT' AS parent_segment_id, NULL AS transaction_segment_id, NULL AS started_at, NULL AS completed_at, NULL AS last_error_message, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.target_id=src.target_id)
WHEN MATCHED THEN UPDATE SET tgt.target_payload=src.target_payload, tgt.status_code=src.status_code, tgt.retry_count=src.retry_count, tgt.transaction_id=src.transaction_id, tgt.parent_segment_id=src.parent_segment_id, tgt.transaction_segment_id=src.transaction_segment_id, tgt.started_at=src.started_at, tgt.completed_at=src.completed_at, tgt.last_error_message=src.last_error_message, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (target_id, center_cut_job_id, business_key, business_date, target_payload, status_code, retry_count, transaction_id, parent_segment_id, transaction_segment_id, started_at, completed_at, last_error_message, use_yn, created_by, updated_by) VALUES (src.target_id, src.center_cut_job_id, src.business_key, src.business_date, src.target_payload, src.status_code, src.retry_count, src.transaction_id, src.parent_segment_id, src.transaction_segment_id, src.started_at, src.completed_at, src.last_error_message, src.use_yn, src.created_by, src.updated_by);
