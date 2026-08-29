# Generated Customer Domain의 물리 식별은 Generated Project 내부 manifest가 아니라 Framework canonical definition을 사용합니다.
function Get-CpfPythonCommand {
    $python = Get-Command python -ErrorAction SilentlyContinue
    if ($null -ne $python) { return @($python.Source) }
    $py = Get-Command py -ErrorAction SilentlyContinue
    if ($null -ne $py) { return @($py.Source, '-3') }
    throw 'Python 3 실행파일을 찾을 수 없습니다.'
}

function Get-CpfGeneratedDomainInventory {
    param(
        [Parameter(Mandatory = $true)][string] $Root,
        [string] $DomainName = '',
        [string] $DefinitionPath = '',
        [switch] $IncludeMissing
    )
    if (-not [string]::IsNullOrWhiteSpace($DomainName) -and
            -not [string]::IsNullOrWhiteSpace($DefinitionPath)) {
        throw 'DomainName과 DefinitionPath는 동시에 지정할 수 없습니다.'
    }
    $tool = Join-Path $Root 'cpf-tools/generator/tools/generated_domain_inventory.py'
    if (-not (Test-Path -LiteralPath $tool -PathType Leaf)) {
        throw "Generated Domain canonical inventory tool이 없습니다: $tool"
    }
    $python = @(Get-CpfPythonCommand)
    $arguments = @()
    if ($python.Count -gt 1) { $arguments += $python[1..($python.Count - 1)] }
    $arguments += @($tool, '--root', $Root)
    if (-not [string]::IsNullOrWhiteSpace($DomainName)) {
        $arguments += @('--domain', $DomainName)
    }
    if (-not [string]::IsNullOrWhiteSpace($DefinitionPath)) {
        if (-not [IO.Path]::IsPathRooted($DefinitionPath)) {
            $DefinitionPath = Join-Path $Root $DefinitionPath
        }
        $arguments += @('--file', ([IO.Path]::GetFullPath($DefinitionPath)))
    }
    if ($IncludeMissing) { $arguments += '--include-missing' }
    $previousPythonUtf8 = $env:PYTHONUTF8
    try {
        $env:PYTHONUTF8 = '1'
        $json = & $python[0] @arguments
        $exitCode = $LASTEXITCODE
    } finally {
        $env:PYTHONUTF8 = $previousPythonUtf8
    }
    if ($exitCode -ne 0) { throw 'Generated Domain canonical inventory 실행에 실패했습니다.' }
    $parsed = ($json -join [Environment]::NewLine) | ConvertFrom-Json -ErrorAction Stop
    return @($parsed.domains)
}

function Get-CpfGeneratedDomainDefinition {
    param(
        [Parameter(Mandatory = $true)][string] $Root,
        [Parameter(Mandatory = $true)][string] $DomainName,
        [string] $DefinitionPath = '',
        [switch] $IncludeMissing
    )
    $arguments = @{ Root = $Root; IncludeMissing = $IncludeMissing }
    if ([string]::IsNullOrWhiteSpace($DefinitionPath)) {
        $arguments.DomainName = $DomainName
    } else {
        $arguments.DefinitionPath = $DefinitionPath
    }
    $rows = @(Get-CpfGeneratedDomainInventory @arguments)
    $normalized = $DomainName.Trim().ToLowerInvariant()
    $matches = @($rows | Where-Object { ([string]$_.domainName).ToLowerInvariant() -eq $normalized })
    if ($matches.Count -ne 1) {
        throw "Generated Domain canonical definition을 정확히 하나 찾지 못했습니다: domain=$normalized count=$($matches.Count)"
    }
    return $matches[0]
}

function Invoke-CpfCanonicalCli {
    param(
        [Parameter(Mandatory = $true)][string] $Root,
        [Parameter(Mandatory = $true)][string[]] $Arguments
    )
    $cliJar = Join-Path $Root 'cpf-tools/runtime/cli/lib/cpf-cli.jar'
    if (-not (Test-Path -LiteralPath $cliJar -PathType Leaf)) { throw "CPF canonical CLI JAR가 없습니다: $cliJar" }
    $processArguments = @($Arguments)
    if ($processArguments.Count -gt 0 -and $processArguments[0] -eq 'domain') {
        $processArguments = @('dev') + $processArguments
    } elseif ($processArguments.Count -gt 1 -and
            $processArguments[0] -eq 'db' -and $processArguments[1] -eq 'render') {
        $remainingArguments = if ($processArguments.Count -gt 2) {
            @($processArguments[2..($processArguments.Count - 1)])
        } else { @() }
        $processArguments = @('dev', 'db-render') + $remainingArguments
    }

    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    if ($IsLinux -or $IsMacOS) {
        $cli = Join-Path $Root 'cpf-tools/runtime/cli/cpf'
        if (-not (Test-Path -LiteralPath $cli -PathType Leaf)) { throw "CPF canonical CLI가 없습니다: $cli" }
        $shell = Get-Command sh -ErrorAction SilentlyContinue
        if ($null -eq $shell) { throw 'CPF canonical CLI 실행에 필요한 POSIX sh를 찾을 수 없습니다.' }
        $startInfo.FileName = $shell.Source
        $startInfo.ArgumentList.Add($cli)
    } else {
        $cli = Join-Path $Root 'cpf-tools/runtime/cli/cpf.cmd'
        if (-not (Test-Path -LiteralPath $cli -PathType Leaf)) { throw "CPF canonical CLI가 없습니다: $cli" }
        $command = if (-not [string]::IsNullOrWhiteSpace($env:ComSpec)) { $env:ComSpec } else { 'cmd.exe' }
        $startInfo.FileName = $command
        $startInfo.ArgumentList.Add('/d')
        $startInfo.ArgumentList.Add('/c')
        $startInfo.ArgumentList.Add($cli)
    }
    foreach ($argument in $processArguments) { $startInfo.ArgumentList.Add([string]$argument) }
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.CreateNoWindow = $true
    $utf8 = [Text.UTF8Encoding]::new($false)
    $startInfo.StandardOutputEncoding = $utf8
    $startInfo.StandardErrorEncoding = $utf8
    $startInfo.Environment['CPF_WORKSPACE'] = $Root
    $startInfo.Environment['PYTHONUTF8'] = '1'
    $startInfo.Environment['PYTHONIOENCODING'] = 'utf-8'
    $javaOptions = @($env:JAVA_TOOL_OPTIONS, '-Dfile.encoding=UTF-8', '-Dsun.stdout.encoding=UTF-8', '-Dsun.stderr.encoding=UTF-8') |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    $startInfo.Environment['JAVA_TOOL_OPTIONS'] = ($javaOptions -join ' ')

    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    try {
        if (-not $process.Start()) { throw 'CPF canonical CLI process를 시작하지 못했습니다.' }
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $stdout = $stdoutTask.GetAwaiter().GetResult()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        $exitCode = $process.ExitCode
    } finally {
        $process.Dispose()
    }
    if ($exitCode -ne 0) {
        throw "CPF canonical CLI 실패: exitCode=$exitCode stderr=$($stderr.Trim()) stdout=$($stdout.Trim())"
    }
    $output = @($stdout -split "`r?`n")
    $jsonStart = -1
    for ($index = 0; $index -lt $output.Count; $index++) {
        $candidate = $output[$index].TrimStart()
        if ($candidate.StartsWith('{')) {
            $jsonStart = $index
            break
        }
    }
    if ($jsonStart -lt 0) {
        throw "CPF canonical CLI 결과에 JSON document가 없습니다: $($stdout.Trim())"
    }
    $jsonLines = [Collections.Generic.List[string]]::new()
    for ($index = $jsonStart; $index -lt $output.Count; $index++) {
        $jsonLines.Add($output[$index])
        $json = $jsonLines -join [Environment]::NewLine
        try {
            return $json | ConvertFrom-Json -Depth 100 -ErrorAction Stop
        } catch {
            # Unified CLI의 END log 전까지 완결된 첫 JSON document를 계속 탐색합니다.
        }
    }
    throw "CPF canonical CLI 결과가 JSON이 아닙니다: $($jsonLines -join [Environment]::NewLine)"
}
