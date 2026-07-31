[CmdletBinding()]
param([string]$Root=(Get-Location).Path,[Parameter(Mandatory)][string]$EvidenceDir,[Parameter(Mandatory)][string]$IndependentReview,[string]$Output)
Set-StrictMode -Version Latest;$ErrorActionPreference='Stop';$rootPath=(Resolve-Path $Root).Path;$evidencePath=(Resolve-Path $EvidenceDir).Path
if(-not$Output){$Output=Join-Path $evidencePath 'CPF_QA34_FINAL_EVIDENCE_INDEX.sanitized.json'}
& python (Join-Path $rootPath 'cpf-tools/scripts/verify-cpf-qa34-evidence-closure.py') --root $rootPath --evidence-root $evidencePath --qa33-matrix (Join-Path $evidencePath 'CPF_20260731_QA33_RESULT_MATRIX_EXACT_SHA.csv') --independent-review $IndependentReview --output $Output
if($LASTEXITCODE-ne0){throw'QA34 final evidence closure failed'}
$hash=(Get-FileHash $Output -Algorithm SHA256).Hash.ToLowerInvariant();[IO.File]::WriteAllText("$Output.sha256","$hash  $([IO.Path]::GetFileName($Output))`n",[Text.UTF8Encoding]::new($false));Write-Host"[CPF][QA34][PASS] FINAL evidence closure=$Output"
