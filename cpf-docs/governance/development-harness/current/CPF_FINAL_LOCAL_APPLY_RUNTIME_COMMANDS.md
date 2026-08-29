# CPF Development Harness Final Commands — Current

모든 실행 명령은 프로젝트 Root에서 **각각 한 줄**로 실행한다. Git commit/push/reset/restore/stash/clean은 포함하지 않는다.

## Windows PowerShell 7 — Overlay 적용 한 줄
```powershell
$ErrorActionPreference='Stop';$PSNativeCommandUseErrorActionPreference=$true;$root=(git rev-parse --show-toplevel).Trim();Set-Location $root;$zip=Get-ChildItem "$HOME\Downloads" -File -Filter 'CPF_C_DEV_QA_2_5_SOURCE_OVERLAY_*.zip'|Sort-Object LastWriteTime -Descending|Select-Object -First 1;if(!$zip){throw 'CPF SOURCE OVERLAY ZIP NOT FOUND'};$side="$($zip.FullName).sha256.txt";if(!(Test-Path -LiteralPath $side)){throw 'SHA256 SIDECAR NOT FOUND'};$expected=((Get-Content -LiteralPath $side -Raw -Encoding UTF8)-split '\s+')[0].ToUpperInvariant();$actual=(Get-FileHash -LiteralPath $zip.FullName -Algorithm SHA256).Hash.ToUpperInvariant();if($actual-ne$expected){throw "ZIP SHA256 MISMATCH expected=$expected actual=$actual"};tar -xf $zip.FullName -C $root;if($LASTEXITCODE-ne0){throw "OVERLAY APPLY FAILED RC=$LASTEXITCODE"};python .\cpf-docs\governance\development-harness\validators\run_all_gates.py;if($LASTEXITCODE-ne0){throw 'HARNESS FINAL GATE FAILED AFTER OVERLAY'};Write-Host "CPF_OVERLAY_APPLY=PASS ZIP=$($zip.FullName) SHA256=$actual"
```

## Windows PowerShell 7 — 기존 정본·가비지·빈 폴더 삭제 한 줄
```powershell
$ErrorActionPreference='Stop';$PSNativeCommandUseErrorActionPreference=$true;$root=(git rev-parse --show-toplevel).Trim();Set-Location $root;& pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-docs\governance\development-harness\DELETE_LEGACY_CANONICAL.ps1 -ApplyApprovedManifest;if($LASTEXITCODE-ne0){throw "DELETE FAILED RC=$LASTEXITCODE"}
```

## Windows PowerShell 7 — 저비용 Current Harness 검증 한 줄
```powershell
$ErrorActionPreference='Stop';$PSNativeCommandUseErrorActionPreference=$true;$root=(git rev-parse --show-toplevel).Trim();Set-Location $root;python .\cpf-docs\governance\development-harness\validators\run_all_gates.py;python .\cpf-tools\verification\tools\verify-cpf-current-final.py;python .\cpf-tools\verification\nxt3\verify_nxt3_hygiene.py;python .\cpf-tools\verification\nxt3\verify_nxt3_repository_garbage.py;python .\cpf-tools\verification\tools\verify-cpf-runtime-utf8-boundaries.py --root $root;python .\cpf-tools\verification\tools\verify-cpf-physical-db-consolidation.py --root $root;python .\cpf-tools\verification\tools\verify-cpf-unified-cli.py --root $root;python .\cpf-tools\verification\verify_requirement_progress.py --root $root;python .\cpf-tools\verification\tools\verify-cpf-requirement-projection-consistency.py --root $root;Write-Host 'CPF_CURRENT_HARNESS_LOW_COST_VERIFY=PASS';git status --short
```

## Windows PowerShell 7 — 최고강도 Java25 Full Runtime + Fresh Replay 한 줄
```powershell
$ErrorActionPreference='Continue';$root=(git rev-parse --show-toplevel).Trim();Set-Location $root;$out=Join-Path $HOME 'Downloads';$vscode=Get-ChildItem $out -Filter 'CPF_VSCODE_PROBLEMS_*.json' -File|Sort-Object LastWriteTime -Descending|Select-Object -First 1;if(-not$vscode){throw 'Fresh Java25/Gradle VS Code Problems JSON 필요: Error=0 Warning=0'};$launch=Join-Path $out ('CPF_FULL_RUNTIME_LAUNCH_'+(Get-Date -Format 'yyyyMMdd_HHmmss')+'.log');$started=Get-Date;& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root 'cpf-tools\verification\tools\run-cpf-required-full-runtime-validation.ps1') -RepoRoot $root -DockerRoot 'C:\dev\Docker' -OutputRoot $out -VsCodeProblemsJson $vscode.FullName 2>&1|Tee-Object -FilePath $launch;$rc=$LASTEXITCODE;Write-Host ('CPF_FULL_RUNTIME_FINAL='+$(if($rc-eq0){'PASS'}else{'FAIL'})+' EXIT_CODE='+$rc+' START='+$started.ToString('o')+' END='+(Get-Date).ToString('o')+' LOG='+$launch);git status --short;if($rc-ne0){throw "FULL RUNTIME FAILED RC=$rc"}
```

## Linux — Overlay 적용 한 줄
```bash
set -euo pipefail; ROOT="$(git rev-parse --show-toplevel)"; ZIP="$(ls -1t "$HOME"/Downloads/CPF_C_DEV_QA_2_5_SOURCE_OVERLAY_*.zip | head -1)"; SIDE="$ZIP.sha256.txt"; test -f "$SIDE"; EXPECTED="$(awk '{print toupper($1);exit}' "$SIDE")"; ACTUAL="$(sha256sum "$ZIP"|awk '{print toupper($1)}')"; test "$EXPECTED" = "$ACTUAL"; unzip -oq "$ZIP" -d "$ROOT"; cd "$ROOT"; python3 ./cpf-docs/governance/development-harness/validators/run_all_gates.py; echo "CPF_OVERLAY_APPLY=PASS ZIP=$ZIP SHA256=$ACTUAL"
```

## Linux — 기존 정본·가비지·빈 폴더 삭제 한 줄
```bash
set -euo pipefail; ROOT="$(git rev-parse --show-toplevel)"; cd "$ROOT"; bash ./cpf-docs/governance/development-harness/DELETE_LEGACY_CANONICAL.sh --apply-approved-manifest
```

실제 Physical 결과가 없으면 PASS로 승격하지 않는다. 필수 `FAIL=0`, mandatory `SKIP/NOT_EXECUTED=0`, unresolved `UNKNOWN=0`, Fresh VS Code `Error=0 Warning=0`, Source/Managed drift=0, Same Source Fresh Replay PASS, Independent Reviewer/QA PASS가 전체 완료 조건이다.
