# CPF 최종 목표 요구사항 정본

> Canonical path: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
> Revision date: `2026-08-12`
> Previous runtime/evidence basis SHA: `c4068ae7dcc3afe4a9516e5e0f743bd6b2ef9e07` (`11_02`; historical evidence only)
> Currentization static-source basis SHA: `d14a6e4fab73533871cc4a5f833c9b7be56cc4a6` (`12_03 docs-currentized master`; 12_04 integrated redevelopment requirement basis, not runtime PASS)
> Canonical Requirement Count: **186개**
> Legacy Alias: **8개** — 완료율 중복 집계 금지

### Core Kernel / Root Layout 강제 정책

Core는 기능 수를 줄이기 위한 빈 껍데기가 아니라 CPF 전체의 **헌법/Kernel**이다.
전역 Error/Result/Outcome, Transaction/Execution Context, transactionId/lineage,
UNKNOWN/Reconcile/Idempotency, 최소 Identity/Security/Tenant Context와 장기 안정적인 공통 Value/Contract를 보존한다.
특정 Owner/Optional Capability의 API/SPI/DTO/Operations는 Core에 두지 않는다.

Repository 물리 구조는 Root를 임의 확장하지 않는다.
Base/Foundation의 canonical owner는 `cpf-starters/base`이며 `cpf-core` 외 Capability `*/core`는 금지한다. 기존 Pure Foundation Source는 `cpf-starters/base` 또는 실제 Capability Owner로 이관한다. 공식 범용 Test Support는 `cpf-tools/testing/cpf-testkit`에 둔다. 이번 공개 Starter 명칭 전환은 Release/Tag가 없는 현재 Repository에서 one-shot으로 수행하며 구 Public Coordinate를 장기 병존시키지 않는다.
새 Root 파일/Directory는 사용자 명시 승인과 Canonical Root Allowlist 변경 없이는 금지한다.

Core Closure Gate는 unknown class의 자동 KEEP를 금지하고,
Owner-specific/Optional API·SPI residue, Runtime/Operations residue, invalid logging operations,
old/new duplicate, stale reference, moved-source residue, empty migrated directory가 하나라도 남으면 FAIL한다.


## 0.1 Current Target Architecture Freeze — 2026-08-11

본 절의 currentization static-source basis는 `51611d5799379fac9c23c93a27d2190aff26a629` (`12_02`)이며 최신 사용자 Architecture Steering과 12_01→12_02 독립 Source Audit을 반영한 최상위 Current Target이다. `11_02 / c4068ae7dcc3afe4a9516e5e0f743bd6b2ef9e07` 이후 대규모 Source 변경이 발생했으므로 과거 Runtime/Evidence PASS를 현재 SHA에 승계하지 않는다. 실제 execution source는 각 작업 시작 시 최신 `origin/master` exact SHA를 다시 확인한다. 본 문서의 다른 과거 문구가 본 절과 충돌하면 본 절을 우선하고 같은 Current 문서 안의 구 전환 문구도 즉시 현행화한다.

### 0.1B 12_02 Independent Source Audit / Revalidation Freeze

`12_01 / bd7bbfccc720e8703f3073eafb32705f97ef168b`은 additions 90,029 / deletions 54,804, 약 2,900 files 규모의 대형 IA·Education·Batch 변경이고,
`12_02 / 51611d5799379fac9c23c93a27d2190aff26a629`는 그 후 17-file blocker correction이다. 따라서 `12_02`의 소규모 보정만으로 기존 전체 Requirement를 완료 처리하지 않는다.

현재 exact Source에서 확인된 강제 재개방 항목:

- Root `build.gradle`의 missing convention apply-from.
- Batch retired `host-agent`/`runtime-common` tracked residue.
- `cpf-tools`/`deploy` target IA 미완결.
- Generator와 `cpf-member`/`cpf-external`의 DAO Golden Path 및 legacy transaction sample.
- EDU의 deterministic test-double 기반 generic Integration Harness와 actual provider runtime 미증명.
- ADM 80 requirement 대비 64 source route, 정확히 16 route 미구현.
- `11_02 / c4068ae7dcc3afe4a9516e5e0f743bd6b2ef9e07` 기반 Evidence/Status의 successor SHA 재검증 필요.
- Starter/Runtime Catalog는 고정 과거 SHA를 정본으로 사용하지 않고 실행 시점 `RUNTIME_GIT_HEAD` 또는 successor exact SHA로 검증하며, Catalog/Source/Generator/BOM parity를 유지한다.

위 항목은 현재 Source에 대한 정적 판정이며 Runtime 성공을 뜻하지 않는다. 개발 완료 후 successor exact SHA에서 Build/Test/DB3/Browser/Provider Runtime/Fault Injection Evidence를 재생성해야 한다.

### 0.1A Stateless Generated Project / CLI Invariant

- Generated Customer Project에는 `cpf-domain.yaml`, lock, ownership/manifest 같은 Generator lifecycle metadata를 **영구 저장하지 않는다**.
- Fresh/Regenerate/Upgrade/Restore의 입력은 Framework `cpf-tools/generator/definitions/<domain>/cpf-domain.yaml` 또는 명시적인 외부 `--file`이다.
- Generated Customer Project에는 `online/`을 필수로 두고, Domain 정의의 `modules.batch=true`일 때 `batch/`를 선택적으로 생성한다. Batch Runtime 구현과 실행 계약의 Owner는 `cpf-batch`이며 Generated Domain은 Public `cpf-starter-batch`를 소비한다.
- `README.md`, `verification/`, `db/canonical/`, `db/vendors/`, Vendor별 SQL 3벌과 Generator metadata directory를 Customer Project에 만들지 않는다.
- lifecycle은 expected generated seed를 입력/Template에서 결정적으로 계산해 `diff/regenerate/remove`를 fail-closed로 수행하고, 사용자 변경을 자동 덮어쓰지 않는다.
- Repository-local CLI는 `cpf-tools/runtime/cli/cpf` / `cpf-tools/runtime/cli/cpf.bat`이며 Root `bin/`을 새로 만들지 않는다. 설치 시 논리 명령은 `cpf ...`다.

- `10_06` 기준 독립 `cpf-common` Product Root는 제거되어 있다. CPF Common Product Services의 Canonical Owner는 `cpf-starters/common` + `cpf-starter-common`이며 구 Root 재등장을 금지한다.
- `cpf-starter`는 Base + Common의 기본 개발 진입점이다. `base`와 `common`은 내부 Owner는 분리한다.
- `cpf-starter-common` Full Runtime은 Data JDBC 기반과 `cpfDB`를 필수로 사용하며 silent memory fallback을 금지한다.
- CPF 관계형 Platform State는 `CPF_PLATFORM_DB(cpfDB)`로 통합한다. `cmnDB`, `admDB`, `batDB`를 별도 Target Physical DB로 유지하지 않는다. BZA와 Customer Business DB는 분리한다.
- Ownership은 단일 Schema 안에서도 `CMN_*`, `ADM_*`, `BAT_*`, `GW_*`, `SEC_*`, `OPS_*` Prefix와 Canonical Metadata/Migration Owner로 보존한다.
- Generated Domain별 `mbrDB/accDB/...` 생성은 금지하고 Customer Business DB + Domain Table Prefix를 사용한다.
- Generated Customer Business Domain의 물리 Project Root는 `cpf-<domain>/` naming을 사용한다. CPF 개발 Repository의 공식 회귀 Generated Root는 `cpf-member/`(MBR)와 `cpf-external/`(EXS) 두 개이며 **둘 다 동일 Canonical Generator로 실제 생성·유지하고 최종 결과물에 포함**한다. `cpf-` Prefix는 Generated Project naming convention일 뿐 CPF Product Module/Public BOM/Publication 등록을 의미하지 않는다. 모든 다른 Domain도 동일 설정 Schema로 생성 가능해야 하며 member/MBR/external/EXS 특수 하드코딩을 금지한다.
- `cpf-core` 외 Capability `*/core` Module을 금지한다. `10_06` 기준 `cpf-starters/foundation/**` 물리 Root는 이미 제거되어 있으므로 `base`/실제 Owner 상태를 유지하고 `foundation` physical owner 재도입을 금지한다.
- Web Base/Context/Error mapping은 Profile이 아니라 `cpf-starters/web` Capability가 소유한다.
- Education Canonical Module은 `cpf-education`, Java package는 `com.cpf.education`, Application은 `EducationApplication`, System Code는 `EDU`로 고정한다. 다른 Education Root/Package/Application 명칭은 Active Surface에 존재할 수 없으며 재도입을 금지한다.
- Top-level Profile은 Deployable당 exactly-one이다. Public Profile Artifact는 `cpf-starter-profile-*` 대신 `cpf-starter-web-api`, `cpf-starter-secure-api`, `cpf-starter-bff`, `cpf-starter-event`, `cpf-starter-batch`를 사용한다.
- Redis와 Valkey는 공식 Provider로 병존한다. Developer-facing 명시 선택명은 `cpf-starter-cache-redis` / `cpf-starter-cache-valkey`이며 old coordinate를 active surface로 재도입하지 않는다.
- Core Error는 `CpfException`, `CpfBusinessException`, `CpfValidationException`, `CpfSystemException` 최소 taxonomy를 제공한다. `CpfRuntimeException`은 추가하지 않는다. 업무/기관 Error/Message는 DB Catalog로 동적 등록하고 Core Enum은 Framework reserved fallback semantics만 보유한다.
- Error throw path는 arbitrary response code로 messageCode를 합성하지 않는다. `errorCode/reference + arguments`를 전달하고 Common Catalog가 DB `responseCode → messageCode → locale message`를 해석한다.


## 0.2 Three-Vendor DB Development Freeze — Canonical One Source / Generated Vendor Packs

Oracle, PostgreSQL, MariaDB 3개를 **동등한 공식 지원 Vendor**로 유지한다. MySQL, MSSQL, H2는 공식 Vendor/Evidence 대상에 추가하지 않는다. MariaDB 지원을 MySQL 지원으로 표현하지 않는다.

3개 Vendor 지원은 개발자가 동일 DDL/DML/Migration을 3벌 수작업 유지하는 방식으로 운영하지 않는다. 사람이 유지하는 정본은 Vendor-neutral Canonical Model 하나이며 Vendor SQL Pack은 가능한 범위에서 Generator가 생성한다.

```text
Canonical DB Model / Migration Intent
  ├─ schema object + logical owner + prefix + constraints/index
  ├─ datatype semantics
  ├─ seed model
  ├─ non-table object semantics
  └─ immutable migration intent
                 ↓
        CPF DB Vendor Renderer
        ┌────────┼─────────┐
        ↓        ↓         ↓
      Oracle  PostgreSQL  MariaDB
        │        │         │
 generated pack + explicit vendor override only when unavoidable
```

강제 원칙:

1. `cpf-tools/db/canonical/**`가 Schema/Seed/Object 의미론의 단일 정본이다. Vendor SQL을 먼저 수정하고 Canonical을 뒤따라 맞추는 방식을 금지한다.
2. `cpf-tools/db/vendor/<vendor>/**`는 Canonical에서 재현 가능해야 한다. 수동 Vendor Override는 Canonical로 표현할 수 없는 Vendor 고유 차이에만 허용하고 object ID, owner, reason, affected vendor, test/evidence를 Manifest에 기록한다.
3. 업무/Common/Admin/Batch/Gateway Source에 `if oracle`, `if postgres`, `if mariadb` 형태의 Vendor 분기를 분산시키지 않는다. Pagination/Upsert/Generated Key/Lock/Time/JSON/LOB/Identifier 등 실제 차이는 Data Capability의 제한된 Dialect/Strategy 경계가 소유한다.
4. 새 DB 변경은 `Canonical Change → 3 Vendor Render → Static Parity → Runtime Matrix` 순서로 처리한다. 개발자의 정상 Inner Loop는 Canonical 검증 + Reference Vendor 빠른 Gate를 사용하되, DB 영향 변경의 최종 개발 완료와 QA 통과는 Oracle/PostgreSQL/MariaDB 3개 모두의 검증 없이 선언하지 않는다.
5. Release Baseline으로 채택된 과거 Migration은 Generator 개선을 이유로 다시 생성/변조하지 않는다. 신규 변경은 새 Migration으로 누적한다. 현재 pre-GA 구조개편에서 기존 Migration을 일회성 currentize할 경우 Current Cut-over 완료 후 immutable baseline으로 고정한다.
6. Vendor Pack은 Fresh Install, Seed, Upgrade, Rollback 또는 Forward-Recovery, Reapply/Idempotency, Runtime Query, Checksum/Drift를 동일 Scenario ID로 검증한다.
7. Local/Docker/Oracle 환경 부재는 Source/Renderer/Test Harness 작성을 중단하는 사유가 아니다. 실제 Runtime을 실행하지 못한 Vendor는 `미검증`으로 기록하며 PASS로 간주하지 않는다.


## 0.3 Current-State Reconciliation / Drift Prevention Freeze

currentization source 기준 CPF Product Module로서 `cpf-common`, `cpf-member`, `cpf-external`, `cpf-starters/foundation`은 Target Product Surface가 아니다. 단 Generated Customer Project naming은 `cpf-<domain>/`을 사용하므로 물리 Root `cpf-member/`, `cpf-external/`은 **Generated Customer Domain 역할로만** 허용한다. 현재 CPF Repository 공식 회귀 Generated Root는 이 두 개뿐이다.

따라서 Current 문서는 **과거 전환 절차를 현재 해야 할 일처럼 반복해서 남기지 않는다.**

강제 규칙:

1. Canonical 문서, Work Request, Developer Instruction, Generator Contract, Starter Catalog, settings, README/EDU는 매 기준 SHA에서 실제 Source Surface와 대조한다.
2. 이미 제거된 경로는 `MIGRATE`가 아니라 `REINTRODUCTION_FORBIDDEN` Invariant로 관리한다.
3. `cpf-member`/`cpf-external`을 CPF Product Module/Public Artifact로 등록하지 않는다. 동일한 물리 이름은 Generated Customer Project Root로만 허용하며 Canonical Output naming은 `cpf-<domain>/`이다. 현재 회귀 Root `cpf-member/`와 `cpf-external/`은 실제 Generator Output으로 유지한다. 제3 임의 Domain은 Repository Root에 만들지 않고 `build/domain-generator/verification/<scenario>/`에서 genericity를 추가 검증한다.
4. `cpf-starter-common`은 Public Entry이다. Generator Contract의 forbidden dependency rule이 `project(':starters:common')`을 legacy `cpf-common`과 함께 금지하면 Contract 오류다.
5. `com.cpf.core.api.context`는 Canonical Core Public Context API이다. Generated-domain forbidden import 목록이 이를 `OLD_*`로 금지하면 Contract 오류다.
6. `settings.gradle`/Catalog가 제거된 `foundation` ownerGroup을 active tolerance로 허용하면 fail한다. 과거 removed path 목록/negative assertion은 허용한다.
7. Legacy token은 Supersession/removed/forbidden/history Context에서만 허용한다. Current owner/path/생성 Target처럼 사용하면 stale-current-state defect다.
8. Source/Contract/Gate와 Canonical 문서는 최종적으로 일치해야 한다. 통합 개발·자체검수 역할을 맡은 GPT는 사용자 지시에 따라 Source 변경과 동시에 Canonical 개발요건·Architecture·Developer 문서를 현행화한다. 단, 역할별 상태 원장의 QA/Codex 역사·판정 영역을 임의로 조작하거나 미검증 결과를 완료로 변경하지 않는다.


## Generated Domain Public Starter 경계

Generated Customer Domain이 직접 의존할 수 있는 Artifact는 **최신 Canonical Starter Catalog에서 `visibility=public`인 Public Profile/Provider/Entry만**이다.

`currentization_source_sha`의 Catalog 기준 대표 직접 참조 가능 Artifact:

```text
cpf-starter-secure-api
cpf-starter-batch
cpf-starter-data-mybatis
```

반면 다음은 해당 Catalog에서 Internal-only leaf이므로 Generated Domain 직접 dependency로 생성하지 않는다.

```text
cpf-starter-integration-http
cpf-starter-integration-resilience
```

HTTP/Resilience 기능은 Public Profile/Composition 내부에서 Internal leaf를 소비하도록 구성한다. 필요한 Capability가 현재 Public Surface에 노출되어 있지 않다면 Generated Domain이 Internal 경계를 뚫는 방식으로 우회하지 않는다. **Starter Framework 측 Public Composition/Profile/Provider Gap을 먼저 구현·Catalog/BOM/Publication/Test와 함께 닫은 뒤 Generated Domain이 그 Public Surface를 소비**해야 한다.

Generator Template/Generated build.gradle의 Internal Artifact 직접 참조는 Gate 실패다.

## 1. 문서 목적과 정본성

이 문서는 **Core Platform Framework(CPF)**의 최상위 제품 목표, 장기 Architecture, Module Ownership, Public Contract, 운영·보안·배포 품질, 최종 완료 판정과 Requirement Catalog를 정의하는 최우선 정본이다.

이 문서는 작업 일지, 현재 진행률, 특정 QA 회차의 완료 보고 또는 날짜별 Evidence 저장소가 아니다. 구현 상태는 Current Request, Gap/Result Matrix, Review, Handover와 Evidence가 관리하지만, 모든 요구 도출·구현·검수·완료 판정은 이 문서에 종속된다.

하위 문서나 구현이 이 문서와 충돌하면 다음 순서로 처리한다.

1. 실제 최신 Git 구현과 실행 결과를 확인한다.
2. 이 문서의 제품 목표와 Architecture 원칙에 맞는 Owner를 결정한다.
3. 하위 문서·Source·SQL·API·Test·Generator·Guide·Evidence를 함께 이관한다.
4. 잘못된 Legacy와 중복 정본을 제거한다.
5. Requirement Continuity Ledger에 ID 이동·분해·통합 근거를 남긴다.

## 2. 규범 용어와 완료 상태

- **MUST / 필수**: GA 완료에 반드시 충족해야 한다.
- **MUST NOT / 금지**: 존재하면 Release 또는 완료 판정을 차단한다.
- **SHOULD / 권고**: 미적용 시 ADR과 대체 통제를 요구한다.
- **MAY / 선택**: 제품 정책에 따라 활성화할 수 있으나 선택하지 않은 Runtime을 강제 의존시키면 안 된다.

허용 상태는 다음뿐이다.

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

`개발 완료`와 `검증 완료`는 서로 다른 축이다. Source가 존재해도 실제 Consumer, 오류·복구, 다중 인스턴스, 보안·운영, 최신 exact-SHA Evidence가 없으면 전체 완료가 아니다.

## 3. 제품 정의와 최종 결과

CPF는 금융권을 포함한 다양한 업무 시스템을 구축·운영·감사·확장·검증·배포·상용화할 수 있는 **Business Platform 품질의 상용 Framework**다. 단순 공통 Library, Sample, 예제 모음, 특정 프로젝트의 Base Code가 아니다.

최종 제품은 다음을 하나의 일관된 제품 구조로 제공해야 한다.

- MSA와 Modular Monolith
- 동일 JVM Local Call과 분리 WAS Remote Call
- Embedded JAR, External WAS WAR, 독립 Static Web Artifact와 독립 Worker Process
- Multi-instance, 부분 실패, retry, failover, restart, reconciliation과 DR
- 금융권 수준의 인증, 권한, 승인, 감사, masking, 개인정보와 credential 통제
- idempotency, async, outbox/inbox, DLQ, compensation, unknown-result recovery
- Gateway, 외부 REST/전문/파일, Kafka messaging
- Spring Batch, Scheduler, Agent, Runner, Worker와 Center-Cut
- ADM/BZA 운영 조회·제어·승인·감사·통계·incident
- 표준 Generator와 신규 업무 Domain lifecycle
- OpenAPI, JavaDoc, Test Kit, EDU와 실제 Reference Runtime
- install, migration, upgrade, rollback/forward recovery, backup/restore, deploy와 artifact trust
- Source, SQL, API, Test, Config, Frontend, Script, Guide와 Evidence의 양방향 일치

단기 구현 편의보다 장기 제품 구조, 확장성, 운영성, 보안성, 복구 가능성, 재현성과 상용 배포 가능성을 우선한다.

## 4. 지원 Topology와 동등성

공식 지원 Topology:

- Embedded Boot JAR
- External WAS WAR
- Modular Monolith
- 독립 Microservice
- 동일 JVM Local Facade
- 분리 WAS Remote Facade
- ADM/BZA 독립 Static Artifact + Web Server
- Gateway 독립 Runtime
- Agent/Runner/Worker 독립 Process
- Multi-instance와 Multi-zone
- Rolling, Canary, Blue-Green
- Backup/Restore와 DR Failover/Failback

Topology가 달라도 다음 계약은 동일해야 한다.

- 업무 요청·응답 DTO와 validation
- Standard/Extension Header
- transactionId, trace, segment와 attempt
- authentication/authorization와 service identity
- timeout budget, idempotency와 error taxonomy
- audit, masking와 observability
- version/compatibility와 failure/recovery semantics

Local 구현이 Remote보다 기능이 적거나, Remote 전환을 위해 업무 Source를 다시 작성하거나, 내부 호출이 필수적으로 Gateway를 경유하면 Architecture 실패다.

## 5. 공식 Module·식별·Ownership

| 역할 | Module | Java Root Package | SystemCode | 필수 Owner 책임 |
|---|---|---|---:|---|
| 기술 공통 Framework | `cpf-core` | `com.cpf.core` | CPF | CPF 전역 Kernel: topology-independent Contract/Semantics/Value와 최소 순수 Logic. 특정 Owner/Optional Capability API·SPI와 Runtime 구현은 소유하지 않음 |
| CPF Common Product Capability | `cpf-starters/common` (`cpf-starter-common`) | `com.cpf.common` | CMN | CPF가 소유·버전관리하며 고객 업무가 직접 사용하는 Code/Parameter/Message/Calendar/Template. 고객 특화 공통은 `<customer>-common` |
| 플랫폼 관리자 | `cpf-admin` | `com.cpf.admin` | ADM | 플랫폼 운영 Control Plane, 플랫폼 위험조치 승인과 운영자 감사 |
| 고객 업무 관리자 | `cpf-biz-admin` | `com.cpf.bizadmin` | BZA | 고객 업무 관리, 조직·업무 결재, 선택형 Customization Sample |
| Batch 실행 기반 | `cpf-batch` | `com.cpf.batch` | BAT | Spring Batch, Scheduler, Center-Cut, Agent, Runner, Worker |
| Gateway Runtime | `cpf-gateway` | `com.cpf.gateway` | GWY | 외부 진입, trust boundary, route/load balance/resilience와 attempt ledger |
| Generated Customer Domain Verification — Member | `cpf-member` | `member` | MBR | Root-level 실제 Generator Output. `online/`을 필수 생성하고 `modules.batch=true` 회귀로 `batch/`도 선택 생성한다. MBR_* Sample Transaction과 Public `cpf-starter-batch` 소비를 검증한다. CPF Product/Public Artifact 아님 |
| Generated Customer Domain Verification — External | `cpf-external` | `external` | EXS | Root-level 실제 Generator Output. `online/`을 필수 생성하고 `modules.batch=false` 회귀로 online-only 조합을 검증한다. EXS_* Sample Transaction과 불필요한 batch 미생성을 검증한다. CPF Product/Public Artifact 아님 |
| Transient Generated Genericity Verification | `build/domain-generator/verification/<scenario>` | `<generated package>` | `<scenario systemCode>` | 제3 임의 Domain genericity 검증용 Git 비추적 Output. member/external과 동일 Engine/Template으로 생성 후 cleanup |
| 교육 | `cpf-education` | `com.cpf.education` | EDU | 제품 Public API의 실제 EDU·복구·운영 예제 |

`cpf-external`을 CPF Product Module/Public Artifact로 두지 않는다. 다만 Root `cpf-external/`(System Code EXS)은 **공식 Generated Customer Domain 회귀 인스턴스**로 유지하며, 기관별 외부연계 Adapter 역시 같은 Domain-neutral Generator/Metadata와 고객 확장 Owner를 통해 생성·확장한다.

### 5.1 의존성 방향

```text
Generated/Business Domain → Public Starter/Common Capability → cpf-core
cpf-gateway → cpf-core Public Contract + 선택 Starter
cpf-batch → cpf-core Public Contract + Business Public Contract
cpf-admin → Operations Command/Query Contract
cpf-biz-admin → Business Public Contract
Customer Adapter/Plugin → Capability Public SPI / `<customer>-common`; Core SPI는 genuine Kernel extension에 한정
```

금지:

- `cpf-core`의 Common/Admin/Batch/업무 역방향 의존
- 선택 기능 Runtime(Kafka, Redis, OTel exporter 등)의 Core 강제 포함
- 업무 Domain 간 DB 직접 접근
- ADM/BZA의 Owner DB 직접 갱신
- 내부 호출의 Gateway 재경유
- 순환 의존과 Internal Package 직접 참조
- 실제 Product Consumer 없는 Interface/Adapter/Starter
- OSS와 Legacy의 Dual Primary
- Sample 또는 Generated Reference를 제품 원장으로 간주

### 5.2 Public API / SPI / Internal

```text
com.cpf.<owner>.api
com.cpf.<owner>.spi
com.cpf.<owner>.internal
```

- Public API는 semantic version과 compatibility 대상이다.
- SPI는 capability, lifecycle, failure, thread-safety와 version contract를 문서화한다.
- Internal은 외부 Module에서 compile되지 않도록 module metadata, package rule, ArchUnit와 publication gate로 차단한다.
- Public API에 선택 OSS 구현 type을 직접 노출하지 않는다.
- Public API/SPI와 중요 복구·동시성·보안 로직에는 한글 JavaDoc/주석을 제공한다.


### 5.3 Lightweight Core·Starter·Capability Profile

`cpf-starters/`는 CPF의 정식 Root 제품 영역이다. `cpf-core`는 Spring Boot 선택 Runtime을 직접 조립하는 범용 실행 모듈이 아니라, CPF 전역 Kernel로서 topology-independent Contract/Semantics/Value·표준 식별자·오류·문맥·최소 순수 Logic만 제공하는 초경량 Artifact여야 한다. Provider-neutral이라는 이유만으로 Core 소유가 정당화되지 않으며, 특정 Owner 또는 Optional Capability에만 필요한 API/SPI/DTO/Port도 해당 Owner/Capability가 소유한다.

선택 기술은 다음 계층으로 제공한다.

1. **Leaf Starter**: 하나의 기술 Capability와 AutoConfiguration을 소유한다.
2. **Generator Capability Profile**: 사용 사례를 승인된 Leaf Starter 목록으로 해석하고 Domain Manifest에 `resolvedStarters`와 버전을 고정한다.
3. **Aggregate Starter**: 안정성이 입증된 조합에 한해 전이 Dependency만 제공하며 고유 Bean·AutoConfiguration을 소유하지 않는다.
4. **Platform BOM**: 버전만 정렬하며 Capability 선택을 대신하지 않는다.

대표 Starter 하나가 의존 Starter를 자동 포함하는 것은 Gradle 전이 Dependency로 가능하다. 다만 기존 Domain의 묵시적 변경을 막기 위해 Generator Profile이 해석된 Leaf 목록과 Profile Version을 Manifest에 고정하는 방식을 우선한다.

다음을 금지한다.

- `all`, `full`, `everything` 형태의 Mega Starter
- Starter 선택만으로 업무·Admin·Batch·Gateway 고유 정책이 유입되는 구조
- 상호 배타 Provider의 무승인 동시 활성화
- 선택하지 않은 Starter의 JAR·Bean·SQL·Config·Secret 요구
- Core와 Starter에 동일 Primary AutoConfiguration·Adapter가 동시에 남는 구조
- Consumer 없는 Starter를 GA 완료로 처리하는 행위



### 5.2 제품 제공 영역과 Education/EDU 경계

CPF가 제품으로 제공하는 Runtime/Application 자체와 CPF 도입 개발자가 직접 개발해야 하는 영역을 구분한다.

- `cpf-admin`의 ADM은 플랫폼 운영 Control Plane **제품**이다. CPF 도입 개발자가 ADM 자체를 다시 개발하는 교육 대상이 아니다.
- `cpf-biz-admin`의 BZA도 고객 업무 관리 제품/확장 Surface이며, 제품 본체의 내부 기능을 EDU에 복제하지 않는다.
- `cpf-education`은 CPF 도입 개발자가 실제로 사용해야 하는 **Public API, Public SPI, 공식 Extension Point, Integration Contract, Generator 산출물 사용법**을 실행 가능한 예제로 교육하는 영역이다.
- ADM/BZA/Gateway/Batch 내부 구현을 이름만 바꾼 Generic Handler/JDBC 예제로 EDU에 중복 구현하지 않는다.
- ADM/BZA 제품 기능의 완전성은 해당 Product Source/API/Frontend/SQL/Test/Runtime/Manual에서 검증한다.
- ADM/BZA와 관련된 EDU는 외부 Consumer가 실제로 구현·호출하는 공식 Public Extension/Integration 시나리오일 때만 유지한다.
- EDU 수량은 그 자체가 목표가 아니다. Canonical EDU Catalog의 수량은 Public Consumer 교육 필요성과 Architecture Ownership에 의해 결정한다.
- 기존 EDU ID를 축소·통합·재분류해야 할 경우 QA가 Source/Consumer/Generator/Manual/Test 영향도를 검토하고 정본 Requirement를 먼저 갱신한다. 개발GPT가 QA 원장을 임의 삭제하거나 완료 처리하지 않는다.

## 6. 모든 Requirement에 적용되는 공통 완료 축

각 Requirement는 적용 가능한 항목을 모두 충족해야 한다. `N/A`는 이유와 검수 승인이 있어야 한다.

| 완료 축 | 필수 기준 |
|---|---|
| Ownership | 단일 Owner Module, Public API/SPI/Internal 경계, 역방향·순환 의존 없음 |
| Consumer | 실제 Product Consumer, Bean/Route/SQL/Frontend/Script 연결, Dead abstraction 없음 |
| 정상 기능 | 대표 정상 흐름과 실제 Runtime 결과 |
| 오류·경계 | invalid input, 권한, timeout, conflict, empty, oversize, dependency failure |
| 동시성 | race, optimistic/distributed lock, idempotency, duplicate와 multi-thread |
| Multi-instance | lease, fencing, rebalance, failover, stale writer와 shared state |
| 결과 불명 | side effect 전후·DB commit 전후·ACK/response loss 분류와 reconciliation |
| 복구 | retry, restart, reprocess, compensation, rollback/forward recovery와 manual recovery |
| Security | authN/authZ, trust boundary, secret/PII masking, negative corpus와 secure default |
| Audit/Operations | 조회, status, control, reason, approval, immutable audit, metric/alert/runbook |
| Resource | memory/disk/thread/connection/queue/time budget, bounded streaming, cleanup |
| Data/DB | schema/query owner, 3 Vendor 또는 DB-less 근거, migration/rollback/drift |
| Compatibility | Local/Remote, mixed version, API/message/file/DB/config compatibility |
| Test | unit, contract, integration, runtime/browser/broker/fault 중 적용 항목 |
| Documentation | OpenAPI, JavaDoc, developer/operation/install/recovery guide |
| Evidence | exact Source SHA, command, environment, time, exit code, report/artifact hash, sanitization |
| Hygiene | Legacy/Dead Code/Stale Evidence/임시 산출물/Secret 제거와 회귀 방지 |

## 7. 기술 정본과 OSS Primary 정책

기술 Stack의 exact version은 `gradle/cpf-stack.properties`, Wrapper, BOM과 Lockfile을 단일 정본으로 한다. 이 Revision의 목표 baseline은 Java 25 LTS, Gradle 9.x, Spring Boot 4.1 계열, Spring Cloud 2025.1 계열, Spring Batch 6 계열이며, 공식 지원 Matrix 밖 조합은 `TRANSITION`으로 관리하고 GA를 차단한다.

승인된 Primary 방향:

| 영역 | Primary 방향 | 제품 경계 |
|---|---|---|
| Gateway | Spring Cloud Gateway Server Web MVC | CPF는 route/trust/audit/ledger 정책 확장 |
| Batch | Spring Batch | Job/Step/Repository/ExecutionContext/Restart 정본 |
| Scheduler | db-scheduler 기본, 고급 Adapter 선택 | Trigger 소유권과 Batch 실행 경계 명확화 |
| Messaging | Kafka | in-memory는 unit/local test Adapter만 |
| Resilience | Spring Cloud CircuitBreaker + Resilience4j | operation 정책·timeout budget 정본 |
| Observability | Micrometer Observation + OpenTelemetry | SDK/exporter는 Starter가 소유 |
| Cache | Caffeine local + 선택형 distributed provider | `cpf-starters/common`/Core에 선택 Runtime 강제 금지 |
| Feature Flag | OpenFeature + CPF Provider | evaluation/audit/secure override |
| Session/BFF | Spring Security + Spring Session JDBC | Browser credential 저장 금지 |
| Frontend | Vue 3, Router, Pinia, TanStack Query/Table, Zod, Orval, Element Plus | 실제 Consumer 이관과 lock/generated drift 검증 |
| Migration | Flyway OSS Core | 기존 자체 migration Primary 제거 |
| Supply Chain | CycloneDX, ORT, Syft, Grype | 동일 final artifact와 exact SHA 검사 |

Dependency나 파일만 추가하고 실제 Consumer가 Legacy를 사용하면 전환 완료가 아니다.

## 8. 거래·신뢰·오류 표준

- 거래 실행 인스턴스 ID는 `transactionId` 하나다.
- 기본 생성 규격은 `yyyyMMddHHmmssSSS(17)+SystemCode(3)+instanceToken(7)+sequence(7)`의 34자리다.
- `instanceId`는 WAS Runtime 인스턴스 식별자이며 `CPF_RUNTIME_INSTANCE_ID` 명시값을 우선 사용하고, 미지정 시 WAS가 실행되는 PC/서버/Container의 Runtime hostname을 기동 시 1회 확정해 사용한다. Domain명, `local`, `*-local-01` 같은 임의 fallback을 Framework 기본값으로 사용하지 않는다.
- 34자리 transactionId의 `instanceToken(7)`은 위 `instanceId`에서 결정적으로 파생하는 포맷용 token이며 별도 운영 식별자나 개발자 설정값이 아니다.
- **정식 거래 기동 Channel 또는 최초 기동 System은 CPF 규격의 transactionId를 최초 1회 생성할 수 있다.**
- 이후 Local/Remote/REST/SOAP/Gateway/Message/Async/Retry/Batch/File/UNKNOWN/Reconcile 등 동일 거래의 모든 참여 구간은 **동일 transactionId를 End-to-End로 승계·보존**하며, System hop이나 재시도마다 새 transactionId를 만들지 않는다.
- 하위 호출·병렬 호출·재시도는 `segmentId`, `parentSegmentId`, `attempt`, `traceId`, `spanId` 등 세부 실행 식별자로 구분한다.
- 정식 거래 기동 Channel/System이 생성한 transactionId와 비신뢰 Client가 임의 주입·변조·재사용한 transactionId를 구분한다.
- transactionId의 신뢰 여부를 Header 존재나 형식 적합성만으로 판단하지 않고 인증된 Channel/System identity, 호출 경로와 trust policy를 함께 검증한다.
- Client가 보낸 내부 Header, principal, environment, instance ID를 무조건 신뢰하지 않는다.
- 오류는 code, message, field/offset, retryability, failure stage, unknown-result 여부와 operator guidance를 가져야 한다.
- pre-execution failure, side-effect confirmed failure, success, stopped, retryable failure와 unknown result를 구분한다.
- 결과 불명은 자동 성공이나 무조건 재시도로 닫지 않는다.


### 8.1 거래 추적·파일로그·DB로그 표준

프레임워크의 거래·대외연계·Batch·Scheduler·Center-Cut·Gateway·비동기 실행은 장애 분석과 운영 추적을 위해 동일한 식별 체계를 사용한다.

#### 거래 계보

- 최상위 거래의 `transactionId`는 호출 체인 전체에서 유지한다.
- 거래가 다른 거래, Remote Service, Gateway, Message, File, Batch 또는 비동기 작업을 호출해도 원 `transactionId`를 잃지 않는다.
- 하위 호출은 `segmentId`, `parentSegmentId`, `attempt`, `traceId`, `spanId`로 계층과 재시도를 구분한다.
- Batch 전환 시 `jobId/jobInstanceId/jobExecutionId/stepExecutionId/partitionId/itemId/agentId/workerId`와 원 `transactionId/requestId`를 연결한다.
- 외부 연계는 destination/service/operation/requestId/attempt/timeout/result/error를 거래 계보에 연결한다.
- 정식 거래 기동 Channel/System이 생성하거나 신뢰된 내부 호출 체인에서 승계된 transactionId는 End-to-End로 유지한다.
- 비신뢰 Client의 내부 transaction/instance/security context 주입·변조·replay는 trust boundary에서 차단하거나 정책에 따라 별도 신규 거래로 격리하며, 이 경우 외부 correlation 정보는 내부 transactionId와 분리해 보존할 수 있다.

#### 표준 로그 필드

로그 종류에 따라 적용 가능한 범위에서 최소 다음을 구조화한다.

`timestamp`, `level`, `systemCode`, `environment`, `instanceId`, `transactionId`, `traceId`, `spanId`,
`segmentId`, `parentSegmentId`, `attempt`, `requestId/idempotencyKey`, `actor/tenant/channel`,
`jobId/jobInstanceId/jobExecutionId/stepExecutionId/partitionId/itemId/agentId/workerId`,
`operation/endpoint/remoteSystem`, `result/status`, `errorCode`, `failureStage`, `retryable`,
`unknownResult`, `elapsedMs`, `message/file identifiers`.

민감 Payload, Credential, Token, Session, Private Key, 주민번호/계좌 등 PII는 원문 기록하지 않고 표준 masking/redaction 정책을 적용한다.

#### 파일 로그

- 제품 표준 경로·파일명·encoding·event format·rotation·compression·retention·권한을 정의한다.
- 다중 인스턴스에서 파일 충돌이나 교차 기록이 없도록 system/date/instance 식별이 가능해야 한다.
- 비동기 File Writer는 bounded queue, backpressure/fallback, shutdown drain, disk-full/write-failure, process-kill, terminal-loss 탐지와 alert를 제공한다.
- 로그 저장 실패가 원 업무 Transaction을 불필요하게 Rollback시키지 않되, 법적/보안 감사처럼 fail-closed가 필요한 로그는 정책을 구분한다.
- local spool/replay를 사용하는 경우 순서, 중복 제거, checksum, retry, poison record/quarantine와 유실 탐지를 제공한다.

#### DB 거래·운영 로그

- transaction/segment/attempt/batch execution/remote call 상태를 조회할 수 있는 Canonical Schema와 Index를 제공한다.
- `transactionId` 단일 조건으로 대량 데이터에서도 효율적으로 전체 Timeline을 조회할 수 있어야 한다.
- append/duplicate/idempotency, retention/partition/archive/purge, DB 장애와 재전송, 부분 기록을 검증한다.
- Audit DB Log는 append-only/tamper-evident 요구를 별도로 만족한다.
- File Log와 DB Timeline이 동일 거래를 가리키되 민감 Payload를 중복 저장하지 않는다.

#### ADM 통합 거래 조회

ADM은 운영자가 **transactionId 하나로** 해당 거래의 전체 호출 계보를 조회할 수 있어야 한다.

최소 조회 범위:

- 최초 요청과 종료 결과
- Local/Remote 하위 Transaction Segment
- 외부 REST/전문/File/Gateway 호출과 attempt
- Message producer/consumer, retry, DLQ
- Batch/Center-Cut/Scheduler로 이어진 job/execution/step/partition/worker
- instance/was/agent/server identity
- 오류 code/failure stage/UNKNOWN/reconcile 결과
- 관련 File Log/Remote Log/Trace/Audit의 안전한 연결
- 시간순 Timeline, 계층 Tree, 검색/Paging/Detail
- 데이터 누락·지연·수집 불가 시 명시적 partial/stale 경고

원문 민감 로그 조회·다운로드는 별도 권한, 사유, 승인, masking, 감사와 만료 정책을 적용한다.

## 9. 데이터·SQL·Migration 정본

공식 지원 DB Vendor는 MariaDB, PostgreSQL, Oracle 3종이다. MySQL/MSSQL은 지원 선택값에서 제거한다.

권장 Schema:

```text
CPF_PLATFORM_DB (`cpfDB`) — CPF_*/CMN_*/ADM_*/BAT_*/GW_*/SEC_*/OPS_*
bzaDB — BZA_*
Customer Business DB/Schema — MBR_*/ACC_*/PRD_* 등 Domain Prefix
`refDB`는 Production 기본 DB가 아닌 Reference/Test Fixture
```

- 모든 DB Artifact는 `cpf-tools/db/vendor/<vendor>` Owner 경계에서 동일 구조로 관리한다.
- Canonical Schema/Metadata에서 Vendor-native install/seed/migration/rollback/runtime query를 생성·동기화한다.
- 특정 Vendor SQL의 복사·치환만으로 완료 처리하지 않는다.
- Index/FK가 없는 Column을 참조하면 DB 실행 전 생성 Gate에서 실패해야 한다.
- Flyway 적용 Migration은 불변이며 신규 변경은 새 Version으로 제공한다.
- Empty Install과 Upgrade 최종상태는 schema manifest로 동등해야 한다.
- 기존 Schema가 다르면 조용히 skip하지 않고 drift 또는 migration 문제로 실패한다.
- destructive rollback은 데이터 보존/backup/승인/대체 recovery가 명시되어야 한다.
- 업무/관리 SQL은 Java literal이 아닌 Owner Query ID와 Vendor Resource로 관리한다.
- DB 변경은 Generator domain-template, Generated Domain, checksum, install/upgrade/rollback까지 한 작업 단위로 검토한다.


### 9.1 Generator-first Fresh Database Lifecycle

모든 DB 변경은 다음 순서로 수행한다.

```text
Requirement/Data Model
→ Canonical Schema·Metadata·Runtime Query Contract
→ Generator·Golden Template
→ Oracle/PostgreSQL/MariaDB Vendor Source
→ Install·Migration·Rollback·Runtime Pack
→ Java Consumer·Test
→ Fresh Runtime Evidence
```

Vendor SQL이나 Historical Migration을 먼저 수동 수정해 정본을 역전시키면 안 된다.

Codex·QA의 DB 검증은 기존 사용자 DB를 재사용하지 않고, 각 Vendor별 전용 QA Database/Schema가 CPF Object 0건인 초기 상태임을 확인한 뒤 시작한다. 공식 Reset/Provision 경로가 없으면 수동 SQL로 우회하지 말고 그 경로를 Source Defect로 구현한다.

각 Vendor는 단독으로 다음 Lifecycle을 통과해야 한다.

- Fresh Provision·Install·Mandatory Metadata/Seed
- Generator로 만든 임의 Domain Bootstrap
- Upgrade·Runtime Query·Schema Drift
- Rollback·Reapply·Idempotent Reapply
- Different-hash Conflict·Partial Failure·Restart
- Optional Pack On/Off
- Cleanup 후 CPF Object 0건 또는 승인된 보존 상태
- exact-SHA Evidence


## 10. File·Attachment·Archive·전문

- create/extract/upload/download/transfer 전 경로를 bounded streaming으로 처리한다.
- 대용량 payload를 `byte[]`, `readAllBytes`, 전체 DOM/문자열로 적재하지 않는다.
- 임시 파일→fsync/checksum→atomic publish를 사용하고 실패 시 partial target을 제거한다.
- path alias, canonical path, symlink/hardlink/device/FIFO, zip slip, duplicate canonical entry, 압축률·entry/total budget을 통제한다.
- client cancellation, timeout, disk full, process kill과 restart cleanup을 검증한다.
- 고정길이 전문은 byte length·encoding·padding·group·version·field offset·masking·streaming을 지원한다.
- 기관별 Layout/Endpoint/Auth는 고객 Adapter가 소유한다.

## 11. Gateway·외부연계·Event

Gateway:

- Control Plane과 Data Plane을 분리한다.
- Route snapshot은 atomic refresh하고 stale/invalid snapshot을 fail-closed한다.
- trusted header allowlist와 proxy chain을 적용하고 내부 Header spoof를 차단한다.
- SSRF 방지를 위해 scheme/host/port/CIDR/service allowlist, URI canonicalization, redirect와 DNS 정책을 적용한다.
- one-shot/streaming body는 안전한 replay 조건이 없으면 retry하지 않는다.
- 실제 async/stream 종료와 client disconnect 시점에 ledger를 닫는다.
- connect/send/response/read failure를 분류한다.
- audit/ledger 저장 실패가 원 업무를 불필요하게 오염시키지 않도록 transaction 경계를 분리한다.

Kafka/Event:

- stable message ID, schema version, key/partition/order, TTL, size/depth, producer/environment binding을 제공한다.
- at-least-once + idempotent consumer를 기본으로 한다.
- ACK/transaction/consumer commit과 업무 side effect 경계를 명시한다.
- retry topic, DLT, poison isolation, replay approval와 audit를 제공한다.
- 다중 Manager/Worker에서 reply/correlation이 인스턴스 로컬 queue에 의존하지 않게 한다.
- rebalance, broker outage, duplicate, late reply, process kill과 response loss를 검증한다.


### 11.1 Messaging Provider·JMS·MQ·TCP 지원

CPF Event 계약은 특정 Broker Client에 종속되지 않는 Envelope·Idempotency·Ordering·Retry·DLQ·Unknown-result 계약을 `cpf-core` Public API/SPI로 제공한다. 실제 Provider Runtime은 Starter가 소유한다.

공식 구현 대상은 다음과 같다.

- Kafka: `cpf-starter-messaging-kafka`
- JMS 3.x 공통 Runtime: `cpf-starter-messaging-jms`
- IBM MQ Provider: `cpf-starter-messaging-ibm-mq` — JMS Starter를 기반으로 TLS, Queue Manager, Channel, CCDT/Endpoint, Connection Recovery와 운영 상태를 제공한다.
- RabbitMQ/AMQP Provider: `cpf-starter-messaging-rabbitmq`
- 영속 연결형 TCP 전문: `cpf-starter-integration-tcp`

`JMS`는 API/Runtime 추상화이고 `IBM MQ`는 Provider이므로 하나로 뭉개지 않는다. RabbitMQ는 AMQP Provider로 별도 Lifecycle을 가진다. 각 Provider는 같은 CPF Envelope와 오류 분류를 사용하되 ACK·Transaction·Ordering·Redelivery 의미 차이를 숨기지 않는다.

TCP Starter는 연결 수명주기, framing, encoding, heartbeat, reconnect, backoff, half-open 탐지, bounded queue, backpressure, request-response correlation, 전송 후 응답 유실, duplicate/reconciliation, TLS와 credential rotation을 제공해야 한다.

사용자 입력에서 확인된 `TPC` 표기는 별도 요구를 버리지 않기 위한 검색 Alias로 보존하고, 후속 확인 전까지 `EXS-TCP`에 연결한다.


## 12. Batch·Scheduler·Center-Cut·Agent

- Spring Batch가 Primary Engine이며 자체 Job/Step/Execution Repository를 중복 구현하지 않는다.
- CPF 승인·idempotency·fencing·unknown-result 원장과 Spring Batch JobInstance/JobExecution/StepExecution ID를 연결한다.
- idempotency key 재사용 시 canonical request hash와 scope가 다르면 conflict로 거부한다.
- fencing은 실행 행에 저장된 token이 아니라 최신 owner/lease epoch를 검증한다.
- reserve→start→bind 사이 response loss와 고아 상태를 reconciliation한다.
- STOPPED/RETRYABLE_FAILURE/FAILED/UNKNOWN_RESULT가 Spring Batch 상태와 운영 UI에서 정확히 일치해야 한다.
- ExecutionContext에 Secret, 전체 stdout/stderr, 대용량 payload를 저장하지 않는다.
- Remote Partition/Chunk/Step은 Kafka transport와 stable correlation, DLT, backpressure를 사용한다.
- Product Profile에서 Remote topology가 in-memory channel로 조용히 fallback하면 안 된다.
- Scheduler trigger claim과 Job start 사이를 outbox/state machine으로 복구 가능하게 한다.
- Center-Cut은 immutable parameter, item claim/lease/fencing, global rate, failed-only reprocess와 unknown reconciliation을 제공한다.
- Agent는 승인 Script/Artifact만 실행하고 process tree, output budget, timeout, drain, takeover, artifact trust를 제공한다.

## 13. ADM·BZA·Frontend·BFF

ADM은 플랫폼 운영 Control Plane이며 Owner DB를 직접 수정하지 않는다. 위험조치는 Owner Command API로 수행한다.

BZA는 고객 업무 관리와 업무 결재를 소유하며 플랫폼 Runtime을 직접 제어하지 않는다.

공통 Frontend 기준:

- ADM/BZA 독립 Vue 3 + TypeScript + Vite Application/Artifact
- feature folder, route registry, Pinia state, TanStack Query API boundary
- Orval exact-SHA generated client와 drift gate
- package.json/package-lock exact 일치와 clean `npm ci`
- Element Plus/TanStack Table/Zod를 실제 화면 Consumer에 적용
- raw `fetch`는 단일 승인 mutator/auth bootstrap 경계 외 금지
- search/paging/sort/detail/status/loading/empty/error/retry UX
- deep link, 403/404, session expiry, browser history
- responsive, keyboard, accessibility와 Chromium/Firefox/WebKit E2E
- 외부 Runtime CDN/font/script 의존 금지

BFF/Session 기준:

- Browser Local/Session Storage, URL, DOM, response body, console/log에 Access Token, Refresh Token, Session ID를 노출하지 않는다.
- 인증 응답 형태가 Map/DTO/record 중 무엇이든 credential stripping은 fail-closed한다.
- JDBC Session의 credential 저장은 최소화·암호화/참조화하고 DB dump/운영화면에 원문을 노출하지 않는다.
- Session fixation 보호, rotation, timeout, concurrency, 권한회수, 강제 logout을 제공한다.
- Spring Security 표준 CSRF와 route inventory 기반 보호를 사용하고 mutation 전체를 검증한다.
- Session Store readiness는 연결뿐 아니라 schema/index/create-read-delete를 검증한다.
- 제품 Profile에서 DB 오류를 Memory Session/Repository 성공으로 대체하지 않는다.

## 14. Security·Privacy·Audit

- 관리자 MFA, IP/Network policy, Session policy와 service mTLS/OIDC/OAuth/JWT/API key를 지원한다.
- credential/secret/certificate는 외부화하고 keyId 기반 trust, rotation, expiry, revocation을 제공한다.
- PII는 분류·최소수집·masking·raw 승인·retention/deletion을 제공한다.
- 위험조치는 requester/approver 분리, 자기승인 금지, ALL/ANY/N_OF_M, expiry와 immutable command hash를 제공한다.
- Break-glass는 별도 권한, TTL, 긴급사유, 사후 Review와 immutable audit가 필수다.
- Audit는 append-only/tamper-evident하고 before/after snapshot은 credential/PII를 redaction한다.
- XSS, CSRF, SSRF, injection, path traversal, deserialization, upload/archive bomb, unsafe process 실행을 negative corpus로 검증한다.
- Evidence와 로그도 제품 보안 경계이며 Secret/Token/Session/Private Key 원문을 저장하지 않는다.

## 15. 운영·Observability·Reliability

주요 실행 흐름은 system/domain/instance/transaction/segment/attempt/job/execution/item/agent 식별자를 연결한다.

필수:

- metrics, logs, traces, transaction timeline
- bounded cardinality와 masking
- SLI/SLO, error budget와 burn-rate
- alert dedup/group/routing/escalation
- incident, runbook, recovery action와 postmortem
- topology/service catalog
- maintenance/drain/quiesce
- runtime config version/approval/rollback
- desired/actual drift
- capacity trend/load limit
- backup/restore와 DR drill

운영 기능 자체의 장애가 원 업무를 불필요하게 오염시키지 않도록 보안 결정과 관측 기록의 transaction 경계를 분리한다.

## 16. Generator·Developer Experience·EDU

Generator 입력:

- DomainName
- 3자리 SystemCode
- Module/Package
- DB Vendor
- Capability

필수 lifecycle:

```text
create → optional DB bootstrap → build/test/runtime
→ CRUD/Search/Paging/Validation/Commit/Rollback
→ remove → regenerate → normalized parity
```

- Module/Package/SystemCode/Config/Route/Menu/SQL/DB 충돌을 사전 검증한다.
- 하나의 표준 Template을 사용하고 특정 Domain 예외 `if/switch`를 늘리지 않는다.
- 사용자 소유 영역을 덮어쓰지 않는다.
- Generator-owned 영역은 checksum과 deterministic output으로 관리한다.
- 중앙 `domain-template`만 DB 정본으로 사용한다.
- Generated Customer Domain은 Root `cpf-<domain>/`에 실제 Generator Output으로 생성한다. 공식 회귀 Domain은 `cpf-member/`와 `cpf-external/` 두 개이며 같은 Engine/Template으로 생성·재생성한다. 두 Domain normalized parity와 제3 임의 Domain transient 검증으로 Domain-specific hardcoding 0을 증명한다.
- Generated Domain은 CPF BOM + Convention Plugin + Versioned Maven Artifact를 사용하고 Source/JAR 수동 복사를 금지한다.
- EDU와 Sample은 실제 제품 Header/API/DB/Event/Batch/Security 계약을 사용하고 정상뿐 아니라 오류·복구·권한·운영을 교육한다.



### 16.0A Generated Customer Project Naming / Minimal Surface 정책

Generated Business Domain의 고객 사용 Golden Path는 **Project Root `cpf-<domain>/`**다.

논리 Domain명과 물리 Root명을 분리한다.

```text
member   / MBR / member   → cpf-member/
external / EXS / external → cpf-external/
<domain> / <SYS> / <package>            → cpf-<domain>/
```

`cpf-` Prefix는 CPF Generator가 생성한 Project naming convention이며 CPF Product Module/Public Artifact라는 뜻이 아니다.

Generated Project는 개발자가 실제 수정·사용해야 하는 Surface만 노출한다.

```text
cpf-<domain>/
├─ build.gradle          # 실제 Build에 필요할 때
├─ settings.gradle       # multi-module에 필요할 때
└─ online/               # Generated Domain의 Online 업무 Source

Domain Generator는 `online/`을 필수 생성하고 `modules.batch=true`일 때 `batch/`를 선택 생성한다. `jobpack/`은 생성하지 않으며, 공유 `domain/`은 online/batch 등 둘 이상의 실제 Consumer가 공유할 코드가 있을 때만 생성한다. Batch 구현은 `cpf-batch` Owner와 Public `cpf-starter-batch` 계약을 사용한다.
```

강제 원칙:

1. `member`와 `external`은 같은 Canonical Schema, Naming Strategy, Generator Engine, Template Set으로 생성한다.
2. Generator/Template/Script/Build에 `member/MBR/external/EXS` 업무별 특수분기를 두지 않는다.
3. Runtime 구현 Module명으로 `api`를 사용하지 않는다. Online Runtime은 `online/`, 실제 독립 Public Contract는 `contract/`로 구분한다.
4. Root가 이미 Domain Identity를 가지므로 하위 물리 Directory에 Domain명을 반복하지 않는다. `member-online`, `member-batch`, `cpf-member-api` 같은 물리 폴더를 만들지 않는다. Generated Domain의 업무 Runtime Module은 필수 `online/`과 선택 `batch/`다.
5. 현재 Generated Domain Generator는 Online 단일 Consumer만 생성하므로 별도 `domain/` 공유 Module을 만들지 않는다. 향후 실제 복수 Generated Consumer Requirement가 승인되기 전에는 `common/shared/domain` 추상화를 재도입하지 않는다.
6. 선택하지 않은 capability와 빈 Directory를 생성하지 않는다.
7. Generated Project 내부에 `README.md`, `verification/`, `db/canonical/`, `db/vendors/`, Vendor별 SQL 3벌을 기본 생성하지 않는다.
8. 검증/Generation Manifest/hash/DB3 render 결과는 `build/domain-generator/verification/**` 및 CPF Tooling/Evidence owner가 관리한다.
9. DB Canonical Model과 Oracle/PostgreSQL/MariaDB Renderer는 CPF Tooling 내부가 소유한다. 고객이 직접 관리할 DB Extension Surface가 실제 요구될 때만 최소 인터페이스를 별도 제공한다.
10. `standard-enterprise` Generated Domain 회귀는 Online + Sample Transaction을 실제 생성·검증하고, `modules.batch=true/false` 두 경우를 모두 생성·재생성하여 선택형 Batch IA와 Public `cpf-starter-batch` 소비를 검증한다.
11. 이후 `account/product/loan/...`도 설정값만 변경하여 동일 구조/품질로 생성 가능해야 한다.
12. 생성 결과는 날림 Skeleton이 아니라 개발자가 즉시 Run/API/DB/Test 가능한 업무 Base Project다.
13. `dry-run/diff/regenerate/idempotent rerun/upgrade/remove/restore`와 user-owned modification 보호를 구현한다. Generated Project에는 `cpf-domain.yaml`, lock, ownership 같은 lifecycle metadata를 영구 저장하지 않고 Framework definition 또는 명시 `--file` 입력과 실제 Project 구조를 기준으로 stateless/fail-closed하게 동작한다.
14. member↔external normalized parity를 검증한다. Domain/SystemCode/Package/TablePrefix/Port 외 이유 없는 차이는 Generator 결함이다.
15. 제3 임의 Domain은 Repository Root에 남기지 않고 transient verification owner에서 생성·검증·cleanup한다.

### 16.0B Generated Domain 보존 / 삭제 / 재생성 영구 규칙

`cpf-member/`와 `cpf-external/`은 Generator 회귀검증 자산이며 일반 Hygiene/Generated Source Cleanup/Sample 정리를 이유로 삭제하지 않는다. 단 이 둘은 CPF Product Module이 아니라 Generated Customer Project다.

Generator lifecycle의 remove/fresh-generation/upgrade/restore 검증을 위해 일시 삭제한 경우 같은 작업 단위에서 반드시:

```text
Framework `cpf-tools/generator/definitions/<domain>/cpf-domain.yaml` 또는 명시 `--file` 입력
→ Canonical Generator
→ 고객 Project 영구 lifecycle metadata 0
→ Generated Online module
→ Customer DB Extension Source(실제 Consumer가 있을 때만)
→ transient manifest/hash (`build/domain-generator/verification/**`)
→ compile/test/runtime/DB3
→ normalized parity
```

로 복구한다.

Source를 복사하거나 수동 작성하여 복원하지 않는다. 삭제된 상태로 Requirement/Session/최종 결과를 종료하지 않는다.

Permanent 삭제 또는 Canonical Root 정책 변경은 상위 Architecture 검토와 사용자 명시 승인 없이 수행하지 않는다.

### 16.0C Generated Sample Transaction 완료 기준

각 공식 Generated Domain은 최소 하나의 실제 DB 거래를 생성한다.

```text
member   → MBR_SAMPLE_TX
external → EXS_SAMPLE_TX
```

최소 Column은 `ID`, `BUSINESS_KEY`, `STATUS`, `REQUEST_VALUE`, `RESULT_VALUE`, `CREATED_AT`, `CREATED_BY`, `UPDATED_AT`, `UPDATED_BY`, `VERSION`이며 실제 CPF Data/Transaction/Validation/Error/Paging/Logging/Audit 사용법을 보여준다.

최소 API는 Create/Detail/Search(Page)/Update이고, API Test와 DB Transaction Test를 제공한다. Batch Module에는 Sample Transaction 상태를 조회/처리/갱신하는 실행 가능한 Sample Batch와 retry/restart/idempotency Test를 제공한다.

Generator 완료는 **생성 직후 개발자가 Application 실행 → API 호출 → DB 확인 → Test 실행**을 대량 수동 보정 없이 재현할 수 있을 때만 인정한다.

### 16.1 EDU Architecture 판정 기준

EDU는 Product 완성도를 대신하는 우회 구현이 아니다.

- 제품 ADM/BZA/Gateway/Batch 자체의 CRUD·운영·승인·Incident·Topology·Log/Trace·Session 기능은 제품 Module에서 완성한다.
- EDU는 도입 개발자가 직접 작성해야 하는 Consumer/Extension/Integration 개발 예제에 집중한다.
- EDU ID별로 `교육 대상 사용자`, `공개 계약`, `실제 Consumer`, `왜 EDU가 필요한지`를 설명할 수 없으면 Architecture 재분류 대상으로 본다.
- 기존 `EDU-ADM-*`를 포함한 EDU 항목은 숫자를 유지하기 위해 Product 기능을 복제하지 않는다.
- QA는 각 항목을 `유지`, `통합`, `Product 귀속`, `공식 Extension Sample`, `삭제 후보`로 판정하고 영향도를 보고한다.


### 16.2 R6J EDU-ADM 중앙 Architecture 결정

R6J QA A/B 독립 검수 후 다음 원칙을 확정한다.

- `EDU-ADM-08`, `10`, `11`, `12`, `13`, `14`, `15`, `16`, `17`은 ADM Product 기능으로 귀속한다. ADM Product Source/API/Frontend/Test/Runtime/Manual에서 검증하고 generic Education EDU로 복제하지 않는다.
- `EDU-ADM-02`, `03`, `04`, `07`은 공식 Public Extension/Integration 계약을 사용하는 adopter-facing Sample로만 유지한다. 해당 Public 계약이 없으면 먼저 정식 Extension Point를 설계하거나 Product로 귀속한다.
- `EDU-ADM-01`, `05`, `06`, `09`는 독립 ADM EDU로 유지하지 않고 기존 Public Extension/Async/Recovery/Concurrency EDU와 통합한다.
- EDU 17개 또는 전체 135개라는 수량 자체를 완료 기준으로 사용하지 않는다.
- 전체 EDU Canonical Count는 다른 EDU의 Architecture/Consumer 적정성까지 검토하고 Merge/Product 귀속을 반영한 뒤 Catalog에서 재산정한다.
- 수량 보존을 위한 dummy handler, generic JDBC state-machine, Product mimic을 금지한다.
- 물리 Source 삭제는 Delete Manifest와 사용자 승인 절차를 따른다.

### 16.3 Developer Productivity / 표준 확장 계층 / Annotation DX

CPF의 주 사용자는 업무 개발자다. Framework 내부 plumbing을 반복 구현하지 않도록
Controller/Service/Repository/Batch의 **안정된 Golden Path + Starter Capability Composition + Public Operations**를 제공한다.
자동화는 Transaction/Security/Retry/UNKNOWN 경계를 숨기지 않으며, 간편 API와 Native Escape Hatch를 항상 함께 제공한다.

#### 16.3.1 고객 Public Persistence 개념은 `Repository` 하나로 수렴

DAO와 Repository를 고객 Public 개념으로 병렬 노출하지 않는다.

```text
CpfBaseController (abstract, Web)
  → DomainBaseController (abstract, Domain Common)
    → BusinessController (concrete)

CpfBaseService (abstract, Base/Application)
  → DomainBaseService (abstract, Domain Common)
    → BusinessService (concrete)

CpfBaseRepository (abstract, Data/Persistence)
  → DomainBaseRepository (abstract, Domain Common)
    → BusinessRepository (concrete)
```

- JDBC/MyBatis의 class-based Golden Path는 위 `CpfBaseRepository` 3단 구조를 사용한다.
- JPA/Spring Data와 같이 interface 기반이 자연스러운 Provider는 `@CpfRepository + CpfRepositoryContract/Port + composition` 모드를 사용하며 억지 class 상속을 강제하지 않는다.
- 고객에게 보이는 의미는 두 경우 모두 Repository다. DAO/Mapper/JdbcTemplate/MyBatis Mapper 등 저수준 요소는 Provider/Internal 또는 명시적 Legacy Compatibility로 제한한다.
- 기존 `CpfBaseDao`, `@CpfDao`, `*BaseDao`가 active generated/customer-facing surface에 남아 있으면 대체 Consumer 이관과 compatibility 결정이 끝날 때까지 Requirement 미완료다.
- CRUD/Search/Bulk/Lock 등 현재 Repository Port 자산은 폐기하지 않고 Canonical Repository Operations로 통합한다.

#### 16.3.2 Annotation / Bean / Naming Contract

- `@CpfController`, `@CpfService`, `@CpfRepository`는 실제 Runtime Consumer를 가져야 하며 Spring 표준 stereotype/AutoConfiguration과 정합되어야 한다.
- 명시 name은 선택적으로 허용하고, 생략 시 class/interface 기반 Canonical Naming Strategy로 결정한다.
- duplicate bean name, 같은 type의 상충 CPF role, 잘못된 target, 필수 Base/Contract 위반은 startup/preflight에서 fail-fast한다.
- 기본 의존성 주입은 type-based constructor injection이다. 문자열 Service Locator/임의 `ApplicationContext.getBean(name)`을 Golden Path로 만들지 않는다.
- `@CpfDto`는 Singleton Spring Bean 등록용이 아니다. Validation/Serialization/Mapping/Masking/Metadata에 사용하는 Data Contract annotation이다.
- Annotation은 marker 존재가 아니라 `annotation → scanner/registrar/post-processor/interceptor/resolver → runtime behavior → invalid-use fail-fast → actual consumer → test`까지 증명한다.


#### 16.3.2A Common Function / Public Operations Bean Registration Contract

공통 Function은 Java 메서드 목록만 존재해서는 완료가 아니다.
선택된 Starter/Capability가 실제 Spring Runtime에 **Public Operations Bean**으로 materialize되고,
Domain Base의 ergonomic helper와 type-based constructor injection 양쪽에서 동일 Runtime semantics를 사용해야 한다.

- `@CpfController`, `@CpfService`, class-mode `@CpfRepository`는 실제 Spring Bean으로 등록한다.
- interface-mode `@CpfRepository`는 Provider Factory/Proxy를 통해 실제 injectable Repository Bean으로 materialize한다.
- `@CpfDto`는 Singleton Spring Bean으로 등록하지 않는다.
- 각 선택 Public Capability는 고객이 직접 injection 가능한 Canonical `*Operations`/Facade Bean을 제공한다.
- Public Operations Bean의 기본 구현은 가능한 한 **stateless singleton**으로 설계한다.
- Request/User/Transaction/Execution mutable state를 Singleton field에 저장하지 않고 CPF Context/Execution Provider에서 읽는다.
- 미선택 Starter는 관련 Public Operations Bean, Provider Bean, Listener/Endpoint, Scheduler, background thread, SQL/config requirement가 **0**이어야 한다.
- 같은 Capability의 Default Provider가 둘 이상 활성화되면 silent `@Primary`로 임의 선택하지 않고 Generator preflight 또는 startup에서 fail-fast한다.
- Customer override는 공식 Custom Bean/Provider SPI로 허용한다. Default AutoConfiguration은 documented backoff/override contract를 따른다.
- 문자열 Bean lookup/Service Locator를 Golden Path로 사용하지 않는다.
- Bean Scope, 생성 순서, conditional activation, override/backoff, circular dependency, startup failure, shutdown order를 Test로 검증한다.
- 최소 Context Test Matrix:
  - selected capability → Public Operations Bean 존재.
  - unselected capability → Public Operations/Provider/Thread/Listener 0.
  - missing mandatory config/provider → 명시적 startup fail.
  - conflicting providers → fail-fast.
  - documented custom override → customer bean 사용.
  - invalid duplicate role/name → fail-fast.

##### Async Executor Bean

`callAsync`는 JVM common pool이나 무제한 thread 생성에 기대지 않는다.

- CPF가 소유하거나 공식 SPI로 주입받는 **bounded Executor Bean**을 사용한다.
- Context/TransactionId/ExecutionId/Trace/Security Identity를 snapshot/restore한다.
- queue capacity, rejection/backpressure, timeout/cancel, uncaught failure, metrics를 지원한다.
- Graceful Drain/Shutdown 시 신규 작업 수락을 중지하고 in-flight completion/cancel 정책을 적용한다.
- Async Capability가 미선택이면 background Executor를 생성하지 않는다.

##### Messaging / Realtime / Integration Bean

- Listener/Consumer/Publisher/Client/Webhook/SSE/GraphQL 관련 Bean은 해당 Capability가 선택되고 필수 Config가 유효할 때만 등록한다.
- destination/group/listener/client name collision은 fail-fast한다.
- Listener Container는 retry/DLQ/rebalance/drain/stop ordering과 Context propagation을 보장한다.
- External Client Bean은 timeout/retry/security/observability policy가 적용된 Canonical Client/Operations를 제공한다.

##### Batch Bean / Job Registration

- Batch Job/Step/Reader/Processor/Writer/Tasklet/Scheduler/Worker Adapter는 실제 Runtime Bean으로 등록된다.
- Job name/Step name/Bean name 중복은 fail-fast한다.
- Job/Step Scope mutable state를 Singleton에 저장하지 않는다.
- JobRepository/TransactionManager/Executor/Clock/BusinessDate/Lock/Checkpoint dependency는 명시적 Public/Runtime Owner를 가진다.
- 선택되지 않은 Batch Profile에서는 Batch background scheduler/worker bean이 0이어야 한다.



#### 16.3.2B Developer Custom Bean Injection / Method Invocation Contract

CPF는 Starter가 제공하는 Framework Bean뿐 아니라 **업무 개발자가 만든 Spring/CPF Bean을 다른 업무 Bean에서 간단히 자동 주입하여 메서드를 호출**할 수 있어야 한다.
개발자가 `ApplicationContext.getBean(...)`, Bean 이름 문자열, Service Locator를 직접 사용할 필요가 없어야 한다.

##### Public Annotation

Spring 의존 기능이므로 Owner는 `cpf-core`가 아니라 Base/Application Runtime 계층의 Public Annotation으로 둔다.
예시 명칭은 `@CpfInject`이며 실제 Package/이름은 기존 Annotation Naming과 충돌 검토 후 확정한다.

지원 대상:
- Field
- Constructor
- Constructor Parameter
- Method Parameter

기본 동작:
- **타입 기반 자동 주입**.
- `@CpfService`, `@CpfRepository`, CPF Starter의 Public Operations Bean, 일반 Spring `@Component/@Service/@Repository`, `@Bean`으로 등록한 Customer Bean을 모두 주입 대상으로 사용할 수 있다.
- 후보가 정확히 1개면 자동 주입한다.
- 필수 Bean이 0개면 startup fail-fast.
- 같은 Type 후보가 2개 이상이면 임의 선택하지 않고 fail-fast한다.
- 다중 후보가 업무적으로 필요한 경우 `@CpfQualifier("...")` 또는 Spring `@Qualifier`와 동등한 Canonical Qualifier를 사용한다.
- Bean 이름 문자열은 **Qualifier/명시 선택에서만 보조적으로 허용**하고 일반 호출 Golden Path로 사용하지 않는다.
- Optional Capability는 `required=false` 같은 무분별한 null-injection보다 typed Optional/Capability Presence Contract를 우선한다.

##### 개발자 사용 Golden Path

```java
@CpfService
public class MemberPolicyService extends MemberBaseService {

    public boolean canJoin(String memberId) {
        return true;
    }
}
```

다른 개발자 Service:

```java
@CpfService
public class MemberService extends MemberBaseService {

    @CpfInject
    private MemberPolicyService memberPolicyService;

    public boolean joinable(String memberId) {
        return memberPolicyService.canJoin(memberId);
    }
}
```

CPF 실행 경계가 필요한 경우:

```java
public boolean joinable(String memberId) {
    return call(() -> memberPolicyService.canJoin(memberId));
}
```

비동기 실행 경계:

```java
public CompletionStage<Boolean> joinableAsync(String memberId) {
    return callAsync(() -> memberPolicyService.canJoin(memberId));
}
```

다중 구현체:

```java
@CpfInject
@CpfQualifier("vipMemberPolicy")
private MemberPolicy memberPolicy;
```

##### Proxy / Transaction / Security 주의

- 주입되는 객체는 Spring/CPF가 관리하는 **Proxy Bean**이어야 하며, CPF Transaction/Retry/Logging/Security Aspect가 필요한 경우 Proxy를 통해 호출한다.
- `this.someMethod()` self-invocation으로 Proxy 경계를 우회해 Transaction/Retry/Security가 빠지는 구조를 Golden Path로 문서화하지 않는다.
- `call(() -> bean.method())`는 문자열 Reflection 호출이 아니라 compile-time safe lambda 호출이어야 한다.
- `call("beanName", "methodName", args...)` 같은 Bean/Method 문자열 호출 API는 Golden Path에서 금지한다.
- Circular dependency는 field injection으로 숨기지 않고 architecture fail로 탐지한다.

##### Constructor Injection과 `@CpfInject` 관계

- Framework 내부와 Generator-managed Base는 **Constructor Injection을 기본**으로 유지한다.
- Customer Business Source에는 간편한 `@CpfInject` Field/Parameter Injection을 공식 DX로 허용한다.
- 테스트 가능성·불변성·필수 의존성이 중요한 고객 Component에서는 `@CpfInject` Constructor/Parameter Injection을 사용할 수 있다.
- 즉 `@CpfInject`는 `ApplicationContext.getBean()`을 대체하는 공식 간편 API이며, 타입 안전성과 fail-fast를 유지해야 한다.

##### 필수 Test

- Customer `@CpfService` Bean → 다른 `@CpfService`에 `@CpfInject` 주입 PASS.
- 일반 Spring `@Component`/`@Bean` Customer Bean 주입 PASS.
- Public Starter `*Operations` Bean 주입 PASS.
- Bean 0개 → startup FAIL.
- 동일 Type 2개 + Qualifier 없음 → startup FAIL.
- 동일 Type 2개 + Qualifier → 정확한 Bean 주입 PASS.
- explicit bean name/qualifier compatibility.
- injected Proxy를 통한 `@CpfTx`/Retry/Logging/Security Runtime Consumer 동작.
- self-invocation negative test.
- circular dependency negative test.
- Generator/`cpf-member`/`cpf-external`/`cpf-education`에 실제 Custom Bean 호출 Sample과 Test를 포함한다.


#### 16.3.3 Starter를 바꿔도 상속 Class는 바뀌지 않는다

업무 개발자는 Starter를 추가할 때 `extends CpfCacheService`, `extends CpfMessagingService`처럼 Base를 교체하지 않는다.

```text
BusinessService extends DomainBaseService
                           │
                           ├─ Base mandatory operations
                           ├─ selected Cache Operations
                           ├─ selected Messaging Operations
                           ├─ selected Integration Operations
                           ├─ selected Security Operations
                           └─ selected Observability Operations
```

- Generator/Canonical Catalog/AutoConfiguration이 선택된 Public Capability Operations를 composition한다.
- 미선택 Capability는 `NO_DEPENDENCY_NO_BEAN_NO_CONFIG_NO_SQL`을 유지하고 가능한 경우 compile-time helper surface에도 나타나지 않는다.
- exactly-one Provider/conflict는 Generator preflight 또는 startup에서 fail-fast한다.
- Internal leaf를 Base/Generated Domain이 직접 참조하지 않는다.
- Base에 모든 기능을 때려 넣는 God Base를 금지한다. Capability Owner가 Public `*Operations`/Facade를 소유하고 Base는 자주 쓰는 ergonomic helper만 제공한다.
- 복잡한 사용을 위해 동일 Public Operations를 직접 constructor injection하는 Native Escape Hatch를 제공한다.

#### 16.3.4 Controller Common Operations

Controller Base는 최소 다음 개발자 경험을 제공한다.

- `call(...)`, `callAsync(...)`: Service 호출 Context/Trace/Execution/Error semantics 연계.
- current Context, transactionId, executionId, actor/currentUser, permission metadata.
- request/path/query/header/body validation.
- page/sort/cursor normalization과 allow-list.
- standard success/created/accepted/no-content/error response.
- request metadata/idempotency-key/correlation 접근.
- request/error/operation logging 연계.

Controller→Service 및 다른 Service 호출은 허용한다.
Controller→Repository 직접 접근은 Golden Path에서 금지한다.
여러 Service/DB/Message/External orchestration이 커지면 Application/Orchestration Service로 내린다.

#### 16.3.5 Service Common Operations

Service Base/Operations는 Starter 선택에 따라 다음을 제공한다.

- sync `call` / async `callAsync`와 Context/TransactionId/ExecutionId/Trace propagation.
- `required`, 제한된 `requiresNew`, `readOnly`, timeout/isolation/rollback rule, afterCommit/afterRollback.
- retry/backoff/timeout/idempotent/reconcile.
- Cache get/put/evict/getOrLoad.
- Message/Event publish/send 및 Outbox 연계.
- Integration sync/async call, remote timeout/error/UNKNOWN.
- authorize/hasPermission/currentPrincipal와 Audit.
- Common Code/Message/Parameter/Calendar/Template shortcut.
- structured/business/operation/security/audit/error logging 및 metric/trace.

`call()`은 단순 lambda wrapper가 아니다. CPF Runtime semantics를 실제 적용해야 하며,
아무 부가 의미가 없는 wrapper API는 제거하거나 만들지 않는다.

#### 16.3.6 Repository Common Operations

Provider-neutral Repository Operations는 최소 다음 의미를 제공한다.

- findById/findOne/exists/save/insert/update/delete.
- page/cursor/count/search.
- bulkInsert/bulkUpdate/bulkDelete where safe.
- optimistic/pessimistic lock, fencing/lease 연계가 필요한 분산 케이스.
- query timeout, row/page limit, sort allow-list.
- duplicate/constraint/deadlock/timeout/error mapping과 retryability classification.
- vendor-neutral ID/sequence strategy.
- Oracle/PostgreSQL/MariaDB에서 동일 Public 의미.

Provider 고유 고급 기능은 Native Escape Hatch로 접근할 수 있으나 CPF Transaction/Context/Security/Audit 경계를 우회하지 않는다.

#### 16.3.7 Batch Workload Golden Path / Common Operations

Batch 개발자용 workload API와 운영 Control Plane API를 같은 것으로 만들지 않는다.
기존 `CpfBatchOperationsPort` 등 운영 조회/제어 자산은 보존하고,
Job/Step workload 쪽에는 역할에 맞는 `CpfBaseBatch* → DomainBaseBatch* → BusinessBatch*` Golden Path 또는 동등한 안정적 Public Composition을 제공한다.

Batch 업무 개발용 공통 기능:

- Job/Step/Execution Context, businessDate, parameter validation.
- chunk/page/cursor/partition.
- checkpoint/resume/watermark.
- retry/skip/idempotency.
- transaction/commit boundary.
- lock/lease/fencing/concurrency.
- scheduler/agent/runner/worker context propagation.
- stop/cancel/drain.
- progress/metric/business log/audit.
- failure/UNKNOWN/reconcile.
- output/result/restart/rerun/duplicate-run protection.

Scheduler/Agent/Runner/Worker/Center-Cut 실제 Runtime Consumer와 동일 execution/transaction/audit identity를 공유해야 한다.

#### 16.3.8 Logging Common Operations

Logging은 `log.info()` 축약 API가 아니다.

- business / operation / security / audit / error log 의미를 구분한다.
- System/Instance/TransactionId/ExecutionId/User 또는 Service Identity/Batch Job/Step을 자동 연결한다.
- structured field, masking/redaction, trace/metric correlation, CPF error classification을 적용한다.
- 개인정보/credential/secret 원문을 Log/Evidence에 기록하지 않는다.
- log sink 실패가 원거래 의미를 임의로 뒤집지 않도록 failure policy를 명시한다.
- Audit durability가 별도 보장이 필요하면 local tx에 무분별한 `REQUIRES_NEW`를 붙이지 말고 Outbox/별도 durable boundary를 설계한다.
- ergonomic Base helper와 Public Logging Operations 직접 injection을 함께 제공한다.

#### 16.3.9 Transaction Boundary Operations

Transaction DX는 단순 `@Transactional` alias가 아니다.

- programmatic Public Transaction Operations/Executor를 사용하여 self-invocation/proxy 함정을 피한다.
- required/requiresNew/readOnly/timeout/isolation/rollback rule/afterCommit/afterRollback을 명시적 API로 제공한다.
- `REQUIRES_NEW`는 독립 commit이 업무적으로 필요한 경우만 사용하고 Audit/Log 편의를 위해 남발하지 않는다.
- DB + HTTP + Message를 하나의 Local Transaction처럼 보이게 하지 않는다.
- DB→Message는 Outbox, Message→DB는 Inbox/idempotency, 장기 workflow는 Saga/Compensation/Reconcile을 사용한다.
- commit response loss/process kill/timeout에서 UNKNOWN을 보존하고 operator/reconcile flow를 제공한다.
- multi-datasource/XA/JTA는 선택형이며 atomicity/owner/recovery boundary를 명확히 한다.

#### 16.3.10 Cache Operations

기존 provider-neutral Cache/Lock 기반을 재사용하여 다음을 Public semantics로 완성한다.

- get/put/evict/getOrLoad.
- TTL/negative cache.
- invalidation 및 multi-instance refresh/version fence.
- stale policy.
- provider outage/reconnect/fail-open-or-closed 명시.
- stampede/single-flight.
- serialization/version compatibility.
- Caffeine/Redis/Valkey Provider parity와 exactly-one conflict.

#### 16.3.11 Common Product Service와 기술 Operations 경계

`cpf-starters/common`은 Code/Message/Parameter/Calendar/Template 등 고객 업무 공통 Product Service Owner다.
Transaction/Cache/Messaging/Integration/Logging/Security/Persistence 같은 기술 실행 helper를 Common에 몰아넣지 않는다.
Service Base의 `code()`, `message()`, `parameter()`, `businessDate()` 같은 shortcut은 Common Public Service를 composition해 노출한다.

#### 16.3.12 Starter Function Catalog 완전성

최신 Canonical Starter Catalog의 모든 Module을 전수 inventory하고,
각 Module마다 다음 연결이 있어야 한다.

`Dependency/Config → AutoConfiguration → Public Operations/Facade → Base helper → activation/conflict → actual consumer → failure/recovery → Native Escape → Generator/EDU/Sample → Test/Evidence`

Public Starter뿐 아니라 Internal Runtime도 자신이 어느 Public Capability를 구현하는지 연결되어야 한다.
Catalog에 존재하는 Capability가 Function Catalog에서 빠지면 완료가 아니다.

#### 16.3.13 Generator / Generated Domain / EDU 실제 사용 강제

공통 API를 만들고 소비하지 않으면 완료가 아니다.

- Generator template은 새 Annotation/Base/Operations를 생성 또는 소비한다.
- `cpf-member`, `cpf-external`은 동일 Canonical Generator 결과로 새 Golden Path를 사용한다.
- `cpf-education`은 개발자가 복사 가능한 Online/Batch 예제에서 새 API를 실제 사용한다.
- representative Golden Path에 직접 raw CompletableFuture/paging/logging/cache/transaction/integration boilerplate가 남으면 migration 미완료다.
- Advanced/Native Sample은 의도적 Escape Hatch 예제로 명확히 구분한다.
- ADM/BZA/OpenAPI/Generated Client가 해당 운영 Capability의 Consumer라면 함께 currentize한다.

#### 16.3.14 EDU/Testkit False-Green 금지

공통 deterministic state-machine/test-double harness는 Unit/Contract Test로 유용하게 사용할 수 있다.
그러나 Cache/Batch/Messaging/Gateway/DB/DR 등의 `Integration`, `Runtime`, `Recovery` PASS를 주장할 때는
해당 CPF Starter/Provider/Runtime을 실제로 통과해야 한다.

Scenario 이름, JSON Contract, Handler, Mock Consumer 존재만으로 실제 Provider 검증을 대체하지 않는다.
실제 Provider를 거치지 않은 test-double 결과는 해당 Capability Runtime Evidence로 집계하지 않는다.

#### 16.3.15 한글 JavaDoc / 확장성

신규·변경 Public Base/Operations/Extension Point에는 의미 있는 한글 JavaDoc을 작성한다.

최소 설명:
- 목적과 언제 사용하는지.
- 자동 적용되는 Context/Transaction/Security/Logging 정책.
- 정상/오류/UNKNOWN/복구 의미.
- thread-safety/async 주의.
- 필요한 Starter/Capability.
- 확장 지점과 Native Escape.
- 금지/오사용 예.

메서드명을 한국어로 번역한 한 줄 주석은 완료로 인정하지 않는다.


#### 16.3.16 Configuration → Binding → Invocation Commercial Closure

CPF의 모든 Public Capability는 **기능이 존재하는 것**만으로 완료하지 않는다.
고객이 실제 설치·설정하고, Framework가 검증·등록하고, 업무 Source에서 타입 안전하게 호출하며,
운영자가 상태·변경·복구를 확인할 수 있어야 한다.

모든 Starter/Capability의 완료 연결은 다음을 기본으로 한다.

```text
Logical Definition
→ Runtime Binding
→ Typed Configuration
→ Schema / Validation
→ Secret Reference
→ Environment / Profile
→ AutoConfiguration
→ Public Bean / Registry / Client
→ Business Source Invocation
→ Health / Diagnostics
→ ADM/BZA Operations where applicable
→ Audit / Drift / Rollback
→ Generator / EDU
→ Runtime / Fault Evidence
```

##### 16.3.16A 설정 계층과 우선순위

설정은 최소 다음 네 계층을 구분한다.

1. **Build/Generator Selection** — Domain identity, Starter/Capability/Provider 선택, 논리 Dependency/Client 선언. 물리 IP·URL·Secret은 저장하지 않는다.
2. **Deployment/Runtime Binding** — 환경별 Endpoint, Service Discovery, DataSource/Broker/Cache/외부기관 연결, Credential/Certificate Secret Reference, timeout/pool 등 실제 운영 Binding.
3. **Authorized Runtime Override** — `REFRESHABLE` 설정만 versioned/atomic/audited apply 및 rollback. `RESTART_REQUIRED`를 hot apply한 것처럼 가장하지 않는다.
4. **Per-call Override** — Framework가 명시적으로 허용한 timeout/deadline/idempotency 등 bounded option만 허용한다. 임의 endpoint/credential 우회는 Golden Path에서 금지한다.

정확한 우선순위와 충돌 규칙을 정의한다.

```text
safe framework default
→ generator/profile default
→ application config
→ environment/deployment override
→ secret resolution
→ approved runtime override
→ explicitly permitted per-call override
```

최종 effective value는 source/version/hash를 추적할 수 있어야 하며 Secret 원문은 Catalog/Log/UI/Evidence에 노출하지 않는다.

##### 16.3.16B CPF 내부 Domain Call — Topology Independent

`MBR → EXS`, `MBR → ACC`처럼 CPF가 관리하는 업무 Domain 간 호출은 대상 Domain이
동일 JVM, 동일 WAS, 별도 WAS, 별도 IP, 다중 Instance, MSA 중 어디에 배치되어도
**동일한 Typed Domain Contract/Client를 사용**한다.

```text
Business Source
→ Typed Domain Client/Contract
→ Logical Domain/SystemCode
→ Domain Binding Resolver
   ├─ LOCAL  → managed local adapter/proxy
   └─ REMOTE → service/instance registry → routing → transport adapter
```

- 업무 Source에 IP/URL/VIP를 하드코딩하지 않는다.
- 동일 Source가 Local/Remote 전환 시 수정되지 않아야 한다.
- Local과 Remote는 Header/Context/TransactionId/Security/Timeout/Error/Trace/Idempotency의 Public 의미를 동일하게 유지한다.
- Local 배치라고 해서 원격 배치 시 불가능한 숨은 cross-domain local transaction semantics를 만들지 않는다.
- Remote는 `serviceId/domainId`, instanceId, endpoint, zone, version, weight, health, maintenance, draining, lease/TTL를 Registry/Router와 연결한다.
- Static endpoint/VIP/DNS와 Dynamic Registry를 모두 지원할 수 있으나 우선순위와 fallback은 명시적 정책으로 둔다.
- 내부 Domain Call이 외부 Gateway를 재진입하지 않는다.
- 같은 TransactionId를 E2E로 유지하고 각 hop/attempt/async는 Execution/Segment identity로 구분한다.
- 현재 존재하는 Service Call/Registry/Health-aware Routing 자산을 우선 재사용·currentize하고 평행 `callRemote` Framework를 새로 만들지 않는다.
- topology-independent 계약은 `cpf-core` Public API/SPI가 소유하고 HTTP 등 실제 transport implementation은 해당 Starter/Runtime Owner가 소유한다.

##### 16.3.16C External Integration Client / Channel Setup

CPF 외부 시스템 호출은 내부 Domain Registry와 별도 논리 Namespace/Binding으로 관리한다.

예:
`bank-host`, `credit-agency`, `card-company`, `government-api`.

각 Named External Binding은 적용 가능한 범위에서 다음을 가진다.

- Client/Channel ID, owner Domain, enabled/environment.
- HTTP/HTTPS/TCP/SOAP/Fixed-Length/ISO8583/MQ/Webhook/SFTP/Object Storage 등 transport/protocol.
- URL/Host/Port/Destination/Endpoint Pool.
- Codec/Layout/Schema/Version/Charset/Framing.
- TLS/mTLS, Trust/Certificate, OAuth/API Key/Service Credential의 **Secret Reference**.
- DNS/Proxy/Pinning/Allowlist.
- connection/read/write/response timeout, pool/keepalive.
- retry eligibility/backoff/jitter, circuit breaker, bulkhead/rate limit.
- idempotency/correlation/masking/error mapping.
- send-after-response-loss UNKNOWN, result probe, reconcile/compensation.
- health/readiness/degraded/drain.
- Native Provider escape hatch.

여러 외부기관이 같은 transport를 동시에 사용할 수 있어야 하므로 TCP/SFTP/HTTP/S3/Notification 등
자연스럽게 다중 대상이 필요한 Capability를 단일 전역 Bean/단일 endpoint 설정으로 고정하지 않는다.

##### 16.3.16D Binding Cardinality Contract

Capability별 Binding cardinality를 명시적으로 Catalog화한다.

- `SINGLE_DEFAULT_REQUIRED` — 정확히 하나의 기본 Provider/Binding이 필요한 경우.
- `NAMED_MULTI_OPTIONAL_DEFAULT` — 여러 Named Binding을 허용하고 필요할 때만 하나의 default 허용.
- `EXPLICIT_ONLY` — default 없이 이름으로만 선택해야 안전한 경우.
- `INTERNAL_NO_PUBLIC_BINDING` — Public Binding이 아닌 내부 implementation leaf.

전체 Capability에 “default 정확히 하나”를 일괄 강제하지 않는다.
반대로 exactly-one Provider slot에 silent `@Primary`를 허용하지 않는다.
중복 이름, 허용되지 않은 다중 default, missing mandatory default/explicit binding은 preflight/startup fail-fast한다.

##### 16.3.16E Native Provider Configuration Bridge

CPF가 Spring Boot/OSS의 성숙한 연결 설정을 무의미하게 복제하지 않는다.

예:
- Redis/Valkey connection factory와 native client settings.
- Kafka/JMS/MQ broker connection/security.
- DataSource/JPA/MyBatis connection/pool.
- Spring Security/OIDC provider settings.
- AWS/S3 credential provider chain.
- WebClient/TLS/network provider settings.

CPF는 **업무 의미/Binding/정책/보안/복구**를 소유하고,
필요한 Native Property Prefix/Bean/Secret Provider 의존성을 Catalog에 선언한다.
선택 Capability인데 필수 Native Config/Bean이 없으면 preflight/startup에서 명확히 실패한다.
Guide/Diagnostics는 CPF 설정과 Native 설정의 정확한 연결을 보여준다.

##### 16.3.16F Configuration Catalog / Metadata

Canonical Starter Catalog의 모든 `configPrefix`는 Config Catalog와 1:1 정합되어야 한다.
각 Config family는 최소 다음 Metadata를 관리한다.

`id/prefix | owner/capability | scope(global/domain/binding/instance) | typed property/schema |
default/required | unit/range/enum/pattern | secretSeparated | mutability |
runtimeOverrideAllowed | maskedDisplay | nativeDependency | version/since/deprecated/alias |
effectiveSource | healthImpact`

- duplicate prefix, orphan prefix, stale alias, selected Capability의 missing config는 Gate 실패.
- unknown CPF prefix는 strict/preflight/quality gate에서 탐지한다.
- refresh는 immutable snapshot 단위로 atomic apply하며 실패 시 previous-good snapshot을 유지한다.
- multi-instance effective config version/hash drift를 탐지한다.
- `CpfConfigPolicy/CpfConfigCatalog` 등 현재 자산을 우선 확장하며 중복 Config Framework를 만들지 않는다.

##### 16.3.16G Setup Family 전수검수

64 Starter Module과 관련 Runtime에 대해 다음 Setup Family를 누락 없이 검수한다.

- Base/Context/Execution: systemCode, environment, instanceId, Clock/ID, async executor.
- Web/API: port/forwarded header/request limit/CORS/paging/deadline.
- Persistence/Transaction: logical DB role, datasource/provider/vendor/pool/read-write/tx.
- Cache/Lock/Session: provider/binding/native connection/namespace/TTL/invalidation/fail policy/lease.
- Messaging/Event: broker binding/destination/group/schema/ack/retry/DLQ/security.
- Domain Call: logical Domain dependency/topology/registry/routing/service identity.
- External Integration: named external clients/channels/transport/codec/security/recovery.
- File/SFTP/Object: named site/storage/bucket/path/secret/limit/quarantine/reconcile.
- Notification: channel/provider/sender/template/secret/rate/fallback.
- Security: auth mode/OIDC/service identity/session/secret/certificate/trust.
- Platform Operations: log/trace/metric/OTLP/health/config/runtime control/feature flag.
- Common Product Service: cpfDB role/cache/refresh/locale/calendar.
- Batch: scheduler/worker/agent/control/concurrency/chunk/lock/checkpoint/businessDate.
- Generator/Test: logical definitions, binding skeleton, negative/fault tests.

##### 16.3.16H Generator / Generated Domain Setup

Generator input은 물리 운영값을 소유하지 않는다.

Generator는 최소:
- Domain/SystemCode.
- selected Starter/Capability/Provider.
- 논리 `domainDependencies`.
- 논리 `externalClients` 또는 동등한 Client/Channel Definition.
- 필요한 Named Binding의 **이름과 기능 계약**.

을 표현할 수 있어야 한다.

실제 IP/URL/Password/Token/Certificate Secret은 `cpf-domain.yaml`에 넣지 않는다.
Generated application config에는 환경에서 채울 안전한 binding skeleton/env-reference와 상세 한글 주석을 제공한다.

회귀 증명:
- MBR→EXS 동일 JVM.
- 동일 MBR Source로 EXS 별도 WAS/IP.
- MBR→제3 Domain(ACC 또는 ephemeral generated domain)으로 Domain-neutrality.
- EXS→외부 HTTPS.
- EXS→외부 TCP Fixed-Length.
- 같은 transport의 외부기관 2개 이상 Named Binding.
- missing/duplicate/invalid config fail-fast.

##### 16.3.16I ADM / Diagnostics / 운영설정

Configuration/Topology/External Binding은 운영에서 보이지 않는 hidden plumbing으로 두지 않는다.

최소:
- `ops-config`: Config Catalog, effective source/version, mutability, masked value, validation, staged apply/rollback.
- `ops-topology`: Domain/Service/Instance/Endpoint/Zone/Version/Weight/Health/Drain/Routing.
- `external-institutions`: external Client/Channel/Endpoint/Protocol/Health/Certificate 상태와 UNKNOWN/Reconcile.
- `ops-drift`: multi-instance config/runtime drift.
- Diagnostics/doctor/preflight: active profile/capability/binding, missing/unused/unknown config, masked endpoint/secret reference, restart-required 여부.

위험한 Runtime 변경은 permission/reason/approval/SoD/audit를 적용하며 Raw Secret은 표시하지 않는다.


##### 16.3.16K Local / Test / Dev / Stage / Prod Default Setup Contract

CPF는 `127.0.0.1` 또는 `localhost`를 모든 환경의 암묵적 Default로 사용하지 않는다.
Loopback Default는 개발 편의를 위한 **local/test 전용 안전 정책**이다.

- `application.yml`: 환경 중립 Safe Default와 공통 정책. 운영 IP/URL/Secret을 넣지 않는다.
- `application-local.yml`: 단일 개발 PC/IDE 실행. Local Dependency는 기본 `127.0.0.1` 허용.
- `application-test.yml`: Testcontainers/ephemeral port/deterministic harness 우선. 실제 외부기관 접속 금지.
- `application-dev.yml`: 공유 개발환경. Endpoint는 Env/Deployment Binding으로 공급. silent localhost fallback 금지.
- `application-stg.yml`: 운영 유사 검증. 필수 Endpoint/Secret 누락 시 fail-fast.
- `application-prod.yml`: localhost/127.0.0.1/example/sample credential fallback 금지. 필수 Binding 누락 시 fail-fast.

`localhost`는 IPv4/IPv6/hosts 해석 차이가 있으므로 Local Canonical Host 기본은 `127.0.0.1`을 권장한다.
IPv6 local test는 명시적으로 `::1` 또는 별도 binding을 사용한다.

여러 Generated Domain/Gateway/Batch Runtime/Simulator를 한 개발 PC에서 동시에 실행할 수 있어야 한다.
기존 Canonical Port는 호환성 검토 없이 일괄 변경하지 않는다.
신규 Generated Domain은 충돌이 적은 Canonical Local Port Range를 사용하며 권장 신규 범위는 `18080~18999`다.
Generator는 `systemCode/domainId` 기준 stable port를 배정하고 중복을 preflight에서 차단한다.
`server.port: 0`은 Test Harness에서만 허용한다.

##### 16.3.16L Korean Configuration Documentation / Discoverability Contract

CPF의 주요 설정은 Source를 뒤져야만 알 수 있는 hidden option으로 두지 않는다.

선택된 Starter/Capability의 설정 파일은:
1. 실제 실행에 필요한 활성 최소 설정,
2. 자주 사용하는 주요 옵션,
3. 고급/선택 옵션의 주석 처리된 예제,
4. 각 주요 설정의 한글 설명
을 제공한다.

주요 option 한글 주석은 가능한 범위에서 다음을 설명한다.

`[역할] [기본값] [허용값] [단위] [적용범위] [우선순위] [변경] [보안] [운영주의] [실패조건] [관련기능]`

모든 옵션에 장황한 주석을 강제하지는 않지만 개발자가 `application*.yml`과 IDE metadata만 보고
주요 기능을 설정할 수 있을 정도의 설명은 필수다.

`@ConfigurationProperties`/record/config object의 public property에도 의미 있는 **한글 JavaDoc**을 제공하고
Spring Boot configuration metadata와 YAML 주석/default/validation을 일치시킨다.

##### 16.3.16M Profile File Generation / Commented Option Catalog

Generator/Reference App는 역할에 따라:
`application.yml`, `application-local.yml`, `application-test.yml`,
`application-dev.yml`, `application-stg.yml`, `application-prod.yml`
을 생성/currentize한다.

같은 값을 모든 파일에 복제하지 않는다.
공통값은 `application.yml`, 환경별 차이는 profile 파일, 주요 선택옵션은 적절한 위치에 commented example로 둔다.
Secret은 값 대신 `${ENV_VAR}`/Secret Ref 예시를 사용한다.
Native Provider 설정은 CPF semantic option과 실제 native prefix의 연결을 주석으로 설명한다.

##### 16.3.16N Source-level Configuration Override / Customizer Contract

업무 개발자는 필요한 경우 Source에서 Typed Configuration을 확장할 수 있어야 하지만,
모든 설정에 Source 우선권을 주어 운영 Binding을 우회하게 만들지 않는다.

각 Config Property/Family는 다음 Override Policy 중 하나를 명시한다.

- `CONFIG_LOCKED`: Deployment/Secret/운영 정책 소유. Source/Per-call override 금지.
- `SOURCE_DEFAULT`: 외부 설정이 없을 때 Source/Customizer default 허용.
- `SOURCE_CUSTOMIZABLE`: Typed Customizer/Builder/SPI에서 application policy 조정 가능.
- `PER_CALL_BOUNDED`: 호출별 제한적 override. configured min/max를 넘지 못함.
- `RUNTIME_MANAGED`: 승인된 ADM/Operations runtime override만 허용.

기본 안전 precedence:

`Framework Safe Default → Generated/Profile Default → Source Default/Typed Customizer → Application Config → Environment/Deployment Binding → Secret Resolution/Deployment Lock → Authorized Runtime Override → Per-call Bounded Override`

Prod endpoint/credential/TLS/auth policy는 Source override로 무력화하지 못한다.
Source API는 raw Map/string mutation보다 Typed Customizer/Builder/SPI를 우선한다.

##### 16.3.16O Whole-CPF Configuration Completeness Audit

이 Configuration closure는 Gateway/Integration 몇 개만 검수하고 종료하지 않는다.

최소 Scope:
`cpf-core`, `cpf-starters/**`, `cpf-gateway`, `cpf-batch/**`, `cpf-admin`, `cpf-biz-admin`,
`cpf-member`, `cpf-external`, `cpf-education`, `cpf-tools/generator`, `cpf-tools/environment/**`, `deploy/**`.

전 Repository의:
- `application*.yml/yaml/properties/json`
- `@ConfigurationProperties`, Config record/class
- env key/system property/JVM option/CLI parameter
- Docker/Compose/K8s/Helm/CI binding
- Generator config/template
- hardcoded URL/IP/port/path/timeout/retry/TTL/pool/thread/concurrency
를 inventory한다.

중복 key, 동일 의미의 다른 prefix, 문서 없는 주요 옵션, 사용되지 않는 config,
profile별 상충값, stale env var, 환경 종속 hardcoding을 찾아 일괄 정비한다.

##### 16.3.16P Gateway Configuration Closure

`cpf-gateway`는 Configuration Audit의 별도 필수 축이다.

현재 Gateway Safety/Route/Registry/Runtime 설정을 전수검수하고 최소 다음을
한글 주석 + typed property + validation + runtime consumer + operations evidence로 연결한다.

- server bind address / port / graceful shutdown
- public/admin/control-plane bind separation
- forwarded/X-Forwarded/Forwarded trust policy
- route source/static/dynamic registry and refresh
- domain/service target resolution
- timeout/deadline/retry/circuit breaker
- rate limit/bulkhead/concurrency/request size
- CORS/origin/method/header/credentials
- auth/token/service identity/mTLS/TLS/trust
- IP/host/header allowlist
- TransactionId/Context/idempotency
- 401/403/404/409/429/500/502/503/504 mapping
- health/readiness/drain/maintenance
- zone/version/weight/canary routing
- unsafe wildcard/duplicate route fail-fast
- access/security/audit logging and sensitive-header masking
- control-plane permission/reason/approval/audit
- certificate/key/trust Secret Reference
- proxy/DNS/network limits
- local/dev/stg/prod profile-specific defaults

Gateway internal routing과 Domain-to-Domain call을 혼동하지 않는다.
MBR→EXS/ACC 내부 Domain Call은 Gateway를 불필요하게 재진입하지 않는다.

Gateway `application*.yml`의 주요 옵션/주석은 `CpfGatewaySafetyProperties` 등의
필드별 한글 JavaDoc/configuration metadata/실제 default와 일치해야 한다.

##### 16.3.16J Configuration Closure 완료 기준

각 Starter/Capability는 다음 질문에 모두 답할 수 있어야 한다.

1. 무엇을 선택해야 하는가?
2. 어디에 무엇을 설정하는가?
3. 환경마다 무엇이 달라지는가?
4. Secret은 어디에서 공급되는가?
5. 어떤 Bean/Client/Registry가 생기는가?
6. 개발자는 어떤 Typed API로 호출하는가?
7. 여러 Binding/Provider일 때 어떻게 선택하는가?
8. 잘못된 설정은 언제 어떤 오류로 실패하는가?
9. Runtime 변경 가능한가, 재기동이 필요한가?
10. Multi-instance에서 어떻게 일관성을 보장하는가?
11. 운영자가 어디서 상태·변경·복구를 보는가?
12. Generator/EDU/Sample에서 실제 어떻게 사용하는가?
13. Native Provider 기능은 어떻게 escape하는가?
14. 장애/UNKNOWN/복구 Evidence는 무엇인가?

하나라도 답이 없으면 해당 Capability의 Commercial DX/Release 완료로 판정하지 않는다.


#### 16.3.17 Developer-First Call / Standard Result / Transaction / Logging Contract

CPF의 최종 DX 기준은 “기능 수가 많음”이 아니라 **처음 보는 업무 개발자가 History를 몰라도 이름만 보고 용도를 이해하고,
IDE 자동완성과 짧은 코드로 안전하게 개발할 수 있는가**다.

다음 원칙은 Controller/Service/Repository/Domain Call/External Integration/Messaging/Async/Batch/Operation에 공통 적용한다.

##### 16.3.17A 모든 호출을 관리하되 모든 Java Method를 Wrapper로 오염시키지 않는다

CPF는 모든 Public 호출을 정책적으로 관리하지만 모든 Java Method의 반환을 `CpfResult<T>`로 일괄 Wrapping하지 않는다.

- 동일 JVM의 일반 `Service → Service`는 자연스러운 Java Type을 반환한다.
- `Service → Repository`도 `T`, `Optional<T>`, `List<T>`, `CpfPage<T>`, `long`, `boolean` 등 자연스러운 Persistence Type을 유지한다.
- 이 호출들도 CPF Managed Bean/Proxy/AOP/Context/Transaction/Logging/Security 정책 아래에 있으므로 관리 밖이 아니다.
- 반대로 Network/Distributed/Async/Messaging/External Side Effect/Runtime Operation처럼 호출결과의 확정 여부와 Recovery 판단이 필요한 **CPF Boundary Function**은 표준 `CpfResult<T>` 계열을 강제한다.

Architecture Gate는 Annotation/Role별 허용 반환계약을 검사하여 개발자별 임의 설계를 막는다.

##### 16.3.17B 표준 호출 결과

Core는 topology-independent Public Value Contract로 최소 다음 의미를 제공한다.

```java
CpfResult<T>
CpfCallOutcome
CpfCallMeta
CpfErrorInfo
CpfRecoveryInfo
```

`CpfCallOutcome`의 최소 상태:

```text
SUCCESS
BUSINESS_FAILURE
TECHNICAL_FAILURE
UNKNOWN
```

의미:

- `SUCCESS`: 요청 결과가 확정적으로 성공.
- `BUSINESS_FAILURE`: 상대/업무 로직의 거절·검증실패 등 업무결과가 확정. 기본적으로 기술 Retry 대상 아님.
- `TECHNICAL_FAILURE`: 기술 실패가 확정. `retryable` 정책에 따라 제한적 Retry 가능.
- `UNKNOWN`: 요청이 상대에 전달되어 Side Effect가 발생했을 수 있으나 응답 유실/timeout/process-kill 등으로 결과를 확정할 수 없음.

`false`, `0`, 빈 List/Map은 정상 `data`일 수 있으므로 데이터 값으로 성공/실패를 판단하지 않는다.

`CpfResult<T>`는 최소:
- `outcome`
- `data`
- `error`
- `meta`
- `recovery`
와 `isSuccess/isBusinessFailure/isTechnicalFailure/isUnknown`, 안전한 `map/fold` 또는 동등 Convenience를 제공한다.

조회처럼 Side Effect가 없고 Contract가 허용하는 경우에만 `requireData()` 또는 동등 편의 API로 표준 예외 변환을 허용한다.
Side Effect/UNKNOWN 가능 호출에서 UNKNOWN을 숨기는 무조건 unwrap API는 금지한다.

##### 16.3.17C 표준 자료구조 / 자료형

표준 Result는 다음을 모두 Type-safe하게 표현한다.

- 단건: `CpfResult<MemberResponse>`
- List: `CpfResult<List<MemberResponse>>`
- Page: `CpfResult<CpfPage<MemberResponse>>`
- Cursor: `CpfResult<CpfCursorPage<MemberResponse>>`
- Map: `CpfResult<Map<String, MemberResponse>>`
- Scalar: `CpfResult<String/Integer/Long/BigDecimal/Boolean/...>`
- Count: `CpfResult<Long>`
- no-data: `CpfResult<Void>` 가능하나 Side Effect는 `CpfAck`/Receipt를 우선
- async: `CompletionStage<CpfResult<T>>` 또는 동등 표준
- operation: `CpfResult<CpfOperationReceipt>`
- messaging: `CpfResult<CpfMessageReceipt>`
- file/object transfer: `CpfResult<CpfTransferReceipt>`
- streaming: 전체 메모리 적재 없이 item Publisher/Stream + terminal `CpfResult<CpfStreamSummary>` 또는 동등 계약

Generic `List<T>`, `Map<K,V>`, Page 같은 응답을 raw `Class<T>` 하나로 역직렬화하지 않는다.
Golden Path는 Generated Typed Client이며 Generic Escape에는 `CpfTypeRef<T>`/Parameterized Type Token 또는 동등 안전 계약을 제공한다.

##### 16.3.17D Error / Meta / Recovery

`CpfErrorInfo`는 적용 가능한 범위에서:
`code, category, message, field/offset, remoteStatus, failureStage, retryable, unknownResult, operatorGuidance`
를 가진다. Internal stack trace/secret/raw payload를 Public Result에 넣지 않는다.

`CpfCallMeta`는 적용 가능한 범위에서:
`transactionId, executionId, segmentId, parentSegmentId, attempt, requestId, traceId, spanId,
targetDomain/clientId, targetInstance, elapsedMs, timestamp, idempotency reference`
를 가진다.

`CpfRecoveryInfo`는 최소:
`NONE/RETRY/RECONCILE/COMPENSATE/MANUAL_REVIEW` 또는 동등 next-action,
reconcile key/status, retry-after, previous/confirmed outcome을 표현한다.

XA/TCC 상태를 표현하는 기존 `CpfTransactionOutcome`과 일반 호출 결과 `CpfCallOutcome`을 혼용하지 않는다.

##### 16.3.17E 호출 Family와 Golden Path

1. **Controller → Service**
   - `call(...)`, `callAsync(...)`.
   - Web/Application execution envelope.
   - DB Transaction 자체가 아님.

2. **Service → Service same JVM**
   - `otherService.method(...)` 직접 Typed 호출이 기본.
   - 의미 있는 orchestration execution boundary가 필요할 때만 Service `call/callAsync`.

3. **Service → Repository**
   - Repository Typed API 직접 호출.
   - Controller→Repository Golden Path 금지.

4. **CPF Domain → Domain**
   - MBR→EXS, MBR→ACC는 EXS/ACC가 같은 JVM/별도 WAS/IP/MSA 어디에 있어도 CPF Domain Call.
   - Generated Typed Domain Client가 Primary.
   - Generic helper는 `callDomain/callDomainAsync` 또는 naming-consistent 동등 API.
   - 기존 `callRemote/callRemoteAsync`는 **명시적으로 Remote Transport를 강제해야 하는 Advanced/Compatibility Escape**로 한정하고 Domain Golden Path로 문서화하지 않는다.

5. **CPF Domain → External**
   - Generated Typed External Client가 Primary.
   - Generic helper는 `callExternal/callExternalAsync` 또는 naming-consistent 동등 API.
   - Business Source에서 URL/IP/WebClient/Socket/Registry lookup 금지.

6. **Messaging/Event**
   - `publish/send/requestReply`와 Receipt/UNKNOWN 의미.
   - DB→Message Outbox, Message→DB Inbox/idempotency.

7. **Async**
   - Context snapshot/restore, bounded executor, same transactionId, meaningful new execution/segment identity.

8. **Batch/Operation**
   - Job/Step/Chunk/Checkpoint/Operation Receipt와 TxId lineage를 연결.

##### 16.3.17F Domain Call의 Local/Remote 동일 의미

Domain Client는 Local/Remote에 따라 업무 Source가 바뀌지 않는다.

```text
Typed Domain Client
→ Logical Domain/SystemCode
→ Binding
   ├─ LOCAL  → managed domain adapter
   └─ REMOTE → Registry → Health/Drain → Routing → Transport
```

Local 배치에서도 caller의 DB Transaction이 callee Domain DB Transaction에 우연히 참여해
Remote 배포 시 의미가 바뀌는 hidden cross-domain transaction을 만들지 않는다.

Remote call 중 caller local write transaction을 오래 유지하는 패턴은 Golden Path가 아니다.
필요한 경우 명시 Policy를 요구하고 lock/timeout/UNKNOWN 영향 Test를 제공한다.
장기 일관성은 Outbox/Inbox/Saga/TCC/XA(선택형)/Compensation/Reconcile로 해결한다.

##### 16.3.17G MSA 성공/실패/UNKNOWN 개발자 처리

Boundary Client는 기대 가능한 Remote 결과를 무조건 Exception으로 던져 개발자가 catch chain을 만들게 하지 않는다.
정상적으로 분류 가능한 Remote 결과는 `CpfResult<T>`로 반환한다.

- SUCCESS → `data` 사용.
- BUSINESS_FAILURE → 업무 오류 code/message를 표준 응답으로 처리, blind retry 금지.
- TECHNICAL_FAILURE → `retryable`과 idempotency 정책으로 bounded retry 또는 상위 오류.
- UNKNOWN → blind retry 금지. idempotency가 보장된 정책만 retry 가능하며 기본은 result probe/reconcile/manual review.

Programming Error, missing binding, invalid configuration, impossible contract misuse는 startup/preflight 또는 `CpfFrameworkException` 계열로 fail-fast할 수 있다.

##### 16.3.17H TransactionId / ExecutionId / SegmentId

- `transactionId`: 최초 정식 Channel/System에서 1회 생성/신뢰승계하고 전체 거래 E2E 유지.
- `executionId`: 의미 있는 runtime execution/async/attempt 단위.
- `segmentId/parentSegmentId`: 호출 계보와 hop을 표현.
- `attempt`: retry attempt.
- 동일 거래 retry에서 새 transactionId 생성 금지.

DB Transaction은 위 추적 ID와 다른 개념이다.
MBR DB Tx, EXS DB Tx, 외부기관 호출은 하나의 transactionId 아래 별도 consistency boundary일 수 있다.

##### 16.3.17I Logging — 8.1 정본과 일치

모든 거래/Domain Call/External/Messaging/Async/Batch는 8.1 로그 정책을 사용한다.

최소 구조화 필드는 적용 가능한 범위에서:
`timestamp, level, systemCode, environment, instanceId, transactionId, executionId,
traceId, spanId, segmentId, parentSegmentId, attempt, requestId/idempotencyKey,
actor/tenant/channel, jobId/jobInstanceId/jobExecutionId/stepExecutionId/partitionId/itemId/agentId/workerId,
operation/endpoint/remoteSystem, result/status, errorCode, failureStage, retryable,
unknownResult, elapsedMs, message/file identifiers`.

Logging Common Operations는:
- `businessLog`
- `operationLog`
- `securityLog`
- `audit`
- `errorLog`
또는 naming-consistent 동등 API를 제공한다.

자동 Technical Call Log와 개발자가 명시하는 Business/Audit Log를 구분한다.
Payload/credential/token/session/private key/PII 원문 기록 금지.
Audit durability를 위해 `REQUIRES_NEW`를 남발하지 않는다.

##### 16.3.17J Developer-Friendly Function Contract

모든 Public Function/Operations/Client는 개발자가 Manual을 정독하지 않아도 IDE에서 이해할 수 있어야 한다.

각 Public Function은 한글 JavaDoc/IDE metadata에서 최소:
- 언제 쓰는가
- 입력/반환 Type
- 주요 Option/default
- Transaction 영향
- Context/TxId/Log 자동처리
- SUCCESS/BUSINESS_FAILURE/TECHNICAL_FAILURE/UNKNOWN 의미
- Retry/Recovery
- 필요한 Starter/Config
- 잘못 쓰면 어떻게 실패하는가
를 설명한다.

String service locator, bean/method reflection, raw Map 기반 옵션, 임의 endpoint 입력을 Golden Path로 사용하지 않는다.

##### 16.3.17K Architecture / Generator / EDU Enforcement

Architecture Test/Generator/EDU는 최소 다음을 강제한다.

- `@CpfService`: 자연스러운 return type 허용, Result wrapper 강제 금지.
- `@CpfRepository`: persistence type 사용, network call result type 남용 금지.
- Domain/External Boundary Client: `CpfResult<T>` 또는 승인된 stream/async variant.
- `callRemote` raw URL/IP 사용 금지.
- Generic Boundary Call의 parameterized type 안전성.
- Generator가 Domain/External Typed Client와 표준 Result 예제를 생성.
- EDU가 단건/List/Page/Map/scalar/boolean/count/void/async/UNKNOWN 예제를 실제 Consumer로 제공.
- Manual과 실제 Public API/Config/Log field drift Gate.

#### Developer Experience 완료 기준

- 일반 금융권 Spring 개발자가 IDE 자동완성과 Public JavaDoc만으로 기본 업무를 구현할 수 있다.
- Starter 추가/제거 때문에 Business class의 상속 Base를 바꾸지 않는다.
- CPF 사용 코드가 Native OSS 직접 구성보다 같거나 짧고 기본 보안/오류/운영 정책은 더 안전하다.
- Controller→Service→Repository/Integration/Message/Batch 실제 Consumer가 존재한다.
- Logging을 위해 MDC/transactionId/traceId를 수동 조립하지 않는다.
- Validation/업무예외/외부연계/DB/Batch 오류가 CPF 표준 결과/로그/운영 추적으로 연결된다.
- Generator/Generated Domain/EDU/Sample/Testkit이 동일 Golden Path를 실제 사용한다.
- actual-provider Integration/Recovery, misuse Negative Test, Native Escape Test, boilerplate 비교 Evidence가 있어야 완료다.


### 16.4 CPF Standard Enforcement / Domain Scoped Exception

CPF 표준 규격은 문서 권고가 아니라 자동 검증 가능한 실행 정책이어야 한다.
표준 위반이 발견되면 가능한 가장 이른 단계에서 실패시키며, Build/CI를 통과한 뒤 운영에서 처음 발견되는 구조를 금지한다.

#### Enforcement Chain

```text
Canonical Requirement / Architecture
  → Canonical Policy Catalog
    → IDE·Generator Guard
      → Compile·Static·Architecture Gate
        → Gradle qualityGate
          → Test·Runtime Startup Fail-Fast
            → CI/Release Gate
              → ADM/Observability
```

표준 Rule은 stable `ruleId`를 가지며 최소 다음 Metadata를 가진다.

- owner
- category
- description
- applicable scope
- severity
- enforcement stage
- validator/gate
- exception policy
- remediation guidance

Architecture Ownership, dependency direction, standard extension hierarchy, Public API/SPI/Internal boundary,
Context/Header, Validation, Error, Logging, Transaction, Security, DB Vendor, Generator, Generated Domain,
Sample/EDU, OpenAPI/Frontend, Batch/Message/Integration, Repository Hygiene를 가능한 한 동일 Policy Catalog에서 추적한다.

#### Rule Classification

Rule은 최소 다음 세 종류로 분류한다.

1. `NON_OVERRIDABLE`
   - Security/Secret/법적·감사 필수조건, 보호 경로, 치명적 dependency cycle 등 Domain이 임의 해제해서는 안 되는 규칙
   - 위반 시 항상 Fail
2. `DOMAIN_OVERRIDABLE`
   - Domain 특성상 합리적인 예외가 있을 수 있는 Architecture/DX/구성 규칙
   - 아래의 Scoped Exception Contract를 만족할 때만 제한적으로 허용
3. `ADVISORY`
   - 권장 규칙
   - Warning과 진단을 제공하되 별도 정책에서 Fail로 승격할 수 있음

Rule 분류가 없는 규칙을 Domain이 임의로 예외 처리하지 못한다.

#### Domain Scoped Exception Contract

Domain 예외는 Framework 전체 통제를 끄는 기능이 아니다.
`DOMAIN_OVERRIDABLE` Rule 하나의 특정 Scope만 한시적으로 면제하는 관리 기능이다.

예외 설정은 최소 다음을 포함한다.

```text
ruleId
domain
scope(module/package/type/resource)
reason
owner
expiresAt
```

위험도가 높은 Rule은 추가로 approver/ticket/reference를 요구할 수 있다.

필수 정책:

- `disableAll`, wildcard 전체 해제, 무기한 예외를 금지한다.
- 예외는 Domain 범위를 벗어나 다른 Domain/Framework 모듈에 전파되지 않는다.
- 만료된 예외는 자동으로 무효화하고 Gate에서 Fail한다.
- 존재하지 않는 Rule, 잘못된 Scope, 중복 Exception, NON_OVERRIDABLE Rule 예외 요청은 Fail한다.
- 예외 사용 사실은 Build/CI/Evidence/Runtime 진단에 명확히 남긴다.
- 예외 때문에 Security/Masking/Secret/Audit 등 상위 필수 보호를 우회할 수 없다.
- Framework 기본 정책보다 Domain 설정이 우선하는 것은 해당 Rule/Scope의 승인된 면제에 한정한다.

#### Runtime / Operations

Runtime 적용 Rule은 Startup 시 Effective Policy와 Exception을 검증하고 잘못된 설정은 Fail-Fast 한다.
운영자는 ADM/진단 기능에서 최소 다음을 조회할 수 있어야 한다.

- 활성 Rule/Effective Policy
- 현재 Domain Exception
- reason/owner/만료일
- 적용 Scope
- 최근 위반/예외 사용 이력

운영 화면에서 임의로 전체 표준 통제를 해제하는 기능은 제공하지 않는다.

#### Completion Gate

표준 Enforcement 기능은 Script 존재만으로 완료하지 않는다.

- 실제 위반 Fixture가 각 Gate에서 실패하는 Negative Test
- 유효한 Domain Exception은 해당 Scope만 통과하는 Test
- 다른 Scope는 계속 실패하는 Isolation Test
- NON_OVERRIDABLE 우회 실패 Test
- Expired/Unknown/Wildcard Exception 실패 Test
- Generator/Generated Domain/Sample/EDU의 표준 준수
- CI/Runtime/ADM Evidence
- Policy Catalog와 실제 Gate 구현의 1:1 정합성

을 증명해야 한다.

## 17. Build·Artifact·배포·Supply Chain

Artifact 공급 모드:

- `LOCAL_DEV`: 검증된 shared local Maven repository
- `REMOTE`: Nexus/Artifactory 등 승인 Registry
- `OFFLINE`: Manifest/Checksum이 있는 versioned offline Maven bundle

REMOTE/OFFLINE 실패 시 개발자 Local Repository로 fallback하지 않는다.

Build 필수:

- fresh clone, clean Gradle/npm cache
- settings/includeBuild/project path 전체 존재와 resolution
- Java 25 toolchain, Wrapper/BOM/Plugin/Lock 정합성
- Published POM/BOM/source/javadoc
- deterministic/reproducible artifact
- final JAR/WAR/static artifact dependency inclusion
- ADM/BZA package lock와 generated client
- unsupported stack fail-closed

Artifact/Deploy 필수:

- canonical manifest와 SHA-256
- environment/channel/service/version/release sequence binding
- keyId 기반 signature/trust/revocation
- local artifact state tamper protection
- install lock와 atomic activation
- health/service identity/build SHA 검증
- 실제 side effect가 발생한 instance만 selective rollback
- deployment request hash/idempotency와 unknown-result reconciliation
- power loss/process kill 후 이전 또는 새 version 중 하나로 복구

Supply-chain은 Source directory가 아니라 **각 최종 Release Artifact**를 검사한다.

- CycloneDX resolved graph
- ORT analyze + evaluate + report
- Syft final artifact SBOM
- Grype final artifact vulnerability
- Approved OSS lock와 PURL/name/version/hash 양방향 대조
- conditional license 승인과 THIRD_PARTY_NOTICES/source obligation
- 모든 도구의 source SHA, input artifact hash, config/tool binary hash 일치

## 18. 설치·Migration·Upgrade·Rollback·Compatibility

GA 지원 표기는 다음이 실제 실행됐을 때만 가능하다.

- Empty Install
- 최소권한 Service User Provision
- idempotent mandatory seed
- reinstall
- upgrade
- rollback 또는 forward recovery
- backup/restore
- mixed-version rolling compatibility
- JAR/WAR/static artifact
- 3 DB Vendor
- Local/Remote topology
- multi-instance
- signed deploy와 rollback
- API/DB/config/message/file/전문 compatibility

지원하지 않은 Docker/Kubernetes/Cloud/DB/Browser/OS는 문서 문자열만으로 지원 표기하지 않는다.

## 19. Repository·문서·Evidence 정본

Repository Root에는 제품 식별, Build, 실행에 필요한 최소 파일과 공식 Module만 둔다. Root 문서는 `README.md` 하나만 허용한다.

정본 역할:

- Final Target: 최상위 제품 목표와 Requirement Catalog
- Continuity Ledger: Requirement ID 영속성
- Architecture/ADR/Specification: 구조·계약·결정
- Guide: 개발·운영·설치·복구
- Current Request: 현재 작업
- Review/Handover: 독립 검수와 연속성
- Evidence: 직접 실행 근거
- Generated: 재생성 가능한 파생물
- Release: 실제 Release만

Evidence 최소 필드:

- exact source SHA와 clean tree
- 실행 명령
- profile/environment/topology
- tool/runtime version
- 시작·종료 시각
- requirement/scenario ID
- exit code와 실제 결과
- report/log/artifact SHA-256
- 민감정보 정제 여부
- 현재 Commit 유효성

파일 존재, 문자열 Marker 수, 정적 검색, Swagger 노출, 일부 Test, 과거 Commit Evidence, 작업자 보고는 단독 완료 근거가 아니다.

### 19.1 Business Framework 횡단 완결성

CPF는 기능 개수보다 **업무 개발 표준화, 낮은 boilerplate, 운영 추적성, 개방형 확장성**을 제품 품질로 본다. 다음 6개 횡단 관점은 기존 Requirement 전체에 적용되는 Acceptance이며 별도 기능 축으로 중복 집계하지 않는다.

1. **Golden Path** — 동일 목적의 Public 개발 경로는 하나를 우선 제공하고, 대체 경로는 compatibility/internal/native escape 용도를 명시한다. String service locator, raw URL, internal registry 직접 접근을 Golden Path로 사용하지 않는다.
2. **Execution Lifecycle** — Request 수신부터 Context, Authorization, Transaction/Execution ID, Validation, Business Execution, DB Tx, Domain/External Call, Retry/Idempotency, Result, Audit/Log/Metric/Trace, Recovery/Reconcile, Response까지 하나의 추적 가능한 실행 생명주기로 연결한다.
3. **Common Product Service** — Code/Message/Parameter/Calendar/Template는 단순 Utility가 아니라 DB ownership, version/effective time, cache/refresh/invalidation, locale/business date, authorization/audit와 운영 관리까지 실제 업무에서 사용할 수 있는 Product Service로 제공한다.
4. **Operational Journey** — 하나의 transaction/execution identifier로 Gateway→Domain→External/Message/Batch→Retry/UNKNOWN→Reconcile→최종 결과와 Audit을 운영자가 추적할 수 있어야 한다. 분리된 Log/Trace/ADM 기능의 존재만으로 완료하지 않는다.
5. **Generator-first DX** — 생성된 고객 Project가 CPF의 권장 사용법을 가장 정확히 보여야 하며 Framework 내부 Source를 읽지 않아도 DB, Domain Call, External Call, Config, Result/Recovery, Online/Batch 개발 흐름을 이해할 수 있어야 한다.
6. **Open Extension / Native Escape** — CPF는 정책·보안·복구·운영 의미를 소유하되 Spring/JDK/Provider native 기능을 불필요하게 재구현하거나 차단하지 않는다. Typed Customizer/Builder/Provider SPI/Bean override/native configuration bridge를 우선하고 내부 구현 침범을 요구하지 않는다.

다음은 금지한다.
- 모든 기능을 CPF 전용 API로 다시 감싸는 폐쇄적 wrapping
- 중앙 Registry/Manager에 대한 Business Source 직접 결합
- 배치 위치나 서버 구조에 따라 Business Source가 달라지는 topology 종속
- Config 의미를 Source 내부를 읽어야만 알 수 있는 불투명성
- 문자열 상태/서비스명/Provider명 비교를 기본 개발방식으로 강제
- Framework upgrade 시 Config/API/Generated Source 호환성 정책 없는 일괄 파괴 변경
- 운영 화면은 존재하지만 실제 실패/UNKNOWN/복구 결과를 연결하지 못하는 False Green

## 20. 최종 제품화 Gate

다음이 모두 최신 exact Commit과 재현 가능한 환경에서 확인돼야 GA 완료다.

1. 공식 Module/Package/SystemCode/DB Ownership과 dependency 방향
2. fresh clean settings evaluation, full build/test와 published artifact
3. Empty DB install, reinstall, upgrade, rollback/forward recovery, backup/restore
4. 주요 API와 Runtime E2E
5. Local/Remote parity와 mixed-version compatibility
6. Multi-instance, lease, fencing, rebalance, failover와 recovery
7. 실제 Kafka, 외부 failure, response loss와 unknown-result reconciliation
8. Spring Batch, Scheduler, Center-Cut, Agent/Runner/Worker
9. Gateway streaming/disconnect/retry/failover와 ledger
10. ADM/BZA Server Authorization, Production Build와 3 Browser E2E
11. Session/BFF, Security, Approval, Audit, Privacy와 Masking
12. Generator create→runtime→remove→regenerate lifecycle
13. Final Artifact signature, deploy, selective rollback와 supply-chain scan
14. EDU, OpenAPI, JavaDoc, 개발/운영/설치/복구 Guide
15. Requirement→Source/API/SQL/Test/Runtime/Evidence와 역방향 추적
16. Root Hygiene, No Legacy/Dual Primary/Dead Code/Stale Evidence/Secret

하나라도 `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`이면 전체 GA 완료가 아니다.

## 21. Requirement ID 연속성

- Requirement ID는 세션, PC, AI 계정, Architecture Rename과 무관한 영구 Key다.
- 통합은 `superseded-by`, 분해는 `split-into`, 폐기는 근거·영향·대체·승인을 Continuity Ledger에 기록한다.
- Owner 변경으로 ID 의미를 지우지 않는다.
- Legacy Alias와 Canonical ID를 완료율에 중복 집계하지 않는다.
- Canonical Count 감소는 Continuity Mapping으로 완전히 설명돼야 한다.
- 새 요구는 `REQ-GAP` 절차로 기존 ID와 중복을 먼저 검사한다.

현재 Canonical Requirement Count는 **186개**이며, 아래 Catalog가 각 ID의 최소 제품 의미와 완료 증명을 정의한다.

## 22. 상세 Requirement Catalog

### 22.1 Architecture/Core

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `ARCH-MISSION` | cpf-core / repository architecture | CPF를 샘플이나 공통 라이브러리가 아닌 금융권 포함 엔터프라이즈 업무시스템의 구축·운영·감사·확장·배포를 책임지는 상용 Business Platform Framework로 완성한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `ARCH-MSA` | cpf-core / repository architecture | 동일 Public Contract로 Modular Monolith, 동일 JVM Local Call, 분리 WAS Remote Call, 독립 Microservice를 지원하며 topology 변경이 업무 계약을 바꾸지 않게 한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `ARCH-BOUNDARY` | cpf-core / repository architecture | 기술 공통·고객 공통·플랫폼 운영·업무 관리·Batch·Gateway·Generated Domain의 Owner를 단일화하고 역방향·순환·DB 직접 접근을 금지한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `ARCH-LAYER` | cpf-core / repository architecture | Public API, 확장 SPI, Internal 구현을 Module·Package·Publication·JavaDoc·ArchUnit로 구분하고 외부 Consumer가 Internal Package를 참조하지 못하게 한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-API` | cpf-core / repository architecture | 고객 개발자가 최소 입력으로 안전하게 사용할 수 있는 발견 가능한 Public API를 제공하고 거대 Utils·의미 없는 Wrapper·선택 Runtime type 노출을 금지한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-SPI` | cpf-core / repository architecture | 고객·Generated Domain·기관 Adapter가 구현할 안정된 SPI와 lifecycle, capability, version compatibility, failure contract를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-CONFIG` | cpf-core / repository architecture | safe default→customer property→profile→operation override→per-call override 순서와 허용범위·권한·버전·감사·rollback을 보장한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-TESTKIT` | cpf-core / repository architecture | Public Contract, Header, 오류, idempotency, Local/Remote parity, failure injection을 외부 Consumer가 재사용할 수 있는 Test Kit로 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-CALL` | cpf-core / repository architecture | 동일 JVM과 분리 WAS 호출에 동일한 Header·권한·timeout budget·오류·추적·idempotency를 적용하고 내부 호출의 Gateway 재경유를 금지한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-REGISTRY` | cpf-core / repository architecture | Service·Endpoint·Instance·capability·version·zone·health·maintenance·draining 상태의 등록, lease, TTL, stale 제거와 조회 계약을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-ROUTING` | cpf-core / repository architecture | service/instance/zone/version/weight/maintenance 정책에 따른 routing과 failover를 결정적으로 수행하고 승인된 운영 override와 audit를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-HEALTH` | cpf-core / repository architecture | liveness·readiness·startup·dependency·business readiness를 구분하고 service identity·build SHA·schema version까지 검증 가능한 health 계약을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-HEADER` | cpf-core / repository architecture | 표준/확장 Header의 이름·형식·신뢰경계·생성자·전파·masking·최대크기·호환성을 정본화하고 spoofing을 차단한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-CONTEXT` | cpf-core / repository architecture | transaction, trace, segment, caller, principal, environment, channel, deadline, attempt context를 동기·비동기·Batch 전 구간에 보존한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-TXID` | cpf-core / repository architecture | 정식 거래 기동 Channel/System이 34자리 transactionId를 최초 생성할 수 있고 이후 Local/Remote/REST/SOAP/Gateway/Message/Async/Retry/Batch/File/UNKNOWN/Reconcile/Log/ADM Timeline 전체가 같은 transactionId를 승계한다. 비신뢰 주체의 사칭·변조·replay는 인증된 Channel/System identity와 trust policy로 차단한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-ROLE` | cpf-core / repository architecture | transaction role, direction, source/target, caller/receiver 관계를 표준 Context·Log·Audit에 일관되게 기록한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-ERROR` | cpf-core / repository architecture | 표준 오류 코드·HTTP/Protocol mapping·retryability·unknown-result·field error·operator message를 버전 가능한 계약으로 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-VALID` | cpf-core / repository architecture | 입력·출력·설정·Header·파일·메시지·SQL parameter 검증과 오류 위치, allowlist, 크기·깊이·개수 상한을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-IDEMP` | cpf-core / repository architecture | canonical request hash, scope, TTL, result replay, conflict semantics와 concurrent race를 포함한 idempotency 원장을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-STATE` | cpf-core / repository architecture | 승인·비동기·배치·배포·복구 등 장기 거래의 허용 상태전이, 낙관적 잠금, terminal state, reconciliation을 명시한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-LOCK` | cpf-core / repository architecture | optimistic/distributed lock, lease, fencing token, owner epoch, expiry, takeover와 stale writer 차단을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-RESILIENCE` | cpf-core / repository architecture | timeout, retry, circuit breaker, bulkhead, rate/backpressure, retry storm 방지와 operation별 정책을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-DEADLINE` | cpf-core / repository architecture | 요청 전체 deadline budget을 하위 호출·DB·Broker·파일·process에 분배하고 초과 시 cancel·cleanup·unknown-result 규칙을 적용한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-SCHED` | cpf-core / repository architecture | 기술 Scheduler의 trigger, cluster claim, misfire, calendar, idempotency, pause/resume, 운영 제어 계약을 정의하고 Batch Scheduler와 Owner를 분리한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-OPSDB` | cpf-core / repository architecture | 운영 DB의 공유/분리 topology, schema ownership, 연결 장애 시 fail-open/fail-closed, backpressure, 복구와 readiness를 정의한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-LOGDB` | platform-operations/logging + persistence provider; core는 공통 transaction/error/context 의미만 | DB Log의 schema·index·retention·masking·비동기 적재·조회 성능·장애 격리와 ADM projection을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-FILELOG` | platform-operations/observability logging capability; core는 공통 context 의미만 | 환경·Domain·Instance·transactionId·execution 단위로 탐색 가능한 구조화 File Log, rotation, retention, secure permission과 수집 계약을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-LOGFAIL` | platform-operations/observability logging capability | 로그 저장 실패가 업무를 오염시키지 않도록 정책별 fail-open/closed, local spool, 재전송, 중복 제거, 유실 탐지와 alert를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-TRACE` | core trace/context contract + platform-operations/observability provider | transactionId와 trace/span/segment/attempt를 연결하고 sampling, trace boost, baggage allowlist, cardinality·민감정보 통제를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-MASK` | core classification/redaction contract + security/masking capability | PII/Secret/Credential 분류, context-aware masking/redaction, raw 조회 승인, logging/evidence/download 정책과 테스트 corpus를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-FIXED` | integration/fixed-length contract + fixed-length starter/provider | 고정길이 전문 Layout/Field/Group/encoding/byte length/parser/writer/validator/version/streaming과 secure diagnostic engine을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-FILE` | file capability contracts + archive/attachment/transfer/object-storage providers | Path Alias, bounded streaming, checksum, atomic publish, symlink/path traversal 방지, cleanup, cancellation을 포함한 File/Attachment/Archive 기술 계약을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-MESSAGE` | cpf-core / repository architecture | versioned broker envelope, correlation, idempotency key, schema, TTL, producer/environment binding, size limit와 serialization allowlist를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |

| `ARCH-STARTER` | product architecture + cpf-tools generator/build | `cpf-core`를 Spring Boot 없는 초경량 계약 Artifact로 유지하고 Leaf Starter·Capability Profile·Aggregate Starter·BOM의 역할, Provider 충돌, Consumer와 Footprint를 정본화한다. Starter는 단순 OSS Dependency Wrapper가 아니며 OSS 직접 적용보다 설정·API·오류처리·보안·감사·운영이 더 단순하고 안전해야 하고, 좋은 Default·Fail-Fast·세밀한 Override·Native API Escape Hatch를 제공한다. | non-Boot Core consumer, Starter removal compile, Profile resolution lock, Aggregate POM, BOM/publication, actual Consumer, startup/classpath/fault Evidence + OSS 직접 적용 대비 boilerplate/설정 감소와 misuse fail-fast 검증 |

### 22.37 Common/Data

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `CMN-EXTENSION` | cpf-starters/common + customer `<customer>-common` | 고객 Header·User Context·Validation·Error Mapping·Masking·Audit·Web Client 정책을 cpf-core SPI 위에서 확장하며 기술 Engine을 중복 소유하지 않는다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-SAMPLE-DB` | cpf-education/testkit | Production Common DB가 아닌 Reference Fixture의 단일 Golden Sample Table로 CRUD/Search/Paging/Validation/duplicate/optimistic lock/commit/rollback을 3 Vendor에서 검증한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-CODE` | cpf-starters/common | 고객 공통 코드·참조데이터의 group/item/version/유효기간/cache/invalidation/조회·관리·audit 계약을 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-MSG` | cpf-starters/common | 다국어·오류·업무 메시지의 code, locale, parameter schema, fallback, cache, version과 관리 계약을 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-CALENDAR` | cpf-starters/common | 영업일·휴일·기관 calendar, 기준일 계산, DB-less fallback, override 승인과 Batch/업무 공통 소비 계약을 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-TEMPLATE` | cpf-starters/common | 알림·문서 Template의 version, variable schema, escaping, preview, channel extension, approval과 audit를 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-OWNERSHIP` | cpf-tools DB + owning module | 모든 schema/table/view/index/FK/trigger/seed/query에 단일 Owner와 실제 Consumer를 부여하고 Admin/타 Domain의 직접 갱신을 금지한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-INSTALL` | cpf-tools DB + owning module | Schema/User provision, 최소권한, product table/index/constraint, mandatory seed, verify/smoke를 Vendor-native 정본으로 재현 가능하게 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-FRESH` | cpf-tools DB + owning module | Oracle·PostgreSQL·MariaDB 검증을 CPF Object 0건의 전용 초기 Database/Schema에서 시작하고 Canonical/Generator-first Fresh Install→Upgrade→Rollback→Reapply→Cleanup을 자동화한다. | Vendor별 pre-object-count 0, generated metadata/seed, runtime query, drift, rollback/reapply, different-hash, optional pack, post-cleanup exact-SHA Evidence |
| `DB-MIGRATION` | cpf-tools DB + owning module | 불변 version migration, expand-migrate-contract, checksum, drift fail-closed, restart, data transform와 신규설치 최종상태 parity를 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-ROLLBACK` | cpf-tools DB + owning module | rollback/forward recovery 가능성 분류, 데이터 보존·backup checkpoint·승인·재적용·부분 실패 복구를 Vendor별로 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-BACKUP` | cpf-tools DB + owning module | schema/data/config/key metadata의 backup, encryption, retention, restore validation, PITR와 DR 연계 절차를 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-MULTI-VENDOR` | cpf-tools DB + owning module | MariaDB·PostgreSQL·Oracle의 type/default/index/FK/paging/locking/error semantics와 install→upgrade→rollback→reapply 동등성을 보장한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-SQL` | cpf-tools DB + owning module | Query ID·Owner·parameter/result contract·sort/filter allowlist·Vendor SQL resource·MyBatis/JDBC 규칙과 Java literal SQL 금지를 적용한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-PERF` | cpf-tools DB + owning module | index/plan/statistics/partition/slow query/capacity/purge 성능 기준과 대표 데이터 규모의 regression gate를 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-MULTI` | cpf-tools DB + owning module | multi datasource, read replica, read/write routing, transaction consistency, lag, failover/failback와 tenant/domain isolation을 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DATA-LINEAGE` | cpf-tools DB + owning module | 입력 source→처리→저장→외부전달의 dataset/field lineage, quality rule, reconciliation, owner와 audit 연결을 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DATA-RETENTION` | cpf-tools DB + owning module | retention, purge, archive, legal hold, 개인정보 삭제, backup 예외와 실행 증적을 데이터 유형별로 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |

### 22.57 Gateway/External/Event

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `GWY-ENTRY` | cpf-gateway | 외부 진입점의 TLS, listener, protocol, client identity, request limit, maintenance와 control/data plane 분리를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `GWY-ROUTING` | cpf-gateway | Spring Cloud Gateway 기반 route snapshot, service registry, path/query rewrite, load balancing, version/zone/weight routing과 atomic refresh를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `GWY-TRUST` | cpf-gateway | trusted proxy와 client header allowlist, internal header overwrite, forwarded chain, principal/context 생성과 SSRF target allowlist를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `GWY-RESILIENCE` | cpf-gateway | connect/send/response/read 단계별 timeout·retry·failover·circuit breaker·streaming completion·client disconnect·unknown-result ledger를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `API-LIMIT` | cpf-gateway + cpf-core contract | client/channel/API/tenant별 rate limit·quota·burst·abuse detection·distributed counter·429/Retry-After·운영 override를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-INST` | root `cpf-external/` generated domain / customer adapter | `external`(EXS)을 공식 Generated Customer Domain 회귀 인스턴스로 사용하되 Generator에 EXS를 하드코딩하지 않는다. 기관별 Adapter는 Metadata/Feature와 고객 확장 Owner로 생성·확장하며 `cpf-external`은 Generated Project Root 역할로만 사용하고 CPF Product Module/Public Artifact로 등록하지 않는다. | external fresh generation, integration-http/resilience actual consumer, Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-REST` | generated domain / customer adapter | 외부 REST 호출의 auth, timeout, retry, idempotency, schema, mapping, audit, mock/test contract를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-FIXED` | generated domain / customer adapter | 기관별 고정길이 Layout/Mapping/endpoint를 CORE-FIXED Engine 위에 versioned Adapter로 구현한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-SEC` | generated domain / customer adapter | 외부연계 mTLS/OAuth/API key/certificate/secret rotation, endpoint allowlist, payload masking과 non-repudiation을 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-FILE` | generated domain / customer adapter | SFTP/파일명/ack-nack/checksum/claim/transfer/reconciliation/retention을 고객 Adapter가 안전하게 소유한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-UNKNOWN` | generated domain / customer adapter | 외부 요청의 전송 전 실패·전송 후 응답 유실·상대 처리 불명 상태를 분류하고 자동 성공·무조건 재시도를 금지한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-RECON` | generated domain / customer adapter | 상대 조회·callback·file ack·수동 확인을 통한 reconciliation, compensation, reprocess, SLA와 운영 UI를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EVENT-CORE` | cpf-core contract + owning business adapter | Provider-neutral destination naming, versioned envelope, key/order, producer/consumer contract와 in-memory test adapter를 제공하며 Provider별 의미 차이를 명시한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EVENT-OUTBOX` | cpf-core contract + owning business adapter | 업무 데이터 변경과 Outbox INSERT를 동일 Local Transaction으로 묶고 stable event/message ID, claim/lease/fencing, retry, ordering, publish/confirm 상태, cleanup, broker ACK 유실·process kill 후 중복 발행과 UNKNOWN/Reconcile을 제공한다. Outbox는 일반 로그가 아니라 외부 전달이 완료될 때까지 생명주기를 관리하는 durable delivery state다. | 업무 DB+Outbox 동일 commit/rollback, 실제 Broker, publisher kill/restart, ACK loss, duplicate publish, multi-instance claim, timeout·unknown·reconcile Evidence |
| `EVENT-BROKER` | cpf-core contract + Starter Provider owner | Kafka/JMS/IBM MQ/AMQP의 ACK·transaction·redelivery·consumer concurrency·backpressure·schema·TTL·security·observability와 multi-instance correlation을 공통 계약으로 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EVENT-MQ` | cpf-core contract + Starter Provider owner | Queue 기반 Messaging의 destination, durable delivery, correlation, idempotency, expiry, priority, transaction, redelivery, DLQ와 운영 조회를 Provider-neutral 계약으로 제공한다. | Kafka/JMS/IBM MQ/RabbitMQ provider contract parity, actual broker, duplicate/ordering/outage/recovery/multi-instance Evidence |
| `EVENT-JMS` | cpf-starter-messaging-jms | Jakarta JMS 3.x ConnectionFactory, producer/consumer, transaction/session, selector, durable subscription, redelivery, exception listener와 readiness를 CPF Event 계약에 연결한다. Local JMS transaction과 XA-capable ConnectionFactory/XAResource 경로를 구분하며 XA/JTA 선택 시 CPF Transaction Strategy와 연결한다. | embedded/mock만이 아닌 실제 JMS provider matrix, local/XA transaction, redelivery/connection-loss/recovery Evidence |
| `EVENT-IBM-MQ` | cpf-starter-messaging-ibm-mq | JMS 공통 Starter 위의 Optional Provider로 IBM MQ Queue Manager·Channel·TLS·CCDT/endpoint·connection recovery·reason-code mapping·운영 상태를 제공한다. CPF 기본 Runtime에 IBM MQ 의존성을 강제하지 않으며 고객이 선택할 때만 로드되고 XA-capable JMS 구성이 필요한 경우 TX-XA-JTA 계약과 연결한다. | optional dependency/bean 0-footprint, IBM MQ compatible runtime, TLS/credential rotation, queue manager outage, reconnect, XA/in-doubt/duplicate/reconcile Evidence |
| `EVENT-AMQP` | cpf-starter-messaging-rabbitmq | RabbitMQ/AMQP exchange·queue·binding·publisher confirm·consumer ack/nack·redelivery·DLX·quorum/connection recovery를 CPF Event 계약에 연결한다. | actual RabbitMQ runtime, confirm/ack/nack/DLX, duplicate/order/outage/recovery Evidence |
| `EXS-TCP` | cpf-starter-integration-tcp + generated/customer adapter | 영속 TCP 연결의 framing·encoding·heartbeat·reconnect·backpressure·correlation·TLS·half-open·전송 후 결과 불명과 기관별 전문 Adapter 연결을 제공한다. | loopback/mock 및 실제 fault proxy, disconnect/half-open/timeout/response-loss/duplicate/reconcile/multi-instance Evidence |
| `EVENT-DLQ` | cpf-core contract + owning business adapter | retry topic, DLT, poison isolation, replay approval, payload masking, idempotent reprocess와 운영 추적을 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `SAGA-CORE` | cpf-core contract + owning business adapter | 장기 업무 흐름의 step, state, version, timeout, idempotency, event/call correlation과 durable orchestration/choreography 계약을 제공한다. 동일 transactionId lineage 아래 STARTED/RUNNING/COMPLETED/FAILED/COMPENSATING/COMPENSATED/UNKNOWN/MANUAL_REVIEW 상태를 구분하고 restart/multi-instance에서도 이어서 복구한다. | A→B→C(/D) 실제 Reference, 부분 성공, process kill, timeout, duplicate, compensation/retry/unknown/reconcile/multi-instance Evidence |
| `SAGA-COMP` | cpf-core contract + owning business adapter | 각 step의 compensation eligibility, reverse order, idempotency, partial compensation와 unknown-result 분리를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `SAGA-MANUAL` | cpf-core contract + owning business adapter | 자동 복구 불가 Saga의 승인된 수동 확정·보상·재실행·audit·operator guidance를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |

### 22.78 Core Transaction Strategy / Starter DX / AI

기존 `CPF-CALL`, `CPF-CONTEXT`, `CPF-TXID`, `EXS-*`, `EVENT-OUTBOX`, `EVENT-*`, `SAGA-*`, `ADM-TX`, `ADM-TIMELINE`, `SEC-*`를 대체하지 않는다. 아래 Requirement는 이 기능들을 하나의 상용 Transaction/Integration 모델로 연결하는 새 상위 Capability와 기존 정본에 없던 XA/JTA·TCC·AI·Developer Experience 공백만 추가한다.

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `TX-STRATEGY` | cpf-core contract + selected runtime owner | 업무 특성에 따라 `LOCAL`, `XA_JTA`, `OUTBOX`, `SAGA`, `TCC`를 명시적으로 선택·조합하는 정책, 안전한 Default, 상호배타/호환 규칙과 선택하지 않은 Runtime 0-footprint를 제공한다. 동일 거래에서 전략을 혼합해도 transactionId lineage와 오류/복구 모델이 유지되어야 한다. | strategy selection API/config, fail-fast conflict test, selected-only dependency/bean/config/SQL, 실제 Consumer와 혼합 전략 E2E Evidence |
| `TX-LOCAL` | cpf-core contract + data runtime owner | 단일 Resource의 local transaction을 가장 단순한 기본 개발경험으로 제공하고 commit/rollback, propagation, isolation, timeout, read-only, exception mapping, transactionId/log 연계를 표준화한다. XA Provider가 없어도 기본 업무가 정상 동작해야 한다. | 실제 Domain Consumer, commit/rollback/timeout/exception Test, 3 DB Vendor Runtime 또는 타당한 DB-less 근거 |
| `TX-XA-JTA` | cpf-core transaction contract + Optional JTA Provider owner | JTA/XA를 Optional 상용 Capability로 제공한다. Tomcat에서는 standalone Transaction Manager Adapter, JTA-capable WAS에서는 managed JTA Adapter를 지원하며 DB+DB, DB+JMS의 XAResource enlistment와 2PC prepare/commit/rollback/heuristic/in-doubt 상태를 제공한다. 특정 TM 구현을 `cpf-core`에 강제하지 않는다. | Tomcat-compatible standalone TM Reference, managed-JTA adapter contract, Oracle/PostgreSQL/MariaDB XADataSource, JMS XAConnectionFactory/XAResource, DB+DB·DB+JMS Consumer/Test/Runtime Evidence |
| `TX-XA-RECOVERY` | JTA Provider owner + operations | prepare 이후 process kill, TM/RM restart, commit 중 장애와 in-doubt transaction을 durable recovery log와 resource recovery scan으로 안전하게 해소하고 duplicate recovery·heuristic outcome을 구분한다. ADM에서 권한·사유·감사와 함께 조회/조치한다. | prepare-kill-restart, TM/RM restart, in-doubt scan, heuristic/manual review, multi-instance/fencing, ADM Timeline/Recovery Evidence |
| `TX-INBOX` | messaging reliability owner + business consumer | At-least-once 전달 환경에서 Inbox/Dedup을 공식 계약으로 제공하고 eventId/messageId, consumer identity, idempotency, concurrency, duplicate/partial processing, process kill/restart와 retention을 관리하여 업무 중복 Side Effect를 방지한다. | Outbox→Broker→Inbox 실제 Consumer, duplicate/redelivery/process-kill/multi-instance Test, dedup state/cleanup/reconcile Evidence |
| `TX-TCC` | cpf-core contract + owning business domain | Hold/Reservation형 업무를 위한 Optional `Try/Confirm/Cancel` 계약을 제공한다. Try/Confirm/Cancel idempotency, empty rollback, hanging, duplicate confirm/cancel, timeout, UNKNOWN, recovery를 지원하되 Framework가 업무 보상 의미를 임의 결정하지 않는다. | 잔액/한도/재고 등 Reference Consumer, Try→Confirm/Cancel, duplicate/empty rollback/hanging/process-kill/reconcile Evidence |
| `TX-E2E` | cpf-core + all integration/runtime owners | Domain Call, 외부 REST/SOAP/TCP/File, DB, JMS/Kafka/RabbitMQ, Batch, Outbox/Inbox, Saga/TCC/XA, Retry/UNKNOWN/Reconcile, Logging/Audit/Trace/ADM을 하나의 transaction lineage로 연결한다. 기능별 단독 PASS로 E2E 완료를 대신하지 않는다. | 동일 Reference Transaction의 Source→Consumer→Call Path→failure/recovery→Log/ADM Timeline, local/remote/multi-instance/process-kill Evidence |
| `TX-DX` | cpf-core + Starter owners | 업무 개발자가 transactionId/log/audit/metrics/retry/idempotency/recovery를 매번 수동 조립하지 않도록 typed API, 안전 Default, 최소 Config, Fail-Fast와 세밀한 Override를 제공하고 고급 사용자는 underlying transaction/provider native 기능에 접근할 수 있게 한다. | 실제 업무 Consumer 코드 비교, boilerplate 감소, configuration misuse negative test, native escape/conformance Evidence |
| `TX-EDU` | cpf-education + generator | Local, XA DB+DB, XA DB+JMS, XA crash recovery, Outbox/Inbox, Saga compensation, TCC, 외부 timeout/UNKNOWN/Reconcile, Domain A→B→C, Batch 연계를 실행 가능한 Reference로 제공하고 동일 transactionId와 ADM Timeline을 검증한다. | executable EDU/Reference, Source+Test+Harness, 3 Vendor/actual broker where applicable, failure/process-kill/restart Evidence |
| `STARTER-DX` | all Starter owners + generator/build | Canonical Starter Catalog의 모든 활성 Starter가 OSS 직접 사용보다 편하고 안전한 개발경험을 제공해야 한다. 편의 API, AutoConfiguration, 최소 설정, safe default, Fail-Fast, CPF Error/Security/Audit/Masking/Observability/Transaction 연계, Provider 확장성, Native API Escape Hatch, 미사용 0-footprint와 실제 EDU Consumer를 갖춘다. Wrapper-only/consumer-less Starter는 완료가 아니다. | 활성 Starter 전수 DX matrix, OSS-direct 대비 사용 코드/Config 비교, actual Consumer, boot context, failure/timeout/retry/unknown, selected-only footprint, EDU Evidence |
| `AI-OPTIONAL` | Optional AI Starter/Capability owner | 특정 AI Provider에 종속되지 않는 Optional AI API/SPI를 제공하고 model/provider routing, timeout/retry/circuit breaker/fallback, sensitive data masking, token/usage/cost metering, audit/observability, transactionId, authorization/policy, 위험 작업 approval, provider failure/UNKNOWN을 제공한다. 자체 LLM·Vector DB·거대 Agent Framework를 제품 기본범위로 만들지 않는다. | 최소 2 Provider 또는 1 Provider+customer plugin conformance, sensitive-data negative test, timeout/fallback/unknown, usage/cost/audit, approval, actual Consumer/EDU Evidence |


### 22.79 Batch/Center-Cut

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `BAT-CORE` | cpf-batch | Spring Batch를 Job/Step/Repository/ExecutionContext/Restart의 단일 Primary Engine으로 사용하고 자체 중복 실행 Engine을 제거한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-JOB` | cpf-batch | immutable approved definition/plan checksum, Job identity, parameter schema, start/stop/restart/abandon/recover/reconcile와 상태 연결을 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-ITEM` | cpf-batch | reader/processor/writer, chunk/skip/retry/checkpoint, item idempotency, partition, restart와 대용량 memory bound를 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-EXECUTOR` | cpf-batch | Java, approved Shell, File Watch/Process/Transfer, Service/API, Message Executor를 Step 안에서 timeout·resource·security 정책과 함께 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-AGENT` | cpf-batch | Agent pool, capability, zone, lease, heartbeat, drain, takeover, artifact/config, process tree와 execution output budget을 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-CALL-SYNC` | cpf-batch | Batch/Worker의 업무 Domain 동기 호출에 Local/Remote parity, Header, deadline, idempotency, retry/unknown-result를 적용한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-CALL-ASYNC` | cpf-batch | Batch/Worker의 Event/Outbox 비동기 호출에 stable message ID, retry/DLT, consumer idempotency와 completion correlation을 적용한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-SHARED` | cpf-batch | Batch가 온라인/공유 Facade를 재사용할 때 Owner, transaction boundary, load isolation, version, topology와 운영 영향도를 정의한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-CORE` | cpf-batch | Center-Cut의 job/item/attempt/aggregate 상태모델, immutable policy와 Spring Batch/업무 transaction 경계를 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-RUNNER` | cpf-batch | CenterCutRunner를 Agent 내장 또는 독립 Process로 배포하고 target generation→claim→dispatch→aggregate lifecycle을 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-PARAM` | cpf-batch | 대량 작업 parameter snapshot, schema, canonical hash, encryption/masking, version과 replay 재현성을 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-CLAIM` | cpf-batch | item claim, lease, fencing, chunk assignment, stale worker 차단, duplicate prevention과 restart를 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-RATE` | cpf-batch | global/domain/target TPS·RPS, concurrency, backpressure, adaptive throttle, pause/drain과 multi-instance 일관성을 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-REPROCESS` | cpf-batch | failed-only·selected·range 재처리, approval, idempotency, prior result 보존, compensation와 결과 비교를 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-UNKNOWN` | cpf-batch | item/attempt 결과 불명을 분류·대사하고 확인 전 무조건 재처리를 금지하며 수동 확정과 audit를 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-OPS` | cpf-batch | ADM에서 job/item/attempt/timeline/progress/error/reprocess/pause/cancel/drain을 권한·사유·승인과 함께 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |

### 22.98 Admin/Security/Operations

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `ADM-AUTH` | cpf-admin | 운영자 identity, password/MFA/OIDC, JDBC Session, session fixation·concurrency·revocation·force logout과 fail-closed product profile을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-RBAC` | cpf-admin | menu/button/API/command 권한, role mapping, 유효기간, organization context, server-side enforcement와 cache invalidation을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-AUDIT` | cpf-admin | 운영자·대상·before/after masked snapshot·reason·approval·result·transactionId의 immutable/tamper-evident audit를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-TX` | cpf-admin | 온라인/비동기/Batch/외부연계 transaction 검색, 표준 Header, payload masking, segment/attempt linkage와 상세 조회를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-TIMELINE` | cpf-admin | transactionId 기준 Local/Remote/Event/Batch/Gateway/File/Agent timeline을 순서·시각·instance·failure stage로 재구성한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-SERVICE` | cpf-admin | service/endpoint/instance/health/version/zone/routing/maintenance/draining을 조회·제어하되 Owner Command API를 사용한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-LOG` | cpf-admin | File/DB Log 조회, saved search, trace boost, dynamic log level, retention, download guard와 권한·audit를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-BATCH` | cpf-admin | Job definition/execution/step/checkpoint/restart/stop/recover와 승인·사유·Spring Batch ID 연계를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-CENTER` | cpf-admin | Center-Cut job/item/attempt/progress/reprocess/unknown/compensation 운영 기능을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-AGENT` | cpf-admin | Agent/Runner/Worker registry, capability, heartbeat, artifact, process, drain/takeover와 위험조치 승인을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-EXS` | cpf-admin | 외부기관 endpoint, health, credential/certificate status, request/response timeline, unknown/reconciliation을 기술 Owner API로 관제한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-RECOVERY` | cpf-admin | unknown-result, DLQ, Saga, deployment, file/batch 실패의 runbook, 승인된 recover/compensate/reconcile와 결과 추적을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-INCIDENT` | cpf-admin | alert→incident→severity/owner→runbook/action→postmortem/closure 흐름과 관련 transaction/evidence를 연결한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-UX` | cpf-admin | 대량 검색·paging·sort·filter·saved condition·status·empty/error/loading·responsive·keyboard·accessibility·safe download UX를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-APPROVAL` | cpf-admin | 플랫폼 위험조치의 versioned policy, ALL/ANY/N_OF_M, SoD, expiry, break-glass, immutable command hash와 owner-command execution을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `BZA-BUSINESS` | cpf-biz-admin | 고객 업무 관리자 메뉴·권한·업무 조회·등록·변경·download·approval을 업무 Domain Public Contract로 수행한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `BZA-ORG` | cpf-biz-admin | 조직 hierarchy, 직원, 사번, 직급/직책, 유효기간 assignment, 겸직/파견/대행, masked profile과 결재 snapshot을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `BZA-APPROVAL` | cpf-biz-admin | 순차/병렬/개인/role/조직/ALL/ANY/N_OF_M/위임/대결/회수/재상신/만료/동시승인과 policy/instance 분리를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `BZA-SEQUENCE-SAMPLE` | cpf-biz-admin | 업무 채번을 선택형 Customization Sample로 제공하되 기본 Runtime 의존을 만들지 않고 규칙·시험·승인·audit를 교육한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `SEC-AUTHN` | cpf-core security contract + product owner | 사용자·운영자·service의 MFA/OIDC/OAuth2/JWT/API key/mTLS 인증, credential lifecycle, session/token replay 방어를 제공한다. Resource Server뿐 아니라 OIDC/OAuth2 Login 기반 SSO를 Keycloak·Microsoft Entra ID·Okta 등 외부 IdP와 연동하고 user/tenant/role/group/scope/claim을 CPF Security Context로 안전하게 매핑하며 login/logout/session/token 만료·갱신과 Frontend/BFF 연결을 제공한다. SAML2는 필요 시 Optional 확장으로 둔다. | 보안 Negative Corpus, credential/PII leak scan, issuer/audience/expiry/claim mapping, login/logout/session/refresh, IdP failure, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-AUTHZ` | cpf-core security contract + product owner | RBAC/ABAC, least privilege, server-side resource/action authorization, SoD, permission version과 즉시 회수를 제공한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-SECRET` | cpf-core security contract + product owner | Secret Provider SPI, 외부 Vault/file/env 및 KMS/HSM Provider integration, key version/rotation/revocation/provider health/failure-timeout, 필요 시 PKCS#11 연계, zeroization와 log/config/ADM/evidence의 key·secret 원문 금지를 제공한다. Local/JCE와 외부 KMS/HSM은 동일 계약을 따르되 Provider 고유 기능을 불필요하게 가두지 않는다. | 보안 Negative Corpus, credential/PII leak scan, KMS/HSM/provider failover·health, key version/rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-CERT` | cpf-core security contract + product owner | certificate/key trust store, issuance/import, expiry alert, rotation, revocation, mTLS identity와 keyId 기반 검증을 제공한다. 기존 Crypto/Secret을 재사용하여 범용 digital signature의 sign/verify, algorithm, keyId/keyVersion, certificate, signature metadata와 audit를 제공하고 Private Key 원문 노출을 금지한다. | 보안 Negative Corpus, credential/PII leak scan, sign/verify negative corpus, key/certificate rotation·revocation, 권한·audit와 침해경계 Evidence |
| `SEC-PRIVACY` | cpf-core security contract + product owner | PII catalog, 목적·최소수집·동의/법적근거, masking, raw access, retention/deletion, export와 audit를 제공한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-DOWNLOAD` | cpf-core security contract + product owner | 대량/민감 download의 권한·사유·승인·watermark·row/size limit·expiry·encryption·one-time link와 audit를 제공한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-APP` | cpf-core security contract + product owner | injection, SSRF, path traversal, upload/archive bomb, XSS/CSRF, deserialization, process execution, security header와 secure default를 통제한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-APPROVAL` | cpf-core security contract + product owner | 보안 위험행위의 dual control, 자기승인 금지, immutable target hash, expiry, break-glass, 사후 review를 제공한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-AUDIT` | cpf-core security contract + product owner | audit append-only/tamper detection, canonical payload, previousHash/currentHash chain, 선택적 digital signature, record 수정·삭제 탐지, concurrency/multi-instance 일관성, clock/identity, retention, search, export와 evidence integrity를 제공한다. Masking 후 canonicalization과 검증 순서를 명확히 하고 Audit 자체가 Secret/PII 원문 저장소가 되지 않게 한다. | hash-chain mutation/delete/reorder/concurrency/multi-instance 검증, signature verification, 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `OPS-METRIC` | cpf-admin control plane + runtime owner | transaction/service/instance/DB/Broker/Batch/Gateway/Agent의 bounded-cardinality metric과 dashboard/export를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-SLO` | cpf-admin control plane + runtime owner | availability, latency, error, freshness, backlog, recovery SLI/SLO와 error budget, burn-rate alert를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-ALERT` | cpf-admin control plane + runtime owner | dedup, grouping, inhibition, severity, routing, escalation, maintenance suppression와 acknowledgement를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-INCIDENT` | cpf-admin control plane + runtime owner | incident lifecycle, commander/owner, communication, timeline, evidence, action item와 problem linkage를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-RUNBOOK` | cpf-admin control plane + runtime owner | 탐지조건·영향·진단·안전조치·rollback·escalation·검증·종결 기준을 실행 가능한 runbook으로 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-SELF` | cpf-admin control plane + runtime owner | 자동진단/자동복구의 allowlist, rate/attempt limit, circuit stop, approval boundary, rollback와 immutable audit를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-TOPOLOGY` | cpf-admin control plane + runtime owner | service/instance/dependency/domain/owner/database/broker/endpoint 관계를 versioned topology/service catalog로 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-MAINT` | cpf-admin control plane + runtime owner | maintenance, admission block, drain/quiesce, in-flight deadline, health/routing 반영, resume와 audit를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-CONFIG` | cpf-admin control plane + runtime owner | runtime config catalog, schema, encryption, version, staged rollout, approval, dynamic apply, rollback와 drift detection을 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-DRIFT` | cpf-admin control plane + runtime owner | Source/Artifact/Config/DB/Route/Permission/Runtime version의 desired-actual drift를 탐지·차단·복구한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-CAPACITY` | cpf-admin control plane + runtime owner | CPU/memory/thread/connection/queue/storage/DB/Broker 용량과 threshold, trend, forecast, load test 기준을 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-DR` | cpf-admin control plane + runtime owner | RTO/RPO, multi-zone/site, backup/restore, failover/failback, data consistency, runbook와 정기 DR drill을 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |

### 22.141 Generator/EDU/API/Quality/Productization

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `DEVEX-QUICK` | cpf-tools + public artifacts | 신규 개발자가 표준 환경에서 설치→생성→빌드→실행→테스트→디버깅을 문서대로 재현할 수 있는 quick start를 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `DEVEX-CODEGEN` | cpf-tools + public artifacts | OpenAPI/Orval/DB metadata/domain template 등 code generation의 exact input SHA, deterministic output, drift gate와 user-owned 영역 보호를 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `DEVEX-COMMENT` | cpf-tools + public artifacts | Public API/SPI, 주요 Service/Controller, 복구·동시성·보안 로직에 유지보수 가능한 한글 JavaDoc/주석과 설정 설명을 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `DEVEX-LAYER` | Foundation/Starter owners + Domain owners + generator | Controller/Service/Batch 등 주요 실행 계층이 `CPF 공통 → Domain 공통 → 업무 구현` 표준 확장점을 경유하며 Runtime 의존 Base는 Core 밖의 올바른 Owner가 소유한다. DTO/Entity/Repository는 기술 제약에 따라 interface/composition/meta-annotation으로 동등한 확장성을 제공한다. | Generated/handwritten 실제 Consumer 계층, ArchUnit/compile gate, stale base FQCN 0, Native extension Evidence |
| `DEVEX-ANNOTATION` | Foundation/Web/Data/Batch/Messaging/Integration owners | CPF 의미가 있는 meta-annotation과 declarative API로 등록·Context·Validation·Error·Audit·Transaction·Retry 등의 plumbing을 단순화하되 표준 Spring/Jakarta API와 Native Escape를 유지하고 no-op wrapper/annotation pollution을 금지한다. | Annotation consumer matrix, IDE/JavaDoc, boot/AOP/interceptor runtime, misuse negative test, Native API parity Evidence |
| `DEVEX-VALIDATION` | Foundation/Web/Batch/Messaging/Integration + generator | Bean Validation을 기본으로 Body/Query/Path/Header/Method/Batch Parameter/Message/Integration DTO와 CPF 전용 값/교차필드 검증을 일관되게 제공하고 실패를 CPF Error/OpenAPI/Frontend 계약으로 연결한다. | 실제 validation consumer, invalid corpus, header trust 분리, Generator/OpenAPI/Frontend parity Evidence |
| `DEVEX-ERROR` | Core error semantics + endpoint/provider owners | 개발자가 반복 try/catch·ErrorResponse 조립을 하지 않도록 기술중립 Error taxonomy와 endpoint별 mapping을 제공한다. HTTP/Persistence/External/Batch/Message mapping은 각 Owner가 담당하고 secret/stack/provider detail은 외부 응답에 노출하지 않는다. | Web/DB/Integration/Message/Batch error runtime, mapping contract, masking/unknown/retry negative Evidence |
| `DEVEX-LOGGING` | platform-operations/observability + runtime owners | 일반 SLF4J 사용만으로 Context/trace/instance/operation이 구조화 로그에 자동 연결되고 masking, slow/error, integration/message/batch lifecycle을 제공한다. Audit/Performance annotation은 필요한 곳에만 사용하며 전 메서드 log annotation 강제를 금지한다. | 실제 업무/Batch/Message/Integration 로그, MDC leak test, masking, dynamic level, ADM trace lookup Evidence |
| `DEVEX-UTILITY` | base + capability owners | 반복 업무 Utility를 Date/Time/BusinessDate/Money/Decimal/ID/Validation/Text/Collection/JSON/File/Hash/Paging 등 올바른 Owner에 제공하되 OSS 단순 재포장·God Utils·Core utility dump를 금지하고 typed/safe API와 Test를 제공한다. | Utility ownership catalog, duplicate/dead check, boundary/security tests, actual consumer/EDU Evidence |
| `DEVEX-TESTKIT` | cpf-tools/testing/cpf-testkit + capability owners | Context/Security/Tenant/Transaction/BusinessDate/DB/REST/Message/Batch/Logging/Audit/Idempotency/Retry fixture와 assertion을 제공하여 제품 Golden Path를 쉽게 검증하고 실제 Provider runtime test로 연결한다. | Testkit actual consumers, provider runtime bridge, leak/failure/retry/restart assertions, Generated Domain Evidence |
| `ONBOARD-DOMAIN` | cpf-tools + public artifacts | DomainName+SystemCode로 신규 업무 Domain을 충돌 검증 후 생성·DB bootstrap·build/runtime·remove/regenerate할 수 있게 한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `SAMPLE-ACC` | cpf-education / generated reference | 범용 계정/업무 흐름을 과도한 제품 원장 없이 Local/Remote·validation·transaction·error 사용법을 보여주는 선택형 Sample로 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `SAMPLE-MBR` | Generator verification + root generated domains | `member`(MBR)와 `external`(EXS)을 동일 Generator/Template으로 각각 `cpf-member/`, `cpf-external/` Root에 실제 생성·유지한다. 둘 다 필수 Online 회귀, 기본 Public Starter, CUSTOMER_BUSINESS_DB, `<PREFIX>_SAMPLE_TX` 실제 거래, 3단 Base, CPF Runtime Consumer를 갖는다. member는 `modules.batch=true`로 선택형 `batch/` 생성과 Public `cpf-starter-batch` 소비를 검증하고 external은 `modules.batch=false`로 batch 미생성 조합을 검증한다. 이후 모든 Domain은 설정만 바꿔 같은 품질로 생성 가능해야 한다. | fresh generation→member/external normalized parity→sample DB transaction→Online compile/test/runtime→DB3→Batch capability include/exclude 독립 회귀→hardcoding scan→dry-run/diff/regenerate/idempotency/upgrade/remove/restore→user-owned 보호→최종 Root 보존 Evidence |
| `SAMPLE-REF` | cpf-education / generated reference | cpf-education에서 제품 Public API의 정상·오류·경계·복구·권한·운영 사용법을 실제 Runtime으로 교육한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `SAMPLE-BIZADM` | cpf-education / generated reference | BZA 선택형 업무관리/채번/결재 Customization Sample을 기본 활성화 없이 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `SAMPLE-EDU` | cpf-education / generated reference | 교육 시나리오가 장난감 계약을 만들지 않고 실제 Header/API/DB/Event/Batch/Security 표준을 사용한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `API-CONTRACT` | cpf-core API contract + endpoint owner | HTTP/Local/Remote API의 header, auth, success/error, version, idempotency, permission, content type, example과 consumer compatibility를 정의한다. | OpenAPI/Generated Client/Consumer Contract, 정상·오류·경계·호환성·streaming Runtime Evidence |
| `API-PAGING` | cpf-core API contract + endpoint owner | offset page, slice, keyset/signed cursor, sort/filter allowlist, stable ordering, max size와 count 비용 정책을 제공한다. | OpenAPI/Generated Client/Consumer Contract, 정상·오류·경계·호환성·streaming Runtime Evidence |
| `API-ASYNC` | cpf-core API contract + endpoint owner | 202 acceptance, operationId, status/result polling, callback/event, cancellation, idempotency, expiry와 unknown-result를 제공한다. | OpenAPI/Generated Client/Consumer Contract, 정상·오류·경계·호환성·streaming Runtime Evidence |
| `API-FILE` | cpf-core API contract + endpoint owner | multipart, streaming upload/download, range, checksum, content disposition/type, size/virus policy, cancellation와 secure filename을 제공한다. | OpenAPI/Generated Client/Consumer Contract, 정상·오류·경계·호환성·streaming Runtime Evidence |
| `RULE-ARCH` | cpf-tools quality gates | Module/package/dependency/owner/internal API/DB access/dual primary/generated drift 위반을 자동 Architecture Gate로 차단한다. | 정상 저장소 PASS와 의도적 위반 Negative Fixture FAIL Evidence |
| `RULE-SEC` | cpf-tools quality gates | secret/credential/URL/TLS/security header/unsafe API/path/query/log/evidence pattern과 dependency vulnerability를 자동 Gate로 차단한다. | 정상 저장소 PASS와 의도적 위반 Negative Fixture FAIL Evidence |
| `RULE-QUALITY` | cpf-tools quality gates | compile/static analysis/duplication/dead code/dependency lock/license/SBOM/test coverage/marker-only 구현을 자동 Gate로 검증한다. | 정상 저장소 PASS와 의도적 위반 Negative Fixture FAIL Evidence |
| `TEST-UNIT` | repository-wide test ownership | 순수 로직·validation·state transition·error mapping·serialization·security utility를 deterministic unit test로 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-CONTRACT` | repository-wide test ownership | Public API/SPI, Local/Remote, OpenAPI, message schema, DB query, generated client와 published artifact compatibility를 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-RUNTIME` | repository-wide test ownership | 실제 Java25/WAS/DB/Process 환경에서 startup, endpoint, transaction, shutdown, recovery와 resource leak를 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-BROWSER` | repository-wide test ownership | ADM/BZA의 Chromium/Firefox/WebKit, route/deep link/session/permission/error/a11y/keyboard/responsive를 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-BROKER` | repository-wide test ownership | 실제 Kafka·JMS/IBM MQ·RabbitMQ 지원 Matrix에서 ACK, transaction, duplicate, ordering, redelivery/rebalance, retry/DLQ, broker outage와 consumer crash를 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-FAULT` | repository-wide test ownership | DB/network/broker/disk/process/time/response loss를 side-effect 전후에 주입해 idempotency·unknown·recovery·compensation을 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-EVIDENCE` | repository-wide test ownership | 모든 검증의 exact SHA, command, environment, time, exit code, report/artifact hash, sanitized 여부를 schema로 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `REL-BUILD` | cpf-tools release/deploy | fresh clone과 clean cache에서 Java25/Gradle build, LOCAL_DEV/REMOTE/OFFLINE resolution, lock/POM/BOM/source/javadoc/reproducible artifact를 제공한다. | fresh clean build, signed final artifact, install/upgrade/rollback, mixed-version와 final-artifact SBOM Evidence |
| `REL-DEPLOY` | cpf-tools release/deploy | signed artifact, environment/channel binding, install lock, rolling/canary/blue-green, health/smoke, selective rollback와 deployment reconciliation을 제공한다. | fresh clean build, signed final artifact, install/upgrade/rollback, mixed-version와 final-artifact SBOM Evidence |
| `REL-MIG` | cpf-tools release/deploy | 제품/DB/config/API/message/file schema의 install/upgrade/downgrade/forward recovery, compatibility window와 migration guide를 제공한다. | fresh clean build, signed final artifact, install/upgrade/rollback, mixed-version와 final-artifact SBOM Evidence |
| `REL-COMPAT` | cpf-tools release/deploy | semantic version, compatibility range, deprecation, consumer matrix, rolling mixed-version, rollback와 unsupported combination fail-closed를 제공한다. | fresh clean build, signed final artifact, install/upgrade/rollback, mixed-version와 final-artifact SBOM Evidence |
| `DOC-GOV` | cpf-docs + source owner | Final Target, ADR, Requirement Continuity, Current Request, Review, Handover의 역할·정본·폐기·변경 승인 규칙을 제공한다. | Source/API/SQL/Test와 문서의 링크·예제·명령을 현재 exact SHA에서 검증한 Evidence |
| `DOC-PRODUCT` | cpf-docs + source owner | 개발자·운영자·ADM/BZA·Gateway·Batch·설치·Migration·복구 Guide, OpenAPI, JavaDoc가 실제 Source와 일치하게 한다. | Source/API/SQL/Test와 문서의 링크·예제·명령을 현재 exact SHA에서 검증한 Evidence |
| `PROD-EDITION` | product governance | Edition/License/Capability packaging을 기술 Runtime과 분리하고 미결정 정책을 GA 완료처럼 노출하지 않는다. | ADR, capability boundary, packaging/compatibility/security prototype와 명시적 지원 상태 |
| `PROD-MULTITENANT` | product governance | tenant context, data/config/permission/secret/quota/audit isolation과 tenant lifecycle을 선택 기능으로 설계한다. | ADR, capability boundary, packaging/compatibility/security prototype와 명시적 지원 상태 |
| `PROD-PLUGIN` | product governance | 고객/기관 Plugin·Adapter의 SPI, package, signature, compatibility, isolation, lifecycle, permission와 marketplace 후보 정책을 제공한다. | ADR, capability boundary, packaging/compatibility/security prototype와 명시적 지원 상태 |
| `PROD-PACKAGE` | product governance | 산업군·금융권별 capability package의 dependency, install/upgrade, config, license, compatibility와 support boundary를 제공한다. | ADR, capability boundary, packaging/compatibility/security prototype와 명시적 지원 상태 |
| `REQ-GOV` | requirement governance | Requirement ID, owner, priority, acceptance, status, continuity, traceability와 변경 승인 규칙을 영속 정본으로 관리한다. | Continuity Ledger, 양방향 Trace Matrix, 독립 Review와 Count/상태 무결성 Gate |
| `REQ-REVIEW` | requirement governance | 각 작업 전후 Requirement→구현과 구현→Requirement를 독립 검수하고 완료 보고와 실제 Git 차이를 기록한다. | Continuity Ledger, 양방향 Trace Matrix, 독립 Review와 Count/상태 무결성 Gate |
| `REQ-CODEX` | requirement governance | Codex/AI 요청서가 이전 대화 없이 실행 가능하도록 baseline, scope, architecture, acceptance, evidence, 금지조건과 output을 포함한다. | Continuity Ledger, 양방향 Trace Matrix, 독립 Review와 Count/상태 무결성 Gate |
| `REQ-GAP` | requirement governance | 새 상용 필수 요구를 기존 ID와 중복 검사해 intake하고 split/supersede/deprecate 관계와 count 변화를 기록한다. | Continuity Ledger, 양방향 Trace Matrix, 독립 Review와 Count/상태 무결성 Gate |

## 22.42 Core Slimming / Modern Starter Portfolio Currentization

본 절은 기존 `ARCH-STARTER`, `CPF-TXID`, `CPF-HEALTH`, `CPF-LOCK`, `CORE-TESTKIT`,
`SEC-AUTHN`, `SEC-SECRET`, `SEC-CERT`, `SEC-AUDIT`, `EVENT-*`, `AI-OPTIONAL`,
`DOC-GOV`, `REQ-REVIEW`를 대체하지 않는다. 기존 Requirement의 의미를 유지하면서
2026-08-08 `07_16` Currentization 이후 제품 구조에서 새로 명시가 필요한 Capability와 Core Ownership 해석만 보강하고,
기존 Requirement에는 아래 강제 해석을 적용한다.

### 기존 Requirement 강제 해석

- `ARCH-STARTER`: `cpf-core -> cpf-starters/*` 의존은 0이어야 한다. Core는 CPF 전역 Kernel에 필요한 topology-independent Contract/Semantics/Value와 최소 순수 Logic만 소유한다. **Provider-neutral이라는 사실만으로 Core 소유를 허용하지 않는다.** Core Class는 (1) 대부분의 CPF Capability에 공통으로 필요하고, (2) Admin/Batch/Gateway/File/AI 등 특정 Owner 전용이 아니며, (3) Optional Capability를 사용하지 않아도 필요하고, (4) Runtime/Topology/Provider와 독립적이며, (5) 기술 교체 후에도 의미가 유지되고, (6) CPF 자체 Contract/Semantics/Value라는 조건을 충족해야 한다. 조건을 충족하지 못하는 API/SPI/DTO/Port는 해당 Capability/Owner Module이 소유한다. Spring AutoConfiguration, Servlet/Web Runtime, Logging Runtime, Dynamic Log Level/Remote Log 운영, OTel Adapter, Actuator Runtime, JDBC/JPA/MyBatis 구현, 특정 Provider와 일반 개발 편의 Utility를 Core에 적치하지 않는다. `compileOnly`도 Ownership 면죄부가 아니다.
- `CPF-TXID`: transactionId 의미·Context·Generator Contract는 Core에 둘 수 있으나 UUID/ULID/sequence 등 실제 기본 생성 구현, Spring wiring, Servlet/Message/Channel Adapter는 Foundation/Capability/Starter가 소유한다. 최초 신뢰 Entry에서 생성된 동일 transactionId는 Retry/Hop에서도 바꾸지 않는다.
- `CPF-HEALTH`: Core는 Health 의미·Port만 소유하고 Actuator, `HealthIndicator`, `HealthContributor`, Probe, Dependency Check와 Instance Runtime 구현은 Platform Operations Health Capability가 소유한다. Liveness/Readiness/Startup/Drain/DEGRADED/UNKNOWN과 Multi-instance ADM projection을 제공한다.
- `CPF-LOCK`: JDBC/Valkey 등 Provider 구현은 Core 밖에 둔다. 분산 Lock은 fencing token, lease, owner identity, stale-writer 차단, process kill/network partition/multi-instance recovery를 포함한다.
- `ARCH-STARTER/CACHE`: 기존 Valkey 지원은 유지하며 Redis는 별도 공식 Cache Provider Starter로 제공한다. Redis/Valkey는 Spring Data Redis 기반의 공통 provider-neutral runtime을 Data/cache 내부 internal leaf에서 공유하고, 각 Provider Starter는 선택·설정·AutoConfiguration·health/recovery adapter만 소유한다. `cpf-core`/`cpf-starters/common`에 Redis/Valkey Provider 구현을 직접 적치하거나 선택 Runtime을 강제하지 않으며, Valkey 내부 Redis compatibility alias로 Redis 선택을 숨기지 않는다.
- `CORE-TESTKIT`: Runtime 제품 Module이 아니라 공식 Test Support로 제공하며 deterministic clock/id, transaction/security/tenant fixture, DB/Messaging/Batch/Health/Object Storage/GraphQL fixture, failure injection, multi-instance/process-kill harness를 지원한다.
- `DOC-GOV`: 개발·QA·Codex가 세션마다 `*_REV*`, `*_SESSION*`, 날짜별 `*_FINAL*`, Checkpoint, 중복 결과서·Matrix를 만들지 않는다. 동일 목적은 기존 Canonical/Current 파일을 직접 현행화한다. History 보존에 의존하지 말고 삭제 전 필요한 현재 Requirement/Decision/Evidence를 Canonical/Current에 흡수한다.
- `REQ-REVIEW`: QA A와 QA B는 같은 전체 Scope를 각각 100% 독립 전수검수한다. 한쪽 PASS/Evidence 승계, 대표 ID·샘플링 일괄 PASS, Source 직접 확인 없는 Deep Review를 금지하며 A/B 판정을 Requirement ID 단위로 Cross Validation한다.
- `ARCH-BOUNDARY/ARCH-LAYER`: 특정 Owner 전용 Contract는 그 Owner가 소유한다. `admin`, `batch`, `centercut`, `gateway` 등 전용 Command/Query/Operations/DTO/Status/Port를 단지 interface라는 이유로 `cpf-core`에 유지하지 않는다. 해당 Owner가 Core의 범용 Error/Transaction/Context/Security 계약을 소비하는 방향만 허용한다.
- `CPF-LOGDB/CPF-FILELOG/CPF-LOGFAIL/CPF-TRACE/CPF-MASK`: Core는 transaction/trace/context, 민감정보 분류·redaction 의미와 필요한 최소 contract만 보유할 수 있다. Structured/File/Async Logging, Logback/SLF4J 연계, Log Policy Runtime, Dynamic Log Level, Recovery Spool, Remote Log Artifact/Search/Bundle/Download/Node 운영은 Platform Operations/Observability 또는 Security/Masking Capability가 소유한다.
- `CORE-FIXED/CORE-FILE/AI-OPTIONAL`: FixedLength/File/AI처럼 선택 Capability의 Contract/API/SPI는 해당 capability owner가 소유한다. `fixed-length` contract leaf, file capability contract, AI capability가 Core의 범용 contract를 소비하며, 선택 기능을 사용하지 않는 Application의 `cpf-core`에 해당 전용 API가 따라오지 않게 한다.

### 신규 Canonical Requirement

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `FOUNDATION-UTILITY` | pure foundation + foundation convenience starter | Core를 Utility 창고로 사용하지 않는다. `CpfClock/Dates/Decimals/Ids/Json/Lists/Maps/Numbers/Strings/Times/Validation/Values/Files/Hashes/Headers/Pages/Attributes` 등 현재 Core Utility를 전수 분류하여 JDK/Spring 단순 Wrapper는 제거 후보로 전환하고, CPF 고유 정책 가치가 있는 순수 기능만 topology-independent Foundation으로 이동한다. Header/Crypto/File/Paging/TransactionId처럼 Owner가 분명한 기능은 해당 Capability로 이동한다. 업무 개발자는 Application Convenience Starter/Profile을 통해 쉽게 사용하되 Core는 Starter를 참조하지 않는다. | Core Utility class-by-class ownership matrix, Core→Starter 0, simple-wrapper 0, actual consumer, deterministic test, native JDK/OSS escape, relocation duplicate 0 |
| `CACHE-REDIS-PROVIDER` | data/cache provider | 기존 `cache-valkey`를 유지하면서 `cache-redis`를 공식 Optional Provider Starter로 추가한다. Redis/Valkey는 `CpfCache`/invalidation/health/metrics/recovery 의미와 Spring Data Redis protocol runtime을 내부 공통 leaf로 공유하고, `cache=redis` 또는 `cache=valkey`를 Catalog/Profile/Generator에서 명시적으로 선택한다. Redis 연결 정상·장애·재연결, durable invalidation, multi-instance, duplicate/out-of-order/version fence, process-kill/reconcile을 검증하며 미선택 Provider는 0-footprint를 지킨다. | `cache-redis`/`cache-valkey` provider parity, shared-runtime duplicate 0, Catalog/BOM/Metadata/Generator/Profile/Sample/EDU, Redis actual runtime outage/reconnect, Valkey regression, multi-instance/process-kill/reconcile Evidence |
| `SEC-SESSION-DIST` | security/session provider | 기존 JDBC Session을 유지하면서 Multi-instance용 Valkey Distributed Session Provider를 Optional로 제공한다. expiration/renewal/rotation, fixation 방어, concurrent-session control, forced logout/logout propagation, user·tenant index, audit/metrics, provider failure와 0-footprint를 제공한다. | JDBC/Valkey provider parity, 2+ instance login/logout/revoke, provider outage/expiry/rotation test, security negative corpus, optional removal boot evidence |
| `FILE-OBJECT-STORAGE` | file/attachment + object-storage provider | Attachment/Archive/SFTP와 중복 Public API를 만들지 않고 S3-compatible Object Storage를 Provider-neutral하게 제공한다. streaming, multipart, checksum, range, metadata, presigned access, encryption/KMS, tenant isolation, timeout/retry, partial failure, orphan reconcile, retention/lifecycle와 malware-scan hook을 지원한다. | Attachment/Object Storage ownership trace, AWS S3 또는 MinIO reference provider, stream/multipart/failure/reconcile test, security/audit, 0-footprint, actual consumer |
| `EVENT-SCHEMA` | messaging contract governance + generator | Kafka/RabbitMQ/JMS/IBM MQ의 Broker 선택과 독립적인 Event Contract Governance를 제공한다. JSON Schema/Avro/Protobuf version, backward/forward compatibility, breaking-change gate, producer/consumer validation, generated model, schema id/content type와 provider-neutral registry boundary를 제공한다. | compatibility corpus, producer/consumer contract test, breaking-change CI gate, generated model, broker-independent reference, EDU |
| `API-GRAPHQL` | optional web/graphql starter + application service owner | REST/OpenAPI를 기본 API로 유지하면서 Browser/Mobile BFF와 복합 Domain Query를 위한 Optional GraphQL을 제공한다. Resolver는 Service/Application Layer를 재사용하고 Query/Mutation, 필요 시 Subscription, CPF Error/Paging/Cursor/Sort/Search, authN/authZ/field auth/tenant/transactionId/audit/trace, depth/complexity/size/rate-limit, N+1/DataLoader, introspection/GraphiQL prod policy와 Native Spring GraphQL escape를 제공한다. | real BFF consumer, schema/contract test, field-auth negative test, N+1 guard, query limit test, REST service reuse, 0-footprint |
| `API-REALTIME` | web/operations capability | Batch progress, Transaction Timeline, Runtime/Health State와 long-running operation을 위해 Server→Browser 단방향은 SSE를 우선하고 실제 양방향 요구에만 WebSocket을 사용한다. authN/authZ, reconnect/heartbeat, duplicate, slow-consumer/backpressure, rate limit, multi-instance fan-out, graceful shutdown, fallback polling과 typed frontend consumer를 제공한다. | SSE reference consumer, optional WebSocket consumer where justified, reconnect/duplicate/backpressure/multi-instance test, frontend typed consumer, fallback evidence |

### 신규 Capability 채택 경계

- Spring Data JPA는 이미 `cpf-starters/data/persistence-jpa`에 반영된 Optional Provider이므로 재생성하지 않고 `JpaRepository/Pageable/Sort/Specification/@Query/EntityManager`, CPF Paging Adapter, Lock, XA/JTA, DB3, Generator, EDU와 실제 Consumer를 재검수·보강한다.
- OAuth2/JWT/OIDC/SSO, KMS/HSM, Digital Signature, Tamper-evident Audit, AI Optional, XA/JTA/TCC/Inbox/Saga는 `07_15` Source를 기준으로 재검수하며 신규 중복 Starter를 만들지 않는다.
- gRPC는 실제 Product Consumer가 없는 한 이번 Canonical 기본 Portfolio에 추가하지 않는다. Protobuf는 `EVENT-SCHEMA`에서 사용할 수 있다.
- R2DBC/WebFlux persistence는 실제 채택 Requirement가 생길 때까지 강제하지 않는다.
- GraphQL, Distributed Session, Object Storage 등 Optional Capability는 미선택 Application에서 dependency/bean/config/sql/thread/endpoint/background runtime side effect가 0이어야 한다.

## 22.43 Commercial Hardening 40 — Full-Scope Iterative Closure Governance

본 40개는 신규 Canonical Requirement ID 40개를 추가하는 목록이 아니다.
기존 Requirement의 Acceptance Criteria/Defect/Execution 축을 상용 Release 수준으로 닫기 위한 **고정 상위 검수축**이다.
항목을 41개 이상 자동 증식하지 않으며 새 결함은 가장 적절한 기존 항목에 병합한다.
P0/P1/P2는 수행 우선순위일 뿐 Developer GPT 전달물을 분할하는 근거가 아니다.
매 회차는 최신 master 독립검수 결함 + 아래 40대 전체 + §16.3 DX를 하나의 Full-Scope로 수행한다.

| # | Commercial Hardening Axis | 주 Canonical Requirement 연결 |
|---:|---|---|
| 1 | Runtime 장애·복구·UNKNOWN Hardening | `ARCH-MSA`, `TX-E2E`, `TEST-FAULT`, `TEST-RUNTIME` |
| 2 | 다중 인스턴스 / 분리 WAS / MSA 일관성 | `ARCH-MSA`, `CPF-LOCK`, `CPF-HEALTH`, `TEST-RUNTIME` |
| 3 | Transaction / Outbox / Inbox / Idempotency 통합 | `TX-LOCAL`, `TX-INBOX`, `TX-E2E`, `EVENT-OUTBOX`, `TEST-FAULT` |
| 4 | Security / Identity 통합 모델 | `SEC-AUTHN`, `SEC-AUTHZ`, `SEC-APP`, `SEC-AUDIT`, `SEC-SECRET` |
| 5 | 위험 운영조치 승인 / SoD / Break-glass | `SEC-APPROVAL`, `ADM-APPROVAL`, `ADM-AUDIT`, `BZA-APPROVAL` |
| 6 | Starter/API Developer Experience 전수 Audit | `STARTER-DX`, `DEVEX-LAYER`, `DEVEX-QUICK`, `DEVEX-UTILITY` |
| 7 | Public API / SPI / Internal 경계 최종 정리 | `ARCH-BOUNDARY`, `ARCH-LAYER`, `PROD-PLUGIN`, `RULE-ARCH` |
| 8 | Starter Canonical Catalog 단일화 | `ARCH-STARTER`, `STARTER-DX`, `RULE-ARCH` |
| 9 | 고객 실제 개발 표준 흐름 완성 | `DEVEX-LAYER`, `DEVEX-ANNOTATION`, `DEVEX-VALIDATION`, `DEVEX-ERROR`, `DEVEX-LOGGING` |
| 10 | Generator Stateless Lifecycle / DX 완성 | `DEVEX-CODEGEN`, `SAMPLE-MBR`, `SAMPLE-REF`, `SAMPLE-EDU` |
| 11 | Root Build / Convention / Publication 경로 단일화 | `REL-BUILD`, `RULE-ARCH`, `RULE-QUALITY` |
| 12 | Education / Sample 실행체계 완성 | `SAMPLE-EDU`, `DEVEX-TESTKIT`, `DOC-PRODUCT`, `TEST-CONTRACT` |
| 13 | Batch Runtime / Scheduler / Worker / Center-Cut 구조 완성 | `BAT-CORE`, `BAT-JOB`, `BAT-EXECUTOR`, `BAT-SHARED`, `CENTER-RUNNER`, `CENTER-OPS`, `CENTER-UNKNOWN` |
| 14 | cpf-tools / deploy Canonical IA 완성 | `ARCH-LAYER`, `REL-DEPLOY`, `RULE-ARCH` |
| 15 | Repository Garbage / Dead Source / False-Green 제거 | `RULE-QUALITY`, `REQ-REVIEW`, `TEST-EVIDENCE` |
| 16 | Current Evidence / Exact SHA 신뢰성 체계 | `TEST-EVIDENCE`, `REQ-REVIEW`, `REQ-GOV` |
| 17 | Persistence 상용 기본기 강화 | `DB-SQL`, `DB-PERF`, `DEVEX-LAYER`, `TEST-RUNTIME` |
| 18 | Oracle/PostgreSQL/MariaDB DB3 완성 | `DB-MULTI-VENDOR`, `DB-INSTALL`, `DB-MIGRATION`, `DB-ROLLBACK`, `DB-FRESH` |
| 19 | Observability E2E 추적 | `CPF-TRACE`, `DEVEX-LOGGING`, `OPS-METRIC`, `TEST-RUNTIME` |
| 20 | Runtime Health / Readiness / Graceful Drain | `CPF-HEALTH`, `API-REALTIME`, `TEST-RUNTIME` |
| 21 | Cache / Redis / Valkey Hardening | `CACHE-REDIS-PROVIDER`, `CPF-LOCK`, `TEST-FAULT` |
| 22 | Messaging 장애대응 표준화 | `EVENT-BROKER`, `EVENT-OUTBOX`, `EVENT-DLQ`, `EVENT-SCHEMA`, `TEST-BROKER` |
| 23 | Integration 장애대응 표준화 | `ARCH-MSA`, `API-CONTRACT`, `TEST-FAULT` |
| 24 | ADM Commercial Control Plane 완성 | `ADM-AUTH`, `ADM-TX`, `ADM-BATCH`, `ADM-CENTER`, `ADM-LOG`, `ADM-AUDIT`, `ADM-RECOVERY`, `ADM-INCIDENT`, `ADM-UX` |
| 25 | BZA Business Admin 완성 | `BZA-BUSINESS`, `BZA-ORG`, `BZA-APPROVAL`, `BZA-SEQUENCE-SAMPLE` |
| 26 | Common Code / Message / Parameter Runtime화 | `CMN-CODE`, `CMN-MSG`, `CMN-CALENDAR`, `CMN-TEMPLATE`, `CMN-SAMPLE-DB` |
| 27 | Config / Profile / Secret Governance | `OPS-CONFIG`, `SEC-SECRET`, `ARCH-STARTER`, `RULE-SEC` |
| 28 | API / Event / DB Schema Versioning & Compatibility | `API-CONTRACT`, `EVENT-SCHEMA`, `REL-COMPAT`, `DB-MIGRATION` |
| 29 | Event Schema / Contract Governance | `EVENT-SCHEMA`, `TEST-CONTRACT`, `EVENT-BROKER` |
| 30 | Testkit / Contract Test / Fault Injection Harness | `DEVEX-TESTKIT`, `TEST-CONTRACT`, `TEST-FAULT`, `TEST-BROKER` |
| 31 | 성능·확장성 Engineering | `DB-PERF`, `OPS-CAPACITY`, `TEST-RUNTIME`, `RULE-QUALITY` |
| 32 | Upgrade / Rollback / Publication / Supply Chain | `REL-BUILD`, `REL-DEPLOY`, `REL-MIG`, `REL-COMPAT`, `RULE-SEC` |
| 33 | Time / Clock / Timezone / Sequence 표준 | `CMN-CALENDAR`, `BZA-SEQUENCE-SAMPLE`, `DEVEX-TESTKIT` |
| 34 | Resource Exhaustion / Backpressure / Overload Protection | `API-LIMIT`, `OPS-CAPACITY`, `TEST-FAULT`, `TEST-RUNTIME` |
| 35 | Backup / Restore / DR / Rebuildability | `DB-BACKUP`, `OPS-DR`, `REL-MIG`, `TEST-RUNTIME` |
| 36 | Data Privacy / Retention / Masking / Audit Lifecycle | `SEC-PRIVACY`, `SEC-AUDIT`, `CPF-LOGDB`, `DEVEX-LOGGING` |
| 37 | Extension / Plugin / Native Escape Hatch 정책 | `PROD-PLUGIN`, `DEVEX-LAYER`, `ARCH-BOUNDARY` |
| 38 | Cross-platform CLI / Developer Tooling 완성 | `DEVEX-CODEGEN`, `REL-BUILD`, `RULE-QUALITY` |
| 39 | Commercial Education / Onboarding / Troubleshooting | `SAMPLE-EDU`, `DOC-PRODUCT`, `DEVEX-QUICK` |
| 40 | Release Readiness / Commercial Acceptance Closure | `REL-BUILD`, `REL-DEPLOY`, `TEST-EVIDENCE`, `REQ-REVIEW` |

상세 Acceptance/현재 Source Gap/검증법은 `cpf-docs/work/current/CPF_COMMERCIAL_HARDENING_40_CROSSMAP.md`를 Current 실행 문서로 사용한다.

## 23. Legacy Alias Mapping

아래 ID는 검색과 과거 Evidence 연속성만 유지하고 Canonical 완료율에 포함하지 않는다.

| Legacy ID | 현재 Canonical 추적 대상 |
|---|---|
| `FACADE-LOCAL` | `ARCH-MSA + CPF-CALL` |
| `FACADE-REMOTE` | `ARCH-MSA + CPF-CALL` |
| `CMN-ID` | `CPF-TXID + BZA-SEQUENCE-SAMPLE/업무 Domain` |
| `CMN-FILE` | `CORE-FILE` |
| `CMN-FIXED` | `CORE-FIXED` |
| `ADM-COMP` | `ADM-RECOVERY` |
| `CENTER-ADV` | `CENTER-RUNNER + CENTER-PARAM + CENTER-CLAIM + CENTER-RATE + CENTER-REPROCESS + CENTER-UNKNOWN + CENTER-OPS` |
| `API-GATEWAY` | `GWY-ENTRY + GWY-ROUTING + GWY-TRUST + GWY-RESILIENCE + API-CONTRACT` |

## 24. 영구 완료 금지 조건

다음 상태에서는 어떤 Requirement도 `완료`로 처리하지 않는다.

- Dependency, Interface, DTO, Adapter, 화면, Table 또는 Script만 존재
- 실제 Product Consumer가 없음
- OSS와 Legacy가 동시에 Primary
- 일부 Module/화면/Vendor/Topology만 이관
- compile 또는 static Marker Gate만 통과
- package manifest와 lock/generated artifact 불일치
- Local에서만 동작하고 Remote/Multi-instance가 미검증
- 정상 예제만 있고 오류·권한·부분 실패·복구가 없음
- idempotency/fencing/unknown-result가 문자열이나 Column만 존재
- 위험 운영조치의 권한·사유·승인·감사가 없음
- DB/Generator/Vendor/Migration/Rollback 영향 누락
- final artifact가 아닌 Source directory만 SBOM/보안 검사
- 다른 Commit·장비·Artifact의 Evidence를 현재 결과로 사용
- 실행하지 않은 Test를 성공으로 기록
- 민감정보 원문이 Log, DB, Browser, Evidence 또는 운영화면에 존재
- 기존 성공 기능 회귀, Dead Code, Stale Evidence 또는 Repository garbage 잔존
- README/Guide/문서만 변경하고 실제 Source·Runtime이 불일치

## 25. 작업과 검수의 영구 원칙

- 작업 시작 전 Final Target, Continuity Ledger, Current Request, ADR, 최신 master와 실제 Git diff를 확인한다.
- 어떤 Requirement를 해결하는지, 실제 Owner와 Consumer가 누구인지 먼저 결정한다.
- MSA와 동일 JVM, 다중 인스턴스와 부분 실패, 보안·감사·운영·DB·Generator 영향을 함께 검토한다.
- 잘못된 구조를 영향도라는 이유로 무기한 보존하지 않는다. 대체 구현과 Consumer 이관 후 Legacy를 제거한다.
- 구현 가능한 Source·SQL·Test·Script·Guide·Evidence를 관성적으로 추후 작업으로 넘기지 않는다.
- 반복 비용이 큰 Runtime 검증은 통합 계획에 누적할 수 있으나 실행 전에는 `미검증`이다.
- 작업 종료 시 최신 Handover와 Requirement 상태를 갱신하되 README를 작업 일지로 사용하지 않는다.
- 사용자 승인 없이 Commit, Push, Branch, Tag와 PR을 생성하지 않는다.

- 사용자 Repository에서 currentizer/migration/source 변환/move/rename/package rewrite/dynamic source generation을 실행하지 않는다.
- 사용자용 Apply는 `exact SHA 확인 → 완성 Overlay 복사 → 승인된 Root-relative Delete Manifest 삭제 → 검증`만 허용한다.
- Education/Batch/Tools/Deploy 구조 전환은 완성된 최종 Source를 Overlay에 직접 포함한다.
- 중간 종료 시 partial transformed repository가 생길 수 있는 적용 방식은 Package FAIL이다.
- 내부 currentizer가 필요하면 Developer GPT 격리 작업공간에서만 사용하고 ZIP/Repository/사용자 명령에 포함하지 않는다.
- Overlay 적용 후 추가 Source/Package 생성이 필요해야 정상 상태가 되는 Package는 FAIL이다.


## Unified Context / Standard Header / Root Layout 최종 강제 Requirement

### CPF Core Context

Core는 단순 DTO 저장소가 아니라 CPF 실행 전체를 연결하는 기술중립 Kernel Context를 제공한다.

필수 의미:
- transactionId / correlation
- transaction/execution/segment/attempt/lineage
- businessDate 독립 의미
- authenticated subject/actor
- tenant
- deadline/idempotency semantics
- immutable snapshot/scope/access contract

Core Context는 HTTP/JMS/Kafka/Spring Batch/MDC/OTel/Provider Runtime을 알지 않는다.

### Mandatory Fan-out

Core Context 변경은 Web/Gateway/Messaging/Async/**Batch/Center-Cut**/File/Integration/
Saga/Recovery/Reconcile/Security/Observability/ADM/Generator/EDU/Testkit까지 같은 개발 단위에서 전수 영향도 수정한다.

특히 Batch Context/Runtime/Restart/Process Kill/Multi-instance 연결은 무조건 필수다.

### Header

기존 `cpf-docs/api/API_GUIDE.md`의 Header Wire Contract를 정본으로 사용한다.
Header는 Context 자체가 아니며 Transport Adapter가 Core Context와 mapping한다.
Trust/Propagation/Mutation/Direction/Compatibility Policy를 Source와 Test로 구현한다.

### Repository Root — Permanent Policy

사용자 명시 승인 없이 Repository Root에 신규 File/Directory/Module을 만들지 않는다.
이 규칙은 상시 적용한다.

Root 확장 조건:
1. Canonical Architecture 근거
2. 사용자 명시 승인
3. Canonical Root Allowlist 변경
4. Build/Generator/Packaging 영향 검토

Root Allowlist 밖 tracked entry는 Architecture Gate FAIL이다.

Generated Customer Domain은 예외적 임의 Root가 아니라 **Canonical Generator가 관리하는 동적 Project naming 계약**이다. 고객 Project Root naming은 `cpf-<domain>/`이다. CPF 개발 Repository Root에는 사용자 승인된 회귀 Root `cpf-member/`, `cpf-external/`만 허용하며 제3 Domain은 `build/domain-generator/verification/<scenario>/`에서 transient 검증한다. 사람이 임의 Root를 만들어 Generated Domain으로 선언하는 것은 금지한다. Generated Root는 Build/Test Graph에 참여할 수 있으나 CPF Product Publication Allowlist에는 들어가지 않는다.

### Completion

Context Class/Interface/DTO 존재만으로 완료 금지.
실제 Adapter/Consumer/Test/Generator/ADM/Batch 연계와 old source garbage closure까지 완료해야 한다.

## Final Unified Context Field-Level Acceptance

Unified Context Requirement는 이름/DTO 존재가 아니라 아래 field-level 설계와 전체 Consumer 연계가 Acceptance다.

Core Kernel:
Transaction / Execution / Operation / Identity / Tenant.

Owner Context:
Interaction / Gateway / ServiceCall / Message / Async / Batch / CenterCut /
Security / Session / Approval / BusinessOrganization / Integration / File /
Saga / TCC / XA / Recovery 및 현재 Repository에 존재하는 GraphQL/Realtime/Notification/AI.

필수:
- immutable snapshot/scope
- typed component registry
- transport-specific inject/extract
- trust/spoof policy
- deadline/cancellation
- operation-scoped idempotency
- businessDate independent from transactionId
- async capture/restore/clear
- batch restart/process-kill/multi-instance
- message redelivery
- recovery/reconcile lineage
- Generator/EDU/ADM/Testkit parity
- old model garbage closure

한 Context 변경 시 모든 Context/Boundary 영향도 전수 검토가 의무이며 개발 가능한 연계를 후속으로 남길 수 없다.

- 신규 또는 현행화된 Developer-facing API/Annotation/Utility는 문서에만 존재해서는 안 된다. 적용 가능한 모든 Sample, EDU, Reference Domain, Generator Template, Generated Domain, Testkit이 동일 Golden Path를 실제 사용해야 한다.
- 신규 권장 API가 생긴 뒤 기존 예제가 구 방식/직접 plumbing을 계속 사용하면 완료가 아니다. 호환성 전용 예제를 제외하고 권장 예제는 신규 표준으로 전환한다.


---


## Generated Customer Domain Current Target

Current Target은 다음 두 Root Generated Customer Domain을 동일 Domain-neutral Generator로 실제 생성·유지하는 것이다.

```text
cpf-member/   = logical domain member / MBR
cpf-external/ = logical domain external / EXS
```

두 Root는 CPF Product Module/Public Artifact가 아니다. `cpf-` Prefix는 Generated Project naming convention이다.

Generated Project 내부는 개발자가 실제 사용하는 최소 Surface만 허용한다. `online/`은 필수, `batch/`는 `modules.batch=true`일 때만 생성한다. `jobpack/`은 생성하지 않고 공유 `domain/`은 둘 이상의 실제 Runtime Consumer가 공유할 코드가 있을 때만 생성한다. Generated Project 내부 `README.md`, `verification/`, `db/canonical/`, `db/vendors/`, Vendor별 3벌 Source, 빈 Directory는 금지한다.

CPF Repository Root에는 위 두 회귀 Root 외 신규 파일·폴더·Generated Domain Root를 사용자 승인 없이 만들지 않는다. 제3 임의 Domain genericity 검증은 `build/domain-generator/verification/<scenario>/`에서 수행한다.

## Currentization SHA 의미

- `currentization_source_sha`: `e6f2e7a599a948277b118967d0fb5f840f65c114` (`18_19`) — 본 문서 현행화 시 비교 기준으로 사용한 Source.
- `execution_source_sha`: 각 Developer/Codex/QA 세션 시작 시 최신 `origin/master`에서 동적으로 확인한다.
- `verified_sha`: Build/Test/Runtime/Evidence를 실제 실행한 exact SHA다.

`currentization_source_sha`를 이후 세션의 영구적인 "현재 master"로 해석하지 않는다. 완료 판정과 Evidence는 `verified_sha`를 기준으로 한다.

## 통합 개발·검수 Canonical Stewardship — 영구 규칙

현재 마무리 운영에서는 개발 담당이 실제 Source 수정과 1차 개발검수뿐 아니라 **개발요건·Architecture·Current 실행지침의 정합성 현행화까지 같은 변경 단위에서 책임진다.**

정본 수정은 구현을 편하게 만들기 위한 요구 약화가 아니다. 다음 규칙을 동시에 지킨다.

1. 최신 `origin/master` exact SHA와 실제 Source/Catalog/settings를 먼저 확인한다.
2. 상위 Final Target, Architecture, QA Requirement와 충돌 여부를 먼저 검토한다.
3. Source/API/Config/SQL/Generator/Frontend/Test 계약을 변경하면 관련 정본을 같은 작업에서 현행화한다.
4. 정본이 실제 Source보다 stale이면 현재 제품 목표를 보존하는 방향으로 정정하고 근거를 Evidence에 남긴다.
5. Requirement ID 의미를 임의로 삭제·축소하지 않으며 supersession/split은 Continuity에 기록한다.
6. 과거 세션 문서·완료보고·stale SHA를 현행 정본으로 승계하지 않는다.
7. 동일 역할의 Current 문서를 버전명으로 복제하지 않고 **역할별 active canonical 파일 하나**를 직접 현행화한다.
8. 파생 대용량 Dataset은 논리 단일 원장으로 유지하며 parts 파일은 버전 복제본으로 보지 않는다.
9. 구현과 맞추기 위해 Acceptance를 약화하거나 미실행 검증을 PASS로 바꾸는 행위는 False Green이다.

Local/Current 영역에 세션별 Final/Revision/Checkpoint 복제본을 누적하지 않는다. History를 정리하더라도 필요한 현재 정보가 유실되지 않게 Canonical/Current에 먼저 병합한다.

## Windows/Linux Cross-Platform CLI·Script 영구 계약

CPF의 Generator, DB Lifecycle, Verification Gate, Runtime/Deploy, Release/Promotion 및 개발자용 운영 명령은 **PowerShell-only 또는 Bash-only 구현을 허용하지 않는다.** Windows와 Linux는 동일한 기능·옵션·Exit Code·Evidence 의미를 가져야 한다.

### Canonical 실행 모델

사용자에게 노출하는 논리 명령은 가능한 한 다음처럼 OS와 무관하게 통일한다.

```text
cpf <command> [sub-command] [options]
```

예:

```text
cpf domain generate --file cpf-tools/generator/definitions/member/cpf-domain.yaml
cpf domain generate --file cpf-tools/generator/definitions/external/cpf-domain.yaml
cpf domain diff --file cpf-tools/generator/definitions/member/cpf-domain.yaml --output cpf-member
cpf domain regenerate member
cpf db render --vendor oracle
cpf verify generator
cpf verify all
```

구현 원칙:

1. `cpf`의 **명령 해석·검증·Generator/DB/Verification 핵심 로직은 OS-neutral Engine**이 소유한다.
2. CPF가 이미 사용하는 Java Runtime을 기본 Canonical Engine 후보로 삼으며, 기존 Canonical Engine이 있으면 중복 CLI를 만들지 않고 그 Engine을 Facade로 연결한다.
3. 배포/개발자 UX에서는 Windows와 Linux 모두 사용자가 `cpf ...`라는 같은 논리 명령을 사용할 수 있게 한다.
4. Windows launcher와 POSIX launcher는 thin wrapper다. Wrapper에 Generator/DB/검증 비즈니스 로직을 복제하지 않는다.
5. 실제 Distribution은 예를 들어 `bin/cpf`(Linux/POSIX)와 `bin/cpf.bat` 또는 `bin/cpf.cmd`(Windows)를 함께 제공할 수 있다. PATH가 설정된 환경에서는 양쪽 모두 `cpf ...`로 호출한다.
6. Wrapper가 없는 환경에서도 Java 기반 Canonical CLI라면 `java -jar <cpf-cli>.jar ...` 같은 동일 OS-neutral fallback을 제공할 수 있다.
7. PowerShell 7(`pwsh`)을 Linux 필수 전제로 두지 않는다. Bash/WSL/Git Bash를 Windows 필수 전제로 두지도 않는다.
8. 기존 `.ps1`/`.sh`가 필요하면 둘 다 동일 Core Engine을 호출하며 기능 parity를 유지한다.
9. 신규 기능이 한 OS wrapper에만 추가되는 상태를 허용하지 않는다.
10. 기존에 PowerShell-only 또는 Bash-only Tool이 있으면 그대로 완료 처리하지 말고 OS-neutral Core로 이동하거나 공통 CLI에 통합하고 반대 OS entrypoint를 추가한다.
11. 해당 공통 CLI/launcher가 Repository에 없다면 본 Requirement 범위에서 **실제로 구현**한다. 문서에 명령 예시만 추가하고 완료 처리하지 않는다.

### Repository-local / Installed UX

Installed/Distribution 사용자는 Windows/Linux 모두:

```text
cpf domain generate ...
cpf verify ...
```

를 목표 UX로 한다.

Repository-local 실행은 OS launcher 차이만 허용한다.

```text
Windows: .\cpf-tools\runtime\cli\cpf.bat domain generate ...
Linux:   ./cpf-tools/runtime/cli/cpf domain generate ...
```

이 차이는 launcher 파일명/호출 규칙의 차이일 뿐, CLI Grammar와 실제 기능은 동일해야 한다.

### Cross-platform Gate

최소 다음을 Windows와 Linux에서 모두 검증한다.

- CLI help/version
- argument/option parsing
- exit code
- path with spaces
- relative/absolute path
- `/`와 `\` 경로 처리
- UTF-8 및 한글 경로/출력
- LF/CRLF
- temp/home/work directory
- generated file hash/normalized parity
- executable permission이 필요한 POSIX launcher
- Gradle `gradlew` / `gradlew.bat`
- Generator member(MBR) + external(EXS) fresh generate
- compile/test
- dry-run/diff/regenerate
- DB Canonical Renderer Oracle/PostgreSQL/MariaDB
- Verification Gate
- Runtime/Deploy command 중 OS-neutral 대상
- 실패 시 동일 범주의 오류/Exit Code

CI에는 최소 Windows와 Linux 실행 Matrix를 두고, 한 OS만 PASS한 상태를 전체 PASS로 기록하지 않는다.

Shell script 자체가 OS 고유 기능을 수행해야 하는 예외가 있으면 같은 기능의 counterpart 또는 동일 Core Engine을 호출하는 대체 경로를 제공하고, 지원 차이를 문서·Test·Evidence에 명시한다.

### 16.3.16D Local Integrated / Server Optional Topology 최종 계약

CPF의 배치 위치는 업무 Source의 의미가 아니며 환경별 선택이다. Requirement를 실행환경 편의에 맞춰 약화하지 않고 동일 Public API/Context/Result/보안/로그 의미를 모든 topology에서 유지한다.

- 개발자 기본 Local은 `local-integrated`: **하나의 JVM / 하나의 HTTP Port**, Gateway 기본 OFF다.
- Generated Domain의 `online/`과 선택형 `batch/` Component는 각각 Generator가 생성한 `META-INF/cpf/generated-domain.properties`로 Runtime identity를 제공한다. Batch 실행 의미와 운영 Runtime은 `cpf-batch` Owner/Public Starter 계약을 따른다.
- Local 통합모드의 Batch 실행 API는 같은 JVM에 조립하되 Spring Batch Job은 자동 실행하지 않는다. Scheduler/Worker/Center-Cut/Agent 운영 프로세스 분리는 topology 검증이 필요할 때만 선택한다.
- `local-distributed` 또는 독립 Generated Domain 실행에서는 Generator의 stable port를 사용하며, Definition/기존 Generated Domain과 충돌하면 생성·preflight 단계에서 fail-fast한다.
- dev/stg/prod는 `single-node`, `split-online`, `split-batch`, `full-distributed`, `custom` 중 설치환경 니즈에 따라 선택한다. 한 서버에 전부 설치하는 것도 정식 지원 경로이며 Batch 분리를 강제하지 않는다.
- 배포 준비는 각 Module의 `build/libs`를 운영자가 직접 찾지 않게 `build/cpf-distribution/<env>/<topology>/artifacts/`와 `deployment-manifest.json`으로 모은다. Source Repository에는 Build JAR을 영구 보관하지 않는다.
- Windows/Linux/Jenkins/수동 배포는 동일 inventory/topology/distribution contract를 사용한다.
- Service Call뿐 아니라 Code/Message/Parameter/Calendar/Template, Transaction, Cache, Messaging, Integration, Security, Logging/Context, Batch/Async 등 CPF Public Function의 의미가 topology 전환 때문에 달라지면 실패다.

### 16.3.16E Generated Domain 생성·삭제 Integration Point 완결

Generated Domain lifecycle은 Source folder 생성/삭제로 끝나지 않는다.

- 생성 시 Definition 검증 → Online/Batch Source → Local Runtime descriptor → Local integrated classpath → distributed local port → deploy/distribution auto-discovery → Test/Education 계약까지 이어진다.
- 기본 remove는 Generator가 소유하고 hash가 일치하는 파일만 제거하며 사용자 수정 Source/DB 객체는 fail-closed로 보호한다.
- `--purge-definition`은 사용자가 해당 Domain을 더 이상 사용하지 않는다고 명시한 경우에만 Canonical Definition까지 제거한다.
- Domain 제거 후 `settings.gradle` opt-in composite, Local Runtime auto-discovery, distribution auto-discovery, port ownership이 stale entry를 남기지 않아야 한다.
- Windows `remove-domain.ps1`과 OS-neutral `cpf domain remove ... --purge-definition`은 동일 의미를 제공한다.

### 16.3.16F 분산 Call Result 후처리 Golden Path

JTA를 사용하지 않는 MSA/외부 연동 Boundary는 `CpfResult` 4-state를 그대로 처리한다.

- `SUCCESS` → 확정 성공 후처리
- `BUSINESS_FAILURE` → 업무실패 후처리
- `TECHNICAL_FAILURE` → 기술실패/재시도 정책
- `UNKNOWN` → 일반 실패로 합치지 않고 `RecoveryInfo` 기반 재조회·보상·Reconcile·Manual Review

`CpfResult.fold(...)`를 표준 분기 API로 제공하여 개발자가 상태 문자열 비교나 중첩 if/exception으로 동일 패턴을 반복 구현하지 않게 한다.

### 16.3.16G CPF HTTP Header / Transaction Context Developer Contract

온라인 CPF 거래 Header는 Web Runtime이 소유하는 단일 Canonical 계약이며, 업무 개발자가 직접 조립하지 않는다.

- 신뢰된 내부 CPF Domain HTTP hop의 Canonical 6개는 `X-Transaction-Id`, `X-Original-System-Code`, `X-System-Code`, `X-Caller-System-Code`, `X-Target-System-Code`, `X-Target-Operation-Id`다.
- `X-Transaction-Id`와 `X-Original-System-Code`는 최초 거래에서 확정 후 유지하고, current/caller/target system과 target operation은 hop의 실제 Runtime/계약에 따라 Framework가 자동 구성한다.
- `X-Channel-Code`, `X-Original-Channel-Code`, `X-Cpf-Execution-Id`, `X-Cpf-Caller`, `X-Cpf-Target`은 온라인 Canonical 거래 Header로 사용하지 않는다. Batch/Message/Recovery의 execution metadata와 알림·채널 레지스트리 같은 진짜 Channel 업무 개념은 각 Owner의 내부 Context/전용 계약으로 분리한다.
- 외부 최초 ingress는 내부 Canonical 6개를 Client에게 요구하지 않는다. 외부가 System/Caller/Target/Operation 보호 Header를 주장하면 신뢰하지 않고 경계에서 차단하며, transaction/system/operation은 Framework가 신뢰 가능한 Runtime/Handler 정보로 확정한다.
- 내부 수신은 Controller 실행 전에 Canonical 6개 누락·형식·현재 Runtime System·Target System·인증된 Caller·실제 Canonical operationId를 검증한다. 누락/형식은 400, 신뢰 경계 위조는 403, System/Target/Operation protocol mismatch는 409로 처리하고 실패도 transaction/log/ADM correlation에 남긴다.
- 업무 개발자 Public Surface는 `CpfHttpHeaders` 하나로 통일한다. 미등록 Custom Header도 `current()/requireCurrent()/get()/getRequired()/getAll()/containsKey()/names()/asMap()`으로 읽고 안전한 타입 변환을 사용할 수 있으며, 추가 Header는 `set()/add()`로 다룬다. Canonical 6개, Authorization/Proxy/Trace 등 Framework 보호값은 일반 Custom mutation API로 변경할 수 없다.
- 내부 Domain Client는 대상 System/Operation 계약을 기준으로 Canonical 6개와 허용된 Context를 자동 serialize한다. 동일 JVM 호출은 self-HTTP를 만들지 않고 논리 Context를 전달한다.
- 외부기관 outbound에는 CPF 내부 Canonical Header를 기본 전파하지 않고 기관별 Allowlist 계약만 적용한다. Generic `RestClient/WebClient` interceptor가 요청 Header 전체를 복사해서는 안 된다.
- `CpfContexts`는 transactionId/traceId/operationId/originalSystemCode/systemCode/callerSystemCode/targetSystemCode/targetOperationId와 Context capture/restore를 Public API로 제공한다. Web 전용 client/locale 정보는 `CpfWebContexts`, 인증/권한은 `CpfSecurityContext`, Runtime instance/hostname은 `CpfInstanceIdentity`가 각 Owner의 read-only Public Surface로 제공한다. Core Context가 Web/Security/Operations 구현에 역의존하도록 합치지 않는다. Framework가 이미 아는 Runtime/거래 값을 업무 개발자가 UUID, ThreadLocal, MDC, raw Servlet API로 재구성하지 않는다.
- Operation ID는 Annotation/OpenAPI/Generated Client/Domain Client/Header/ADM/Log에서 하나의 Canonical ID를 사용한다. 실행 인스턴스를 매번 식별하는 `executionId`는 별도 의미이며 Canonical API `operationId` 대신 UUID 실행 식별자를 넣지 않는다. Java/API/DB에서 실행 식별자는 `executionId`/`EXECUTION_ID`로 동일하게 표현한다.
- Header Catalog, Policy, Context adapter, Domain call, Gateway, Logging/DB/ADM, Generator/EDU/Test/문서는 동일 계약과 동일 이름을 사용한다. 같은 의미를 `clientAppId/clientId`, `channelCode/systemCode`, `callerService/callerSystemCode`처럼 병존시키지 않는다.

### 16.3.16H Fixed-Length 전문 Starter 실제 사용 계약

고정길이 전문은 Interface/DTO만 제공해서 완료하지 않는다.

- Starter 하나로 `CpfFixedLengthOperations`를 주입받아 `layoutId/version` 기준 `parse/write/logView`를 바로 수행할 수 있어야 한다.
- Parser와 Writer는 각각 독립적으로 교체 가능해야 하며 한쪽 Provider를 확장했다고 다른 기본 구현이 사라지면 안 된다.
- 기본 Parser/Writer는 byte length, charset, padding/alignment, required field, 반복부 count/max, typed field, custom converter, overflow를 검증한다.
- Domain은 Layout Registry/Config로 기관별 전문을 등록하며 Parser 내부 구현을 복제하지 않는다.
- 로그/ADM에는 원문 민감필드를 표시하지 않고 등록된 Layout ID/version으로 파싱한 masked fields/groups를 표시한다.
- Layout이 없거나 version이 맞지 않을 때 offset을 임의 추론해 잘못 파싱하지 않는다.


## 최신 통합 Steering — 2026-08-17 Current-State 경계

이 절은 과거 중간 구현 문구와 충돌할 때 우선하는 현재 요구다.

1. **Operation ID 단일 정본**: `@CpfOnlineTransaction.operationId`, OpenAPI `operationId`, `X-Target-Operation-Id`, Domain Client target operation, ADM 거래관리, Log/Trace의 Operation ID는 하나의 안정 ID를 공유한다. 업무 개발자가 Annotation에 입력하는 필수 Metadata는 `operationId + name + description` 중심이며 운영 허용정책은 Source Annotation이 소유하지 않는다.
2. **Catalog와 Policy Ownership 분리**: Source/Framework는 Operation 사실·Handler/OpenAPI·발견상태·배포 Metadata를 소유한다. YML은 신규 Operation의 최초 Policy Seed만 제공하고, 최초 등록 뒤 enabled/Caller/System·Domain/Operation/Channel/ALL/override/version은 ADM Policy가 최종 정본이다. Source 미발견은 자동 삭제·자동 비활성화가 아니라 발견상태 변경으로 표현한다.
3. **호출 통제와 장애 기본값**: 등록·활성 Caller 확인 후 System/Domain 1차, Operation 2차, 필요한 거래만 Channel 3차 통제를 Controller invocation 전에 적용한다. Policy Store 장애는 유효 LKG와 maxStale 범위에서만 허용하고 LKG 부재·만료 시 fail-close한다. wildcard local fallback으로 호출을 허용하지 않는다.
4. **Runtime Identity**: `instanceId`는 명시 `cpf.runtime.instance-id`, 환경변수 `CPF_RUNTIME_INSTANCE_ID`, 실제 Runtime Hostname 순으로 기동 시 한 번 확정한다. `local/dev/test/prod`, localhost, Domain명 등을 합성 fallback으로 사용하지 않는다.
5. **관리 Application 경계**: ADM/BZA/Gateway는 업무 Domain Online Transaction Runtime이 아니다. 자체 관리 API에는 `@CpfOnlineTransaction`이나 거래 Header 6개를 강제하지 않는다. 각 Owner Module/Public Starter/API를 사용하고 `cpf-core` internal 구현에 직접 결합하지 않는다. 실제 업무 Domain Operation을 호출하는 outbound 경계부터 CPF Domain Client가 거래 Context를 생성·전파한다.
6. **Generated Domain IA**: `cpf-<domain>/online/`은 필수, `modules.batch=true`이면 `cpf-<domain>/batch/`를 선택 생성한다. Batch 실행 계약 Owner는 `cpf-batch`이며 Public Starter를 소비한다. 공유 `domain/`은 둘 이상의 Runtime에서 실제 공유 Consumer가 있을 때만 생성한다.
7. **EDU Canonical**: `cpf-education/src/main/java/com/cpf/education/online` 20개와 `.../batch` 15개, 총 35개만 Canonical 업무 예제로 유지한다. ADM/BZA/Gateway/OPS/Legacy/Compatibility/Micro Sample 체계는 병행 유지하지 않는다. EDU는 최신 Public API/Golden Path를 사용하고 Internal/raw API 우회를 두지 않는다.
8. **OSS/Spring Naming**: Spring/공식 OSS를 감싸거나 확장하는 CPF 공개 타입은 `Cpf + 공식 타입명`을 사용하고 메서드명은 공식 API 이름을 따른다. 동일 역할 Alias를 병행하지 않으며 Spring/OSS의 의미·기본값·예외 규칙을 CPF가 임의 변경하지 않는다.
