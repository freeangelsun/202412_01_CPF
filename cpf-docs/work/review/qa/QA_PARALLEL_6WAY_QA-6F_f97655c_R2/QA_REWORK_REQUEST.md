# QA REWORK REQUEST — QA-6F R2

- 기준 SHA: `f97655c1299936a1101bc3ec10239265ec3b502e`
- 실제 미통과 Requirement: **61건**
- 원칙: 미검증 행은 재개발 요청으로 자동 전환하지 않는다.

## Seed Version

### CPF-FR-025450 — COMPATIBILITY
- 미통과 사유: QA 직접보완 Source는 12/12 정적·Unit 회귀를 통과했으나 QA 자기보완은 즉시 PASS할 수 없고 개발GPT/Codex 교차검토 및 공식 3 Vendor 실제 DB Runtime 재검수가 남아 있다.
- 요청 내용: Overlay 적용 후 변경 Source를 독립 검토하고 공식 3 Vendor install→seed 재적용→verify를 exact HEAD에서 실행한다.
- 대상 Source 후보: `cpf-tools/config/database-source-plan.json;cpf-tools/db/canonical/seed-model.json;cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql;cpf-tools/db/vendor/mariadb/source/00_product_seed.sql;cpf-tools/db/vendor/oracle/seed/00_product_seed.sql;cpf-tools/db/vendor/oracle/source/00_product_seed.sql;cpf-tools/db/vendor/postgresql/seed/00_product_seed.sql;cpf-tools/db/vendor/postgresql/source/00_product_seed.sql;cpf-tools/scripts/invoke-official-db-vendor-sql.ps1;cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py;cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py;cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py`
- 재실행: `python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py`
- 성공 기대: 12/12 PASS 후 MariaDB/PostgreSQL/Oracle 실제 DB lifecycle PASS, secret 원문 노출 0건
- 실패 기준: Test 실패, bundle 누락/중복, retry-unsafe mutation, credential process argument 노출, 실제 DB lifecycle 실패
- 요구 Evidence: exact HEAD·clean tree·환경버전·명령·exit code·3 Vendor pre/post DB 상태·sanitized logs·artifact SHA-256

### CPF-FR-025451 — TEST
- 미통과 사유: QA 직접보완 Source는 12/12 정적·Unit 회귀를 통과했으나 QA 자기보완은 즉시 PASS할 수 없고 개발GPT/Codex 교차검토 및 공식 3 Vendor 실제 DB Runtime 재검수가 남아 있다.
- 요청 내용: Overlay 적용 후 변경 Source를 독립 검토하고 공식 3 Vendor install→seed 재적용→verify를 exact HEAD에서 실행한다.
- 대상 Source 후보: `cpf-tools/config/database-source-plan.json;cpf-tools/db/canonical/seed-model.json;cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql;cpf-tools/db/vendor/mariadb/source/00_product_seed.sql;cpf-tools/db/vendor/oracle/seed/00_product_seed.sql;cpf-tools/db/vendor/oracle/source/00_product_seed.sql;cpf-tools/db/vendor/postgresql/seed/00_product_seed.sql;cpf-tools/db/vendor/postgresql/source/00_product_seed.sql;cpf-tools/scripts/invoke-official-db-vendor-sql.ps1;cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py;cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py;cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py`
- 재실행: `python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py`
- 성공 기대: 12/12 PASS 후 MariaDB/PostgreSQL/Oracle 실제 DB lifecycle PASS, secret 원문 노출 0건
- 실패 기준: Test 실패, bundle 누락/중복, retry-unsafe mutation, credential process argument 노출, 실제 DB lifecycle 실패
- 요구 Evidence: exact HEAD·clean tree·환경버전·명령·exit code·3 Vendor pre/post DB 상태·sanitized logs·artifact SHA-256

### CPF-FR-025452 — EVIDENCE
- 미통과 사유: QA 직접보완 Source는 12/12 정적·Unit 회귀를 통과했으나 QA 자기보완은 즉시 PASS할 수 없고 개발GPT/Codex 교차검토 및 공식 3 Vendor 실제 DB Runtime 재검수가 남아 있다.
- 요청 내용: Overlay 적용 후 변경 Source를 독립 검토하고 공식 3 Vendor install→seed 재적용→verify를 exact HEAD에서 실행한다.
- 대상 Source 후보: `cpf-tools/config/database-source-plan.json;cpf-tools/db/canonical/seed-model.json;cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql;cpf-tools/db/vendor/mariadb/source/00_product_seed.sql;cpf-tools/db/vendor/oracle/seed/00_product_seed.sql;cpf-tools/db/vendor/oracle/source/00_product_seed.sql;cpf-tools/db/vendor/postgresql/seed/00_product_seed.sql;cpf-tools/db/vendor/postgresql/source/00_product_seed.sql;cpf-tools/scripts/invoke-official-db-vendor-sql.ps1;cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py;cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py;cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py`
- 재실행: `python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py`
- 성공 기대: 12/12 PASS 후 MariaDB/PostgreSQL/Oracle 실제 DB lifecycle PASS, secret 원문 노출 0건
- 실패 기준: Test 실패, bundle 누락/중복, retry-unsafe mutation, credential process argument 노출, 실제 DB lifecycle 실패
- 요구 Evidence: exact HEAD·clean tree·환경버전·명령·exit code·3 Vendor pre/post DB 상태·sanitized logs·artifact SHA-256

## Seed Idempotency

### CPF-FR-025453 — SPEC
- 미통과 사유: QA 직접보완 Source는 12/12 정적·Unit 회귀를 통과했으나 QA 자기보완은 즉시 PASS할 수 없고 개발GPT/Codex 교차검토 및 공식 3 Vendor 실제 DB Runtime 재검수가 남아 있다.
- 요청 내용: Overlay 적용 후 변경 Source를 독립 검토하고 공식 3 Vendor install→seed 재적용→verify를 exact HEAD에서 실행한다.
- 대상 Source 후보: `cpf-tools/config/database-source-plan.json;cpf-tools/db/canonical/seed-model.json;cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql;cpf-tools/db/vendor/mariadb/source/00_product_seed.sql;cpf-tools/db/vendor/oracle/seed/00_product_seed.sql;cpf-tools/db/vendor/oracle/source/00_product_seed.sql;cpf-tools/db/vendor/postgresql/seed/00_product_seed.sql;cpf-tools/db/vendor/postgresql/source/00_product_seed.sql;cpf-tools/scripts/invoke-official-db-vendor-sql.ps1;cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py;cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py;cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py`
- 재실행: `python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py`
- 성공 기대: 12/12 PASS 후 MariaDB/PostgreSQL/Oracle 실제 DB lifecycle PASS, secret 원문 노출 0건
- 실패 기준: Test 실패, bundle 누락/중복, retry-unsafe mutation, credential process argument 노출, 실제 DB lifecycle 실패
- 요구 Evidence: exact HEAD·clean tree·환경버전·명령·exit code·3 Vendor pre/post DB 상태·sanitized logs·artifact SHA-256

### CPF-FR-025454 — IMPLEMENT
- 미통과 사유: QA 직접보완 Source는 12/12 정적·Unit 회귀를 통과했으나 QA 자기보완은 즉시 PASS할 수 없고 개발GPT/Codex 교차검토 및 공식 3 Vendor 실제 DB Runtime 재검수가 남아 있다.
- 요청 내용: Overlay 적용 후 변경 Source를 독립 검토하고 공식 3 Vendor install→seed 재적용→verify를 exact HEAD에서 실행한다.
- 대상 Source 후보: `cpf-tools/config/database-source-plan.json;cpf-tools/db/canonical/seed-model.json;cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql;cpf-tools/db/vendor/mariadb/source/00_product_seed.sql;cpf-tools/db/vendor/oracle/seed/00_product_seed.sql;cpf-tools/db/vendor/oracle/source/00_product_seed.sql;cpf-tools/db/vendor/postgresql/seed/00_product_seed.sql;cpf-tools/db/vendor/postgresql/source/00_product_seed.sql;cpf-tools/scripts/invoke-official-db-vendor-sql.ps1;cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py;cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py;cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py`
- 재실행: `python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py`
- 성공 기대: 12/12 PASS 후 MariaDB/PostgreSQL/Oracle 실제 DB lifecycle PASS, secret 원문 노출 0건
- 실패 기준: Test 실패, bundle 누락/중복, retry-unsafe mutation, credential process argument 노출, 실제 DB lifecycle 실패
- 요구 Evidence: exact HEAD·clean tree·환경버전·명령·exit code·3 Vendor pre/post DB 상태·sanitized logs·artifact SHA-256

### CPF-FR-025455 — STATE_DATA
- 미통과 사유: QA 직접보완 Source는 12/12 정적·Unit 회귀를 통과했으나 QA 자기보완은 즉시 PASS할 수 없고 개발GPT/Codex 교차검토 및 공식 3 Vendor 실제 DB Runtime 재검수가 남아 있다.
- 요청 내용: Overlay 적용 후 변경 Source를 독립 검토하고 공식 3 Vendor install→seed 재적용→verify를 exact HEAD에서 실행한다.
- 대상 Source 후보: `cpf-tools/config/database-source-plan.json;cpf-tools/db/canonical/seed-model.json;cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql;cpf-tools/db/vendor/mariadb/source/00_product_seed.sql;cpf-tools/db/vendor/oracle/seed/00_product_seed.sql;cpf-tools/db/vendor/oracle/source/00_product_seed.sql;cpf-tools/db/vendor/postgresql/seed/00_product_seed.sql;cpf-tools/db/vendor/postgresql/source/00_product_seed.sql;cpf-tools/scripts/invoke-official-db-vendor-sql.ps1;cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py;cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py;cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py`
- 재실행: `python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py`
- 성공 기대: 12/12 PASS 후 MariaDB/PostgreSQL/Oracle 실제 DB lifecycle PASS, secret 원문 노출 0건
- 실패 기준: Test 실패, bundle 누락/중복, retry-unsafe mutation, credential process argument 노출, 실제 DB lifecycle 실패
- 요구 Evidence: exact HEAD·clean tree·환경버전·명령·exit code·3 Vendor pre/post DB 상태·sanitized logs·artifact SHA-256

### CPF-FR-025456 — SECURITY
- 미통과 사유: QA 직접보완 Source는 12/12 정적·Unit 회귀를 통과했으나 QA 자기보완은 즉시 PASS할 수 없고 개발GPT/Codex 교차검토 및 공식 3 Vendor 실제 DB Runtime 재검수가 남아 있다.
- 요청 내용: Overlay 적용 후 변경 Source를 독립 검토하고 공식 3 Vendor install→seed 재적용→verify를 exact HEAD에서 실행한다.
- 대상 Source 후보: `cpf-tools/config/database-source-plan.json;cpf-tools/db/canonical/seed-model.json;cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql;cpf-tools/db/vendor/mariadb/source/00_product_seed.sql;cpf-tools/db/vendor/oracle/seed/00_product_seed.sql;cpf-tools/db/vendor/oracle/source/00_product_seed.sql;cpf-tools/db/vendor/postgresql/seed/00_product_seed.sql;cpf-tools/db/vendor/postgresql/source/00_product_seed.sql;cpf-tools/scripts/invoke-official-db-vendor-sql.ps1;cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py;cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py;cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py`
- 재실행: `python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py`
- 성공 기대: 12/12 PASS 후 MariaDB/PostgreSQL/Oracle 실제 DB lifecycle PASS, secret 원문 노출 0건
- 실패 기준: Test 실패, bundle 누락/중복, retry-unsafe mutation, credential process argument 노출, 실제 DB lifecycle 실패
- 요구 Evidence: exact HEAD·clean tree·환경버전·명령·exit code·3 Vendor pre/post DB 상태·sanitized logs·artifact SHA-256

### CPF-FR-025457 — FAILURE
- 미통과 사유: QA 직접보완 Source는 12/12 정적·Unit 회귀를 통과했으나 QA 자기보완은 즉시 PASS할 수 없고 개발GPT/Codex 교차검토 및 공식 3 Vendor 실제 DB Runtime 재검수가 남아 있다.
- 요청 내용: Overlay 적용 후 변경 Source를 독립 검토하고 공식 3 Vendor install→seed 재적용→verify를 exact HEAD에서 실행한다.
- 대상 Source 후보: `cpf-tools/config/database-source-plan.json;cpf-tools/db/canonical/seed-model.json;cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql;cpf-tools/db/vendor/mariadb/source/00_product_seed.sql;cpf-tools/db/vendor/oracle/seed/00_product_seed.sql;cpf-tools/db/vendor/oracle/source/00_product_seed.sql;cpf-tools/db/vendor/postgresql/seed/00_product_seed.sql;cpf-tools/db/vendor/postgresql/source/00_product_seed.sql;cpf-tools/scripts/invoke-official-db-vendor-sql.ps1;cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py;cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py;cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py`
- 재실행: `python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py`
- 성공 기대: 12/12 PASS 후 MariaDB/PostgreSQL/Oracle 실제 DB lifecycle PASS, secret 원문 노출 0건
- 실패 기준: Test 실패, bundle 누락/중복, retry-unsafe mutation, credential process argument 노출, 실제 DB lifecycle 실패
- 요구 Evidence: exact HEAD·clean tree·환경버전·명령·exit code·3 Vendor pre/post DB 상태·sanitized logs·artifact SHA-256

### CPF-FR-025458 — UNKNOWN_RECOVERY
- 미통과 사유: QA 직접보완 Source는 12/12 정적·Unit 회귀를 통과했으나 QA 자기보완은 즉시 PASS할 수 없고 개발GPT/Codex 교차검토 및 공식 3 Vendor 실제 DB Runtime 재검수가 남아 있다.
- 요청 내용: Overlay 적용 후 변경 Source를 독립 검토하고 공식 3 Vendor install→seed 재적용→verify를 exact HEAD에서 실행한다.
- 대상 Source 후보: `cpf-tools/config/database-source-plan.json;cpf-tools/db/canonical/seed-model.json;cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql;cpf-tools/db/vendor/mariadb/source/00_product_seed.sql;cpf-tools/db/vendor/oracle/seed/00_product_seed.sql;cpf-tools/db/vendor/oracle/source/00_product_seed.sql;cpf-tools/db/vendor/postgresql/seed/00_product_seed.sql;cpf-tools/db/vendor/postgresql/source/00_product_seed.sql;cpf-tools/scripts/invoke-official-db-vendor-sql.ps1;cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py;cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py;cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py`
- 재실행: `python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py`
- 성공 기대: 12/12 PASS 후 MariaDB/PostgreSQL/Oracle 실제 DB lifecycle PASS, secret 원문 노출 0건
- 실패 기준: Test 실패, bundle 누락/중복, retry-unsafe mutation, credential process argument 노출, 실제 DB lifecycle 실패
- 요구 Evidence: exact HEAD·clean tree·환경버전·명령·exit code·3 Vendor pre/post DB 상태·sanitized logs·artifact SHA-256

### CPF-FR-025459 — OPERATIONS
- 미통과 사유: QA 직접보완 Source는 12/12 정적·Unit 회귀를 통과했으나 QA 자기보완은 즉시 PASS할 수 없고 개발GPT/Codex 교차검토 및 공식 3 Vendor 실제 DB Runtime 재검수가 남아 있다.
- 요청 내용: Overlay 적용 후 변경 Source를 독립 검토하고 공식 3 Vendor install→seed 재적용→verify를 exact HEAD에서 실행한다.
- 대상 Source 후보: `cpf-tools/config/database-source-plan.json;cpf-tools/db/canonical/seed-model.json;cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql;cpf-tools/db/vendor/mariadb/source/00_product_seed.sql;cpf-tools/db/vendor/oracle/seed/00_product_seed.sql;cpf-tools/db/vendor/oracle/source/00_product_seed.sql;cpf-tools/db/vendor/postgresql/seed/00_product_seed.sql;cpf-tools/db/vendor/postgresql/source/00_product_seed.sql;cpf-tools/scripts/invoke-official-db-vendor-sql.ps1;cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py;cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py;cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py`
- 재실행: `python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py`
- 성공 기대: 12/12 PASS 후 MariaDB/PostgreSQL/Oracle 실제 DB lifecycle PASS, secret 원문 노출 0건
- 실패 기준: Test 실패, bundle 누락/중복, retry-unsafe mutation, credential process argument 노출, 실제 DB lifecycle 실패
- 요구 Evidence: exact HEAD·clean tree·환경버전·명령·exit code·3 Vendor pre/post DB 상태·sanitized logs·artifact SHA-256

### CPF-FR-025460 — COMPATIBILITY
- 미통과 사유: QA 직접보완 Source는 12/12 정적·Unit 회귀를 통과했으나 QA 자기보완은 즉시 PASS할 수 없고 개발GPT/Codex 교차검토 및 공식 3 Vendor 실제 DB Runtime 재검수가 남아 있다.
- 요청 내용: Overlay 적용 후 변경 Source를 독립 검토하고 공식 3 Vendor install→seed 재적용→verify를 exact HEAD에서 실행한다.
- 대상 Source 후보: `cpf-tools/config/database-source-plan.json;cpf-tools/db/canonical/seed-model.json;cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql;cpf-tools/db/vendor/mariadb/source/00_product_seed.sql;cpf-tools/db/vendor/oracle/seed/00_product_seed.sql;cpf-tools/db/vendor/oracle/source/00_product_seed.sql;cpf-tools/db/vendor/postgresql/seed/00_product_seed.sql;cpf-tools/db/vendor/postgresql/source/00_product_seed.sql;cpf-tools/scripts/invoke-official-db-vendor-sql.ps1;cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py;cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py;cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py`
- 재실행: `python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py`
- 성공 기대: 12/12 PASS 후 MariaDB/PostgreSQL/Oracle 실제 DB lifecycle PASS, secret 원문 노출 0건
- 실패 기준: Test 실패, bundle 누락/중복, retry-unsafe mutation, credential process argument 노출, 실제 DB lifecycle 실패
- 요구 Evidence: exact HEAD·clean tree·환경버전·명령·exit code·3 Vendor pre/post DB 상태·sanitized logs·artifact SHA-256

### CPF-FR-025461 — TEST
- 미통과 사유: QA 직접보완 Source는 12/12 정적·Unit 회귀를 통과했으나 QA 자기보완은 즉시 PASS할 수 없고 개발GPT/Codex 교차검토 및 공식 3 Vendor 실제 DB Runtime 재검수가 남아 있다.
- 요청 내용: Overlay 적용 후 변경 Source를 독립 검토하고 공식 3 Vendor install→seed 재적용→verify를 exact HEAD에서 실행한다.
- 대상 Source 후보: `cpf-tools/config/database-source-plan.json;cpf-tools/db/canonical/seed-model.json;cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql;cpf-tools/db/vendor/mariadb/source/00_product_seed.sql;cpf-tools/db/vendor/oracle/seed/00_product_seed.sql;cpf-tools/db/vendor/oracle/source/00_product_seed.sql;cpf-tools/db/vendor/postgresql/seed/00_product_seed.sql;cpf-tools/db/vendor/postgresql/source/00_product_seed.sql;cpf-tools/scripts/invoke-official-db-vendor-sql.ps1;cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py;cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py;cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py`
- 재실행: `python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py`
- 성공 기대: 12/12 PASS 후 MariaDB/PostgreSQL/Oracle 실제 DB lifecycle PASS, secret 원문 노출 0건
- 실패 기준: Test 실패, bundle 누락/중복, retry-unsafe mutation, credential process argument 노출, 실제 DB lifecycle 실패
- 요구 Evidence: exact HEAD·clean tree·환경버전·명령·exit code·3 Vendor pre/post DB 상태·sanitized logs·artifact SHA-256

### CPF-FR-025462 — EVIDENCE
- 미통과 사유: QA 직접보완 Source는 12/12 정적·Unit 회귀를 통과했으나 QA 자기보완은 즉시 PASS할 수 없고 개발GPT/Codex 교차검토 및 공식 3 Vendor 실제 DB Runtime 재검수가 남아 있다.
- 요청 내용: Overlay 적용 후 변경 Source를 독립 검토하고 공식 3 Vendor install→seed 재적용→verify를 exact HEAD에서 실행한다.
- 대상 Source 후보: `cpf-tools/config/database-source-plan.json;cpf-tools/db/canonical/seed-model.json;cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql;cpf-tools/db/vendor/mariadb/source/00_product_seed.sql;cpf-tools/db/vendor/oracle/seed/00_product_seed.sql;cpf-tools/db/vendor/oracle/source/00_product_seed.sql;cpf-tools/db/vendor/postgresql/seed/00_product_seed.sql;cpf-tools/db/vendor/postgresql/source/00_product_seed.sql;cpf-tools/scripts/invoke-official-db-vendor-sql.ps1;cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py;cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py;cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py`
- 재실행: `python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py`
- 성공 기대: 12/12 PASS 후 MariaDB/PostgreSQL/Oracle 실제 DB lifecycle PASS, secret 원문 노출 0건
- 실패 기준: Test 실패, bundle 누락/중복, retry-unsafe mutation, credential process argument 노출, 실제 DB lifecycle 실패
- 요구 Evidence: exact HEAD·clean tree·환경버전·명령·exit code·3 Vendor pre/post DB 상태·sanitized logs·artifact SHA-256

## Backup Encryption

### CPF-FR-025474 — IMPLEMENT
- 미통과 사유: Backup 생성 경로가 plaintext SQL artifact를 만들며 암호화 writer/reader Consumer가 연결되지 않았다.
- 요청 내용: Backup 생성 경로가 plaintext SQL artifact를 만들며 암호화 writer/reader Consumer가 연결되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1;cpf-tools/scripts/restore-cpf-database.ps1`
- 재실행: `Backup Encryption Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Backup Encryption의 제품 구현·Consumer가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: Backup 생성 경로가 plaintext SQL artifact를 만들며 암호화 writer/reader Consumer가 연결되지 않았다.
- 요구 Evidence: Backup Encryption Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025475 — STATE_DATA
- 미통과 사유: 암호화 artifact 상태·key reference·algorithm/version·rotation metadata 저장 모델이 확인되지 않았다.
- 요청 내용: 암호화 artifact 상태·key reference·algorithm/version·rotation metadata 저장 모델이 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1;cpf-tools/scripts/restore-cpf-database.ps1`
- 재실행: `Backup Encryption Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Backup Encryption의 상태·데이터가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 암호화 artifact 상태·key reference·algorithm/version·rotation metadata 저장 모델이 확인되지 않았다.
- 요구 Evidence: Backup Encryption Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025476 — SECURITY
- 미통과 사유: manifest가 containsSensitiveData=true인 plaintext backup을 기록하지만 KMS/Key Vault 연계·암호화 저장·복호화 권한 경로가 없다.
- 요청 내용: manifest가 containsSensitiveData=true인 plaintext backup을 기록하지만 KMS/Key Vault 연계·암호화 저장·복호화 권한 경로가 없다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1;cpf-tools/scripts/restore-cpf-database.ps1`
- 재실행: `Backup Encryption Source/Consumer trace + SECURITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Backup Encryption의 보안·권한가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: manifest가 containsSensitiveData=true인 plaintext backup을 기록하지만 KMS/Key Vault 연계·암호화 저장·복호화 권한 경로가 없다.
- 요구 Evidence: Backup Encryption Source/Consumer trace + SECURITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025481 — TEST
- 미통과 사유: 암호화 저장·잘못된 key·rotation·복호화·민감정보 비노출을 실행하는 제품 Test가 확인되지 않았다.
- 요청 내용: 암호화 저장·잘못된 key·rotation·복호화·민감정보 비노출을 실행하는 제품 Test가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1;cpf-tools/scripts/restore-cpf-database.ps1`
- 재실행: `Backup Encryption Source/Consumer trace + TEST positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Backup Encryption의 검증가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 암호화 저장·잘못된 key·rotation·복호화·민감정보 비노출을 실행하는 제품 Test가 확인되지 않았다.
- 요구 Evidence: Backup Encryption Source/Consumer trace + TEST positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

## Backup Retention

### CPF-FR-025484 — IMPLEMENT
- 미통과 사유: 보존기간 계산·만료 선택·삭제를 수행하는 실제 Retention Consumer/Worker가 확인되지 않았다.
- 요청 내용: 보존기간 계산·만료 선택·삭제를 수행하는 실제 Retention Consumer/Worker가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1`
- 재실행: `Backup Retention Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Backup Retention의 제품 구현·Consumer가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 보존기간 계산·만료 선택·삭제를 수행하는 실제 Retention Consumer/Worker가 확인되지 않았다.
- 요구 Evidence: Backup Retention Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025485 — STATE_DATA
- 미통과 사유: backup artifact별 retain-until·hold·deletion state와 version/idempotency ledger가 확인되지 않았다.
- 요청 내용: backup artifact별 retain-until·hold·deletion state와 version/idempotency ledger가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1`
- 재실행: `Backup Retention Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Backup Retention의 상태·데이터가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: backup artifact별 retain-until·hold·deletion state와 version/idempotency ledger가 확인되지 않았다.
- 요구 Evidence: Backup Retention Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025489 — OPERATIONS
- 미통과 사유: 보존 만료 scan·실행 결과·감사·metric·alert·재실행 운영 경로가 확인되지 않았다.
- 요청 내용: 보존 만료 scan·실행 결과·감사·metric·alert·재실행 운영 경로가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1`
- 재실행: `Backup Retention Source/Consumer trace + OPERATIONS positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Backup Retention의 운영 기능가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 보존 만료 scan·실행 결과·감사·metric·alert·재실행 운영 경로가 확인되지 않았다.
- 요구 Evidence: Backup Retention Source/Consumer trace + OPERATIONS positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

## Restore Validation

### CPF-FR-025494 — IMPLEMENT
- 미통과 사유: Restore 경로가 process exit code 중심이며 schema/data/checksum 의미 검증 Consumer가 연결되지 않았다.
- 요청 내용: Restore 경로가 process exit code 중심이며 schema/data/checksum 의미 검증 Consumer가 연결되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/restore-cpf-database.ps1;cpf-tools/scripts/run-cpf-r4-substitute-validation.py;cpf-tools/scripts/run-cpf-r4-exact-head-validation.ps1;cpf-tools/scripts/tests/test_r4_exact_head_validation_script.py;cpf-tools/scripts/tests/test_run_cpf_r4_substitute_validation.py;cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R3/VALIDATION_SUMMARY.json;cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R4/VALIDATION_SUMMARY.json;cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R3/QA_FINDING_REVALIDATION.csv;cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R4/QA_FINDING_REVALIDATION.csv`
- 재실행: `Restore Validation Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Restore Validation의 제품 구현·Consumer가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: Restore 경로가 process exit code 중심이며 schema/data/checksum 의미 검증 Consumer가 연결되지 않았다.
- 요구 Evidence: Restore Validation Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025497 — FAILURE
- 미통과 사유: 복원 process가 0으로 종료해도 누락 row·schema drift·checksum mismatch를 실패로 판정하는 검증 경로가 없다.
- 요청 내용: 복원 process가 0으로 종료해도 누락 row·schema drift·checksum mismatch를 실패로 판정하는 검증 경로가 없다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/restore-cpf-database.ps1;cpf-tools/scripts/run-cpf-r4-substitute-validation.py;cpf-tools/scripts/run-cpf-r4-exact-head-validation.ps1;cpf-tools/scripts/tests/test_r4_exact_head_validation_script.py;cpf-tools/scripts/tests/test_run_cpf_r4_substitute_validation.py;cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R3/VALIDATION_SUMMARY.json;cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R4/VALIDATION_SUMMARY.json;cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R3/QA_FINDING_REVALIDATION.csv;cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R4/QA_FINDING_REVALIDATION.csv`
- 재실행: `Restore Validation Source/Consumer trace + FAILURE positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Restore Validation의 오류·부분 실패가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 복원 process가 0으로 종료해도 누락 row·schema drift·checksum mismatch를 실패로 판정하는 검증 경로가 없다.
- 요구 Evidence: Restore Validation Source/Consumer trace + FAILURE positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025501 — TEST
- 미통과 사유: 손상 backup·부분 restore·schema/data mismatch를 주입하는 의미 검증 Test가 확인되지 않았다.
- 요청 내용: 손상 backup·부분 restore·schema/data mismatch를 주입하는 의미 검증 Test가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/restore-cpf-database.ps1;cpf-tools/scripts/run-cpf-r4-substitute-validation.py;cpf-tools/scripts/run-cpf-r4-exact-head-validation.ps1;cpf-tools/scripts/tests/test_r4_exact_head_validation_script.py;cpf-tools/scripts/tests/test_run_cpf_r4_substitute_validation.py;cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R3/VALIDATION_SUMMARY.json;cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R4/VALIDATION_SUMMARY.json;cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R3/QA_FINDING_REVALIDATION.csv;cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R4/QA_FINDING_REVALIDATION.csv`
- 재실행: `Restore Validation Source/Consumer trace + TEST positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Restore Validation의 검증가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 손상 backup·부분 restore·schema/data mismatch를 주입하는 의미 검증 Test가 확인되지 않았다.
- 요구 Evidence: Restore Validation Source/Consumer trace + TEST positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

## Point-in-time Recovery

### CPF-FR-025504 — IMPLEMENT
- 미통과 사유: MariaDB binlog/PostgreSQL WAL/Oracle archive log·SCN을 선택해 목표시점으로 복구하는 제품 Consumer가 확인되지 않았다.
- 요청 내용: MariaDB binlog/PostgreSQL WAL/Oracle archive log·SCN을 선택해 목표시점으로 복구하는 제품 Consumer가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1;cpf-tools/scripts/restore-cpf-database.ps1;cpf-admin/src/test/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlControllerEndpointTest.java;cpf-tools/verification/java21/network-runtime/stubs/com/cpf/core/common/http/CpfServiceEndpointProperties.java;cpf-tools/scripts/tests/test_runtime_handoff_scripts.py;cpf-tools/verification/frontend-api-runtime/harness.cjs;cpf-admin/frontend/src/features/batch-runtime-control/api.ts;cpf-tools/scripts/tests/test_adm_approval_runtime_harness.py;cpf-tools/verification/frontend-workflow-runtime/harness.cjs;cpf-tools/scripts/tests/test_batch_abandon_runtime_harness.py`
- 재실행: `Point-in-time Recovery Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Point-in-time Recovery의 제품 구현·Consumer가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: MariaDB binlog/PostgreSQL WAL/Oracle archive log·SCN을 선택해 목표시점으로 복구하는 제품 Consumer가 확인되지 않았다.
- 요구 Evidence: Point-in-time Recovery Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025505 — STATE_DATA
- 미통과 사유: 목표시점·recovery point·log sequence·적용 상태·재개 idempotency ledger가 확인되지 않았다.
- 요청 내용: 목표시점·recovery point·log sequence·적용 상태·재개 idempotency ledger가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1;cpf-tools/scripts/restore-cpf-database.ps1;cpf-admin/src/test/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlControllerEndpointTest.java;cpf-tools/verification/java21/network-runtime/stubs/com/cpf/core/common/http/CpfServiceEndpointProperties.java;cpf-tools/scripts/tests/test_runtime_handoff_scripts.py;cpf-tools/verification/frontend-api-runtime/harness.cjs;cpf-admin/frontend/src/features/batch-runtime-control/api.ts;cpf-tools/scripts/tests/test_adm_approval_runtime_harness.py;cpf-tools/verification/frontend-workflow-runtime/harness.cjs;cpf-tools/scripts/tests/test_batch_abandon_runtime_harness.py`
- 재실행: `Point-in-time Recovery Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Point-in-time Recovery의 상태·데이터가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 목표시점·recovery point·log sequence·적용 상태·재개 idempotency ledger가 확인되지 않았다.
- 요구 Evidence: Point-in-time Recovery Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025510 — COMPATIBILITY
- 미통과 사유: 공식 3 Vendor 각각의 PITR 설치·복구·rollback 계약과 Runtime 검증 경로가 확인되지 않았다.
- 요청 내용: 공식 3 Vendor 각각의 PITR 설치·복구·rollback 계약과 Runtime 검증 경로가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1;cpf-tools/scripts/restore-cpf-database.ps1;cpf-admin/src/test/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlControllerEndpointTest.java;cpf-tools/verification/java21/network-runtime/stubs/com/cpf/core/common/http/CpfServiceEndpointProperties.java;cpf-tools/scripts/tests/test_runtime_handoff_scripts.py;cpf-tools/verification/frontend-api-runtime/harness.cjs;cpf-admin/frontend/src/features/batch-runtime-control/api.ts;cpf-tools/scripts/tests/test_adm_approval_runtime_harness.py;cpf-tools/verification/frontend-workflow-runtime/harness.cjs;cpf-tools/scripts/tests/test_batch_abandon_runtime_harness.py`
- 재실행: `Point-in-time Recovery Source/Consumer trace + COMPATIBILITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Point-in-time Recovery의 호환·Migration가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 공식 3 Vendor 각각의 PITR 설치·복구·rollback 계약과 Runtime 검증 경로가 확인되지 않았다.
- 요구 Evidence: Point-in-time Recovery Source/Consumer trace + COMPATIBILITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

## Cross-region Backup

### CPF-FR-025514 — IMPLEMENT
- 미통과 사유: 보조 Region 복제·검증·복구를 수행하는 실제 Consumer가 확인되지 않았다.
- 요청 내용: 보조 Region 복제·검증·복구를 수행하는 실제 Consumer가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1`
- 재실행: `Cross-region Backup Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Cross-region Backup의 제품 구현·Consumer가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 보조 Region 복제·검증·복구를 수행하는 실제 Consumer가 확인되지 않았다.
- 요구 Evidence: Cross-region Backup Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025515 — STATE_DATA
- 미통과 사유: region별 replica state·RPO/RTO·copy checksum·fencing/version ledger가 확인되지 않았다.
- 요청 내용: region별 replica state·RPO/RTO·copy checksum·fencing/version ledger가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1`
- 재실행: `Cross-region Backup Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Cross-region Backup의 상태·데이터가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: region별 replica state·RPO/RTO·copy checksum·fencing/version ledger가 확인되지 않았다.
- 요구 Evidence: Cross-region Backup Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025518 — UNKNOWN_RECOVERY
- 미통과 사유: 복제 결과불명·부분 복제·failover/failback 대사·복구 경로가 확인되지 않았다.
- 요청 내용: 복제 결과불명·부분 복제·failover/failback 대사·복구 경로가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1`
- 재실행: `Cross-region Backup Source/Consumer trace + UNKNOWN_RECOVERY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Cross-region Backup의 결과 불명·복구가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 복제 결과불명·부분 복제·failover/failback 대사·복구 경로가 확인되지 않았다.
- 요구 Evidence: Cross-region Backup Source/Consumer trace + UNKNOWN_RECOVERY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025520 — COMPATIBILITY
- 미통과 사유: 분리 Region·다중 instance·혼합 버전에서의 복제/복구 Runtime 계약이 확인되지 않았다.
- 요청 내용: 분리 Region·다중 instance·혼합 버전에서의 복제/복구 Runtime 계약이 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/backup-cpf-database.ps1`
- 재실행: `Cross-region Backup Source/Consumer trace + COMPATIBILITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Cross-region Backup의 호환·Migration가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 분리 Region·다중 instance·혼합 버전에서의 복제/복구 Runtime 계약이 확인되지 않았다.
- 요구 Evidence: Cross-region Backup Source/Consumer trace + COMPATIBILITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

## Data Retention

### CPF-FR-025524 — IMPLEMENT
- 미통과 사유: retention_days 설정 후보는 있으나 Owner service에서 실제 enforcement/delete를 수행하는 Consumer가 확인되지 않았다.
- 요청 내용: retention_days 설정 후보는 있으나 Owner service에서 실제 enforcement/delete를 수행하는 Consumer가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json;cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmLogPolicyService.java`
- 재실행: `Data Retention Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Data Retention의 제품 구현·Consumer가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: retention_days 설정 후보는 있으나 Owner service에서 실제 enforcement/delete를 수행하는 Consumer가 확인되지 않았다.
- 요구 Evidence: Data Retention Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025529 — OPERATIONS
- 미통과 사유: retention scan·예외·실행 결과·감사·metric·alert 운영 경로가 확인되지 않았다.
- 요청 내용: retention scan·예외·실행 결과·감사·metric·alert 운영 경로가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json;cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmLogPolicyService.java`
- 재실행: `Data Retention Source/Consumer trace + OPERATIONS positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Data Retention의 운영 기능가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: retention scan·예외·실행 결과·감사·metric·alert 운영 경로가 확인되지 않았다.
- 요구 Evidence: Data Retention Source/Consumer trace + OPERATIONS positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

## Data Purge

### CPF-FR-025534 — IMPLEMENT
- 미통과 사유: Purge 요청을 검증·승인·실행하는 실제 Consumer가 확인되지 않았다.
- 요청 내용: Purge 요청을 검증·승인·실행하는 실제 Consumer가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Data Purge Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Data Purge의 제품 구현·Consumer가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: Purge 요청을 검증·승인·실행하는 실제 Consumer가 확인되지 않았다.
- 요구 Evidence: Data Purge Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025535 — STATE_DATA
- 미통과 사유: purge request/state/version/idempotency/result ledger가 확인되지 않았다.
- 요청 내용: purge request/state/version/idempotency/result ledger가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Data Purge Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Data Purge의 상태·데이터가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: purge request/state/version/idempotency/result ledger가 확인되지 않았다.
- 요구 Evidence: Data Purge Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025536 — SECURITY
- 미통과 사유: 대상 scope·승인·사유·감사·민감정보 보호를 강제하는 purge command 경로가 확인되지 않았다.
- 요청 내용: 대상 scope·승인·사유·감사·민감정보 보호를 강제하는 purge command 경로가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Data Purge Source/Consumer trace + SECURITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Data Purge의 보안·권한가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 대상 scope·승인·사유·감사·민감정보 보호를 강제하는 purge command 경로가 확인되지 않았다.
- 요구 Evidence: Data Purge Source/Consumer trace + SECURITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025538 — UNKNOWN_RECOVERY
- 미통과 사유: 부분 삭제·commit/응답 유실·재시도 시 중복/누락을 대사하는 복구 경로가 확인되지 않았다.
- 요청 내용: 부분 삭제·commit/응답 유실·재시도 시 중복/누락을 대사하는 복구 경로가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Data Purge Source/Consumer trace + UNKNOWN_RECOVERY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Data Purge의 결과 불명·복구가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 부분 삭제·commit/응답 유실·재시도 시 중복/누락을 대사하는 복구 경로가 확인되지 않았다.
- 요구 Evidence: Data Purge Source/Consumer trace + UNKNOWN_RECOVERY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025539 — OPERATIONS
- 미통과 사유: 검색·상세·진행률·취소/재개·감사·metric·alert 운영 기능이 확인되지 않았다.
- 요청 내용: 검색·상세·진행률·취소/재개·감사·metric·alert 운영 기능이 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Data Purge Source/Consumer trace + OPERATIONS positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Data Purge의 운영 기능가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 검색·상세·진행률·취소/재개·감사·metric·alert 운영 기능이 확인되지 않았다.
- 요구 Evidence: Data Purge Source/Consumer trace + OPERATIONS positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

## Legal Hold

### CPF-FR-025544 — IMPLEMENT
- 미통과 사유: Legal Hold 적용·해제와 Retention/Purge 우선순위를 소비하는 실제 Owner 경로가 확인되지 않았다.
- 요청 내용: Legal Hold 적용·해제와 Retention/Purge 우선순위를 소비하는 실제 Owner 경로가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Legal Hold Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Legal Hold의 제품 구현·Consumer가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: Legal Hold 적용·해제와 Retention/Purge 우선순위를 소비하는 실제 Owner 경로가 확인되지 않았다.
- 요구 Evidence: Legal Hold Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025545 — STATE_DATA
- 미통과 사유: hold 대상·근거·기간·version·해제·감사 ledger가 확인되지 않았다.
- 요청 내용: hold 대상·근거·기간·version·해제·감사 ledger가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Legal Hold Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Legal Hold의 상태·데이터가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: hold 대상·근거·기간·version·해제·감사 ledger가 확인되지 않았다.
- 요구 Evidence: Legal Hold Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025546 — SECURITY
- 미통과 사유: hold 발급/해제 권한·승인·사유·감사 강제가 확인되지 않았다.
- 요청 내용: hold 발급/해제 권한·승인·사유·감사 강제가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Legal Hold Source/Consumer trace + SECURITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Legal Hold의 보안·권한가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: hold 발급/해제 권한·승인·사유·감사 강제가 확인되지 않았다.
- 요구 Evidence: Legal Hold Source/Consumer trace + SECURITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025549 — OPERATIONS
- 미통과 사유: hold 조회·상세·충돌·만료·해제·감사 운영 기능이 확인되지 않았다.
- 요청 내용: hold 조회·상세·충돌·만료·해제·감사 운영 기능이 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Legal Hold Source/Consumer trace + OPERATIONS positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Legal Hold의 운영 기능가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: hold 조회·상세·충돌·만료·해제·감사 운영 기능이 확인되지 않았다.
- 요구 Evidence: Legal Hold Source/Consumer trace + OPERATIONS positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

## Archive

### CPF-FR-025554 — IMPLEMENT
- 미통과 사유: archive table 후보와 ZIP utility는 있으나 업무 데이터 archive writer/reader/recovery Consumer가 연결되지 않았다.
- 요청 내용: archive table 후보와 ZIP utility는 있으나 업무 데이터 archive writer/reader/recovery Consumer가 연결되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json;cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmBatchJobLogService.java;cpf-admin/frontend/src/features/batch-runtime-control/api.ts;cpf-batch/contract/src/main/java/com/cpf/batch/api/BatControlHeaders.java;cpf-batch/contract/src/main/java/com/cpf/batch/spi/BatchExecutionLedgerPort.java;cpf-batch/contract/src/main/java/com/cpf/batch/api/BatchControlState.java;cpf-tools/verification/frontend-api-runtime/adm-stubs/generated/cpf-operation-contract.ts;cpf-tools/verification/frontend-api-runtime/bza-stubs/generated/cpf-operation-contract.ts;cpf-tools/config/cpf-starter-catalog.json;cpf-tools/generator/contracts/cpf-starter-catalog.json;cpf-tools...`
- 재실행: `Archive Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Archive의 제품 구현·Consumer가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: archive table 후보와 ZIP utility는 있으나 업무 데이터 archive writer/reader/recovery Consumer가 연결되지 않았다.
- 요구 Evidence: Archive Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025557 — FAILURE
- 미통과 사유: 부분 archive·checksum mismatch·source 삭제 전후 실패를 안전하게 판정하는 제품 경로가 확인되지 않았다.
- 요청 내용: 부분 archive·checksum mismatch·source 삭제 전후 실패를 안전하게 판정하는 제품 경로가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json;cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmBatchJobLogService.java;cpf-admin/frontend/src/features/batch-runtime-control/api.ts;cpf-batch/contract/src/main/java/com/cpf/batch/api/BatControlHeaders.java;cpf-batch/contract/src/main/java/com/cpf/batch/spi/BatchExecutionLedgerPort.java;cpf-batch/contract/src/main/java/com/cpf/batch/api/BatchControlState.java;cpf-tools/verification/frontend-api-runtime/adm-stubs/generated/cpf-operation-contract.ts;cpf-tools/verification/frontend-api-runtime/bza-stubs/generated/cpf-operation-contract.ts;cpf-tools/config/cpf-starter-catalog.json;cpf-tools/generator/contracts/cpf-starter-catalog.json;cpf-tools...`
- 재실행: `Archive Source/Consumer trace + FAILURE positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Archive의 오류·부분 실패가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 부분 archive·checksum mismatch·source 삭제 전후 실패를 안전하게 판정하는 제품 경로가 확인되지 않았다.
- 요구 Evidence: Archive Source/Consumer trace + FAILURE positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025558 — UNKNOWN_RECOVERY
- 미통과 사유: archive side effect 결과불명에 대한 reconcile/rollback/resume 경로가 확인되지 않았다.
- 요청 내용: archive side effect 결과불명에 대한 reconcile/rollback/resume 경로가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json;cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmBatchJobLogService.java;cpf-admin/frontend/src/features/batch-runtime-control/api.ts;cpf-batch/contract/src/main/java/com/cpf/batch/api/BatControlHeaders.java;cpf-batch/contract/src/main/java/com/cpf/batch/spi/BatchExecutionLedgerPort.java;cpf-batch/contract/src/main/java/com/cpf/batch/api/BatchControlState.java;cpf-tools/verification/frontend-api-runtime/adm-stubs/generated/cpf-operation-contract.ts;cpf-tools/verification/frontend-api-runtime/bza-stubs/generated/cpf-operation-contract.ts;cpf-tools/config/cpf-starter-catalog.json;cpf-tools/generator/contracts/cpf-starter-catalog.json;cpf-tools...`
- 재실행: `Archive Source/Consumer trace + UNKNOWN_RECOVERY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Archive의 결과 불명·복구가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: archive side effect 결과불명에 대한 reconcile/rollback/resume 경로가 확인되지 않았다.
- 요구 Evidence: Archive Source/Consumer trace + UNKNOWN_RECOVERY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025559 — OPERATIONS
- 미통과 사유: archive job 조회·상태·재개·복원·감사·metric·alert 운영 기능이 확인되지 않았다.
- 요청 내용: archive job 조회·상태·재개·복원·감사·metric·alert 운영 기능이 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json;cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmBatchJobLogService.java;cpf-admin/frontend/src/features/batch-runtime-control/api.ts;cpf-batch/contract/src/main/java/com/cpf/batch/api/BatControlHeaders.java;cpf-batch/contract/src/main/java/com/cpf/batch/spi/BatchExecutionLedgerPort.java;cpf-batch/contract/src/main/java/com/cpf/batch/api/BatchControlState.java;cpf-tools/verification/frontend-api-runtime/adm-stubs/generated/cpf-operation-contract.ts;cpf-tools/verification/frontend-api-runtime/bza-stubs/generated/cpf-operation-contract.ts;cpf-tools/config/cpf-starter-catalog.json;cpf-tools/generator/contracts/cpf-starter-catalog.json;cpf-tools...`
- 재실행: `Archive Source/Consumer trace + OPERATIONS positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Archive의 운영 기능가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: archive job 조회·상태·재개·복원·감사·metric·alert 운영 기능이 확인되지 않았다.
- 요구 Evidence: Archive Source/Consumer trace + OPERATIONS positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

## Data Quality Rule

### CPF-FR-025574 — IMPLEMENT
- 미통과 사유: Data quality rule을 등록·실행·평가하는 실제 Consumer가 확인되지 않았다.
- 요청 내용: Data quality rule을 등록·실행·평가하는 실제 Consumer가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Data Quality Rule Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Data Quality Rule의 제품 구현·Consumer가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: Data quality rule을 등록·실행·평가하는 실제 Consumer가 확인되지 않았다.
- 요구 Evidence: Data Quality Rule Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025575 — STATE_DATA
- 미통과 사유: rule/version/result/execution/violation ledger가 확인되지 않았다.
- 요청 내용: rule/version/result/execution/violation ledger가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Data Quality Rule Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Data Quality Rule의 상태·데이터가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: rule/version/result/execution/violation ledger가 확인되지 않았다.
- 요구 Evidence: Data Quality Rule Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025577 — FAILURE
- 미통과 사유: rule error·부분 scan·dependency failure를 표준 상태로 보존하는 경로가 확인되지 않았다.
- 요청 내용: rule error·부분 scan·dependency failure를 표준 상태로 보존하는 경로가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Data Quality Rule Source/Consumer trace + FAILURE positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Data Quality Rule의 오류·부분 실패가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: rule error·부분 scan·dependency failure를 표준 상태로 보존하는 경로가 확인되지 않았다.
- 요구 Evidence: Data Quality Rule Source/Consumer trace + FAILURE positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025579 — OPERATIONS
- 미통과 사유: rule 검색·실행·위반 상세·재실행·metric·alert 운영 기능이 확인되지 않았다.
- 요청 내용: rule 검색·실행·위반 상세·재실행·metric·alert 운영 기능이 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Data Quality Rule Source/Consumer trace + OPERATIONS positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Data Quality Rule의 운영 기능가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: rule 검색·실행·위반 상세·재실행·metric·alert 운영 기능이 확인되지 않았다.
- 요구 Evidence: Data Quality Rule Source/Consumer trace + OPERATIONS positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

## Test Data Masking

### CPF-FR-025594 — IMPLEMENT
- 미통과 사유: 운영 데이터 복제 시 컬럼·정책 기반 masking을 수행하는 DB test-data Consumer가 확인되지 않았다.
- 요청 내용: 운영 데이터 복제 시 컬럼·정책 기반 masking을 수행하는 DB test-data Consumer가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/initialize-integration-fixtures.ps1;cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Test Data Masking Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Test Data Masking의 제품 구현·Consumer가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 운영 데이터 복제 시 컬럼·정책 기반 masking을 수행하는 DB test-data Consumer가 확인되지 않았다.
- 요구 Evidence: Test Data Masking Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025596 — SECURITY
- 미통과 사유: 비가역/결정적 masking·secret 분리·재식별 방지·scope 통제가 확인되지 않았다.
- 요청 내용: 비가역/결정적 masking·secret 분리·재식별 방지·scope 통제가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/initialize-integration-fixtures.ps1;cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Test Data Masking Source/Consumer trace + SECURITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Test Data Masking의 보안·권한가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 비가역/결정적 masking·secret 분리·재식별 방지·scope 통제가 확인되지 않았다.
- 요구 Evidence: Test Data Masking Source/Consumer trace + SECURITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025600 — COMPATIBILITY
- 미통과 사유: Oracle/PostgreSQL/MariaDB의 datatype별 masking parity와 upgrade/rollback 검증이 확인되지 않았다.
- 요청 내용: Oracle/PostgreSQL/MariaDB의 datatype별 masking parity와 upgrade/rollback 검증이 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/initialize-integration-fixtures.ps1;cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Test Data Masking Source/Consumer trace + COMPATIBILITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Test Data Masking의 호환·Migration가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: Oracle/PostgreSQL/MariaDB의 datatype별 masking parity와 upgrade/rollback 검증이 확인되지 않았다.
- 요구 Evidence: Test Data Masking Source/Consumer trace + COMPATIBILITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025601 — TEST
- 미통과 사유: 민감 필드 원문 0건·참조 무결성·결정성/비결정성 정책을 검증하는 Test가 확인되지 않았다.
- 요청 내용: 민감 필드 원문 0건·참조 무결성·결정성/비결정성 정책을 검증하는 Test가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/initialize-integration-fixtures.ps1;cpf-tools/db/canonical/platform-schema.json`
- 재실행: `Test Data Masking Source/Consumer trace + TEST positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Test Data Masking의 검증가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 민감 필드 원문 0건·참조 무결성·결정성/비결정성 정책을 검증하는 Test가 확인되지 않았다.
- 요구 Evidence: Test Data Masking Source/Consumer trace + TEST positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

## Synthetic Test Data

### CPF-FR-025604 — IMPLEMENT
- 미통과 사유: 고정 fixture 외에 schema/constraint를 소비하는 합성 데이터 Generator 경로가 확인되지 않았다.
- 요청 내용: 고정 fixture 외에 schema/constraint를 소비하는 합성 데이터 Generator 경로가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/initialize-integration-fixtures.ps1`
- 재실행: `Synthetic Test Data Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Synthetic Test Data의 제품 구현·Consumer가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 고정 fixture 외에 schema/constraint를 소비하는 합성 데이터 Generator 경로가 확인되지 않았다.
- 요구 Evidence: Synthetic Test Data Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025605 — STATE_DATA
- 미통과 사유: seed/version/재현성/cleanup/run ledger가 확인되지 않았다.
- 요청 내용: seed/version/재현성/cleanup/run ledger가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/initialize-integration-fixtures.ps1`
- 재실행: `Synthetic Test Data Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Synthetic Test Data의 상태·데이터가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: seed/version/재현성/cleanup/run ledger가 확인되지 않았다.
- 요구 Evidence: Synthetic Test Data Source/Consumer trace + STATE_DATA positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025606 — SECURITY
- 미통과 사유: 운영 원문을 입력으로 사용하지 않는다는 enforcement와 secret/PII 차단 경로가 확인되지 않았다.
- 요청 내용: 운영 원문을 입력으로 사용하지 않는다는 enforcement와 secret/PII 차단 경로가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/initialize-integration-fixtures.ps1`
- 재실행: `Synthetic Test Data Source/Consumer trace + SECURITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Synthetic Test Data의 보안·권한가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 운영 원문을 입력으로 사용하지 않는다는 enforcement와 secret/PII 차단 경로가 확인되지 않았다.
- 요구 Evidence: Synthetic Test Data Source/Consumer trace + SECURITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025611 — TEST
- 미통과 사유: 재현성·분포·FK/unique·cleanup·대용량 경계를 실행하는 Generator Test가 확인되지 않았다.
- 요청 내용: 재현성·분포·FK/unique·cleanup·대용량 경계를 실행하는 Generator Test가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/initialize-integration-fixtures.ps1`
- 재실행: `Synthetic Test Data Source/Consumer trace + TEST positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Synthetic Test Data의 검증가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 재현성·분포·FK/unique·cleanup·대용량 경계를 실행하는 Generator Test가 확인되지 않았다.
- 요구 Evidence: Synthetic Test Data Source/Consumer trace + TEST positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

## Production Data Use Prohibition

### CPF-FR-025614 — IMPLEMENT
- 미통과 사유: reset/test-data 실행 전 production endpoint/environment를 deny하는 공통 Guard Consumer가 확인되지 않았다.
- 요청 내용: reset/test-data 실행 전 production endpoint/environment를 deny하는 공통 Guard Consumer가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/reset-test-data.ps1;cpf-tools/scripts/initialize-integration-fixtures.ps1`
- 재실행: `Production Data Use Prohibition Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Production Data Use Prohibition의 제품 구현·Consumer가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: reset/test-data 실행 전 production endpoint/environment를 deny하는 공통 Guard Consumer가 확인되지 않았다.
- 요구 Evidence: Production Data Use Prohibition Source/Consumer trace + IMPLEMENT positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025616 — SECURITY
- 미통과 사유: ConfirmReset과 fixed DB name만으로는 production data 접근 금지·권한·승인·감사 경계를 강제하지 못한다.
- 요청 내용: ConfirmReset과 fixed DB name만으로는 production data 접근 금지·권한·승인·감사 경계를 강제하지 못한다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/reset-test-data.ps1;cpf-tools/scripts/initialize-integration-fixtures.ps1`
- 재실행: `Production Data Use Prohibition Source/Consumer trace + SECURITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Production Data Use Prohibition의 보안·권한가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: ConfirmReset과 fixed DB name만으로는 production data 접근 금지·권한·승인·감사 경계를 강제하지 못한다.
- 요구 Evidence: Production Data Use Prohibition Source/Consumer trace + SECURITY positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025617 — FAILURE
- 미통과 사유: 환경 판별 불명·설정 누락·DNS/endpoint 오인 시 fail-closed로 중단하는 경로가 확인되지 않았다.
- 요청 내용: 환경 판별 불명·설정 누락·DNS/endpoint 오인 시 fail-closed로 중단하는 경로가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/reset-test-data.ps1;cpf-tools/scripts/initialize-integration-fixtures.ps1`
- 재실행: `Production Data Use Prohibition Source/Consumer trace + FAILURE positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Production Data Use Prohibition의 오류·부분 실패가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: 환경 판별 불명·설정 누락·DNS/endpoint 오인 시 fail-closed로 중단하는 경로가 확인되지 않았다.
- 요구 Evidence: Production Data Use Prohibition Source/Consumer trace + FAILURE positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator

### CPF-FR-025621 — TEST
- 미통과 사유: production-like endpoint/environment에 대한 negative Test가 확인되지 않았다.
- 요청 내용: production-like endpoint/environment에 대한 negative Test가 확인되지 않았다. Owner·실제 Consumer·실패/복구·운영·Test·Evidence를 포함해 제품 경로를 구현한다.
- 대상 Source 후보: `cpf-tools/scripts/reset-test-data.ps1;cpf-tools/scripts/initialize-integration-fixtures.ps1`
- 재실행: `Production Data Use Prohibition Source/Consumer trace + TEST positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator`
- 성공 기대: Production Data Use Prohibition의 검증가 실제 Consumer 경로에서 성공하고 pre/post 상태·감사·복구 Evidence가 남음
- 실패 기준: production-like endpoint/environment에 대한 negative Test가 확인되지 않았다.
- 요구 Evidence: Production Data Use Prohibition Source/Consumer trace + TEST positive/negative test + 실제 Runtime/DB/Browser/Fault 적용 검증 + exact-SHA Evidence validator
