param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$failures = New-Object System.Collections.Generic.List[string]
$modules = @(
    [ordered]@{ project = 'cpf-member'; package = 'member' },
    [ordered]@{ project = 'cpf-reference'; package = 'reference' },
    [ordered]@{ project = 'cpf-biz-admin'; package = 'bizadmin' },
    [ordered]@{ project = 'cpf-batch'; package = 'batch' },
    [ordered]@{ project = 'cpf-admin'; package = 'admin' },
    [ordered]@{ project = 'cpf-account'; package = 'account' },
    [ordered]@{ project = 'cpf-external'; package = 'external' }
)

function Add-Failure {
    param([string] $Message)
    $failures.Add($Message)
}

foreach ($module in $modules) {
    $sourceRoot = Join-Path $Root "$($module.project)/src/main/java"
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
            if ($targetModule.package -eq $module.package) {
                continue
            }
            $forbiddenImport = "import\s+com\.cpf\.$([regex]::Escape($targetModule.package))\..*(controller|repository|mapper).*;"
            if ($text -match $forbiddenImport) {
                Add-Failure "cross-domain Controller/Repository/Mapper import is forbidden: $relativePath -> com.cpf.$($targetModule.package)"
            }
        }
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Service call boundary scan passed."
