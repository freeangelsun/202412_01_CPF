param(
    [Parameter(Position=0)]
    [ValidateSet('help','build','test','verify-fast','verify-full','run-local','run-batch','status','stop','modules','resource')]
    [string] $Action,

    [ValidateSet('local','dev','test','stg','prod')]
    [string] $ResourceProfile = 'local',

    [string] $OutputRoot = (Join-Path $HOME 'Downloads')
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..')).Path
Set-Location $RepoRoot

$Gradle = Join-Path $RepoRoot 'gradlew.bat'
$RuntimeTools = Join-Path $RepoRoot 'cpf-tools\runtime\tools'
$Validation = Join-Path $RepoRoot 'cpf-tools\verification\tools\run-cpf-local-full-validation.ps1'


function Initialize-CpfJava25 {
    $candidates = [System.Collections.Generic.List[string]]::new()
    if (-not [string]::IsNullOrWhiteSpace($env:CPF_JAVA25_HOME)) { $candidates.Add($env:CPF_JAVA25_HOME) }
    if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) { $candidates.Add($env:JAVA_HOME) }
    if ($IsWindows) {
        foreach ($base in @('C:\dev\java', (Join-Path $env:ProgramFiles 'Eclipse Adoptium'))) {
            if (Test-Path -LiteralPath $base -PathType Container) {
                foreach ($dir in Get-ChildItem -LiteralPath $base -Directory -Filter 'jdk-25*' -ErrorAction SilentlyContinue | Sort-Object Name -Descending) {
                    $candidates.Add($dir.FullName)
                }
            }
        }
    }

    foreach ($candidate in @($candidates | Select-Object -Unique)) {
        if ([string]::IsNullOrWhiteSpace($candidate)) { continue }
        $javaExe = Join-Path $candidate $(if ($IsWindows) { 'bin\java.exe' } else { 'bin/java' })
        if (-not (Test-Path -LiteralPath $javaExe -PathType Leaf)) { continue }
        $versionText = (& $javaExe -version 2>&1 | Out-String)
        if ($versionText -match 'version\s+"25(?:\.|\")') {
            $env:JAVA_HOME = (Resolve-Path -LiteralPath $candidate).Path
            $env:PATH = (Join-Path $env:JAVA_HOME 'bin') + [IO.Path]::PathSeparator + $env:PATH
            Write-Host "Java         25 ($env:JAVA_HOME)"
            return
        }
    }
    throw 'CPF 개발 Shell은 Java 25가 필요합니다. CPF_JAVA25_HOME 또는 JAVA_HOME에 JDK 25를 지정하세요.'
}

function Read-CpfDevAction {
    $items = [ordered]@{
        '1' = @{ Action='run-local';   Label='로컬 실행';       Hint='권장 · 통합 WAS 1개 / 1 Port' }
        '2' = @{ Action='build';       Label='전체 빌드';       Hint='Java25 + 저메모리 + 품질 Gate' }
        '3' = @{ Action='test';        Label='전체 테스트';     Hint='Java Test만 실행' }
        '4' = @{ Action='verify-fast'; Label='빠른 검증';       Hint='NXT3 / 계약 / 정적 Gate' }
        '5' = @{ Action='verify-full'; Label='전체 로컬 검증';  Hint='Java/Frontend/DB/Runtime 최대 범위' }
        '6' = @{ Action='run-batch';   Label='Batch 실행';      Hint='Batch 사용할 때만' }
        '7' = @{ Action='modules';     Label='모듈 보기';       Hint='Public Starter 중심' }
        '8' = @{ Action='resource';    Label='자원 설정';       Hint='local/dev/test/stg/prod' }
        '9' = @{ Action='status';      Label='실행 상태';       Hint='Local Runtime 상태 확인' }
        '0' = @{ Action='stop';        Label='실행 종료';       Hint='Local Runtime 종료' }
    }

    while ($true) {
        Write-Host ''
        Write-Host '╔════════════════════════════════════════════════════════════╗'
        Write-Host '║                    CPF 개발 메뉴                          ║'
        Write-Host '╠════════════════════════════════════════════════════════════╣'
        foreach ($key in $items.Keys) {
            $item = $items[$key]
            Write-Host ("║ [{0}] {1,-14} {2,-34} ║" -f $key, $item.Label, $item.Hint)
        }
        Write-Host '║ [H] 도움말                                                 ║'
        Write-Host '║ [Q] 종료                                                   ║'
        Write-Host '╚════════════════════════════════════════════════════════════╝'
        Write-Host "Profile: $ResourceProfile   Output: $OutputRoot"
        $choice = (Read-Host '선택').Trim().ToUpperInvariant()
        if ($choice -eq 'Q') { return $null }
        if ($choice -eq 'H') { return 'help' }
        if ($items.Contains($choice)) { return $items[$choice].Action }
        Write-Host '지원하지 않는 선택입니다.' -ForegroundColor Yellow
    }
}

function Show-CpfDevHelp {
    Write-Host ''
    Write-Host '============================================================'
    Write-Host ' CPF 개발 명령 · 평소에는 run-local / build / verify-fast 세 개만 기억하세요'
    Write-Host '============================================================'
    Write-Host ' build         전체 Build + 정적 품질 Gate'
    Write-Host ' test          전체 Java Test'
    Write-Host ' verify-fast   빠른 정적 검증'
    Write-Host ' verify-full   Java25/Frontend/DB/Runtime 최대 로컬 검증'
    Write-Host ' run-local     권장 Local 통합 Runtime (1 JVM / 1 Port)'
    Write-Host ' run-batch     Batch 사용 시에만 별도 Runtime'
    Write-Host ' status        Local Runtime 상태'
    Write-Host ' stop          Local Runtime 종료'
    Write-Host ' modules       개발자가 선택할 Public Starter 보기'
    Write-Host ' resource      현재 환경 자원/메모리 정책 보기'
    Write-Host '============================================================'
    Write-Host " Profile       $ResourceProfile"
    Write-Host " Resource      gradle\cpf-runtime\$ResourceProfile.properties"
    Write-Host " Output        $OutputRoot"
    Write-Host '============================================================'
    Write-Host ' VS Code      Projects: apps/runtime/framework/starters/internal · Tasks: CPF 00~50'
    Write-Host ' 처음 실행    pwsh .\cpf-tools\build\tools\cpf-dev.ps1  (번호 메뉴)'
    Write-Host '============================================================'
    Write-Host ''
    Write-Host '예시'
    Write-Host '  pwsh .\cpf-tools\build\tools\cpf-dev.ps1 build'
    Write-Host '  pwsh .\cpf-tools\build\tools\cpf-dev.ps1 verify-full'
    Write-Host '  pwsh .\cpf-tools\build\tools\cpf-dev.ps1 run-local'
    Write-Host ''
}

function Invoke-CpfCommand([string] $Title, [scriptblock] $Command) {
    Write-Host ''
    Write-Host "=== $Title ==="
    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "$Title failed. exit=$LASTEXITCODE"
    }
}

if ([string]::IsNullOrWhiteSpace($Action)) {
    if ([Environment]::UserInteractive) {
        $Action = Read-CpfDevAction
        if ([string]::IsNullOrWhiteSpace($Action)) { return }
    } else {
        $Action = 'help'
    }
}

if ($Action -notin @('help','status','stop')) { Initialize-CpfJava25 }

$GradleCommon = @("-PcpfResourceProfile=$ResourceProfile", '--no-daemon', '--no-parallel')

switch ($Action) {
    'help' {
        Show-CpfDevHelp
    }
    'build' {
        Invoke-CpfCommand 'CPF BUILD' { & $Gradle @GradleCommon '--continue' 'cpfBuild' }
    }
    'test' {
        Invoke-CpfCommand 'CPF TEST' { & $Gradle @GradleCommon '--continue' 'cpfTest' }
    }
    'verify-fast' {
        Invoke-CpfCommand 'CPF VERIFY FAST' { & $Gradle @GradleCommon '--continue' 'cpfVerifyFast' }
    }
    'verify-full' {
        Invoke-CpfCommand 'CPF VERIFY FULL LOCAL' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File $Validation `
                -ResourceProfile $ResourceProfile -OutputRoot $OutputRoot -FullLocal
        }
    }
    'run-local' {
        Invoke-CpfCommand 'CPF RUN LOCAL' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $RuntimeTools 'start-cpf-local.ps1') `
                -ResourceProfile $ResourceProfile -Mode integrated
        }
    }
    'run-batch' {
        Invoke-CpfCommand 'CPF RUN BATCH' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $RuntimeTools 'start-cpf-local.ps1') `
                -ResourceProfile $ResourceProfile -Mode standard -BatchOnly
        }
    }
    'status' {
        Invoke-CpfCommand 'CPF RUNTIME STATUS' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $RuntimeTools 'status-cpf-local.ps1')
        }
    }
    'stop' {
        Invoke-CpfCommand 'CPF RUNTIME STOP' {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $RuntimeTools 'stop-cpf-local.ps1')
        }
    }
    'modules' {
        Invoke-CpfCommand 'CPF PUBLIC MODULES' { & $Gradle @GradleCommon 'cpfModules' }
    }
    'resource' {
        Invoke-CpfCommand 'CPF RESOURCE POLICY' { & $Gradle @GradleCommon 'cpfResourcePolicy' }
    }
}
