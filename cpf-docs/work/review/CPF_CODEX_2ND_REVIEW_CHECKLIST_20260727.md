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
