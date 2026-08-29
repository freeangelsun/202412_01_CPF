# CPF Development Harness — 3 Commands

프로젝트 Root의 PowerShell 7에서 아래 3개만 순서대로 사용한다. 적용은 파일을 덮어쓰지만 삭제/Git write는 하지 않는다.

## 1. Harness 적용 + Self Gate
```powershell
$ErrorActionPreference='Stop';$root=(git rev-parse --show-toplevel).Trim();$zip=Get-ChildItem @((Join-Path $HOME 'Downloads'),'D:\11096\Downloads') -File -Filter 'CPF_DEVELOPMENT_HARNESS_FINAL_*.zip' -ErrorAction SilentlyContinue|Sort-Object LastWriteTime -Descending|Select-Object -First 1;if(!$zip){throw 'HARNESS ZIP NOT FOUND'};$tmp=Join-Path $env:TEMP ('cpf-harness-'+[guid]::NewGuid().ToString('N'));New-Item -ItemType Directory $tmp -Force|Out-Null;try{Expand-Archive -LiteralPath $zip.FullName -DestinationPath $tmp -Force;& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $tmp 'cpf-docs\deliverables\development-harness\APPLY_CURRENT_HARNESS.ps1') -ZipPath $zip.FullName;if($LASTEXITCODE-ne0){throw "HARNESS APPLY FAILED RC=$LASTEXITCODE"}}finally{Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue}
```

## 2. Legacy 정본 삭제
```powershell
$ErrorActionPreference='Stop';$root=(git rev-parse --show-toplevel).Trim();Set-Location $root;& pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-docs\governance\development-harness\DELETE_LEGACY_CANONICAL.ps1 -ApplyApprovedManifest;if($LASTEXITCODE-ne0){throw "HARNESS LEGACY DELETE FAILED RC=$LASTEXITCODE"}
```

## 3. Harness 최종 검증
```powershell
$ErrorActionPreference='Stop';$PSNativeCommandUseErrorActionPreference=$true;$root=(git rev-parse --show-toplevel).Trim();Set-Location $root;python .\cpf-docs\governance\development-harness\validators\run_all_gates.py;if($LASTEXITCODE-ne0){throw "HARNESS FINAL GATE FAILED RC=$LASTEXITCODE"};Write-Host 'CPF_DEVELOPMENT_HARNESS_FINAL_GATE=PASS';git status --short
```

Product 개발의 최고강도 Build/Runtime은 Harness의 `bin/full-verify.ps1` 및 Current Test Ledger가 지시하는 실제 환경 Gate를 따른다.
