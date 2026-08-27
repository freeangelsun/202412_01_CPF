# CPF C 개발/QA 관리_1_8 — 최종 Local 적용·검증 명령 — 2026-08-27

## 전제

- 프로젝트 Root에서 실행한다.
- 최종 ZIP과 같은 이름의 `.sha256.txt` sidecar를 `$HOME\Downloads`에 함께 둔다.
- Git reset/restore/clean/stash를 사용하지 않는다.
- 현재 DevGPT Product Source Identity 기대값: `b41abc892e6652ef8461ece1b3daa1057acfbf153185fe8e67ea70a6e20de4af`.

## 1. 최종 Overlay SHA 확인 + 적용 + 내부 Target Hash 검증 — 한 줄

```powershell
$ErrorActionPreference='Stop';$root=(Resolve-Path .).Path;$zip=Join-Path $HOME 'Downloads\CPF_C_DEV_QA_1_8_FINAL_SOURCE_OVERLAY_20260827.zip';$sidecar=Join-Path $HOME 'Downloads\CPF_C_DEV_QA_1_8_FINAL_SOURCE_OVERLAY_20260827.zip.sha256.txt';if(!(Test-Path -LiteralPath $zip)){throw "ZIP NOT FOUND: $zip"};if(!(Test-Path -LiteralPath $sidecar)){throw "SHA SIDECAR NOT FOUND: $sidecar"};$expected=((Get-Content -LiteralPath $sidecar -Raw -Encoding UTF8)-split'\s+')[0].ToUpperInvariant();$actual=(Get-FileHash -Algorithm SHA256 -LiteralPath $zip).Hash.ToUpperInvariant();if($actual-ne$expected){throw "ZIP SHA256 MISMATCH expected=$expected actual=$actual"};$tmp=Join-Path $env:TEMP ('cpf-c18-final-'+[guid]::NewGuid().ToString('N'));New-Item -ItemType Directory -Path $tmp -Force|Out-Null;try{Expand-Archive -LiteralPath $zip -DestinationPath $tmp -Force;Get-ChildItem -LiteralPath $tmp -Force|Copy-Item -Destination $root -Recurse -Force;$sum=Join-Path $root 'cpf-docs\work\current\SHA256SUMS.txt';$n=0;foreach($line in Get-Content -LiteralPath $sum -Encoding UTF8){if($line-match'^([0-9a-fA-F]{64})  (.+)$'){$p=Join-Path $root ($Matches[2]-replace'/','\');if(!(Test-Path -LiteralPath $p -PathType Leaf)){throw "MISSING TARGET: $($Matches[2])"};if((Get-FileHash -Algorithm SHA256 -LiteralPath $p).Hash.ToUpperInvariant()-ne$Matches[1].ToUpperInvariant()){throw "TARGET HASH MISMATCH: $($Matches[2])"};$n++}};Write-Host "CPF_C18_FINAL_APPLY=PASS ZIP_SHA256=$actual TARGET_HASHES=$n"}finally{Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue}
```

## 2. 승인 Delete Manifest 적용 — 한 줄

```powershell
$ErrorActionPreference='Stop';$root=(Resolve-Path .).Path;$mf=Join-Path $root 'cpf-docs\work\current\DELETE_MANIFEST.csv';$protected=@('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/');$deleted=0;$missing=0;Import-Csv -LiteralPath $mf -Encoding UTF8|Where-Object{$_.approved-eq'true'-and$_.user_approved-eq'true'-and$_.precondition-eq'SATISFIED'-and$_.lifecycle-eq'PENDING_USER_EXECUTION'}|ForEach-Object{$rel=$_.path.Replace('\','/').TrimStart('/');if($rel.Contains('../')-or($protected|Where-Object{$rel.StartsWith($_)})){throw "UNSAFE DELETE: $rel"};$p=[IO.Path]::GetFullPath((Join-Path $root $rel));if(!$p.StartsWith($root+[IO.Path]::DirectorySeparatorChar)){throw "PATH ESCAPE: $rel"};if(!(Test-Path -LiteralPath $p)){$missing++;return};if(Test-Path -LiteralPath $p -PathType Container){throw "DIRECTORY DELETE REJECTED: $rel"};if($_.replacement_path){$rp=Join-Path $root ($_.replacement_path-replace'/','\');if(!(Test-Path -LiteralPath $rp)){throw "REPLACEMENT MISSING: $($_.replacement_path)"}};Remove-Item -LiteralPath $p -Force;$deleted++};Write-Host "CPF_DELETE_MANIFEST=PASS DELETED=$deleted ALREADY_MISSING=$missing"
```

## 3. 저비용 Source/Hygiene/Git-trackability 검증 — 한 줄

```powershell
$ErrorActionPreference='Stop';$root=(Resolve-Path .).Path;python cpf-tools\verification\tools\cpf-source-state.py --root $root --scope source;python cpf-tools\verification\tools\verify-cpf-batch-standalone-profile.py --root $root;python cpf-tools\verification\tools\verify-cpf-current-final.py;python cpf-tools\verification\nxt3\verify_nxt3_hygiene.py;python cpf-tools\verification\nxt3\verify_nxt3_repository_garbage.py;$ignored=@();foreach($role in 'control-plane','scheduler','worker','agent','center-cut'){foreach($n in 'run.ps1','stop.ps1','run.sh','stop.sh'){$rel="cpf-batch/$role/bin/$n";git check-ignore --quiet --no-index -- $rel;if($LASTEXITCODE-eq 0){$ignored+=$rel}elseif($LASTEXITCODE-ne 1){throw "git check-ignore failed: $rel rc=$LASTEXITCODE"}}};if($ignored.Count){throw "BATCH SHELL GIT-IGNORED: $($ignored -join ', ')"};Write-Host 'CPF_LOW_COST_VERIFY=PASS BATCH_SHELL_GIT_TRACKABLE=20'
```

기대 Source Identity는 `b41abc892e6652ef8461ece1b3daa1057acfbf153185fe8e67ea70a6e20de4af`이다. Delete Manifest가 제품 Source Scope 밖의 stale Evidence만 삭제하면 Product Source Identity는 변하지 않아야 한다.

## 4. Fresh VS Code Problems JSON 준비

Java25 + Gradle Fresh Import를 수행한 뒤 VS Code Problems를 `CPF_VSCODE_PROBLEMS_yyyyMMdd_HHmmss.json` 이름으로 `$HOME\Downloads`에 저장한다. Error 0 / Warning 0이 아니면 Full Runtime은 FAIL이 정상이다.

## 5. 최대강도 Local Full Runtime + Fresh Replay — 한 줄

```powershell
$ErrorActionPreference='Continue';$root=(Resolve-Path .).Path;$out=Join-Path $HOME 'Downloads';$vscode=Get-ChildItem -LiteralPath $out -Filter 'CPF_VSCODE_PROBLEMS_*.json' -File -ErrorAction SilentlyContinue|Sort-Object LastWriteTime -Descending|Select-Object -First 1;if(-not$vscode){throw 'Fresh VS Code Problems JSON이 없습니다. Fresh Gradle Import 후 Problems를 CPF_VSCODE_PROBLEMS_yyyyMMdd_HHmmss.json 이름으로 Downloads에 export하세요.'};$launch=Join-Path $out ('CPF_FULL_RUNTIME_LAUNCH_'+(Get-Date -Format 'yyyyMMdd_HHmmss')+'.log');$started=Get-Date;& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root 'cpf-tools\verification\tools\run-cpf-required-full-runtime-validation.ps1') -RepoRoot $root -DockerRoot 'C:\dev\Docker' -OutputRoot $out -VsCodeProblemsJson $vscode.FullName 2>&1|Tee-Object -FilePath $launch;$rc=$LASTEXITCODE;$ended=Get-Date;$runtimeLog=Get-ChildItem -LiteralPath $out -Filter 'CPF_REQUIRED_FULL_RUNTIME_*.log' -ErrorAction SilentlyContinue|Sort-Object LastWriteTime -Descending|Select-Object -First 1;$resultZip=Get-ChildItem -LiteralPath $out -Filter 'CPF_LOCAL_VALIDATION_*.zip' -ErrorAction SilentlyContinue|Sort-Object LastWriteTime -Descending|Select-Object -First 1;Write-Host ('CPF_FULL_RUNTIME_FINAL='+$(if($rc-eq 0){'PASS'}else{'FAIL'})+' EXIT_CODE='+$rc);Write-Host ('START='+$started.ToString('o')+' END='+$ended.ToString('o'));Write-Host ('VSCODE_PROBLEMS='+$vscode.FullName);Write-Host ('LAUNCH_LOG='+$launch);Write-Host ('RUNTIME_LOG='+$(if($runtimeLog){$runtimeLog.FullName}else{'NOT_CREATED'}));Write-Host ('RESULT_ZIP='+$(if($resultZip){$resultZip.FullName}else{'NOT_CREATED'}));Write-Host '--- GIT STATUS (READ ONLY) ---';git status --short
```

성공 기준: `FAIL=0 / mandatory SKIP_ENV=0 / mandatory NOT_EXECUTED=0 / unresolved UNKNOWN=0 / Source drift=0 / Managed drift=0 / ExitCode=0`. Runner가 동일 Source Fresh Replay까지 수행해야 한다.

## 6. Git 상태 확인 — 한 줄

```powershell
git status --short
```

Batch Shell 20개가 신규 Source라면 Git status에 추적 가능한 상태로 나타나야 하며 `git check-ignore` 결과가 ignored이면 안 된다.

## 7. Codex 연속 수행

- `cpf-docs/work/current/CODEX_NEXT_WORK_INSTRUCTION_20260827.md` 내용을 **현재 진행 중인 Codex turn에 그대로 전달**한다.
- 새 Codex 업무로 초기화하지 않는다.
- 결과 승계는 `CODEX_RESULT_TO_NEXT_WORK_TRACE_20260827.md` 기준으로 한다.
