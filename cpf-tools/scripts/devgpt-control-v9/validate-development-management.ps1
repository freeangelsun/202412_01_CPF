param([switch]$RequireFullAssignment)
$ErrorActionPreference = "Stop"
$argsList = @("cpf-tools/scripts/devgpt-control-v9/validate_development_management.py", "--repo-root", ".")
if ($RequireFullAssignment) { $argsList += "--require-full-assignment" }
python @argsList
