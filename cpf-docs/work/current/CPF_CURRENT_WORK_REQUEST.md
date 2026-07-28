# CPF Current Work Request — Full QA Closure after 20260729 Stage 2

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Stage 2 Overlay 작성 기준 SHA: `9e5d1676a9ccba55fedf4dfb633a9e710f487a02`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 상세 요청서: `cpf-docs/work/requests/CPF_CODEX_FULL_QA_CLOSURE_REQUEST_20260729.md`
- 병합 원장: `cpf-tools/verification/20260729_02/CPF_FINAL_QA_MASTER_LEDGER_20260729.csv`

## 현재 해야 할 일

사용자가 Stage 2 Root Overlay를 적용하고 Push한 **최신 master 정확한 SHA**에서 기존 QA 2,118건과 신규 QA를 병합한 2,715건 원장을 전수 재판정한다.

P0 Ledger 18건과 ADM Runtime Control 14개 Capability를 우선 처리하되 전체 범위를 대신하지 않는다. 1차 구현 묶음도 Compile·Test·Runtime·DB·Browser·다중 인스턴스 회귀 대상에 포함한다.

결함이 발견되면 문서 상태만 변경하지 말고 Source·SQL·3개 DB·Frontend·Generator·Test·Guide·Evidence를 함께 수정한다.

## 완료 기준

최신 master에서 `완료` 이외 상태가 남지 않고 Java 25·Gradle 9.1, Oracle·PostgreSQL·MariaDB Lifecycle, ADM/BZA Browser, Multi-instance·Offline·Retry·Rollback·UNKNOWN_RESULT Evidence가 현재 SHA로 보존돼야 한다.

사용자 승인 없이 Commit·Push·Branch·Tag·Release를 생성하지 않는다.
