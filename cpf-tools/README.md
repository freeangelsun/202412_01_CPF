# CPF Tools

`cpf-tools`는 Core Platform Framework의 설치·DB lifecycle·Generated Domain·검증·Evidence 보조 도구를 소유합니다.

개발·Generator·검증 도구 사용은 [`CPF 개발자 매뉴얼`](../cpf-docs/guides/01_개발자매뉴얼.md)을, 설치·DB lifecycle·배포 운영은 [`CPF 플랫폼 운영 매뉴얼`](../cpf-docs/guides/05_플랫폼운영매뉴얼.md)을 참조합니다.

Build Support Unit도 제품 Runtime Module과 분리해 이 경계가 소유합니다.

```text
cpf-tools/build/gradle-plugin   CPF Domain Convention Plugin 격리 Build
cpf-tools/build/platform-bom    CPF Published Dependency BOM 격리 Build
```

핵심 명령:

```powershell
# DB Canonical/Vendor Pack/Manifest 동기화
pwsh -NoProfile -File .\cpf-tools\scripts\sync-database-artifacts.ps1

# Platform DB 선택 설치
pwsh -NoProfile -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun

# Generated Domain 생성
pwsh -NoProfile -File .\cpf-tools\generator\create-domain.ps1 -DomainName payment -SystemCode PAY -Apply

# EXS도 동일 Generated Domain 정책
pwsh -NoProfile -File .\cpf-tools\generator\create-domain.ps1 -DomainName external -SystemCode EXS -Apply
```

DB/SQL/Metadata를 수정한 뒤 `sync-database-artifacts.ps1`을 생략해서는 안 됩니다.

## 작업 시작 / 동기화 / 통합 검증

```powershell
# 1) 다른 PC/AI가 이어받기 전에 정본·요청·HEAD 확인
pwsh -NoProfile -File .\cpf-tools\scripts\check-work-context.ps1

# 2) DB Canonical + Vendor Pack + Schema Manifest + 기존 Generated Domain DB artifact parity
pwsh -NoProfile -File .\cpf-tools\scripts\sync-database-artifacts.ps1

# 3) Generator DB/MyBatis/SQL 변경을 기존 Generated Domain에 안전 적용
pwsh -NoProfile -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope Database -Apply

# 4) 고정 Domain 목록 없이 서로 다른 임의 Metadata로 생성 구조 parity 검증
pwsh -NoProfile -File .\cpf-tools\scripts\check-generator-arbitrary-domain-parity.ps1

# 5) 여러 개발 작업을 누적한 뒤 한 번의 전체 검증
pwsh -NoProfile -File .\cpf-tools\scripts\verify-full-product.ps1 -WithDatabase -WithGeneratorLifecycle -WithBrowser -RequireAll -Profile local
```

Generated Domain 동기화는 `generator-ownership.json`의 이전 SHA-256과 현재 파일을 비교합니다.
개발자가 generator-owned 파일을 직접 수정한 경우 자동 덮어쓰지 않고 실패하므로 먼저 변경 원인을 정본 Generator에 반영해야 합니다.

`verify-full-product.ps1`은 동일 검증을 PC마다 여러 번 수작업하는 대신 Build/Test/DB/Generator/Frontend/Browser/Evidence를 하나의 재현 가능한 순서로 모읍니다.
실행하지 않은 그룹은 `SKIPPED`이며 `-RequireAll`에서 전체 완료가 될 수 없습니다.


## Verification Tool Current-State Policy

정식 통합 검증 진입점은 `cpf-tools/scripts/verify-full-product.ps1`이다.

개별 verification helper는 다음 중 하나의 실제 Consumer를 가져야 한다.

- `verify-full-product.ps1`
- GitHub Workflow
- Gradle Task
- 다른 canonical Script
- 공식 Runbook/Developer workflow
- 독립 Runtime fault harness

날짜/QA 회차/`final-*` 이름의 과거 캠페인 Script와 Python helper가 현재 Gate에 흡수되었고
Consumer가 없다면 Repository에 역사 보관하지 않는다. Git history가 과거를 보존하며,
삭제 대상은 exact `cpf-docs/work/CPF_DELETE_MANIFEST.csv`로 관리한다.

반대로 실제 CI/Build/Release/Runtime Consumer가 있는 Script는 이름이 오래됐다는 이유만으로 삭제하지 않는다.
Developer는 Tool Hygiene 작업에서 `KEEP_CANONICAL_GATE / MERGE_INTO_CANONICAL_GATE /
RENAME_CURRENT / REMOVE_CANDIDATE`를 전수 판정한다.
