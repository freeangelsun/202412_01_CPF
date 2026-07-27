# CPF Codex 2차 집중 검수 Checklist — 2026-07-27

## 목적

이 구현은 ChatGPT의 1차 개발 결과다. 완료 보고를 신뢰하지 말고 최신 Git Diff, 영향도 Ledger, 실제 Consumer, Runtime과 Evidence를 독립적으로 재검증한다.
결함과 누락은 보고만 하지 말고 올바른 Owner에서 수정·보완한 뒤 동일 검증을 재실행한다.

단, **크레딧 절약을 위해 이미 검증된 CPF 전체를 무조건 처음부터 다시 검수하지 않는다.**
이번 ChatGPT 변경과 Codex 중단 지점에서 시작하고, 실제 회귀가 발견될 때만 범위를 확장한다.

## A. 시작 Baseline

- [ ] `git fetch origin`
- [ ] `master` 최신 SHA 확인
- [ ] ChatGPT patch 적용 Commit/Working Tree 확인
- [ ] `CPF_CHANGE_IMPACT_AND_VALIDATION_LEDGER.md`의 `CHG-20260727-*` Change ID와 실제 Diff 대조

## B. CHG-20260727-ARTIFACT-001 집중검증

### Build Tooling

- [ ] `cpf-tools/build/gradle-plugin` isolated `check validatePlugins`
- [ ] `cpf-tools/build/platform-bom` isolated `check`
- [ ] `gradlew publishCpfLocalPlatformArtifacts --no-daemon --max-workers=1`
- [ ] `gradlew verifyCpfLocalArtifactPropagation --no-daemon --max-workers=1`
- [ ] `${user.home}/.cpf/repository` 또는 지정 Repository에 Core/Common/BAT Contract/BOM/Plugin 실제 artifact 확인
- [ ] Remote Repository 설정 시 자격정보가 로그/Evidence에 노출되지 않는지 확인
- [ ] isolated local repo로 Root build 성공 후 artifact가 갱신되는지 확인
- [ ] 실패한 Root build에서는 shared local repo가 갱신되지 않는지 확인(실패 build artifact 전파 금지)

### Generator

- [ ] `create-domain-repository.ps1`로 임시 Domain 생성
- [ ] 원격 Repository 없이 local CPF artifact만으로 build
- [ ] Generated build가 `com.cpf.domain-conventions`를 해석
- [ ] `clean test verifyCpfPackagedDependencies`
- [ ] bootJar의 `BOOT-INF/lib`에 CPF Core/Common
- [ ] bootWar의 `WEB-INF/lib`에 CPF Core/Common
- [ ] Batch/Center-Cut capability 시 BAT Contract 포함
- [ ] 독립 Repository 삭제 후 동일 입력 재생성 가능
- [ ] 첫 Domain 성공 후에만 두 번째 Domain parity 검증

## C. CHG-20260727-CONTACT-001 집중검증

### Java/Frontend

- [ ] `:cpf-admin:test`
- [ ] `:cpf-biz-admin:test`
- [ ] 기존 AdmOperator/AdmOperatorCreateRequest 생성자 Consumer compile
- [ ] 기존 BZA EmployeeRequest 생성자 Consumer compile
- [ ] ADM frontend test/typecheck/build
- [ ] BZA frontend test/typecheck/build
- [ ] UI 표기: `연락처(휴대폰)`
- [ ] UI 표기: `내부 전화번호`
- [ ] 미입력 값이 빈 문자열이 아닌 null로 Backend에 전달되는지 확인

### DB

- [ ] `check-admin-contact-model.ps1`
- [ ] `sync-database-artifacts.ps1`
- [ ] Migration checksum 재생성 결과가 patch와 일치
- [ ] MariaDB 기존 V58 상태 → V59 적용
- [ ] ADM/BZA API 실제 insert/read
- [ ] ADM 연락처가 `adm_operator_profile`에 저장되고 `adm_operator`에는 연락처 컬럼이 없는지 확인
- [ ] ADM 인증 조회가 연락처 Profile에 의존하지 않는지 확인
- [ ] V59 rollback
- [ ] V59 reapply
- [ ] `check-bza-safe-defaults.ps1`
- [ ] V60 `EMPLOYED` default upgrade/rollback/reapply
- [ ] Fresh Install 후 연락처 필드와 `EMPLOYED` default 존재
- [ ] 개인정보가 Console/Evidence에 원문으로 출력되지 않음

## D. Codex 중단 지점 연속 작업

A/C 검증과 별도로 다음만 이어서 조사한다.

- [ ] MBR/ACC normalized Generator parity
- [ ] Root settings Generated Domain 고정 include 정책
- [ ] `CpfSystemCodes.inferFromTypeName` 실제 Consumer 검색
- [ ] REF가 특정 Generated Domain 없이 boot/test
- [ ] BAT Legacy EDU migration map의 미대체 Gap

## E. 이번 회차에서 기본적으로 재실행하지 않는 것

아래는 이번 변경과 직접 관련 회귀가 발견되지 않으면 반복하지 않는다.

- BAT 158/158 MariaDB PREPARE
- V58 전체 comment migration lifecycle
- 이미 기록된 MBR/ADM/BZA/REF/ACC/GWY 단일 Runtime 전체 반복
- 전체 Browser E2E
- 전체 Multi-instance/Fault suite

최종 통합 검증 기준 Commit에서 한 번에 수행할 수 있도록 `미검증/통합검증 예정`으로 유지한다.

## F. 완료 보고

- [ ] 기준 SHA
- [ ] 실제 수정 파일
- [ ] 보완한 결함
- [ ] 실행한 명령만 기록
- [ ] Test 수/결과
- [ ] V59/V60 DB 결과
- [ ] Generated Domain artifact 경로/버전
- [ ] bootJar/bootWar dependency 결과
- [ ] 실패 이력
- [ ] 미검증 사유
- [ ] Evidence 경로
- [ ] Continuity/Ledger 갱신


## 통합 잔여 Requirement Matrix

- `cpf-docs/work/current/CPF_REMAINING_REQUIREMENT_MATRIX_20260727.md`
- QA 전체 항목을 구현 확인/부분 구현/미완료/통합검증 예정으로 재분류한 정본이며, 다음 검수는 이 Matrix와 Change Ledger를 함께 사용한다.

---

## G. 이 Checklist의 현재 지위

이 문서는 **중간 Checklist**다. Codex는 즉시 투입되지 않으며 ChatGPT 개발이 추가로 진행될 예정이다.
실제 Codex 착수 시 이 문서를 그대로 실행하지 말고 최신 master와 누적 Change Ledger를 기준으로 재작성한다.

특히 다음 원칙을 반영한다.

- 이후 ChatGPT 변경 영향권에 들어온 과거 PASS는 `재검증 필요`로 다시 연다.
- 변경과 무관한 고비용 검증은 자동 반복하지 않는다.
- ChatGPT 1차 구현을 Codex가 대규모 재개발하는 상황은 결함으로 보고 원인/Architecture를 함께 분석한다.

## H. 미래 Codex Gate·Tool 정리 검수

정본: `cpf-docs/guides/CPF_GATE_AND_TOOL_LIFECYCLE_GUIDE.md`

- [ ] 최신 master의 PowerShell Script/Gradle Gate Inventory 작성
- [ ] `DEV_ONLY` / `CI_RELEASE` / `PRODUCT_ADMIN_TOOL` 분류
- [ ] 상위 Aggregate/CI/Guide/다른 Script 실제 Caller 확인
- [ ] 보호 Requirement와 대체 Gate 확인
- [ ] 중복/Legacy/무호출/일회성 Gate 삭제 후보 확정
- [ ] 삭제 시 stale 문서/Task/CI 참조도 함께 제거
- [ ] `QUICK` / `VERIFY` / `FULL` Aggregate가 실제 하위 Gate와 일치하는지 검증
- [ ] 공식 Tool의 옵션/Default/환경변수/입출력/Side Effect/실패/복구/예제 누락 확인
- [ ] Script Help와 Guide 옵션 불일치 검출
- [ ] Runtime 배포물에 `DEV_ONLY`/`CI_RELEASE` Script가 포함되지 않는지 확인
- [ ] 관리 Tool 패키지에는 필요한 `PRODUCT_ADMIN_TOOL`만 포함되는지 확인

ChatGPT가 삭제 여부를 확정하지 못한 Gate는 실제 호출자/Requirement coverage를 확인해 안전하면 Codex가 제거한다.
무근거 대량 삭제는 금지한다.

## I. 미래 Codex Artifact 공급 검수

- [ ] `LOCAL_DEV` Source 변경 → Domain package 자동 반영
- [ ] `REMOTE` CI build가 승인된 Registry의 고정 Version만 사용
- [ ] `REMOTE` artifact 미존재 시 Local Repository fallback 없이 실패
- [ ] `OFFLINE` Bundle에 version/manifest/checksum/BOM 존재
- [ ] `OFFLINE` consumer가 수동 JAR 복사 없이 Gradle로 package
- [ ] bootJar/bootWar/lib에 필요한 CPF Library 실제 포함
- [ ] 동일 Domain Source + 동일 CPF Version의 Dependency Set 재현성 확인

## 20260727_04 이후 재생성 규칙

현재 Checklist는 **최종 Codex 실행본이 아니다.** 이후 ChatGPT 개발이 더 누적된 뒤 Codex 투입 직전 최신 master에서 다시 작성한다.

반드시 추가할 검수축:

### Stack

- `gradle/cpf-stack.properties`와 Wrapper/Root/Module/Generator/Standalone 일치
- Boot 3.4.13 + Java25 + Gradle9.1을 GA로 오표기하지 않음
- Boot 4.x Migration 시 External WAS/Servlet6.1/Spring Batch/MyBatis/Flyway/Generated Domain 회귀

### Artifact

- `aggregateQualityBuild` 실패 시 Shared Local 변경 0
- Root/Included Build staging task 실제 존재/실행
- exact POM/module/BOM/plugin marker/hash
- manifest sourceCommit / promotionState
- promotion 중 실패 rollback
- concurrent publisher
- Local current-HEAD reuse
- REMOTE publish Local side effect 0
- REMOTE missing URL fail-closed
- OFFLINE bundle standalone plugin/dependency resolution
- bootJar/bootWar 내부 exact CPF version/hash

### 이전 PASS 재개방

Build/BOM/Plugin/Generator 영향권의 과거 PASS는 다시 실행한다.
BAT 158 SQL/V58처럼 직접 영향 없는 고비용 Evidence는 무조건 반복하지 않는다.
