# CPF Developer GPT QA · 개발착수 상세 리뷰 / 실행 원장

- 작성일: **2026-08-24**
- 문서 성격: **정식 Developer GPT QA 결과 + 개발 실행 원장 + Closure 리뷰 + 다음 세션 인수인계 기준**
- 최종 Repository 관리 경로: `cpf-docs/work/current/CPF_DEVELOPER_GPT_QA_DEVELOPMENT_REVIEW.md`
- 현재 단계: **Source 개발 완료 / 정적 검증 PASS / 필수 로컬 Runtime·Codex 독립 검증 대기**
- Source 기준: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260824_203050.zip`
- Codex 보호 원칙: `CODEX_*`, Codex Work Package, `cpf-docs/work/evidence/codex/current/**`는 **읽기 전용**. Developer GPT가 상태/내용/Evidence를 대신 수정하지 않는다.
- 중요: 본 문서는 개발 중 **동일 ID를 유지한 채 실제 개발내용·수정파일·정적검증·로컬 Runtime·Codex 재검수 결과를 계속 현행화**한다.
- 중요: 최종 완료 ZIP에는 이 문서의 **완료본**, Developer GPT Evidence, Handover, Delete Manifest, 로컬 Runtime 명령/결과를 함께 포함한다.

## 1. 왜 이 문서를 다시 상세화했는가

기존 초안의 105개 인덱스는 범주와 상태는 추적할 수 있었지만, 실제 개발자가 다음 세션에서 그대로 이어받기에는 **Owner/Source/Consumer/수정방법/실제 개발기록/Runtime/DB3/Codex cross-check**가 부족했다. 본 개정본은 각 인덱스를 하나의 독립 작업카드로 확장하여 다음 정보를 반드시 남긴다.

1. 원 Requirement / Upstream Finding
2. 현재 Source에서 확인된 사실
3. 변경 목적과 Root Cause
4. 영향 Owner / Source / Consumer / 호출경로
5. **계획된 구체 개발내용**
6. **실제 개발내용 기록란** — 무엇을 어떻게 바꿨는지
7. **실제 수정파일 기록란**
8. Side Effect / 회귀범위
9. DB3 영향 및 Vendor 3사 Lifecycle 의무
10. 정적검증 계획/결과
11. 로컬 Runtime 검증 계획/결과
12. Codex 독립 재검수 여부/결과
13. Evidence
14. Closure 조건 및 최종 판정

## 2. 상태축 — 서로 대신하지 않는다

| 상태축 | 허용 상태 | 완료 의미 |
|---|---|---|
| 개발완료 | 미착수 / 진행중 / Source Fixed / 기존구현확인 / 완료 | 실제 Source·Consumer·DB·Frontend·Script까지 구현됨 |
| 정적검증완료 | 미실행 / 부분 PASS / PASS / FAIL | build/unit/static/verifier 결과 |
| 런타임검증완료 | 미실행 / 부분 PASS / PASS / FAIL / BLOCKED_EXTERNAL | 실제 Process/DB/Browser/Multi-instance 결과 |
| Codex검증완료 | 미실행 / PENDING / PASS / FAIL / 해당없음 | Codex가 자기 원장에서 독립 cross-check한 결과 |
| 전체 Closure | OPEN / IN_PROGRESS / SOURCE_FIXED / VERIFICATION_PENDING / CLOSED / BLOCKED_EXTERNAL | Requirement·Side Effect·Runtime·Evidence까지 닫힘 |

**규칙:** `개발완료=완료`나 `정적검증완료=PASS`만으로 `CLOSED`가 아니다. Developer GPT가 수정한 Runtime 영향 항목은 `런타임검증완료=PASS` 전에는 CLOSED 금지. Codex 재검수 대상은 Developer GPT가 `Codex검증완료`를 대신 PASS로 바꾸지 않는다.

## 3. QA 핵심 사실

- Codex 후반 Center-Cut/Generated Domain/Observability Source는 현재 ZIP에 실제 반영되어 있어 처음부터 재개발하지 않는다.
- ADM Approval Engine은 정책/참여자 Snapshot/결정/멱등성/Owner Command/UNKNOWN-Reconcile 기반이 있으나, 업무유형별 동적 판단문서·History Search·Before/After·Result Read Model은 부족하다.
- ADM `AdmApprovalService.detail()`은 현재 `payloadSnapshot`을 통째로 제거하며 관련 테스트도 이를 기대하므로, 민감정보 마스킹과 업무 판단정보 제공을 분리해야 한다.
- Backoffice Approval 화면은 현재 `StructuredDataView` 기반 범용 표현이고 `MBW_APPROVAL_HISTORY`는 DB write가 있으나 정식 History read API가 확인되지 않았다.
- DB3 기반은 강하지만 semantic parity가 referenceFixture 4개 테이블 혼입을 3 Vendor 모두에서 FAIL로 잡았다.
- 현재 ZIP에는 path>200, nested cpf-docs/project-cache 등의 Final Hygiene 실패가 존재한다.

## 4. 전체 실행 인덱스 요약 — 기존 105개 + BAT/CEC 10개 + Open Git 40개 = 총 155개

| ID | Origin | 작업 제목 | 개발완료 | 정적검증 | Runtime | Codex검증 | 재검수 |
|---|---|---|---|---|---|---|---|
| `WP-00.01` | `GOVERNANCE` | Developer GPT/Codex 문서 경계 고정 | 미착수 | QA 확인 | 미실행 | 해당없음 | NO |
| `WP-00.02` | `GOVERNANCE` | 정식 작업문서 등록 | 미착수 | QA 확인 | 미실행 | 해당없음 | NO |
| `WP-00.03` | `GOVERNANCE` | 상태모델 고정 | 미착수 | QA 확인 | 미실행 | 해당없음 | NO |
| `WP-00.04` | `GOVERNANCE` | upstream reference 규칙 | 미착수 | QA 확인 | 미실행 | 해당없음 | NO |
| `WP-00.05` | `GOVERNANCE` | Codex 재검수 대상 식별 | 미착수 | QA 확인 | 미실행 | 해당없음 | NO |
| `WP-00.06` | `GOVERNANCE` | Source Identity 기준 | 미착수 | QA 확인 | 미실행 | 해당없음 | NO |
| `WP-00.07` | `GOVERNANCE` | Root Cause 작업원칙 | 미착수 | QA 확인 | 미실행 | 해당없음 | NO |
| `WP-00.08` | `GOVERNANCE` | Runtime Closure 규칙 | 미착수 | QA 확인 | 미실행 | 해당없음 | NO |
| `WP-00.09` | `GOVERNANCE` | DB3 비협상 규칙 | 미착수 | QA 확인 | 미실행 | 해당없음 | NO |
| `WP-00.10` | `GOVERNANCE` | 최종 ZIP 승계 | 미착수 | QA 확인 | 미실행 | 해당없음 | NO |
| `WP-01.01` | `CODEX_CROSSCHECK` | F265 Center-Cut Kafka 직접결합 제거 확인 | 기존구현확인 | 부분 PASS | 우리측 미실행 | Codex CLOSED/PENDING 혼재 | YES |
| `WP-01.02` | `CODEX_CROSSCHECK` | F274 TargetProvider concrete 구현 확인 | 기존구현확인 | 부분 PASS | 우리측 미실행 | Codex CLOSED/PENDING 혼재 | YES |
| `WP-01.03` | `CODEX_CROSSCHECK` | F274 Handler concrete 구현 확인 | 기존구현확인 | 부분 PASS | 우리측 미실행 | Codex CLOSED/PENDING 혼재 | YES |
| `WP-01.04` | `CODEX_CROSSCHECK` | Center-Cut Seed→Provider/Handler 연결 확인 | 기존구현확인 | 부분 PASS | 우리측 미실행 | Codex CLOSED/PENDING 혼재 | YES |
| `WP-01.05` | `CODEX_CROSSCHECK` | DB Work/Claim/Lease/Fencing 경로 확인 | 기존구현확인 | 부분 PASS | 우리측 미실행 | Codex CLOSED/PENDING 혼재 | YES |
| `WP-01.06` | `CODEX_CROSSCHECK` | Header6/CEC Identity 연결 확인 | 기존구현확인 | 부분 PASS | 우리측 미실행 | Codex CLOSED/PENDING 혼재 | YES |
| `WP-01.07` | `CODEX_CROSSCHECK` | Generated MBR sample canonical operation 확인 | 기존구현확인 | 부분 PASS | 우리측 미실행 | Codex CLOSED/PENDING 혼재 | YES |
| `WP-01.08` | `CODEX_CROSSCHECK` | F275 Boot5 공통 실행체 확인 | 기존구현확인 | 부분 PASS | 우리측 미실행 | Codex CLOSED/PENDING 혼재 | YES |
| `WP-01.09` | `CODEX_CROSSCHECK` | F277~F281 공통 Boot 결함 회귀 확인 | 기존구현확인 | 부분 PASS | 우리측 미실행 | Codex CLOSED/PENDING 혼재 | YES |
| `WP-01.10` | `CODEX_CROSSCHECK` | F293~F299 실제 Domain Invocation 보완 확인 | 기존구현확인 | 부분 PASS | 우리측 미실행 | Codex CLOSED/PENDING 혼재 | YES |
| `WP-01.11` | `CODEX_CROSSCHECK` | Codex CLOSED 영역 최소 재현 Gate | 기존구현확인 | 부분 PASS | 우리측 미실행 | Codex CLOSED/PENDING 혼재 | YES |
| `WP-01.12` | `CODEX_CROSSCHECK` | Codex PENDING 영역 재개발 금지/영향재검증 | 기존구현확인 | 부분 PASS | 우리측 미실행 | Codex CLOSED/PENDING 혼재 | YES |
| `WP-02.01` | `CODEX_UNFINISHED` | F304 E2E27 최신 Segment INSERT 오류 확보 | Source Fixed 확인 | 부분 PASS | FAIL/미완료 | PENDING | YES |
| `WP-02.02` | `CODEX_UNFINISHED` | CPF_TRANSACTION_SEGMENT execution_id canonical 확인 | Source Fixed 확인 | 부분 PASS | FAIL/미완료 | PENDING | YES |
| `WP-02.03` | `CODEX_UNFINISHED` | Segment execution index 확인 | Source Fixed 확인 | 부분 PASS | FAIL/미완료 | PENDING | YES |
| `WP-02.04` | `CODEX_UNFINISHED` | Segment Mapper parameter/result contract 확인 | Source Fixed 확인 | 부분 PASS | FAIL/미완료 | PENDING | YES |
| `WP-02.05` | `CODEX_UNFINISHED` | Segment persistence lifecycle 확인 | Source Fixed 확인 | 부분 PASS | FAIL/미완료 | PENDING | YES |
| `WP-02.06` | `CODEX_UNFINISHED` | Summary→Segment root transaction 연결 | Source Fixed 확인 | 부분 PASS | FAIL/미완료 | PENDING | YES |
| `WP-02.07` | `CODEX_UNFINISHED` | parentSegment/attempt/execution lineage 확인 | Source Fixed 확인 | 부분 PASS | FAIL/미완료 | PENDING | YES |
| `WP-02.08` | `CODEX_UNFINISHED` | Timeline target_system_code 현행화 확인 | Source Fixed 확인 | 부분 PASS | FAIL/미완료 | PENDING | YES |
| `WP-02.09` | `CODEX_UNFINISHED` | Durable fallback 상태 확인 | Source Fixed 확인 | 부분 PASS | FAIL/미완료 | PENDING | YES |
| `WP-02.10` | `CODEX_UNFINISHED` | DB3 F304 projection parity | Source Fixed 확인 | 부분 PASS | FAIL/미완료 | PENDING | YES |
| `WP-02.11` | `CODEX_UNFINISHED` | Fresh E2E 재실행 | Source Fixed 확인 | 부분 PASS | FAIL/미완료 | PENDING | YES |
| `WP-02.12` | `CODEX_UNFINISHED` | F300~F304 통합 Closure | Source Fixed 확인 | 부분 PASS | FAIL/미완료 | PENDING | YES |
| `WP-03.01` | `CODEX_UNFINISHED` | 2 Worker 정상 분산 | 부분 구현 | 부분 PASS | 미완료 | PENDING | YES |
| `WP-03.02` | `CODEX_UNFINISHED` | Worker process kill | 부분 구현 | 부분 PASS | 미완료 | PENDING | YES |
| `WP-03.03` | `CODEX_UNFINISHED` | Lease expiry | 부분 구현 | 부분 PASS | 미완료 | PENDING | YES |
| `WP-03.04` | `CODEX_UNFINISHED` | Fencing stale mutation 차단 | 부분 구현 | 부분 PASS | 미완료 | PENDING | YES |
| `WP-03.05` | `CODEX_UNFINISHED` | UNKNOWN 진입 | 부분 구현 | 부분 PASS | 미완료 | PENDING | YES |
| `WP-03.06` | `CODEX_UNFINISHED` | UNKNOWN blocking | 부분 구현 | 부분 PASS | 미완료 | PENDING | YES |
| `WP-03.07` | `CODEX_UNFINISHED` | Probe | 부분 구현 | 부분 PASS | 미완료 | PENDING | YES |
| `WP-03.08` | `CODEX_UNFINISHED` | Reconcile | 부분 구현 | 부분 PASS | 미완료 | PENDING | YES |
| `WP-03.09` | `CODEX_UNFINISHED` | Retry/Restart/Recovery/Reprocess 구분 | 부분 구현 | 부분 PASS | 미완료 | PENDING | YES |
| `WP-03.10` | `CODEX_UNFINISHED` | Partial success | 부분 구현 | 부분 PASS | 미완료 | PENDING | YES |
| `WP-03.11` | `CODEX_UNFINISHED` | Trace/Timeline 장애 연속성 | 부분 구현 | 부분 PASS | 미완료 | PENDING | YES |
| `WP-03.12` | `CODEX_UNFINISHED` | Leak cleanup | 부분 구현 | 부분 PASS | 미완료 | PENDING | YES |
| `WP-04.01` | `DEVELOPER_GPT_NEW` | ADM Approval Type Inventory | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-04.02` | `DEVELOPER_GPT_NEW` | Dynamic Approval Document SPI | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-04.03` | `DEVELOPER_GPT_NEW` | 승인 상세 payload 정책 보완 | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-04.04` | `DEVELOPER_GPT_NEW` | 업무별 필수 판단정보 Read Model | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-04.05` | `DEVELOPER_GPT_NEW` | Before/After field diff | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-04.06` | `DEVELOPER_GPT_NEW` | Snapshot version contract | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-04.07` | `DEVELOPER_GPT_NEW` | ADM Approval 목록/검색 | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-04.08` | `DEVELOPER_GPT_NEW` | ADM History 조회 | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-04.09` | `DEVELOPER_GPT_NEW` | Attachment/근거 접근 | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-04.10` | `DEVELOPER_GPT_NEW` | Execution result 문서 연결 | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-04.11` | `DEVELOPER_GPT_NEW` | Backend approve-time 재검증 | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-04.12` | `DEVELOPER_GPT_NEW` | ADM Browser E2E | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-05.01` | `DEVELOPER_GPT_NEW` | MBW Approval Type Inventory | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-05.02` | `DEVELOPER_GPT_NEW` | MBW Dynamic Document Provider | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-05.03` | `DEVELOPER_GPT_NEW` | Backoffice UI generic StructuredDataView 제거/보완 | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-05.04` | `DEVELOPER_GPT_NEW` | Approval ID 직접조회 의존 축소 | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-05.05` | `DEVELOPER_GPT_NEW` | MBW History read API | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-05.06` | `DEVELOPER_GPT_NEW` | MBW History 검색/Paging/Sorting | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-05.07` | `DEVELOPER_GPT_NEW` | MBW Before/After | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-05.08` | `DEVELOPER_GPT_NEW` | MBW Attachment detail | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-05.09` | `DEVELOPER_GPT_NEW` | MBW Execution/Apply result | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-05.10` | `DEVELOPER_GPT_NEW` | Resubmit lineage | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-05.11` | `DEVELOPER_GPT_NEW` | Concurrency/SoD/optimistic lock 회귀 | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-05.12` | `DEVELOPER_GPT_NEW` | Backoffice Browser E2E | 미완료 | FAIL(Requirement Gap) | 미실행 | 미실행 | YES |
| `WP-06.01` | `DEVELOPER_GPT_NEW` | Approval Canonical DB Model Gap 분석 | 미착수 | 미실행 | 미실행 | 미실행 | YES |
| `WP-06.02` | `DEVELOPER_GPT_NEW` | ADM Approval DB 변경 설계 | 미착수 | 미실행 | 미실행 | 미실행 | YES |
| `WP-06.03` | `DEVELOPER_GPT_NEW` | MBW Approval DB 변경 설계 | 미착수 | 미실행 | 미실행 | 미실행 | YES |
| `WP-06.04` | `DEVELOPER_GPT_NEW` | Approval API/OpenAPI 동시 확장 | 미착수 | 미실행 | 미실행 | 미실행 | YES |
| `WP-06.05` | `DEVELOPER_GPT_NEW` | Approval Existing Data preservation | 미착수 | 미실행 | 미실행 | 미실행 | YES |
| `WP-06.06` | `DEVELOPER_GPT_NEW` | Approval Runtime result persistence | 미착수 | 미실행 | 미실행 | 미실행 | YES |
| `WP-06.07` | `DEVELOPER_GPT_NEW` | Approval Audit/Trace | 미착수 | 미실행 | 미실행 | 미실행 | YES |
| `WP-06.08` | `DEVELOPER_GPT_NEW` | Approval security negative | 미착수 | 미실행 | 미실행 | 미실행 | YES |
| `WP-06.09` | `DEVELOPER_GPT_NEW` | Approval full E2E | 미착수 | 미실행 | 미실행 | 미실행 | YES |
| `WP-06.10` | `DEVELOPER_GPT_NEW` | Codex Cross-check package | 미착수 | 미실행 | 미실행 | 미실행 | YES |
| `WP-07.01` | `QA_GAP+NEW_REQUIREMENT` | 최상위 정본 DB3 원칙 보강 | 부분 구현 | PASS+FAIL 혼재 | 미실행 | 부분 | YES |
| `WP-07.02` | `QA_GAP+NEW_REQUIREMENT` | Canonical DB Source Owner 확인 | 부분 구현 | PASS+FAIL 혼재 | 미실행 | 부분 | YES |
| `WP-07.03` | `QA_GAP+NEW_REQUIREMENT` | Initializer/Fresh Init current 확인 | 부분 구현 | PASS+FAIL 혼재 | 미실행 | 부분 | YES |
| `WP-07.04` | `QA_GAP+NEW_REQUIREMENT` | Oracle current | 부분 구현 | PASS+FAIL 혼재 | 미실행 | 부분 | YES |
| `WP-07.05` | `QA_GAP+NEW_REQUIREMENT` | PostgreSQL current | 부분 구현 | PASS+FAIL 혼재 | 미실행 | 부분 | YES |
| `WP-07.06` | `QA_GAP+NEW_REQUIREMENT` | MariaDB current | 부분 구현 | PASS+FAIL 혼재 | 미실행 | 부분 | YES |
| `WP-07.07` | `QA_GAP+NEW_REQUIREMENT` | Semantic parity FAIL root cause | 부분 구현 | PASS+FAIL 혼재 | 미실행 | 부분 | YES |
| `WP-07.08` | `QA_GAP+NEW_REQUIREMENT` | Fresh Init vs Upgrade schema parity | 부분 구현 | PASS+FAIL 혼재 | 미실행 | 부분 | YES |
| `WP-07.09` | `QA_GAP+NEW_REQUIREMENT` | Immutable migration 보존 | 부분 구현 | PASS+FAIL 혼재 | 미실행 | 부분 | YES |
| `WP-07.10` | `QA_GAP+NEW_REQUIREMENT` | 3 Vendor Upgrade/Rollback-Recovery | 부분 구현 | PASS+FAIL 혼재 | 미실행 | 부분 | YES |
| `WP-07.11` | `QA_GAP+NEW_REQUIREMENT` | 3 Vendor Runtime Query consumer | 부분 구현 | PASS+FAIL 혼재 | 미실행 | 부분 | YES |
| `WP-07.12` | `QA_GAP+NEW_REQUIREMENT` | DB3 local integrated command | 부분 구현 | PASS+FAIL 혼재 | 미실행 | 부분 | YES |
| `WP-08.01` | `DEVELOPER_GPT_NEW` | Generator DB 영향 Gate | 미착수 | 부분 확인 | 미실행 | 미실행 | YES |
| `WP-08.02` | `DEVELOPER_GPT_NEW` | Scratch fresh generate | 미착수 | 부분 확인 | 미실행 | 미실행 | YES |
| `WP-08.03` | `DEVELOPER_GPT_NEW` | MBR/EXS regenerate parity | 미착수 | 부분 확인 | 미실행 | 미실행 | YES |
| `WP-08.04` | `DEVELOPER_GPT_NEW` | Approval OpenAPI Generated Client | 미착수 | 부분 확인 | 미실행 | 미실행 | YES |
| `WP-08.05` | `DEVELOPER_GPT_NEW` | Frontend static contract | 미착수 | 부분 확인 | 미실행 | 미실행 | YES |
| `WP-08.06` | `DEVELOPER_GPT_NEW` | Browser Runtime matrix | 미착수 | 부분 확인 | 미실행 | 미실행 | YES |
| `WP-09.01` | `FINAL_GATE_NEW` | Source Identity tool 보정 | 미완료 | FAIL | 미실행 | 미실행 | YES |
| `WP-09.02` | `FINAL_GATE_NEW` | 200자 path Gate | 미완료 | FAIL | 미실행 | 미실행 | YES |
| `WP-09.03` | `FINAL_GATE_NEW` | nested cpf-docs/project-cache 정리 | 미완료 | FAIL | 미실행 | 미실행 | YES |
| `WP-09.04` | `FINAL_GATE_NEW` | Codex 보호자료 충돌 처리 | 미완료 | FAIL | 미실행 | 미실행 | YES |
| `WP-09.05` | `FINAL_GATE_NEW` | UTF-8/파일명 Gate | 미완료 | FAIL | 미실행 | 미실행 | YES |
| `WP-09.06` | `FINAL_GATE_NEW` | Canonical Final Gate | 미완료 | FAIL | 미실행 | 미실행 | YES |
| `WP-09.07` | `FINAL_GATE_NEW` | Fresh Replay/최종 ZIP | 미완료 | FAIL | 미실행 | 미실행 | YES |

## 5. 상세 실행 원장 — 기존 105개 작업카드

### WP-00.01 — Developer GPT/Codex 문서 경계 고정 — Codex 전용 CODEX_* 및 evidence/codex/current 읽기 전용, Developer GPT 전용 원장 분리

- **Origin / 분류:** `GOVERNANCE`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `QA 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `해당없음`
- **Codex 재검수 필요:** `NO`
- **현재 Source QA 사실:** QA에서 운영/정본 관리 항목으로 확인되었으며 개발 착수 후 실제 Repository 기준으로 현행화한다.
- **실제 호출/Consumer 경로:** `Governance → Developer GPT Inventory → Source/Consumer/Test/Runtime/Evidence → Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-docs/work/current/CPF_DEVELOPMENT_HANDOVER.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_QA_DEVELOPMENT_REVIEW.md (신규 정식 관리 대상)`
  - `cpf-docs/work/evidence/developer-gpt/current/** (Developer GPT 전용 Evidence)`
- **계획된 구체 개발내용:** 운영 정본과 Developer GPT 관리문서에서 이 규칙을 명문화하고, Codex 보호영역의 write/delete를 금지하는 검증 항목을 추가한다. 구체 실행 목표: Codex 전용 CODEX_* 및 evidence/codex/current 읽기 전용, Developer GPT 전용 원장 분리

- **개발 상세 단계:**
  1. `Developer GPT/Codex 문서 경계 고정`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `Codex 전용 CODEX_* 및 evidence/codex/current 읽기 전용, Developer GPT 전용 원장 분리` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Codex 원장 오염, 역할별 상태 충돌, Handover 누락
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** 문서 보호경로 diff 0, Inventory schema/상태값 validation, Handover reference validation
- **로컬 Runtime 검증 계획:** 직접 Runtime 대상 없음. 단 관리규칙이 실제 로컬 통합검증 명령/Evidence 흐름에 반영됐는지 Final Gate에서 확인.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-00-01/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-00.02 — 정식 작업문서 등록 — 본 리뷰를 Developer GPT 공식 작업/QA/Closure 문서로 관리하고 개발 착수 후 governance/handover에서 참조

- **Origin / 분류:** `GOVERNANCE`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `QA 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `해당없음`
- **Codex 재검수 필요:** `NO`
- **현재 Source QA 사실:** QA에서 운영/정본 관리 항목으로 확인되었으며 개발 착수 후 실제 Repository 기준으로 현행화한다.
- **실제 호출/Consumer 경로:** `Governance → Developer GPT Inventory → Source/Consumer/Test/Runtime/Evidence → Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-docs/work/current/CPF_DEVELOPMENT_HANDOVER.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_QA_DEVELOPMENT_REVIEW.md (신규 정식 관리 대상)`
  - `cpf-docs/work/evidence/developer-gpt/current/** (Developer GPT 전용 Evidence)`
- **계획된 구체 개발내용:** 운영 정본과 Developer GPT 관리문서에서 이 규칙을 명문화하고, Codex 보호영역의 write/delete를 금지하는 검증 항목을 추가한다. 구체 실행 목표: 본 리뷰를 Developer GPT 공식 작업/QA/Closure 문서로 관리하고 개발 착수 후 governance/handover에서 참조

- **개발 상세 단계:**
  1. `정식 작업문서 등록`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `본 리뷰를 Developer GPT 공식 작업/QA/Closure 문서로 관리하고 개발 착수 후 governance/handover에서 참조` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Codex 원장 오염, 역할별 상태 충돌, Handover 누락
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** 문서 보호경로 diff 0, Inventory schema/상태값 validation, Handover reference validation
- **로컬 Runtime 검증 계획:** 직접 Runtime 대상 없음. 단 관리규칙이 실제 로컬 통합검증 명령/Evidence 흐름에 반영됐는지 Final Gate에서 확인.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-00-02/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-00.03 — 상태모델 고정 — 개발완료/정적검증완료/런타임검증완료/Codex검증완료를 독립 상태로 관리

- **Origin / 분류:** `GOVERNANCE`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `QA 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `해당없음`
- **Codex 재검수 필요:** `NO`
- **현재 Source QA 사실:** QA에서 운영/정본 관리 항목으로 확인되었으며 개발 착수 후 실제 Repository 기준으로 현행화한다.
- **실제 호출/Consumer 경로:** `Governance → Developer GPT Inventory → Source/Consumer/Test/Runtime/Evidence → Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-docs/work/current/CPF_DEVELOPMENT_HANDOVER.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_QA_DEVELOPMENT_REVIEW.md (신규 정식 관리 대상)`
  - `cpf-docs/work/evidence/developer-gpt/current/** (Developer GPT 전용 Evidence)`
- **계획된 구체 개발내용:** 운영 정본과 Developer GPT 관리문서에서 이 규칙을 명문화하고, Codex 보호영역의 write/delete를 금지하는 검증 항목을 추가한다. 구체 실행 목표: 개발완료/정적검증완료/런타임검증완료/Codex검증완료를 독립 상태로 관리

- **개발 상세 단계:**
  1. `상태모델 고정`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `개발완료/정적검증완료/런타임검증완료/Codex검증완료를 독립 상태로 관리` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Codex 원장 오염, 역할별 상태 충돌, Handover 누락
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** 문서 보호경로 diff 0, Inventory schema/상태값 validation, Handover reference validation
- **로컬 Runtime 검증 계획:** 직접 Runtime 대상 없음. 단 관리규칙이 실제 로컬 통합검증 명령/Evidence 흐름에 반영됐는지 Final Gate에서 확인.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-00-03/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-00.04 — upstream reference 규칙 — Codex Fxxx를 Developer GPT 항목에서 참조하되 Codex 상태를 대신 변경하지 않음

- **Origin / 분류:** `GOVERNANCE`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `QA 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `해당없음`
- **Codex 재검수 필요:** `NO`
- **현재 Source QA 사실:** QA에서 운영/정본 관리 항목으로 확인되었으며 개발 착수 후 실제 Repository 기준으로 현행화한다.
- **실제 호출/Consumer 경로:** `Governance → Developer GPT Inventory → Source/Consumer/Test/Runtime/Evidence → Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-docs/work/current/CPF_DEVELOPMENT_HANDOVER.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_QA_DEVELOPMENT_REVIEW.md (신규 정식 관리 대상)`
  - `cpf-docs/work/evidence/developer-gpt/current/** (Developer GPT 전용 Evidence)`
- **계획된 구체 개발내용:** 운영 정본과 Developer GPT 관리문서에서 이 규칙을 명문화하고, Codex 보호영역의 write/delete를 금지하는 검증 항목을 추가한다. 구체 실행 목표: Codex Fxxx를 Developer GPT 항목에서 참조하되 Codex 상태를 대신 변경하지 않음

- **개발 상세 단계:**
  1. `upstream reference 규칙`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `Codex Fxxx를 Developer GPT 항목에서 참조하되 Codex 상태를 대신 변경하지 않음` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Codex 원장 오염, 역할별 상태 충돌, Handover 누락
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** 문서 보호경로 diff 0, Inventory schema/상태값 validation, Handover reference validation
- **로컬 Runtime 검증 계획:** 직접 Runtime 대상 없음. 단 관리규칙이 실제 로컬 통합검증 명령/Evidence 흐름에 반영됐는지 Final Gate에서 확인.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-00-04/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-00.05 — Codex 재검수 대상 식별 — Developer GPT 신규/수정 영역은 codex_crosscheck_required=true로 관리

- **Origin / 분류:** `GOVERNANCE`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `QA 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `해당없음`
- **Codex 재검수 필요:** `NO`
- **현재 Source QA 사실:** QA에서 운영/정본 관리 항목으로 확인되었으며 개발 착수 후 실제 Repository 기준으로 현행화한다.
- **실제 호출/Consumer 경로:** `Governance → Developer GPT Inventory → Source/Consumer/Test/Runtime/Evidence → Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-docs/work/current/CPF_DEVELOPMENT_HANDOVER.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_QA_DEVELOPMENT_REVIEW.md (신규 정식 관리 대상)`
  - `cpf-docs/work/evidence/developer-gpt/current/** (Developer GPT 전용 Evidence)`
- **계획된 구체 개발내용:** 운영 정본과 Developer GPT 관리문서에서 이 규칙을 명문화하고, Codex 보호영역의 write/delete를 금지하는 검증 항목을 추가한다. 구체 실행 목표: Developer GPT 신규/수정 영역은 codex_crosscheck_required=true로 관리

- **개발 상세 단계:**
  1. `Codex 재검수 대상 식별`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `Developer GPT 신규/수정 영역은 codex_crosscheck_required=true로 관리` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Codex 원장 오염, 역할별 상태 충돌, Handover 누락
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** 문서 보호경로 diff 0, Inventory schema/상태값 validation, Handover reference validation
- **로컬 Runtime 검증 계획:** 직접 Runtime 대상 없음. 단 관리규칙이 실제 로컬 통합검증 명령/Evidence 흐름에 반영됐는지 Final Gate에서 확인.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-00-05/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-00.06 — Source Identity 기준 — 사용자 최신 ZIP SHA-256 + source-state + file manifest를 기준으로 관리

- **Origin / 분류:** `GOVERNANCE`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `QA 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `해당없음`
- **Codex 재검수 필요:** `NO`
- **현재 Source QA 사실:** QA에서 운영/정본 관리 항목으로 확인되었으며 개발 착수 후 실제 Repository 기준으로 현행화한다.
- **실제 호출/Consumer 경로:** `Governance → Developer GPT Inventory → Source/Consumer/Test/Runtime/Evidence → Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-docs/work/current/CPF_DEVELOPMENT_HANDOVER.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_QA_DEVELOPMENT_REVIEW.md (신규 정식 관리 대상)`
  - `cpf-docs/work/evidence/developer-gpt/current/** (Developer GPT 전용 Evidence)`
- **계획된 구체 개발내용:** 운영 정본과 Developer GPT 관리문서에서 이 규칙을 명문화하고, Codex 보호영역의 write/delete를 금지하는 검증 항목을 추가한다. 구체 실행 목표: 사용자 최신 ZIP SHA-256 + source-state + file manifest를 기준으로 관리

- **개발 상세 단계:**
  1. `cpf-source-state.py`의 include/exclude 규칙을 `.gitignore` 및 실제 배포 Source 범위와 비교한다.
  2. `logs/`, `*.log`, runtime temp/cache가 Product Source Identity에 포함되는지 재현한다.
  3. Git 의존 없이 tracked+untracked non-ignored에 준하는 canonical source scope를 정의하고 도구를 보정한다.
  4. 보정 전/후 file count/bytes/hash 차이를 Evidence로 남기고 실제 Source 파일이 빠지지 않았는지 manifest diff한다.
  5. 최종 Overlay/Fresh Replay/Package Manifest가 동일 Source Identity를 사용하도록 단일 함수로 연결한다.
- **Side Effect / 회귀 필수범위:** Codex 원장 오염, 역할별 상태 충돌, Handover 누락
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** 문서 보호경로 diff 0, Inventory schema/상태값 validation, Handover reference validation
- **로컬 Runtime 검증 계획:** 직접 Runtime 대상 없음. 단 관리규칙이 실제 로컬 통합검증 명령/Evidence 흐름에 반영됐는지 Final Gate에서 확인.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-00-06/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-00.07 — Root Cause 작업원칙 — Requirement→Owner→Source→Consumer→Test→Runtime→Evidence를 하나의 WP로 관리

- **Origin / 분류:** `GOVERNANCE`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `QA 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `해당없음`
- **Codex 재검수 필요:** `NO`
- **현재 Source QA 사실:** QA에서 운영/정본 관리 항목으로 확인되었으며 개발 착수 후 실제 Repository 기준으로 현행화한다.
- **실제 호출/Consumer 경로:** `Governance → Developer GPT Inventory → Source/Consumer/Test/Runtime/Evidence → Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-docs/work/current/CPF_DEVELOPMENT_HANDOVER.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_QA_DEVELOPMENT_REVIEW.md (신규 정식 관리 대상)`
  - `cpf-docs/work/evidence/developer-gpt/current/** (Developer GPT 전용 Evidence)`
- **계획된 구체 개발내용:** 운영 정본과 Developer GPT 관리문서에서 이 규칙을 명문화하고, Codex 보호영역의 write/delete를 금지하는 검증 항목을 추가한다. 구체 실행 목표: Requirement→Owner→Source→Consumer→Test→Runtime→Evidence를 하나의 WP로 관리

- **개발 상세 단계:**
  1. `Root Cause 작업원칙`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `Requirement→Owner→Source→Consumer→Test→Runtime→Evidence를 하나의 WP로 관리` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Codex 원장 오염, 역할별 상태 충돌, Handover 누락
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** 문서 보호경로 diff 0, Inventory schema/상태값 validation, Handover reference validation
- **로컬 Runtime 검증 계획:** 직접 Runtime 대상 없음. 단 관리규칙이 실제 로컬 통합검증 명령/Evidence 흐름에 반영됐는지 Final Gate에서 확인.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-00-07/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-00.08 — Runtime Closure 규칙 — Developer GPT 수정 영향은 로컬 Runtime PASS 전 CLOSED 금지

- **Origin / 분류:** `GOVERNANCE`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `QA 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `해당없음`
- **Codex 재검수 필요:** `NO`
- **현재 Source QA 사실:** QA에서 운영/정본 관리 항목으로 확인되었으며 개발 착수 후 실제 Repository 기준으로 현행화한다.
- **실제 호출/Consumer 경로:** `Governance → Developer GPT Inventory → Source/Consumer/Test/Runtime/Evidence → Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-docs/work/current/CPF_DEVELOPMENT_HANDOVER.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_QA_DEVELOPMENT_REVIEW.md (신규 정식 관리 대상)`
  - `cpf-docs/work/evidence/developer-gpt/current/** (Developer GPT 전용 Evidence)`
- **계획된 구체 개발내용:** 운영 정본과 Developer GPT 관리문서에서 이 규칙을 명문화하고, Codex 보호영역의 write/delete를 금지하는 검증 항목을 추가한다. 구체 실행 목표: Developer GPT 수정 영향은 로컬 Runtime PASS 전 CLOSED 금지

- **개발 상세 단계:**
  1. `Runtime Closure 규칙`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `Developer GPT 수정 영향은 로컬 Runtime PASS 전 CLOSED 금지` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Codex 원장 오염, 역할별 상태 충돌, Handover 누락
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** 문서 보호경로 diff 0, Inventory schema/상태값 validation, Handover reference validation
- **로컬 Runtime 검증 계획:** 직접 Runtime 대상 없음. 단 관리규칙이 실제 로컬 통합검증 명령/Evidence 흐름에 반영됐는지 Final Gate에서 확인.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-00-08/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-00.09 — DB3 비협상 규칙 — DB 영향 시 Oracle/PostgreSQL/MariaDB Fresh부터 Lifecycle 전체를 동일 WP에서 수행

- **Origin / 분류:** `GOVERNANCE`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `QA 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `해당없음`
- **Codex 재검수 필요:** `NO`
- **현재 Source QA 사실:** QA에서 운영/정본 관리 항목으로 확인되었으며 개발 착수 후 실제 Repository 기준으로 현행화한다.
- **실제 호출/Consumer 경로:** `Governance → Developer GPT Inventory → Source/Consumer/Test/Runtime/Evidence → Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-docs/work/current/CPF_DEVELOPMENT_HANDOVER.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_QA_DEVELOPMENT_REVIEW.md (신규 정식 관리 대상)`
  - `cpf-docs/work/evidence/developer-gpt/current/** (Developer GPT 전용 Evidence)`
- **계획된 구체 개발내용:** 운영 정본과 Developer GPT 관리문서에서 이 규칙을 명문화하고, Codex 보호영역의 write/delete를 금지하는 검증 항목을 추가한다. 구체 실행 목표: DB 영향 시 Oracle/PostgreSQL/MariaDB Fresh부터 Lifecycle 전체를 동일 WP에서 수행

- **개발 상세 단계:**
  1. `DB3 비협상 규칙`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `DB 영향 시 Oracle/PostgreSQL/MariaDB Fresh부터 Lifecycle 전체를 동일 WP에서 수행` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Codex 원장 오염, 역할별 상태 충돌, Handover 누락
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** 문서 보호경로 diff 0, Inventory schema/상태값 validation, Handover reference validation
- **로컬 Runtime 검증 계획:** 직접 Runtime 대상 없음. 단 관리규칙이 실제 로컬 통합검증 명령/Evidence 흐름에 반영됐는지 Final Gate에서 확인.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-00-09/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-00.10 — 최종 ZIP 승계 — 현행화된 본 리뷰/원장/Evidence/Handover를 최종 ZIP에 포함

- **Origin / 분류:** `GOVERNANCE`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `QA 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `해당없음`
- **Codex 재검수 필요:** `NO`
- **현재 Source QA 사실:** QA에서 운영/정본 관리 항목으로 확인되었으며 개발 착수 후 실제 Repository 기준으로 현행화한다.
- **실제 호출/Consumer 경로:** `Governance → Developer GPT Inventory → Source/Consumer/Test/Runtime/Evidence → Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-docs/work/current/CPF_DEVELOPMENT_HANDOVER.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`
  - `cpf-docs/work/current/CPF_DEVELOPER_GPT_QA_DEVELOPMENT_REVIEW.md (신규 정식 관리 대상)`
  - `cpf-docs/work/evidence/developer-gpt/current/** (Developer GPT 전용 Evidence)`
- **계획된 구체 개발내용:** 운영 정본과 Developer GPT 관리문서에서 이 규칙을 명문화하고, Codex 보호영역의 write/delete를 금지하는 검증 항목을 추가한다. 구체 실행 목표: 현행화된 본 리뷰/원장/Evidence/Handover를 최종 ZIP에 포함

- **개발 상세 단계:**
  1. `최종 ZIP 승계`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `현행화된 본 리뷰/원장/Evidence/Handover를 최종 ZIP에 포함` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Codex 원장 오염, 역할별 상태 충돌, Handover 누락
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** 문서 보호경로 diff 0, Inventory schema/상태값 validation, Handover reference validation
- **로컬 Runtime 검증 계획:** 직접 Runtime 대상 없음. 단 관리규칙이 실제 로컬 통합검증 명령/Evidence 흐름에 반영됐는지 Final Gate에서 확인.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-00-10/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-01.01 — F265 Center-Cut Kafka 직접결합 제거 확인 — center-cut source/classpath가 broker-neutral인지 확인하고 remote-kafka 독립 Adapter 경계 유지

- **Origin / 분류:** `CODEX_CROSSCHECK`
- **현재 개발상태:** `기존구현확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `우리측 미실행`
- **현재 Codex 검증:** `Codex CLOSED/PENDING 혼재`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex가 구현한 Center-Cut/Boot/Domain Invocation 기반이 현재 Source에 존재한다. `CenterCutTargetProvider`/`CenterCutHandler` concrete 구현과 `remote-kafka` 분리 구조를 확인했으며, 이 WP는 재개발보다 regression/cross-check가 목적이다.
- **실제 호출/Consumer 경로:** `Control Plane → CEC Runner/TargetProvider → DB Work Item → Worker Claim/Lease/Fencing → CenterCutHandler → CpfDomainClientRouter → Generated Domain Operation`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutTargetProvider.java`
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutHandler.java`
  - `cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/ParameterSnapshotCenterCutTargetProvider.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CpfDomainInvocationCenterCutHandler.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CenterCutWorkProcessor.java`
  - `cpf-batch/remote-kafka/**`
  - `cpf-batch/{control-plane,scheduler,worker,center-cut,agent}/**`
- **계획된 구체 개발내용:** 현재 Codex 구현을 보존한 상태에서 Source/Bean/classpath/Seed/실제 Consumer를 역추적하고, 결함이 확인될 때만 최소 Root Cause 범위를 수정한다. 구체 실행 목표: center-cut source/classpath가 broker-neutral인지 확인하고 remote-kafka 독립 Adapter 경계 유지

- **개발 상세 단계:**
  1. Center-Cut/공용 Runtime/remote-kafka의 Gradle dependency와 `runtimeClasspath`를 비교해 Kafka 구현이 전이되는 실제 경로를 확정한다.
  2. Center-Cut 전용 Topic/Listener/Reply/DLT/Consumer Group/Broker Control/Property/Starter의 실제 Consumer를 Repository-wide로 추적한다.
  3. 독립 Remote Transport Consumer가 있는 Kafka 코드는 `cpf-batch/remote-kafka` Owner에만 남기고 Center-Cut 직접결합은 제거한다.
  4. Control Plane/Scheduler/Worker의 선택형 Kafka 기능은 회귀하지 않도록 조건부 AutoConfiguration/Property/Bean activation을 검증한다.
  5. Kafka OFF 상태의 Center-Cut bootJar/runtimeClasspath와 실제 no-Kafka boot를 재검증한다.
- **Side Effect / 회귀 필수범위:** Center-Cut Kafka 선택형 Transport 회귀, 5 Executable boot 회귀, Generated Domain 호출 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** rg/source scan, Gradle targeted test, bootJar/runtimeClasspath inspection, Seed/Bean uniqueness verifier
- **로컬 Runtime 검증 계획:** Kafka OFF CEC boot + 5 Executable boot/registry + actual Target→DB Work→Worker→Domain Invocation smoke.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-01-01/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-01.02 — F274 TargetProvider concrete 구현 확인 — Parameter Snapshot에서 실제 Target/Request를 생성하는 concrete Consumer 확인

- **Origin / 분류:** `CODEX_CROSSCHECK`
- **현재 개발상태:** `기존구현확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `우리측 미실행`
- **현재 Codex 검증:** `Codex CLOSED/PENDING 혼재`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex가 구현한 Center-Cut/Boot/Domain Invocation 기반이 현재 Source에 존재한다. `CenterCutTargetProvider`/`CenterCutHandler` concrete 구현과 `remote-kafka` 분리 구조를 확인했으며, 이 WP는 재개발보다 regression/cross-check가 목적이다.
- **실제 호출/Consumer 경로:** `Control Plane → CEC Runner/TargetProvider → DB Work Item → Worker Claim/Lease/Fencing → CenterCutHandler → CpfDomainClientRouter → Generated Domain Operation`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutTargetProvider.java`
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutHandler.java`
  - `cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/ParameterSnapshotCenterCutTargetProvider.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CpfDomainInvocationCenterCutHandler.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CenterCutWorkProcessor.java`
  - `cpf-batch/remote-kafka/**`
  - `cpf-batch/{control-plane,scheduler,worker,center-cut,agent}/**`
- **계획된 구체 개발내용:** 현재 Codex 구현을 보존한 상태에서 Source/Bean/classpath/Seed/실제 Consumer를 역추적하고, 결함이 확인될 때만 최소 Root Cause 범위를 수정한다. 구체 실행 목표: Parameter Snapshot에서 실제 Target/Request를 생성하는 concrete Consumer 확인

- **개발 상세 단계:**
  1. `CenterCutTargetProvider` SPI의 입력/출력/페이징/멱등 계약을 먼저 읽고 Seed 이름만 맞춘 Dummy 구현 여부를 배제한다.
  2. `ParameterSnapshotCenterCutTargetProvider`가 실제 Parameter Snapshot에서 business key, request payload, target 식별자를 생성하는지 검증한다.
  3. Provider key/Seed/Bean 등록과 `CenterCutTargetGenerator` 실제 lookup 경로를 연결해 Consumer 0 상태가 없는지 확인한다.
  4. 빈 대상/중복 대상/잘못된 payload/대량 page 경계를 fail-closed 또는 명확한 상태로 처리하도록 테스트를 보강한다.
  5. Fresh Runtime에서 생성된 Target 수와 DB Work Item 수가 예상 대상과 일치하는지 확인한다.
- **Side Effect / 회귀 필수범위:** Center-Cut Kafka 선택형 Transport 회귀, 5 Executable boot 회귀, Generated Domain 호출 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** rg/source scan, Gradle targeted test, bootJar/runtimeClasspath inspection, Seed/Bean uniqueness verifier
- **로컬 Runtime 검증 계획:** Kafka OFF CEC boot + 5 Executable boot/registry + actual Target→DB Work→Worker→Domain Invocation smoke.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-01-02/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-01.03 — F274 Handler concrete 구현 확인 — CenterCutHandler가 CpfDomainClientRouter 공식 Invocation을 실제 호출하는지 확인

- **Origin / 분류:** `CODEX_CROSSCHECK`
- **현재 개발상태:** `기존구현확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `우리측 미실행`
- **현재 Codex 검증:** `Codex CLOSED/PENDING 혼재`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex가 구현한 Center-Cut/Boot/Domain Invocation 기반이 현재 Source에 존재한다. `CenterCutTargetProvider`/`CenterCutHandler` concrete 구현과 `remote-kafka` 분리 구조를 확인했으며, 이 WP는 재개발보다 regression/cross-check가 목적이다.
- **실제 호출/Consumer 경로:** `Control Plane → CEC Runner/TargetProvider → DB Work Item → Worker Claim/Lease/Fencing → CenterCutHandler → CpfDomainClientRouter → Generated Domain Operation`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutTargetProvider.java`
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutHandler.java`
  - `cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/ParameterSnapshotCenterCutTargetProvider.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CpfDomainInvocationCenterCutHandler.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CenterCutWorkProcessor.java`
  - `cpf-batch/remote-kafka/**`
  - `cpf-batch/{control-plane,scheduler,worker,center-cut,agent}/**`
- **계획된 구체 개발내용:** 현재 Codex 구현을 보존한 상태에서 Source/Bean/classpath/Seed/실제 Consumer를 역추적하고, 결함이 확인될 때만 최소 Root Cause 범위를 수정한다. 구체 실행 목표: CenterCutHandler가 CpfDomainClientRouter 공식 Invocation을 실제 호출하는지 확인

- **개발 상세 단계:**
  1. `CenterCutHandler` SPI 의미와 `CpfDomainInvocationCenterCutHandler` 구현의 request/result 계약을 대조한다.
  2. `CpfDomainClientRouter` 호출 시 target system, operationId, Header6, request payload가 canonical context에서 전달되는지 검증한다.
  3. 성공/업무실패/HTTP 오류/timeout/UNKNOWN 결과가 Work Item result와 Aggregate에 손실 없이 매핑되도록 구현·테스트한다.
  4. Mock/No-op client가 아닌 실제 Generated Domain canonical operation을 Consumer로 사용한다.
  5. No-Kafka Fresh E2E에서 Handler가 실제 MBR/EXS 업무 DB side effect까지 연결되는지 확인한다.
- **Side Effect / 회귀 필수범위:** Center-Cut Kafka 선택형 Transport 회귀, 5 Executable boot 회귀, Generated Domain 호출 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** rg/source scan, Gradle targeted test, bootJar/runtimeClasspath inspection, Seed/Bean uniqueness verifier
- **로컬 Runtime 검증 계획:** Kafka OFF CEC boot + 5 Executable boot/registry + actual Target→DB Work→Worker→Domain Invocation smoke.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-01-03/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-01.04 — Center-Cut Seed→Provider/Handler 연결 확인 — Seed key가 실제 Bean/Consumer와 일치하는지 검증

- **Origin / 분류:** `CODEX_CROSSCHECK`
- **현재 개발상태:** `기존구현확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `우리측 미실행`
- **현재 Codex 검증:** `Codex CLOSED/PENDING 혼재`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex가 구현한 Center-Cut/Boot/Domain Invocation 기반이 현재 Source에 존재한다. `CenterCutTargetProvider`/`CenterCutHandler` concrete 구현과 `remote-kafka` 분리 구조를 확인했으며, 이 WP는 재개발보다 regression/cross-check가 목적이다.
- **실제 호출/Consumer 경로:** `Control Plane → CEC Runner/TargetProvider → DB Work Item → Worker Claim/Lease/Fencing → CenterCutHandler → CpfDomainClientRouter → Generated Domain Operation`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutTargetProvider.java`
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutHandler.java`
  - `cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/ParameterSnapshotCenterCutTargetProvider.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CpfDomainInvocationCenterCutHandler.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CenterCutWorkProcessor.java`
  - `cpf-batch/remote-kafka/**`
  - `cpf-batch/{control-plane,scheduler,worker,center-cut,agent}/**`
- **계획된 구체 개발내용:** 현재 Codex 구현을 보존한 상태에서 Source/Bean/classpath/Seed/실제 Consumer를 역추적하고, 결함이 확인될 때만 최소 Root Cause 범위를 수정한다. 구체 실행 목표: Seed key가 실제 Bean/Consumer와 일치하는지 검증

- **개발 상세 단계:**
  1. `CenterCutHandler` SPI 의미와 `CpfDomainInvocationCenterCutHandler` 구현의 request/result 계약을 대조한다.
  2. `CpfDomainClientRouter` 호출 시 target system, operationId, Header6, request payload가 canonical context에서 전달되는지 검증한다.
  3. 성공/업무실패/HTTP 오류/timeout/UNKNOWN 결과가 Work Item result와 Aggregate에 손실 없이 매핑되도록 구현·테스트한다.
  4. Mock/No-op client가 아닌 실제 Generated Domain canonical operation을 Consumer로 사용한다.
  5. No-Kafka Fresh E2E에서 Handler가 실제 MBR/EXS 업무 DB side effect까지 연결되는지 확인한다.
- **Side Effect / 회귀 필수범위:** Center-Cut Kafka 선택형 Transport 회귀, 5 Executable boot 회귀, Generated Domain 호출 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** rg/source scan, Gradle targeted test, bootJar/runtimeClasspath inspection, Seed/Bean uniqueness verifier
- **로컬 Runtime 검증 계획:** Kafka OFF CEC boot + 5 Executable boot/registry + actual Target→DB Work→Worker→Domain Invocation smoke.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-01-04/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-01.05 — DB Work/Claim/Lease/Fencing 경로 확인 — CEC/Worker가 메모리가 아닌 DB work item을 실제 Consumer로 사용하는지 확인

- **Origin / 분류:** `CODEX_CROSSCHECK`
- **현재 개발상태:** `기존구현확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `우리측 미실행`
- **현재 Codex 검증:** `Codex CLOSED/PENDING 혼재`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex가 구현한 Center-Cut/Boot/Domain Invocation 기반이 현재 Source에 존재한다. `CenterCutTargetProvider`/`CenterCutHandler` concrete 구현과 `remote-kafka` 분리 구조를 확인했으며, 이 WP는 재개발보다 regression/cross-check가 목적이다.
- **실제 호출/Consumer 경로:** `Control Plane → CEC Runner/TargetProvider → DB Work Item → Worker Claim/Lease/Fencing → CenterCutHandler → CpfDomainClientRouter → Generated Domain Operation`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutTargetProvider.java`
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutHandler.java`
  - `cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/ParameterSnapshotCenterCutTargetProvider.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CpfDomainInvocationCenterCutHandler.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CenterCutWorkProcessor.java`
  - `cpf-batch/remote-kafka/**`
  - `cpf-batch/{control-plane,scheduler,worker,center-cut,agent}/**`
- **계획된 구체 개발내용:** 현재 Codex 구현을 보존한 상태에서 Source/Bean/classpath/Seed/실제 Consumer를 역추적하고, 결함이 확인될 때만 최소 Root Cause 범위를 수정한다. 구체 실행 목표: CEC/Worker가 메모리가 아닌 DB work item을 실제 Consumer로 사용하는지 확인

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `DB Work/Claim/Lease/Fencing 경로 확인` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** Center-Cut Kafka 선택형 Transport 회귀, 5 Executable boot 회귀, Generated Domain 호출 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** rg/source scan, Gradle targeted test, bootJar/runtimeClasspath inspection, Seed/Bean uniqueness verifier
- **로컬 Runtime 검증 계획:** Kafka OFF CEC boot + 5 Executable boot/registry + actual Target→DB Work→Worker→Domain Invocation smoke.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-01-05/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-01.06 — Header6/CEC Identity 연결 확인 — CEC System/Channel/Context가 공식 Invocation까지 이어지는지 확인

- **Origin / 분류:** `CODEX_CROSSCHECK`
- **현재 개발상태:** `기존구현확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `우리측 미실행`
- **현재 Codex 검증:** `Codex CLOSED/PENDING 혼재`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex가 구현한 Center-Cut/Boot/Domain Invocation 기반이 현재 Source에 존재한다. `CenterCutTargetProvider`/`CenterCutHandler` concrete 구현과 `remote-kafka` 분리 구조를 확인했으며, 이 WP는 재개발보다 regression/cross-check가 목적이다.
- **실제 호출/Consumer 경로:** `Control Plane → CEC Runner/TargetProvider → DB Work Item → Worker Claim/Lease/Fencing → CenterCutHandler → CpfDomainClientRouter → Generated Domain Operation`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutTargetProvider.java`
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutHandler.java`
  - `cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/ParameterSnapshotCenterCutTargetProvider.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CpfDomainInvocationCenterCutHandler.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CenterCutWorkProcessor.java`
  - `cpf-batch/remote-kafka/**`
  - `cpf-batch/{control-plane,scheduler,worker,center-cut,agent}/**`
- **계획된 구체 개발내용:** 현재 Codex 구현을 보존한 상태에서 Source/Bean/classpath/Seed/실제 Consumer를 역추적하고, 결함이 확인될 때만 최소 Root Cause 범위를 수정한다. 구체 실행 목표: CEC System/Channel/Context가 공식 Invocation까지 이어지는지 확인

- **개발 상세 단계:**
  1. `Header6/CEC Identity 연결 확인`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `CEC System/Channel/Context가 공식 Invocation까지 이어지는지 확인` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Center-Cut Kafka 선택형 Transport 회귀, 5 Executable boot 회귀, Generated Domain 호출 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** rg/source scan, Gradle targeted test, bootJar/runtimeClasspath inspection, Seed/Bean uniqueness verifier
- **로컬 Runtime 검증 계획:** Kafka OFF CEC boot + 5 Executable boot/registry + actual Target→DB Work→Worker→Domain Invocation smoke.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-01-06/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-01.07 — Generated MBR sample canonical operation 확인 — 임시 centerCutTest가 아닌 Generator sample operation 사용 여부 확인

- **Origin / 분류:** `CODEX_CROSSCHECK`
- **현재 개발상태:** `기존구현확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `우리측 미실행`
- **현재 Codex 검증:** `Codex CLOSED/PENDING 혼재`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex가 구현한 Center-Cut/Boot/Domain Invocation 기반이 현재 Source에 존재한다. `CenterCutTargetProvider`/`CenterCutHandler` concrete 구현과 `remote-kafka` 분리 구조를 확인했으며, 이 WP는 재개발보다 regression/cross-check가 목적이다.
- **실제 호출/Consumer 경로:** `Control Plane → CEC Runner/TargetProvider → DB Work Item → Worker Claim/Lease/Fencing → CenterCutHandler → CpfDomainClientRouter → Generated Domain Operation`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutTargetProvider.java`
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutHandler.java`
  - `cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/ParameterSnapshotCenterCutTargetProvider.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CpfDomainInvocationCenterCutHandler.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CenterCutWorkProcessor.java`
  - `cpf-batch/remote-kafka/**`
  - `cpf-batch/{control-plane,scheduler,worker,center-cut,agent}/**`
- **계획된 구체 개발내용:** 현재 Codex 구현을 보존한 상태에서 Source/Bean/classpath/Seed/실제 Consumer를 역추적하고, 결함이 확인될 때만 최소 Root Cause 범위를 수정한다. 구체 실행 목표: 임시 centerCutTest가 아닌 Generator sample operation 사용 여부 확인

- **개발 상세 단계:**
  1. `Generated MBR sample canonical operation 확인`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `임시 centerCutTest가 아닌 Generator sample operation 사용 여부 확인` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Center-Cut Kafka 선택형 Transport 회귀, 5 Executable boot 회귀, Generated Domain 호출 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** rg/source scan, Gradle targeted test, bootJar/runtimeClasspath inspection, Seed/Bean uniqueness verifier
- **로컬 Runtime 검증 계획:** Kafka OFF CEC boot + 5 Executable boot/registry + actual Target→DB Work→Worker→Domain Invocation smoke.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-01-07/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-01.08 — F275 Boot5 공통 실행체 확인 — Control Plane/Scheduler/Worker/CEC/Agent 실행 JAR/DB3/Registry wiring 확인

- **Origin / 분류:** `CODEX_CROSSCHECK`
- **현재 개발상태:** `기존구현확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `우리측 미실행`
- **현재 Codex 검증:** `Codex CLOSED/PENDING 혼재`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex가 구현한 Center-Cut/Boot/Domain Invocation 기반이 현재 Source에 존재한다. `CenterCutTargetProvider`/`CenterCutHandler` concrete 구현과 `remote-kafka` 분리 구조를 확인했으며, 이 WP는 재개발보다 regression/cross-check가 목적이다.
- **실제 호출/Consumer 경로:** `Control Plane → CEC Runner/TargetProvider → DB Work Item → Worker Claim/Lease/Fencing → CenterCutHandler → CpfDomainClientRouter → Generated Domain Operation`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutTargetProvider.java`
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutHandler.java`
  - `cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/ParameterSnapshotCenterCutTargetProvider.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CpfDomainInvocationCenterCutHandler.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CenterCutWorkProcessor.java`
  - `cpf-batch/remote-kafka/**`
  - `cpf-batch/{control-plane,scheduler,worker,center-cut,agent}/**`
- **계획된 구체 개발내용:** 현재 Codex 구현을 보존한 상태에서 Source/Bean/classpath/Seed/실제 Consumer를 역추적하고, 결함이 확인될 때만 최소 Root Cause 범위를 수정한다. 구체 실행 목표: Control Plane/Scheduler/Worker/CEC/Agent 실행 JAR/DB3/Registry wiring 확인

- **개발 상세 단계:**
  1. Control Plane/Scheduler/Worker/Center-Cut Runner/Agent 5개 Main과 bootJar task를 inventory한다.
  2. 각 JAR의 Starter/Provider/DB3 driver/Secret provider/RuntimeStateProvider/ConfigurationProperties 포함 여부를 물리 검사한다.
  3. Bean uniqueness, AOP proxy 가능성, Clock/DataSource/TransactionManager ambiguity를 공통 Gate로 정적 차단한다.
  4. Fresh DB3에서 5개 JAR을 순차 기동해 liveness와 Runtime Registry `UP`을 확인한다.
  5. 실패 시 각 실행체별 최초 root exception, ExitCode, 로그 경로를 Evidence에 남기고 같은 Root Cause를 5개 전체에서 검색한다.
- **Side Effect / 회귀 필수범위:** Center-Cut Kafka 선택형 Transport 회귀, 5 Executable boot 회귀, Generated Domain 호출 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** rg/source scan, Gradle targeted test, bootJar/runtimeClasspath inspection, Seed/Bean uniqueness verifier
- **로컬 Runtime 검증 계획:** Kafka OFF CEC boot + 5 Executable boot/registry + actual Target→DB Work→Worker→Domain Invocation smoke.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-01-08/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-01.09 — F277~F281 공통 Boot 결함 회귀 확인 — optional Kafka, Agent DB, Secret/Ledger, RuntimeStateProvider 중복 재발 여부 확인

- **Origin / 분류:** `CODEX_CROSSCHECK`
- **현재 개발상태:** `기존구현확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `우리측 미실행`
- **현재 Codex 검증:** `Codex CLOSED/PENDING 혼재`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex가 구현한 Center-Cut/Boot/Domain Invocation 기반이 현재 Source에 존재한다. `CenterCutTargetProvider`/`CenterCutHandler` concrete 구현과 `remote-kafka` 분리 구조를 확인했으며, 이 WP는 재개발보다 regression/cross-check가 목적이다.
- **실제 호출/Consumer 경로:** `Control Plane → CEC Runner/TargetProvider → DB Work Item → Worker Claim/Lease/Fencing → CenterCutHandler → CpfDomainClientRouter → Generated Domain Operation`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutTargetProvider.java`
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutHandler.java`
  - `cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/ParameterSnapshotCenterCutTargetProvider.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CpfDomainInvocationCenterCutHandler.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CenterCutWorkProcessor.java`
  - `cpf-batch/remote-kafka/**`
  - `cpf-batch/{control-plane,scheduler,worker,center-cut,agent}/**`
- **계획된 구체 개발내용:** 현재 Codex 구현을 보존한 상태에서 Source/Bean/classpath/Seed/실제 Consumer를 역추적하고, 결함이 확인될 때만 최소 Root Cause 범위를 수정한다. 구체 실행 목표: optional Kafka, Agent DB, Secret/Ledger, RuntimeStateProvider 중복 재발 여부 확인

- **개발 상세 단계:**
  1. Center-Cut/공용 Runtime/remote-kafka의 Gradle dependency와 `runtimeClasspath`를 비교해 Kafka 구현이 전이되는 실제 경로를 확정한다.
  2. Center-Cut 전용 Topic/Listener/Reply/DLT/Consumer Group/Broker Control/Property/Starter의 실제 Consumer를 Repository-wide로 추적한다.
  3. 독립 Remote Transport Consumer가 있는 Kafka 코드는 `cpf-batch/remote-kafka` Owner에만 남기고 Center-Cut 직접결합은 제거한다.
  4. Control Plane/Scheduler/Worker의 선택형 Kafka 기능은 회귀하지 않도록 조건부 AutoConfiguration/Property/Bean activation을 검증한다.
  5. Kafka OFF 상태의 Center-Cut bootJar/runtimeClasspath와 실제 no-Kafka boot를 재검증한다.
- **Side Effect / 회귀 필수범위:** Center-Cut Kafka 선택형 Transport 회귀, 5 Executable boot 회귀, Generated Domain 호출 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** rg/source scan, Gradle targeted test, bootJar/runtimeClasspath inspection, Seed/Bean uniqueness verifier
- **로컬 Runtime 검증 계획:** Kafka OFF CEC boot + 5 Executable boot/registry + actual Target→DB Work→Worker→Domain Invocation smoke.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-01-09/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-01.10 — F293~F299 실제 Domain Invocation 보완 확인 — Runtime state, HTTP, Security, MVC, Policy, TxManager 변경 Source 확인

- **Origin / 분류:** `CODEX_CROSSCHECK`
- **현재 개발상태:** `기존구현확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `우리측 미실행`
- **현재 Codex 검증:** `Codex CLOSED/PENDING 혼재`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex가 구현한 Center-Cut/Boot/Domain Invocation 기반이 현재 Source에 존재한다. `CenterCutTargetProvider`/`CenterCutHandler` concrete 구현과 `remote-kafka` 분리 구조를 확인했으며, 이 WP는 재개발보다 regression/cross-check가 목적이다.
- **실제 호출/Consumer 경로:** `Control Plane → CEC Runner/TargetProvider → DB Work Item → Worker Claim/Lease/Fencing → CenterCutHandler → CpfDomainClientRouter → Generated Domain Operation`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutTargetProvider.java`
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutHandler.java`
  - `cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/ParameterSnapshotCenterCutTargetProvider.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CpfDomainInvocationCenterCutHandler.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CenterCutWorkProcessor.java`
  - `cpf-batch/remote-kafka/**`
  - `cpf-batch/{control-plane,scheduler,worker,center-cut,agent}/**`
- **계획된 구체 개발내용:** 현재 Codex 구현을 보존한 상태에서 Source/Bean/classpath/Seed/실제 Consumer를 역추적하고, 결함이 확인될 때만 최소 Root Cause 범위를 수정한다. 구체 실행 목표: Runtime state, HTTP, Security, MVC, Policy, TxManager 변경 Source 확인

- **개발 상세 단계:**
  1. `F293~F299 실제 Domain Invocation 보완 확인`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `Runtime state, HTTP, Security, MVC, Policy, TxManager 변경 Source 확인` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Center-Cut Kafka 선택형 Transport 회귀, 5 Executable boot 회귀, Generated Domain 호출 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** rg/source scan, Gradle targeted test, bootJar/runtimeClasspath inspection, Seed/Bean uniqueness verifier
- **로컬 Runtime 검증 계획:** Kafka OFF CEC boot + 5 Executable boot/registry + actual Target→DB Work→Worker→Domain Invocation smoke.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-01-10/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-01.11 — Codex CLOSED 영역 최소 재현 Gate — Codex 완료영역을 저비용 Source/Verifier로 독립 cross-check

- **Origin / 분류:** `CODEX_CROSSCHECK`
- **현재 개발상태:** `기존구현확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `우리측 미실행`
- **현재 Codex 검증:** `Codex CLOSED/PENDING 혼재`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex가 구현한 Center-Cut/Boot/Domain Invocation 기반이 현재 Source에 존재한다. `CenterCutTargetProvider`/`CenterCutHandler` concrete 구현과 `remote-kafka` 분리 구조를 확인했으며, 이 WP는 재개발보다 regression/cross-check가 목적이다.
- **실제 호출/Consumer 경로:** `Control Plane → CEC Runner/TargetProvider → DB Work Item → Worker Claim/Lease/Fencing → CenterCutHandler → CpfDomainClientRouter → Generated Domain Operation`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutTargetProvider.java`
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutHandler.java`
  - `cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/ParameterSnapshotCenterCutTargetProvider.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CpfDomainInvocationCenterCutHandler.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CenterCutWorkProcessor.java`
  - `cpf-batch/remote-kafka/**`
  - `cpf-batch/{control-plane,scheduler,worker,center-cut,agent}/**`
- **계획된 구체 개발내용:** 현재 Codex 구현을 보존한 상태에서 Source/Bean/classpath/Seed/실제 Consumer를 역추적하고, 결함이 확인될 때만 최소 Root Cause 범위를 수정한다. 구체 실행 목표: Codex 완료영역을 저비용 Source/Verifier로 독립 cross-check

- **개발 상세 단계:**
  1. `Codex CLOSED 영역 최소 재현 Gate`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `Codex 완료영역을 저비용 Source/Verifier로 독립 cross-check` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Center-Cut Kafka 선택형 Transport 회귀, 5 Executable boot 회귀, Generated Domain 호출 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** rg/source scan, Gradle targeted test, bootJar/runtimeClasspath inspection, Seed/Bean uniqueness verifier
- **로컬 Runtime 검증 계획:** Kafka OFF CEC boot + 5 Executable boot/registry + actual Target→DB Work→Worker→Domain Invocation smoke.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-01-11/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-01.12 — Codex PENDING 영역 재개발 금지/영향재검증 — 이미 구현된 Source를 처음부터 재작성하지 않고 남은 Closure만 이어감

- **Origin / 분류:** `CODEX_CROSSCHECK`
- **현재 개발상태:** `기존구현확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `우리측 미실행`
- **현재 Codex 검증:** `Codex CLOSED/PENDING 혼재`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex가 구현한 Center-Cut/Boot/Domain Invocation 기반이 현재 Source에 존재한다. `CenterCutTargetProvider`/`CenterCutHandler` concrete 구현과 `remote-kafka` 분리 구조를 확인했으며, 이 WP는 재개발보다 regression/cross-check가 목적이다.
- **실제 호출/Consumer 경로:** `Control Plane → CEC Runner/TargetProvider → DB Work Item → Worker Claim/Lease/Fencing → CenterCutHandler → CpfDomainClientRouter → Generated Domain Operation`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutTargetProvider.java`
  - `cpf-batch/api/src/main/java/com/cpf/batch/spi/CenterCutHandler.java`
  - `cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/ParameterSnapshotCenterCutTargetProvider.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CpfDomainInvocationCenterCutHandler.java`
  - `cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/CenterCutWorkProcessor.java`
  - `cpf-batch/remote-kafka/**`
  - `cpf-batch/{control-plane,scheduler,worker,center-cut,agent}/**`
- **계획된 구체 개발내용:** 현재 Codex 구현을 보존한 상태에서 Source/Bean/classpath/Seed/실제 Consumer를 역추적하고, 결함이 확인될 때만 최소 Root Cause 범위를 수정한다. 구체 실행 목표: 이미 구현된 Source를 처음부터 재작성하지 않고 남은 Closure만 이어감

- **개발 상세 단계:**
  1. `Codex PENDING 영역 재개발 금지/영향재검증`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `이미 구현된 Source를 처음부터 재작성하지 않고 남은 Closure만 이어감` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Center-Cut Kafka 선택형 Transport 회귀, 5 Executable boot 회귀, Generated Domain 호출 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** rg/source scan, Gradle targeted test, bootJar/runtimeClasspath inspection, Seed/Bean uniqueness verifier
- **로컬 Runtime 검증 계획:** Kafka OFF CEC boot + 5 Executable boot/registry + actual Target→DB Work→Worker→Domain Invocation smoke.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-01-12/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-02.01 — F304 E2E27 최신 Segment INSERT 오류 확보 — Codex 종료 직전 실제 MBR 오류/DB 상태를 동일 Source에서 재현

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `Source Fixed 확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `FAIL/미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex 후반 Source에는 F300~F304 Observability 보완이 실제 존재한다. F304는 DB3/정적/Java targeted 검증 후 Fresh E2E에서 Segment 물리 저장 0건이 재발해 `VERIFICATION_PENDING`에서 종료됐다. 따라서 Source를 처음부터 재작성하지 않고 실제 persistence 경계를 이어서 확인해야 한다.
- **실제 호출/Consumer 경로:** `BAT/CEC root CpfContext → Domain Invocation HTTP ingress → Generated Domain Operation boundary → Observability AutoConfiguration → TransactionLog/Segment Port → MyBatis/JDBC → CPF_TRANSACTION_* → ADM Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-starters/platform-operations/observability/**`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionLogMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionSegmentMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/**`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/runtime-template/cpf/vendor/{oracle,postgresql,mariadb}/mybatis/logging/*.xml.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/cpf/mybatis/logging/*.xml`
  - `cpf-tools/verification/tools/verify-cpf-integrated-logging-closure.py`
- **계획된 구체 개발내용:** F304의 최신 물리 실패를 재현해 Schema/Mapper/Writer/Context lifecycle 중 실제 Owner를 확정하고 DB3 canonical source부터 일괄 보정한다. 구체 실행 목표: Codex 종료 직전 실제 MBR 오류/DB 상태를 동일 Source에서 재현

- **개발 상세 단계:**
  1. Codex E2E27과 동일 Source에서 최신 MBR Segment INSERT 오류/DB 상태를 재현해 정확한 SQL statement와 bind 값을 확보한다.
  2. `platform-schema.json`의 `CPF_TRANSACTION_SEGMENT` 정의와 immutable V115, generated DB3 schema, mapper template/output을 필드 단위로 비교한다.
  3. `TransactionSegmentMapper`/adapter가 executionId, transactionId, parentSegmentId, System6, operationId를 동일 명칭·순서·타입으로 bind하는지 확인한다.
  4. Generated Domain operation boundary에서 Segment scope start/finish/failure가 실제로 호출되고 transaction context가 소실되지 않는지 추적한다.
  5. Oracle/PostgreSQL/MariaDB projection과 Runtime mapper를 재생성한 뒤 drift 0을 확인한다.
  6. Fresh MBR+Control Plane+CEC+Worker2 E2E에서 Summary/Segment/Lineage/Timeline이 동일 root transaction으로 물리 저장되는지 검증한다.
- **Side Effect / 회귀 필수범위:** Transaction Summary/Segment/Lineage/Timeline drift, DB3 Mapper drift, Header6/operationId/executionId 회귀
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB3 render/parity, MyBatis mapper test, integrated logging closure verifier, Java25 targeted observability tests
- **로컬 Runtime 검증 계획:** Fresh DB + MBR + Control Plane + CEC + Worker2 → 실제 업무 20건 상당 workload → Summary/Segment/Lineage/Timeline DB 물리 조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-02-01/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-02.02 — CPF_TRANSACTION_SEGMENT execution_id canonical 확인 — Canonical schema와 immutable V115 계약 일치 확인

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `Source Fixed 확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `FAIL/미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex 후반 Source에는 F300~F304 Observability 보완이 실제 존재한다. F304는 DB3/정적/Java targeted 검증 후 Fresh E2E에서 Segment 물리 저장 0건이 재발해 `VERIFICATION_PENDING`에서 종료됐다. 따라서 Source를 처음부터 재작성하지 않고 실제 persistence 경계를 이어서 확인해야 한다.
- **실제 호출/Consumer 경로:** `BAT/CEC root CpfContext → Domain Invocation HTTP ingress → Generated Domain Operation boundary → Observability AutoConfiguration → TransactionLog/Segment Port → MyBatis/JDBC → CPF_TRANSACTION_* → ADM Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-starters/platform-operations/observability/**`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionLogMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionSegmentMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/**`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/runtime-template/cpf/vendor/{oracle,postgresql,mariadb}/mybatis/logging/*.xml.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/cpf/mybatis/logging/*.xml`
  - `cpf-tools/verification/tools/verify-cpf-integrated-logging-closure.py`
- **계획된 구체 개발내용:** F304의 최신 물리 실패를 재현해 Schema/Mapper/Writer/Context lifecycle 중 실제 Owner를 확정하고 DB3 canonical source부터 일괄 보정한다. 구체 실행 목표: Canonical schema와 immutable V115 계약 일치 확인

- **개발 상세 단계:**
  1. Codex E2E27과 동일 Source에서 최신 MBR Segment INSERT 오류/DB 상태를 재현해 정확한 SQL statement와 bind 값을 확보한다.
  2. `platform-schema.json`의 `CPF_TRANSACTION_SEGMENT` 정의와 immutable V115, generated DB3 schema, mapper template/output을 필드 단위로 비교한다.
  3. `TransactionSegmentMapper`/adapter가 executionId, transactionId, parentSegmentId, System6, operationId를 동일 명칭·순서·타입으로 bind하는지 확인한다.
  4. Generated Domain operation boundary에서 Segment scope start/finish/failure가 실제로 호출되고 transaction context가 소실되지 않는지 추적한다.
  5. Oracle/PostgreSQL/MariaDB projection과 Runtime mapper를 재생성한 뒤 drift 0을 확인한다.
  6. Fresh MBR+Control Plane+CEC+Worker2 E2E에서 Summary/Segment/Lineage/Timeline이 동일 root transaction으로 물리 저장되는지 검증한다.
- **Side Effect / 회귀 필수범위:** Transaction Summary/Segment/Lineage/Timeline drift, DB3 Mapper drift, Header6/operationId/executionId 회귀
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB3 render/parity, MyBatis mapper test, integrated logging closure verifier, Java25 targeted observability tests
- **로컬 Runtime 검증 계획:** Fresh DB + MBR + Control Plane + CEC + Worker2 → 실제 업무 20건 상당 workload → Summary/Segment/Lineage/Timeline DB 물리 조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-02-02/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-02.03 — Segment execution index 확인 — ix_cpf_transaction_segment_execution 3 Vendor parity 확인

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `Source Fixed 확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `FAIL/미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex 후반 Source에는 F300~F304 Observability 보완이 실제 존재한다. F304는 DB3/정적/Java targeted 검증 후 Fresh E2E에서 Segment 물리 저장 0건이 재발해 `VERIFICATION_PENDING`에서 종료됐다. 따라서 Source를 처음부터 재작성하지 않고 실제 persistence 경계를 이어서 확인해야 한다.
- **실제 호출/Consumer 경로:** `BAT/CEC root CpfContext → Domain Invocation HTTP ingress → Generated Domain Operation boundary → Observability AutoConfiguration → TransactionLog/Segment Port → MyBatis/JDBC → CPF_TRANSACTION_* → ADM Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-starters/platform-operations/observability/**`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionLogMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionSegmentMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/**`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/runtime-template/cpf/vendor/{oracle,postgresql,mariadb}/mybatis/logging/*.xml.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/cpf/mybatis/logging/*.xml`
  - `cpf-tools/verification/tools/verify-cpf-integrated-logging-closure.py`
- **계획된 구체 개발내용:** F304의 최신 물리 실패를 재현해 Schema/Mapper/Writer/Context lifecycle 중 실제 Owner를 확정하고 DB3 canonical source부터 일괄 보정한다. 구체 실행 목표: ix_cpf_transaction_segment_execution 3 Vendor parity 확인

- **개발 상세 단계:**
  1. Codex E2E27과 동일 Source에서 최신 MBR Segment INSERT 오류/DB 상태를 재현해 정확한 SQL statement와 bind 값을 확보한다.
  2. `platform-schema.json`의 `CPF_TRANSACTION_SEGMENT` 정의와 immutable V115, generated DB3 schema, mapper template/output을 필드 단위로 비교한다.
  3. `TransactionSegmentMapper`/adapter가 executionId, transactionId, parentSegmentId, System6, operationId를 동일 명칭·순서·타입으로 bind하는지 확인한다.
  4. Generated Domain operation boundary에서 Segment scope start/finish/failure가 실제로 호출되고 transaction context가 소실되지 않는지 추적한다.
  5. Oracle/PostgreSQL/MariaDB projection과 Runtime mapper를 재생성한 뒤 drift 0을 확인한다.
  6. Fresh MBR+Control Plane+CEC+Worker2 E2E에서 Summary/Segment/Lineage/Timeline이 동일 root transaction으로 물리 저장되는지 검증한다.
- **Side Effect / 회귀 필수범위:** Transaction Summary/Segment/Lineage/Timeline drift, DB3 Mapper drift, Header6/operationId/executionId 회귀
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB3 render/parity, MyBatis mapper test, integrated logging closure verifier, Java25 targeted observability tests
- **로컬 Runtime 검증 계획:** Fresh DB + MBR + Control Plane + CEC + Worker2 → 실제 업무 20건 상당 workload → Summary/Segment/Lineage/Timeline DB 물리 조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-02-03/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-02.04 — Segment Mapper parameter/result contract 확인 — execution_id/transaction/System6/operation binding 전체 확인

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `Source Fixed 확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `FAIL/미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex 후반 Source에는 F300~F304 Observability 보완이 실제 존재한다. F304는 DB3/정적/Java targeted 검증 후 Fresh E2E에서 Segment 물리 저장 0건이 재발해 `VERIFICATION_PENDING`에서 종료됐다. 따라서 Source를 처음부터 재작성하지 않고 실제 persistence 경계를 이어서 확인해야 한다.
- **실제 호출/Consumer 경로:** `BAT/CEC root CpfContext → Domain Invocation HTTP ingress → Generated Domain Operation boundary → Observability AutoConfiguration → TransactionLog/Segment Port → MyBatis/JDBC → CPF_TRANSACTION_* → ADM Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-starters/platform-operations/observability/**`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionLogMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionSegmentMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/**`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/runtime-template/cpf/vendor/{oracle,postgresql,mariadb}/mybatis/logging/*.xml.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/cpf/mybatis/logging/*.xml`
  - `cpf-tools/verification/tools/verify-cpf-integrated-logging-closure.py`
- **계획된 구체 개발내용:** F304의 최신 물리 실패를 재현해 Schema/Mapper/Writer/Context lifecycle 중 실제 Owner를 확정하고 DB3 canonical source부터 일괄 보정한다. 구체 실행 목표: execution_id/transaction/System6/operation binding 전체 확인

- **개발 상세 단계:**
  1. Codex E2E27과 동일 Source에서 최신 MBR Segment INSERT 오류/DB 상태를 재현해 정확한 SQL statement와 bind 값을 확보한다.
  2. `platform-schema.json`의 `CPF_TRANSACTION_SEGMENT` 정의와 immutable V115, generated DB3 schema, mapper template/output을 필드 단위로 비교한다.
  3. `TransactionSegmentMapper`/adapter가 executionId, transactionId, parentSegmentId, System6, operationId를 동일 명칭·순서·타입으로 bind하는지 확인한다.
  4. Generated Domain operation boundary에서 Segment scope start/finish/failure가 실제로 호출되고 transaction context가 소실되지 않는지 추적한다.
  5. Oracle/PostgreSQL/MariaDB projection과 Runtime mapper를 재생성한 뒤 drift 0을 확인한다.
  6. Fresh MBR+Control Plane+CEC+Worker2 E2E에서 Summary/Segment/Lineage/Timeline이 동일 root transaction으로 물리 저장되는지 검증한다.
- **Side Effect / 회귀 필수범위:** Transaction Summary/Segment/Lineage/Timeline drift, DB3 Mapper drift, Header6/operationId/executionId 회귀
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB3 render/parity, MyBatis mapper test, integrated logging closure verifier, Java25 targeted observability tests
- **로컬 Runtime 검증 계획:** Fresh DB + MBR + Control Plane + CEC + Worker2 → 실제 업무 20건 상당 workload → Summary/Segment/Lineage/Timeline DB 물리 조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-02-04/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-02.05 — Segment persistence lifecycle 확인 — MAIN/INBOUND segment start/finish/failure 실제 persistence 확인

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `Source Fixed 확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `FAIL/미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex 후반 Source에는 F300~F304 Observability 보완이 실제 존재한다. F304는 DB3/정적/Java targeted 검증 후 Fresh E2E에서 Segment 물리 저장 0건이 재발해 `VERIFICATION_PENDING`에서 종료됐다. 따라서 Source를 처음부터 재작성하지 않고 실제 persistence 경계를 이어서 확인해야 한다.
- **실제 호출/Consumer 경로:** `BAT/CEC root CpfContext → Domain Invocation HTTP ingress → Generated Domain Operation boundary → Observability AutoConfiguration → TransactionLog/Segment Port → MyBatis/JDBC → CPF_TRANSACTION_* → ADM Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-starters/platform-operations/observability/**`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionLogMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionSegmentMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/**`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/runtime-template/cpf/vendor/{oracle,postgresql,mariadb}/mybatis/logging/*.xml.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/cpf/mybatis/logging/*.xml`
  - `cpf-tools/verification/tools/verify-cpf-integrated-logging-closure.py`
- **계획된 구체 개발내용:** F304의 최신 물리 실패를 재현해 Schema/Mapper/Writer/Context lifecycle 중 실제 Owner를 확정하고 DB3 canonical source부터 일괄 보정한다. 구체 실행 목표: MAIN/INBOUND segment start/finish/failure 실제 persistence 확인

- **개발 상세 단계:**
  1. Codex E2E27과 동일 Source에서 최신 MBR Segment INSERT 오류/DB 상태를 재현해 정확한 SQL statement와 bind 값을 확보한다.
  2. `platform-schema.json`의 `CPF_TRANSACTION_SEGMENT` 정의와 immutable V115, generated DB3 schema, mapper template/output을 필드 단위로 비교한다.
  3. `TransactionSegmentMapper`/adapter가 executionId, transactionId, parentSegmentId, System6, operationId를 동일 명칭·순서·타입으로 bind하는지 확인한다.
  4. Generated Domain operation boundary에서 Segment scope start/finish/failure가 실제로 호출되고 transaction context가 소실되지 않는지 추적한다.
  5. Oracle/PostgreSQL/MariaDB projection과 Runtime mapper를 재생성한 뒤 drift 0을 확인한다.
  6. Fresh MBR+Control Plane+CEC+Worker2 E2E에서 Summary/Segment/Lineage/Timeline이 동일 root transaction으로 물리 저장되는지 검증한다.
- **Side Effect / 회귀 필수범위:** Transaction Summary/Segment/Lineage/Timeline drift, DB3 Mapper drift, Header6/operationId/executionId 회귀
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB3 render/parity, MyBatis mapper test, integrated logging closure verifier, Java25 targeted observability tests
- **로컬 Runtime 검증 계획:** Fresh DB + MBR + Control Plane + CEC + Worker2 → 실제 업무 20건 상당 workload → Summary/Segment/Lineage/Timeline DB 물리 조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-02-05/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-02.06 — Summary→Segment root transaction 연결 — BAT root transactionId와 MBR summary/segment가 동일 lineage를 유지하는지 확인

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `Source Fixed 확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `FAIL/미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex 후반 Source에는 F300~F304 Observability 보완이 실제 존재한다. F304는 DB3/정적/Java targeted 검증 후 Fresh E2E에서 Segment 물리 저장 0건이 재발해 `VERIFICATION_PENDING`에서 종료됐다. 따라서 Source를 처음부터 재작성하지 않고 실제 persistence 경계를 이어서 확인해야 한다.
- **실제 호출/Consumer 경로:** `BAT/CEC root CpfContext → Domain Invocation HTTP ingress → Generated Domain Operation boundary → Observability AutoConfiguration → TransactionLog/Segment Port → MyBatis/JDBC → CPF_TRANSACTION_* → ADM Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-starters/platform-operations/observability/**`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionLogMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionSegmentMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/**`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/runtime-template/cpf/vendor/{oracle,postgresql,mariadb}/mybatis/logging/*.xml.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/cpf/mybatis/logging/*.xml`
  - `cpf-tools/verification/tools/verify-cpf-integrated-logging-closure.py`
- **계획된 구체 개발내용:** F304의 최신 물리 실패를 재현해 Schema/Mapper/Writer/Context lifecycle 중 실제 Owner를 확정하고 DB3 canonical source부터 일괄 보정한다. 구체 실행 목표: BAT root transactionId와 MBR summary/segment가 동일 lineage를 유지하는지 확인

- **개발 상세 단계:**
  1. Codex E2E27과 동일 Source에서 최신 MBR Segment INSERT 오류/DB 상태를 재현해 정확한 SQL statement와 bind 값을 확보한다.
  2. `platform-schema.json`의 `CPF_TRANSACTION_SEGMENT` 정의와 immutable V115, generated DB3 schema, mapper template/output을 필드 단위로 비교한다.
  3. `TransactionSegmentMapper`/adapter가 executionId, transactionId, parentSegmentId, System6, operationId를 동일 명칭·순서·타입으로 bind하는지 확인한다.
  4. Generated Domain operation boundary에서 Segment scope start/finish/failure가 실제로 호출되고 transaction context가 소실되지 않는지 추적한다.
  5. Oracle/PostgreSQL/MariaDB projection과 Runtime mapper를 재생성한 뒤 drift 0을 확인한다.
  6. Fresh MBR+Control Plane+CEC+Worker2 E2E에서 Summary/Segment/Lineage/Timeline이 동일 root transaction으로 물리 저장되는지 검증한다.
- **Side Effect / 회귀 필수범위:** Transaction Summary/Segment/Lineage/Timeline drift, DB3 Mapper drift, Header6/operationId/executionId 회귀
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB3 render/parity, MyBatis mapper test, integrated logging closure verifier, Java25 targeted observability tests
- **로컬 Runtime 검증 계획:** Fresh DB + MBR + Control Plane + CEC + Worker2 → 실제 업무 20건 상당 workload → Summary/Segment/Lineage/Timeline DB 물리 조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-02-06/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-02.07 — parentSegment/attempt/execution lineage 확인 — parent/child/attempt/execution 연결이 DB와 Timeline에서 동일한지 확인

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `Source Fixed 확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `FAIL/미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex 후반 Source에는 F300~F304 Observability 보완이 실제 존재한다. F304는 DB3/정적/Java targeted 검증 후 Fresh E2E에서 Segment 물리 저장 0건이 재발해 `VERIFICATION_PENDING`에서 종료됐다. 따라서 Source를 처음부터 재작성하지 않고 실제 persistence 경계를 이어서 확인해야 한다.
- **실제 호출/Consumer 경로:** `BAT/CEC root CpfContext → Domain Invocation HTTP ingress → Generated Domain Operation boundary → Observability AutoConfiguration → TransactionLog/Segment Port → MyBatis/JDBC → CPF_TRANSACTION_* → ADM Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-starters/platform-operations/observability/**`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionLogMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionSegmentMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/**`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/runtime-template/cpf/vendor/{oracle,postgresql,mariadb}/mybatis/logging/*.xml.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/cpf/mybatis/logging/*.xml`
  - `cpf-tools/verification/tools/verify-cpf-integrated-logging-closure.py`
- **계획된 구체 개발내용:** F304의 최신 물리 실패를 재현해 Schema/Mapper/Writer/Context lifecycle 중 실제 Owner를 확정하고 DB3 canonical source부터 일괄 보정한다. 구체 실행 목표: parent/child/attempt/execution 연결이 DB와 Timeline에서 동일한지 확인

- **개발 상세 단계:**
  1. Codex E2E27과 동일 Source에서 최신 MBR Segment INSERT 오류/DB 상태를 재현해 정확한 SQL statement와 bind 값을 확보한다.
  2. `platform-schema.json`의 `CPF_TRANSACTION_SEGMENT` 정의와 immutable V115, generated DB3 schema, mapper template/output을 필드 단위로 비교한다.
  3. `TransactionSegmentMapper`/adapter가 executionId, transactionId, parentSegmentId, System6, operationId를 동일 명칭·순서·타입으로 bind하는지 확인한다.
  4. Generated Domain operation boundary에서 Segment scope start/finish/failure가 실제로 호출되고 transaction context가 소실되지 않는지 추적한다.
  5. Oracle/PostgreSQL/MariaDB projection과 Runtime mapper를 재생성한 뒤 drift 0을 확인한다.
  6. Fresh MBR+Control Plane+CEC+Worker2 E2E에서 Summary/Segment/Lineage/Timeline이 동일 root transaction으로 물리 저장되는지 검증한다.
- **Side Effect / 회귀 필수범위:** Transaction Summary/Segment/Lineage/Timeline drift, DB3 Mapper drift, Header6/operationId/executionId 회귀
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB3 render/parity, MyBatis mapper test, integrated logging closure verifier, Java25 targeted observability tests
- **로컬 Runtime 검증 계획:** Fresh DB + MBR + Control Plane + CEC + Worker2 → 실제 업무 20건 상당 workload → Summary/Segment/Lineage/Timeline DB 물리 조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-02-07/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-02.08 — Timeline target_system_code 현행화 확인 — retired remote_system 사용 0 및 actual reader consumer 확인

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `Source Fixed 확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `FAIL/미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex 후반 Source에는 F300~F304 Observability 보완이 실제 존재한다. F304는 DB3/정적/Java targeted 검증 후 Fresh E2E에서 Segment 물리 저장 0건이 재발해 `VERIFICATION_PENDING`에서 종료됐다. 따라서 Source를 처음부터 재작성하지 않고 실제 persistence 경계를 이어서 확인해야 한다.
- **실제 호출/Consumer 경로:** `BAT/CEC root CpfContext → Domain Invocation HTTP ingress → Generated Domain Operation boundary → Observability AutoConfiguration → TransactionLog/Segment Port → MyBatis/JDBC → CPF_TRANSACTION_* → ADM Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-starters/platform-operations/observability/**`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionLogMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionSegmentMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/**`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/runtime-template/cpf/vendor/{oracle,postgresql,mariadb}/mybatis/logging/*.xml.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/cpf/mybatis/logging/*.xml`
  - `cpf-tools/verification/tools/verify-cpf-integrated-logging-closure.py`
- **계획된 구체 개발내용:** F304의 최신 물리 실패를 재현해 Schema/Mapper/Writer/Context lifecycle 중 실제 Owner를 확정하고 DB3 canonical source부터 일괄 보정한다. 구체 실행 목표: retired remote_system 사용 0 및 actual reader consumer 확인

- **개발 상세 단계:**
  1. Codex E2E27과 동일 Source에서 최신 MBR Segment INSERT 오류/DB 상태를 재현해 정확한 SQL statement와 bind 값을 확보한다.
  2. `platform-schema.json`의 `CPF_TRANSACTION_SEGMENT` 정의와 immutable V115, generated DB3 schema, mapper template/output을 필드 단위로 비교한다.
  3. `TransactionSegmentMapper`/adapter가 executionId, transactionId, parentSegmentId, System6, operationId를 동일 명칭·순서·타입으로 bind하는지 확인한다.
  4. Generated Domain operation boundary에서 Segment scope start/finish/failure가 실제로 호출되고 transaction context가 소실되지 않는지 추적한다.
  5. Oracle/PostgreSQL/MariaDB projection과 Runtime mapper를 재생성한 뒤 drift 0을 확인한다.
  6. Fresh MBR+Control Plane+CEC+Worker2 E2E에서 Summary/Segment/Lineage/Timeline이 동일 root transaction으로 물리 저장되는지 검증한다.
- **Side Effect / 회귀 필수범위:** Transaction Summary/Segment/Lineage/Timeline drift, DB3 Mapper drift, Header6/operationId/executionId 회귀
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB3 render/parity, MyBatis mapper test, integrated logging closure verifier, Java25 targeted observability tests
- **로컬 Runtime 검증 계획:** Fresh DB + MBR + Control Plane + CEC + Worker2 → 실제 업무 20건 상당 workload → Summary/Segment/Lineage/Timeline DB 물리 조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-02-08/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-02.09 — Durable fallback 상태 확인 — DB 정상 시 pending durable fallback 0, 실패 시 recovery 가능 상태 구분

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `Source Fixed 확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `FAIL/미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex 후반 Source에는 F300~F304 Observability 보완이 실제 존재한다. F304는 DB3/정적/Java targeted 검증 후 Fresh E2E에서 Segment 물리 저장 0건이 재발해 `VERIFICATION_PENDING`에서 종료됐다. 따라서 Source를 처음부터 재작성하지 않고 실제 persistence 경계를 이어서 확인해야 한다.
- **실제 호출/Consumer 경로:** `BAT/CEC root CpfContext → Domain Invocation HTTP ingress → Generated Domain Operation boundary → Observability AutoConfiguration → TransactionLog/Segment Port → MyBatis/JDBC → CPF_TRANSACTION_* → ADM Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-starters/platform-operations/observability/**`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionLogMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionSegmentMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/**`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/runtime-template/cpf/vendor/{oracle,postgresql,mariadb}/mybatis/logging/*.xml.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/cpf/mybatis/logging/*.xml`
  - `cpf-tools/verification/tools/verify-cpf-integrated-logging-closure.py`
- **계획된 구체 개발내용:** F304의 최신 물리 실패를 재현해 Schema/Mapper/Writer/Context lifecycle 중 실제 Owner를 확정하고 DB3 canonical source부터 일괄 보정한다. 구체 실행 목표: DB 정상 시 pending durable fallback 0, 실패 시 recovery 가능 상태 구분

- **개발 상세 단계:**
  1. `Durable fallback 상태 확인`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `DB 정상 시 pending durable fallback 0, 실패 시 recovery 가능 상태 구분` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Transaction Summary/Segment/Lineage/Timeline drift, DB3 Mapper drift, Header6/operationId/executionId 회귀
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB3 render/parity, MyBatis mapper test, integrated logging closure verifier, Java25 targeted observability tests
- **로컬 Runtime 검증 계획:** Fresh DB + MBR + Control Plane + CEC + Worker2 → 실제 업무 20건 상당 workload → Summary/Segment/Lineage/Timeline DB 물리 조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-02-09/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-02.10 — DB3 F304 projection parity — Oracle/PostgreSQL/MariaDB Schema/Mapper/Runtime Query 동일 변경 확인

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `Source Fixed 확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `FAIL/미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex 후반 Source에는 F300~F304 Observability 보완이 실제 존재한다. F304는 DB3/정적/Java targeted 검증 후 Fresh E2E에서 Segment 물리 저장 0건이 재발해 `VERIFICATION_PENDING`에서 종료됐다. 따라서 Source를 처음부터 재작성하지 않고 실제 persistence 경계를 이어서 확인해야 한다.
- **실제 호출/Consumer 경로:** `BAT/CEC root CpfContext → Domain Invocation HTTP ingress → Generated Domain Operation boundary → Observability AutoConfiguration → TransactionLog/Segment Port → MyBatis/JDBC → CPF_TRANSACTION_* → ADM Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-starters/platform-operations/observability/**`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionLogMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionSegmentMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/**`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/runtime-template/cpf/vendor/{oracle,postgresql,mariadb}/mybatis/logging/*.xml.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/cpf/mybatis/logging/*.xml`
  - `cpf-tools/verification/tools/verify-cpf-integrated-logging-closure.py`
- **계획된 구체 개발내용:** F304의 최신 물리 실패를 재현해 Schema/Mapper/Writer/Context lifecycle 중 실제 Owner를 확정하고 DB3 canonical source부터 일괄 보정한다. 구체 실행 목표: Oracle/PostgreSQL/MariaDB Schema/Mapper/Runtime Query 동일 변경 확인

- **개발 상세 단계:**
  1. Codex E2E27과 동일 Source에서 최신 MBR Segment INSERT 오류/DB 상태를 재현해 정확한 SQL statement와 bind 값을 확보한다.
  2. `platform-schema.json`의 `CPF_TRANSACTION_SEGMENT` 정의와 immutable V115, generated DB3 schema, mapper template/output을 필드 단위로 비교한다.
  3. `TransactionSegmentMapper`/adapter가 executionId, transactionId, parentSegmentId, System6, operationId를 동일 명칭·순서·타입으로 bind하는지 확인한다.
  4. Generated Domain operation boundary에서 Segment scope start/finish/failure가 실제로 호출되고 transaction context가 소실되지 않는지 추적한다.
  5. Oracle/PostgreSQL/MariaDB projection과 Runtime mapper를 재생성한 뒤 drift 0을 확인한다.
  6. Fresh MBR+Control Plane+CEC+Worker2 E2E에서 Summary/Segment/Lineage/Timeline이 동일 root transaction으로 물리 저장되는지 검증한다.
- **Side Effect / 회귀 필수범위:** Transaction Summary/Segment/Lineage/Timeline drift, DB3 Mapper drift, Header6/operationId/executionId 회귀
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB3 render/parity, MyBatis mapper test, integrated logging closure verifier, Java25 targeted observability tests
- **로컬 Runtime 검증 계획:** Fresh DB + MBR + Control Plane + CEC + Worker2 → 실제 업무 20건 상당 workload → Summary/Segment/Lineage/Timeline DB 물리 조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-02-10/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-02.11 — Fresh E2E 재실행 — Fresh DB에서 MBR+ControlPlane+CEC+Worker2 실제 거래/Segment/Timeline 확인

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `Source Fixed 확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `FAIL/미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex 후반 Source에는 F300~F304 Observability 보완이 실제 존재한다. F304는 DB3/정적/Java targeted 검증 후 Fresh E2E에서 Segment 물리 저장 0건이 재발해 `VERIFICATION_PENDING`에서 종료됐다. 따라서 Source를 처음부터 재작성하지 않고 실제 persistence 경계를 이어서 확인해야 한다.
- **실제 호출/Consumer 경로:** `BAT/CEC root CpfContext → Domain Invocation HTTP ingress → Generated Domain Operation boundary → Observability AutoConfiguration → TransactionLog/Segment Port → MyBatis/JDBC → CPF_TRANSACTION_* → ADM Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-starters/platform-operations/observability/**`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionLogMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionSegmentMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/**`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/runtime-template/cpf/vendor/{oracle,postgresql,mariadb}/mybatis/logging/*.xml.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/cpf/mybatis/logging/*.xml`
  - `cpf-tools/verification/tools/verify-cpf-integrated-logging-closure.py`
- **계획된 구체 개발내용:** F304의 최신 물리 실패를 재현해 Schema/Mapper/Writer/Context lifecycle 중 실제 Owner를 확정하고 DB3 canonical source부터 일괄 보정한다. 구체 실행 목표: Fresh DB에서 MBR+ControlPlane+CEC+Worker2 실제 거래/Segment/Timeline 확인

- **개발 상세 단계:**
  1. Codex E2E27과 동일 Source에서 최신 MBR Segment INSERT 오류/DB 상태를 재현해 정확한 SQL statement와 bind 값을 확보한다.
  2. `platform-schema.json`의 `CPF_TRANSACTION_SEGMENT` 정의와 immutable V115, generated DB3 schema, mapper template/output을 필드 단위로 비교한다.
  3. `TransactionSegmentMapper`/adapter가 executionId, transactionId, parentSegmentId, System6, operationId를 동일 명칭·순서·타입으로 bind하는지 확인한다.
  4. Generated Domain operation boundary에서 Segment scope start/finish/failure가 실제로 호출되고 transaction context가 소실되지 않는지 추적한다.
  5. Oracle/PostgreSQL/MariaDB projection과 Runtime mapper를 재생성한 뒤 drift 0을 확인한다.
  6. Fresh MBR+Control Plane+CEC+Worker2 E2E에서 Summary/Segment/Lineage/Timeline이 동일 root transaction으로 물리 저장되는지 검증한다.
- **Side Effect / 회귀 필수범위:** Transaction Summary/Segment/Lineage/Timeline drift, DB3 Mapper drift, Header6/operationId/executionId 회귀
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB3 render/parity, MyBatis mapper test, integrated logging closure verifier, Java25 targeted observability tests
- **로컬 Runtime 검증 계획:** Fresh DB + MBR + Control Plane + CEC + Worker2 → 실제 업무 20건 상당 workload → Summary/Segment/Lineage/Timeline DB 물리 조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-02-11/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-02.12 — F300~F304 통합 Closure — Summary/Header6/Segment/Lineage/Timeline이 하나의 물리 E2E로 닫힌 경우만 완료

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `Source Fixed 확인`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `FAIL/미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Codex 후반 Source에는 F300~F304 Observability 보완이 실제 존재한다. F304는 DB3/정적/Java targeted 검증 후 Fresh E2E에서 Segment 물리 저장 0건이 재발해 `VERIFICATION_PENDING`에서 종료됐다. 따라서 Source를 처음부터 재작성하지 않고 실제 persistence 경계를 이어서 확인해야 한다.
- **실제 호출/Consumer 경로:** `BAT/CEC root CpfContext → Domain Invocation HTTP ingress → Generated Domain Operation boundary → Observability AutoConfiguration → TransactionLog/Segment Port → MyBatis/JDBC → CPF_TRANSACTION_* → ADM Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-starters/platform-operations/observability/**`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionLogMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/mapper/logging/TransactionSegmentMapper.java`
  - `cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/**`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/runtime-template/cpf/vendor/{oracle,postgresql,mariadb}/mybatis/logging/*.xml.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/cpf/mybatis/logging/*.xml`
  - `cpf-tools/verification/tools/verify-cpf-integrated-logging-closure.py`
- **계획된 구체 개발내용:** F304의 최신 물리 실패를 재현해 Schema/Mapper/Writer/Context lifecycle 중 실제 Owner를 확정하고 DB3 canonical source부터 일괄 보정한다. 구체 실행 목표: Summary/Header6/Segment/Lineage/Timeline이 하나의 물리 E2E로 닫힌 경우만 완료

- **개발 상세 단계:**
  1. Codex E2E27과 동일 Source에서 최신 MBR Segment INSERT 오류/DB 상태를 재현해 정확한 SQL statement와 bind 값을 확보한다.
  2. `platform-schema.json`의 `CPF_TRANSACTION_SEGMENT` 정의와 immutable V115, generated DB3 schema, mapper template/output을 필드 단위로 비교한다.
  3. `TransactionSegmentMapper`/adapter가 executionId, transactionId, parentSegmentId, System6, operationId를 동일 명칭·순서·타입으로 bind하는지 확인한다.
  4. Generated Domain operation boundary에서 Segment scope start/finish/failure가 실제로 호출되고 transaction context가 소실되지 않는지 추적한다.
  5. Oracle/PostgreSQL/MariaDB projection과 Runtime mapper를 재생성한 뒤 drift 0을 확인한다.
  6. Fresh MBR+Control Plane+CEC+Worker2 E2E에서 Summary/Segment/Lineage/Timeline이 동일 root transaction으로 물리 저장되는지 검증한다.
- **Side Effect / 회귀 필수범위:** Transaction Summary/Segment/Lineage/Timeline drift, DB3 Mapper drift, Header6/operationId/executionId 회귀
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB3 render/parity, MyBatis mapper test, integrated logging closure verifier, Java25 targeted observability tests
- **로컬 Runtime 검증 계획:** Fresh DB + MBR + Control Plane + CEC + Worker2 → 실제 업무 20건 상당 workload → Summary/Segment/Lineage/Timeline DB 물리 조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-02-12/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-03.01 — 2 Worker 정상 분산 — 동일 Work를 두 Worker가 중복 없이 처리

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 정상 No-Kafka 업무 E2E 기반은 Codex가 실제 MBR 업무 처리까지 진행했으나 Kill/Lease/Fencing/UNKNOWN/Recovery/Reconcile 전체 Closure는 최종 완료 전에 별도 Runtime 검증이 필요하다.
- **실제 호출/Consumer 경로:** `CEC Execution/Work Item → Worker lease/fence → 처리/장애 → Result/UNKNOWN → Probe/Reconcile/Recovery → Aggregate/Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/center-cut-runtime/**`
  - `cpf-batch/worker/**`
  - `cpf-batch/control-plane/**`
  - `cpf-batch/runtime/**`
  - `cpf-tools/db/runtime-template/bat/repository/centercut-*.sql.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/bat/repository/centercut-*.sql`
  - `cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1`
- **계획된 구체 개발내용:** 정상 흐름을 재작성하지 않고 기존 No-Kafka 실행기반에 장애·복구 시나리오를 추가해 상태전이와 idempotency/fencing을 검증·보완한다. 구체 실행 목표: 동일 Work를 두 Worker가 중복 없이 처리

- **개발 상세 단계:**
  1. `2 Worker 정상 분산`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `동일 Work를 두 Worker가 중복 없이 처리` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** 중복 처리, stale worker mutation, 무한 retry, UNKNOWN 오판, lease 누수
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Batch runtime/worker/control-plane targeted tests, SQL catalog parity, state transition/fencing/idempotency tests
- **로컬 Runtime 검증 계획:** 2 Worker 정상 → Worker kill → lease expiry → fencing → UNKNOWN → Probe/Reconcile/Recovery/Reprocess → leak 0.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-03-01/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-03.02 — Worker process kill — 처리 중 Worker 종료 후 lease/fencing 기반 안전 재할당

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 정상 No-Kafka 업무 E2E 기반은 Codex가 실제 MBR 업무 처리까지 진행했으나 Kill/Lease/Fencing/UNKNOWN/Recovery/Reconcile 전체 Closure는 최종 완료 전에 별도 Runtime 검증이 필요하다.
- **실제 호출/Consumer 경로:** `CEC Execution/Work Item → Worker lease/fence → 처리/장애 → Result/UNKNOWN → Probe/Reconcile/Recovery → Aggregate/Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/center-cut-runtime/**`
  - `cpf-batch/worker/**`
  - `cpf-batch/control-plane/**`
  - `cpf-batch/runtime/**`
  - `cpf-tools/db/runtime-template/bat/repository/centercut-*.sql.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/bat/repository/centercut-*.sql`
  - `cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1`
- **계획된 구체 개발내용:** 정상 흐름을 재작성하지 않고 기존 No-Kafka 실행기반에 장애·복구 시나리오를 추가해 상태전이와 idempotency/fencing을 검증·보완한다. 구체 실행 목표: 처리 중 Worker 종료 후 lease/fencing 기반 안전 재할당

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `Worker process kill` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** 중복 처리, stale worker mutation, 무한 retry, UNKNOWN 오판, lease 누수
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Batch runtime/worker/control-plane targeted tests, SQL catalog parity, state transition/fencing/idempotency tests
- **로컬 Runtime 검증 계획:** 2 Worker 정상 → Worker kill → lease expiry → fencing → UNKNOWN → Probe/Reconcile/Recovery/Reprocess → leak 0.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-03-02/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-03.03 — Lease expiry — stale owner lease 만료 후 takeover

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 정상 No-Kafka 업무 E2E 기반은 Codex가 실제 MBR 업무 처리까지 진행했으나 Kill/Lease/Fencing/UNKNOWN/Recovery/Reconcile 전체 Closure는 최종 완료 전에 별도 Runtime 검증이 필요하다.
- **실제 호출/Consumer 경로:** `CEC Execution/Work Item → Worker lease/fence → 처리/장애 → Result/UNKNOWN → Probe/Reconcile/Recovery → Aggregate/Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/center-cut-runtime/**`
  - `cpf-batch/worker/**`
  - `cpf-batch/control-plane/**`
  - `cpf-batch/runtime/**`
  - `cpf-tools/db/runtime-template/bat/repository/centercut-*.sql.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/bat/repository/centercut-*.sql`
  - `cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1`
- **계획된 구체 개발내용:** 정상 흐름을 재작성하지 않고 기존 No-Kafka 실행기반에 장애·복구 시나리오를 추가해 상태전이와 idempotency/fencing을 검증·보완한다. 구체 실행 목표: stale owner lease 만료 후 takeover

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `Lease expiry` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** 중복 처리, stale worker mutation, 무한 retry, UNKNOWN 오판, lease 누수
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Batch runtime/worker/control-plane targeted tests, SQL catalog parity, state transition/fencing/idempotency tests
- **로컬 Runtime 검증 계획:** 2 Worker 정상 → Worker kill → lease expiry → fencing → UNKNOWN → Probe/Reconcile/Recovery/Reprocess → leak 0.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-03-03/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-03.04 — Fencing stale mutation 차단 — old fence token의 result/update 거부

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 정상 No-Kafka 업무 E2E 기반은 Codex가 실제 MBR 업무 처리까지 진행했으나 Kill/Lease/Fencing/UNKNOWN/Recovery/Reconcile 전체 Closure는 최종 완료 전에 별도 Runtime 검증이 필요하다.
- **실제 호출/Consumer 경로:** `CEC Execution/Work Item → Worker lease/fence → 처리/장애 → Result/UNKNOWN → Probe/Reconcile/Recovery → Aggregate/Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/center-cut-runtime/**`
  - `cpf-batch/worker/**`
  - `cpf-batch/control-plane/**`
  - `cpf-batch/runtime/**`
  - `cpf-tools/db/runtime-template/bat/repository/centercut-*.sql.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/bat/repository/centercut-*.sql`
  - `cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1`
- **계획된 구체 개발내용:** 정상 흐름을 재작성하지 않고 기존 No-Kafka 실행기반에 장애·복구 시나리오를 추가해 상태전이와 idempotency/fencing을 검증·보완한다. 구체 실행 목표: old fence token의 result/update 거부

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `Fencing stale mutation 차단` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** 중복 처리, stale worker mutation, 무한 retry, UNKNOWN 오판, lease 누수
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Batch runtime/worker/control-plane targeted tests, SQL catalog parity, state transition/fencing/idempotency tests
- **로컬 Runtime 검증 계획:** 2 Worker 정상 → Worker kill → lease expiry → fencing → UNKNOWN → Probe/Reconcile/Recovery/Reprocess → leak 0.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-03-04/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-03.05 — UNKNOWN 진입 — timeout/disconnect/reply-loss에서 실패 단정 금지

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 정상 No-Kafka 업무 E2E 기반은 Codex가 실제 MBR 업무 처리까지 진행했으나 Kill/Lease/Fencing/UNKNOWN/Recovery/Reconcile 전체 Closure는 최종 완료 전에 별도 Runtime 검증이 필요하다.
- **실제 호출/Consumer 경로:** `CEC Execution/Work Item → Worker lease/fence → 처리/장애 → Result/UNKNOWN → Probe/Reconcile/Recovery → Aggregate/Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/center-cut-runtime/**`
  - `cpf-batch/worker/**`
  - `cpf-batch/control-plane/**`
  - `cpf-batch/runtime/**`
  - `cpf-tools/db/runtime-template/bat/repository/centercut-*.sql.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/bat/repository/centercut-*.sql`
  - `cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1`
- **계획된 구체 개발내용:** 정상 흐름을 재작성하지 않고 기존 No-Kafka 실행기반에 장애·복구 시나리오를 추가해 상태전이와 idempotency/fencing을 검증·보완한다. 구체 실행 목표: timeout/disconnect/reply-loss에서 실패 단정 금지

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `UNKNOWN 진입` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** 중복 처리, stale worker mutation, 무한 retry, UNKNOWN 오판, lease 누수
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Batch runtime/worker/control-plane targeted tests, SQL catalog parity, state transition/fencing/idempotency tests
- **로컬 Runtime 검증 계획:** 2 Worker 정상 → Worker kill → lease expiry → fencing → UNKNOWN → Probe/Reconcile/Recovery/Reprocess → leak 0.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-03-05/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-03.06 — UNKNOWN blocking — UNKNOWN 미해결 Work의 위험 재실행/중복 처리 차단

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 정상 No-Kafka 업무 E2E 기반은 Codex가 실제 MBR 업무 처리까지 진행했으나 Kill/Lease/Fencing/UNKNOWN/Recovery/Reconcile 전체 Closure는 최종 완료 전에 별도 Runtime 검증이 필요하다.
- **실제 호출/Consumer 경로:** `CEC Execution/Work Item → Worker lease/fence → 처리/장애 → Result/UNKNOWN → Probe/Reconcile/Recovery → Aggregate/Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/center-cut-runtime/**`
  - `cpf-batch/worker/**`
  - `cpf-batch/control-plane/**`
  - `cpf-batch/runtime/**`
  - `cpf-tools/db/runtime-template/bat/repository/centercut-*.sql.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/bat/repository/centercut-*.sql`
  - `cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1`
- **계획된 구체 개발내용:** 정상 흐름을 재작성하지 않고 기존 No-Kafka 실행기반에 장애·복구 시나리오를 추가해 상태전이와 idempotency/fencing을 검증·보완한다. 구체 실행 목표: UNKNOWN 미해결 Work의 위험 재실행/중복 처리 차단

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `UNKNOWN blocking` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** 중복 처리, stale worker mutation, 무한 retry, UNKNOWN 오판, lease 누수
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Batch runtime/worker/control-plane targeted tests, SQL catalog parity, state transition/fencing/idempotency tests
- **로컬 Runtime 검증 계획:** 2 Worker 정상 → Worker kill → lease expiry → fencing → UNKNOWN → Probe/Reconcile/Recovery/Reprocess → leak 0.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-03-06/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-03.07 — Probe — side effect 존재 여부를 비파괴 조회

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 정상 No-Kafka 업무 E2E 기반은 Codex가 실제 MBR 업무 처리까지 진행했으나 Kill/Lease/Fencing/UNKNOWN/Recovery/Reconcile 전체 Closure는 최종 완료 전에 별도 Runtime 검증이 필요하다.
- **실제 호출/Consumer 경로:** `CEC Execution/Work Item → Worker lease/fence → 처리/장애 → Result/UNKNOWN → Probe/Reconcile/Recovery → Aggregate/Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/center-cut-runtime/**`
  - `cpf-batch/worker/**`
  - `cpf-batch/control-plane/**`
  - `cpf-batch/runtime/**`
  - `cpf-tools/db/runtime-template/bat/repository/centercut-*.sql.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/bat/repository/centercut-*.sql`
  - `cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1`
- **계획된 구체 개발내용:** 정상 흐름을 재작성하지 않고 기존 No-Kafka 실행기반에 장애·복구 시나리오를 추가해 상태전이와 idempotency/fencing을 검증·보완한다. 구체 실행 목표: side effect 존재 여부를 비파괴 조회

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `Probe` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** 중복 처리, stale worker mutation, 무한 retry, UNKNOWN 오판, lease 누수
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Batch runtime/worker/control-plane targeted tests, SQL catalog parity, state transition/fencing/idempotency tests
- **로컬 Runtime 검증 계획:** 2 Worker 정상 → Worker kill → lease expiry → fencing → UNKNOWN → Probe/Reconcile/Recovery/Reprocess → leak 0.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-03-07/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-03.08 — Reconcile — 실제 Owner 결과와 DB 상태를 일치시킴

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 정상 No-Kafka 업무 E2E 기반은 Codex가 실제 MBR 업무 처리까지 진행했으나 Kill/Lease/Fencing/UNKNOWN/Recovery/Reconcile 전체 Closure는 최종 완료 전에 별도 Runtime 검증이 필요하다.
- **실제 호출/Consumer 경로:** `CEC Execution/Work Item → Worker lease/fence → 처리/장애 → Result/UNKNOWN → Probe/Reconcile/Recovery → Aggregate/Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/center-cut-runtime/**`
  - `cpf-batch/worker/**`
  - `cpf-batch/control-plane/**`
  - `cpf-batch/runtime/**`
  - `cpf-tools/db/runtime-template/bat/repository/centercut-*.sql.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/bat/repository/centercut-*.sql`
  - `cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1`
- **계획된 구체 개발내용:** 정상 흐름을 재작성하지 않고 기존 No-Kafka 실행기반에 장애·복구 시나리오를 추가해 상태전이와 idempotency/fencing을 검증·보완한다. 구체 실행 목표: 실제 Owner 결과와 DB 상태를 일치시킴

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `Reconcile` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** 중복 처리, stale worker mutation, 무한 retry, UNKNOWN 오판, lease 누수
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Batch runtime/worker/control-plane targeted tests, SQL catalog parity, state transition/fencing/idempotency tests
- **로컬 Runtime 검증 계획:** 2 Worker 정상 → Worker kill → lease expiry → fencing → UNKNOWN → Probe/Reconcile/Recovery/Reprocess → leak 0.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-03-08/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-03.09 — Retry/Restart/Recovery/Reprocess 구분 — 서로 다른 의미와 idempotency 정책을 Runtime에서 검증

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 정상 No-Kafka 업무 E2E 기반은 Codex가 실제 MBR 업무 처리까지 진행했으나 Kill/Lease/Fencing/UNKNOWN/Recovery/Reconcile 전체 Closure는 최종 완료 전에 별도 Runtime 검증이 필요하다.
- **실제 호출/Consumer 경로:** `CEC Execution/Work Item → Worker lease/fence → 처리/장애 → Result/UNKNOWN → Probe/Reconcile/Recovery → Aggregate/Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/center-cut-runtime/**`
  - `cpf-batch/worker/**`
  - `cpf-batch/control-plane/**`
  - `cpf-batch/runtime/**`
  - `cpf-tools/db/runtime-template/bat/repository/centercut-*.sql.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/bat/repository/centercut-*.sql`
  - `cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1`
- **계획된 구체 개발내용:** 정상 흐름을 재작성하지 않고 기존 No-Kafka 실행기반에 장애·복구 시나리오를 추가해 상태전이와 idempotency/fencing을 검증·보완한다. 구체 실행 목표: 서로 다른 의미와 idempotency 정책을 Runtime에서 검증

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `Retry/Restart/Recovery/Reprocess 구분` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** 중복 처리, stale worker mutation, 무한 retry, UNKNOWN 오판, lease 누수
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Batch runtime/worker/control-plane targeted tests, SQL catalog parity, state transition/fencing/idempotency tests
- **로컬 Runtime 검증 계획:** 2 Worker 정상 → Worker kill → lease expiry → fencing → UNKNOWN → Probe/Reconcile/Recovery/Reprocess → leak 0.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-03-09/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-03.10 — Partial success — 부분 완료 후 aggregate/recovery 정책 확인

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 정상 No-Kafka 업무 E2E 기반은 Codex가 실제 MBR 업무 처리까지 진행했으나 Kill/Lease/Fencing/UNKNOWN/Recovery/Reconcile 전체 Closure는 최종 완료 전에 별도 Runtime 검증이 필요하다.
- **실제 호출/Consumer 경로:** `CEC Execution/Work Item → Worker lease/fence → 처리/장애 → Result/UNKNOWN → Probe/Reconcile/Recovery → Aggregate/Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/center-cut-runtime/**`
  - `cpf-batch/worker/**`
  - `cpf-batch/control-plane/**`
  - `cpf-batch/runtime/**`
  - `cpf-tools/db/runtime-template/bat/repository/centercut-*.sql.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/bat/repository/centercut-*.sql`
  - `cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1`
- **계획된 구체 개발내용:** 정상 흐름을 재작성하지 않고 기존 No-Kafka 실행기반에 장애·복구 시나리오를 추가해 상태전이와 idempotency/fencing을 검증·보완한다. 구체 실행 목표: 부분 완료 후 aggregate/recovery 정책 확인

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `Partial success` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** 중복 처리, stale worker mutation, 무한 retry, UNKNOWN 오판, lease 누수
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Batch runtime/worker/control-plane targeted tests, SQL catalog parity, state transition/fencing/idempotency tests
- **로컬 Runtime 검증 계획:** 2 Worker 정상 → Worker kill → lease expiry → fencing → UNKNOWN → Probe/Reconcile/Recovery/Reprocess → leak 0.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-03-10/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-03.11 — Trace/Timeline 장애 연속성 — Kill/UNKNOWN/Reconcile 전후 동일 transaction/execution 추적

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 정상 No-Kafka 업무 E2E 기반은 Codex가 실제 MBR 업무 처리까지 진행했으나 Kill/Lease/Fencing/UNKNOWN/Recovery/Reconcile 전체 Closure는 최종 완료 전에 별도 Runtime 검증이 필요하다.
- **실제 호출/Consumer 경로:** `CEC Execution/Work Item → Worker lease/fence → 처리/장애 → Result/UNKNOWN → Probe/Reconcile/Recovery → Aggregate/Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/center-cut-runtime/**`
  - `cpf-batch/worker/**`
  - `cpf-batch/control-plane/**`
  - `cpf-batch/runtime/**`
  - `cpf-tools/db/runtime-template/bat/repository/centercut-*.sql.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/bat/repository/centercut-*.sql`
  - `cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1`
- **계획된 구체 개발내용:** 정상 흐름을 재작성하지 않고 기존 No-Kafka 실행기반에 장애·복구 시나리오를 추가해 상태전이와 idempotency/fencing을 검증·보완한다. 구체 실행 목표: Kill/UNKNOWN/Reconcile 전후 동일 transaction/execution 추적

- **개발 상세 단계:**
  1. Codex E2E27과 동일 Source에서 최신 MBR Segment INSERT 오류/DB 상태를 재현해 정확한 SQL statement와 bind 값을 확보한다.
  2. `platform-schema.json`의 `CPF_TRANSACTION_SEGMENT` 정의와 immutable V115, generated DB3 schema, mapper template/output을 필드 단위로 비교한다.
  3. `TransactionSegmentMapper`/adapter가 executionId, transactionId, parentSegmentId, System6, operationId를 동일 명칭·순서·타입으로 bind하는지 확인한다.
  4. Generated Domain operation boundary에서 Segment scope start/finish/failure가 실제로 호출되고 transaction context가 소실되지 않는지 추적한다.
  5. Oracle/PostgreSQL/MariaDB projection과 Runtime mapper를 재생성한 뒤 drift 0을 확인한다.
  6. Fresh MBR+Control Plane+CEC+Worker2 E2E에서 Summary/Segment/Lineage/Timeline이 동일 root transaction으로 물리 저장되는지 검증한다.
- **Side Effect / 회귀 필수범위:** 중복 처리, stale worker mutation, 무한 retry, UNKNOWN 오판, lease 누수
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Batch runtime/worker/control-plane targeted tests, SQL catalog parity, state transition/fencing/idempotency tests
- **로컬 Runtime 검증 계획:** 2 Worker 정상 → Worker kill → lease expiry → fencing → UNKNOWN → Probe/Reconcile/Recovery/Reprocess → leak 0.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-03-11/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-03.12 — Leak cleanup — DB/role/process/port/runtime temp 누수 0 확인

- **Origin / 분류:** `CODEX_UNFINISHED`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `부분 PASS`
- **현재 Runtime 검증:** `미완료`
- **현재 Codex 검증:** `PENDING`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 정상 No-Kafka 업무 E2E 기반은 Codex가 실제 MBR 업무 처리까지 진행했으나 Kill/Lease/Fencing/UNKNOWN/Recovery/Reconcile 전체 Closure는 최종 완료 전에 별도 Runtime 검증이 필요하다.
- **실제 호출/Consumer 경로:** `CEC Execution/Work Item → Worker lease/fence → 처리/장애 → Result/UNKNOWN → Probe/Reconcile/Recovery → Aggregate/Timeline`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-batch/center-cut-runtime/**`
  - `cpf-batch/worker/**`
  - `cpf-batch/control-plane/**`
  - `cpf-batch/runtime/**`
  - `cpf-tools/db/runtime-template/bat/repository/centercut-*.sql.template`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/bat/repository/centercut-*.sql`
  - `cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1`
- **계획된 구체 개발내용:** 정상 흐름을 재작성하지 않고 기존 No-Kafka 실행기반에 장애·복구 시나리오를 추가해 상태전이와 idempotency/fencing을 검증·보완한다. 구체 실행 목표: DB/role/process/port/runtime temp 누수 0 확인

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `Leak cleanup` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** 중복 처리, stale worker mutation, 무한 retry, UNKNOWN 오판, lease 누수
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Batch runtime/worker/control-plane targeted tests, SQL catalog parity, state transition/fencing/idempotency tests
- **로컬 Runtime 검증 계획:** 2 Worker 정상 → Worker kill → lease expiry → fencing → UNKNOWN → Probe/Reconcile/Recovery/Reprocess → leak 0.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-03-12/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-04.01 — ADM Approval Type Inventory — Batch/Gateway/Runtime/Security/DataQuality 등 actionType/ownerCommand 실제 목록화

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** ADM Approval Engine은 정책 Version, Participant Snapshot, Decision Rule, self-approval, idempotency, Optimistic Version, Owner Command 실행, UNKNOWN/Reconcile 기반이 존재한다. 신규 Requirement의 동적 결재문서/History Search/Before-After/실행결과 Read Model은 부족하다.
- **실제 호출/Consumer 경로:** `ADM Approval UI → Generated Client → AdmApprovalController → AdmApprovalService → AdmApprovalRepository / OwnerCommandPort → 실제 Owner Runtime → execution/history detail`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/owner/*.java`
  - `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue`
  - `cpf-admin/frontend/src/features/approvals/methods.ts`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
- **계획된 구체 개발내용:** ADM 기존 Approval Engine은 유지하고, 업무유형별 판단문서/Read Model/History/Result 조회를 Public API와 Generated Client까지 확장한다. 구체 실행 목표: Batch/Gateway/Runtime/Security/DataQuality 등 actionType/ownerCommand 실제 목록화

- **개발 상세 단계:**
  1. Controller/OpenAPI/Frontend route/OwnerCommandAdapter를 기준으로 실제 Approval actionType/targetType/ownerModule/ownerCommand 조합을 전수 추출한다.
  2. Batch/Gateway/Runtime/Security/DataQuality/Backoffice 등 업무유형별 실제 Consumer와 승인 후 실행 Owner를 연결한다.
  3. 각 유형에 필요한 판단정보, Before/After, Snapshot, History, Attachment, Result 요구를 Matrix로 정의한다.
  4. 범용 JSON만 있는 유형, Owner Consumer가 없는 유형, 실행결과 연결이 없는 유형을 Gap으로 하위 인덱스에 등록한다.
  5. 새 Approval Type을 추가해야 하는 경우 DB metadata/API/Frontend/Permission/Test를 같은 변경 단위로 묶는다.
- **Side Effect / 회귀 필수범위:** 민감정보 노출, Approval snapshot 불변성 훼손, 기존 Owner Command/SoD/idempotency 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** ADM approval unit/integration tests, OpenAPI source validation, generated operation consumer 100%, sensitive-data tests
- **로컬 Runtime 검증 계획:** ADM Browser/API/DB/Owner Runtime: 요청→업무별 문서→승인/반려→Owner 실행→결과/History 재조회 + 오류상태.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-04-01/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-04.02 — Dynamic Approval Document SPI — 업무유형별 Document Resolver/Provider 또는 schema-driven contract 구현 필요 여부 확정

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** ADM Approval Engine은 정책 Version, Participant Snapshot, Decision Rule, self-approval, idempotency, Optimistic Version, Owner Command 실행, UNKNOWN/Reconcile 기반이 존재한다. 신규 Requirement의 동적 결재문서/History Search/Before-After/실행결과 Read Model은 부족하다.
- **실제 호출/Consumer 경로:** `ADM Approval UI → Generated Client → AdmApprovalController → AdmApprovalService → AdmApprovalRepository / OwnerCommandPort → 실제 Owner Runtime → execution/history detail`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/owner/*.java`
  - `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue`
  - `cpf-admin/frontend/src/features/approvals/methods.ts`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
- **계획된 구체 개발내용:** ADM 기존 Approval Engine은 유지하고, 업무유형별 판단문서/Read Model/History/Result 조회를 Public API와 Generated Client까지 확장한다. 구체 실행 목표: 업무유형별 Document Resolver/Provider 또는 schema-driven contract 구현 필요 여부 확정

- **개발 상세 단계:**
  1. 기존 Approval detail DTO/Map 구조와 actionType/ownerCommand 식별자를 기준으로 Document Resolver SPI의 Owner 경계를 확정한다.
  2. 공통 Header/요청정보와 업무유형별 Section을 분리하고, Section마다 required/label/value/masked/validation/version 계약을 정의한다.
  3. Batch/Gateway/Runtime/User/Backoffice 대표 유형에 concrete Provider/Resolver를 연결해 generic fallback만으로 승인되지 않게 한다.
  4. Document Version/Snapshot Version 불일치 또는 필수 Section 로드 실패 시 승인 버튼/API가 fail-closed하도록 Backend를 보강한다.
  5. OpenAPI/Generated Client/Frontend Renderer를 동일 contract로 생성·소비하고 Browser에서 유형별 다른 문서가 보이는지 검증한다.
- **Side Effect / 회귀 필수범위:** 민감정보 노출, Approval snapshot 불변성 훼손, 기존 Owner Command/SoD/idempotency 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** ADM approval unit/integration tests, OpenAPI source validation, generated operation consumer 100%, sensitive-data tests
- **로컬 Runtime 검증 계획:** ADM Browser/API/DB/Owner Runtime: 요청→업무별 문서→승인/반려→Owner 실행→결과/History 재조회 + 오류상태.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-04-02/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-04.03 — 승인 상세 payload 정책 보완 — 현재 payloadSnapshot 전체 제거 대신 민감필드만 마스킹하고 판단정보 제공

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 현재 `AdmApprovalService.detail()`은 `sanitizeDetail()`을 통해 `payloadSnapshot`, `secret`, `password`, `token` 키를 응답에서 통째로 제거한다. `AdmApprovalSensitiveDetailR6Test`도 `payloadSnapshot` 미노출을 기대한다. 민감정보 차단은 필요하지만 결재 판단정보 자체가 소실되는 현재 방식은 신규 동적 결재문서 Requirement와 충돌한다.
- **실제 호출/Consumer 경로:** `ADM Approval UI → Generated Client → AdmApprovalController → AdmApprovalService → AdmApprovalRepository / OwnerCommandPort → 실제 Owner Runtime → execution/history detail`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/owner/*.java`
  - `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue`
  - `cpf-admin/frontend/src/features/approvals/methods.ts`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
- **계획된 구체 개발내용:** ADM 기존 Approval Engine은 유지하고, 업무유형별 판단문서/Read Model/History/Result 조회를 Public API와 Generated Client까지 확장한다. 구체 실행 목표: 현재 payloadSnapshot 전체 제거 대신 민감필드만 마스킹하고 판단정보 제공

- **개발 상세 단계:**
  1. `AdmApprovalService.sanitizeDetail()`과 `AdmApprovalSensitiveDetailR6Test`의 현재 전체 payload 제거 정책을 기준선으로 고정한다.
  2. 원본 Snapshot을 외부에 그대로 노출하지 않고 업무판단용 safe read model을 만드는 Masking/Projection 계층을 분리한다.
  3. password/token/secret/credential/개인정보는 field-level masking하고, 비민감 변경값·업무식별자·Before/After는 유지한다.
  4. 승인 시에는 safe read model이 아니라 저장된 Snapshot hash/version과 원본 canonical payload를 Backend에서 다시 검증한다.
  5. 민감정보 raw 노출 0과 판단정보 누락 0을 양·음수 테스트 및 Browser E2E로 동시에 확인한다.
- **Side Effect / 회귀 필수범위:** 민감정보 노출, Approval snapshot 불변성 훼손, 기존 Owner Command/SoD/idempotency 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** ADM approval unit/integration tests, OpenAPI source validation, generated operation consumer 100%, sensitive-data tests
- **로컬 Runtime 검증 계획:** ADM Browser/API/DB/Owner Runtime: 요청→업무별 문서→승인/반려→Owner 실행→결과/History 재조회 + 오류상태.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-04-03/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-04.04 — 업무별 필수 판단정보 Read Model — Job/Execution/Route/Runtime/User 등 유형별 상세 Section 제공

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** ADM Approval Engine은 정책 Version, Participant Snapshot, Decision Rule, self-approval, idempotency, Optimistic Version, Owner Command 실행, UNKNOWN/Reconcile 기반이 존재한다. 신규 Requirement의 동적 결재문서/History Search/Before-After/실행결과 Read Model은 부족하다.
- **실제 호출/Consumer 경로:** `ADM Approval UI → Generated Client → AdmApprovalController → AdmApprovalService → AdmApprovalRepository / OwnerCommandPort → 실제 Owner Runtime → execution/history detail`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/owner/*.java`
  - `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue`
  - `cpf-admin/frontend/src/features/approvals/methods.ts`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
- **계획된 구체 개발내용:** ADM 기존 Approval Engine은 유지하고, 업무유형별 판단문서/Read Model/History/Result 조회를 Public API와 Generated Client까지 확장한다. 구체 실행 목표: Job/Execution/Route/Runtime/User 등 유형별 상세 Section 제공

- **개발 상세 단계:**
  1. `업무별 필수 판단정보 Read Model`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `Job/Execution/Route/Runtime/User 등 유형별 상세 Section 제공` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** 민감정보 노출, Approval snapshot 불변성 훼손, 기존 Owner Command/SoD/idempotency 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** ADM approval unit/integration tests, OpenAPI source validation, generated operation consumer 100%, sensitive-data tests
- **로컬 Runtime 검증 계획:** ADM Browser/API/DB/Owner Runtime: 요청→업무별 문서→승인/반려→Owner 실행→결과/History 재조회 + 오류상태.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-04-04/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-04.05 — Before/After field diff — 변경성 업무의 field label/before/after/changed/validation 구현

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** ADM Approval Engine은 정책 Version, Participant Snapshot, Decision Rule, self-approval, idempotency, Optimistic Version, Owner Command 실행, UNKNOWN/Reconcile 기반이 존재한다. 신규 Requirement의 동적 결재문서/History Search/Before-After/실행결과 Read Model은 부족하다.
- **실제 호출/Consumer 경로:** `ADM Approval UI → Generated Client → AdmApprovalController → AdmApprovalService → AdmApprovalRepository / OwnerCommandPort → 실제 Owner Runtime → execution/history detail`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/owner/*.java`
  - `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue`
  - `cpf-admin/frontend/src/features/approvals/methods.ts`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
- **계획된 구체 개발내용:** ADM 기존 Approval Engine은 유지하고, 업무유형별 판단문서/Read Model/History/Result 조회를 Public API와 Generated Client까지 확장한다. 구체 실행 목표: 변경성 업무의 field label/before/after/changed/validation 구현

- **개발 상세 단계:**
  1. Approval Snapshot/ChangeSet에서 업무 필드별 canonical path와 label을 추출하는 diff model을 정의한다.
  2. 각 field에 before/after/changed/importance/validation/masked 속성을 제공하고 Raw Payload는 권한 있는 경우에만 보조정보로 둔다.
  3. 동일값/추가/삭제/null/type-change/대량변경을 일관된 규칙으로 처리한다.
  4. Frontend에 변경필드만/전체/Group별 보기와 중요변경 강조가 가능한 renderer 계약을 제공한다.
  5. 승인 후 실제 적용된 값과 승인 Snapshot diff가 달라지면 conflict/recovery로 추적한다.
- **Side Effect / 회귀 필수범위:** 민감정보 노출, Approval snapshot 불변성 훼손, 기존 Owner Command/SoD/idempotency 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** ADM approval unit/integration tests, OpenAPI source validation, generated operation consumer 100%, sensitive-data tests
- **로컬 Runtime 검증 계획:** ADM Browser/API/DB/Owner Runtime: 요청→업무별 문서→승인/반려→Owner 실행→결과/History 재조회 + 오류상태.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-04-05/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-04.06 — Snapshot version contract — Approval Request/Business Snapshot/Policy/Parameter/ChangeSet version 불변성

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** ADM Approval Engine은 정책 Version, Participant Snapshot, Decision Rule, self-approval, idempotency, Optimistic Version, Owner Command 실행, UNKNOWN/Reconcile 기반이 존재한다. 신규 Requirement의 동적 결재문서/History Search/Before-After/실행결과 Read Model은 부족하다.
- **실제 호출/Consumer 경로:** `ADM Approval UI → Generated Client → AdmApprovalController → AdmApprovalService → AdmApprovalRepository / OwnerCommandPort → 실제 Owner Runtime → execution/history detail`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/owner/*.java`
  - `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue`
  - `cpf-admin/frontend/src/features/approvals/methods.ts`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
- **계획된 구체 개발내용:** ADM 기존 Approval Engine은 유지하고, 업무유형별 판단문서/Read Model/History/Result 조회를 Public API와 Generated Client까지 확장한다. 구체 실행 목표: Approval Request/Business Snapshot/Policy/Parameter/ChangeSet version 불변성

- **개발 상세 단계:**
  1. Approval Request Version, Business Snapshot Version, Policy Version, Parameter Snapshot, ChangeSet hash를 canonical field로 확정한다.
  2. 요청 시점에 Snapshot/hash/version을 immutable하게 저장하고 후속 update로 원본이 덮이지 않도록 한다.
  3. 상세 조회는 현재 원본이 아니라 승인 당시 Snapshot을 기준으로 문서를 구성한다.
  4. 승인 직전 저장된 version/hash/status/auth를 다시 검증하고 stale/conflict면 승인 실행을 차단한다.
  5. 승인 후 Owner 실행은 승인된 Snapshot을 사용하며 원본 변경 시 재요청/재검토 정책을 적용한다.
- **Side Effect / 회귀 필수범위:** 민감정보 노출, Approval snapshot 불변성 훼손, 기존 Owner Command/SoD/idempotency 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** ADM approval unit/integration tests, OpenAPI source validation, generated operation consumer 100%, sensitive-data tests
- **로컬 Runtime 검증 계획:** ADM Browser/API/DB/Owner Runtime: 요청→업무별 문서→승인/반려→Owner 실행→결과/History 재조회 + 오류상태.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-04-06/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-04.07 — ADM Approval 목록/검색 — 기간/요청자/승인자/상태/시스템/업무/transactionId 검색 및 paging/sort

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** ADM Approval Engine은 정책 Version, Participant Snapshot, Decision Rule, self-approval, idempotency, Optimistic Version, Owner Command 실행, UNKNOWN/Reconcile 기반이 존재한다. 신규 Requirement의 동적 결재문서/History Search/Before-After/실행결과 Read Model은 부족하다.
- **실제 호출/Consumer 경로:** `ADM Approval UI → Generated Client → AdmApprovalController → AdmApprovalService → AdmApprovalRepository / OwnerCommandPort → 실제 Owner Runtime → execution/history detail`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/owner/*.java`
  - `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue`
  - `cpf-admin/frontend/src/features/approvals/methods.ts`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
- **계획된 구체 개발내용:** ADM 기존 Approval Engine은 유지하고, 업무유형별 판단문서/Read Model/History/Result 조회를 Public API와 Generated Client까지 확장한다. 구체 실행 목표: 기간/요청자/승인자/상태/시스템/업무/transactionId 검색 및 paging/sort

- **개발 상세 단계:**
  1. 현재 write/history table과 기존 read query 존재 여부를 확인해 INSERT-only 또는 제한 조회 Gap을 확정한다.
  2. 기간/문서번호/요청번호/요청자/결재자/조직/유형/상태/시스템/대상/transactionId 등 canonical search contract를 정의한다.
  3. Repository SQL → Service → Controller/OpenAPI → Generated Client → UI를 한 번에 연결하고 paging/sort whitelist를 적용한다.
  4. History detail에 before/after, decision comment, actor, timestamp, snapshot/version, execution/recovery result를 조립한다.
  5. 권한 없는 조직/첨부/민감 필드가 검색 결과나 상세에서 노출되지 않는지 Runtime negative test를 수행한다.
- **Side Effect / 회귀 필수범위:** 민감정보 노출, Approval snapshot 불변성 훼손, 기존 Owner Command/SoD/idempotency 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** ADM approval unit/integration tests, OpenAPI source validation, generated operation consumer 100%, sensitive-data tests
- **로컬 Runtime 검증 계획:** ADM Browser/API/DB/Owner Runtime: 요청→업무별 문서→승인/반려→Owner 실행→결과/History 재조회 + 오류상태.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-04-07/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-04.08 — ADM History 조회 — 현재 INSERT 중심 history를 목록/상세로 actual read consumer 제공

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** ADM Approval Engine은 정책 Version, Participant Snapshot, Decision Rule, self-approval, idempotency, Optimistic Version, Owner Command 실행, UNKNOWN/Reconcile 기반이 존재한다. 신규 Requirement의 동적 결재문서/History Search/Before-After/실행결과 Read Model은 부족하다.
- **실제 호출/Consumer 경로:** `ADM Approval UI → Generated Client → AdmApprovalController → AdmApprovalService → AdmApprovalRepository / OwnerCommandPort → 실제 Owner Runtime → execution/history detail`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/owner/*.java`
  - `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue`
  - `cpf-admin/frontend/src/features/approvals/methods.ts`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
- **계획된 구체 개발내용:** ADM 기존 Approval Engine은 유지하고, 업무유형별 판단문서/Read Model/History/Result 조회를 Public API와 Generated Client까지 확장한다. 구체 실행 목표: 현재 INSERT 중심 history를 목록/상세로 actual read consumer 제공

- **개발 상세 단계:**
  1. 현재 write/history table과 기존 read query 존재 여부를 확인해 INSERT-only 또는 제한 조회 Gap을 확정한다.
  2. 기간/문서번호/요청번호/요청자/결재자/조직/유형/상태/시스템/대상/transactionId 등 canonical search contract를 정의한다.
  3. Repository SQL → Service → Controller/OpenAPI → Generated Client → UI를 한 번에 연결하고 paging/sort whitelist를 적용한다.
  4. History detail에 before/after, decision comment, actor, timestamp, snapshot/version, execution/recovery result를 조립한다.
  5. 권한 없는 조직/첨부/민감 필드가 검색 결과나 상세에서 노출되지 않는지 Runtime negative test를 수행한다.
- **Side Effect / 회귀 필수범위:** 민감정보 노출, Approval snapshot 불변성 훼손, 기존 Owner Command/SoD/idempotency 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** ADM approval unit/integration tests, OpenAPI source validation, generated operation consumer 100%, sensitive-data tests
- **로컬 Runtime 검증 계획:** ADM Browser/API/DB/Owner Runtime: 요청→업무별 문서→승인/반려→Owner 실행→결과/History 재조회 + 오류상태.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-04-08/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-04.09 — Attachment/근거 접근 — 결재 판단용 첨부 metadata/access permission/masking 연결

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** ADM Approval Engine은 정책 Version, Participant Snapshot, Decision Rule, self-approval, idempotency, Optimistic Version, Owner Command 실행, UNKNOWN/Reconcile 기반이 존재한다. 신규 Requirement의 동적 결재문서/History Search/Before-After/실행결과 Read Model은 부족하다.
- **실제 호출/Consumer 경로:** `ADM Approval UI → Generated Client → AdmApprovalController → AdmApprovalService → AdmApprovalRepository / OwnerCommandPort → 실제 Owner Runtime → execution/history detail`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/owner/*.java`
  - `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue`
  - `cpf-admin/frontend/src/features/approvals/methods.ts`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
- **계획된 구체 개발내용:** ADM 기존 Approval Engine은 유지하고, 업무유형별 판단문서/Read Model/History/Result 조회를 Public API와 Generated Client까지 확장한다. 구체 실행 목표: 결재 판단용 첨부 metadata/access permission/masking 연결

- **개발 상세 단계:**
  1. 현재 `attachment_group_id`/reference가 저장되는 위치와 실제 Attachment service/repository Owner를 확인한다.
  2. 결재 상세 Read Model에 파일명/크기/유형/등록자/등록시각/검증상태 등 판단용 metadata를 추가한다.
  3. 다운로드/미리보기는 업무권한과 Approval 접근권한을 재검증하고 직접 object key/secret을 노출하지 않는다.
  4. 삭제/만료/권한없음/악성 또는 무결성 실패 Attachment는 승인 가능 상태를 차단하거나 명시 오류로 표시한다.
  5. History 재조회 시 과거 Attachment reference가 유지되는지 Upgrade/Runtime에서 확인한다.
- **Side Effect / 회귀 필수범위:** 민감정보 노출, Approval snapshot 불변성 훼손, 기존 Owner Command/SoD/idempotency 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** ADM approval unit/integration tests, OpenAPI source validation, generated operation consumer 100%, sensitive-data tests
- **로컬 Runtime 검증 계획:** ADM Browser/API/DB/Owner Runtime: 요청→업무별 문서→승인/반려→Owner 실행→결과/History 재조회 + 오류상태.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-04-09/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-04.10 — Execution result 문서 연결 — APPROVED와 EXECUTED/FAILED/UNKNOWN/RECOVERED를 동일 상세에서 추적

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** ADM Approval Engine은 정책 Version, Participant Snapshot, Decision Rule, self-approval, idempotency, Optimistic Version, Owner Command 실행, UNKNOWN/Reconcile 기반이 존재한다. 신규 Requirement의 동적 결재문서/History Search/Before-After/실행결과 Read Model은 부족하다.
- **실제 호출/Consumer 경로:** `ADM Approval UI → Generated Client → AdmApprovalController → AdmApprovalService → AdmApprovalRepository / OwnerCommandPort → 실제 Owner Runtime → execution/history detail`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/owner/*.java`
  - `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue`
  - `cpf-admin/frontend/src/features/approvals/methods.ts`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
- **계획된 구체 개발내용:** ADM 기존 Approval Engine은 유지하고, 업무유형별 판단문서/Read Model/History/Result 조회를 Public API와 Generated Client까지 확장한다. 구체 실행 목표: APPROVED와 EXECUTED/FAILED/UNKNOWN/RECOVERED를 동일 상세에서 추적

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `Execution result 문서 연결` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** 민감정보 노출, Approval snapshot 불변성 훼손, 기존 Owner Command/SoD/idempotency 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** ADM approval unit/integration tests, OpenAPI source validation, generated operation consumer 100%, sensitive-data tests
- **로컬 Runtime 검증 계획:** ADM Browser/API/DB/Owner Runtime: 요청→업무별 문서→승인/반려→Owner 실행→결과/History 재조회 + 오류상태.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-04-10/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-04.11 — Backend approve-time 재검증 — Document version/request version/status/auth를 승인 시 다시 검증

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** ADM Approval Engine은 정책 Version, Participant Snapshot, Decision Rule, self-approval, idempotency, Optimistic Version, Owner Command 실행, UNKNOWN/Reconcile 기반이 존재한다. 신규 Requirement의 동적 결재문서/History Search/Before-After/실행결과 Read Model은 부족하다.
- **실제 호출/Consumer 경로:** `ADM Approval UI → Generated Client → AdmApprovalController → AdmApprovalService → AdmApprovalRepository / OwnerCommandPort → 실제 Owner Runtime → execution/history detail`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/owner/*.java`
  - `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue`
  - `cpf-admin/frontend/src/features/approvals/methods.ts`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
- **계획된 구체 개발내용:** ADM 기존 Approval Engine은 유지하고, 업무유형별 판단문서/Read Model/History/Result 조회를 Public API와 Generated Client까지 확장한다. 구체 실행 목표: Document version/request version/status/auth를 승인 시 다시 검증

- **개발 상세 단계:**
  1. `Backend approve-time 재검증`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `Document version/request version/status/auth를 승인 시 다시 검증` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** 민감정보 노출, Approval snapshot 불변성 훼손, 기존 Owner Command/SoD/idempotency 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** ADM approval unit/integration tests, OpenAPI source validation, generated operation consumer 100%, sensitive-data tests
- **로컬 Runtime 검증 계획:** ADM Browser/API/DB/Owner Runtime: 요청→업무별 문서→승인/반려→Owner 실행→결과/History 재조회 + 오류상태.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-04-11/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-04.12 — ADM Browser E2E — Generated Client로 상세→승인/반려→실행→이력/결과 재조회까지 Browser 검증

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** ADM Approval Engine은 정책 Version, Participant Snapshot, Decision Rule, self-approval, idempotency, Optimistic Version, Owner Command 실행, UNKNOWN/Reconcile 기반이 존재한다. 신규 Requirement의 동적 결재문서/History Search/Before-After/실행결과 Read Model은 부족하다.
- **실제 호출/Consumer 경로:** `ADM Approval UI → Generated Client → AdmApprovalController → AdmApprovalService → AdmApprovalRepository / OwnerCommandPort → 실제 Owner Runtime → execution/history detail`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/owner/*.java`
  - `cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue`
  - `cpf-admin/frontend/src/features/approvals/methods.ts`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
- **계획된 구체 개발내용:** ADM 기존 Approval Engine은 유지하고, 업무유형별 판단문서/Read Model/History/Result 조회를 Public API와 Generated Client까지 확장한다. 구체 실행 목표: Generated Client로 상세→승인/반려→실행→이력/결과 재조회까지 Browser 검증

- **개발 상세 단계:**
  1. `ADM Browser E2E`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `Generated Client로 상세→승인/반려→실행→이력/결과 재조회까지 Browser 검증` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** 민감정보 노출, Approval snapshot 불변성 훼손, 기존 Owner Command/SoD/idempotency 회귀
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** ADM approval unit/integration tests, OpenAPI source validation, generated operation consumer 100%, sensitive-data tests
- **로컬 Runtime 검증 계획:** ADM Browser/API/DB/Owner Runtime: 요청→업무별 문서→승인/반려→Owner 실행→결과/History 재조회 + 오류상태.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-04-12/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-05.01 — MBW Approval Type Inventory — businessDomain/approvalType 실제 목록과 업무별 판단정보 요구를 매핑

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** MBW에는 submit/detail/inbox/decision/withdraw/cancel/resubmit/expire 및 Approval DB 테이블이 존재한다. 다만 화면은 범용 StructuredDataView 중심이고 History read/search, 업무별 문서, execution result 연결이 충분하지 않다.
- **실제 호출/Consumer 경로:** `Backoffice Approval UI → Generated Client/API → BackofficeApprovalPolicyService → Repository → MBW_APPROVAL_* → 실제 Business Apply/History`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-backoffice-web/frontend/src/features/approvals/**`
  - `cpf-backoffice-web/frontend/src/shared/components/StructuredDataView.vue`
  - `cpf-backoffice/openapi/**`
  - `cpf-tools/db/canonical/platform-schema.json (MBW_APPROVAL_*)`
- **계획된 구체 개발내용:** MBW 기존 Approval Lifecycle은 유지하고, 범용 JSON 표시를 업무별 문서 Renderer와 History/Result Read Model로 확장한다. 구체 실행 목표: businessDomain/approvalType 실제 목록과 업무별 판단정보 요구를 매핑

- **개발 상세 단계:**
  1. Controller/OpenAPI/Frontend route/OwnerCommandAdapter를 기준으로 실제 Approval actionType/targetType/ownerModule/ownerCommand 조합을 전수 추출한다.
  2. Batch/Gateway/Runtime/Security/DataQuality/Backoffice 등 업무유형별 실제 Consumer와 승인 후 실행 Owner를 연결한다.
  3. 각 유형에 필요한 판단정보, Before/After, Snapshot, History, Attachment, Result 요구를 Matrix로 정의한다.
  4. 범용 JSON만 있는 유형, Owner Consumer가 없는 유형, 실행결과 연결이 없는 유형을 Gap으로 하위 인덱스에 등록한다.
  5. 새 Approval Type을 추가해야 하는 경우 DB metadata/API/Frontend/Permission/Test를 같은 변경 단위로 묶는다.
- **Side Effect / 회귀 필수범위:** Backoffice 승인 Lifecycle/위임/재요청 회귀, 민감정보 노출, UI dead route
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Backoffice approval service/repository tests, OpenAPI validation, frontend generated-client consumer test
- **로컬 Runtime 검증 계획:** Backoffice Browser/API/DB: Inbox/Search→업무별 상세→승인/반려/회수/재요청→실제 적용→History/Result 재조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-05-01/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-05.02 — MBW Dynamic Document Provider — 현재 generic payload/detail을 업무별 문서/section으로 확장

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** MBW에는 submit/detail/inbox/decision/withdraw/cancel/resubmit/expire 및 Approval DB 테이블이 존재한다. 다만 화면은 범용 StructuredDataView 중심이고 History read/search, 업무별 문서, execution result 연결이 충분하지 않다.
- **실제 호출/Consumer 경로:** `Backoffice Approval UI → Generated Client/API → BackofficeApprovalPolicyService → Repository → MBW_APPROVAL_* → 실제 Business Apply/History`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-backoffice-web/frontend/src/features/approvals/**`
  - `cpf-backoffice-web/frontend/src/shared/components/StructuredDataView.vue`
  - `cpf-backoffice/openapi/**`
  - `cpf-tools/db/canonical/platform-schema.json (MBW_APPROVAL_*)`
- **계획된 구체 개발내용:** MBW 기존 Approval Lifecycle은 유지하고, 범용 JSON 표시를 업무별 문서 Renderer와 History/Result Read Model로 확장한다. 구체 실행 목표: 현재 generic payload/detail을 업무별 문서/section으로 확장

- **개발 상세 단계:**
  1. 기존 Approval detail DTO/Map 구조와 actionType/ownerCommand 식별자를 기준으로 Document Resolver SPI의 Owner 경계를 확정한다.
  2. 공통 Header/요청정보와 업무유형별 Section을 분리하고, Section마다 required/label/value/masked/validation/version 계약을 정의한다.
  3. Batch/Gateway/Runtime/User/Backoffice 대표 유형에 concrete Provider/Resolver를 연결해 generic fallback만으로 승인되지 않게 한다.
  4. Document Version/Snapshot Version 불일치 또는 필수 Section 로드 실패 시 승인 버튼/API가 fail-closed하도록 Backend를 보강한다.
  5. OpenAPI/Generated Client/Frontend Renderer를 동일 contract로 생성·소비하고 Browser에서 유형별 다른 문서가 보이는지 검증한다.
- **Side Effect / 회귀 필수범위:** Backoffice 승인 Lifecycle/위임/재요청 회귀, 민감정보 노출, UI dead route
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Backoffice approval service/repository tests, OpenAPI validation, frontend generated-client consumer test
- **로컬 Runtime 검증 계획:** Backoffice Browser/API/DB: Inbox/Search→업무별 상세→승인/반려/회수/재요청→실제 적용→History/Result 재조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-05-02/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-05.03 — Backoffice UI generic StructuredDataView 제거/보완 — 결재 업무유형별 전용/동적 Renderer 제공

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 현재 Backoffice `ApprovalInboxPage.vue`는 `ApprovalLookupForm`, `ApprovalDecisionForm`, `StructuredDataView`를 사용해 상세/Inbox를 범용 구조화 데이터로 표시한다. 업무 유형별 전용 Section/Schema Renderer가 확인되지 않았다.
- **실제 호출/Consumer 경로:** `Backoffice Approval UI → Generated Client/API → BackofficeApprovalPolicyService → Repository → MBW_APPROVAL_* → 실제 Business Apply/History`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-backoffice-web/frontend/src/features/approvals/**`
  - `cpf-backoffice-web/frontend/src/shared/components/StructuredDataView.vue`
  - `cpf-backoffice/openapi/**`
  - `cpf-tools/db/canonical/platform-schema.json (MBW_APPROVAL_*)`
- **계획된 구체 개발내용:** MBW 기존 Approval Lifecycle은 유지하고, 범용 JSON 표시를 업무별 문서 Renderer와 History/Result Read Model로 확장한다. 구체 실행 목표: 결재 업무유형별 전용/동적 Renderer 제공

- **개발 상세 단계:**
  1. `Backoffice UI generic StructuredDataView 제거/보완`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `결재 업무유형별 전용/동적 Renderer 제공` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Backoffice 승인 Lifecycle/위임/재요청 회귀, 민감정보 노출, UI dead route
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Backoffice approval service/repository tests, OpenAPI validation, frontend generated-client consumer test
- **로컬 Runtime 검증 계획:** Backoffice Browser/API/DB: Inbox/Search→업무별 상세→승인/반려/회수/재요청→실제 적용→History/Result 재조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-05-03/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-05.04 — Approval ID 직접조회 의존 축소 — 목록/검색/Inbox에서 상세로 진입하는 정상 UX 제공

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** MBW에는 submit/detail/inbox/decision/withdraw/cancel/resubmit/expire 및 Approval DB 테이블이 존재한다. 다만 화면은 범용 StructuredDataView 중심이고 History read/search, 업무별 문서, execution result 연결이 충분하지 않다.
- **실제 호출/Consumer 경로:** `Backoffice Approval UI → Generated Client/API → BackofficeApprovalPolicyService → Repository → MBW_APPROVAL_* → 실제 Business Apply/History`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-backoffice-web/frontend/src/features/approvals/**`
  - `cpf-backoffice-web/frontend/src/shared/components/StructuredDataView.vue`
  - `cpf-backoffice/openapi/**`
  - `cpf-tools/db/canonical/platform-schema.json (MBW_APPROVAL_*)`
- **계획된 구체 개발내용:** MBW 기존 Approval Lifecycle은 유지하고, 범용 JSON 표시를 업무별 문서 Renderer와 History/Result Read Model로 확장한다. 구체 실행 목표: 목록/검색/Inbox에서 상세로 진입하는 정상 UX 제공

- **개발 상세 단계:**
  1. 현재 write/history table과 기존 read query 존재 여부를 확인해 INSERT-only 또는 제한 조회 Gap을 확정한다.
  2. 기간/문서번호/요청번호/요청자/결재자/조직/유형/상태/시스템/대상/transactionId 등 canonical search contract를 정의한다.
  3. Repository SQL → Service → Controller/OpenAPI → Generated Client → UI를 한 번에 연결하고 paging/sort whitelist를 적용한다.
  4. History detail에 before/after, decision comment, actor, timestamp, snapshot/version, execution/recovery result를 조립한다.
  5. 권한 없는 조직/첨부/민감 필드가 검색 결과나 상세에서 노출되지 않는지 Runtime negative test를 수행한다.
- **Side Effect / 회귀 필수범위:** Backoffice 승인 Lifecycle/위임/재요청 회귀, 민감정보 노출, UI dead route
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Backoffice approval service/repository tests, OpenAPI validation, frontend generated-client consumer test
- **로컬 Runtime 검증 계획:** Backoffice Browser/API/DB: Inbox/Search→업무별 상세→승인/반려/회수/재요청→실제 적용→History/Result 재조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-05-04/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-05.05 — MBW History read API — MBW_APPROVAL_HISTORY 실제 조회 Repository/API/OpenAPI 제공

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Canonical DB에는 `MBW_APPROVAL_HISTORY`가 존재하고 Service는 `insertHistory()`를 호출한다. 현재 `BackofficeApprovalPolicyRepository`에는 `historyActionExists()`와 `insertHistory()`는 있으나 결재문서별 History를 반환하는 정식 read Repository/API는 확인되지 않았다.
- **실제 호출/Consumer 경로:** `Backoffice Approval UI → Generated Client/API → BackofficeApprovalPolicyService → Repository → MBW_APPROVAL_* → 실제 Business Apply/History`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-backoffice-web/frontend/src/features/approvals/**`
  - `cpf-backoffice-web/frontend/src/shared/components/StructuredDataView.vue`
  - `cpf-backoffice/openapi/**`
  - `cpf-tools/db/canonical/platform-schema.json (MBW_APPROVAL_*)`
- **계획된 구체 개발내용:** MBW 기존 Approval Lifecycle은 유지하고, 범용 JSON 표시를 업무별 문서 Renderer와 History/Result Read Model로 확장한다. 구체 실행 목표: MBW_APPROVAL_HISTORY 실제 조회 Repository/API/OpenAPI 제공

- **개발 상세 단계:**
  1. 현재 write/history table과 기존 read query 존재 여부를 확인해 INSERT-only 또는 제한 조회 Gap을 확정한다.
  2. 기간/문서번호/요청번호/요청자/결재자/조직/유형/상태/시스템/대상/transactionId 등 canonical search contract를 정의한다.
  3. Repository SQL → Service → Controller/OpenAPI → Generated Client → UI를 한 번에 연결하고 paging/sort whitelist를 적용한다.
  4. History detail에 before/after, decision comment, actor, timestamp, snapshot/version, execution/recovery result를 조립한다.
  5. 권한 없는 조직/첨부/민감 필드가 검색 결과나 상세에서 노출되지 않는지 Runtime negative test를 수행한다.
- **Side Effect / 회귀 필수범위:** Backoffice 승인 Lifecycle/위임/재요청 회귀, 민감정보 노출, UI dead route
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Backoffice approval service/repository tests, OpenAPI validation, frontend generated-client consumer test
- **로컬 Runtime 검증 계획:** Backoffice Browser/API/DB: Inbox/Search→업무별 상세→승인/반려/회수/재요청→실제 적용→History/Result 재조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-05-05/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-05.06 — MBW History 검색/Paging/Sorting — 기간/문서번호/요청자/결재자/조직/유형/상태/대상 조건 지원

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** MBW에는 submit/detail/inbox/decision/withdraw/cancel/resubmit/expire 및 Approval DB 테이블이 존재한다. 다만 화면은 범용 StructuredDataView 중심이고 History read/search, 업무별 문서, execution result 연결이 충분하지 않다.
- **실제 호출/Consumer 경로:** `Backoffice Approval UI → Generated Client/API → BackofficeApprovalPolicyService → Repository → MBW_APPROVAL_* → 실제 Business Apply/History`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-backoffice-web/frontend/src/features/approvals/**`
  - `cpf-backoffice-web/frontend/src/shared/components/StructuredDataView.vue`
  - `cpf-backoffice/openapi/**`
  - `cpf-tools/db/canonical/platform-schema.json (MBW_APPROVAL_*)`
- **계획된 구체 개발내용:** MBW 기존 Approval Lifecycle은 유지하고, 범용 JSON 표시를 업무별 문서 Renderer와 History/Result Read Model로 확장한다. 구체 실행 목표: 기간/문서번호/요청자/결재자/조직/유형/상태/대상 조건 지원

- **개발 상세 단계:**
  1. 현재 write/history table과 기존 read query 존재 여부를 확인해 INSERT-only 또는 제한 조회 Gap을 확정한다.
  2. 기간/문서번호/요청번호/요청자/결재자/조직/유형/상태/시스템/대상/transactionId 등 canonical search contract를 정의한다.
  3. Repository SQL → Service → Controller/OpenAPI → Generated Client → UI를 한 번에 연결하고 paging/sort whitelist를 적용한다.
  4. History detail에 before/after, decision comment, actor, timestamp, snapshot/version, execution/recovery result를 조립한다.
  5. 권한 없는 조직/첨부/민감 필드가 검색 결과나 상세에서 노출되지 않는지 Runtime negative test를 수행한다.
- **Side Effect / 회귀 필수범위:** Backoffice 승인 Lifecycle/위임/재요청 회귀, 민감정보 노출, UI dead route
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Backoffice approval service/repository tests, OpenAPI validation, frontend generated-client consumer test
- **로컬 Runtime 검증 계획:** Backoffice Browser/API/DB: Inbox/Search→업무별 상세→승인/반려/회수/재요청→실제 적용→History/Result 재조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-05-06/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-05.07 — MBW Before/After — payload JSON 문자열이 아닌 업무 필드별 비교 모델 제공

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** MBW에는 submit/detail/inbox/decision/withdraw/cancel/resubmit/expire 및 Approval DB 테이블이 존재한다. 다만 화면은 범용 StructuredDataView 중심이고 History read/search, 업무별 문서, execution result 연결이 충분하지 않다.
- **실제 호출/Consumer 경로:** `Backoffice Approval UI → Generated Client/API → BackofficeApprovalPolicyService → Repository → MBW_APPROVAL_* → 실제 Business Apply/History`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-backoffice-web/frontend/src/features/approvals/**`
  - `cpf-backoffice-web/frontend/src/shared/components/StructuredDataView.vue`
  - `cpf-backoffice/openapi/**`
  - `cpf-tools/db/canonical/platform-schema.json (MBW_APPROVAL_*)`
- **계획된 구체 개발내용:** MBW 기존 Approval Lifecycle은 유지하고, 범용 JSON 표시를 업무별 문서 Renderer와 History/Result Read Model로 확장한다. 구체 실행 목표: payload JSON 문자열이 아닌 업무 필드별 비교 모델 제공

- **개발 상세 단계:**
  1. `AdmApprovalService.sanitizeDetail()`과 `AdmApprovalSensitiveDetailR6Test`의 현재 전체 payload 제거 정책을 기준선으로 고정한다.
  2. 원본 Snapshot을 외부에 그대로 노출하지 않고 업무판단용 safe read model을 만드는 Masking/Projection 계층을 분리한다.
  3. password/token/secret/credential/개인정보는 field-level masking하고, 비민감 변경값·업무식별자·Before/After는 유지한다.
  4. 승인 시에는 safe read model이 아니라 저장된 Snapshot hash/version과 원본 canonical payload를 Backend에서 다시 검증한다.
  5. 민감정보 raw 노출 0과 판단정보 누락 0을 양·음수 테스트 및 Browser E2E로 동시에 확인한다.
- **Side Effect / 회귀 필수범위:** Backoffice 승인 Lifecycle/위임/재요청 회귀, 민감정보 노출, UI dead route
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Backoffice approval service/repository tests, OpenAPI validation, frontend generated-client consumer test
- **로컬 Runtime 검증 계획:** Backoffice Browser/API/DB: Inbox/Search→업무별 상세→승인/반려/회수/재요청→실제 적용→History/Result 재조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-05-07/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-05.08 — MBW Attachment detail — attachment_group_id를 실제 Attachment 조회/권한과 연결

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** MBW에는 submit/detail/inbox/decision/withdraw/cancel/resubmit/expire 및 Approval DB 테이블이 존재한다. 다만 화면은 범용 StructuredDataView 중심이고 History read/search, 업무별 문서, execution result 연결이 충분하지 않다.
- **실제 호출/Consumer 경로:** `Backoffice Approval UI → Generated Client/API → BackofficeApprovalPolicyService → Repository → MBW_APPROVAL_* → 실제 Business Apply/History`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-backoffice-web/frontend/src/features/approvals/**`
  - `cpf-backoffice-web/frontend/src/shared/components/StructuredDataView.vue`
  - `cpf-backoffice/openapi/**`
  - `cpf-tools/db/canonical/platform-schema.json (MBW_APPROVAL_*)`
- **계획된 구체 개발내용:** MBW 기존 Approval Lifecycle은 유지하고, 범용 JSON 표시를 업무별 문서 Renderer와 History/Result Read Model로 확장한다. 구체 실행 목표: attachment_group_id를 실제 Attachment 조회/권한과 연결

- **개발 상세 단계:**
  1. 현재 `attachment_group_id`/reference가 저장되는 위치와 실제 Attachment service/repository Owner를 확인한다.
  2. 결재 상세 Read Model에 파일명/크기/유형/등록자/등록시각/검증상태 등 판단용 metadata를 추가한다.
  3. 다운로드/미리보기는 업무권한과 Approval 접근권한을 재검증하고 직접 object key/secret을 노출하지 않는다.
  4. 삭제/만료/권한없음/악성 또는 무결성 실패 Attachment는 승인 가능 상태를 차단하거나 명시 오류로 표시한다.
  5. History 재조회 시 과거 Attachment reference가 유지되는지 Upgrade/Runtime에서 확인한다.
- **Side Effect / 회귀 필수범위:** Backoffice 승인 Lifecycle/위임/재요청 회귀, 민감정보 노출, UI dead route
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Backoffice approval service/repository tests, OpenAPI validation, frontend generated-client consumer test
- **로컬 Runtime 검증 계획:** Backoffice Browser/API/DB: Inbox/Search→업무별 상세→승인/반려/회수/재요청→실제 적용→History/Result 재조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-05-08/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-05.09 — MBW Execution/Apply result — 승인 후 실제 업무 적용 상태/실패/복구/결과 저장 및 재조회

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** MBW에는 submit/detail/inbox/decision/withdraw/cancel/resubmit/expire 및 Approval DB 테이블이 존재한다. 다만 화면은 범용 StructuredDataView 중심이고 History read/search, 업무별 문서, execution result 연결이 충분하지 않다.
- **실제 호출/Consumer 경로:** `Backoffice Approval UI → Generated Client/API → BackofficeApprovalPolicyService → Repository → MBW_APPROVAL_* → 실제 Business Apply/History`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-backoffice-web/frontend/src/features/approvals/**`
  - `cpf-backoffice-web/frontend/src/shared/components/StructuredDataView.vue`
  - `cpf-backoffice/openapi/**`
  - `cpf-tools/db/canonical/platform-schema.json (MBW_APPROVAL_*)`
- **계획된 구체 개발내용:** MBW 기존 Approval Lifecycle은 유지하고, 범용 JSON 표시를 업무별 문서 Renderer와 History/Result Read Model로 확장한다. 구체 실행 목표: 승인 후 실제 업무 적용 상태/실패/복구/결과 저장 및 재조회

- **개발 상세 단계:**
  1. Approval `APPROVED`와 실제 Owner Command/business apply의 상태를 분리해 실행 correlation key를 확정한다.
  2. EXECUTING/EXECUTED/FAILED/UNKNOWN/RECONCILING/RECOVERED 상태와 result code/message/timestamp를 저장·조회한다.
  3. Owner Adapter에서 반환되는 실제 결과와 Approval execution record를 멱등하게 연결한다.
  4. 재시도/복구 시 동일 승인 Snapshot과 execution lineage를 유지하고 중복 실행을 차단한다.
  5. UI 상세/History에서 승인 상태와 실제 적용 결과를 함께 보여주고 실패/UNKNOWN을 숨기지 않는다.
- **Side Effect / 회귀 필수범위:** Backoffice 승인 Lifecycle/위임/재요청 회귀, 민감정보 노출, UI dead route
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Backoffice approval service/repository tests, OpenAPI validation, frontend generated-client consumer test
- **로컬 Runtime 검증 계획:** Backoffice Browser/API/DB: Inbox/Search→업무별 상세→승인/반려/회수/재요청→실제 적용→History/Result 재조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-05-09/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-05.10 — Resubmit lineage — previous approval/snapshot/version/이력 연결과 과거문서 보존

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** MBW에는 submit/detail/inbox/decision/withdraw/cancel/resubmit/expire 및 Approval DB 테이블이 존재한다. 다만 화면은 범용 StructuredDataView 중심이고 History read/search, 업무별 문서, execution result 연결이 충분하지 않다.
- **실제 호출/Consumer 경로:** `Backoffice Approval UI → Generated Client/API → BackofficeApprovalPolicyService → Repository → MBW_APPROVAL_* → 실제 Business Apply/History`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-backoffice-web/frontend/src/features/approvals/**`
  - `cpf-backoffice-web/frontend/src/shared/components/StructuredDataView.vue`
  - `cpf-backoffice/openapi/**`
  - `cpf-tools/db/canonical/platform-schema.json (MBW_APPROVAL_*)`
- **계획된 구체 개발내용:** MBW 기존 Approval Lifecycle은 유지하고, 범용 JSON 표시를 업무별 문서 Renderer와 History/Result Read Model로 확장한다. 구체 실행 목표: previous approval/snapshot/version/이력 연결과 과거문서 보존

- **개발 상세 단계:**
  1. Codex E2E27과 동일 Source에서 최신 MBR Segment INSERT 오류/DB 상태를 재현해 정확한 SQL statement와 bind 값을 확보한다.
  2. `platform-schema.json`의 `CPF_TRANSACTION_SEGMENT` 정의와 immutable V115, generated DB3 schema, mapper template/output을 필드 단위로 비교한다.
  3. `TransactionSegmentMapper`/adapter가 executionId, transactionId, parentSegmentId, System6, operationId를 동일 명칭·순서·타입으로 bind하는지 확인한다.
  4. Generated Domain operation boundary에서 Segment scope start/finish/failure가 실제로 호출되고 transaction context가 소실되지 않는지 추적한다.
  5. Oracle/PostgreSQL/MariaDB projection과 Runtime mapper를 재생성한 뒤 drift 0을 확인한다.
  6. Fresh MBR+Control Plane+CEC+Worker2 E2E에서 Summary/Segment/Lineage/Timeline이 동일 root transaction으로 물리 저장되는지 검증한다.
- **Side Effect / 회귀 필수범위:** Backoffice 승인 Lifecycle/위임/재요청 회귀, 민감정보 노출, UI dead route
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Backoffice approval service/repository tests, OpenAPI validation, frontend generated-client consumer test
- **로컬 Runtime 검증 계획:** Backoffice Browser/API/DB: Inbox/Search→업무별 상세→승인/반려/회수/재요청→실제 적용→History/Result 재조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-05-10/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-05.11 — Concurrency/SoD/optimistic lock 회귀 — 동시승인/자기승인/위임/ALL-ANY-N_OF_M 상태전이 검증

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** MBW에는 submit/detail/inbox/decision/withdraw/cancel/resubmit/expire 및 Approval DB 테이블이 존재한다. 다만 화면은 범용 StructuredDataView 중심이고 History read/search, 업무별 문서, execution result 연결이 충분하지 않다.
- **실제 호출/Consumer 경로:** `Backoffice Approval UI → Generated Client/API → BackofficeApprovalPolicyService → Repository → MBW_APPROVAL_* → 실제 Business Apply/History`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-backoffice-web/frontend/src/features/approvals/**`
  - `cpf-backoffice-web/frontend/src/shared/components/StructuredDataView.vue`
  - `cpf-backoffice/openapi/**`
  - `cpf-tools/db/canonical/platform-schema.json (MBW_APPROVAL_*)`
- **계획된 구체 개발내용:** MBW 기존 Approval Lifecycle은 유지하고, 범용 JSON 표시를 업무별 문서 Renderer와 History/Result Read Model로 확장한다. 구체 실행 목표: 동시승인/자기승인/위임/ALL-ANY-N_OF_M 상태전이 검증

- **개발 상세 단계:**
  1. `Concurrency/SoD/optimistic lock 회귀`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `동시승인/자기승인/위임/ALL-ANY-N_OF_M 상태전이 검증` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Backoffice 승인 Lifecycle/위임/재요청 회귀, 민감정보 노출, UI dead route
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Backoffice approval service/repository tests, OpenAPI validation, frontend generated-client consumer test
- **로컬 Runtime 검증 계획:** Backoffice Browser/API/DB: Inbox/Search→업무별 상세→승인/반려/회수/재요청→실제 적용→History/Result 재조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-05-11/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-05.12 — Backoffice Browser E2E — Generated backoffice client로 Inbox→상세→결정→History/Result 조회 검증

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL(Requirement Gap)`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** MBW에는 submit/detail/inbox/decision/withdraw/cancel/resubmit/expire 및 Approval DB 테이블이 존재한다. 다만 화면은 범용 StructuredDataView 중심이고 History read/search, 업무별 문서, execution result 연결이 충분하지 않다.
- **실제 호출/Consumer 경로:** `Backoffice Approval UI → Generated Client/API → BackofficeApprovalPolicyService → Repository → MBW_APPROVAL_* → 실제 Business Apply/History`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-backoffice-web/frontend/src/features/approvals/**`
  - `cpf-backoffice-web/frontend/src/shared/components/StructuredDataView.vue`
  - `cpf-backoffice/openapi/**`
  - `cpf-tools/db/canonical/platform-schema.json (MBW_APPROVAL_*)`
- **계획된 구체 개발내용:** MBW 기존 Approval Lifecycle은 유지하고, 범용 JSON 표시를 업무별 문서 Renderer와 History/Result Read Model로 확장한다. 구체 실행 목표: Generated backoffice client로 Inbox→상세→결정→History/Result 조회 검증

- **개발 상세 단계:**
  1. 현재 write/history table과 기존 read query 존재 여부를 확인해 INSERT-only 또는 제한 조회 Gap을 확정한다.
  2. 기간/문서번호/요청번호/요청자/결재자/조직/유형/상태/시스템/대상/transactionId 등 canonical search contract를 정의한다.
  3. Repository SQL → Service → Controller/OpenAPI → Generated Client → UI를 한 번에 연결하고 paging/sort whitelist를 적용한다.
  4. History detail에 before/after, decision comment, actor, timestamp, snapshot/version, execution/recovery result를 조립한다.
  5. 권한 없는 조직/첨부/민감 필드가 검색 결과나 상세에서 노출되지 않는지 Runtime negative test를 수행한다.
- **Side Effect / 회귀 필수범위:** Backoffice 승인 Lifecycle/위임/재요청 회귀, 민감정보 노출, UI dead route
- **DB3 영향:** CONDITIONAL — Persistence/Query/Schema/Seed를 수정하면 즉시 DB3 전체 Lifecycle 대상으로 승격.
- **정적검증 계획:** Backoffice approval service/repository tests, OpenAPI validation, frontend generated-client consumer test
- **로컬 Runtime 검증 계획:** Backoffice Browser/API/DB: Inbox/Search→업무별 상세→승인/반려/회수/재요청→실제 적용→History/Result 재조회.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-05-12/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-06.01 — Approval Canonical DB Model Gap 분석 — Document/Snapshot/ChangeSet/ExecutionResult/Audit 구조가 신규 Requirement를 충족하는지 확정

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `미실행`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Approval 신규 요구를 충족하기 위해 DB/API/Frontend/Runtime 변경이 실제 필요한지 먼저 세부 Gap을 확정해야 한다. DB 변경이 발생하는 순간 DB3 전체 Lifecycle이 동일 WP에 강제된다.
- **실제 호출/Consumer 경로:** `Approval Request → Snapshot/Document → Decision → Owner Runtime Execute → Result/Audit → 과거 History Read → Frontend 재조회`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/**`
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-backoffice/openapi/**`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Approval 변경으로 필요한 DB/API/Runtime 계약을 하나의 Root Cause 묶음으로 설계하고, DB 변경 시 append-only DB3 변경으로 구현한다. 구체 실행 목표: Document/Snapshot/ChangeSet/ExecutionResult/Audit 구조가 신규 Requirement를 충족하는지 확정

- **개발 상세 단계:**
  1. Approval Request Version, Business Snapshot Version, Policy Version, Parameter Snapshot, ChangeSet hash를 canonical field로 확정한다.
  2. 요청 시점에 Snapshot/hash/version을 immutable하게 저장하고 후속 update로 원본이 덮이지 않도록 한다.
  3. 상세 조회는 현재 원본이 아니라 승인 당시 Snapshot을 기준으로 문서를 구성한다.
  4. 승인 직전 저장된 version/hash/status/auth를 다시 검증하고 stale/conflict면 승인 실행을 차단한다.
  5. 승인 후 Owner 실행은 승인된 Snapshot을 사용하며 원본 변경 시 재요청/재검토 정책을 적용한다.
- **Side Effect / 회귀 필수범위:** 과거 결재데이터 손실, Upgrade 불일치, API/DB/Frontend contract drift
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB model/contract test, migration checksum/order, approval API/DTO/OpenAPI compatibility, existing-data migration fixtures
- **로컬 Runtime 검증 계획:** Approval DB 변경 시 Oracle/PostgreSQL/MariaDB 각각 Fresh/Upgrade/Rollback-Recovery + 기존결재 데이터 보존 + API/Browser E2E.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-06-01/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-06.02 — ADM Approval DB 변경 설계 — 필요 컬럼/테이블/metadata 변경은 Canonical Source first

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `미실행`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Approval 신규 요구를 충족하기 위해 DB/API/Frontend/Runtime 변경이 실제 필요한지 먼저 세부 Gap을 확정해야 한다. DB 변경이 발생하는 순간 DB3 전체 Lifecycle이 동일 WP에 강제된다.
- **실제 호출/Consumer 경로:** `Approval Request → Snapshot/Document → Decision → Owner Runtime Execute → Result/Audit → 과거 History Read → Frontend 재조회`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/**`
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-backoffice/openapi/**`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Approval 변경으로 필요한 DB/API/Runtime 계약을 하나의 Root Cause 묶음으로 설계하고, DB 변경 시 append-only DB3 변경으로 구현한다. 구체 실행 목표: 필요 컬럼/테이블/metadata 변경은 Canonical Source first

- **개발 상세 단계:**
  1. `ADM Approval DB 변경 설계`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `필요 컬럼/테이블/metadata 변경은 Canonical Source first` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** 과거 결재데이터 손실, Upgrade 불일치, API/DB/Frontend contract drift
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB model/contract test, migration checksum/order, approval API/DTO/OpenAPI compatibility, existing-data migration fixtures
- **로컬 Runtime 검증 계획:** Approval DB 변경 시 Oracle/PostgreSQL/MariaDB 각각 Fresh/Upgrade/Rollback-Recovery + 기존결재 데이터 보존 + API/Browser E2E.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-06-02/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-06.03 — MBW Approval DB 변경 설계 — 과거 문서/이력/실행결과 보존을 포함한 append-only 변경

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `미실행`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Approval 신규 요구를 충족하기 위해 DB/API/Frontend/Runtime 변경이 실제 필요한지 먼저 세부 Gap을 확정해야 한다. DB 변경이 발생하는 순간 DB3 전체 Lifecycle이 동일 WP에 강제된다.
- **실제 호출/Consumer 경로:** `Approval Request → Snapshot/Document → Decision → Owner Runtime Execute → Result/Audit → 과거 History Read → Frontend 재조회`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/**`
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-backoffice/openapi/**`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Approval 변경으로 필요한 DB/API/Runtime 계약을 하나의 Root Cause 묶음으로 설계하고, DB 변경 시 append-only DB3 변경으로 구현한다. 구체 실행 목표: 과거 문서/이력/실행결과 보존을 포함한 append-only 변경

- **개발 상세 단계:**
  1. `MBW Approval DB 변경 설계`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `과거 문서/이력/실행결과 보존을 포함한 append-only 변경` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** 과거 결재데이터 손실, Upgrade 불일치, API/DB/Frontend contract drift
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB model/contract test, migration checksum/order, approval API/DTO/OpenAPI compatibility, existing-data migration fixtures
- **로컬 Runtime 검증 계획:** Approval DB 변경 시 Oracle/PostgreSQL/MariaDB 각각 Fresh/Upgrade/Rollback-Recovery + 기존결재 데이터 보존 + API/Browser E2E.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-06-03/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-06.04 — Approval API/OpenAPI 동시 확장 — DB/API/Generated Client/Frontend contract 단일 변경단위

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `미실행`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Approval 신규 요구를 충족하기 위해 DB/API/Frontend/Runtime 변경이 실제 필요한지 먼저 세부 Gap을 확정해야 한다. DB 변경이 발생하는 순간 DB3 전체 Lifecycle이 동일 WP에 강제된다.
- **실제 호출/Consumer 경로:** `Approval Request → Snapshot/Document → Decision → Owner Runtime Execute → Result/Audit → 과거 History Read → Frontend 재조회`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/**`
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-backoffice/openapi/**`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Approval 변경으로 필요한 DB/API/Runtime 계약을 하나의 Root Cause 묶음으로 설계하고, DB 변경 시 append-only DB3 변경으로 구현한다. 구체 실행 목표: DB/API/Generated Client/Frontend contract 단일 변경단위

- **개발 상세 단계:**
  1. `Approval API/OpenAPI 동시 확장`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `DB/API/Generated Client/Frontend contract 단일 변경단위` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** 과거 결재데이터 손실, Upgrade 불일치, API/DB/Frontend contract drift
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB model/contract test, migration checksum/order, approval API/DTO/OpenAPI compatibility, existing-data migration fixtures
- **로컬 Runtime 검증 계획:** Approval DB 변경 시 Oracle/PostgreSQL/MariaDB 각각 Fresh/Upgrade/Rollback-Recovery + 기존결재 데이터 보존 + API/Browser E2E.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-06-04/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-06.05 — Approval Existing Data preservation — Upgrade 후 기존 request/document/history/snapshot 조회 유지

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `미실행`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Approval 신규 요구를 충족하기 위해 DB/API/Frontend/Runtime 변경이 실제 필요한지 먼저 세부 Gap을 확정해야 한다. DB 변경이 발생하는 순간 DB3 전체 Lifecycle이 동일 WP에 강제된다.
- **실제 호출/Consumer 경로:** `Approval Request → Snapshot/Document → Decision → Owner Runtime Execute → Result/Audit → 과거 History Read → Frontend 재조회`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/**`
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-backoffice/openapi/**`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Approval 변경으로 필요한 DB/API/Runtime 계약을 하나의 Root Cause 묶음으로 설계하고, DB 변경 시 append-only DB3 변경으로 구현한다. 구체 실행 목표: Upgrade 후 기존 request/document/history/snapshot 조회 유지

- **개발 상세 단계:**
  1. 현재 write/history table과 기존 read query 존재 여부를 확인해 INSERT-only 또는 제한 조회 Gap을 확정한다.
  2. 기간/문서번호/요청번호/요청자/결재자/조직/유형/상태/시스템/대상/transactionId 등 canonical search contract를 정의한다.
  3. Repository SQL → Service → Controller/OpenAPI → Generated Client → UI를 한 번에 연결하고 paging/sort whitelist를 적용한다.
  4. History detail에 before/after, decision comment, actor, timestamp, snapshot/version, execution/recovery result를 조립한다.
  5. 권한 없는 조직/첨부/민감 필드가 검색 결과나 상세에서 노출되지 않는지 Runtime negative test를 수행한다.
- **Side Effect / 회귀 필수범위:** 과거 결재데이터 손실, Upgrade 불일치, API/DB/Frontend contract drift
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB model/contract test, migration checksum/order, approval API/DTO/OpenAPI compatibility, existing-data migration fixtures
- **로컬 Runtime 검증 계획:** Approval DB 변경 시 Oracle/PostgreSQL/MariaDB 각각 Fresh/Upgrade/Rollback-Recovery + 기존결재 데이터 보존 + API/Browser E2E.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-06-05/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-06.06 — Approval Runtime result persistence — 실제 Owner command/business apply 결과와 Recovery/Reconcile 결과 영속화

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `미실행`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Approval 신규 요구를 충족하기 위해 DB/API/Frontend/Runtime 변경이 실제 필요한지 먼저 세부 Gap을 확정해야 한다. DB 변경이 발생하는 순간 DB3 전체 Lifecycle이 동일 WP에 강제된다.
- **실제 호출/Consumer 경로:** `Approval Request → Snapshot/Document → Decision → Owner Runtime Execute → Result/Audit → 과거 History Read → Frontend 재조회`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/**`
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-backoffice/openapi/**`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Approval 변경으로 필요한 DB/API/Runtime 계약을 하나의 Root Cause 묶음으로 설계하고, DB 변경 시 append-only DB3 변경으로 구현한다. 구체 실행 목표: 실제 Owner command/business apply 결과와 Recovery/Reconcile 결과 영속화

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `Approval Runtime result persistence` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** 과거 결재데이터 손실, Upgrade 불일치, API/DB/Frontend contract drift
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB model/contract test, migration checksum/order, approval API/DTO/OpenAPI compatibility, existing-data migration fixtures
- **로컬 Runtime 검증 계획:** Approval DB 변경 시 Oracle/PostgreSQL/MariaDB 각각 Fresh/Upgrade/Rollback-Recovery + 기존결재 데이터 보존 + API/Browser E2E.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-06-06/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-06.07 — Approval Audit/Trace — transactionId/operationId/trace/instance/actor/action/result 연결

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `미실행`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Approval 신규 요구를 충족하기 위해 DB/API/Frontend/Runtime 변경이 실제 필요한지 먼저 세부 Gap을 확정해야 한다. DB 변경이 발생하는 순간 DB3 전체 Lifecycle이 동일 WP에 강제된다.
- **실제 호출/Consumer 경로:** `Approval Request → Snapshot/Document → Decision → Owner Runtime Execute → Result/Audit → 과거 History Read → Frontend 재조회`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/**`
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-backoffice/openapi/**`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Approval 변경으로 필요한 DB/API/Runtime 계약을 하나의 Root Cause 묶음으로 설계하고, DB 변경 시 append-only DB3 변경으로 구현한다. 구체 실행 목표: transactionId/operationId/trace/instance/actor/action/result 연결

- **개발 상세 단계:**
  1. `Approval Audit/Trace`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `transactionId/operationId/trace/instance/actor/action/result 연결` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** 과거 결재데이터 손실, Upgrade 불일치, API/DB/Frontend contract drift
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB model/contract test, migration checksum/order, approval API/DTO/OpenAPI compatibility, existing-data migration fixtures
- **로컬 Runtime 검증 계획:** Approval DB 변경 시 Oracle/PostgreSQL/MariaDB 각각 Fresh/Upgrade/Rollback-Recovery + 기존결재 데이터 보존 + API/Browser E2E.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-06-07/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-06.08 — Approval security negative — 401/403/404/409/429/500/503, stale version, self approval, attachment denial

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `미실행`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Approval 신규 요구를 충족하기 위해 DB/API/Frontend/Runtime 변경이 실제 필요한지 먼저 세부 Gap을 확정해야 한다. DB 변경이 발생하는 순간 DB3 전체 Lifecycle이 동일 WP에 강제된다.
- **실제 호출/Consumer 경로:** `Approval Request → Snapshot/Document → Decision → Owner Runtime Execute → Result/Audit → 과거 History Read → Frontend 재조회`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/**`
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-backoffice/openapi/**`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Approval 변경으로 필요한 DB/API/Runtime 계약을 하나의 Root Cause 묶음으로 설계하고, DB 변경 시 append-only DB3 변경으로 구현한다. 구체 실행 목표: 401/403/404/409/429/500/503, stale version, self approval, attachment denial

- **개발 상세 단계:**
  1. 현재 `attachment_group_id`/reference가 저장되는 위치와 실제 Attachment service/repository Owner를 확인한다.
  2. 결재 상세 Read Model에 파일명/크기/유형/등록자/등록시각/검증상태 등 판단용 metadata를 추가한다.
  3. 다운로드/미리보기는 업무권한과 Approval 접근권한을 재검증하고 직접 object key/secret을 노출하지 않는다.
  4. 삭제/만료/권한없음/악성 또는 무결성 실패 Attachment는 승인 가능 상태를 차단하거나 명시 오류로 표시한다.
  5. History 재조회 시 과거 Attachment reference가 유지되는지 Upgrade/Runtime에서 확인한다.
- **Side Effect / 회귀 필수범위:** 과거 결재데이터 손실, Upgrade 불일치, API/DB/Frontend contract drift
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB model/contract test, migration checksum/order, approval API/DTO/OpenAPI compatibility, existing-data migration fixtures
- **로컬 Runtime 검증 계획:** Approval DB 변경 시 Oracle/PostgreSQL/MariaDB 각각 Fresh/Upgrade/Rollback-Recovery + 기존결재 데이터 보존 + API/Browser E2E.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-06-08/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-06.09 — Approval full E2E — request→document→decision→execute→result→history 재조회

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `미실행`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Approval 신규 요구를 충족하기 위해 DB/API/Frontend/Runtime 변경이 실제 필요한지 먼저 세부 Gap을 확정해야 한다. DB 변경이 발생하는 순간 DB3 전체 Lifecycle이 동일 WP에 강제된다.
- **실제 호출/Consumer 경로:** `Approval Request → Snapshot/Document → Decision → Owner Runtime Execute → Result/Audit → 과거 History Read → Frontend 재조회`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/**`
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-backoffice/openapi/**`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Approval 변경으로 필요한 DB/API/Runtime 계약을 하나의 Root Cause 묶음으로 설계하고, DB 변경 시 append-only DB3 변경으로 구현한다. 구체 실행 목표: request→document→decision→execute→result→history 재조회

- **개발 상세 단계:**
  1. 현재 write/history table과 기존 read query 존재 여부를 확인해 INSERT-only 또는 제한 조회 Gap을 확정한다.
  2. 기간/문서번호/요청번호/요청자/결재자/조직/유형/상태/시스템/대상/transactionId 등 canonical search contract를 정의한다.
  3. Repository SQL → Service → Controller/OpenAPI → Generated Client → UI를 한 번에 연결하고 paging/sort whitelist를 적용한다.
  4. History detail에 before/after, decision comment, actor, timestamp, snapshot/version, execution/recovery result를 조립한다.
  5. 권한 없는 조직/첨부/민감 필드가 검색 결과나 상세에서 노출되지 않는지 Runtime negative test를 수행한다.
- **Side Effect / 회귀 필수범위:** 과거 결재데이터 손실, Upgrade 불일치, API/DB/Frontend contract drift
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB model/contract test, migration checksum/order, approval API/DTO/OpenAPI compatibility, existing-data migration fixtures
- **로컬 Runtime 검증 계획:** Approval DB 변경 시 Oracle/PostgreSQL/MariaDB 각각 Fresh/Upgrade/Rollback-Recovery + 기존결재 데이터 보존 + API/Browser E2E.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-06-09/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-06.10 — Codex Cross-check package — Developer GPT 신규 결재 구현 전체를 Codex 후속 독립 검수 대상으로 목록화

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `미실행`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Approval 신규 요구를 충족하기 위해 DB/API/Frontend/Runtime 변경이 실제 필요한지 먼저 세부 Gap을 확정해야 한다. DB 변경이 발생하는 순간 DB3 전체 Lifecycle이 동일 WP에 강제된다.
- **실제 호출/Consumer 경로:** `Approval Request → Snapshot/Document → Decision → Owner Runtime Execute → Result/Audit → 과거 History Read → Frontend 재조회`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-admin/src/main/java/com/cpf/admin/approval/**`
  - `cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/approval/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-backoffice/openapi/**`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Approval 변경으로 필요한 DB/API/Runtime 계약을 하나의 Root Cause 묶음으로 설계하고, DB 변경 시 append-only DB3 변경으로 구현한다. 구체 실행 목표: Developer GPT 신규 결재 구현 전체를 Codex 후속 독립 검수 대상으로 목록화

- **개발 상세 단계:**
  1. `Codex Cross-check package`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `Developer GPT 신규 결재 구현 전체를 Codex 후속 독립 검수 대상으로 목록화` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** 과거 결재데이터 손실, Upgrade 불일치, API/DB/Frontend contract drift
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** DB model/contract test, migration checksum/order, approval API/DTO/OpenAPI compatibility, existing-data migration fixtures
- **로컬 Runtime 검증 계획:** Approval DB 변경 시 Oracle/PostgreSQL/MariaDB 각각 Fresh/Upgrade/Rollback-Recovery + 기존결재 데이터 보존 + API/Browser E2E.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-06-10/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-07.01 — 최상위 정본 DB3 원칙 보강 — 일반 개발 단일 DB 허용 문구를 DB 변경 시 3사 동시 Lifecycle 필수로 수정

- **Origin / 분류:** `QA_GAP+NEW_REQUIREMENT`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `PASS+FAIL 혼재`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `부분`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** DB3 Canonical/Renderer/Lifecycle/Verifier 기반은 강하지만 신규 비협상 기준과 정본 일부가 충돌하며, semantic parity 실패와 실제 3 Vendor Runtime 미검증이 남아 있다.
- **실제 호출/Consumer 경로:** `Canonical DB Definition → Renderer → Initializer/Fresh → Migration/Upgrade → Rollback/Recovery → Seed → Runtime Query/Mapper → Runtime Consumer → Schema Parity`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-tools/db/generated/current/{oracle,postgresql,mariadb}/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/source/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/migration/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/**`
  - `cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py`
  - `cpf-tools/db/verification/**`
- **계획된 구체 개발내용:** DB Canonical Owner를 먼저 수정한 뒤 Initializer와 3 Vendor 산출물·Migration·Seed·Runtime Query를 같은 실행에서 재생성/검증한다. 구체 실행 목표: 일반 개발 단일 DB 허용 문구를 DB 변경 시 3사 동시 Lifecycle 필수로 수정

- **개발 상세 단계:**
  1. `최상위 정본 DB3 원칙 보강`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `일반 개발 단일 DB 허용 문구를 DB 변경 시 3사 동시 Lifecycle 필수로 수정` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Vendor drift, immutable migration 훼손, Fresh/Upgrade schema 불일치, 기존 데이터 손실
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** schema governance, vendor semantic/token parity, lifecycle contract, manifest, immutable checksum, Fresh-vs-Upgrade schema diff
- **로컬 Runtime 검증 계획:** Oracle/PostgreSQL/MariaDB 3사 모두 Empty→Initializer→Seed→Runtime smoke, Previous→Upgrade, Rollback/Recovery→Reapply, schema parity.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-07-01/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-07.02 — Canonical DB Source Owner 확인 — platform-schema/seed/model/renderer owner와 변경 진입점 고정

- **Origin / 분류:** `QA_GAP+NEW_REQUIREMENT`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `PASS+FAIL 혼재`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `부분`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** DB3 Canonical/Renderer/Lifecycle/Verifier 기반은 강하지만 신규 비협상 기준과 정본 일부가 충돌하며, semantic parity 실패와 실제 3 Vendor Runtime 미검증이 남아 있다.
- **실제 호출/Consumer 경로:** `Canonical DB Definition → Renderer → Initializer/Fresh → Migration/Upgrade → Rollback/Recovery → Seed → Runtime Query/Mapper → Runtime Consumer → Schema Parity`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-tools/db/generated/current/{oracle,postgresql,mariadb}/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/source/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/migration/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/**`
  - `cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py`
  - `cpf-tools/db/verification/**`
- **계획된 구체 개발내용:** DB Canonical Owner를 먼저 수정한 뒤 Initializer와 3 Vendor 산출물·Migration·Seed·Runtime Query를 같은 실행에서 재생성/검증한다. 구체 실행 목표: platform-schema/seed/model/renderer owner와 변경 진입점 고정

- **개발 상세 단계:**
  1. `Canonical DB Source Owner 확인`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `platform-schema/seed/model/renderer owner와 변경 진입점 고정` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Vendor drift, immutable migration 훼손, Fresh/Upgrade schema 불일치, 기존 데이터 손실
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** schema governance, vendor semantic/token parity, lifecycle contract, manifest, immutable checksum, Fresh-vs-Upgrade schema diff
- **로컬 Runtime 검증 계획:** Oracle/PostgreSQL/MariaDB 3사 모두 Empty→Initializer→Seed→Runtime smoke, Previous→Upgrade, Rollback/Recovery→Reapply, schema parity.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-07-02/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-07.03 — Initializer/Fresh Init current 확인 — Fresh install이 current production schema를 직접 생성

- **Origin / 분류:** `QA_GAP+NEW_REQUIREMENT`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `PASS+FAIL 혼재`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `부분`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** DB3 Canonical/Renderer/Lifecycle/Verifier 기반은 강하지만 신규 비협상 기준과 정본 일부가 충돌하며, semantic parity 실패와 실제 3 Vendor Runtime 미검증이 남아 있다.
- **실제 호출/Consumer 경로:** `Canonical DB Definition → Renderer → Initializer/Fresh → Migration/Upgrade → Rollback/Recovery → Seed → Runtime Query/Mapper → Runtime Consumer → Schema Parity`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-tools/db/generated/current/{oracle,postgresql,mariadb}/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/source/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/migration/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/**`
  - `cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py`
  - `cpf-tools/db/verification/**`
- **계획된 구체 개발내용:** DB Canonical Owner를 먼저 수정한 뒤 Initializer와 3 Vendor 산출물·Migration·Seed·Runtime Query를 같은 실행에서 재생성/검증한다. 구체 실행 목표: Fresh install이 current production schema를 직접 생성

- **개발 상세 단계:**
  1. `Initializer/Fresh Init current 확인`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `Fresh install이 current production schema를 직접 생성` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Vendor drift, immutable migration 훼손, Fresh/Upgrade schema 불일치, 기존 데이터 손실
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** schema governance, vendor semantic/token parity, lifecycle contract, manifest, immutable checksum, Fresh-vs-Upgrade schema diff
- **로컬 Runtime 검증 계획:** Oracle/PostgreSQL/MariaDB 3사 모두 Empty→Initializer→Seed→Runtime smoke, Previous→Upgrade, Rollback/Recovery→Reapply, schema parity.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-07-03/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-07.04 — Oracle current — DDL/Install/Migration/Seed/Runtime Query/Verify current

- **Origin / 분류:** `QA_GAP+NEW_REQUIREMENT`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `PASS+FAIL 혼재`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `부분`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** DB3 Canonical/Renderer/Lifecycle/Verifier 기반은 강하지만 신규 비협상 기준과 정본 일부가 충돌하며, semantic parity 실패와 실제 3 Vendor Runtime 미검증이 남아 있다.
- **실제 호출/Consumer 경로:** `Canonical DB Definition → Renderer → Initializer/Fresh → Migration/Upgrade → Rollback/Recovery → Seed → Runtime Query/Mapper → Runtime Consumer → Schema Parity`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-tools/db/generated/current/{oracle,postgresql,mariadb}/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/source/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/migration/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/**`
  - `cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py`
  - `cpf-tools/db/verification/**`
- **계획된 구체 개발내용:** DB Canonical Owner를 먼저 수정한 뒤 Initializer와 3 Vendor 산출물·Migration·Seed·Runtime Query를 같은 실행에서 재생성/검증한다. 구체 실행 목표: DDL/Install/Migration/Seed/Runtime Query/Verify current

- **개발 상세 단계:**
  1. Oracle의 canonical render/source/generated-current/install/migration/rollback/runtime query 경로를 inventory한다.
  2. Canonical model/seed 변경이 Oracle 산출물에 누락 없이 투영되는지 table/column/index/FK/seed 단위로 비교한다.
  3. Oracle Empty DB/User에서 Initializer→Seed→Verify→Runtime CRUD/Query를 실제 실행한다.
  4. 지원 이전 버전에서 Upgrade→data preservation→Runtime smoke를 수행한다.
  5. Rollback 또는 명시적 Recovery→Reapply 후 Current Schema parity와 Evidence를 저장한다.
- **Side Effect / 회귀 필수범위:** Vendor drift, immutable migration 훼손, Fresh/Upgrade schema 불일치, 기존 데이터 손실
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** schema governance, vendor semantic/token parity, lifecycle contract, manifest, immutable checksum, Fresh-vs-Upgrade schema diff
- **로컬 Runtime 검증 계획:** Oracle/PostgreSQL/MariaDB 3사 모두 Empty→Initializer→Seed→Runtime smoke, Previous→Upgrade, Rollback/Recovery→Reapply, schema parity.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-07-04/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-07.05 — PostgreSQL current — DDL/Install/Migration/Seed/Runtime Query/Verify current

- **Origin / 분류:** `QA_GAP+NEW_REQUIREMENT`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `PASS+FAIL 혼재`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `부분`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** DB3 Canonical/Renderer/Lifecycle/Verifier 기반은 강하지만 신규 비협상 기준과 정본 일부가 충돌하며, semantic parity 실패와 실제 3 Vendor Runtime 미검증이 남아 있다.
- **실제 호출/Consumer 경로:** `Canonical DB Definition → Renderer → Initializer/Fresh → Migration/Upgrade → Rollback/Recovery → Seed → Runtime Query/Mapper → Runtime Consumer → Schema Parity`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-tools/db/generated/current/{oracle,postgresql,mariadb}/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/source/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/migration/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/**`
  - `cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py`
  - `cpf-tools/db/verification/**`
- **계획된 구체 개발내용:** DB Canonical Owner를 먼저 수정한 뒤 Initializer와 3 Vendor 산출물·Migration·Seed·Runtime Query를 같은 실행에서 재생성/검증한다. 구체 실행 목표: DDL/Install/Migration/Seed/Runtime Query/Verify current

- **개발 상세 단계:**
  1. PostgreSQL의 canonical render/source/generated-current/install/migration/rollback/runtime query 경로를 inventory한다.
  2. Canonical model/seed 변경이 PostgreSQL 산출물에 누락 없이 투영되는지 table/column/index/FK/seed 단위로 비교한다.
  3. PostgreSQL Empty DB/User에서 Initializer→Seed→Verify→Runtime CRUD/Query를 실제 실행한다.
  4. 지원 이전 버전에서 Upgrade→data preservation→Runtime smoke를 수행한다.
  5. Rollback 또는 명시적 Recovery→Reapply 후 Current Schema parity와 Evidence를 저장한다.
- **Side Effect / 회귀 필수범위:** Vendor drift, immutable migration 훼손, Fresh/Upgrade schema 불일치, 기존 데이터 손실
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** schema governance, vendor semantic/token parity, lifecycle contract, manifest, immutable checksum, Fresh-vs-Upgrade schema diff
- **로컬 Runtime 검증 계획:** Oracle/PostgreSQL/MariaDB 3사 모두 Empty→Initializer→Seed→Runtime smoke, Previous→Upgrade, Rollback/Recovery→Reapply, schema parity.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-07-05/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-07.06 — MariaDB current — DDL/Install/Migration/Seed/Runtime Query/Verify current

- **Origin / 분류:** `QA_GAP+NEW_REQUIREMENT`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `PASS+FAIL 혼재`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `부분`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** DB3 Canonical/Renderer/Lifecycle/Verifier 기반은 강하지만 신규 비협상 기준과 정본 일부가 충돌하며, semantic parity 실패와 실제 3 Vendor Runtime 미검증이 남아 있다.
- **실제 호출/Consumer 경로:** `Canonical DB Definition → Renderer → Initializer/Fresh → Migration/Upgrade → Rollback/Recovery → Seed → Runtime Query/Mapper → Runtime Consumer → Schema Parity`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-tools/db/generated/current/{oracle,postgresql,mariadb}/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/source/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/migration/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/**`
  - `cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py`
  - `cpf-tools/db/verification/**`
- **계획된 구체 개발내용:** DB Canonical Owner를 먼저 수정한 뒤 Initializer와 3 Vendor 산출물·Migration·Seed·Runtime Query를 같은 실행에서 재생성/검증한다. 구체 실행 목표: DDL/Install/Migration/Seed/Runtime Query/Verify current

- **개발 상세 단계:**
  1. MariaDB의 canonical render/source/generated-current/install/migration/rollback/runtime query 경로를 inventory한다.
  2. Canonical model/seed 변경이 MariaDB 산출물에 누락 없이 투영되는지 table/column/index/FK/seed 단위로 비교한다.
  3. MariaDB Empty DB/User에서 Initializer→Seed→Verify→Runtime CRUD/Query를 실제 실행한다.
  4. 지원 이전 버전에서 Upgrade→data preservation→Runtime smoke를 수행한다.
  5. Rollback 또는 명시적 Recovery→Reapply 후 Current Schema parity와 Evidence를 저장한다.
- **Side Effect / 회귀 필수범위:** Vendor drift, immutable migration 훼손, Fresh/Upgrade schema 불일치, 기존 데이터 손실
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** schema governance, vendor semantic/token parity, lifecycle contract, manifest, immutable checksum, Fresh-vs-Upgrade schema diff
- **로컬 Runtime 검증 계획:** Oracle/PostgreSQL/MariaDB 3사 모두 Empty→Initializer→Seed→Runtime smoke, Previous→Upgrade, Rollback/Recovery→Reapply, schema parity.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-07-06/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-07.07 — Semantic parity FAIL root cause — productionDefault=false referenceFixture 4개가 source/install에 혼입된 원인 확정/수정

- **Origin / 분류:** `QA_GAP+NEW_REQUIREMENT`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `PASS+FAIL 혼재`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `부분`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** QA에서 DB schema governance/lifecycle/manifest/static token parity는 PASS했지만 `verify-cpf-db-vendor-semantic-parity.py`는 Oracle/PostgreSQL/MariaDB 모두에서 `referenceFixture` 계열 4개 `REF_*` 테이블이 production source/install에 extra라고 판정했다.
- **실제 호출/Consumer 경로:** `Canonical DB Definition → Renderer → Initializer/Fresh → Migration/Upgrade → Rollback/Recovery → Seed → Runtime Query/Mapper → Runtime Consumer → Schema Parity`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-tools/db/generated/current/{oracle,postgresql,mariadb}/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/source/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/migration/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/**`
  - `cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py`
  - `cpf-tools/db/verification/**`
- **계획된 구체 개발내용:** DB Canonical Owner를 먼저 수정한 뒤 Initializer와 3 Vendor 산출물·Migration·Seed·Runtime Query를 같은 실행에서 재생성/검증한다. 구체 실행 목표: productionDefault=false referenceFixture 4개가 source/install에 혼입된 원인 확정/수정

- **개발 상세 단계:**
  1. `verify-cpf-db-vendor-semantic-parity.py`의 extra 4개 `REF_*` 객체를 canonical model의 `productionDefault`/`logicalDatabase` 기준으로 역추적한다.
  2. referenceFixture가 production source/install로 투영되는 renderer/bundle/initializer owner를 정확히 찾아 production projection에서만 제외한다.
  3. immutable historical migration/rollback 내부 REF identifier는 수정하지 않고 current source/install/runtime consumer만 현행화한다.
  4. Canonical source에서 Oracle/PostgreSQL/MariaDB generated/current와 install bundle을 재생성해 semantic parity 0을 확인한다.
  5. 3사 Fresh Init/Upgrade에서 current production schema에는 REF fixture가 없고 필요한 fixture 전용 lifecycle은 별도 유지되는지 Runtime 확인한다.
- **Side Effect / 회귀 필수범위:** Vendor drift, immutable migration 훼손, Fresh/Upgrade schema 불일치, 기존 데이터 손실
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** schema governance, vendor semantic/token parity, lifecycle contract, manifest, immutable checksum, Fresh-vs-Upgrade schema diff
- **로컬 Runtime 검증 계획:** Oracle/PostgreSQL/MariaDB 3사 모두 Empty→Initializer→Seed→Runtime smoke, Previous→Upgrade, Rollback/Recovery→Reapply, schema parity.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-07-07/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-07.08 — Fresh Init vs Upgrade schema parity — table/column/type/default/null/PK/FK/UK/index/identity/seed/metadata diff 0

- **Origin / 분류:** `QA_GAP+NEW_REQUIREMENT`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `PASS+FAIL 혼재`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `부분`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** DB3 Canonical/Renderer/Lifecycle/Verifier 기반은 강하지만 신규 비협상 기준과 정본 일부가 충돌하며, semantic parity 실패와 실제 3 Vendor Runtime 미검증이 남아 있다.
- **실제 호출/Consumer 경로:** `Canonical DB Definition → Renderer → Initializer/Fresh → Migration/Upgrade → Rollback/Recovery → Seed → Runtime Query/Mapper → Runtime Consumer → Schema Parity`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-tools/db/generated/current/{oracle,postgresql,mariadb}/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/source/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/migration/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/**`
  - `cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py`
  - `cpf-tools/db/verification/**`
- **계획된 구체 개발내용:** DB Canonical Owner를 먼저 수정한 뒤 Initializer와 3 Vendor 산출물·Migration·Seed·Runtime Query를 같은 실행에서 재생성/검증한다. 구체 실행 목표: table/column/type/default/null/PK/FK/UK/index/identity/seed/metadata diff 0

- **개발 상세 단계:**
  1. A경로 Empty DB→Current Initializer와 B경로 Supported Previous→Migration Chain을 동일 Vendor별로 준비한다.
  2. 두 결과의 table/column/type/default/null/PK/FK/unique/index/identity/seed/metadata manifest를 생성한다.
  3. Canonical exception이 아닌 차이는 모두 Finding으로 등록하고 Initializer 또는 migration owner를 수정한다.
  4. Existing data preservation을 별도 fixture로 검증해 schema 같음만으로 PASS하지 않는다.
  5. Oracle/PostgreSQL/MariaDB 3사 모두 diff 0일 때만 parity를 PASS 처리한다.
- **Side Effect / 회귀 필수범위:** Vendor drift, immutable migration 훼손, Fresh/Upgrade schema 불일치, 기존 데이터 손실
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** schema governance, vendor semantic/token parity, lifecycle contract, manifest, immutable checksum, Fresh-vs-Upgrade schema diff
- **로컬 Runtime 검증 계획:** Oracle/PostgreSQL/MariaDB 3사 모두 Empty→Initializer→Seed→Runtime smoke, Previous→Upgrade, Rollback/Recovery→Reapply, schema parity.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-07-08/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-07.09 — Immutable migration 보존 — 기존 migration/rollback rewrite/checksum 우회 0

- **Origin / 분류:** `QA_GAP+NEW_REQUIREMENT`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `PASS+FAIL 혼재`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `부분`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** DB3 Canonical/Renderer/Lifecycle/Verifier 기반은 강하지만 신규 비협상 기준과 정본 일부가 충돌하며, semantic parity 실패와 실제 3 Vendor Runtime 미검증이 남아 있다.
- **실제 호출/Consumer 경로:** `Canonical DB Definition → Renderer → Initializer/Fresh → Migration/Upgrade → Rollback/Recovery → Seed → Runtime Query/Mapper → Runtime Consumer → Schema Parity`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-tools/db/generated/current/{oracle,postgresql,mariadb}/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/source/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/migration/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/**`
  - `cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py`
  - `cpf-tools/db/verification/**`
- **계획된 구체 개발내용:** DB Canonical Owner를 먼저 수정한 뒤 Initializer와 3 Vendor 산출물·Migration·Seed·Runtime Query를 같은 실행에서 재생성/검증한다. 구체 실행 목표: 기존 migration/rollback rewrite/checksum 우회 0

- **개발 상세 단계:**
  1. 기존 versioned migration/rollback 파일의 checksum manifest와 현재 bytes를 비교한다.
  2. rename/rewrite/checksum allowlist 우회가 없는지 Repository-wide 검증한다.
  3. Current 변경이 필요한 경우 기존 파일을 수정하지 않고 신규 append-only migration/rollback 또는 recovery를 추가한다.
  4. Fresh Initializer current schema와 신규 migration chain 결과가 동일한지 검증한다.
  5. Codex/Developer GPT Evidence에 기존 immutable 파일 diff 0을 명시한다.
- **Side Effect / 회귀 필수범위:** Vendor drift, immutable migration 훼손, Fresh/Upgrade schema 불일치, 기존 데이터 손실
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** schema governance, vendor semantic/token parity, lifecycle contract, manifest, immutable checksum, Fresh-vs-Upgrade schema diff
- **로컬 Runtime 검증 계획:** Oracle/PostgreSQL/MariaDB 3사 모두 Empty→Initializer→Seed→Runtime smoke, Previous→Upgrade, Rollback/Recovery→Reapply, schema parity.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-07-09/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-07.10 — 3 Vendor Upgrade/Rollback-Recovery — 지원 이전 버전→current→rollback/recovery→reapply 실제 검증

- **Origin / 분류:** `QA_GAP+NEW_REQUIREMENT`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `PASS+FAIL 혼재`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `부분`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** DB3 Canonical/Renderer/Lifecycle/Verifier 기반은 강하지만 신규 비협상 기준과 정본 일부가 충돌하며, semantic parity 실패와 실제 3 Vendor Runtime 미검증이 남아 있다.
- **실제 호출/Consumer 경로:** `Canonical DB Definition → Renderer → Initializer/Fresh → Migration/Upgrade → Rollback/Recovery → Seed → Runtime Query/Mapper → Runtime Consumer → Schema Parity`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-tools/db/generated/current/{oracle,postgresql,mariadb}/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/source/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/migration/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/**`
  - `cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py`
  - `cpf-tools/db/verification/**`
- **계획된 구체 개발내용:** DB Canonical Owner를 먼저 수정한 뒤 Initializer와 3 Vendor 산출물·Migration·Seed·Runtime Query를 같은 실행에서 재생성/검증한다. 구체 실행 목표: 지원 이전 버전→current→rollback/recovery→reapply 실제 검증

- **개발 상세 단계:**
  1. `3 Vendor Upgrade/Rollback-Recovery`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `지원 이전 버전→current→rollback/recovery→reapply 실제 검증` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Vendor drift, immutable migration 훼손, Fresh/Upgrade schema 불일치, 기존 데이터 손실
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** schema governance, vendor semantic/token parity, lifecycle contract, manifest, immutable checksum, Fresh-vs-Upgrade schema diff
- **로컬 Runtime 검증 계획:** Oracle/PostgreSQL/MariaDB 3사 모두 Empty→Initializer→Seed→Runtime smoke, Previous→Upgrade, Rollback/Recovery→Reapply, schema parity.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-07-10/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-07.11 — 3 Vendor Runtime Query consumer — CRUD/lock/lease/paging/sort/date/json/clob 등 실제 Runtime 검증

- **Origin / 분류:** `QA_GAP+NEW_REQUIREMENT`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `PASS+FAIL 혼재`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `부분`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** DB3 Canonical/Renderer/Lifecycle/Verifier 기반은 강하지만 신규 비협상 기준과 정본 일부가 충돌하며, semantic parity 실패와 실제 3 Vendor Runtime 미검증이 남아 있다.
- **실제 호출/Consumer 경로:** `Canonical DB Definition → Renderer → Initializer/Fresh → Migration/Upgrade → Rollback/Recovery → Seed → Runtime Query/Mapper → Runtime Consumer → Schema Parity`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-tools/db/generated/current/{oracle,postgresql,mariadb}/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/source/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/migration/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/**`
  - `cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py`
  - `cpf-tools/db/verification/**`
- **계획된 구체 개발내용:** DB Canonical Owner를 먼저 수정한 뒤 Initializer와 3 Vendor 산출물·Migration·Seed·Runtime Query를 같은 실행에서 재생성/검증한다. 구체 실행 목표: CRUD/lock/lease/paging/sort/date/json/clob 등 실제 Runtime 검증

- **개발 상세 단계:**
  1. 현재 write/history table과 기존 read query 존재 여부를 확인해 INSERT-only 또는 제한 조회 Gap을 확정한다.
  2. 기간/문서번호/요청번호/요청자/결재자/조직/유형/상태/시스템/대상/transactionId 등 canonical search contract를 정의한다.
  3. Repository SQL → Service → Controller/OpenAPI → Generated Client → UI를 한 번에 연결하고 paging/sort whitelist를 적용한다.
  4. History detail에 before/after, decision comment, actor, timestamp, snapshot/version, execution/recovery result를 조립한다.
  5. 권한 없는 조직/첨부/민감 필드가 검색 결과나 상세에서 노출되지 않는지 Runtime negative test를 수행한다.
- **Side Effect / 회귀 필수범위:** Vendor drift, immutable migration 훼손, Fresh/Upgrade schema 불일치, 기존 데이터 손실
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** schema governance, vendor semantic/token parity, lifecycle contract, manifest, immutable checksum, Fresh-vs-Upgrade schema diff
- **로컬 Runtime 검증 계획:** Oracle/PostgreSQL/MariaDB 3사 모두 Empty→Initializer→Seed→Runtime smoke, Previous→Upgrade, Rollback/Recovery→Reapply, schema parity.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-07-11/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-07.12 — DB3 local integrated command — DB 영향 수정 시 Oracle/PostgreSQL/MariaDB Fresh부터 한 번에 검증하는 PowerShell 제공

- **Origin / 분류:** `QA_GAP+NEW_REQUIREMENT`
- **현재 개발상태:** `부분 구현`
- **현재 정적검증:** `PASS+FAIL 혼재`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `부분`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** DB3 Canonical/Renderer/Lifecycle/Verifier 기반은 강하지만 신규 비협상 기준과 정본 일부가 충돌하며, semantic parity 실패와 실제 3 Vendor Runtime 미검증이 남아 있다.
- **실제 호출/Consumer 경로:** `Canonical DB Definition → Renderer → Initializer/Fresh → Migration/Upgrade → Rollback/Recovery → Seed → Runtime Query/Mapper → Runtime Consumer → Schema Parity`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
  - `cpf-tools/db/canonical/platform-schema.json`
  - `cpf-tools/db/canonical/seed-model.json`
  - `cpf-tools/db/generated/current/{oracle,postgresql,mariadb}/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/source/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/migration/**`
  - `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/runtime/**`
  - `cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py`
  - `cpf-tools/db/verification/**`
- **계획된 구체 개발내용:** DB Canonical Owner를 먼저 수정한 뒤 Initializer와 3 Vendor 산출물·Migration·Seed·Runtime Query를 같은 실행에서 재생성/검증한다. 구체 실행 목표: DB 영향 수정 시 Oracle/PostgreSQL/MariaDB Fresh부터 한 번에 검증하는 PowerShell 제공

- **개발 상세 단계:**
  1. `DB3 local integrated command`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `DB 영향 수정 시 Oracle/PostgreSQL/MariaDB Fresh부터 한 번에 검증하는 PowerShell 제공` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Vendor drift, immutable migration 훼손, Fresh/Upgrade schema 불일치, 기존 데이터 손실
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** schema governance, vendor semantic/token parity, lifecycle contract, manifest, immutable checksum, Fresh-vs-Upgrade schema diff
- **로컬 Runtime 검증 계획:** Oracle/PostgreSQL/MariaDB 3사 모두 Empty→Initializer→Seed→Runtime smoke, Previous→Upgrade, Rollback/Recovery→Reapply, schema parity.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-07-12/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-08.01 — Generator DB 영향 Gate — DB 변경 시 generator model/template/render를 함께 current

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `부분 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Generator/Generated Domain/OpenAPI Generated Client가 존재하므로 새 계약 변경 시 이들을 함께 동기화해야 한다. 직접 수동 패치로만 해결하면 Generator 재생성 시 회귀할 위험이 있다.
- **실제 호출/Consumer 경로:** `Canonical Requirement/DB/API → Generator/OpenAPI → Generated Domain/Client → Consumer → Compile/Test/Runtime`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/generator/**`
  - `cpf-member/online/**`
  - `cpf-external/online/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice/openapi/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Source 계약 변경을 Generator/OpenAPI 정본에 먼저 반영하고 scratch fresh generate 및 MBR/EXS/Generated Client를 재생성해 drift를 제거한다. 구체 실행 목표: DB 변경 시 generator model/template/render를 함께 current

- **개발 상세 단계:**
  1. `Generator DB 영향 Gate`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `DB 변경 시 generator model/template/render를 함께 current` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Generator 재생성 시 수동 수정 소실, OpenAPI client drift, MBR/EXS 비대칭
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** generator fresh/regenerate diff 0, MBR/EXS compile/test, OpenAPI generation diff, handwritten fetch/axios scan
- **로컬 Runtime 검증 계획:** Scratch Generated Domain + MBR/EXS boot, DB3 init, Generated Client로 ADM/MBW 실제 Browser/API 호출.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-08-01/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-08.02 — Scratch fresh generate — reserved sample feature로 새 Domain fresh generate 후 DB3/compile/test

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `부분 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Generator/Generated Domain/OpenAPI Generated Client가 존재하므로 새 계약 변경 시 이들을 함께 동기화해야 한다. 직접 수동 패치로만 해결하면 Generator 재생성 시 회귀할 위험이 있다.
- **실제 호출/Consumer 경로:** `Canonical Requirement/DB/API → Generator/OpenAPI → Generated Domain/Client → Consumer → Compile/Test/Runtime`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/generator/**`
  - `cpf-member/online/**`
  - `cpf-external/online/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice/openapi/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Source 계약 변경을 Generator/OpenAPI 정본에 먼저 반영하고 scratch fresh generate 및 MBR/EXS/Generated Client를 재생성해 drift를 제거한다. 구체 실행 목표: reserved sample feature로 새 Domain fresh generate 후 DB3/compile/test

- **개발 상세 단계:**
  1. `Scratch fresh generate`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `reserved sample feature로 새 Domain fresh generate 후 DB3/compile/test` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Generator 재생성 시 수동 수정 소실, OpenAPI client drift, MBR/EXS 비대칭
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** generator fresh/regenerate diff 0, MBR/EXS compile/test, OpenAPI generation diff, handwritten fetch/axios scan
- **로컬 Runtime 검증 계획:** Scratch Generated Domain + MBR/EXS boot, DB3 init, Generated Client로 ADM/MBW 실제 Browser/API 호출.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-08-02/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-08.03 — MBR/EXS regenerate parity — 기존 Generated Domain이 canonical generator와 동일한지 확인

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `부분 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Generator/Generated Domain/OpenAPI Generated Client가 존재하므로 새 계약 변경 시 이들을 함께 동기화해야 한다. 직접 수동 패치로만 해결하면 Generator 재생성 시 회귀할 위험이 있다.
- **실제 호출/Consumer 경로:** `Canonical Requirement/DB/API → Generator/OpenAPI → Generated Domain/Client → Consumer → Compile/Test/Runtime`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/generator/**`
  - `cpf-member/online/**`
  - `cpf-external/online/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice/openapi/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Source 계약 변경을 Generator/OpenAPI 정본에 먼저 반영하고 scratch fresh generate 및 MBR/EXS/Generated Client를 재생성해 drift를 제거한다. 구체 실행 목표: 기존 Generated Domain이 canonical generator와 동일한지 확인

- **개발 상세 단계:**
  1. `MBR/EXS regenerate parity`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `기존 Generated Domain이 canonical generator와 동일한지 확인` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Generator 재생성 시 수동 수정 소실, OpenAPI client drift, MBR/EXS 비대칭
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** generator fresh/regenerate diff 0, MBR/EXS compile/test, OpenAPI generation diff, handwritten fetch/axios scan
- **로컬 Runtime 검증 계획:** Scratch Generated Domain + MBR/EXS boot, DB3 init, Generated Client로 ADM/MBW 실제 Browser/API 호출.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-08-03/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-08.04 — Approval OpenAPI Generated Client — ADM/MBW 신규 API를 Generated Client 실제 Consumer와 연결

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `부분 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Generator/Generated Domain/OpenAPI Generated Client가 존재하므로 새 계약 변경 시 이들을 함께 동기화해야 한다. 직접 수동 패치로만 해결하면 Generator 재생성 시 회귀할 위험이 있다.
- **실제 호출/Consumer 경로:** `Canonical Requirement/DB/API → Generator/OpenAPI → Generated Domain/Client → Consumer → Compile/Test/Runtime`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/generator/**`
  - `cpf-member/online/**`
  - `cpf-external/online/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice/openapi/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Source 계약 변경을 Generator/OpenAPI 정본에 먼저 반영하고 scratch fresh generate 및 MBR/EXS/Generated Client를 재생성해 drift를 제거한다. 구체 실행 목표: ADM/MBW 신규 API를 Generated Client 실제 Consumer와 연결

- **개발 상세 단계:**
  1. `Approval OpenAPI Generated Client`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `ADM/MBW 신규 API를 Generated Client 실제 Consumer와 연결` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Generator 재생성 시 수동 수정 소실, OpenAPI client drift, MBR/EXS 비대칭
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** generator fresh/regenerate diff 0, MBR/EXS compile/test, OpenAPI generation diff, handwritten fetch/axios scan
- **로컬 Runtime 검증 계획:** Scratch Generated Domain + MBR/EXS boot, DB3 init, Generated Client로 ADM/MBW 실제 Browser/API 호출.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-08-04/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-08.05 — Frontend static contract — operation consumer 100%, handwritten duplicate fetch/axios 0

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `부분 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Generator/Generated Domain/OpenAPI Generated Client가 존재하므로 새 계약 변경 시 이들을 함께 동기화해야 한다. 직접 수동 패치로만 해결하면 Generator 재생성 시 회귀할 위험이 있다.
- **실제 호출/Consumer 경로:** `Canonical Requirement/DB/API → Generator/OpenAPI → Generated Domain/Client → Consumer → Compile/Test/Runtime`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/generator/**`
  - `cpf-member/online/**`
  - `cpf-external/online/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice/openapi/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Source 계약 변경을 Generator/OpenAPI 정본에 먼저 반영하고 scratch fresh generate 및 MBR/EXS/Generated Client를 재생성해 drift를 제거한다. 구체 실행 목표: operation consumer 100%, handwritten duplicate fetch/axios 0

- **개발 상세 단계:**
  1. `Frontend static contract`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `operation consumer 100%, handwritten duplicate fetch/axios 0` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Generator 재생성 시 수동 수정 소실, OpenAPI client drift, MBR/EXS 비대칭
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** generator fresh/regenerate diff 0, MBR/EXS compile/test, OpenAPI generation diff, handwritten fetch/axios scan
- **로컬 Runtime 검증 계획:** Scratch Generated Domain + MBR/EXS boot, DB3 init, Generated Client로 ADM/MBW 실제 Browser/API 호출.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-08-05/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-08.06 — Browser Runtime matrix — ADM/MBW Loading/Empty/error/status/accessibility/responsive 실제 E2E

- **Origin / 분류:** `DEVELOPER_GPT_NEW`
- **현재 개발상태:** `미착수`
- **현재 정적검증:** `부분 확인`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** Generator/Generated Domain/OpenAPI Generated Client가 존재하므로 새 계약 변경 시 이들을 함께 동기화해야 한다. 직접 수동 패치로만 해결하면 Generator 재생성 시 회귀할 위험이 있다.
- **실제 호출/Consumer 경로:** `Canonical Requirement/DB/API → Generator/OpenAPI → Generated Domain/Client → Consumer → Compile/Test/Runtime`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/generator/**`
  - `cpf-member/online/**`
  - `cpf-external/online/**`
  - `cpf-admin/frontend/openapi/cpf-openapi.json`
  - `cpf-admin/frontend/src/generated/**`
  - `cpf-backoffice/openapi/**`
  - `cpf-backoffice-web/frontend/**`
- **계획된 구체 개발내용:** Source 계약 변경을 Generator/OpenAPI 정본에 먼저 반영하고 scratch fresh generate 및 MBR/EXS/Generated Client를 재생성해 drift를 제거한다. 구체 실행 목표: ADM/MBW Loading/Empty/error/status/accessibility/responsive 실제 E2E

- **개발 상세 단계:**
  1. `Browser Runtime matrix`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `ADM/MBW Loading/Empty/error/status/accessibility/responsive 실제 E2E` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Generator 재생성 시 수동 수정 소실, OpenAPI client drift, MBR/EXS 비대칭
- **DB3 영향:** YES — DB 변경/영향 가능성이 높음. 변경 발생 시 Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback-Recovery/Runtime 모두 필수.
- **정적검증 계획:** generator fresh/regenerate diff 0, MBR/EXS compile/test, OpenAPI generation diff, handwritten fetch/axios scan
- **로컬 Runtime 검증 계획:** Scratch Generated Domain + MBR/EXS boot, DB3 init, Generated Client로 ADM/MBW 실제 Browser/API 호출.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-08-06/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-09.01 — Source Identity tool 보정 — ignored logs/runtime temp가 source identity에 포함되지 않도록 git-independent scope 정합

- **Origin / 분류:** `FINAL_GATE_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 최종 전달물은 Source Identity, Hygiene, UTF-8, path<=200, Final Gate, Fresh Replay가 모두 일치해야 한다. 현재 ZIP에는 hygiene/path 관련 실제 실패가 존재한다.
- **실제 호출/Consumer 경로:** `Current Source Identity → Static/Build/Runtime Gates → Hygiene/Manifest → Fresh Replay → Overlay/Final ZIP/Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/verification/**`
  - `cpf-tools/runtime/**`
  - `cpf-tools/db/verification/**`
  - `cpf-docs/work/current/** (Codex 보호 파일 제외)`
  - `cpf-docs/work/evidence/developer-gpt/current/**`
  - `Repository 전체 tracked + untracked(non-ignored) Source`
- **계획된 구체 개발내용:** 제품 Source를 건드리는 생성 원인을 먼저 수정하고 disposable 산출물/경로/Identity를 Final Gate 기준으로 정리한다. 구체 실행 목표: ignored logs/runtime temp가 source identity에 포함되지 않도록 git-independent scope 정합

- **개발 상세 단계:**
  1. `cpf-source-state.py`의 include/exclude 규칙을 `.gitignore` 및 실제 배포 Source 범위와 비교한다.
  2. `logs/`, `*.log`, runtime temp/cache가 Product Source Identity에 포함되는지 재현한다.
  3. Git 의존 없이 tracked+untracked non-ignored에 준하는 canonical source scope를 정의하고 도구를 보정한다.
  4. 보정 전/후 file count/bytes/hash 차이를 Evidence로 남기고 실제 Source 파일이 빠지지 않았는지 manifest diff한다.
  5. 최종 Overlay/Fresh Replay/Package Manifest가 동일 Source Identity를 사용하도록 단일 함수로 연결한다.
- **Side Effect / 회귀 필수범위:** Codex Evidence 훼손, 제품 Source 오삭제, Windows ZIP 경로 실패, stale evidence
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** git diff --check, secret/hygiene/path/UTF-8/manifest/hash/source-identity/final integration ledger gates
- **로컬 Runtime 검증 계획:** 전체 로컬 통합 Runtime 명령 1회 → 모든 단계 PASS → 동일 Source Fresh Replay → 최종 ZIP 재검증.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-09-01/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-09.02 — 200자 path Gate — Root-relative path+filename >200 0

- **Origin / 분류:** `FINAL_GATE_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 현재 ZIP 실측 기준 Root-relative path 200자 초과 47개, 최대 272자가 확인됐다.
- **실제 호출/Consumer 경로:** `Current Source Identity → Static/Build/Runtime Gates → Hygiene/Manifest → Fresh Replay → Overlay/Final ZIP/Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/verification/**`
  - `cpf-tools/runtime/**`
  - `cpf-tools/db/verification/**`
  - `cpf-docs/work/current/** (Codex 보호 파일 제외)`
  - `cpf-docs/work/evidence/developer-gpt/current/**`
  - `Repository 전체 tracked + untracked(non-ignored) Source`
- **계획된 구체 개발내용:** 제품 Source를 건드리는 생성 원인을 먼저 수정하고 disposable 산출물/경로/Identity를 Final Gate 기준으로 정리한다. 구체 실행 목표: Root-relative path+filename >200 0

- **개발 상세 단계:**
  1. Repository/Overlay/Evidence/Release 전체 Entry의 Root-relative path+filename 길이를 계산해 초과 목록을 만든다.
  2. 긴 이름만 기계적으로 축약하지 않고 반복 directory segment와 불필요 evidence 중첩의 생성 Owner를 찾는다.
  3. Codex 보호 Evidence는 Developer GPT가 임의 rename/delete하지 않고 coordinated cleanup 대상으로 분리한다.
  4. Developer GPT 신규 Evidence는 처음부터 짧고 의미 있는 IA로 생성해 200자 이내를 보장한다.
  5. 최종 ZIP 생성 직전 path>200=0 Gate를 fail-closed로 실행한다.
- **Side Effect / 회귀 필수범위:** Codex Evidence 훼손, 제품 Source 오삭제, Windows ZIP 경로 실패, stale evidence
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** git diff --check, secret/hygiene/path/UTF-8/manifest/hash/source-identity/final integration ledger gates
- **로컬 Runtime 검증 계획:** 전체 로컬 통합 Runtime 명령 1회 → 모든 단계 PASS → 동일 Source Fresh Replay → 최종 ZIP 재검증.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-09-02/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-09.03 — nested cpf-docs/project-cache 정리 — Root cpf-docs 외 작업생성 중첩 docs/cache 원인 수정 후 재생성 0

- **Origin / 분류:** `FINAL_GATE_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 현재 ZIP에서 `project-cache` 1,231건, `buildOutputCleanup` 210건, `vcs-1` 105건 및 Root 외 nested `cpf-docs`가 확인됐다. 단 Codex 전용 Evidence는 Developer GPT가 직접 변경하지 않는다.
- **실제 호출/Consumer 경로:** `Current Source Identity → Static/Build/Runtime Gates → Hygiene/Manifest → Fresh Replay → Overlay/Final ZIP/Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/verification/**`
  - `cpf-tools/runtime/**`
  - `cpf-tools/db/verification/**`
  - `cpf-docs/work/current/** (Codex 보호 파일 제외)`
  - `cpf-docs/work/evidence/developer-gpt/current/**`
  - `Repository 전체 tracked + untracked(non-ignored) Source`
- **계획된 구체 개발내용:** 제품 Source를 건드리는 생성 원인을 먼저 수정하고 disposable 산출물/경로/Identity를 Final Gate 기준으로 정리한다. 구체 실행 목표: Root cpf-docs 외 작업생성 중첩 docs/cache 원인 수정 후 재생성 0

- **개발 상세 단계:**
  1. Root `cpf-docs` 외 nested `cpf-docs`, `project-cache`, `buildOutputCleanup`, `vcs-1`의 생성 위치와 생성 명령/working directory를 역추적한다.
  2. 제품 Source인지 disposable build/cache인지 Consumer와 Gradle/Script owner를 기준으로 분류한다.
  3. 생성 Root Cause를 수정해 다음 실행에서 workspace 내부에 재생성되지 않게 한다.
  4. 삭제 대상은 Delete Manifest로 분리하고 Codex 보호 Evidence/보호경로는 자동삭제에서 제외한다.
  5. 동일 build/verifier를 재실행해 재생성 0과 path 200 Gate를 함께 확인한다.
- **Side Effect / 회귀 필수범위:** Codex Evidence 훼손, 제품 Source 오삭제, Windows ZIP 경로 실패, stale evidence
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** git diff --check, secret/hygiene/path/UTF-8/manifest/hash/source-identity/final integration ledger gates
- **로컬 Runtime 검증 계획:** 전체 로컬 통합 Runtime 명령 1회 → 모든 단계 PASS → 동일 Source Fresh Replay → 최종 ZIP 재검증.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-09-03/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-09.04 — Codex 보호자료 충돌 처리 — Codex 전용 문서/Evidence는 Developer GPT가 변경하지 않고 보호 경로로 분리 관리

- **Origin / 분류:** `FINAL_GATE_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 최종 전달물은 Source Identity, Hygiene, UTF-8, path<=200, Final Gate, Fresh Replay가 모두 일치해야 한다. 현재 ZIP에는 hygiene/path 관련 실제 실패가 존재한다.
- **실제 호출/Consumer 경로:** `Current Source Identity → Static/Build/Runtime Gates → Hygiene/Manifest → Fresh Replay → Overlay/Final ZIP/Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/verification/**`
  - `cpf-tools/runtime/**`
  - `cpf-tools/db/verification/**`
  - `cpf-docs/work/current/** (Codex 보호 파일 제외)`
  - `cpf-docs/work/evidence/developer-gpt/current/**`
  - `Repository 전체 tracked + untracked(non-ignored) Source`
- **계획된 구체 개발내용:** 제품 Source를 건드리는 생성 원인을 먼저 수정하고 disposable 산출물/경로/Identity를 Final Gate 기준으로 정리한다. 구체 실행 목표: Codex 전용 문서/Evidence는 Developer GPT가 변경하지 않고 보호 경로로 분리 관리

- **개발 상세 단계:**
  1. `Codex 보호자료 충돌 처리`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `Codex 전용 문서/Evidence는 Developer GPT가 변경하지 않고 보호 경로로 분리 관리` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Codex Evidence 훼손, 제품 Source 오삭제, Windows ZIP 경로 실패, stale evidence
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** git diff --check, secret/hygiene/path/UTF-8/manifest/hash/source-identity/final integration ledger gates
- **로컬 Runtime 검증 계획:** 전체 로컬 통합 Runtime 명령 1회 → 모든 단계 PASS → 동일 Source Fresh Replay → 최종 ZIP 재검증.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-09-04/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-09.05 — UTF-8/파일명 Gate — mojibake/#Uxxxx/ANSI/ZIP entry 깨짐 0

- **Origin / 분류:** `FINAL_GATE_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 최종 전달물은 Source Identity, Hygiene, UTF-8, path<=200, Final Gate, Fresh Replay가 모두 일치해야 한다. 현재 ZIP에는 hygiene/path 관련 실제 실패가 존재한다.
- **실제 호출/Consumer 경로:** `Current Source Identity → Static/Build/Runtime Gates → Hygiene/Manifest → Fresh Replay → Overlay/Final ZIP/Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/verification/**`
  - `cpf-tools/runtime/**`
  - `cpf-tools/db/verification/**`
  - `cpf-docs/work/current/** (Codex 보호 파일 제외)`
  - `cpf-docs/work/evidence/developer-gpt/current/**`
  - `Repository 전체 tracked + untracked(non-ignored) Source`
- **계획된 구체 개발내용:** 제품 Source를 건드리는 생성 원인을 먼저 수정하고 disposable 산출물/경로/Identity를 Final Gate 기준으로 정리한다. 구체 실행 목표: mojibake/#Uxxxx/ANSI/ZIP entry 깨짐 0

- **개발 상세 단계:**
  1. `UTF-8/파일명 Gate`의 현재 Source/정본/Consumer를 Repository-wide로 검색해 실제 Owner와 구현 상태를 확정한다.
  2. Interface/DTO/Config 존재만으로 판단하지 않고 호출자→구현→DB/API/Frontend/Runtime 연결을 끝까지 추적한다.
  3. `mojibake/#Uxxxx/ANSI/ZIP entry 깨짐 0` 요구를 충족하도록 최소 Root Cause 범위에서 Source와 관련 정본을 함께 수정한다.
  4. 동일 Root Cause의 잠복 결함을 관련 Module/Generator/Test/Script에서 검색해 함께 보정한다.
  5. Targeted static → 영향 회귀 → 로컬 Runtime → Evidence 순서로 검증하고 미실행 항목은 PASS 처리하지 않는다.
- **Side Effect / 회귀 필수범위:** Codex Evidence 훼손, 제품 Source 오삭제, Windows ZIP 경로 실패, stale evidence
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** git diff --check, secret/hygiene/path/UTF-8/manifest/hash/source-identity/final integration ledger gates
- **로컬 Runtime 검증 계획:** 전체 로컬 통합 Runtime 명령 1회 → 모든 단계 PASS → 동일 Source Fresh Replay → 최종 ZIP 재검증.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-09-05/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-09.06 — Canonical Final Gate — 필수 FAIL/SKIP/NOT_EXECUTED/UNKNOWN/identity mismatch 0

- **Origin / 분류:** `FINAL_GATE_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 최종 전달물은 Source Identity, Hygiene, UTF-8, path<=200, Final Gate, Fresh Replay가 모두 일치해야 한다. 현재 ZIP에는 hygiene/path 관련 실제 실패가 존재한다.
- **실제 호출/Consumer 경로:** `Current Source Identity → Static/Build/Runtime Gates → Hygiene/Manifest → Fresh Replay → Overlay/Final ZIP/Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/verification/**`
  - `cpf-tools/runtime/**`
  - `cpf-tools/db/verification/**`
  - `cpf-docs/work/current/** (Codex 보호 파일 제외)`
  - `cpf-docs/work/evidence/developer-gpt/current/**`
  - `Repository 전체 tracked + untracked(non-ignored) Source`
- **계획된 구체 개발내용:** 제품 Source를 건드리는 생성 원인을 먼저 수정하고 disposable 산출물/경로/Identity를 Final Gate 기준으로 정리한다. 구체 실행 목표: 필수 FAIL/SKIP/NOT_EXECUTED/UNKNOWN/identity mismatch 0

- **개발 상세 단계:**
  1. 정상 2-Worker 분산 처리 기준선을 먼저 확보하고 work/lease/fence/result 상태를 DB에서 캡처한다.
  2. `Canonical Final Gate` 시나리오를 실제 Process/DB 경계에서 유발하고 상태전이·중복처리·stale mutation 여부를 확인한다.
  3. 실패/UNKNOWN을 단순 FAILED로 축약하지 않고 retry/restart/recovery/reconcile/reprocess의 Owner와 허용 조건을 구분한다.
  4. Header6/transactionId/executionId와 Trace/Timeline이 장애 전후 동일 lineage를 유지하는지 확인한다.
  5. 재할당/복구 후 DB role, process, port, temp, lease 누수가 0인지 cleanup gate로 확인한다.
- **Side Effect / 회귀 필수범위:** Codex Evidence 훼손, 제품 Source 오삭제, Windows ZIP 경로 실패, stale evidence
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** git diff --check, secret/hygiene/path/UTF-8/manifest/hash/source-identity/final integration ledger gates
- **로컬 Runtime 검증 계획:** 전체 로컬 통합 Runtime 명령 1회 → 모든 단계 PASS → 동일 Source Fresh Replay → 최종 ZIP 재검증.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-09-06/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`

### WP-09.07 — Fresh Replay/최종 ZIP — 동일 Source Identity 재현 후 Review/Inventory/Evidence/Handover/Delete Manifest 포함 ZIP 생성

- **Origin / 분류:** `FINAL_GATE_NEW`
- **현재 개발상태:** `미완료`
- **현재 정적검증:** `FAIL`
- **현재 Runtime 검증:** `미실행`
- **현재 Codex 검증:** `미실행`
- **Codex 재검수 필요:** `YES`
- **현재 Source QA 사실:** 최종 전달물은 Source Identity, Hygiene, UTF-8, path<=200, Final Gate, Fresh Replay가 모두 일치해야 한다. 현재 ZIP에는 hygiene/path 관련 실제 실패가 존재한다.
- **실제 호출/Consumer 경로:** `Current Source Identity → Static/Build/Runtime Gates → Hygiene/Manifest → Fresh Replay → Overlay/Final ZIP/Handover`
- **주요 Owner / 영향 Source 후보:**
  - `cpf-tools/verification/**`
  - `cpf-tools/runtime/**`
  - `cpf-tools/db/verification/**`
  - `cpf-docs/work/current/** (Codex 보호 파일 제외)`
  - `cpf-docs/work/evidence/developer-gpt/current/**`
  - `Repository 전체 tracked + untracked(non-ignored) Source`
- **계획된 구체 개발내용:** 제품 Source를 건드리는 생성 원인을 먼저 수정하고 disposable 산출물/경로/Identity를 Final Gate 기준으로 정리한다. 구체 실행 목표: 동일 Source Identity 재현 후 Review/Inventory/Evidence/Handover/Delete Manifest 포함 ZIP 생성

- **개발 상세 단계:**
  1. `cpf-source-state.py`의 include/exclude 규칙을 `.gitignore` 및 실제 배포 Source 범위와 비교한다.
  2. `logs/`, `*.log`, runtime temp/cache가 Product Source Identity에 포함되는지 재현한다.
  3. Git 의존 없이 tracked+untracked non-ignored에 준하는 canonical source scope를 정의하고 도구를 보정한다.
  4. 보정 전/후 file count/bytes/hash 차이를 Evidence로 남기고 실제 Source 파일이 빠지지 않았는지 manifest diff한다.
  5. 최종 Overlay/Fresh Replay/Package Manifest가 동일 Source Identity를 사용하도록 단일 함수로 연결한다.
- **Side Effect / 회귀 필수범위:** Codex Evidence 훼손, 제품 Source 오삭제, Windows ZIP 경로 실패, stale evidence
- **DB3 영향:** NO 또는 Final Gate 수준. DB 파일을 수정하는 순간 YES로 전환.
- **정적검증 계획:** git diff --check, secret/hygiene/path/UTF-8/manifest/hash/source-identity/final integration ledger gates
- **로컬 Runtime 검증 계획:** 전체 로컬 통합 Runtime 명령 1회 → 모든 단계 PASS → 동일 Source Fresh Replay → 최종 ZIP 재검증.
- **Evidence 기본 경로:** `cpf-docs/work/evidence/developer-gpt/current/wp-09-07/`
- **실제 개발내용 — 개발 후 반드시 기록:**
  - `[미기록]` 변경한 클래스/메서드/SQL/Config/API/Frontend와 **어떻게 수정했는지**를 문장으로 기록한다.
  - `[미기록]` 원래 문제를 어떤 Root Cause로 판정했고, 왜 이 구현 방식을 선택했는지 기록한다.
  - `[미기록]` 개발 중 추가 발견한 Finding과 기존 인덱스 병합/신규 하위 인덱스 여부를 기록한다.
- **실제 수정파일 — 개발 후 반드시 기록:** `[미기록]`
- **실제 Consumer 변경 — 개발 후 반드시 기록:** `[미기록]`
- **실제 DB 변경 — 개발 후 반드시 기록:** `[없음/있음 미확정]`; 있음이면 Oracle/PostgreSQL/MariaDB 각각 Initializer/Fresh/Migration/Upgrade/Rollback/Seed/Runtime 결과를 모두 연결한다.
- **정적검증 실제 명령/결과:** `[미기록]`
- **로컬 Runtime 실제 명령/결과:** `[미기록]` — 사용자에게 제공한 PowerShell 한 줄, 로그 절대경로, ExitCode, 단계별 PASS/FAIL을 기록한다.
- **Codex 독립 재검수 결과:** `[미실행]` — Developer GPT가 대신 PASS/CLOSED 처리하지 않는다.
- **실패·재보정 이력:** `[없음/미기록]` — 실패 단계, 실제 오류, Root Cause, 보정, 재실행 결과를 누적한다.
- **최종 Evidence:** `[미기록]`
- **최종 Closure 조건:** 위 계획된 개발/영향 범위가 Source에서 확인되고, 정적 Gate PASS, Runtime 대상이면 로컬 Runtime PASS, DB 영향이면 DB3 전체 Lifecycle PASS, Evidence/Source Identity 일치 후에만 `CLOSED`.
- **최종 판정/완료 사유:** `[개발 후 기록]`


## 5-A. 추가 필수 Requirement 실행 원장

> 아래 항목은 기존 105개 카드 검토 후 누락/약화가 확인되어 **정식 Developer GPT 실행 인덱스에 추가**한다. 기존 105개 ID는 변경하지 않고 `WP-10`, `WP-11`로 확장한다.

### WP-10 — Batch / Center-Cut Runtime Identity 최종 Closure

#### WP-10.01 — 일반 Batch System/Channel Identity = BAT/BAT
- **원 Requirement:** 일반 Batch의 System Code와 Channel Code는 모두 `BAT`.
- **현재 판정:** 기존 `WP-01.06`에 CEC/Header6 관점이 일부 있으나 BAT Identity 자체의 독립 Closure 카드가 없었음.
- **개발 목적:** Scheduler/Worker/Batch 업무 거래가 임의 Runtime 값이나 CEC 값으로 오염되지 않고 Canonical `BAT/BAT`를 사용하도록 한다.
- **영향 Source/Consumer:** Batch bootstrap, runtime identity provider, application/profile YAML, `CpfContext`, Header6 producer, Registry, Logging, Trace, Audit, Batch execution metadata, ADM.
- **개발 상세 단계:**
  1. Batch 5개 실행체 중 일반 Batch 거래 생성 주체의 System/Channel 초기화 Owner를 전수 추적한다.
  2. System Code=`BAT`, Channel Code=`BAT` 기본값/설정/검증 계약을 Source와 Config에 일치시킨다.
  3. Header6 생성 시 `X-System-Code`, Caller/Target 계보가 BAT 거래에서 올바른지 확인한다.
  4. Registry/Log/Trace/Audit/Execution metadata가 같은 Identity를 소비하는지 확인한다.
  5. BAT 거래가 실제 Domain Invocation을 호출하는 Runtime E2E를 수행한다.
- **실제 개발내용:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임검증:** `[미실행]`
- **Codex검증:** `[미실행 — 재검수 필요]`
- **Closure:** BAT 거래의 Config→Context→Header6→Invocation→Registry/Log/Trace/DB metadata 전 경로가 BAT/BAT로 일치하고 Runtime PASS.

#### WP-10.02 — Center-Cut Runner Identity = CEC/CEC/CENTER_CUT_RUNNER
- **원 Requirement:** Center-Cut Runner System Code=`CEC`, Channel Code=`CEC`, Runtime Role=`CENTER_CUT_RUNNER`.
- **개발 상세 단계:**
  1. Center-Cut Main/bootstrap/identity provider의 현재 System/Channel/Role 값을 확인한다.
  2. CEC/CEC/CENTER_CUT_RUNNER canonical default와 validation을 적용한다.
  3. 일반 BAT Runtime과 CEC Runtime의 identity가 같은 host/multi-instance에서도 혼동되지 않게 한다.
  4. Registry/Health/ADM에서 CEC Runtime Role을 정확히 노출한다.
  5. CEC가 생성한 실제 거래의 Header6/Trace/Timeline/DB metadata를 물리 확인한다.
- **실제 개발내용:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임검증:** `[미실행]`
- **Codex검증:** `[미실행 — 재검수 필요]`

#### WP-10.03 — CEC Identity Properties/YAML 외부화 + 한글 실무 주석
- **원 Requirement:** CEC Identity는 hardcoding만으로 끝내지 않고 Center-Cut Runtime properties/YAML에서 설정 가능해야 하며 한국어 실무 주석을 제공한다.
- **개발 상세 단계:**
  1. `application.yml` 및 profile별 config owner를 확인한다.
  2. System Code/Channel Code/Runtime Role property key와 canonical default를 정의한다.
  3. invalid/blank/금지값 validation과 startup fail-fast를 구현한다.
  4. 설정 의미·운영 변경 주의사항을 한글 실무 주석으로 기록한다.
  5. explicit override와 default boot를 각각 Runtime 검증한다.
- **실제 개발내용:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임검증:** `[미실행]`
- **Codex검증:** `[미실행 — 재검수 필요]`

#### WP-10.04 — BAT/CEC Runtime Bootstrap/Identity Owner 단일화
- **개발 목적:** 각 실행체가 자체 하드코딩/중복 Bean으로 Identity를 만들지 않도록 canonical owner를 고정한다.
- **개발 상세 단계:** Main/AutoConfiguration/Properties/Identity Bean을 inventory하고 duplicate/fallback/legacy owner를 제거·통합한다.
- **Side Effect:** 5 Executable boot, multi-instance, registry.
- **검증:** Bean uniqueness + Boot5 + Registry 5/5.

#### WP-10.05 — BAT/CEC Context + Header6 전파
- **개발 상세 단계:** Context bootstrap → inbound/outbound propagation → Domain Invocation → target domain ingress까지 BAT/CEC origin identity를 검증한다.
- **필수:** Original/System/Caller/Target/Operation ID 계보 왜곡 0.
- **Runtime:** 실제 BAT 거래와 CEC 거래를 각각 호출해 Header6 캡처.

#### WP-10.06 — BAT/CEC Official Domain Invocation Engine 공통 사용
- **원 Requirement:** BAT와 CEC 업무 거래 호출은 동일 CPF 공식 Invocation Engine을 재사용하며 Center-Cut 전용 중복 Client/호출 Stack을 만들지 않는다.
- **개발 상세 단계:**
  1. BAT/CEC outbound client/router를 Repository-wide 검색한다.
  2. `CpfDomainClientRouter`/canonical invocation path 이외 중복 호출 stack의 Consumer를 판정한다.
  3. 필요한 호출을 canonical engine으로 통합한다.
  4. Same JVM/Remote 선택과 self-HTTP 금지를 확인한다.
  5. BAT/CEC 양쪽 실제 Domain Runtime E2E를 수행한다.

#### WP-10.07 — Registry / Logging / Trace / Timeline / Audit Identity
- **개발 상세 단계:** instanceId + System/Channel/Role이 Registry, runtime log, transaction evidence, Trace/Timeline, Audit에 동일하게 저장/조회되는지 검증한다.
- **Runtime:** transactionId 기준으로 BAT/CEC origin을 ADM까지 추적.

#### WP-10.08 — Batch Execution Metadata Identity
- **개발 상세 단계:** execution/job/work-item/result metadata의 system/channel/runtime-role 컬럼·query·mapper를 확인하고 BAT/CEC를 구분한다.
- **DB3 영향:** DB 변경이 발생하면 즉시 DB3 전체 Lifecycle 필수.

#### WP-10.09 — ADM / Sample / EDU / Config / Canonical Docs Identity 현행화
- **개발 상세 단계:** ADM display/search/filter, Sample/EDU config, launcher, 운영문서, Architecture/Specification에서 BAT/CEC 의미가 동일한지 전수 확인한다.
- **False Green 금지:** Source만 BAT/CEC이고 문서/샘플이 legacy 값이면 미완료.

#### WP-10.10 — BAT/CEC Identity 로컬 통합 Runtime Gate
- **Runtime 시나리오:** 일반 Batch 실제 거래 + CEC no-Kafka 실제 거래 → Generated Domain Operation → DB/Trace/Timeline/Registry 확인.
- **완료 조건:** BAT/BAT 및 CEC/CEC/CENTER_CUT_RUNNER가 각각 전 경로에서 일치, 오류 0.
- **사용자 PowerShell 한 줄:** `[개발 후 실제 Source task/script 확인 후 기록]`
- **Codex 재검수:** `YES`.

---

### WP-11 — Open Git Fresh Release Full Closure — Steering 1~40 1:1 실행원장

#### WP-11.01 — 최상위 Release 원칙
- **원 Requirement:** Open Git Steering `#1. 최상위 Release 원칙`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - Open Git Release는 Private master의 과거 결과물을 단순 복사하는 패키지가 아니다.
  - 매 Release마다 현재 최신 Private Source를 입력으로 하여 Clean Release Workspace에서 필요한 것은 Fresh Generate하고, Framework Binary는 Fresh Compile/Publish하고, 사람이 작성·유지하는 Canonical Source만 Projection하여 새로 구성해야 한다.
  - 다음 결과물은 Release 입력으로 재사용하지 않는다.
  - 기존 `build/`
  - 기존 `build/libs/`
  - 과거 JAR/POM/BOM
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.02 — Private master가 유일한 개발 정본
- **원 Requirement:** Open Git Steering `#2. Private master가 유일한 개발 정본`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - Private master는 다음의 유일한 개발 정본이다.
  - CPF 전체 Product Source
  - Generator
  - Generated Domain 규칙
  - Framework Publication
  - Open Git Release Policy
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.03 — Release Owner와 Local Generated Root
- **원 Requirement:** Open Git Steering `#3. Release Owner와 Local Generated Root`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - Open Git Release의 canonical Owner는 다음을 기준으로 한다.
  - 이 영역이 다음을 소유한다.
  - Surface Policy
  - Artifact Policy
  - Fresh Build/Publication
  - Fresh Generated Domain generation
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.04 — Release 재수행은 전체 Clean Regeneration
- **원 Requirement:** Open Git Steering `#4. Release 재수행은 전체 Clean Regeneration`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 이전 결과를 부분 재사용하거나 stale 파일을 남기지 않는다.
  - 매 실행:
  - 제품 Source나 보호경로를 광범위하게 삭제하지 않는다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.05 — Open Git Source와 Binary Deliverable 분리
- **원 Requirement:** Open Git Steering `#5. Open Git Source와 Binary Deliverable 분리`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - Open Git Source repository와 Maven-folder Binary Repository는 분리한다.
  - Framework JAR을 Open Git Git history에 Release마다 누적 commit하는 구조를 만들지 않는다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.06 — Framework Binary는 매 Release Fresh Build
- **원 Requirement:** Open Git Steering `#6. Framework Binary는 매 Release Fresh Build`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - Framework Binary는 기존 JAR 복사가 아니라 현재 최신 Source에서 Fresh Build/Publication한다.
  - 최소:
  - 금지:
  - 기존 `build/libs` 복사
  - Local Maven 과거 artifact에 기대는 build
  - 이전 Release binary 재사용
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.07 — Generated Customer Domain Java Source는 반드시 Fresh Generation
- **원 Requirement:** Open Git Steering `#7. Generated Customer Domain Java Source는 반드시 Fresh Generation`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - Generated Customer Domain의 Java Source는 Private master의 기존 생성 결과를 단순 복사하지 않는다.
  - 대상:
  - `cpf-member`
  - `cpf-external`
  - Scratch/Public Generated Domain
  - 향후 canonical Generated Customer Domain
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.08 — Generated Domain에는 개발자에게 필요한 것만 생성
- **원 Requirement:** Open Git Steering `#8. Generated Domain에는 개발자에게 필요한 것만 생성`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - Generated Customer Domain은 Generator 내부 상태 저장소가 아니다.
  - 개발자가 실제 업무 개발에 필요한 Developer-facing Source/Build 계약만 생성한다.
  - 특히 다음 두 파일은 Generated Domain Root 및 Fresh Generate 결과에서 존재하면 안 된다.
  - 이 파일에 있던 정보가 기술적으로 필요하다면:
  - Generator-owned canonical input/state로 이동
  - central generator metadata로 이동
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.09 — Generated Domain Fresh Validation Matrix
- **원 Requirement:** Open Git Steering `#9. Generated Domain Fresh Validation Matrix`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 한 개 Domain 생성 성공으로 끝내지 않는다.
  - 최소:
  - 각 경우:
  - generated file inventory
  - Root cleanliness
  - canonical directory IA
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.10 — Backoffice는 Generated Domain 표준을 따르지만 Fresh Generation 대상이 아니다
- **원 Requirement:** Open Git Steering `#10. Backoffice는 Generated Domain 표준을 따르지만 Fresh Generation 대상이 아니다`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - Backoffice는 Generated Domain과 같은 개발 IA/패키지 표준을 따르되 Generator로 매 Release 다시 만드는 영역은 아니다.
  - 이미 선구현된 업무 Sample/Consumer/연동 Source가 의미 있는 canonical authored source이므로 다음과 같이 처리한다.
  - Backoffice 예외를 이유로 Platform Internal Runtime/Private Source까지 공개하지 않는다.
  - Backoffice UI도 canonical Source를 Projection할 수 있으나 Release workspace에서 fresh dependency install/build/test를 수행한다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.11 — EDU는 공식 Canonical Example Source
- **원 Requirement:** Open Git Steering `#11. EDU는 공식 Canonical Example Source`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - `cpf-education`은 개발자가 CPF 기능을 실제 코드로 보고 참고·학습하는 공식 Sample Source다.
  - EDU는 Generator가 다시 생성하는 Domain이 아니다.
  - Release에서는:
  - 을 수행한다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.12 — EDU는 “존재 확인”이 아니라 실제 Example 검증
- **원 Requirement:** Open Git Steering `#12. EDU는 “존재 확인”이 아니라 실제 Example 검증`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 다음은 EDU PASS 근거가 아니다.
  - 폴더 존재
  - Java file 존재
  - Interface/Sample skeleton 존재
  - README 존재
  - compile/test 미실행
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.13 — EDU가 사용하는 계약도 Current여야 한다
- **원 Requirement:** Open Git Steering `#13. EDU가 사용하는 계약도 Current여야 한다`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - EDU Example은 현재 CPF Canonical 계약을 실제 사용해야 한다.
  - 예:
  - Canonical Header/Context
  - operationId contract
  - Public API/SPI
  - current Starter
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.14 — Open Git 공개 Source Policy
- **원 Requirement:** Open Git Steering `#14. Open Git 공개 Source Policy`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - Default-Deny를 사용한다.
  - Source 공개 대상:
  - Fresh Generated Customer Domain Source
  - 고객 Backoffice 개발 Surface
  - `cpf-education`
  - Developer-facing Generator/Setup/Build/Test/Domain CLI
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.15 — Binary / sources.jar / javadoc 정책
- **원 Requirement:** Open Git Steering `#15. Binary / sources.jar / javadoc 정책`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - `sources.jar`는 Source 공개다.
  - Binary 허용과 Source 허용을 분리한다.
  - 기본 방향:
  - 미분류 Artifact는 공개하지 않는다.
  - 실제 Artifact별 policy를 canonical catalog와 publication source 기준으로 확정한다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.16 — Developer-facing 명령 전수 Inventory
- **원 Requirement:** Open Git Steering `#16. Developer-facing 명령 전수 Inventory`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 미리 정한 몇 개 명령만 검사하지 않는다.
  - 실제 Open Git Package에 노출된 모든 user-facing command와 option을 전수 Inventory한다.
  - 대표 목표 UX:
  - 실제 공개 명령이 더 있으면 모두 검사한다.
  - 긴 내부 script filename을 사용자가 외워야 하는 구조를 피한다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.17 — Developer command UX 검증
- **원 Requirement:** Open Git Steering `#17. Developer command UX 검증`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 각 명령은 실행 중 최소:
  - 를 보여준다.
  - 종료 시:
  - 을 보여준다.
  - 장시간 작업 중 콘솔이 멈춘 것처럼 보이지 않게 한다.
  - 로그는 Timestamp별 저장하고 console에도 실시간 출력한다.
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.18 — `cpf bootstrap` 실제 Acceptance
- **원 Requirement:** Open Git Steering `#18. `cpf bootstrap` 실제 Acceptance`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - `cpf bootstrap`은 Shell launch 성공으로 PASS가 아니다.
  - Fresh Open Git 개발자의 실제 개발환경을 준비해야 한다.
  - 최소:
  - 최종 성공 기준:
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.19 — 모든 Developer command 실제 실행검증
- **원 Requirement:** Open Git Steering `#19. 모든 Developer command 실제 실행검증`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - Shell/CLI 존재나 `help`만으로 PASS 처리하지 않는다.
  - 각 명령에서 실제 확인:
  - 1. 명령 인식
  - 2. help/options/default와 구현 일치
  - 3. Open Git Package/Binary만으로 실행
  - 4. Private CPF Source 참조 없음
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.20 — Fresh Open Git Developer Acceptance Scenario
- **원 Requirement:** Open Git Steering `#20. Fresh Open Git Developer Acceptance Scenario`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - Release 생성 후 완전히 별도 Fresh Workspace에서 외부 개발자 관점으로 검증한다.
  - Private CPF Source를 사용할 수 없는 조건이어야 한다.
  - 최소:
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.21 — Domain Generator Acceptance
- **원 Requirement:** Open Git Steering `#21. Domain Generator Acceptance`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - `cpf domain new` ExitCode 0으로 끝내지 않는다.
  - 실제 Scratch Domain을 만들고:
  - Directory IA
  - Java package IA
  - feature structure
  - dependency
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.22 — Failure Path 검증
- **원 Requirement:** Open Git Steering `#22. Failure Path 검증`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 최소 다음을 실제로 검사한다.
  - Java version 오류
  - Docker/Container runtime 미기동
  - Binary Repository 접근 실패
  - 잘못된 CPF Version
  - 잘못된/중복 Domain name
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.23 — stop/reset 안전
- **원 Requirement:** Open Git Steering `#23. stop/reset 안전`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - `stop`과 `reset` 역할을 분리한다.
  - `reset`은 destructive action이므로 명시 확인 없이 실행하지 않는다.
  - 예:
  - 확인이 없으면 안전하게 거부하고 ExitCode/이유/다음 명령을 알려준다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.24 — Open Git Release Gate
- **원 Requirement:** Open Git Steering `#24. Open Git Release Gate`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 최소 의미:
  - 실제 구현에 따라 단계 수는 조정 가능하나 의미는 줄이지 않는다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.25 — READY\_TO\_COMMIT 판정
- **원 Requirement:** Open Git Steering `#25. READY\_TO\_COMMIT 판정`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 다음만으로 READY가 아니다.
  - package directory 생성
  - source copy 성공
  - build 한 번 성공
  - secret 검사 PASS
  - generator 실행 성공
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.26 — 자동 commit/push 금지
- **원 Requirement:** Open Git Steering `#26. 자동 commit/push 금지`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 자동 Git 동작은 검증 수준까지만 허용한다.
  - 예:
  - 실제:
  - 는 사용자가 최종 확인 후 직접 수행한다.
  - Private master에도 자동 commit/push하지 않는다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.27 — ★ 개발 정본 현행화는 필수 완료조건
- **원 Requirement:** Open Git Steering `#27. ★ 개발 정본 현행화는 필수 완료조건`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 이번 Steering에서 가장 중요한 추가 요구다.
  - Open Git/Generated Domain/EDU/Developer CLI 구현을 Source에만 넣고 끝내지 않는다.
  - Codex는 구현이 끝난 뒤 반드시 현재 개발 정본을 직접 열어 이번 Requirement를 정확하고 상세하게 반영한다.
  - 최소 현행화 대상:
  - 정본 위치가 최신 Source에서 달라졌다면 실제 canonical owner를 찾아 동일 의미로 현행화한다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.28 — 개발 정본에는 최소 다음 의미가 반드시 남아야 한다
- **원 Requirement:** Open Git Steering `#28. 개발 정본에는 최소 다음 의미가 반드시 남아야 한다`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - CPF 전체 Source와 Release 정의의 유일 정본
  - local generated
  - Private Git ignore
  - 매 실행 clean regeneration
  - 기존 binary copy 금지
  - 매 Release Fresh Compile/Test/Publication
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.29 — 잘못된 개발 정본은 Source와 함께 수정
- **원 Requirement:** Open Git Steering `#29. 잘못된 개발 정본은 Source와 함께 수정`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 현재 정본에 아래와 같은 오래되거나 잘못된 Requirement가 있다면 그대로 승계하지 않는다.
  - 예:
  - 기존 Generated Domain Source copy를 Release 방식으로 정의
  - 기존 JAR 복사를 Publication으로 간주
  - `cpf-domain.yaml` 필수
  - `cpf-generator.lock.json` 필수
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.30 — Requirement 원장/Closure Inventory 반영
- **원 Requirement:** Open Git Steering `#30. Requirement 원장/Closure Inventory 반영`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 이번 Open Git Release는 별도 정식 Requirement/Work Package로 추적되어야 한다.
  - 예를 들어 현재 ID 체계와 충돌하지 않는 방식으로:
  - 등의 acceptance가 누락되지 않도록 관리한다.
  - 단, 역할별 컬럼 수정 권한은 기존 QA 규칙을 지킨다.
  - Codex는 Codex 소유 검수/보완 영역을 중심으로 수정하며, QA의 최종 상태를 임의로 완료 처리하지 않는다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.31 — Source와 정본이 일치해야 완료
- **원 Requirement:** Open Git Steering `#31. Source와 정본이 일치해야 완료`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 다음 상태는 허용하지 않는다.
  - 최종 검수에서:
  - 가 동일한 의미를 가져야 한다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.32 — Finding 처리
- **원 Requirement:** Open Git Steering `#32. Finding 처리`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 검증 중 결함 발견 시:
  - 임시 Release workaround로 Product defect를 숨기지 않는다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.33 — False Green 금지
- **원 Requirement:** Open Git Steering `#33. False Green 금지`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 금지:
  - 기존 build 결과 복사
  - Local Maven cache 성공
  - 기존 Generated Source copy
  - MBR/EXS 수동정리만 수행
  - help만 확인
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.34 — Evidence
- **원 Requirement:** Open Git Steering `#34. Evidence`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 최소 기록:
  - 기준 Source Identity
  - 실제 실행 명령
  - Start/End
  - ExitCode
  - PASS/FAIL/미검증
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.35 — Delete/Garbage 안전
- **원 Requirement:** Open Git Steering `#35. Delete/Garbage 안전`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 삭제 후보는 다음 모두 충족할 때만 Delete Manifest에 넣는다.
  - 사용자 승인 없이 실제 제품 Source 삭제를 수행하지 않는다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.36 — Git 안전
- **원 Requirement:** Open Git Steering `#36. Git 안전`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 사용자 승인 없이 수행 금지:
  - commit
  - push
  - branch
  - tag
  - reset
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.37 — 최종 Acceptance Criteria
- **원 Requirement:** Open Git Steering `#37. 최종 Acceptance Criteria`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 다음 질문에 모두 YES여야 한다.
  - 최신 Source에서 Framework Binary Fresh Build했는가?
  - 기존 JAR을 복사하지 않았는가?
  - Generated Domain을 latest Generator로 Fresh Generate했는가?
  - 기존 Generated Java Source copy를 사용하지 않았는가?
  - Backoffice Source Projection 후 Fresh Compile/Test했는가?
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.38 — 최종 외부 개발자 판정
- **원 Requirement:** Open Git Steering `#38. 최종 외부 개발자 판정`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - Codex는 최종적으로 실제 실행 근거를 가지고 다음 질문에 답해야 한다.
  - > Private CPF Source가 없는 Fresh 환경에서 Open Git Release만 받은 개발자가 공개된 Binary와 명령을 사용해 최초 설정 → Bootstrap → 기존 Domain Build/Test → 신규 Domain Fresh Generation → 신규 Domain Build/Test → EDU Example Test → Backoffice/UI Build/T...
  - YES가 아니면 Open Git Release는 READY가 아니다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.39 — 최종 개발 정본 판정
- **원 Requirement:** Open Git Steering `#39. 최종 개발 정본 판정`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - 또한 다음 질문에도 YES여야 한다.
  - > 현재 `CPF_FINAL_TARGET_REQUIREMENTS.md`, Requirement 원장/Closure Inventory 및 관련 Canonical 문서가 실제 Open Git Release Source/Generator/Artifact Policy/Developer CLI/EDU 구현과 동일한 최종 제품 규칙을 상세하고 왜곡 없이 설명하고 있는가?
  - NO이면 정본을 현행화한 뒤 다시 검수한다.
  - ---
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

#### WP-11.40 — 최종 한 문장
- **원 Requirement:** Open Git Steering `#40. 최종 한 문장`를 축약하지 않고 적용한다.
- **현재 상태:** `QA/개발 착수` — 기존 Source 구현 존재 여부를 먼저 확인하며, 구현이 있으면 재개발하지 않고 Gap만 수정한다.
- **원문 핵심 Acceptance:**
  - > Open Git Release는 최신 Private Source를 기준으로 매번 Clean하게 재구성하며, Framework Binary는 Fresh Build하고 Generated Customer Domain Java Source는 latest Generator로 Fresh Generate한다. Backoffice/EDU/UI처럼 사람이 유지하는 Canonical Source만 P...
  - 다시 강조한다: 개발 정본 현행화는 선택사항이 아니라 이번 Steering의 필수 Acceptance Criteria다.
- **영향 Owner / Source:** `cpf-tools/release/open-git/**`, `cpf-tools/runtime/cli/**`, Generator/Generated Domain, Publication/Catalog/BOM, `cpf-education`, Backoffice, OpenAPI/UI, `.gitignore`, Canonical Requirement/Inventory.
- **실제 Consumer:** Release 담당자 `cpf open-git*`, 외부 개발자 `cpf bootstrap/build/test/verify/status/stop/reset/domain new/domain sync`, Generated Domain/EDU/Backoffice consumer.
- **개발 상세 단계:**
  1. 이 Requirement의 현재 Source/Policy/Test/CLI/정본 Consumer를 전수 확인한다.
  2. 이미 구현된 부분은 유지하고, 빠진 Owner/Consumer/Failure Path만 Root Cause 단위로 수정한다.
  3. Product 결함을 Release-only workaround로 숨기지 않고 정확한 Private canonical owner를 수정한다.
  4. Targeted Test 후 Open Git Release를 clean fresh regenerate한다.
  5. Fresh Open Git workspace/isolated cache에서 실제 명령·compile/test/runtime 가능한 범위를 재검증한다.
  6. Source Identity/Manifest/SHA/Leakage/Path/UTF-8/정본 Currentization까지 Evidence로 연결한다.
- **실제 개발내용:** `[개발 후 기록]`
- **실제 수정파일:** `[개발 후 기록]`
- **정적검증:** `[미실행]`
- **런타임/Release 검증:** `[미실행]`
- **Codex검증:** `[미실행 — Developer GPT 완료 후 독립 재검수 필요]`
- **Closure 조건:** 해당 Steering section의 Source·Consumer·Failure Path·Fresh Release/Developer Acceptance가 실제 PASS하고 Canonical Currentization이 완료된 경우에만 `CLOSED`.

### WP-09.03 강화 해석 — Root `cpf-docs` 외 모든 nested `cpf-docs` 제거

기존 `WP-09.03`은 다음 의미로 고정한다.

1. Repository에서 공식 문서 Root는 **프로젝트 루트의 `cpf-docs/**` 하나뿐**이다.
2. `.cpf-admin/cpf-docs/**`, 각 module 하위 `cpf-docs/**`, 생성기/검증기가 만든 nested `cpf-docs/**`는 제품 Consumer가 없는 한 최종 상태에서 **0건**이어야 한다.
3. 삭제만 하지 않고 **생성 Root Cause(Script/Gradle working directory/output root)를 먼저 수정**하여 동일 작업 재실행 후 재생성 0을 증명한다.
4. 필요한 공식 Evidence/Review/Handover는 모두 Root `cpf-docs/**` 아래의 canonical 짧은 IA로 이관한다.
5. Codex 보호자료도 Root `cpf-docs/**` 아래에 있으므로 보존한다. Developer GPT는 Codex 전용 파일 내용을 수정하지 않는다.
6. Delete Manifest로 정확한 nested 경로를 관리하고 보호경로/제품 Source 오삭제 0을 검증한다.
7. Final Gate: nested `cpf-docs` 0 + project-cache 0 + buildOutputCleanup 0 + vcs-1 0 + 불필요 빈폴더 0 + path>200 0.

## 6. 개발 시 문서 현행화 규칙

1. Source 수정 전에 해당 작업카드를 `IN_PROGRESS`로 변경한다.
2. Source를 수정하면 `실제 개발내용`, `실제 수정파일`, `실제 Consumer 변경`, `Side Effect`를 즉시 기록한다.
3. Source만 수정된 상태는 `SOURCE_FIXED`; 정적 PASS만으로 `CLOSED` 금지.
4. Runtime 대상은 사용자 로컬 PowerShell 한 줄 검증 결과까지 기록하고 오류 0일 때만 Runtime PASS.
5. DB 영향이 확인되면 해당 작업카드의 DB3 영향 상태를 YES로 확정하고 Oracle/PostgreSQL/MariaDB를 같은 WP에서 동시에 닫는다.
6. Developer GPT 신규/수정 영역은 `Codex 재검수 필요=YES`를 유지하고 재현 명령/Evidence를 남긴다.
7. Codex가 나중에 PASS하더라도 Developer GPT Source/Runtime Evidence와 대조한 후 최종 Closure를 유지한다.
8. 신규 Root Cause는 목록 밖에서 수정하지 말고 기존 WP의 하위 인덱스로 추가한다.

## 7. 로컬 Runtime 결과 기록 표준

각 Runtime 대상 작업카드에는 개발 완료 후 최소 다음을 기록한다.

- 사용자 실행 PowerShell 한 줄
- 사용 Source Identity / Overlay SHA-256
- Java 25 / Docker·DB asset / Node·Browser 환경
- 시작시각 / 완료시각
- 단계 N/Total
- 각 단계 PASS/FAIL
- ExitCode
- 실패 Task/Test/Endpoint/SQL
- 로그 절대경로
- 재보정 후 재실행 횟수
- 최종 오류 0 여부

## 8. DB3 변경 시 작업카드 강제 확장

DB 변경이 하나라도 발생한 작업카드는 아래 하위 검증을 **생략할 수 없다**.

1. Canonical DB Source
2. DB Initializer / Fresh Init
3. Oracle Render/DDL
4. PostgreSQL Render/DDL
5. MariaDB Render/DDL
6. Migration append-only
7. Supported Previous Version Upgrade
8. Rollback 또는 명시적 Recovery
9. Seed parity
10. Runtime Query/Repository/Mapper
11. Generator/Generated Domain 영향
12. Existing Data Preservation
13. Fresh vs Upgrade Current Schema parity
14. Oracle 실제 Runtime
15. PostgreSQL 실제 Runtime
16. MariaDB 실제 Runtime
17. Evidence/Source Identity

한 항목이라도 미실행이면 `런타임검증완료=PASS`, `전체 Closure=CLOSED` 금지.

## 9. 최종 개발 완료 리뷰 형식

개발 완료 시 별도의 축약 보고서로 다시 번호를 만들지 않는다. **이 문서의 동일 기존 105개 ID + WP-10/WP-11 추가 ID를 그대로 사용**하여 다음을 1:1로 채운다.

`원 요구 → QA 사실 → Root Cause → 변경 이유 → 변경 Source/Consumer → 실제 개발내용 → 추가 Finding → Side Effect → 정적 결과 → Runtime 결과 → DB3 결과 → Codex 결과 → Evidence → 최종 상태`

이 구조로 완료된 문서를 최종 ZIP과 다음 세션 Handover에 함께 포함한다.
---

# 2026-08-25 Developer GPT 실제 Source Reconciliation / 개발 결과 현행화

> 이 절은 앞선 계획 카드의 추정 상태보다 우선한다. 기준은 `CPF_FULL_SOURCE_FOR_NEXT_QA_20260824_203050.zip`을 fresh extract한 뒤 실제로 적용·검증한 현재 Source다.

## 상태축

| 영역 | 개발완료 | 정적검증완료 | 런타임검증완료 | Codex검증완료 | Codex 재검수 |
|---|---|---|---|---|---|
| DB3 referenceFixture production 분리 | 완료 | PASS | 미실행 | 미실행 | YES |
| BAT/CEC Identity (`BAT/BAT`, `CEC/CEC/CENTER_CUT_RUNNER`) | 완료 | PASS | 미실행 | 미실행 | YES |
| V138/R138 append-only migration | 완료 | PASS, V001~V137/R001~R137 byte-diff 0 | 미실행 | 미실행 | YES |
| MBW Approval Execution V139/R139 | 완료 | PASS | 미실행 | 미실행 | YES |
| MBW 동적 판단문서/Before-After/History/Snapshot 결정 | 완료 | PASS | 미실행 | 미실행 | YES |
| MBW UNKNOWN non-mutating reconcile | 완료 | PASS 계약검증 | 미실행 | 미실행 | YES |
| Batch Executor `FILE_WATCH` 실제 Worker Consumer | 완료 | PASS | 미실행 | 미실행 | YES |
| Batch Executor `CENTER_CUT` 실제 Control Plane Consumer | 완료 | PASS | 미실행 | 미실행 | YES |
| ADM Batch Job 유형별 등록 UX | 완료 | PASS 계약검증 | Browser 미실행 | 미실행 | YES |
| Open Git canonical CLI/template 보완 | 완료 | 17/17 PASS | Fresh external runtime 미실행 | 미실행 | YES |
| FullLocal child output console+log 동시출력 | 완료 | Source 계약 확인 | 로컬 실행 필요 | 미실행 | YES |
| CPF Platform DB Physical Naming 고정 정본화 | 완료 | PASS | 해당 변경 Runtime은 DB3에서 확인 필요 | 미실행 | YES |

## 실제 개발 내용

- `referenceFixture`의 `productionDefault=false`가 production `40_business_modules_schema.sql`/Fresh Install에 혼입되던 Renderer mapping을 수정했다. Reference Fixture generated/current lifecycle은 유지했다.
- Center-Cut 기능명 `CENTER_CUT`과 Runtime Role을 분리했다. Center-Cut Runner는 `CEC/CEC/CENTER_CUT_RUNNER`, 일반 Batch는 `BAT/BAT`를 사용한다.
- 기존 released migration V001~V137/R001~R137은 수정하지 않고 V138/R138으로 Runtime Role 전환을 append-only 처리했다.
- MBW Approval 승인 후 실제 Owner 적용 결과를 `MBW_APPROVAL_EXECUTION`에서 PENDING/RUNNING/SUCCEEDED/FAILED/UNKNOWN/RECONCILING/RECOVERED로 분리 추적하도록 V139/R139와 Repository/Service Consumer를 연결했다.
- UNKNOWN Reconcile은 mutation을 재호출하지 않고 Employee/Organization current state를 read-only로 비교해 RECOVERED/UNKNOWN을 판정하도록 했다.
- MBW Approval detail은 raw `payloadJson`을 외부로 노출하지 않고 field-level masking된 `approvalDocument`, Before/After, History, Execution Result를 제공한다.
- 승인/반려 API는 사용자가 상세에서 확인한 `expectedVersionNo`와 `expectedPayloadHash`를 Backend에서 저장 Snapshot과 다시 비교한다.
- Backoffice Approval UI는 Approval ID 직접 결정 입력을 제거하고, 상세에서 확인한 동일 version/hash로만 결정하도록 보완했다.
- `FILE_WATCH`는 enum/UI만 존재하던 False Green을 제거하고 `ApprovedFileExecutor.awaitReady()` 실제 Worker Consumer를 연결했다.
- `CENTER_CUT:<jobId>`는 Control Plane `CpfCenterCutOperations.launch()` 실제 Consumer를 연결하고 등록 시 활성 Center-Cut Job 존재를 검증한다.
- Open Git canonical owner에 `cpf.ps1/cpf.sh` dispatcher와 bootstrap/build/test/stop/reset/domain new/domain sync wrapper를 추가했다.
- `CPF_FINAL_TARGET_REQUIREMENTS.md`에 DB3 개발단계 전체 Lifecycle, CPF Platform DB Naming 고정, BAT/CEC Identity/Batch Executor, Codex 보호/Developer GPT 독립 상태축, 완전 Runtime Test 원칙을 현행화했다.

## 정적 검증 실제 결과

- Historical migration V001~V137/R001~R137 byte-diff: `0`
- DB Vendor Semantic Parity: `PASS` (Oracle/PostgreSQL/MariaDB)
- DB Lifecycle Contract: `PASS`
- Reference DB Lifecycle Contract: `PASS`
- Batch Executor Registration Contract: `PASS` (8 files/check groups)
- Approval State Machine: `PASS`
- Batch Approval Trust Boundary: `PASS`
- Open Git tests: `17/17 PASS`
- Approval/Open Git combined pytest: `23/23 PASS`
- Frontend Consumer Closure: `PASS`
- Backoffice Boundary Contract: `PASS`
- Backoffice OpenAPI Generated Client Consumer: `PASS` (`96/96`)
- ADM OpenAPI/controller exact coverage: `337/337 PASS`
- Backoffice OpenAPI/controller exact coverage: `96/96 PASS`
- Codex protected files baseline comparison: `1,526 files, changed=0, missing=0`

## Runtime 판정

현재 ChatGPT 실행환경은 Java 21이고 Docker/Java25 Full Runtime 환경이 아니므로 필수 Runtime을 PASS로 기록하지 않는다. 최종 로컬 검증은 `cpf-tools/verification/tools/run-cpf-required-full-runtime-validation.ps1`을 Java25 + Docker + 공식 DB3 환경에서 실행해야 한다. 이 명령은 `FullLocal + StrictExit + DB3 verifier-owned isolation + rollback/reapply + Runtime Closure + Browser E2E`를 강제한다.

**전체 상태:** `VERIFICATION_PENDING` — 개발/정적검증은 완료했으나 필수 로컬 Runtime/Codex 독립검증 전이므로 전체 `CLOSED`가 아니다.


# 2026-08-25 최종 Developer GPT Currentization — 이후 계획 상태보다 우선

## 최종 상태축

| WP | 범위 | 개발완료 | 정적검증완료 | 런타임검증완료 | Codex검증완료 | 최종 상태 |
|---|---|---|---|---|---|---|
| WP-00 | 운영정본/Codex 보호/상태모델 | 완료 | PASS | 해당없음 | 해당없음 | CLOSED |
| WP-01 | Codex 완료분 Cross-check | 기존구현+보완 완료 | PASS | 미실행 | Codex 기존 원장 유지 | VERIFICATION_PENDING |
| WP-02 | F304 Observability 후속 | Source Fixed | PASS | 미실행 | PENDING | VERIFICATION_PENDING |
| WP-03 | Kill/Lease/Fencing/UNKNOWN/Recovery | Source Fixed | PASS | 미실행 | PENDING | VERIFICATION_PENDING |
| WP-04~06 | ADM/MBW Approval 동적문서·Snapshot·History·Execution | 완료 | PASS | 미실행 | PENDING | VERIFICATION_PENDING |
| WP-07 | DB3 Canonical/Lifecycle/V138/V139 | 완료 | PASS | Oracle/PG/MariaDB 물리 미실행 | PENDING | VERIFICATION_PENDING |
| WP-08 | Generator/OpenAPI/Generated Client | 완료 | PASS | Browser/Generated Runtime 미실행 | PENDING | VERIFICATION_PENDING |
| WP-09 | Source Identity/Hygiene/Final Package | 완료 | PASS(보호 Codex long-path 예외 별도) | 미실행 | PENDING | VERIFICATION_PENDING |
| WP-10 | BAT/CEC Identity | 완료 | PASS | 미실행 | PENDING | VERIFICATION_PENDING |
| WP-11 | Open Git Fresh Release | 완료 | 17/17 PASS | Fresh external acceptance 미실행 | PENDING | VERIFICATION_PENDING |
| WP-12 | Runtime System Code 외부화 / CPF Platform DB Naming 고정 | 완료 | PASS | 미실행 | PENDING | VERIFICATION_PENDING |
| WP-13 | Batch 실행유형/ADM 등록 UX | 완료 | PASS | 실제 FILE/CENTER_CUT 거래 미실행 | PENDING | VERIFICATION_PENDING |

## WP-12 — Runtime System Identity / CPF Platform DB Naming

- **개발 목적:** Runtime Identity의 하드코딩을 줄이되 CPF Framework 고유 DB physical naming을 불필요하게 동적화하지 않는다.
- **최종 설계:** System Code/Channel/Runtime Role은 Runtime property/YAML 계약을 사용한다. CPF Platform DB의 `CPF_*`, `CMN_*`, `ADM_*`, `BAT_*` 및 canonical schema/object naming은 고정한다.
- **CEC:** `systemCode=CEC`, `channelCode=CEC`, `runtimeRole=CENTER_CUT_RUNNER`; Batch data owner이므로 `BAT_*` 사용.
- **Generated Domain:** Generator-owned canonical naming을 사용한다. Runtime 중 table-name 문자열 조립은 금지한다.
- **개발완료:** 완료
- **정적검증완료:** PASS — RuntimeRole contract, DB3 schema parity, Java syntax.
- **런타임검증완료:** 미실행 — FullLocal 필요.
- **Codex검증완료:** PENDING.

## WP-13 — Batch 실행유형 / ADM 등록 UX

- **실행유형:** `SPRING_BATCH`, `APPROVED_SHELL`, `FILE_WATCH`, `FILE_PROCESS`, `FILE_TRANSFER`, `CENTER_CUT`, `SERVICE_CALL`, `MESSAGE_TRIGGER`, `PROTOCOL_ADAPTER`.
- **FILE_WATCH:** enum/UI-only False Green을 제거하고 Worker의 `ApprovedFileExecutor.awaitReady()` Consumer로 연결했다. 승인 `PATH_ALIAS`, 상대경로, 안정화/marker/size/checksum 조건을 사용한다.
- **CENTER_CUT:** `CENTER_CUT:<jobId>`를 Control Plane `CpfCenterCutOperations.launch()`에 연결하고 등록 시 활성 Center-Cut Job을 검증한다.
- **ADM UX:** 유형 선택박스와 FILE_WATCH/CENTER_CUT 전용 입력을 제공하고 OpenAPI Generated Client로 후보 목록을 조회한다.
- **재발방지:** `verify-cpf-batch-executor-registration-contract.py`.
- **개발완료:** 완료
- **정적검증완료:** PASS
- **런타임검증완료:** 미실행 — 실제 file watch 및 CEC 거래 FullLocal 필요.
- **Codex검증완료:** PENDING.

## 최종 Source Identity

- Baseline ZIP SHA-256: `0eba1e95d1552342a128984930b0f0f533787caad209f8b9e7f04ddcacf7caf1`
- Current Source SHA-256: `7c7b806d4284a5a655731cff60b3cce214cdcec9f73ce489b9f3f96bf9bac809`
- Source files: `8434`
- Source bytes: `49517641`
- Codex 보호파일: `1526` files, changed=`0`, missing=`0`.
- Immutable DB history V001~V137/R001~R137: checked=`629`, changed=`0`, missing=`0`.

## Runtime 완료조건

Source/정적 PASS는 Runtime PASS를 대신하지 않는다. `run-cpf-required-full-runtime-validation.ps1`이 Java 25 + Docker 환경에서 ExitCode 0, DB3 3사 Fresh/Upgrade/Rollback-Reapply/거래 E2E, Batch/CEC 장애복구, Approval, Browser/OpenAPI, Open Git/Fresh consumer와 Side Effect를 모두 PASS할 때만 `런타임검증완료=PASS`로 변경한다.
