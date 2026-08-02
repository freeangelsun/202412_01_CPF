# CPF QA38 Codex 최종 독립 검수 요청서

## 1. 독립 시작 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 개발 기준 SHA: `dafe5c0e5260ea8149234e8ab2e75347e75338c1`
- Overlay 적용 후 실제 `HEAD`, `origin/master`, Working Tree를 다시 기록한다.
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Requirement Matrix: `cpf-docs/quality/CPF_QA38_FINAL_REQUIREMENT_MATRIX.csv`
- Scenario Matrix: `cpf-docs/quality/CPF_QA38_FINAL_SCENARIO_MATRIX.csv`
- 사전 리뷰: `cpf-docs/work/review/CPF_QA38_PRE_DEVELOPMENT_REVIEW.md`
- 사후 리뷰: `cpf-docs/work/review/CPF_QA38_POST_DEVELOPMENT_REVIEW.md`
- 패키지 리뷰: `cpf-docs/work/review/CPF_QA38_POST_PACKAGE_REVIEW.md`

과거 SHA의 PASS를 최신 SHA의 성공으로 승계하지 않는다. 문서 상태보다 실제 Source·SQL·Generator·Consumer·Test·실행 결과를 우선한다.

## 2. 이번 변경 범위

- Root `settings.gradle`, Build Foundation, BOM, Artifact Catalog
- `cpf-core`·`cpf-common` 선택 Runtime의 Leaf Starter 이관
- Base/Leaf/Aggregate/Profile과 13개 Versioned Capability Profile
- ADM/BZA/Gateway/Batch/Reference/Member 실제 Consumer Dependency
- Generator Profile·Named Provider Binding·Resolved Starter Lock
- Reliability JDBC, Kafka, RabbitMQ, JMS, IBM MQ Plugin Boundary
- TCP, Fixed-length, ISO8583, SFTP, Archive, Attachment, Notification/Email/SMS, Quartz
- Runtime Control Agent/Plane/Applier, HTTP Service Call, Remote Log, Logging Aspect
- Oracle/PostgreSQL/MariaDB Migration·Rollback SQL
- exact Delete Manifest 160개와 빈 폴더 후보 24개
- Apply/Verify/Harness/Evidence/Matrix/Handover

## 3. 보호할 기존 성공 기능

- Identifier/Header/Transaction Context/Error/Validation 계약
- ADM/BZA 권한·감사·마스킹·위험조치
- Spring Batch Primary Engine 및 Center-cut/Worker/Scheduler 계약
- Gateway Route/Permission Owner 경계
- 기존 Kafka·Security·Cache·Observability·Resilience·FeatureFlag·Secret 기능
- Canonical Generator와 Oracle/PostgreSQL/MariaDB Vendor Pack
- 타 GPT 전담 보호 경로:
  - `cpf-docs/deliverables/**`
  - `cpf-docs/guides/**`
  - `cpf-docs/environment/docker/**`
  - `cpf-tools/environment/docker-development-test/**`

## 4. 이미 실행한 검증 — 동일 Source가 유지되면 분석 반복 금지

- QA38 Structure/Profile/Artifact/AutoConfiguration Gate
- Java class-scope duplicate member Gate
- Oracle/PostgreSQL/MariaDB SQL semantic parity Gate
- JDK 21 Pure Runtime Harness 33개 검사
- Messaging Named Binding/Schema + Service Identity Harness
- Java Main Source parser syntax check

이 결과는 현재 Overlay Source에 대한 개발자 자체검증이다. Java 25 전체 Build나 실제 Provider Runtime PASS로 확대 해석하지 않는다.

## 5. 한 번만 실행할 통합 검증 순서

1. **BASELINE**
   - `git rev-parse HEAD`, `git rev-parse origin/master`, `git status --short --branch`
   - 보호 경로와 Overlay 관리 경로 충돌 확인
2. **LOW-COST GATE**
   - `git diff --check`
   - Repository Hygiene, Secret, Ownership/Dependency, Route/OpenAPI, JSON/CSV, SQL/Generator/Evidence Gate
   - `pwsh -NoProfile -File .\cpf-tools\scripts\verify-qa38-starter-closure.ps1 -ProjectRoot .`
3. **JAVA 25**
   - Fresh cache 전체 `clean test publish`를 한 번 실행
   - 실패 시 해당 Module Focused Test 후 상위 전체 Build를 한 번만 재실행
4. **FRONTEND**
   - ADM/BZA 각각 clean `npm ci`, lint, typecheck, unit, production build
5. **DB**
   - Vendor별 전용 QA DB/Schema와 CPF Object 0건 증명
   - MariaDB → PostgreSQL → Oracle 순서
   - Fresh Install → Seed → Generated Domain → Runtime Query → Upgrade → Rollback/Forward Recovery → Reapply → Drift → Cleanup
6. **RUNTIME**
   - Kafka → RabbitMQ → JMS Provider → IBM MQ Customer Plugin Fixture
   - TCP → SFTP → Notification/Email/SMS → Quartz
   - ACK/Ordering/Duplicate/DLQ/Replay/Unknown Result/Reconcile
7. **FAULT/OTEL**
   - Toxiproxy, Multi-instance, Process Kill, Lease/Fencing, Credential Rotation, OTel
8. **BROWSER**
   - Playwright Chromium/Firefox/WebKit
9. **SUPPLY CHAIN**
   - SBOM, Vulnerability, License, Artifact Hash/Publication
10. **TRUTH**
   - Requirement/Scenario/Result/Evidence의 exact-SHA 일치
   - `development_status`와 `verification_status` 분리 유지

## 6. 실패 처리

같은 명령을 원인 분석 없이 반복하지 않는다.

`Root Cause → Requirement → Owner → Consumer → Source/SQL/Test/Config/Generator 수정 → Focused Test → 관련 상위 Lifecycle 1회 → Matrix/Evidence 갱신`

- Source Defect와 Environment Blocker를 분리한다.
- 환경 부재를 PASS나 Requirement 삭제로 처리하지 않는다.
- Proprietary IBM MQ Driver/Server를 Framework 기본 Artifact에 포함하지 않는다.
- Source 수정 시 Commit·Push하지 말고 Root Overlay와 재검증 최소 단위를 사용자에게 제공한다.

## 7. 완료 처리 금지 조건

- Core/Common과 Starter의 Dual Primary 잔존
- 실제 Consumer 없는 Starter 또는 Interface/Marker/Dependency만 존재
- Delete Manifest 적용 후 Legacy Source·Test·Resource 잔존
- Aggregate/Profile에 자체 Bean·업무 정책 존재
- 다중 Messaging Provider의 unnamed/default 모호성
- Oracle/PostgreSQL/MariaDB 중 하나라도 SQL/Lifecycle 누락
- TCP/SFTP/Messaging의 Unknown Result·Reconcile 누락
- 민감정보가 Evidence/로그에 원문으로 존재
- 최신 exact-SHA Evidence와 Artifact Hash 부재
- 보호 경로 변경
