# CPF Generated Domain Lifecycle Policy

- Currentization source/basis SHA: `e6f2e7a599a948277b118967d0fb5f840f65c114` (`18_19`)
- Canonical customer generated project root: **Customer Project Root의 `cpf-<domain>/`**
- Current retained regression domains in CPF repository: `cpf-member/` (`MBR`) + `cpf-external/` (`EXS`)
- 이 문서는 Generated Business Domain의 생성 위치, 설정 기반 일반화, 기본 Starter/DB/Sample Transaction, 장기 회귀, 삭제·재생성, 최종 결과물 정책을 정의한다.

## 1. 최종 정책

CPF Generator는 `member` 전용 Generator가 아니다. **모든 고객 업무 Domain을 동일 Canonical Schema + 동일 Generator Engine + 동일 Template Set으로 생성하는 Domain-neutral Generator**다.

논리 Domain명과 물리 Project Root명을 분리한다.

```text
domainName    : member
systemCode    : MBR
packageName   : member  # 생략 시 domainName에서 자동 파생
tablePrefix   : MBR
projectRoot   : cpf-member/
```

Generated Customer Project Root naming은 항상 `cpf-<domain>/`이다. 여기서 `cpf-` Prefix는 **CPF Generator가 생성한 Project naming convention**이며, 해당 Root를 CPF Framework Product Module/Public BOM/Publication 대상으로 만든다는 뜻이 아니다.

CPF 개발 Repository에서는 실제 고객 구조를 회귀검증하기 위해 사용자 승인된 두 Root만 유지한다.

```text
cpf-member/      # Generated Customer Domain, MBR
cpf-external/    # Generated Customer Domain, EXS
```

그 외 임의 Domain은 Repository Root에 만들지 않는다. Genericity 검증은 `build/domain-generator/verification/<scenario>/` 또는 Repository 외부 Temp에서 수행하고 cleanup한다.

Generated Project에는 **개발자가 실제 수정·사용해야 하는 Surface만 노출**한다. Framework 내부 Canonical DB 모델, Vendor Render 결과, 검증 산출물, 중복 README/Report/Manifest를 Generated Project 내부에 기본 생성하지 않는다.

금지:

- `member/`, `external/`, `<domain>/`처럼 `cpf-` Prefix 없는 Generated Project Root
- `cpf-member`/`cpf-external`을 CPF Framework Product Module/Public Artifact로 등록
- Repository Root에 임의 `cpf-<domain>`을 수동 생성
- `cpf-tools/generator/golden/member` 같은 concrete domain Golden Source
- Domain별 별도 Generator Engine/Template 복사
- 개발자가 사용하지 않는 `README.md`, `verification/`, `db/canonical/`, `db/vendors/`, 빈 capability directory의 기본 생성

## 1.1 18_19 Stateless Lifecycle Source Invariant

- Generated Project metadataRequired = false. `.cpf/`, root-visible `cpf-domain.yaml`, lock, ownership/manifest를 생성하지 않는다.
- official regression input은 `cpf-tools/generator/definitions/member/cpf-domain.yaml`, `cpf-tools/generator/definitions/external/cpf-domain.yaml`.
- user-supplied arbitrary domain은 명시 `--file`을 사용하며 Project Root는 `cpf-<domain>/`.
- `diff/regenerate/remove`는 동일 input + current Template에서 expected seed를 계산하고 changed file을 만나면 fail-closed한다.
- lifecycle transient state/evidence는 `build/domain-generator/verification/**`가 소유한다.
- Canonical user input schema는 `cpf-domain.schema.json` exactly-one으로 수렴한다. competing legacy schema는 Consumer 전환 후 제거한다.

## 2. Canonical 입력은 Domain-neutral Metadata다

모든 Domain 차이는 설정/Metadata로 주입한다.

최소 Canonical 입력 의미론:

```yaml
domain:
  name: <domainName>
  systemCode: <3-char-code>
  # packageName은 선택값. 생략 시 domain.name을 Java package root로 사용

database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: <domain-prefix>

preset: standard-enterprise

modules:
  online: true
  batch: true

features:
  persistence: mybatis
  httpClient: true
  resilience: true
  cache: none
  messaging: none

generation:
  sampleTransaction: true
```

`standard-enterprise`의 기본 Golden Path는 `sampleTransaction=true`이며 생성 직후 개발자가 실제 실행·DB 거래·Test를 확인할 수 있어야 한다.

`custom`에서는 기능을 명시적으로 끌 수 있지만, 이번 공식 회귀 Domain `member`/`external`은 Online + Batch + Sample Transaction 회귀 시나리오를 반드시 검증한다.

## 3. Domain별 하드코딩 금지

Generator/Template/Script/Build Logic에 다음과 같은 업무별 특수분기를 금지한다.

```text
if domain == member
if systemCode == MBR
if domain == external
if systemCode == EXS
member-only template
external-only template
MBR-only SQL/Mapper
EXS-only Controller/Service
```

정상 구조:

```text
cpf-domain.yaml
→ Canonical Schema validation
→ Domain Metadata Model
→ Canonical Naming Strategy
→ Single Generator Engine
→ Single Template Set
→ Domain Root 생성
```

Domain명/Module명/SystemCode/Package/TablePrefix/Port/Provider 선택만 달라지고 동일 Capability 조합의 구조와 Framework 사용방식은 normalize 후 동일해야 한다.

## 4. 공식 이중 Generated Verification Domain

이번 개발기간에는 다음 두 Domain을 **동시에 실제 생성·유지·회귀검증**한다.

### 4.1 member

```yaml
domain:
  name: member
  moduleName: member
  projectName: cpf-member
  systemCode: MBR

database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: MBR
preset: standard-enterprise
modules:
  online: true
  batch: true
generation:
  sampleTransaction: true
```

Canonical Generated Project Root: `cpf-member/`

### 4.2 external

```yaml
domain:
  name: external
  moduleName: external
  projectName: cpf-external
  systemCode: EXS

database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: EXS
preset: standard-enterprise
modules:
  online: true
  batch: true
generation:
  sampleTransaction: true
```

Canonical Generated Project Root: `cpf-external/`

둘은 같은 Generator Engine/Template을 사용한다. `member` 생성 결과를 복사·치환하여 `external`을 만드는 방식은 금지한다.

## 5. Generated Customer Project 최소 Surface

Generated Project의 기본 원칙은 **필요한 것만 생성**이다.

필수 Root 계약은 Generator lifecycle에 실제 필요한 항목만 둔다.

```text
cpf-<domain>/
├─ build.gradle          # 실제 Build에 필요할 때
├─ settings.gradle       # multi-module 구성에 필요할 때
├─ online/               # Online Runtime capability 선택 시만
├─ batch/                # Batch capability 선택 시만
├─ domain/               # 2개 이상 실제 Consumer가 공유하는 업무코드가 있을 때만
└─ contract/             # 독립 Public Contract Consumer가 실제 있을 때만
```

물리 하위 Directory는 Root가 이미 Domain Identity를 가지므로 `member-online`, `member-batch`, `cpf-member-api`처럼 Domain명을 반복하지 않는다. Runtime 구현 Module에 `api`라는 이름을 쓰지 않는다. `api`는 HTTP endpoint 의미와 Public Java Contract 의미를 혼동시키므로 Online Runtime은 `online/`, 진짜 독립 Contract는 `contract/`로 구분한다.

`domain/`은 표준이라는 이유로 무조건 만들지 않는다. `online`/`batch` 등 둘 이상의 실제 Consumer가 공유할 Model/Application/Persistence 코드가 있을 때만 생성한다. Consumer 없는 `common/`, `shared/`, `domain/` 추상화는 금지한다.

Generated Project 내부 기본 생성 금지:

```text
README.md
verification/
db/canonical/
db/vendors/
db/oracle/
db/postgresql/
db/mariadb/
<domain>-api/
<domain>-common/
<domain>-online/
<domain>-batch/
```

Generator 검증 산출물은 Generated Project가 아니라 다음 transient 영역으로 이동한다.

```text
build/domain-generator/verification/<scenario>/
```

DB Canonical Model과 Oracle/PostgreSQL/MariaDB Renderer/Vendor Pack은 CPF Generator/DB Tooling 내부가 소유한다. Generated Customer Project에는 개발자가 직접 관리해야 하는 DB Extension Surface가 실제 요구될 때만 최소 인터페이스를 별도 정의하며, 기본적으로 Canonical/Vendor 내부 구조를 노출하지 않는다.

`README.md`를 Generated Project마다 복제하지 않는다. 사용법·Golden Path·오류복구 설명은 CPF의 Canonical Guide/EDU/CLI help가 제공한다.

## 6. 기본 Public Starter Composition

`standard-enterprise` 생성 결과는 개발자가 Internal Artifact를 직접 선택하지 않고 바로 실행할 수 있어야 한다.

Generated Domain의 **직접 dependency는 최신 Starter Catalog의 Public Surface만** 사용한다.

Online 기본 직접 참조:

```text
cpf-starter-secure-api
cpf-starter-data-mybatis
+ 필요한 기능의 Public Composition/Profile/Provider
```

Batch 기본 직접 참조:

```text
cpf-starter-batch
cpf-starter-data-mybatis
+ 필요한 기능의 Public Composition/Profile/Provider
```

`currentization_source_sha` Catalog에서 아래 Artifact는 Internal-only leaf다.

```text
cpf-starter-integration-http
cpf-starter-integration-resilience
```

따라서 Generated `build.gradle`에 위 Internal leaf를 직접 생성하지 않는다. `features.httpClient=true`, `features.resilience=true`는 Public Profile/Composition이 내부에서 해당 capability-runtime을 소비하도록 해결한다. 필요한 Public Composition이 아직 없다면 **Framework Starter 측 구현 Gap**이며, Public Surface를 먼저 구현한 후 Generated Domain이 소비한다.

금지:

- Internal leaf project/package/artifact 직접 dependency
- legacy `cpf-common` 직접 dependency
- legacy profile coordinate
- 선택하지 않은 Redis/Valkey/Kafka/MQ/S3 강제 의존

Provider는 Metadata로 선택하며 exactly-one 제약이 있는 Capability는 충돌 시 fail-fast한다.

## 7. 기본 Sample Transaction은 실제 거래다

Generated Domain은 Hello World/빈 Interface/Skeleton만 생성하지 않는다. 최소 하나의 **실제 DB Transaction Sample**을 생성한다.

Canonical Sample 이름:

```text
SampleTransaction
```

Table:

```text
<DOMAIN_PREFIX>_SAMPLE_TX
```

이번 공식 Domain:

```text
member   → MBR_SAMPLE_TX
external → EXS_SAMPLE_TX
```

최소 논리 Column:

```text
ID
BUSINESS_KEY
STATUS
REQUEST_VALUE
RESULT_VALUE
CREATED_AT
CREATED_BY
UPDATED_AT
UPDATED_BY
VERSION
```

Framework Context/Trace 값은 실제 Persistence 요구가 있는 경우만 저장하며 표준 Header 값을 의미 없이 모든 Table에 복제하지 않는다.

최소 API:

```text
POST   /api/v1/sample-transactions
GET    /api/v1/sample-transactions/<built-in function id>
GET    /api/v1/sample-transactions?page=...
PUT    /api/v1/sample-transactions/<built-in function id>
```

호출 경로:

```text
HTTP
→ CPF Web / Security
→ CPF Context / transactionId
→ Validation
→ Business Controller
→ Domain Service
→ Transaction
→ DAO/Mapper
→ CUSTOMER_BUSINESS_DB
→ CPF Result/Error Mapping
→ Logging/Audit
```

생성 직후 개발자가 Application을 실행하고 Create → Read → Search/Page → Update → DB 확인을 수행할 수 있어야 한다.

## 8. 3단 Base Class Golden Path

Controller:

```text
CpfBaseController (abstract)
→ <Domain>BaseController (abstract)
→ SampleTransactionController (concrete)
```

Service:

```text
CpfBaseService (abstract)
→ <Domain>BaseService (abstract)
→ SampleTransactionService (concrete)
```

DAO:

```text
CpfBaseDao (abstract)
→ <Domain>BaseDao (abstract)
→ SampleTransactionDao (concrete)
```

예:

```text
MemberBaseController / ExternalBaseController
MemberBaseService    / ExternalBaseService
MemberBaseDao        / ExternalBaseDao
```

Domain Base는 빈 ceremonial class가 아니다. Context 검증, systemCode helper, domain error/audit helper, request normalization, persistence 공통정책, protected template hook 등 실제 Domain 공통 역할을 가진다.

동일 의미 `CpfControllerExtension/CpfServiceExtension/CpfDaoExtension` 병존과 4단 compatibility wrapper를 금지한다.

## 9. Framework 기능 실제 Consumer

Generated Sample은 다음 CPF 기능을 실제 호출 경로에서 사용한다.

- Context / transactionId propagation
- Security/authorization 기본 경계
- Bean Validation + CPF Error Mapping
- `CpfBusinessException`, `CpfValidationException`, `CpfSystemException` 계약
- Transaction
- Persistence / Paging
- Logging / Audit
- Masking 경계
- OpenAPI
- Health/Observability 연결
- API/Batch별 Configuration

Spring/MyBatis Native API를 허용하더라도 CPF가 제공하는 의미론/자동화가 있는 부분을 우회해 직접 plumbing을 중복 생성하지 않는다.

## 10. Test도 실제 Generated Output이다

각 Domain에 최소 다음 Test를 생성한다.

```text
Generation Contract Test
Controller Test
Service Test
DAO/Persistence Test
Validation Negative Test
Transaction Commit/Rollback Test
Error Mapping Test
Context/transactionId Propagation Test
Sample Transaction Integration Test
Batch Sample Test
```

Integration Test는 가능한 환경에서 실제 DB를 사용하여:

```text
POST Create
→ DB INSERT
→ GET
→ UPDATE
→ DB SELECT 검증
```

까지 수행한다. `ApplicationContext loads` 하나 또는 Mock Repository만으로 완료하지 않는다.

## 11. Batch도 빈 Skeleton 금지

Batch capability를 선택한 경우 `batch/`는 최소 하나의 실행 가능한 Sample Batch/Worker를 생성한다.

예:

```text
SampleTransactionStatusBatch
```

흐름:

```text
대상 Query
→ paging/chunk
→ process
→ update
→ execution context/logging
→ error/retry/restart/idempotency test
```

업무 데이터는 `CUSTOMER_BUSINESS_DB`를 사용하고 CPF Batch Runtime metadata는 공식 Platform DB 계약을 따른다.

## 12. DB / SQL / Vendor 규칙

Generated Domain별 별도 물리 DB를 만들지 않는다.

```text
memberDB / mbrDB       = 금지
externalDB / exsDB     = 금지
```

모든 Generated Business Domain은 `CUSTOMER_BUSINESS_DB`를 사용하고 Prefix로 논리 소유권을 구분한다.

```text
member   → MBR_*
external → EXS_*
```

동일 Generated Application Source로 Oracle/PostgreSQL/MariaDB를 검증한다.

Generated Java/Mapper/Service에 `if oracle/postgres/mariadb`를 만들거나 Vendor별 업무 Source 3벌을 생성하지 않는다.

## 13. Root Build/Settings 연결

Generated Project Root는 `cpf-<domain>/` naming을 사용한다. CPF 검증 Repository에서 유지할 수 있는 Generated Root는 명시적으로 승인된 `cpf-member/`, `cpf-external/`뿐이다.

필수:

- 선택된 `online/`, `batch/`, 실제 공유가 있는 `domain/`, 실제 Contract Consumer가 있는 `contract/`만 Build Graph에 연결
- 공통 Convention/BOM/Java/Spring/Gradle 정합
- Test configuration
- Domain/project/path collision 검사
- 기존 Generated Domain과 Root Build 보호
- CPF Product Publication/BOM/Production deploy inventory와 Generated Customer Project inventory 분리

Root Build를 무단 덮어쓰지 않는다. 기존 설정과 충돌하면 dry-run/diff/conflict report 후 안전하게 처리한다.

## 14. 개발자 즉시 실행 UX

생성 완료 후 **Generated Project 내부 README 복제 없이** Generator CLI 출력, Canonical Guide, EDU에서 최소 다음을 제공한다.

- Domain/SystemCode/Package/Project Root
- 실제 생성된 Module만 표시
- Selected Preset/Starters/Providers
- Database Role/Table Prefix
- Sample Transaction/Table
- Application start command
- Test command
- Sample API path
- OpenAPI path
- DB install/migration/verify command
- Batch sample execution command
- Regenerate/Diff/Upgrade/Remove 안내
- User-owned extension point

개발자는 생성 후 대량 수동 수정 없이 바로 Run → API 호출 → DB 확인 → Test를 수행할 수 있어야 한다.

## 15. Lifecycle / Existing Domain 보호

지원 lifecycle:

```text
generate all
generate domain <name>
add domain <name>
dry-run
diff
regenerate
idempotent rerun
upgrade
remove
restore/regenerate
```

새 Domain 추가는 기존 Domain을 훼손하지 않는다.

```text
cpf-member/ 존재
→ add external
→ cpf-member/ 그대로 유지
→ cpf-external/ 추가
```

Generated Project에는 ownership/lock metadata를 영구 저장하지 않는다. regenerate는 Framework definition 또는 명시 입력으로 기대 Seed Source를 계산하고 현재 Seed Source가 다르면 사용자 변경/Template drift를 구분할 수 없으므로 fail-closed하여 덮어쓰지 않는다. 선택하지 않은 capability, 빈 Directory, 내부 검증/DB Renderer 폴더를 lifecycle 과정에서 새로 남기지 않는다.

## 16. 이유 없는 삭제 금지 / 삭제 시 Generator 복구

`cpf-member/`와 `cpf-external/`은 개발기간 동안 유지하는 공식 Generated Verification Customer Domains다.

일반 Hygiene, Generated Source Cleanup, Sample 정리, Product Root 정리 등을 이유로 삭제하지 않는다. 단 이 둘은 CPF Product Module/Public Artifact가 아니라 Generated Customer Project다.

Generator의 remove/fresh-generation/deterministic-regeneration/upgrade/restore 검증을 위해 일시 삭제할 수 있지만 같은 작업 단위에서 반드시 Canonical Generator로 다시 생성한다.

복구:

```text
Framework `cpf-tools/generator/definitions/<domain>/cpf-domain.yaml` 또는 명시 `--file` 입력
→ Canonical Generator
→ 고객 Project 영구 lifecycle metadata 0
→ 필요한 capability module만 생성
→ CPF 내부 DB3 render/verification
→ compile/test/runtime/DB3
```

Source 복사/수동 작성으로 복원하지 않는다. 삭제된 상태로 Requirement/Session/최종 결과를 종료하지 않는다.

Permanent 삭제/Canonical Path 변경은 사용자 명시 승인과 replacement 검증 없이는 수행하지 않는다.

## 17. member + external Parity와 추가 Genericity

두 Domain을 normalize하여 구조/기능 parity를 비교한다.

허용 차이:

```text
member   ↔ external
MBR      ↔ EXS
package
port
tablePrefix
Domain metadata
```

동일 Capability인데 Framework 구조/Starter/Test/Sample 거래 기능이 이유 없이 다르면 Generator 결함이다.

또한 Source/Template 전체에서 `member/MBR/external/EXS` 특수분기 0을 검사한다. Root naming은 `cpf-<domain>` 규칙으로 계산하며 member/external 전용 예외분기를 두지 않는다.

필요한 추가 genericity 증명을 위해 제3의 임의 Domain을 다음 transient 경로에 fresh 생성할 수 있다.

```text
build/domain-generator/verification/<scenario>/
```

제3 Domain은 실제 generate→compile→test→가능한 runtime→normalized parity 후 cleanup한다. **이 cleanup은 retained `cpf-member`/`cpf-external`에는 적용하지 않는다.**

## 18. Final Deliverable

최종 Repository/최종 ZIP에는 실제 Generator로 생성된 두 회귀 Domain을 모두 포함한다.

```text
cpf-member/**
cpf-external/**
```

각 Domain은 **실제 필요한 Source Surface만** 포함한다.

필수/조건부:

- 실제 선택된 `online/`, `batch/`
- 실제 공유 Consumer가 있을 때만 `domain/`
- 실제 독립 Contract Consumer가 있을 때만 `contract/`
- Source/Test/Config

포함 금지:

- Generated Project 내부 `README.md`
- Generated Project 내부 `verification/`
- Generated Project 내부 `db/canonical/`, `db/vendors/`, Vendor별 3벌
- 선택하지 않은 capability/빈 Directory
- 과거 이름 `member-api`, `external-api`, `<domain>-common`

Generation Manifest, hash, DB3 render, verification 결과, Evidence는 CPF Tooling/Build/Evidence 소유 경로에 기록하고 Generated Customer Source Tree를 오염시키지 않는다.

## 19. Evidence

Domain별로 기록:

- exact master SHA
- Generator version/hash
- input/lock hash
- generated file count/hash
- Starter/Provider composition
- compile/test 명령과 exit code
- runtime 결과
- Sample API/DB Transaction 결과
- Oracle/PostgreSQL/MariaDB 결과
- dry-run/diff/regenerate/idempotency/upgrade/remove/restore
- user-owned modification preservation

공통 Evidence:

- member↔external normalized parity
- member/MBR/external/EXS hardcoding scan
- Public Starter dependency scan
- Internal dependency scan
- Vendor branch scan
- 3단 Base hierarchy scan
- Annotation runtime consumer
- Korean comment gate

미실행 검증은 PASS로 기록하지 않는다.

## 20. 완료 조건

다음이 모두 충족되어야 Generator 관련 Requirement를 완료할 수 있다.

- `cpf-member/` 실제 Generator 생성 및 최종 유지
- `cpf-external/` 실제 Generator 생성 및 최종 유지
- Root naming `cpf-<domain>` 일관성 및 무-prefix Generated Root 0
- 두 회귀 Domain의 Online + Batch 시나리오 검증
- 실제 필요한 module만 생성, 빈/consumerless module 0
- Runtime 구현 Module명 `api` 사용 0; Online Runtime은 `online/`
- `domain/`은 실제 다중 Consumer 공유가 있을 때만 존재
- Generated Project 내부 README/verification/db canonical/vendor 폴더 0
- 기본 Public Starter 자동 구성
- Sample Transaction + Customer Business DB3 lifecycle
- Create/Read/Search/Update 실제 거래 검증
- Controller/Service/DAO 3단 Golden Path
- CPF Context/Error/Validation/Transaction/Persistence/Logging/Audit/OpenAPI 실제 Consumer
- Oracle/PostgreSQL/MariaDB 동일 Application Source
- member↔external normalized parity
- domain/systemCode 특수 하드코딩 0
- add-domain 시 기존 Domain 비파괴
- dry-run/diff/regenerate/idempotency/upgrade/remove/restore
- user-owned modification protection
- 검증 산출물은 `build/domain-generator/verification/**` 등 transient owner에만 존재
- final Repository/ZIP에 cpf-member + cpf-external 포함

단순 Skeleton, Interface/DTO/Mock/Swagger 존재, compile-only, member 하나만 성공, 불필요한 폴더를 잔존시키는 생성은 완료가 아니다.

---

## Cross-Platform Generator CLI 계약

### 공통 CLI·Script 규칙

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

## Currentization SHA 의미

- `currentization_source_sha`: `e6f2e7a599a948277b118967d0fb5f840f65c114` (`18_19`) — 본 문서 현행화 시 비교 기준으로 사용한 Source.
- `execution_source_sha`: 각 Developer/Codex/QA 세션 시작 시 최신 `origin/master`에서 동적으로 확인한다.
- `verified_sha`: Build/Test/Runtime/Evidence를 실제 실행한 exact SHA다.

`currentization_source_sha`를 이후 세션의 영구적인 "현재 master"로 해석하지 않는다. 완료 판정과 Evidence는 `verified_sha`를 기준으로 한다.

## Current-State Generated Domain Runtime 선택 규칙

`cpf-<domain>/online/`은 필수 Runtime이다. `cpf-domain.yaml`에서 `modules.batch=true`를 선택하면 같은 Root에 `batch/`를 생성한다. Batch 실행 의미와 Runtime 구현의 Owner는 `cpf-batch`이고 Generated Domain은 Public `cpf-starter-batch` 계약을 소비한다. `modules.batch=false`이면 batch Source/Config/Runtime을 생성하지 않는다.

업무 개발 계약은 Public API/Starter에 두고 Provider/Runtime 구현은 Internal/Owner 영역에 둔다. Generator/Generated Domain/EDU가 Internal Starter 또는 internal package를 직접 참조하면 실패다.

