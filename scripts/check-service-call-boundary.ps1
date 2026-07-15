param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"
$failures = New-Object System.Collections.Generic.List[string]
$modules = @("mbr", "xyz", "bza", "bat", "adm")

function Add-Failure {
    param([string] $Message)
    $failures.Add($Message)
}

foreach ($module in $modules) {
    $sourceRoot = Join-Path $Root "$module/src/main/java"
    if (-not (Test-Path -LiteralPath $sourceRoot)) {
        continue
    }

    Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*.java" | ForEach-Object {
        $relativePath = $_.FullName.Substring($Root.Length + 1).Replace("\", "/")
        $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)

        if ($relativePath -notmatch "/tst/" -and $text -match "cpfWebClient\s*\.\s*service\s*\(") {
            Add-Failure "cpfWebClient.service direct call is forbidden: $relativePath"
        }
        if ($text -match "WebClient\s*\.\s*builder\s*\(") {
            Add-Failure "raw WebClient.builder call is forbidden in business module: $relativePath"
        }
        if ($text -match "new\s+RestTemplate\s*\(") {
            Add-Failure "raw RestTemplate creation is forbidden in business module: $relativePath"
        }
        if ($text -match "RestClient\s*\.\s*create\s*\(") {
            Add-Failure "raw RestClient.create call is forbidden in business module: $relativePath"
        }

        foreach ($targetModule in $modules) {
            if ($targetModule -eq $module) {
                continue
            }
            $forbiddenImport = "import\s+cpf\.$targetModule\..*(controller|repository|mapper).*;"
            if ($text -match $forbiddenImport) {
                Add-Failure "cross-domain Controller/Repository/Mapper import is forbidden: $relativePath -> cpf.$targetModule"
            }
        }
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Service call boundary scan passed."
