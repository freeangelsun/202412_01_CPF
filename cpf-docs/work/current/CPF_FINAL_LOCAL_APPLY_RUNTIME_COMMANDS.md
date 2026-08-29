# CPF Final Local Apply / Delete / Runtime Commands — Current

> 아래 명령은 PowerShell 7.x, CPF 프로젝트 Root에서 실행한다. Full Runtime PASS 전에는 Commit/Push하지 않는다.

## 1. Final Overlay 적용 — 한 줄

```powershell
$ErrorActionPreference='Stop';$root=(git rev-parse --show-toplevel).Trim();$zip=Get-ChildItem "$HOME\Downloads" -File -Filter 'CPF_C_DEV_QA_2_2_SOURCE_CLOSURE_OVERLAY_*.zip'|Sort-Object LastWriteTime -Descending|Select-Object -First 1;if(!$zip){throw 'OVERLAY ZIP NOT FOUND'};$side="$($zip.FullName).sha256.txt";if(!(Test-Path -LiteralPath $side)){throw "SHA SIDECAR NOT FOUND: $side"};$expected=((Get-Content -LiteralPath $side -Raw -Encoding UTF8)-split'\s+')[0].ToUpperInvariant();$actual=(Get-FileHash -LiteralPath $zip.FullName -Algorithm SHA256).Hash.ToUpperInvariant();if($actual-ne$expected){throw "ZIP SHA256 MISMATCH expected=$expected actual=$actual"};$tmp=Join-Path $env:TEMP ('cpf-final-overlay-'+[guid]::NewGuid().ToString('N'));New-Item -ItemType Directory -Path $tmp -Force|Out-Null;try{Expand-Archive -LiteralPath $zip.FullName -DestinationPath $tmp -Force;Get-ChildItem $tmp -Recurse -File|ForEach-Object{$rel=$_.FullName.Substring($tmp.Length).TrimStart('\','/');$dst=Join-Path $root $rel;$parent=Split-Path -Parent $dst;if(!(Test-Path $parent)){New-Item -ItemType Directory -Path $parent -Force|Out-Null};Copy-Item $_.FullName $dst -Force}}finally{Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue};python cpf-tools\verification\tools\cpf-source-state.py --root $root --scope source;git status --short
```

## 2. 승인 Delete Manifest 적용 — 한 줄

```powershell
$ErrorActionPreference='Stop';$root=(git rev-parse --show-toplevel).Trim();$mf=Join-Path $root 'cpf-docs\work\current\DELETE_MANIFEST.csv';$protected=@('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/');$deleted=0;$missing=0;Import-Csv $mf -Encoding UTF8|Where-Object{$_.approved-eq'true'-and$_.user_approved-eq'true'-and$_.precondition-eq'SATISFIED'-and$_.lifecycle-eq'PENDING_USER_EXECUTION'}|ForEach-Object{$rel=$_.path.Replace('\','/').TrimStart('/');if([IO.Path]::IsPathRooted($_.path)-or$rel.Contains('../')-or($protected|Where-Object{$rel.StartsWith($_)})){throw "UNSAFE DELETE: $rel"};$p=[IO.Path]::GetFullPath((Join-Path $root $rel));if(!(Test-Path $p)){$missing++;return};if($_.replacement_path){$rp=Join-Path $root ($_.replacement_path-replace'/','\');if(!(Test-Path $rp)){throw "REPLACEMENT MISSING: $($_.replacement_path)"}};if(Test-Path $p -PathType Container){throw "DIRECTORY DELETE REJECTED: $rel"};Remove-Item $p -Force;$deleted++};Write-Host "CPF_DELETE_MANIFEST=PASS DELETED=$deleted ALREADY_MISSING=$missing";git status --short
```

## 3. 저비용 검증 — 한 줄

```powershell
$ErrorActionPreference='Stop';$root=(git rev-parse --show-toplevel).Trim();python cpf-tools\verification\tools\cpf-source-state.py --root $root --scope source;python cpf-tools\verification\tools\verify-cpf-current-final.py;python cpf-tools\verification\nxt3\verify_nxt3_hygiene.py;python cpf-tools\verification\nxt3\verify_nxt3_repository_garbage.py;python cpf-tools\verification\tools\verify-cpf-runtime-utf8-boundaries.py --root $root;python cpf-tools\verification\tools\verify-cpf-physical-db-consolidation.py --root $root;python cpf-tools\verification\tools\verify-cpf-unified-cli.py --root $root;python cpf-tools\verification\verify_requirement_progress.py --root $root;python cpf-tools\verification\tools\verify-cpf-requirement-projection-consistency.py --root $root;python -m pytest -q cpf-tools\release\open-git\tests cpf-tools\release\public\tests;Write-Host 'CPF_FINAL_LOW_COST_VERIFY=PASS';git status --short
```

## 4. 최대강도 Java25 Local Full Runtime + Fresh Replay — 한 줄

```powershell
$ErrorActionPreference='Continue';$root=(git rev-parse --show-toplevel).Trim();$out=Join-Path $HOME 'Downloads';$vscode=Get-ChildItem $out -Filter 'CPF_VSCODE_PROBLEMS_*.json' -File|Sort-Object LastWriteTime -Descending|Select-Object -First 1;if(-not$vscode){throw 'Fresh Java25/Gradle VS Code Problems JSON 필요: Error=0 Warning=0 결과를 export하세요.'};$launch=Join-Path $out ('CPF_FULL_RUNTIME_LAUNCH_'+(Get-Date -Format 'yyyyMMdd_HHmmss')+'.log');$started=Get-Date;& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root 'cpf-tools\verification\tools\run-cpf-required-full-runtime-validation.ps1') -RepoRoot $root -DockerRoot 'C:\dev\Docker' -OutputRoot $out -VsCodeProblemsJson $vscode.FullName 2>&1|Tee-Object -FilePath $launch;$rc=$LASTEXITCODE;$ended=Get-Date;Write-Host ('CPF_FULL_RUNTIME_FINAL='+$(if($rc-eq0){'PASS'}else{'FAIL'})+' EXIT_CODE='+$rc);Write-Host ('START='+$started.ToString('o')+' END='+$ended.ToString('o'));Write-Host ('LAUNCH_LOG='+$launch);git status --short
```

성공조건: `FAIL=0 / mandatory SKIP_ENV=0 / mandatory NOT_EXECUTED=0 / unresolved UNKNOWN=0 / VSCode Error=0 Warning=0 / mojibake=0 / legacy active DB=0 / Source drift=0 / Managed drift=0 / Fresh Replay PASS`.

## 5. Git 상태 — read-only

```powershell
git rev-parse --show-toplevel; git branch --show-current; git rev-parse HEAD; git status --short
```

## 6. CPF development master Commit + Push — 위 Full Runtime PASS 확인 후 사용자 직접 실행

```powershell
$root=(git rev-parse --show-toplevel).Trim();Set-Location $root;if(Test-Path 'cpf-release'){Write-Host 'cpf-release is Open-Git staging and will not be added to CPF master'};$ts=Get-Date -Format yyyyMMdd_HHmmss;git add -A -- . ':!cpf-release';git status --short;git commit -m "CPF $ts";git push origin master;Write-Host "PUSHED_SHA=$((git rev-parse HEAD).Trim())";git status --short
```

## 7. Open Git Commit + Push — `cpf-release/` 검증 완료 후 Open Git Repository에서 사용자 직접 실행

```powershell
$root=(git rev-parse --show-toplevel).Trim();$ts=Get-Date -Format yyyyMMdd_HHmmss;git status --short;git add -A;git commit -m "CPF $ts";git push;Write-Host "OPEN_GIT_PUSHED_SHA=$((git rev-parse HEAD).Trim())";git status --short
```
