# CPF QA-6F R2 Requirement-by-Requirement Review Index

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- exact baseline SHA: `f97655c1299936a1101bc3ec10239265ec3b502e`
- QA Partition: `QA-6F`
- 실행순서: `04-00025450` ~ `13-00030542`
- Requirement ID: `CPF-FR-025450` ~ `CPF-FR-030542`
- 단일 원장: `cpf-docs/work/review/qa/QA_PARALLEL_6WAY_QA-6F_f97655c_R2/REQUIREMENT_STATUS.csv`
- Git 쓰기: 수행하지 않음

## 전수 처리 결과

- Requirement: **5,093건**, QA-6F-R2 행별 검수표시 **5,093건**
- Scenario: **5,093건**, Requirement 1:1 링크
- Feature/Work Package 검수 단위: **509개** (고유 Feature명 508개)
- QA 통과: **0건**
- QA 미통과: **61건**
- 미검증: **5,032건**
- 중복 Requirement/Scenario: **0/0**
- Unlinked Scenario: **0**

R2는 R1의 `Feature 1개 Finding → 10개 Requirement 전부 미통과` 확장을 폐기했다. 각 행의
`function_type` Acceptance를 별도로 대조해 실제 위반이 확인된 축만 미통과로 유지했고,
Runtime·DB·Browser·다중 인스턴스·Fault·exact-SHA Evidence가 없는 행은 미검증으로 보존했다.

## 중요한 판정 정정

- `Data Lineage`: 계약, bounded fallback, `CpfServiceCallEngine` Consumer가 존재한다. 제품 영속 Backend·Bean wiring·운영·다중 인스턴스 Runtime은 미검증이다.
- `Data Reconciliation`: Service Call terminal UNKNOWN 등록, JDBC ledger, lease/row_version fencing, scheduled probe/defer/manual resolve, 감사사유·masking Source와 Unit 후보가 존재한다. 10건 일괄 미통과를 전부 미검증으로 정정했다.
- Seed QA 직접보완: 정적·Unit 회귀 12/12 PASS. QA 자기보완이므로 개발GPT·Codex 교차검토와 공식 3 Vendor 실제 DB 재검수 전 PASS로 전환하지 않았다.

## 현재 미통과 축

- **Seed Version**: 3건 — COMPATIBILITY, TEST, EVIDENCE
- **Seed Idempotency**: 10건 — SPEC, IMPLEMENT, STATE_DATA, SECURITY, FAILURE, UNKNOWN_RECOVERY, OPERATIONS, COMPATIBILITY, TEST, EVIDENCE
- **Backup Encryption**: 4건 — IMPLEMENT, STATE_DATA, SECURITY, TEST
- **Backup Retention**: 3건 — IMPLEMENT, STATE_DATA, OPERATIONS
- **Restore Validation**: 3건 — IMPLEMENT, FAILURE, TEST
- **Point-in-time Recovery**: 3건 — IMPLEMENT, STATE_DATA, COMPATIBILITY
- **Cross-region Backup**: 4건 — IMPLEMENT, STATE_DATA, UNKNOWN_RECOVERY, COMPATIBILITY
- **Data Retention**: 2건 — IMPLEMENT, OPERATIONS
- **Data Purge**: 5건 — IMPLEMENT, STATE_DATA, SECURITY, UNKNOWN_RECOVERY, OPERATIONS
- **Legal Hold**: 4건 — IMPLEMENT, STATE_DATA, SECURITY, OPERATIONS
- **Archive**: 4건 — IMPLEMENT, FAILURE, UNKNOWN_RECOVERY, OPERATIONS
- **Data Quality Rule**: 4건 — IMPLEMENT, STATE_DATA, FAILURE, OPERATIONS
- **Test Data Masking**: 4건 — IMPLEMENT, SECURITY, COMPATIBILITY, TEST
- **Synthetic Test Data**: 4건 — IMPLEMENT, STATE_DATA, SECURITY, TEST
- **Production Data Use Prohibition**: 4건 — IMPLEMENT, SECURITY, FAILURE, TEST

## 정본 파일

- `REQUIREMENT_STATUS.csv`: 5,093개 Requirement 개별 판정
- `SCENARIO_STATUS.csv`: 5,093개 Scenario 개별 상태
- `REQUIREMENT_AXIS_EVIDENCE.csv`: Requirement별 Source·판정·요구 Evidence
- `FEATURE_QA_MATRIX.csv`: 행별 결과를 집계한 Feature 요약
- `SOURCE_TRACE_SUPPLEMENT.csv`: Lineage·Reconciliation·Seed 실제 수직 경로
- `REQUIREMENT_REVIEW_INTEGRITY.json`: 개수·중복·링크·상태 검증
- `TEST_AND_EVIDENCE.md`: 수행·미수행 검증
- `QA_REWORK_REQUEST.md`: 실제 미통과 Requirement만의 재개발 요청
