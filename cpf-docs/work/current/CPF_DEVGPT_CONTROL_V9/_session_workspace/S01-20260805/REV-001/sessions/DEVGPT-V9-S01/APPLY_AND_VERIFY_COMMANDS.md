# Apply and verify commands

## Apply Root Overlay (PowerShell one line)

```powershell
$z=Get-ChildItem "$env:USERPROFILE\Downloads" -File -Filter "CPF_V9_S01_S01-20260805_REV-001_CHECKPOINT_ROOT_OVERLAY.zip" | Sort-Object LastWriteTime -Descending | Select-Object -First 1; if(-not $z){throw "Checkpoint ZIP not found"}; Expand-Archive -LiteralPath $z.FullName -DestinationPath (Get-Location) -Force
```

## Low-cost verification (PowerShell one line)

```powershell
git diff --check; @('cpf-core/src/main/java/com/cpf/core/api/locking/CpfLockManager.java','cpf-core/src/main/java/com/cpf/core/api/logging/CpfAsyncLogWriterOperations.java') | ForEach-Object { if(-not (Test-Path -LiteralPath $_)){throw "Missing $_"} }
```

## Git status (PowerShell one line)

```powershell
git -c core.quotepath=false status --short --branch
```

No deletion is requested. Do not push until S02/S04/S05/S06 integration requests are implemented and latest-master regression succeeds.
