# CPF 20260729_04 다음 ChatGPT 개발 세션 상세 인수인계

## 1. 역할과 완료 목표

- ChatGPT가 개발 주체다.
- Codex는 개발 완료 후 검수자다. Codex에게 기능 개발·SQL 작성·UI 구현·Architecture 변경을 넘기지 않는다.
- 이번 후속 세션의 목표는 최종 목표 162개와 QA Requirement/Scenario 전체에서 Source 상태의 `부분 구현`·`미구현`·`실패`를 제거하는 것이다.
- 실행하지 않은 검증은 `미검증`으로 남기되 구현 누락을 검증 문제로 위장하지 않는다.
- 사용자 승인 없이 Commit·Push·Branch·Tag·PR을 만들지 않는다.

## 2. 기준 정보

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 체크포인트 기준 SHA: `b8941577b99535ff3e64a4fad99b74bafa544227` (`20260728_07`)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` — 162개
- QA Master Ledger: `cpf-docs/quality/qa-20260729/CPF_FINAL_QA_MASTER_LEDGER_20260729.csv`
- QA Scenario: 387개
- 200개 검토 Matrix: `cpf-docs/quality/qa-20260729/CPF_QA_SCENARIO_200_CHECKPOINT_MATRIX_20260729_04.csv`
- 전체 Backlog: `cpf-docs/quality/qa-20260729/CPF_QA_387_SCENARIO_VALIDATION_BACKLOG_20260729_04.csv`

## 3. 적용 직후 첫 절차

1. ZIP을 프로젝트 Root `C:\dev\projects\jck\202412_01_CPF`에 덮어쓴다.
2. `git status --short`를 저장한다. 사용자 기존 변경을 임의 restore/delete하지 않는다.
3. 최신 `master` SHA를 다시 확인한다. 기준 SHA와 다르면 새 SHA의 실제 Source를 먼저 재대조한다.
4. 아래 구조 검증을 실행한다.

```powershell
python .\cpf-tools\verification\20260729_04\check_checkpoint_overlay.py .
```

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\verification\20260729_04\check-checkpoint-overlay.ps1 -ProjectRoot .
```

5. 다음 전체 Build를 실행하고 최초 오류부터 Owner Module에서 수정한다.

```powershell
.\gradlew.bat clean test assemble qualityGate --no-daemon --no-build-cache
```

## 4. 이번 체크포인트에 실제 포함된 개발 묶음

### 4.1 Cache/Redis

- `cpf-core`: Cache/Invalidation/Distributed Lock Public API
- `cpf-common`: Local/Redis Provider, Cache Aside, Single-flight, JDBC Durable Event, Redis TLS/Secret/Topology Guard
- `cpf-admin`: Provider 상태, Hit/Miss/Error, Evict/Reconcile API/UI
- 다음 세션은 실제 Standalone/Sentinel/Cluster, 장애 Fallback, 복구 후 checkpoint reconciliation을 실행하고 Evidence를 저장한다.

### 4.2 Streaming File Job

- `cpf-core`: Tabular Reader/Writer/Schema/Row/Result 계약
- `cpf-common`: CSV/XLSX Streaming Adapter 및 보안 제한
- `cpf-admin`: Download/Upload Job, Dry-run, Apply, 행별 결과, Retry/Cancel/Rollback, Retention Cleanup, Notification Rule Consumer
- 다음 세션은 Compile API 정합, 대용량·Formula·Macro·Zip Bomb·중단 복구·다중 Worker CAS를 실행한다.

### 4.3 BZA Tree·Action Permission

- 조직/메뉴 재귀 Tree, 검색, 고아/순환 탐지
- Permission 결과를 Raw JSON이 아닌 운영형 그룹 UI로 표시
- Frontend Button과 Backend Filter가 동일 Manifest를 사용
- V70/R70 3 Vendor Migration 포함
- 다음 세션은 5단계 Tree, cycle 저장 거부, READ/CREATE/UPDATE/DELETE/SIMULATE 직접 API 403, Browser 접근성을 검증한다.

### 4.4 Local Runtime

- Local Web: minimal/standard/full/integration Profile, Domain Registrar, Production/Remote Bind Guard
- Local Batch: Web와 별도 JVM, minimal/full Profile, Production Guard
- 다음 세션은 Application Class/Bean 충돌, 1 Port 정책, Profile 조합, Simulator 상태, 메모리 상한을 실제 Boot로 검증한다.

### 4.5 DB

- Oracle/PostgreSQL/MariaDB V69/V70/R69/R70 파일 포함
- Canonical `seed-model.json` 포함
- 다음 세션은 `generate-official-db-vendor-source.ps1`, `generate-migration-checksums.ps1`, Install SQL 생성 흐름을 실행해 정본과 생성 SQL을 동기화한다.
- Fresh Install → Upgrade → Rollback/Forward Recovery → Reapply를 Vendor별로 실행한다.

## 5. 다음 세션이 반드시 먼저 수정할 Source 결함

### P0-A. Runtime Control Raw Map 제거

현재 다음 공개 계약에 Raw Map이 남아 있다.

- `cpf-core/src/main/java/com/cpf/core/api/runtimecontrol/CpfRuntimeControlPlane.java`
- `cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmRuntimeControlController.java`
- `cpf-core/src/main/java/com/cpf/core/common/runtimecontrol/CpfRuntimeControlPlaneService.java`

`status`, `states`, `previewTargets`, `previewChange`, 오류 응답을 Typed DTO로 바꾸고 Repository 내부 Map만 Internal로 제한한다. Frontend도 Typed 응답에 맞춘다.

### P0-B. ADM Auth/Notification Raw Map 제거

- `AdmAuthController.me/logout`
- `AdmNotificationController.sendTest/retry/cancel`
- `AdmNotificationService`, `AdmNotificationOutboxService`

Typed Response를 만들고 OpenAPI Example·Frontend 타입·Test를 함께 변경한다. 인증 실패/DB 실패 시 예외 원문이나 성공 위장을 반환하지 않는다.

### P0-C. Generator Golden Template 실제 개발

이번 Overlay에는 Generator Source 변경이 없다. 이전 보고를 완료로 승계하지 않는다.

- 고정 `.reference` Package 제거
- Typed Query/Command Port 분리
- Public `Map<String,Object>` 제거
- Memory Adapter 실제 CRUD+Version+Idempotency 또는 운영 비활성
- `CpfHttpClient` 등 실제 CPF 공개 Service Call 계약 사용
- 임의 Domain 2개 생성→Build→Runtime→삭제→재생성
- Oracle/PostgreSQL/MariaDB SQL parity

### P0-D. DB Canonical/Checksum 통합

V69/V70 파일 존재만으로 완료 금지다. Canonical Schema, Install, Upgrade, Rollback, Verify, Checksum, Pack 생성 결과까지 동기화한다. MariaDB만의 비표준 checksum 정본을 남기지 않는다.

### P0-E. 전체 Consumer Compile

- `cpf-common` Boot 4.1 Redis/POI AutoConfiguration 조건
- `cpf-admin` File Job ↔ Notification Service 계약
- ADM/BZA Frontend Route/State/API Method 중복
- Local Runtime Application Class와 선택 Domain Registrar
- Runtime Control API 변경 Consumer

## 6. 그 다음 완료할 전체 제품 축

1. Notification Provider/Incident: EMAIL 기본 Provider, SMS/KakaoTalk SPI, Simulator, Secret Reference, Dedup/Threshold/Escalation/Quiet Hours, Incident 연결.
2. Batch: Metadata Catalog, Schedule CAS, Lease/Fencing, Checkpoint, Unknown Result, Retry/Skip/Cancel/Pause/Resume, ADM 운영·EDU.
3. Gateway/외부연계: 내부 호출 Gateway 재경유 금지, SSRF/Header Spoof/Auth, REST/TCP Fixed-length/SFTP/File/Callback API/SPI, Simulator, UNKNOWN 복구.
4. Generator/DB 3 Vendor 전체 Lifecycle.
5. 162개 Requirement 양방향 Traceability와 README/Guide/EDU/OpenAPI/JavaDoc.
6. Repository Root/Dead Code/Stale Evidence 정리.

## 7. QA 처리 방식

- 200개 Matrix는 검토 완료 체크포인트다. `verification_status`를 실행 없이 PASS로 바꾸지 않는다.
- 387개 Backlog를 모두 실행·판정한다.
- 실패 시 Codex가 수정하지 않는다. Scenario ID, SHA, 명령, Expected/Actual, 최초 실패, Evidence를 개발 세션으로 반환한다.
- Source/API/SQL/Test/Frontend/Script/Evidence 중 하나라도 끊기면 완료 금지다.

## 8. 보호할 기존 성공 기능

- Transaction ID·표준 Header·오류 계약
- ADM/BZA Session·Permission fail-closed
- Runtime Control Durable Delivery/ACK/Drift/Rollback 원장
- Batch Runtime 분리와 Center-Cut Ownership
- EXS Generated Domain 정책
- 공식 DB Oracle/PostgreSQL/MariaDB 3종
- Root Hygiene와 `cpf-` Module 명명

## 9. 최종 완료 판정

- Source 상태에 `부분 구현`·`미구현`·`실패`가 없어야 한다.
- 162개 Requirement 각각 Owner/Consumer/API/SQL/Test/Runtime/Evidence가 연결돼야 한다.
- 최신 master exact SHA에서 필수 Build/Test/DB/Browser/Multi-instance/Generator Evidence가 있어야 한다.
- 미실행 검증은 `미검증`으로 남기며 최종 전체 완료를 선언하지 않는다.
