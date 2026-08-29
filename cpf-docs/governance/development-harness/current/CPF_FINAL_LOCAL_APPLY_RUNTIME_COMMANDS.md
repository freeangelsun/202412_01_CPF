# CPF Development Harness Final Commands — Current

모든 실행 명령은 프로젝트 Root에서 **각각 한 줄**로 실행한다. Git write는 포함하지 않는다.

## Windows PowerShell 7 — 적용 한 줄
```powershell
$ErrorActionPreference='Stop';$zip=Get-ChildItem "$HOME\Downloads" -File -Filter 'CPF_DEVELOPMENT_HARNESS_CURRENT_FINAL_*.zip'|Sort-Object LastWriteTime -Descending|Select-Object -First 1;if(!$zip){throw 'FINAL HARNESS ZIP NOT FOUND'};$side="$($zip.FullName).sha256.txt";if(!(Test-Path $side)){throw 'SHA SIDECAR NOT FOUND'};$actual=(Get-FileHash -Algorithm SHA256 $zip.FullName).Hash.ToUpperInvariant();$expected=((Get-Content $side -Raw -Encoding UTF8)-split '\s+')[0].ToUpperInvariant();if($actual-ne$expected){throw "ZIP SHA MISMATCH expected=$expected actual=$actual"};$tmp=Join-Path $env:TEMP ('cpf-dev-harness-bootstrap-'+[guid]::NewGuid().ToString('N'));New-Item -ItemType Directory $tmp -Force|Out-Null;try{Expand-Archive -LiteralPath $zip.FullName -DestinationPath $tmp -Force;& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $tmp 'cpf-docs\deliverables\development-harness\APPLY_CURRENT_HARNESS.ps1') -ZipPath $zip.FullName;if($LASTEXITCODE-ne0){throw "APPLY FAILED RC=$LASTEXITCODE"}}finally{Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue}
```

## Windows — 기존 정본·가비지·빈 폴더 삭제 한 줄
```powershell
$ErrorActionPreference='Stop';$root=(git rev-parse --show-toplevel).Trim();Set-Location $root;& pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-docs\governance\development-harness\DELETE_LEGACY_CANONICAL.ps1;if($LASTEXITCODE-ne0){throw "DELETE FAILED RC=$LASTEXITCODE"}
```

## Windows — 저비용 검증 한 줄
```powershell
$ErrorActionPreference='Stop';$root=(git rev-parse --show-toplevel).Trim();Set-Location $root;python .\cpf-docs\governance\development-harness\validators\run_all_gates.py;python .\cpf-tools\verification\tools\verify-cpf-current-final.py;python .\cpf-tools\verification\nxt3\verify_nxt3_hygiene.py;python .\cpf-tools\verification\nxt3\verify_nxt3_repository_garbage.py;python .\cpf-tools\verification\tools\verify-cpf-runtime-utf8-boundaries.py --root $root;python .\cpf-tools\verification\tools\verify-cpf-physical-db-consolidation.py --root $root;python .\cpf-tools\verification\tools\verify-cpf-unified-cli.py --root $root;python .\cpf-tools\verification\verify_requirement_progress.py --root $root;python .\cpf-tools\verification\tools\verify-cpf-requirement-projection-consistency.py --root $root;python -m pytest -q .\cpf-tools\release\open-git\tests .\cpf-tools\release\public\tests;Write-Host 'CPF_FINAL_LOW_COST_VERIFY=PASS';git status --short
```

## Windows — 최고강도 Local Runtime + Fresh Replay 한 줄
```powershell
$ErrorActionPreference='Continue';$root=(git rev-parse --show-toplevel).Trim();$out=Join-Path $HOME 'Downloads';$vscode=Get-ChildItem $out -Filter 'CPF_VSCODE_PROBLEMS_*.json' -File|Sort-Object LastWriteTime -Descending|Select-Object -First 1;if(-not$vscode){throw 'Fresh Java25/Gradle VS Code Problems JSON 필요: Error=0 Warning=0'};$launch=Join-Path $out ('CPF_FULL_RUNTIME_LAUNCH_'+(Get-Date -Format 'yyyyMMdd_HHmmss')+'.log');$started=Get-Date;& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root 'cpf-tools\verification\tools\run-cpf-required-full-runtime-validation.ps1') -RepoRoot $root -DockerRoot 'C:\dev\Docker' -OutputRoot $out -VsCodeProblemsJson $vscode.FullName 2>&1|Tee-Object -FilePath $launch;$rc=$LASTEXITCODE;Write-Host ('CPF_FULL_RUNTIME_FINAL='+$(if($rc-eq0){'PASS'}else{'FAIL'})+' EXIT_CODE='+$rc+' START='+$started.ToString('o')+' END='+(Get-Date).ToString('o')+' LOG='+$launch);git status --short;if($rc-ne0){throw "FULL RUNTIME FAILED RC=$rc"}
```

## Windows — Git read-only 상태 한 줄
```powershell
$root=(git rev-parse --show-toplevel).Trim();Set-Location $root;git branch --show-current;git rev-parse HEAD;git status --short
```

## Linux — 적용 한 줄
```bash
set -euo pipefail; ZIP="$(ls -1t "$HOME"/Downloads/CPF_DEVELOPMENT_HARNESS_CURRENT_FINAL_*.zip | head -1)"; SIDE="$ZIP.sha256.txt"; test -f "$SIDE"; EXPECTED="$(awk '{print toupper($1);exit}' "$SIDE")"; ACTUAL="$(sha256sum "$ZIP"|awk '{print toupper($1)}')"; test "$EXPECTED" = "$ACTUAL"; TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT; python3 -c 'import zipfile,sys;zipfile.ZipFile(sys.argv[1]).extractall(sys.argv[2])' "$ZIP" "$TMP"; bash "$TMP/cpf-docs/deliverables/development-harness/apply-current-harness.sh" "$ZIP"
```

## Linux — 기존 정본·가비지·빈 폴더 삭제 한 줄
```bash
set -euo pipefail; ROOT="$(git rev-parse --show-toplevel)"; cd "$ROOT"; bash ./cpf-docs/governance/development-harness/DELETE_LEGACY_CANONICAL.sh
```

## Linux — 저비용 검증 한 줄
```bash
set -euo pipefail; ROOT="$(git rev-parse --show-toplevel)"; cd "$ROOT"; python3 ./cpf-docs/governance/development-harness/validators/run_all_gates.py; python3 ./cpf-tools/verification/tools/verify-cpf-current-final.py; python3 ./cpf-tools/verification/nxt3/verify_nxt3_hygiene.py; python3 ./cpf-tools/verification/nxt3/verify_nxt3_repository_garbage.py; python3 ./cpf-tools/verification/tools/verify-cpf-runtime-utf8-boundaries.py --root "$ROOT"; python3 ./cpf-tools/verification/tools/verify-cpf-physical-db-consolidation.py --root "$ROOT"; python3 ./cpf-tools/verification/tools/verify-cpf-unified-cli.py --root "$ROOT"; python3 ./cpf-tools/verification/verify_requirement_progress.py --root "$ROOT"; python3 ./cpf-tools/verification/tools/verify-cpf-requirement-projection-consistency.py --root "$ROOT"; python3 -m pytest -q ./cpf-tools/release/open-git/tests ./cpf-tools/release/public/tests; echo CPF_FINAL_LOW_COST_VERIFY=PASS; git status --short
```

## Linux — 최고강도 Local Runtime + Fresh Replay 한 줄
```bash
set -euo pipefail; ROOT="$(git rev-parse --show-toplevel)"; cd "$ROOT"; python3 ./cpf-docs/governance/development-harness/validators/run_all_gates.py; bash ./cpf-tools/verification/nxt3/run_nxt3_final_all.sh; git status --short
```

## Linux — Git read-only 상태 한 줄
```bash
ROOT="$(git rev-parse --show-toplevel)"; cd "$ROOT"; git branch --show-current; git rev-parse HEAD; git status --short
```

최고강도 성공조건은 필수 `FAIL=0`, mandatory `SKIP/NOT_EXECUTED=0`, unresolved `UNKNOWN=0`, VS Code Java25/Gradle `Error=0 Warning=0`, Source/Managed drift=0, Fresh Replay PASS다. 실제 결과가 없으면 `PASS`로 승계하지 않는다.
