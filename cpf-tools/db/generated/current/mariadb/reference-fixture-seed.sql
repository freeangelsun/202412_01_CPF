-- GENERATED FILE. DO NOT EDIT.
-- Source: cpf-tools/db/canonical/seed-model.json
-- Vendor: mariadb
-- Role: REFERENCE_FIXTURE

INSERT INTO REF_CMN_SAMPLE_ITEM (sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by)
VALUES ('CMN-SAMPLE-001', 'CPF CMN 기본 샘플', 'DATABASE', 'ACTIVE', 'connection migration crud search offset slice cursor', NULL, 10, 0, 'CMN_SAMPLE', 'CMN_SAMPLE'),
    ('CMN-SAMPLE-002', 'CPF CMN 비활성 샘플', 'VALIDATION', 'INACTIVE', 'validation duplicate optimistic-lock rollback', NULL, 20, 0, 'CMN_SAMPLE', 'CMN_SAMPLE')
ON DUPLICATE KEY UPDATE item_name=VALUES(item_name), category_code=VALUES(category_code), status_code=VALUES(status_code), searchable_text=VALUES(searchable_text), owner_reference=VALUES(owner_reference), sort_order=VALUES(sort_order), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO REF_CMN_SAMPLE_ITEM (sample_item_id, sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by)
VALUES (101, 'CMN-TEST-101', '표준 헤더 단건 조회', 'HEADER', 'ACTIVE', 'header single query', 'CPF-TEST-101', 101, 0, 'CMN_TEST', 'CMN_TEST'),
    (102, 'CMN-TEST-102', '거래 로그 목록 조회', 'LOG', 'ACTIVE', 'transaction log list', 'CPF-TEST-102', 102, 0, 'CMN_TEST', 'CMN_TEST'),
    (103, 'CMN-TEST-103', 'offset 페이징 조회', 'QUERY', 'ACTIVE', 'offset page', 'CPF-TEST-103', 103, 0, 'CMN_TEST', 'CMN_TEST'),
    (104, 'CMN-TEST-104', 'keyset 페이징 조회', 'QUERY', 'ACTIVE', 'keyset cursor', 'CPF-TEST-104', 104, 0, 'CMN_TEST', 'CMN_TEST'),
    (105, 'CMN-TEST-105', '검색 조건 정규화', 'QUERY', 'INACTIVE', 'search validation', 'CPF-TEST-105', 105, 0, 'CMN_TEST', 'CMN_TEST'),
    (106, 'CMN-TEST-106', '정렬 allowlist', 'QUERY', 'ACTIVE', 'stable sort allowlist', 'CPF-TEST-106', 106, 0, 'CMN_TEST', 'CMN_TEST'),
    (107, 'CMN-TEST-107', '낙관적 잠금 충돌', 'LOCK', 'ACTIVE', 'optimistic lock version', 'CPF-TEST-107', 107, 0, 'CMN_TEST', 'CMN_TEST'),
    (108, 'CMN-TEST-108', 'Transaction rollback', 'TRANSACTION', 'ACTIVE', 'transaction rollback', 'CPF-TEST-108', 108, 0, 'CMN_TEST', 'CMN_TEST')
ON DUPLICATE KEY UPDATE sample_key=VALUES(sample_key), item_name=VALUES(item_name), category_code=VALUES(category_code), status_code=VALUES(status_code), searchable_text=VALUES(searchable_text), owner_reference=VALUES(owner_reference), sort_order=VALUES(sort_order), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);

DELETE FROM REF_SAMPLE_ITEM WHERE sample_item_id BETWEEN 90001 AND 90008;

DELETE FROM REF_SAMPLE_ITEM WHERE sample_item_id BETWEEN 91000 AND 91999;

INSERT INTO REF_SAMPLE_ITEM (sample_item_id, sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, deleted_yn, created_by, created_at, updated_by, updated_at)
VALUES (90001, 'REF-MAPPER-90001', '단건 조회 샘플', 'SINGLE', 'ACTIVE', 'single', 'REF-90001', 90001, 0, 'N', 'MAPPER_TEST', '2026-06-01 09:00:00.000', 'MAPPER_TEST', '2026-06-01 09:00:00.000'),
    (90002, 'REF-MAPPER-90002', '목록 조회 샘플', 'LIST', 'ACTIVE', 'list', 'REF-90002', 90002, 0, 'N', 'MAPPER_TEST', '2026-06-02 09:00:00.000', 'MAPPER_TEST', '2026-06-02 09:00:00.000'),
    (90003, 'REF-MAPPER-90003', '검색 조회 샘플', 'SEARCH', 'ACTIVE', 'search', 'REF-90003', 90003, 0, 'N', 'MAPPER_TEST', '2026-06-03 09:00:00.000', 'MAPPER_TEST', '2026-06-03 09:00:00.000'),
    (90004, 'REF-MAPPER-90004', '정렬 조회 샘플', 'SORT', 'ACTIVE', 'sort', 'REF-90004', 90004, 0, 'N', 'MAPPER_TEST', '2026-06-04 09:00:00.000', 'MAPPER_TEST', '2026-06-04 09:00:00.000'),
    (90005, 'REF-MAPPER-90005', '페이지 조회 샘플', 'PAGE', 'ACTIVE', 'page', 'REF-90005', 90005, 0, 'N', 'MAPPER_TEST', '2026-06-05 09:00:00.000', 'MAPPER_TEST', '2026-06-05 09:00:00.000'),
    (90006, 'REF-MAPPER-90006', '비활성 조회 샘플', 'LIST', 'INACTIVE', 'inactive', 'REF-90006', 90006, 0, 'N', 'MAPPER_TEST', '2026-06-06 09:00:00.000', 'MAPPER_TEST', '2026-06-06 09:00:00.000'),
    (90007, 'REF-MAPPER-90007', 'Validation 조회 샘플', 'VALIDATION', 'INACTIVE', 'validation', 'REF-90007', 90007, 0, 'N', 'MAPPER_TEST', '2026-06-07 09:00:00.000', 'MAPPER_TEST', '2026-06-07 09:00:00.000'),
    (90008, 'REF-MAPPER-90008', 'Keyset 조회 샘플', 'KEYSET', 'ACTIVE', 'keyset', 'REF-90008', 90008, 0, 'N', 'MAPPER_TEST', '2026-06-08 09:00:00.000', 'MAPPER_TEST', '2026-06-08 09:00:00.000')
ON DUPLICATE KEY UPDATE sample_key=VALUES(sample_key), item_name=VALUES(item_name), category_code=VALUES(category_code), status_code=VALUES(status_code), searchable_text=VALUES(searchable_text), owner_reference=VALUES(owner_reference), sort_order=VALUES(sort_order), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

DELETE FROM REF_CENTER_CUT_SAMPLE_RESULT WHERE center_cut_job_id = 'CPF_REF_CENTER_CUT_SAMPLE_JOB';

INSERT INTO REF_CENTER_CUT_SAMPLE_TARGET (target_id, center_cut_job_id, business_key, business_date, target_payload, status_code, retry_count, transaction_id, parent_segment_id, transaction_segment_id, started_at, completed_at, last_error_message, use_yn, created_by, updated_by)
VALUES ('REF-CENTER-CUT-001', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-001', '2026-07-02', '{"amount":1000,"forceFail":false}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-002', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-002', '2026-07-02', '{"amount":2000,"forceFail":false}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-003', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-003', '2026-07-02', '{"amount":3000,"forceFail":true}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-004', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-004', '2026-07-02', '{"amount":4000,"forceFail":false}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE target_payload=VALUES(target_payload), status_code=VALUES(status_code), retry_count=VALUES(retry_count), transaction_id=VALUES(transaction_id), parent_segment_id=VALUES(parent_segment_id), transaction_segment_id=VALUES(transaction_segment_id), started_at=VALUES(started_at), completed_at=VALUES(completed_at), last_error_message=VALUES(last_error_message), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
