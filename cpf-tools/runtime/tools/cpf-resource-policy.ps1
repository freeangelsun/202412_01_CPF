# Full Runtime child-process UTF-8 contract. Keep the emitted byte stream UTF-8 even when pwsh is redirected.
$CpfUtf8ConsoleEncoding = [Text.UTF8Encoding]::new($false)
try {
    [Console]::InputEncoding = $CpfUtf8ConsoleEncoding
    [Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
    $OutputEncoding = $CpfUtf8ConsoleEncoding
    $global:OutputEncoding = $CpfUtf8ConsoleEncoding
} catch { }
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'

Set-StrictMode -Version Latest

function Import-CpfPropertyFile {
    param([Parameter(Mandatory=$true)][string] $Path)
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "CPF resource property file missing: $Path"
    }
    $values = @{}
    foreach ($raw in Get-Content -LiteralPath $Path -Encoding UTF8) {
        $line = $raw.Trim()
        if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith('#')) { continue }
        $index = $line.IndexOf('=')
        if ($index -lt 1) { throw "Invalid CPF resource property line: $Path :: $raw" }
        $key = $line.Substring(0,$index).Trim()
        $value = $line.Substring($index+1).Trim()
        if ($values.ContainsKey($key)) { throw "Duplicate CPF resource property: $Path :: $key" }
        $values[$key] = $value
    }
    return $values
}

function ConvertTo-CpfMemoryMb {
    param([Parameter(Mandatory=$true)][string] $Value)
    $normalized = $Value.Trim().ToLowerInvariant()
    if ($normalized -notmatch '^([0-9]+)(m|g)$') {
        throw "Invalid CPF memory value '$Value'. Use <number>m or <number>g."
    }
    $amount = [int]$Matches[1]
    if ($Matches[2] -eq 'g') { return $amount * 1024 }
    return $amount
}

function Resolve-CpfResourcePolicy {
    param(
        [Parameter(Mandatory=$true)][string] $RepoRoot,
        [ValidateSet('local','dev','test','stg','prod')][string] $Profile = 'local',
        [string] $ModuleDir,
        [hashtable] $Explicit = @{}
    )
    $policyRoot = Join-Path $RepoRoot 'gradle\cpf-runtime'
    $common = Import-CpfPropertyFile (Join-Path $policyRoot 'common.properties')
    $environment = Import-CpfPropertyFile (Join-Path $policyRoot "$Profile.properties")
    $resolved = @{}
    foreach ($entry in $common.GetEnumerator()) { $resolved[$entry.Key] = [string]$entry.Value }
    foreach ($entry in $environment.GetEnumerator()) { $resolved[$entry.Key] = [string]$entry.Value }
    foreach ($entry in $Explicit.GetEnumerator()) {
        if ($null -ne $entry.Value -and -not [string]::IsNullOrWhiteSpace([string]$entry.Value)) {
            $resolved[$entry.Key] = [string]$entry.Value
        }
    }
    if (-not [string]::IsNullOrWhiteSpace($ModuleDir)) {
        $moduleFile = Join-Path $ModuleDir 'cpf-resource.properties'
        if (Test-Path -LiteralPath $moduleFile -PathType Leaf) {
            $module = Import-CpfPropertyFile $moduleFile
            foreach ($entry in $module.GetEnumerator()) { $resolved[$entry.Key] = [string]$entry.Value }
        }
    }

    if ((($resolved['runtime.memory.enforceCeiling'] ?? 'true').ToLowerInvariant() -eq 'true')) {
        $step = [int]($resolved['heap.step.mb'] ?? '250')
        $ceiling = [int]($resolved['runtime.memory.ceiling.mb'] ?? '1000')
        foreach ($key in @('gradle.jvm.xms','gradle.jvm.xmx','test.xms','test.xmx','runtime.web.xms','runtime.web.xmx','runtime.batch.xms','runtime.batch.xmx')) {
            if (-not $resolved.ContainsKey($key)) { continue }
            $mb = ConvertTo-CpfMemoryMb ([string]$resolved[$key])
            if ($mb -lt $step -or $mb -gt $ceiling -or ($mb % $step) -ne 0) {
                throw "CPF resource '$key=$($resolved[$key])' must use ${step}MB increments and stay between ${step}MB and ${ceiling}MB."
            }
        }
        foreach ($pair in @(@('runtime.web.xms','runtime.web.xmx'), @('runtime.batch.xms','runtime.batch.xmx'), @('test.xms','test.xmx'), @('gradle.jvm.xms','gradle.jvm.xmx'))) {
            if ($resolved.ContainsKey($pair[0]) -and $resolved.ContainsKey($pair[1]) -and (ConvertTo-CpfMemoryMb ([string]$resolved[$pair[0]])) -gt (ConvertTo-CpfMemoryMb ([string]$resolved[$pair[1]]))) {
                throw "CPF resource $($pair[0]) must be <= $($pair[1])."
            }
        }
    }

    [pscustomobject]@{
        Profile = $Profile
        Values = $resolved
        PolicyRoot = $policyRoot
        ModuleFile = if ($ModuleDir) { Join-Path $ModuleDir 'cpf-resource.properties' } else { $null }
    }
}
