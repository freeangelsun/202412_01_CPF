param([Parameter(Mandatory=$true)][string]$ZipPath,[string]$ExpectedSha256='')
$ErrorActionPreference='Stop';$PSNativeCommandUseErrorActionPreference=$true
$root=(git rev-parse --show-toplevel).Trim();$zip=(Resolve-Path -LiteralPath $ZipPath).Path
$actual=(Get-FileHash -LiteralPath $zip -Algorithm SHA256).Hash.ToUpperInvariant();$verified='INTERNAL_ONLY'
if(-not[string]::IsNullOrWhiteSpace($ExpectedSha256)){$expected=$ExpectedSha256.ToUpperInvariant();if($actual-ne$expected){throw "ZIP SHA256 MISMATCH expected=$expected actual=$actual"};$verified='EXPECTED_SHA256'}elseif(Test-Path -LiteralPath "$zip.sha256.txt"){$expected=((Get-Content -LiteralPath "$zip.sha256.txt" -Raw -Encoding UTF8)-split '\s+')[0].ToUpperInvariant();if($actual-ne$expected){throw "ZIP SHA256 MISMATCH expected=$expected actual=$actual"};$verified='SIDECAR'}
$tmp=Join-Path $env:TEMP ('cpf-dev-harness-'+[guid]::NewGuid().ToString('N'));New-Item -ItemType Directory -Path $tmp -Force|Out-Null
try{
 Expand-Archive -LiteralPath $zip -DestinationPath $tmp -Force
 $payload=Join-Path $tmp 'cpf-docs\governance\development-harness';if(!(Test-Path -LiteralPath $payload -PathType Container)){throw 'DEVELOPMENT HARNESS PAYLOAD MISSING'}
 $sum=Join-Path $tmp 'cpf-docs\deliverables\development-harness\SHA256SUMS.txt';if(!(Test-Path -LiteralPath $sum)){throw 'HARNESS SHA256SUMS MISSING'}
 Get-Content -LiteralPath $sum -Encoding UTF8|Where-Object{$_ -and -not $_.StartsWith('#')}|ForEach-Object{$parts=$_ -split '\s+',2;if($parts.Count-ne2){throw "BAD SHA256SUMS LINE: $_"};$p=Join-Path $tmp $parts[1];if(!(Test-Path -LiteralPath $p -PathType Leaf)){throw "PACKAGE FILE MISSING: $($parts[1])"};$h=(Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash.ToLowerInvariant();if($h-ne$parts[0].ToLowerInvariant()){throw "PACKAGE FILE SHA256 MISMATCH: $($parts[1])"}}
 Get-ChildItem -LiteralPath $tmp -Recurse -File|ForEach-Object{$rel=$_.FullName.Substring($tmp.Length).TrimStart('\','/');if($rel){$dst=Join-Path $root $rel;$parent=Split-Path -Parent $dst;if(!(Test-Path -LiteralPath $parent)){New-Item -ItemType Directory -Path $parent -Force|Out-Null};Copy-Item -LiteralPath $_.FullName -Destination $dst -Force}}
 Set-Location $root;python .\cpf-docs\governance\development-harness\validators\run_all_gates.py;if($LASTEXITCODE-ne0){throw "HARNESS FINAL GATE FAILED RC=$LASTEXITCODE"}
 Write-Host "CPF_DEVELOPMENT_HARNESS_APPLY=PASS ZIP_SHA256=$actual ZIP_VERIFY=$verified DELETE_EXECUTED=0";git status --short
}finally{if(Test-Path -LiteralPath $tmp){Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue}}
