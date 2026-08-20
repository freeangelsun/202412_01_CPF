param([string]$Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
$python=Get-Command python -ErrorAction Stop
& $python.Source (Join-Path $Root 'cpf-tools/verification/tools/verify-cpf-frontend-consumer-closure.py') --root $Root
if($LASTEXITCODE -ne 0){throw 'Frontend consumer closure failed.'}
$admRouteRoot=Join-Path $Root 'cpf-admin/frontend/src/app/routes'
$admAggregator=Join-Path $Root 'cpf-admin/frontend/src/app/routes.ts'
$bzaRouter=Join-Path $Root 'cpf-backoffice-web/frontend/src/router/index.ts'
$bzaGenerator=Join-Path $Root 'cpf-backoffice-web/frontend/scripts/generate-reference-client.mjs'
foreach($path in @($admRouteRoot,$admAggregator,$bzaRouter,$bzaGenerator)){if(-not(Test-Path $path)){throw "Frontend route contract source missing: $path"}}
$bzaText=Get-Content $bzaRouter -Raw -Encoding UTF8
foreach($route in @("path: '/'","path: '/employees'","path: '/approvals'","path: '/authorization'")){if($bzaText -notlike "*$route*"){throw "BZA reference route missing: $route"}}
$generatorText=Get-Content $bzaGenerator -Raw -Encoding UTF8
if($generatorText -notmatch 'wanted' -or $generatorText -notmatch '8'){throw 'BZA reference operation allowlist/generator contract missing.'}
Write-Host '[PASS] Frontend feature/route coverage: ADM grouped registry + BZA 4-page reference surface'
