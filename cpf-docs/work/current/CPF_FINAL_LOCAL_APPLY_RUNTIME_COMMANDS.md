# CPF Final Local Apply / Runtime / Push Commands — Current

> PowerShell 7.x, CPF 프로젝트 Root에서 실행. **실제 Fresh VS Code Error 0 / Warning 0 + Full Runtime mojibake 0 전 Commit/Push 금지.**

## 1. 최신 VS Code + UTF-8 Final Overlay 적용 + 저비용 검증 — 한 줄

```powershell
$ErrorActionPreference='Stop';$PSNativeCommandUseErrorActionPreference=$true;$root=(git rev-parse --show-toplevel).Trim();Set-Location $root;$zip=Get-ChildItem "$HOME\Downloads" -File -Filter 'CPF_C_DEV_QA_2_4_VSCODE_UTF8_FINAL_*.zip' -ErrorAction SilentlyContinue|Sort-Object LastWriteTime -Descending|Select-Object -First 1;if(!$zip){throw 'FINAL OVERLAY ZIP NOT FOUND'};$tmp=Join-Path $env:TEMP ('cpf-final-'+[guid]::NewGuid().ToString('N'));New-Item -ItemType Directory -Path $tmp -Force|Out-Null;try{Expand-Archive -LiteralPath $zip.FullName -DestinationPath $tmp -Force;Get-ChildItem -LiteralPath $tmp -Recurse -File|ForEach-Object{$rel=$_.FullName.Substring($tmp.Length).TrimStart('\','/');if($rel){$dst=Join-Path $root $rel;$parent=Split-Path -Parent $dst;if(!(Test-Path -LiteralPath $parent)){New-Item -ItemType Directory -Path $parent -Force|Out-Null};Copy-Item -LiteralPath $_.FullName -Destination $dst -Force}}}finally{if(Test-Path -LiteralPath $tmp){Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue}};python cpf-tools\verification\tools\cpf-source-state.py --root $root --scope source;python cpf-tools\verification\tools\verify-cpf-current-final.py;python cpf-tools\verification\nxt3\verify_nxt3_hygiene.py;python cpf-tools\verification\nxt3\verify_nxt3_repository_garbage.py;python cpf-tools\verification\tools\verify-cpf-runtime-utf8-boundaries.py --root $root;python cpf-tools\verification\tools\verify-cpf-unified-cli.py --root $root;python cpf-tools\verification\verify_requirement_progress.py --root $root;python cpf-tools\verification\tools\verify-cpf-requirement-projection-consistency.py --root $root;python -m pytest -q cpf-tools\testing\tools\tests\test_cpf_local_full_validation_contract.py cpf-tools\testing\tools\tests\test_cpf_required_full_runtime_contract.py cpf-tools\runtime\tools\tests\test_runtime_handoff_scripts.py cpf-tools\verification\tests\test_runtime_utf8_boundary_contract.py cpf-tools\verification\tests\test_cpf_cross_platform_cli_contract.py cpf-tools\testing\tools\tests\test_cpf_unified_cli_contract.py cpf-tools\verification\tests\test_cpf_vscode_classpath_output_contract.py;Write-Host 'CPF_FINAL_OVERLAY_APPLY_AND_LOW_COST_VERIFY=PASS';git status --short
```

## 2. 최대강도 Full Runtime — 한 줄

```powershell
$ErrorActionPreference='Continue';$root=(git rev-parse --show-toplevel).Trim();Set-Location $root;$out=Join-Path $HOME 'Downloads';$log=Join-Path $out ('CPF_FULL_RUNTIME_LAUNCH_'+(Get-Date -Format 'yyyyMMdd_HHmmss')+'.log');& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root 'cpf-tools\verification\tools\run-cpf-local-full-validation.ps1') -RepoRoot $root -DockerRoot 'C:\dev\Docker' -OutputRoot $out -FullLocal -IncludePerformanceLoad -AllowDestructiveDbRollback -StrictExit 2>&1|Tee-Object -FilePath $log;$rc=$LASTEXITCODE;Write-Host "CPF_FULL_RUNTIME_EXIT_CODE=$rc";Write-Host "LOG=$log";git status --short;if($rc-ne0){throw "CPF FULL RUNTIME FAIL rc=$rc log=$log"}
```

성공조건: `FAIL=0 / mandatory SKIP_ENV=0 / mandatory NOT_EXECUTED=0 / unresolved UNKNOWN=0 / mojibake=0 / Source drift=0 / Managed drift=0 / Fresh Replay PASS`. 이후 **VS Code Fresh Gradle Import 전체 Domain/Module Error 0 / Warning 0**을 실제 확인한다.

## 3. 전부 PASS 후 Commit + Push — 한 줄

```powershell
$ErrorActionPreference='Stop';$PSNativeCommandUseErrorActionPreference=$true;$root=(git rev-parse --show-toplevel).Trim();Set-Location $root;$ts=Get-Date -Format yyyyMMdd_HHmmss;git add -A;$staged=@(git diff --cached --name-only);if($staged.Count-eq0){throw 'NO STAGED CHANGES'};git commit -m "CPF $ts";git push origin master;Write-Host "PUSHED_SHA=$((git rev-parse HEAD).Trim())";git status --short
```
