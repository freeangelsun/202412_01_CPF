# CPF ChatGPT → Codex 인수인계 — 2026-07-27

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- ChatGPT 작업 시작 SHA: `fb95e15f90856adcff39040a50b128aa40f5ef43` (`20260727_01`)
- 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 요구 통합: QA 요구 + Codex 중단분 + 사용자 추가 Artifact Federation/연락처 요구
- ChatGPT Commit/Push/Branch: 수행하지 않음

이 패치는 ChatGPT 1차 구현 결과다. 적용 후 최신 Git Diff를 기준으로 검수하되, 이전에 이미 실제 검증된 CPF 전체를 무조건 처음부터 반복하지 않는다.

## 2. 이번 패치의 Change ID

### CHG-20260727-ARTIFACT-001

목표: CPF Root에서 빌드한 Core/Common/BAT public contract/BOM/Convention Plugin이 원격 Registry가 없는 로컬에서도 shared Maven repository를 통해 독립 Generated Domain에 자동 공급되고, 최종 bootJar/bootWar에 실제 포함되는지 Gate로 보장한다.

핵심 파일:

- `build.gradle`
- `cpf-batch/build.gradle`
- `cpf-tools/build/gradle-plugin/build.gradle`
- `cpf-tools/build/gradle-plugin/src/main/groovy/com/cpf/build/CpfDomainConventionPlugin.groovy`
- `cpf-tools/build/platform-bom/build.gradle`
- `cpf-tools/generator/create-domain.ps1`
- `cpf-tools/generator/create-domain-repository.ps1`
- `cpf-tools/generator/export-domain-repository.ps1`
- `cpf-tools/scripts/verify-local-artifact-propagation.ps1`

집중 검증 순서:

1. `gradlew publishCpfLocalPlatformArtifacts --no-daemon --max-workers=1`
2. `gradlew verifyCpfLocalArtifactPropagation --no-daemon --max-workers=1`
3. 임시 Generated Domain 1개 standalone 생성
4. 생성 Repository `clean test verifyCpfPackagedDependencies --refresh-dependencies --no-daemon --max-workers=1`
5. bootJar `BOOT-INF/lib`, bootWar `WEB-INF/lib`의 CPF JAR 확인
6. 첫 Domain 성공 후 두 번째 Domain parity/repeatability

실패 시 전체 CPF를 재검증하기 전에 publication task wiring, local repository resolution, generated build template 순으로 원인을 좁힌다.

### CHG-20260727-CONTACT-001

목표:

- 휴대폰 UI 표기: `연락처(휴대폰)`
- 별도 필드: `내부 전화번호`
- ADM 운영자/BZA 직원에 동일 의미 적용
- ADM은 `adm_operator_profile` Directory/Profile이 연락처를 소유하고 `adm_operator` 인증 Identity는 비소유
- 빈 선택값은 null

핵심 파일:

- `cpf-admin/src/main/java/com/cpf/admin/opr/dto/AdmOperator.java`
- `cpf-admin/src/main/java/com/cpf/admin/opr/dto/AdmOperatorCreateRequest.java`
- `cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmOperatorService.java` (`adm_operator_profile` JOIN/upsert, 인증 Identity 조회와 연락처 분리)
- `cpf-admin/frontend/src/.../operators/OperatorsPage.vue`
- `cpf-biz-admin/src/main/java/com/cpf/bizadmin/backoffice/...`
- `cpf-biz-admin/frontend/src/.../employees/EmployeesPage.vue`
- `cpf-tools/db/vendor/mariadb/source/30_adm_schema.sql`
- `cpf-tools/db/vendor/mariadb/source/40_business_modules_schema.sql`
- `V59__admin_contact_model.sql` / rollback
- `cpf-tools/scripts/check-admin-contact-model.ps1`
- `cpf-admin/src/test/java/com/cpf/admin/opr/dto/AdmOperatorContactContractTest.java`
- `cpf-biz-admin/src/test/java/com/cpf/bizadmin/backoffice/service/BzaEmployeeRequestContactContractTest.java`

DB 첫 순서:

1. Canonical Source Diff 확인
2. `sync-database-artifacts.ps1` 실행
3. 생성 Bundle/Manifest/Checksum Diff 검토
4. V59 Upgrade
5. ADM/BZA API insert/read
6. Rollback
7. Reapply
8. Fresh Install 구조 확인

ChatGPT 환경에서는 위 DB 실행을 하지 않았다. 실행 전에는 DB 완료로 판정하지 않는다.

### CHG-20260727-BZA-DEFAULT-001

QA `BZA-005`에 맞춰 신규 직원의 미입력 재직 상태 Default를 `ACTIVE`에서 `EMPLOYED`로 정렬했다. 기존 Row는 변경하지 않는다.

집중 검증:

1. `check-bza-safe-defaults.ps1`
2. BZA targeted test
3. `V60__bza_employee_safe_defaults.sql` 적용
4. Default 확인
5. Rollback으로 `ACTIVE` 복원 확인
6. Reapply
7. Fresh Install canonical default 확인

## 3. 이전 Codex 결과 중 재검증 최소화 대상

다음은 이번 Patch와 직접 관련된 회귀가 없으면 무조건 반복하지 않는다.

- BAT Query Pack 158/158 MariaDB PREPARE
- V58 schema-comment Upgrade/Rollback/Reapply
- 이전 MBR/ADM/BZA/REF/ACC/GWY 단일 Runtime 전체 재기동
- 이전 전체 416 Test를 단순 동일 조건으로 처음부터 다시 실행
- 전체 Browser E2E
- 전체 Multi-instance/Fault suite

단, 이번 focused validation에서 공통 계약 회귀가 발견되면 영향 범위만 확대한다.

## 4. Codex 중단분 중 최신 Source에서 구현 확인된 것

- 고정 `/mbr`/port 기반 `CpfTargetServiceResolver` 제거
- DB Installer의 Generated Domain metadata/profile 기반 동적 분류
- ADM의 MBR 전용 Controller/Service/Remote Adapter/UI 제거
- REF MBR Service Call DTO/Client 제거 및 neutral Echo 구조
- Build Tooling `cpf-tools/build/*` 이동
- BAT Legacy `cpf-batch/src/**` 물리 제거
- ACC/Generator DataSource namespace 격리

이 항목들은 다시 설계 조사부터 시작하지 말고 관련 focused Gate만 수행한다.

## 5. 이어서 개발해야 하는 실제 P0

1. MBR/ACC normalized Generator Golden parity
2. Root `settings.gradle` Generated Domain 고정 include 정책
3. `CpfSystemCodes.inferFromTypeName` package/type-name 추론 Consumer 확인 후 Registry/Header/Manifest 계약으로 제거/대체
4. REF Generated Domain dependency-0 Gate
5. BAT Legacy EDU 기능 parity + Job Pack Generator
6. Gateway 실제 Failover/Timeout/Retry Budget/UNKNOWN_RESULT/O-S-B/Header trust/Multi-instance
7. ADM/BZA 업무관리·승인·개인정보·Browser 완결성

## 6. Evidence 규칙

- 실행한 명령만 기록한다.
- 기준 SHA와 Patch 적용 후 SHA를 함께 기록한다.
- 비밀값/연락처 원문을 Evidence에 남기지 않는다.
- 실패 이력을 삭제하지 않고 수정 전/후를 모두 남긴다.
- ChatGPT가 `정적 검토`라고 기록한 항목을 Codex가 자동 PASS로 승계하지 않는다.

## 7. Git 권한

이전 Codex 세션의 Commit/Push 허용은 이번 ChatGPT 패치에 자동 승계되지 않는다.
Codex도 사용자의 해당 시점 명시 승인 범위에서만 Commit/Push한다. Force Push는 금지한다.


## 통합 잔여 Requirement Matrix

- `cpf-docs/work/current/CPF_REMAINING_REQUIREMENT_MATRIX_20260727.md`
- QA 전체 항목을 구현 확인/부분 구현/미완료/통합검증 예정으로 재분류한 정본이며, 다음 검수는 이 Matrix와 Change Ledger를 함께 사용한다.
