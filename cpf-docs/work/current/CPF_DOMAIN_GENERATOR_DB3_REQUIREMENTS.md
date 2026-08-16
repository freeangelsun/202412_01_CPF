# CPF Domain Generator · Input Contract · Sample Transaction · DB3 상세 개발요건

> Currentization source/basis: 아래 Canonical Schema·Engine·중앙 DB3 Template·Runtime Dialect Pack. 실행 완료 판정은 별도 exact-SHA Evidence에서만 한다.
> Canonical 입력 Schema: `cpf-tools/generator/contracts/cpf-domain.schema.json`; OS-neutral Engine: `cpf-tools/generator/engine/cpf_domain_generator.py`  
> 다음 실행 시 최신 master에서 다시 읽고 Drift를 먼저 확인한다.

## 1. 목표

고객 개발자가 Domain을 만들기 전에 필요한 정보를 한 번 명확하게 입력하면:

```text
입력/검증
→ Domain Skeleton
→ API/Batch Module
→ Starter Dependencies
→ Config/Profile
→ Canonical DB
→ Oracle/PostgreSQL/MariaDB
→ Sample Transaction
→ Test
→ OpenAPI
→ README/Guide
→ Transient Lifecycle State
```

가 동일 Canonical Generator에서 생성되어야 한다.

`member(MBR)`와 `external(EXS)`은 같은 Engine/Template의 정식 회귀 Domain이다.
둘 중 하나만 유지하면 Generator 완료가 아니다.

## 2. Canonical Input Contract / Drift Closure

실제 Generator가 읽는 Canonical 입력은 `cpf-tools/generator/contracts/cpf-domain.schema.json`이다. `domain-metadata.schema.json`의 과거 22-property 모델을 Current 정본으로 사용하지 않는다.

강제:
- Canonical user input schema는 **exactly one**이어야 한다.
- `domain-metadata.schema.json`, `generated-domain-standard-contract.json` 등 competing contract는 모든 Consumer를 canonical contract로 전환한 후 stale-reference 0을 증명하고 Delete Manifest 대상으로 분류한다.
- logical `domain.name=member`와 physical `projectRoot=cpf-member/`를 분리한다. user input에 `cpf-member` 같은 prefixed logical name을 허용해 double-prefix/ownership 혼동을 만들지 않는다.
- canonical YAML은 개발자가 이해해야 하는 선택값만 노출한다. Generator 내부 resolved state/hash는 Generated Project에 저장하지 않고 메모리/`build/domain-generator/verification/**` transient owner가 소유한다.
- Schema validation과 Engine validation은 동일해야 한다. 현재 `securityProfile=oidc`처럼 JSON Schema는 허용하지만 Engine은 거부하는 조합을 제거한다.
- Preflight는 파일 쓰기 전에 domain/project/path/package/systemCode/tablePrefix/vendor/provider/profile/capability/service/endpoint/secret/internal-starter/duplicate 충돌을 전부 거부한다.

### 2.1 입력 Surface

최소 개발자 입력:
- domain logical name, systemCode, package/base package
- Customer Business DB role + tablePrefix
- preset
- Generated Domain은 Online 업무 Source만 생성한다. Batch 사용 여부는 초기 프로젝트 구성에서 `cpf-starter-batch` Capability를 별도 선택한다.
- persistence provider
- outbound HTTP/resilience
- cache/messaging/object storage/security/OIDC 선택
- sample transaction
- 서비스 등록/endpoint/protocol/basePath 등 실제 생성 Consumer가 존재하는 옵션

옵션은 **실제 Generated Source/Config/Consumer에 사용될 때만** 노출한다. Consumer 없는 metadata field를 늘려 Schema만 복잡하게 만들지 않는다.

### 2.2 Stateless Lifecycle / Zero Project Metadata

Generated Customer Project에는 lifecycle metadata를 영구 저장하지 않는다.

```text
cpf-<domain>/
└─ online/      # Generated Online 업무 Source

Batch는 Generated Domain 산출물이 아니며 초기 프로젝트 구성에서 `cpf-starter-batch`를 별도 선택한다.
```

Fresh regression input:
- `cpf-tools/generator/definitions/member/cpf-domain.yaml`
- `cpf-tools/generator/definitions/external/cpf-domain.yaml`

임의 고객 Domain은 explicit `--file`을 사용한다. `diff/regenerate/upgrade/remove/restore`는 input + current Template에서 expected generated seed를 계산하고 사용자 변경이 있으면 fail-closed한다. transient manifest/hash/DB3/Evidence는 `build/domain-generator/verification/**`가 소유한다.

Generated Project 금지:
- `.cpf/**`
- root `cpf-domain.yaml` / lock / ownership / manifest
- README / verification / DB renderer/vendor tree
- 선택하지 않은 capability / empty dirs

## 3. Public Generator Input — 개발자가 실제 선택할 것만

사용자 입력 Surface는 **필요 최소값 + 합리적 Default + 필요한 경우 Override** 원칙을 따른다. Framework가 결정적으로 계산할 수 있는 값을 사용자에게 중복 입력시키지 않는다.

### 기본 입력

```yaml
domain:
  name: member
  systemCode: MBR
  # packageName 생략 → member를 Java package root로 사용

database:
  tablePrefix: MBR                  # systemCode와 같으면 default derivation 가능

preset: standard-enterprise

modules:
  online: true

features:
  persistence: mybatis
  httpClient: true
  resilience: true
  cache: none
  messaging: none
  objectStorage: none
  securityProfile: resource-server

generation:
  sampleTransaction: true
```

### 사용자에게 기본 노출하지 않을 값

다음은 별도 실제 Consumer/Override 필요성이 증명되지 않으면 Public YAML 입력으로 요구하지 않는다.

- `projectName` → `cpf-<domain>`으로 derive.
- Module/physical root → `domain.name`에서 `cpf-<domain>`으로 derive.
- DB role → Generated Customer Domain은 `CUSTOMER_BUSINESS_DB`로 고정.
- DB Vendor → Domain Source는 vendor-neutral. `cpf db render --file <definition> --vendor oracle|postgresql|mariadb`의 실행 선택값.
- Platform/Template/BOM version → Framework Catalog/BOM/Generator가 소유.
- dependency model/internal starter coordinate → Generator/Catalog가 소유.
- schema/DB account/credential/hostname/URL → Deployment/Runtime Config/Secret owner.
- lifecycle lock/ownership/manifest → Customer Project에 저장하지 않음.
- UI/BZA menu/Service registration/Gateway binding 같은 옵션 → 해당 Capability를 Generator가 실제 생성하고 Consumer가 있을 때만 optional nested contract로 추가.

**DB Password/Secret/API Key/Token/실제 Credential을 `cpf-domain.yaml`에 저장하지 않는다.**

### Optional Capability 입력

Preset이 기본값을 제공하고, 고객이 바꿀 필요가 있을 때만 명시한다.

- persistence: none/jdbc/mybatis/jpa
- cache: none/caffeine/redis/valkey
- messaging: none/kafka/rabbitmq/jms/ibm-mq
- objectStorage: none/s3
- security profile / OIDC
- outbound HTTP/resilience
- online (항상 true)
- Batch는 Public YAML 입력이 아니며 초기 프로젝트 구성의 별도 Capability 선택
- sampleTransaction

현재 공통 Minimal Sample Transaction은 중앙 Vendor Mapper Pack과 1:1로 연결되는 `persistence=mybatis` Golden Path이다. `jdbc`/`jpa` 선택은 `sampleTransaction=false`인 고객 확장 Skeleton에서 허용하며, 구현되지 않은 Provider×Sample 조합은 Preflight에서 부분 파일 없이 거부한다.

Public Starter/Profile 선택을 coordinate 문자열로 직접 입력받지 않는다. Generator가 latest Canonical Starter Catalog의 `visibility=public` Surface로 조립한다. Internal leaf 선택은 입력 단계에서 허용하지 않는다.

## 4. Write-Before Preflight Validation

파일을 쓰기 전에 최소 다음을 한 번에 검증하고 전체 오류를 수집한다.

- domain logical name 형식과 `cpf-` prefixed logical name 금지.
- project root `cpf-<domain>` derivation/path collision.
- systemCode/package/tablePrefix format 및 uniqueness.
- canonical definitions + explicit `--file` 간 identity collision.
- preset/feature enum과 상호의존.
- provider exactly-one / incompatible provider combination.
- public Starter composition 가능 여부; Internal-only leaf 직접선택 금지.
- DB render vendor는 Oracle/PostgreSQL/MariaDB만.
- plaintext Secret/credential 금지.
- 실제 optional integration/service endpoint contract가 존재할 때 protocol/path/service-id collision.
- 기존 output에 사용자 변경이 있을 때 overwrite 금지.
- Root Freeze / output root escape / protected path 위반.

Invalid input에서는 **partial Generated file 0**이어야 한다. 여러 독립 validation 오류는 한 번에 반환한다.

## 5. 입력 방식

현재 Canonical CLI:

```text
cpf domain validate --file <definition> [--output <root>]
cpf domain dry-run --file <definition> [--output <root>]
cpf domain diff --file <definition> [--output <root>]
cpf domain generate --file <definition> [--output <root>]
cpf domain add --file <definition> [--output <root>]
cpf domain regenerate <domain> [--file <definition>] [--output <root>]
cpf domain upgrade <domain> [--file <definition>] [--output <root>]
cpf domain remove <domain> [--file <definition>] [--output <root>] [--apply]
cpf domain restore --file <definition> [--output <root>]
cpf domain generate-all [--definitions-root <dir>] [--output-root <dir>]
```

Windows/Linux에서 동일 의미를 제공한다.
PowerShell/Bash Wrapper는 Thin Adapter이고 OS-neutral Engine/Contract가 정본이어야 한다.

## 6. Generated Customer Project 구조

Generated Project Root naming은 `cpf-<domain>/`으로 고정한다. `domainName`은 논리 업무명이며 `projectName`/물리 Root와 분리한다.

```text
logical domainName = member
projectName        = cpf-member
projectRoot        = cpf-member/
packageName        = member (기본 파생값)
tablePrefix        = MBR
```

Generated Project는 개발자가 실제 수정·사용할 Surface만 생성한다.

```text
cpf-<domain>/
└─ online/               # Generated Online 업무 Source

`batch/`, `jobpack/`, 별도 공유 `domain/`은 생성하지 않는다. Batch가 필요한 프로젝트는 초기 구성에서 `cpf-starter-batch`를 선택한다.
```

공식 회귀:

- `cpf-member/` / logical `member` / MBR / `member`
- `cpf-external/` / logical `external` / EXS / `external`

내부 물리 Directory는 역할명만 사용한다. `member-online`, `member-batch`, `cpf-member-api`, `member-common`처럼 Root Domain명을 반복하지 않는다. Generated Runtime 구현 Module은 `api`가 아니라 `online/`으로 명명한다. Batch Runtime은 Generated Domain 밖의 별도 Framework Capability다.

현재 Generator는 Online 단일 Consumer만 생성하므로 업무 Model/Application/Persistence도 `online/` 내부에 둔다. 복수 Generated Consumer Requirement가 승인되기 전에는 별도 `domain/` 공통 Module을 만들지 않는다.

기본 생성 금지:

- `README.md`
- `verification/`
- `db/canonical/`
- `db/vendors/`
- `db/oracle|postgresql|mariadb/`
- 선택하지 않은 capability/빈 Directory

Generator 검증 결과는 `build/domain-generator/verification/<scenario>/`에 둔다. DB Canonical/Renderer/Vendor3는 CPF Tooling 내부가 소유하며 Generated Project Source Tree에 노출하지 않는다. 고객 개발자가 직접 관리해야 하는 DB Extension Surface가 실제 Requirement로 생길 때만 최소 계약을 별도 정의한다.

두 회귀 Domain 모두 동일 Template/Engine에서 재생성 가능해야 한다.

## 6A. Current Source Acceptance

1. Stateless minimal IA를 유지한다. `.cpf/**`, root lifecycle yaml/lock/ownership/manifest를 재도입하지 않는다.
2. `cpf-domain.schema.json`과 Python Engine validator의 enum/combination을 one-source parity로 유지한다. `securityProfile=oidc`를 포함한 허용·거부 조합은 Schema와 Engine이 동일하게 해석해야 한다.
3. `domain validate`는 `verify_generated`가 아닌 **write-before input preflight**를 호출해야 하며 Invalid input이면 partial file 0을 유지한다.
4. `regenerate`, `upgrade`, `remove`, `restore`는 각각의 version/diff/user-owned protection/restore semantics와 transient generation-state 대사를 fail-closed로 유지한다.
5. Stateless uniqueness/collision은 Framework canonical definitions뿐 아니라 explicit user `--file`, output `cpf-<domain>` path, systemCode/package/tablePrefix collision도 안전하게 검증한다. 이를 위해 Project-local metadata를 다시 만들지 않는다.
6. `domain-metadata.schema.json`, `generated-domain-standard-contract.json` 등 competing stale contract Consumer를 canonical로 전환하고 reference 0 후 Delete Manifest 대상으로 분류한다.
7. member/external 전용 분기 없이 동일 Engine/Template/Schema로 생성하고 normalized parity를 검증한다.
8. Generated direct dependency는 latest Starter Catalog `visibility=public`만 허용한다. Internal integration leaf는 Public profile composition이 소비한다.

## 7. Generated Source Layer

최소 계층:

```text
CpfBaseController → DomainBaseController → Business Controller
CpfBaseService    → DomainBaseService    → Business Service
CpfBaseDao        → DomainBaseDao        → Business DAO
```

Domain Base는 실질적 재사용 로직을 가진다.
같은 역할의 Compatibility Wrapper를 여러 단계 쌓지 않는다.

## 8. Sample Transaction Canonical Contract

현재 Metadata Schema가 Sample Transaction에 요구하는 모델을 최신 Source 기준으로 전수 확인한다.

`minimalTransactionContract`:
- Business sample table 1개.
- Idempotency support ledger 1개.
- 총 2개 Table.
- 추가 Table은 기본 계약에서 허용하지 않음.

현재 Sample Business Column Contract는 14개:
- sample_item_id
- sample_key
- item_name
- status_code
- version_no
- idempotency_key
- transaction_id
- transaction_sequence
- transaction_at
- deleted_yn
- created_by
- created_at
- updated_by
- updated_at

현재 Operations Contract는 22개:
- create
- read
- update
- delete
- search
- offset-page
- slice
- cursor
- validation
- transaction-commit
- transaction-rollback
- optimistic-lock
- duplicate
- local-call
- remote-call
- standard-header
- transaction-id
- error-mapping
- idempotency
- audit
- masking
- framework-edu

다음 개발에서는 Metadata 선언만 보지 않고 생성된 Controller/Service/DAO/SQL/Test에서 전부 증명한다.

## 9. 사용자 요청의 Sample 거래 표준

현재 정본과 최신 Source를 먼저 대조하되 고객에게 이해하기 쉬운 Sample 거래는 다음 흐름을 실제 제공한다.

```text
POST Create
→ INSERT
→ GET Detail
→ Search/Page
→ PUT Update
→ Optimistic Lock
→ Idempotent Replay
→ Duplicate Conflict
→ Commit
→ Forced Rollback
→ Error Mapping
→ TransactionId/Audit
```

`cpf-member`와 `cpf-external` 양쪽에서 logical Domain/SystemCode/Package/TablePrefix만 달라지고 의미론과 최소 Surface 규칙은 동일해야 한다.

## 10. DB Vendor 관리

정본과 선택 Vendor Runtime 조립:

```text
cpf-tools/db/canonical/generated-domain-schema.json
        ↓ cpf-tools/db/render_generated_domain_template.py
cpf-tools/db/generated/domain-template/{oracle|postgresql|mariadb}/
  install/ seed/ migration/ rollback/ verify/

cpf-starters/data/persistence/src/main/resources/
  cpf-generated-domain-dialect/{oracle|postgresql|mariadb}/mybatis/
        ↓ generated build.gradle: -PcpfDbVendor=<vendor>
build/generated-resources/cpf-vendor/
```

Install/Seed/Migration/Rollback/Verify는 중앙 DB Tool Vendor Pack이 소유하고 Runtime Mapper Template은 CPF Data Capability가 소유한다. Generated Project의 `src/main/resources` 아래에 Vendor Pack/Mapper를 복제하지 않고, Build가 `cpfDbVendor`로 선택한 하나만 `build/generated-resources/cpf-vendor` overlay에 조립한다. Vendor 선택은 Java Source와 Git Working Tree를 변경하지 않는다.

금지:
- Application Source를 Vendor별 복제.
- Mapper/DAO에 `if oracle/postgres/mariadb` 난립.
- Vendor SQL 3벌을 사람이 독립 정본으로 유지.
- MariaDB를 MySQL로 표현.
- H2/MySQL/MSSQL을 공식 Evidence에 포함.

Vendor 차이:
- Datatype.
- Sequence/Identity.
- Pagination.
- Upsert.
- Lock.
- Time.
- JSON/LOB.
- Identifier.
- Function syntax.

이 차이는 제한된 Dialect/Renderer/Strategy 경계가 소유한다.

## 11. Vendor Override

Canonical로 표현할 수 없는 차이만 Override한다.

Manifest:
- canonicalObjectId.
- owner.
- vendor.
- reason.
- override path.
- affected migration.
- test/evidence.

Override가 늘어나면 Generator/Dialect 모델 부족 여부를 검토한다.

## 12. Migration Lifecycle

세 Vendor 모두 동일 Scenario ID로:

- Fresh Install.
- Seed.
- Upgrade.
- Reapply/Idempotency.
- Rollback 또는 Forward Recovery.
- Runtime Query.
- Checksum/Drift.
- Index/FK/Constraint.
- Transaction/Lock.

과거 Release Baseline Migration을 Generator 개선 때문에 재작성하지 않는다.

## 13. Generator Lifecycle

- Dry Run.
- Validate.
- Create.
- Add.
- Diff.
- Regenerate.
- Idempotent regenerate.
- Upgrade.
- User-modified file protection.
- Conflict report.
- Remove.
- Restore.
- Transient generation-state update.

Generator가 사용자 Source를 조용히 덮어쓰지 않는다.

## 14. Test Generation

생성 Test 최소:

- Metadata validation.
- Dependency public visibility.
- Compilation.
- Controller.
- Validation.
- Service transaction.
- DAO.
- CRUD.
- Search/Paging.
- Optimistic lock.
- Duplicate.
- Idempotency.
- Commit.
- Rollback.
- Error mapping.
- TransactionId.
- Context/Header.
- Audit/Masking.
- Local call.
- Remote call.
- Batch sample.
- DB3 SQL parity.

## 15. Cross-platform

Windows와 Linux에서 동일 Metadata를 사용해 `cpf-<domain>` Generated Tree/Content가 의미상 동일해야 하며 불필요한 README/verification/vendor tree를 생성하지 않는다.

검증:
- Path space.
- Korean UTF-8.
- LF/CRLF.
- Exit code.
- relative/absolute path.
- no WSL mandatory.
- no pwsh mandatory on Linux.

## 16. 완료

다음이 모두 연결되어야 한다.

```text
Input Contract
→ Preflight
→ Generator
→ Public Starter Dependency
→ Source
→ Canonical DB
→ DB3
→ Sample Transaction
→ Test
→ Transient State/Lifecycle
```

Schema JSON, Template 파일, Sample 하나만 존재해서는 완료가 아니다.


## Generated Call / Result Golden Path — 필수

Generated Domain은 선택 Capability에 따라 실제 compile되는 예제를 생성한다.

- Controller `call`.
- same-JVM Service direct call.
- Repository typed call.
- Domain Typed Client returning `CpfResult<T>`.
- External Typed Client returning `CpfResult<T>`.
- DTO/List/Page/Cursor/Map/scalar/boolean/count/no-data.
- async boundary.
- business/technical/unknown handling.
- structured business log/audit.
- Outbox/Inbox or reconcile sample where selected.

Generator가 stale `CpfRequest/CpfResponse`, DAO, legacy Transaction annotation, raw Map/URL, old Result contract를 생성하면 FAIL.
