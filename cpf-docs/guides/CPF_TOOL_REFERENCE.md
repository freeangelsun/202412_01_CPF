# CPF Tool Reference

이 문서는 `cpf-tools`의 DB, Generator, Backup/Restore/DR, Certificate, Metadata, Environment Promotion 도구를 운영/개발자가 재현 가능하게 실행하기 위한 정본 참조다. Script parameter와 문서가 다르면 Script가 우선이며 같은 작업에서 문서를 고친다.

## 1. 공통 실행 원칙
- Repository Root에서 실행한다.
- 실행 전 `git rev-parse HEAD`를 기록한다.
- credential/password/token을 command line이나 Evidence에 넣지 않는다.
- 운영 destructive 작업은 명시적 확인/승인 없이 실행하지 않는다.
- 실행하지 않은 항목을 성공으로 기록하지 않는다.
- MariaDB 외 Vendor는 해당 pack이 실제 implemented일 때만 실행한다.

## 2. `build-all-install-sql.ps1`
목적: `database-source-plan.json`과 canonical vendor source를 이용해 lifecycle SQL bundle을 재생성한다.

### Parameter
| Parameter | Default | 의미 |
|---|---|---|
| `-Root` | repository root | CPF Root 경로 |

### 실행
```powershell
pwsh -File .\cpf-tools\scripts\build-all-install-sql.ps1
```

### 생성/동기화
- source `00_provision.sql`
- `00_empty_install.sql`
- `00_product_seed.sql`
- `00_optional_sample_seed.sql`
- `00_test_seed.sql`
- `00_verify.sql`
- `00_all_install.sql`
- `00_all_install_and_smoke.sql`
- central provision/install/seed/verify copy
- migration/rollback lifecycle copy

### Fail 조건
- source plan 또는 source file 누락
- non-provision bundle에 CREATE/ALTER/DROP USER 포함
- non-test bundle의 DROP DATABASE/TABLE
- product seed의 cross logical DB 참조
- product seed의 localhost fixture
- Generated Domain 고정 SQL이 product source plan에 포함

## 3. `verify-default-metadata.ps1`
목적: vendor-independent metadata catalog가 실제 implemented vendor seed에 존재하는지 검증한다.

### Parameter
| Parameter | Default | 의미 |
|---|---|---|
| `-Vendor` | `mariadb` | 검증 Vendor |
| `-Root` | repository root | CPF Root |
| `-RequireImplemented` | switch | 미지원 Vendor를 명시 실패 |

### 검증 대상
- Code Group
- 각 required Code Value
- Message Code
- Response Code
- Config Key
- vendor implementation status

```powershell
pwsh -File .\cpf-tools\scripts\verify-default-metadata.ps1 -Vendor mariadb -RequireImplemented
```
Catalog에만 있고 Seed에 없거나, 미지원 Vendor를 완료 처리하면 실패해야 한다.

## 4. `backup-cpf-database.ps1`
목적: DB native dump와 SHA-256 manifest 생성.

### Parameter
| Parameter | 의미 |
|---|---|
| `-Vendor` | DB Vendor. R14에서는 MariaDB만 구현 |
| `-Database` | backup DB 이름 |
| `-Host` | DB host |
| `-Port` | DB port |
| `-User` | backup user |
| `-OutputDirectory` | backup/manifest 출력 경로 |
| `-Root` | repository root |

Password parameter는 의도적으로 제공하지 않는다.

```powershell
pwsh -File .\cpf-tools\scripts\backup-cpf-database.ps1 `
  -Vendor mariadb -Database cpfDB -Host 127.0.0.1 -Port 3306 -User cpf_backup
```

### Manifest 핵심 필드
- vendor/database
- baseCommit
- startedAt/finishedAt
- backupFile
- SHA-256
- `containsSensitiveData=true`
- `credentialEmbedded=false`
- handling=`RESTRICTED`

## 5. `restore-cpf-database.ps1`
목적: Backup manifest를 검증한 뒤 대상 DB로 restore.

### Parameter
| Parameter | 의미 |
|---|---|
| `-Vendor` | 대상 Vendor |
| `-Database` | restore 대상 DB |
| `-BackupFile` | dump 파일 |
| `-Host/-Port/-User` | 대상 연결정보 |
| `-ConfirmRestore` | 실제 Restore 확인 필수 switch |
| `-AllowMissingManifest` | legacy backup에만 허용하는 예외 |
| `-Root` | repository root |

### Fail-closed
- ConfirmRestore 없음
- Manifest 없음(예외 switch 미사용)
- manifest SHA와 실제 backup SHA 불일치
- vendor mismatch
- database mismatch

## 6. `verify-dr-restore.ps1`
목적: 격리 복구 DB의 실제 사용 가능성을 Evidence로 검증.

### Parameter
| Parameter | 의미 |
|---|---|
| `-Database` | 복구된 DB |
| `-Host/-Port/-User` | 검증 연결정보 |
| `-VerifySql` | 해당 DB 전용 추가 verify SQL |
| `-RunPlatformVerify` | 모든 CPF logical DB가 복구됐을 때 canonical full verify |
| `-Root` | repository root |
| `-EvidenceDirectory` | DR Evidence 위치 |

`-VerifySql`과 `-RunPlatformVerify`는 동시에 사용하지 않는다.

기본 실행은 connection + table count baseline을 확인한다. full verify는 모든 logical DB가 준비된 환경에서만 사용한다.

## 7. `check-certificate-expiry.ps1`
목적: 공개 인증서 만료 사전 탐지.

### Parameter
| Parameter | 의미 |
|---|---|
| `-CertificatePath` | X.509 인증서 경로 |
| `-WarnDays` | 만료 경고 기준일 |

출력은 Subject, Issuer, Serial, Thumbprint, NotBefore, NotAfter, RemainingDays, Status이며 private key를 출력하지 않는다.

## 8. `new-cpf-changeset.ps1`
목적: 환경간 promotion change-set manifest 생성.

### Parameter
| Parameter | 의미 |
|---|---|
| `-ChangeSetId` | 변경 식별자 |
| `-SourceEnvironment` | source env |
| `-TargetEnvironment` | target env |
| `-Reason` | 변경 사유 |
| `-Files` | promotion 대상 Root-relative 파일 목록 |
| `-Root` | repository root |
| `-OutputDirectory` | manifest 위치 |

Manifest에는 base commit과 각 파일 SHA가 포함된다. Secret 값 자체는 manifest에 넣지 않는다.

## 9. `verify-cpf-changeset.ps1`
목적: change-set이 지금 적용하려는 source/target/commit/file과 동일한지 검증.

### Parameter
| Parameter | 의미 |
|---|---|
| `-Manifest` | change-set manifest |
| `-ExpectedSourceEnvironment` | 기대 source env |
| `-ExpectedTargetEnvironment` | 기대 target env |
| `-AllowDifferentBaseCommit` | 예외적으로 base SHA mismatch 허용 |
| `-Root` | repository root |

기본은 base commit 불일치와 file hash mismatch를 실패시킨다. 실제 조직 승인/서명은 별도 Release/CD workflow가 담당한다.

## 10. Generator `create-domain.ps1`
정본 Generator. 주요 입력은 다음과 같다.

### Identity
`DomainName`, `SystemCode`, `ModuleCode`, `ModuleName`, `DomainIdCode`, `PackageName`, `BasePackage`.

### DB
`SchemaName`, `TablePrefix`, `DatabaseVendor`, `DatabaseHost`, `DatabasePort`, `DatabaseName`, `DatabaseSchema`, `DatabaseAdminUsername`, `DatabaseMigrationUsername`, `DatabaseRuntimeUsername`, `DatabaseClientPath`, `ProvisionDatabase`.

### Runtime/Capability
`Port`, `Online`, `Capabilities`, `Batch`, `CenterCut`, `External`, `Messaging`, `File`, `SecurityAudit`, `Ui`, `BzaMenu`, `ProductionProfile`.

### Execution
`OutputDir`, `DryRun`, `GeneratePatch`, `Apply`, `AllowReserved`.

Generator는 입력 충돌을 생성 전에 검증하고 Generated source가 internal CPF implementation에 직접 의존하지 않게 한다.

## 11. 권장 Generator 검증
```powershell
# 실제 parameter는 Script help와 함께 확인
pwsh -File .\cpf-tools\generator\create-domain.ps1 -DomainName Example -SystemCode XYZ -DryRun
```
- 서로 다른 2 Domain 생성
- 같은 SystemCode/Module/Package/DB/Route 충돌 negative test
- build/test
- internal import gate
- DB/OpenAPI/Test/Config 산출물 확인
- 재실행이 고객 코드를 덮어쓰지 않는지 확인

## 12. Database Initialization
기존 `initialize-cpf-database.ps1` 등 lifecycle consumer가 있는 경우 `database-source-plan.json`과 canonical bundle을 소비해야 한다. 운영 DB에서 Reset/Drop 성격 명령은 승인 없이 실행하지 않는다.

대표 개발 환경 검증:
```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun
```
실제 Script 존재/option은 최신 master에서 다시 확인한다.

## 13. Contract / Release Tool
Contract, Release Manifest, SBOM, Provenance, Deploy inventory 관련 기존 도구는 다음 원칙을 따른다.
- 입력 JSON Schema 선검증
- Artifact/source identity 필수
- dirty/final release 정책
- Frontend dependency 포함 SBOM
- License/CVE Gate
- signature/attestation
- prod inventory template fallback 금지

R14 audit에서 이 영역은 다수 `부분 구현/미구현`이므로 Codex 통합 검증에서 실제 Gate 수준으로 보완한다.

## 14. Evidence 표준
모든 도구 Evidence에는 다음을 남긴다.
- HEAD SHA
- exact command
- environment/profile
- start/end
- 관련 Requirement/QA ID
- actual result/exit code
- raw log 또는 query result
- sensitive-data scrub 여부
- current SHA validity

## 15. 실패 해석
도구가 실패했을 때 임의로 성공 처리하거나 `-ErrorAction SilentlyContinue`로 숨기지 않는다. 원인을 Source defect / environment / credential / unsupported vendor / migration drift / external dependency로 분류하고 수정 후 다시 실행한다.
