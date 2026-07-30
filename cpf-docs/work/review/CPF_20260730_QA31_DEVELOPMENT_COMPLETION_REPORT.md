# CPF QA31 개발 결과 보고서

## 1. 판정 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 시작 기준 및 실제 확인 HEAD: `9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e` (`20260730_09`)
- 결과 Head: `WORKTREE-OVERLAY` — Commit·Push하지 않음
- QA31 원본: Defect 23, Requirement 99, Scenario 66
- README/Guide 제외: 모든 `README*`, `cpf-docs/guides/**`, `cpf-tools/README.md`, `cpf-docs/assets/readme/**`, `cpf-docs/work/overlay/20260730-readme-guides/**`

이 보고서는 전체 완료 선언이 아니다. 구현된 Source도 Java 25 전체 Gradle, 실제 DB·Redis·Runtime·Browser 검증이 끝나지 않았으면 `완료`로 승격하지 않았다.

## 2. 결과 상태


### 통합 Matrix 926건

- Requirement 708, Scenario 218
- 개발 상태: 완료 652, 미검증 274
- 검증 상태: 완료 82, 미검증 844
- QA31 개발 결과가 통합 Matrix 원본 상태를 임의 승격하지 않도록 Gate로 고정했다.

### QA31 Requirement + Scenario Result Matrix 165건

- 완료: 0
- 부분 구현: 73
- 미구현: 0
- 미검증: 92
- 실패: 0
- 재확인 필요: 0

### Defect Unresolved Register 23건

- 완료: 0
- 부분 구현: 20
- 미검증: 0
- 실패: 0
- 재확인 필요: 3

## 3. 실제 보완 개발

### Gateway

- Control HMAC에 Body SHA-256, Content-Type, Audience, Key ID, 실제 Target을 포함했다.
- Nonce를 JVM Map이 아닌 DB 공유 원장으로 Claim하고, 인증 성공·실패를 별도 보안 감사 원장에 남긴다.
- 실제 Ingress Path와 Target Path Template을 분리하고 traversal·encoded separator를 차단했다.
- ACK가 완료된 Route만 활성화하고 Candidate 적용 실패 시 Last Known Good Snapshot을 유지한다.
- NETWORK/TCP/TLS/APPLICATION/GATEWAY_E2E Probe를 분리했다.
- retry·failover 시도별 Callback과 Ledger 기록 계약을 추가했다.
- Streaming은 Client 전송 완료 후 성공 확정하고, 전송 실패를 성공으로 위장하지 않는다.
- ADM Remote 호출에 connect/response/overall timeout과 typed unknown result를 연결했다.

### Batch

- Map Payload의 `Objects.toString()` 전송을 제거하고 ObjectMapper Canonical JSON으로 직렬화·검증한다.
- FILE_PROCESS를 Ready→Claim→Fingerprint→Processor Consumer→Complete/Fail→Release로 연결했다.
- 고객 확장 SPI만 추가하지 않고 Worker Consumer Registry와 `cpf-reference` CSV Handler/Test를 함께 추가했다.
- File Provider Capability를 실제 지원 기능과 맞추고 미지원 Watch/Restart/Claim을 fail-closed 처리한다.
- Shell 민감 파라미터는 `STDIN_JSON`만 허용하고, Detached Signature·Trust Key·PKIX 검증을 추가했다.
- Attempt 상세 결과에 exit code, stdout/stderr, truncation, duration, artifact hash, unknown 여부를 저장한다.

### ADM/BZA/EDU

- Secret/Path/File Reference를 원문 없이 Metadata Alias로 검색·Paging하는 제품 Adapter를 추가했다.
- Reference Picker의 서버 Paging, 검색 debounce, 선택값 복원, Capability unavailable 처리를 보완했다.
- Service Registry Code를 공용 Catalog로 통합했다.
- Log Export를 JVM Map/임시파일에서 ADM DB 만료 Artifact 원장으로 변경했다.
- ADM 기존 메뉴 구조에 권한 기반 전역검색, 최근 메뉴, 즐겨찾기, 키보드 접근성을 추가했다.
- BZA 8개 기능과 EDU 30개 기능의 Source·Frontend·Test 구조가 동시에 존재하는지 검사하는 Gate를 추가했다.

### DB·Gate

- Oracle·PostgreSQL·MariaDB의 cpfDB/batDB/admDB Migration과 Rollback을 Owner별로 분리했다.
- Canonical Schema와 Vendor Checksum을 동기화했다.
- QA 원본 Hash, 23/99/66 Count, Result/Unresolved ID Coverage, Consumer 연결, Legacy 삭제, DB Drift, Evidence Schema를 fail-closed로 검사한다.
- Final Gate가 QA31 Gate를 호출하도록 연결했다.

## 4. 실제 실행 결과

| 실행 | 환경 | Exit Code | 결과 |
|---|---|---:|---|
| `python cpf-tools/scripts/verify-cpf-qa31-development.py --root <validation-root> --base-sha 9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e --mode full --expected-sha 9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e` | Python 3.13.5, overlay validation root | 0 | 476 checks, failures 0 |
| `python cpf-tools/scripts/verify-cpf-qa31-development.py --self-test` | Python 3.13.5 | 0 | missing/stale/tamper/exclusion negative test PASS |
| EDU/BZA Coverage Gate synthetic positive/negative fixture | Python 3.13.5 | 0 | complete fixture accepted; missing product path rejected, 4 checks |
| `javac --release 21` — Gateway Signer isolated compile | OpenJDK 21 | 0 | Compile PASS |
| Gateway HMAC body tamper Harness | OpenJDK 21 | 0 | `QA31_GATEWAY_HMAC_PASS` |
| `javac --release 21` — Batch JCA/PKIX verifier isolated compile | OpenJDK 21 | 0 | Compile PASS |
| RSA Detached Signature tamper Harness | OpenJDK 21 | 0 | `QA31_JCA_SIGNATURE_PASS` |
| Vue Script TypeScript transpile parse | Node 22 / TypeScript | 0 | App, Reference Picker, Gateway Operations parse PASS |

## 5. 실행하지 않은 검증

다음은 성공 또는 PASS로 기록하지 않았다.

- Java 25 전체 Gradle Build/Test
- Full Repository 기준 EDU/BZA Coverage Gate (현재 실행환경에는 변경되지 않은 전체 Source Snapshot이 없어 제품 Coverage 판정 미실행)
- ADM/BZA `npm ci`, lint, unit test, production build
- Oracle·PostgreSQL·MariaDB Install·Upgrade·Rollback 실제 실행
- Redis, Multi-instance, Gateway 전체 Runtime와 Failure Injection
- Batch Definition→Approval→Publish→Scheduler→Worker 전체 Runtime와 복구
- ADM/BZA Browser E2E, SSE, 권한·승인 실제 브라우저 검증
- 실제 OS 계정 전환, Process Tree Kill, SFTP/FTP, Trust Store 인증서 폐기/교체
- 사용자 Push 이후 exact Head SHA Evidence

## 6. 변경 규모

- Payload 파일 수: 128개
- ZIP 실제 파일 수: 129개 (Payload 128 + Package Manifest 1)
- Source/Build 59, Test 16, Frontend 4, DB/SQL/Canonical 25, Script/Gate 7, Evidence 8, Result/Handover/Manifest 9

## 7. 주요 변경 파일

### Source
- `cpf-reference/build.gradle`
- `cpf-gateway/src/main/java/com/cpf/gateway/config/CpfGatewayConfiguration.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/route/CpfGatewayRouteSnapshot.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/route/CpfGatewayPathRewriter.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/registry/JdbcCpfGatewayRegistryAdapter.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/registry/JdbcCpfGatewayRouteProvider.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/transport/CpfGatewayProxyResponse.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/transport/CpfGatewayHttpExchangePort.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/transport/JdkCpfGatewayHttpExchangeAdapter.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/control/CpfGatewayControlAuthenticationFilter.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/control/CpfGatewayControlSecurityProperties.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/control/JdbcCpfGatewayControlNonceAdapter.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/control/JdbcCpfGatewayControlSecurityAuditAdapter.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/runtime/CpfGatewayHealthWorker.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/runtime/CpfGatewayProbeExecutor.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/runtime/CpfGatewayRouteSynchronizer.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/runtime/CpfGatewayConnectionTestWorker.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/service/CpfGatewayProxyService.java`
- `cpf-gateway/src/main/java/com/cpf/gateway/controller/CpfGatewayPublicController.java`
- `cpf-reference/src/main/java/com/cpf/reference/edu/batch/ReferenceCsvFileProcessHandler.java`
- `cpf-core/src/main/java/com/cpf/core/common/servicecall/ServiceCallAttemptEvent.java`
- `cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceCallExecutorAdapter.java`
- `cpf-core/src/main/java/com/cpf/core/common/servicecall/ServiceCallAttemptObserver.java`
- `cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceCallEngine.java`
- `cpf-core/src/main/java/com/cpf/core/api/servicecall/CpfServiceCallAttemptObserver.java`
- `cpf-core/src/main/java/com/cpf/core/api/servicecall/CpfServiceRegistryCatalog.java`
- `cpf-core/src/main/java/com/cpf/core/api/servicecall/CpfServiceCallExecutor.java`
- `cpf-core/src/main/java/com/cpf/core/api/servicecall/CpfServiceCallAttempt.java`
- `cpf-core/src/main/java/com/cpf/core/api/servicecall/CpfServiceRegistryControlPort.java`
- `cpf-core/src/main/java/com/cpf/core/api/gateway/CpfGatewayControlNoncePort.java`
- `cpf-core/src/main/java/com/cpf/core/api/gateway/CpfGatewayControlSecurityAuditPort.java`
- `cpf-core/src/main/java/com/cpf/core/api/gateway/CpfGatewayControlHeaders.java`
- `cpf-core/src/main/java/com/cpf/core/api/gateway/CpfGatewayControlSigner.java`
- `cpf-core/src/main/java/com/cpf/core/api/gateway/CpfGatewayRegistryPort.java`
- `cpf-core/src/main/java/com/cpf/core/api/gateway/CpfGatewayRoute.java`
- `cpf-admin/src/main/java/com/cpf/admin/AdmApplication.java`
- `cpf-admin/src/main/java/com/cpf/admin/opr/parameter/AdmParameterReferenceProperties.java`
- `cpf-admin/src/main/java/com/cpf/admin/opr/parameter/AdmParameterReferenceCatalogAdapter.java`
- `cpf-admin/src/main/java/com/cpf/admin/opr/gateway/AdmGatewayRegistryClientConfiguration.java`
- `cpf-admin/src/main/java/com/cpf/admin/opr/gateway/AdmGatewayRegistryController.java`
- `cpf-admin/src/main/java/com/cpf/admin/opr/gateway/RemoteCpfGatewayRegistryAdapter.java`
- `cpf-admin/src/main/java/com/cpf/admin/opr/gateway/CpfGatewayRemoteCallException.java`
- `cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmLogExportService.java`
- `cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmLogExportController.java`
- `cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmServiceRegistryController.java`
- `cpf-admin/src/main/java/com/cpf/admin/approval/owner/GatewayApprovalOwnerCommandAdapter.java`
- `cpf-batch/worker/src/main/java/com/cpf/batch/worker/Sha256ScriptArtifactVerifier.java`
- `cpf-batch/worker/src/main/java/com/cpf/batch/worker/ApprovedShellExecutor.java`
- `cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchRuntimeExecutorRegistry.java`
- `cpf-batch/worker/src/main/java/com/cpf/batch/worker/JcaScriptArtifactVerifier.java`
- `cpf-batch/worker/src/main/java/com/cpf/batch/worker/ScriptArtifactVerifier.java`
- `cpf-batch/worker/src/main/java/com/cpf/batch/worker/JobPackDispatcher.java`
- `cpf-batch/worker/src/main/java/com/cpf/batch/worker/ApprovedFileExecutor.java`
- `cpf-batch/worker/src/main/java/com/cpf/batch/worker/WorkerOperationalProperties.java`
- `cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchFileProcessHandlerRegistry.java`
- `cpf-batch/worker/src/main/java/com/cpf/batch/worker/internal/JdbcWorkerExecutionRepository.java`
- `cpf-batch/control-server/src/main/java/com/cpf/batch/control/job/BatchJobDefinitionService.java`
- `cpf-batch/contract/src/main/java/com/cpf/batch/spi/FileProcessHandler.java`
- `cpf-batch/contract/src/main/java/com/cpf/batch/api/BatchJobDefinition.java`

### Test
- `cpf-gateway/src/test/java/com/cpf/gateway/route/CpfGatewayPathRewriterTest.java`
- `cpf-gateway/src/test/java/com/cpf/gateway/transport/CpfGatewayProxyResponseTest.java`
- `cpf-gateway/src/test/java/com/cpf/gateway/transport/CpfGatewayHttpExchangePortTest.java`
- `cpf-gateway/src/test/java/com/cpf/gateway/runtime/CpfGatewayProbeExecutorTest.java`
- `cpf-reference/src/test/java/com/cpf/reference/edu/batch/ReferenceCsvFileProcessHandlerTest.java`
- `cpf-core/src/test/java/com/cpf/core/api/servicecall/CpfServiceRegistryCatalogTest.java`
- `cpf-core/src/test/java/com/cpf/core/api/gateway/CpfGatewayControlSignerTest.java`
- `cpf-admin/src/test/java/com/cpf/admin/opr/parameter/AdmParameterReferenceCatalogAdapterTest.java`
- `cpf-batch/worker/src/test/java/com/cpf/batch/worker/ScriptArtifactVerificationModeTest.java`
- `cpf-batch/worker/src/test/java/com/cpf/batch/worker/JcaScriptArtifactVerifierTest.java`
- `cpf-batch/worker/src/test/java/com/cpf/batch/worker/BatchRuntimeExecutorRegistryCanonicalJsonTest.java`
- `cpf-batch/worker/src/test/java/com/cpf/batch/worker/ApprovedFileExecutorCapabilityTest.java`
- `cpf-batch/contract/src/test/java/com/cpf/batch/api/BatchJobDefinitionFileProcessTest.java`

### Frontend
- `cpf-admin/frontend/src/App.vue`
- `cpf-admin/frontend/src/styles/adm.css`
- `cpf-admin/frontend/src/components/parameters/ReferenceCatalogSelect.vue`
- `cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue`

### DB·SQL
- `cpf-tools/db/canonical/platform-schema.json`
- `cpf-tools/db/vendor/mariadb/rollback/R81__qa31_gateway_target_batch_attempt_detail.sql`
- `cpf-tools/db/vendor/mariadb/runtime/bat/repository/worker-attempt-finish.sql`
- `cpf-tools/db/vendor/mariadb/migration/flyway/V81__qa31_gateway_target_batch_attempt_detail.sql`
- `cpf-tools/db/vendor/mariadb/migration/flyway/checksums.sha256`
- `cpf-tools/db/vendor/postgresql/runtime/bat/repository/worker-attempt-finish.sql`
- `cpf-tools/db/vendor/postgresql/migration/rollback/admDB/R81__qa31_durable_log_export.sql`
- `cpf-tools/db/vendor/postgresql/migration/rollback/cpfDB/R81__qa31_gateway_target_nonce.sql`
- `cpf-tools/db/vendor/postgresql/migration/rollback/batDB/R81__qa31_batch_attempt_detail.sql`
- `cpf-tools/db/vendor/postgresql/migration/flyway/admDB/V81__qa31_durable_log_export.sql`
- `cpf-tools/db/vendor/postgresql/migration/flyway/admDB/checksums.sha256`
- `cpf-tools/db/vendor/postgresql/migration/flyway/cpfDB/V81__qa31_gateway_target_nonce.sql`
- `cpf-tools/db/vendor/postgresql/migration/flyway/cpfDB/checksums.sha256`
- `cpf-tools/db/vendor/postgresql/migration/flyway/batDB/V81__qa31_batch_attempt_detail.sql`
- `cpf-tools/db/vendor/postgresql/migration/flyway/batDB/checksums.sha256`
- `cpf-tools/db/vendor/oracle/runtime/bat/repository/worker-attempt-finish.sql`
- `cpf-tools/db/vendor/oracle/migration/rollback/admDB/R81__qa31_durable_log_export.sql`
- `cpf-tools/db/vendor/oracle/migration/rollback/cpfDB/R81__qa31_gateway_target_nonce.sql`
- `cpf-tools/db/vendor/oracle/migration/rollback/batDB/R81__qa31_batch_attempt_detail.sql`
- `cpf-tools/db/vendor/oracle/migration/flyway/admDB/V81__qa31_durable_log_export.sql`
- `cpf-tools/db/vendor/oracle/migration/flyway/admDB/checksums.sha256`
- `cpf-tools/db/vendor/oracle/migration/flyway/cpfDB/V81__qa31_gateway_target_nonce.sql`
- `cpf-tools/db/vendor/oracle/migration/flyway/cpfDB/checksums.sha256`
- `cpf-tools/db/vendor/oracle/migration/flyway/batDB/V81__qa31_batch_attempt_detail.sql`
- `cpf-tools/db/vendor/oracle/migration/flyway/batDB/checksums.sha256`

### 결과·인수인계
- `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- `cpf-docs/work/current/CPF_20260730_QA31_CODEX_REVIEW_READY.md`
- `cpf-docs/work/handover/CPF_20260730_QA31_DEVELOPMENT_HANDOVER.md`
- `cpf-docs/work/review/CPF_20260730_QA31_DEVELOPMENT_COMPLETION_REPORT.md`

### Gate·운영 Script
- `cpf-tools/scripts/package-cpf-qa31-development-result.ps1`
- `cpf-tools/scripts/verify-cpf-qa31-development-result.ps1`
- `cpf-tools/scripts/verify-cpf-qa31-development.py`
- `cpf-tools/scripts/apply-cpf-qa31-development-result.ps1`
- `cpf-tools/scripts/verify-cpf-bza-qa31-coverage.py`
- `cpf-tools/scripts/verify-cpf-reference-qa31-coverage.py`
- `cpf-tools/scripts/verify-cpf-final-completion.ps1`

## 8. 적용 후 필수 재판정

사용자 Repository 적용 후 최신 `origin/master`가 `9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e`와 다르면 적용을 중단하고 Diff를 재검토해야 한다. 적용·Push 후 새 exact SHA에서 Full Gate, Java 25, Frontend, DB Lifecycle, Runtime, Browser 검증을 실행하고 그 Evidence에서만 상태를 승격한다.
