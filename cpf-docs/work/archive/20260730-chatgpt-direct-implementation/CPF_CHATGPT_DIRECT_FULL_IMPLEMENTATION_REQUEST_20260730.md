# CPF ChatGPT 직접 최종 보완개발 요청서

> 이 문서 하나가 현재 ChatGPT 개발 작업의 전체 요청서다. 별도 채팅 지시문, 요약문, 추가 QA 목록 작성 없이 이 문서와 최신 Repository의 정본을 기준으로 직접 개발한다.

## 0. 문서 식별

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 문서 작성 시 확인 master SHA: `4732d17259e39da93e781fd14cd545b3c897fa87`
- 최우선 제품 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 현재 작업자: **ChatGPT 개발 세션**
- 현재 목적: **결함 목록 작성이 아니라 실제 Source·SQL·API·Frontend·Test·문서 보완개발**
- 포함 개발 Requirement: **405개**
- 포함 검증 Scenario: **90개**
- 포함 확인 결함: **22개**
- 포함 공통 영향도 Root: **12개**

## 1. 최우선 실행 지시

1. 최신 `origin/master`를 먼저 확인한다. 문서 작성 시 SHA보다 새 Commit이 있으면 최신 master를 기준으로 전체 상태를 다시 판단한다.
2. Repository 정본 문서와 실제 Source를 확인한 뒤 개발을 시작한다. 과거 보고서의 완료 표시는 근거로 사용하지 않는다.
3. 이 문서의 Requirement와 결함을 다시 요약하거나 새 요청서만 작성하고 종료하지 않는다.
4. 구현 가능한 항목은 직접 Source·SQL·API·Frontend·Test·Guide에 구현한다.
5. Interface, DTO, 화면 껍데기 또는 Test Stub만 추가하고 완료 처리하지 않는다.
6. 한 결함을 고칠 때 같은 Root Cause와 패턴을 Repository 전체에서 검색해 함께 수정한다.
7. 정상·오류·경계·부분 실패·다중 인스턴스·재시도·복구·권한·감사·마스킹을 하나의 완료 단위로 다룬다.
8. 외부 서버가 없어도 Source, Configuration, Recovery, Unit/Contract/Failure Injection Test는 완결한다. 실제 외부 Runtime 실행만 `미검증`으로 분리한다.
9. 실행하지 않은 검증을 성공으로 기록하지 않는다.
10. 사용자의 명시적 승인 없이 Commit, Push, Branch, Tag, PR을 생성하지 않는다.

## 2. 작업 시작 절차

```powershell
git status --short
git branch --show-current
git fetch origin
git checkout master
git pull --ff-only origin master
git rev-parse HEAD
git rev-parse origin/master
```

기존 Local 변경이 있으면 무조건 삭제하거나 덮어쓰지 않는다. 파일별 소유자와 목적을 확인하고 현재 작업과 통합한다.

먼저 다음 정본과 실제 구현을 확인한다.

- `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- `cpf-docs/governance/CPF_CANONICAL_PATH_AND_ROLE_MAP.md`
- `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`
- `cpf-docs/governance/CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- `cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md`
- `최신 Handover, Review, Evidence, Requirement/QA Matrix`
- 실제 Source, SQL, API, Test, Frontend, Config, Script

## 3. 역할과 종료 형태

### 3.1 ChatGPT가 지금 해야 하는 일

- 현재 중간 Push의 Compile, Runtime SQL, Public Boundary, 문서 정합성 결함을 먼저 닫는다.
- 확인 결함과 사용자 논의 요구를 Work Package 순서대로 실제 구현한다.
- 공통 Contract·DB·UI Component를 먼저 구현하고 모든 Consumer를 이관한다.
- 구현한 범위의 Unit, Contract, Static, Frontend Test를 직접 실행한다.
- 세션 종료 시 Source 상태와 실행 결과를 Handover에 정확히 기록한다.

### 3.2 현재 하지 않는 일

- Codex에게 지금 개발을 넘기지 않는다.
- Codex 검수 요청서만 새로 만들고 종료하지 않는다.
- 사용자에게 세부 기능을 다시 선택해 달라고 요구하지 않는다.
- 크레딧 절약을 이유로 오류 처리, 복구, Test, Evidence를 생략하지 않는다.
- 기존 성공 기능을 임시 호환 코드로 덮어 회귀시키지 않는다.

### 3.3 Codex의 시점

Codex는 ChatGPT 개발과 자체 검수가 반복되어 구현 가능한 `부분 구현`과 `미구현`이 0이 된 후, 크레딧이 확보된 시점에 최신 master를 독립 검수한다. 이 문서의 WP17은 그 최종 검수 기준이며 지금 개발을 Codex에게 넘긴다는 뜻이 아니다.

## 4. 상태와 근거 분류

허용 상태:

- `완료`
- `부분 구현`
- `미구현`
- `미검증`
- `실패`
- `재확인 필요`

| 분류 | 의미 | 처리 |
|---|---|---|
| SourceConfirmed | 최신 Git Source에서 직접 확인 | 즉시 수정 또는 유지 근거 기록 |
| ReportedUnverified | 작업 보고 또는 문서 주장만 존재 | Source/Test로 재확인 |
| RuntimeRequired | Source 개발 후 실제 환경 검증 필요 | 개발 완료와 Runtime 미검증 분리 |
| NewRequirement | 사용자 논의와 제품 목표에서 확정 | Owner/Consumer/DB/UI/Test까지 구현 |

## 5. 현재 Repository 판정

문서 작성 시점의 최신 master `4732d17259e39da93e781fd14cd545b3c897fa87`는 최종 완료 Commit이 아니라 중간 Push로 판정한다.

- Continuity가 현재 상태를 `부분 구현`으로 기록한다.
- 전체 Clean Build, Frontend, Final Gate, 기존 DB 최신 재검증, 분리 Clean Install이 완료되지 않았다.
- Current Work Request와 Handover가 과거 SHA 및 과거 ChatGPT/Codex 역할을 유지한다.
- Gateway, Service Registry, Log Policy, Batch File/Shell UI가 사용자 논의 수준에 도달하지 않았다.
- GitHub Commit CI/Workflow Evidence가 존재하지 않는다.

따라서 이전 문서의 `완료` 표시는 최신 Source와 exact-SHA Evidence가 일치할 때만 승계한다.

## 6. 공통 영향도 분석과 작업 순서

| Root | 공통 원인 | 관련 WP | Owner | 구현 순서 | 중복 금지 |
|---|---|---|---|---|---|
| ROOT-01 | Service Registry + Gateway Binding | WP04,WP05,WP06,WP07,WP09 | cpf-core API/cpf-gateway | Contract→DB→Selector→Gateway→ADM | 별도 IP 원장 금지 |
| ROOT-02 | Health + Runtime State | WP04,WP06,WP07 | cpf-core health SPI/Worker | State→Probe→Lease→Routing→Dashboard | 대상별 Health 모델 복제 금지 |
| ROOT-03 | Transaction + Attempt Log | WP07,WP08,WP14,WP15 | cpf-core logging | Contract→DB→Producer→Sink→ADM | 목적 없는 로그 중복 금지 |
| ROOT-04 | Code Catalog + Dynamic Form | WP03,WP05,WP10,WP13 | cpf-core catalog/cpf-admin | Code→API→Shared UI→Feature | 문자열 하드코딩 금지 |
| ROOT-05 | Approval/Audit/Reason | WP04,WP05,WP08,WP10,WP12,WP14 | cpf-admin approval | Action→Approval→Owner Command→Audit | 기능별 승인 복제 금지 |
| ROOT-06 | Batch Job + Parameter | WP10,WP11,WP12,WP13 | cpf-batch contract | Contract→Adapter→Agent→ADM | Executor별 Parameter 복제 금지 |
| ROOT-07 | Generator + Domain Setup | WP02,WP04,WP09,WP15 | cpf-tools/generator | Contract→Template→Golden→Smoke | Domain 이름 예외 금지 |
| ROOT-08 | ADM Information Architecture | WP03,WP04,WP07,WP08,WP10 | cpf-admin frontend | IA→Manifest→Feature→E2E | 다중 편집 Owner 금지 |
| ROOT-09 | Vendor-neutral SQL | WP01,WP15,WP16 | cpf-tools/db | Canonical→Generate→Static→Runtime | 업무 Module SQL 복제 금지 |
| ROOT-10 | Public API/SPI Boundary | WP01,WP02,WP09,WP16 | cpf-core | Review→Move→Consumer→Delete Legacy | Gate 예외로 Internal 허용 금지 |
| ROOT-11 | Realtime Event Delivery | WP06,WP07,WP08 | event SPI/cpf-admin | Durable Event→Stream→UI | 브라우저 수와 Probe 수 결합 금지 |
| ROOT-12 | Final Verification/Evidence | WP00,WP01,WP16,WP17 | Governance | Development→Exact SHA→Codex Audit | 유효 SHA 검증 반복 방지 |

### 6.1 전체 Phase

#### Phase 1 — 현재 중간 Push 정상화

WP00, WP01: Compile·Runtime SQL·Public Boundary·DB·문서/Evidence 충돌을 닫는다.

#### Phase 2 — 공통 기반

WP02, WP13, WP14, WP15: API/SPI, Code Catalog, Parameter, 승인·감사, Canonical DB를 완결한다.

#### Phase 3 — Gateway

WP04~WP09: Registry, Protocol, Route, Server Group, Health, Test, Dashboard, Log, Typed Client를 연결한다.

#### Phase 4 — Batch

WP10~WP12: Job 등록, File Watch/Transfer, Approved Shell과 Agent 복구를 완결한다.

#### Phase 5 — ADM와 전체 검증

WP03, WP16: 5개 메뉴, Feature Package, Browser, Build, DB, Evidence, Hygiene를 마무리한다.

#### Phase 6 — 최종 Codex 검수

WP17: 개발 완료 Push 이후 독립 exact-SHA 검수만 수행한다.

## 7. Source 확인 결함 원장

### DEF-001 — 최신 Push가 최종 완료가 아닌 부분 구현 상태

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md`
- 관찰 내용: Checkpoint 7이 전체 Clean Build, Frontend, Final Gate, Clean Install, 문서/Evidence 동기화를 미완료로 명시한다.
- 제품 위험: 중간 Push를 최종 완료로 오판할 수 있다.
- 필수 조치: 미완료 작업을 구현·검증하고 최신 SHA로 상태 문서를 갱신한다.
- 현재 상태: `미구현`

### DEF-002 — Continuity 기준 SHA가 최신 master와 불일치

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md`
- 관찰 내용: 문서는 2e3e46d... 기준이나 실제 master는 4732d172...다.
- 제품 위험: Evidence가 현재 Source를 증명하지 못한다.
- 필수 조치: 시작·최종·Evidence SHA를 동기화한다.
- 현재 상태: `미구현`

### DEF-003 — Current Work Request가 과거 역할과 SHA를 유지

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- 관찰 내용: b894157... 기준이며 Codex는 Source를 수정하지 않는다고 기록한다.
- 제품 위험: 다음 작업자가 잘못된 지시를 따른다.
- 필수 조치: 현재 개발·검증 지시로 교체한다.
- 현재 상태: `미구현`

### DEF-004 — 최종 인수인계가 과거 SHA와 역할을 유지

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-docs/work/current/CPF_20260729_04_FINAL_HANDOVER.md`
- 관찰 내용: 최신 대규모 변경과 미완료 상태가 반영되지 않았다.
- 제품 위험: 세션 연속성이 깨진다.
- 필수 조치: 최신 Source/검증/미검증/다음 시작점을 기록한다.
- 현재 상태: `미구현`

### DEF-005 — 최종 개발 보고서가 최신 Push를 반영하지 않음

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-docs/work/current/CPF_20260729_04_FINAL_DEVELOPMENT_REPORT.md`
- 관찰 내용: 과거 SHA와 검증 환경만 기록한다.
- 제품 위험: False Completion 위험이 있다.
- 필수 조치: 최신 SHA 기준으로 재작성한다.
- 현재 상태: `미구현`

### DEF-006 — 최신 Commit CI/Workflow 근거 부재

- 심각도: `P1`
- 분류: `SourceConfirmed`
- 확인 경로: `GitHub commit 4732d172...`
- 관찰 내용: Combined status와 workflow run이 비어 있다.
- 제품 위험: 독립 자동 검증 근거가 없다.
- 필수 조치: CI 또는 exact-SHA 로컬 통합 Evidence를 확보한다.
- 현재 상태: `미구현`

### DEF-007 — ADM 정보구조가 합의한 5개 메뉴와 불일치

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-admin/frontend/src/app/routes.ts`
- 관찰 내용: 8개 기술 중심 그룹이며 Gateway 관리 메뉴가 없다.
- 제품 위험: 운영 흐름 중복과 Ownership 혼란이 지속된다.
- 필수 조치: 홈+5개 최상위 메뉴로 재구성한다.
- 현재 상태: `미구현`

### DEF-008 — 서비스 레지스트리 화면이 조회 전용 압축 단일 파일

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-admin/frontend/src/features/service-registry/ServiceRegistryPage.vue`
- 관찰 내용: CRUD, 서버 그룹, Protocol, Health, 연결시험, 적용 이력이 없다.
- 제품 위험: Gateway 연동 서버를 운영할 수 없다.
- 필수 조치: 기능별 Package와 등록→검증→적용 흐름을 구현한다.
- 현재 상태: `미구현`

### DEF-009 — 서비스 레지스트리 API가 Map 중심

- 심각도: `P1`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-admin/.../AdmServiceRegistryController.java`
- 관찰 내용: List<Map>와 Map 응답이 중심이다.
- 제품 위험: 타입·OpenAPI·호환성 계약이 약하다.
- 필수 조치: Typed DTO/Page/Result/Error로 전환한다.
- 현재 상태: `미구현`

### DEF-010 — Gateway 운영 핵심 Registry 모델 누락

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-core/.../CpfServiceRegistryControlPort.java`
- 관찰 내용: Server Group, Protocol, Health Method, Gateway Binding, Connection Test가 없다.
- 제품 위험: SLB 없는 서버군과 Gateway 연동을 표현할 수 없다.
- 필수 조치: 공통 Registry와 Gateway Binding 계약을 추가한다.
- 현재 상태: `미구현`

### DEF-011 — Gateway Route 모델이 운영 정책을 표현하지 못함

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-core/.../CpfGatewayRoute.java`
- 관찰 내용: 서버 그룹, 타깃 Protocol, TLS, Header, Rate Limit, Health, Failover가 없다.
- 제품 위험: 실제 상용 Routing이 불가능하다.
- 필수 조치: Versioned External Route Contract로 확장한다.
- 현재 상태: `미구현`

### DEF-012 — Gateway 설정이 Route Refresh 외 Capability를 제공하지 않음

- 심각도: `P1`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-gateway/src/main/resources/application.yml`
- 관찰 내용: Policy Sync, Health, Log Spool, Protocol 안전 상한이 없다.
- 제품 위험: Fail-closed Bootstrap이 어렵다.
- 필수 조치: Property 안전 상한과 ADM Runtime Policy를 분리한다.
- 현재 상태: `미구현`

### DEF-013 — 로그 정책 UI가 Y/N·자유문자열·Raw JSON 중심

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-admin/frontend/src/features/log-policies/LogPoliciesPage.vue`
- 관찰 내용: Body 저장 Y/N와 pre JSON을 사용한다.
- 제품 위험: 민감정보 과다 저장과 운영 오류 위험이 있다.
- 필수 조치: Code Select, Allowlist, Masking Preview, 적용 결과 UI로 교체한다.
- 현재 상태: `미구현`

### DEF-014 — 로그 정책 다중 인스턴스 전파 미완료

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-admin/.../AdmLogPolicyService.java`
- 관찰 내용: 다중 인스턴스 Broker 전파가 별도 보강으로 남아 있다.
- 제품 위험: 정책 Drift와 부분 적용 실패가 발생한다.
- 필수 조치: Durable Broadcast, ACK, CAS, Rollback/Reconcile를 구현한다.
- 현재 상태: `미구현`

### DEF-015 — 거래 로그 화면 IN/Gateway/OUT/Attempt 구분 부족

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-admin/frontend/src/features/logs/LogsPage.vue`
- 관찰 내용: 기본 행과 pre 상세만 있고 Route/Group/Attempt Timeline이 없다.
- 제품 위험: 장애 원인 추적이 어렵다.
- 필수 조치: 구간별 Timeline과 Attempt Table로 재설계한다.
- 현재 상태: `미구현`

### DEF-016 — Batch Job Pack 화면 조회 전용

- 심각도: `P1`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-admin/frontend/src/features/batch-job-packs/BatchJobPacksPage.vue`
- 관찰 내용: Job 등록·Executor·Parameter·Schedule·Recovery 편집이 없다.
- 제품 위험: 운영 등록이 불가능하다.
- 필수 조치: Job Definition Wizard를 구현한다.
- 현재 상태: `미구현`

### DEF-017 — Batch Parameter 계약 부족

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-batch/contract/.../BatchParameterDefinition.java`
- 관찰 내용: 6개 기본 필드뿐이다.
- 제품 위험: Enum/범위/Secret/Alias/UI를 표현할 수 없다.
- 필수 조치: Versioned Typed Parameter Schema로 확장한다.
- 현재 상태: `미구현`

### DEF-018 — File Watch가 Local ENTRY_CREATE 수준

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-batch/worker/.../ApprovedFileExecutor.java`
- 관찰 내용: Stable Window, Marker, Checksum, Remote Provider, Lease가 없다.
- 제품 위험: 전송 중/중복 처리 위험이 있다.
- 필수 조치: 상용 File Watch/복구 기능을 구현한다.
- 현재 상태: `미구현`

### DEF-019 — Approved Shell 보안·복구 불완전

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-batch/worker/.../ApprovedShellExecutor.java`
- 관찰 내용: 파라미터가 Command Line에 노출되고 Parent만 종료한다.
- 제품 위험: Secret 노출·Child 잔류·결과 불명 위험이 있다.
- 필수 조치: 안전 전달, Process Tree 종료, 출력 제한, 서명 검증을 구현한다.
- 현재 상태: `미구현`

### DEF-020 — 최종 요청서에 합의 Gateway·로그 UX 상세 미반영

- 심각도: `P0`
- 분류: `SourceConfirmed`
- 확인 경로: `CPF_CODEX_FINAL_FULL_VALIDATION...md`
- 관찰 내용: 타깃 서버 관리, 연결시험, Dashboard, IN/GW/OUT 로그 상세가 없다.
- 제품 위험: 개발 범위가 축소될 수 있다.
- 필수 조치: 새 상세 Requirement/Scenario 정본으로 연결한다.
- 현재 상태: `미구현`

### DEF-021 — Gateway Runtime 구현의 Core Ownership 재검토 필요

- 심각도: `P1`
- 분류: `SourceConfirmed`
- 확인 경로: `cpf-core/.../CpfGatewayRouteCatalog.java`
- 관찰 내용: Gateway Snapshot 구현이 cpf-core common에 있다.
- 제품 위험: Core가 선택 Runtime으로 비대해질 수 있다.
- 필수 조치: Core Contract와 Gateway Runtime 구현을 분리한다.
- 현재 상태: `미구현`

### DEF-022 — 대규모 변경 후 exact-SHA 검증 중단

- 심각도: `P0`
- 분류: `ReportedUnverified`
- 확인 경로: `사용자 제공 Codex 작업 로그`
- 관찰 내용: Runtime SQL Gate 강화 도중 사용 한도에 도달했다.
- 제품 위험: Compile/Gate 실패 상태일 수 있다.
- 필수 조치: 최신 master 전체 Clean Build/Gate를 재실행한다.
- 현재 상태: `미검증`

## 8. 공통 Architecture 원칙

- `cpf-core`: topology-independent Public API/SPI와 기술 Runtime 기반만 소유한다.
- `cpf-common`: 고객 업무 공통 Extension을 소유한다.
- `cpf-admin`: 플랫폼 운영 Control Plane, 조회·승인·감사 UI를 소유한다.
- `cpf-biz-admin`: 고객 업무 관리자와 업무 결재를 소유한다.
- `cpf-batch`: Scheduler, Worker, Agent, Runner, Center-Cut Runtime을 소유한다.
- `cpf-gateway`: 외부 진입, Routing, Gateway 정책 집행을 소유한다.
- 내부 ACC→MBR 호출은 Gateway Mandatory Hop으로 만들지 않는다.
- Gateway와 내부 Service Call은 같은 Service Registry 원장을 사용하되 Gateway Binding을 별도로 관리한다.
- Public API/SPI/Internal Package를 분리하고 외부 Module의 Internal Import를 차단한다.
- 새 Interface에는 실제 Consumer, 기본 구현, 오류·복구 Test가 반드시 존재해야 한다.
- Generated Domain 추가 때문에 CPF 본체의 Domain 고정 배열이나 if/switch가 늘어나면 실패다.

## 9. ADM 최종 정보구조

```text
홈
├─ 전체 현황
├─ 나의 업무
├─ 대기 승인
└─ 최근 장애·변경

온라인 운영
├─ 거래 관리
├─ 거래 운영
├─ 거래 조회
├─ 거래 분석
└─ 게이트웨이 관리

배치 운영
├─ 작업 관리
├─ 실행 운영
├─ 작업자 관리
├─ 장애 복구
└─ 실행 조회

연계 관리
├─ 연계 정의
├─ 실시간 연계
├─ 메시지 연계
├─ 파일 연계
├─ 알림 제공자
└─ 연계 실행 내역

통합 관제
├─ 통합 현황
├─ 실시간 관제
├─ 로그 및 추적
├─ 알림 및 장애
└─ 장애 분석

프레임워크 관리
├─ 시스템 구성
├─ 공통 관리
├─ 자원 관리
├─ 사용자 및 권한
├─ 승인 및 감사
└─ 변경 및 데이터베이스 관리
```

한 Capability의 편집 Owner 메뉴는 하나만 둔다. 다른 화면은 동일 API와 Data Source를 사용하는 Filtered Deep Link만 제공한다. 좌측 메뉴는 최대 2단계로 유지하고 세부 기능은 Tab, Drawer, Context Action으로 제공한다.

## 10. Gateway 최종 운영 흐름

```text
채널
→ Gateway 공개 Endpoint
→ 인증·권한·Rate Limit·Header Trust
→ Route Version
→ Service Registry Server Group
→ Health·Circuit·LB 기반 Target Instance
→ ACC/MBR 등 업무 서버
→ 거래/Attempt 원장
→ ADM Dashboard·로그·장애 분석
```

Gateway 관리 메뉴:

```text
온라인 운영
└─ 게이트웨이 관리
   ├─ 대시보드
   ├─ 연동 서버
   ├─ 서버 그룹
   ├─ 경로·라우팅
   ├─ 보안·제한
   ├─ Health·연결시험
   ├─ 거래 조회
   ├─ 로그 정책
   └─ 적용 상태·이력
```

등록 완료는 저장 성공이 아니라 다음을 모두 만족해야 한다.

```text
Service/Endpoint/Instance/Group 등록
+ Target Protocol·TLS·인증 설정
+ Health 정책
+ Gateway 직접 Target 연결시험
+ Gateway 공개 Endpoint 경유 E2E
+ LB·Failover 검증
+ 승인·Version 적용
+ Gateway Instance별 ACK·Drift 확인
+ Dashboard 실시간 관제
+ IN/GATEWAY/OUT/Attempt 거래로그 조회
```

## 11. Gateway 거래로그 최종 모델

| 구간 | 필수 내용 |
|---|---|
| IN | Channel, Source IP/Port, Host, Method, Path, Protocol, TLS, 요청 시각 |
| GATEWAY | Gateway Instance, Route ID/Version, 인증·권한, Rate Limit, Rewrite, 처리시간 |
| OUT | Target Service, Endpoint, Server Group, Target Instance, Host/Port, Protocol, Connect/Response 시간 |
| RESULT | 최종 상태, HTTP/업무 코드, 실패 단계, Unknown, Retry/Failover, 전체 시간 |

Retry와 Failover는 각 Attempt를 별도 원장으로 저장한다. Body, Query, Header 저장은 ADM 정책과 설치 Property 안전 상한을 함께 적용한다. 거래 필수 Metadata는 운영자가 비활성화할 수 없다.

Body Capture Mode:

- `NONE`: 본문 미저장
- `METADATA_ONLY`: 크기·MIME·Schema·Checksum만 저장
- `ALLOWLIST_FIELDS`: Schema 기반 허용 필드만 저장
- `MASKED_BODY`: 정책 기반 마스킹 후 저장
- `ENCRYPTED_BODY`: 승인된 Route에서 암호화 저장

`FULL_RAW_BODY`는 일반 운영 옵션으로 제공하지 않는다. 설치 상한, 별도 권한, 승인, TTL, 감사가 모두 있을 때만 제한적으로 허용한다.

## 12. Batch 최종 모델

```text
기본정보
→ Executor 선택
→ Trigger/Schedule
→ Typed Parameter
→ Agent/Resource
→ Retry/Recovery/Unknown
→ Alert
→ Preview
→ Validation/Connection Test
→ Approval
→ Publish/Deploy
```

지원 Executor:

- `SPRING_BATCH`
- `APPROVED_SHELL`
- `FILE_WATCH`
- `FILE_PROCESS`
- `FILE_TRANSFER`
- `확장 Protocol Adapter`

File Watch는 단순 `ENTRY_CREATE`가 아니라 Stability Window, Marker, Checksum, Header/Trailer, Duplicate, Claim, Lease/Fencing, Restart Scan, Archive, Reconcile를 지원한다.

Approved Shell은 Script Catalog, Version, Hash, Signature, 최소 권한 계정, 안전한 Secret 전달, Process Tree 종료, stdout/stderr 제한, Unknown Result Reconcile를 지원한다.

## 13. 공통 UI·Code·Parameter 표준

- Protocol, Executor, Health Method, Load Balance, 상태, Parameter Type은 Code Catalog API에서 조회한다.
- 화면별 문자열 하드코딩을 금지한다.
- Protocol 또는 Executor 선택에 따라 동적 Form을 제공한다.
- 미설치 Capability는 숨기지 않고 비활성 사유를 표시한다.
- Service→Endpoint→Server Group 같은 종속 Select를 제공한다.
- 대량 항목은 검색·Paging Select를 제공한다.
- Secret은 원문 입력 대신 Secret Reference Picker를 제공한다.
- Raw JSON 입력은 고급 Object Parameter 외에는 금지한다.
- 설정 전 최종 적용값, 영향 대상, 예상 용량, 보안 위험을 Preview한다.
- Backend Validation, OpenAPI, Frontend Form, Generator가 같은 Schema 정본을 사용한다.

## 14. 상세 개발 Requirement

아래 Requirement는 요약이 아니라 직접 구현 대상이다. 각 ID를 Source/API/SQL/UI/Test/Evidence에 연결한다.

### WP00 — 기준선·영향도·크레딧 최적화

#### WP00-R001 — 최신 기준선 확정

- 우선순위: `P0`
- 요구사항: origin/master를 동기화하고 시작 SHA를 기록하며 새 Push가 있으면 새 SHA를 우선한다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R002 — 상태 문서 우선순위

- 우선순위: `P0`
- 요구사항: Current, Continuity, Handover, Report, Evidence의 역할을 재확정하고 중복 정본을 만들지 않는다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R003 — 결함 분류

- 우선순위: `P0`
- 요구사항: 모든 항목을 SourceConfirmed, ReportedUnverified, RuntimeRequired, NewRequirement 중 하나로 분류한다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R004 — 상태 제한

- 우선순위: `P0`
- 요구사항: 완료·부분 구현·미구현·미검증·실패·재확인 필요 외 상태명을 쓰지 않는다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R005 — 영향도 선행 분석

- 우선순위: `P0`
- 요구사항: 수정 전에 Requirement, Owner, Consumer, API, SQL, UI, Generator, Test, Evidence 영향을 기록한다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R006 — 공통 원인 통합

- 우선순위: `P0`
- 요구사항: 같은 Contract/DB/UI Component를 공유하는 결함은 한 Work Package로 묶는다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R007 — 작업 순서

- 우선순위: `P0`
- 요구사항: Contract/Schema→Runtime Adapter→Consumer→ADM UI→Test→통합 검증 순서를 따른다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R008 — 저비용 Gate

- 우선순위: `P0`
- 요구사항: Compile, Unit, Ownership, Secret, Route, SQL Syntax Gate를 개발 중 지속 실행한다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R009 — 고비용 검증 통합

- 우선순위: `P0`
- 요구사항: 전체 Build, 3 Vendor, Browser, Multi-instance는 연결 완료 SHA에서 통합 실행한다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R010 — 중간 Push 판정

- 우선순위: `P0`
- 요구사항: 완료 Evidence 없는 사용 한도/환경 중단 Push는 중간 Push로 판정한다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R011 — 사용자 변경 보호

- 우선순위: `P0`
- 요구사항: 로컬 WIP를 reset, clean, revert로 폐기하지 않는다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R012 — Test 약화 금지

- 우선순위: `P0`
- 요구사항: Test 삭제, Assertion 약화, Gate 범위 축소, 예외 삼킴을 금지한다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R013 — 미실행 성공 금지

- 우선순위: `P0`
- 요구사항: 실행하지 않은 검증을 PASS로 기록하지 않는다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R014 — 검증 재사용 규칙

- 우선순위: `P0`
- 요구사항: 이전 Evidence는 SHA·환경·범위가 동일할 때만 재사용한다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R015 — 크레딧 기록

- 우선순위: `P0`
- 요구사항: 비용 큰 검증의 유효 SHA와 재실행 조건을 Handover에 기록한다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R016 — 독립 수행 요청서

- 우선순위: `P0`
- 요구사항: 다음 세션이 이전 대화 없이 수행할 수 있게 경로·완료·금지 조건을 포함한다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP00-R017 — Commit 통제

- 우선순위: `P0`
- 요구사항: 사용자 명시 승인 전 Branch, Commit, Push, Tag, PR을 생성하지 않는다.
- Owner Module: `Repository Governance`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP01 — 현재 Codex WIP 완결

#### WP01-R001 — Core Runtime SQL 완결

- 우선순위: `P0`
- 요구사항: MariaDB 전용 Inline SQL을 제거하고 3 Vendor 동등 결과를 내는 JDBC/Vendor Catalog 경계를 완결한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R002 — ADM Runtime SQL 완결

- 우선순위: `P0`
- 요구사항: LIMIT, information_schema, ON DUPLICATE KEY, LAST_INSERT_ID 의존을 중립 흐름으로 교체한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R003 — Public Boundary

- 우선순위: `P0`
- 요구사항: ADM/BZA/Common/Reference/Batch/Gateway의 core common/internal 직접 Import를 제거한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R004 — BAT SQL Provider

- 우선순위: `P0`
- 요구사항: Batch Runtime은 주입 가능한 Public SPI로 Vendor SQL을 선택한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R005 — 전체 Compile

- 우선순위: `P0`
- 요구사항: 모든 Java Module compileJava/testClasses를 최신 SHA에서 통과한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R006 — 전체 Test

- 우선순위: `P0`
- 요구사항: 모든 subproject test를 수행하고 동일 Root Cause를 전수 수정한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R007 — 전체 Assemble

- 우선순위: `P0`
- 요구사항: Boot JAR, WAR, Library JAR, Batch Artifact와 Runtime Classpath를 검증한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R008 — ADM Frontend

- 우선순위: `P0`
- 요구사항: Typecheck, Unit, Lint, Production Build를 실행한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R009 — BZA Frontend

- 우선순위: `P0`
- 요구사항: Typecheck, Unit, Lint, Production Build를 실행한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R010 — Final Gate

- 우선순위: `P0`
- 요구사항: qualityGate와 최종 Script를 검토 후 실행하고 False Green/Negative를 수정한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R011 — 기존 MariaDB

- 우선순위: `P0`
- 요구사항: 기존 DB 보존 상태로 Seed Cleanup, Drift, Upgrade, Rollback, Reapply를 검증한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R012 — 분리 Clean Install

- 우선순위: `P0`
- 요구사항: 새 Database/Schema에서 Provision, Install, Seed, Verify를 수행한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R013 — Runtime Query

- 우선순위: `P0`
- 요구사항: MariaDB에서 CPF/BZA/BAT/Generated Domain Runtime Query를 실제 실행한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R014 — PG/Oracle 상태

- 우선순위: `P0`
- 요구사항: Source/Static 완료와 실제 Runtime 미검증을 분리한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R015 — Repository Cleanup

- 우선순위: `P0`
- 요구사항: build, dist, node_modules, tmp, log, dump, 빈 폴더, stale 산출물을 제거한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R016 — 문서 SHA 동기화

- 우선순위: `P0`
- 요구사항: Current, Continuity, Handover, Report, Review, Evidence를 최신 SHA로 갱신한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R017 — Evidence 유효성

- 우선순위: `P0`
- 요구사항: PASS마다 명령, 종료코드, 환경, 시간, SHA를 기록한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R018 — Working Tree

- 우선순위: `P0`
- 요구사항: git diff --check와 git status를 확인하고 잔여 WIP를 명확히 기록한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R019 — CI 대체 근거

- 우선순위: `P0`
- 요구사항: CI가 없으면 exact-SHA 로컬 통합 로그를 공식 Evidence로 보존한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R020 — Commit 이력 설명

- 우선순위: `P0`
- 요구사항: 날짜형 Commit을 Handover에서 실제 변경 범위와 연결한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R021 — 문서 충돌 제거

- 우선순위: `P0`
- 요구사항: 과거 검수 전용 지시와 현재 개발 지시가 동시에 Active로 남지 않게 한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP01-R022 — 완료 수치

- 우선순위: `P0`
- 요구사항: 구현 가능한 미구현·부분 구현 수 0을 Matrix와 Source로 양방향 검증한다.
- Owner Module: `각 Owner Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP02 — Architecture·Ownership·Generated Domain

#### WP02-R001 — Platform Module

- 우선순위: `P0`
- 요구사항: CPF/CMN/ADM/BZA/BAT/GWY/REF를 유지하고 고객 Domain 고정 목록을 만들지 않는다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R002 — Generated Domain 발견

- 우선순위: `P0`
- 요구사항: 유효 domain-manifest.json을 가진 Domain만 Metadata로 발견한다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R003 — Golden parity

- 우선순위: `P0`
- 요구사항: cpf-member는 Generator-owned 파일 단위 parity를 유지한다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R004 — 고정 switch 금지

- 우선순위: `P0`
- 요구사항: ACC/MBR/EXS를 Tool, Build, Deploy, Gate의 조건 목록에 추가하지 않는다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R005 — Gateway Ownership

- 우선순위: `P0`
- 요구사항: Gateway Snapshot/Runtime 구현은 cpf-gateway가 소유하고 Core는 Contract/SPI만 둔다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R006 — Batch Ownership

- 우선순위: `P0`
- 요구사항: Batch Runtime 구현은 cpf-batch가 소유한다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R007 — ADM 경계

- 우선순위: `P0`
- 요구사항: ADM은 타 Owner DB를 직접 수정하지 않고 Command/Query Port를 호출한다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R008 — 내부 호출 우회 금지

- 우선순위: `P0`
- 요구사항: ACC→MBR 내부 호출을 Gateway Mandatory Hop으로 만들지 않는다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R009 — Local/Remote 동일 계약

- 우선순위: `P0`
- 요구사항: Local Provider와 Remote Provider는 동일 Typed Port를 구현한다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R010 — Public API 안정성

- 우선순위: `P0`
- 요구사항: Public API/SPI는 JavaDoc, Version, Compatibility Test를 갖는다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R011 — Internal 차단

- 우선순위: `P0`
- 요구사항: 외부 Module의 Internal Import를 ArchUnit/Build Gate로 실패시킨다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R012 — Consumer 검증

- 우선순위: `P0`
- 요구사항: Interface/SPI마다 기본 구현, 실제 Consumer, 오류·복구 Test를 둔다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R013 — 순환 차단

- 우선순위: `P0`
- 요구사항: Module과 Feature Package 순환 의존을 차단한다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R014 — 선택 Provider

- 우선순위: `P0`
- 요구사항: gRPC, Redis, Broker, SFTP 등은 Capability Adapter로 분리한다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R015 — Feature Package

- 우선순위: `P0`
- 요구사항: Backend/Frontend를 기능 Owner 기준 Package로 구성한다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R016 — Legacy 제거

- 우선순위: `P0`
- 요구사항: Consumer 이관 후 불필요 Compat Controller/Alias/Dead Code를 제거한다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R017 — 사용자 영역 보호

- 우선순위: `P0`
- 요구사항: Generator-owned 영역만 동기화하고 사용자 코드를 덮어쓰지 않는다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP02-R018 — Decision 기록

- 우선순위: `P0`
- 요구사항: Owner 변경·삭제의 이유와 대체 구현을 Decision/Handover에 남긴다.
- Owner Module: `cpf-core/cpf-tools/각 Module`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP03 — ADM 정보구조·메뉴·Feature Package

#### WP03-R001 — 홈 분리

- 우선순위: `P0`
- 요구사항: 홈은 전체 현황·나의 업무·대기 승인·최근 장애/변경 Landing으로 제공한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R002 — 5개 메뉴

- 우선순위: `P0`
- 요구사항: 온라인 운영, 배치 운영, 연계 관리, 통합 관제, 프레임워크 관리만 최상위로 둔다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R003 — 온라인 운영

- 우선순위: `P0`
- 요구사항: 거래 관리·운영·조회·분석과 Gateway 관리 흐름을 구성한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R004 — 배치 운영

- 우선순위: `P0`
- 요구사항: 작업 관리·실행 운영·작업자/Agent·복구·실행 조회를 구성한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R005 — 연계 관리

- 우선순위: `P0`
- 요구사항: 연계 정의·실시간·메시지·파일·Provider·실행 내역을 구성한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R006 — 통합 관제

- 우선순위: `P0`
- 요구사항: 통합 현황·실시간 관제·로그/추적·알림/장애·장애 분석을 구성한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R007 — 프레임워크 관리

- 우선순위: `P0`
- 요구사항: 시스템·공통·자원·사용자/권한·승인/감사·DB 관리를 구성한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R008 — 단일 Owner 메뉴

- 우선순위: `P0`
- 요구사항: 기능은 한 메뉴에서만 편집하고 다른 곳은 Filtered Deep Link만 제공한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R009 — 2단계 제한

- 우선순위: `P0`
- 요구사항: 세부 기능은 Tab/Drawer/Context Action으로 처리한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R010 — Capability 노출

- 우선순위: `P0`
- 요구사항: Gateway 등 선택 기능은 설치/Profile Capability에 따라 메뉴·API·권한을 함께 제어한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R011 — 미설치/장애 구분

- 우선순위: `P0`
- 요구사항: 미설치는 숨기고 설치됐으나 Down이면 unavailable과 복구 동선을 보인다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R012 — 역할별 가시성

- 우선순위: `P0`
- 요구사항: Role, Domain, Environment, Organization Scope를 적용한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R013 — Manifest 정합

- 우선순위: `P0`
- 요구사항: Menu, Route, API, Button Permission을 선언형 Manifest로 연결한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R014 — Feature 분리

- 우선순위: `P0`
- 요구사항: Page, Component, State, API, Model, Test를 기능 Directory에 분리한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R015 — 대형 파일 Gate

- 우선순위: `P0`
- 요구사항: 압축 1줄 Vue와 거대 Java/TS 파일을 금지한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R016 — Raw JSON 금지

- 우선순위: `P0`
- 요구사항: 운영 기본 화면은 구조화 Table/Timeline/Field View를 제공한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R017 — 조회 편의

- 우선순위: `P0`
- 요구사항: 검색, Paging, 정렬, 저장 필터, 최근 조회, 컬럼 개인화를 제공한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R018 — 오류 UX

- 우선순위: `P0`
- 요구사항: 빈 목록 위장 없이 오류 코드·원인·재시도·Trace를 표시한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R019 — 접근성

- 우선순위: `P0`
- 요구사항: Keyboard, Focus, Label, ARIA, Contrast, Responsive를 검증한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP03-R020 — 외부 CDN 금지

- 우선순위: `P0`
- 요구사항: Font/Script/Style Runtime CDN 의존을 금지한다.
- Owner Module: `cpf-admin/frontend`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP04 — Gateway 연동 서버·Registry·Binding

#### WP04-R001 — Gateway 관리 위치

- 우선순위: `P0`
- 요구사항: 온라인 운영 아래 Gateway 관리에서 타깃 서버 등록부터 연동까지 처리한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R002 — 공통 서비스 원장

- 우선순위: `P0`
- 요구사항: Service, Endpoint, Instance의 Canonical 원장은 공통 Registry가 소유한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R003 — 중복 IP 금지

- 우선순위: `P0`
- 요구사항: Gateway가 Host/IP/Port 원장을 별도로 중복 저장하지 않는다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R004 — Binding 분리

- 우선순위: `P0`
- 요구사항: Registry 정보와 외부 공개 설정을 Versioned Gateway Binding으로 연결한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R005 — Service 등록

- 우선순위: `P0`
- 요구사항: ID, 이름, Owner, 유형, 환경, 사용기간, 설명을 Typed Form으로 등록한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R006 — Endpoint 등록

- 우선순위: `P0`
- 요구사항: Code, Target Protocol, Base Address, Context, Timeout, Retry, TLS, Contract Version을 등록한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R007 — Instance 등록

- 우선순위: `P0`
- 요구사항: ID, Host, Port, Zone, Cell, Weight, Priority, Drain, Maintenance를 등록한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R008 — Server Group

- 우선순위: `P0`
- 요구사항: 2대 이상 Instance를 그룹화하고 동일 Protocol/Contract를 강제한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R009 — SLB 지원

- 우선순위: `P0`
- 요구사항: SLB/VIP가 있으면 단일 Endpoint로 등록한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R010 — SLB 없음

- 우선순위: `P0`
- 요구사항: SLB가 없으면 Client-side Load Balancing을 사용한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R011 — 등록 Wizard

- 우선순위: `P0`
- 요구사항: Gateway 화면에서 미등록 Service/Endpoint/Instance를 단계별 등록한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R012 — 기존 Registry 선택

- 우선순위: `P0`
- 요구사항: 기존 Service/Endpoint/Group을 검색해 Binding한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R013 — 연동 상태

- 우선순위: `P0`
- 요구사항: 미연동, 준비, 검증 실패, 승인 대기, 적용, 정상, 부분 적용, 차단, 해제를 구분한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R014 — Direct/Gateway 분리

- 우선순위: `P0`
- 요구사항: direct_allowed와 gateway_allowed를 별도 정책으로 관리한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R015 — Default Deny

- 우선순위: `P0`
- 요구사항: 등록 Service를 자동 외부 공개하지 않는다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R016 — 관리 API 금지

- 우선순위: `P0`
- 요구사항: ADM/BAT/Actuator/Internal Endpoint를 기본 공개 금지한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R017 — 환경 분리

- 우선순위: `P0`
- 요구사항: 개발·검증·운영 Route와 Endpoint를 혼합하지 않는다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R018 — CAS

- 우선순위: `P0`
- 요구사항: 등록·수정·삭제에 expectedVersion을 적용한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R019 — Preview

- 우선순위: `P0`
- 요구사항: 중복, 충돌, 영향 서버, 예상 Traffic, 정책 상한을 저장 전에 보여준다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R020 — 승인/감사

- 우선순위: `P0`
- 요구사항: 운영 활성화·차단·삭제에 권한·사유·승인·Before/After를 적용한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R021 — 연동 해제

- 우선순위: `P0`
- 요구사항: Stale Route, Policy, Credential Binding, Health Schedule을 함께 정리한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R022 — Typed API

- 우선순위: `P0`
- 요구사항: Registry/Binding 응답을 Map이 아닌 Typed DTO로 제공한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R023 — OpenAPI

- 우선순위: `P0`
- 요구사항: 코드 값, 예제, 오류, Version 충돌 계약을 제공한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP04-R024 — Generator 연계

- 우선순위: `P0`
- 요구사항: Generated Domain Manifest가 Service 등록 후보/Capability를 제공한다.
- Owner Module: `cpf-core API + cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP05 — Gateway Protocol·Route·보안 정책

#### WP05-R001 — Protocol 3분리

- 우선순위: `P0`
- 요구사항: Ingress, Gateway→Target, Health Protocol을 각각 저장한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R002 — Protocol Catalog

- 우선순위: `P0`
- 요구사항: HTTP, HTTPS, gRPC, WebSocket, SSE, 선택 TCP를 Code Catalog로 제공한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R003 — 동적 Form

- 우선순위: `P0`
- 요구사항: Protocol 선택에 맞는 Host/TLS/Service/Method/Streaming/Encoding 필드를 보여준다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R004 — Group 호환성

- 우선순위: `P0`
- 요구사항: 한 Group에는 동일 Protocol과 호환 Contract만 넣는다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R005 — HTTP/HTTPS

- 우선순위: `P0`
- 요구사항: Host, Path, Method, Query, Header, Body Size, Keepalive를 지원한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R006 — gRPC

- 우선순위: `P0`
- 요구사항: Unary/Streaming, Service/Method, Deadline, Metadata, TLS, Health를 Adapter로 지원한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R007 — WebSocket/SSE

- 우선순위: `P0`
- 요구사항: Upgrade, Idle Timeout, Drain, Backpressure를 제공한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R008 — TCP 격리

- 우선순위: `P0`
- 요구사항: Raw TCP는 명시적 L4/Protocol Adapter로 분리한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R009 — 메시징 경계

- 우선순위: `P0`
- 요구사항: Kafka/Rabbit/JMS Consumer는 Messaging Runtime이 소유한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R010 — 파일 경계

- 우선순위: `P0`
- 요구사항: SFTP/File Runtime은 연계·Batch가 소유한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R011 — Protocol 변환

- 우선순위: `P0`
- 요구사항: REST→gRPC 등은 Versioned Transformation Adapter 없이는 금지한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R012 — Route Key

- 우선순위: `P0`
- 요구사항: Host, Base Path, Pattern, Method, API Version을 Route Key로 관리한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R013 — Path Rewrite

- 우선순위: `P0`
- 요구사항: Context/Path Rewrite를 Preview/Test 가능하게 한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R014 — Header 정책

- 우선순위: `P0`
- 요구사항: 전달·제거·추가·재작성 Allowlist와 Trust Header 방어를 적용한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R015 — 인증 정책

- 우선순위: `P0`
- 요구사항: JWT, mTLS, API Key, 기관 서명을 Route에 연결한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R016 — 권한 정책

- 우선순위: `P0`
- 요구사항: Permission, Scope, Channel, Organization 규칙을 Route에 연결한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R017 — Rate Limit

- 우선순위: `P0`
- 요구사항: Route, Channel, Principal, IP 단위 Limit/Burst를 제공한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R018 — 요청 제한

- 우선순위: `P0`
- 요구사항: Header, Query, Body, Upload 최대 크기를 적용한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R019 — Timeout Budget

- 우선순위: `P0`
- 요구사항: Connect, Response, Overall Timeout을 분리한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R020 — Retry 안전성

- 우선순위: `P0`
- 요구사항: 멱등/결과 불명 계약이 있는 Route만 Retry한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R021 — Circuit/Failover

- 우선순위: `P0`
- 요구사항: Instance/Group Circuit와 Failover를 적용한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R022 — 점검/차단

- 우선순위: `P0`
- 요구사항: Route별 Maintenance, Manual Block, 기간 차단을 제공한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R023 — TLS/mTLS

- 우선순위: `P0`
- 요구사항: Trust, Client Cert, 만료, Rotation, SNI를 지원한다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP05-R024 — 충돌 Gate

- 우선순위: `P0`
- 요구사항: Host/Path/Method/Version 충돌과 Shadowing을 적용 전에 실패시킨다.
- Owner Module: `cpf-gateway`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP06 — Server Group·Load Balance·Health

#### WP06-R001 — Round Robin

- 우선순위: `P0`
- 요구사항: 정상 Instance를 순환 선택한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R002 — Weighted Round Robin

- 우선순위: `P0`
- 요구사항: 서버 성능과 Canary를 위한 Weight 분산을 제공한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R003 — Rendezvous Hash

- 우선순위: `P0`
- 요구사항: 서버 증감 시 Key 재배치를 최소화하는 Hash를 제공한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R004 — Priority Failover

- 우선순위: `P0`
- 요구사항: Primary Group 장애 시 Secondary/DR로 전환한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R005 — Least Load

- 우선순위: `P0`
- 요구사항: 선택 Capability로 Active Request/EWMA 기반 정책을 제공한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R006 — 정책 Select

- 우선순위: `P0`
- 요구사항: Load Balance 정책은 Code Catalog Select로 제공한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R007 — Hash Key

- 우선순위: `P0`
- 요구사항: Header/Path/Schema Allowlist에서 Key를 선택하고 민감 원문을 기록하지 않는다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R008 — Health URL

- 우선순위: `P0`
- 요구사항: URL, Method, 정상 코드, Body 조건, Timeout을 설정한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R009 — TCP Check

- 우선순위: `P0`
- 요구사항: Application Health가 없으면 TCP Connect Check를 제공한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R010 — Ping 보조

- 우선순위: `P0`
- 요구사항: ICMP Ping은 보조 신호이며 단독 Application UP 판정에 쓰지 않는다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R011 — gRPC Health

- 우선순위: `P0`
- 요구사항: 표준 gRPC Health를 지원한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R012 — Protocol Probe

- 우선순위: `P0`
- 요구사항: SOAP/TCP/SFTP/Broker는 안전한 전용 Probe Adapter를 쓴다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R013 — CPF Health

- 우선순위: `P0`
- 요구사항: Liveness, Readiness, Version, Instance, DB, Pool, Drain을 제공한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R014 — Monitor Only

- 우선순위: `P0`
- 요구사항: 업무 호출 없이 상대 기관 상태만 점검하는 Target을 등록한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R015 — 상태 모델

- 우선순위: `P0`
- 요구사항: UP, DEGRADED, RECOVERING, DOWN, DRAINING, MAINTENANCE, DISABLED, UNKNOWN, STALE을 구분한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R016 — 임계/Hysteresis

- 우선순위: `P0`
- 요구사항: 연속 실패·성공, Cooldown, 복구 임계를 설정한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R017 — Freshness

- 우선순위: `P0`
- 요구사항: 마지막 Probe와 수신 시각을 저장하고 STALE을 판정한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R018 — 실호출 결합

- 우선순위: `P0`
- 요구사항: 정기 Health와 실제 호출·Circuit 결과를 Routing에 반영한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R019 — 수동 상태 우선

- 우선순위: `P0`
- 요구사항: Disable/Drain/Maintenance는 Health UP이어도 제외한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R020 — 중복 Probe 방지

- 우선순위: `P0`
- 요구사항: 다중 Worker에서 Lease/Fencing으로 단일 Probe를 보장한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R021 — Jitter

- 우선순위: `P0`
- 요구사항: 동시 Probe 폭주를 막는다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R022 — 기관 보호

- 우선순위: `P0`
- 요구사항: 외부 기관별 최소 주기, Rate Limit, 점검 시간을 둔다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R023 — 복구 Traffic

- 우선순위: `P0`
- 요구사항: 복구 후 연속 성공과 Canary를 거쳐 UP 편입한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R024 — 상태 이력

- 우선순위: `P0`
- 요구사항: 상태 전이, 원인, 자동/수동 조치를 Timeline/Audit에 저장한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP06-R025 — Canary 편입

- 우선순위: `P0`
- 요구사항: 신규 Instance를 낮은 Weight로 편입하고 오류율 기준으로 확대/중단한다.
- Owner Module: `cpf-core service-call + cpf-gateway + Health Worker`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP07 — 연결시험·Dashboard·실시간 상태

#### WP07-R001 — 직접 연결시험

- 우선순위: `P0`
- 요구사항: Gateway Runtime에서 DNS, Ping, TCP, TLS, Protocol, Health를 단계별 시험한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R002 — Gateway 경유 E2E

- 우선순위: `P0`
- 요구사항: 실제 Gateway URL과 Route 정책으로 Target까지 종단 간 시험한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R003 — ADM 직접 호출 금지

- 우선순위: `P0`
- 요구사항: ADM이 Target을 직접 호출해 Gateway 성공으로 위장하지 않는다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R004 — Gateway 선택

- 우선순위: `P0`
- 요구사항: 특정/전체 Gateway Instance에서 시험한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R005 — 전체 Instance 시험

- 우선순위: `P0`
- 요구사항: Group의 모든 Instance를 개별 시험하고 부분 실패를 표시한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R006 — 정책 시험

- 우선순위: `P0`
- 요구사항: Round Robin, Weight, Hash, Failover 선택 분포를 반복 호출로 검증한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R007 — 안전 Operation

- 우선순위: `P0`
- 요구사항: Health 또는 무부작용 Test API를 기본 사용한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R008 — 업무 Test 통제

- 우선순위: `P0`
- 요구사항: 변경 API는 테스트 데이터, 멱등키, 승인, 보상 확인을 요구한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R009 — 단계별 결과

- 우선순위: `P0`
- 요구사항: DNS→TCP→TLS→Auth→Route→Target→Response 시간과 실패 단계를 표시한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R010 — Trace 연결

- 우선순위: `P0`
- 요구사항: Test 결과에 transactionId/traceId/operationId를 부여한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R011 — 설정 변경 만료

- 우선순위: `P0`
- 요구사항: 설정 변경 후 과거 성공을 STALE 처리한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R012 — 적용 후 재검증

- 우선순위: `P0`
- 요구사항: Route/Policy 적용 후 Gateway 경유 Test를 실행한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R013 — Dashboard

- 우선순위: `P0`
- 요구사항: Service, Group, Instance, Route, 상태, Traffic, 오류, 지연을 요약한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R014 — KPI

- 우선순위: `P0`
- 요구사항: 요청량, 성공률, 오류율, P50/P95/P99, Circuit, 미검증, 인증서 만료를 표시한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R015 — Drill-down

- 우선순위: `P0`
- 요구사항: 경고에서 서버, Route, 로그, Test, 조치로 이동한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R016 — 실시간 Push

- 우선순위: `P0`
- 요구사항: 상태 변경을 SSE/WebSocket으로 증분 전송한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R017 — Polling Fallback

- 우선순위: `P0`
- 요구사항: Push 실패 시 제한 Polling과 연결 상태를 표시한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R018 — 재연결

- 우선순위: `P0`
- 요구사항: Last Event ID와 중복 제거를 지원한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R019 — 신선도 UI

- 우선순위: `P0`
- 요구사항: Probe 시각, 서버 수신, 화면 수신을 구분한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R020 — 대량 성능

- 우선순위: `P0`
- 요구사항: 수천 Instance에서 Virtualization, Server Paging, 증분 집계를 사용한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R021 — Dashboard 권한

- 우선순위: `P0`
- 요구사항: 환경·Domain·Route Scope를 적용한다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP07-R022 — 장애 격리

- 우선순위: `P0`
- 요구사항: Push 실패가 원 거래/Health Probe를 오염시키지 않는다.
- Owner Module: `cpf-gateway + cpf-admin`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP08 — Gateway 거래로그·로그정책·조회 UI

#### WP08-R001 — Gateway 거래 원장

- 우선순위: `P0`
- 요구사항: 성공·실패·차단·Timeout 거래를 DB 정형 원장에 기록한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R002 — Attempt 원장

- 우선순위: `P0`
- 요구사항: Retry/Failover의 Target 선택과 각 결과를 별도 Attempt로 저장한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R003 — IN 구간

- 우선순위: `P0`
- 요구사항: Channel, Source, Client IP/Port, Host, Method, Path, Protocol, TLS를 저장한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R004 — Gateway 구간

- 우선순위: `P0`
- 요구사항: Gateway Instance, Route ID/Version, Auth, Rate Limit, Rewrite, 처리시간을 저장한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R005 — OUT 구간

- 우선순위: `P0`
- 요구사항: Target Service, Endpoint, Group, Instance, Host/Port, Protocol, Connect/Response 시간을 저장한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R006 — RESULT 구간

- 우선순위: `P0`
- 요구사항: 최종 상태, HTTP/업무 코드, 실패 단계, Unknown, Retry/Failover, 총 시간을 저장한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R007 — 동기 DB 결합 금지

- 우선순위: `P0`
- 요구사항: 중앙 DB Insert를 원 거래 Transaction에 직접 결합하지 않는다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R008 — Durable Spool

- 우선순위: `P0`
- 요구사항: BALANCED 모드에서 Local WAL/Spool 후 DB Batch 적재한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R009 — Strict 모드

- 우선순위: `P0`
- 요구사항: 법적 필수 Route는 Durable 기록 확인 후 응답할 수 있다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R010 — Spool 복구

- 우선순위: `P0`
- 요구사항: DB 복구 후 순서·중복·Checksum을 보존해 재적재한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R011 — 적재 관제

- 우선순위: `P0`
- 요구사항: Queue, Spool 용량, 지연, 마지막 성공, 실패, 재전송을 ADM에서 본다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R012 — 기술 로그 분리

- 우선순위: `P0`
- 요구사항: DEBUG/TRACE/Stack/Library 로그는 구조화 파일/중앙 Provider로 분리한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R013 — Metric 분리

- 우선순위: `P0`
- 요구사항: TPS, 오류율, Percentile은 Metric Store를 사용한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R014 — 정책 범위

- 우선순위: `P0`
- 요구사항: Platform, Environment, Gateway, Channel, Service, Route, API, 기간 Override를 지원한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R015 — 필수 Metadata

- 우선순위: `P0`
- 요구사항: 거래 ID, Route, Target, Result, Time, Attempt는 끌 수 없다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R016 — Query 정책

- 우선순위: `P0`
- 요구사항: NONE, ALLOWLIST, MASKED, HASHED를 제공한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R017 — Header 정책

- 우선순위: `P0`
- 요구사항: Authorization/Cookie/Token은 금지하고 Allowlist만 저장한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R018 — Body 정책

- 우선순위: `P0`
- 요구사항: NONE, METADATA_ONLY, ALLOWLIST_FIELDS, MASKED_BODY, ENCRYPTED_BODY를 제공한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R019 — Raw Body 제한

- 우선순위: `P0`
- 요구사항: FULL_RAW_BODY는 기본 금지하고 Property 상한, 승인, TTL을 요구한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R020 — Schema 필드

- 우선순위: `P0`
- 요구사항: JSONPath, XPath, Fixed Layout Field 기반 Allowlist/Masking을 지원한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R021 — Binary

- 우선순위: `P0`
- 요구사항: 원문 대신 MIME, Size, Checksum, Artifact Reference를 저장한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R022 — 크기 제한

- 우선순위: `P0`
- 요구사항: Body/Stack/Output 최대 크기와 Truncate 표시를 적용한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R023 — 검색 Hash

- 우선순위: `P0`
- 요구사항: 고객번호·계좌번호는 보호 Hash로 검색한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R024 — Property 상한

- 우선순위: `P0`
- 요구사항: Sink, Spool, body-capture-allowed, Fail Mode는 Property가 상한을 정한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R025 — ADM 정책

- 우선순위: `P0`
- 요구사항: Route별 상세 기록, Sampling, TTL, Masking, 보존을 운영 중 관리한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R026 — 정책 우선순위

- 우선순위: `P0`
- 요구사항: Safe Default→Property→Platform→Environment→Gateway→Route→Temporary를 적용한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R027 — 다중 Instance 적용

- 우선순위: `P0`
- 요구사항: 정책 Version을 모든 Gateway에 배포하고 ACK/실패/Drift를 표시한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R028 — 정책 Rollback

- 우선순위: `P0`
- 요구사항: 부분 적용 시 이전 Version과 Reconcile를 제공한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R029 — 조회 목록

- 우선순위: `P0`
- 요구사항: 기간, 거래/Trace, Channel, Route, Target, Instance, 상태, 오류, 지연, Attempt로 검색한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R030 — 상세 Timeline

- 우선순위: `P0`
- 요구사항: IN→Gateway→Attempt→OUT→Result를 구조화 Timeline/Tab으로 표시한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R031 — Raw JSON 제한

- 우선순위: `P0`
- 요구사항: 기본 UI에서 Raw Map/JSON을 금지하고 고급 권한 보기로 제한한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R032 — 실시간 Tail

- 우선순위: `P0`
- 요구사항: 필터, 일시정지, Backpressure, 최대 건수, 재연결을 제공한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R033 — 조회/Export 감사

- 우선순위: `P0`
- 요구사항: 민감 조회, 복호화, Download/Export에 권한·사유·감사를 적용한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R034 — 보존/Archive

- 우선순위: `P0`
- 요구사항: Partition, Online 보존, Archive, 파기, Legal Hold를 제공한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R035 — 로그 장애 격리

- 우선순위: `P0`
- 요구사항: 일반 로그 실패가 원 거래를 불필요하게 실패시키지 않되 유실 위험을 경고한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R036 — Audit 불변성

- 우선순위: `P0`
- 요구사항: 정책 변경·위험 조치 Audit은 Append-only와 위변조 탐지를 제공한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R037 — 컬럼 개인화

- 우선순위: `P0`
- 요구사항: 거래 목록의 컬럼/저장 View를 사용자별로 제공한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP08-R038 — Trace Deep Link

- 우선순위: `P0`
- 요구사항: Gateway 거래에서 내부 호출, Batch, Incident Trace로 연결한다.
- Owner Module: `cpf-core logging + cpf-gateway + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP09 — 개발자 Service Call·Typed Client·Setup

#### WP09-R001 — Typed Client

- 우선순위: `P0`
- 요구사항: 업무 개발자는 Raw CpfHttpClient/URL Path 대신 서비스별 Typed Client를 주입한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R002 — Contract Registry

- 우선순위: `P0`
- 요구사항: OpenAPI, Proto, WSDL/XSD, AsyncAPI, 전문/File Layout을 Version 관리한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R003 — Client Generator

- 우선순위: `P0`
- 요구사항: Contract에서 DTO, Error, Header, Client, Simulator, Contract Test를 생성한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R004 — Artifact 배포

- 우선순위: `P0`
- 요구사항: Client를 Maven Artifact 또는 Review 가능한 Git Patch로 제공한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R005 — ADM Source 수정 금지

- 우선순위: `P0`
- 요구사항: ADM 등록이 운영 Source를 자동 수정/Commit하지 않는다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R006 — Local/Remote/Auto

- 우선순위: `P0`
- 요구사항: 동일 Port를 Local, Remote, Auto Provider가 구현한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R007 — Auto 선택

- 우선순위: `P0`
- 요구사항: Local Bean 우선, 없으면 Registry Remote, 모두 없으면 Fail-fast한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R008 — Service ID 정본

- 우선순위: `P0`
- 요구사항: 업무 Source에는 IP/Port 대신 Typed Service ID/Client만 둔다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R009 — Runtime Address

- 우선순위: `P0`
- 요구사항: 환경, Health, Zone, Weight, Priority로 Instance를 해석한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R010 — 외부 Client

- 우선순위: `P0`
- 요구사항: 외부 기관도 Protocol Adapter 뒤 Typed Client로 호출한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R011 — Protocol 교체

- 우선순위: `P0`
- 요구사항: Public Client를 유지하고 Adapter만 교체한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R012 — 오류 계약

- 우선순위: `P0`
- 요구사항: Timeout, Circuit, Auth, Remote Error, Unknown을 Typed Result/Exception으로 제공한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R013 — Header 전파

- 우선순위: `P0`
- 요구사항: transactionId, traceId, channel, actor를 표준 전파한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R014 — Idempotency

- 우선순위: `P0`
- 요구사항: 변경 호출에 Key, Request Hash, Sequence를 포함한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R015 — Retry Metadata

- 우선순위: `P0`
- 요구사항: API가 멱등 여부와 Retry Policy를 Metadata로 가진다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R016 — IDE 발견성

- 우선순위: `P0`
- 요구사항: Client/DTO/Error에 한글 JavaDoc과 예제를 제공한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R017 — Generator 연계

- 우선순위: `P0`
- 요구사항: 새 Domain에 Public Contract, Local/Remote Adapter, 등록 Manifest를 생성한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R018 — Consumer Inventory

- 우선순위: `P0`
- 요구사항: ADM에서 Client Version과 Consumer를 조회한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R019 — Compatibility

- 우선순위: `P0`
- 요구사항: Breaking Change와 영향 Consumer를 계산한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R020 — Deprecated/Sunset

- 우선순위: `P0`
- 요구사항: 폐기 일정, 대체 Version, 미이관 Consumer를 관리한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R021 — Setup Wizard

- 우선순위: `P0`
- 요구사항: Domain/외부 서버 등록→Contract→Client→Test를 안내한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R022 — Fail-fast

- 우선순위: `P0`
- 요구사항: Endpoint, Secret, Provider 누락을 기동 전에 명확히 알린다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP09-R023 — 샘플 생성

- 우선순위: `P0`
- 요구사항: 정상·오류·복구 사용 예제를 자동 생성한다.
- Owner Module: `cpf-core API + generator + domain`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP10 — Batch Job 등록·Executor·Schedule

#### WP10-R001 — Job Wizard

- 우선순위: `P0`
- 요구사항: ADM에서 기본정보, Executor, Trigger, Parameter, Resource, Recovery, Alert, Approval을 등록한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R002 — Executor Catalog

- 우선순위: `P0`
- 요구사항: SPRING_BATCH, APPROVED_SHELL, FILE_WATCH, FILE_PROCESS, FILE_TRANSFER를 Select로 제공한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R003 — Java Job

- 우선순위: `P0`
- 요구사항: Job Pack의 Job, Version, Checksum, Signature를 선택한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R004 — Shell Job

- 우선순위: `P0`
- 요구사항: 승인 Script Catalog와 Agent Pool을 선택한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R005 — File Watch Job

- 우선순위: `P0`
- 요구사항: Provider, Alias, Pattern, Stability, Marker, Trigger를 설정한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R006 — File Transfer Job

- 우선순위: `P0`
- 요구사항: Source/Target Provider, Path, Overwrite, Checksum, 암호화를 설정한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R007 — Service Call Job

- 우선순위: `P0`
- 요구사항: Typed Service Client Operation을 실행 Step으로 등록한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R008 — Message Trigger

- 우선순위: `P0`
- 요구사항: Kafka/Rabbit/JMS Trigger는 Messaging Runtime Contract를 사용한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R009 — Schedule

- 우선순위: `P0`
- 요구사항: Cron, Calendar, 업무일, Timezone, Misfire를 설정한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R010 — 즉시 실행

- 우선순위: `P0`
- 요구사항: 권한과 Parameter Override를 가진 수동 실행을 제공한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R011 — Dependency

- 우선순위: `P0`
- 요구사항: 선행 Job, 결과 조건, Timeout, Cycle 검증을 제공한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R012 — Versioning

- 우선순위: `P0`
- 요구사항: Job Definition은 Immutable Version과 시행일을 가진다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R013 — Draft/Publish

- 우선순위: `P0`
- 요구사항: Draft, Validated, Approval, Published, Retired 상태를 관리한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R014 — 등록 Validation

- 우선순위: `P0`
- 요구사항: Executor 필수 값, Capability, 경로, Secret, Contract를 검증한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R015 — 연결 Test

- 우선순위: `P0`
- 요구사항: Script/서명, File 접근, Service Health를 등록 때 시험한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R016 — Preview

- 우선순위: `P0`
- 요구사항: 최종 Parameter, Agent, Schedule, 영향, 권한을 보여준다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R017 — Agent Capability

- 우선순위: `P0`
- 요구사항: OS, Runtime, Zone, Alias, Tool Version을 매칭한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R018 — Concurrency

- 우선순위: `P0`
- 요구사항: Job, Group, Business Key별 동시 실행을 제한한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R019 — Restartability

- 우선순위: `P0`
- 요구사항: Restart 가능 여부와 Checkpoint/보상 요구를 정의한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R020 — Retry/Skip

- 우선순위: `P0`
- 요구사항: Backoff, Skip, Threshold를 Step/Job 단위로 설정한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R021 — Unknown Result

- 우선순위: `P0`
- 요구사항: 통신 단절을 UNKNOWN_RESULT로 분류하고 확인 절차를 제공한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R022 — Alert

- 우선순위: `P0`
- 요구사항: 지연, 실패, 미실행, SLA, Parameter 오류 알림을 설정한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R023 — UI 상세

- 우선순위: `P0`
- 요구사항: 목록, 상세, Version 비교, 실행 이력, 의존 Graph를 제공한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R024 — OpenAPI/Guide

- 우선순위: `P0`
- 요구사항: 등록·실행 API와 운영 Guide를 제공한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP10-R025 — Job Template

- 우선순위: `P0`
- 요구사항: 자주 쓰는 Executor 조합을 표준 Template으로 제공한다.
- Owner Module: `cpf-batch + cpf-admin`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP11 — Batch File Watch·Process·Transfer

#### WP11-R001 — Provider SPI

- 우선순위: `P0`
- 요구사항: Local, NFS/Shared, SFTP/FTPS, SMB, Object Storage를 Adapter로 분리한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R002 — Path Alias

- 우선순위: `P0`
- 요구사항: 실제 경로 대신 승인 Alias를 선택한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R003 — Pattern

- 우선순위: `P0`
- 요구사항: Glob, Regex, Extension, 업무일자 Token을 설정한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R004 — Stable Window

- 우선순위: `P0`
- 요구사항: 크기와 수정시각이 안정된 뒤 처리한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R005 — Marker

- 우선순위: `P0`
- 요구사항: DONE/CTL 등 완료 Marker를 조건으로 사용한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R006 — Checksum

- 우선순위: `P0`
- 요구사항: Sidecar/등록 Checksum을 검증한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R007 — Header/Trailer

- 우선순위: `P0`
- 요구사항: Layout, Record Count를 검증한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R008 — 암호화/압축

- 우선순위: `P0`
- 요구사항: PGP/Zip 정책과 Zip Bomb 방어를 적용한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R009 — 중복 파일

- 우선순위: `P0`
- 요구사항: Filename, Size, Checksum, Business Key 중복을 탐지한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R010 — 재전송 정책

- 우선순위: `P0`
- 요구사항: Ignore, Replace, New Version, Manual Review를 지원한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R011 — 지연/누락

- 우선순위: `P0`
- 요구사항: 예상 도착과 SLA 기준 경고를 제공한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R012 — 순서 역전

- 우선순위: `P0`
- 요구사항: Sequence/Business Date 역전을 보류/처리한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R013 — Claim

- 우선순위: `P0`
- 요구사항: 원자 이동 또는 Provider Lease로 소유권을 확보한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R014 — Lease/Fencing

- 우선순위: `P0`
- 요구사항: 다중 Worker 중복 처리를 막는다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R015 — Restart Scan

- 우선순위: `P0`
- 요구사항: 재기동 후 Inbox/Processing/Unknown을 재탐색한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R016 — Unknown Result

- 우선순위: `P0`
- 요구사항: 이동/처리/완료 사이 장애를 Reconcile한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R017 — Archive

- 우선순위: `P0`
- 요구사항: 완료·오류·보류를 정책별 위치로 이동한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R018 — Retention

- 우선순위: `P0`
- 요구사항: 원본, 처리본, 결과, Evidence 보존/파기를 관리한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R019 — Streaming

- 우선순위: `P0`
- 요구사항: 전체 파일 메모리 적재를 금지한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R020 — Backpressure

- 우선순위: `P0`
- 요구사항: 동시 파일 수, 총 크기, 처리량을 제한한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R021 — Security

- 우선순위: `P0`
- 요구사항: Traversal, Symlink, MIME, 권한, Malware Hook을 검증한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R022 — Monitor UI

- 우선순위: `P0`
- 요구사항: 발견→안정화→Claim→처리→Archive Timeline을 표시한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R023 — Manual Recovery

- 우선순위: `P0`
- 요구사항: 재탐색, Claim 해제, 재처리, 보류 해제를 통제한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP11-R024 — Failure Test

- 우선순위: `P0`
- 요구사항: Worker Kill, Network Partition, Duplicate Event, Partial Upload를 검증한다.
- Owner Module: `cpf-batch worker/agent`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP12 — Approved Shell·Agent·Process 보안

#### WP12-R001 — Script Catalog

- 우선순위: `P0`
- 요구사항: Script ID, Version, Artifact, Hash, Signature, Owner, 사용기간을 등록한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R002 — 임의 Command 금지

- 우선순위: `P0`
- 요구사항: 운영자 입력 Command String, shell expansion, pipe, redirect를 금지한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R003 — Interpreter

- 우선순위: `P0`
- 요구사항: sh/bash/pwsh/python 등 승인 Interpreter와 Version을 선택한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R004 — OS/Agent 조건

- 우선순위: `P0`
- 요구사항: 지원 OS, Architecture, Agent Capability를 검증한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R005 — 실행 계정

- 우선순위: `P0`
- 요구사항: 최소 권한 OS 계정/Container Identity로 실행한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R006 — Working Directory

- 우선순위: `P0`
- 요구사항: 승인 Path Alias 기반 Working Directory를 사용한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R007 — 인수 분리

- 우선순위: `P0`
- 요구사항: 고정 인수와 Typed Parameter를 분리한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R008 — Secret 전달

- 우선순위: `P0`
- 요구사항: Secret은 Command Line이 아닌 권한 파일/stdin/Provider Token으로 전달한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R009 — 환경변수 Allowlist

- 우선순위: `P0`
- 요구사항: 환경변수 이름과 Source를 제한한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R010 — Timeout

- 우선순위: `P0`
- 요구사항: Graceful Signal→유예→Process Tree 강제 종료를 적용한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R011 — Child Process

- 우선순위: `P0`
- 요구사항: 하위/Detached Process 잔류를 탐지·종료한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R012 — Exit Code

- 우선순위: `P0`
- 요구사항: 성공, 재시도, 비즈니스 실패, Unknown을 정의한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R013 — stdout/stderr

- 우선순위: `P0`
- 요구사항: Stream을 분리하고 크기/속도 제한과 마스킹을 적용한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R014 — 출력 폭주

- 우선순위: `P0`
- 요구사항: Ring Buffer/Spool과 최대 용량을 적용한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R015 — 결과 Artifact

- 우선순위: `P0`
- 요구사항: Path Alias, Checksum, 크기, 보존을 기록한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R016 — Idempotency

- 우선순위: `P0`
- 요구사항: 재실행 Side Effect 중복 방지 Key와 Precheck를 제공한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R017 — Unknown Result

- 우선순위: `P0`
- 요구사항: Agent 단절 후 Process/결과를 Reconcile한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R018 — Concurrency

- 우선순위: `P0`
- 요구사항: Script, Host, Account별 동시 실행을 제한한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R019 — 승인

- 우선순위: `P0`
- 요구사항: 등록·변경·즉시 실행·강제 종료에 승인 정책을 적용한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R020 — 감사

- 우선순위: `P0`
- 요구사항: Script Hash, Parameter Snapshot, 실행자, Agent, Exit, Artifact를 남긴다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R021 — Simulator

- 우선순위: `P0`
- 요구사항: 운영 Script 없이 Contract를 검증하는 Test Adapter를 제공한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R022 — Security Test

- 우선순위: `P0`
- 요구사항: Command Injection, Secret Leak, Path Escape, Child Leak을 검증한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP12-R023 — Container Runner

- 우선순위: `P0`
- 요구사항: 선택적으로 승인 Image/Entrypoint 격리 실행을 지원한다.
- Owner Module: `cpf-batch agent/worker`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP13 — 공통 Parameter Schema·동적 UI

#### WP13-R001 — 공통 Schema

- 우선순위: `P0`
- 요구사항: 온라인, Batch, Gateway Test, 외부 연계가 재사용하는 Versioned Parameter Definition을 제공한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R002 — 자료형

- 우선순위: `P0`
- 요구사항: String, Integer, Decimal, Money, Boolean, Date, DateTime, Enum, List, Object를 지원한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R003 — 운영 참조형

- 우선순위: `P0`
- 요구사항: File/Directory Alias, Service, Endpoint, Group, Calendar, Secret Reference를 지원한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R004 — 표시 Metadata

- 우선순위: `P0`
- 요구사항: 표시명, 설명, 예제, Help, Group, 순서, 조건부 표시를 정의한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R005 — Validation

- 우선순위: `P0`
- 요구사항: 필수, Min/Max, Length, Regex, Scale, Allowed Value, Cross-field Rule을 지원한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R006 — 민감도

- 우선순위: `P0`
- 요구사항: Sensitive, PII, Secret, Search Hash, Masking Policy를 정의한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R007 — 식별/멱등

- 우선순위: `P0`
- 요구사항: Identifying, Idempotency Key, Request Hash 포함 여부를 정의한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R008 — 기본값

- 우선순위: `P0`
- 요구사항: Safe Default와 Dynamic Default Provider를 구분한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R009 — 우선순위

- 우선순위: `P0`
- 요구사항: Default→Environment→Group→Job/Route→Schedule→Execution Override를 적용한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R010 — Immutable Snapshot

- 우선순위: `P0`
- 요구사항: 최종 값과 Schema Version을 Hash/암호화 저장한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R011 — UI 자동 생성

- 우선순위: `P0`
- 요구사항: Schema에서 Select, Date, Secret, File Picker를 생성한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R012 — Code Select

- 우선순위: `P0`
- 요구사항: Protocol, Executor, 상태, Policy를 Code Catalog로 제공한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R013 — 종속 Select

- 우선순위: `P0`
- 요구사항: Service→Endpoint→Group 연관 값을 종속 Select로 제공한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R014 — 검색 Select

- 우선순위: `P0`
- 요구사항: 대량 Code/Service는 검색·Paging Select를 쓴다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R015 — 비활성 사유

- 우선순위: `P0`
- 요구사항: Capability 미설치 옵션은 사유와 함께 Disabled로 표시한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R016 — Raw JSON 제한

- 우선순위: `P0`
- 요구사항: 일반 운영 입력에서 Raw JSON을 금지한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R017 — 최종값 Preview

- 우선순위: `P0`
- 요구사항: Source별 값, 최종값, Masking 상태를 실행 전에 보여준다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R018 — Override 권한

- 우선순위: `P0`
- 요구사항: Parameter별 실행 Override 권한을 정의한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R019 — 오류 위치

- 우선순위: `P0`
- 요구사항: Validation 오류를 필드·규칙 단위로 표시한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R020 — 동일 정본

- 우선순위: `P0`
- 요구사항: Backend, OpenAPI, Frontend, Generator가 같은 Schema를 사용한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP13-R021 — 국제화

- 우선순위: `P0`
- 요구사항: 표시명, 설명, 오류 메시지를 Locale 확장 가능하게 한다.
- Owner Module: `cpf-core API + cpf-admin component`
- 시작 상태: `미구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP14 — 권한·승인·감사·Masking

#### WP14-R001 — 인증 Actor

- 우선순위: `P0`
- 요구사항: request body requestedBy 대신 인증 Context Operator를 사용한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R002 — 권한 분리

- 우선순위: `P0`
- 요구사항: READ, CREATE, UPDATE, DELETE, TEST, APPLY, DRAIN, EXPORT, RAW_VIEW를 분리한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R003 — 위험 승인

- 우선순위: `P0`
- 요구사항: Route 공개, Body Capture, Masking 해제, Shell 실행, 강제 Failover를 승인 대상으로 둔다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R004 — 자기승인 금지

- 우선순위: `P0`
- 요구사항: 기본적으로 요청자와 승인자를 분리한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R005 — 사유 필수

- 우선순위: `P0`
- 요구사항: 위험 조치와 민감 조회에 Reason Code와 상세 사유를 요구한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R006 — 불변 Snapshot

- 우선순위: `P0`
- 요구사항: 승인 Command와 Parameter/Policy Hash를 고정한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R007 — Break-glass

- 우선순위: `P0`
- 요구사항: 별도 권한, TTL, 긴급 사유, 사후 Review, 자동 원복을 적용한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R008 — Secret Reference

- 우선순위: `P0`
- 요구사항: Password, Token, Private Key를 원문 저장하지 않는다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R009 — Server-side Masking

- 우선순위: `P0`
- 요구사항: 수집/저장/조회 API에서 마스킹한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R010 — Raw PII

- 우선순위: `P0`
- 요구사항: 별도 권한, POST Reason, no-store, Audit이 있을 때만 허용한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R011 — Header Trust

- 우선순위: `P0`
- 요구사항: 외부가 내부 신뢰 Header를 위조하지 못하게 제거·재작성한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R012 — CSRF/CORS

- 우선순위: `P0`
- 요구사항: 관리 API의 CSRF, CORS, SameSite를 검증한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R013 — Audit 실패

- 우선순위: `P0`
- 요구사항: 필수 Audit 실패를 성공으로 숨기지 않는다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R014 — 운영 장애 격리

- 우선순위: `P0`
- 요구사항: 관제/로그 UI 실패가 원 업무를 오염시키지 않는다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R015 — Rate Limit

- 우선순위: `P0`
- 요구사항: 연결 Test, Tail, Health Probe에도 운영 Limit을 적용한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R016 — Export 보안

- 우선순위: `P0`
- 요구사항: CSV Injection, 대량 제한, Watermark, 암호화, 만료 Download를 적용한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R017 — 권한 Matrix

- 우선순위: `P0`
- 요구사항: Menu, Button, API, Owner Command Permission parity를 검증한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP14-R018 — 보안 Evidence

- 우선순위: `P0`
- 요구사항: Secret/PII Scan과 권한 우회 Failure Test를 수행한다.
- Owner Module: `cpf-admin/cpf-gateway/cpf-core security`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP15 — DB·Migration·Vendor·Archive

#### WP15-R001 — Canonical Schema

- 우선순위: `P0`
- 요구사항: Gateway Binding, Group, Health, Transaction/Attempt, Log Policy를 단일 Metadata에서 생성한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R002 — 3 Vendor parity

- 우선순위: `P0`
- 요구사항: MariaDB/PostgreSQL/Oracle DDL, Index, FK, Seed, Verify, Rollback을 함께 제공한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R003 — Migration

- 우선순위: `P0`
- 요구사항: 기존 설치를 보존하는 Forward와 Rollback을 작성한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R004 — Historical 불변

- 우선순위: `P0`
- 요구사항: 기존 배포 Migration Checksum을 변경하지 않는다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R005 — Group FK

- 우선순위: `P0`
- 요구사항: Service→Endpoint→Group→Instance 관계와 삭제 정책을 정의한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R006 — Binding Version

- 우선순위: `P0`
- 요구사항: Route/Policy/Target Binding Version, 시행일, 상태를 저장한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R007 — Health 저장

- 우선순위: `P0`
- 요구사항: Current와 History를 분리 저장한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R008 — 거래 Partition

- 우선순위: `P0`
- 요구사항: 업무일자 Partition과 Transaction/Trace/Route/Target Index를 제공한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R009 — Body Storage

- 우선순위: `P0`
- 요구사항: 암호화 Body를 Metadata 원장과 분리한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R010 — Policy Schema

- 우선순위: `P0`
- 요구사항: Capture Mode, Allowlist, Masking, Sampling, TTL, Version을 저장한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R011 — Apply Operation

- 우선순위: `P0`
- 요구사항: 정책 적용 Operation과 Target별 결과를 저장한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R012 — Spool Checkpoint

- 우선순위: `P0`
- 요구사항: Gateway별 마지막 적재 Sequence와 Checksum을 저장한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R013 — Archive

- 우선순위: `P0`
- 요구사항: Online/Archive/Legal Hold/파기 Lifecycle을 제공한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R014 — 대량 Query

- 우선순위: `P0`
- 요구사항: 기간 제한, Keyset Paging, Covering Index를 제공한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R015 — Drift

- 우선순위: `P0`
- 요구사항: 존재하지 않는 Column/Index/FK와 Default Drift를 검출한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R016 — Install 멱등

- 우선순위: `P0`
- 요구사항: 설치/Seed 재실행이 중복·손상을 만들지 않는다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R017 — Upgrade 보존

- 우선순위: `P0`
- 요구사항: 기존 Service, Route, Policy, 거래 이력을 보존한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R018 — Rollback

- 우선순위: `P0`
- 요구사항: Rollback 후 이전 Runtime이 정상 동작한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R019 — Generator 영향

- 우선순위: `P0`
- 요구사항: Domain Manifest/Service Seed를 하드코딩 없이 생성한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R020 — DB Evidence

- 우선순위: `P0`
- 요구사항: 기존 DB와 Clean Install 결과를 Vendor/Profile별 기록한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP15-R021 — 용량 예측

- 우선순위: `P0`
- 요구사항: 로그 보존/Archive 정책별 예상 용량을 계산한다.
- Owner Module: `cpf-tools/db`
- 시작 상태: `부분 구현`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP16 — Test·Evidence·Hygiene·Handover

#### WP16-R001 — Requirement 추적

- 우선순위: `P0`
- 요구사항: 모든 ID를 Source, API, SQL, Test, UI, Evidence에 연결한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R002 — 구현 역추적

- 우선순위: `P0`
- 요구사항: Class, Table, API, Menu를 Requirement, Owner, Consumer와 연결한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R003 — 표적 Unit

- 우선순위: `P0`
- 요구사항: Policy, Selector, Parser, Validator, State Machine Unit Test를 제공한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R004 — Contract Test

- 우선순위: `P0`
- 요구사항: Local/Remote, Gateway/Target, ADM/Owner 계약을 검증한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R005 — Architecture Test

- 우선순위: `P0`
- 요구사항: Internal Import, 순환, Owner DB 접근, Package 규칙을 검증한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R006 — Gateway E2E

- 우선순위: `P0`
- 요구사항: 채널→Gateway→ACC/MBR 성공·실패·Failover를 검증한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R007 — Multi-instance

- 우선순위: `P0`
- 요구사항: Gateway/Health/Batch Worker 2개 이상에서 Lease와 수렴을 검증한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R008 — Failure Injection

- 우선순위: `P0`
- 요구사항: DB, Network, TLS, Target, Broker, Agent, Browser Push 장애를 주입한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R009 — Security Test

- 우선순위: `P0`
- 요구사항: Header spoofing, Command injection, PII leakage, 권한 우회를 검증한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R010 — Load Test

- 우선순위: `P0`
- 요구사항: Routing, Log Spool, Dashboard, 대량 조회 성능을 검증한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R011 — Browser E2E

- 우선순위: `P0`
- 요구사항: 5개 메뉴, Wizard, 연결 Test, 로그 Timeline, Batch 등록을 검증한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R012 — Accessibility

- 우선순위: `P0`
- 요구사항: Keyboard, Screen Reader, Responsive, Contrast를 검증한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R013 — Generator Smoke

- 우선순위: `P0`
- 요구사항: 임의 2개 Domain 생성, Test, Jar/War, 삭제, 재생성 parity를 검증한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R014 — DB Existing

- 우선순위: `P0`
- 요구사항: 기존 MariaDB Drift/Upgrade/Rollback/Reapply를 검증한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R015 — DB Clean

- 우선순위: `P0`
- 요구사항: 분리 DB Install/Seed/Verify를 검증한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R016 — Vendor Static

- 우선순위: `P0`
- 요구사항: PostgreSQL/Oracle Source/Contract/Checksum을 검증한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R017 — Frontend Build

- 우선순위: `P0`
- 요구사항: ADM/BZA Production Build와 Unit/Type/Lint를 검증한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R018 — 전체 Build

- 우선순위: `P0`
- 요구사항: clean test assemble를 최신 SHA에서 실행한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R019 — 공식 Gate

- 우선순위: `P0`
- 요구사항: qualityGate와 Final Gate를 통과한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R020 — False Green

- 우선순위: `P0`
- 요구사항: Gate가 현재 Source를 자동 정답화하거나 범위를 누락하지 않는지 검토한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R021 — Evidence 정화

- 우선순위: `P0`
- 요구사항: Token, Password, PII, DB 접속정보를 제거한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R022 — Evidence SHA

- 우선순위: `P0`
- 요구사항: 모든 Evidence에 최종 SHA를 명시한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R023 — Hygiene

- 우선순위: `P0`
- 요구사항: Build/Log/Tmp/Empty/Dead/Stale 파일을 제거한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R024 — Handover

- 우선순위: `P0`
- 요구사항: 완료·미검증·환경·다음 명령·결정을 독립 수행 가능하게 기록한다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP16-R025 — 성능 Baseline

- 우선순위: `P0`
- 요구사항: Gateway, Log, Batch 주요 경로의 성능 기준선을 남긴다.
- Owner Module: `전체`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

### WP17 — Codex 최종 독립 검수 전용

#### WP17-R001 — Codex 역할

- 우선순위: `P0`
- 요구사항: 개발 완료 Push 후 최신 master 독립 검수와 실패 반환만 수행한다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R002 — 새 설계 금지

- 우선순위: `P0`
- 요구사항: 미구현 기능을 축소 설계하지 않고 Requirement 충족 여부를 검증한다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R003 — Source 수정 통제

- 우선순위: `P0`
- 요구사항: 별도 승인 없이는 최종 검수에서 Source를 변경하지 않는다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R004 — 정확 SHA

- 우선순위: `P0`
- 요구사항: 검수 시작/종료 origin/master SHA를 기록한다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R005 — 전체 범위

- 우선순위: `P0`
- 요구사항: 최근 파일이 아니라 Repository 전체를 대조한다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R006 — 보고 불신

- 우선순위: `P0`
- 요구사항: 문서 완료보다 Source, SQL, UI, Test, Runtime Evidence를 우선한다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R007 — Requirement 전수

- 우선순위: `P0`
- 요구사항: 모든 ID를 허용 상태로 판정한다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R008 — Scenario 실행

- 우선순위: `P0`
- 요구사항: 가능 환경에서 Scenario를 실행하고 Evidence를 남긴다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R009 — False Completion

- 우선순위: `P0`
- 요구사항: Map, Raw JSON, Interface-only, Simulator-only를 완료로 인정하지 않는다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R010 — Test 약화

- 우선순위: `P0`
- 요구사항: 삭제, Skip, 범위 축소, Assertion 약화를 확인한다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R011 — DB 검수

- 우선순위: `P0`
- 요구사항: 기존/Clean DB와 3 Vendor 정합성을 확인한다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R012 — Gateway 검수

- 우선순위: `P0`
- 요구사항: 등록, Protocol, LB, Health, Test, Dashboard, Log를 검증한다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R013 — Batch 검수

- 우선순위: `P0`
- 요구사항: Job, Parameter, File, Shell, Recovery를 검증한다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R014 — 결함 반환

- 우선순위: `P0`
- 요구사항: 실패 ID, Source 위치, 명령, Expected/Actual, Evidence를 반환한다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

#### WP17-R015 — 완료 조건

- 우선순위: `P0`
- 요구사항: 구현 가능한 부분·미구현 0, 필수 PASS, 최신 SHA일 때만 완료한다.
- Owner Module: `Codex/QA`
- 시작 상태: `미검증`
- 필수 검증: Unit/Contract/Static/Runtime/Browser 중 해당 범위를 실제 실행
- 완료 조건: Source·API·SQL·UI·Test·Evidence가 연결되고 구현 가능한 부분 구현·미구현이 0
- 필수 Evidence: 기준 SHA, 명령, 환경, Expected/Actual, 종료코드, 민감정보 제거 여부

## 15. 상세 최종 검증 Scenario

아래 Scenario는 개발 중 Unit/Contract/Failure Injection Test와 최종 통합검증으로 연결한다.

### WP00 — 기준선·영향도·크레딧 최적화

#### WP00-S001 — 중간 Push 오판

- 우선순위: `P0`
- 사전조건: Continuity와 최신 master 존재
- 실행 절차: SHA와 상태 비교
- 기대 결과: 부분 구현/미검증이면 최종 완료가 아니다.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP00-S002 — 문서 충돌

- 우선순위: `P0`
- 사전조건: Current와 Final Request 충돌
- 실행 절차: Active/Historical 확인
- 기대 결과: 현재 지시만 Active다.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP00-S003 — 영향도 통합

- 우선순위: `P0`
- 사전조건: Gateway/Health/Log 요구 존재
- 실행 절차: Owner/Schema/Consumer 분석
- 기대 결과: 공통 Root WP로 묶인다.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP01 — 현재 Codex WIP 완결

#### WP01-S001 — 전체 Build

- 우선순위: `P0`
- 사전조건: 최신 SHA
- 실행 절차: clean test assemble
- 기대 결과: 모든 Module 성공.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP01-S002 — 기존 DB Upgrade

- 우선순위: `P0`
- 사전조건: 기존 MariaDB
- 실행 절차: 백업→Drift→Upgrade→Rollback→Reapply
- 기대 결과: 데이터 보존과 정합성 확인.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP01-S003 — Clean Install

- 우선순위: `P0`
- 사전조건: 분리 DB
- 실행 절차: Provision→Install→Seed→Verify
- 기대 결과: 기존 DB 영향 없이 성공.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP01-S004 — 문서 SHA

- 우선순위: `P0`
- 사전조건: 검증 완료
- 실행 절차: Active 문서 SHA 확인
- 기대 결과: 최종 SHA 일치.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP02 — Architecture·Ownership·Generated Domain

#### WP02-S001 — 임의 Domain

- 우선순위: `P0`
- 사전조건: Generator 가능
- 실행 절차: loan/LON, card/CRD 생성
- 기대 결과: 동일 Golden 구조.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP02-S002 — Internal Import

- 우선순위: `P0`
- 사전조건: 전체 Source
- 실행 절차: 금지 Package 검색
- 기대 결과: 외부 Import 0.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP02-S003 — 내부 호출

- 우선순위: `P0`
- 사전조건: ACC/MBR 구성
- 실행 절차: ACC→MBR 호출
- 기대 결과: Gateway 없이 Local/Remote 성공.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP03 — ADM 정보구조·메뉴·Feature Package

#### WP03-S001 — 5개 메뉴

- 우선순위: `P0`
- 사전조건: ADM 로그인
- 실행 절차: 좌측 메뉴 확인
- 기대 결과: 홈+5개 메뉴.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP03-S002 — Gateway OFF

- 우선순위: `P0`
- 사전조건: Capability OFF
- 실행 절차: 메뉴/API 접근
- 기대 결과: Gateway만 비활성, Registry 유지.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP03-S003 — Gateway DOWN

- 우선순위: `P0`
- 사전조건: Capability ON Runtime DOWN
- 실행 절차: ADM 접근
- 기대 결과: 메뉴 유지, unavailable 표시.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP03-S004 — Raw JSON

- 우선순위: `P0`
- 사전조건: 상세 조회
- 실행 절차: UI 확인
- 기대 결과: 기본 pre/raw JSON 없음.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP04 — Gateway 연동 서버·Registry·Binding

#### WP04-S001 — ACC 등록

- 우선순위: `P0`
- 사전조건: Registry 비어 있음
- 실행 절차: Service→Endpoint→2 Instance→Group
- 기대 결과: Versioned 저장.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP04-S002 — 기존 MBR Binding

- 우선순위: `P0`
- 사전조건: MBR Registry 존재
- 실행 절차: Gateway Binding 생성
- 기대 결과: IP 중복 없이 Binding.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP04-S003 — Default Deny

- 우선순위: `P0`
- 사전조건: 내부 Service
- 실행 절차: 공개 설정 없이 호출
- 기대 결과: 외부 접근 차단.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP04-S004 — CAS 충돌

- 우선순위: `P0`
- 사전조건: 동일 Version 두 사용자
- 실행 절차: 동시 수정
- 기대 결과: 한 건 Conflict.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP05 — Gateway Protocol·Route·보안 정책

#### WP05-S001 — HTTPS→HTTP

- 우선순위: `P0`
- 사전조건: 외부 HTTPS/내부 HTTP
- 실행 절차: Route/Test
- 기대 결과: 정상 전달.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP05-S002 — gRPC 미설치

- 우선순위: `P0`
- 사전조건: Adapter 없음
- 실행 절차: Select 확인
- 기대 결과: 사유와 Disabled.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP05-S003 — Route 충돌

- 우선순위: `P0`
- 사전조건: 동일 Key 존재
- 실행 절차: 저장
- 기대 결과: 적용 전 실패.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP05-S004 — Header 위조

- 우선순위: `P0`
- 사전조건: 외부 Trust Header
- 실행 절차: 호출
- 기대 결과: 제거·재작성.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP05-S005 — 비멱등 Retry

- 우선순위: `P0`
- 사전조건: 변경 POST
- 실행 절차: Retry 설정
- 기대 결과: 거부.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP06 — Server Group·Load Balance·Health

#### WP06-S001 — Round Robin

- 우선순위: `P0`
- 사전조건: 3 UP
- 실행 절차: 6회 호출
- 기대 결과: 순환 선택.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP06-S002 — Weighted

- 우선순위: `P0`
- 사전조건: 40/40/20
- 실행 절차: 반복 호출
- 기대 결과: 허용 오차 분포.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP06-S003 — Rendezvous

- 우선순위: `P0`
- 사전조건: 3대+Key
- 실행 절차: 서버 추가 전후
- 기대 결과: 제한 재배치.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP06-S004 — Failover

- 우선순위: `P0`
- 사전조건: Primary DOWN
- 실행 절차: 호출
- 기대 결과: Secondary와 Attempt.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP06-S005 — Health URL 실패

- 우선순위: `P0`
- 사전조건: TCP UP/App DOWN
- 실행 절차: Probe
- 기대 결과: Routing 제외.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP06-S006 — Ping 실패

- 우선순위: `P0`
- 사전조건: ICMP 차단/HTTP 정상
- 실행 절차: Probe
- 기대 결과: App 기준 판정.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP06-S007 — 중복 Probe

- 우선순위: `P0`
- 사전조건: Worker 2대
- 실행 절차: 동시 실행
- 기대 결과: Lease Owner만 Probe.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP06-S008 — Stale

- 우선순위: `P0`
- 사전조건: 갱신 중단
- 실행 절차: 임계 경과
- 기대 결과: STALE/UNKNOWN.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP07 — 연결시험·Dashboard·실시간 상태

#### WP07-S001 — 직접 Test

- 우선순위: `P0`
- 사전조건: ACC 2대
- 실행 절차: Gateway에서 Test
- 기대 결과: 단계별 결과.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP07-S002 — 경유 Test

- 우선순위: `P0`
- 사전조건: Route Draft
- 실행 절차: Channel 모의 호출
- 기대 결과: 실 Route/Target/Trace.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP07-S003 — 과거 성공 만료

- 우선순위: `P0`
- 사전조건: Test 성공 후 설정 변경
- 실행 절차: 상태 확인
- 기대 결과: STALE.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP07-S004 — SSE 단절

- 우선순위: `P0`
- 사전조건: Dashboard 연결
- 실행 절차: Network 복구
- 기대 결과: 단절 경고와 재연결.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP07-S005 — 부분 적용

- 우선순위: `P0`
- 사전조건: Gateway 3대 중 1대 실패
- 실행 절차: 정책 적용
- 기대 결과: 대상별 결과/rollback.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP08 — Gateway 거래로그·로그정책·조회 UI

#### WP08-S001 — 성공 원장

- 우선순위: `P0`
- 사전조건: 정상 Route
- 실행 절차: 1회 호출
- 기대 결과: IN/GW/OUT/RESULT 저장.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP08-S002 — Failover Attempt

- 우선순위: `P0`
- 사전조건: 첫 Timeout/둘째 성공
- 실행 절차: 호출
- 기대 결과: 2 Attempt 연결.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP08-S003 — DB 장애 Spool

- 우선순위: `P0`
- 사전조건: DB 중단
- 실행 절차: 호출 후 복구
- 기대 결과: 중복 없이 재적재.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP08-S004 — Body 상한

- 우선순위: `P0`
- 사전조건: Property false
- 실행 절차: FULL 요청
- 기대 결과: 거부.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP08-S005 — Allowlist

- 우선순위: `P0`
- 사전조건: JSON 정책
- 실행 절차: 민감/허용 필드 호출
- 기대 결과: 허용만 마스킹 저장.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP08-S006 — Search Hash

- 우선순위: `P0`
- 사전조건: 계좌번호 거래
- 실행 절차: 검색
- 기대 결과: 원문 없이 검색.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP08-S007 — 정책 부분 적용

- 우선순위: `P0`
- 사전조건: 1 Gateway ACK 실패
- 실행 절차: 적용
- 기대 결과: 상태/재시도/rollback.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP08-S008 — Export 감사

- 우선순위: `P0`
- 사전조건: 승인 없음
- 실행 절차: 민감 Export
- 기대 결과: 차단/Audit.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP09 — 개발자 Service Call·Typed Client·Setup

#### WP09-S001 — ACC→MBR Client

- 우선순위: `P0`
- 사전조건: Contract 생성
- 실행 절차: Typed Client 주입
- 기대 결과: URL 없이 성공.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP09-S002 — Local Auto

- 우선순위: `P0`
- 사전조건: 동일 JVM
- 실행 절차: 호출
- 기대 결과: Local 선택.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP09-S003 — Remote Auto

- 우선순위: `P0`
- 사전조건: 분리 WAS
- 실행 절차: 호출
- 기대 결과: Registry Remote 선택.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP09-S004 — Breaking Change

- 우선순위: `P0`
- 사전조건: 응답 필드 제거
- 실행 절차: 재생성
- 기대 결과: Consumer 영향 표시.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP09-S005 — 설정 누락

- 우선순위: `P0`
- 사전조건: Endpoint 없음
- 실행 절차: 기동
- 기대 결과: 명확한 Fail-fast.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP10 — Batch Job 등록·Executor·Schedule

#### WP10-S001 — Shell 등록

- 우선순위: `P0`
- 사전조건: Script Catalog 존재
- 실행 절차: Wizard 등록/Publish
- 기대 결과: 승인 Version 생성.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP10-S002 — File Watch 등록

- 우선순위: `P0`
- 사전조건: SFTP Provider
- 실행 절차: Pattern/Marker/Stability
- 기대 결과: 동적 Form 검증.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP10-S003 — Misfire

- 우선순위: `P0`
- 사전조건: 과거 Schedule
- 실행 절차: 재기동
- 기대 결과: 정책 적용.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP10-S004 — Dependency Cycle

- 우선순위: `P0`
- 사전조건: A↔B
- 실행 절차: 저장
- 기대 결과: 거부.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP10-S005 — Unknown

- 우선순위: `P0`
- 사전조건: Worker 단절
- 실행 절차: 조회
- 기대 결과: UNKNOWN_RESULT.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP11 — Batch File Watch·Process·Transfer

#### WP11-S001 — 전송 중

- 우선순위: `P0`
- 사전조건: 크기 증가
- 실행 절차: Watch Event
- 기대 결과: Stable 전 미실행.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP11-S002 — Marker 누락

- 우선순위: `P0`
- 사전조건: Data만 존재
- 실행 절차: 주기 실행
- 기대 결과: 보류.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP11-S003 — 중복 Worker

- 우선순위: `P0`
- 사전조건: 2 Worker
- 실행 절차: 동일 파일
- 기대 결과: 1 Claim.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP11-S004 — Worker Kill

- 우선순위: `P0`
- 사전조건: Processing 중 종료
- 실행 절차: Lease 만료
- 기대 결과: Reconcile/Takeover.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP11-S005 — SFTP 단절

- 우선순위: `P0`
- 사전조건: 원격 단절
- 실행 절차: 복구
- 기대 결과: 누락 재탐색.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP11-S006 — Checksum 불일치

- 우선순위: `P0`
- 사전조건: Sidecar 불일치
- 실행 절차: 검증
- 기대 결과: 처리 차단.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP12 — Approved Shell·Agent·Process 보안

#### WP12-S001 — Command Injection

- 우선순위: `P0`
- 사전조건: 제어문자 Parameter
- 실행 절차: 실행
- 기대 결과: 안전 전달/거부.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP12-S002 — Secret Process

- 우선순위: `P0`
- 사전조건: Secret Parameter
- 실행 절차: Process 목록
- 기대 결과: 원문 없음.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP12-S003 — Child Timeout

- 우선순위: `P0`
- 사전조건: Child 생성
- 실행 절차: Timeout
- 기대 결과: Process Tree 종료.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP12-S004 — 출력 폭주

- 우선순위: `P0`
- 사전조건: 무한 stdout
- 실행 절차: 실행
- 기대 결과: 메모리 보호.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP12-S005 — Agent 단절

- 우선순위: `P0`
- 사전조건: 실행 중 단절
- 실행 절차: 조회
- 기대 결과: Unknown/Reconcile.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP12-S006 — 서명 변조

- 우선순위: `P0`
- 사전조건: 파일 변경
- 실행 절차: 실행
- 기대 결과: 차단.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP13 — 공통 Parameter Schema·동적 UI

#### WP13-S001 — Protocol Select

- 우선순위: `P0`
- 사전조건: Endpoint Form
- 실행 절차: Select 확인
- 기대 결과: Catalog 옵션.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP13-S002 — 종속 Select

- 우선순위: `P0`
- 사전조건: Service 선택
- 실행 절차: Endpoint/Group 확인
- 기대 결과: 유효 값만.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP13-S003 — Secret Picker

- 우선순위: `P0`
- 사전조건: Sensitive Parameter
- 실행 절차: Form
- 기대 결과: Reference 선택.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP13-S004 — 최종 Preview

- 우선순위: `P0`
- 사전조건: 여러 Override
- 실행 절차: Preview
- 기대 결과: Source/최종/Masking 표시.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP13-S005 — 검증 일치

- 우선순위: `P0`
- 사전조건: 잘못된 Date/Enum
- 실행 절차: UI/API
- 기대 결과: 동일 실패.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP14 — 권한·승인·감사·Masking

#### WP14-S001 — requestedBy 위조

- 우선순위: `P0`
- 사전조건: 다른 사용자 Body
- 실행 절차: API
- 기대 결과: 차단.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP14-S002 — 자기 승인

- 우선순위: `P0`
- 사전조건: 요청자=승인자
- 실행 절차: 승인
- 기대 결과: 거부.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP14-S003 — Masking 해제

- 우선순위: `P0`
- 사전조건: 일반 Role
- 실행 절차: Raw 조회
- 기대 결과: 거부.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP14-S004 — Audit 실패

- 우선순위: `P0`
- 사전조건: 위험 변경
- 실행 절차: Audit DB 실패
- 기대 결과: 성공 위장 없음.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP14-S005 — Header Spoof

- 우선순위: `P0`
- 사전조건: 외부 Actor Header
- 실행 절차: Gateway
- 기대 결과: 검증 Context만 전달.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP15 — DB·Migration·Vendor·Archive

#### WP15-S001 — 3 Vendor 생성

- 우선순위: `P0`
- 사전조건: Canonical 변경
- 실행 절차: Sync
- 기대 결과: 3 Vendor 일치.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP15-S002 — Upgrade

- 우선순위: `P0`
- 사전조건: 기존 Schema
- 실행 절차: Migration
- 기대 결과: 데이터 보존.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP15-S003 — Rollback

- 우선순위: `P0`
- 사전조건: Upgrade 후
- 실행 절차: Rollback/구버전
- 기대 결과: 정상.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP15-S004 — Partition

- 우선순위: `P0`
- 사전조건: 대량 로그
- 실행 절차: 기간/Trace 조회
- 기대 결과: Pruning/Index.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP15-S005 — Legal Hold

- 우선순위: `P0`
- 사전조건: 만료+Hold
- 실행 절차: 파기
- 기대 결과: Hold 보존.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP16 — Test·Evidence·Hygiene·Handover

#### WP16-S001 — Exact SHA

- 우선순위: `P0`
- 사전조건: 검증 완료
- 실행 절차: Manifest 확인
- 기대 결과: 명령/환경/SHA 존재.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP16-S002 — False Green

- 우선순위: `P0`
- 사전조건: 고의 결함
- 실행 절차: Gate
- 기대 결과: 실패.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP16-S003 — Browser E2E

- 우선순위: `P0`
- 사전조건: 운영 Role
- 실행 절차: 주요 흐름
- 기대 결과: UI/API/권한 일치.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP16-S004 — Secret Scan

- 우선순위: `P0`
- 사전조건: Evidence 생성
- 실행 절차: Scan
- 기대 결과: 원문 0.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP16-S005 — Hygiene

- 우선순위: `P0`
- 사전조건: Build 후
- 실행 절차: 잔재 검사
- 기대 결과: Build/Tmp/Log 0.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

### WP17 — Codex 최종 독립 검수 전용

#### WP17-S001 — 독립 검수

- 우선순위: `P0`
- 사전조건: 개발 Push
- 실행 절차: 최신 master 검수
- 기대 결과: 보고와 무관하게 결함 탐지.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP17-S002 — 전수 판정

- 우선순위: `P0`
- 사전조건: Inventory
- 실행 절차: 모든 ID
- 기대 결과: 누락 0.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP17-S003 — 부분 구현

- 우선순위: `P0`
- 사전조건: Interface/Stub
- 실행 절차: 검수
- 기대 결과: 부분 구현 판정.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

#### WP17-S004 — 최종 완료

- 우선순위: `P0`
- 사전조건: 필수 Evidence
- 실행 절차: Gate 확인
- 기대 결과: 미완료 0일 때만 완료.
- 초기 실행 상태: `미검증`
- 필수 Evidence: 최종 SHA와 명령/Trace/DB/UI 근거

## 16. 개발 중 검증 전략

### 16.1 지속 실행할 저비용 Gate

- 변경 Module compileJava/testClasses
- 표적 Unit/Slice/Contract Test
- Public API/SPI/Internal Import Gate
- Module Dependency/Ownership Gate
- Frontend Typecheck와 Component Test
- SQL Syntax/Canonical/Checksum
- Route/Menu/API/Permission parity
- Secret/PII Static Scan
- Repository Hygiene와 diff --check

### 16.2 공통 구현 연결 후 실행할 고비용 검증

- `.\gradlew.bat clean test assemble --no-daemon`
- ADM/BZA Frontend Production Build와 Browser E2E
- 기존 MariaDB Drift/Upgrade/Rollback/Re-apply
- 분리 Database Clean Install/Product Seed/Verify
- Gateway 다중 Instance, LB, Health, Policy Convergence
- Batch 다중 Worker, File/Shell Failure Injection
- 전체 qualityGate와 Final Gate

Gate가 잘못된 경우 Gate를 우회하지 말고 Gate와 Source 정본을 함께 수정한다.

## 17. DB 원칙

- 공식 Vendor는 MariaDB, PostgreSQL, Oracle 3종이다.
- Canonical Metadata에서 DDL, Index, FK, Seed, Verify, Migration, Rollback을 생성·동기화한다.
- Historical Migration Checksum을 변경하지 않는다.
- 기존 로컬 DB는 삭제하지 않고 Drift, Upgrade, Rollback, Re-apply를 검증한다.
- Clean Install은 새 Database 또는 Schema에서 수행한다.
- Gateway Transaction/Attempt는 업무일자 Partition과 Trace/Route/Target Index를 갖는다.
- Body Storage는 Metadata 원장과 분리하고 Encryption, Retention, Legal Hold를 관리한다.
- DB 변경은 Generator와 Generated Domain 영향을 함께 검토한다.

## 18. 보안·권한·감사 완료 기준

- Body의 requestedBy를 신뢰하지 않고 인증 Context의 Actor를 사용한다.
- READ, CREATE, UPDATE, DELETE, TEST, APPLY, DRAIN, EXPORT, RAW_VIEW 권한을 분리한다.
- Route 공개, Masking 해제, Body Capture, Shell 실행, 강제 Failover에 승인 정책을 적용한다.
- 요청자와 승인자의 자기승인을 기본 금지한다.
- 위험 조치에는 구조화 Reason, Immutable Command Snapshot, Before/After Audit을 남긴다.
- Password, Token, Private Key, Secret을 DB·화면·로그·Evidence에 원문 저장하지 않는다.
- 마스킹은 Frontend가 아니라 수집·저장·조회 Server 경계에서 적용한다.
- Audit 저장 실패를 성공으로 위장하지 않는다.

## 19. 완료 금지 조건

- Requirement Inventory에 구현 가능한 `부분 구현` 또는 `미구현`이 남아 있다.
- Interface, DTO, SPI만 있고 Runtime Consumer가 없다.
- 화면은 있지만 Owner API, DB, Runtime Command가 연결되지 않았다.
- Public API가 `Map<String,Object>` 또는 Raw JSON 중심이다.
- ADM 기본 화면이 `<pre>` 또는 로그 원문 출력에 의존한다.
- Gateway 거래가 IN/GATEWAY/OUT/Attempt로 구분되지 않는다.
- Gateway 연결시험이 ADM 직접 Target 호출이고 실제 Gateway를 경유하지 않는다.
- Health가 Ping 하나로 Application 정상 여부를 판정한다.
- 로그 Body 설정이 Y/N만 있고 Allowlist, Masking, 안전 상한이 없다.
- File Watch가 단순 파일 생성 Event 대기 수준이다.
- Shell Timeout 뒤 Child Process가 남거나 Secret이 Command Line에 노출된다.
- 다중 Instance Policy 적용 결과와 Drift가 없다.
- 공식 DB 3종과 Generator 영향이 누락됐다.
- 전체 Clean Build, Frontend, DB, Final Gate를 실행하지 않았다.
- Evidence SHA가 최종 Source SHA와 다르다.
- 실행하지 않은 검증을 PASS로 기록했다.
- 다음 요청서만 작성하고 Source 개발 없이 종료했다.

## 20. 세션 단위 작업 규칙

한 세션에서 전체 완료가 불가능하더라도 질문이나 새 설계 문서로 시간을 소비하지 않는다. 의존 순서상 완결 가능한 Work Package를 실제 Source와 Test까지 끝낸다.

세션 종료 시 기존 정본 Handover/Continuity를 다음 항목으로 갱신한다.

- 시작 SHA와 종료 시점 SHA
- 실제 수정한 Requirement/Defect ID
- 완료·부분 구현·미구현·미검증 상태
- 변경 Source·SQL·API·Frontend·Test 경로
- 직접 실행한 명령, 종료코드, 실제 결과
- 실행하지 못한 Runtime 검증
- 공통 기반 완료 여부
- 아직 연결되지 않은 Consumer
- 보호해야 할 기존 성공 기능
- 다음 세션의 정확한 첫 파일·첫 명령·첫 Requirement ID
- Commit/Push 미수행 여부

Current Request에는 현재 해야 할 일만 남기고 과거 완료 이력을 계속 누적하지 않는다.

## 21. 최종 Evidence 형식

- 기준 Commit SHA
- Requirement/Scenario ID
- 실행 명령과 Profile
- 도구와 환경 Version
- 시작·종료 시각
- Expected와 Actual
- 종료코드
- 관련 Source/API/SQL/UI/Test 경로
- 민감정보 제거 여부
- Existing DB/Clean DB/Vendor 구분
- 다중 Instance/Browser/Failure Injection 여부

과거 Commit이나 다른 장비의 Evidence를 현재 SHA 성공 근거로 자동 승계하지 않는다.

## 22. 최종 종료 조건

- Source 확인 결함이 모두 수정되거나 현재 Source가 올바른 근거가 기록됐다.
- 구현 가능한 Requirement의 `부분 구현`과 `미구현`이 0이다.
- Gateway 등록·Protocol·LB·Health·연결시험·Dashboard·로그가 실제 Runtime과 연결됐다.
- Batch Job·Parameter·File Watch·Approved Shell이 상용 Runtime 수준으로 연결됐다.
- ADM 5개 메뉴, Route, API, Permission과 Feature Package가 일치한다.
- MariaDB 기존 DB와 Clean Install이 검증됐다.
- PostgreSQL/Oracle은 Source/Contract/Static 결과와 Runtime 미검증을 구분했다.
- 전체 Clean Build/Test/Frontend/Final Gate가 통과했다.
- Repository Hygiene와 Secret/PII 검사가 통과했다.
- Current/Handover/Review/Evidence가 최종 SHA와 일치한다.

개발 완료 후에만 Codex가 WP17과 Scenario Matrix를 이용해 최종 독립검수를 수행한다.
