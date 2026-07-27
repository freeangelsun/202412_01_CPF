# CPF Final Completion Package Manifest — 2026-07-28

## Package 기준

- Base Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Base Branch: `master`
- Base SHA: `2daef3b7d2f82745d42d9b19804dde4bcac60edb`
- Base Commit: `20260727_05`
- Package Root 기준: Repository Root 상대경로
- 최종 ZIP 예상 파일 수: 323 files
- 삭제 경로: 52 paths
- Git Commit/Push/Branch: 수행하지 않음

## 주요 산출물

- Source/API/SPI/Frontend/Test 변경
- Spring Boot 4.1 / Java25 / Gradle9.1 build 계약
- ADM/BZA Security/Data Safety/Idempotency 보강
- Gateway failover Consumer/Public Boundary 보강
- Generated Domain/BAT JobPack/Public Boundary 보강
- MariaDB/PostgreSQL/Oracle 3 Vendor Source/Lifecycle/Runtime Query 계약
- MySQL/MSSQL 제품 지원 제거
- Canonical DB Vendor Generator/Sync/Gate
- Runtime Query Contract/3 Vendor MyBatis
- Current Request/Remaining Matrix/Handover/Validation Evidence
- 안전한 적용 스크립트와 삭제 Manifest

## 적용 방법

ZIP은 먼저 별도 디렉터리에 해제한다. Repository HEAD가 Base SHA와 같은지 확인한 후 다음 스크립트로 overlay 한다.

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File <PACKAGE_ROOT>\cpf-tools\scripts\apply-cpf-final-completion-package.ps1 `
  -PackageRoot <PACKAGE_ROOT> `
  -RepositoryRoot <CPF_REPOSITORY_ROOT>
```

적용 스크립트는 Base SHA를 기본 검증하며, Package 파일을 먼저 복사한 뒤 `cpf-tools/scripts/config/CPF_FINAL_COMPLETION_DELETE_PATHS.txt`의 obsolete 경로를 Repository 내부에서만 안전하게 삭제한다.

## 적용 후 검증

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-cpf-final-completion.ps1 `
  -RepoRoot <CPF_REPOSITORY_ROOT> `
  -SkipRuntime
```

실제 MariaDB/PostgreSQL/Oracle 환경까지 준비된 통합 검증에서는 각 Vendor Profile을 전달한다.

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-cpf-final-completion.ps1 `
  -RepoRoot <CPF_REPOSITORY_ROOT> `
  -RunDatabaseLifecycle `
  -DatabaseProfilePath <MARIADB_PROFILE>,<POSTGRESQL_PROFILE>,<ORACLE_PROFILE>
```

Runtime/Browser/다중 인스턴스 환경이 준비되면 `-SkipRuntime` 없이 실행하고 Browser E2E를 별도 수행한다.

## 무결성

`cpf-docs/evidence/20260728-final-completion/PACKAGE_SHA256SUMS.txt`는 checksum index 자신을 제외한 Package 파일 322개의 SHA-256을 기록한다.

정적 검증 원본과 판정은 `cpf-docs/evidence/20260728-final-completion/`에 있다.
