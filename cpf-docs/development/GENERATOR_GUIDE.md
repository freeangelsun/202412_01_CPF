# CPF Generated Domain Guide

## 1. 목적

CPF Generated Domain은 고객사의 신규 업무 주제영역을 사람이 복사해 만드는 방식이 아니라 **하나의 Golden Template + Metadata + 중앙 DB Vendor Template**으로 반복 생성하기 위한 제품 기능이다.

`MBR`, `LNG`, `ING`, `PAY`, `INS`, `CRM`은 모두 예시일 뿐이며 Generator 코드에 특정 업무 Domain 목록을 하드코딩하지 않는다.

## 2. 핵심 계약

생성형 Domain은 다음 Metadata만 다를 수 있다.

- `DomainName`
- `SystemCode` 3자리
- Module/Package 이름
- Schema/Database 이름
- Table Prefix
- Port
- 선택 Capability

동일 Capability 조합이면 Controller/Facade/Service/Port/Adapter/Repository/Mapper/DTO/Test/DB 논리 구조는 이름을 normalize했을 때 동일해야 한다.

기본 DB Sample은 `${TablePrefix}_sample_item` **1개**다. CPF 공통 기능을 사용하기 위해 각 업무 DB에 플랫폼 테이블을 복제하지 않는다.

## 3. 기본 생성 결과

DB를 사용하는 기본 생성형 Domain은 최소 다음을 포함한다.

```text
cpf-<domain>/
├─ build.gradle
├─ manifest/
│  ├─ domain-manifest.json
│  └─ generator-ownership.json
├─ src/main/java/com/cpf/<domain>/
│  ├─ common/base
│  ├─ common/contract
│  └─ reference/
│     ├─ controller
│     ├─ facade
│     ├─ service
│     ├─ port
│     ├─ adapter/local
│     ├─ adapter/remote
│     ├─ repository
│     ├─ dto
│     └─ validation
├─ src/main/resources/
├─ src/test/
└─ sql/*.candidate.sql     # 중앙 제품 Metadata에 반영할 검토 후보
```

Batch, External, Messaging, File, UI 등은 명시 Capability일 때만 추가한다. 미선택 Capability의 빈 Package/Config/Dependency를 남기지 않는다.

## 4. transactionId와 표준 Header

업무 Controller/DTO는 transport 식별자를 Body에 중복해서 받지 않는다.

- `transactionId`: 동일 업무 흐름 전체에서 하나를 승계한다.
- 외부/선행 호출이 유효한 transactionId를 주면 Core가 승계한다.
- 내부 독립 기동이면 Core가 34자리 transactionId를 생성한다.
- 세부 호출은 `segmentId`/`parentSegmentId`로 구분한다.
- `standardExecutionId`는 `O<시스템><기능><일련번호>` 형태의 실행 **정의 ID**이며 transactionId와 별개다.
- idempotency/user/operator/channel/trace/error/masking/audit는 CPF Core 공통 Filter/Context/Port 계약을 사용한다.

Generated Controller에 표준 Header 파싱/거래ID 생성/공통 오류 조립 코드를 반복 구현하면 Generator 결함으로 판정한다.

## 5. DB 정본

Generated Domain DDL/Seed/Mapper의 정본은 Generator Source 안의 문자열이 아니다.

```text
cpf-tools/db/vendor/<vendor>/domain-template/
├─ provision/
├─ install/
├─ seed/
├─ migration/
├─ runtime/mybatis/
├─ verify/
└─ rollback/
```

`create-domain.ps1 -ProvisionDatabase`는 DB 작업을 직접 구현하지 않고 `initialize-domain-database.ps1`를 호출한다.

변경 순서는 항상 다음과 같다.

```text
Vendor Domain Template
→ initialize-domain-database
→ Generated Repository/Mapper Contract
→ Service/API/Test
→ Runtime Evidence
```

## 6. 생성 Dry-Run

먼저 충돌만 검사한다.

```powershell
pwsh -File .\cpf-tools\scripts\create-domain.ps1 `
  -DomainName "lng" `
  -SystemCode "LNG" `
  -DatabaseVendor "mariadb" `
  -DryRun
```

예약 SystemCode, 기존 Domain/Module/Package, Route, Port, Schema/Table Prefix, Manifest 충돌이 있으면 생성하지 않는다.

## 7. Source + DB 생성

Credential은 명령문에 직접 쓰지 말고 환경변수로 전달한다.

```powershell
$env:CPF_DOMAIN_DB_ADMIN_USERNAME = "root"
$env:CPF_DOMAIN_DB_ADMIN_PASSWORD = "<local-secret>"
$env:CPF_DOMAIN_DB_USERNAME       = "cpf_lng_migration"
$env:CPF_DOMAIN_DB_PASSWORD       = "<local-secret>"

pwsh -File .\cpf-tools\scripts\create-domain.ps1 `
  -DomainName "lng" `
  -SystemCode "LNG" `
  -DatabaseVendor "mariadb" `
  -ProvisionDatabase `
  -Apply
```

Generator는 Source/Manifest를 만든 뒤 중앙 Vendor Template을 이용해 Domain DB Provision → Install → Product Seed → Verify를 수행한다.

## 8. 생성 결과 검증

```powershell
pwsh -File .\cpf-tools\scripts\verify-domain.ps1 `
  -DomainName "lng" `
  -SystemCode "LNG"
```

검증 대상:

- Module/Package/SystemCode/Manifest
- Base/Facade/Port/Local/Remote/Repository 구조
- 선택 Capability
- Golden Sample Table/Mapper 계약
- CRUD/Search/Paging/Validation
- transactionId/idempotency/Core Header 계약
- Build/Test

## 9. DB 검증

```powershell
pwsh -File .\cpf-tools\scripts\initialize-domain-database.ps1 `
  -DomainName "lng" `
  -SystemCode "LNG" `
  -DatabaseVendor "mariadb" `
  -Operation verify `
  -Apply
```

실제 DB에서 `${TablePrefix}_sample_item`의 PK/UK/Index/Check와 Product Seed를 확인한다.

## 10. CRUD Runtime Smoke

생성 Domain WAS를 실행한 뒤 별도 터미널에서 lifecycle smoke를 수행한다.

```powershell
pwsh -File .\cpf-tools\scripts\smoke-generated-domain-lifecycle.ps1 `
  -DomainName "lng" `
  -SystemCode "LNG" `
  -Apply `
  -ProvisionDatabase `
  -RunHttpCrud `
  -ServiceBaseUrl "http://localhost:8080"
```

Runtime Smoke는 동일 transactionId로 Create → Read → Update → Delete를 수행한다. 실제 WAS 포트는 생성 시 지정한 Port를 사용한다.

## 11. Gateway 호출 방식

Generated API는 일반 REST URL로 직접 호출할 수 있고, 외부 채널은 Gateway를 통해 `standardExecutionId`로 호출할 수 있다.

- Header 방식: `X-Cpf-Standard-Execution-Id: OLNGQY0001`
- URI 방식: `/cpf/execute/OLNGQY0001`

둘은 실행 정의를 선택하는 방법일 뿐 transactionId를 대체하지 않는다. Gateway/Core가 transactionId를 생성 또는 승계하고 하위 호출에 전파한다.

## 12. 안전 삭제

삭제 전 반드시 Dry-Run한다.

```powershell
pwsh -File .\cpf-tools\scripts\remove-domain.ps1 `
  -DomainName "lng" `
  -SystemCode "LNG" `
  -DryRun
```

다른 Source/SQL/Config가 참조하거나 사용자 소유 파일이 있으면 제거가 차단된다.

실제 Generated Source 제거:

```powershell
pwsh -File .\cpf-tools\scripts\remove-domain.ps1 `
  -DomainName "lng" `
  -SystemCode "LNG"
```

이 명령은 업무 DB를 자동 DROP하지 않는다. DB 삭제는 백업/승인/명시 절차로 수행한다.

## 13. 삭제 후 재생성 결정성

```powershell
pwsh -File .\cpf-tools\scripts\smoke-generated-domain-lifecycle.ps1 `
  -DomainName "lng" `
  -SystemCode "LNG" `
  -Apply `
  -RoundTrip `
  -ConfirmGeneratedSourceRemoval
```

첫 생성과 재생성의 Generator-owned Source Hash가 다르면 실패한다.

## 14. 서로 다른 Domain Parity

두 Generated Domain을 같은 Capability로 생성한 후:

```powershell
pwsh -File .\cpf-tools\scripts\check-generated-domain-parity.ps1 `
  -ReferenceDomain "lng" `
  -CandidateDomain "ing"
```

DomainName/SystemCode/Module/Package/Schema/TablePrefix/Port를 normalize한 뒤 파일 구조와 내용 Hash를 비교한다.

MBR은 최종적으로 이 Gate를 통과하는 checked-in Golden Reference Instance가 되어야 한다. 현재 수작업 MBR Source를 무조건 표준으로 간주하지 않는다.

## 15. ACC/EXS 같은 기존 고정 Domain

기존 고정 Domain에 실제 Consumer가 남아 있으면 DB나 Source를 먼저 삭제하지 않는다.

```text
Consumer Inventory
→ 기능 Owner 이관
→ Regression
→ Generated Domain/Customer Adapter로 전환
→ 고정 Module/Schema 제거
```

ACC나 EXS 이름을 Generator의 예약 Template로 하드코딩하지 않는다.

## 16. 완료 판정

다음이 모두 증명되어야 Generated Domain 기능을 `완료`로 판정한다.

- arbitrary Domain 생성 성공
- 실제 MariaDB bootstrap 성공
- CRUD/Search/Paging/Validation/Optimistic Lock/Commit/Rollback 성공
- Core Header/transactionId/Error/Idempotency 연결
- Local/Remote 호출 parity
- 생성→삭제→재생성 결정성
- 두 임의 Domain normalized parity
- 사용자 소유 파일 보호
- Generated DB 자동 DROP 없음
- 최신 Commit과 일치하는 실행 Evidence

실행하지 않은 항목은 `미검증`이다.

## R12 실행 정책

- 정본: `cpf-tools/generator/create-domain.ps1`
- 호환 launcher: `cpf-tools/scripts/create-domain.ps1` (별도 Template 금지)
- 기본 Library: `cpf-core`, `cpf-common`
- `cpf.common.runtime-mode=product`에서는 Memory adapter가 활성화되지 않아야 합니다.
- 생성 후 `core.common` import 0건, `cpf-common` dependency, compile/test/local lifecycle을 검증합니다.
