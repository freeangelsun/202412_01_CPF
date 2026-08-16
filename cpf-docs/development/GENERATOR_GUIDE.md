# CPF Generator Guide

> **주 독자**: 업무 개발자 / Tech Lead / Generator·Template 관리자  
> **이 문서로 끝내는 일**: `cpf-domain.yaml`에서 Online 업무 Domain을 생성하고 재생성·Upgrade·Remove까지 사용자 코드를 보호하며 운영한다. Batch는 초기 프로젝트 구성에서 별도 Capability로 선택한다.  
> **이 문서를 펼칠 때**: 신규 Domain 생성, 기존 Domain 재생성, Framework Upgrade, Generator Template 변경을 수행할 때

## 가장 먼저 할 일

1. `cpf-domain.yaml`을 작성하고 Schema Validation을 통과합니다.  
2. `dry-run`과 `diff`로 변경 범위와 충돌을 확인합니다.  
3. Online 업무 Source를 생성하고 Generated Project에는 lifecycle metadata를 영구 저장하지 않습니다. Batch는 Generator가 만들지 않고 초기 프로젝트 구성에서 `cpf-starter-batch`를 별도 선택합니다.  
4. Sample Transaction과 DB3/Test를 실행합니다.  
5. regenerate/upgrade/remove에서도 User-owned Source와 DB 안전성을 보존합니다.

이 문서는 CPF 기능이 완성되어 제공되는 기준으로 설명합니다. 개발 회차나 진행 상태가 아니라 **선택 → 실행 → 정상 결과 → 실패/부분 실패/UNKNOWN → 상태 확인·대사·조정 → 운영 종료 조건**을 중심으로 사용합니다.

---


## 1. 5분 Quick Start

### Generator가 만드는 Orchestration-ready 기본 구조

Generator는 서비스를 단순 분리하는 Skeleton만 만들지 않습니다. Generated Online 업무에 필요한 Context propagation, Transaction/Idempotency, Event reliability, 운영 추적 기본 연결점을 같은 Domain 계약으로 생성합니다. Batch Runtime/lineage는 별도 `cpf-starter-batch` Capability가 소유합니다. Generated Domain은 Public Surface만 직접 참조하며 Orchestration을 위해 Internal leaf를 우회 참조하지 않습니다.

### 1.1 `cpf-domain.yaml` 작성

```yaml
domain:
  name: member
  systemCode: MBR

database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: MBR

preset: standard-enterprise

modules:
  online: true

features:
  persistence: mybatis
  httpClient: true
  resilience: true
  cache: none
  messaging: none

generation:
  sampleTransaction: true
```

### 1.2 생성 계획 확인

```console
cpf domain dry-run --file cpf-tools/generator/definitions/member/cpf-domain.yaml
```

Dry-run에서 다음을 확인합니다.

- Domain logical name/project root/package/systemCode/tablePrefix 유효성
- Deployable별 Top-level Profile exactly-one
- Persistence/Cache/Messaging 등 Provider exactly-one
- Public Starter dependency만 사용
- Customer Business DB 계약
- `CUSTOMER_BUSINESS_DB`와 tablePrefix 계약 및 3-Vendor portability 확인
- 기존 사용자 소스와 충돌 여부

### 1.3 Diff 확인

```console
cpf domain diff --file cpf-tools/generator/definitions/member/cpf-domain.yaml --output cpf-member
```

Diff는 파일 단순 비교가 아니라 `Generated-owned`, `User-owned`, `Mergeable`, `Conflict`, `Obsolete`를 구분합니다.

### 1.4 생성

```console
cpf domain generate --file cpf-tools/generator/definitions/member/cpf-domain.yaml
```

`standard-enterprise` 기본 결과:

```text
cpf-member/
└─ online/

Batch를 사용하는 프로젝트는 초기 구성에서 `cpf-starter-batch`를 별도 선택합니다.
```

### 1.5 개발 시작

생성 직후 개발자는 다음 순서로 진행합니다.

```text
DTO/Validation
→ Application Command/Query
→ Domain Rule
→ Persistence
→ Transaction/Idempotency
→ Integration/Resilience
→ Error Catalog
→ Security/Audit
→ Test/Fault/Concurrency
→ OpenAPI/Orchestration/운영 인계
```

---

## 2. Cross-Platform CLI - 동일한 Generator를 Windows/Linux에서 사용

사용자에게 노출되는 Generator의 Canonical 명령은 `cpf domain ...`입니다. 운영체제마다 별도 Generator Script나 Template을 유지하지 않습니다.

```console
cpf domain generate --file cpf-tools/generator/definitions/member/cpf-domain.yaml
cpf domain generate --file cpf-tools/generator/definitions/external/cpf-domain.yaml
cpf domain diff --file cpf-tools/generator/definitions/member/cpf-domain.yaml --output cpf-member
cpf domain regenerate member
cpf domain regenerate external
```

설치된 환경은 Windows/Linux 모두 `cpf ...`를 사용합니다. Repository-local 실행만 launcher가 다릅니다.

```text
Windows CURRENT  .\cpf-tools\runtime\cli\cpf.bat domain generate --file cpf-tools/generator/definitions/member/cpf-domain.yaml
Linux CURRENT    ./cpf-tools/runtime/cli/cpf domain generate --file cpf-tools/generator/definitions/member/cpf-domain.yaml
```

`cpf-tools/runtime/cli/cpf`와 `cpf-tools/runtime/cli/cpf.bat`는 Repository-local thin wrapper입니다. 설치 후 논리 명령은 `cpf ...`입니다. Schema validation, naming, template selection, DB intent, diff, lifecycle state와 exit-code 의미는 OS-neutral Engine이 소유합니다. 따라서 같은 `cpf-domain.yaml`을 Windows와 Linux에서 생성했을 때 line-ending 같은 비의미 차이를 normalize한 결과가 같아야 합니다.

Generator Cross-platform Gate는 다음을 함께 확인합니다.

- 인자/옵션 파싱과 exit-code 범주
- 공백·한글·상대·절대 경로
- `/`와 `\` 경로 구분
- UTF-8 출력과 LF/CRLF
- temp/home/work directory
- member(MBR) + external(EXS) fresh generation
- compile/test, dry-run/diff/regenerate
- Framework definition + deterministic expected-seed / transient manifest·hash normalized parity

PowerShell 7은 Linux 필수 의존성이 아니며 Bash/WSL/Git Bash도 Windows 필수 의존성이 아닙니다. Shell script가 존재하더라도 핵심 Generator 로직은 공통 Engine에서만 구현합니다.

## 3. Generator가 보장하는 Framework 계약

Generator는 다음을 생성 결과의 불변조건으로 유지합니다.

| 영역 | Framework 계약 |
|---|---|
| Dependency | Public Starter만 사용, Internal leaf 직접 참조 없음 |
| Profile | Deployable당 Top-level Profile exactly-one |
| Class | `CpfBase* → DomainBase* → Business*` 정확한 3단 구조 |
| Annotation | Runtime Consumer가 있는 CPF Annotation을 실제 사용 |
| Context | CPF Context/Transaction lineage를 사용 |
| Error | `CpfBusinessException`/Validation/System taxonomy + Common Catalog |
| DB | `CUSTOMER_BUSINESS_DB + tablePrefix`, Domain Physical DB 생성 없음 |
| Vendor | Oracle/PostgreSQL/MariaDB에서 Application Source 동일 + 선택 Vendor Build overlay |
| API | OpenAPI 생성과 실제 Consumer 연결 가능한 구조 |
| Test | Unit/Integration/Fault/Concurrency/Reconcile 진입점 포함 |
| Lifecycle | dry-run/diff/regenerate/upgrade/remove/idempotent rerun |
| Ownership | Generator-owned와 User-owned 영역 분리 |

`sampleTransaction=true`의 정본 예제는 중앙 Vendor Mapper Pack을 실제 소비하는 `persistence=mybatis` Golden Path입니다. `jdbc`/`jpa`는 `sampleTransaction=false`인 고객 확장 Skeleton에서 선택합니다. Generator는 구현되지 않은 Provider×Sample 조합을 부분 생성하지 않고 Preflight에서 실패시킵니다.

---

## 4. Canonical 입력 — `cpf-domain.yaml`

대화형 Wizard는 보조 UI일 수 있지만 **정본은 선언형 `cpf-domain.yaml`**입니다. 이 파일은 코드 리뷰, 재현, 자동화, CI, Upgrade에 사용됩니다.

### 4.1 Domain

```yaml
domain:
  name: payment
  systemCode: PAY
```

- `name`: 업무 Domain 논리명
- `systemCode`: Trace/Audit/Table prefix의 기본 업무 식별자
- `packageName`: 선택형 Java package override. 생략하면 `domain.name`을 바로 package root로 사용합니다. 회사 표준 namespace가 필요할 때만 `kr.example.payment`처럼 명시합니다.

`systemCode`, `tablePrefix`, 최종 package는 조직 내 중복을 허용하지 않습니다. 기본 package는 `domain.name`과 같으며 CPF가 `com.customer` 같은 조직 접두어를 강제하지 않습니다.

### 4.2 Database

```yaml
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: PAY
```

`cpf-domain.yaml`은 업무 Domain의 DB 역할과 Prefix를 선언하며 특정 Vendor를 업무 Source 생성 조건으로 사용하지 않습니다. 실제 배포/검증 환경의 Vendor 선택은 중앙 DB Lifecycle/환경 설정에서 수행합니다. Generated Application Source는 Oracle/PostgreSQL/MariaDB에서 동일하게 유지됩니다.

#### 생성 Setup에서 DB Vendor 선택

DB Vendor는 Domain Source 정본과 분리하지만, 개발자가 처음 준비할 때는 한 번에 선택할 수 있습니다.

```console
cpf domain setup --name payment --system-code PAY --table-prefix PAY --vendor postgresql --database-name businessDB
```

이 명령은 transient `build/domain-generator/setup/payment/` 아래에 다음 두 파일을 만듭니다.

- `cpf-domain.yaml`: Vendor-neutral 업무 Domain 정본. `packageName` 생략 시 `payment` package를 사용합니다.
- `cpf-db-profile.local.json`: `postgresql` 같은 환경별 DB Vendor/Host/Port/Database와 Secret 환경변수 참조만 저장합니다.

비밀번호 원문은 Profile에 저장하지 않습니다. Generated build도 MariaDB를 암묵 선택하지 않으며 `-PcpfDbVendor=<vendor>` 또는 `CPF_DB_VENDOR`가 없으면 fail-closed합니다.

### 4.3 Preset

```yaml
preset: standard-enterprise
```

지원 Preset:

| Preset | 용도 |
|---|---|
| `minimal` | 최소 Runtime과 명시적 기능 선택 |
| `standard-enterprise` | 일반 Online 업무 기본값 |
| `full-enterprise` | 다수 Capability를 함께 사용하는 업무 |
| `custom` | 모든 Module/Feature를 명시적으로 선택 |

### 4.4 Modules

```yaml
modules:
  online: true
```

Generated Domain은 Online 업무 Source만 생성합니다. Batch가 필요한 프로젝트는 생성 YAML이 아니라 초기 프로젝트 구성에서 **`cpf-starter-batch` Capability를 별도로 선택**합니다. Batch 미사용 프로젝트에는 해당 Batch Runtime/Starter/Config를 추가하지 않습니다.

### 4.5 Features

```yaml
features:
  persistence: mybatis
  httpClient: true
  resilience: true
  cache: valkey
  messaging: kafka
  objectStorage: s3
  oidc: true
```

외부 Infra Provider는 Preset 이름만으로 무조건 끌어오지 않습니다. 선택한 기능만 Runtime graph에 포함됩니다.

Secret/password/token은 `cpf-domain.yaml`에 넣지 않습니다. 환경변수나 Secret Manager reference만 기록합니다.

---

## 5. Schema Validation

Generator는 파일 생성 전에 입력 계약을 검증합니다.

### Naming

- domain logical name과 `cpf-<domain>` physical root 규칙
- packageName 규칙
- systemCode uniqueness
- tablePrefix uniqueness
- Reserved prefix 충돌

### Runtime Composition

- Top-level Profile exactly-one
- Persistence Provider exactly-one
- Cache/Session/Messaging Provider collision
- incompatible feature 조합
- Internal artifact 직접 지정 금지

### Database

- Database role은 `CUSTOMER_BUSINESS_DB`
- Oracle/PostgreSQL/MariaDB에서 동일 Generated Application Source를 사용할 수 있는 DB 계약
- Domain별 Physical DB name 생성 금지
- Prefix/Owner metadata 일치

오류가 있으면 생성 전에 Fail-Fast하며, 일부 파일만 만들어 놓고 성공으로 종료하지 않습니다.

---

## 6. `standard-enterprise` 기본 조립

### API Deployable - 직접 참조 Public Surface

```text
cpf-starter-secure-api
cpf-starter-data-mybatis
```

HTTP/Resilience 같은 내부 Capability leaf는 `features.httpClient`, `features.resilience` Metadata를 보고 Public Profile/Composition 내부에서 조립합니다. Generated `build.gradle`은 Internal-only Artifact를 직접 참조하지 않습니다.

### Batch Deployable - 직접 참조 Public Surface

```text
cpf-starter-batch
cpf-starter-data-mybatis
```

Batch의 Integration/Resilience Runtime도 공개 Profile/Composition이 내부에서 조립하며 Generated Domain이 Internal leaf를 직접 선택하지 않습니다.

API와 Batch에 `cpf-starter-data-mybatis`가 각각 나타나는 것은 두 Deployable이 독립적으로 Persistence Provider를 소비하기 때문입니다. 동일 Build 파일에 같은 Dependency를 중복 선언하지 않습니다.

기본 Preset은 Redis/Valkey/Kafka/IBM MQ/S3 같은 외부 Infra를 강제하지 않습니다.

---

## 7. Public Starter Surface

Generator가 고객 Application에 노출하는 주요 Public Artifact는 다음과 같습니다. **Catalog의 `visibility=public` Surface만 직접 참조하며 Internal-only leaf 직접 참조는 금지합니다.**

### Top-level

```text
cpf-starter
cpf-starter-common
cpf-starter-web-api
cpf-starter-secure-api
cpf-starter-bff
cpf-starter-event
cpf-starter-batch
```

### Provider

```text
cpf-starter-data-jdbc
cpf-starter-data-mybatis
cpf-starter-data-jpa
cpf-starter-cache-caffeine
cpf-starter-cache-redis
cpf-starter-cache-valkey
cpf-starter-lock-valkey
cpf-starter-session-jdbc
cpf-starter-session-valkey
cpf-starter-messaging-kafka
cpf-starter-messaging-rabbitmq
cpf-starter-messaging-jms
cpf-starter-messaging-ibm-mq
cpf-starter-object-storage-s3
cpf-starter-graphql
cpf-starter-realtime
cpf-starter-oidc
```

`spring-data-redis` 같은 Internal shared runtime은 생성 Build 파일과 사용자 선택 목록에 노출하지 않습니다.

---

## 8. 생성 결과 구조

`standard-enterprise` 예:

```text
cpf-member/
└─ online/
   ├─ src/main/java/member/online/
   ├─ src/main/java/member/domain/   # Online이 사용하는 업무 Model/Application/Persistence package
   ├─ src/main/resources/
   └─ src/test/
```

생성 Directory는 역할이 이름으로 이해되어야 하며 `misc`, `temp`, `legacy`, 회차명 같은 Dumping Directory를 만들지 않습니다.

---

## 9. Controller / Service / DAO — 정확한 3단 Golden Path

CPF Domain은 Controller, Service, DAO를 다음 3단 구조로 생성합니다.

```text
CpfBaseController (abstract, cpf-starters/web)
→ DomainBaseController (abstract, Domain Common)
→ BusinessController (concrete)

CpfBaseService (abstract, cpf-starters/base)
→ DomainBaseService (abstract, Domain Common)
→ BusinessService (concrete)

CpfBaseDao (abstract, cpf-starters/data)
→ DomainBaseDao (abstract, Domain Common)
→ BusinessDao (concrete)
```

### 1단 — Framework Base

CPF가 전역적인 실행 계약을 제공합니다.

- Context/transaction correlation
- 표준 Error/Validation hook
- Audit/Logging hook
- Persistence/Transaction integration point
- 보호된 Template Method/extension point

직접 인스턴스화하지 않습니다.

### 2단 — Domain Common Base

Domain 전체에서 공유하는 실제 재사용 책임을 둡니다.

예:

- Domain 공통 Context helper
- 공통 Validation rule
- Error reference helper
- Logging/Audit field composition
- Domain 공통 정책
- Template Method
- protected Hook

**이 계층은 생략하지 않습니다.** 동시에 계층 수를 맞추기 위한 빈 Class로 두지도 않습니다.

### 3단 — Business Concrete

실제 Use Case를 구현합니다. CPF Annotation은 이 상속구조를 대체하지 않고 Runtime 정책을 보완합니다.

같은 역할의 `Cpf*Extension`과 `CpfBase*`를 병존시키거나 4단 이상의 compatibility inheritance를 만들지 않습니다.

---

## 10. Annotation Golden Path

Generator가 붙이는 Annotation에는 실제 Runtime Consumer가 있습니다.

```text
Annotation
→ scanner / interceptor / aspect / argument resolver / config consumer
→ runtime behavior
→ invalid usage fail-fast
→ cpf-education example
→ test
```

Context, Error, Validation, Transaction, Persistence, Cache, Lock, Messaging, Integration, Security, File, Batch, Observability, Audit, Paging, OpenAPI 등 CPF가 제공하는 Golden Path를 일반 생성물에서 Spring/OSS Native API로 우회하지 않습니다.

Native API 직접 사용은 CPF가 의도적으로 추상화하지 않은 기능의 `Advanced Native Extension`에서만 사용합니다.

---

## 11. Customer Business DB 계약

생성 업무의 관계형 Data는 Domain별 Physical DB가 아니라 **Customer Business DB**를 사용합니다.

```text
Customer Business DB
├─ MBR_*  member
├─ ACC_*  account
├─ PAY_*  payment
└─ ...
```

업무 Transaction과 원자성이 필요한 다음 상태도 Customer Business DB에 둡니다.

- Outbox
- Inbox/Dedup
- Idempotency
- Reconcile state

이를 cpfDB에 강제로 분리해 불필요한 XA를 만들지 않습니다.

업무 Domain별 별도 Physical DB name을 생성하지 않습니다.

---

## 12. 3-Vendor Neutrality

Generator가 만드는 Application Source와 Query는 Oracle/PostgreSQL/MariaDB에 대해 동일합니다.

```text
Application / Generated Source
        ↓
CPF Data Capability
        ↓
Dialect / Strategy boundary
        ↓
Oracle | PostgreSQL | MariaDB
```

업무 Source에 다음 패턴을 생성하지 않습니다.

```text
if oracle ...
else if postgres ...
else if mariadb ...
```

Pagination, Upsert, Generated Key, Sequence, Lock, Time, JSON, LOB, Identifier 같은 차이는 CPF Data Capability 경계가 처리합니다.

Generated Domain DB의 소유권과 Runtime Query 조립은 다음처럼 분리합니다.

```text
cpf-tools/db/canonical/generated-domain-schema.json       # 2-table 논리 정본
        ↓ cpf-tools/db/render_generated_domain_template.py
cpf-tools/db/generated/domain-template/{vendor}/          # Install/Seed/Migration/Rollback/Verify

cpf-starters/data/persistence/src/main/resources/
  cpf-generated-domain-dialect/{vendor}/mybatis/          # CPF Data 소유 Runtime Mapper Template
        ↓ generated build.gradle: -PcpfDbVendor=<vendor>
build/generated-resources/cpf-vendor/                     # 선택한 Mapper만 조립
```

Generated Project의 `src/main/resources` 아래에 Vendor별 SQL/Mapper를 복제하지 않습니다. `cpfDbVendor`를 바꿔도 Java Source와 Git Working Tree는 바뀌지 않고, `prepareCpfVendorResources`가 선택한 하나의 Mapper만 `build/generated-resources/cpf-vendor` overlay로 조립합니다.

---

## 13. Stateless lifecycle state / user modification protection

Generated Project에 Lock/ownership/manifest를 저장하지 않습니다. Generator는 정본 입력과 현재 Template에서 expected seed를 재계산하고, 사용자 수정 보호에 필요한 실행 상태만 다음 transient 경로가 소유합니다.

```text
build/domain-generator/verification/cpf-<domain>/generation-state.json
```

이 상태는 state/Generator version, definition·Generator SHA-256, expected Generated file 경로·hash, 검증 결과를 저장합니다. `regenerate/upgrade/remove/restore`는 이 기준과 현재 파일을 대사하고 사용자 수정이 있으면 fail-closed합니다. 해당 `build/**` 상태를 임의 재생성하여 충돌을 숨기지 않습니다.

---

## 14. Dry-run

```console
cpf domain dry-run --file cpf-tools/generator/definitions/member/cpf-domain.yaml
```

Dry-run은 다음 변경 계획을 보여줍니다.

- Create / Update / Preserve / Remove candidate
- Module graph
- Public Starter graph
- DB role/prefix + canonical DB impact
- Profile/Provider selection
- User-owned modification conflict
- Migration/Config/OpenAPI/Test impact

파일을 쓰지 않으므로 코드 리뷰 전 계획 검토에 사용합니다.

---

## 15. Diff

```console
cpf domain diff --file cpf-tools/generator/definitions/member/cpf-domain.yaml --output cpf-member
```

Diff는 다음 Ownership을 구분합니다.

```text
GENERATED_OWNED
USER_OWNED
MERGEABLE
CONFLICT
OBSOLETE_GENERATED
```

사용자 변경을 “Generator 출력과 다르다”는 이유만으로 덮어쓰지 않습니다.

---

## 16. Regenerate

```console
cpf domain regenerate member
```

흐름:

```text
cpf-domain.yaml
→ stateless expected-seed comparison
→ current file ownership/hash
→ user modification detection
→ generated-owned update
→ conflict stop
→ transient generation-state/expected-file hash update
```

동일 입력과 동일 Template 버전에서는 Generated-owned 영역이 deterministic하게 재현됩니다.

---

## 17. Upgrade

```console
cpf domain upgrade member
```

Framework 계약 변경을 다음 영역에 함께 반영합니다.

- Public Starter coordinate
- package/import
- config key
- Base Class/Annotation
- API skeleton/OpenAPI
- DB migration hook
- Test fixture
- transient expected-file baseline

Compatibility shim을 영구적으로 중첩하여 구 구조와 새 구조를 동시에 생성하지 않습니다.

---

## 18. Remove

```console
cpf domain remove member --file cpf-tools/generator/definitions/member/cpf-domain.yaml
```

삭제 계획은 먼저 다음을 확인합니다.

1. Generated/User ownership
2. 다른 Module Consumer
3. 사용자 Source
4. DB artifact retention/rollback
5. OpenAPI/Frontend/Batch consumer
6. Config/Secret reference
7. 실행 plan

위 명령은 제거 계획만 출력합니다. 실제 제거는 계획을 검토한 뒤 `cpf domain remove member --file cpf-tools/generator/definitions/member/cpf-domain.yaml --apply`로 수행합니다. Generator는 자신이 소유한 안전한 범위만 제거하고 User-owned Source와 승인되지 않은 DB 상태는 보존합니다.

---

## 19. 사용자 수정 영역 보호

Source Ownership은 명시적입니다.

```text
Generated-owned skeleton  → regenerate 가능
User-owned business code  → overwrite 금지
Generated declaration     → transient expected-file hash로 추적
Extension point            → 사용자 확장 대상
```

Comment pattern이나 heuristic만으로 사용자 코드 여부를 추측하지 않습니다.

Conflict가 발생하면 Generator는 실패하고 Diff를 제공합니다.

---

## 20. 전체/개별 생성

하나의 Generator Engine/Template로 다음을 모두 수행합니다.

- 모든 정의 Domain 일괄 생성
- 특정 Domain 하나 생성
- 신규 Domain 추가
- dry-run
- diff
- regenerate
- upgrade
- remove
- idempotent rerun

“All Generator”, “Single Generator”, “Verification Generator”가 서로 다른 Template를 사용하지 않습니다.

---

## 21. Root-level Generated Customer Domain / Verification Surface

CPF Generator의 CURRENT Customer Project Root naming은 **`cpf-<domain>/`**입니다. CPF 회귀 Repository에는 서로 다른 성격의 두 Domain `cpf-member/`, `cpf-external/`을 실제 Generator Output으로 유지합니다.

```text
cpf-member/
└─ online/

cpf-external/
└─ online/

Batch는 두 Generated Root의 산출물이 아니며 프로젝트 초기 구성에서 `cpf-starter-batch`로 별도 선택합니다.
```

Generated Project에는 `.cpf/**`, root `cpf-domain.yaml`, lock, ownership/manifest, README, verification, DB Vendor tree를 영구 저장하지 않습니다. Fresh input은 `cpf-tools/generator/definitions/<domain>/cpf-domain.yaml` 또는 explicit `--file`, transient state/DB3 Evidence는 `build/domain-generator/verification/**`가 소유합니다.

### `cpf-member/` - MBR 회귀 Domain

- `online` 업무 Source만 생성합니다. Batch는 초기 프로젝트 구성의 별도 Capability입니다.
- `MBR_SAMPLE_TX` 거래가 `MBR_sample_item` + `MBR_sample_item_idem` 2-table 계약으로 Create → Read → Search/Page/Slice/Cursor → Update/Delete → Idempotency/DB 확인을 검증합니다.
- `CUSTOMER_BUSINESS_DB`, `MBR_*` table prefix, Public Starter, 3단 Base Class를 사용합니다.

### `cpf-external/` - EXS 회귀 Domain

- `online` 업무 Source만 생성합니다. Batch는 초기 프로젝트 구성의 별도 Capability입니다.
- `EXS_SAMPLE_TX` 거래가 동일한 `EXS_sample_item` + `EXS_sample_item_idem` 2-table lifecycle을 독립 SystemCode/Prefix로 검증합니다.
- `member`와 다른 설정값만으로 동일 Generator Engine/Template가 동작함을 확인합니다.

두 Root는 고객이 받는 Generated Domain 구조를 그대로 검증하는 **실제 생성물**입니다. CPF BOM/Publication/Deploy Inventory에는 포함하지 않습니다. Source를 사람이 직접 고쳐 Generator 결함을 숨기지 않고, Framework/Starter/DB/Annotation/Base hierarchy 변경 시 같은 Generator로 재생성합니다.

## 22. Fresh Genericity Verification

`cpf-member/`와 `cpf-external/`만으로도 두 개의 독립 Domain을 검증하지만, Template가 이름에 특수화되지 않았는지 제3 임의 Domain을 fresh 생성해 확인합니다.

```text
build/domain-generator/verification/<scenario>/
```

또는 Repository 외부 Temp Directory를 사용합니다.

예시:

```text
name=contract
systemCode=CTR
tablePrefix=CTR
```

검증 흐름:

```text
fresh generate
→ compile
→ unit/integration test
→ sample transaction runtime
→ representative DB query
→ diff / regenerate / idempotency
→ deterministic normalized diff / hash
→ cleanup
```

Fresh output은 최종 CPF Source가 아니므로 검증 후 제거합니다. 반대로 `cpf-member/`와 `cpf-external/`은 공식 회귀 Root이므로 일반 cleanup 대상으로 취급하지 않습니다.

### Genericity 금지선

- Template/Engine에 `member`, `MBR`, `external`, `EXS` 이름을 조건문으로 하드코딩하지 않습니다.
- Generated Java/SQL/Config를 Vendor별 fork하지 않습니다.
- Generated Source가 Internal Starter leaf/package를 직접 참조하지 않습니다.
- Domain별 Physical DB name을 만들지 않습니다.

## 23. 생성 결과 Acceptance

### Build/Dependency

- Public Starter only
- Internal leaf direct dependency 0
- Top-level Profile exactly-one
- Provider collision 0

### Source

- 정확한 3단 Base Class 구조
- CPF Annotation 실제 Runtime 소비
- Context/Error/Validation/Security/Audit 연결
- 사용자 수정 영역 보호

### Database

- `CUSTOMER_BUSINESS_DB + tablePrefix`
- Domain Physical DB name 0
- Business Source raw vendor branch 0
- Oracle/PostgreSQL/MariaDB Source neutral

### Lifecycle

- Framework definition/explicit `--file` + generated Source tree; Project-local lock/ownership metadata 없음
- dry-run / diff
- regenerate / upgrade / remove
- idempotent rerun
- transient generation-state/expected-file hash

### Consumer

- Online/Batch compile
- OpenAPI 생성
- Frontend/ADM 등 필요한 Consumer 연결 가능
- 운영 추적 transactionId/audit 제공

---

## 24. API 생성 후 업무 개발

Generator는 업무 Rule을 대신 작성하지 않습니다. 생성 후 개발자가 다음을 완성합니다.

1. API DTO와 Validation
2. Application Command/Query
3. Domain invariant
4. Persistence mapping
5. Transaction boundary
6. Idempotency/Concurrency
7. External Integration/Resilience
8. Error reference/Common Catalog
9. Security/Permission/Audit/Masking
10. Unit/Integration/Fault/Concurrency test
11. OpenAPI/Generated Client
12. 운영 추적/상태 조정 인계

구현 순서와 예제는 `cpf-docs/guides/01_개발자매뉴얼.pdf`를 사용합니다.

---

## 25. Batch 생성 후 업무 개발

1. Job/Step/Tasklet/Chunk 선택
2. Reader/Processor/Writer
3. JobParameter/Metadata
4. Checkpoint/Restart
5. Retry/Skip/Rollback
6. Partition/Worker/Lease/Fencing
7. Scheduler/Misfire
8. Center-Cut
9. UNKNOWN/Reconcile
10. Process Kill/Restart
11. ADM Batch 운영 인계

상세 절차는 `cpf-docs/guides/02_배치개발매뉴얼.pdf`를 사용합니다.

---

## 26. CPF Education과의 관계

`cpf-education`는 Generator Template가 아니라 CPF 기능을 학습·실행하는 Reference/Education Source입니다. 승인 TARGET은 `cpf-education`이며 Source 전환이 완료된 뒤에만 CURRENT로 표기합니다.

역할 차이:

| 자산 | 목적 |
|---|---|
| Generator Template | 고객 Domain Source 생성 |
| `cpf-member/` + `cpf-external/` | 동일 Domain-neutral Generator의 Root-level 회귀 Customer Domain |
| transient verification | 제3 임의 Domain genericity 확인 |
| `cpf-education` | CPF Golden Path 학습/실행/오류/Reconcile 예제 |

Generator 결과는 `cpf-education`가 소비하는 Public API/Starter/Class/Annotation Golden Path와 맞추고, `cpf-education` 전환 후에도 같은 Contract를 유지하며, 두 Root 회귀 Domain과 제3 임의 Domain으로 재현성과 일반성을 함께 확인합니다.

---

## 27. Troubleshooting

### Profile collision

**증상**: 하나의 Deployable에 두 개 이상의 Top-level Profile이 들어감.  
**조치**: `modules/features`를 확인하고 `web-api`, `secure-api`, `bff`, `event`, `batch` 중 Deployable 목적에 맞는 하나만 선택합니다.

### Provider collision

**증상**: Redis와 Valkey, 두 Persistence Provider 등 동일 종류 Provider가 동시에 Classpath에 존재.  
**조치**: `cpf-domain.yaml`에서 Provider를 하나로 명시합니다. Framework가 임의 우선순위를 선택하지 않습니다.

### User modification conflict

**증상**: transient generation-state의 expected-file hash와 현재 파일이 다름.
**조치**: `cpf domain diff --file <definition> --output <cpf-domain-root>`로 변경을 확인하고 사용자 로직을 User-owned 영역/Public extension point로 옮긴 뒤 regenerate합니다.

### Unsupported deployment DB vendor

**증상**: 배포/DB Lifecycle 설정에서 CPF 공식 지원 범위 밖의 Vendor가 선택됨.  
**조치**: 중앙 DB Lifecycle 환경에서 Oracle, PostgreSQL, MariaDB 중 하나를 선택하고 Generated Application Source는 변경하지 않습니다.

### Domain별 DB name 생성

**증상**: 업무 Domain마다 별도 Physical DB 이름이 생성 결과에 나타남.  
**조치**: `database.role=CUSTOMER_BUSINESS_DB`와 `tablePrefix` 계약을 사용합니다.

### Internal dependency가 생성됨

**증상**: 생성 Build가 `cpf-starters/**` Internal leaf project/package를 직접 참조.  
**조치**: Public Starter Catalog를 사용하도록 Template/Generator Contract를 바로잡습니다.

### 3단 Base 중 Domain Base가 비어 있음

**증상**: `DomainBaseController/Service/Dao`가 상속만 하고 아무 Domain 공통 책임이 없음.  
**조치**: Domain 공통 Context/Validation/Error/Audit/정책/Template Hook을 실제 책임으로 배치합니다. 계층을 생략하거나 빈 ceremonial class로 남기지 않습니다.

---

## 28. Reviewer Checklist

Generator 변경을 리뷰할 때 파일 생성 여부보다 다음을 봅니다.

- 한 입력 파일로 결과를 재현할 수 있는가?
- Public Surface만 소비하는가?
- 3단 Class Golden Path가 실제 책임을 갖는가?
- Customer Business DB/Prefix/3-Vendor 계약이 맞는가?
- 사용자 수정이 재생성에서 보호되는가?
- `member`/`external`과 제3 Fresh Genericity Domain이 같은 Engine/Template를 사용하는가?
- Generator 변경이 OpenAPI/Test/Config/DB/Education까지 일관되게 반영되는가?
- Remove/Upgrade가 사용자 파일과 DB state를 안전하게 다루는가?

---

## 29. 관련 문서

- `README.md` — CPF 전체 전체 구성와 시작점
- `cpf-docs/guides/00_프레임워크안내.pdf` — Architecture/Starter/Topology
- `cpf-docs/guides/01_개발자매뉴얼.pdf` — Generated Domain 실제 개발
- `cpf-docs/guides/02_배치개발매뉴얼.pdf` — Batch 개발/Restart·Reconcile
- `cpf-docs/development/EDU_GUIDE.md` — Education Golden Path
- `cpf-docs/architecture/CENTRAL_DB_VENDOR_PACK_GUIDE.md` — Canonical DB/Vendor Renderer
- `cpf-docs/operations/DB_OPERATIONS_GUIDE.md` — DB 운영 Lifecycle
- `cpf-docs/deliverables/기술표준서.pdf` — 코드/설정/SQL 표준
- `cpf-docs/deliverables/데이터베이스표준서.pdf` — DB 설계/운영 표준

## 10_20 Generator Currentization

### CURRENT Source (`9d751c3`)

- Canonical Engine: `cpf-tools/generator/engine/cpf_domain_generator.py`
- PS/Bash entry는 Core Engine을 호출하는 thin adapter 방향으로 정리되었다.
- 회귀 결과 Root는 `cpf-member/`, `cpf-external/`; Generated Runtime은 `online/` 하나다. Batch는 초기 프로젝트 구성에서 `cpf-starter-batch`를 별도 선택한다.
- CURRENT retained Root에는 `.cpf/**`, root yaml/lock/ownership을 영구 저장하지 않는다. Fresh input은 Framework definition 또는 explicit `--file`이다.
- 고객 Runtime 구현 폴더에 `member-api`, `external-api`, `<domain>-api`를 쓰지 않는다.

### CURRENT 최소 Surface와 남은 Target

```text
cpf-<domain>/
└─ online/       # Generated Online 업무 Source

`batch/`, `jobpack/`, 별도 `domain/` Module은 Generated Domain 산출물이 아닙니다.
```

위 stateless 최소 Surface는 CURRENT입니다. Generated Project에 `.cpf/**`, root lifecycle yaml/lock/ownership, `README.md`, `verification/`, `db/canonical/`, `db/vendors/`, Vendor별 Source 3벌, 빈 폴더를 재도입하지 않습니다. Canonical `cpf-domain.schema.json`, Schema/Engine validation parity, write-before preflight, explicit `--file` collision 검증, versioned upgrade/restore, 중앙 DB3 Template·선택 Vendor build overlay를 한 계약으로 유지하고 exact-SHA cross-platform/runtime Evidence로 재검증합니다.
