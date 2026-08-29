# CPF External Development Agent LONG-TURN MODE Policy

## 1. 적용 대상

Codex, Claude 및 향후 CPF 개발·검수에 사용하는 외부 Coding Agent 전체에 적용한다.

## 2. 목적

서버 측 사용량·시간 제한을 변경하거나 우회하는 정책이 아니다. 장시간 CPF 개발·검수에서 에이전트가 자의로 turn을 종료하거나 동일 분석·Build·DB/Runtime을 반복하여 크레딧을 낭비하지 않고, 진행 중 Work Package를 가능한 한 하나의 연속 turn에서 완결하는 것이 목적이다.

## 3. 최상위 실행 순서

1. **현재 에이전트가 이미 하던 작업을 먼저 완전히 종결한다.**
2. 종결 후 남은 크레딧으로 외부 독립검증 가치가 높은 필수 항목만 수행한다.
3. 기존 미완료 Finding 전체나 Repository 전체를 전수 재검수하지 않는다.
4. ADM/Backoffice/Frontend/Browser는 가장 마지막이다.
5. 크레딧이 줄어들면 신규 WP를 열지 말고 이미 시작한 WP를 끝낸다.

## 4. LONG-TURN 비협상 규칙

- 현재 WP 완료 전 자의적 turn 종료 금지.
- `계속할까요?`, `다음 단계로 갈까요?` 같은 종료성 질문 금지.
- 계획·중간보고·진행률은 중단점이 아니며 보고 직후 같은 turn에서 계속 실행.
- 같은 Root Cause는 하나의 WP로 묶어 수정.
- 같은 Root Build, DB Fresh, Batch Runtime, Browser/Frontend Runtime은 Finding별로 반복하지 않고 공통 실행으로 묶음.
- Source 수정 시 Owner/Consumer/Test/Runtime/Evidence/관련 문서/개발요청 정본을 같은 작업에서 현행화.
- 플랫폼 강제 종료 시에만 변경 Source, 수행/미수행 검증, 실제 실패, 재실행 조건, 다음 시작 위치를 Checkpoint/Handover에 남김.

## 5. Source 수정 후 전체 VS Code / Requirement 증적 강제

- Codex/Claude가 Source, Gradle, Catalog, Generator, Config, DB, OpenAPI 또는 Frontend를 하나라도 수정하면 해당 WP를 닫기 전에 **Fresh Gradle Import 기준 전체 Domain/Module VS Code Problems를 다시 확인**한다.
- 검수 통과조건은 **Error 0 / Warning 0**이다. 수정 영향 모듈만 보지 않고 전체 Domain/Module을 확인한다.
- Error 또는 Warning이 하나라도 남으면 환경 탓으로 추정하거나 후속 작업으로 넘기지 않는다. 실제 진단의 Source/Classpath/Gradle/Generated/Dependency/IDE import 원인을 확인하고 **같은 WP에서 즉시 수정 → Fresh Import → Problems 재검증**한다.
- 환경 문제로 Fresh VS Code 검증 자체를 실행할 수 없는 경우에만 `BLOCKED_EXTERNAL`/`NOT_EXECUTED`로 두고, **환경·명령·실제 오류·부족 prerequisite·재실행 조건·기대 결과(Error 0 / Warning 0)·필요 Evidence**를 해당 WP에 개별 기록한다. 미실행을 PASS로 기록하지 않는다.
- 모든 Requirement/Finding은 묶음 문구로 완료 처리하지 않고 최소 **development_status / verification_status / runtime_status / 실제 변경 Source·Consumer / 실행한 Test·Verifier / 실제 결과 / 환경 blocker / 재실행 조건 / Evidence 경로·Source Identity**를 항목별로 남긴다.
- 하나의 공통 Build/Runtime으로 여러 Requirement를 검증할 수는 있으나, 각 Requirement가 그 Evidence에 어떻게 연결되는지 개별 trace를 남긴다.

## 6. 남은 크레딧 기본 우선순위

1. Java25 Root Build / Compile / Dependency
2. Logging 실제 File↔DB↔Transaction/Timeline 추적
3. DB3 Physical Runtime
4. Batch 5-role/2-worker/process-kill/takeover/fencing/UNKNOWN/reconcile
5. Generator / Generated Domain idempotency
6. Performance signed source identity
7. Open Git Actual Fresh Release 핵심 경로
8. ADM / Backoffice / Frontend / Browser — 최후순위

## 7. 병행 Local Working Tree 비간섭

- Git status, HEAD, Git SHA, 전체 Local Working Tree, 전체 Source Identity를 작업 시작/완료 Gate로 사용하지 않는다.
- 다른 세션 변경을 조사·판정·복구·초기화하지 않는다.
- fetch/checkout/reset/restore/clean/stash로 다른 세션 변경을 건드리지 않는다.
- 자신의 할당 범위와 직접 영향 범위만 검증·수정한다.
- RT-02/Performance 제품 기능 자체의 provenance 검증은 해당 기능 계약 안에서만 수행한다.

## 8. 세션 인수인계

다음 세션은 이 정책을 자동 승계한다. 다음 세션이 Codex/Claude 요청서를 만들 때 이 정책을 다시 해석하거나 생략하지 않는다.

## 9. 요청서 필수 문구

> **LONG-TURN MODE:** 현재 Work Package를 끝내기 전에 자의로 turn을 종료하거나 다음 진행 여부를 묻지 마. 계획·중간보고는 중단점이 아니며 같은 turn에서 즉시 작업을 계속해. 현재 하던 작업을 먼저 Source·Consumer·Test/Runtime·Evidence·문서까지 완전히 종결한 뒤 남은 크레딧으로 필수 고위험 독립검증만 진행해. 기존 미완료 전체나 Repository 전체를 전수 재검수하지 마. 같은 Root Cause와 같은 Build/DB/Runtime은 묶어서 처리하고, 크레딧이 줄어들면 신규 범위를 시작하지 말고 현재 WP를 먼저 완결해. **Source를 하나라도 수정했다면 Fresh Gradle Import 후 전체 Domain/Module VS Code Problems Error 0 / Warning 0을 반드시 다시 확인하고, 하나라도 발생하면 같은 WP에서 즉시 원인 수정과 재검증을 끝내. 실행 불가한 경우에만 환경·명령·실제 오류·재실행 조건·Evidence를 Requirement별로 남기고 PASS로 올리지 마. 각 Requirement의 개발/검증/Runtime/Evidence 근거를 개별 기록해.** ADM/Frontend는 필수 핵심 검증 이후 최후순위로 둬. Git/HEAD/전체 Local Working Tree 상태를 작업 Gate로 쓰지 말고 다른 세션 변경을 건드리지 마. 서버 측 사용량 제한을 우회하는 지침은 아니며 플랫폼 강제 종료 시에만 정확한 Checkpoint/Handover를 남겨.
