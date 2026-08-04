# TEST AND EVIDENCE — QA-6F R2

## 실행 환경

- Java/Javac: 21.0.10
- Python: 3.13.5
- Node: 22.16.0
- npm: 10.9.2
- git: 2.47.3
- PowerShell Core: 미설치
- 실제 Oracle/PostgreSQL/MariaDB: 이 실행환경에 미구성
- 기준 SHA: `f97655c1299936a1101bc3ec10239265ec3b502e`

## 수행 결과

### Seed bundle / runtime contract / Oracle secret transport

```text
python -m unittest -v   cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py   cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py   cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py
```

- 결과: **12/12 PASS**
- 종료 코드: **0**
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_6WAY_QA-6F_f97655c_R2/batch001_seed/final_regression_12_tests.log`
- 검증 항목:
  - canonical statementCount와 statements 배열 정합성
  - 3 Vendor product bundle에 gateway seed 포함
  - canonical insert mutation 129건 bundle closure
  - 3 Vendor retry-safe mutation
  - DB install consumer의 product seed 실행·fail-closed
  - Oracle SQL*Plus credential이 process argument에 남지 않고 redirected stdin만 사용
  - 실패 출력 credential masking과 verify contract 유지

초기 Overlay-only 작업복사본에서는 비변경 기준 파일이 없어 3개 Test가 FileNotFound로 중단됐다.
이를 제품 실패로 기록하지 않고 exact-SHA의 `00_optional_sample_seed.sql`, `00_test_seed.sql`,
`initialize-cpf-database.ps1`을 read-only 검증 입력으로 보충한 후 동일 명령을 재실행해 통과했다.
비변경 기준 파일은 Overlay에 포함하지 않았다.

## Source·Consumer 재검수

- Data Lineage: `CpfLineageRecord`/`CpfLineageRecorder` → `CpfBoundedLineageRecorder` → `CpfServiceCallEngine.recordLineage`
- Data Reconciliation: `CpfServiceCallEngine.registerUnknown` → `JdbcCpfReconciliationRepository` → `CpfReconciliationWorker`
- 상세: `cpf-docs/evidence/qa/QA_PARALLEL_6WAY_QA-6F_f97655c_R2/SOURCE_TRACE_SUPPLEMENT.csv`

## 미수행 검증

다음은 환경 부재로 성공 처리하지 않았다.

- PowerShell DB lifecycle Script 실제 실행
- Oracle/PostgreSQL/MariaDB install→upgrade→seed→verify→rollback→reapply
- Java 전체 Gradle compile/test/publication
- Browser/Playwright ADM/BZA
- 다중 인스턴스·Process Kill·Network fault·partial failure
- Provider/Kafka/Redis/외부 연계 Runtime

따라서 QA 통과는 0건이며, 미실행 항목은 `미검증`이다.
