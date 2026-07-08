param([string] $Env = "dev", [switch] $DryRun, [switch] $BuildBeforeDeploy, [string] $ResultDir)
& "$PSScriptRoot/deploy-module.ps1" -Module BAT -Env $Env -DryRun:$DryRun -BuildBeforeDeploy:$BuildBeforeDeploy -ResultDir $ResultDir
