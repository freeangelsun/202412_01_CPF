# CPF Tools

`cpf-tools`는 Core Platform Framework의 설치·DB lifecycle·Generated Domain·검증·Evidence 보조 도구를 소유합니다.

정식 사용 가이드는 [`cpf-docs/guides/CPF_TOOLS_GUIDE.md`](../cpf-docs/guides/CPF_TOOLS_GUIDE.md)를 참조합니다.

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
