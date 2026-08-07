Describe 'CPF DB3 runner safety contract' {
    BeforeAll { $script = Get-Content -LiteralPath (Join-Path $PSScriptRoot '../run-db3-lifecycle.ps1') -Raw }
    It 'does not place URL username or password in argv' {
        $script | Should -Not -Match '\"--url=\$url\"'
        $script | Should -Not -Match '\"--username=\$username\"'
        $script | Should -Match '--connection-json-stdin'
    }
    It 'clears inherited secrets and injects only an allowlist' {
        $script | Should -Match '\$start\.Environment\.Clear\(\)'
        $script | Should -Match 'CPF_DB_RUNNER_CHILD'
    }
    It 'kills a timed out child tree and reports UNKNOWN timeout' {
        $script | Should -Match 'WaitForExit\(\$TimeoutSeconds \* 1000\)'
        $script | Should -Match 'ExitCode = 124'
        $script | Should -Match 'UNKNOWN_TIMEOUT'
        $script | Should -Match 'Kill\(\$true\)'
    }
    It 'rejects credentials embedded in JDBC URLs' {
        $script | Should -Match 'Assert-SafeJdbcUrl'
        $script | Should -Match 'password\|passwd\|pwd\|secret\|token'
    }
}
