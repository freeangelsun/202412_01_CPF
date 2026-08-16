param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('mariadb', 'postgresql')]
    [string] $Vendor,
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot '../../../..')).Path,
    [string] $EvidenceDirectory = 'build/codex-onepass/0566f41d'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$image = if ($Vendor -eq 'mariadb') { 'mariadb:12.3.2' } else { 'postgres:18.4-trixie' }
$containerName = "cpf-d009-$Vendor-evidence"
$password = 'd009-container-only'
$startedAt = (Get-Date).ToUniversalTime().ToString('o')
$commands = [Collections.Generic.List[string]]::new()
$steps = [ordered]@{}
$containerId = $null
$exitCode = 1
$forwardPath = $null
$rollbackPath = $null
$invocation = "pwsh -NoProfile -File cpf-tools/runtime/tools/tests/invoke-bat-runtime-role-migration-container.ps1 -Vendor $Vendor -Root $Root"

function Invoke-SqlText([string] $Text, [bool] $ExpectSuccess = $true) {
    if ($Vendor -eq 'mariadb') {
        $Text | & docker exec -i $containerName mariadb -uroot "-p$password" 2>$null | Out-Null
    } else {
        $Text | & docker exec -i $containerName psql -v ON_ERROR_STOP=1 -U postgres 2>$null | Out-Null
    }
    $code = $LASTEXITCODE
    if ($ExpectSuccess -and $code -ne 0) { throw "$Vendor SQL execution failed: exitCode=$code" }
    if (-not $ExpectSuccess -and $code -eq 0) { throw "$Vendor SQL failure injection unexpectedly succeeded" }
    return $code
}

function Invoke-Query([string] $Sql) {
    if ($Vendor -eq 'mariadb') {
        $output = & docker exec $containerName mariadb -uroot "-p$password" -N -e $Sql
    } else {
        $output = & docker exec $containerName psql -At -v ON_ERROR_STOP=1 -U postgres -c $Sql
    }
    if ($LASTEXITCODE -ne 0) { throw "$Vendor query failed: $Sql" }
    return @($output) -join "`n"
}

try {
    if (@(docker ps -a --format '{{.Names}}' | Where-Object { $_ -eq $containerName }).Count -ne 0) {
        throw "unexpected existing container: $containerName"
    }
    if ($Vendor -eq 'mariadb') {
        $commands.Add("docker run --rm -d --name $containerName -e MARIADB_ROOT_PASSWORD=[REDACTED] $image")
        docker run --rm -d --name $containerName -e "MARIADB_ROOT_PASSWORD=$password" $image | Out-Null
    } else {
        $commands.Add("docker run --rm -d --name $containerName -e POSTGRES_PASSWORD=[REDACTED] $image")
        docker run --rm -d --name $containerName -e "POSTGRES_PASSWORD=$password" $image | Out-Null
    }
    if ($LASTEXITCODE -ne 0) { throw "$Vendor container start failed" }
    $containerId = docker inspect -f '{{.Id}}' $containerName

    $ready = $false
    for ($attempt = 0; $attempt -lt 45; $attempt++) {
        if ($Vendor -eq 'mariadb') {
            docker exec $containerName mariadb -uroot "-p$password" -N -e 'SELECT 1' 2>$null | Out-Null
        } else {
            docker exec $containerName pg_isready -U postgres 2>$null | Out-Null
        }
        if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) { throw "$Vendor readiness failed" }
    $steps.readiness = 'PASS'

    $fixture = if ($Vendor -eq 'mariadb') { "CREATE DATABASE cpfDB;`nUSE cpfDB;`n" } else { '' }
    $fixture += @'
CREATE TABLE OPS_RUNTIME_INSTANCE_STATE(instance_id VARCHAR(120) NOT NULL PRIMARY KEY, runtime_role VARCHAR(40) NULL);
CREATE TABLE BAT_RUNTIME_INSTANCE(instance_id VARCHAR(160) NOT NULL PRIMARY KEY, runtime_role VARCHAR(40) NOT NULL);
CREATE TABLE BAT_DEPLOYMENT_CELL(cell_id VARCHAR(120) NOT NULL PRIMARY KEY, runtime_role VARCHAR(40) NOT NULL);
INSERT INTO OPS_RUNTIME_INSTANCE_STATE VALUES ('ops-legacy','CONTROL_SERVER'),('ops-current','WORKER'),('ops-null',NULL);
INSERT INTO BAT_RUNTIME_INSTANCE VALUES ('bat-legacy','CENTER_CUT_RUNNER'),('bat-current','SCHEDULER');
INSERT INTO BAT_DEPLOYMENT_CELL VALUES ('cell-legacy','HOST_AGENT'),('cell-current','CONTROL_PLANE');
'@
    $commands.Add("inline-pre-v116-mixed-fixture.sql | docker exec -i $containerName $Vendor-client")
    [void](Invoke-SqlText $fixture)

    $vendorRoot = Join-Path $Root "cpf-tools/db/vendor/$Vendor"
    if ($Vendor -eq 'mariadb') {
        $forwardPath = Join-Path $vendorRoot 'migration/flyway/V116__batch_runtime_role_currentization.sql'
        $rollbackPath = Join-Path $vendorRoot 'rollback/R116__batch_runtime_role_currentization.sql'
    } else {
        $forwardPath = Join-Path $vendorRoot 'migration/flyway/cpfDB/V116__batch_runtime_role_currentization.sql'
        $rollbackPath = Join-Path $vendorRoot 'rollback/cpfDB/R116__batch_runtime_role_currentization.sql'
    }
    $forward = [IO.File]::ReadAllText($forwardPath, [Text.Encoding]::UTF8)
    $rollback = [IO.File]::ReadAllText($rollbackPath, [Text.Encoding]::UTF8)
    $commands.Add("$forwardPath | docker exec -i $containerName $Vendor-client")
    [void](Invoke-SqlText $forward)

    $constraintQuery = if ($Vendor -eq 'mariadb') {
        "SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='cpfDB' AND CONSTRAINT_NAME IN ('ck_ops_runtime_instance_role','ck_bat_runtime_instance_role','ck_bat_deployment_runtime_role')"
    } else {
        "SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=current_schema() AND constraint_name IN ('ck_ops_runtime_instance_role','ck_bat_runtime_instance_role','ck_bat_deployment_runtime_role')"
    }
    $roleQuery = if ($Vendor -eq 'mariadb') {
        "SELECT GROUP_CONCAT(DISTINCT runtime_role ORDER BY runtime_role) FROM (SELECT runtime_role FROM cpfDB.OPS_RUNTIME_INSTANCE_STATE UNION ALL SELECT runtime_role FROM cpfDB.BAT_RUNTIME_INSTANCE UNION ALL SELECT runtime_role FROM cpfDB.BAT_DEPLOYMENT_CELL) r"
    } else {
        "SELECT string_agg(DISTINCT runtime_role,',' ORDER BY runtime_role) FROM (SELECT runtime_role FROM OPS_RUNTIME_INSTANCE_STATE UNION ALL SELECT runtime_role FROM BAT_RUNTIME_INSTANCE UNION ALL SELECT runtime_role FROM BAT_DEPLOYMENT_CELL) r"
    }
    if ([int](Invoke-Query $constraintQuery) -ne 3) { throw "$Vendor canonical constraint count mismatch" }
    if ((Invoke-Query $roleQuery) -cne 'AGENT,CENTER_CUT,CONTROL_PLANE,SCHEDULER,WORKER') { throw "$Vendor canonical role mapping mismatch" }
    $snapshotQuery = if ($Vendor -eq 'mariadb') {
        "SELECT CONCAT('OPS:',instance_id,':',COALESCE(runtime_role,'<NULL>')) FROM cpfDB.OPS_RUNTIME_INSTANCE_STATE UNION ALL SELECT CONCAT('BAT:',instance_id,':',runtime_role) FROM cpfDB.BAT_RUNTIME_INSTANCE UNION ALL SELECT CONCAT('CELL:',cell_id,':',runtime_role) FROM cpfDB.BAT_DEPLOYMENT_CELL ORDER BY 1"
    } else {
        "SELECT 'OPS:'||instance_id||':'||COALESCE(runtime_role,'<NULL>') FROM OPS_RUNTIME_INSTANCE_STATE UNION ALL SELECT 'BAT:'||instance_id||':'||runtime_role FROM BAT_RUNTIME_INSTANCE UNION ALL SELECT 'CELL:'||cell_id||':'||runtime_role FROM BAT_DEPLOYMENT_CELL ORDER BY 1"
    }
    $collisionBefore = Invoke-Query $snapshotQuery
    $commands.Add("constraint-collision failure-injection: $forwardPath | docker exec -i $containerName $Vendor-client")
    $collisionExit = Invoke-SqlText $forward $false
    if ($collisionBefore -cne (Invoke-Query $snapshotQuery) -or [int](Invoke-Query $constraintQuery) -ne 3) {
        throw "$Vendor constraint-collision preflight changed data or constraints"
    }
    $steps.constraintCollisionNoWrite = 'PASS'
    $steps.constraintCollisionExpectedExitCode = $collisionExit
    $lowerCaseRejects = 0
    $databasePrefix = if ($Vendor -eq 'mariadb') { 'USE cpfDB; ' } else { '' }
    foreach ($sql in @(
        "${databasePrefix}INSERT INTO OPS_RUNTIME_INSTANCE_STATE VALUES ('ops-lower','worker');",
        "${databasePrefix}INSERT INTO BAT_RUNTIME_INSTANCE VALUES ('bat-lower','worker');",
        "${databasePrefix}INSERT INTO BAT_DEPLOYMENT_CELL VALUES ('cell-lower','worker');"
    )) {
        $commands.Add("failure-injection: $sql | docker exec -i $containerName $Vendor-client")
        if ((Invoke-SqlText $sql $false) -ne 0) { $lowerCaseRejects++ }
    }
    if ($lowerCaseRejects -ne 3) { throw "$Vendor lowercase constraint rejection mismatch: $lowerCaseRejects" }
    $steps.upgradeAndConstraint = 'PASS'

    $commands.Add("$rollbackPath | docker exec -i $containerName $Vendor-client")
    [void](Invoke-SqlText $rollback)
    $commands.Add("reapply: $forwardPath | docker exec -i $containerName $Vendor-client")
    [void](Invoke-SqlText $forward)
    $commands.Add("rollback-before-unknown: $rollbackPath | docker exec -i $containerName $Vendor-client")
    [void](Invoke-SqlText $rollback)
    $steps.rollbackReapply = 'PASS'

    [void](Invoke-SqlText "${databasePrefix}INSERT INTO OPS_RUNTIME_INSTANCE_STATE VALUES ('ops-unknown','ALIEN_ROLE');")
    $before = Invoke-Query $snapshotQuery
    $commands.Add("unknown-role failure-injection: $forwardPath | docker exec -i $containerName $Vendor-client")
    $unknownExit = Invoke-SqlText $forward $false
    $after = Invoke-Query $snapshotQuery
    if ($before -cne $after -or [int](Invoke-Query $constraintQuery) -ne 0) {
        throw "$Vendor unknown-role preflight changed data or constraints"
    }
    $steps.unknownNoWrite = 'PASS'
    $steps.unknownExpectedExitCode = $unknownExit
    $exitCode = 0
}
finally {
    $endedAt = (Get-Date).ToUniversalTime().ToString('o')
    if (@(docker ps -a --format '{{.Names}}' | Where-Object { $_ -eq $containerName }).Count -ne 0) {
        docker rm -f $containerName | Out-Null
    }
    $record = [ordered]@{
        stage = 'D-009-BAT-RUNTIME-ROLE-MIGRATION'
        gitHead = (git -C $Root rev-parse HEAD)
        invocation = $invocation
        invocationSha256 = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes($invocation))).ToLowerInvariant()
        verifierSha256 = (Get-FileHash -LiteralPath $PSCommandPath -Algorithm SHA256).Hash.ToLowerInvariant()
        vendor = $Vendor
        image = $image
        containerName = $containerName
        containerId = $containerId
        startedAtUtc = $startedAt
        endedAtUtc = $endedAt
        exitCode = $exitCode
        commands = $commands
        sqlSha256 = [ordered]@{
            forward = if ($forwardPath) { (Get-FileHash -LiteralPath $forwardPath -Algorithm SHA256).Hash.ToLowerInvariant() } else { $null }
            rollback = if ($rollbackPath) { (Get-FileHash -LiteralPath $rollbackPath -Algorithm SHA256).Hash.ToLowerInvariant() } else { $null }
        }
        steps = $steps
        containerRemoved = $true
    }
    $evidenceRoot = if ([IO.Path]::IsPathRooted($EvidenceDirectory)) { $EvidenceDirectory } else { Join-Path $Root $EvidenceDirectory }
    New-Item -ItemType Directory -Force -Path $evidenceRoot | Out-Null
    $logPath = Join-Path $evidenceRoot "d009-$Vendor-runtime-role-lifecycle.json"
    [IO.File]::WriteAllText($logPath, (($record | ConvertTo-Json -Depth 8) + "`n"), [Text.UTF8Encoding]::new($false))
    $logHash = (Get-FileHash -LiteralPath $logPath -Algorithm SHA256).Hash.ToLowerInvariant()
    Write-Host "EVIDENCE=$logPath"
    Write-Host "EVIDENCE_SHA256=$logHash"
    Write-Host "EXIT_CODE=$exitCode"
}
if ($exitCode -ne 0) { exit $exitCode }
