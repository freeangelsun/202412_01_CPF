# CPF Enterprise QA·개선 통합 요청서 — 20260728_03

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 최신 검수 기준 Commit: `9fa4bb0f7b83ea73615e019a09af9ffac2bc89c0` (`20260728_03`)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 기존 QA 정본: `cpf-tools/verification/20260728_01/qa-source/CPF_ENTERPRISE_FULL_QA_*_20260728.*`
- 사용자 승인 없는 Commit·Push·Branch·Tag·Release 금지

## 2. 최종 검수 판정

**현재 판정: 실패. Commercial Release·GA·전체 QA 완료 판정 금지.**

최신 Push에는 Runtime Control, ADM Runtime Change Center, Gateway Streaming, Batch Runtime Policy, DB Migration, Generator 보강이 실제 Source에 반영되어 있다. 그러나 최신 SHA에 CI/Workflow가 없고, Current/Handover/Ledger가 과거 Overlay 기준이며, Java25 전체 Build·WAS·MariaDB Lifecycle·Browser·다중 인스턴스·분산 Batch·전체 QA 재판정 Evidence가 없다. 또한 알림 DB Vendor 종속, Mock-only 발송, Instance Group 모델 부족, Batch 제어 화면의 조회 중심 구조, Agent 로그 Archive Lifecycle과 마스킹 부족 등 직접 결함이 확인됐다.

## 3. 기존 QA와 이번 Delta의 관계

- 기존 **1,214개 QA 요구사항과 201개 실행 시나리오는 폐기하지 않는다.**
- 기존 항목은 최신 SHA에서 전부 재판정한다.
- 이번 문서는 기존 QA 이후 Push에서 발견한 결함과 이번 대화에서 확정한 제품 요구를 중복 제거해 추가한 Delta다.
- 신규 Delta 요구사항: **535개**
- 신규 Delta 실행 시나리오: **168개**
- 병합 후 추적 대상: 요구사항 **1,749개**, 실행 시나리오 **369개**, 총 **2,118개**

## 4. 최신 Push 직접 검수 핵심 결과

| 심각도 | 발견사항 | 판정 근거 |
|---|---|---|
| P0 | 최신 SHA CI 없음 | GitHub combined status와 workflow run이 0건이다. |
| P0 | 정본 SHA 드리프트 | Current Request·Validation Ledger·Handover가 `ecaddd...` Overlay 기준이다. |
| P0 | Evidence 불충분 | 일부 Evidence가 `..._PASS` 한 줄뿐이고 명령·환경·출력·시간이 없다. |
| P0 | 완료/미검증 모순 | Remaining Matrix는 완료로 표시하면서 Browser·DB·분산 Runtime은 미검증이다. |
| P0 | Notification DB 종속 | `LIMIT`, `ON DUPLICATE KEY`, `LAST_INSERT_ID()` 등 MariaDB 전용 SQL을 직접 사용한다. |
| P0 | Notification 인증 경계 | 인증된 operator가 없을 때 요청값/`ADM` fallback이 가능하다. |
| P0 | Notification Runtime 부족 | Mock sender 동기 호출만 확인되며 Outbox·Worker·Retry/DLQ·Email/SMS 구현이 부족하다. |
| P0 | Instance Group 부분 구현 | Group은 parent/environment 중심이며 Domain·Cluster·Application Group·운영담당이 정식 필드가 아니다. |
| P0 | ADM Batch 제어 부분 구현 | 다수 메뉴가 Generic 조회 View이며 강제 Lock·Ghost 해제·선택 재처리 Command가 확인되지 않는다. |
| P0 | 로그 다운로드 부분 구현 | Agent ZIP은 존재하지만 임시파일 자동삭제·다운로드 마스킹·ADM 다중 인스턴스 수집이 부족하다. |
| P1 | Local 개발 Runtime 부족 | Web All-in-One 1 JVM/1 Port와 별도 Local Batch 표준 Launcher가 확인되지 않는다. |
| P1 | Reference 검증 약함 | `cpf-reference`가 선택 Mount이며 상위 실행 Guide가 확인되지 않는다. |

## 5. 현재 환경 검증 정책

| 대상 | 현재 정책 | 허용 판정 |
|---|---|---|
| WAS·MariaDB | 현재 개발기간 실제 실행 대상 | 실제 Build/API/DB/Browser/장애 시나리오 Evidence 전에는 완료 금지 |
| PostgreSQL·Oracle | 공식 구현·Canonical/Migration/Query parity 유지, 실환경은 추후 | 현재 `미검증`; 정적 parity만으로 완료 금지 |
| Redis·Kafka·RabbitMQ·JMS·SFTP·SMS 등 | 계약·기본 구현·AutoConfiguration·Properties·Mock/Fake/Simulator·Contract Test까지 구현 | 실제품 연결 전 `미검증`; 구현 누락은 `부분 구현/미구현` |
| Email | Mock/Local Capture + Embedded SMTP는 현재 자동 통합 Test 가능 | Embedded SMTP 성공과 외부 SMTP 실도착을 분리 판정 |
| Browser·다중 Instance | WAS와 MariaDB로 가능한 범위는 현재 실행 | 미실행은 `미검증` |

## 6. 확정 Architecture 결정

### 6.1 Local 개발 Topology

- Web 개발: ADM/BZA/Gateway/선택 업무 API를 **1 JVM·1 HTTP Port**로 구성한다.
- 내부 동일 JVM 호출은 HTTP loopback이 아니라 Local Facade를 사용한다.
- Batch는 기본적으로 **별도 JVM 1개**로 실행한다.
- Local Batch는 Scheduler·Worker·Runner를 선택 통합하고 Agent·Center-Cut·다중 Worker는 필요할 때 추가한다.
- TCP/gRPC 실제 Listener는 기본 비활성/Simulator이며 integration profile에서만 추가 Port를 사용한다.

### 6.2 Batch 분류

일반 Job, Scheduled, On-Demand, Center-Cut, File-Triggered, Event-Triggered, Remote Partition/Multi-Worker, Workflow, Recovery/Reprocessing, Housekeeping, Agent/Host, Migration/Initial Load로 세분화한다.

### 6.3 예제의 제품 지위

예제는 장난감 Sample이 아니라 CPF 기능을 검증하는 실행 가능한 제품 산출물이다.

```text
Requirement → Public API/SPI → 실제 구현 → Reference/EDU → 자동 Test
→ ADM 운영 확인 → JavaDoc/OpenAPI/Guide → 최신 SHA Evidence
```

### 6.4 명시적 제외

- IntelliJ/VS Code/Eclipse 전용 Plugin·Extension 개발은 범위에서 제외한다.
- 대신 기능 중심 Package, 일관된 Naming, `package-info.java`, 한글 JavaDoc, 주석, 기능 Matrix를 강제한다.

## 7. 작업 묶음과 순서

| 순서 | Work Package | 목적 | Delta 수량 |
|---:|---|---|---:|
| 1 | `WP00` | 최신 SHA 정본·CI·Evidence 재개방 | 13 |
| 2 | `WP01` | Architecture·Ownership·Public Boundary 선행 수리 | 17 |
| 3 | `WP02` | 개발용 Local Web All-in-One + 별도 Local Batch | 21 |
| 4 | `WP03` | batDB Job Metadata·실행 정본 | 30 |
| 5 | `WP04` | Batch 유형별 Runtime | 18 |
| 6 | `WP05` | ADM Batch 실시간 제어·Ghost 복구 | 36 |
| 7 | `WP06` | Batch Reference/EDU 실전 예제 | 62 |
| 8 | `WP07` | CPF 전체 기능 Reference/EDU 카탈로그 | 86 |
| 9 | `WP08` | 공통 Integration/Client Framework | 40 |
| 10 | `WP09` | 통합 Registry/Metadata Platform | 32 |
| 11 | `WP10` | 관제·알림·Incident·Email/SMS | 49 |
| 12 | `WP11` | 도메인/그룹별 Instance·로그 관리 | 48 |
| 13 | `WP12` | JavaDoc·주석·OpenAPI·Guide QA | 24 |
| 14 | `WP13` | 현재 환경별 실검증·미검증 정책 | 20 |
| 15 | `WP14` | Generator·Generated Domain 동기화 | 15 |
| 16 | `WP15` | 통합 실행·회귀·Evidence·Hygiene | 24 |

**수행 순서:** WP00 정본/Evidence → WP01 Architecture → WP02 Local Runtime → WP03~06 Batch/EDU → WP08 Integration → WP09 Metadata → WP10 Alert → WP11 Instance/Log → WP12 Docs → WP13 환경검증 → WP14 Generator → WP15 통합 검증.

같은 Source·Module·DB·기능을 여러 번 열지 않도록 Root Cause와 Consumer가 같은 항목은 하나의 Change Set에서 Source·SQL·Test·Guide·Evidence까지 닫는다.

## 8. Work Package별 QA 목록

### WP00 — 최신 SHA 정본·CI·Evidence 재개방

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-FND-001` | 실패 | P0 | 최신 master SHA Required CI 부재 |
| `CPF-QA-D03-FND-002` | 실패 | P0 | Current Request 기준 Commit 드리프트 |
| `CPF-QA-D03-FND-003` | 실패 | P0 | Validation Ledger 최신 SHA 불일치 |
| `CPF-QA-D03-FND-004` | 실패 | P0 | Overlay 작업 디렉터리 PASS를 Repository PASS로 오인 |
| `CPF-QA-D03-FND-005` | 실패 | P0 | 한 줄 PASS Evidence 증거 불충분 |
| `CPF-QA-D03-FND-006` | 미검증 | P0 | 기존 1,214 QA 요구사항 최신 SHA 전수 재판정 미실행 |
| `CPF-QA-D03-FND-007` | 미검증 | P0 | 기존 201 실행 시나리오 최신 SHA 전수 실행 미완료 |
| `CPF-QA-D03-FND-008` | 실패 | P0 | 완료 Matrix와 미검증 조건의 모순 |
| `CPF-QA-D03-FND-009` | 부분 구현 | P1 | 중간 Overlay 산출물 Current 영역 잔존 |
| `CPF-QA-D03-FND-010` | 미검증 | P0 | Java25·Gradle9.1 전체 clean test assemble 미실행 |
| `CPF-QA-D03-FND-011` | 미검증 | P0 | WAS 실제 통합 실행 Evidence 부재 |
| `CPF-QA-D03-FND-012` | 미검증 | P0 | MariaDB 실제 Lifecycle·Runtime 검증 미완료 |
| `CPF-QA-D03-FND-013` | 미검증 | P1 | PostgreSQL·Oracle 실환경 검증 지연 정책 미정 |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP01 — Architecture·Ownership·Public Boundary 선행 수리

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-ARCH-001` | 실패 | P0 | cpf-admin의 cpf-core common 구현 Package 직접 의존 |
| `CPF-QA-D03-ARCH-002` | 재확인 필요 | P0 | Public API·SPI·Internal Package 경계 강제 |
| `CPF-QA-D03-ARCH-003` | 재확인 필요 | P0 | 외부 Module의 core common/internal 직접 import 전수 제거 |
| `CPF-QA-D03-ARCH-004` | 재확인 필요 | P0 | ADM의 다른 Owner DB 직접 변경 금지 |
| `CPF-QA-D03-ARCH-005` | 재확인 필요 | P0 | BZA의 업무 Owner Command API 경계 |
| `CPF-QA-D03-ARCH-006` | 재확인 필요 | P0 | Batch의 Business Public Contract 의존 |
| `CPF-QA-D03-ARCH-007` | 재확인 필요 | P0 | Gateway의 Public Header·Security Contract 사용 |
| `CPF-QA-D03-ARCH-008` | 재확인 필요 | P0 | Topology-independent 계약과 Runtime 구현 분리 |
| `CPF-QA-D03-ARCH-009` | 재확인 필요 | P0 | 실제 Consumer 없는 Interface·Adapter 탐지 |
| `CPF-QA-D03-ARCH-010` | 재확인 필요 | P0 | 중복 구현·Dead Code·Legacy 호환물 제거 |
| `CPF-QA-D03-ARCH-011` | 재확인 필요 | P0 | Module 간 순환·역방향 의존 Gate |
| `CPF-QA-D03-ARCH-012` | 재확인 필요 | P0 | Provider 유형·예외의 Public API 누출 차단 |
| `CPF-QA-D03-ARCH-013` | 재확인 필요 | P0 | Generated Domain과 CPF 본체 의존 방향 검증 |
| `CPF-QA-D03-ARCH-014` | 재확인 필요 | P0 | Runtime Control Plane Owner와 저장소 경계 |
| `CPF-QA-D03-ARCH-015` | 재확인 필요 | P0 | Notification Event Contract와 ADM 정책 Owner 분리 |
| `CPF-QA-D03-ARCH-016` | 재확인 필요 | P0 | Log Agent·ADM Control Plane 책임 분리 |
| `CPF-QA-D03-ARCH-017` | 재확인 필요 | P0 | Batch Control Server·Scheduler·Worker 책임 분리 |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP02 — 개발용 Local Web All-in-One + 별도 Local Batch

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-LOCAL-001` | 부분 구현 | P0 | Local Web All-in-One 개발 Runtime 부재 |
| `CPF-QA-D03-LOCAL-002` | 미구현 | P1 | Local Web All-in-One Launcher |
| `CPF-QA-D03-LOCAL-003` | 미구현 | P1 | Local Web 1 JVM·1 HTTP Port |
| `CPF-QA-D03-LOCAL-004` | 미구현 | P1 | ADM·BZA·Gateway·업무 API 선택 조합 |
| `CPF-QA-D03-LOCAL-005` | 미구현 | P1 | Local 내부 호출 HTTP loopback 금지와 Local Facade 사용 |
| `CPF-QA-D03-LOCAL-006` | 미구현 | P1 | minimal 개발 profile |
| `CPF-QA-D03-LOCAL-007` | 미구현 | P1 | standard 개발 profile |
| `CPF-QA-D03-LOCAL-008` | 미구현 | P1 | full 개발 profile |
| `CPF-QA-D03-LOCAL-009` | 미구현 | P1 | integration 개발 profile |
| `CPF-QA-D03-LOCAL-010` | 미구현 | P1 | Module enable/disable flags |
| `CPF-QA-D03-LOCAL-011` | 미구현 | P1 | Local Batch 별도 JVM Launcher |
| `CPF-QA-D03-LOCAL-012` | 미구현 | P1 | Local Scheduler·Worker·Runner 선택 통합 |
| `CPF-QA-D03-LOCAL-013` | 미구현 | P1 | Embedded Batch unit/debug 전용 mode |
| `CPF-QA-D03-LOCAL-014` | 미구현 | P1 | Agent·Center-Cut·다중 Worker 선택 기동 |
| `CPF-QA-D03-LOCAL-015` | 미구현 | P1 | TCP·gRPC 실제 Listener opt-in 추가 Port |
| `CPF-QA-D03-LOCAL-016` | 미구현 | P1 | Redis·Kafka 미설치 Fake/Simulator mode |
| `CPF-QA-D03-LOCAL-017` | 미구현 | P1 | MariaDB 단일 Instance Schema 분리 개발 mode |
| `CPF-QA-D03-LOCAL-018` | 미구현 | P1 | 저사양 PC 메모리 상한 |
| `CPF-QA-D03-LOCAL-019` | 미구현 | P1 | 기동시간·Hot Reload 개발 생산성 |
| `CPF-QA-D03-LOCAL-020` | 미구현 | P1 | Production topology 회귀 검증 연결 |
| `CPF-QA-D03-LOCAL-021` | 미구현 | P1 | Local profile의 Production 사용 차단 |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP03 — batDB Job Metadata·실행 정본

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-BATDB-001` | 부분 구현 | P0 | Job Definition 정본과 Version |
| `CPF-QA-D03-BATDB-002` | 부분 구현 | P0 | Job Type 일반·센터컷·온디맨드·스케줄 분류 |
| `CPF-QA-D03-BATDB-003` | 부분 구현 | P0 | Step Definition과 실행 순서 |
| `CPF-QA-D03-BATDB-004` | 부분 구현 | P0 | Handler Stable ID와 Source 구현 연결 |
| `CPF-QA-D03-BATDB-005` | 부분 구현 | P0 | Reader·Processor·Writer Metadata |
| `CPF-QA-D03-BATDB-006` | 부분 구현 | P0 | Job Parameter Schema·Validation·Masking |
| `CPF-QA-D03-BATDB-007` | 부분 구현 | P0 | Schedule·Cron·Calendar Metadata |
| `CPF-QA-D03-BATDB-008` | 부분 구현 | P0 | On-Demand 요청·승인 Metadata |
| `CPF-QA-D03-BATDB-009` | 부분 구현 | P0 | File/Event Trigger Metadata |
| `CPF-QA-D03-BATDB-010` | 부분 구현 | P0 | Job Instance·Execution·Status |
| `CPF-QA-D03-BATDB-011` | 부분 구현 | P0 | Step Execution·처리 건수 |
| `CPF-QA-D03-BATDB-012` | 부분 구현 | P0 | Chunk·Checkpoint·재시작 위치 |
| `CPF-QA-D03-BATDB-013` | 부분 구현 | P0 | Skip·Retry·오류 건 저장 |
| `CPF-QA-D03-BATDB-014` | 부분 구현 | P0 | 재처리 대상·범위·상태 |
| `CPF-QA-D03-BATDB-015` | 부분 구현 | P0 | Partition·Worker 할당 |
| `CPF-QA-D03-BATDB-016` | 부분 구현 | P0 | Worker Heartbeat·Lease·Fencing |
| `CPF-QA-D03-BATDB-017` | 부분 구현 | P0 | 중복 실행 방지 Lock |
| `CPF-QA-D03-BATDB-018` | 부분 구현 | P0 | 강제 잠금·만료·해제 이력 |
| `CPF-QA-D03-BATDB-019` | 부분 구현 | P0 | Ghost Execution 판정 Metadata |
| `CPF-QA-D03-BATDB-020` | 부분 구현 | P0 | Center-Cut 대상·분할·배정 Metadata |
| `CPF-QA-D03-BATDB-021` | 부분 구현 | P0 | 실행 우선순위·동시성 정책 |
| `CPF-QA-D03-BATDB-022` | 부분 구현 | P0 | 정지·재개·취소·강제종료 이력 |
| `CPF-QA-D03-BATDB-023` | 부분 구현 | P0 | 운영 승인·사유·명령자 감사 |
| `CPF-QA-D03-BATDB-024` | 부분 구현 | P0 | 실행 결과 Unknown·Recovery 상태 |
| `CPF-QA-D03-BATDB-025` | 부분 구현 | P0 | 보관·Archive·Cleanup 정책 |
| `CPF-QA-D03-BATDB-026` | 부분 구현 | P0 | DB Job Metadata와 Spring Batch 표준 테이블 정합성 |
| `CPF-QA-D03-BATDB-027` | 부분 구현 | P0 | batDB Migration·Upgrade·Rollback |
| `CPF-QA-D03-BATDB-028` | 부분 구현 | P0 | batDB Index·FK·대량 조회 성능 |
| `CPF-QA-D03-BATDB-029` | 부분 구현 | P0 | Job Metadata Generator Template |
| `CPF-QA-D03-BATDB-030` | 부분 구현 | P0 | Job Metadata ADM 조회·검색·Paging |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP04 — Batch 유형별 Runtime

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-BAT-001` | 부분 구현 | P0 | 일반 Job Batch |
| `CPF-QA-D03-BAT-002` | 부분 구현 | P0 | Scheduled Batch |
| `CPF-QA-D03-BAT-003` | 부분 구현 | P0 | On-Demand Batch |
| `CPF-QA-D03-BAT-004` | 부분 구현 | P0 | Center-Cut Batch |
| `CPF-QA-D03-BAT-005` | 부분 구현 | P0 | File-Triggered Batch |
| `CPF-QA-D03-BAT-006` | 부분 구현 | P0 | Event-Triggered Batch |
| `CPF-QA-D03-BAT-007` | 부분 구현 | P0 | Remote Partition Batch |
| `CPF-QA-D03-BAT-008` | 부분 구현 | P0 | Multi-Worker Batch |
| `CPF-QA-D03-BAT-009` | 부분 구현 | P0 | Workflow Batch 선후행 |
| `CPF-QA-D03-BAT-010` | 부분 구현 | P0 | Workflow Batch 병렬·합류 |
| `CPF-QA-D03-BAT-011` | 부분 구현 | P0 | Recovery/Reprocessing Batch |
| `CPF-QA-D03-BAT-012` | 부분 구현 | P0 | Housekeeping Batch |
| `CPF-QA-D03-BAT-013` | 부분 구현 | P0 | Agent/Host 작업 Batch |
| `CPF-QA-D03-BAT-014` | 부분 구현 | P0 | Migration/Initial Load Batch |
| `CPF-QA-D03-BAT-015` | 부분 구현 | P0 | End-of-Day·Center-Cut 업무일 Batch |
| `CPF-QA-D03-BAT-016` | 부분 구현 | P0 | 대외연계 호출 포함 Batch |
| `CPF-QA-D03-BAT-017` | 부분 구현 | P0 | 장시간 실행 Batch Heartbeat |
| `CPF-QA-D03-BAT-018` | 부분 구현 | P0 | 결과 불명 외부 호출 복구 Batch |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP05 — ADM Batch 실시간 제어·Ghost 복구

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-BATOPS-001` | 부분 구현 | P0 | ADM Batch 메뉴가 조회 중심 Generic View |
| `CPF-QA-D03-BATOPS-002` | 부분 구현 | P0 | Job 즉시 강제 실행 |
| `CPF-QA-D03-BATOPS-003` | 부분 구현 | P0 | 예약 실행 |
| `CPF-QA-D03-BATOPS-004` | 부분 구현 | P0 | 파라미터 지정 재실행 |
| `CPF-QA-D03-BATOPS-005` | 부분 구현 | P0 | 실패 Job 재시작 |
| `CPF-QA-D03-BATOPS-006` | 부분 구현 | P0 | 특정 Step 재시작 |
| `CPF-QA-D03-BATOPS-007` | 부분 구현 | P0 | 특정 Chunk 재처리 |
| `CPF-QA-D03-BATOPS-008` | 부분 구현 | P0 | 특정 업무 대상만 재처리 |
| `CPF-QA-D03-BATOPS-009` | 부분 구현 | P0 | 실행 일시정지 |
| `CPF-QA-D03-BATOPS-010` | 부분 구현 | P0 | 실행 재개 |
| `CPF-QA-D03-BATOPS-011` | 부분 구현 | P0 | 실행 취소 |
| `CPF-QA-D03-BATOPS-012` | 부분 구현 | P0 | 강제 종료 |
| `CPF-QA-D03-BATOPS-013` | 부분 구현 | P0 | Job 신규 실행 차단 |
| `CPF-QA-D03-BATOPS-014` | 부분 구현 | P0 | Job 강제 Lock |
| `CPF-QA-D03-BATOPS-015` | 부분 구현 | P0 | Lock 만료시간 설정 |
| `CPF-QA-D03-BATOPS-016` | 부분 구현 | P0 | 안전한 Lock 해제 |
| `CPF-QA-D03-BATOPS-017` | 부분 구현 | P0 | Ghost RUNNING 탐지 |
| `CPF-QA-D03-BATOPS-018` | 부분 구현 | P0 | Heartbeat 단절 Worker 탐지 |
| `CPF-QA-D03-BATOPS-019` | 부분 구현 | P0 | 만료 Lease·Lock 탐지 |
| `CPF-QA-D03-BATOPS-020` | 부분 구현 | P0 | 존재하지 않는 Owner Instance 탐지 |
| `CPF-QA-D03-BATOPS-021` | 부분 구현 | P0 | Ghost 실행 실패/복구대기 전환 |
| `CPF-QA-D03-BATOPS-022` | 부분 구현 | P0 | 미완료 Partition 재배정 |
| `CPF-QA-D03-BATOPS-023` | 부분 구현 | P0 | Scheduler Enable·Disable |
| `CPF-QA-D03-BATOPS-024` | 부분 구현 | P0 | 업무그룹·인스턴스 실행 차단 |
| `CPF-QA-D03-BATOPS-025` | 부분 구현 | P0 | Center-Cut 중지·재개·재분배 |
| `CPF-QA-D03-BATOPS-026` | 부분 구현 | P0 | On-Demand 승인·취소 |
| `CPF-QA-D03-BATOPS-027` | 부분 구현 | P0 | Worker 격리·복귀·Drain |
| `CPF-QA-D03-BATOPS-028` | 부분 구현 | P0 | 실행 우선순위 변경 |
| `CPF-QA-D03-BATOPS-029` | 부분 구현 | P0 | 동시 실행 수 변경 |
| `CPF-QA-D03-BATOPS-030` | 부분 구현 | P0 | 오류 건 Skip·Retry·보류 |
| `CPF-QA-D03-BATOPS-031` | 부분 구현 | P0 | Checkpoint 지정 재시작 |
| `CPF-QA-D03-BATOPS-032` | 부분 구현 | P0 | Unknown Result 상태 확정 |
| `CPF-QA-D03-BATOPS-033` | 부분 구현 | P0 | 운영 명령 영향 Preview |
| `CPF-QA-D03-BATOPS-034` | 부분 구현 | P0 | 위험 조치 사유·승인 |
| `CPF-QA-D03-BATOPS-035` | 부분 구현 | P0 | CAS·Version·Fencing 동시 보호 |
| `CPF-QA-D03-BATOPS-036` | 부분 구현 | P0 | 명령 결과·변경 전후 감사 |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP06 — Batch Reference/EDU 실전 예제

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-EDU-001` | 부분 구현 | P0 | cpf-reference 기본 검증 경로 제외 |
| `CPF-QA-D03-EDU-002` | 부분 구현 | P1 | cpf-reference 상위 실행 Guide 부재 |
| `CPF-QA-D03-BATEDU-001` | 부분 구현 | P1 | 10만 건 DB Cursor Chunk 처리 예제 |
| `CPF-QA-D03-BATEDU-002` | 부분 구현 | P1 | 10만 건 DB Paging 처리 예제 |
| `CPF-QA-D03-BATEDU-003` | 부분 구현 | P1 | 10만 건 Keyset/Cursor 처리 예제 |
| `CPF-QA-D03-BATEDU-004` | 부분 구현 | P1 | Bulk Insert 예제 |
| `CPF-QA-D03-BATEDU-005` | 부분 구현 | P1 | Bulk Update 예제 |
| `CPF-QA-D03-BATEDU-006` | 부분 구현 | P1 | Upsert 예제 |
| `CPF-QA-D03-BATEDU-007` | 부분 구현 | P1 | 건별 Handler 예제 |
| `CPF-QA-D03-BATEDU-008` | 부분 구현 | P1 | Chunk Handler 예제 |
| `CPF-QA-D03-BATEDU-009` | 부분 구현 | P1 | Handler Chain 예제 |
| `CPF-QA-D03-BATEDU-010` | 부분 구현 | P1 | 검증→변환→저장 Pipeline 예제 |
| `CPF-QA-D03-BATEDU-011` | 부분 구현 | P1 | 조건 분기 Handler 예제 |
| `CPF-QA-D03-BATEDU-012` | 부분 구현 | P1 | 다중 Partition 예제 |
| `CPF-QA-D03-BATEDU-013` | 부분 구현 | P1 | Multi-thread Worker 예제 |
| `CPF-QA-D03-BATEDU-014` | 부분 구현 | P1 | 다중 Instance Worker 분배 예제 |
| `CPF-QA-D03-BATEDU-015` | 부분 구현 | P1 | Worker 장애 인계 예제 |
| `CPF-QA-D03-BATEDU-016` | 부분 구현 | P1 | 37번째 Chunk 장애 후 재시작 예제 |
| `CPF-QA-D03-BATEDU-017` | 부분 구현 | P1 | Skip 한도 초과 예제 |
| `CPF-QA-D03-BATEDU-018` | 부분 구현 | P1 | Retry 소진 예제 |
| `CPF-QA-D03-BATEDU-019` | 부분 구현 | P1 | 오류 건 Reject 저장 예제 |
| `CPF-QA-D03-BATEDU-020` | 부분 구현 | P1 | 특정 실패 건 재처리 예제 |
| `CPF-QA-D03-BATEDU-021` | 부분 구현 | P1 | CSV Streaming Reader 예제 |
| `CPF-QA-D03-BATEDU-022` | 부분 구현 | P1 | 고정길이 파일 Reader 예제 |
| `CPF-QA-D03-BATEDU-023` | 부분 구현 | P1 | JSON Lines Reader 예제 |
| `CPF-QA-D03-BATEDU-024` | 부분 구현 | P1 | XML Streaming Reader 예제 |
| `CPF-QA-D03-BATEDU-025` | 부분 구현 | P1 | Header·Detail·Trailer 파일 예제 |
| `CPF-QA-D03-BATEDU-026` | 부분 구현 | P1 | 여러 파일 순차 처리 예제 |
| `CPF-QA-D03-BATEDU-027` | 부분 구현 | P1 | 여러 파일 병렬 처리 예제 |
| `CPF-QA-D03-BATEDU-028` | 부분 구현 | P1 | 파일 병합 예제 |
| `CPF-QA-D03-BATEDU-029` | 부분 구현 | P1 | 파일 분할 예제 |
| `CPF-QA-D03-BATEDU-030` | 부분 구현 | P1 | 파일→DB 적재 예제 |
| `CPF-QA-D03-BATEDU-031` | 부분 구현 | P1 | DB→파일 생성 예제 |
| `CPF-QA-D03-BATEDU-032` | 부분 구현 | P1 | 파일→Kafka Simulator 예제 |
| `CPF-QA-D03-BATEDU-033` | 부분 구현 | P1 | 파일→TCP Simulator 예제 |
| `CPF-QA-D03-BATEDU-034` | 부분 구현 | P1 | SFTP 다운로드 Simulator 예제 |
| `CPF-QA-D03-BATEDU-035` | 부분 구현 | P1 | Atomic Rename 수신완료 예제 |
| `CPF-QA-D03-BATEDU-036` | 부분 구현 | P1 | Checksum 검증 예제 |
| `CPF-QA-D03-BATEDU-037` | 부분 구현 | P1 | 압축·암호화·PGP 예제 |
| `CPF-QA-D03-BATEDU-038` | 부분 구현 | P1 | 중단 지점 Resume 예제 |
| `CPF-QA-D03-BATEDU-039` | 부분 구현 | P1 | 중복 파일·재수신 멱등 예제 |
| `CPF-QA-D03-BATEDU-040` | 부분 구현 | P1 | Zip Bomb 방어 예제 |
| `CPF-QA-D03-BATEDU-041` | 부분 구현 | P1 | Batch 내 REST 호출 예제 |
| `CPF-QA-D03-BATEDU-042` | 부분 구현 | P1 | Batch 내 TCP 전문 호출 예제 |
| `CPF-QA-D03-BATEDU-043` | 부분 구현 | P1 | Batch 내 SOAP 호출 예제 |
| `CPF-QA-D03-BATEDU-044` | 부분 구현 | P1 | Batch 내 gRPC 호출 예제 |
| `CPF-QA-D03-BATEDU-045` | 부분 구현 | P1 | 외부 호출 Timeout·Retry 예제 |
| `CPF-QA-D03-BATEDU-046` | 부분 구현 | P1 | 외부 호출 Unknown Result 복구 예제 |
| `CPF-QA-D03-BATEDU-047` | 부분 구현 | P1 | 부분 성공·보상 거래 예제 |
| `CPF-QA-D03-BATEDU-048` | 부분 구현 | P1 | Center-Cut 대상 추출·분할 예제 |
| `CPF-QA-D03-BATEDU-049` | 부분 구현 | P1 | Center-Cut Worker 재배정 예제 |
| `CPF-QA-D03-BATEDU-050` | 부분 구현 | P1 | On-Demand 승인 실행 예제 |
| `CPF-QA-D03-BATEDU-051` | 부분 구현 | P1 | Scheduled Calendar 예제 |
| `CPF-QA-D03-BATEDU-052` | 부분 구현 | P1 | File Trigger 예제 |
| `CPF-QA-D03-BATEDU-053` | 부분 구현 | P1 | Event Trigger 예제 |
| `CPF-QA-D03-BATEDU-054` | 부분 구현 | P1 | Housekeeping Archive 예제 |
| `CPF-QA-D03-BATEDU-055` | 부분 구현 | P1 | 초기 Migration 재시작 예제 |
| `CPF-QA-D03-BATEDU-056` | 부분 구현 | P1 | Chunk·Fetch Size 성능 비교 예제 |
| `CPF-QA-D03-BATEDU-057` | 부분 구현 | P1 | Thread 수·메모리 비교 예제 |
| `CPF-QA-D03-BATEDU-058` | 부분 구현 | P1 | 장시간 Heartbeat·진행률 예제 |
| `CPF-QA-D03-BATEDU-059` | 부분 구현 | P1 | 민감정보 마스킹 Batch 예제 |
| `CPF-QA-D03-BATEDU-060` | 부분 구현 | P1 | Batch 운영 조회·제어 예제 |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP07 — CPF 전체 기능 Reference/EDU 카탈로그

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-EDU-003` | 부분 구현 | P1 | 동일 JVM Local Service Call 예제 |
| `CPF-QA-D03-EDU-004` | 부분 구현 | P1 | 분리 WAS Remote Service Call 예제 |
| `CPF-QA-D03-EDU-005` | 부분 구현 | P1 | Local/Remote 동일 계약 예제 |
| `CPF-QA-D03-EDU-006` | 부분 구현 | P1 | REST 조회·등록·변경 예제 |
| `CPF-QA-D03-EDU-007` | 부분 구현 | P1 | 여러 외부기관 순차 호출 예제 |
| `CPF-QA-D03-EDU-008` | 부분 구현 | P1 | 여러 외부기관 병렬 호출 예제 |
| `CPF-QA-D03-EDU-009` | 부분 구현 | P1 | 부분 성공 결과 취합 예제 |
| `CPF-QA-D03-EDU-010` | 부분 구현 | P1 | Primary/Secondary Failover 예제 |
| `CPF-QA-D03-EDU-011` | 부분 구현 | P1 | 원거래 취소·보상 호출 예제 |
| `CPF-QA-D03-EDU-012` | 부분 구현 | P1 | 멱등 요청 중복 수신 예제 |
| `CPF-QA-D03-EDU-013` | 부분 구현 | P1 | 응답 유실 Exact Replay 예제 |
| `CPF-QA-D03-EDU-014` | 부분 구현 | P1 | Outbox/Inbox 정합성 예제 |
| `CPF-QA-D03-EDU-015` | 부분 구현 | P1 | Saga 보상 예제 |
| `CPF-QA-D03-EDU-016` | 부분 구현 | P1 | Optimistic Lock 충돌 예제 |
| `CPF-QA-D03-EDU-017` | 부분 구현 | P1 | Distributed Lock 충돌 예제 |
| `CPF-QA-D03-EDU-018` | 부분 구현 | P1 | 고정길이 TCP 전문 예제 |
| `CPF-QA-D03-EDU-019` | 부분 구현 | P1 | 가변길이 Length Header 전문 예제 |
| `CPF-QA-D03-EDU-020` | 부분 구현 | P1 | STX/ETX·CRLF 전문 예제 |
| `CPF-QA-D03-EDU-021` | 부분 구현 | P1 | Binary·BCD·Hex 전문 예제 |
| `CPF-QA-D03-EDU-022` | 부분 구현 | P1 | UTF-8·EUC-KR·EBCDIC 변환 예제 |
| `CPF-QA-D03-EDU-023` | 부분 구현 | P1 | 전문 Header·Body·Trailer 예제 |
| `CPF-QA-D03-EDU-024` | 부분 구현 | P1 | 전문 Version Layout 예제 |
| `CPF-QA-D03-EDU-025` | 부분 구현 | P1 | Correlation·응답 순서 역전 예제 |
| `CPF-QA-D03-EDU-026` | 부분 구현 | P1 | 재접속·Heartbeat 예제 |
| `CPF-QA-D03-EDU-027` | 부분 구현 | P1 | TLS·mTLS TCP 예제 |
| `CPF-QA-D03-EDU-028` | 부분 구현 | P1 | 잘린 전문·초과길이·Encoding 오류 예제 |
| `CPF-QA-D03-EDU-029` | 부분 구현 | P1 | ISO8583 확장 Adapter 예제 |
| `CPF-QA-D03-EDU-030` | 부분 구현 | P1 | Typed HTTP Client 예제 |
| `CPF-QA-D03-EDU-031` | 부분 구현 | P1 | 공통 Header·Trace 전파 예제 |
| `CPF-QA-D03-EDU-032` | 부분 구현 | P1 | OAuth2·API Key·mTLS HTTP 예제 |
| `CPF-QA-D03-EDU-033` | 부분 구현 | P1 | Multipart·대용량 Streaming 예제 |
| `CPF-QA-D03-EDU-034` | 부분 구현 | P1 | Pagination 전체 수집 예제 |
| `CPF-QA-D03-EDU-035` | 부분 구현 | P1 | Rate Limit·Retry-After 예제 |
| `CPF-QA-D03-EDU-036` | 부분 구현 | P1 | SOAP Fault Mapping 예제 |
| `CPF-QA-D03-EDU-037` | 부분 구현 | P1 | XML/XSD 검증·XXE 방어 예제 |
| `CPF-QA-D03-EDU-038` | 부분 구현 | P1 | gRPC Unary 예제 |
| `CPF-QA-D03-EDU-039` | 부분 구현 | P1 | gRPC Server Streaming 예제 |
| `CPF-QA-D03-EDU-040` | 부분 구현 | P1 | Deadline·Cancellation 예제 |
| `CPF-QA-D03-EDU-041` | 부분 구현 | P1 | API Version 호환 예제 |
| `CPF-QA-D03-EDU-042` | 부분 구현 | P1 | Kafka Produce·Consume 예제 |
| `CPF-QA-D03-EDU-043` | 부분 구현 | P1 | RabbitMQ Adapter 예제 |
| `CPF-QA-D03-EDU-044` | 부분 구현 | P1 | JMS Adapter 예제 |
| `CPF-QA-D03-EDU-045` | 부분 구현 | P1 | Consumer Group 병렬 예제 |
| `CPF-QA-D03-EDU-046` | 부분 구현 | P1 | 메시지 순서 보장 예제 |
| `CPF-QA-D03-EDU-047` | 부분 구현 | P1 | 중복 메시지 Inbox 예제 |
| `CPF-QA-D03-EDU-048` | 부분 구현 | P1 | Poison Message·DLQ 예제 |
| `CPF-QA-D03-EDU-049` | 부분 구현 | P1 | DLQ 선택 재처리 예제 |
| `CPF-QA-D03-EDU-050` | 부분 구현 | P1 | 지연 재시도 예제 |
| `CPF-QA-D03-EDU-051` | 부분 구현 | P1 | Replay 범위 지정 예제 |
| `CPF-QA-D03-EDU-052` | 부분 구현 | P1 | Schema Version 변경 예제 |
| `CPF-QA-D03-EDU-053` | 부분 구현 | P1 | Consumer Lag 예제 |
| `CPF-QA-D03-EDU-054` | 부분 구현 | P1 | Broker 장애 복구 Simulator 예제 |
| `CPF-QA-D03-EDU-055` | 부분 구현 | P1 | Transactional Outbox 전송 예제 |
| `CPF-QA-D03-EDU-056` | 부분 구현 | P1 | Redis Cache Aside 예제 |
| `CPF-QA-D03-EDU-057` | 부분 구현 | P1 | Redis TTL·Eviction 예제 |
| `CPF-QA-D03-EDU-058` | 부분 구현 | P1 | Cache Stampede 방지 예제 |
| `CPF-QA-D03-EDU-059` | 부분 구현 | P1 | Redis Distributed Lock 예제 |
| `CPF-QA-D03-EDU-060` | 부분 구현 | P1 | Redis Rate Limit 예제 |
| `CPF-QA-D03-EDU-061` | 부분 구현 | P1 | Redis Session 공유 예제 |
| `CPF-QA-D03-EDU-062` | 부분 구현 | P1 | Redis Streams 예제 |
| `CPF-QA-D03-EDU-063` | 부분 구현 | P1 | Redis Pub/Sub 설정 전파 예제 |
| `CPF-QA-D03-EDU-064` | 부분 구현 | P1 | Redis 장애 DB Fallback 예제 |
| `CPF-QA-D03-EDU-065` | 부분 구현 | P1 | Redis Cluster·Sentinel Simulator 예제 |
| `CPF-QA-D03-EDU-066` | 부분 구현 | P1 | Redis 직렬화 Version 복구 예제 |
| `CPF-QA-D03-EDU-067` | 부분 구현 | P1 | WebSocket 실시간 예제 |
| `CPF-QA-D03-EDU-068` | 부분 구현 | P1 | SSE 실시간 예제 |
| `CPF-QA-D03-EDU-069` | 부분 구현 | P1 | STOMP 예제 |
| `CPF-QA-D03-EDU-070` | 부분 구현 | P1 | Webhook 서명·재시도 예제 |
| `CPF-QA-D03-EDU-071` | 부분 구현 | P1 | CDC 이벤트 예제 |
| `CPF-QA-D03-EDU-072` | 부분 구현 | P1 | Object Storage Adapter 예제 |
| `CPF-QA-D03-EDU-073` | 부분 구현 | P1 | SMB·FTPS Adapter 예제 |
| `CPF-QA-D03-EDU-074` | 부분 구현 | P1 | Secret·Certificate Reload 예제 |
| `CPF-QA-D03-EDU-075` | 부분 구현 | P1 | Feature Flag 변경 예제 |
| `CPF-QA-D03-EDU-076` | 부분 구현 | P1 | Service Discovery 예제 |
| `CPF-QA-D03-EDU-077` | 부분 구현 | P1 | Schema Registry 예제 |
| `CPF-QA-D03-EDU-078` | 부분 구현 | P1 | Mapping·Transformation 예제 |
| `CPF-QA-D03-EDU-079` | 부분 구현 | P1 | Notification Provider 확장 예제 |
| `CPF-QA-D03-EDU-080` | 부분 구현 | P1 | Search Provider 예제 |
| `CPF-QA-D03-EDU-081` | 부분 구현 | P1 | Workflow·State Machine 예제 |
| `CPF-QA-D03-EDU-082` | 부분 구현 | P1 | Protocol Translation 예제 |
| `CPF-QA-D03-EDU-083` | 부분 구현 | P1 | Offline/Air-gapped 예제 |
| `CPF-QA-D03-EDU-084` | 부분 구현 | P1 | Multi-region·DR 예제 |
| `CPF-QA-D03-EDU-085` | 부분 구현 | P1 | Quota·Metering 예제 |
| `CPF-QA-D03-EDU-086` | 부분 구현 | P1 | Data Retention·Deletion 예제 |
| `CPF-QA-D03-EDU-087` | 부분 구현 | P1 | Supply Chain·SBOM 예제 |
| `CPF-QA-D03-EDU-088` | 부분 구현 | P1 | Fault Injection Testkit 예제 |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP08 — 공통 Integration/Client Framework

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-INT-001` | 부분 구현 | P0 | Raw WebClient·RestClient·JDK HttpClient 직접 사용 금지 |
| `CPF-QA-D03-INT-002` | 부분 구현 | P0 | Raw RedisTemplate·Lettuce·Jedis 직접 사용 금지 |
| `CPF-QA-D03-INT-003` | 부분 구현 | P0 | Raw KafkaTemplate·Consumer 직접 사용 금지 |
| `CPF-QA-D03-INT-004` | 부분 구현 | P0 | Raw RabbitTemplate·JMS 직접 사용 금지 |
| `CPF-QA-D03-INT-005` | 부분 구현 | P0 | Raw Socket·Netty 직접 사용 금지 |
| `CPF-QA-D03-INT-006` | 부분 구현 | P0 | Raw FTP·SFTP·SMB Client 직접 사용 금지 |
| `CPF-QA-D03-INT-007` | 부분 구현 | P0 | Raw S3/Object SDK 직접 사용 금지 |
| `CPF-QA-D03-INT-008` | 부분 구현 | P0 | Raw gRPC ManagedChannel 직접 사용 금지 |
| `CPF-QA-D03-INT-009` | 부분 구현 | P0 | Raw SOAP Factory 직접 사용 금지 |
| `CPF-QA-D03-INT-010` | 부분 구현 | P0 | CPF 단순 한 줄 호출 API |
| `CPF-QA-D03-INT-011` | 부분 구현 | P0 | CPF Fluent 호출 API |
| `CPF-QA-D03-INT-012` | 부분 구현 | P0 | Generated Typed Client |
| `CPF-QA-D03-INT-013` | 부분 구현 | P0 | Provider-neutral Public Contract |
| `CPF-QA-D03-INT-014` | 부분 구현 | P0 | Provider 예외·유형 누출 차단 |
| `CPF-QA-D03-INT-015` | 부분 구현 | P0 | Endpoint Profile Registry |
| `CPF-QA-D03-INT-016` | 부분 구현 | P0 | Timeout Budget |
| `CPF-QA-D03-INT-017` | 부분 구현 | P0 | Bounded Retry |
| `CPF-QA-D03-INT-018` | 부분 구현 | P0 | Circuit Breaker |
| `CPF-QA-D03-INT-019` | 부분 구현 | P0 | Bulkhead |
| `CPF-QA-D03-INT-020` | 부분 구현 | P0 | Backpressure |
| `CPF-QA-D03-INT-021` | 부분 구현 | P0 | Correlation·Trace 전파 |
| `CPF-QA-D03-INT-022` | 부분 구현 | P0 | Idempotency |
| `CPF-QA-D03-INT-023` | 부분 구현 | P0 | Unknown Result |
| `CPF-QA-D03-INT-024` | 부분 구현 | P0 | Reconciliation |
| `CPF-QA-D03-INT-025` | 부분 구현 | P0 | Compensation Hook |
| `CPF-QA-D03-INT-026` | 부분 구현 | P0 | Security·Credential Reference |
| `CPF-QA-D03-INT-027` | 부분 구현 | P0 | Metrics·Tracing |
| `CPF-QA-D03-INT-028` | 부분 구현 | P0 | Safe ConfigurationProperties 기본값 |
| `CPF-QA-D03-INT-029` | 부분 구현 | P0 | 설정 Metadata·Fail-fast |
| `CPF-QA-D03-INT-030` | 부분 구현 | P0 | 설정 우선순위 계층 |
| `CPF-QA-D03-INT-031` | 부분 구현 | P0 | 승인된 Runtime Override |
| `CPF-QA-D03-INT-032` | 부분 구현 | P0 | Per-call Override 보안 상한 |
| `CPF-QA-D03-INT-033` | 부분 구현 | P0 | Provider Adapter 교체 |
| `CPF-QA-D03-INT-034` | 부분 구현 | P0 | 실제 Consumer 이관 |
| `CPF-QA-D03-INT-035` | 부분 구현 | P0 | 직접 Library 예외 승인·만료 |
| `CPF-QA-D03-INT-036` | 부분 구현 | P0 | Static Direct-client Gate |
| `CPF-QA-D03-INT-037` | 부분 구현 | P0 | Testkit·Simulator |
| `CPF-QA-D03-INT-038` | 부분 구현 | P0 | Generator·EDU 연계 |
| `CPF-QA-D03-INT-039` | 부분 구현 | P0 | 운영 조회·제어 |
| `CPF-QA-D03-INT-040` | 부분 구현 | P0 | License·SBOM·CVE |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP09 — 통합 Registry/Metadata Platform

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-ALR-001` | 실패 | P0 | Notification Repository MariaDB 전용 SQL |
| `CPF-QA-D03-ALR-002` | 실패 | P0 | Notification 운영자 식별 fallback 허용 |
| `CPF-QA-D03-ALR-003` | 부분 구현 | P0 | 알림이 Mock 동기 발송에 한정 |
| `CPF-QA-D03-META-001` | 부분 구현 | P1 | Parameter Registry |
| `CPF-QA-D03-META-002` | 부분 구현 | P1 | Common Code Group·Code Registry |
| `CPF-QA-D03-META-003` | 부분 구현 | P1 | Business Rule Registry |
| `CPF-QA-D03-META-004` | 부분 구현 | P1 | Validation Rule Registry |
| `CPF-QA-D03-META-005` | 부분 구현 | P1 | State·State Transition Registry |
| `CPF-QA-D03-META-006` | 부분 구현 | P1 | Approval Line Registry |
| `CPF-QA-D03-META-007` | 부분 구현 | P1 | Permission Policy Registry |
| `CPF-QA-D03-META-008` | 부분 구현 | P1 | Endpoint·Connector Profile Registry |
| `CPF-QA-D03-META-009` | 부분 구현 | P1 | Retry·Timeout·Circuit Policy Registry |
| `CPF-QA-D03-META-010` | 부분 구현 | P1 | Topic·Queue·Routing Metadata |
| `CPF-QA-D03-META-011` | 부분 구현 | P1 | Message·API·Event Schema Registry |
| `CPF-QA-D03-META-012` | 부분 구현 | P1 | File Layout Registry |
| `CPF-QA-D03-META-013` | 부분 구현 | P1 | Mapping·Transformation Registry |
| `CPF-QA-D03-META-014` | 부분 구현 | P1 | Notification Template Registry |
| `CPF-QA-D03-META-015` | 부분 구현 | P1 | UI Menu·Feature Flag Registry |
| `CPF-QA-D03-META-016` | 부분 구현 | P1 | Batch·Scheduler Policy Registry |
| `CPF-QA-D03-META-017` | 부분 구현 | P1 | Masking·Sensitive Classification Registry |
| `CPF-QA-D03-META-018` | 부분 구현 | P1 | Retention·Deletion Policy Registry |
| `CPF-QA-D03-META-019` | 부분 구현 | P1 | Provider·Adapter Configuration Registry |
| `CPF-QA-D03-META-020` | 부분 구현 | P1 | Domain/System/Group/Item Namespace |
| `CPF-QA-D03-META-021` | 부분 구현 | P1 | Version·Environment·Tenant Override |
| `CPF-QA-D03-META-022` | 부분 구현 | P1 | Effective Period |
| `CPF-QA-D03-META-023` | 부분 구현 | P1 | Approval·Audit·Rollback |
| `CPF-QA-D03-META-024` | 부분 구현 | P1 | Cache·Multi-instance Propagation |
| `CPF-QA-D03-META-025` | 부분 구현 | P1 | Secret Reference 분리 |
| `CPF-QA-D03-META-026` | 부분 구현 | P1 | Deployment·Migration 연계 |
| `CPF-QA-D03-META-027` | 부분 구현 | P1 | Generator Template 연계 |
| `CPF-QA-D03-META-028` | 부분 구현 | P1 | Typed Adapter 확장 |
| `CPF-QA-D03-META-029` | 부분 구현 | P1 | Drift·Conflict Detection |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP10 — 관제·알림·Incident·Email/SMS

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-INS-001` | 부분 구현 | P0 | Runtime Group에 Domain·Cluster·Application Group 정식 모델 부족 |
| `CPF-QA-D03-LOG-001` | 부분 구현 | P0 | Agent 로그 ZIP 임시파일 Lifecycle 불완전 |
| `CPF-QA-D03-LOG-002` | 부분 구현 | P0 | 로그 다운로드 민감정보 마스킹 부재 |
| `CPF-QA-D03-LOG-003` | 부분 구현 | P1 | 로그 Archive 선생성으로 대용량 디스크 부하 |
| `CPF-QA-D03-ALR-004` | 부분 구현 | P0 | 표준 Alert Event 계약 |
| `CPF-QA-D03-ALR-005` | 부분 구현 | P0 | Alert Event 민감정보 마스킹 |
| `CPF-QA-D03-ALR-006` | 부분 구현 | P0 | Notification Outbox |
| `CPF-QA-D03-ALR-007` | 부분 구현 | P0 | Notification Worker |
| `CPF-QA-D03-ALR-008` | 부분 구현 | P0 | Email Provider SPI |
| `CPF-QA-D03-ALR-009` | 부분 구현 | P0 | Embedded SMTP 통합 Test |
| `CPF-QA-D03-ALR-010` | 부분 구현 | P0 | Local Mail Capture mode |
| `CPF-QA-D03-ALR-011` | 부분 구현 | P0 | 실제 SMTP Profile |
| `CPF-QA-D03-ALR-012` | 부분 구현 | P0 | SMS Provider SPI |
| `CPF-QA-D03-ALR-013` | 부분 구현 | P0 | SMS Simulator |
| `CPF-QA-D03-ALR-014` | 부분 구현 | P0 | Webhook Provider |
| `CPF-QA-D03-ALR-015` | 부분 구현 | P0 | 메신저 Provider 확장 |
| `CPF-QA-D03-ALR-016` | 부분 구현 | P0 | INFO·WARN·ERROR·CRITICAL 심각도 |
| `CPF-QA-D03-ALR-017` | 부분 구현 | P0 | 플랫폼·시스템·도메인 관제 범위 |
| `CPF-QA-D03-ALR-018` | 부분 구현 | P0 | 업무그룹 관제 범위 |
| `CPF-QA-D03-ALR-019` | 부분 구현 | P0 | 서비스·API 관제 범위 |
| `CPF-QA-D03-ALR-020` | 부분 구현 | P0 | Batch·Job 관제 범위 |
| `CPF-QA-D03-ALR-021` | 부분 구현 | P0 | 연계기관·Endpoint 관제 범위 |
| `CPF-QA-D03-ALR-022` | 부분 구현 | P0 | 오류코드·오류그룹 관리 |
| `CPF-QA-D03-ALR-023` | 부분 구현 | P0 | 반복 횟수 Threshold |
| `CPF-QA-D03-ALR-024` | 부분 구현 | P0 | 지속시간 Threshold |
| `CPF-QA-D03-ALR-025` | 부분 구현 | P0 | 실패율·처리량 Threshold |
| `CPF-QA-D03-ALR-026` | 부분 구현 | P0 | 업무시간·야간·휴일 정책 |
| `CPF-QA-D03-ALR-027` | 부분 구현 | P0 | 수신 개인·역할·조직·당직그룹 |
| `CPF-QA-D03-ALR-028` | 부분 구현 | P0 | 채널 우선순위·Fallback |
| `CPF-QA-D03-ALR-029` | 부분 구현 | P0 | 중복 억제·Cooldown |
| `CPF-QA-D03-ALR-030` | 부분 구현 | P0 | 집계 알림 |
| `CPF-QA-D03-ALR-031` | 부분 구현 | P0 | 무음시간·Maintenance Suppression |
| `CPF-QA-D03-ALR-032` | 부분 구현 | P0 | 시간당 발송 제한 |
| `CPF-QA-D03-ALR-033` | 부분 구현 | P0 | Escalation 단계 |
| `CPF-QA-D03-ALR-034` | 부분 구현 | P0 | 5분 미확인 Escalation |
| `CPF-QA-D03-ALR-035` | 부분 구현 | P0 | Incident 자동 생성 |
| `CPF-QA-D03-ALR-036` | 부분 구현 | P0 | Incident 확인·담당자 지정 |
| `CPF-QA-D03-ALR-037` | 부분 구현 | P0 | 처리중·종료·재발 상태 |
| `CPF-QA-D03-ALR-038` | 부분 구현 | P0 | 임시 관제 제외·만료 |
| `CPF-QA-D03-ALR-039` | 부분 구현 | P0 | Template Version·다국어 |
| `CPF-QA-D03-ALR-040` | 부분 구현 | P0 | 발송 Retry·Backoff |
| `CPF-QA-D03-ALR-041` | 부분 구현 | P0 | 발송 DLQ·수동 재처리 |
| `CPF-QA-D03-ALR-042` | 부분 구현 | P0 | Preview·Test Send |
| `CPF-QA-D03-ALR-043` | 부분 구현 | P0 | 설정 변경 승인·감사·Rollback |
| `CPF-QA-D03-ALR-044` | 부분 구현 | P0 | Provider 장애가 원 거래 미오염 |
| `CPF-QA-D03-ALR-045` | 부분 구현 | P0 | Notification DB Vendor portability |
| `CPF-QA-D03-ALR-046` | 부분 구현 | P0 | Receiver 개인정보 보호 |
| `CPF-QA-D03-ALR-047` | 부분 구현 | P0 | 발송 결과·Provider Message ID 추적 |
| `CPF-QA-D03-ALR-048` | 부분 구현 | P0 | SLO·발송 지연 Monitoring |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP11 — 도메인/그룹별 Instance·로그 관리

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-INS-002` | 부분 구현 | P0 | 환경 계층 |
| `CPF-QA-D03-INS-003` | 부분 구현 | P0 | 플랫폼·클러스터 계층 |
| `CPF-QA-D03-INS-004` | 부분 구현 | P0 | 시스템·도메인 계층 |
| `CPF-QA-D03-INS-005` | 부분 구현 | P0 | 애플리케이션 그룹 계층 |
| `CPF-QA-D03-INS-006` | 부분 구현 | P0 | 인스턴스 역할 Web·Gateway·Batch·Scheduler·Worker·Agent |
| `CPF-QA-D03-INS-007` | 부분 구현 | P0 | Instance ID·Host·Port·JVM Metadata |
| `CPF-QA-D03-INS-008` | 부분 구현 | P0 | Artifact Version·Commit 표시 |
| `CPF-QA-D03-INS-009` | 부분 구현 | P0 | 시작시각·Heartbeat·Lease 상태 |
| `CPF-QA-D03-INS-010` | 부분 구현 | P0 | Group 정상·경고·장애 집계 |
| `CPF-QA-D03-INS-011` | 부분 구현 | P0 | 도메인·그룹 검색·Paging |
| `CPF-QA-D03-INS-012` | 부분 구현 | P0 | Group 운영담당·권한 |
| `CPF-QA-D03-INS-013` | 부분 구현 | P0 | Group Capacity·동시성 정책 |
| `CPF-QA-D03-INS-014` | 부분 구현 | P0 | Group Drain·격리·복귀 |
| `CPF-QA-D03-INS-015` | 부분 구현 | P0 | 개별 Instance 예외 제어 |
| `CPF-QA-D03-INS-016` | 부분 구현 | P0 | Rolling Upgrade·버전 혼재 표시 |
| `CPF-QA-D03-INS-017` | 부분 구현 | P0 | Tenant·도메인 접근 격리 |
| `CPF-QA-D03-INS-018` | 부분 구현 | P0 | Static·Dynamic Membership |
| `CPF-QA-D03-INS-019` | 부분 구현 | P0 | Nested Group Cycle 방지 |
| `CPF-QA-D03-INS-020` | 부분 구현 | P0 | Target Preview·Immutable Snapshot |
| `CPF-QA-D03-INS-021` | 부분 구현 | P0 | Instance Ghost 등록 정리 |
| `CPF-QA-D03-LOG-004` | 부분 구현 | P0 | 인스턴스별 로그 파일 목록 |
| `CPF-QA-D03-LOG-005` | 부분 구현 | P0 | 도메인·그룹별 로그 조회 |
| `CPF-QA-D03-LOG-006` | 부분 구현 | P0 | Application Log 다운로드 |
| `CPF-QA-D03-LOG-007` | 부분 구현 | P0 | Access Log 다운로드 |
| `CPF-QA-D03-LOG-008` | 부분 구현 | P0 | Error Log 다운로드 |
| `CPF-QA-D03-LOG-009` | 부분 구현 | P0 | Audit Log 다운로드 |
| `CPF-QA-D03-LOG-010` | 부분 구현 | P0 | Batch·Worker Log 다운로드 |
| `CPF-QA-D03-LOG-011` | 부분 구현 | P0 | Gateway·연계 Log 다운로드 |
| `CPF-QA-D03-LOG-012` | 부분 구현 | P0 | GC·JVM 진단 Log 다운로드 |
| `CPF-QA-D03-LOG-013` | 부분 구현 | P0 | 현재·Rotation·Archive 구분 |
| `CPF-QA-D03-LOG-014` | 부분 구현 | P0 | 단일 파일 Streaming 다운로드 |
| `CPF-QA-D03-LOG-015` | 부분 구현 | P0 | 다중 파일 ZIP 다운로드 |
| `CPF-QA-D03-LOG-016` | 부분 구현 | P0 | 다중 인스턴스 시간대 묶음 |
| `CPF-QA-D03-LOG-017` | 부분 구현 | P0 | Transaction ID 관련 로그 묶음 |
| `CPF-QA-D03-LOG-018` | 부분 구현 | P0 | Trace ID 관련 로그 묶음 |
| `CPF-QA-D03-LOG-019` | 부분 구현 | P0 | 종료 Instance Archive 조회 |
| `CPF-QA-D03-LOG-020` | 부분 구현 | P0 | 통신불가 Instance 오류 표시 |
| `CPF-QA-D03-LOG-021` | 부분 구현 | P0 | 다운로드 취소·Timeout |
| `CPF-QA-D03-LOG-022` | 부분 구현 | P0 | Path Traversal·Symlink 방어 |
| `CPF-QA-D03-LOG-023` | 부분 구현 | P0 | 허용 Log Root Catalog |
| `CPF-QA-D03-LOG-024` | 부분 구현 | P0 | Secret·Key 파일 반출 차단 |
| `CPF-QA-D03-LOG-025` | 부분 구현 | P0 | 다운로드 권한·사유·승인 |
| `CPF-QA-D03-LOG-026` | 부분 구현 | P0 | 다운로드 감사 |
| `CPF-QA-D03-LOG-027` | 부분 구현 | P0 | 파일 수·크기·기간 제한 |
| `CPF-QA-D03-LOG-028` | 부분 구현 | P0 | 동시 다운로드 부하 제한 |
| `CPF-QA-D03-LOG-029` | 부분 구현 | P0 | 임시 ZIP 암호화·자동삭제 |
| `CPF-QA-D03-LOG-030` | 부분 구현 | P0 | 다운로드 마스킹 정책 |
| `CPF-QA-D03-LOG-031` | 부분 구현 | P0 | 부분 실패 Manifest |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP12 — JavaDoc·주석·OpenAPI·Guide QA

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-DOC-001` | 부분 구현 | P1 | 기능 중심 Package 구조 |
| `CPF-QA-D03-DOC-002` | 부분 구현 | P1 | 일관된 Class·Method Naming |
| `CPF-QA-D03-DOC-003` | 부분 구현 | P1 | 모든 핵심 Package package-info.java 한글 설명 |
| `CPF-QA-D03-DOC-004` | 부분 구현 | P1 | Public API 한글 JavaDoc |
| `CPF-QA-D03-DOC-005` | 부분 구현 | P1 | SPI 한글 JavaDoc |
| `CPF-QA-D03-DOC-006` | 부분 구현 | P1 | Service·Controller 한글 JavaDoc |
| `CPF-QA-D03-DOC-007` | 부분 구현 | P1 | 복잡 Method 의도·실패조건 주석 |
| `CPF-QA-D03-DOC-008` | 부분 구현 | P1 | Batch Job·Step·Handler JavaDoc |
| `CPF-QA-D03-DOC-009` | 부분 구현 | P1 | ConfigurationProperties JavaDoc |
| `CPF-QA-D03-DOC-010` | 부분 구현 | P1 | 설정 기본값·단위·범위·재기동 설명 |
| `CPF-QA-D03-DOC-011` | 부분 구현 | P1 | OpenAPI Controller 설명 |
| `CPF-QA-D03-DOC-012` | 부분 구현 | P1 | OpenAPI 요청·응답 Schema |
| `CPF-QA-D03-DOC-013` | 부분 구현 | P1 | OpenAPI Header 계약 |
| `CPF-QA-D03-DOC-014` | 부분 구현 | P1 | OpenAPI 정상 예시 |
| `CPF-QA-D03-DOC-015` | 부분 구현 | P1 | OpenAPI 오류 예시 |
| `CPF-QA-D03-DOC-016` | 부분 구현 | P1 | OpenAPI 권한·상태코드 |
| `CPF-QA-D03-DOC-017` | 부분 구현 | P1 | EDU Guide 실행 명령 |
| `CPF-QA-D03-DOC-018` | 부분 구현 | P1 | EDU Sample Data·예상 결과 |
| `CPF-QA-D03-DOC-019` | 부분 구현 | P1 | 장애·복구 Guide |
| `CPF-QA-D03-DOC-020` | 부분 구현 | P1 | Requirement↔Source↔Test↔EDU Matrix |
| `CPF-QA-D03-DOC-021` | 부분 구현 | P1 | Source 기능 Markdown/HTML Catalog |
| `CPF-QA-D03-DOC-022` | 부분 구현 | P1 | JavaDoc·Swagger·Guide Drift Gate |
| `CPF-QA-D03-DOC-023` | 부분 구현 | P1 | 외부 Runtime Font/CDN 의존 금지 |
| `CPF-QA-D03-DOC-024` | 부분 구현 | P1 | IDE Plugin 개발 제외와 기본 IDE 호환 |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP13 — 현재 환경별 실검증·미검증 정책

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-DBVAL-001` | 미검증 | P0 | MariaDB Fresh Install 실제 검증 |
| `CPF-QA-D03-DBVAL-002` | 미검증 | P0 | MariaDB Migration 실제 검증 |
| `CPF-QA-D03-DBVAL-003` | 미검증 | P0 | MariaDB Upgrade 실제 검증 |
| `CPF-QA-D03-DBVAL-004` | 미검증 | P0 | MariaDB Rollback/Forward Recovery 실제 검증 |
| `CPF-QA-D03-DBVAL-005` | 미검증 | P0 | MariaDB Runtime Query 실제 검증 |
| `CPF-QA-D03-DBVAL-006` | 미검증 | P0 | MariaDB Drift·Checksum Negative Test |
| `CPF-QA-D03-DBVAL-007` | 미검증 | P0 | PostgreSQL Canonical·Migration·Rollback 정적 parity |
| `CPF-QA-D03-DBVAL-008` | 미검증 | P0 | Oracle Canonical·Migration·Rollback 정적 parity |
| `CPF-QA-D03-DBVAL-009` | 미검증 | P0 | PostgreSQL 실Runtime 미검증 명시 |
| `CPF-QA-D03-DBVAL-010` | 미검증 | P0 | Oracle 실Runtime 미검증 명시 |
| `CPF-QA-D03-DBVAL-011` | 미검증 | P0 | Redis 구현·Contract·Fake Test |
| `CPF-QA-D03-DBVAL-012` | 미검증 | P0 | Redis 실Runtime 미검증 명시 |
| `CPF-QA-D03-DBVAL-013` | 미검증 | P0 | Kafka 구현·Contract·Simulator Test |
| `CPF-QA-D03-DBVAL-014` | 미검증 | P0 | Kafka 실Runtime 미검증 명시 |
| `CPF-QA-D03-DBVAL-015` | 미검증 | P0 | RabbitMQ/JMS 구현·Simulator Test |
| `CPF-QA-D03-DBVAL-016` | 미검증 | P0 | SMS 실발송 미검증 명시 |
| `CPF-QA-D03-DBVAL-017` | 미검증 | P0 | 외부 SFTP/SOAP/gRPC 실연동 미검증 명시 |
| `CPF-QA-D03-DBVAL-018` | 미검증 | P0 | Testcontainers 선택 사용과 설치 의존 분리 |
| `CPF-QA-D03-DBVAL-019` | 미검증 | P0 | 실환경 확보 후 동일 Scenario 재사용 |
| `CPF-QA-D03-DBVAL-020` | 미검증 | P0 | 미검증→완료 승격 Evidence 규칙 |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP14 — Generator·Generated Domain 동기화

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-GEN-001` | 부분 구현 | P1 | Generator가 CPF Client API만 생성 |
| `CPF-QA-D03-GEN-002` | 부분 구현 | P1 | Generator Batch Handler Template |
| `CPF-QA-D03-GEN-003` | 부분 구현 | P1 | Generator Job Metadata Template |
| `CPF-QA-D03-GEN-004` | 부분 구현 | P1 | Generator 연동 Adapter Template |
| `CPF-QA-D03-GEN-005` | 부분 구현 | P1 | Generator EDU·Test Template |
| `CPF-QA-D03-GEN-006` | 부분 구현 | P1 | Generator package-info·JavaDoc Template |
| `CPF-QA-D03-GEN-007` | 부분 구현 | P1 | Generator OpenAPI Template |
| `CPF-QA-D03-GEN-008` | 부분 구현 | P1 | Module·Package·SystemCode 충돌 |
| `CPF-QA-D03-GEN-009` | 부분 구현 | P1 | Route·DB·Schema 충돌 |
| `CPF-QA-D03-GEN-010` | 부분 구현 | P1 | Runtime Agent·Group Metadata 생성 |
| `CPF-QA-D03-GEN-011` | 부분 구현 | P1 | 사용자 코드 덮어쓰기 방지 |
| `CPF-QA-D03-GEN-012` | 부분 구현 | P1 | 재생성 normalized parity |
| `CPF-QA-D03-GEN-013` | 부분 구현 | P1 | ACC/MBR 임의 Domain 생성 |
| `CPF-QA-D03-GEN-014` | 부분 구현 | P1 | Generated Domain Local/Remote parity |
| `CPF-QA-D03-GEN-015` | 부분 구현 | P1 | Generated Domain Reference Matrix |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

### WP15 — 통합 실행·회귀·Evidence·Hygiene

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D03-VAL-001` | 미검증 | P0 | 최신 SHA Source Gate |
| `CPF-QA-D03-VAL-002` | 미검증 | P0 | Java25 full build |
| `CPF-QA-D03-VAL-003` | 미검증 | P0 | Frontend build·typecheck·test |
| `CPF-QA-D03-VAL-004` | 미검증 | P0 | ArchUnit·Boundary Gate |
| `CPF-QA-D03-VAL-005` | 미검증 | P0 | Secret·PII Scan |
| `CPF-QA-D03-VAL-006` | 미검증 | P0 | Dependency·License·SBOM Gate |
| `CPF-QA-D03-VAL-007` | 미검증 | P0 | WAS 동일 JVM E2E |
| `CPF-QA-D03-VAL-008` | 미검증 | P0 | WAS 분리 Runtime E2E |
| `CPF-QA-D03-VAL-009` | 미검증 | P0 | MariaDB Lifecycle |
| `CPF-QA-D03-VAL-010` | 미검증 | P0 | Runtime Control Multi-instance |
| `CPF-QA-D03-VAL-011` | 미검증 | P0 | Gateway 대용량·timeout·failover |
| `CPF-QA-D03-VAL-012` | 미검증 | P0 | Batch Multi-instance·takeover |
| `CPF-QA-D03-VAL-013` | 미검증 | P0 | ADM Browser 권한별 E2E |
| `CPF-QA-D03-VAL-014` | 미검증 | P0 | Notification Embedded SMTP·Simulator |
| `CPF-QA-D03-VAL-015` | 미검증 | P0 | Reference/EDU executable Matrix |
| `CPF-QA-D03-VAL-016` | 미검증 | P0 | Generator lifecycle |
| `CPF-QA-D03-VAL-017` | 미검증 | P0 | Evidence exact SHA Gate |
| `CPF-QA-D03-VAL-018` | 미검증 | P0 | Current/Handover 정본 갱신 |
| `CPF-QA-D03-VAL-019` | 미검증 | P0 | Repository Garbage 정리 |
| `CPF-QA-D03-VAL-020` | 미검증 | P0 | 중간 Overlay·bak·tmp·log 제거 |
| `CPF-QA-D03-VAL-021` | 미검증 | P0 | 완료 기능 영향도 회귀 자동 선택 |
| `CPF-QA-D03-VAL-022` | 미검증 | P0 | Requirement→구현 추적 |
| `CPF-QA-D03-VAL-023` | 미검증 | P0 | 구현→Requirement·Owner·Consumer 역추적 |
| `CPF-QA-D03-VAL-024` | 미검증 | P0 | Release Gate와 미검증 차단 |

상세 필수 구현·검증·근거·선행조건은 `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`의 동일 ID 행을 정본으로 사용한다.

## 9. 예제 공통 완료 계약

모든 예제는 다음 구조를 가진다.

```text
목적·Requirement ID
→ 사용 CPF Public API/SPI·설정
→ 정상 시나리오
→ 오류·경계·부분 실패
→ 재시도·복구·재처리·멱등성
→ 보안·마스킹·감사
→ ADM 운영 조회·제어
→ 자동 Unit/Integration/Contract/Fault Test
→ 실행 명령·Sample Data·예상 결과
→ 최신 Commit Evidence
```

다음 중 하나라도 해당하면 예제와 관련 기능을 완료 처리하지 않는다.

- CPF Public API 대신 Raw foundation client를 직접 사용
- 정상 흐름만 있고 오류·복구·운영 시나리오 없음
- 실제 Consumer·DB·Runtime 연결 없음
- JavaDoc·주석·OpenAPI·Guide·Test 불일치
- Generator가 다른 규격을 생성
- 외부 제품 미설치인데 실연동 성공으로 기록
- 최신 SHA Evidence 없음

## 10. ADM 관제·알림 필수 구조

```text
업무/Batch/연계 오류
→ 표준 Alert Event
→ Notification Outbox
→ 독립 Notification Worker
→ Email/SMS/Webhook/Customer Provider
→ Delivery·Retry·DLQ·Incident·Audit
```

관제 정책은 오류 레벨뿐 아니라 환경·시스템·도메인·업무그룹·서비스/API·Batch Job·연계기관·오류그룹·반복횟수·지속시간·실패율·업무시간·휴일을 조합한다. 중복 억제, 집계, 무음시간, 발송 제한, Escalation, 담당자 지정, 확인/처리중/종료/재발 상태를 ADM에서 관리한다.

## 11. ADM Instance·로그 관리 필수 구조

```text
환경 → 플랫폼/클러스터 → 시스템/도메인 → Application Group
→ Web/Gateway/Batch/Scheduler/Worker/Agent Instance
```

로그는 등록된 Instance/Agent와 허용 Log Root에서만 조회한다. 파일 목록, Current/Rotation/Archive, 단일 Streaming, 다중 ZIP, 여러 Instance 시간대/Trace 묶음, 종료 Instance Archive, 권한·사유·승인·마스킹·감사·임시파일 cleanup을 포함한다.

## 12. 완료 판정 금지 조건

- 최신 SHA CI/Workflow가 없거나 필수 Check 미실행
- Java25/Gradle9.1 전체 Build·Test 미실행 또는 실패
- WAS·MariaDB로 가능한 실제 통합 검증을 수행하지 않음
- Current/Matrix/Handover/Ledger/Evidence 기준 SHA 불일치
- Interface·Route·Table·Swagger 문자열만 존재하고 Consumer/Runtime 없음
- ADM/BZA가 다른 Owner DB를 직접 변경
- Raw client 직접 사용 또는 Provider 유형 Public API 누출
- Batch Lock/Ghost/재처리 위험조치에 권한·사유·승인·CAS/Fencing·감사 누락
- 로그 다운로드에 Path 방어·마스킹·감사·cleanup 누락
- 알림이 원 거래 Transaction을 오염하거나 storm 보호 없음
- 예제·Generator·EDU·OpenAPI·JavaDoc·Test·Guide 불일치
- 미설치 외부 제품을 실연동 완료로 기록
- 과거 Commit/Overlay Evidence를 최신 성공으로 승계

## 13. Evidence 계약

각 Evidence는 최소 다음을 포함한다.

- exact Commit SHA
- 실행 명령
- 시작·종료 시각
- Java/Gradle/Node/DB/WAS/Profile 환경
- 관련 QA ID와 실행 Scenario ID
- 실제 stdout/stderr·HTTP·SQL·Browser 결과
- 민감정보 제거 여부
- PASS/FAIL/미검증 근거
- 생성 파일 hash와 유효기간

## 14. 산출물

- 본 요청서: `CPF_ENTERPRISE_QA_REQUEST_20260728_03.md`
- 상세 Delta Inventory: `CPF_ENTERPRISE_QA_DELTA_INVENTORY_20260728_03.csv`
- Delta 실행 시나리오: `CPF_ENTERPRISE_QA_DELTA_SCENARIOS_20260728_03.csv`
- 수량·Hash Manifest: `CPF_ENTERPRISE_QA_MANIFEST_20260728_03.json`

이 문서는 구현을 완료했다고 주장하는 문서가 아니라, 최신 master 전체를 기준으로 결함을 수리하고 제품 완료 여부를 다시 판정하기 위한 요청서다.
