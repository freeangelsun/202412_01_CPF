# CPF QA39 Codex 독립 통합 검수 요청서

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- QA38 Post-Push Review 기준 SHA: `54bcc10887a83b933685bff462c0b0d7df824923`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA39 자체 개발 요건: `cpf-docs/work/current/CPF_QA39_SELF_DEVELOPMENT_REQUIREMENTS.csv`
- QA38 Post-Push Defect: `cpf-docs/work/review/CPF_QA38_POST_PUSH_DEFECT_REGISTER.csv`
- Source Audit: `cpf-docs/work/review/CPF_QA38_POST_PUSH_SOURCE_AUDIT_MATRIX.csv`

검수 시작 시 실제 HEAD와 origin/master를 다시 확인하고, SHA가 달라졌으면 변경 영향만 먼저 대조한다.

## 2. Codex 역할

Codex는 개발 대체자가 아니라 독립 검수자다.

- 임의 재설계 금지
- 불필요한 전체 재작업 금지
- 이미 exact-SHA에서 성공 Evidence가 있는 검증은 반복 금지
- Source Defect와 Environment Blocker 분리
- 수정이 필요하면 영향 범위와 최소 재검증 단위를 제시
- 사용자 승인 없는 Git write 작업 금지

## 3. 완료 처리 금지 조건

다음이 하나라도 있으면 완료 승인 금지다.

- 미등록 제품 Module
- BOM/Artifact/Platform/Settings 불일치
- Internal Classpath 오류 또는 Starter Cycle
- Dead Bean/Consumer 없는 Product implementation
- Provider Binding과 generated dependency 불일치
- Product Leaf Test 0개
- 실제 3 Vendor lifecycle 미실행
- Java 25 전체 Build 미실행
- Runtime/Fault/Browser/Supply-chain 필수 검증 미실행
- Evidence SHA와 HEAD 불일치
- Matrix에 부분 구현·미구현·미검증·실패·재확인 필요 잔존
- Protected path 변경

## 4. 한 번만 실행할 통합 검증 순서

### 00 Baseline

1. `git rev-parse HEAD`
2. `git rev-parse origin/master`
3. `git status --short --branch`
4. Protected path 변경 확인
5. QA39 Requirement/Defect/Source Audit 정본 확인

### 01 저비용 Gate

1. `git diff --check`
2. Repository Hygiene
3. Secret Scan
4. `verify-qa39-post-push-closure.py`
5. Settings/Artifact/BOM/Platform parity
6. Internal FQN Classpath
7. Starter dependency SCC
8. AutoConfiguration target/Bean Consumer
9. JSON/CSV/SQL syntax
10. Generator/Profile lock parity

선행 Gate가 실패하면 대형 검증을 실행하지 말고 Source Defect로 기록한다.

### 02 Java Build/Publication

- Java 25 Fresh `GRADLE_USER_HOME`
- 전체 clean build/test
- disabled/skipped test 확인
- all publications to isolated repository
- generated POM/BOM inspection
- external consumer compile
- core-only consumer boot

### 03 Frontend

- ADM/BZA clean `npm ci`
- lint/typecheck/unit test/production build
- Generated Client drift
- Route/API/error/accessibility

### 04 DB

- MariaDB Fresh→Upgrade→Rollback→Reapply→Conflict
- PostgreSQL 동일
- Oracle 동일
- 전용 QA Schema 사용
- Cleanup 확인

### 05 Runtime/Fault

- Kafka/RabbitMQ/JMS/IBM MQ
- SFTP/FTPS/SMB/Object Storage
- TCP/ISO8583/Fixed-length
- Notification Email/SMS/Webhook
- gRPC/SOAP/Realtime/Quartz
- multi-instance/process kill/response loss/unknown result/reconcile
- Toxiproxy/OTel

### 06 Browser

- Chromium
- Firefox
- WebKit

### 07 Supply-chain

- SBOM
- Vulnerability
- License
- Artifact completeness
- SHA-256

### 08 Truth

- Requirement→Source/API/SQL/Test/Runtime/Evidence 추적
- 구현→Requirement/Owner/Consumer 역추적
- exact-SHA Evidence
- 상태 0잔존 Gate
- Working Tree와 generated artifact 정리

## 5. 최초 확인해야 할 QA38 결함

최소한 다음이 실제로 해소됐는지 확인한다.

1. Settings 미등록 Integration Module 7개
2. Artifact Catalog 누락 7개
3. BOM literal `${project.version}` 51개
4. Internal Classpath 오류 7개
5. HTTP/Runtime Control Cycle
6. Runtime Control God Starter
7. IBM MQ Plugin 미완결
8. Archive API Bean 단절
9. Batch 전체 subproject Runtime Profile
10. Generator Provider Binding metadata-only
11. QA38 Gate False Green
12. Stale exact-SHA Evidence
13. False Complete Matrix
14. Product Leaf Test 0개 18 Module

## 6. 검수 결과 형식

- 기준 SHA
- 검증 명령/환경/도구 버전/시작·종료/Exit
- Defect/Requirement별 PASS/FAIL/BLOCKED
- Source Defect와 Environment Blocker
- Artifact와 SHA-256
- Sanitization 여부
- 완료 승인 여부
- 수정 필요 시 최소 영향 범위와 재검증 순서
