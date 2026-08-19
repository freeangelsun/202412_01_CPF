# CPF Canonical Path and Role Map

- Currentization source/basis SHA: `e6f2e7a599a948277b118967d0fb5f840f65c114` (`18_19`)
- 본 문서는 최신 Source와 Target을 함께 가리키는 Current-State Path Map이다. 이미 제거/이관된 경로를 다시 생성하도록 지시하지 않는다.

## 1. Canonical 문서 위치

| 역할 | 정본 위치 |
|---|---|
| 최상위 목표/Target | `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` |
| 정책 Supersession/연속성 | `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md` |
| Root/Module Surface | `cpf-docs/governance/CPF_REPOSITORY_SURFACE_INDEX.md` |
| Starter Architecture | `cpf-docs/governance/CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md` |
| Generated Domain | `cpf-docs/governance/CPF_GENERATED_DOMAIN_LIFECYCLE_POLICY.md` |
| Architecture Guide | `cpf-docs/architecture/ARCHITECTURE_GUIDE.md` |
| DB Vendor/Operations | `cpf-docs/architecture/CENTRAL_DB_VENDOR_PACK_GUIDE.md`, `cpf-docs/operations/DB_OPERATIONS_GUIDE.md` |
| Developer/Generator/EDU | `cpf-docs/development/DEVELOPER_GUIDE.md`, `GENERATOR_GUIDE.md`, `EDU_GUIDE.md` |
| 현재 개발 요청 | `cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md` |
| Developer GPT 지침 | `cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md` |
| Finding/Status | `cpf-docs/work/CPF_SOURCE_FINDINGS.csv`, `REQUIREMENT_STATUS.csv` |
| Evidence | `cpf-docs/work/TEST_AND_EVIDENCE.md` |
| Delete/Hygiene | `cpf-docs/work/CPF_DELETE_MANIFEST.csv`, `CPF_DELETE_ONE_LINE.ps1.txt` |


## 2.0 Optional Surface Role Rule

`cpf-biz-admin`은 Generator Output이 아니라 CPF가 미리 제공하는 Optional Business Domain이다. 폴더 부재가 Root Build/Publication/Installer/Verifier와 필수 Runtime을 깨뜨리지 않아야 한다. `cpf-biz-channel`과 `cpf-biz-frontend`도 선택형이며 외부 Channel/Reference 역할만 소유한다. 모든 optional/user-selectable Surface는 `cpf-tools/governance/cpf-optional-surface-policy.json`과 동일한 physical-removal 계약을 따른다.

Source 구조는 기능 Owner를 우선하고 기능 아래 필요한 역할 package를 둔다. Backend/Frontend 모두 파일 크기만으로 기계 분리하거나 거대 통파일/모호한 Helper·Util 계층을 만들지 않는다.

## 2. Target Source Ownership

| 기능 | Canonical Owner/Path |
|---|---|
| 최소 Kernel Context/Error/Transaction 의미 | `cpf-core/src/main/java/com/cpf/core/api/**` |
| Runtime Context 설치/실행 Factory | `cpf-starters/base/**` 및 각 Capability Adapter |
| Base Service/Logging/Validation/DX | `cpf-starters/base/**` |
| Common Code/Parameter/Message/Calendar/Template | `cpf-starters/common/**` |
| Controller/Web Context/Web Error Mapping | `cpf-starters/web/**` |
| DTO/Persistence/Repository/DAO | `cpf-starters/data/**` |
| Redis/Valkey/Caffeine | `cpf-starters/data/cache/**` |
| Messaging | `cpf-starters/messaging/**` |
| 외부연계/Resilience/GraphQL/Realtime | `cpf-starters/integration/**` |
| Security/OIDC/Session | `cpf-starters/security/**` |
| Health/Audit/Observability/Runtime Control | `cpf-starters/platform-operations/**` |
| Batch Runtime | `cpf-batch/**` |
| Gateway Runtime | `cpf-gateway/**` |
| Platform Admin | `cpf-admin/**` |
| Optional Prebuilt Business Administration Domain | `cpf-biz-admin/**` |
| External DB-less BZA Channel | `cpf-biz-channel/**` |
| External BZA Reference Frontend | `cpf-biz-frontend/**` |
| Generator Contract/Template | `cpf-tools/generator/contracts/**`, `cpf-tools/generator/templates/**` |
| Generated Customer Domain — Member | `cpf-member/**` — Root-level 실제 Generator Output, logical domain member/MBR, Online 회귀, minimal generated surface, final deliverable 유지, CPF Product/Public Artifact 아님. Batch는 초기 프로젝트 구성의 별도 `cpf-starter-batch` Capability |
| Generated Customer Domain — External | `cpf-external/**` — Root-level 실제 Generator Output, logical domain external/EXS, Online 회귀, minimal generated surface, final deliverable 유지, CPF Product/Public Artifact 아님. Batch는 초기 프로젝트 구성의 별도 `cpf-starter-batch` Capability |
| Generic Generated Customer Domain | `cpf-<domain>/**` — Customer Project naming contract. CPF repository에서는 retained 두 Root 외에는 transient verification에서만 생성 |
| Transient Generated Genericity Output | `build/domain-generator/verification/**` — 제3 임의 Domain Git 비추적 Build Output |
| Education/EDU | `cpf-education/**` |

Generated Customer Domain Root는 CPF Product Root와 역할이 다르다. `cpf-member/`와 `cpf-external/`은 현재 공식 Root-level Generator 회귀 Domain이며 Public BOM/Publication/Production Deploy Product Inventory에서 제외한다. 동일 물리 이름을 CPF Product Module로 등록하지 않는다.


## 2.1 Controller / Service / DAO 3단 Golden Path

| 계층 | 1단 CPF Framework Base | 2단 Domain Common Base | 3단 Business |
|---|---|---|---|
| Controller | `CpfBaseController` — `cpf-starters/web` | `DomainBaseController` — 해당 Domain | `BusinessController` |
| Service | `CpfBaseService` — `cpf-starters/base` | `DomainBaseService` — 해당 Domain | `BusinessService` |
| DAO | `CpfBaseDao` — `cpf-starters/data` | `DomainBaseDao` — 해당 Domain | `BusinessDao` |

1단/2단은 `abstract class`, 3단만 concrete class다. 1단/2단은 빈 계층이 아니며 실제 공통 기능/정책/Hook을 제공한다.
동일 의미 `Cpf*Extension`은 Canonical `CpfBase*`로 통합하고 4단 Compatibility Wrapper를 만들지 않는다.


### Root Generated Customer Domain 경계

Canonical Customer Generated Project Root:

```text
<root>/cpf-<domain>/
```

Current retained verification roots:

```text
cpf-member/    # logical domain member / MBR
cpf-external/  # logical domain external / EXS
```

Role:

```text
Generator Output / Customer Business Base Project / Verification Canary / Final Deliverable Source
```

Not role:

```text
CPF Product Module
Public Starter
CPF Maven Publication Artifact
Generator Template Source
```

각 Root는 실제 선택 capability Source와 Build 파일만 가진다. `cpf-domain.yaml`, lock, ownership/manifest는 Customer Project에 영구 저장하지 않으며 Framework definition 또는 명시 입력과 transient `build/domain-generator/verification/**`가 lifecycle 상태를 소유한다. lifecycle 검증에서 일시 삭제할 수 있으나 종료 전 Canonical Generator로 재생성한다.


## 3. Target DB Roles

```text
CPF_PLATFORM_DB (default physical target: cpfDB)
  CPF_* / CMN_* / ADM_* / BAT_* / GW_* / SEC_* / OPS_*

BZA_DB (bzaDB)
  BZA_*

CUSTOMER_BUSINESS_DB
  MBR_* / ACC_* / PRD_* / ...
```

`cmnDB`, `admDB`, `batDB`는 Target Physical DB가 아니다. `refDB`는 Legacy Production Target으로 금지하고 Education/Test Fixture로만 한정한다. `eduDB` 같은 Education 전용 Production DB Role을 만들지 않는다. Business Transaction과 Local Atomicity가 필요한 Outbox/Inbox/Idempotency는 Customer Business DB에 둔다.

## 4. 시작 규칙

새 Developer/Codex/QA 세션은 `CPF_DOCUMENT_CANONICAL_INDEX.md` → `CPF_FINAL_TARGET_REQUIREMENTS.md` → `CPF_CURRENT_WORK_REQUEST.md` → 역할별 실행지침 순서로 읽고 최신 `origin/master` exact SHA를 다시 확인한다. 과거 Handover/세션 지침을 먼저 승계하지 않는다.


## Canonical CLI 실행 경계

| Role | Canonical 논리 Surface |
|---|---|
| 공통 CPF CLI | `cpf <command> ...` |
| POSIX launcher | `cpf-tools/runtime/cli/cpf` — repository-local thin wrapper |
| Windows launcher | `cpf-tools/runtime/cli/cpf.bat` — repository-local thin wrapper |
| Canonical Core | OS-neutral CLI/Engine. Shell wrapper에 핵심 로직을 두지 않음 |
| Generator | `cpf domain ...` |
| DB | `cpf db ...` |
| Verification | `cpf verify ...` |

새 top-level `scripts`/`cli` Root를 임의 추가하지 않는다. 기존 `cpf-tools` Canonical IA 안에서 실제 Owner를 정하고, 중복 Engine/Wrapper를 만들지 않는다. 기존 PowerShell-only/Bash-only Tool은 Owner별 OS-neutral Core 또는 공통 CLI로 통합한다.





### Generated Customer Project 내부 IA

```text
cpf-<domain>/
├─ online/       # 선택 시
├─ batch/        # 선택 시
├─ domain/       # 실제 다중 Consumer 공유 시만
└─ contract/     # 실제 독립 Contract Consumer 존재 시만
```

Generated Project 내부 `README.md`, `verification/`, `db/canonical/`, `db/vendors/`, Vendor별 3벌 Source, `<domain>-api`, `<domain>-common`, 빈 Directory는 Canonical Surface가 아니다. 검증·DB Renderer 산출물은 CPF Tooling/build owner가 관리한다.

## Generated Customer Domain Current Target

Current Target은 다음 두 Root Generated Customer Domain을 동일 Domain-neutral Generator로 실제 생성·유지하는 것이다.

```text
cpf-member/   = logical domain member / MBR
cpf-external/ = logical domain external / EXS
```

둘은 CPF Product Module/Public Artifact가 아니며, 동일 Canonical Schema·Naming Strategy·Generator Engine·Template으로 생성한다. `CUSTOMER_BUSINESS_DB`, API+Batch, Sample Transaction, DB3, Build/Test를 함께 검증하고 최종 결과물에 포함한다.

Legacy single-canary/transient-only 정책은 Current Target이 아니다. 단, 제3 임의 Domain은 genericity 추가 검증용 transient output으로 생성 후 cleanup할 수 있다.

## Currentization SHA 의미

- `currentization_source_sha`: `e6f2e7a599a948277b118967d0fb5f840f65c114` (`18_19`) — 본 문서 현행화 시 비교 기준으로 사용한 Source.
- `execution_source_sha`: 각 Developer/Codex/QA 세션 시작 시 최신 `origin/master`에서 동적으로 확인한다.
- `verified_sha`: Build/Test/Runtime/Evidence를 실제 실행한 exact SHA다.

`currentization_source_sha`를 이후 세션의 영구적인 "현재 master"로 해석하지 않는다. 완료 판정과 Evidence는 `verified_sha`를 기준으로 한다.
