param([string]$Root='.',[string]$EvidenceDir='cpf-docs/evidence/current/runtime',[switch]$SkipExternalTools)
$ErrorActionPreference='Stop'; Set-Location $Root; New-Item -ItemType Directory -Force $EvidenceDir | Out-Null
$start=(Get-Date).ToUniversalTime().ToString('o'); $sha=(git rev-parse HEAD).Trim(); if($LASTEXITCODE -ne 0){throw 'git SHA resolution failed'}
$results=@(); function Run([string]$Name,[scriptblock]$Command){$s=Get-Date;&$Command;$code=$LASTEXITCODE;$results+=@{name=$Name;exitCode=$code;startedAt=$s.ToUniversalTime().ToString('o');finishedAt=(Get-Date).ToUniversalTime().ToString('o')};if($code -ne 0){throw "$Name failed ($code)"}}
if((java -version 2>&1 | Out-String) -notmatch 'version "25'){throw 'Java 25 is required'}
Run 'gradle-full' { .\gradlew.bat clean test verifyQa32ReleaseReadiness --no-daemon --stacktrace }
Run 'adm-frontend' { Push-Location cpf-admin/frontend; npm ci; npm run typecheck; npm test; npm run build; npx playwright test; Pop-Location }
Run 'bza-frontend' { Push-Location cpf-biz-admin/frontend; npm ci; npm run typecheck; npm test; npm run build; npx playwright test; Pop-Location }
if(-not $SkipExternalTools){Run 'supply-chain' { pwsh -NoProfile -File cpf-tools/scripts/generate-cpf-supply-chain-evidence.ps1 -Root . }}
# DB/Kafka integration is implemented as Testcontainers/real-profile tests and must be selected by the final environment.
Run 'qa32-integration' { .\gradlew.bat qa32IntegrationTest -Pqa32Vendors=oracle,postgresql,mariadb -Pqa32Kafka=true --no-daemon --stacktrace }
@{schemaVersion=1;sourceSha=$sha;startedAt=$start;finishedAt=(Get-Date).ToUniversalTime().ToString('o');results=$results}|ConvertTo-Json -Depth 8|Set-Content -Encoding UTF8 "$EvidenceDir/qa32-runtime-evidence.json"
