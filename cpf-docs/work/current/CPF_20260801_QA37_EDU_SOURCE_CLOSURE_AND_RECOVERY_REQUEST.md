# CPF QA37 — Deterministic Build 복구 및 EDU·Customer Manual Source Closure 통합 개발 요청

## 1. 목적

latest master `23a16f35a5633ce1317920468a69fef00c1a6a41`에서 발견된 Build Source 파손과 EDU False Closure를 복구하고, 기존 EDU 32건과 Customer Manual EDU 135건을 실제 Product Source·Reference Consumer·Test·Runtime·Evidence로 닫는다.

이번 작업은 ADM·검증 Tool 추가 작업이 아니다. 실제 Owner Module의 Product Capability와 `cpf-reference`·표준 Job Pack의 실행형 고객 업무 예제를 개발하는 작업이다.

## 2. 시작 기준

작업 시작 시 다음을 읽기 전용으로 확인하고 기록한다.

```powershell
git fetch origin
git rev-parse origin/master
git rev-parse HEAD
git status --porcelain=v1 --untracked-files=all
git remote -v
```

- 시작 예상 SHA: `23a16f35a5633ce1317920468a69fef00c1a6a41`
- 실제 `origin/master`가 달라졌으면 새 exact SHA로 모든 Matrix와 Evidence를 재기준한다.
- 기존 Working Tree 변경을 보호한다.
- 사용자 승인 없이 Commit·Push·Branch·Tag·PR·Release·Reset·Restore·Stash·파일 삭제를 수행하지 않는다.
- `git clean`, `git reset --hard`, `git restore .`를 사용하거나 제안하지 않는다.

## 3. 정본과 입력 우선순위

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. 이 QA37 요청서와 QA37 Requirement Matrix
3. `cpf-docs/work/current/CPF_CUSTOMER_MANUAL_EDU_IMPLEMENTATION_REQUIREMENTS.md`의 135개 원본 ID
4. 최신 Git의 실제 Source·SQL·API·Config·Frontend·Script·Test
5. Architecture·Specification
6. QA36 Canonical 162·Active Gap 85·SELF 30·기존 상세 QA 1,873·Scenario 441
7. 과거 Review·Evidence

README와 README 연결 Manual·Guide는 개발·수정·완료 판단 범위에서 제외한다.

## 4. P0 Stop Gate — Build Source 복구

### 4.1 Root Build 책임 복원

현재 Root `build.gradle`은 BZA Module Build Script와 동일하다. 다음을 수행한다.

- 직전 정상 Root Build와 latest 변경 요구를 대조하여 Root Platform Build를 재구성한다.
- BZA 전용 Build Script는 `cpf-biz-admin/build.gradle`에만 둔다.
- Root Build가 다음을 소유하게 한다.
  - Platform·Stack Version
  - Java 25 Toolchain
  - exact Source SHA
  - Artifact Mode
  - 전체 Project Repository
  - Dependency Management
  - 공통 Test·JavaDoc·Publication
  - Quality Gate·SBOM
- Root와 BZA Build Blob·내용이 동일하지 않음을 검증한다.
- Valid change를 과거 파일로 무조건 덮어쓰지 않고 latest 요구와 병합한다.

### 4.2 Included Build 복구

`settings.gradle`이 참조하는 다음 Source를 확인한다.

- `cpf-tools/build/gradle-plugin/**`
- `cpf-tools/build/platform-bom/**`

선택지는 Architecture Review 후 하나만 허용한다.

1. 정식 Product Source로 복원하고 Fresh Clone에서 사용
2. 새 Owner·경로로 완전 이관하고 `settings.gradle`, Consumer, Test, Publication, Guide 제외 문서를 함께 갱신

단순 참조 삭제, Ignore, Build Output 대체는 금지한다.

### 4.3 Build 수용 기준

```text
development_status = 완료
verification_status = 완료
```

조건:

- Fresh Clone
- Empty `GRADLE_USER_HOME`
- Java 25
- `gradlew.bat tasks`
- `gradlew.bat clean test assemble qualityGate`
- 필요한 Publication/BOM/Convention Test
- Build 후 Dirty Tree 0
- `cpf-tools/build/**` Source가 Git 추적 상태

이 Gate가 실패하면 EDU·Runtime 대규모 검증으로 넘어가지 않는다.

## 5. Current Request·Evidence 정본 복구

- `CPF_CURRENT_WORK_REQUEST.md`를 QA37 Primary Work Request로 갱신한다.
- QA37, Customer Manual EDU 135, EDU 32, Canonical 162, QA36 85, SELF 30의 상하 관계를 명시한다.
- 모든 Result Matrix·Evidence·Manifest는 최종 result SHA를 기록한다.
- `sourceSha`, `resultSha`, `HEAD`, `origin/master` 관계를 분리한다.
- 과거 SHA Evidence를 latest 성공 근거로 사용하지 않는다.
- Push 전 Source Evidence와 Push 후 Read-only exact-SHA Evidence를 분리한다.

## 6. EDU 32개 완료 판정 해제

작업 시작 시 `EDU-001~032`를 다음 상태로 되돌린다.

```text
development_status = 재확인 필요
verification_status = 미검증
```

각 ID를 독립적으로 다시 판정한다.

### 6.1 각 EDU ID 필수 Mapping

- Feature ID
- Canonical·QA36·SELF·Manual EDU Mapping
- 실제 Owner Module
- Target Package
- 정확한 Source 파일
- Class·Method·Bean
- Public API·SPI
- 실제 Product Consumer
- `cpf-reference` 또는 표준 Job Pack Consumer
- Config·Profile
- 중앙 DB Vendor Pack SQL
- 정상·오류·경계·복구 Test
- Runtime 명령
- ADM·BZA·Gateway 운영 확인
- Audit·Log·Metric·Trace
- exact-SHA Evidence

### 6.2 기존 Source 재사용 조건

기존 구현 재사용은 다음이 모두 확인될 때만 허용한다.

- 최종 병합 Root에 파일이 존재
- 실제 Requirement 동작 구현
- Consumer 존재
- Test 존재
- Public API/SPI 경계 준수
- Runtime 명령이 실제 Class/Test를 지정
- latest SHA 근거
- 적용 가능한 오류·복구 경로 존재

각 재사용 항목은 `EDU_REUSED_SOURCE_JUSTIFICATION.csv`에 Source·Class·Method·Consumer·Test와 근거를 기록한다.

### 6.3 EDU Gate 수정

개발 모드에서도 최소한 다음을 fail-closed로 확인한다.

- Source Glob 실제 해석
- Test Glob 실제 해석
- Public Contract 실제 해석
- Class·Method·Package 존재
- Runtime Command 대상 존재
- Consumer 존재
- README·Manual만 존재하는 항목 실패
- 동일 Source의 근거 없는 Bulk ID Mapping 실패
- Internal Package 직접 참조 실패
- Mock·고정 응답만 있는 예제 실패
- 과거 SHA 근거 실패
- Overlay 단독 Root 검사 실패

`--release`는 Runtime 완료와 Evidence를 추가 검증하되, Source 존재 검사를 Release 전용으로 미루지 않는다.

## 7. Customer Manual EDU 135건 통합 구현

`CPF_CUSTOMER_MANUAL_EDU_IMPLEMENTATION_REQUIREMENTS.md`를 처음부터 끝까지 읽고 원본 135개 ID를 누락 없이 개발 원장으로 가져온다.

### 7.1 수량 Gate

| 영역 | 요구 수량 |
|---|---:|
| 온라인·공통·외부 연계 | 45 |
| Batch | 30 |
| ADM 업무 연동 | 17 |
| BZA 적용·운영 | 14 |
| Gateway | 14 |
| 플랫폼 설치·운영·복구 | 15 |
| 합계 | 135 |

135건보다 적거나 중복 ID가 있으면 Gate 실패다. 같은 문구가 반복돼도 270건으로 계산하지 않는다.

### 7.2 통합 개발 원칙

- 일부 구현 후 후속 차수로 넘기지 않는다.
- 135개 밖의 필수 Gap이 발견되면 신규 ID로 같은 작업에 추가한다.
- 정상 요청 한 건·메모리 고정 응답·문자열 출력·Swagger만으로 완료하지 않는다.
- 환경 부족은 Source 미개발 사유가 아니다.
- 가능한 Product Source·SQL·Config·Test Double·Test는 우선 개발한다.
- 실제 외부기관·Secret·인프라 검증만 미검증으로 분리한다.

### 7.3 장애·복구 축

각 ID에 적용 가능한 범위를 명시하고 구현·시험한다.

- 입력 오류
- 권한·데이터 범위
- 중복·멱등성
- 동시성·CAS·Fencing
- Timeout
- DB Commit 전·후 장애
- 외부 전송 전·후 장애
- 응답 유실·결과 불명
- 부분 성공
- Retry·Restart·Reprocess
- Reconcile·Compensation·Rollback
- Audit
- Log·Metric·Trace
- ADM·BZA·Gateway 운영 확인

적용되지 않는 축은 `N/A` 한 단어가 아니라 적용 제외 근거를 기록한다.

## 8. Module·Package·Layer 표준

### 8.1 배치 원칙

- topology-independent 핵심 계약: `cpf-core`
- 고객 업무 공통 Product 기능: `cpf-common`
- Batch Runtime: `cpf-batch`
- Gateway Runtime: `cpf-gateway`
- 플랫폼 운영: `cpf-admin`
- 고객 업무 관리자: `cpf-biz-admin`
- 실행형 교육 고객 업무: `cpf-reference`
- Batch 교육 Job: 문서가 허용한 표준 Job Pack
- Generator·검증 Tool: `cpf-tools`

교육 예제라는 이유로 Product 기능을 `cpf-reference`에 복제하지 않는다. Product Capability Gap은 정식 Owner Module에 구현하고 EDU가 Public API·SPI로 소비한다.

### 8.2 Package 원칙

각 예제는 기능별 `api`, `application`, `domain`, `infrastructure`, `adapter`, `configuration` 책임을 필요에 맞게 분리한다.

금지:

- `sample`, `demo`, `misc`, `temp`, `common2`
- Product Runtime Module 내부 고객 업무 예제
- External Module의 Internal Package 참조
- 기존 대형 Class에 모든 EDU 로직 집중
- Consumer 없는 Interface·Marker
- `cpf-tools`에 Product Runtime 구현

## 9. DB Vendor Pack

Oracle·PostgreSQL·MariaDB는 중앙 Vendor Pack 구조를 사용한다.

필수:

- Schema
- Table·Index·FK·Constraint
- Install
- Sequential Upgrade
- Rollback
- Reapply
- Runtime Query
- Seed/Fixture 분리
- Checksum
- Drift
- Backup/Restore 영향

금지:

- Module-local Vendor SQL 복제본
- Vendor Token만 바꾼 형식적 SQL
- `cpf-reference` 내부 임시 Product Schema
- Install만 있고 Rollback이 없는 변경

## 10. Architecture Blocker 처리

구조상 바로 구현할 수 없는 항목도 삭제·완료 처리하지 않는다.

먼저 가능한 범위의 다음 코드를 개발한다.

- Port·Adapter
- 상태 모델
- 오류·Timeout·Retry
- 결과불명·Reconcile
- Test Double
- Config·SQL·Test

남은 Blocker는 다음으로 분류한다.

- PRODUCT_CAPABILITY_GAP
- OWNERSHIP_OR_DEPENDENCY_CONFLICT
- PUBLIC_API_SPI_GAP
- DB_VENDOR_LIMITATION
- EXTERNAL_SYSTEM_LIMITATION
- SECURITY_OR_COMPLIANCE_CONFLICT
- REQUIREMENT_CONFLICT
- ENVIRONMENT_BLOCKED
- ARCHITECTURE_DECISION_REQUIRED

필수 보고서:

- `EDU_ARCHITECTURE_BLOCKER_REPORT.md`
- `EDU_ARCHITECTURE_BLOCKER_MATRIX.csv`
- `EDU_DECISION_REQUESTS.md`
- `EDU_FOLLOWUP_REQUIREMENTS.csv`

Blocker가 남으면 전체 135/135 완료를 선언하지 않는다.

## 11. Docker 개발·테스트 환경

작업 전에 다음 순서로 읽는다.

1. `cpf-docs/guides/CPF_도커_개발테스트환경_안내.md`
2. `cpf-docs/guides/CPF_도커_연동및사용가이드.md`
3. `cpf-docs/architecture/CPF_도커_개발테스트환경_구성명세.md`
4. 필요 시 `cpf-docs/guides/CPF_도커_문제해결및초기화가이드.md`
5. 다른 PC 신규 구축 시에만 `cpf-docs/guides/CPF_도커_개발테스트환경_전체구축가이드.md`

```text
Docker Runtime = C:\dev\Docker\CPF
Secret         = C:\dev\Docker\Secrets
```

현재 환경은 준비돼 있으므로 전체 설치 Script를 재실행하지 않는다.

- 필수 Image 13/13
- 기존 Runner Image 3/3 보존
- Container 7/7 Created/Stopped
- Running 0
- Volume 5/5
- CPF 업무 Schema·Data·Seed 없음

필요 Service만 선택적으로 시작한다.

금지:

- `docker system prune`
- Image/Volume Prune
- Docker Desktop 초기화
- 기존 Compose·Image·Runner·Secret·Volume 삭제·덮어쓰기
- Docker 설치 단계에서 CPF 업무 Schema·User·Seed·Kafka Topic 임의 생성

Toxiproxy로 장애를 만들고 OpenTelemetry Collector로 Trace·Metric·Log를 확인한다. Trivy와 ORT로 취약점·Secret·SBOM·License를 검증한다. 종료 후 사용한 Container는 중지하되 Volume과 Tool은 유지한다.

## 12. 검증 순서

1. Build Source·Included Build Static Gate
2. Java 25 Fresh Clone·Empty Cache
3. EDU 32 Source Closure
4. Manual EDU 135 Source Closure
5. ADM/BZA clean `npm ci`·verify
6. 3DB Install·Upgrade·Rollback·Reapply
7. Kafka·Redis
8. Toxiproxy 장애·복구
9. OTel Trace·Metric·Log
10. Playwright Chromium·Firefox·WebKit
11. Multi-instance·Process Kill·Response Loss
12. Trivy·ORT·SBOM
13. exact-SHA·Clean Tree Evidence

개발자가 Docker 환경에서 직접 수행한다. 사용자에게 추가 수동 검증을 요구하지 않는다.

## 13. 상태 판정

항상 분리한다.

```text
development_status
verification_status
```

- Source·Consumer·Test 완결, Runtime 미실행: `완료 / 미검증`
- Source 일부: `부분 구현 / 미검증`
- Architecture 결정 필요: `재확인 필요 / 미검증`
- 실행 실패: 구현 상태 별도, `verification_status = 실패`
- Matrix·Catalog·Tool만 존재: 완료 금지

## 14. 필수 산출물

Root Overlay ZIP 하나에 포함한다.

- 작업 전 리뷰
- QA37 자체 개발요건
- 변경 Product Source·SQL·Config·Frontend·Test
- EDU 32 Mapping
- Manual EDU 135 Coverage Matrix
- 재사용 Source 근거
- 신규·수정 Source Manifest
- Missing/Partial 목록
- Architecture Blocker·Decision Request
- Test·Runtime 결과
- 작업 후 독립 리뷰
- Codex 검수 패키지
- Delete Manifest
- File Hash·Package Manifest
- 적용·Rollback 명령

README와 README 연결 Manual·Guide는 ZIP에서 제외한다.

## 15. Codex 검수 시점

이번 QA37 개발과 검증을 먼저 끝낸다. 사용자가 Commit·Push한 후에만 Codex 독립 검수를 수행한다.

Codex 순서:

```text
P0 Build Source Gate
→ EDU 32·Manual EDU 135 Source Closure
→ Java/Frontend/3DB
→ Distributed/Fault
→ Browser
→ Supply-chain
→ exact-SHA Evidence
```

P0 실패 시 비싼 Runtime 검수를 중단하고 개발 보완으로 반환한다.
