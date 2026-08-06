BeforeAll {
    $Script = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..' 'run-db3-lifecycle.ps1'))
    $Source = Get-Content -LiteralPath $Script -Raw
}

Describe 'CPF DB3 lifecycle runner protocol' {
    It 'requires ExpectedHead and resolves repository root from Git' {
        $Source | Should -Match '\[Parameter\(Mandatory = \$true\)\][\s\S]*\$ExpectedHead'
        $Source | Should -Match 'rev-parse --show-toplevel'
        $Source | Should -Not -Match '\$expectedHead\s*=\s*["''][0-9a-f]{40}'
    }

    It 'never places a password in the argument list' {
        $Source | Should -Match "'--password-stdin'"
        $Source | Should -Match 'StandardInput\.WriteLine\(\$Password\)'
        $Source | Should -Not -Match '--password='
        $Source | Should -Not -Match 'ArgumentList\.Add\(\$password\)'
    }

    It 'redacts all configured secrets before Evidence write' {
        $Source | Should -Match 'Protect-Text'
        $Source | Should -Match '\*\*\*REDACTED\*\*\*'
        $Source | Should -Not -Match 'Tee-Object'
    }

    It 'preflights all Oracle PostgreSQL and MariaDB variables' {
        foreach ($token in @('CPF_RUNTIME_ORACLE_', 'CPF_RUNTIME_POSTGRESQL_', 'CPF_RUNTIME_MARIADB_')) {
            $Source | Should -Match $token
        }
    }

    It 'passes each password only through stdin and redacts runner output' -Skip:($null -eq (Get-Command pwsh -ErrorAction SilentlyContinue)) {
        $repoRoot = (& git -C $PSScriptRoot rev-parse --show-toplevel).Trim()
        $head = (& git -C $repoRoot rev-parse HEAD).Trim()
        $evidence = Join-Path $TestDrive 'evidence'
        $fake = Join-Path $TestDrive 'fake-db-runner.ps1'
        @'
param([Parameter(ValueFromRemainingArguments=$true)][string[]]$RunnerArguments)
$password = [Console]::In.ReadLine()
$vendorArg = $RunnerArguments | Where-Object { $_ -like '--vendor=*' } | Select-Object -First 1
$auditArg = $RunnerArguments | Where-Object { $_ -like '--audit-output=*' } | Select-Object -First 1
if (-not $vendorArg -or -not $auditArg) { exit 81 }
if ($RunnerArguments -match [regex]::Escape($password)) { exit 82 }
$auditPath = $auditArg.Substring('--audit-output='.Length)
$vendor = $vendorArg.Substring('--vendor='.Length)
[IO.File]::WriteAllText($auditPath, (@{
  dbVersion = "fake-$vendor-1"; lifecycleStatus = 'SUCCEEDED';
  stdinSha256 = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes($password))).ToLowerInvariant();
  arguments = $RunnerArguments
} | ConvertTo-Json -Depth 5), [Text.UTF8Encoding]::new($false))
[Console]::Out.WriteLine("stdout-secret=$password")
[Console]::Error.WriteLine("stderr-secret=$password")
exit 0
'@ | Set-Content -LiteralPath $fake -Encoding utf8NoBOM

        $saved = @{}
        $vars = @(
            'CPF_RUNTIME_ORACLE_JDBC_URL','CPF_RUNTIME_ORACLE_USERNAME','CPF_RUNTIME_ORACLE_PASSWORD',
            'CPF_RUNTIME_POSTGRESQL_JDBC_URL','CPF_RUNTIME_POSTGRESQL_USERNAME','CPF_RUNTIME_POSTGRESQL_PASSWORD',
            'CPF_RUNTIME_MARIADB_JDBC_URL','CPF_RUNTIME_MARIADB_USERNAME','CPF_RUNTIME_MARIADB_PASSWORD')
        foreach ($name in $vars) { $saved[$name] = [Environment]::GetEnvironmentVariable($name) }
        try {
            $passwords = @{
                oracle = 'test-' + [Guid]::NewGuid().ToString('N')
                postgresql = 'test-' + [Guid]::NewGuid().ToString('N')
                mariadb = 'test-' + [Guid]::NewGuid().ToString('N')
            }
            $env:CPF_RUNTIME_ORACLE_JDBC_URL = 'jdbc:oracle:thin:@fake'; $env:CPF_RUNTIME_ORACLE_USERNAME = 'oracle-user'; $env:CPF_RUNTIME_ORACLE_PASSWORD = $passwords.oracle
            $env:CPF_RUNTIME_POSTGRESQL_JDBC_URL = 'jdbc:postgresql://fake/db'; $env:CPF_RUNTIME_POSTGRESQL_USERNAME = 'pg-user'; $env:CPF_RUNTIME_POSTGRESQL_PASSWORD = $passwords.postgresql
            $env:CPF_RUNTIME_MARIADB_JDBC_URL = 'jdbc:mariadb://fake/db'; $env:CPF_RUNTIME_MARIADB_USERNAME = 'maria-user'; $env:CPF_RUNTIME_MARIADB_PASSWORD = $passwords.mariadb
            & $Script -ExpectedHead $head -EvidenceDir $evidence -RunnerExecutable (Get-Command pwsh).Source `
                -RunnerPrefixArguments @('-NoProfile','-File',$fake)
            $LASTEXITCODE | Should -Be 0
            foreach ($vendor in @('oracle','postgresql','mariadb')) {
                $passwordPattern = [regex]::Escape($passwords[$vendor])
                $stdout = Get-Content -LiteralPath (Join-Path $evidence "$vendor-stdout.log") -Raw
                $stderr = Get-Content -LiteralPath (Join-Path $evidence "$vendor-stderr.log") -Raw
                $stdout | Should -Match '\*\*\*REDACTED\*\*\*'
                $stderr | Should -Match '\*\*\*REDACTED\*\*\*'
                $stdout | Should -Not -Match $passwordPattern
                $stderr | Should -Not -Match $passwordPattern
                $audit = Get-Content -LiteralPath (Join-Path $evidence "$vendor-audit.json") -Raw | ConvertFrom-Json
                ($audit.arguments -join ' ') | Should -Not -Match $passwordPattern
                $audit.stdinSha256 | Should -Match '^[0-9a-f]{64}$'
            }
        }
        finally {
            foreach ($name in $vars) { [Environment]::SetEnvironmentVariable($name, $saved[$name]) }
        }
    }
}
