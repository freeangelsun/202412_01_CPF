param([string]$Root='.',[string]$OutputDir='cpf-docs/evidence/current/supply-chain')
$ErrorActionPreference='Stop'; Set-Location $Root; New-Item -ItemType Directory -Force $OutputDir | Out-Null
function Invoke-Checked([string]$Name,[scriptblock]$Action){ & $Action; if($LASTEXITCODE -ne 0){throw "$Name failed (exit=$LASTEXITCODE)"} }
Invoke-Checked 'CycloneDX' { .\gradlew.bat cyclonedxBom --no-daemon --stacktrace }
if(-not(Get-Command ort -ErrorAction SilentlyContinue)){throw 'ORT executable is required'}
Invoke-Checked 'ORT' { ort analyze -i . -o "$OutputDir/ort" }
if(-not(Get-Command syft -ErrorAction SilentlyContinue)){throw 'Syft executable is required'}
Invoke-Checked 'Syft' { syft dir:. -o cyclonedx-json="$OutputDir/syft-final-artifact.cdx.json" }
if(-not(Get-Command grype -ErrorAction SilentlyContinue)){throw 'Grype executable is required'}
Invoke-Checked 'Grype' { grype "sbom:$OutputDir/syft-final-artifact.cdx.json" -o json --file "$OutputDir/grype.json" --fail-on high }
Invoke-Checked 'License policy' { python cpf-tools/scripts/verify-cpf-supply-chain.py --root . --sbom build/reports/cyclonedx/bom.json --release }
