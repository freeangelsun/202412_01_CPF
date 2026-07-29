# CPF Database Tool Guide

## 1. 정본 정책
DB 정본은 `cpf-tools/db/vendor/<vendor>/source`다. MariaDB의 lifecycle bundle은 이 source에서 생성된다. `cpf-tools/db/source`라는 별도 정본을 만들지 않는다.

## 2. Vendor 상태
- 공식 Vendor는 MariaDB, PostgreSQL, Oracle 세 종류다.
- 실제 완료 상태는 Vendor Pack의 정적 Gate와 해당 환경의 실행 Evidence를 구분해 판정한다.
지원하지 않는 Vendor를 MariaDB SQL 복사/치환으로 완료 처리하지 않는다.

## 3. Source Plan
`cpf-tools/config/database-source-plan.json`은 provision, empty install, product seed, optional sample, test seed, verify에 어떤 source SQL이 들어가는지 정의한다. 신규 SQL을 추가할 때 lifecycle 역할을 먼저 결정한다.

## 4. Bundle 종류
- `00_provision.sql`: Schema/User/권한 Provision. 관리자 권한 필요.
- `00_empty_install.sql`: 제품 Object만 설치.
- `00_product_seed.sql`: 제품 필수 기준정보.
- `00_optional_sample_seed.sql`: 선택 Sample/Local reference.
- `00_test_seed.sql`: 격리 Test fixture.
- `00_verify.sql`: read-only 설치 검증.
- `00_all_install.sql`: empty + product seed.
- `00_all_install_and_smoke.sql`: empty + product + verify.

## 5. Bundle 재생성
```powershell
pwsh -File .\cpf-tools\scripts\build-all-install-sql.ps1
```
SQL source 수정 후 bundle을 수동 복사하지 않는다. 생성 후 source bundle과 central lifecycle copy가 byte-identical인지 확인한다.

## 6. Product Seed 규칙
Product seed는 다른 logical DB를 직접 참조하지 않는다. localhost fixture나 Test 전용 데이터도 넣지 않는다. 제품 기본 Code/Message/Response/Config는 idempotent하게 적용한다.

## 7. R14 Metadata
R14 catalog는 HTTP/Execution/Async/Batch/Retry/Idempotency/Health/Circuit/File Scan/Data Classification/Approval/Sort/Error/Retention 등의 공통 코드를 관리한다. Catalog와 Seed가 다르면 build/release 전에 실패해야 한다.

## 8. Migration
Migration은 설치된 고객 DB를 안전하게 최신 schema로 이동한다. fresh schema 수정만 하고 Migration을 누락하지 않는다.

플랫폼 DB의 정식 실행기는 `invoke-platform-database-migration.ps1`다. 이 실행기는
Profile의 `enabled=true`, `databaseLifecycle=platform-pack` Module 선언을 읽으며
Domain/SystemCode 고정 목록을 사용하지 않는다. MariaDB/PostgreSQL/Oracle Vendor Pack을
동적으로 선택하고, SQL의 logical DB를 Profile의 physical database/schema로 렌더링한다.
Generated Domain migration은 이 실행기의 대상이 아니다.

Dry-run이 기본값이다. 자동으로 현재 baseline이나 최신 Version을 추정하지 않으므로 다음 중
하나를 반드시 명시한다.

- 범위: `-FromVersion <현재> -ToVersion <목표>`
- 선택: `-MigrationVersion <V1,V2,...>`

```powershell
# DB를 변경하지 않는 Upgrade plan
pwsh -File .\cpf-tools\scripts\invoke-platform-database-migration.ps1 `
  -Direction upgrade -FromVersion 72 -ToVersion 73 -Modules batch

# 단일 migration 선택 plan
pwsh -File .\cpf-tools\scripts\invoke-platform-database-migration.ps1 `
  -Direction upgrade -MigrationVersion 73 -Modules batch
```

Dry-run 결과의 `planSha256`를 검토한다. 실제 Apply는 같은 입력과 함께 다음 안전장치를
모두 요구한다.

- `-ConfirmApply`
- `-ConfirmApplicationsStopped`
- `-ConfirmRollbackReady`
- Dry-run과 동일한 `-ExpectedPlanSha256`
- 변경 대상 physical DB마다 hash 검증 가능한 `-BackupManifestPath`

```powershell
pwsh -File .\cpf-tools\scripts\invoke-platform-database-migration.ps1 `
  -Direction upgrade -MigrationVersion 73 -Modules batch -Apply `
  -ConfirmApply -ConfirmApplicationsStopped -ConfirmRollbackReady `
  -ExpectedPlanSha256 <DRY_RUN_PLAN_SHA256> `
  -BackupManifestPath <BAT_DB_BACKUP_MANIFEST>
```

Password는 Profile이 가리키는 process environment에서만 해석하며 command line, plan,
Evidence에 기록하지 않는다. `checksums.sha256`가 없거나 선택 SQL의 SHA-256이 다르거나
동일 Version의 rollback SQL이 없으면 Apply 전에 실패한다. Rollback SQL도 Dry-run plan의
hash에 포함되므로 계획 이후 파일 변경은 `ExpectedPlanSha256` 불일치로 차단된다.

Flyway history가 없는 기존 DB에 대해 Tool이 설치 Version을 추정하지 않는다. 반드시
명시적인 From/To 또는 MigrationVersion 선택을 사용하고, DB drift와 실제 baseline을
운영자가 별도로 확인한다. MariaDB historical SQL에 명시적 `USE <logicalDatabase>`가
없으면 어느 physical DB인지 table prefix로 추정하지 않고 실패한다. 이런 historical
migration은 변경하지 말고 canonical metadata에 근거한 새 bridge migration 또는 명시적
routing 계약으로 보정한다.

현재 Lifecycle:
- V53: BZA governance/operability hardening.
- V54: BAT operation log retention archive.
- V57: ADM audit delivery `transactionId` 표준화.
- V58: Platform schema comment delta.

Migration source와 runtime lifecycle copy는 byte-identical해야 하고 checksum manifest를 갱신한다.

Platform Table의 lifecycle/audit 정책 정본은
`cpf-tools/db/metadata/platform-table-lifecycle-policy.json`이다. 신규 Table은 기본
`full-audit` 정책을 적용하며, immutable event, semantic lifecycle, lease, projection,
sequence, static contract, framework metadata 예외는 사유와 필수 semantic
actor/time/fencing Column을 Metadata에 명시해야 한다. Gate는 미등록 Table, 알 수 없는
정책, stale 예외를 fail-closed한다.

V58 Comment Migration 정본은
`cpf-tools/db/metadata/platform-schema-comment-migration-v58.json`이며 기존 Comment를
다시 소유하지 않고 이번 Version이 추가한 delta만 생성·Rollback한다.

## 9. Rollback
Rollback은 데이터 손실 가능성을 우선 검사한다.
- R53: Role 재부여 이력을 old model로 collapse할 수 없거나 audit chain이 사용 중이면 SIGNAL로 중단.
- R54: archive row가 존재하면 table drop을 중단.
운영 데이터를 조용히 DROP하고 성공 처리하지 않는다.

## 10. Drift
기존 column이 존재한다는 이유만으로 migration을 skip하지 않는다. type/nullability/default/index/FK가 target과 다른지 검사하고 drift를 명시적으로 실패시킨다.

## 11. EXS
EXS는 Generated Domain이다. Platform provision/verify에서 고정 `exsDB`가 반드시 존재한다고 가정하지 않는다. EXS가 필요하면 Generator가 해당 Domain DB artifact를 생성한다.

## 12. Backup
```powershell
pwsh -File .\cpf-tools\scripts\backup-cpf-database.ps1 \
  -Vendor mariadb -Database cpfDB -Host 127.0.0.1 -Port 3306 -User cpf_backup
```
Password argument는 없다. DB client의 안전한 credential mechanism을 사용한다. Backup manifest는 SHA-256, DB/Vendor, 기준 commit, 시작/종료를 기록하고 `containsSensitiveData=true`로 분류한다.

## 13. Restore
```powershell
pwsh -File .\cpf-tools\scripts\restore-cpf-database.ps1 \
  -Vendor mariadb -Database cpfDB -BackupFile .\backup.sql -ConfirmRestore
```
Manifest, checksum, vendor, database가 일치해야 한다. `-AllowMissingManifest`는 legacy backup에 대한 명시적 예외이며 정상 절차로 사용하지 않는다.

## 14. DR Verify
```powershell
pwsh -File .\cpf-tools\scripts\verify-dr-restore.ps1 \
  -Database cpfDB -Host 127.0.0.1 -Port 3306 -User cpf_verify
```
기본은 isolated DB가 연결되고 table이 존재하는지 검증한다. DB별 추가 SQL은 `-VerifySql`, 모든 CPF logical DB를 복구한 통합 환경은 `-RunPlatformVerify`를 사용한다. 두 옵션을 동시에 쓰지 않는다.

## 15. Fresh Install 검증 순서
1. Provision
2. Empty Install
3. Product Seed
4. Verify
5. Optional/Test는 요청된 환경에서만
6. Object count/index/FK/metadata 확인
7. Evidence 저장

## 16. Upgrade 검증 순서
1. 이전 기준 schema 설치
2. `invoke-platform-database-migration.ps1` Dry-run과 plan SHA 검토
3. physical DB별 backup과 manifest checksum 확인
4. 동일 plan SHA로 실제 migration 적용
5. target schema/seed/drift 확인
6. application build/runtime smoke
7. rollback precondition 확인
8. 별도 Dry-run/plan SHA로 rollback
9. rollback 검증 후 Upgrade 재적용
10. migration checksum과 sanitized result 확인

MariaDB V58의 실제 검증 명령:

```powershell
pwsh -File .\cpf-tools\scripts\smoke-platform-schema-comment-migration.ps1
pwsh -File .\cpf-tools\scripts\smoke-platform-schema-comment-migration.ps1 `
  -Apply -Confirmation APPLY_V58_COMMENT_MIGRATION
pwsh -File .\cpf-tools\scripts\smoke-platform-schema-comment-migration.ps1 -VerifyOnly
```

`-Apply` 경로는 Upgrade → Rollback → Re-upgrade를 실행하고 comment delta뿐 아니라
Column/Index/FK 정의 hash와 `FOREIGN_KEY_CHECKS` 복원도 함께 검증한다. 2026-07-26
HOME MariaDB 실행에서는 Column Comment 299개와 Table Comment 19개 delta가 왕복 후
일치했다. 이 Evidence를 다른 PC 또는 Vendor의 실행 성공으로 승계하지 않는다.

## 17. 완료 기준
SQL 파일이 존재하는 것만으로 완료가 아니다. MariaDB client에서 fresh/upgrade/rollback을 실행하고 query result를 Evidence로 남겨야 Runtime 완료다.
