param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
function Run([string]$Name,[scriptblock]$Block){Write-Host "== $Name ==";& $Block;if(-not $?){throw "$Name failed"}}
Run 'Targeted stale artifact cleanup' { & (Join-Path $Root 'cpf-tools\scripts\cleanup-20260728-enterprise-qa.ps1') -Root $Root }
# CMN cache mapper is a new canonical runtime template and must be rendered before full DB artifact parity checks.
Run 'CMN cache runtime mapper canonical sync' { & (Join-Path $Root 'cpf-tools\scripts\sync-cmn-cache-runtime-pack.ps1') -Root $Root }
# Existing CPF canonical pipeline regenerates install/vendor source/lifecycle packs/runtime query packs,
# migration checksums, schema manifest and generated-domain DB artifacts from canonical sources.
Run 'Canonical DB / official vendor / generated-domain synchronization' { & (Join-Path $Root 'cpf-tools\scripts\sync-database-artifacts.ps1') -Root $Root -ApplyGeneratedDomains }
Run 'CMN cache runtime mapper drift check' { & (Join-Path $Root 'cpf-tools\scripts\sync-cmn-cache-runtime-pack.ps1') -Root $Root -Check }
Run 'Enterprise QA closing static gate' { & (Join-Path $Root 'cpf-tools\scripts\check-enterprise-qa-closing.ps1') -Root $Root }
Run 'Migration checksum integrity' { & (Join-Path $Root 'cpf-tools\scripts\check-migration-checksums.ps1') -Root $Root }
Run 'Official DB vendor parity' { & (Join-Path $Root 'cpf-tools\scripts\check-db-vendor-pack-parity.ps1') -Root $Root }
Run 'Runtime query pack integrity' { & (Join-Path $Root 'cpf-tools\scripts\check-platform-runtime-query-packs.ps1') -Root $Root }
Write-Host 'CPF 20260728 enterprise QA overlay post-apply gates PASS'
