# Apply and Low-Cost Verify

## Apply in Windows PowerShell

```powershell
$z=Get-ChildItem "$env:USERPROFILE\Downloads" -File -Filter "CPF_REV004_R4_DEVGPT_COMPLETE_ROOT_OVERLAY_*.zip" | Sort-Object LastWriteTime -Descending | Select-Object -First 1; if(-not $z){throw "Overlay ZIP을 찾을 수 없습니다."}; $repo=(git rev-parse --show-toplevel).Trim(); if((git -C $repo rev-parse HEAD).Trim() -ne "a8be27a34bdac0b7c075e06d6e86571244c96421"){throw "HEAD 불일치"}; Expand-Archive -LiteralPath $z.FullName -DestinationPath $repo -Force; git -C $repo diff --check
```

## Low-cost verification

```powershell
python -B cpf-tools/verification/final-dev/verify-rev004-overlay.py .; python -B cpf-tools/verification/verify_starter_catalog.py --root .
```

## Working tree

```powershell
git -c core.quotepath=false status --short --branch
```

Cleanup target: none. This package contains no repository bytecode/cache/temp directory. The retained legacy starter is not deleted.
