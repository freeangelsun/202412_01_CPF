# CPF Framework Public Developer Workspace

This repository is the public developer workspace for CPF (Core Platform Framework).
Framework implementation source is not distributed here. Applications consume released CPF artifacts through the CPF public BOM and public Starters.

## Included reference surfaces

- `cpf-member`: generated business-domain shaped Online/Batch reference source.
- `cpf-biz-channel`: optional DB-less Pure Spring Boot BZA channel reference.
- `cpf-biz-frontend`: optional BZA reference frontend. It calls the BZA Channel only.

## Verify

Windows PowerShell:

```powershell
.\tools\verify-public-workspace.ps1
```

Linux/macOS:

```bash
./tools/verify-public-workspace.sh
```

Verification is fail-closed. A failed build/test must be fixed before a release is published.
