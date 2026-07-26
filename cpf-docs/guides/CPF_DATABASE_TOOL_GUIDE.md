# CPF Database Tool Guide

## 1. 정본 정책
DB 정본은 `cpf-tools/db/vendor/<vendor>/source`다. MariaDB의 lifecycle bundle은 이 source에서 생성된다. `cpf-tools/db/source`라는 별도 정본을 만들지 않는다.

## 2. Vendor 상태
- MariaDB: implemented.
- MySQL/PostgreSQL/Oracle/SQL Server: 현재 구현 상태가 not-implemented이면 도구가 fail-closed해야 한다.
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
R14:
- V53: BZA governance/operability hardening.
- V54: BAT operation log retention archive.

Migration source와 runtime lifecycle copy는 byte-identical해야 하고 checksum manifest를 갱신한다.

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
2. 실제 migration 적용
3. target schema/seed/drift 확인
4. application build/runtime smoke
5. rollback precondition 확인
6. rollback 가능한 경우 rollback
7. re-apply
8. migration checksum 확인

## 17. 완료 기준
SQL 파일이 존재하는 것만으로 완료가 아니다. MariaDB client에서 fresh/upgrade/rollback을 실행하고 query result를 Evidence로 남겨야 Runtime 완료다.
