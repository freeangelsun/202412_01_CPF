param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [ValidateSet("", "mariadb", "mysql", "postgresql", "oracle", "sqlserver")]
    [string] $Vendor = "",
    [switch] $Quiet
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$Root = (Resolve-Path -LiteralPath $Root).Path
$failures = [System.Collections.Generic.List[string]]::new()
$checkedPairs = 0

function Get-RepositoryPath {
    param([string] $RelativePath)

    if ([System.IO.Path]::IsPathRooted($RelativePath)) {
        throw "Repository 상대경로만 허용됩니다: $RelativePath"
    }
    return [System.IO.Path]::GetFullPath(
        (Join-Path $Root ($RelativePath -replace "/", [System.IO.Path]::DirectorySeparatorChar))
    )
}

function Test-ExactFile {
    param(
        [string] $CentralPath,
        [string] $MirrorPath,
        [string] $Label
    )

    if (-not (Test-Path -LiteralPath $CentralPath -PathType Leaf)) {
        $failures.Add("중앙 파일 누락: $Label central=$CentralPath")
        return
    }
    if (-not (Test-Path -LiteralPath $MirrorPath -PathType Leaf)) {
        $failures.Add("전환 Mirror 누락: $Label mirror=$MirrorPath")
        return
    }
    $centralHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $CentralPath).Hash
    $mirrorHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $MirrorPath).Hash
    if ($centralHash -ne $mirrorHash) {
        $failures.Add("SHA-256 불일치: $Label")
        return
    }
    $script:checkedPairs++
}

function Get-RelativeFileMap {
    param(
        [string] $Directory,
        [string[]] $Extensions = @()
    )

    $map = @{}
    if (-not (Test-Path -LiteralPath $Directory -PathType Container)) {
        return $map
    }
    foreach ($file in Get-ChildItem -LiteralPath $Directory -Recurse -File) {
        if ($Extensions.Count -gt 0 -and $file.Extension -notin $Extensions) { continue }
        if ($file.Name -eq ".gitkeep") { continue }
        $relative = [System.IO.Path]::GetRelativePath($Directory, $file.FullName).Replace("\", "/")
        $map[$relative] = $file.FullName
    }
    return $map
}

function Test-ExactDirectory {
    param(
        [string] $CentralDirectory,
        [string] $MirrorDirectory,
        [string] $Label
    )

    $centralFiles = Get-RelativeFileMap $CentralDirectory
    $mirrorFiles = Get-RelativeFileMap $MirrorDirectory
    foreach ($relative in @($centralFiles.Keys + $mirrorFiles.Keys | Sort-Object -Unique)) {
        if (-not $centralFiles.ContainsKey($relative)) {
            $failures.Add("중앙 Directory 파일 누락: $Label/$relative")
            continue
        }
        if (-not $mirrorFiles.ContainsKey($relative)) {
            $failures.Add("전환 Mirror Directory 파일 누락: $Label/$relative")
            continue
        }
        Test-ExactFile $centralFiles[$relative] $mirrorFiles[$relative] "$Label/$relative"
    }
}

$canonicalManifestPath = Get-RepositoryPath "cpf-tools/db/vendor-pack-manifest.json"
$compatibilityManifestPath = Get-RepositoryPath "specs/sql/vendor-resource-manifest.json"
Test-ExactFile $canonicalManifestPath $compatibilityManifestPath "vendor-pack-manifest"
if (-not (Test-Path -LiteralPath $canonicalManifestPath -PathType Leaf)) {
    throw "중앙 Vendor Pack manifest가 없습니다: $canonicalManifestPath"
}
$manifest = Get-Content -Raw -Encoding UTF8 -LiteralPath $canonicalManifestPath | ConvertFrom-Json
$vendors = if ([string]::IsNullOrWhiteSpace($Vendor)) {
    @($manifest.supportedVendors)
} else {
    @($Vendor.ToLowerInvariant())
}

foreach ($currentVendor in $vendors) {
    $vendorEntry = $manifest.vendors.$currentVendor
    if ($null -eq $vendorEntry) {
        $failures.Add("Manifest Vendor 정의 누락: $currentVendor")
        continue
    }
    $vendorRoot = Get-RepositoryPath ([string] $vendorEntry.vendorRoot)
    $packPath = Get-RepositoryPath ([string] $vendorEntry.pack)
    if (-not (Test-Path -LiteralPath $packPath -PathType Leaf)) {
        $failures.Add("pack.json 누락: $currentVendor")
        continue
    }
    $pack = Get-Content -Raw -Encoding UTF8 -LiteralPath $packPath | ConvertFrom-Json
    if ([string] $pack.vendor -cne $currentVendor) {
        $failures.Add("pack.json Vendor 불일치: expected=$currentVendor actual=$($pack.vendor)")
    }

    if ($currentVendor -eq "mariadb") {
        foreach ($property in $pack.lifecycleAuthoringSources.PSObject.Properties) {
            $centralPath = Join-Path $vendorRoot ($property.Name -replace "/", "\")
            $sourcePath = Get-RepositoryPath ([string] $property.Value)
            if (Test-Path -LiteralPath $sourcePath -PathType Container) {
                Test-ExactDirectory $centralPath $sourcePath "mariadb/$($property.Name)"
            } else {
                Test-ExactFile $centralPath $sourcePath "mariadb/$($property.Name)"
            }
        }
    }

    $centralRuntimeRoot = Get-RepositoryPath ([string] $vendorEntry.runtimeRoot)
    $expectedCentralFiles = @{}
    $legacyResourceFiles = @(
        Get-ChildItem -LiteralPath $Root -Directory |
            ForEach-Object {
                $resourceRoot = Join-Path $_.FullName "src\main\resources"
                if (-not (Test-Path -LiteralPath $resourceRoot -PathType Container)) { return }
                @(
                    Get-ChildItem -LiteralPath (Join-Path $resourceRoot "sql\vendor\$currentVendor") `
                        -Recurse -File -Filter "*.sql" -ErrorAction SilentlyContinue
                    Get-ChildItem -LiteralPath (Join-Path $resourceRoot "mybatis\vendor\$currentVendor\mapper") `
                        -Recurse -File -Filter "*.xml" -ErrorAction SilentlyContinue
                )
            }
    )

    foreach ($legacyFile in $legacyResourceFiles) {
        $legacyRelative = [System.IO.Path]::GetRelativePath($Root, $legacyFile.FullName).Replace("\", "/")
        $targetRelative = $null
        if ($legacyRelative -match "^([^/]+)/src/main/resources/sql/vendor/$currentVendor/([^/]+)/(.+\.sql)$") {
            $ownerArtifact = $Matches[1]
            $module = $Matches[2]
            $tail = $Matches[3]
            $targetRelative = "$module/repository/$tail"
        } elseif ($legacyRelative -match "^([^/]+)/src/main/resources/mybatis/vendor/$currentVendor/mapper/([^/]+)/(.+\.xml)$") {
            $ownerArtifact = $Matches[1]
            $module = $Matches[2]
            $tail = $Matches[3]
            $targetRelative = "$module/mybatis/$tail"
        } else {
            $failures.Add("Legacy Runtime resource 경로 계약 위반: $legacyRelative")
            continue
        }

        $moduleDescriptor = $pack.runtimeModules.$module
        if ($null -eq $moduleDescriptor) {
            $failures.Add("pack.json Runtime module 선언 누락: vendor=$currentVendor module=$module")
        } elseif ([string] $moduleDescriptor.ownerArtifact -cne $ownerArtifact) {
            $failures.Add(
                "Runtime Ownership 불일치: vendor=$currentVendor module=$module " +
                "pack=$($moduleDescriptor.ownerArtifact) legacy=$ownerArtifact"
            )
        }

        $centralFile = Join-Path $centralRuntimeRoot ($targetRelative -replace "/", "\")
        if ($expectedCentralFiles.ContainsKey($targetRelative)) {
            $failures.Add("동일 중앙 경로로 충돌하는 Legacy resource: vendor=$currentVendor path=$targetRelative")
            continue
        }
        $expectedCentralFiles[$targetRelative] = $legacyFile.FullName
        Test-ExactFile $centralFile $legacyFile.FullName "$currentVendor/runtime/$targetRelative"
    }

    $actualCentralFiles = Get-RelativeFileMap $centralRuntimeRoot @(".sql", ".xml")
    foreach ($relative in $actualCentralFiles.Keys) {
        if (-not $expectedCentralFiles.ContainsKey($relative)) {
            $failures.Add("Legacy Mirror 등록이 없는 중앙 Runtime resource: $currentVendor/runtime/$relative")
        }
    }

    $requiredTemplateFiles = @(
        "provision/01_provision.sql.template",
        "install/10_empty_install.sql.template",
        "seed/20_product_seed.sql.template",
        "migration/V1____DOMAIN___domain.sql.template",
        "runtime/mybatis/__MAPPER__.xml.template",
        "verify/90_verify.sql.template",
        "rollback/R1__remove___DOMAIN___domain.sql.template"
    )
    $domainTemplateRoot = Get-RepositoryPath ([string] $vendorEntry.domainTemplateRoot)
    foreach ($relative in $requiredTemplateFiles) {
        if (-not (Test-Path -LiteralPath (Join-Path $domainTemplateRoot ($relative -replace "/", "\")) -PathType Leaf)) {
            $failures.Add("생성형 Domain 중앙 Template 누락: vendor=$currentVendor path=$relative")
        }
    }
    $templateFiles = @(
        Get-ChildItem -LiteralPath $domainTemplateRoot -Recurse -File -Filter "*.template" `
            -ErrorAction SilentlyContinue
    )
    $templateText = ($templateFiles | ForEach-Object {
        [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
    }) -join "`n"
    if ($templateText -match "(?i)\btransaction_id\b|\btransactionId\b") {
        $failures.Add("생성형 Domain Template에 폐기된 transaction_id/transactionId 계약이 남았습니다: $currentVendor")
    }
    if ($templateText -match "(?i)\b(?:MBR|ACC|REF|PAY)\b") {
        $failures.Add("생성형 Domain Template에 고정 Domain/SystemCode가 남았습니다: $currentVendor")
    }
    foreach ($relative in @(
        "install/10_empty_install.sql.template",
        "migration/V1____DOMAIN___domain.sql.template",
        "seed/20_product_seed.sql.template",
        "runtime/mybatis/__MAPPER__.xml.template",
        "verify/90_verify.sql.template"
    )) {
        $templatePath = Join-Path $domainTemplateRoot ($relative -replace "/", "\")
        if ((Test-Path -LiteralPath $templatePath -PathType Leaf) -and
                [System.IO.File]::ReadAllText(
                    $templatePath,
                    [System.Text.Encoding]::UTF8
                ) -notmatch "\btransaction_global_id\b") {
            $failures.Add(
                "생성형 Domain transactionGlobalId 물리 계약 누락: " +
                "vendor=$currentVendor path=$relative"
            )
        }
    }
    $provisionTemplate = Join-Path $domainTemplateRoot "provision\01_provision.sql.template"
    if ((Test-Path -LiteralPath $provisionTemplate -PathType Leaf) -and
            [System.IO.File]::ReadAllText(
                $provisionTemplate,
                [System.Text.Encoding]::UTF8
            ) -notmatch "@CPF_SCHEMA_NAME@") {
        $failures.Add("생성형 Domain Provision에 CPF_SCHEMA_NAME Metadata가 없습니다: $currentVendor")
    }

    if ([string] $vendorEntry.status -eq "미구현") {
        $unexpectedExecutableFiles = @(
            "provision/00_provision.sql",
            "install/00_empty_install.sql",
            "seed/00_product_seed.sql",
            "verify/00_verify.sql"
        ) | Where-Object {
            Test-Path -LiteralPath (Join-Path $vendorRoot ($_ -replace "/", "\")) -PathType Leaf
        }
        if ($unexpectedExecutableFiles.Count -gt 0) {
            $failures.Add(
                "미구현 Vendor에 실행 Lifecycle SQL이 있으나 상태가 승격되지 않았습니다: " +
                "vendor=$currentVendor files=$($unexpectedExecutableFiles -join ',')"
            )
        }
    }
}

$result = [ordered]@{
    checkedAt = (Get-Date).ToString("o")
    status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
    vendors = $vendors
    checkedFilePairs = $checkedPairs
    failures = @($failures)
}

if (-not $Quiet) {
    $result | ConvertTo-Json -Depth 10
}
if ($failures.Count -gt 0) {
    throw "중앙 DB Vendor Pack parity 검증 실패 ($($failures.Count)건): $($failures -join ' | ')"
}
