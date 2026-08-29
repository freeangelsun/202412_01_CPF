# CPF Required Full Runtime Request — 2026-08-27

## 목적

현재 Developer Source에서 정적/계약 PASS를 실제 Windows Java25/Gradle9.1/Docker/Browser Runtime으로 검증한다. 축소 Smoke가 아니라 전체 Lifecycle/E2E다.

## 필수 포함

- Java25 Root clean build/test/publication/SBOM.
- Generated Domain member/external/scratch build/test/runtime.
- VSCode Fresh Import/JDT Problems Error 0 / Warning 0.
- Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback/Reapply/Fault/Cleanup.
- Batch 5-role + Worker×2 + process kill/takeover/fencing/UNKNOWN/reconcile/Center-Cut/Gateway.
- One-WAS.
- File Log↔DB Log↔Trace↔ADM correlation.
- ADM/Backoffice Runtime OpenAPI + frontend build + Browser E2E/a11y.
- signed Performance Live + load/soak required scope.
- UTF-8 console/file/docker/native child process mojibake 0.
- Actual Open Git Fresh Binary Release/Golden Path.
- Source/Managed before/after + Fresh Replay.

## 최종 성공 기준

`FAIL=0`, mandatory `SKIP_ENV=0`, mandatory `NOT_EXECUTED=0`, unresolved `UNKNOWN=0`, Source drift=0, Managed drift=0, ExitCode=0.

## 사용자 로컬 실행 한 줄

```powershell
$ErrorActionPreference='Continue';$root=(Resolve-Path .).Path;$out=Join-Path $HOME 'Downloads';$vscode=Get-ChildItem -LiteralPath $out -Filter 'CPF_VSCODE_PROBLEMS_*.json' -File -ErrorAction SilentlyContinue|Sort-Object LastWriteTime -Descending|Select-Object -First 1;if(-not$vscode){throw 'Fresh VS Code Problems JSON이 없습니다. Fresh Gradle Import 후 Problems를 CPF_VSCODE_PROBLEMS_yyyyMMdd_HHmmss.json 이름으로 Downloads에 export하세요.'};$launch=Join-Path $out ('CPF_FULL_RUNTIME_LAUNCH_'+(Get-Date -Format 'yyyyMMdd_HHmmss')+'.log');$started=Get-Date;& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root 'cpf-tools\verification\tools\run-cpf-required-full-runtime-validation.ps1') -RepoRoot $root -DockerRoot 'C:\dev\Docker' -OutputRoot $out -VsCodeProblemsJson $vscode.FullName 2>&1|Tee-Object -FilePath $launch;$rc=$LASTEXITCODE;$ended=Get-Date;$runtimeLog=Get-ChildItem -LiteralPath $out -Filter 'CPF_REQUIRED_FULL_RUNTIME_*.log' -ErrorAction SilentlyContinue|Sort-Object LastWriteTime -Descending|Select-Object -First 1;$resultZip=Get-ChildItem -LiteralPath $out -Filter 'CPF_LOCAL_VALIDATION_*.zip' -ErrorAction SilentlyContinue|Sort-Object LastWriteTime -Descending|Select-Object -First 1;Write-Host ('CPF_FULL_RUNTIME_FINAL='+$(if($rc-eq 0){'PASS'}else{'FAIL'})+' EXIT_CODE='+$rc);Write-Host ('START='+$started.ToString('o')+' END='+$ended.ToString('o'));Write-Host ('VSCODE_PROBLEMS='+$vscode.FullName);Write-Host ('LAUNCH_LOG='+$launch);Write-Host ('RUNTIME_LOG='+$(if($runtimeLog){$runtimeLog.FullName}else{'NOT_CREATED'}));Write-Host ('RESULT_ZIP='+$(if($resultZip){$resultZip.FullName}else{'NOT_CREATED'}));Write-Host '--- GIT STATUS (READ ONLY) ---';git status --short
```
