param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [ValidateSet("", "mariadb", "postgresql", "oracle")]
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
        [string] $Label,
        [string[]] $Extensions = @()
    )

    $centralFiles = Get-RelativeFileMap $CentralDirectory $Extensions
    $mirrorFiles = Get-RelativeFileMap $MirrorDirectory $Extensions
    # Authoring source는 현재 수정 가능한 migration/rollback만 보유할 수 있고,
    # 중앙 lifecycle pack에는 불변 Historical migration이 추가로 존재할 수 있습니다.
    # Source의 모든 파일이 중앙 Pack과 byte-identical한지만 검사하며 중앙의
    # Historical 파일을 Source로 다시 복제하도록 강제하지 않습니다.
    foreach ($relative in @($mirrorFiles.Keys | Sort-Object -Unique)) {
        if (-not $centralFiles.ContainsKey($relative)) {
            $failures.Add("중앙 Directory 파일 누락: $Label/$relative")
            continue
        }
        Test-ExactFile $centralFiles[$relative] $mirrorFiles[$relative] "$Label/$relative"
    }
}

$canonicalManifestPath = Get-RepositoryPath "cpf-tools/db/vendor-pack-manifest.json"
if (-not (Test-Path -LiteralPath $canonicalManifestPath -PathType Leaf)) {
    throw "중앙 Vendor Pack manifest가 없습니다: $canonicalManifestPath"
}
$manifest = Get-Content -Raw -Encoding UTF8 -LiteralPath $canonicalManifestPath | ConvertFrom-Json
$domainTemplateContractPath = Get-RepositoryPath "cpf-tools/generator/contracts/central-domain-template-contract.json"
if (-not (Test-Path -LiteralPath $domainTemplateContractPath -PathType Leaf)) {
    throw "Generated Domain Template contract가 없습니다: $domainTemplateContractPath"
}
$domainTemplateContract = Get-Content -Raw -Encoding UTF8 -LiteralPath $domainTemplateContractPath |
        ConvertFrom-Json
$requiredVerifyColumns = @($domainTemplateContract.verifyContract.requiredColumns)
if ($requiredVerifyColumns.Count -eq 0) {
    throw "Generated Domain Template verifyContract.requiredColumns가 비어 있습니다."
}
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
                $extensions = if ($property.Name -in @("migration/flyway", "rollback")) {
                    @(".sql")
                } else {
                    @()
                }
                Test-ExactDirectory `
                    $centralPath `
                    $sourcePath `
                    "mariadb/$($property.Name)" `
                    $extensions
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
                # cpf-batch/src is a protected aggregate/source-cleanup area.  The
                # independent projects below cpf-batch are validated by their own
                # build and must not make this central pack gate traverse that tree.
                if ($_.Name -eq "cpf-batch") { return }
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
    $actualRuntimeModules = @(
        $actualCentralFiles.Keys |
            ForEach-Object { ($_ -split "/")[0] } |
            Sort-Object -CaseSensitive -Unique
    )
    $declaredRuntimeModules = @(
        $pack.runtimeModules.PSObject.Properties.Name |
            Sort-Object -CaseSensitive -Unique
    )
    $runtimeModuleDrift = @(Compare-Object $actualRuntimeModules $declaredRuntimeModules)
    if ($runtimeModuleDrift.Count -gt 0) {
        $failures.Add(
            "pack.json Runtime module 선언과 실제 중앙 Resource directory가 다릅니다: " +
            "vendor=$currentVendor actual=$($actualRuntimeModules -join ',') " +
            "declared=$($declaredRuntimeModules -join ',')"
        )
    }
    if ($legacyResourceFiles.Count -gt 0) {
        $failures.Add("R4 이후 금지된 module-local Vendor Runtime resource가 남았습니다: vendor=$currentVendor count=$($legacyResourceFiles.Count)")
    }
    foreach ($relative in $actualCentralFiles.Keys) {
        $module = ($relative -split "/")[0]
        if ($null -eq $pack.runtimeModules.$module) {
            $failures.Add("pack.json Runtime module 선언 누락: vendor=$currentVendor module=$module path=$relative")
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
    if ($templateText -match "(?i)\btransaction_global_id\b|\btransactionGlobalId\b") {
        $failures.Add("생성형 Domain Template에 폐기된 transactionGlobalId 계약이 남았습니다: $currentVendor")
    }
    if ($templateText -match "(?i)\b(?:MBR|ACC|REF|PAY)\b") {
        $failures.Add("생성형 Domain Template에 고정 Domain/SystemCode가 남았습니다: $currentVendor")
    }
    foreach ($relative in @(
        "install/10_empty_install.sql.template",
        "migration/V1____DOMAIN___domain.sql.template",
        "runtime/mybatis/__MAPPER__.xml.template",
        "verify/90_verify.sql.template"
    )) {
        $templatePath = Join-Path $domainTemplateRoot ($relative -replace "/", "\")
        if ((Test-Path -LiteralPath $templatePath -PathType Leaf) -and
                [System.IO.File]::ReadAllText(
                    $templatePath,
                    [System.Text.Encoding]::UTF8
                ) -notmatch "\btransaction_id\b") {
            $failures.Add(
                "생성형 Domain transactionId 물리 계약 누락: " +
                "vendor=$currentVendor path=$relative"
            )
        }
    }
    $verifyTemplate = Join-Path $domainTemplateRoot "verify\90_verify.sql.template"
    if (Test-Path -LiteralPath $verifyTemplate -PathType Leaf) {
        $verifyTemplateText = [System.IO.File]::ReadAllText(
                $verifyTemplate,
                [System.Text.Encoding]::UTF8)
        foreach ($requiredColumn in $requiredVerifyColumns) {
            if ($verifyTemplateText -notmatch ("\b" + [regex]::Escape([string]$requiredColumn) + "\b")) {
                $failures.Add(
                    "생성형 Domain 물리 Verify 공통 Column 누락: " +
                    "vendor=$currentVendor column=$requiredColumn"
                )
            }
        }
        if ($verifyTemplateText -notmatch "(?i)(?:character_maximum_length|char_length|max_length)\s*=\s*34") {
            $failures.Add(
                "생성형 Domain transaction_id 길이 Verify 누락: vendor=$currentVendor"
            )
        }
    }
    $provisionTemplate = Join-Path $domainTemplateRoot "provision\01_provision.sql.template"
    if (Test-Path -LiteralPath $provisionTemplate -PathType Leaf) {
        $provisionTemplateText = [System.IO.File]::ReadAllText(
            $provisionTemplate,
            [System.Text.Encoding]::UTF8
        )
        # Provision owns the physical database/container only where the vendor
        # supports it. PostgreSQL creates a database here and creates
        # the application schema during Install. Oracle provisioning is a
        # DBA-owned connectivity check. Requiring CPF_SCHEMA_NAME in every
        # Provision template incorrectly collapses these lifecycle boundaries.
        $requiredProvisionToken = switch ($currentVendor) {
            { $_ -eq "mariadb" } { "@CPF_SCHEMA_NAME@"; break }
            { $_ -eq "postgresql" } { "@CPF_DATABASE_NAME@"; break }
            default { $null }
        }
        if ($null -ne $requiredProvisionToken -and
                -not $provisionTemplateText.Contains($requiredProvisionToken)) {
            $failures.Add(
                "생성형 Domain Provision 물리 Database Metadata 누락: " +
                "vendor=$currentVendor token=$requiredProvisionToken"
            )
        }
        if ($currentVendor -eq "oracle" -and
                $provisionTemplateText -notmatch "(?i)SYS_CONTEXT\s*\(") {
            $failures.Add("Oracle 생성형 Domain Provision 연결 검증 누락")
        }
    }
    foreach ($schemaOwnedTemplate in @(
        "install/10_empty_install.sql.template",
        "migration/V1____DOMAIN___domain.sql.template",
        "runtime/mybatis/__MAPPER__.xml.template",
        "verify/90_verify.sql.template",
        "rollback/R1__remove___DOMAIN___domain.sql.template"
    )) {
        $schemaOwnedTemplatePath = Join-Path $domainTemplateRoot (
            $schemaOwnedTemplate -replace "/", "\"
        )
        if ((Test-Path -LiteralPath $schemaOwnedTemplatePath -PathType Leaf) -and
                -not [System.IO.File]::ReadAllText(
                    $schemaOwnedTemplatePath,
                    [System.Text.Encoding]::UTF8
                ).Contains("@CPF_SCHEMA_NAME@")) {
            $failures.Add(
                "생성형 Domain Schema 소유 Phase Metadata 누락: " +
                "vendor=$currentVendor path=$schemaOwnedTemplate"
            )
        }
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
