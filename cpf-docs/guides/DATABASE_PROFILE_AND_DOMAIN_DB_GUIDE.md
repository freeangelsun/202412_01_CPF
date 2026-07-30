# CPF 데이터베이스 프로필과 업무영역 DB 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 데이터베이스 설계자, 업무영역 개발자, 설치 담당자
> **목적**: 공급자별 연결·계정·논리 DB·다중 자료원·읽기 복제본을 업무 소유권에 맞게 구성한다.
> **관련 문서**: [데이터베이스 도구](CPF_DATABASE_TOOL_GUIDE.md) · [업무영역 생성기](CPF_GENERATOR_TOOL_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | 각 Module DB Owner와 Runtime Profile Owner |
| 이 문서로 완료하는 일 | Vendor·Logical DB·Credential·Pool·Read Replica·Multi-datasource를 명시하고 업무영역 간 DB 직접 접근을 차단한다. |
| 적용 범위 | Oracle/PostgreSQL/MariaDB Profile, CPF/CMN/ADM/BZA/BAT/Domain DB |
| 주요 독자 | DBA, Application 개발자, 설치 담당자, Generator 개발자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 목적

CPF는 Platform 모듈과 생성 업무영역마다 DB 공급자, 호스트, 데이터베이스/스키마와 계정을 독립적으로 구성한다. 이 문서는 프로필 구조, 논리 DB와 물리 DB 매핑, 계정 분리와 설치 선택 방법을 설명한다.

## 2. 식별

각 DB 소유자:

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

## 3. 공식 공급자

- `mariadb`
- `postgresql`
- `oracle`

업무영역별로 다른 공급자를 사용할 수 있다.

## 4. 프로필 예시

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

Git에는 비밀값 참조만 저장한다.

## 5. 계정 분리

| 계정 | 권한 |
|---|---|
| Admin | User/스키마/권한 Provision |
| 이관 | DDL, 이관, Seed |
| 실행 환경 | 업무 DML과 필요한 조회 |
| Readonly | 운영 조회 |
| 백업 | 백업 |
| Verify | DR/설치 검증 |

실행 환경 계정에 불필요한 DDL 권한을 주지 않는다.

## 6. 논리 DB

권장 논리 소유자:

```text
cpfDB
cmnDB
admDB
bzaDB
batDB
refDB
<generated-domain>DB
```

물리 구성은 고객 환경에 따라 데이터베이스 또는 스키마로 매핑한다.

## 7. 다중 자료원

지원 구성:

- 모듈별 독립 DB
- 같은 DB의 스키마 분리
- 읽기 복제본
- Read/Write 경로 선택
- 트랜잭션 전용 DataSource
- 운영 조회 DataSource

업무 트랜잭션 중 무분별한 DataSource 전환을 금지한다.

## 8. 읽기 복제본

- 일관성 요구 조회는 Primary
- 지연 허용 조회만 Replica
- 트랜잭션 내 Read-after-write는 Primary
- Replica Lag 지표
- Failover
- Hint 허용 목록

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

생성 업무영역:

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

범위:

- platform
- generated
- all

## 11. 기존 DB 안전성

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

`all`은 격리된 로컬/테스트에서만 사용한다.

## 13. 생성 업무영역 프로필

생성기는 다음을 생성한다.

```text
cpf-payment/deploy/database/database-profile.json
```

업무영역은 자신의 공급자와 계정을 소유한다. 중앙 Script에 PAY, INS 같은 고정 목록을 추가하지 않는다.

## 14. EXS

EXS는 고정 Platform DB가 아니다. 필요하면 일반 생성 업무영역으로 생성한다.

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName external `
  -SystemCode EXS `
  -Apply
```

## 15. 실행 환경 Mapper

공급자별 Resource:

```text
mybatis/vendor/{vendor}/mapper/{domain}/
```

프로필 공급자와 정확히 일치하는 경로만 로드한다.

## 16. 트랜잭션

분산된 소유자 DB를 하나의 로컬 트랜잭션으로 묶지 않는다.

- 공개 API
- 사건
- Saga
- 송신함
- Reconciliation

으로 일관성을 관리한다.

## 17. Failover

DB Failover 시:

- Connection Pool 폐기
- New 엔드포인트
- 준비 상태
- In-flight 결과
- 결과 불명 거래
- Replica Lag
- 실행 환경 재개
- 감사

## 18. 프로필 검증

- 공급자
- Port
- 데이터베이스/스키마
- User 분리
- 비밀값 참조
- Logical 소유자
- Duplicate 매핑
- Production Memory 어댑터
- 알 수 없거나 미지원인 공급자
- Cross-domain 직접 접근

## 19. 환경별 관리

### 로컬

편의 기본값을 허용하되 비밀값 원문 Git 저장 금지.

### CI

격리 DB와 자동 Provision.

### Staging

Production과 같은 공급자/스키마 정책.

### Production

- 명시 프로필
- 최소 권한
- 비밀값 Manager
- 백업
- 모니터링
- 변경 승인
- No 기본값 Password

## 20. 체크리스트

- [ ] 소유자별 DB 매핑이 명확하다.
- [ ] 공급자는 공식 3종 중 하나다.
- [ ] 계정 권한이 분리됐다.
- [ ] 비밀값 참조를 사용한다.
- [ ] Partial Install과 정본 불일치를 실패시킨다.
- [ ] 생성 업무영역이 자신의 프로필을 소유한다.
- [ ] Cross-domain DB 직접 접근이 없다.
- [ ] 읽기 복제본 일관성 정책이 있다.

## 부록 A. 프로필 예

```yaml
cpf:
  datasource:
    PAY:
      vendor: postgresql
      jdbcUrl: ${PAY_DB_URL}
      usernameRef: vault://cpf/pay/db/username
      passwordRef: vault://cpf/pay/db/password
      schema: pay
      pool:
        minimumIdle: 2
        maximumPoolSize: 20
        connectionTimeout: 3000ms
        validationTimeout: 1000ms
```

비밀값 원문을 프로필·명령행·Git에 저장하지 않는다.

## 부록 B. 다중 자료원 트랜잭션

여러 업무 DB를 하나의 로컬 트랜잭션처럼 묶지 않는다. 각 소유자 트랜잭션을 분리하고 사건·상태 대사·보상으로 일관성을 관리한다. 읽기 복제본은 지연을 고려해 즉시 일관성이 필요한 조회에 사용하지 않는다.

## 부록 C. 연결 풀 용량

`인스턴스 수 × 인스턴스별 최대 연결`이 데이터베이스 전체 허용 연결과 운영 여유를 초과하지 않아야 한다. 배치·온라인·운영 조회의 풀을 분리할 때 각 상한과 우선순위를 문서화한다.

## 28. Account와 Logical DB 분리

| Logical DB | 대표 Owner | 직접 접근 허용 |
|---|---|---|
| CPF | 기술 기반 Metadata | CPF Owner Adapter |
| CMN | 고객 업무 공통 | 승인된 공통 Service/Repository |
| ADM | 플랫폼 운영·감사 | ADM |
| BZA | 업무 관리자 | BZA |
| BAT | Batch Definition·Execution | Batch Runtime |
| Domain | 업무 원장 | 해당 업무영역 |

ADM이 운영 편의를 위해 다른 Owner DB를 직접 Update하지 않는다. 조회도 Public Operations Contract가 우선이며, 통합 Reporting DB가 필요하면 별도 Projection·CDC 정책을 둔다.

## 29. Profile 검증

- Vendor ID와 JDBC Driver가 일치한다.
- URL, Schema, Service Name/SID와 Timezone Option을 검증한다.
- Credential은 Secret Reference로 제공한다.
- Pool 최대값이 DB Session 상한과 Instance 수를 고려한다.
- Validation Query와 Connection Timeout이 장애 탐지 목표에 맞는다.
- Transaction Isolation과 Read-only 의미를 확인한다.
- Migration 계정과 Runtime 계정을 분리한다.

## 30. Read Replica 사용

Read Replica는 단순 URL 교체가 아니다. 지연 허용 범위, Read-your-write 필요 여부, Transaction 내 Primary 고정, 장애 시 Primary Fallback과 운영 표시를 정의한다. 권한·결재·잔액·상태 전이 판단은 Stale Read를 허용하지 않는다.

## 31. 다중 Datasource Transaction

두 DB를 하나의 로컬 Transaction처럼 가정하지 않는다. Owner별 Commit을 분리하고 Outbox, Saga, Reconcile 또는 보상으로 일관성을 관리한다. XA를 선택한다면 장애 복구, Heuristic Outcome, 운영 Tool과 성능을 실제로 검증한다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| Runtime Config | 각 Module `application.yml`과 Profile | Datasource·Pool·Vendor 선택 |
| Canonical Ownership | `cpf-tools/db/canonical/platform-schema.json` | Logical DB와 Table Owner |
| Vendor Source | `cpf-tools/db/vendor/{oracle,postgresql,mariadb}/source` | 공급자별 DDL |
| Migration | Vendor `migration/flyway`, `rollback` | Upgrade·Rollback |
| Generator | 업무영역 DB Template | 신규 Domain DB 정합성 |

### Z.1 공통 확인 명령

```powershell
git status --short
git diff --check
git grep -n "TODO\|UnsupportedOperationException\|return null" -- ':!cpf-docs/archive/**'
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
```

명령이 현재 Repository에 존재하지 않거나 Parameter가 달라졌다면 해당 Tool Source와 [도구 상세 참조](CPF_TOOL_REFERENCE.md)를 먼저 갱신한다.

### Z.2 완료 상태 사용

- **완료**: 구현·Consumer·운영 경로·검증·Evidence가 현재 Commit에서 확인됨
- **부분 구현**: 일부 계층 또는 실패·복구·운영 경로가 빠짐
- **미구현**: 제품 동작이 없음
- **미검증**: 구현은 있으나 요구된 실행 검증을 수행하지 않음
- **실패**: 검증을 수행했으나 기대 결과를 충족하지 못함
- **재확인 필요**: Source·문서·Evidence 또는 환경이 서로 달라 현재 상태를 확정할 수 없음
