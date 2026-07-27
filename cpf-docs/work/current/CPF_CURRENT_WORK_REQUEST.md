# CPF Current Work Request

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 작업 시작 SHA: `702bf83580b9c4db2dbba6482ece233e00842f1b` (`20260727_03`)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA 입력: `CPF 차기 통합 QA 요구사항` — 기준 SHA `9e4edaef...`에서 작성됐으므로 최신 `702bf835...`의 문서 보강 Commit까지 재기준화한다.
- 현재 작업자: ChatGPT 1차 개발. Codex는 즉시 투입하지 않고 몇 차례 ChatGPT 개발 후 최신 누적 Diff 기준으로 2차 독립 검수한다.

## 2. 작업 전 통합 리뷰 결론

QA와 기존 ChatGPT 계획은 큰 방향이 일치한다. 이번 작업에서는 범위를 넓게 벌리지 않고 다음 폐쇄 Change Set을 먼저 처리한다.

### CHANGE-SET-A — Stack / Artifact / Baseline Safety

1. **기술 Stack 지원 상태를 정본화한다.**
   - 현재: Java 25 + Gradle 9.1.0 + Spring Boot 3.4.13.
   - Spring Boot 3.4.13 공식 범위는 Java 24 및 Gradle 8.x까지이므로 현재 조합은 GA Release 가능 상태가 아니다.
   - `TRANSITION` 상태를 명시하고, Boot 4 계열 Migration을 별도 Change Set으로 검증한다.
   - 당장 Major Upgrade를 강행하지 않고 External WAS, Servlet, Spring Batch, Security, MyBatis, Flyway, Generator 회귀 범위를 먼저 닫는다.

2. **Version Single Source를 만든다.**
   - `gradle/cpf-stack.properties`를 Java/Gradle/Spring Boot/Plugin Stack 정본으로 사용한다.
   - Root, Module, Generator, Standalone Export가 같은 값을 사용한다.

3. **Artifact 공급 모드를 명확히 분리한다.**
   - `LOCAL_DEV`: 검증된 Shared Local Maven Repository.
   - `REMOTE`: Nexus/Artifactory 등 승인 Registry만 사용. Local fallback 금지.
   - `OFFLINE`: Manifest/Checksum을 가진 Versioned Offline Maven Bundle.

4. **Local Artifact Publish를 fail-closed로 강화한다.**
   - 자동 Sync 기본값은 `false`.
   - `aggregateQualityBuild` 이후 staging publish → POM/BOM/Plugin Marker/Hash 검증 → repository lock → promote.
   - Manifest에 Commit뿐 아니라 dirty working-tree `sourceFingerprint`를 기록해 stale Artifact 재사용을 막는다.
   - Manifest를 마지막에 기록하고 실패 시 기존 version으로 rollback한다.
   - 범용 Gradle `publish`는 공식 Entry에서 제외하고 Local/Staging/Remote 목적별 Task만 허용한다.
   - Generated Standalone Consumer는 Local/Offline manifest가 없으면 build를 중단한다.

5. **최신 SHA와 문서 상태를 재기준화한다.**
   - 과거 Patch 설명과 당시 SHA는 History로 보존한다.
   - 현재 검수 기준 SHA와 실행 미검증 상태를 별도로 기록한다.

## 3. 이번 Change Set에서 보호할 기존 성공 기능

- BAT 158 Query Pack/V58 기존 Evidence 자체는 폐기하지 않는다.
- ADM의 MBR 직접 종속 제거.
- REF의 MBR 중립화.
- BAT standalone 역할 분리와 Lease/Fencing 구조.
- V59/V60 Source/Migration/Rollback 변경분.
- ADM Identity/Profile 연락처 Ownership.
- BZA `EMPLOYED` 신규 Default 변경분.

단, 이번 Build/Generator/Artifact 변경의 영향권에 들어오는 Compile/Test/Generated Domain/Packaging Evidence는 `재검증 필요`로 다시 연다.

## 4. 이번 Change Set 이후 다음 우선순위

### CHANGE-SET-B — ADM/BZA Data Safety

- ADM 운영자 Identity/Profile/Role 생성 Transaction 원자성.
- Product DB 오류 Memory fallback 금지/fail-closed.
- 연락처 입력 정규화, Masked API/UI, Audit redaction, Log/Trace/Evidence PII 제거.
- BZA 상태 Catalog와 `ACTIVE`/`EMPLOYED` 의미 분리.
- V59/V60 upgrade/rollback/reapply/fresh lifecycle.
- BZA inline SQL과 `com.cpf.core.common.*` 경계 보정.

### CHANGE-SET-C — Generated Domain

- MBR/ACC/Generator Golden parity.
- Root 고정 Include 제거 정책.
- SystemCode Package/Class 추론 제거.
- 임시 Domain 2개 create/export/build/runtime/remove/regenerate.

### CHANGE-SET-D — BAT

- Legacy 삭제 Inventory와 Runtime/EDU parity.
- Generated Job Pack 표준.
- Scheduler/Worker/Control/Center-Cut/Agent Multi-instance.

### CHANGE-SET-E — Gateway / Operations

- target-down failover, timeout/retry/UNKNOWN_RESULT, O/S/B, Header trust, 2 Gateway drift/rejoin.
- ADM/BZA Browser, Observability, Install/Upgrade/Rollback, Release.

## 5. Gate/Tool 정책

정본: `cpf-docs/guides/CPF_GATE_AND_TOOL_LIFECYCLE_GUIDE.md`

- Gate/Tool은 `DEV_ONLY` / `CI_RELEASE` / `PRODUCT_ADMIN_TOOL`로 분류한다.
- 대표 검증은 `QUICK` / `VERIFY` / `FULL`로 정리한다.
- 호출자 0, Requirement 대체 완료, Legacy 전제, 중복 Gate는 삭제 후보로 관리한다.
- ChatGPT가 안전하게 삭제를 확정하지 못한 후보는 Codex가 최신 Caller/Requirement Coverage를 확인 후 삭제한다.
- 개발/CI Gate는 Runtime 제품 배포물에 포함하지 않는다.

## 6. 완료 판정

이번 Change Set은 Source 구현만으로 완료 처리하지 않는다.

필수 후속 검증:

- Java 25 실제 Gradle configuration/compile/test.
- Included Build plugin/BOM build.
- `aggregateQualityBuild`.
- 실패 Build가 Local Repository를 갱신하지 않는 Fault Test.
- staging/promotion/manifest/hash 검증.
- Generated Standalone Domain Local/Offline plugin resolution.
- bootJar/bootWar 정확 Version/Hash 검증.

현재 환경에서 실행하지 못한 항목은 `미검증`으로 유지한다.

## 7. Git 작업

사용자 명시 승인 없이 ChatGPT/Codex가 Commit, Push, Branch, Tag, Release를 생성하지 않는다.
