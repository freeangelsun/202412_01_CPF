# CPF 20260801_03 Push 이후 독립 검수 보고서

## 1. 검수 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 SHA: `23a16f35a5633ce1317920468a69fef00c1a6a41`
- Parent SHA: `19dd72b5978f2a3c630943c0fff05bee2d2fed34`
- Commit: `20260801 CPF integrated development checkpoint`
- Commit 규모: 373개 변경 파일, 추가 46,894줄, 삭제 45,254줄, 총 92,148줄
- 판단 기준: 문서의 완료 표시보다 Source, Build, Config, SQL, Test, CI, Evidence를 우선

## 2. 최종 판정

**판정: 실패**

현재 SHA는 개발 체크포인트로는 보존할 수 있으나 상용 Framework 완료본이나 독립 검수 통과본으로 처리할 수 없다. 아래 P0 Source/Build/Evidence 결함이 해결되기 전에는 완료, 검증 완료, 배포 가능으로 표시하지 않는다.

## 3. 확정 결함

### P0-01 Root `build.gradle` Owner 파일 덮어쓰기

현재 Root `build.gradle`은 `com.cpf.bizadmin`, `war`, BZA Frontend Task와 `bza-permission-manifest`를 사용하는 BZA 모듈 Build 내용이다. Commit에서 Root 파일은 1,972줄 삭제, 71줄 추가되어 전체 Root Build 계약이 소실됐다.

영향:

- Root Plugin/BOM/공통 Java 25 설정과 전체 Module Build 계약 상실
- `clean test assemble qualityGate` 실행 불가 또는 잘못된 Project 구성
- Publication, Dependency Boundary, Generator, Quality Gate 회귀 가능
- 동시에 `cpf-tools/build/gradle-plugin`, `cpf-tools/build/platform-bom`이 삭제되어 Build Ownership 단절 위험

조치:

- Parent SHA의 Root `build.gradle`을 기준으로 복구한다.
- BZA 전용 내용은 `cpf-biz-admin/build.gradle`에서만 관리한다.
- 삭제된 Gradle Plugin/BOM은 `settings.gradle`, Version Catalog, Artifact Catalog, 실제 Consumer를 추적해 복구 또는 정식 제거를 결정한다.

### P0-02 Frontend Source OpenAPI와 CI Verification Scope 불일치

Tracked ADM/BZA OpenAPI는 `CONTROLLER_SOURCE_PRE_RUNTIME`, `x-cpf-release-eligible=false` 계약이다. `validate-openapi.mjs` 기본 Scope는 `release`이지만 CI는 환경변수 없이 `npm run verify`를 실행한다.

영향:

- CI Frontend Job이 Source OpenAPI를 Release OpenAPI로 검사해 즉시 실패할 수 있다.
- Source Gate와 Release Gate의 의미가 섞인다.

조치:

- Source Job은 `CPF_OPENAPI_SCOPE=source`, `CPF_CONSUMER_SCOPE=source`를 명시한다.
- Release Job은 Backend Runtime을 기동해 `BACKEND_RUNTIME` OpenAPI를 Export한 뒤 `release/full` Scope를 사용한다.

### P0-03 Generated Client 삭제와 생성 단계 누락

Commit은 ADM/BZA의 `src/generated/cpf-api.ts`, Orval 산출물 Marker를 삭제했다. 그러나 `npm run verify`와 `build` 앞에 `generate:api`가 보장되지 않는다. CI는 `npm ci --ignore-scripts` 후 바로 `npm run verify`를 실행한다.

영향:

- `verify:generated`, Typecheck, Vite Build가 생성 파일 부재로 실패할 수 있다.
- 생성 산출물 관리 정책과 Self-dirty Gate가 충돌할 수 있다.

조치:

- `verify` 실행 순서를 `validate source OpenAPI -> generate:api -> verify:generated -> verify:consumer -> lint -> typecheck -> test -> build`로 고정한다.
- Generated 파일을 추적할지 비추적할지 정책을 하나로 정하고 `.gitignore`, Delete Manifest, CI Dirty Check를 일치시킨다.

### P0-04 npm Peer/Install 정책 충돌

ADM/BZA `.npmrc`에 `strict-peer-deps=true`와 `legacy-peer-deps=true`가 동시에 설정되고 `install-strategy=nested`가 추가됐다. 사용자 환경에서 이전 Lock 재생성은 ERESOLVE를 발생시켰고, 이후 우회 설치가 사용됐다.

조치:

- Node 22.18.x, npm 10.9.2의 Clean Container에서 Lockfile을 한 번 재생성한다.
- Strict 또는 Legacy 정책 중 하나를 명시적으로 선택한다.
- `package.json`, Lockfile Root Metadata, `.npmrc`, CI Command를 동일 조건으로 고정한다.
- 우회 정책을 유지한다면 이유, 종료 조건, Supply-chain 영향을 기록한다.

### P0-05 exact-SHA Evidence와 완료 보고 불일치

개발 완료 보고와 Evidence의 기준 SHA는 `19dd72b5978f2a3c630943c0fff05bee2d2fed34`이나 현재 `master`는 `23a16f35a5633ce1317920468a69fef00c1a6a41`다. CI는 `--expected-sha ${ github.sha }` 및 `--require-clean`을 사용하도록 변경됐다.

또한 완료 보고에는 README·연결 Manual/Guide를 수정하지 않았다고 기록됐지만 실제 Commit은 README와 Guide 8종, 문서 표준을 대규모 변경했다.

영향:

- Evidence를 현재 SHA 성공 결과로 승계할 수 없다.
- Requirement Trace, Work Context SHA, Protected Document Boundary, Manifest Hash가 실패할 수 있다.

조치:

- 기존 Evidence는 `미검증` 또는 과거 SHA Evidence로 유지한다.
- Source 수정 완료 후 최종 SHA에서 Evidence, Matrix, Completion Report, Manifest, 파일 SHA-256을 다시 만든다.
- README/Guide 변경은 별도 문서 작업으로 분리하고 Source 완료 근거에서 제외한다.

### P0-06 Overlay Manifest와 실제 Push 변경 불일치

기존 20260801_01 Overlay Manifest는 초기 373개 Overlay를 기준으로 작성됐으나 이후 Windows Hotfix, `.npmrc`, Package Metadata, README/Guide 변경이 같은 Commit에 포함됐다. 기존 Manifest와 File Hash를 현재 Commit의 완전한 증적으로 사용할 수 없다.

조치:

- 최종 Remediation Commit에서 Git Tree 기준 변경 목록과 SHA-256을 다시 생성한다.
- 삭제 파일은 Delete Manifest에 명시한다.

## 4. EDU 변경 파일이 적은 이유와 실제 판정

이번 작업의 EDU 변경은 신규 EDU 기능 Source를 대량 작성한 작업이 아니라 다음 계약을 추가한 작업이다.

- `cpf-tools/governance/cpf-edu-executable-catalog.json`
- EDU 32 Feature Catalog
- Canonical 162 Coverage Matrix
- EDU Coverage/Profile/Impact Gate와 Unit Test

원격 `master`의 `cpf-reference`에는 이미 `crud`, `header`, `transaction`, `servicecall`, `messaging`, `security`, `batch`, `logging`, `audit`, `failure`, `validation` 등 Reference Source와 대응 Test Package가 존재한다. 따라서 EDU 파일이 적다는 사실만으로 신규 구현 누락이라고 단정하지 않는다.

다만 기존 EDU 리뷰 자체가 다음을 `미검증`으로 기록했다.

- 전체 Repository에서 Source/Test Glob 실제 해석
- Java 25 Runtime Test
- Oracle/PostgreSQL/MariaDB, Kafka, Gateway, Agent, Browser Scenario
- Final Push SHA별 162개 Runtime Evidence

따라서 현재 EDU 판정은 다음과 같다.

- Catalog·Matrix·Fail-closed Gate Source: `완료`
- 기존 Reference Source/Test와 32 Feature의 실제 연결: `재확인 필요`
- Runtime Scenario와 exact-SHA Evidence: `미검증`
- 신규 EDU Source 개발 필요 여부: 전체 Glob 해석 결과에서 누락된 Feature에 한해 결정

## 5. 보호해야 할 기존 성공 기능

- 기존 `cpf-reference`의 CRUD/Header/Transaction/Messaging/Batch/Failure/Recovery 예제
- ADM 59 Route와 BZA 26 Route의 실제 Consumer 연결
- Controller Permission·Audit Fail-closed·Operator Trust Boundary
- Batch Ghost/Fencing/Recovery와 3 Vendor SQL 의미
- Incident·Notification DLQ Lifecycle
- Network/SSRF 정책과 실제 Consumer
- Generator 3 Vendor Lifecycle 및 사용자 변경 보호
- README·Guide는 다음 개발 작업에서 수정하지 않음

## 6. 검수 수행 사실

수행:

- 원격 `master` 최신 SHA 확인
- Commit 373개 파일 전체 목록과 통계 검토
- Root Build, CI Workflow, Frontend Package/OpenAPI/Generated Client 변경 검토
- EDU Catalog·Review·Matrix와 원격 `cpf-reference` Source/Test Package 존재 확인
- Evidence·Completion Report의 SHA 및 문서 변경 진술 대조

미실행:

- Java 25 Fresh Build/Test/Publication
- ADM/BZA Clean npm 전체 검증 및 Playwright 3 Browser
- 3 Vendor 실제 DB Lifecycle
- Kafka·Redis·다중 인스턴스·Fault Recovery
- Artifact/SBOM/Vulnerability/License Runtime

미실행 항목은 성공으로 기록하지 않는다.
