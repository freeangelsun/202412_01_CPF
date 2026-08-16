# CPF Local Validation V4 Remediation

- 입력 Runtime 결과: `CPF_LOCAL_VALIDATION_20260816_002158.zip`
- 입력 결과 SHA-256: `ba2a6f9b324599f1fe764ba815da32c0aad2a7a89deeddf9e7d6676d83c709a1`
- 입력 결과: PASS 10 / FAIL 18 / SKIP_ENV 3
- V4 원칙: 기존 18 FAIL을 각각 다시 닫기 전 완료 처리하지 않는다.
- 사용자 로컬 공식 기준: Windows / PowerShell 7.6.5 / Java 25 / `ResourceProfile=local`
- Git 상태/HEAD/origin은 사용자 명시 요청 없이는 검증기가 조회하지 않는다.

## 실패별 보정/재검수

| 기존 단계 | 기존 상태 | 원인 | V4 보정 | 개발GPT 재검수 | 사용자 Local V4 재검수 |
|---|---|---|---|---|---|
| EVIDENCE_INTEGRITY | FAIL | `$Format:%H$`가 exact SHA처럼 전달되고 Package identity와 불일치 | exact package source identity fail-closed + V4 Manifest/BASE_SHA 재생성 | 패키징 후 integrity 재검산 대상 | LOCAL_REVERIFY_REQUIRED |
| TESTING_TOOLS | FAIL | pytest 미설치, Windows cp949, DB/OpenAPI/Vendor lifecycle drift | project-local Python 3.13 venv + UTF-8 + requirements bootstrap + Source/Test currentization | 과거 실패 파일 묶음 56 PASS / 3 SKIP | LOCAL_REVERIFY_REQUIRED |
| DB_VERIFICATION_TESTS | FAIL | cp949 + cryptography 부재 + fixture drift | UTF-8 강제 + cryptography bootstrap + fixture currentization | 75/75 PASS | LOCAL_REVERIFY_REQUIRED |
| DB_TESTS | FAIL | Runtime matrix ClientAdapter 계약 drift | current runtime matrix 계약으로 test currentization | 86/86 PASS | LOCAL_REVERIFY_REQUIRED |
| CPF_TOOLS_PYTEST | FAIL | pytest 환경 부재 | project-local reusable venv, `--import-mode=importlib`, cache disabled | 694 collected / collection error 0; 주요 tree 별도 PASS | LOCAL_REVERIFY_REQUIRED |
| GRADLE_HELP | FAIL | `cpfResourceProfile` Windows wrapper parsing + BootRun eager lookup | wrapper parsing 보정 + lazy BootRun configuration | resource/Gradle contract 5/5 PASS | LOCAL_REVERIFY_REQUIRED(Java25) |
| GRADLE_JAVA_TEST_QUALITY | FAIL | 위 Gradle configuration 공통 원인 | Java25 강제 + `clean build qualityGate --continue`, 저메모리 순차 실행 | 정적 계약 PASS | LOCAL_REVERIFY_REQUIRED(Java25) |
| ADM_FRONTEND_VERIFY | FAIL | `admApprovalExecute` 503가 Controller에는 있으나 canonical OpenAPI에 누락 | canonical OpenAPI 503 동기화 | OpenAPI lifecycle PASS + source validate 321 operations PASS | LOCAL_REVERIFY_REQUIRED |
| BZA_FRONTEND_VERIFY | FAIL | `no-control-regex` | control code를 charCode 검사로 변경 | Source 보정 확인 | LOCAL_REVERIFY_REQUIRED |
| GRADLE_ASSEMBLE_AFTER_FRONTEND | FAIL | Gradle 공통 configuration 실패의 파급 | Full build/assemble 경로 currentization | 정적 계약 PASS | LOCAL_REVERIFY_REQUIRED(Java25) |
| GRADLE_SBOM | FAIL | Gradle 공통 configuration 실패의 파급 | Full build 뒤 CycloneDX SBOM 독립 단계 | 정적 계약 PASS | LOCAL_REVERIFY_REQUIRED(Java25) |
| GENERATOR_LIFECYCLE | FAIL | Gradle 파급 + lifecycle smoke drift | lifecycle smoke/current Generator 계약 보정 | Generator 27 PASS / 10 SKIP / 6 subtests PASS | LOCAL_REVERIFY_REQUIRED(pwsh/Java25) |
| DB_CHECK-DB-VENDOR-PACK-PARITY | FAIL | stale pack/resource ownership 검사 | canonical JSON renderer/runtime contract 기반 gate로 currentize | vendor semantic parity PASS | LOCAL_REVERIFY_REQUIRED(pwsh) |
| DB_CHECK-CANONICAL-DB-LIFECYCLE-CONTRACT | FAIL | V118/currentVersion/DB3 lifecycle drift | V118 + lifecycle source/rollback/checksum currentization | migration lifecycle PASS, development contract PASS | LOCAL_REVERIFY_REQUIRED(pwsh) |
| DB_CHECK-SQL-STANDARD | FAIL | stale install/lifecycle policy derivation | canonical development/semantic gates로 currentize | DB focused tests PASS | LOCAL_REVERIFY_REQUIRED(pwsh) |
| DB_CHECK-SQL-CANONICAL | FAIL | split/generated count, sparse Flyway version을 연속버전으로 오판, checksum 형식/Generator owner drift | canonical renderer + migration lifecycle 정본에서 판정하도록 currentize | migration lifecycle + vendor semantic parity PASS | LOCAL_REVERIFY_REQUIRED(pwsh) |
| LOCAL_ONE_WAS_START | FAIL | PowerShell 단일 결과에서 `.Count` property 접근 | `@(...).Count`로 배열 정규화 | resource/local runtime contract tests PASS | LOCAL_REVERIFY_REQUIRED(Java25) |
| MANAGED_STATE_AFTER | FAIL | BZA `npm verify`가 Repository generated client를 재생성 | Frontend 전체 검증을 TEMP sandbox에서 수행하여 Repository read-only 보장 | orchestrator contract 6/6 PASS | LOCAL_REVERIFY_REQUIRED |

## V4에서 추가로 넓힌 1회 Full Local 범위

`-FullLocal`은 destructive DB rollback과 장시간 HTTP soak를 제외한 로컬 검증 범위를 최대화한다.

- NXT3 22
- Evidence/Architecture release
- Python 전체 collection
- Python test tree 12개 독립 실행
- Windows path compatibility
- Security controller permission
- Gateway static closure
- Batch UNKNOWN/Reconcile, Approval, Fencing, Ghost safety, Fail-closed
- Publication starter closure
- Broker/Batch/Resource performance contract
- Java25 Root `clean build + qualityGate`
- Maven local publication gate
- ADM/BZA isolated frontend verify + OpenAPI release validation
- assemble / SBOM
- Generator lifecycle
- DB static lifecycle gates
- Docker 준비 시 DB3를 MariaDB → PostgreSQL → Oracle 순차 실행
- Cache provider live + QA39 runtime/fault smoke
- Local integrated 1-WAS
- ADM/BZA Browser smoke + Playwright E2E + accessibility
- Supply chain
- Package managed-state before/after
- SUMMARY CSV/JSON/TXT + 단일 결과 ZIP

### 별도 opt-in 유지

- `-AllowDestructiveDbRollback`: 실제 destructive rollback 허용 시에만 사용
- `-IncludePerformanceLoad`: 10,000회 HTTP load / 장시간 soak를 명시적으로 수행할 때만 사용

노트북 보호를 위해 Docker/DB/Runtime/Frontend/Java 검증은 가능한 한 순차 실행하며 모든 컨테이너를 동시에 올리지 않는다.
