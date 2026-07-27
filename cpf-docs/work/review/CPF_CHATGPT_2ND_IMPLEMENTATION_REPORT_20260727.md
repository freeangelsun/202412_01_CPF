# CPF ChatGPT 2차 구현 보고 — Stack / Artifact / Baseline Safety

## 1. 기준

- 시작 SHA: `702bf83580b9c4db2dbba6482ece233e00842f1b` (`20260727_03`)
- 상태: **미커밋 Root-relative Patch**
- 사용자 승인 없는 Commit/Push/Branch/Tag/Release: 없음
- QA 병합: `CPF 차기 통합 QA 요구사항`의 `QA-STACK`, `QA-ART`, `QA-BASE` 중 폐쇄 가능한 Source 범위

## 2. 구현 요약

### CHG-20260727-STACK-001

- `gradle/cpf-stack.properties` 추가
- 현재 Stack을 `TRANSITION`으로 명시
- `commercialReleaseGate`에서 `SUPPORTED_GA`가 아니면 차단
- Spring Boot Plugin/BOM Version을 Root/Module/Generator/Standalone에서 단일 정본 사용
- Migration Candidate를 Spring Boot 4.1.0으로 기록하되 실제 Upgrade는 별도 Change Set으로 분리

### CHG-20260727-ARTIFACT-002

- 공급 모드 `LOCAL_DEV / REMOTE / OFFLINE` 배타화
- REMOTE는 Local fallback 금지
- Local Auto Sync 기본값 `false`
- `aggregateQualityBuild` 추가
- Shared Local 공개 전 격리 staging repository 사용
- POM identity, Gradle module metadata, BOM exact constraint, Plugin Marker, SHA-256 검증
- Source Commit을 포함한 Artifact Manifest 생성
- Dirty working tree까지 식별하는 `sourceFingerprint`를 Manifest에 포함하여 stale local Artifact 재사용 차단
- BAT Runtime executable까지 staging된 전체 CPF Version-directory 파일을 Hash Manifest 범위에 포함
- Promotion 대상은 Manifest-backed directory만 허용하고 예상 밖 staged Artifact가 있으면 fail-closed
- Publisher Lock + Manifest Barrier + Rollback Promotion
- `PROMOTED` Manifest가 없는 Local/Offline Standalone Build fail-closed
- Remote Publish를 `cpfInternal` 전용 Task로 분리하여 Local Repository side effect 방지
- 범용 `publish`/BAT `publishStandaloneArtifacts`는 공식 Entry에서 제외하고 명시 대상 Task 사용을 강제
- Offline Artifact Bundle 생성 Task 추가
- 현재 HEAD와 PROMOTED Manifest가 일치하면 Generator가 검증 Local Artifact를 재사용하여 불필요한 aggregate build 반복 방지

### CHG-20260727-DOC-BAT-001

- BAT Scheduler/Execution/Spring Batch JobInstance lifecycle Guide 추가
- 업무 스케줄은 `bat_schedule` DB 정본
- `cpf.batch.scheduler.dispatch-ms`는 polling interval
- due 시 CPF `bat_execution`, Worker launch 시 Spring Batch JobInstance 생성 구조 문서화

## 3. 검증한 것

현재 실행환경 제약 내 정적 검증:

- 변경 Text UTF-8
- 주요 Groovy/Gradle delimiter balance
- 신규 PowerShell Script delimiter balance
- Artifact Mode/Remote fallback 금지 marker
- Manifest/POM/BOM/Plugin Marker 검증 Source marker
- 기존 `transactionGlobalId` 신규 추가 없음
- 문서 기준 SHA/작업상태 갱신

## 4. 실행하지 못한 것

현재 Container는 Java 21이며 PowerShell/Gradle Runtime이 없어 다음은 실행하지 않았다.

- Java 25 Gradle 9.1 configuration/compile/test
- `aggregateQualityBuild`
- Included Build publish
- 실제 Local staging/promotion/rollback
- Generated Domain standalone Local/OFFLINE build
- bootJar/bootWar inspection
- Windows concurrency
- Remote Registry
- GitHub CI

위 항목은 **미검증**이다.

## 5. 남은 위험

- 현재 Boot 3.4.13/Java25/Gradle9.1은 공식 지원 조합이 아니므로 상용 Release 불가
- Local promotion은 Manifest Barrier와 rollback을 구현했으나 실제 Windows concurrent consumer race는 미검증
- Mutable SNAPSHOT immutable policy는 후속
- Remote Registry의 서버-side atomic staging/promotion은 Registry 제품별 검증 필요
- bootJar/bootWar의 exact inner JAR hash 검증은 후속

## 6. 다음 Change Set

`CHANGE-SET-B — ADM/BZA Data Safety`

- ADM Identity/Profile/Role Transaction 원자화
- Product Runtime DB fail-closed
- PII Masking/Audit Redaction/NULL
- BZA Status Catalog
- V59/V60 lifecycle
- BZA inline SQL/Core internal boundary

Codex는 아직 투입하지 않는다. 이후 ChatGPT 변경을 누적한 최신 master에서 Checklist를 재생성한다.
