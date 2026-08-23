param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path
)

if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw "CPF Platform DB migration tool gate는 pwsh 7 이상이 필요합니다."
}

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
$runner = Join-Path $Root "cpf-tools/db/tools/invoke-platform-database-migration.ps1"
$defaultProfilePath = Join-Path $Root "cpf-tools/db/config/database-install.default.json"

function Assert-CpfGate {
    param([bool] $Condition, [string] $Message)
    if (-not $Condition) { throw $Message }
    Write-Host "[PASS] $Message"
}

function Invoke-CpfFixtureRunner {
    param(
        [Parameter(Mandatory = $true)][string[]] $Arguments,
        [Parameter(Mandatory = $true)][int] $ExpectedExitCode
    )

    $output = @(& pwsh -NoProfile -File $runner @Arguments 2>&1 |
            ForEach-Object { $_.ToString() })
    $exitCode = $LASTEXITCODE
    if ($exitCode -ne $ExpectedExitCode) {
        throw "Fixture runner exit code가 다릅니다. expected=$ExpectedExitCode actual=$exitCode output=$($output -join ' | ')"
    }
    return @($output)
}

Assert-CpfGate (Test-Path -LiteralPath $runner -PathType Leaf) "Platform migration runner가 존재한다."

$tokens = $null
$parseErrors = $null
[void][Management.Automation.Language.Parser]::ParseFile(
    $runner,
    [ref]$tokens,
    [ref]$parseErrors)
Assert-CpfGate ($parseErrors.Count -eq 0) "Platform migration runner PowerShell syntax가 유효하다."

$runnerText = Get-Content -LiteralPath $runner -Raw -Encoding UTF8
foreach ($requiredToken in @(
        'Get-CpfDatabaseProfile',
        'databaseLifecycle -eq "platform-pack"',
        'supportedVendors',
        'checksums.sha256',
        'Get-CpfMariaVersionedMigrationFiles',
        '^U{0}__.+\.sql$',
        'mariadb-historical-migration-routing.json',
        'MariaDB migration에는 명시적 USE logicalDatabase routing이 필요합니다',
        'ConfirmApply',
        'ConfirmApplicationsStopped',
        'ConfirmRollbackReady',
        'ExpectedPlanSha256',
        'BackupManifestPath',
        'MYSQL_PWD',
        'PGPASSWORD',
        '/nolog',
        '미검증',
        '실패'
    )) {
    Assert-CpfGate $runnerText.Contains($requiredToken) "Runner contract token을 포함한다: $requiredToken"
}
Assert-CpfGate (-not $runnerText.Contains("mbrDB','accDB")) "Runner에 Generated Domain 고정 목록이 없다."
Assert-CpfGate (-not $runnerText.Contains("CPF_DB_ROOT_PASSWORD")) "Runner에 특정 password 환경변수나 평문 secret이 하드코딩되지 않았다."

$tempRoot = Join-Path ([IO.Path]::GetTempPath()) ("cpf-platform-migration-gate-" + [guid]::NewGuid().ToString("N"))
[IO.Directory]::CreateDirectory($tempRoot) | Out-Null
try {
    $defaultProfile = Get-Content -LiteralPath $defaultProfilePath -Raw -Encoding UTF8 |
        ConvertFrom-Json -Depth 50
    $mariaProfilePath = ""

    foreach ($vendor in @("mariadb", "postgresql", "oracle")) {
        $profile = $defaultProfile |
            ConvertTo-Json -Depth 50 |
            ConvertFrom-Json -Depth 50
        # Historical migration packs retain their published logical DB names.
        # Enable and separate only these disposable verifier targets; canonical
        # Product defaults continue to share cpfDB and keep optional modules off.
        $profile.modules.batch.enabled = $true
        $profile.modules.batch.logicalDatabase = "batDB"
        $profile.modules.batch.sharesDatabaseWith = ""
        $profile.modules.batch.vendor = $vendor
        $profile.modules.batch.port = @{ mariadb = 3306; postgresql = 5432; oracle = 1521 }[$vendor]
        $profile.modules.batch.databaseName = "batToolFixture"
        $profile.modules.batch.schemaName = "batToolSchema"
        $profile.modules.batch.clientPath = ""
        $profile.modules.admin.enabled = $true
        $profile.modules.admin.logicalDatabase = "admDB"
        $profile.modules.admin.sharesDatabaseWith = ""
        $profile.modules.admin.vendor = $vendor
        $profile.modules.admin.port = @{ mariadb = 3306; postgresql = 5432; oracle = 1521 }[$vendor]
        $profile.modules.admin.databaseName = "admToolFixture"
        $profile.modules.admin.schemaName = "admToolSchema"
        $profile.modules.admin.clientPath = ""
        # The developer-facing owner is education.  This isolated fixture uses
        # the immutable historical refDB pack name while mapping it to a
        # disposable physical database/schema; Product profiles remain on the
        # canonical referenceFixture declaration.
        $profile.modules.education.logicalDatabase = "refDB"
        $profile.modules.education.vendor = $vendor
        $profile.modules.education.port = @{ mariadb = 3306; postgresql = 5432; oracle = 1521 }[$vendor]
        $profile.modules.education.databaseName = "refToolFixture"
        $profile.modules.education.schemaName = "refToolSchema"
        $profile.modules.education.clientPath = ""

        $profilePath = Join-Path $tempRoot "$vendor-profile.json"
        $resultPath = Join-Path $tempRoot "$vendor-result.json"
        [IO.File]::WriteAllText(
            $profilePath,
            ($profile | ConvertTo-Json -Depth 50) + "`n",
            $Utf8NoBom)
        if ($vendor -eq "mariadb") { $mariaProfilePath = $profilePath }

        [void](Invoke-CpfFixtureRunner @(
                "-Root", $Root,
                "-ProfilePath", $profilePath,
                "-Direction", "upgrade",
                "-MigrationVersion", "73",
                "-Modules", "batch",
                "-ResultPath", $resultPath
            ) 0)

        $resultText = Get-Content -LiteralPath $resultPath -Raw -Encoding UTF8
        $result = $resultText | ConvertFrom-Json -Depth 50
        Assert-CpfGate ($result.mode -eq "DRY_RUN" -and $result.status -eq "미검증") "$vendor 기본 실행은 DB를 변경하지 않는 Dry-run이다."
        Assert-CpfGate ($result.plan.vendor -eq $vendor) "$vendor Vendor pack이 선택된다."
        Assert-CpfGate (@($result.plan.operations).Count -eq 1) "$vendor V73 BAT operation이 정확히 하나 계획된다."
        Assert-CpfGate ($result.plan.operations[0].physicalDatabase -eq "batToolFixture") "$vendor logical DB가 profile physical DB로 매핑된다."
        Assert-CpfGate ($result.plan.operations[0].physicalSchema -eq "batToolSchema") "$vendor physical schema가 plan에 반영된다."
        Assert-CpfGate ($result.plan.operations[0].migrationPath -match "/$vendor/migration/flyway/") "$vendor 중앙 migration pack 경로를 사용한다."
        Assert-CpfGate ($result.planSha256 -match "^[0-9a-f]{64}$") "$vendor plan checksum을 생성한다."
        Assert-CpfGate ($result.plan.operations[0].migrationSha256 -match "^[0-9a-f]{64}$") "$vendor migration checksum을 검증한다."
        Assert-CpfGate ($result.plan.operations[0].rollbackSha256 -match "^[0-9a-f]{64}$") "$vendor rollback safety hash를 plan에 고정한다."
        Assert-CpfGate (-not $resultText.Contains("CPF_FIXTURE_SECRET")) "$vendor sanitized result에 secret이 없다."

        foreach ($referenceVersion in @(93, 94)) {
            $referenceResultPath = Join-Path $tempRoot "$vendor-v$referenceVersion-result.json"
            [void](Invoke-CpfFixtureRunner @(
                    "-Root", $Root,
                    "-ProfilePath", $profilePath,
                    "-Direction", "upgrade",
                    "-MigrationVersion", "$referenceVersion",
                    "-Modules", "education",
                    "-ResultPath", $referenceResultPath
                ) 0)
            $referenceResult = Get-Content -LiteralPath $referenceResultPath -Raw -Encoding UTF8 |
                ConvertFrom-Json -Depth 50
            Assert-CpfGate (@($referenceResult.plan.operations).Count -eq 1) "$vendor V$referenceVersion REF operation이 정확히 하나 계획된다."
            Assert-CpfGate ($referenceResult.plan.operations[0].logicalDatabase -eq "refDB") "$vendor V$referenceVersion logical DB ownership이 refDB다."
            Assert-CpfGate ($referenceResult.plan.operations[0].migrationPath -match "/migration/flyway/refDB/V${referenceVersion}__") "$vendor V$referenceVersion logical DB 하위 migration을 발견한다."
            Assert-CpfGate ($referenceResult.plan.operations[0].rollbackPath -match "/rollback/refDB/U${referenceVersion}__") "$vendor U$referenceVersion top-level rollback pack을 발견한다."
        }

        $referenceRollbackPath = Join-Path $tempRoot "$vendor-u94-result.json"
        [void](Invoke-CpfFixtureRunner @(
                "-Root", $Root,
                "-ProfilePath", $profilePath,
                "-Direction", "rollback",
                "-MigrationVersion", "94",
                "-Modules", "education",
                "-ResultPath", $referenceRollbackPath
            ) 0)
        $referenceRollback = Get-Content -LiteralPath $referenceRollbackPath -Raw -Encoding UTF8 |
            ConvertFrom-Json -Depth 50
        Assert-CpfGate ($referenceRollback.plan.operations[0].selectedPath -match "/rollback/refDB/U94__") "$vendor rollback plan은 U94를 선택한다."
    }

    if ([string]::IsNullOrWhiteSpace($mariaProfilePath)) { throw "MariaDB fixture profile was not created." }

    $missingSelectionPath = Join-Path $tempRoot "missing-selection.json"
    [void](Invoke-CpfFixtureRunner @(
            "-Root", $Root,
            "-ProfilePath", $mariaProfilePath,
            "-Modules", "batch",
            "-ResultPath", $missingSelectionPath
        ) 1)
    $missingSelection = Get-Content -LiteralPath $missingSelectionPath -Raw -Encoding UTF8 |
        ConvertFrom-Json -Depth 30
    Assert-CpfGate ($missingSelection.status -eq "실패") "명시 baseline/selection 없는 실행은 실패로 기록된다."
    Assert-CpfGate ($missingSelection.error -match "자동 baseline/latest 추정은 금지") "명시 baseline/selection 없는 실행은 fail-closed다."

    $v64RoutingPath = Join-Path $tempRoot "v64-routing.json"
    [void](Invoke-CpfFixtureRunner @(
            "-Root", $Root,
            "-ProfilePath", $mariaProfilePath,
            "-Direction", "upgrade",
            "-MigrationVersion", "64",
            "-Modules", "core",
            "-ResultPath", $v64RoutingPath
        ) 0)
    $v64Routing = Get-Content -LiteralPath $v64RoutingPath -Raw -Encoding UTF8 |
        ConvertFrom-Json -Depth 30
    Assert-CpfGate (@($v64Routing.plan.operations).Count -eq 1 -and
        $v64Routing.plan.operations[0].logicalDatabase -eq "cpfDB") "Checksum 고정 V64 explicit routing이 cpfDB 하나로 계획된다."

    $v69RoutingPath = Join-Path $tempRoot "v69-routing.json"
    [void](Invoke-CpfFixtureRunner @(
            "-Root", $Root,
            "-ProfilePath", $mariaProfilePath,
            "-Direction", "upgrade",
            "-MigrationVersion", "69",
            "-Modules", "core,admin",
            "-ResultPath", $v69RoutingPath
        ) 0)
    $v69Routing = Get-Content -LiteralPath $v69RoutingPath -Raw -Encoding UTF8 |
        ConvertFrom-Json -Depth 30
    Assert-CpfGate (@($v69Routing.plan.operations).Count -eq 2) "Checksum 고정 V69 explicit routing이 cpfDB/admDB 두 operation으로 계획된다."
    Assert-CpfGate ((@($v69Routing.plan.operations.logicalDatabase | Sort-Object) -join ",") -eq "admDB,cpfDB") "V69 multi-owner logical routing이 정확하다."

    $partialV69Path = Join-Path $tempRoot "v69-partial-owner.json"
    [void](Invoke-CpfFixtureRunner @(
            "-Root", $Root,
            "-ProfilePath", $mariaProfilePath,
            "-Direction", "upgrade",
            "-MigrationVersion", "69",
            "-Modules", "core",
            "-ResultPath", $partialV69Path
        ) 1)
    $partialV69 = Get-Content -LiteralPath $partialV69Path -Raw -Encoding UTF8 |
        ConvertFrom-Json -Depth 30
    Assert-CpfGate ($partialV69.error -match "logical DB Module을 모두 선택") "V69 multi-owner migration의 부분 적용을 차단한다."

    $ambiguousRoutingPath = Join-Path $tempRoot "ambiguous-routing.json"
    [void](Invoke-CpfFixtureRunner @(
            "-Root", $Root,
            "-ProfilePath", $mariaProfilePath,
            "-Direction", "upgrade",
            "-MigrationVersion", "58",
            "-Modules", "core",
            "-ResultPath", $ambiguousRoutingPath
        ) 1)
    $ambiguousRouting = Get-Content -LiteralPath $ambiguousRoutingPath -Raw -Encoding UTF8 |
        ConvertFrom-Json -Depth 30
    Assert-CpfGate ($ambiguousRouting.status -eq "실패") "MariaDB logical routing이 없는 historical SQL은 실패로 기록된다."
    Assert-CpfGate ($ambiguousRouting.error -match "명시적 USE logicalDatabase routing") "MariaDB logical routing을 임의 추정하지 않는다."

    $missingConfirmationPath = Join-Path $tempRoot "missing-confirmation.json"
    [void](Invoke-CpfFixtureRunner @(
            "-Root", $Root,
            "-ProfilePath", $mariaProfilePath,
            "-Direction", "upgrade",
            "-MigrationVersion", "73",
            "-Modules", "batch",
            "-Apply",
            "-ResultPath", $missingConfirmationPath
        ) 1)
    $missingConfirmation = Get-Content -LiteralPath $missingConfirmationPath -Raw -Encoding UTF8 |
        ConvertFrom-Json -Depth 30
    Assert-CpfGate ($missingConfirmation.error -match "-ConfirmApply") "Apply는 명시 confirmation 없이는 실패한다."

    $planMismatchPath = Join-Path $tempRoot "plan-mismatch.json"
    [void](Invoke-CpfFixtureRunner @(
            "-Root", $Root,
            "-ProfilePath", $mariaProfilePath,
            "-Direction", "upgrade",
            "-MigrationVersion", "73",
            "-Modules", "batch",
            "-Apply",
            "-ConfirmApply",
            "-ConfirmApplicationsStopped",
            "-ConfirmRollbackReady",
            "-Operator", "CPF_VERIFIER",
            "-Reason", "plan-hash-fail-closed-contract",
            "-ApprovalReference", "CPF-STATIC-VERIFY",
            "-ExpectedPlanSha256", ("0" * 64),
            "-ResultPath", $planMismatchPath
        ) 1)
    $planMismatch = Get-Content -LiteralPath $planMismatchPath -Raw -Encoding UTF8 |
        ConvertFrom-Json -Depth 30
    Assert-CpfGate ($planMismatch.error -match "ExpectedPlanSha256") "Apply는 검토한 plan checksum 불일치 시 DB 연결 전에 실패한다."

    # Deleted duplicate release guides are not recreated.  The Developer Golden
    # Path is the single user-facing authority for migration safety and commands.
    foreach ($guide in @("cpf-docs/development/CPF_DEVELOPER_GOLDEN_PATH.md")) {
        $guidePath = Join-Path $Root $guide
        Assert-CpfGate (Test-Path -LiteralPath $guidePath -PathType Leaf) "Migration 실행계약 Guide가 존재한다: $guide"
        $guideText = Get-Content -LiteralPath $guidePath -Raw -Encoding UTF8
        Assert-CpfGate $guideText.Contains("invoke-platform-database-migration.ps1") "Guide가 정식 Platform migration runner를 안내한다: $guide"
        Assert-CpfGate $guideText.Contains("ExpectedPlanSha256") "Guide가 plan checksum 승인 계약을 안내한다: $guide"
        Assert-CpfGate $guideText.Contains("BackupManifestPath") "Guide가 backup 강제 계약을 안내한다: $guide"
    }
} finally {
    if (Test-Path -LiteralPath $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}

Write-Host "[PASS] CPF Platform DB migration tool static/fixture contract"
