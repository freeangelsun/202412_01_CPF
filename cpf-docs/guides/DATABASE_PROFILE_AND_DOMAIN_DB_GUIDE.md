# CPF 데이터베이스 Profile과 업무영역 DB 가이드

## 1. 목적

CPF는 Platform Module과 Generated Domain마다 DB Vendor, Host, Database/Schema와 계정을 독립적으로 구성한다. 이 문서는 Profile 구조, 논리 DB와 물리 DB Mapping, 계정 분리와 설치 선택 방법을 설명한다.

## 2. 식별

각 DB Owner:

- domainName
- systemCode
- moduleName
- logicalDatabase
- physicalDatabase
- schema
- vendor

예:

```text
payment / PAY / cpf-payment
logicalDatabase = payDB
vendor = postgresql
physicalDatabase = cpf_business
schema = pay
```

## 3. 공식 Vendor

- `mariadb`
- `postgresql`
- `oracle`

업무영역별로 다른 Vendor를 사용할 수 있다.

## 4. Profile 예시

```json
{
  "profileId": "local-postgresql",
  "environment": "local",
  "modules": {
    "admin": {
      "enabled": true,
      "systemCode": "ADM",
      "vendor": "postgresql",
      "host": "127.0.0.1",
      "port": 5432,
      "database": "cpf_platform",
      "schema": "adm",
      "adminUser": "cpf_adm_admin",
      "migrationUser": "cpf_adm_migration",
      "runtimeUser": "cpf_adm_runtime",
      "secretRef": "env://CPF_ADM_DB_PASSWORD"
    }
  }
}
```

Git에는 Secret Reference만 저장한다.

## 5. 계정 분리

| 계정 | 권한 |
|---|---|
| Admin | User/Schema/권한 Provision |
| Migration | DDL, Migration, Seed |
| Runtime | 업무 DML과 필요한 Query |
| Readonly | 운영 조회 |
| Backup | Backup |
| Verify | DR/설치 검증 |

Runtime 계정에 불필요한 DDL 권한을 주지 않는다.

## 6. 논리 DB

권장 논리 Owner:

```text
cpfDB
cmnDB
admDB
bzaDB
batDB
refDB
<generated-domain>DB
```

물리 구성은 고객 환경에 따라 Database 또는 Schema로 Mapping한다.

## 7. Multi Datasource

지원 구성:

- Module별 독립 DB
- 같은 DB의 Schema 분리
- Read Replica
- Read/Write Routing
- Transaction 전용 DataSource
- 운영 조회 DataSource

업무 Transaction 중 무분별한 DataSource 전환을 금지한다.

## 8. Read Replica

- 일관성 요구 Query는 Primary
- 지연 허용 조회만 Replica
- Transaction 내 Read-after-write는 Primary
- Replica Lag Metric
- Failover
- Hint Allowlist

## 9. 설치 선택

전체 Platform:

```powershell
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun
```

특정 SystemCode:

```powershell
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 `
  -SystemCode ADM,BAT `
  -RequireRun
```

Generated Domain:

```powershell
pwsh -File .\cpf-tools\scripts\initialize-generated-domain-databases.ps1 `
  -SystemCode PAY `
  -Operation bootstrap `
  -Apply
```

## 10. 통합 Installer

```powershell
pwsh -File .\cpf-tools\scripts\initialize-databases.ps1 `
  -Scope all `
  -All `
  -Apply
```

Scope:

- platform
- generated
- all

## 11. Existing DB 안전성

```text
예상 Object 0개
→ Fresh Install

예상 Object 모두 존재 + Manifest 일치
→ 설치 완료

일부 Object 존재
→ Partial Install 실패

Object 존재 + 구조 불일치
→ Drift/Migration 실패
```

Fresh Install DDL을 Upgrade에 사용하지 않는다.

## 12. Seed Mode

- profile
- product
- none
- all

`all`은 격리된 Local/Test에서만 사용한다.

## 13. Generated Domain Profile

Generator는 다음을 생성한다.

```text
cpf-payment/deploy/database/database-profile.json
```

Domain은 자신의 Vendor와 계정을 소유한다. 중앙 Script에 PAY, INS 같은 고정 목록을 추가하지 않는다.

## 14. EXS

EXS는 고정 Platform DB가 아니다. 필요하면 일반 Generated Domain으로 생성한다.

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName external `
  -SystemCode EXS `
  -Apply
```

## 15. Runtime Mapper

Vendor별 Resource:

```text
mybatis/vendor/{vendor}/mapper/{domain}/
```

Profile Vendor와 정확히 일치하는 경로만 로드한다.

## 16. Transaction

분산된 Owner DB를 하나의 Local Transaction으로 묶지 않는다.

- Public API
- Event
- Saga
- Outbox
- Reconciliation

으로 일관성을 관리한다.

## 17. Failover

DB Failover 시:

- Connection Pool 폐기
- New Endpoint
- Readiness
- In-flight 결과
- Unknown Transaction
- Replica Lag
- Runtime 재개
- Audit

## 18. Profile 검증

- Vendor
- Port
- Database/Schema
- User 분리
- Secret Reference
- Logical Owner
- Duplicate Mapping
- Production Memory Adapter
- Unknown Vendor
- Cross-domain 직접 접근

## 19. 환경별 관리

### Local

편의 기본값을 허용하되 Secret 원문 Git 저장 금지.

### CI

격리 DB와 자동 Provision.

### Staging

Production과 같은 Vendor/Schema 정책.

### Production

- 명시 Profile
- 최소 권한
- Secret Manager
- Backup
- Monitoring
- Change Approval
- No Default Password

## 20. 체크리스트

- [ ] Owner별 DB Mapping이 명확하다.
- [ ] Vendor는 공식 3종 중 하나다.
- [ ] 계정 권한이 분리됐다.
- [ ] Secret Reference를 사용한다.
- [ ] Partial Install과 Drift를 실패시킨다.
- [ ] Generated Domain이 자신의 Profile을 소유한다.
- [ ] Cross-domain DB 직접 접근이 없다.
- [ ] Read Replica 일관성 정책이 있다.
