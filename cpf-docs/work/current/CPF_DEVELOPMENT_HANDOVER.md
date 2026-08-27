# CPF Development Handover — C 개발/QA 관리_1_7 — 2026-08-27

## 현재 상태

- Baseline ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260826_195052.zip`
- Baseline ZIP SHA-256: `00abb643557a9562ff3aa40f088c8791af4e01d0cfb056e5509f70d146b90ec0`
- Current Product Source SHA-256: `79264c2975bd0b8504a0e2f8ec375070c08699ebcb512e26323d90d7e39490fb`
- Product Source: `8,334` files / `38,599,665` bytes
- Canonical Requirements: `208`
- Developer Closure: **127/127 완료**
- Verification: **7 완료 / 120 미검증**
- QA 최종 완료: **아님**

## 이번 개발 핵심

- Compile/JDT Root Cause 보정.
- Logging canonical lineage + 실제 EDU probe currentization.
- Security Context/Execution Scope False Red 제거.
- Generator MBR/EXS idempotency owner fix.
- Batch 5-role standalone Shell 20 + profile 15.
- DB lifecycle Working Tree SHA-256 provenance.
- Performance Live 64-hex signed source identity trust.
- ADM/Backoffice route/consumer/approval static closure.
- generated cache/.class garbage 정리 및 Delete Manifest 보강.

## 실환경 필수 재검증

Java25/Gradle9.1/VSCode, DB3, Batch 2-worker kill/UNKNOWN/reconcile, One-WAS, File↔DB↔ADM logging, Runtime OpenAPI, Node22.18+ Browser/a11y, Performance Live, Actual Open Git Fresh Release, Full Runtime/Fresh Replay.

## 다음 순서 — 최신 고정

1. 현재 진행 중인 Codex/Claude 작업이 있으면 **그 작업을 먼저 완전히 종결**한다.
2. 현재 작업 종결 후 남은 크레딧으로 필수 독립검증만 수행한다.
3. 신규 독립검증 기본 우선순위는 Logging 실제 추적 → DB3 → Batch → Generator → Performance/Open Git이다. Build/Compile 문제가 남아 있으면 가장 먼저 해결한다.
4. ADM/Backoffice/Frontend/Browser는 가장 마지막이며, 크레딧이 부족하면 해당 외부 에이전트 세션에서는 수행하지 않는다.
5. 외부 에이전트가 수정한 Source + Consumer/Test + Runtime + Evidence + 관련 문서 + 개발요청 정본 + Delete Manifest를 같은 변경에서 현행화한다.
6. **외부 에이전트 작업이 완전히 끝난 뒤** 사용자 Local에서 Java25/Windows 최대강도 Full Runtime을 최종 1회 수행한다.
7. Runtime FAIL은 전체를 Root Cause별로 묶어 보정하고 동일 기능 Targeted Gate → Full Runtime/Fresh Replay 순으로 재검증한다.
8. 모든 mandatory PASS 후 QA 재검수 요청한다.

## 다음 세션 필수 승계 — LONG-TURN MODE

다음 세션은 Codex/Claude 요청서를 생성할 때 반드시 `CPF_EXTERNAL_AGENT_LONG_TURN_POLICY.md`를 최상단에 포함한다.

- 현재 에이전트 작업을 먼저 종결하고 새 업무로 넘어간다.
- 현재 작업 종결 후 남은 크레딧으로 필수 고위험 독립검증만 진행한다.
- 기존 미완료 전체나 Repository 전체 전수 재검수 지시를 하지 않는다.
- 계획/중간보고는 중단점이 아니며 현재 WP 완결 전 자의적 turn 종료를 금지한다.
- 같은 Root Cause/Build/DB Fresh/Batch Runtime은 묶어서 실행한다.
- 크레딧이 부족해지면 신규 범위를 열지 말고 현재 WP를 완결한다.
- ADM/Backoffice/Frontend/Browser는 항상 최후순위다.
- Git/HEAD/전체 Local Working Tree/전체 Source Identity를 작업 Gate로 쓰지 않는다.
- 다른 세션 변경을 조사·복구·초기화하지 않는다.
- 서버 측 제한을 변경/우회하지 않으며 플랫폼 강제 종료 시에만 정확한 Checkpoint/Handover를 남긴다.
