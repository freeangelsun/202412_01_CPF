# V9 Root Overlay 설치와 실행

Repository Root에서 압축을 해제한다. ZIP 내부 첫 경로는 `cpf-docs/` 또는 `cpf-tools/`이며 Repository 최상위 파일은 없다.

```powershell
git fetch origin
git rev-parse origin/master
git status --short
```

V9 초기화:

```powershell
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/devgpt-control-v9/initialize-development-management.ps1
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/devgpt-control-v9/validate-development-management.ps1 -RequireFullAssignment
```

첫 Campaign:

```powershell
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/devgpt-control-v9/generate-development-requests.ps1 `
  -CampaignId DEV-YYYYMMDD-R01 `
  -AssignmentRevision 1 `
  -MaxItemsPerSession 8
```

동일 Campaign과 Revision 경로가 존재하면 생성기는 실패한다.
