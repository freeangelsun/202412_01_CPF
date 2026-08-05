param([switch]$RequireFullAssignment)
$ErrorActionPreference = "Stop"
$argsList = @("cpf-tools/scripts/devgpt-control-v9/validate_devgpt_control_v9.py", "--repo-root", ".")
if ($RequireFullAssignment) { $argsList += "--require-full-assignment" }
python @argsList
