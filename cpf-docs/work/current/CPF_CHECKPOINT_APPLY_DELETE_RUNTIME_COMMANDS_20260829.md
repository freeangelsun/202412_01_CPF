# CPF C 개발/QA 관리_2_1 CHECKPOINT — 적용·삭제·로컬 Runtime 명령

> 이 명령은 **중간 체크포인트**용이다. 적용 후에도 최종 완료/Push 가능 상태를 의미하지 않는다.

## 1. Checkpoint Overlay 적용 — PowerShell 한 줄

```powershell
$ErrorActionPreference='Stop';$root=(git rev-parse --show-toplevel).Trim();$zip=Get-ChildItem "$HOME\Downloads" -File -Filter 'CPF_C_DEV_QA_2_1_CHECKPOINT_OVERLAY_20260829_*.zip'|Sort-Object LastWriteTime -Descending|Select-Object -First 1;if(!$zip){throw 'CHECKPOINT ZIP NOT FOUND'};$side="$($zip.FullName).sha256.txt";if(!(Test-Path -LiteralPath $side)){throw "SHA SIDECAR NOT FOUND: $side"};$expected=((Get-Content -LiteralPath $side -Raw -Encoding UTF8)-split'\s+')[0].ToUpperInvariant();$actual=(Get-FileHash -LiteralPath $zip.FullName -Algorithm SHA256).Hash.ToUpperInvariant();if($actual-ne$expected){throw "ZIP SHA256 MISMATCH expected=$expected actual=$actual"};$tmp=Join-Path $env:TEMP ('cpf-c21-checkpoint-'+[guid]::NewGuid().ToString('N'));New-Item -ItemType Directory -Path $tmp -Force|Out-Null;try{Expand-Archive -LiteralPath $zip.FullName -DestinationPath $tmp -Force;Get-ChildItem -LiteralPath $tmp -Recurse -File|ForEach-Object{$rel=$_.FullName.Substring($tmp.Length).TrimStart('\','/');$dst=Join-Path $root $rel;$parent=Split-Path -Parent $dst;if(!(Test-Path -LiteralPath $parent)){New-Item -ItemType Directory -Path $parent -Force|Out-Null};Copy-Item -LiteralPath $_.FullName -Destination $dst -Force};Write-Host "CPF_CHECKPOINT_APPLY=PASS ZIP_SHA256=$actual"}finally{Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue};python cpf-tools\verification\tools\cpf-source-state.py --root $root --scope source;git status --short
```

## 2. 승인 Delete Manifest 적용 — PowerShell 한 줄

```powershell
$ErrorActionPreference='Stop';$root=(git rev-parse --show-toplevel).Trim();$mf=Join-Path $root 'cpf-docs\work\current\DELETE_MANIFEST.csv';$protected=@('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/');$deleted=0;$missing=0;Import-Csv -LiteralPath $mf -Encoding UTF8|Where-Object{$_.approved-eq'true'-and$_.user_approved-eq'true'-and$_.precondition-eq'SATISFIED'-and$_.lifecycle-eq'PENDING_USER_EXECUTION'-and$_.user_execution_required-eq'true'}|ForEach-Object{$rel=$_.path.Replace('\','/').TrimStart('/');if([IO.Path]::IsPathRooted($_.path)-or$rel.Contains('../')-or($protected|Where-Object{$rel.StartsWith($_)})){throw "UNSAFE DELETE: $rel"};$p=[IO.Path]::GetFullPath((Join-Path $root $rel));if(!$p.StartsWith($root+[IO.Path]::DirectorySeparatorChar)){throw "PATH ESCAPE: $rel"};if(!(Test-Path -LiteralPath $p)){$missing++;return};if($_.replacement_path){$rp=Join-Path $root ($_.replacement_path-replace'/','\');if(!(Test-Path -LiteralPath $rp)){throw "REPLACEMENT MISSING: $($_.replacement_path)"}};if(Test-Path -LiteralPath $p -PathType Container){throw "DIRECTORY DELETE REJECTED: $rel"};Remove-Item -LiteralPath $p -Force;$deleted++};Write-Host "CPF_DELETE_MANIFEST=PASS DELETED=$deleted ALREADY_MISSING=$missing";git status --short
```

## 3. 저비용 검증 — PowerShell 한 줄

```powershell
$ErrorActionPreference='Stop';$root=(git rev-parse --show-toplevel).Trim();python cpf-tools\verification\tools\cpf-source-state.py --root $root --scope source;python cpf-tools\verification\tools\verify-cpf-current-final.py;python cpf-tools\verification\nxt3\verify_nxt3_hygiene.py;python cpf-tools\verification\nxt3\verify_nxt3_repository_garbage.py;python cpf-tools\verification\tools\verify-cpf-runtime-utf8-boundaries.py --root $root;python cpf-tools\verification\tools\verify-cpf-physical-db-consolidation.py --root $root;python cpf-tools\verification\tools\verify-cpf-unified-cli.py --root $root;python -m pytest -q cpf-tools\release\open-git\tests\test_cpf_open_git.py;Write-Host 'CPF_CHECKPOINT_LOW_COST_VERIFY=PASS';git status --short
```

## 4. 최대강도 Local Full Runtime + Fresh Replay — PowerShell 한 줄

```powershell
$ErrorActionPreference='Continue';$root=(git rev-parse --show-toplevel).Trim();$out=Join-Path $HOME 'Downloads';$vscode=Get-ChildItem -LiteralPath $out -Filter 'CPF_VSCODE_PROBLEMS_*.json' -File -ErrorAction SilentlyContinue|Sort-Object LastWriteTime -Descending|Select-Object -First 1;if(-not$vscode){throw 'Fresh Java25/Gradle VS Code Problems JSON이 없습니다. Error=0/Warning=0 fresh import 결과를 먼저 export하세요.'};$launch=Join-Path $out ('CPF_FULL_RUNTIME_LAUNCH_'+(Get-Date -Format 'yyyyMMdd_HHmmss')+'.log');$started=Get-Date;& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root 'cpf-tools\verification\tools\run-cpf-required-full-runtime-validation.ps1') -RepoRoot $root -DockerRoot 'C:\dev\Docker' -OutputRoot $out -VsCodeProblemsJson $vscode.FullName 2>&1|Tee-Object -FilePath $launch;$rc=$LASTEXITCODE;$ended=Get-Date;$runtimeLog=Get-ChildItem -LiteralPath $out -Filter 'CPF_REQUIRED_FULL_RUNTIME_*.log' -File -ErrorAction SilentlyContinue|Sort-Object LastWriteTime -Descending|Select-Object -First 1;$resultZip=Get-ChildItem -LiteralPath $out -Filter 'CPF_LOCAL_VALIDATION_*.zip' -File -ErrorAction SilentlyContinue|Sort-Object LastWriteTime -Descending|Select-Object -First 1;Write-Host ('CPF_FULL_RUNTIME_FINAL='+$(if($rc-eq0){'PASS'}else{'FAIL'})+' EXIT_CODE='+$rc);Write-Host ('START='+$started.ToString('o')+' END='+$ended.ToString('o'));Write-Host ('SOURCE='+((& python cpf-tools\verification\tools\cpf-source-state.py --root $root --scope source) -join''));Write-Host ('VSCODE_PROBLEMS='+$vscode.FullName);Write-Host ('LAUNCH_LOG='+$launch);Write-Host ('RUNTIME_LOG='+$(if($runtimeLog){$runtimeLog.FullName}else{'NOT_CREATED'}));Write-Host ('RESULT_ZIP='+$(if($resultZip){$resultZip.FullName}else{'NOT_CREATED'}));git status --short
```

성공조건: `FAIL=0 / mandatory SKIP_ENV=0 / mandatory NOT_EXECUTED=0 / unresolved UNKNOWN=0 / VSCode Error=0 Warning=0 / mojibake=0 / legacy active DB=0 / Source drift=0 / Managed drift=0 / Fresh Replay PASS`.

## 5. Git 상태 — read-only

```powershell
git rev-parse --show-toplevel; git branch --show-current; git rev-parse HEAD; git status --short
```

## 6. Git 반영 주의

- **Checkpoint 단계에서는 Commit/Push 명령을 최종 반영 명령으로 사용하지 않는다.**
- `cpf-release/`는 Private CPF master Commit/Push 대상이 아니다.
- Open Git Gate 전부 PASS 후 사용자가 Open Git repository에서만 직접 Commit/Push한다.
- Private master에는 `cpf-release/` 결과가 아니라 Release Generator/CLI/Policy/Test/정본 Source만 최종 검토 후 반영한다.
