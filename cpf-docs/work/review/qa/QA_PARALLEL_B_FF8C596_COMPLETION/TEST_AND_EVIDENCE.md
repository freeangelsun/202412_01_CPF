# Test and Evidence

## 실행 결과

- 집중 회귀: **23/23 PASS** — `cpf-docs/evidence/qa/QA_PARALLEL_B_FF8C596_COMPLETION/full_regression.log`
- Python compileall: **PASS** — `cpf-docs/evidence/qa/QA_PARALLEL_B_FF8C596_COMPLETION/python_compileall.log`
- Source Manifest: **12/12 hash PASS** — `cpf-docs/evidence/qa/QA_PARALLEL_B_FF8C596_COMPLETION/source_manifest_validation.json`
- Package-local Defect/Scenario ledger: **5/10 rows PASS** — `cpf-docs/evidence/qa/QA_PARALLEL_B_FF8C596_COMPLETION/package_local_ledger_validation.json`
- QA-B Query inventory: **9,962 unique, blank 0, duplicate 0; 모두 미검수** — `cpf-docs/evidence/qa/QA_PARALLEL_B_FF8C596_COMPLETION/query_count.json`
- Full ledger Builder/Validator/Partition validator: canonical data 부재 시 모두 **expected fail-closed**

## 미실행 또는 제품 통과 Evidence로 인정하지 않은 범위

- 19 Requirement Parts, 7 Scenario Parts, 2 Execution Parts의 byte/hash 전체 조립
- QA-B 9,962 Requirement와 연결 Scenario의 개별 Source·Consumer·Runtime 검수
- Java 25 root `clean test assemble`
- 실제 MariaDB non-empty rollback → export/reconcile → retry/reapply와 PostgreSQL/Oracle lifecycle
- ADM/BZA Browser E2E, Spring multi-instance kill/restart, GitHub required checks
- 개발GPT·Codex 교차검토 및 독립 QA 재검수

부분 회귀 Evidence는 제품 QA 통과로 승격하지 않았습니다.
