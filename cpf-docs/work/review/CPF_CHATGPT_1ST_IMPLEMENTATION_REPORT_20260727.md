# CPF ChatGPT 1차 구현 및 Codex 작업 리뷰 — 2026-07-27

## 1. 기준

- Baseline: `fb95e15f90856adcff39040a50b128aa40f5ef43` (`20260727_01`)
- ChatGPT는 Commit/Push/Branch를 생성하지 않았다.
- 이 보고서는 QA 통합 요구와 이전 Codex Continuity를 최신 master Source와 대조한 1차 개발 결과다.

## 2. 이전 Codex 작업 상세 리뷰

### 확인된 성과

1. BAT Legacy Root SourceSet은 최신 Commit에서 실제 삭제됐다.
2. BAT Standalone Module은 `contract/runtime-common/control-server/scheduler/worker/center-cut-runner/host-agent/testkit` 구조로 남아 있다.
3. BAT Runtime Query Contract와 Vendor Runtime Template 대량 추가가 실제 Commit에 존재한다.
4. ADM의 MBR 전용 Controller/Service/Remote Adapter/UI 잔재가 삭제됐다.
5. REF의 MBR Service Call DTO/Client가 제거되고 중립 Echo Client/DTO가 추가됐다.
6. Build Tooling은 Root에서 `cpf-tools/build/gradle-plugin`, `cpf-tools/build/platform-bom`으로 이동했다.
7. ACC datasource namespace 수정과 Generator datasource 격리 보정이 최신 Commit에 존재한다.
8. `CpfTargetServiceResolver`는 고정 `/mbr`/port 추정을 사용하지 않고 명시적 target service/URI host 경계로 변경돼 있다.
9. DB Installer는 profile metadata에서 generated domain을 분류해 전용 initializer로 넘기는 구조가 확인됐다.

### 완료로 승격하지 않은 항목

1. MBR/ACC는 현 Generator Golden tree와 normalized parity가 아니다.
2. Root `settings.gradle`은 MBR/ACC를 여전히 고정 include한다.
3. `CpfSystemCodes.inferFromTypeName` 같은 type/package 추론 계약이 남아 있다. 최신 GitHub code index에서 Consumer가 확인되지 않았지만 index 지연 가능성이 있어 무검증 삭제하지 않았다.
4. BAT EDU 삭제분의 기능 단위 1:1 parity는 최신 Source만으로 최종 완료 판정하지 않았다.
5. 이전 전체 416 Test 성공 뒤 후반 구조 변경이 추가됐으므로 그 숫자를 최신 Commit 전체 PASS로 사용하지 않는다.
6. Browser, Multi-instance, Historical Migration 전체 Chain은 미검증 상태를 유지한다.

## 3. 사용자 추가 Artifact Federation 의견 검토

### 결론

필요한 요구다.

Spring Boot `bootJar`는 runtime dependency JAR를 `BOOT-INF/lib`에 포함하고 WAR는 `WEB-INF/lib`에 포함할 수 있지만,
독립 Generated Domain이 어떤 CPF version/JAR를 해석할지는 별도 Artifact Repository 계약이 필요하다.

기존 구조는 다음까지는 있었다.

- CPF Core/Common/BAT public artifact의 `maven-publish`
- CPF BOM
- Convention Plugin
- Generated Domain의 published coordinate dependency
- Root `bootJar` 의존 JAR 포함 검사

그러나 독립 Repository는 원격 `CPF_ARTIFACT_REPOSITORY_URL`이 없으면 최신 로컬 CPF artifact를 자동 해석하지 못했다.
또 Root package gate는 bootJar 중심이어서 external WAS용 bootWar 포함 검증이 부족했다.

### 이번 구현

- shared local Maven repository 기본값: `${user.home}/.cpf/repository`
- override: `CPF_LOCAL_ARTIFACT_REPOSITORY` 또는 `-PcpfLocalArtifactRepository=...`
- CPF Root 성공 `build` 이후의 local auto sync
- 명시 Task: `publishCpfLocalPlatformArtifacts`
- 확인 Task: `verifyCpfLocalArtifactPropagation`
- BAT local-only publication task
- BOM/Gradle Plugin local publication
- Convention Plugin의 local repo 소비
- Generator standalone orchestration 전 local publish
- Exported Domain settings/build local repo 자동 구성
- Generated Domain에 CPF Convention Plugin 적용
- Generated Domain bootJar/bootWar dependency guard
- Root package gate의 bootWar 검증 보강

## 4. ADM/BZA 연락처 구현

### ADM

- `AdmOperator` 응답에 `mobileNo`, `officePhoneNo`
- `AdmOperatorCreateRequest`에 같은 필드
- 기존 생성자 overload로 기존 Consumer source compatibility 보존
- `AdmOperatorService` 목록은 `adm_operator_profile`을 JOIN해 연락처를 Projection하고, 생성 시 Profile에 연락처를 upsert; 인증 Identity 조회는 연락처와 분리
- UI 입력/목록에서 `연락처(휴대폰)`, `내부 전화번호` 분리
- `AdmOperatorContactContractTest`로 신규/구 생성자 계약 고정

### BZA

- `EmployeeRequest.officePhoneNo`
- 구 생성자 overload 유지
- Repository select/insert/update에 `office_phone_no`
- Employee UI에 email/mobile/internal phone 노출
- 공통 CrudTable header가 field label을 사용하도록 보정
- `BzaEmployeeRequestContactContractTest`로 mobile/office 및 구 생성자 계약 고정

### DB

- ADM canonical `adm_operator_profile`에 `MOBILE_NO`, `OFFICE_PHONE_NO`; `adm_operator` 인증 Identity는 연락처 비소유
- BZA canonical schema에 `office_phone_no`
- V59 forward migration과 rollback 추가
- lifecycle copy/checksum을 patch에 포함

## 5. BZA Safe Default QA 보정

QA의 `BZA-005`는 직원 신규 기본 재직 상태를 `EMPLOYED`로 요구하지만 최신 Backend/DDL은 `ACTIVE`였다.
기존 직원 상태를 일괄 변환하지 않고 신규 입력의 안전한 Default만 정렬했다.

- `BzaBackofficeService`: 미입력 `employmentStatus` → `EMPLOYED`
- Canonical `bza_employee.employment_status`: DEFAULT `EMPLOYED`
- `V60__bza_employee_safe_defaults.sql` / rollback 추가
- `check-bza-safe-defaults.ps1` 추가

상태는 Source 구현이며 DB 실행은 미검증이다.

## 6. 이번 환경에서 실제 수행하지 못한 검증

이 세션은 GitHub connector로 Source를 읽고 patch를 작성했으며 완전한 Repository checkout/Gradle Wrapper/MariaDB Runtime을 실행할 수 있는 환경이 아니다.

따라서 다음은 **PASS가 아니다**.

- Gradle compile/test
- included build publication
- local Maven artifact 실제 생성
- Generated Domain PAY/INS 실제 build
- bootJar/bootWar ZIP 실제 dependency 확인
- Frontend npm test/typecheck/build
- DB artifact sync
- MariaDB V59 upgrade/rollback/reapply
- Browser/Multi-instance

## 7. Codex에 넘길 검증 범위

다음 검증만 먼저 수행한다.

1. 변경 파일 정적 diff 확인
2. `verifyCpfLocalArtifactPropagation`
3. 임시 Generated Domain 1개를 먼저 생성/독립 build/package
4. 성공하면 두 번째 Domain으로 normalized/repeatability 확인
5. ADM/BZA targeted test
6. ADM/BZA frontend targeted build
7. admin contact static gate
8. DB artifact sync
9. MariaDB V59 lifecycle

위가 통과하기 전에 전체 Browser/Multi-instance/BAT 158 query pack을 다시 돌리지 않는다.

## 8. 다음 개발 우선순위

1. MBR/ACC Golden projection과 Root include 정책
2. `CpfSystemCodes` inference 잔재
3. REF dependency-0 gate
4. BAT EDU parity
5. Platform inline SQL
6. Gateway Resilience
7. ADM/BZA 기능 완결성
8. 통합 DB/Browser/Multi-instance/Fault Evidence


## 통합 잔여 Requirement Matrix

- `cpf-docs/work/current/CPF_REMAINING_REQUIREMENT_MATRIX_20260727.md`
- QA 전체 항목을 구현 확인/부분 구현/미완료/통합검증 예정으로 재분류한 정본이며, 다음 검수는 이 Matrix와 Change Ledger를 함께 사용한다.

## 현재 기준 상태 보정 — 20260727_04

이 문서의 `fb95e15f...` Baseline은 **20260727_02 Patch를 만들 당시의 역사적 시작점**이다.
현재 master 검수 기준은 `702bf83580b9c4db2dbba6482ece233e00842f1b` (`20260727_03`)이며,
이후 변경은 `CPF_CHATGPT_2ND_IMPLEMENTATION_REPORT_20260727.md`와 누적 Ledger를 따른다.

이전 "구현" 표시는 현재 latest SHA에서 Runtime 검증 완료를 의미하지 않는다.
Build/BOM/Plugin/Generator/Artifact 공급이 다시 변경되었으므로 관련 과거 PASS는 `재검증 필요` 상태로 다시 연다.
