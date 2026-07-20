# CPF 통합 안정화 보고서

## 최신 직접 검증 판정

- 작업 기간: `2026-07-16` ~ `2026-07-20`
- 기준 branch: `master`
- 시작·종료 HEAD: `ff4661e673dab9a2f417e75f8ad64fb712c96fa6`
- 요청서 SHA-256: `7e66c053bf603b4898cc42cb2439b48a115f118d4cec51a10a6e7de8e48fbee8`
- 최종 목표 SHA-256: `94c86ef09ecbc6e9745b2f5d75e0d5dd96fcfd3e741ed57cf7fe394e623e2e75`
- 작업 시작 시 worktree: 사용자 선행 변경이 있는 dirty 상태
- commit/push: 수행하지 않음

이번 마일스톤은 ACC·XYZ·BAT 구조 정돈, 생성기와 제거기, PFW 채널 정책, ADM 채널 운영, Gateway→ACC 실제 프록시, BAT 온디맨드 Spring Batch 실행과 검증 ledger 정합성을 완료 범위로 삼았습니다. CPF 최종 목표 전체를 완료했다고 판정하지 않습니다.

### 구현·정돈

- ACC를 `account` 대표 CRUD와 `reference` 생성기 기준 기능으로 구분하고, 삭제된 `acc_sample` 대신 정본 `acc_account`를 조회하도록 DB 계약을 통일했습니다.
- `create-domain`이 전용 DataSource·MyBatis·PFW JobRepository·O/S/B catalog·profile·배포·SQL·test를 만들고 `remove-domain`이 dry-run 후 생성 산출물을 원자적으로 제거하도록 보강했습니다.
- ACC의 patch candidate, 중복 deploy/SQL, 생성 결과와 임시 파일을 제거하고 repository hygiene gate를 추가했습니다.
- XYZ 공식 EDU를 capability-first package로, BAT actual job을 JobDefinition별 vertical slice로, BAT EDU를 학습 유형별 package로 정리했습니다.
- PFW에 DB-first 채널 master·거래별 허용 정책·identity binding·불변 snapshot·local fallback·export/import를 구현했습니다.
- ADM에 채널 API, 메뉴·버튼 권한, 정책 조회·변경·refresh·package export/import UI를 연결하고 서버 권한과 감사를 적용했습니다.
- Gateway가 실행 ID, original/current channel, 인증·서명 요구와 거래별 허용 정책을 검사한 뒤 ACC로 프록시하도록 연결했습니다.
- BAT 온디맨드 요청의 null 포함 JSON, 완료 실행 restart 거절, 신규 rerun과 기존 실행 ID 보존 정책을 보강했습니다.
- README를 제품 문서로 확장하고 ACC README를 reference domain 진입점으로 정리했습니다. 요청서 지침에 따라 DOCX 9종은 이번 구조 변경 중 재생성하지 않았습니다.

### 실기동에서 발견한 결함과 조치

| 발견 | 실제 원인 | 조치와 회귀 방지 |
|---|---|---|
| Gateway→ACC 500 | 선택 검색값이 null일 때 불변 컬렉션의 `contains(null)` 호출 | null 선검사 후 whitelist를 적용하고 ACC service 테스트와 생성기 템플릿을 함께 수정 |
| Gateway→ACC 500 | 유지 대상은 `acc_account`인데 reference Mapper가 제거된 `acc_sample` 조회 | `acc_account` 컬럼을 reference 응답 이름으로 alias하고 ACC test·bootJar·실프록시 재검증 |
| BAT restart 500 | 결과 JSON의 null 값을 `Map.copyOf`가 거부 | 순서를 보존하고 null을 허용하는 불변 복사로 변경, 요청·상태 값 객체 회귀 테스트 추가 |
| restart 후 rerun 400 | 거절된 restart의 null 실행 ID가 기존 추적 ID를 덮어씀 | DB 갱신을 `COALESCE`로 변경하고 재시작 거절 후 재수행 회귀 테스트 추가 |

### 실행 검증 결과

| 검증 | 결과 | 직접 확인 내용 | 증적 |
|---|---|---|---|
| ACC 집중 test·bootJar | 완료 | reference null 기본값과 `acc_account` 계약 컴파일·테스트 | `specs/evidence/20260716_02/full-test.sanitized.log` |
| BAT 집중 test·bootJar | 완료 | null JSON과 실행 ID 보존 회귀 테스트 포함 | `specs/evidence/20260716_02/full-test.sanitized.log` |
| 전체 Gradle test | 완료 | 154 suites, 354 tests, failures 0, errors 0, skipped 4 | `specs/evidence/20260716_02/full-test.sanitized.log` |
| 전체 qualityGate | 완료 | 87 tasks, Java 25 major 69, 구조·SQL·UTF-8·OpenAPI·샘플·배포·증적 gate | `specs/evidence/20260716_02/quality-gate.sanitized.log` |
| MariaDB 전체 설치 | 완료 | 7개 SQL 실행 단계, 38개 schema·seed·FK·index·권한 검사 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json` |
| 생성·제거 smoke | 완료 | 임시 PYM test·bootJar·bootWar 후 dry-run·실제 제거 | `specs/evidence/20260716_02/create-domain-result.sanitized.json`, `specs/evidence/20260716_02/remove-domain-smoke.sanitized.json` |
| ADM runtime·권한 | 완료 범위 | DB health, 운영 API, 관리자 200, 조회 역할 허용 200·쓰기 403, 감사 diff | `specs/evidence/20260716_02/adm-runtime-smoke-result.sanitized.json`, `specs/evidence/20260716_02/adm-permission-runtime-result.sanitized.json` |
| ADM OpenAPI | 완료 범위 | OpenAPI 3.1, 160 paths, 28 tags와 필수 채널·로그·배치 경로 | `specs/evidence/20260716_02/openapi-runtime-result.sanitized.json` |
| ADM browser | 완료 범위 | 인증 후 채널 정책 메뉴 click, 6개 행, console error 0 | `specs/evidence/20260716_02/adm-channel-ui-browser-result.sanitized.json` |
| Gateway→ACC | 완료 범위 | ACC·Gateway health, `OACCQY0001` 프록시 200, route·Gateway instance 헤더 | `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` |
| BAT 온디맨드 | 완료 | 접수 202, Job 완료, PFW/Spring 실행 ID, step 1건, restart·rerun 202 | `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` |
| 종료 정리 | 완료 | 실기동한 ACC·Gateway·BAT 프로세스 종료와 포트 해제 | `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` |
| ledger 정합성 | 완료 | 94개 check ID의 report·matrix·GAP·evidence와 파일 존재 검사 | `specs/evidence/20260716_02/report-matrix-evidence-consistency.sanitized.json` |
| 요청서 보호 | 완료 | 작업 시작·종료 SHA-256 일치, 요청서 수정 없음 | `specs/evidence/20260716_02/cpf-new-request-protection.sanitized.json` |

### 현재 상태와 남은 리스크

- 94개 ledger 판정은 `완료 48`, `부분 구현 30`, `재확인 필요 12`, `미검증 4`입니다.
- external Tomcat/WAR와 JNDI parity는 실행하지 않았습니다.
- Gateway streaming·cancellation, 다중 인스턴스 route·failover·circuit·drain은 실행하지 않았습니다.
- Redis·Kafka·RabbitMQ, Vault/KMS, 원격 파일 서버·object storage와 운영 mTLS는 외부 인프라 실검증이 남았습니다.
- ADM 채널 화면 외 전체 ADM/BZA desktop·tablet·mobile browser E2E와 접근성 검증은 남았습니다.
- BAT 단일 worker 온디맨드는 검증했지만 다중 worker claim·ghost 오탐·장시간 checkpoint 복구는 남았습니다.
- DOCX 9종은 OpenXML 구조 검사만 통과했으며 최신 구조 반영 재생성·Word 실제 열기는 최종 정본화 단계로 보류했습니다.
- `CPF_NEW_REQUEST.md`와 최종 목표 파일을 포함한 사용자 선행 변경은 보존했습니다.

### 항상 지켜야 할 기준 점검

| 기준 | 판정 |
|---|---|
| 문서·소스·SQL·OpenAPI·EDU 정합성 | ledger와 sample coverage 기준 완료, DOCX 내용 freshness는 보류 |
| README는 제품 진입점, 상세 상태는 별도 문서 | 완료 |
| 기능 변경 시 README·상태·SQL·EDU 동시 현행화 | 이번 변경 범위 완료 |
| 신규 주석·설명·SQL COMMENT 한글 | UTF-8·mojibake·SQL gate 통과 |
| EDU source와 대응 test | sample coverage 50/50 통과 |
| 미실행 검증을 성공으로 보고하지 않음 | 완료 |
| 민감정보를 소스·문서·증적에 남기지 않음 | security·evidence gate 통과 |

### 변경 파일 목록

아래 목록은 `HEAD` 대비 현재 worktree 전체이며 작업 시작 전 사용자 변경도 포함합니다. `CPF_NEW_REQUEST.md`, `CPF_FINAL_TARGET_REQUIREMENTS.md`, `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`, `CPF_FULL_REQUEST_ALIGNMENT_APPLY_NOTE_20260716.md`는 사용자 입력·선행 변경으로 보존했으며 Codex 구현 대상으로 수정하지 않았습니다. 삭제 파일은 ACC 생성 후보 중복 또는 BAT EDU 중복 정리에 해당합니다.

<!-- CPF_CHANGED_FILES_BEGIN -->
- 수정 `.gitignore`
- 수정 `CPF_EVIDENCE_INDEX.md`
- 수정 `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 수정 `CPF_GAP_MATRIX.md`
- 수정 `CPF_NEW_REQUEST.md`
- 수정 `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- 수정 `CPF_STABILIZATION_REPORT.md`
- 수정 `README.md`
- 수정 `acc/README.md`
- 삭제 `acc/create-domain-result.json` - 생성 후보·중복 산출물 제거
- 삭제 `acc/deploy/env/dev-acc.env` - 구조 정돈에 따른 제거
- 삭제 `acc/deploy/env/local-acc.env` - 구조 정돈에 따른 제거
- 삭제 `acc/deploy/env/prod-acc.env` - 구조 정돈에 따른 제거
- 삭제 `acc/deploy/env/stg-acc.env` - 구조 정돈에 따른 제거
- 삭제 `acc/deploy/inventory/dev-services.json.acc.candidate.json` - 생성 후보·중복 산출물 제거
- 삭제 `acc/deploy/inventory/local-services.json.acc.candidate.json` - 생성 후보·중복 산출물 제거
- 삭제 `acc/deploy/inventory/prod-services.template.json.acc.candidate.json` - 생성 후보·중복 산출물 제거
- 삭제 `acc/deploy/inventory/stg-services.json.acc.candidate.json` - 생성 후보·중복 산출물 제거
- 수정 `acc/manifest/standard-execution-catalog.json`
- 삭제 `acc/patch-candidates/README.acc.candidate.md` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/apply-order.md` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/deploy/env/dev-acc.env` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/deploy/env/local-acc.env` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/deploy/env/prod-acc.env` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/deploy/env/stg-acc.env` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/deploy/inventory/dev-services.json.acc.candidate.json` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/deploy/inventory/local-services.json.acc.candidate.json` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/deploy/inventory/prod-services.template.json.acc.candidate.json` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/deploy/inventory/stg-services.json.acc.candidate.json` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/settings.gradle.patch` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/smoke-acc.ps1` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/sql/40_business_modules_schema.acc.candidate.sql` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/sql/50_framework_seed.acc.candidate.sql` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/sql/60_adm_seed.acc.candidate.sql` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/sql/70_bza_menu_seed.acc.candidate.sql` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/sql/99_smoke_check.acc.candidate.sql` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/sql/migration/Vxx__acc_domain.sql` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/src/main/resources/application-acc-dev.yml` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/src/main/resources/application-acc-local.yml` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/src/main/resources/application-acc-prod.yml` - 생성 후보·중복 산출물 제거
- 삭제 `acc/patch-candidates/src/main/resources/application-acc-stg.yml` - 생성 후보·중복 산출물 제거
- 삭제 `acc/sql/Vxx__acc_domain.sql` - 생성 후보·중복 산출물 제거
- 이동 `acc/src/main/java/cpf/acc/AccountApplication.java` -> `acc/src/main/java/cpf/acc/AccApplication.java`
- 수정 `acc/src/main/java/cpf/acc/account/dto/AccAccountSearchCriteria.java`
- 삭제 `acc/src/main/java/cpf/acc/adapter/local/LocalAccountQueryAdapter.java` - 구조 정돈에 따른 제거
- 삭제 `acc/src/main/java/cpf/acc/controller/AccountController.java` - 구조 정돈에 따른 제거
- 삭제 `acc/src/main/java/cpf/acc/port/AccountQueryPort.java` - 구조 정돈에 따른 제거
- 생성 `acc/src/main/java/cpf/acc/reference/adapter/local/LocalAccountReferenceQueryAdapter.java`
- 이동 `acc/src/main/java/cpf/acc/adapter/remote/RemoteAccountQueryProxy.java` -> `acc/src/main/java/cpf/acc/reference/adapter/remote/RemoteAccountReferenceQueryProxy.java`
- 이동 `acc/src/main/java/cpf/acc/batch/AccountBatchConfig.java` -> `acc/src/main/java/cpf/acc/reference/batch/AccountReferenceBatchConfig.java`
- 생성 `acc/src/main/java/cpf/acc/reference/controller/AccountReferenceController.java`
- 이동 `acc/src/main/java/cpf/acc/dto/AccountSearchRequest.java` -> `acc/src/main/java/cpf/acc/reference/dto/AccountReferenceSearchRequest.java`
- 이동 `acc/src/main/java/cpf/acc/facade/AccountFacade.java` -> `acc/src/main/java/cpf/acc/reference/facade/AccountReferenceFacade.java`
- 생성 `acc/src/main/java/cpf/acc/reference/port/AccountReferenceQueryPort.java`
- 생성 `acc/src/main/java/cpf/acc/reference/repository/AccountReferenceRepository.java`
- 생성 `acc/src/main/java/cpf/acc/reference/service/AccountReferenceService.java`
- 이동 `acc/src/main/java/cpf/acc/validation/AccountSearchValidator.java` -> `acc/src/main/java/cpf/acc/reference/validation/AccountReferenceSearchValidator.java`
- 삭제 `acc/src/main/java/cpf/acc/repository/AccountRepository.java` - 구조 정돈에 따른 제거
- 삭제 `acc/src/main/java/cpf/acc/service/AccountService.java` - 구조 정돈에 따른 제거
- 이동 `acc/src/main/resources/mybatis/mapper/acc/AccountMapper.xml` -> `acc/src/main/resources/mybatis/mapper/acc/reference/AccountReferenceMapper.xml`
- 수정 `acc/src/test/java/cpf/acc/account/service/AccAccountServiceTest.java`
- 생성 `acc/src/test/java/cpf/acc/reference/service/AccountReferenceServiceTest.java`
- 삭제 `acc/src/test/java/cpf/acc/service/AccountServiceTest.java` - 구조 정돈에 따른 제거
- 수정 `adm/src/main/java/cpf/adm/opr/dto/AdmOperatorCreateRequest.java`
- 수정 `adm/src/main/java/cpf/adm/opr/filter/AdmApiAuthFilter.java`
- 수정 `adm/src/main/resources/static/adm/adm.css`
- 수정 `adm/src/main/resources/static/adm/adm.js`
- 수정 `adm/src/main/resources/static/adm/index.html`
- 삭제 `bat/src/main/java/cpf/bat/edu/job/BatTaskletJobEducationSample.java` - 중복 EDU 샘플을 표준 package 샘플로 통합
- 수정 `bat/src/main/java/cpf/bat/edu/ondemand/BatOnDemandRequest.java`
- 수정 `bat/src/main/java/cpf/bat/edu/ondemand/BatOnDemandStatus.java`
- 수정 `bat/src/main/java/cpf/bat/edu/ondemand/JdbcBatOnDemandRepository.java`
- 이동 `bat/src/main/java/cpf/bat/edu/job/BatJobStatusTransitionEducationSample.java` -> `bat/src/main/java/cpf/bat/edu/restart/checkpoint/BatJobStatusTransitionEducationSample.java`
- 이동 `bat/src/main/java/cpf/bat/edu/job/BatJobParameterValidationEducationSample.java` -> `bat/src/main/java/cpf/bat/edu/tasklet/basic/BatJobParameterValidationEducationSample.java`
- 이동 `bat/src/main/java/cpf/bat/edu/BatTaskletEducationSample.java` -> `bat/src/main/java/cpf/bat/edu/tasklet/basic/BatTaskletEducationSample.java`
- 삭제 `bat/src/main/java/cpf/bat/job/BatSmokeJobConfig.java` - 구조 정돈에 따른 제거
- 이동 `bat/src/main/java/cpf/bat/centercut/BatCenterCutSampleHandler.java` -> `bat/src/main/java/cpf/bat/job/centercut/BatCenterCutSampleHandler.java`
- 이동 `bat/src/main/java/cpf/bat/centercut/BatCenterCutSampleTargetProvider.java` -> `bat/src/main/java/cpf/bat/job/centercut/BatCenterCutSampleTargetProvider.java`
- 이동 `bat/src/main/java/cpf/bat/centercut/BatCenterCutSmokeTasklet.java` -> `bat/src/main/java/cpf/bat/job/centercut/BatCenterCutSmokeTasklet.java`
- 이동 `bat/src/main/java/cpf/bat/job/BatFailTasklet.java` -> `bat/src/main/java/cpf/bat/job/failure/BatFailTasklet.java`
- 이동 `bat/src/main/java/cpf/bat/job/BatHeartbeatSmokeTasklet.java` -> `bat/src/main/java/cpf/bat/job/heartbeat/BatHeartbeatSmokeTasklet.java`
- 생성 `bat/src/main/java/cpf/bat/job/smoke/BatSmokeJobConfig.java`
- 이동 `bat/src/main/java/cpf/bat/job/BatSmokeTasklet.java` -> `bat/src/main/java/cpf/bat/job/smoke/BatSmokeTasklet.java`
- 수정 `bat/src/main/java/cpf/bat/operation/BatHealthController.java`
- 수정 `bat/src/main/java/cpf/bat/operation/BatStartupSmokeRunner.java`
- 삭제 `bat/src/test/java/cpf/bat/edu/job/BatTaskletJobEducationSampleTest.java` - 중복 EDU 샘플을 표준 package 샘플로 통합
- 수정 `bat/src/test/java/cpf/bat/edu/ondemand/BatOnDemandServiceTest.java`
- 이동 `bat/src/test/java/cpf/bat/edu/job/BatJobStatusTransitionEducationSampleTest.java` -> `bat/src/test/java/cpf/bat/edu/restart/checkpoint/BatJobStatusTransitionEducationSampleTest.java`
- 이동 `bat/src/test/java/cpf/bat/edu/job/BatJobParameterValidationEducationSampleTest.java` -> `bat/src/test/java/cpf/bat/edu/tasklet/basic/BatJobParameterValidationEducationSampleTest.java`
- 이동 `bat/src/test/java/cpf/bat/edu/BatTaskletEducationSampleTest.java` -> `bat/src/test/java/cpf/bat/edu/tasklet/basic/BatTaskletEducationSampleTest.java`
- 이동 `bat/src/test/java/cpf/bat/centercut/BatCenterCutSampleTargetProviderTest.java` -> `bat/src/test/java/cpf/bat/job/centercut/BatCenterCutSampleTargetProviderTest.java`
- 이동 `bat/src/test/java/cpf/bat/job/BatSmokeTaskletTest.java` -> `bat/src/test/java/cpf/bat/job/smoke/BatSmokeTaskletTest.java`
- 수정 `build.gradle`
- 수정 `mbr/src/main/resources/application-mbr-local.yml`
- 수정 `pfw-gateway-runtime/src/main/java/cpf/pfw/gateway/service/PfwGatewayProxyService.java`
- 수정 `pfw/src/main/java/cpf/pfw/common/exception/CpfErrorResponse.java`
- 수정 `pfw/src/main/java/cpf/pfw/common/exception/CpfGlobalExceptionHandler.java`
- 수정 `pfw/src/main/java/cpf/pfw/common/exception/CpfResponseCodeResolver.java`
- 수정 `pfw/src/main/java/cpf/pfw/common/web/TransactionHeaderValidationInterceptor.java`
- 수정 `pfw/src/main/java/cpf/pfw/config/CpfSecurityAutoConfiguration.java`
- 수정 `pfw/src/main/resources/application-pfw.yml`
- 수정 `pfw/src/test/java/cpf/pfw/common/web/TransactionHeaderValidationInterceptorTest.java`
- 수정 `scripts/build-all-install-sql.ps1`
- 수정 `scripts/check-architecture-ownership.ps1`
- 수정 `scripts/check-cpf-request-protection.ps1`
- 수정 `scripts/check-feature-evidence.ps1`
- 수정 `scripts/check-report-matrix-evidence-consistency.ps1`
- 수정 `scripts/create-domain.ps1`
- 수정 `scripts/smoke-adm-center-cut-runtime.ps1`
- 수정 `scripts/smoke-adm-runtime.ps1`
- 수정 `scripts/smoke-create-domain.ps1`
- 수정 `scripts/smoke-mariadb-full-install.ps1`
- 수정 `scripts/smoke-openapi.ps1`
- 수정 `scripts/smoke-transaction-meta-runtime.ps1`
- 수정 `scripts/sync-verification-ledger.ps1`
- 수정 `specs/sample-coverage-matrix.md`
- 수정 `specs/sql/00_all_install.sql`
- 수정 `specs/sql/00_all_install_and_smoke.sql`
- 수정 `specs/sql/10_pfw_schema.sql`
- 수정 `specs/sql/50_framework_seed_data.sql`
- 수정 `specs/sql/60_adm_seed_data.sql`
- 수정 `specs/sql/99_smoke_check.sql`
- 수정 `specs/sql/migration/flyway/V1__cpf_baseline_install.sql`
- 수정 `"specs/\352\270\260\353\212\245_\352\265\254\355\230\204_\353\247\244\355\212\270\353\246\255\354\212\244.json"`
- 수정 `"specs/\352\270\260\353\212\245_\352\265\254\355\230\204_\353\247\244\355\212\270\353\246\255\354\212\244.md"`
- 이동 `xyz/src/main/java/cpf/xyz/edu/config/XyzDataSourceConfig.java` -> `xyz/src/main/java/cpf/xyz/config/XyzDataSourceConfig.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/config/XyzEducationMyBatisConfig.java` -> `xyz/src/main/java/cpf/xyz/config/XyzEducationMyBatisConfig.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzAiEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/ai/controller/XyzAiEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzAttachmentEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/attachment/controller/XyzAttachmentEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/operation/XyzAdmBatchLogQueryEducationSample.java` -> `xyz/src/main/java/cpf/xyz/edu/batch/XyzAdmBatchLogQueryEducationSample.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/config/XyzBatchEducationConfig.java` -> `xyz/src/main/java/cpf/xyz/edu/batch/config/XyzBatchEducationConfig.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/config/XyzBatchRepositoryConfig.java` -> `xyz/src/main/java/cpf/xyz/edu/batch/config/XyzBatchRepositoryConfig.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzBatchEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/batch/controller/XyzBatchEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/catalog/controller/XyzEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/service/XyzCenterCutEducationService.java` -> `xyz/src/main/java/cpf/xyz/edu/centercut/application/XyzCenterCutEducationService.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzCenterCutEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/centercut/controller/XyzCenterCutEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/dto/XyzCenterCutExecutionResponse.java` -> `xyz/src/main/java/cpf/xyz/edu/centercut/dto/XyzCenterCutExecutionResponse.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzCmnBusinessEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/cmn/controller/XyzCmnBusinessEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzCmnEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/cmn/controller/XyzCmnEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/service/XyzCrudEducationService.java` -> `xyz/src/main/java/cpf/xyz/edu/crud/application/XyzCrudEducationService.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzCrudEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/crud/controller/XyzCrudEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/dto/XyzCrudEducationRequest.java` -> `xyz/src/main/java/cpf/xyz/edu/crud/dto/XyzCrudEducationRequest.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/dto/XyzCrudEducationResponse.java` -> `xyz/src/main/java/cpf/xyz/edu/crud/dto/XyzCrudEducationResponse.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/dto/XyzCrudEducationStatusRequest.java` -> `xyz/src/main/java/cpf/xyz/edu/crud/dto/XyzCrudEducationStatusRequest.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzExceptionEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/failure/controller/XyzExceptionEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzFileExchangeEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/filetransfer/controller/XyzFileExchangeEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzStandardHeaderEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/header/controller/XyzStandardHeaderEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/operation/XyzOperationTraceEducationSample.java` -> `xyz/src/main/java/cpf/xyz/edu/logging/XyzOperationTraceEducationSample.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzDynamicLogEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/logging/controller/XyzDynamicLogEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzMessagingEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/messaging/controller/XyzMessagingEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/mapper/XyzQueryEducationMapper.java` -> `xyz/src/main/java/cpf/xyz/edu/query/adapter/XyzQueryEducationMapper.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/repository/XyzQueryEducationRepository.java` -> `xyz/src/main/java/cpf/xyz/edu/query/adapter/XyzQueryEducationRepository.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/service/XyzQueryEducationService.java` -> `xyz/src/main/java/cpf/xyz/edu/query/application/XyzQueryEducationService.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzQueryEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/query/controller/XyzQueryEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/dto/XyzQueryEducationCriteria.java` -> `xyz/src/main/java/cpf/xyz/edu/query/dto/XyzQueryEducationCriteria.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/dto/XyzQueryEducationItem.java` -> `xyz/src/main/java/cpf/xyz/edu/query/dto/XyzQueryEducationItem.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/dto/XyzQueryKeysetResponse.java` -> `xyz/src/main/java/cpf/xyz/edu/query/dto/XyzQueryKeysetResponse.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/dto/XyzQueryPageResponse.java` -> `xyz/src/main/java/cpf/xyz/edu/query/dto/XyzQueryPageResponse.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzSecurityEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/security/controller/XyzSecurityEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/facade/XyzLocalFacadeEducationSample.java` -> `xyz/src/main/java/cpf/xyz/edu/servicecall/XyzLocalFacadeEducationSample.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzServiceCallEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/servicecall/controller/XyzServiceCallEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzTelegramEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/telegram/controller/XyzTelegramEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/dto/XyzFixedLengthMemberTelegram.java` -> `xyz/src/main/java/cpf/xyz/edu/telegram/dto/XyzFixedLengthMemberTelegram.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/service/XyzTransactionEducationAuditService.java` -> `xyz/src/main/java/cpf/xyz/edu/transaction/application/XyzTransactionEducationAuditService.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzCompositeTransactionEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/transaction/controller/XyzCompositeTransactionEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzTransactionEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/transaction/controller/XyzTransactionEducationController.java`
- 이동 `xyz/src/main/java/cpf/xyz/edu/controller/XyzUtilityEducationController.java` -> `xyz/src/main/java/cpf/xyz/edu/utility/controller/XyzUtilityEducationController.java`
- 이동 `xyz/src/main/resources/mybatis/mapper/xyz/edu/XyzQueryEducationMapper.xml` -> `xyz/src/main/resources/mybatis/mapper/xyz/edu/query/XyzQueryEducationMapper.xml`
- 이동 `xyz/src/test/java/cpf/xyz/edu/operation/XyzAdmBatchLogQueryEducationSampleTest.java` -> `xyz/src/test/java/cpf/xyz/edu/batch/XyzAdmBatchLogQueryEducationSampleTest.java`
- 이동 `xyz/src/test/java/cpf/xyz/edu/service/XyzCrudEducationServiceTest.java` -> `xyz/src/test/java/cpf/xyz/edu/crud/application/XyzCrudEducationServiceTest.java`
- 이동 `xyz/src/test/java/cpf/xyz/edu/operation/XyzOperationTraceEducationSampleTest.java` -> `xyz/src/test/java/cpf/xyz/edu/logging/XyzOperationTraceEducationSampleTest.java`
- 이동 `xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationMapperSliceTest.java` -> `xyz/src/test/java/cpf/xyz/edu/query/adapter/XyzQueryEducationMapperSliceTest.java`
- 이동 `xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationRepositoryTest.java` -> `xyz/src/test/java/cpf/xyz/edu/query/adapter/XyzQueryEducationRepositoryTest.java`
- 이동 `xyz/src/test/java/cpf/xyz/edu/service/XyzQueryEducationServiceTest.java` -> `xyz/src/test/java/cpf/xyz/edu/query/application/XyzQueryEducationServiceTest.java`
- 이동 `xyz/src/test/java/cpf/xyz/edu/facade/XyzLocalFacadeEducationSampleTest.java` -> `xyz/src/test/java/cpf/xyz/edu/servicecall/XyzLocalFacadeEducationSampleTest.java`
- 생성 `adm/src/main/java/cpf/adm/opr/controller/AdmChannelController.java`
- 생성 `adm/src/main/java/cpf/adm/opr/dto/AdmChannelPackageImportRequest.java`
- 생성 `adm/src/main/java/cpf/adm/opr/dto/AdmChannelPolicySaveRequest.java`
- 생성 `adm/src/main/java/cpf/adm/opr/dto/AdmChannelSaveRequest.java`
- 생성 `bat/src/main/java/cpf/bat/job/centercut/BatCenterCutJobConfig.java`
- 생성 `bat/src/main/java/cpf/bat/job/failure/BatFailureJobConfig.java`
- 생성 `bat/src/main/java/cpf/bat/job/heartbeat/BatHeartbeatJobConfig.java`
- 생성 `bat/src/test/java/cpf/bat/edu/ondemand/BatOnDemandValueObjectTest.java`
- 생성 `CPF_FULL_REQUEST_ALIGNMENT_APPLY_NOTE_20260716.md` - 사용자 선행 입력 보존
- 생성 `pfw/src/main/java/cpf/pfw/channel/adapter/JdbcCpfChannelRegistryAdapter.java`
- 생성 `pfw/src/main/java/cpf/pfw/channel/api/CpfChannelRegistryPort.java`
- 생성 `pfw/src/main/java/cpf/pfw/channel/application/CpfChannelPolicyService.java`
- 생성 `pfw/src/main/java/cpf/pfw/channel/model/CpfChannelDefinition.java`
- 생성 `pfw/src/main/java/cpf/pfw/channel/model/CpfChannelExecutionPolicy.java`
- 생성 `pfw/src/main/java/cpf/pfw/channel/model/CpfChannelPolicyDecision.java`
- 생성 `pfw/src/main/java/cpf/pfw/channel/model/CpfChannelPolicyPackage.java`
- 생성 `pfw/src/main/java/cpf/pfw/channel/model/CpfChannelPolicySnapshot.java`
- 생성 `pfw/src/test/java/cpf/pfw/channel/application/CpfChannelPolicyServiceTest.java`
- 생성 `pfw-gateway-runtime/src/test/java/cpf/pfw/gateway/service/PfwGatewayChannelPolicyTest.java`
- 생성 `scripts/check-repository-hygiene.ps1`
- 생성 `scripts/export-acc-exs-capability-inventory.ps1`
- 생성 `scripts/export-architecture-inventory.ps1`
- 생성 `scripts/remove-domain.ps1`
- 생성 `scripts/smoke-gateway-bat-runtime.ps1`
- 생성 `scripts/smoke-remove-domain.ps1`
- 생성 `specs/evidence/20260716_02/acc-exs-capability-inventory.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/adm-center-cut-runtime-smoke-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/adm-channel-ui-browser.sanitized.png` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/adm-channel-ui-browser-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/adm-log-policy-ui-static-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/adm-permission-runtime-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/adm-runtime-smoke-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/adm-ui-model-consistency.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/architecture-inventory.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/architecture-ownership-scan.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/boot-artifacts.sanitized.log` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/bza-ui-static-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/cpf-new-request.baseline.sha256` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/cpf-new-request-protection.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/create-domain-compile.sanitized.log` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/create-domain-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/datasource-mode-scan.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/deleted-empty-directories.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/deleted-files.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/deploy-env-mbr-dev.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/deploy-inventory-mbr-dev.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/docx-standard.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/edu-module-deploy-alias-scan.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/empty-directory-scan.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/evidence-path-existence-check.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/feature-evidence-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/full-test.sanitized.log` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/garbage-file-scan.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/gradle-remote-deploy-task-scan.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/java25-standard.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/local-port-duplicate-scan.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/log-management-standard.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/mariadb-full-install.sanitized.log` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/openapi-runtime-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/openapi-source-coverage.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/packaged-dependencies-mbr.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/profile-loading-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/quality-gate.sanitized.log` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/remote-deploy-dry-run.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/remote-deploy-dry-run-mbr-dev.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/remove-domain-smoke.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/report-matrix-evidence-consistency.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/repository-hygiene.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/retained-empty-directories.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/retained-review-required-files.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/runtime-config-inventory.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/sample-coverage-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/spring-event-usage-scan.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/transaction-meta-runtime-smoke-result.sanitized.json` - 정제 검증 증적
- 생성 `specs/evidence/20260716_02/work-start.sanitized.json` - 정제 검증 증적
- 생성 `specs/sql/migration/flyway/V35__channel_registry_policy.sql`
<!-- CPF_CHANGED_FILES_END -->

아래에는 이전 마일스톤의 누적 기록과 최신 자동 생성 기능 ledger를 함께 보존합니다.

## 2026-07-16 누적 기록 기준

- 작업일: `2026-07-16`
- 기준 branch: `master`
- 시작 commit: `0788fee1eb329f9ba971660d5958a309f544fcbf`
- 요청서: `CPF_NEW_REQUEST.md`
- 요청서 SHA-256: `fc44647df0ab15c6e4edecde68ef2703133da3853a7bb1289e7cae2f40fceaaa`
- 최종 목표 SHA-256: `28564a5ae4553c4c7509fb61d7e1b0d02eb679b524e7d0e6e7c3bb1ff17dc774`
- commit/push: 수행하지 않음

이번 작업은 최신 요청서의 O/S/B 표준 실행 ID, ACC 생성기 reference domain, PFW Gateway, BAT 온디맨드, S형 내부 공유 API, 배포·SQL·증적 기준을 구현하고 검증한 마일스톤입니다. 장기 최종 목표 전체를 완료한 것으로 판정하지 않습니다.

## 수행 작업

### 표준 실행과 PFW 코어

- O/S/B 실행 ID를 `[유형 1][주제영역 3][기능 2][순번 4]` 10자리 규격으로 통일했습니다.
- 구형 실행 ID 327건을 `pfw_standard_execution_alias`로 분리하고 Flyway V32 업그레이드를 추가했습니다.
- `pfwJdbcTemplate` 소유권을 PFW 코어로 이동해 모든 실행 모듈이 동일한 표준 실행 카탈로그 저장 경계를 사용하게 했습니다.
- DB 저장 실패를 조용히 삼키지 않고 메모리 fallback과 운영 경고 로그를 함께 남기도록 보강했습니다.
- S형 공유 API의 실행 ID 일치, 허용 호출자, 내부 신원, 공개 Gateway 우회 차단 기준을 적용했습니다.

### ACC reference domain과 생성기

- `acc`를 선택형 생성기 검증 reference domain으로 복원하고 build, profile, 배포 inventory, SQL/Flyway, OpenAPI를 연결했습니다.
- ACC 전용 DataSource, JdbcTemplate, 트랜잭션 관리자, MyBatis factory/template을 명시해 다중 DataSource 선택 모호성을 제거했습니다.
- 대표 계정 CRUD에 validation, 검색·정렬 whitelist, 낙관적 버전, 논리 삭제, 감사 사유, before/after 이력을 적용했습니다.
- ACC Spring Batch `JobRepository`는 PFW DB를 사용하고 업무 Step은 ACC 트랜잭션 관리자를 사용하도록 분리했습니다.
- 생성기는 위 DataSource·MyBatis·BatchRepository 경계, 자동 Job 실행 차단, bootJar/bootWar 구성을 함께 생성합니다.

### Gateway·배치·공통 계약

- `pfw-gateway-runtime`을 PFW 소유 선택형 실행 모듈로 추가하고 route snapshot, 실행 ID route, 권한 port, 내부 헤더 재생성, health/OpenAPI를 연결했습니다.
- ACC와 Gateway를 local/dev/stg/prod 환경 파일, deploy inventory, runtime harness, Java 25·패키지 검사, remote deploy dry-run에 포함했습니다.
- BAT 온디맨드 202 접수, 멱등 저장, worker 실행, 상태·step 조회, stop·restart·rerun 계약과 Flyway V34를 추가했습니다.
- CMN facade contract와 MBR→ACC remote proxy, XYZ 외부 연계 EDU를 보강했습니다.

### SQL·증적·문서

- 분할 SQL, 단일 설치 SQL 2종, smoke, seed, Flyway V1/V32/V33/V34를 동기화했습니다.
- ACC app 계정은 DML만, migration 계정은 DDL을 수행하도록 권한을 분리했습니다.
- README를 현재 모듈 구조와 PFW/CMN 소유권, O/S/B, 선택형 ACC/Gateway, 생성 배치 DB 경계에 맞췄습니다.
- 원시 로그는 정본 증적에서 제거하고 비밀정보가 없는 정제 JSON·로그만 유지했습니다.
- 요청 지시에 따라 DOCX 9종은 이번 구조 안정화 단계에서 재생성하지 않았습니다.

## 실행 검증

| 검증 | 결과 | 핵심 확인 |
|---|---|---|
| ACC/Gateway 집중 build·test | 완료 | ACC test·bootJar, Gateway test·bootJar 성공 |
| PFW/ACC/ADM/BAT 회귀 | 완료 | PFW 카탈로그·DataSource 테스트, ACC 전체 테스트, ADM/BAT 컴파일 성공 |
| 생성기 smoke | 완료 | 임시 PYM 생성, test, bootJar, bootWar, Java 25 검증 후 정리 |
| MariaDB 전체 설치 | 완료 | all-install·smoke, seed 재실행, FK/index, 권한 분리 성공 |
| ACC/Gateway 실기동 | 완료 | ACC `8082`, Gateway `8070`, health HTTP 200, 종료 정리 성공 |
| OpenAPI runtime | 완료 범위 | ACC 4 paths/3 tags, Gateway 2 paths/1 tag 확인 |
| ACC 실DB CRUD | 완료 | create/read/update/delete, 논리 삭제 1건, 감사 행위 3건 |
| 거래·카탈로그 저장 | 완료 범위 | ACC 거래 로그 4건, O/S/B 표준 실행 카탈로그 8건 영속 등록 |
| 배치 메타 소유권 | 완료 | `accDB.BATCH_*` 0개, `pfwDB.BATCH_*` 9개 |
| 전체 Gradle test | 완료 | 152 suites, 342 tests, failures 0, errors 0, skipped 4 |
| 전체 qualityGate | 완료 | 82 tasks, Java 25 major 69, SQL·UTF-8·mojibake·배포·증적 gate 성공 |

주요 정제 증적:

- `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json`
- `specs/evidence/20260716_01/mariadb-full-install.sanitized.json`
- `specs/evidence/20260716_01/create-domain-result.sanitized.json`
- `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`

## 작업 중 발견과 조치

| 발견 | 원인 | 조치 |
|---|---|---|
| ACC 기동 시 JdbcTemplate 누락 | 다중 DataSource에 ACC 전용 JDBC bean 없음 | ACC 전용 JdbcTemplate과 qualifier 추가 |
| ACC MyBatis template 모호성 | PFW·CMN template과 생성 Repository 주입 충돌 | ACC factory/template 및 Repository qualifier 추가 |
| ACC가 `accDB.BATCH_*` 조회 | Spring Batch가 기본 primary DataSource를 메타 DB로 선택 | PFW BatchRepository 구성 및 자동 Job 실행 차단 |
| Gateway health DOWN | IN_MEMORY 모드에서도 Rabbit health가 활성화 | broker 기본 모드에서 Rabbit health 비활성화 |
| 표준 실행 카탈로그 DB 0건 | `pfwJdbcTemplate`이 ADM/BAT에만 존재 | PFW 코어로 bean 소유권 이동, ACC 8건 실등록 검증 |
| CLI 경고가 검증 예외로 승격 | MariaDB SSL 경고가 PowerShell native error로 처리 | 경고와 SQL 결과를 분리해 DB 판정 재실행 |

최종 미해결 코드 실패는 없습니다. 전체 테스트와 품질 게이트 결과는 위 표와 정제 증적에 반영했습니다.

## 남은 리스크

- Gateway의 실제 MBR/ACC 대상 proxy, timeout, streaming, cancellation, 다중 인스턴스 route E2E는 미검증입니다.
- external Tomcat/JNDI에서 ACC와 Gateway WAR 기동 parity는 미검증입니다.
- BAT 실제 Job 실행, checkpoint/restart, 다중 worker JobRepository 시나리오는 미검증입니다.
- MBR·ADM·BZA·XYZ·BAT를 포함한 최신 전체 서비스 동시 기동은 이번 마지막 변경 이후 재실행하지 않았습니다.
- 운영 mTLS/service token, Redis/Kafka/RabbitMQ, 파일 서버/object storage, Vault/KMS, 장시간 부하·보안·DR은 외부 환경 검증이 필요합니다.
- 인증 후 ADM/BZA browser E2E와 DOCX 최종 정본화는 후속 검증 범위입니다.

## 기준 준수

- 문서·소스·SQL·OpenAPI·EDU 상태를 같은 ledger 기준으로 동기화했습니다.
- README는 제품 진입점으로 유지하고 상세 상태는 보고서와 매트릭스로 분리했습니다.
- 신규 주석과 설명은 한글로 작성했습니다.
- 실행하지 않은 외부 환경 검증은 완료로 기록하지 않았습니다.
- `CPF_NEW_REQUEST.md`는 읽기 전용 요청 기준으로 사용했으며 수정하지 않았습니다.
- 비밀번호·JWT·서비스 secret 원문을 소스·문서·증적에 저장하지 않았습니다.

## 기능별 판정

아래 표는 `specs/기능_구현_매트릭스.json`에서 자동 생성합니다.

<!-- CPF_LEDGER_BEGIN -->
| check id | 상태 | 핵심 증적 | 판정 |
|---|---|---|---|
| baseline-module-layout | 완료 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json` | PFW·CMN·MBR·ADM·BZA·XYZ 기본 구성과 선택형 BAT·ACC·PFW Gateway 모듈을 settings 및 빌드 기준으로 확인함 |
| bza-rename | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | BIZADM 소스·패키지·환경·DB 명칭을 BZA로 전환하고 legacy name gate를 통과함 |
| acc-exs-cleanup | 완료 | `specs/evidence/20260716_02/acc-exs-capability-inventory.sanitized.json`, `specs/evidence/20260716_02/architecture-ownership-scan.sanitized.json`, `specs/evidence/20260716_02/repository-hygiene.sanitized.json` | 삭제 기능 49건의 유지·대체·제거 판정을 기록하고 ACC reference, XYZ 외부 연계 EDU, MBR→ACC 계약을 복원했으며 가비지와 소유권 gate를 통과함 |
| architecture-ownership | 완료 | `specs/evidence/20260716_02/architecture-ownership-scan.sanitized.json` | 최신 모듈 구조에서 타 주제영역 Repository·Mapper·DB 직접 접근 금지와 PFW/CMN 소유권 검사를 통과함 |
| password-hashing | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | PFW PBKDF2 버전 hash, legacy verify·rehash와 ADM/BZA 사용 테스트를 통과함 |
| bza-auth | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 로그인·잠금·비밀번호 변경·access token은 구현·테스트 완료, bzaDB 실로그인은 미검증 |
| bza-refresh-rotation | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | refresh token hash 저장, 조건부 폐기, 회전과 동시 재사용 거부 테스트를 통과함 |
| bza-bootstrap | 미검증 | `specs/evidence/20260715_01/mariadb-full-install.sanitized.log` | 명시적 enable·승인·환경변수 기반 구현은 있으나 DB bootstrap 런타임은 미실행 |
| bza-permission-server | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | 메뉴·행위 권한을 서버 필터에서 검사하고 로그인 이력 USER:READ 권한 테스트를 통과함 |
| bza-ui | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | 권한 기반 조회 메뉴와 사용자·역할·메뉴·권한 등록·수정 dialog를 연결했으나 인증 후 실제 browser E2E는 미검증 |
| bza-organization-employee | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 조직·직원 조회 및 감사 사유 필수 등록·수정 API와 테스트는 완료, DB 런타임은 미검증 |
| bza-approval | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 순차·병렬 결재선, 상태 전이, 낙관적 잠금, idempotency와 감사 테스트는 완료, DB E2E는 미검증 |
| bza-audit | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | BZA 업무 변경 before·after·reason 감사 적재와 조회를 구현했으나 실 DB 행은 미검증 |
| adm-framework-console | 부분 구현 | `specs/evidence/20260716_02/adm-runtime-smoke-result.sanitized.json`, `specs/evidence/20260716_02/openapi-runtime-result.sanitized.json`, `specs/evidence/20260716_02/adm-channel-ui-browser-result.sanitized.json` | ADM DB health, 운영 API, 권한 200/403, OpenAPI 160 paths, 인증 후 채널 화면을 실기동 검증함. BZA와 전체 화면 browser E2E는 별도 추적함 |
| adm-permission | 완료 | `specs/evidence/20260716_02/adm-permission-runtime-result.sanitized.json` | 관리자 읽기 200, 조회 역할의 허용 조회 200과 금지 쓰기 403, 권한 변경 감사 사유와 before/after를 실제 ADM API에서 검증함 |
| adm-log-console | 부분 구현 | `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | 거래·상세·감사·배치·운영 로그 API/UI는 제공하나 MariaDB 실데이터 조회는 미검증 |
| remote-log-local | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | allowlist root, 경로 이탈·symlink·확장자·크기 제한, SHA-256, 마스킹 preview와 다운로드 테스트를 통과함 |
| remote-log-multi-instance | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | registry·node client·service credential port, timeout·부분 실패, 라우팅 ID와 checksum ZIP 테스트는 완료했으나 실 mTLS HTTP adapter와 다중 서버 E2E는 미검증 |
| remote-log-bundle-jobs | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 비동기 ZIP 작업 상태, 소유자 격리, 요청 한도, 부분 실패, 만료, 1회성 다운로드 token과 재발급을 구현하고 단위·ADM UI 정적 테스트를 통과함 |
| attachment-storage | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log`, `specs/evidence/20260715_01/attachment-edu-runtime.sanitized.log` | PFW 첨부 저장 port와 로컬 adapter의 경로·symlink·확장자·크기·content type·SHA-256 검증, XYZ EDU 단위 테스트와 저장·재조회 HTTP 런타임을 통과함 |
| bza-operation-support | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | BZA 대시보드·알림·첨부·저장 검색·다운로드 감사·역할 비교·권한 시뮬레이션 API/UI와 테스트는 완료, 인증 후 DB browser E2E는 미검증 |
| standard-execution-id | 완료 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json`, `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | O/S/B 10자리 값 객체·annotation·중복 gate·327개 alias와 V28→V32 실 MariaDB 전환을 검증함 |
| standard-execution-catalog | 부분 구현 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | PFW 공통 pfwJdbcTemplate 소유권과 O/S/B 실행 메타 영속화를 검증하고 Gateway가 OACCQY0001로 ACC를 실제 호출함. 전체 모듈 catalog·ADM 정합성 E2E는 남음 |
| execution-log-propagation | 부분 구현 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json`, `specs/evidence/20260716_02/transaction-meta-runtime-smoke-result.sanitized.json` | 34자리 거래 ID, O 실행 ID와 transactionGlobalId 조회를 확인하고 Gateway→ACC에서 실행 ID·route·Gateway instance 헤더 전파를 실검증함. 다중 인스턴스 timeline은 남음 |
| batch-standard | 완료 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | Spring Batch JobRepository를 PFW DB에 고정하고 BAT 실제 Job 완료·step 조회·restart·rerun과 종료 정리를 실검증함 |
| scheduler-dependency | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 영업일·허용시간·시뮬레이션·선후행·trigger·실행대상 API/UI는 구현, DB 실행 시나리오는 미검증 |
| batch-ghost | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | heartbeat 기반 ghost 후보·조치·운영 로그를 구현, 다중 worker 오탐 검증은 미실행 |
| bat-runtime | 완료 | `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | BAT bootJar를 MariaDB PFW JobRepository로 기동해 온디맨드 접수 202, 완료, step, restart, rerun과 프로세스 정리를 실검증함 |
| domain-generator | 완료 | `specs/evidence/20260716_02/create-domain-result.sanitized.json`, `specs/evidence/20260716_02/remove-domain-smoke.sanitized.json` | 최신 생성기로 임시 PYM 모듈을 순수 생성해 전용 DataSource/MyBatis, PFW BatchRepository, 자동 Job 실행 차단, test·bootJar·bootWar·Java 25를 검증하고 정리함 |
| xyz-edu | 완료 | `specs/evidence/20260716_02/sample-coverage-result.sanitized.json`, `specs/evidence/20260716_02/architecture-inventory.sanitized.json` | XYZ 공식 EDU를 capability-first package로 정리하고 공개 capability 대비 source·test·문서 매핑 gate를 통과함 |
| bat-edu | 완료 | `specs/evidence/20260716_02/sample-coverage-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | tasklet·chunk·retry·restart·idempotency·center-cut·온디맨드 EDU를 유지하고 실제 PFW JobRepository 실행까지 검증함 |
| ai-edu | 완료 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log`, `specs/evidence/20260715_01/ai-edu-runtime.sanitized.json` | PFW provider·embedding·vector port와 XYZ deterministic 구조화 출력·streaming·tool·RAG·fallback·token·사람 승인 테스트 및 표준 헤더를 포함한 HTTP 200 런타임 검증을 완료함. 실 provider는 외부 자격정보 항목으로 미검증 |
| standard-header | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | 호출 서비스/인스턴스 재생성과 S형 ingress 차단 단위 테스트를 통과함. 실제 MBR→ACC HTTP 전파는 미검증 |
| service-call-engine | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | MBR→ACC Remote Facade Proxy와 endpoint/실행 ID 계약 테스트를 추가함. 다중 인스턴스 runtime은 미검증 |
| broker-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | PFW broker port, outbox·inbox·DLQ와 adapter 테스트는 통과, 실 broker는 미검증 |
| broker-real-integration | 미검증 | `없음` | Redis·Kafka·RabbitMQ 서버가 제공되지 않아 실 장애·fallback·replay를 실행하지 않음 |
| file-transfer-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 파일 검증·checksum·임시파일·이동·이력·원격 명령 계획 테스트는 통과함 |
| file-server-real-integration | 미검증 | `없음` | SFTP·FTP·FTPS·SCP·SSH 실 서버가 없어 전송 runtime은 실행하지 않음 |
| mariadb-full-install | 완료 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/mariadb-full-install.sanitized.log` | 실 MariaDB에서 7단계 전체 설치·smoke·seed 재실행과 FK/index/app·migration 권한 38개 검사를 통과함 |
| flyway-static | 완료 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json` | 기존 V28 checksum을 보존하고 V32 증분 migration을 격리 DB에 실제 적용해 327개 alias와 구형 fixture 전환을 검증함 |
| sql-all-install | 완료 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/mariadb-full-install.sanitized.log` | split SQL에서 SOURCE 없는 합본과 V1 baseline을 재생성하고 실 MariaDB 설치·재실행을 검증함 |
| runtime-baseline | 부분 구현 | `specs/evidence/20260716_02/adm-runtime-smoke-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | ADM을 별도 실기동하고 ACC·PFW Gateway·BAT 3개 서비스를 함께 기동해 health·업무 호출·종료를 확인함. MBR·BZA·XYZ 포함 전체 동시 기동은 남음 |
| openapi-runtime | 부분 구현 | `specs/evidence/20260716_02/openapi-runtime-result.sanitized.json` | ADM 최신 실행본의 OpenAPI 3.1, 160 paths, 28 tags와 채널·로그·배치 필수 API를 실검증함. 나머지 실행 모듈의 동일 최신 build 검증은 남음 |
| browser-public-http | 부분 구현 | `specs/evidence/20260716_02/adm-channel-ui-browser-result.sanitized.json`, `specs/evidence/20260716_02/adm-channel-ui-browser.sanitized.png` | 인증된 ADM 화면에서 채널 정책 메뉴를 실제 클릭하고 6개 행 렌더링과 console error 0건을 확인함. BZA 및 전체 ADM 화면은 남음 |
| browser-auth-e2e | 부분 구현 | `specs/evidence/20260716_02/adm-channel-ui-browser-result.sanitized.json`, `specs/evidence/20260716_02/adm-channel-ui-browser.sanitized.png` | ADM 인증 후 채널 정책 메뉴 click·render를 Edge headless에서 검증함. BZA 인증과 ADM 전체 메뉴·반응형 E2E는 남음 |
| multi-instance-runtime | 미검증 | `없음` | 2개 instance registry·failover·lease·worker claim·graceful shutdown 환경을 실행하지 않음 |
| security-static | 완료 | `specs/evidence/20260716_01/quality-gate.sanitized.log` | 평문 secret·보안 seed·민감 헤더 우회와 PFW 보안 정적 gate를 최신 source에서 통과함 |
| bza-session-storage | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | BZA access·refresh token을 sessionStorage로 제한하고 localStorage 사용을 gate에서 차단함 |
| bza-login-history-auth | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 로그인 이력 조회에 Bearer token과 USER:READ 서버 권한을 강제하는 테스트를 통과함 |
| service-call-boundary | 완료 | `specs/evidence/20260716_01/architecture-ownership-scan.sanitized.json`, `specs/evidence/20260716_01/quality-gate.sanitized.log` | MBR→ACC가 CMN Facade Contract와 PFW Service Call Engine을 사용하며 타 주제영역 저장소 직접 참조 gate를 통과함 |
| spring-event-usage | 완료 | `specs/evidence/20260716_01/spring-event-usage-scan.sanitized.json` | 핵심 동기 처리의 금지 Spring Event 사용 0건을 확인함 |
| profile-loading | 완료 | `specs/evidence/20260716_01/profile-loading-result.sanitized.json` | local/dev/stg/prod profile 로딩과 prod secret 기본값 금지 검사를 최신 모듈에서 통과함 |
| deploy-inventory | 완료 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | ACC와 PFW Gateway를 local/dev/stg/prod env·inventory·runtime harness·remote deploy dry-run에 연결하고 local 선택 기동을 실검증함 |
| sql-standard | 완료 | `specs/evidence/20260716_01/mariadb-full-install.sanitized.json`, `specs/evidence/20260716_01/quality-gate.sanitized.log` | prefix·공통 컬럼·COMMENT·FK·index·seed·합본 정적 gate와 실 MariaDB 설치를 통과함 |
| utf8-mojibake | 완료 | `specs/evidence/20260716_01/quality-gate.sanitized.log` | UTF-8, PowerShell BOM/CRLF와 mojibake 검사를 최신 변경 파일 포함 전체 gate에서 통과함 |
| ui-static | 완료 | `specs/evidence/20260716_02/adm-log-policy-ui-static-result.sanitized.json`, `specs/evidence/20260716_02/bza-ui-static-result.sanitized.json`, `specs/evidence/20260716_02/adm-ui-model-consistency.sanitized.json` | ADM/BZA 정적 UI·JavaScript와 ADM HTML/JS model 정합성 gate를 통과함. 실제 인증 후 전 화면 browser E2E는 별도 추적함 |
| sample-coverage | 완료 | `specs/evidence/20260716_02/sample-coverage-result.sanitized.json`, `specs/sample-coverage-matrix.md` | XYZ 외부 연계와 BAT 온디맨드를 포함한 공식 EDU source·test·matrix 정합성 gate를 통과함 |
| generator-cleanup | 완료 | `specs/evidence/20260716_02/create-domain-result.sanitized.json`, `specs/evidence/20260716_02/remove-domain-smoke.sanitized.json`, `specs/evidence/20260716_02/repository-hygiene.sanitized.json` | 순수 생성·빌드 후 remove-domain dry-run/실삭제를 확인하고 임시 source·verification·candidate 산출물 재유입 금지 gate를 통과함 |
| evidence-sanitization | 완료 | `specs/evidence/20260716_02/log-management-standard.sanitized.json`, `specs/evidence/20260716_02/evidence-path-existence-check.sanitized.json`, `specs/evidence/20260716_02/quality-gate.sanitized.log` | 최신 정본 증적을 sanitized 파일로 제한하고 실행 메타데이터·secret 제거·본문 SHA-256 규격을 gate에서 검증함 |
| docx-openxml | 완료 | `specs/evidence/20260716_01/docx-standard.sanitized.json` | 공식 DOCX 9종의 OpenXML package 무결성을 확인함. 이번 구조 변경 freshness와 Word 실제 열기는 최종 정본화 단계로 보류 |
| readme-docs | 부분 구현 | `README.md`, `specs/sample-coverage-matrix.md` | README를 ACC reference·Gateway·O/S/B·S형 공유 API 실제 구현에 맞췄고 샘플 매트릭스를 갱신함. DOCX 9종은 요청에 따라 최종 정본화 단계로 보류 |
| quality-gate | 완료 | `specs/evidence/20260716_02/quality-gate.sanitized.log`, `specs/evidence/20260716_02/full-test.sanitized.log` | 최종 qualityGate 87 tasks와 전체 154 suites, 354 tests가 성공했고 failures 0, errors 0, skipped 4를 정제 증적으로 기록함 |
| request-protection | 완료 | `specs/evidence/20260716_02/cpf-new-request-protection.sanitized.json` | 요청서 SHA-256가 작업 시작 baseline과 일치하며 요청서를 수정하지 않음 |
| report-matrix-consistency | 완료 | `specs/evidence/20260716_02/report-matrix-evidence-consistency.sanitized.json` | 기능 ledger의 report·matrix·GAP·evidence index 상태와 증적 파일 존재·민감정보 검사를 동일 정본으로 검증함 |
| acc-reference-domain | 부분 구현 | `specs/evidence/20260716_02/create-domain-result.sanitized.json`, `specs/evidence/20260716_02/acc-exs-capability-inventory.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | ACC 생성기 reference, 대표 CRUD, SQL/Flyway, 배포 설정과 local/remote Facade를 유지하고 Gateway→ACC reference 조회를 실검증함. external Tomcat/JNDI parity는 미검증 |
| shared-api-boundary | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | S형 ID 일치, 허용 호출 서비스, 호출 인스턴스, 외부 Gateway 우회 차단과 fail-closed 운영 확장 경계를 구현·단위 검증함. mTLS adapter runtime은 미검증 |
| pfw-gateway-runtime | 부분 구현 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | PFW 선택형 Gateway의 route snapshot·권한·채널 정책과 실제 ACC target proxy 200, 실행 ID·route·instance 헤더를 실검증함. streaming·cancellation·다중 인스턴스는 미검증 |
| batch-on-demand | 완료 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json`, `specs/sample-coverage-matrix.md` | 온디맨드 접수·멱등·비동기 worker·상태·step·stop·restart·rerun API/SQL/EDU를 구현하고 MariaDB JobRepository에서 실검증함 |
| channel-registry-policy | 완료 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/adm-runtime-smoke-result.sanitized.json`, `specs/evidence/20260716_02/adm-channel-ui-browser-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | DB-first 채널 master·거래 허용 정책·identity binding·불변 snapshot·export/import를 구현하고 SQL, ADM API/UI, Gateway 실제 호출로 검증함 |
| transaction-test-console | 재확인 필요 | `없음` | O/S/B 테스트 콘솔, 운영 강제 비활성, 권한·감사·결과 포맷 runtime 검증이 남음 |
| policy-package-promotion | 재확인 필요 | `없음` | 환경 독립 정책 export/import, diff, 승인, rollback runtime 검증이 남음 |
| global-change-approval | 재확인 필요 | `없음` | ADM/BZA 전체 mutation 승인·예약 적용·rollback handler 전수 검증이 남음 |
| adm-bam-responsive-statistics | 재확인 필요 | `없음` | ADM/BZA 반응형 화면, 거래·채널 통계와 drill-down browser 검증이 남음 |
| log-raw-format | 재확인 필요 | `없음` | JSON/XML/text/fixed-length 원문·포맷·마스킹·원문 권한·다운로드 감사 browser/DB 검증이 남음 |
| configuration-secret-lifecycle | 재확인 필요 | `없음` | 설정 버전·drift·last-known-good와 secret/certificate/key rotation 외부 adapter 검증이 남음 |
| observability-alert-slo | 재확인 필요 | `없음` | metric·trace·health·SLI/SLO·alert·ack·runbook의 운영 연계 검증이 남음 |
| resource-protection | 재확인 필요 | `없음` | bulkhead·rate limit·quota·backpressure·retry budget·pool 제한 검증이 남음 |
| schema-versioning-migration | 부분 구현 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json` | DB expand/migrate 기반 한 사례는 실검증했으나 REST/event/file schema 호환과 장기 backfill/resume 표준은 재확인 필요 |
| retention-privacy-dr | 재확인 필요 | `없음` | retention·archive·purge·privacy·backup/restore·RPO/RTO·DR 실복구 검증이 남음 |
| supply-chain-performance | 재확인 필요 | `없음` | SBOM·dependency/license/secret scan과 대표 경로 성능·용량 benchmark 재검증이 남음 |
| full-capability-inventory | 부분 구현 | `specs/evidence/20260716_02/architecture-inventory.sanitized.json`, `specs/evidence/20260716_02/acc-exs-capability-inventory.sanitized.json`, `specs/evidence/20260716_02/feature-evidence-result.sanitized.json` | 9개 모듈 1,078개 파일 ownership과 ACC/EXS capability를 inventory화했으나 최종 목표 전체 요구사항의 source 양방향 추적은 계속 필요 |
| module-topology-authoritative | 완료 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/architecture-inventory.sanitized.json` | BZA 정식 업무 백오피스, ACC generator reference, BAT·Gateway 선택 실행, EXS 비runtime 대체 구조를 settings/빌드에서 확인함 |
| standard-execution-contract-migration | 완료 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json`, `specs/evidence/20260716_01/standard-execution-id-migration-apply.sanitized.json` | O/S/B 10자리 단일 기록과 구형 ID alias 조회 호환 migration을 실제 DB에서 검증함 |
| generator-reference-domain-contract | 부분 구현 | `specs/evidence/20260716_02/create-domain-result.sanitized.json`, `specs/evidence/20260716_02/remove-domain-smoke.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | 생성기 순수 PYM 산출물의 test·bootJar·bootWar와 제거를 확인하고 ACC embedded reference 호출을 실검증함. external Tomcat/JNDI E2E는 미검증 |
| batch-dependency-ghost-contract | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 기존 dependency/ghost 구현은 유지되며 이번에는 온디맨드 restart/rerun을 보강함. 다중 worker JobRepository runtime은 미검증 |
| cmn-telegram-contract | 재확인 필요 | `없음` | CMN 전문 layout/parser/formatter와 XYZ charset·byte length·round-trip 최신 runtime 재검증이 남음 |
| ui-design-system-contract | 재확인 필요 | `없음` | ADM/BZA 실제 browser 렌더링·접근성·반응형·history 검증이 남음 |
| evidence-governance-contract | 부분 구현 | `specs/evidence/20260716_02/work-start.sanitized.json`, `specs/evidence/20260716_02/cpf-new-request-protection.sanitized.json`, `specs/evidence/20260716_02/evidence-path-existence-check.sanitized.json`, `specs/evidence/20260716_02/quality-gate.sanitized.log` | 시작 SHA·요청 hash, 정제 evidence 경로와 파일 존재 검사를 유지함. DOCX freshness와 최종 배포 정본화는 요청서 기준 후속 단계로 보류 |
| package-structure-standard | 완료 | `specs/evidence/20260716_02/architecture-inventory.sanitized.json`, `specs/evidence/20260716_02/architecture-ownership-scan.sanitized.json` | ACC·XYZ·BAT를 포함한 9개 모듈의 package/class ownership을 inventory화하고 주제영역 저장소 직접 접근과 package 규칙 gate를 통과함 |
| repository-garbage-cleanup | 완료 | `specs/evidence/20260716_02/repository-hygiene.sanitized.json`, `specs/evidence/20260716_02/deleted-files.sanitized.json`, `specs/evidence/20260716_02/empty-directory-scan.sanitized.json` | patch-candidates·생성 결과·중복 candidate·빈 디렉터리를 정리하고 금지 산출물 재유입 gate를 통과함 |
| readme-product-document | 완료 | `README.md`, `acc/README.md` | README를 작업 일지 없이 제품 가치·아키텍처·모듈·채널·Gateway·거래·오류·신뢰성·배치·운영·개발·EDU·기술 사양 문서로 정리하고 ACC README를 모듈 진입점으로 제한함 |
| acc-generator-output-cleanup | 완료 | `specs/evidence/20260716_02/create-domain-result.sanitized.json`, `specs/evidence/20260716_02/remove-domain-smoke.sanitized.json`, `specs/evidence/20260716_02/repository-hygiene.sanitized.json` | ACC patch-candidates·중복 deploy/SQL·생성 결과를 제거하고 reference source와 generator verification 산출물을 분리함 |
| xyz-edu-package-standard | 완료 | `specs/evidence/20260716_02/architecture-inventory.sanitized.json`, `specs/evidence/20260716_02/sample-coverage-result.sanitized.json` | XYZ EDU를 capability-first package로 이동하고 source·test·mapper 대응과 공식 샘플 coverage를 검증함 |
| bat-job-package-standard | 완료 | `specs/evidence/20260716_02/architecture-inventory.sanitized.json`, `specs/evidence/20260716_02/sample-coverage-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | BAT actual job을 JobDefinition별 vertical slice로, EDU를 학습 유형별 package로 정리하고 온디맨드 JobRepository runtime을 검증함 |
<!-- CPF_LEDGER_END -->


## 0.2 2026-07-16 구조·README·가비지 기준 재판정

최신 master `ff4661e673dab9a2f417e75f8ad64fb712c96fa6` 직접 확인에서 다음이 확인됐다.

- ACC에 account vertical slice와 controller/service/repository/dto 계층 구조가 중복 존재
- `acc/patch-candidates/**`와 `acc/create-domain-result.json` 잔존
- ACC deploy/SQL candidate와 final 산출물 중복
- XYZ EDU에 capability package와 controller/dto/service/repository/mapper/validation/config 계층 package 혼재
- BAT가 JobDefinition별 vertical slice로 정돈되지 않았고 `BatTaskletEducationSample`이 edu root에 존재
- README의 역할을 제품 소개 문서로 제한하는 기준 보강 필요
- 기존 qualityGate가 위 구조·가비지 문제를 탐지하지 못함

이번 기록은 신규 구현 완료 판정이 아니다. 관련 기존 완료 상태는 최신 구조 보정과 evidence 전까지 재확인한다.
