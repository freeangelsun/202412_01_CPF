Set-StrictMode -Version Latest

$script:CpfSupportedDatabaseVendors = @("mariadb", "mysql", "postgresql", "oracle", "sqlserver")
$script:CpfDefaultDatabasePorts = @{
    mariadb = 3306
    mysql = 3306
    postgresql = 5432
    oracle = 1521
    sqlserver = 1433
}

function Get-CpfDatabaseProfile {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Path
    )

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "CPF DB Profile 파일이 없습니다: $Path"
    }
    $profile = Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
    if ([int]$profile.profileVersion -ne 1) {
        throw "지원하지 않는 CPF DB Profile version입니다: $($profile.profileVersion)"
    }
    return $profile
}

function Resolve-CpfProfileSecret {
    param(
        [Parameter(Mandatory = $true)]
        $SecretSpec,
        [Parameter(Mandatory = $true)]
        [string] $DisplayName,
        [bool] $AllowDevDefault = $false
    )

    if ($SecretSpec -is [string]) {
        if (-not [string]::IsNullOrWhiteSpace([string]$SecretSpec)) {
            return [string]$SecretSpec
        }
    } else {
        $envProperty = $SecretSpec.PSObject.Properties["env"]
        $envName = if ($null -ne $envProperty) { [string]$envProperty.Value } else { "" }
        if (-not [string]::IsNullOrWhiteSpace($envName)) {
            $value = [Environment]::GetEnvironmentVariable($envName)
            if (-not [string]::IsNullOrWhiteSpace($value)) { return $value }
        }

        $fallbackProperty = $SecretSpec.PSObject.Properties["fallbackEnv"]
        $fallbackEnv = if ($null -ne $fallbackProperty) { [string]$fallbackProperty.Value } else { "" }
        if (-not [string]::IsNullOrWhiteSpace($fallbackEnv)) {
            $value = [Environment]::GetEnvironmentVariable($fallbackEnv)
            if (-not [string]::IsNullOrWhiteSpace($value)) { return $value }
        }

        if ($AllowDevDefault) {
            $devDefaultProperty = $SecretSpec.PSObject.Properties["devDefault"]
            $devDefault = if ($null -ne $devDefaultProperty) { [string]$devDefaultProperty.Value } else { "" }
            if (-not [string]::IsNullOrWhiteSpace($devDefault)) { return $devDefault }
        }
    }

    throw "$DisplayName Secret을 해석할 수 없습니다. Profile에 지정된 env/fallbackEnv 값을 설정하세요."
}

function Assert-CpfDbIdentifier {
    param([string] $Value, [string] $DisplayName)
    if ([string]::IsNullOrWhiteSpace($Value) -or $Value -notmatch '^[A-Za-z][A-Za-z0-9_$#]{0,62}$') {
        throw "$DisplayName 값이 안전한 DB 식별자 규칙에 맞지 않습니다: $Value"
    }
}

function Assert-CpfDbUsername {
    param([string] $Value, [string] $DisplayName)
    if ([string]::IsNullOrWhiteSpace($Value) -or $Value -notmatch '^[A-Za-z][A-Za-z0-9_.$#@-]{0,127}$') {
        throw "$DisplayName 값이 안전한 계정명 규칙에 맞지 않습니다: $Value"
    }
}

function Assert-CpfDomainIdentity {
    param(
        [Parameter(Mandatory = $true)][string] $DomainName,
        [Parameter(Mandatory = $true)][string] $SystemCode,
        [Parameter(Mandatory = $true)][string] $DisplayName
    )

    if ([string]::IsNullOrWhiteSpace($DomainName) -or
        $DomainName -notmatch '^[a-z][a-z0-9-]{1,39}$') {
        throw "$DisplayName.domainName 규칙 위반입니다. 소문자 readable domain name을 사용하세요: $DomainName"
    }

    if ([string]::IsNullOrWhiteSpace($SystemCode) -or
        $SystemCode -notmatch '^[A-Z]{3}$') {
        throw "$DisplayName.systemCode 규칙 위반입니다. 정확히 3자리 대문자를 사용하세요: $SystemCode"
    }
}

function Get-CpfVendorDefaultPort {
    param([Parameter(Mandatory = $true)][string] $Vendor)
    $normalized = $Vendor.Trim().ToLowerInvariant()
    if ($normalized -notin $script:CpfSupportedDatabaseVendors) {
        throw "지원하지 않는 DB Vendor입니다: $Vendor"
    }
    return [int]$script:CpfDefaultDatabasePorts[$normalized]
}

function ConvertTo-CpfModuleProfile {
    param(
        [Parameter(Mandatory = $true)] $Profile,
        [Parameter(Mandatory = $true)] [string] $ModuleKey,
        [switch] $SkipSecretResolution
    )

    $raw = $Profile.modules.$ModuleKey
    if ($null -eq $raw) { throw "DB Profile modules에 '$ModuleKey' 설정이 없습니다." }

    $domainName = ([string]$raw.domainName).Trim()
    $systemCode = ([string]$raw.systemCode).Trim().ToUpperInvariant()
    $moduleName = ([string]$raw.moduleName).Trim()
    Assert-CpfDomainIdentity $domainName $systemCode "modules.$ModuleKey"
    if ([string]::IsNullOrWhiteSpace($moduleName) -or $moduleName -notmatch '^cpf-[a-z][a-z0-9-]{1,39}$') {
        throw "modules.$ModuleKey.moduleName 규칙 위반입니다: $moduleName"
    }

    $vendor = ([string]$raw.vendor).Trim().ToLowerInvariant()
    if ($vendor -notin $script:CpfSupportedDatabaseVendors) {
        throw "module=$ModuleKey 지원하지 않는 Vendor=$vendor"
    }

    $port = [int]$raw.port
    if ($port -le 0) { $port = Get-CpfVendorDefaultPort $vendor }

    $sslModeProperty = $raw.PSObject.Properties["sslMode"]
    $sslMode = if ($null -ne $sslModeProperty) {
        ([string]$sslModeProperty.Value).Trim().ToLowerInvariant()
    } else {
        ""
    }
    if ([string]::IsNullOrWhiteSpace($sslMode)) {
        $sslMode = if (([string]$Profile.environment).ToLowerInvariant() -eq "production") {
            "verify-full"
        } else {
            "preferred"
        }
    }
    if ($sslMode -notin @("disabled", "preferred", "required", "verify-full")) {
        throw "module=$ModuleKey 지원하지 않는 sslMode=$sslMode"
    }

    $databaseName = [string]$raw.databaseName
    $schemaName = [string]$raw.schemaName
    Assert-CpfDbIdentifier $databaseName "$ModuleKey.databaseName"
    if (-not [string]::IsNullOrWhiteSpace($schemaName)) {
        Assert-CpfDbIdentifier $schemaName "$ModuleKey.schemaName"
    }

    Assert-CpfDbUsername ([string]$raw.admin.username) "$ModuleKey.admin.username"
    Assert-CpfDbUsername ([string]$raw.migration.username) "$ModuleKey.migration.username"
    Assert-CpfDbUsername ([string]$raw.runtime.username) "$ModuleKey.runtime.username"

    $allowDevDefault = ([string]$Profile.environment).ToLowerInvariant() -in @("development", "dev", "local") -and
        [bool]$Profile.policy.allowInlineDevDefaults

    $sslCaPathProperty = $raw.PSObject.Properties["sslCaPath"]
    $sslCaPath = if ($null -ne $sslCaPathProperty) {
        [string]$sslCaPathProperty.Value
    } else {
        ""
    }
    $databaseLifecycleProperty = $raw.PSObject.Properties["databaseLifecycle"]
    $databaseLifecycle = if ($null -ne $databaseLifecycleProperty) {
        ([string]$databaseLifecycleProperty.Value).Trim().ToLowerInvariant()
    } elseif ([bool]$raw.transitional) {
        "generated-domain"
    } else {
        "platform-pack"
    }
    if ($databaseLifecycle -notin @("platform-pack", "generated-domain")) {
        throw "modules.$ModuleKey.databaseLifecycle 값이 올바르지 않습니다: $databaseLifecycle"
    }
    if ($databaseLifecycle -eq "generated-domain" -and
            [bool]$raw.enabled) {
        throw "Generated Domain DB는 Platform Profile에서 enabled로 둘 수 없습니다. manifest 기반 초기화기를 사용하세요: $ModuleKey"
    }

    return [pscustomobject]@{
        moduleKey = $ModuleKey
        domainName = $domainName
        moduleName = $moduleName
        enabled = [bool]$raw.enabled
        required = [bool]$raw.required
        transitional = [bool]$raw.transitional
        databaseLifecycle = $databaseLifecycle
        systemCode = $systemCode
        logicalDatabase = [string]$raw.logicalDatabase
        vendor = $vendor
        host = [string]$raw.host
        port = $port
        databaseName = $databaseName
        schemaName = $schemaName
        clientPath = [string]$raw.clientPath
        sslMode = $sslMode
        sslCaPath = $sslCaPath
        adminUsername = [string]$raw.admin.username
        adminUserHost = [string]$raw.admin.userHost
        adminPassword = if ($SkipSecretResolution) {
            "__CPF_STATIC_PROFILE_VALIDATION__"
        } else {
            Resolve-CpfProfileSecret $raw.admin.password "$ModuleKey.admin.password" $allowDevDefault
        }
        migrationUsername = [string]$raw.migration.username
        migrationUserHost = [string]$raw.migration.userHost
        migrationPassword = if ($SkipSecretResolution) {
            "__CPF_STATIC_PROFILE_VALIDATION__"
        } else {
            Resolve-CpfProfileSecret $raw.migration.password "$ModuleKey.migration.password" $allowDevDefault
        }
        runtimeUsername = [string]$raw.runtime.username
        runtimeUserHost = [string]$raw.runtime.userHost
        runtimePassword = if ($SkipSecretResolution) {
            "__CPF_STATIC_PROFILE_VALIDATION__"
        } else {
            Resolve-CpfProfileSecret $raw.runtime.password "$ModuleKey.runtime.password" $allowDevDefault
        }
        productSeed = [bool]$raw.seed.product
        optionalSampleSeed = [bool]$raw.seed.optionalSample
        testSeed = [bool]$raw.seed.test
    }
}

function Get-CpfDomainDatabaseProfile {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Path
    )

    $profile = Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
    if ([int]$profile.profileVersion -ne 1) {
        throw "지원하지 않는 Generated Domain DB Profile version입니다: $($profile.profileVersion)"
    }
    return $profile
}
