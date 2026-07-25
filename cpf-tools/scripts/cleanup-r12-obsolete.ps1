param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path,[switch]$WhatIf)
$ErrorActionPreference='Stop';Set-StrictMode -Version Latest
$obsolete=Join-Path $Root 'cpf-tools/db/vendor/mariadb/migration/flyway/V6__bizadm_exs_transaction_identity.sql'
$replacement=Join-Path $Root 'cpf-tools/db/vendor/mariadb/migration/flyway/V6__transaction_server_identity.sql'
if(-not(Test-Path $replacement)){throw "replacement migration missing: $replacement"}
if(-not(Test-Path $obsolete)){Write-Host '[PASS] obsolete V6 already absent';exit 0}
$content=Get-Content -LiteralPath $obsolete -Raw
if($content-notmatch '(?i)bizadm|exs'){throw 'Safety stop: obsolete V6 content does not match expected legacy artifact.'}
if($WhatIf){Write-Host "[WHATIF] remove $obsolete";exit 0}
Remove-Item -LiteralPath $obsolete -Force
if(Test-Path $obsolete){throw "failed to remove $obsolete"}
Write-Host "[REMOVED] $obsolete"
