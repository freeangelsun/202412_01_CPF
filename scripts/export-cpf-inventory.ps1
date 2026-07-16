param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260716_01")
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function Get-RelativePath {
    param([string] $Path)

    return $Path.Substring($Root.Length).TrimStart("\", "/").Replace("\", "/")
}

function Get-Owner {
    param([string] $RelativePath)

    $first = ($RelativePath -split "/")[0].ToUpperInvariant()
    if (@("PFW", "CMN", "MBR", "XYZ", "ADM", "BZA", "BAT") -contains $first) {
        return $first
    }
    return "ROOT"
}

function Get-Capability {
    param([string] $Value)

    $text = $Value.ToLowerInvariant()
    $rules = [ordered]@{
        "transaction-header" = "transaction|header|trace|segment|timeline"
        "service-call" = "servicecall|webclient|restclient|registry|routing|health"
        "logging-recovery" = "logging|logwriter|fallback|recovery|journal|spool"
        "idempotency" = "idempot"
        "broker-event" = "broker|outbox|inbox|dlq|event"
        "unknown-reconciliation" = "unknown|reconcil|compensat"
        "file-transfer" = "filetransfer|sftp|ftp|ftps|scp|ssh"
        "archive" = "archive|compression|gzip|zip|tar"
        "security" = "security|auth|permission|mask|credential|password|session|token"
        "batch-center-cut" = "batch|center|scheduler|worker|job"
        "fixed-length" = "fixedlength|telegram|layout|parser|formatter"
        "code-message-config" = "code|message|config|notification"
        "openapi" = "openapi|swagger"
        "domain-generator" = "create-domain|generator|template"
        "education" = "/edu/|education|sample"
        "database" = "sql|migration|mapper|repository"
        "documentation" = "readme|guide|specs/|\.docx$|\.html$|\.md$"
    }
    foreach ($entry in $rules.GetEnumerator()) {
        if ($text -match $entry.Value) {
            return $entry.Key
        }
    }
    return "application-core"
}

function New-Asset {
    param(
        [string] $Type,
        [string] $Path,
        [string] $Name,
        [string] $Detail = ""
    )

    return [pscustomobject]@{
        type = $Type
        owner = Get-Owner $Path
        capability = Get-Capability ($Path + " " + $Name)
        path = $Path
        name = $Name
        detail = $Detail
    }
}

$assets = New-Object System.Collections.Generic.List[object]
$moduleNames = @("pfw", "cmn", "mbr", "xyz", "adm", "bza", "bat")
foreach ($moduleName in $moduleNames) {
    $modulePath = Join-Path $Root $moduleName
    if (Test-Path -LiteralPath $modulePath -PathType Container) {
        $assets.Add((New-Asset "gradle-module" $moduleName $moduleName)) | Out-Null
    }
}

$javaFiles = @(Get-ChildItem -LiteralPath $Root -Recurse -File -Filter "*.java" | Where-Object {
    $_.FullName -notmatch "[\\/](build|out|generated)[\\/]"
})
foreach ($file in $javaFiles) {
    $relative = Get-RelativePath $file.FullName
    $text = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
    $packageMatch = [regex]::Match($text, '(?m)^\s*package\s+([^;]+);')
    if ($packageMatch.Success) {
        $assets.Add((New-Asset "java-package" $relative $packageMatch.Groups[1].Value)) | Out-Null
    }

    $typeMatch = [regex]::Match($text, '(?m)^\s*public\s+(?:abstract\s+|final\s+|sealed\s+)?(class|interface|record|enum)\s+([A-Za-z0-9_]+)')
    if ($typeMatch.Success) {
        $kind = $typeMatch.Groups[1].Value
        $name = $typeMatch.Groups[2].Value
        $assetType = "java-public-type"
        if ($kind -eq "interface" -and $name -match '(Port|Facade|Contract)$') { $assetType = "public-port" }
        elseif ($name -match 'Controller$') { $assetType = "controller" }
        elseif ($name -match 'Service$') { $assetType = "service" }
        elseif ($name -match 'Repository$') { $assetType = "repository" }
        elseif ($name -match 'Mapper$') { $assetType = "mapper" }
        elseif ($name -match '(Engine|Facade|Adapter|Worker)$') { $assetType = "engine-facade-adapter" }
        elseif ($relative -match '/edu/') { $assetType = "education-source" }
        elseif ($relative -match '/test/') { $assetType = "test-source" }
        $assets.Add((New-Asset $assetType $relative $name $kind)) | Out-Null
    }

    if ($relative -match '/controller/' -or $text -match '@RestController|@Controller') {
        foreach ($mapping in [regex]::Matches($text, '@(?:Get|Post|Put|Delete|Patch|Request)Mapping\s*\((?<args>[^)]*)\)')) {
            $assets.Add((New-Asset "api-mapping" $relative $file.BaseName $mapping.Groups['args'].Value.Trim())) | Out-Null
        }
    }
}

$resourceFiles = @(Get-ChildItem -LiteralPath $Root -Recurse -File | Where-Object {
    $_.FullName -notmatch "[\\/](build|out|generated|\.git)[\\/]" -and
    $_.Name -match '^application.*\.(yml|yaml|properties)$'
})
foreach ($file in $resourceFiles) {
    $relative = Get-RelativePath $file.FullName
    $assets.Add((New-Asset "runtime-configuration" $relative $file.Name)) | Out-Null
}

$sqlFiles = @(Get-ChildItem -LiteralPath (Join-Path $Root "specs/sql") -Recurse -File -Filter "*.sql")
foreach ($file in $sqlFiles) {
    $relative = Get-RelativePath $file.FullName
    $text = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
    foreach ($match in [regex]::Matches($text, '(?im)^\s*CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?(?:(?:`?\w+`?)\.)?`?([a-z][a-z0-9_]*)`?')) {
        $assets.Add((New-Asset "sql-table" $relative $match.Groups[1].Value)) | Out-Null
    }
    foreach ($match in [regex]::Matches($text, '(?im)^\s*(?:CREATE\s+)?(?:UNIQUE\s+)?INDEX\s+`?([a-z][a-z0-9_]*)`?')) {
        $assets.Add((New-Asset "sql-index" $relative $match.Groups[1].Value)) | Out-Null
    }
    $sqlType = if ($relative -match '/migration/flyway/') { "flyway-migration" } else { "sql-file" }
    $assets.Add((New-Asset $sqlType $relative $file.Name)) | Out-Null
}

$scripts = @(Get-ChildItem -LiteralPath (Join-Path $Root "scripts") -File -Filter "*.ps1")
foreach ($file in $scripts) {
    $relative = Get-RelativePath $file.FullName
    $assets.Add((New-Asset "script" $relative $file.Name)) | Out-Null
}

$documentExtensions = @(".md", ".html", ".docx")
$documents = @(Get-ChildItem -LiteralPath $Root -Recurse -File | Where-Object {
    $documentExtensions -contains $_.Extension.ToLowerInvariant() -and
    $_.FullName -notmatch "[\\/](build|out|\.git|node_modules)[\\/]" -and
    $_.FullName -notmatch "[\\/]specs[\\/]evidence[\\/]"
})
foreach ($file in $documents) {
    $relative = Get-RelativePath $file.FullName
    $assets.Add((New-Asset "document" $relative $file.Name $file.Extension.ToLowerInvariant())) | Out-Null
}

$rootEntries = @(Get-ChildItem -LiteralPath $Root -Force | Where-Object { $_.Name -ne ".git" })
foreach ($entry in $rootEntries) {
    $type = if ($entry.PSIsContainer) { "root-directory" } else { "root-file" }
    $assets.Add((New-Asset $type $entry.Name $entry.Name)) | Out-Null
}

$sorted = @($assets | Sort-Object type, owner, path, name -Unique)
$countsByType = [ordered]@{}
foreach ($group in ($sorted | Group-Object type | Sort-Object Name)) {
    $countsByType[$group.Name] = $group.Count
}
$countsByCapability = [ordered]@{}
foreach ($group in ($sorted | Group-Object capability | Sort-Object Name)) {
    $countsByCapability[$group.Name] = $group.Count
}

$head = (& git -C $Root rev-parse HEAD).Trim()
$branch = (& git -C $Root branch --show-current).Trim()
$requestSha = (Get-FileHash -LiteralPath (Join-Path $Root "CPF_NEW_REQUEST.md") -Algorithm SHA256).Hash.ToLowerInvariant()
$inventory = [ordered]@{
    cpfInventoryVersion = 1
    generatedAt = (Get-Date).ToString("o")
    startCommit = $head
    branch = $branch
    requestSha256 = $requestSha
    assetCount = $sorted.Count
    countsByType = $countsByType
    countsByCapability = $countsByCapability
    assets = $sorted
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$json = ($inventory | ConvertTo-Json -Depth 10)
[System.IO.File]::WriteAllText((Join-Path $ResultDir "cpf-inventory.sanitized.json"), $json + "`n", $Utf8NoBom)

$markdown = New-Object System.Collections.Generic.List[string]
$markdown.Add("# CPF Capability Inventory") | Out-Null
$markdown.Add("") | Out-Null
$markdown.Add("- start commit: ``$head``") | Out-Null
$markdown.Add("- request SHA-256: ``$requestSha``") | Out-Null
$markdown.Add("- assets: $($sorted.Count)") | Out-Null
$markdown.Add("") | Out-Null
$markdown.Add("## Counts By Type") | Out-Null
$markdown.Add("") | Out-Null
$markdown.Add("| Type | Count |") | Out-Null
$markdown.Add("| --- | ---: |") | Out-Null
foreach ($entry in $countsByType.GetEnumerator()) {
    $markdown.Add("| $($entry.Key) | $($entry.Value) |") | Out-Null
}
$markdown.Add("") | Out-Null
$markdown.Add("## Counts By Capability") | Out-Null
$markdown.Add("") | Out-Null
$markdown.Add("| Capability | Count |") | Out-Null
$markdown.Add("| --- | ---: |") | Out-Null
foreach ($entry in $countsByCapability.GetEnumerator()) {
    $markdown.Add("| $($entry.Key) | $($entry.Value) |") | Out-Null
}
[System.IO.File]::WriteAllText((Join-Path $ResultDir "cpf-inventory.sanitized.md"), (($markdown -join "`n") + "`n"), $Utf8NoBom)

Write-Host ("CPF inventory exported: assets={0}, result={1}" -f $sorted.Count, $ResultDir)
