param([Parameter(ValueFromRemainingArguments=$true)][string[]]$ArgsFromCli)
$ErrorActionPreference='Stop'
$Root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path

function Show-CpfHelp {
@'
CPF 명령
  cpf bootstrap [옵션]                     로컬 개발 환경을 자동 준비합니다.
  cpf stop                                 CPF 로컬 Runtime을 종료합니다.
  cpf reset --confirm                      CPF 로컬 개발 환경을 안전하게 초기화합니다.
  cpf build                                전체 Build를 실행합니다.
  cpf test                                 전체 Test를 실행합니다.
  cpf verify                               Open Git Workspace 계약을 검증합니다.
  cpf domain new <name> <SYSTEM_CODE>      신규 Business Domain을 생성합니다.
  cpf domain sync                          Generated Domain을 정본과 동기화합니다.
  cpf library create <name>                고객사 공통 JAR 작업공간을 생성합니다.
  cpf library attach <name> <domain>       선택한 Domain에만 고객사 공통 JAR를 연결합니다.
  cpf library sync                         고객사 공통 JAR 연결 정보를 재동기화합니다.
  cpf library verify <name>                고객사 공통 JAR 구조와 경계를 검증합니다.
  cpf help                                 이 도움말을 표시합니다.

고객사 공통 Library는 모든 Domain에 자동 주입하지 않습니다. 필요한 Domain에서만 attach 하여
의존성 경계를 명확하게 유지합니다.
'@
}
function Invoke-CpfNative([string]$Script,[string[]]$ForwardArgs) {
  & (Join-Path $PSScriptRoot $Script) @ForwardArgs
  if($LASTEXITCODE -ne 0){ throw "CPF COMMAND FAILED: $Script exit=$LASTEXITCODE" }
}
function Invoke-CpfGenerator([string[]]$ForwardArgs) {
  & java (Join-Path $PSScriptRoot 'CpfGeneratorLauncher.java') --root $Root @ForwardArgs
  if($LASTEXITCODE -ne 0){ throw "CPF COMMAND FAILED: generator exit=$LASTEXITCODE" }
}
function cpfBuild([string[]]$ForwardArgs){ Invoke-CpfNative 'cpf-build.ps1' $ForwardArgs }
function cpfTest([string[]]$ForwardArgs){ Invoke-CpfNative 'cpf-test.ps1' $ForwardArgs }
function cpfVerify([string[]]$ForwardArgs){ & (Join-Path $Root 'tools/verify-open-git-workspace.ps1') @ForwardArgs; if($LASTEXITCODE -ne 0){ throw "CPF COMMAND FAILED: verify exit=$LASTEXITCODE" } }

$argv=@($ArgsFromCli)
$command=if($argv.Count){$argv[0]}else{'help'}
$rest=if($argv.Count -gt 1){@($argv[1..($argv.Count-1)])}else{@()}
switch($command){
  {$_ -in @('help','-h','--help')} { Show-CpfHelp; return }
  'bootstrap' { Invoke-CpfNative 'cpf-bootstrap.ps1' $rest }
  'stop' { Invoke-CpfNative 'cpf-stop.ps1' $rest }
  'reset' { Invoke-CpfNative 'cpf-reset.ps1' $rest }
  'build' { cpfBuild $rest }
  'test' { cpfTest $rest }
  'verify' { cpfVerify $rest }
  'domain' {
    if($rest.Count -lt 1){ throw 'CPF COMMAND FAILED: domain 하위 명령이 필요합니다.' }
    $sub=$rest[0]; $tail=if($rest.Count -gt 1){@($rest[1..($rest.Count-1)])}else{@()}
    if($sub -eq 'new'){
      if($tail.Count -lt 2){ throw '사용법: cpf domain new <name> <SYSTEM_CODE>' }
      Invoke-CpfGenerator @('domain','create','--name',$tail[0],'--system-code',$tail[1]) + @($tail | Select-Object -Skip 2)
    } elseif($sub -eq 'sync'){ Invoke-CpfGenerator @('domain','sync') + $tail }
    else { throw "CPF COMMAND FAILED: 지원하지 않는 domain 명령: $sub" }
  }
  'library' {
    if($rest.Count -lt 1){ throw 'CPF COMMAND FAILED: library 하위 명령이 필요합니다.' }
    $sub=$rest[0]; $tail=if($rest.Count -gt 1){@($rest[1..($rest.Count-1)])}else{@()}
    if($sub -eq 'create'){
      if($tail.Count -lt 1){ throw '사용법: cpf library create <name>' }
      Invoke-CpfGenerator @('library','create','--name',$tail[0]) + @($tail | Select-Object -Skip 1)
    } elseif($sub -eq 'attach'){
      if($tail.Count -lt 2){ throw '사용법: cpf library attach <name> <domain>' }
      Invoke-CpfGenerator @('library','attach','--name',$tail[0],'--domain',$tail[1]) + @($tail | Select-Object -Skip 2)
    } elseif($sub -eq 'sync'){ Invoke-CpfGenerator @('library','sync') + $tail }
    elseif($sub -eq 'verify'){
      if($tail.Count -lt 1){ throw '사용법: cpf library verify <name>' }
      Invoke-CpfGenerator @('library','verify','--name',$tail[0]) + @($tail | Select-Object -Skip 1)
    } else { throw "CPF COMMAND FAILED: 지원하지 않는 library 명령: $sub" }
  }
  default { throw "CPF COMMAND FAILED: 지원하지 않는 명령: $command" }
}
Write-Host 'CPF Command Result: PASS'
if($command -eq 'bootstrap'){ Write-Host 'CPF LOCAL DEVELOPMENT READY' }
