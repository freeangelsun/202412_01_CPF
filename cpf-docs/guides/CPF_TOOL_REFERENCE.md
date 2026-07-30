# CPF 도구 상세 참조

## 1. 공통

모든 명령은 Repository Root에서 실행한다.

```powershell
git rev-parse HEAD
git status --short
```

Credential은 환경변수 또는 Secret Provider로 주입한다.

## 2. `create-domain.ps1`

### 목적

신규 Generated Domain을 생성한다.

### 핵심 Parameter

| Parameter | 설명 |
|---|---|
| `DomainName` | 읽을 수 있는 업무명 |
| `SystemCode` | 3자리 대문자 코드 |
| `ModuleName` | Module |
| `PackageName` | Java Package |
| `DatabaseVendor` | mariadb/postgresql/oracle |
| `Capabilities` | 선택 기능 |
| `Port` | Runtime Port |
| `DryRun` | Plan |
| `Apply` | 실제 생성 |
| `GeneratePatch` | Patch 생성 |
| `AllowReserved` | 예약 코드 예외 |

### 예

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName payment `
  -SystemCode PAY `
  -DatabaseVendor postgresql `
  -Capabilities "database,remote-call,messaging" `
  -DryRun
```

### 실패

- 식별 충돌
- Port/Route
- DB/Schema
- 예약 코드
- 사용자 파일 덮어쓰기
- 내부 Import

## 3. `sync-database-artifacts.ps1`

### 목적

Canonical DB 변경을 Vendor Pack과 Generated Domain에 동기화한다.

```powershell
pwsh -File .\cpf-tools\scripts\sync-database-artifacts.ps1
```

### 출력

- checksum
- bundle
- manifest
- drift
- profile
- generated parity
- vendor parity

### 실패

- FK Cycle
- 존재하지 않는 Column
- Identity 불일치
- Vendor Artifact Drift
- Checksum 불일치

## 4. `build-all-install-sql.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\build-all-install-sql.ps1
```

생성:

- provision
- empty install
- product seed
- optional/test seed
- verify
- all install
- smoke

## 5. `initialize-cpf-database.ps1`

### 목적

Platform DB 설치.

```powershell
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 `
  -All `
  -RequireRun
```

선택:

- `All`
- `SystemCode`
- `DomainName`
- `ProfilePath`
- `SeedMode`
- `RequireRun`

Partial Install과 Drift를 실패시킨다.

## 6. `initialize-generated-domain-databases.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\initialize-generated-domain-databases.ps1 `
  -SystemCode PAY `
  -Operation bootstrap `
  -Apply
```

Operation은 Bootstrap, Verify, Upgrade, Rollback 등 Script Help를 따른다.

## 7. `initialize-databases.ps1`

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

## 8. `invoke-platform-database-migration.ps1`

### Dry Run

```powershell
pwsh -File .\cpf-tools\scripts\invoke-platform-database-migration.ps1 `
  -Direction upgrade `
  -FromVersion 72 `
  -ToVersion 73 `
  -Modules batch
```

### Apply

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
  -ExpectedPlanSha256 <PLAN> `
  -BackupManifestPath <MANIFEST>
```

### 제약

- 범위와 단일 Version 동시 사용 금지
- Apply는 Plan Hash와 Backup 필수
- Rollback Pair 필수
- Checksum 필수

## 9. `backup-cpf-database.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\backup-cpf-database.ps1 `
  -Vendor mariadb `
  -Database admDB `
  -Host 127.0.0.1 `
  -Port 3306 `
  -User cpf_backup
```

출력:

- Backup
- Manifest
- SHA-256
- 민감정보 분류

## 10. `restore-cpf-database.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\restore-cpf-database.ps1 `
  -Vendor mariadb `
  -Database admDB `
  -BackupFile .\adm.sql `
  -ConfirmRestore
```

거부:

- Confirmation 없음
- Manifest 없음
- Hash 불일치
- Vendor/DB 불일치

## 11. `verify-dr-restore.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\verify-dr-restore.ps1 `
  -Database admDB `
  -Host 127.0.0.1 `
  -Port 3306 `
  -User cpf_verify
```

`VerifySql`과 `RunPlatformVerify`를 동시에 사용하지 않는다.

## 12. `sync-generated-domain-artifacts.ps1`

비교:

```powershell
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope Database
```

적용:

```powershell
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 `
  -Scope AllGeneratorOwned `
  -Apply
```

사용자 수정 파일 충돌 시 실패한다.

## 13. `check-generator-arbitrary-domain-parity.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\check-generator-arbitrary-domain-parity.ps1
```

격리 Sandbox에서 임의 두 Domain을 생성·비교·제거한다.

## 14. `start-cpf-local.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\start-cpf-local.ps1
```

시작:

- ADM
- BZA
- Reference/Generated Domain
- 선택 Gateway
- Local Registry

환경 Parameter는 Script Help를 따른다.

## 15. `status-cpf-local.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\status-cpf-local.ps1
```

Process, Port, Health, Log 경로를 확인한다.

## 16. `stop-cpf-local.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\stop-cpf-local.ps1
```

관리 대상 Process만 종료하고 임의 Java Process를 Kill하지 않는다.

## 17. Batch Local

```powershell
pwsh -File .\cpf-tools\scripts\start-bat-local-distributed.ps1
pwsh -File .\cpf-tools\scripts\stop-bat-local-distributed.ps1
```

Control, Scheduler, Worker, Agent 역할을 분리한다.

## 18. `verify-full-product.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\verify-full-product.ps1 `
  -WithDatabase `
  -WithGeneratorLifecycle `
  -WithBrowser `
  -RequireAll `
  -Profile local
```

결과:

- Java
- Frontend
- DB
- Generator
- Runtime
- Browser
- Artifact
- Evidence

## 19. `check-architecture-ownership.ps1`

검사:

- 역방향 의존
- 내부 Import
- Owner 위반
- 순환
- 고정 Domain
- Module Naming

## 20. `check-repository-hygiene.ps1`

검사:

- build/log/tmp/zip/bak
- Root 문서
- Secret
- Dead File
- Stale Artifact
- 외부 Runtime Asset

## 21. `check-document-links.ps1`

README와 Guide의 상대 Link, Anchor와 대상 파일을 검사한다.

## 22. `check-source-documentation-standard.ps1`

Public API, 주요 Service/Controller의 JavaDoc, OpenAPI와 설명을 검사한다.

## 23. `check-admin-data-safety.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\check-admin-data-safety.ps1
```

- PII Masking
- Raw Data 경계
- 상태 Catalog
- Query Resource
- Migration/Rollback
- Bundle Parity

## 24. `check-certificate-expiry.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\check-certificate-expiry.ps1 `
  -CertificatePath .\cert.pem `
  -WarnDays 30
```

Private Key를 출력하지 않는다.

## 25. `new-cpf-changeset.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\new-cpf-changeset.ps1 `
  -ChangeSetId REL-20260730-01 `
  -SourceEnvironment dev `
  -TargetEnvironment staging `
  -Reason "정기 배포" `
  -Files @("README.md")
```

Manifest에 Commit과 Hash를 기록한다.

## 26. `verify-cpf-changeset.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\verify-cpf-changeset.ps1 `
  -Manifest .\changeset.json `
  -ExpectedSourceEnvironment dev `
  -ExpectedTargetEnvironment staging
```

Commit/Hash 불일치를 실패시킨다.

## 27. Gradle Artifact Task

```powershell
.\gradlew.bat publishCpfVerifiedLocalPlatformArtifacts -PcpfArtifactMode=LOCAL_DEV
.\gradlew.bat verifyCpfLocalArtifactPropagation -PcpfArtifactMode=LOCAL_DEV
.\gradlew.bat publishCpfPlatformArtifacts -PcpfArtifactMode=REMOTE
.\gradlew.bat buildCpfOfflineArtifactBundle -PcpfArtifactMode=LOCAL_DEV
```

## 28. 결과 해석

Status:

- PASS
- FAIL
- SKIPPED
- BLOCKED
- NOT_APPLICABLE

`RequireAll`에서는 SKIPPED/BLOCKED를 성공으로 보지 않는다.

## 29. Evidence 필드

- tool
- version
- sourceCommit
- command
- parameters
- environment
- profile
- start/end
- exitCode
- result
- findings
- rawEvidence
- sanitized

## 30. 안전 규칙

- `-ErrorAction SilentlyContinue`로 실패 숨김 금지
- Tool이 Source를 자동 수정하여 Gate 통과 금지
- Migration Checksum 자기 갱신 금지
- 비밀번호 임시파일 금지
- 운영 Drop/Reset 기본 금지
