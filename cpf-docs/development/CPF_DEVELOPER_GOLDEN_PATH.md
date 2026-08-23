# CPF 개발 Golden Path

> 목적: 일반 업무 개발자가 Internal Runtime을 외우지 않고 **호출 종류 → Starter → 표준 API → 검증 단계**만 선택해 개발할 수 있게 합니다.

## 1. 먼저 이것만 구분합니다

| 구분 | 판단 기준 | Golden Path |
|---|---|---|
| Application 내부 호출 | 같은 Application 안의 Service | `service.method(...)` |
| CPF Domain 호출 | IP/WAS가 달라도 CPF가 소유·관리하는 Domain Contract | `CpfDomainClient.execute(request)` |
| 외부 연계 호출 | CPF Domain 계약 밖의 기관/시스템 | `@CpfClient` 기반 typed client + `@CpfTimeLimiter` / `@CpfRetry` |

네트워크 위치가 아니라 **Ownership과 Contract**로 판단합니다. `cpf-member(MBR) → cpf-external(EXS)`가 별도 WAS여도 둘 다 CPF Domain이면 CPF Domain 호출입니다. EXS가 은행/외부기관으로 나갈 때가 외부 연계 호출입니다.

## 2. LEVEL 1 — 업무 개발자가 먼저 쓰는 기능

전체 API를 외울 필요가 없습니다. 처음에는 `CPF_PUBLIC_FUNCTION_TOP_100.md`의 TOP 20만 봅니다.

- Web/Service/Repository: `@CpfController` → `@CpfService` → `@CpfRepository`
- Local DB 거래: `@CpfTx`
- CPF Domain: `CpfDomainClient.execute(...)`
- 외부 연계: `@CpfClient`, `@CpfTimeLimiter`, `@CpfRetry`
- Context: `CpfContexts.transactionId()` 등
- 공통 기능: `CpfCodeService`, `CpfMessageSource`, `CpfParameterService`, `CpfCalendarService`
- 신뢰성/운영: `@CpfIdempotent`, `@CpfLogging`, `@CpfPermission`, `@CpfApprovalRequired`, `@CpfAudit`
- Messaging/Batch: `@CpfMessageListener`, `@CpfBatchJob`, `@CpfBatchStep`

Router/Executor/SPI는 LEVEL 3 Advanced로 보고 일반 업무 예제 첫 화면에 놓지 않습니다.

## 3. CPF REQUIRED / NATIVE ALLOWED / DIRECT USE FORBIDDEN

### CPF REQUIRED

다음은 CPF가 거래·장애·보안·감사를 함께 관리해야 하므로 CPF 표준 경계를 사용합니다.

- Transaction
- CPF Domain Call
- External Integration
- CPF Context
- Business/Operation Logging
- Audit
- Permission / Approval
- Idempotency
- Reliable Messaging
- UNKNOWN / Reconcile

### NATIVE ALLOWED

다음은 CPF에 종속시킬 이유가 없는 일반 Java/Spring 사용입니다.

- Java Collection
- String/Date utility
- 단순 Converter
- 단순 Bean wiring
- 기술 독립 순수 함수

### DIRECT USE FORBIDDEN

일반 업무/Generated Source에서 다음 방식으로 CPF 관리 경계를 우회하지 않습니다.

- `WebClient.builder()` 직접 조립
- `new RestTemplate(...)`
- `RedisTemplate` 직접 사용
- `KafkaTemplate` 직접 사용
- 자체 transactionId/traceId `ThreadLocal`
- Side Effect Remote Call에 자체 Retry loop
- 자체 메모리 Map으로 Idempotency 구현
- Internal Starter 직접 dependency

Provider/Adapter 구현부에서 필요한 Native API는 Owner module 내부에서 사용할 수 있습니다. 이것이 **Native escape hatch**이며 일반 업무 Golden Path와 구분합니다.

## 4. Transaction과 Remote Call

`transactionId`/`traceId`는 Domain 경계를 넘어 이어질 수 있지만 DB Transaction은 WAS별 Local Transaction입니다. 외부기관 호출과 Local DB Transaction을 하나의 물리 Transaction으로 간주하지 않습니다.

Remote Side Effect를 호출한 뒤 응답 확정 여부를 모르면 `UNKNOWN`을 `FAILED`로 덮거나 blind retry하지 않습니다. Idempotency key, probe/reconcile, recoveryId, fencing 등 해당 Capability의 표준 계약으로 확정합니다.

## 5. Generator를 안전하게 쓰는 순서

```text
cpf domain create/setup
      ↓
gradle.properties Developer Contract
      ↓
 setup --preview
      ↓
     sync/diff
      ↓
deterministic generate/regenerate
      ↓
compile / test
```

Canonical Generator는 `create`, `setup --preview`, `sync`, `diff`, `regenerate`, `upgrade`, `remove`, `restore` lifecycle을 검증합니다. Generated-owned 파일의 사용자 수정이 있으면 fail-closed하고, unmanaged/custom Source를 임의 삭제하지 않습니다. `cpf-domain.yaml`, `cpf-generator.lock.json` 또는 이름을 바꾼 Generator bookkeeping은 생성 결과에 저장하지 않습니다.

## 6. 검증은 3단계

| 단계 | 명령 | 언제 |
|---|---|---|
| FAST | `./gradlew cpfVerifyFast` | 일상 개발 중 정적/계약 빠른 확인 |
| TARGETED | `./gradlew cpfVerifyTargeted -PcpfTargetCapabilities=cache,messaging` | 변경한 Capability와 횡단 영향 확인 |
| FULL LOCAL | `./gradlew cpfVerifyFullLocal` | Java25 + Live Infrastructure + Browser + 장애/복구 + Evidence 최종 확인 |

`cpfVerifyTargeted`는 FullLocal 항목을 삭제하거나 약화하는 기능이 아닙니다. 개발 중 피드백 시간을 줄이고 최종 QA에서는 같은 FullLocal 강도를 유지합니다.

## 7. 기존 시스템은 단계적으로 도입할 수 있습니다

1. Context + Logging
2. Validation + Transaction
3. Code / Message / Parameter / Calendar
4. Domain Call / External Integration
5. Cache / Messaging / Security
6. Generator / ADM / MBW Backoffice / Full Platform

CPF 계약을 fork하지 않고 기존 표준을 연결할 때는 현재 SPI/Extension을 먼저 사용합니다.

| 기존 표준 연결 목적 | 현재 Extension |
|---|---|
| Context/Header/Identity/Tenant 주입 | `CpfContextRuntimeProvider` |
| 메시지/오류 메시지 해석 | `CpfMessageResolver` |
| 응답코드 해석 | `CpfResponseCodeResolver` |
| 감사 Sink | `CpfAuditSink` |

새 Extension을 만들기 전에 이 SPI로 해결되는지 확인합니다. 업체별 구현은 `cpf-core`에 넣지 않습니다.

## 8. 도입 규모 선택

새 Starter를 더 만드는 대신 기존 Public Profile/Capability를 조합합니다.

- **Minimal 성격**: Web + Context + Validation + Logging + Transaction
- **Standard 성격**: Minimal + Security + Domain Call + Common + Cache + Observability
- **Full Platform 성격**: Standard + Messaging + Batch + ADM/MBW Backoffice + Gateway + Advanced Recovery

실제 선택 가능한 Starter는 `CPF_STARTER_QUICK_SELECT.md`가 정본 Catalog와 함께 검증합니다.

## 9. Upgrade 전 영향 확인

`cpf-tools/verification/tools/report-cpf-upgrade-impact.py`는 현재 Public Starter/API, Config, Generator, DB migration, OpenAPI surface의 fingerprint를 만들고 이전 snapshot과 비교할 수 있게 합니다. Breaking 후보가 있으면 Major 검토 대상으로 보고, Deprecated API는 즉시 삭제하지 않습니다.

## 10. Database Migration 안전 실행

Platform DB Migration은 `cpf-tools/db/tools/invoke-platform-database-migration.ps1`만 사용합니다. 먼저 대상 Vendor/Profile/Module/Version으로 dry-run을 생성하고 결과의 `planSha256`과 operation별 migration/rollback hash를 검토합니다.

```powershell
pwsh -NoProfile -File cpf-tools/db/tools/invoke-platform-database-migration.ps1 `
  -ProfilePath <profile.json> -Direction upgrade -MigrationVersion <version> `
  -Modules <module> -ResultPath <dry-run-result.json> -DryRun
```

실제 적용은 애플리케이션 정지, rollback 준비, 검토한 plan hash, 운영 감사정보를 모두 명시해야 합니다. Verifier가 소유한 disposable DB가 아닌 실제 환경은 선택된 모든 물리 DB를 포함하는 `BackupManifestPath`도 필수입니다.

```powershell
pwsh -NoProfile -File cpf-tools/db/tools/invoke-platform-database-migration.ps1 `
  -ProfilePath <profile.json> -Direction upgrade -MigrationVersion <version> `
  -Modules <module> -Apply -ConfirmApply -ConfirmApplicationsStopped `
  -ConfirmRollbackReady -ExpectedPlanSha256 <reviewed-sha256> `
  -BackupManifestPath <backup-manifest.json> `
  -Operator <operator> -Reason <reason> -ApprovalReference <approval> `
  -ResultPath <apply-result.json>
```

Plan hash가 달라지면 다시 dry-run부터 검토합니다. Client/DDL/Network 실패 후 `reconcileRequired=true`이면 성공이나 자동 rollback을 추정하지 말고 결과의 `failureOperation`을 기준으로 실제 DB 상태를 확인한 뒤 명시적으로 복구합니다. Secret은 Profile의 환경변수 참조로만 전달하고 명령행·결과·로그에 값을 기록하지 않습니다.

## 11. 더 찾을 때

1. `CPF_STARTER_QUICK_SELECT.md` — 어떤 Starter를 고를지
2. `CPF_PUBLIC_FUNCTION_TOP_100.md` — 어떤 Public 기능/Annotation이 있는지
3. 기존 `DEVELOPER_GUIDE.md` / `GENERATOR_GUIDE.md` — 상세 사용법
4. Advanced Adapter/SPI — 일반 Golden Path로 해결되지 않을 때만

## 관리 API와 업무 거래 API를 구분하는 기준

- ADM/MBW Backoffice/Gateway 자체 Controller와 Batch Control Plane 관리 Controller는 업무 Domain Online Transaction이 아니므로 `@CpfOnlineTransaction`과 거래 Header 6개를 붙이지 않습니다.
- 관리 기능은 Spring Web/Security/Validation/OpenAPI와 해당 Owner의 CPF Public API를 사용합니다. `cpf-core` internal package나 업무 Domain internal package를 직접 참조하지 않습니다.
- 관리 화면에서 MBR/EXS 같은 실제 업무 Operation을 호출하는 경우에는 Controller가 아니라 **Domain Client outbound 경계**부터 거래 Context를 적용합니다. 개발자가 Header 6개를 직접 만들지 않습니다.
- Generated Domain은 `online/` 필수, `modules.batch=true`일 때 `batch/` 선택 구조입니다. 업무 예제는 EDU `online` 20개와 `batch` 15개를 Canonical로 사용합니다.
