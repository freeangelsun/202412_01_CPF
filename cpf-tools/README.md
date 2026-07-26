# CPF Tools

`cpf-tools`는 Core Platform Framework의 설치·DB lifecycle·Generated Domain·검증·Evidence 보조 도구를 소유합니다.

정식 사용 가이드는 [`cpf-docs/guides/CPF_TOOLS_GUIDE.md`](../cpf-docs/guides/CPF_TOOLS_GUIDE.md)를 참조합니다.

Build Support Unit도 제품 Runtime Module과 분리해 이 경계가 소유합니다.

```text
cpf-tools/build/gradle-plugin   CPF Domain Convention Plugin 격리 Build
cpf-tools/build/platform-bom    CPF Published Dependency BOM 격리 Build
```

핵심 명령:

```powershell
# DB Canonical/Vendor Pack/Manifest 동기화
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\sync-database-artifacts.ps1

# Platform DB 선택 설치
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun

# Generated Domain 생성
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\generator\create-domain.ps1 -DomainName payment -SystemCode PAY -Apply

# EXS도 동일 Generated Domain 정책
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\generator\create-domain.ps1 -DomainName external -SystemCode EXS -Apply
```

DB/SQL/Metadata를 수정한 뒤 `sync-database-artifacts.ps1`을 생략해서는 안 됩니다.

## 작업 시작 / 동기화 / 통합 검증

```powershell
# 1) 다른 PC/AI가 이어받기 전에 정본·요청·HEAD 확인
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-work-context.ps1

# 2) DB Canonical + Vendor Pack + Schema Manifest + 기존 Generated Domain DB artifact parity
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\sync-database-artifacts.ps1

# 3) Generator DB/MyBatis/SQL 변경을 기존 Generated Domain에 안전 적용
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope Database -Apply

# 4) EXS는 baseline에 두지 않고 검증할 때만 생성→검증→삭제
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-exs-generated-domain-lifecycle.ps1

# 5) 여러 개발 작업을 누적한 뒤 한 번의 전체 검증
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-full-product.ps1 -WithDatabase -WithGeneratorLifecycle -WithBrowser -RequireAll -Profile local
```

Generated Domain 동기화는 `generator-ownership.json`의 이전 SHA-256과 현재 파일을 비교한다.
개발자가 generator-owned 파일을 직접 수정한 경우 자동 덮어쓰지 않고 실패하므로 먼저 변경 원인을 정본 Generator에 반영해야 한다.

`verify-full-product.ps1`은 동일 검증을 PC마다 여러 번 수작업하는 대신 Build/Test/DB/Generator/Frontend/Browser/Evidence를 하나의 재현 가능한 순서로 모은다.
실행하지 않은 그룹은 `SKIPPED`이며 `-RequireAll`에서 전체 완료가 될 수 없다.
