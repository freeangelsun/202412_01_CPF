[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [switch] $All,
    [string[]] $DomainName = @(),
    [string[]] $SystemCode = @(),
    [ValidateSet('bootstrap', 'migration', 'verify', 'rollback')]
    [string] $Operation = 'bootstrap',
    [string] $ProfilePath = '',
    [string] $DatabaseVendor = $env:CPF_DOMAIN_DB_VENDOR,
    [string] $DatabaseHost = $env:CPF_DOMAIN_DB_HOST,
    [int] $DatabasePort = 0,
    [string] $DatabaseName = $env:CPF_DOMAIN_DB_NAME,
    [string] $SchemaName = $env:CPF_DOMAIN_DB_SCHEMA,
    [string] $DatabaseUsername = $env:CPF_DOMAIN_DB_USERNAME,
    [string] $DatabasePassword = $env:CPF_DOMAIN_DB_PASSWORD,
    [string] $ClientPath = '',
    [switch] $Apply,
    [switch] $ConfirmRollback
)

# Registered domains are discovered only from each Generated Root's Developer-Facing gradle.properties contract.
# DB Vendor/connection values are deployment inputs and never project-local Generator bookkeeping.
$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root 'cpf-tools/generator/tools/generated-domain-common.ps1')
$initializer = Join-Path $Root 'cpf-tools/generator/tools/initialize-domain-database.ps1'
if (-not (Test-Path -LiteralPath $initializer -PathType Leaf)) {
    throw "Generated Domain DB initializer가 없습니다: $initializer"
}
if ($All -and ($DomainName.Count -gt 0 -or $SystemCode.Count -gt 0)) {
    throw '-All과 DomainName/SystemCode는 동시에 사용할 수 없습니다.'
}
if ($Operation -eq 'rollback' -and (-not $Apply -or -not $ConfirmRollback)) {
    throw 'Generated Domain rollback은 -Apply -ConfirmRollback이 모두 필요합니다.'
}

$catalog = @(Get-CpfGeneratedDomainInventory -Root $Root | Where-Object {
    [bool]$_.exists -and [bool]$_.databaseEnabled
})
foreach ($item in $catalog) {
    if (@($item.forbiddenPermanentMetadata).Count -gt 0) {
        throw "Generated Project에 금지된 영구 metadata가 있습니다: project=$($item.projectName) paths=$(@($item.forbiddenPermanentMetadata) -join ',')"
    }
}
if ($catalog.Count -eq 0) {
    $selected = @()
} elseif ($All -or ($DomainName.Count -eq 0 -and $SystemCode.Count -eq 0)) {
    $selected = @($catalog | Sort-Object systemCode, domainName)
} else {
    $selectedByName = [Collections.Generic.Dictionary[string,object]]::new([StringComparer]::OrdinalIgnoreCase)
    foreach ($name in $DomainName) {
        $match = @($catalog | Where-Object { [string]$_.domainName -eq $name.Trim().ToLowerInvariant() })
        if ($match.Count -ne 1) { throw "알 수 없는 Generated DomainName입니다: $name" }
        $selectedByName[[string]$match[0].domainName] = $match[0]
    }
    foreach ($code in $SystemCode) {
        $match = @($catalog | Where-Object { [string]$_.systemCode -eq $code.Trim().ToUpperInvariant() })
        if ($match.Count -ne 1) { throw "알 수 없는 Generated SystemCode입니다: $code" }
        $selectedByName[[string]$match[0].domainName] = $match[0]
    }
    $selected = @($selectedByName.Values | Sort-Object systemCode, domainName)
}

Write-Host "Generated Domain DB selected: $((@($selected | ForEach-Object { "$($_.domainName)/$($_.systemCode)" })) -join ', ')"
$results = @()
foreach ($item in $selected) {
    $arguments = @(
        '-NoProfile', '-File', $initializer,
        '-Root', $Root,
        '-DomainName', [string]$item.domainName,
        '-SystemCode', [string]$item.systemCode,
        '-DefinitionPath', [string]$item.contractPath,
        '-Operation', $Operation
    )
    foreach ($entry in @(
        @{ name = 'ProfilePath'; value = $ProfilePath },
        @{ name = 'DatabaseVendor'; value = $DatabaseVendor },
        @{ name = 'DatabaseHost'; value = $DatabaseHost },
        @{ name = 'DatabaseName'; value = $DatabaseName },
        @{ name = 'SchemaName'; value = $SchemaName },
        @{ name = 'DatabaseUsername'; value = $DatabaseUsername },
        @{ name = 'DatabasePassword'; value = $DatabasePassword },
        @{ name = 'ClientPath'; value = $ClientPath }
    )) {
        if (-not [string]::IsNullOrWhiteSpace([string]$entry.value)) {
            $arguments += @("-$($entry.name)", [string]$entry.value)
        }
    }
    if ($DatabasePort -gt 0) { $arguments += @('-DatabasePort', [string]$DatabasePort) }
    if ($Apply) { $arguments += '-Apply' }
    if ($ConfirmRollback) { $arguments += '-ConfirmRollback' }
    & pwsh @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Generated Domain DB 작업 실패: domain=$($item.domainName), operation=$Operation"
    }
    $results += [ordered]@{
        domainName = [string]$item.domainName
        systemCode = [string]$item.systemCode
        contractPath = [string]$item.contractPath
        operation = $Operation
        status = if ($Apply) { '완료' } else { '미검증' }
    }
}

$resultDir = Join-Path $Root 'cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/db-install/generated-domains'
New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
$resultPath = Join-Path $resultDir 'generated-domain-batch-result.sanitized.json'
$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString('o')
    operation = $Operation
    applied = [bool]$Apply
    generatedProjectMetadata = 'ABSENT'
    domains = $results
}
[IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 30) + [Environment]::NewLine,
    $Utf8NoBom)
Write-Host "Generated Domain DB result: $resultPath"
