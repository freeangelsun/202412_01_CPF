param(
  [string]$RepoRoot=(Get-Location).Path,
  [string]$EvidenceDir="cpf-docs/work/v9i/fdr/r1/evidence/runtime/FDEV-017"
)
$ErrorActionPreference='Stop'
$Frontend=Join-Path $RepoRoot 'cpf-admin/frontend'
$EvidencePath=Join-Path $RepoRoot $EvidenceDir
Set-Location $Frontend
New-Item -ItemType Directory -Force $EvidencePath | Out-Null
if(-not (Test-Path package-lock.json)){throw 'package-lock.json is required for reproducible npm ci.'}
$Package=Get-Content package.json -Raw | ConvertFrom-Json
if(-not $Package.scripts.'test:e2e'){throw 'package.json scripts.test:e2e is required.'}
node --version | Set-Content (Join-Path $EvidencePath 'node-version.txt')
npm --version | Set-Content (Join-Path $EvidencePath 'npm-version.txt')
npm ci --no-audit --no-fund 2>&1 | Tee-Object (Join-Path $EvidencePath 'npm-ci.log')
if($LASTEXITCODE-ne 0){throw 'npm ci failed'}
npx playwright install chromium firefox webkit 2>&1 | Tee-Object (Join-Path $EvidencePath 'playwright-install.log')
if($LASTEXITCODE-ne 0){throw 'Playwright browser install failed'}
npm run test:e2e -- --project=chromium --project=firefox --project=webkit 2>&1 | Tee-Object (Join-Path $EvidencePath 'playwright.log')
if($LASTEXITCODE-ne 0){throw 'Browser matrix failed'}
