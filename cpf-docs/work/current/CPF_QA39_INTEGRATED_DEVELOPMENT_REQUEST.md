# CPF QA39 자체 개발 통합 요청

## 1. 목적

최신 `master` `54bcc10887a83b933685bff462c0b0d7df824923`에서 QA38 Push 이후 확인된 Source·Build·Truth 결함을 제거하고, QA38 Requirement를 실제 Build·Consumer·Runtime·Evidence 기준으로 완결한다.

이번 작업은 신규 기능 확장이 아니라 **False Complete 제거와 상용 Runtime Closure**가 목적이다.

## 2. 작업 시작 기준

작업 시작 전 반드시 다음을 다시 확인한다.

1. 실제 `HEAD`, `origin/master`, Working Tree
2. `CPF_QA38_POST_PUSH_SOURCE_BY_SOURCE_REVIEW.md`
3. `CPF_QA38_POST_PUSH_DEFECT_REGISTER.csv`
4. `CPF_QA38_POST_PUSH_REQUIREMENT_RECALIBRATION.csv`
5. `CPF_QA39_SELF_DEVELOPMENT_REQUIREMENTS.csv`
6. `CPF_QA38_TO_QA39_HANDOVER.md`
7. 최상위 정본 `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
8. Protected path와 다른 작업자의 변경

사용자 승인 없이 Commit, Push, Branch, Tag, PR, Reset, Restore, Stash, Clean을 수행하지 않는다.

## 3. 완료 금지 조건

다음 중 하나라도 남으면 전체 완료라고 쓰지 않는다.

- Settings에 포함되지 않은 제품 Module
- Artifact Catalog/BOM/Platform Properties/Publication 불일치
- 내부 FQN Compile Classpath 오류
- Starter 순환 의존
- 실제 Bean 또는 Consumer 없는 Product implementation
- Provider Binding과 생성 Dependency 불일치
- Test 0개 Product Leaf
- 3 Vendor Lifecycle 미검증을 완료로 표시
- Java 25 전체 Build 미실행을 PASS로 표시
- exact-SHA가 다른 Evidence
- 부분 구현·미구현·실패·미검증 Requirement를 완료로 변경
- Protected path 변경
- 실제 Consumer 연결 없는 dependency-only migration

## 4. 통합 구현 순서

### Stage 00 — Truth Reset

- QA38 156개 Requirement를 최신 SHA 기준으로 재판정한다.
- 기존 완료 값을 자동 승계하지 않는다.
- Defect와 Requirement를 하나의 Backlog로 병합한다.

### Stage 01 — Build/Artifact Graph

- 7개 미등록 Integration Module을 정식 등록한다.
- Artifact Registry 단일 정본으로 Settings/BOM/Catalog/Platform Properties를 생성한다.
- BOM literal version을 수정하고 generated POM을 검증한다.
- Internal FQN Classpath Gate와 Dependency SCC Gate를 먼저 통과한다.

### Stage 02 — Ownership/Bean Closure

- `CpfRuntimeCanonicalHash` neutral owner를 확정한다.
- HTTP Service Registry JDBC를 별도 Owner로 분리한다.
- Runtime Control God Starter를 SPI와 Leaf Applier로 분해한다.
- Archive Core API Bean 단절을 복구한다.
- Batch Runtime Profile을 실제 executable subproject로 제한한다.
- Starter 내부 Package와 Public compatibility 경계를 정리한다.

### Stage 03 — Generator/Profile

- Provider Binding이 실제 Leaf dependency를 결정하도록 수정한다.
- Profile×Provider 조합별 positive/negative fixture를 생성한다.
- `resolvedStarters`, lock, generated build.gradle, manifest를 일치시킨다.

### Stage 04 — Provider Runtime

다음 Capability를 각각 독립 완료 단위로 처리한다.

1. IBM MQ/JMS/RabbitMQ/Kafka 양방향 Runtime
2. SFTP
3. TCP
4. Notification Core
5. Email
6. SMS Shared Runtime
7. FTPS
8. gRPC
9. Object Storage
10. SMB
11. SOAP
12. Webhook
13. Realtime
14. Quartz

각 Capability는 정상·오류·경계·부분 실패·Unknown Result·복구·다중 인스턴스·보안·운영 조회를 포함한다.

### Stage 05 — DB 3 Vendor

- Canonical Install/Upgrade/Rollback/Reapply/Conflict 경로를 만든다.
- Oracle, PostgreSQL, MariaDB Fresh QA Schema에서 실제 실행한다.
- MySQL, MSSQL, H2를 공식 완료 증적에 사용하지 않는다.

### Stage 06 — Consumer

- ADM/BZA/Gateway/Batch/Reference/Member/Generated Domain/Plugin Fixture를 실제로 기동한다.
- Dependency 추가가 아니라 Bean/Endpoint/Job/Screen을 실제 연결한다.
- ADM/BZA는 Generated OpenAPI Client와 운영 화면을 연결한다.

### Stage 07 — 통합 검증

한 번의 통합 순서로 실행한다.

1. Repository Hygiene/Secret/Protected Path
2. Settings/Artifact/BOM/ClassPath/SCC/Bean Consumer Gate
3. Java 25 Fresh Cache 전체 Build·Test
4. Publication/BOM external consumer
5. ADM/BZA Frontend
6. 3 Vendor DB Lifecycle
7. Broker/SFTP/TCP/Notification/Quartz Runtime
8. Toxiproxy/Process Kill/Multi-instance/Unknown Result
9. Playwright 3 Browser
10. SBOM/Vulnerability/License/Artifact Hash
11. Truth Matrix/Evidence exact-SHA

## 5. 완료 Acceptance

- `QA39_SELF_DEVELOPMENT_REQUIREMENTS.csv` 44건 모두 완료
- `QA38_POST_PUSH_DEFECT_REGISTER.csv` 39건 모두 해소
- 부분 구현 0
- 미구현 0
- 실패 0
- 미검증 0
- 재확인 필요 0
- Settings 미등록 제품 Module 0
- Internal Classpath 오류 0
- Starter Dependency Cycle 0
- Product Leaf Test 0개 Module 0
- Dead Product implementation 0
- Evidence SHA 불일치 0
- Protected path 변경 0
- Java/Frontend/DB/Runtime/Supply-chain 통합 검증 성공
- Codex 독립 검수에서 Source 수정 없이 통과

## 6. 반드시 보호할 기능

- 기존 Public API/SPI와 거래 Context/Error/Masking/Identifier
- 기존 Security/Kafka/Cache/Observability/Resilience/Feature Flag/Secret 성공 기능
- Fixed-length/ISO8583/TCP Codec 성공 시나리오
- Batch 독립 executable과 명시적 publication repository
- 공식 DB 3 Vendor
- 다른 GPT Protected path

## 7. 산출물

작업 종료 시 Root Overlay ZIP 하나에 다음을 포함한다.

- 변경 Source/SQL/Test/Config/Generator/Script
- 사전 리뷰
- 자체 개발 요건 상태
- 사후 Source-by-source 리뷰
- Defect/Requirement/Scenario/Result Matrix
- exact-SHA Sanitized Evidence
- Handover/Continuity
- Codex 독립 검수 요청
- Change/Delete/Empty Directory Manifest
- 파일별 SHA-256
