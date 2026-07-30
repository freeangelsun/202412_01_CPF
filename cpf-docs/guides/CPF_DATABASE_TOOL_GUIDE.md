# CPF 데이터베이스 도구 가이드

## 1. 목적

이 문서는 CPF가 지원하는 Oracle, PostgreSQL, MariaDB의 설치, Seed, Migration, Rollback, Verify, Backup과 Restore 도구를 설명한다.

## 2. 정본 구조

```text
cpf-tools/db/
├─ metadata/
├─ generated/
└─ vendor/
   ├─ mariadb/
   │  ├─ source/
   │  ├─ install/
   │  ├─ seed/
   │  ├─ migration/
   │  ├─ rollback/
   │  ├─ verify/
   │  └─ domain-template/
   ├─ postgresql/
   └─ oracle/
```

원칙:

- Vendor별 동일 Directory 구조
- Canonical Metadata 우선
- 생성 산출물 수동 수정 금지
- Table, Column, PK, FK, Index, Default, Identity, Comment 동등성
- Generated Domain Template 동기화

## 3. 공식 Vendor

| Vendor | 식별자 | 기본 Port |
|---|---|---:|
| MariaDB | `mariadb` | 3306 |
| PostgreSQL | `postgresql` | 5432 |
| Oracle | `oracle` | 1521 |

MySQL, MSSQL, H2는 공식 제품 Vendor가 아니다.

## 4. Source Plan

`database-source-plan.json`은 SQL 역할과 포함 순서를 선언한다.

역할:

- provision
- empty install
- product seed
- optional sample
- test seed
- verify
- migration
- rollback

신규 SQL은 역할과 Owner를 먼저 등록한다.

## 5. Bundle

| 파일 | 목적 |
|---|---|
| `00_provision.sql` | User/Schema/권한 |
| `00_empty_install.sql` | 제품 Object |
| `00_product_seed.sql` | 필수 기준정보 |
| `00_optional_sample_seed.sql` | 선택 Sample |
| `00_test_seed.sql` | 격리 Test |
| `00_verify.sql` | Read-only 검증 |
| `00_all_install.sql` | Empty + Product |
| `00_all_install_and_smoke.sql` | 설치 + Verify |

## 6. FK 생성 순서

Canonical Schema에서 FK Dependency Graph를 만든다.

```text
Parent Table
→ Child Table
→ Index
→ FK
```

Topological Sort로 Table 생성 순서를 결정한다. Cycle 또는 존재하지 않는 Parent는 생성 전에 실패한다.

Spring Batch Metadata Table도 같은 규칙을 적용한다.

## 7. PK 생성 정책

PK Strategy를 Metadata에 선언한다.

- APPLICATION: Application이 ID 생성
- IDENTITY: DB Identity/Auto Increment
- SEQUENCE: Sequence
- NATURAL: 업무 Key
- COMPOSITE: 복합 Key

Vendor 변환:

| 논리 정책 | MariaDB | PostgreSQL | Oracle |
|---|---|---|---|
| IDENTITY | AUTO_INCREMENT | GENERATED ... AS IDENTITY | IDENTITY |
| SEQUENCE | Sequence 또는 Application 정책 | SEQUENCE | SEQUENCE |
| APPLICATION | 일반 Column | 일반 Column | 일반 Column |

Repository Insert와 DDL 정책을 일치시킨다.

## 8. 문자열과 Null

Oracle은 빈 문자열을 Null로 처리한다. Optional Text의 논리 의미는 다음 중 하나로 정본화한다.

- Null
- 명시 Sentinel
- 별도 상태 Flag

`NOT NULL DEFAULT ''`에 의존하지 않는다. DTO, RowMapper, API와 UI도 같은 의미를 사용한다.

## 9. Bundle 생성

```powershell
pwsh -File .\cpf-tools\scripts\build-all-install-sql.ps1
```

전체 동기화:

```powershell
pwsh -File .\cpf-tools\scripts\sync-database-artifacts.ps1
```

수행 항목:

1. Migration Checksum
2. Lifecycle Bundle
3. Schema Manifest
4. Drift
5. Profile
6. Generated Domain 동기화
7. 3 Vendor Parity

## 10. Fresh Install

```powershell
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun
```

절차:

1. Profile 검증
2. Credential Reference 해석
3. Provision
4. Empty Install
5. Product Seed
6. Verify
7. Manifest 비교
8. Evidence

일부 Table만 존재하면 Partial Install로 실패한다.

## 11. Platform Migration

Dry Run:

```powershell
pwsh -File .\cpf-tools\scripts\invoke-platform-database-migration.ps1 `
  -Direction upgrade `
  -FromVersion 72 `
  -ToVersion 73 `
  -Modules batch
```

Apply:

```powershell
pwsh -File .\cpf-tools\scripts\invoke-platform-database-migration.ps1 `
  -Direction upgrade `
  -FromVersion 72 `
  -ToVersion 73 `
  -Modules batch `
  -Apply `
  -ConfirmApply `
  -ConfirmApplicationsStopped `
  -ConfirmRollbackReady `
  -ExpectedPlanSha256 <PLAN_SHA256> `
  -BackupManifestPath <MANIFEST_PATH>
```

## 12. Migration 안전장치

- 명시 Version 범위
- Checksum
- Rollback Pair
- Backup Manifest
- Runtime 중지 확인
- Plan Hash
- Drift Precheck
- Lock
- 실행 결과
- 재실행 정책

Tool이 설치 Version을 추정하지 않는다.

## 13. Rollback

Rollback은 단순 반대 DDL이 아니다.

검사:

- 데이터 손실
- 신규 Column 사용 여부
- Archive Row
- FK 관계
- Application 호환성
- Sequence/Identity
- Seed
- Downstream

위험하면 안전하게 중단하고 수동 Migration 계획을 요구한다.

## 14. Upgrade 검증

```text
이전 Version 설치
→ Backup
→ Upgrade Dry Run
→ Apply
→ Verify
→ Application Smoke
→ Rollback Dry Run
→ Rollback
→ Verify
→ Reapply
→ Verify
```

## 15. Drift

비교 대상:

- Table
- Column Type
- Length/Precision
- Nullability
- Default
- PK
- FK
- Index
- Unique
- Identity/Sequence
- Comment
- Owner

차이가 있으면 Skip하지 않는다.

## 16. Generated Domain

Generated Domain DB는 자신의 Profile과 Vendor를 가진다.

```powershell
pwsh -File .\cpf-tools\scripts\initialize-generated-domain-databases.ps1 `
  -SystemCode PAY `
  -Operation bootstrap `
  -Apply
```

중앙 Golden Template에서 생성한다.

## 17. Runtime SQL

Vendor 선택은 Profile을 따른다.

```text
mybatis/vendor/mariadb/
mybatis/vendor/postgresql/
mybatis/vendor/oracle/
```

다른 Vendor SQL로 Fallback하지 않는다.

## 18. Seed

Product Seed:

- 멱등성
- Owner DB 내부
- 고객 데이터 미변경
- Test Fixture 없음
- Secret 없음
- Version
- Audit 필요 여부

Optional/Test Seed는 운영 설치에 자동 포함하지 않는다.

## 19. Verify

Read-only 검사:

- Object 존재
- Column/Type
- PK/FK/Index
- Seed
- Metadata
- Query
- Identity Insert
- Comment
- 권한

## 20. Backup

```powershell
pwsh -File .\cpf-tools\scripts\backup-cpf-database.ps1 `
  -Vendor postgresql `
  -Database admDB `
  -Host 127.0.0.1 `
  -Port 5432 `
  -User cpf_backup
```

Manifest:

- Vendor
- DB
- Source Commit
- 시각
- File
- SHA-256
- 민감정보 분류
- Tool Version

## 21. Restore

```powershell
pwsh -File .\cpf-tools\scripts\restore-cpf-database.ps1 `
  -Vendor postgresql `
  -Database admDB `
  -BackupFile .\backup.dump `
  -ConfirmRestore
```

Vendor, DB, Checksum 불일치를 거부한다.

## 22. DR Verify

```powershell
pwsh -File .\cpf-tools\scripts\verify-dr-restore.ps1 `
  -Database admDB `
  -Host 127.0.0.1 `
  -Port 5432 `
  -User cpf_verify
```

모든 논리 DB 복구 후 Platform Verify를 수행한다.

## 23. Credential

Password는 Command Line, Plan, SQL 임시파일과 Evidence에 넣지 않는다. OS Credential, Process Environment 또는 Secret Provider를 사용한다.

## 24. 장애 처리

| 장애 | 처리 |
|---|---|
| 연결 실패 | 환경/권한 분류 |
| Partial Install | 실패 후 수동 정리/복구 |
| Checksum 불일치 | Apply 금지 |
| Lock 실패 | 재시도 또는 운영 확인 |
| DDL 일부 적용 | Transaction 가능 여부와 복구 Script |
| Disk Full | 중단, 공간 확보, 상태 확인 |
| Rollback 불가 | 수동 Migration 계획 |

## 25. Evidence

- Vendor
- DB/Schema
- Tool Version
- Source Commit
- Command
- Plan Hash
- Backup Manifest
- 시작/종료
- Exit Code
- Verify Query
- Sanitizing

## 26. 체크리스트

- [ ] Canonical Metadata에서 생성한다.
- [ ] FK 순서가 Dependency 기반이다.
- [ ] PK Strategy가 Vendor 간 일치한다.
- [ ] Oracle 빈 문자열 의미가 일치한다.
- [ ] Fresh/Upgrade/Rollback/Reapply를 검증한다.
- [ ] Generated Domain Artifact가 동기화됐다.
- [ ] Drift를 Skip하지 않는다.
- [ ] Evidence가 Source Commit과 일치한다.
