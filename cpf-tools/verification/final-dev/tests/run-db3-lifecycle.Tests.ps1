Describe 'CPF DB3 runner safety contract' {
    BeforeAll {
        $repoRoot = (& git -C $PSScriptRoot rev-parse --show-toplevel).Trim()
        $head = (& git -C $repoRoot rev-parse HEAD).Trim()
        $runner = Join-Path $PSScriptRoot '../run-db3-lifecycle.ps1'
        $script = Get-Content -LiteralPath $runner -Raw
        $dummy = Join-Path $TestDrive 'dummy-db-runner.ps1'
        @'
param([string]$Mode='success')
$connection = [Console]::In.ReadLine() | ConvertFrom-Json
$vendorArg = $args | Where-Object { $_ -like '--vendor=*' } | Select-Object -First 1
$auditArg = $args | Where-Object { $_ -like '--audit-output=*' } | Select-Object -First 1
$vendor = $vendorArg.Substring('--vendor='.Length)
$audit = $auditArg.Substring('--audit-output='.Length)
$leaked = @(Get-ChildItem Env: | Where-Object { $_.Name -like 'CPF_RUNTIME_*' }).Count
Write-Output "child_env_runtime_secret_count=$leaked"
Write-Output "intentional-secret-echo=$($connection.password)"
if ($Mode -eq 'timeout') {
    $marker = Join-Path (Split-Path -Parent $audit) "$vendor-grandchild-survived.txt"
    $code = "Start-Sleep -Seconds 3; Set-Content -LiteralPath '$marker' -Value survived"
    Start-Process -FilePath (Get-Process -Id $PID).Path -ArgumentList @('-NoProfile','-Command',$code) | Out-Null
    Start-Sleep -Seconds 20
    exit 0
}
@{ dbVersion='dummy-1'; lifecycleStatus='SUCCEEDED' } | ConvertTo-Json | Set-Content -LiteralPath $audit -Encoding utf8
exit 0
'@ | Set-Content -LiteralPath $dummy -Encoding utf8
        $pwsh = (Get-Process -Id $PID).Path
    }

    BeforeEach {
        $env:CPF_RUNTIME_ORACLE_JDBC_URL='jdbc:oracle:thin:@//db.example:1521/CPF'
        $env:CPF_RUNTIME_ORACLE_USERNAME='cpf_oracle'
        $script:oracleSecret = 'test-' + [guid]::NewGuid().ToString('N')
        $env:CPF_RUNTIME_ORACLE_PASSWORD=$script:oracleSecret
        $env:CPF_RUNTIME_POSTGRESQL_JDBC_URL='jdbc:postgresql://db.example:5432/cpf'
        $env:CPF_RUNTIME_POSTGRESQL_USERNAME='cpf_pg'
        $script:postgresSecret = 'test-' + [guid]::NewGuid().ToString('N')
        $env:CPF_RUNTIME_POSTGRESQL_PASSWORD=$script:postgresSecret
        $env:CPF_RUNTIME_MARIADB_JDBC_URL='jdbc:mariadb://db.example:3306/cpf'
        $env:CPF_RUNTIME_MARIADB_USERNAME='cpf_maria'
        $script:mariaSecret = 'test-' + [guid]::NewGuid().ToString('N')
        $env:CPF_RUNTIME_MARIADB_PASSWORD=$script:mariaSecret
    }

    AfterEach {
        'CPF_RUNTIME_ORACLE_JDBC_URL','CPF_RUNTIME_ORACLE_USERNAME','CPF_RUNTIME_ORACLE_PASSWORD',
        'CPF_RUNTIME_POSTGRESQL_JDBC_URL','CPF_RUNTIME_POSTGRESQL_USERNAME','CPF_RUNTIME_POSTGRESQL_PASSWORD',
        'CPF_RUNTIME_MARIADB_JDBC_URL','CPF_RUNTIME_MARIADB_USERNAME','CPF_RUNTIME_MARIADB_PASSWORD' |
            ForEach-Object { Remove-Item "Env:$_" -ErrorAction SilentlyContinue }
    }

    It 'uses the checked-in canonical QA34 runtime executor by default instead of a phantom Java class' {
        $script | Should -Match 'cpf-tools/scripts/invoke-cpf-qa34-db-runtime-matrix.ps1'
        $script | Should -Match 'cpf-db-lifecycle-contract.json'
        $script | Should -Not -Match "build/classes/java/main"
        $script | Should -Not -Match "@\('-cp', \$RunnerClasspath, \$RunnerClass\)"
    }

    It 'does not place URL username or password in argv' {
        $script | Should -Not -Match '"--url=\$url"'
        $script | Should -Not -Match '"--username=\$username"'
        $script | Should -Match '--connection-json-stdin'
    }

    It 'executes a real child with inherited runtime secrets removed and output redacted' {
        $evidence = Join-Path $TestDrive 'sanitized'
        & $runner -ExpectedHead $head -EvidenceDir $evidence -RunnerExecutable $pwsh `
            -RunnerPrefixArguments @('-NoProfile','-File',$dummy,'success') -TimeoutSeconds 10
        $LASTEXITCODE | Should -Be 0
        foreach ($vendor in 'oracle','postgresql','mariadb') {
            $stdout = Get-Content -LiteralPath (Join-Path $evidence "$vendor-stdout.log") -Raw
            $stdout | Should -Match 'child_env_runtime_secret_count=0'
            $stdout | Should -Match '\*\*\*REDACTED\*\*\*'
            foreach ($secret in $script:oracleSecret,$script:postgresSecret,$script:mariaSecret) { $stdout | Should -Not -Match ([regex]::Escape($secret)) }
        }
    }

    It 'kills a timed out child process tree so its grandchild cannot survive' {
        $evidence = Join-Path $TestDrive 'timeout'
        & $runner -ExpectedHead $head -EvidenceDir $evidence -RunnerExecutable $pwsh `
            -RunnerPrefixArguments @('-NoProfile','-File',$dummy,'timeout') -TimeoutSeconds 1
        $LASTEXITCODE | Should -Be 124
        Start-Sleep -Seconds 4
        foreach ($vendor in 'oracle','postgresql','mariadb') {
            Test-Path -LiteralPath (Join-Path $evidence "$vendor-grandchild-survived.txt") | Should -BeFalse
            (Get-Content -LiteralPath (Join-Path $evidence "$vendor-stderr.log") -Raw) | Should -Match 'timeout'
        }
        $summary = Get-Content -LiteralPath (Join-Path $evidence 'db3-lifecycle-summary.json') -Raw | ConvertFrom-Json
        @($summary.vendors | ForEach-Object lifecycleStatus | Select-Object -Unique) | Should -Be @('UNKNOWN_TIMEOUT')
    }

    It 'rejects credentials embedded in JDBC URLs' {
        $env:CPF_RUNTIME_POSTGRESQL_JDBC_URL='jdbc:postgresql://db.example:5432/cpf?password=leak'
        $evidence = Join-Path $TestDrive 'unsafe-url'
        { & $runner -ExpectedHead $head -EvidenceDir $evidence -RunnerExecutable $pwsh `
            -RunnerPrefixArguments @('-NoProfile','-File',$dummy,'success') -TimeoutSeconds 10 } | Should -Throw
    }
}
