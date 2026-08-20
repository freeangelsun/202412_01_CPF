# CPF Repository Surface와 Root Ownership

- 본 문서는 현재 Repository Surface와 Target Invariant를 정의한다. 완료된 구조 이관을 다시 수행하는 문구는 금지하며 exact SHA의 완료 여부는 현재 실행 Evidence로 판정한다.

## 1. Target Root Surface

| Root | 역할 | Target |
|---|---|---|
| `cpf-core` | topology-independent 최소 Kernel Contract/Semantics | 유지. `api/context`, `api/error`, 필요한 `api/transaction`, `api/version`, genuine `spi`만 허용 |
| `cpf-starters` | Base/Common/Web/Data/Messaging/Integration/File/Notification/Security/Platform-Ops/Profile/Provider | 정식 Framework Capability Container |
| `cpf-admin` | CPF Platform Control Plane | 유지 |
| `cpf-backoffice` | Customer Business Management Plane | 유지, CPF Platform DB와 별도 `bzaDB` |
| `cpf-batch` | Batch/Scheduler/Center-Cut/Worker 독립 Runtime | 유지. 관계형 상태의 기본 Physical Target은 `cpfDB` |
| `cpf-gateway` | 독립 Gateway Runtime | 유지. 별도 gatewayDB를 만들지 않음 |
| `cpf-education` | Public API/Starter/Education/Golden Runtime Verification | Target Canonical Root. System Code `EDU`; Production Business Domain/DB Owner가 아님 |
| `cpf-tools` | Build/Generator/DB/Verification/Supply-chain | 유지 |
| `cpf-docs` | Canonical/Architecture/Development/Work/Evidence | 유지 |
| `cpf-member` | Generated Customer Business Domain Verification (MBR) | Root-level Generator Output. API+Batch+MBR_SAMPLE_TX를 유지하고 Build/Test Graph에서 검증. CPF Product Publication 제외 |
| `cpf-external` | Generated Customer Business Domain Verification (EXS) | Root-level Generator Output. API+Batch+EXS_SAMPLE_TX를 유지하고 Build/Test Graph에서 검증. CPF Product Publication 제외 |
| `gradle`, `.github` | Build/CI | 유지 |

다음 비정본/과거 Product Root 역할은 **현재 Product Surface에 재도입하지 않는다.**

- `cpf-common` — 현재 Owner는 `cpf-starters/common`
- `cpf-member`/`cpf-external` — CPF Product Module/Public Artifact로는 재도입 금지. 단 Generated Customer Project Root naming은 `cpf-<domain>/`이므로 이 두 이름은 Generated Domain 역할로 허용
- Education은 `cpf-education`만 Canonical Root로 인정하며 다른 Education Root/Package/Application 명칭의 Active Surface 재도입을 금지한다.

고객 Generated Domain은 Customer Domain Owner가 소유하며 Customer Project Root `cpf-<domain>/`에 생성한다. CPF 자체 검증 Repository에서는 사용자 승인된 회귀 Root `cpf-member/`와 `cpf-external/`만 유지한다. 그 외 Generated Domain은 Root에 만들지 않고 transient verification에서 검증한다.

## 2. Starter Target Physical Group

```text
cpf-starters/
├─ base/
├─ common/
├─ web/
├─ data/
│  ├─ persistence/
│  ├─ cache/
│  └─ lock/
├─ messaging/
├─ integration/
├─ file/
├─ notification/
├─ security/
├─ platform-operations/
└─ profiles/
```

`cpf-core` 외의 `*/core` Capability Module은 금지한다. `cpf-starters/foundation/**` 물리 Root는 존재하지 않으며 재도입을 금지한다. Gate는 `foundation` 또는 Capability `core`의 재생성을 실패 처리하고, 로컬 빈 Folder/Build Output은 Hygiene Script가 정리한다.

## 3. Root 생성 Gate

새 Root는 사용자가 Architecture로 명시적으로 승인하지 않는 한 생성하지 않는다. 신규 Capability는 먼저 위 `cpf-starters` 그룹에 귀속한다. Root 추가가 불가피하면 Canonical Requirement, Owner, Build, Publication, Consumer, Generator, Test, DB/Config 영향까지 동시에 정의해야 한다.

## 4. 금지

- 작업 편의를 위한 Root/Folder 임의 생성
- `cpf-common` 재생성
- Generated Root를 CPF Product Publication/BOM에 포함하거나 concrete Golden Source로 역사용 금지. `cpf-member/`/`cpf-external/`은 Customer Domain 검증 Root로 Build/Test에만 참여
- Capability별 `core` 디렉터리 재도입
- Internal Leaf를 Generated Domain이 직접 참조
- 빈 Folder/Build Output/Local Cache를 Product 산출물로 취급

## 5. Generated Customer Domain Root / Verification Surface

### 5.1 Current retained verification roots

```text
cpf-member/
├─ online/       # selected capability
├─ batch/        # selected capability
└─ domain/       # actual shared consumer가 있을 때만

cpf-external/
├─ online/
├─ batch/
└─ domain/       # actual shared consumer가 있을 때만
```

- member: logical Domain `member`, SystemCode MBR, Prefix MBR, `MBR_SAMPLE_TX`
- external: logical Domain `external`, SystemCode EXS, Prefix EXS, `EXS_SAMPLE_TX`
- `cpf-`는 Generated Project naming convention이며 CPF Product Module 의미가 아니다. Generated Project에는 `cpf-domain.yaml`, lock, ownership 같은 lifecycle metadata를 영구 저장하지 않는다.
- `README.md`, `verification/`, `db/canonical/`, `db/vendors/`, Vendor별 3벌, `<domain>-api`, `<domain>-common`, 빈 Directory는 Generated Customer Surface에 두지 않는다.
- true Public Contract Consumer가 있을 때만 `contract/`를 추가한다.

### 5.2 Dynamic customer project naming

Customer Project의 일반 규칙은 `cpf-<domain>/`이다. CPF Framework Repository Root에서는 retained `cpf-member/`, `cpf-external/` 외 임의 Generated Root 생성은 금지한다.

### 5.3 Fresh genericity output

```text
build/domain-generator/verification/<scenario>/
```

제3 임의 Domain을 fresh 생성해 `member/MBR/external/EXS` 하드코딩 0을 검증하고 cleanup한다. 이 cleanup은 retained `cpf-member/`/`cpf-external/`에는 적용하지 않는다.

### 5.4 Final surface invariant

```text
CPF Product Module named cpf-member/cpf-external          = 0
Retained Generated Customer Root cpf-member               = 1
Retained Generated Customer Root cpf-external             = 1
Generated Project internal README/verification/vendor tree = 0
Transient arbitrary domains after verification             = 0
Unauthorized new repository-root files/directories         = 0
```

retained generated roots를 lifecycle 검증 중 일시 삭제하면 같은 작업 단위에서 Generator로 재생성한다. 일반 Hygiene/Delete Manifest로 영구 삭제하지 않는다.

## Cross-platform Tooling Surface

CPF Tooling의 공개 개발자 명령 Surface는 Windows/Linux에서 의미가 같아야 한다.

정상:

```text
cpf domain ...
cpf db ...
cpf verify ...
```

Repository-local 물리 launcher:

```text
cpf-tools/runtime/cli/cpf
cpf-tools/runtime/cli/cpf.bat
```

설치/배포 후 사용자가 보는 논리 명령은 `cpf ...`로 통일한다. Root `bin/` 신규 생성은 Root Freeze에 따라 금지한다.

비정상:

```text
핵심 기능이 *.ps1에만 존재
핵심 기능이 *.sh에만 존재
Windows/Linux가 서로 다른 Generator Template/DB Renderer/Verification Engine 사용
한 OS에서만 제공되는 기능을 전체 완료 처리
```

Shell wrapper는 Product Capability가 아니라 OS entrypoint이며 동일 OS-neutral Core를 호출한다.




## Generated Customer Domain Current Target

Current Target은 다음 두 Root Generated Customer Domain을 동일 Domain-neutral Generator로 실제 생성·유지하는 것이다.

```text
cpf-member/   = logical member / MBR
cpf-external/ = logical external / EXS
```

둘은 CPF Product Module/Public Artifact가 아니며, 동일 Canonical Schema·Naming Strategy·Generator Engine·Template으로 생성한다. `CUSTOMER_BUSINESS_DB`, Online 회귀, Sample Transaction, DB3, Build/Test를 함께 검증하고 최종 결과물에 포함하되 Generated Project에는 필요한 Source Surface만 둔다. Batch는 Generated Domain과 별개로 프로젝트 초기 구성에서 `cpf-starter-batch`를 선택할 때만 포함한다.

Legacy single-canary/transient-only 정책은 Current Target이 아니다. 단, 제3 임의 Domain은 genericity 추가 검증용 transient output으로 생성 후 cleanup할 수 있다.

## Currentization SHA 의미

- `currentization_source_sha`: `e6f2e7a599a948277b118967d0fb5f840f65c114` (`18_19`) — 본 문서 현행화 시 비교 기준으로 사용한 Source.
- `execution_source_sha`: 각 Developer/Codex/QA 세션 시작 시 최신 `origin/master`에서 동적으로 확인한다.
- `verified_sha`: Build/Test/Runtime/Evidence를 실제 실행한 exact SHA다.

`currentization_source_sha`를 이후 세션의 영구적인 "현재 master"로 해석하지 않는다. 완료 판정과 Evidence는 `verified_sha`를 기준으로 한다.
