param(
    [string] $Baseline = "",
    [string] $Candidate = "",
    [switch] $SelfTest
)

$ErrorActionPreference = "Stop"

function Test-CpfContractCompatibility {
    param(
        [Parameter(Mandatory=$true)] $Old,
        [Parameter(Mandatory=$true)] $New
    )
    $breaking = New-Object System.Collections.Generic.List[string]
    if ([string]$Old.contractId -ne [string]$New.contractId) {
        $breaking.Add("contractId changed: $($Old.contractId) -> $($New.contractId)")
    }
    if ([string]$Old.kind -ne [string]$New.kind) {
        $breaking.Add("contract kind changed: $($Old.kind) -> $($New.kind)")
    }

    $supportedKinds = @("REST", "SHARED_API", "EVENT", "FIXED_LENGTH", "FILE", "BATCH")
    if ([string]$Old.kind -notin $supportedKinds -or [string]$New.kind -notin $supportedKinds) {
        $breaking.Add("unsupported contract kind")
    }

    $oldFields = @{}
    foreach ($field in @($Old.fields)) {
        $name = [string]$field.name
        if ([string]::IsNullOrWhiteSpace($name) -or $oldFields.ContainsKey($name)) {
            $breaking.Add("baseline contains blank/duplicate field: $name")
            continue
        }
        $oldFields[$name] = $field
    }
    $newFields = @{}
    foreach ($field in @($New.fields)) {
        $name = [string]$field.name
        if ([string]::IsNullOrWhiteSpace($name) -or $newFields.ContainsKey($name)) {
            $breaking.Add("candidate contains blank/duplicate field: $name")
            continue
        }
        $newFields[$name] = $field
    }

    foreach ($name in $oldFields.Keys) {
        $before = $oldFields[$name]
        if (-not $newFields.ContainsKey($name)) {
            if ([bool]$before.required) { $breaking.Add("required field removed: $name") }
            continue
        }
        $after = $newFields[$name]
        if ([string]$before.type -ne [string]$after.type) {
            $breaking.Add("field type changed: $name ($($before.type) -> $($after.type))")
        }
        if (-not [bool]$before.required -and [bool]$after.required) {
            $breaking.Add("optional field became required: $name")
        }
        foreach ($shapeProperty in @("length", "scale", "encoding", "position")) {
            $beforeProperty = $before.PSObject.Properties[$shapeProperty]
            $afterProperty = $after.PSObject.Properties[$shapeProperty]
            $beforeValue = if ($null -eq $beforeProperty) { $null } else { $beforeProperty.Value }
            $afterValue = if ($null -eq $afterProperty) { $null } else { $afterProperty.Value }
            if ([string]$beforeValue -ne [string]$afterValue) {
                $breaking.Add("field shape changed: $name.$shapeProperty ($beforeValue -> $afterValue)")
            }
        }
    }

    foreach ($name in $newFields.Keys) {
        if (-not $oldFields.ContainsKey($name) -and [bool]$newFields[$name].required) {
            $breaking.Add("new required field added: $name")
        }
    }

    [ordered]@{
        compatible = ($breaking.Count -eq 0)
        contractId = [string]$New.contractId
        kind = [string]$New.kind
        baselineVersion = [string]$Old.version
        candidateVersion = [string]$New.version
        breakingChanges = @($breaking)
    }
}

if ($SelfTest) {
    $baseline = [pscustomobject]@{
        contractId = "cpf-self-test"
        kind = "REST"
        version = "1.0.0"
        fields = @(
            [pscustomobject]@{ name="id"; type="string"; required=$true },
            [pscustomobject]@{ name="memo"; type="string"; required=$false }
        )
    }
    $compatible = [pscustomobject]@{
        contractId = "cpf-self-test"
        kind = "REST"
        version = "1.1.0"
        fields = @(
            [pscustomobject]@{ name="id"; type="string"; required=$true },
            [pscustomobject]@{ name="memo"; type="string"; required=$false },
            [pscustomobject]@{ name="tag"; type="string"; required=$false }
        )
    }
    $breaking = [pscustomobject]@{
        contractId = "cpf-self-test"
        kind = "REST"
        version = "2.0.0"
        fields = @(
            [pscustomobject]@{ name="memo"; type="string"; required=$true }
        )
    }
    $shapeBaseline = [pscustomobject]@{
        contractId = "cpf-shape-test"; kind = "FIXED_LENGTH"; version = "1.0.0";
        fields = @([pscustomobject]@{ name="amount"; type="string"; required=$true; length=10; position=1 })
    }
    $shapeCandidate = [pscustomobject]@{
        contractId = "cpf-shape-test"; kind = "FIXED_LENGTH"; version = "1.1.0";
        fields = @([pscustomobject]@{ name="amount"; type="string"; required=$true; position=1 })
    }
    $ok = Test-CpfContractCompatibility -Old $baseline -New $compatible
    $bad = Test-CpfContractCompatibility -Old $baseline -New $breaking
    $shapeBad = Test-CpfContractCompatibility -Old $shapeBaseline -New $shapeCandidate
    if (-not $ok.compatible -or $bad.compatible -or $shapeBad.compatible -or @($bad.breakingChanges).Count -lt 1) {
        throw "CPF Contract Compatibility self-test failed."
    }
    Write-Host "CPF Contract Compatibility self-test passed."
    exit 0
}

if ([string]::IsNullOrWhiteSpace($Baseline) -or [string]::IsNullOrWhiteSpace($Candidate)) {
    throw "Baseline과 Candidate contract JSON 경로가 필요합니다. 정적 Gate 검증은 -SelfTest를 사용하세요."
}
if (-not (Test-Path -LiteralPath $Baseline -PathType Leaf)) { throw "Baseline contract not found: $Baseline" }
if (-not (Test-Path -LiteralPath $Candidate -PathType Leaf)) { throw "Candidate contract not found: $Candidate" }

$old = Get-Content -LiteralPath $Baseline -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
$new = Get-Content -LiteralPath $Candidate -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
$result = Test-CpfContractCompatibility -Old $old -New $new
$result | ConvertTo-Json -Depth 20
if (-not $result.compatible) { exit 2 }
