# CPF Codex 다음 작업지침 — 2026-08-27 — 기존 작업 연속 수행

> **LONG-TURN MODE:** 현재 Work Package를 끝내기 전에 자의로 turn을 종료하거나 다음 진행 여부를 묻지 마. 계획·중간보고는 중단점이 아니며 같은 turn에서 즉시 작업을 계속해. 현재 하던 작업을 먼저 Source·Consumer·Test/Runtime·Evidence·문서까지 완전히 종결한 뒤 남은 크레딧으로 필수 고위험 독립검증만 진행해. 기존 미완료 전체나 Repository 전체를 전수 재검수하지 마. 같은 Root Cause와 같은 Build/DB/Runtime은 묶어서 처리하고, 크레딧이 줄어들면 신규 범위를 시작하지 말고 현재 WP를 먼저 완결해. ADM/Frontend는 필수 핵심 검증 이후 최후순위로 둬. Git/HEAD/전체 Local Working Tree 상태를 작업 Gate로 쓰지 말고 다른 세션 변경을 건드리지 마. 서버 측 사용량 제한을 우회하는 지침은 아니며 플랫폼 강제 종료 시에만 정확한 Checkpoint/Handover를 남겨.

## 실행 방식 — 현재 Codex turn 연속 수행

- **새 Codex 대화/새 turn으로 리셋하지 말고, 현재 진행 중인 Codex 작업의 다음 입력으로 이 파일을 그대로 사용한다.**
- 현재 Active Finding을 먼저 끝내고 같은 turn 안에서 다음 고위험 검증으로 이어간다.
- DevGPT Final Package에서 신규 Batch Shell 20개의 Git trackability가 보정되었으므로 실제 Local Git에서 ignore되지 않는지도 CX-F-258/Hygiene 범위에 포함한다.

## Source Authority

- 현재 사용자 Local Working Tree가 Source 정본이다.
- DevGPT Overlay 기준 Product Source Identity: `b41abc892e6652ef8461ece1b3daa1057acfbf153185fe8e67ea70a6e20de4af`.
- Git/HEAD/전체 Working Tree 상태를 시작/완료 Gate로 사용하지 않는다.
- 다른 병행 세션 변경을 fetch/checkout/reset/restore/clean/stash로 건드리지 않는다.

## 1. 새 작업으로 리셋하지 말고 기존 Codex Active부터 종결

### 1순위 — CX-F-026 Root Build / Dependency

DevGPT가 최신 Runtime/VSCode에서 재현된 ADM Comparator, Policy Store generic inference, Batch Map, Logging Map.Entry, HTTP sanitizer, fallback comparator, LoggingAspect consumer drift를 보정했다.

Codex는 기존 CX-F-026을 그대로 이어서:

- Java25 Root clean build/test/publication/SBOM
- affected module consumer/dependency/public API/SPI/internal boundary
- generated member/external/scratch build
- distributed artifact pack/One-WAS 선행 artifact
- compile warning/error false green 0

까지 실제 실행하고 Source만 고친 상태로 남기지 마.

### 2순위 — CX-F-307 DB3 / Oracle

DevGPT 변경:

- SQLPlus secret transport Test를 강화된 failure diagnostic 의미와 일치시킴.
- Oracle seed logical mutation과 row-wise MERGE expansion을 의미 기반으로 검증.
- Spring Batch Oracle Verify를 관리 `BAT_SB_` namespace 기준으로 currentize.
- DB Python regression 228 passed / 2 environment skipped.

Codex는 Oracle Physical Fresh→Seed→Verify→Upgrade→Rollback→Reapply→Fault/UNKNOWN→Cleanup을 실제 실행하여 기존 CX-F-307을 종결해. Released V/R migration byte/baseline을 re-anchor하지 마.

### 3순위 — CX-F-258 Hygiene / Windows Path

- Windows path 정책
- UTF-8 PowerShell/Java/Gradle/Docker/native child mojibake 0
- Managed State before/after exact diff 0
- transient cache/generated evidence 0

를 실제 Windows Runtime에서 확인하고 기존 Finding을 종결해.

## 2. 남은 크레딧으로만 고위험 독립검증

우선순위:

1. **Logging 실제 추적** — File↔DB↔Transaction/Segment/Timeline, Header6/trace/execution/instance, 정상/오류/retry/recovery/partial/masking/multi-instance.
2. **Batch 최대강도** — 5 roles + Worker×2, concurrent claim/lease/fencing/kill/takeover/UNKNOWN/reconcile/duplicate prevention/recovery.
3. **Generator** — member/external/scratch fresh generate/build/test/rerun diff 0/root discovery/user-owned protection.
4. **Performance** — signed 64-hex sourceIdentitySha256, mismatch/missing/tamper/artifact mismatch fail-closed + load/soak.
5. **Actual Open Git Fresh Release** — framework fresh publication/public Maven repo/fresh domains/bootstrap/build/test/start/health/stop/reset.
6. ADM/Backoffice/Frontend/Browser는 앞 항목이 끝난 뒤만 수행.

## 3. Full Runtime Harness 독립검증

DevGPT가 Final Runner에서 빠져 있던 다음 항목을 mandatory fail-closed로 추가했다.

- Fresh VSCode Problems Error 0 / Warning 0
- Performance mandatory load/soak
- Actual Open Git Fresh Binary Release
- mandatory stage SKIP/NOT_EXECUTED/UNKNOWN 차단
- Managed State exact path diff Evidence
- 동일 Source Fresh Replay

단순 script 존재 확인이 아니라 실제 full run 결과가 이 항목 없이 PASS할 수 없는지 negative/mutation까지 확인해.

## 4. 수정 시 같은 turn에서 동시 현행화

Source → Owner/Consumer → Test/Verifier → Config/DB/Generator/OpenAPI 영향 → Runtime → Codex Evidence → 관련 current 문서 → Codex Finding 상태 → Garbage/Delete Manifest를 한 묶음으로 끝내.

DevGPT Developer Inventory나 QA 상태를 Codex 권한으로 수정하지 마. Codex-owned 컬럼/원장만 갱신해.

## 5. 종료 조건

- 시작한 Active Finding은 IN_PROGRESS/SOURCE_FIXED/VERIFICATION_PENDING에 두고 새 WP로 넘어가지 않는다.
- 환경 때문에 Runtime이 불가능하면 가능한 Source/Test/Verifier 보완을 모두 끝낸 후 실제 이유/환경/재실행 명령/기대 결과를 남기고 PASS/CLOSED로 기록하지 않는다.
- Codex가 Source를 수정하면 이전 사용자 Full Runtime PASS를 현재 Source에 승계하지 않는다.
