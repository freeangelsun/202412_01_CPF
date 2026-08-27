# CPF 다음 세션 인수인계 — LONG-TURN / External Agent 운영 고정

## 1. 다음 세션이 반드시 알아야 할 변경

CPF 개발 기본 정본에 Codex/Claude LONG-TURN MODE를 추가했다. 이후 모든 Codex/Claude 요청서와 세션 인수인계는 이 규칙을 자동 승계한다.

## 2. 외부 에이전트 작업 순서

1. 에이전트가 이미 하던 작업을 먼저 완전히 종결한다.
2. 그 뒤 남은 크레딧으로 꼭 필요한 고위험 독립검증만 수행한다.
3. 기존 미완료 전체/Repository 전체를 전수 재검수하지 않는다.
4. Build/Compile, Logging 실제 추적, DB3, Batch, Generator, Performance/Open Git을 우선한다.
5. ADM/Backoffice/Frontend/Browser는 항상 가장 마지막이며 크레딧 부족 시 생략한다.
6. 계획/중간보고 후에도 같은 turn에서 계속 실행한다.
7. 크레딧이 줄어들면 신규 WP보다 이미 시작한 WP 완결을 우선한다.

## 3. LONG-TURN 목적

서버 측 사용량/시간 제한을 우회하는 것이 아니다. 허용된 범위에서 turn을 불필요하게 끊고 재시작하거나 같은 Build/DB/Runtime을 반복해 크레딧을 낭비하지 않도록 하는 CPF 운영 규칙이다.

## 4. 병행 Local Working Tree

- Git/HEAD/전체 Local Working Tree/전체 Source Identity는 작업 시작·적용·완료 차단 Gate로 사용하지 않는다.
- 다른 세션 변경을 조사하거나 reset/restore/clean/fetch/checkout하지 않는다.
- 각 세션은 자신의 할당 범위만 처리한다.
- 제품 RT-02/Performance 자체의 provenance 검증만 해당 기능 계약 안에서 수행한다.

## 5. 사용자 Local Full Runtime 시점

외부 에이전트가 작업 중일 때 Full Runtime을 반복하지 않는다. **Codex/Claude 작업이 완전히 끝난 뒤 최종 Local Full Runtime을 실행**한다.

## 6. 다음 세션이 요청서에 그대로 넣을 문구

> **LONG-TURN MODE:** 현재 Work Package를 끝내기 전에 자의로 turn을 종료하거나 다음 진행 여부를 묻지 마. 계획·중간보고는 중단점이 아니며 같은 turn에서 즉시 작업을 계속해. 현재 하던 작업을 먼저 Source·Consumer·Test/Runtime·Evidence·문서까지 완전히 종결한 뒤 남은 크레딧으로 필수 고위험 독립검증만 진행해. 기존 미완료 전체나 Repository 전체를 전수 재검수하지 마. 같은 Root Cause와 같은 Build/DB/Runtime은 묶어서 처리하고, 크레딧이 줄어들면 신규 범위를 시작하지 말고 현재 WP를 먼저 완결해. ADM/Frontend는 필수 핵심 검증 이후 최후순위로 둬. Git/HEAD/전체 Local Working Tree 상태를 작업 Gate로 쓰지 말고 다른 세션 변경을 건드리지 마. 서버 측 사용량 제한을 우회하는 지침은 아니며 플랫폼 강제 종료 시에만 정확한 Checkpoint/Handover를 남겨.
## 7. 이번 Codex 작업 종료 후 후속 Codex 작업지침 생성 — 필수

다음 세션은 **이번 Codex 작업이 끝났다고 Codex 업무 자체를 종료 처리하지 않는다.**

반드시 다음 순서로 이어간다.

1. **이번 Codex 작업 결과 수집**
   - 실제 완료 Finding
   - 미완료 / SKIP / NOT_EXECUTED
   - 신규 발견 Root Cause
   - 직접 수정 Source
   - Test / Runtime 결과
   - Codex Evidence
   - 문서 / 개발요청 정본 / Delete Manifest 현행화 결과

2. **이번 Codex 결과를 최신 정본에 반영**
   - 과거 요청서나 과거 상태를 그대로 승계하지 않는다.
   - 이번 Codex가 실제 수정한 Source와 실제 실행 Evidence를 기준으로 남은 Gap을 다시 계산한다.
   - `완료`, `미완료`, `환경 미검증`, `신규 결함`을 분리한다.

3. **그 결과를 기준으로 다음 Codex 작업지침을 새로 생성**
   - 다음 Codex에게 기존 작업 전체를 다시 전수검수시키지 않는다.
   - 이번 Codex에서 끝난 항목은 다시 넣지 않는다.
   - 이번 Codex에서 미완료이거나 새로 드러난 결함, 그리고 Codex 독립검증이 꼭 필요한 고위험 항목만 선별한다.
   - 현재 진행 중인 Codex 작업이 있다면 그것부터 완전히 종결하도록 한다.
   - 남은 크레딧으로 수행할 우선순위를 재설계한다.
   - ADM / Backoffice / Frontend / Browser는 핵심 Build / Logging / DB3 / Batch / Generator / Performance보다 항상 후순위로 둔다.
   - `LONG-TURN MODE`와 병행 Local Working Tree 비간섭 규칙을 다음 Codex 요청서 최상단에 다시 포함한다.

4. **다음 Codex 작업지침은 이번 Codex 결과가 나온 뒤 작성**
   - 이번 Codex 실행 전에 미래 Finding을 추측해서 미리 고정하지 않는다.
   - 실제 결과를 보고 다음 작업량, 우선순위, Runtime 재사용 계획을 정한다.
   - 재작업을 최소화하도록 같은 Root Cause / Build / DB / Batch Runtime 항목을 묶는다.

5. **다음 Codex 작업 수행 여부와 Local Full Runtime 순서**
   - 이번 Codex 결과에서 즉시 후속 Source 보정이 필요한 P0/P1이 남아 있으면 **다음 Codex 작업을 먼저 수행**하고 그 변경까지 끝낸 뒤 Local Full Runtime을 실행한다.
   - 이번 Codex 결과가 충분히 닫혀 있고 후속 Codex가 향후 별도 검수용이라면, 다음 Codex 작업지침만 생성·인계하고 사용자 Local Full Runtime을 진행할 수 있다.
   - 어떤 경우에도 Codex가 Source를 다시 수정한 뒤에는 과거 Local Runtime 결과를 현재 PASS로 승계하지 않는다.

### 다음 세션의 고정 산출물

이번 Codex 결과를 받은 다음 세션은 최소한 다음 두 산출물을 만들어야 한다.

- `CODEX_NEXT_WORK_INSTRUCTION_<date>.md`
  - 이번 Codex 실제 결과 기준 다음 필수 작업만 포함
  - LONG-TURN MODE 포함
  - 재작업 최소화 실행계획 요구
  - ADM 최후순위
- `CODEX_RESULT_TO_NEXT_WORK_TRACE_<date>.md`
  - 이번 Codex 완료/미완료/신규 Finding이 다음 Codex 작업으로 어떻게 승계됐는지 추적

**즉, 이번 Codex 작업 종료 → 결과 검토·정본 현행화 → 다음 Codex 작업지침 생성까지가 하나의 인수인계 흐름이다.**

## 8. C 개발/QA 관리_1_8 현재 승계점

- Current DevGPT Product Source Identity: `b41abc892e6652ef8461ece1b3daa1057acfbf153185fe8e67ea70a6e20de4af` / 8,340 files.
- Canonical Development/Closure Inventory: **169행**.
- 이번 세션의 세부 인수인계 정본: `CPF_NEXT_SESSION_HANDOVER_C_DEV_QA_1_8_20260827.md`.
- Local 적용/삭제/저비용/Full Runtime 명령 정본: `CPF_FINAL_LOCAL_APPLY_RUNTIME_COMMANDS_20260827.md`.
- 개발 완료 리뷰: `CPF_DEVELOPMENT_COMPLETION_REVIEW_20260827.md`.
- 최종 전달 인덱스: `CPF_FINAL_DELIVERY_INDEX_20260827.md`.
- Codex는 `CODEX_NEXT_WORK_INSTRUCTION_20260827.md`를 **현재 Codex turn의 다음 입력**으로 사용하고 새 업무로 초기화하지 않는다.
- Local Full Runtime 또는 Codex가 Source를 수정하면 이 Source Identity의 과거 PASS를 승계하지 않고 새 Source Identity에서 Final Runtime/Fresh Replay를 다시 수행한다.
