# CPF 업무영역 Generator 가이드

## 1. 목적

CPF Generator는 신규 업무영역을 CPF 제품 표준과 동일한 구조로 생성한다. 특정 업무명에 종속된 복사본을 만들지 않고 하나의 Golden Template을 사용한다.

정본 명령:

```powershell
cpf-tools/generator/create-domain.ps1
```

## 2. 생성 원칙

- `DomainName`과 `SystemCode`를 분리한다.
- 공식 Module은 `cpf-` 접두사를 사용한다.
- Java Root Package는 `com.cpf.<domain>`을 사용한다.
- 업무영역은 자신의 API, Application, Domain, Adapter와 DB를 소유한다.
- Public API/SPI만 사용한다.
- Oracle, PostgreSQL, MariaDB를 같은 제품 규칙으로 생성한다.
- 사용자 수정 영역을 덮어쓰지 않는다.
- 생성 계획과 충돌을 Apply 전에 확인한다.

## 3. 입력

### 3.1 식별

| 입력 | 설명 | 예 |
|---|---|---|
| `DomainName` | 사람이 읽는 업무명 | `payment` |
| `SystemCode` | 3자리 대문자 내부 코드 | `PAY` |
| `ModuleName` | Gradle Module | `cpf-payment` |
| `PackageName` | Java Package | `com.cpf.payment` |
| `SchemaName` | 논리 Schema | `payDB` |
| `TablePrefix` | Table Prefix | `pay_` |
| `Port` | 독립 Runtime Port | `18080` |

### 3.2 Database

- `DatabaseVendor`
- Host / Port
- Database / Schema
- Admin / Migration / Runtime User
- Secret Reference
- Provision 여부
- Product / Optional / Test Seed 정책

### 3.3 Capability

- `database`
- `remote-call`
- `messaging`
- `file`
- `external`
- `batch`
- `center-cut`
- `security-audit`
- `ui`
- `bza-menu`

실제 Parameter 이름과 Default는 Script Help를 정본으로 한다.

## 4. Dry Run

첫 실행은 반드시 Dry Run으로 수행한다.

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName "payment" `
  -SystemCode "PAY" `
  -DatabaseVendor "postgresql" `
  -Capabilities "database,remote-call,messaging" `
  -DryRun
```

Dry Run 결과:

- 생성 파일 목록
- 변경 파일 목록
- 충돌
- 예약 식별자
- Port와 Route
- DB와 Schema
- Generator 소유 영역
- 적용 순서
- 삭제 예정 파일
- 예상 Build Project

## 5. Apply

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName "payment" `
  -SystemCode "PAY" `
  -DatabaseVendor "postgresql" `
  -Capabilities "database,remote-call,messaging" `
  -Apply
```

Apply는 Dry Run과 같은 입력으로 수행한다. Plan Hash를 지원하는 경우 동일 Plan임을 확인한다.

## 6. 생성 구조

```text
cpf-payment/
├─ build.gradle
├─ README.md
├─ deploy/
│  ├─ database/
│  ├─ runtime/
│  └─ manifests/
├─ src/main/java/com/cpf/payment/
│  ├─ api/
│  ├─ application/
│  ├─ domain/
│  ├─ adapter/
│  └─ config/
├─ src/main/resources/
│  ├─ application.yml
│  ├─ db/
│  └─ mybatis/
└─ src/test/
```

## 7. 기본 업무 예제

DB Capability는 `${TablePrefix}sample_item`을 사용한 최소 CRUD/Search/Paging/Validation 예제를 생성한다.

포함 범위:

- Create
- Detail
- Search
- Page
- Update with `expectedVersion`
- Duplicate 오류
- Validation 오류
- Transaction Rollback
- Vendor별 SQL
- OpenAPI
- Unit/Integration Test

이 예제는 Golden 구조 검증용이며 고객 업무를 추정하지 않는다.

## 8. Public Boundary

생성 Source는 다음만 사용한다.

```text
com.cpf.core.api.*
com.cpf.core.spi.*
com.cpf.common.api.*
```

금지:

```text
com.cpf.core.internal.*
com.cpf.core.common.*
다른 업무 Domain의 persistence package
```

## 9. Database Artifact

각 Vendor에 대해 생성한다.

```text
deploy/database/
├─ mariadb/
│  ├─ install
│  ├─ migration
│  ├─ rollback
│  └─ verify
├─ postgresql/
└─ oracle/
```

생성 항목:

- Provision
- Empty Install
- Product Seed
- Optional/Test Seed
- Migration
- Rollback
- Verify
- Schema Manifest
- Checksums
- Vendor별 MyBatis Mapper

## 10. Runtime Profile

Generated Runtime은 환경별 Profile을 분리한다.

- local
- test
- development
- staging
- production

Production Profile에는 다음 원칙을 적용한다.

- Memory Adapter 금지
- Secret Reference
- 외부 공개 기본 거부
- Secure Header
- DB Pool 상한
- Timeout
- Health
- Structured Log
- Management Endpoint 최소화

## 11. Registry와 Endpoint

Generator는 Registry 등록 후보를 생성한다.

- serviceId
- systemCode
- moduleId
- Endpoint
- Health
- Protocol
- visibility
- gatewayAllowed
- Zone/Cell
- Version

내부 Endpoint는 외부 Gateway Binding 후보에 자동 포함하지 않는다.

## 12. Messaging Capability

생성 항목:

- Event DTO
- Schema Version
- Publisher Service
- Outbox Repository
- Consumer
- Inbox/Idempotency
- DLQ 처리 Port
- Test Fixture

## 13. File Capability

생성 항목:

- File Request/Result
- Endpoint Metadata
- Credential Reference
- Checksum
- 상태와 이력
- 실패·결과 불명
- Test

## 14. Batch Capability

업무 Job Pack SPI를 생성한다.

- Job ID
- Version
- Parameter Schema
- Job/Step 구성
- Retry/Timeout
- Checkpoint
- Testkit
- BAT Registry Metadata

## 15. Center-Cut Capability

- Target Provider
- Item Handler
- Partition Key
- Claim/Fencing DTO
- Internal Endpoint
- 결과 상태
- 재처리 Test

## 16. UI와 BZA Menu

UI Capability는 독립 Feature Directory를 만든다.

```text
features/payment/
├─ api
├─ model
├─ state
├─ components
├─ pages
└─ tests
```

BZA Menu Seed는 중복 Route/Permission을 검사한다. Frontend Route와 Backend Permission을 함께 생성한다.

## 17. 사용자 수정 보호

`generator-ownership.json`은 다음을 기록한다.

- Generator Version
- Template Version
- 파일 경로
- 소유 유형
- 생성 당시 Checksum
- 재생성 정책

재실행 동작:

| 상태 | 처리 |
|---|---|
| Generator 소유 + 미수정 | 안전 갱신 |
| Generator 소유 + 사용자 수정 | 충돌 보고 |
| 사용자 소유 | 변경 금지 |
| 삭제된 Template 파일 | 삭제 Plan에 표시 |
| 신규 Template 파일 | 생성 Plan에 표시 |

## 18. 기존 Domain 동기화

```powershell
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope Database
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope Database -Apply
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope AllGeneratorOwned -Apply
```

Apply 전에 사용자 수정 충돌을 확인한다.

## 19. 충돌 검사

- DomainName
- SystemCode
- Module
- Package
- Port
- Route
- Execution ID
- DB/Schema
- Table Prefix
- Config Key
- Queue/Topic
- Cache Namespace
- Log Directory
- BZA Menu
- Permission Code

## 20. 예약 코드

CPF 공식 SystemCode와 충돌하는 코드는 거부한다.

- CPF
- CMN
- ADM
- BZA
- BAT
- GWY
- REF
- MBR

`AllowReserved`는 제품 내부 관리 작업 외에는 사용하지 않는다.

## 21. 검증 시나리오

최소 두 개의 임의 Domain을 사용한다.

1. 서로 다른 Domain 생성
2. Build/Test
3. 3 Vendor DB Artifact 비교
4. Public Boundary 검사
5. Route/Port 충돌
6. SystemCode 중복
7. 같은 입력 재실행
8. 사용자 수정 파일 보호
9. Remove
10. 재생성
11. Generated Domain 간 구조 정규화 비교
12. Runtime Profile과 Registry Metadata 확인

## 22. 제거

제거는 Manifest를 기준으로 수행한다.

- Gradle Include
- Module Directory
- Local Federation
- Runtime Registry 후보
- Generated Seed
- DB Artifact
- Guide Link
- Test Fixture

운영 DB와 고객 데이터는 제거 Command가 자동 삭제하지 않는다.

## 23. Evidence

- 기준 Commit
- Generator/Template Version
- 정확한 Command
- Credential을 제외한 Parameter
- Dry Run Plan
- 생성·변경·삭제 파일
- 충돌 Test
- Build/Test
- DB Artifact Hash
- 사용자 수정 보호 결과

## 24. 완료 체크리스트

- [ ] 하나의 Golden Template을 사용한다.
- [ ] 특정 업무명 예외가 없다.
- [ ] Public API/SPI만 사용한다.
- [ ] 3개 Vendor Artifact를 생성한다.
- [ ] 외부 공개 기본 거부다.
- [ ] 사용자 Source를 덮어쓰지 않는다.
- [ ] 충돌을 생성 전에 검출한다.
- [ ] 제거·재생성이 가능하다.
- [ ] README/OpenAPI/Test/Evidence가 생성 결과와 일치한다.
