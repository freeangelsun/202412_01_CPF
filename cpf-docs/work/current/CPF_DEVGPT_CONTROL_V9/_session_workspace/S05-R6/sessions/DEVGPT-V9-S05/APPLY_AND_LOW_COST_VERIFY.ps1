param([Parameter(Mandatory=$true)][string]$ZipPath,[Parameter(Mandatory=$true)][string]$RepoPath)
$ErrorActionPreference='Stop'
$actual=(git -C $RepoPath rev-parse HEAD).Trim()
if($actual -ne 'af12a0c8851a2e8d20e9e42964d8dacc0266af03'){throw "Target SHA mismatch: $actual"}
Expand-Archive -LiteralPath $ZipPath -DestinationPath $RepoPath -Force
python (Join-Path $RepoPath 'cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05/tools/verify_s05_overlay.py') --repo $RepoPath
