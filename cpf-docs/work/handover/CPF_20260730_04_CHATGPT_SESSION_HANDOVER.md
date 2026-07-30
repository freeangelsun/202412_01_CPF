# CPF 20260730_04 ChatGPT Session Handover

## 1. 문서 목적

이 문서는 다음 ChatGPT 세션이 이전 대화 없이도 CPF 개발을 정확히 이어받기 위한 인수인계 정본이다.

다음 세션은 이 문서와 `cpf-docs/work/current/CPF_20260730_04_REMAINING_DEVELOPMENT_REQUEST.md`를 먼저 읽고 최신 `master`의 실제 Source·SQL·Frontend·Test·Script·문서를 다시 확인한 뒤 개발을 시작한다.

ChatGPT의 역할은 **직접 개발·수리·정합성 관리**이고, Codex는 크레딧을 아끼기 위해 **최종 독립 검수자**로 사용한다. Codex에 광범위한 신규 개발을 넘기지 않는다.

---

## 2. Repository 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 최신 Push 확인 SHA: `8fb30708f4accc189c00c6fbf020ab4b22f6c51f`
- Commit Message: `20260730_03`
- Commit 변경 규모: 140개 파일, `+33,790 / -12,915`
- 이전 Overlay 개발 기준 SHA: `fae7aa9643f646db4bcbcf665d13b8f3b809e8c8`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 최신 Git 실제 구현 상태가 문서의 완료 표시보다 우선한다.

최신 SHA는 이 문서 작성 시점의 기준이다. 다음 세션 시작 시 반드시 `origin/master`를 다시 조회하여 달라졌으면 새 SHA를 기준으로 전수 재판정한다.

---

## 3. 이번 세션에서 수행한 일

1. 사용자가 최종 개발 Overlay를 적용하고 Push한 뒤 GitHub의 최신 `master`를 재확인했다.
2. 최신 Commit `8fb30708...`의 변경 내용과 주요 SQL·Script·현재 작업 문서를 다시 검수했다.
3. Push 및 Overlay 반영 자체는 확인했다.
4. 그러나 전체 Build·공식 DB 3종·Redis·다중 인스턴스·Browser Runtime 검증은 실행되지 않았으므로 제품 전체 완료로 판정하지 않았다.
5. 최신 Git 재검수 중 Clean Install SQL의 FK 생성 순서와 Identity 불일치 등 추가 개발 결함을 확인했다.
6. Batch Job Definition과 Gateway 운영 기능은 관리 계약과 화면이 추가되었지만 실제 Runtime Consumer·Apply/Test 흐름의 완전한 연결 Evidence가 없어 완료 처리를 금지했다.
7. 기존 `current` 요청서와 Handover가 이전 SHA `fae7aa...`를 가리키고 있으며, `current` 폴더에 과거 요청서·검수서·완료 보고가 다수 남아 있는 Repository Hygiene 문제를 확인했다.
8. QA 신규 요건 목록은 아직 사용자에게서 전달받지 않았다.

---

## 4. 현재 상태 판정

| 영역 | 현재 판정 | 근거 |
|---|---|---|
| 최신 Push 반영 | 완료 | `master=8fb30708...` 확인 |
| Overlay 주요 Source 반영 | 완료 | 최신 Commit 140개 파일 변경 확인 |
| 전체 Java Build/Test | 미검증 | 최신 Clean SHA 전체 실행 Evidence 없음 |
| ADM/BZA Build·Typecheck·Lint·Vitest | 미검증 | 최신 SHA 실행 Evidence 없음 |
| Canonical DB Artifact 생성 | 부분 구현 | Clean Install FK 순서 및 Identity 결함 확인 |
| Oracle/PostgreSQL/MariaDB Lifecycle | 미검증 | 실제 DB Upgrade/Rollback/Reapply/Clean Install Evidence 없음 |
| Batch Job Definition 관리 | 부분 구현 | DB/API/UI 존재, Runtime 실행 연결과 승인 전이 폐쇄 미확인 |
| Gateway Registry/Binding 관리 | 부분 구현 | 저장·조회 기반 존재, Apply/ACK/Test/Reconcile 운영 흐름 폐쇄 미확인 |
| Runtime Policy Distribution | 부분 구현 | Durable 계약 존재, Decode/Row Mapping/실제 다중 인스턴스 검증 필요 |
| File/Shell 보안 기능 | 부분 구현 또는 재확인 필요 | 설정과 Fail-closed 일부 존재, 실제 Scanner/Verifier Consumer 확인 필요 |
| Exact-SHA 문서·Evidence | 실패/재확인 필요 | Active 문서가 이전 SHA를 가리킴 |
| 최종 제품 완료 | 재확인 필요 | P0 개발 결함과 Runtime 미검증이 남음 |

---

## 5. Git에서 확인한 중요 결함

### 5.1 Clean Install DDL 생성 순서

최신 PostgreSQL `cpf-tools/db/vendor/postgresql/install/00_empty_install.sql`에서 다음 자식 Table이 FK 부모 Table보다 먼저 생성된다.

- `cpf_gateway_apply_status` → 부모 `cpf_gateway_binding`
- `cpf_gateway_attempt` → 부모 `cpf_gateway_transaction`
- `cpf_gateway_binding` → 부모 `cpf_gateway_server_group`
- `cpf_runtime_policy_delivery` → 부모 `cpf_runtime_policy_event`
- `BATCH_JOB_EXECUTION` → 부모 `BATCH_JOB_INSTANCE`

이 문제는 개별 Vendor SQL을 직접 재배열하는 방식으로 고치지 않는다. Canonical Schema와 Generator가 FK 의존성 기반 Topological Ordering을 수행하도록 수정하고 3개 공식 Vendor Source·Install·Migration Artifact를 재생성해야 한다.

### 5.2 Batch Definition Audit Identity 불일치

Migration의 `bat_job_definition_audit.audit_id`는 Identity/Auto Increment 성격인데 Clean Install 정본에서는 단순 `BIGINT/NUMBER NOT NULL`로 생성된 부분이 확인됐다.

Runtime Insert가 `audit_id`를 직접 공급하지 않는 구조라면 Clean Install DB에서 실패한다. Canonical PK Generation Policy를 정하고 Oracle/PostgreSQL/MariaDB의 Identity 또는 Sequence 계약을 동일하게 맞춰야 한다.

### 5.3 Oracle 빈 문자열 계약

Oracle Source/Migration에 `VARCHAR2(...) NOT NULL DEFAULT ''` 형태가 다수 생성됐다. Oracle은 빈 문자열을 `NULL`로 처리하므로 `NOT NULL DEFAULT ''`를 Vendor 중립 계약으로 사용할 수 없다.

Canonical에서 Optional Text를 `NULL`로 둘지 명시적 Sentinel을 사용할지 정하고, Java DTO·Repository Mapping·3 Vendor DDL·Migration·Test를 함께 맞춘다.

### 5.4 Active 문서 SHA 불일치

- `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- `cpf-docs/work/handover/CPF_20260730_CHATGPT_DIRECT_IMPLEMENTATION_FINAL_HANDOVER.md`

위 문서가 이전 Overlay SHA `fae7aa...`를 가리킨다. 최신 `check-work-context-sha.ps1` 정책과 충돌하므로 최신 Clean Source SHA에서 Active 문서와 Current Evidence를 현행화해야 한다.

### 5.5 Current 폴더 정본 혼재

`cpf-docs/work/current`에 20260729 요청서·검수서·완료 보고·Checkpoint·다수 Codex 요청이 함께 남아 있다. Current에는 현재 해야 할 일만 남기고, 과거 문서는 Archive/Handover로 이동해야 한다.

단, 삭제 전 Archive 사본과 참조 링크를 보존하고 사용자 승인 없는 Commit/Push는 하지 않는다.

---

## 6. Runtime 연결상 완료 처리 금지 영역

다음은 일부 Source·DB·UI가 존재하더라도 실제 Consumer와 운영 흐름이 최신 SHA에서 끝까지 연결되었는지 확인되지 않은 영역이다. 다음 세션은 Source 검색과 Test를 통해 연결 여부를 먼저 입증하고, 누락 시 개발한다.

### Batch

- `PUBLISHED` Job Definition이 Scheduler·Worker·Agent Runtime 입력으로 Projection되는가
- Definition Version과 실제 실행 이력에 동일한 Version/Checksum이 남는가
- 상세조회 후 수정 시 Parameter·Dependency가 유실되지 않는가
- 상태 변경을 Client Body가 임의 우회하지 못하는가
- Maker와 Approver가 분리되고 권한·사유·감사가 적용되는가
- Misfire·Dependency·Retry·Unknown Result·Compensation·SLA가 실제 실행계에 반영되는가

### Gateway

- ADM Controller가 실제 `CpfGatewayRegistryPort` Adapter Bean과 연결되는가
- Binding 승인·Publish·Instance Apply·ACK·Retry·Drift·Reconcile·Rollback이 있는가
- Connection Test가 단순 결과 기록이 아니라 실제 연결시험을 수행하는가
- Transaction/Attempt 원장이 실제 Gateway 호출 흐름에서 저장되는가
- 각 ADM 메뉴가 동일 화면 별칭이 아니라 목적별 운영 흐름을 제공하는가
- 위험 조치에 권한·사유·확인·감사·결과 추적이 적용되는가

### Runtime Policy

- Metadata Encode/Decode가 다중 항목과 특수문자를 손실 없이 처리하는가
- Oracle의 대문자 Column Label에도 Repository Mapping이 동작하는가
- Claim·Lease·Fencing·ACK·Retry·Poison/Failed 상태가 다중 인스턴스에서 안전한가
- 정책 배포 실패가 원 업무 Transaction을 불필요하게 Rollback시키지 않는가

### File/Shell Security

- Malware Scan Required가 실제 Scanner SPI/구현을 호출하는가
- Signature Required가 실제 Signature Verifier를 호출하거나 지원 불가를 제품 정책으로 명확히 차단하는가
- Interpreter Version Pinning이 실행 전에 검증되는가
- Secret은 Alias/Reference로만 전달되고 로그·명령행·Evidence에 원문 노출되지 않는가

---

## 7. 반드시 보호할 기존 결정

- 공식 DB Vendor는 Oracle, PostgreSQL, MariaDB 3종만 유지한다.
- MySQL, MSSQL, H2를 공식 지원 Vendor로 되돌리지 않는다.
- Generated Domain은 하나의 Golden Template을 사용한다.
- MBR·ACC·EXS를 고정 공식 업무 Module 또는 Generator 예외로 되돌리지 않는다.
- `cpf-core`에는 Topology-independent 핵심 API/SPI만 두고 Admin·Batch Runtime·특정 업무 구현을 넣지 않는다.
- Batch Runtime Ownership은 `cpf-batch`에 유지한다.
- 외부 Module이 `com.cpf.core.common.*` 또는 Internal 구현 Package를 직접 Import하지 않게 한다.
- Gateway를 사용하는 경우 인증·권한·감사·Fail-closed를 적용한다.
- Raw JSON Textarea/`<pre>` 중심 관리 화면으로 회귀하지 않는다.
- Source, SQL, API, Test, Generator, Guide, Evidence를 하나의 변경 단위로 맞춘다.
- 실행하지 않은 Test를 성공으로 기록하지 않는다.
- 사용자 승인 없이 Commit, Push, Branch, Tag, PR을 생성하지 않는다.

---

## 8. QA 신규 요건 수신 시 처리 규칙

현재 QA 신규 요건은 미수신 상태다.

사용자가 QA 목록을 전달하면 다음 순서로 처리한다.

1. 각 QA 항목을 원문 그대로 식별한다.
2. 이 문서와 다음 개발 요청서의 Requirement ID에 Mapping한다.
3. 동일 Root Cause·Owner·Consumer·Failure Mode이면 중복으로 연결하고 신규 개발 건수로 중복 집계하지 않는다.
4. 기존 항목으로 설명되지 않는 요구만 신규 `QA-NEW-*` ID를 부여한다.
5. 개발 요구와 검증 Scenario를 구분한다.
6. 구현 전 사용자에게 다음 수치를 보고한다.
   - 기존 개발 Requirement 수
   - QA 신규 고유 개발 Requirement 수
   - 기존 Requirement에 흡수된 QA 중복 수
   - 검증 Scenario 수
7. QA에 명시되지 않았더라도 CPF 최종 목표에 필요한 구조 결함은 개발 범위에 포함한다.
8. 부분 구현·미구현·TODO 상태로 종료하지 않는다.

---

## 9. 다음 세션 시작 순서

1. 최신 `origin/master` SHA와 Clean Working Tree를 확인한다.
2. `CPF_FINAL_TARGET_REQUIREMENTS.md`, 이 Handover, Remaining Development Request, Current Request를 읽는다.
3. 기존 문서의 완료 선언을 신뢰하지 말고 실제 Source·SQL·API·Consumer·Test를 양방향 추적한다.
4. P0 DB 정본 결함부터 수정한다.
5. Batch와 Gateway의 실제 Runtime Consumer 연결을 확인하고 누락을 완성한다.
6. Runtime Policy와 File/Shell 보안을 완성한다.
7. Generator와 3 Vendor Artifact를 재생성하고 Drift Gate를 실행한다.
8. Java/Frontend/Static Gate를 실행하고 실패를 수정한다.
9. 가능한 Runtime 환경에서 DB·Redis·다중 인스턴스·Browser Scenario를 실행한다.
10. 최신 Clean SHA의 실제 Evidence만 Matrix/Ledger에 연결한다.
11. Current/Handover/Continuity 문서를 최신 SHA로 갱신한다.
12. 사용자가 직접 Commit/Push한 뒤 Codex 독립 검수를 요청한다.

Windows PowerShell 시작 확인 예시:

```powershell
cd "C:\dev\projects\jck\202412_01_CPF"; git fetch --all --prune; git checkout master; git pull --ff-only origin master; git rev-parse HEAD; git status --short
```

---

## 10. 완료 판정 금지 조건

다음 중 하나라도 존재하면 전체 완료로 판정하지 않는다.

- Clean Install SQL이 FK 부모보다 자식을 먼저 생성함
- 3 Vendor의 PK Generation·Default·FK·Index가 다름
- Batch Definition에 실제 Scheduler/Worker Consumer가 없음
- Gateway Binding에 실제 Apply/ACK/Test/Reconcile 흐름이 없음
- Client가 상태 전이를 우회할 수 있음
- 보안 설정은 있으나 실제 Scanner/Verifier Consumer가 없음
- 전체 Gradle Build/Test가 최신 Clean SHA에서 미실행 또는 실패
- ADM/BZA Build·Typecheck·Lint·Vitest가 미실행 또는 실패
- 3개 공식 DB Lifecycle이 미실행 또는 실패
- Redis·다중 인스턴스·Gateway·Batch·Browser 핵심 Scenario가 미실행 또는 실패
- Evidence Source SHA가 최신 Source SHA와 다름
- Matrix/Ledger에 `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`가 남음
- Working Tree가 Dirty이거나 Local/Remote SHA가 다름
- Test를 삭제·약화·Skip하여 Gate를 통과시킴
- 과거 Evidence나 다른 장비의 결과를 현재 SHA 성공으로 승계함

---

## 11. 다음 산출물

다음 개발 세션 종료 시 최소 다음을 남긴다.

- 변경 Source·SQL·Frontend·Script·Test
- Canonical DB와 3 Vendor 재생성 Artifact
- Generator Golden Template 정합성 결과
- 정상·오류·경계·부분 실패 Test
- 실행한 명령·Profile·시작/종료 시각·Source SHA·결과가 있는 Evidence
- 최신 Current Request
- 최종 Development Report
- 최신 Handover/Continuity
- Codex Review-only Request
- Repository Hygiene 정리 내역

