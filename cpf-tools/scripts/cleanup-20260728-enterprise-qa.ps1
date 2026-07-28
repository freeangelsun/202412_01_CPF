param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path,[switch]$WhatIf)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
# Only deterministic stale artifacts superseded by canonical query contracts are removed here.
$targets=@(
 'cpf-tools\db\vendor\mariadb\runtime\bza\repository\auth-revoke-refresh-by-login-operation.sql',
 'cpf-tools\db\vendor\postgresql\runtime\bza\repository\auth-revoke-refresh-by-login-operation.sql',
 'cpf-tools\db\vendor\oracle\runtime\bza\repository\auth-revoke-refresh-by-login-operation.sql'
)
foreach($rel in $targets){$p=Join-Path $Root $rel;if(Test-Path -LiteralPath $p -PathType Leaf){if($WhatIf){Write-Host "[WHATIF] remove $rel"}else{Remove-Item -LiteralPath $p -Force;Write-Host "[REMOVED] $rel"}}}
# Report, but never automatically delete, general build/log/tmp garbage. Codex decides after checking ownership/evidence.
$patterns=@('*.tmp','*.bak','*.orig','*.rej','*.patch')
foreach($pat in $patterns){Get-ChildItem -LiteralPath $Root -Recurse -File -Filter $pat -ErrorAction SilentlyContinue | Where-Object {$_.FullName -notmatch '[\\/]\.git[\\/]'} | ForEach-Object {Write-Host "[GARBAGE-CANDIDATE] $($_.FullName.Substring($Root.Length+1))"}}
