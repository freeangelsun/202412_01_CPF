param(
    [Parameter(Mandatory=$true)][string]$Root,
    [Parameter(Mandatory=$true)][string]$ResultDir,
    [ValidateSet('Host','Docker')][string]$ClientAdapter='Docker',
    [string]$MariaDbContainer='cpf-mariadb',
    [string]$DatabaseName='cpfDB',
    [string]$DbVendor='mariadb',
    [string]$DbResourceRoot=$env:CPF_DB_RESOURCE_ROOT,
    [string]$DbUser='cpf_app',
    [string]$DbPassword=$env:CPF_CORE_DB_RUNTIME_PASSWORD,
    [string]$DbRootPassword=$env:CPF_DB_ROOT_PASSWORD,
    [int]$ControlPlanePort=8180,
    [int]$SchedulerPort=8181,
    [int]$Worker1Port=8182,
    [int]$CenterCutPort=8183,
    [int]$AgentPort=8184,
    [int]$Worker2Port=8282,
    [int]$DomainPort=8285,
    [int]$ResponseLossProxyPort=8286,
    [int]$ResponseLossProxyClientReadTimeoutSeconds=15,
    [int]$ResponseLossProxyUpstreamConnectTimeoutSeconds=15,
    [int]$ResponseLossProxyUpstreamReadTimeoutSeconds=30,
    [string]$DomainSystemCode='',
    [int]$LeaseSeconds=10,
    [int]$TimeoutSeconds=180
)
# Full Runtime child-process UTF-8 contract. Keep the emitted byte stream UTF-8 even when pwsh is redirected.
$CpfUtf8ConsoleEncoding = [Text.UTF8Encoding]::new($false)
try {
    [Console]::InputEncoding = $CpfUtf8ConsoleEncoding
    [Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
    $OutputEncoding = $CpfUtf8ConsoleEncoding
    $global:OutputEncoding = $CpfUtf8ConsoleEncoding
} catch { }
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'

$ErrorActionPreference='Stop'

# Child process가 새 Windows process로 분리되어도 UTF-8 계약을 잃지 않도록 고정합니다.
$CpfUtf8ChildJavaOptions = '-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8'
if ([string]::IsNullOrWhiteSpace($env:JAVA_TOOL_OPTIONS)) {
    $env:JAVA_TOOL_OPTIONS = $CpfUtf8ChildJavaOptions
} elseif ($env:JAVA_TOOL_OPTIONS -notmatch '(?:^|\s)-Dfile\.encoding=UTF-8(?:\s|$)') {
    $env:JAVA_TOOL_OPTIONS = ($env:JAVA_TOOL_OPTIONS.Trim() + ' ' + $CpfUtf8ChildJavaOptions)
}
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'
$env:PGCLIENTENCODING = 'UTF8'
$env:NLS_LANG = '.AL32UTF8'
$ProgressPreference='SilentlyContinue'
Set-StrictMode -Version Latest
$root=(Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($DbPassword)) { $DbPassword = $env:CPF_DB_APP_PASSWORD }
if ([string]::IsNullOrWhiteSpace($DbPassword)) { throw 'CPF DB runtime password is required via CPF_CORE_DB_RUNTIME_PASSWORD or CPF_DB_APP_PASSWORD.' }
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
if ([string]::IsNullOrWhiteSpace($DbResourceRoot)) { $DbResourceRoot = Join-Path $Root ("cpf-tools\db\vendor\" + $DbVendor) }
$DbResourceRoot = [IO.Path]::GetFullPath($DbResourceRoot)
if (-not (Test-Path -LiteralPath (Join-Path $DbResourceRoot 'pack.json') -PathType Leaf)) {
    throw "중앙 DB Vendor Pack을 찾을 수 없습니다. vendor=$DbVendor root=$DbResourceRoot"
}
$script:DbResourceRootResolved = $DbResourceRoot
$started=Get-Date
$runId=Get-Date -Format 'yyyyMMddHHmmssfff'
$processes=@()
$script:AgentArtifactStateMacKeyBase64=[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$script:AgentCommandLedgerRoot=Join-Path $ResultDir 'agent-command-ledger'
$script:TransactionFileLogRoot=Join-Path $ResultDir 'transaction-file-logs'
[IO.Directory]::CreateDirectory($script:TransactionFileLogRoot)|Out-Null
$result=[ordered]@{status='RUNNING';runId=$runId;startedAt=$started.ToString('o');kafkaUsed=$false;roles=[ordered]@{};checks=@();logs=@();transactionLineage=[ordered]@{status='NOT_EXECUTED';fileLogRoot=$script:TransactionFileLogRoot}}

function Step([string]$name,[string]$status,[string]$detail='') {
    $stamp=(Get-Date).ToString('HH:mm:ss')
    Write-Host "[$stamp] [$status] $name $detail"
    $result.checks += [ordered]@{name=$name;status=$status;detail=$detail;at=(Get-Date).ToString('o')}
}
function Require([bool]$condition,[string]$message) { if(-not $condition){throw $message} }
function Json([object]$value){ $value | ConvertTo-Json -Depth 12 -Compress }
function Get-SqlJsonRows([string]$sql) {
    $raw=Invoke-Sql $sql
    if([string]::IsNullOrWhiteSpace($raw)){return @()}
    $rows=[Collections.Generic.List[object]]::new()
    foreach($line in @($raw -split '\r?\n' | Where-Object {-not [string]::IsNullOrWhiteSpace($_)})){
        try {$rows.Add(($line|ConvertFrom-Json -Depth 30))} catch {throw "SQL JSON evidence row is invalid: $line"}
    }
    return @($rows)
}
function Read-CpfLiveLogText([string]$Path) {
    # 살아 있는 Runtime 이 지금도 쓰고 있는 증적 파일을 읽는다.
    # Windows 에서 File Log Owner(CpfFileLogWriter)는 rolling 파일 핸들을 연 채 유지하는데
    # [IO.File]::ReadAllText/ReadAllLines/ReadLines 는 FileShare.Read 로만 열기 때문에
    # 쓰기 핸들이 살아 있으면 "다른 프로세스가 사용 중" 으로 던진다. 실제로 Batch Two-Worker
    # 검증이 업무 단정(sampleRows/idempotencyRows/serviceCallSuccess)을 전부 통과한 뒤
    # 이 지점에서만 실패했다. 그러므로 공유 모드를 명시해서 연다.
    # 파일 부재/권한 오류는 그대로 예외로 남긴다 — '잠김' 만 허용하고 증적 부재는 숨기지 않는다.
    $stream=$null;$reader=$null
    try {
        $stream=[IO.FileStream]::new($Path,[IO.FileMode]::Open,[IO.FileAccess]::Read,
            ([IO.FileShare]::ReadWrite -bor [IO.FileShare]::Delete))
        $reader=[IO.StreamReader]::new($stream,[Text.UTF8Encoding]::new($false),$true)
        return $reader.ReadToEnd()
    } finally {
        if($null -ne $reader){$reader.Dispose()} elseif($null -ne $stream){$stream.Dispose()}
    }
}
function Read-CpfLiveLogLines([string]$Path) {
    return @((Read-CpfLiveLogText $Path) -split "`r`n|`n|`r")
}
function ConvertTo-FileLogEvidenceRow([object]$Entry) {
    # 구조화 파일 로그의 이벤트 키는 이벤트 종류마다 다르다. 예를 들어 ONLINE_TRANSACTION 에는
    # `callerSystemCode` 는 있어도 `sourceSystemCode`/`messageCode`/`errorCode` 는 없다.
    # StrictMode 에서 없는 속성을 그대로 읽으면 "property cannot be found" 로 던져 검증이
    # 실제 결함이 아닌 이유로 실패한다(실제로 그렇게 한 사이클을 소모했다).
    # 그러므로 존재하는 키만 읽고 없으면 빈 문자열로 둔다.
    $record=$Entry.record
    $names=@($record.PSObject.Properties.Name)
    $read={ param([string]$Name) if($names -contains $Name){[string]$record.$Name}else{''} }
    return [ordered]@{
        path=$Entry.path
        transactionId=(& $read 'transactionId')
        traceId=(& $read 'traceId')
        segmentId=(& $read 'segmentId')
        parentSegmentId=(& $read 'parentSegmentId')
        instanceId=(& $read 'instanceId')
        # 이 이벤트의 '호출한 쪽' 정본 키는 callerSystemCode 다.
        callerSystemCode=(& $read 'callerSystemCode')
        systemCode=(& $read 'systemCode')
        targetSystemCode=(& $read 'targetSystemCode')
        operationId=(& $read 'operationId')
        status=(& $read 'status')
        responseCode=(& $read 'responseCode')
        messageCode=(& $read 'messageCode')
        # 신규 File contract가 errorCode를 쓰며, 이전 event는 failureCode만 남겼다. Evidence에는
        # 둘을 분리해 남겨 migration/consumer drift도 판정할 수 있게 한다.
        errorCode=(& $read 'errorCode')
        failureCode=(& $read 'failureCode')
        httpStatus=(& $read 'httpStatus')
        eventType=(& $read 'eventType')
    }
}
function Get-TransactionFileLogRows([string]$transactionId) {
    $rows=[Collections.Generic.List[object]]::new()
    foreach($file in @(Get-ChildItem -LiteralPath $script:TransactionFileLogRoot -Recurse -File -ErrorAction SilentlyContinue)){
        foreach($line in @(Read-CpfLiveLogLines $file.FullName | Where-Object {$_ -like "*$transactionId*"})){
            try {$record=$line|ConvertFrom-Json -Depth 30} catch {continue}
            if([string]$record.transactionId -eq $transactionId){
                $rows.Add([pscustomobject]@{path=$file.FullName;record=$record})
            }
        }
    }
    return @($rows)
}
function Resolve-Java {
    $java=Get-Command java -ErrorAction SilentlyContinue
    if(-not $java){throw 'java command not found'}
    $version=& $java.Source -version 2>&1 | Out-String
    if($version -notmatch 'version "25[\.]'){throw "Java 25 is required: $version"}
    return $java.Source
}
function Resolve-Docker {
    $d=Get-Command docker -ErrorAction SilentlyContinue
    if(-not $d){throw 'Docker CLI is required for MariaDB runtime qualification'}
    return $d.Source
}
function Resolve-Jar([string[]]$patterns,[string]$label){
    foreach($pattern in $patterns){
        $candidates=@(Get-ChildItem -Path (Join-Path $root $pattern) -File -ErrorAction SilentlyContinue | Where-Object {$_.Name -notmatch '-plain\.jar$'} | Sort-Object LastWriteTime -Descending)
        if($candidates.Count -gt 0){return $candidates[0].FullName}
    }
    throw "Boot JAR not found for $label. Build the full Java25 package first."
}
function Resolve-GeneratedDomainTarget {
    # Generated Domain의 물리 Project 이름은 이 Runtime에 고정하지 않는다. Canonical
    # developer contract inventory가 현재 존재하는 독립 Online Domain을 단일 source로 제공한다.
    . (Join-Path $root 'cpf-tools/generator/tools/generated-domain-common.ps1')
    $candidates=@(
        Get-CpfGeneratedDomainInventory -Root $root |
        Where-Object {
            [bool]$_.exists -and [bool]$_.onlineEnabled -and
            [string]$_.generationMode -eq 'generated' -and
            [bool]$_.databaseEnabled -and
            @($_.domainDependencies).Count -eq 0 -and
            @($_.forbiddenPermanentMetadata).Count -eq 0
        }
    )
    if(-not [string]::IsNullOrWhiteSpace($DomainSystemCode)){
        $expected=$DomainSystemCode.Trim().ToUpperInvariant()
        $candidates=@($candidates | Where-Object { [string]$_.systemCode -ceq $expected })
    }
    if($candidates.Count -eq 0){
        $selector=if([string]::IsNullOrWhiteSpace($DomainSystemCode)){'an independent generated Online Domain'}else{"systemCode=$DomainSystemCode"}
        throw "Batch→Domain runtime target is unavailable: $selector"
    }
    # Several valid generated Domains may coexist. The default remains deterministic without
    # coupling the harness to a customer/project name; callers can request a system code explicitly.
    return @($candidates | Sort-Object @{Expression={[string]$_.systemCode};Ascending=$true}, @{Expression={[string]$_.projectName};Ascending=$true})[0]
}
function Resolve-GeneratedDomainCreateOperation([object]$domain) {
    # Batch→Domain recovery must exercise an actual generated business transaction.  The generic
    # /ping operation is intentionally outside the business Operation Manifest/ADM policy catalog
    # and is therefore unsuitable as a transaction/retry/duplicate-effect proof.
    Require ([bool]$domain.sampleTransaction) ("Generated Domain lacks the standard sample transaction contract: " + [string]$domain.projectName)
    $manifest=Join-Path $root (([string]$domain.projectName) + '\\online\\build\\generated\\cpf-operation-manifest\\META-INF\\cpf\\business-operation-manifest.json')
    Require (Test-Path -LiteralPath $manifest -PathType Leaf) "Generated Domain business operation manifest is missing: $manifest"
    try { $document=Get-Content -LiteralPath $manifest -Raw | ConvertFrom-Json -Depth 32 }
    catch { throw "Generated Domain business operation manifest is invalid: $manifest :: $($_.Exception.Message)" }
    $prefix=[regex]::Escape(([string]$domain.systemCode).Trim().ToUpperInvariant() + '_')
    $candidates=@($document.operations | Where-Object {
        [string]$_.httpMethod -ceq 'POST' -and [string]$_.operationId -match "^${prefix}SAMPLE_TX_CREATE$"
    } | Sort-Object @{Expression={[string]$_.operationId};Ascending=$true})
    Require ($candidates.Count -eq 1) ("Generated Domain requires exactly one manifest-backed Sample Create operation: " + $manifest)
    return [pscustomobject]@{operationId=[string]$candidates[0].operationId;apiPath=[string]$candidates[0].apiPath;manifest=$manifest}
}
function Invoke-Sql([string]$sql,[switch]$RootUser){
    $docker=Resolve-Docker
    $password=if($RootUser){$DbRootPassword}else{$DbPassword}
    $user=if($RootUser){'root'}else{$DbUser}
    if([string]::IsNullOrWhiteSpace($password)){throw "DB password unavailable for $user"}
    $args=@('exec','-i',$MariaDbContainer,'mariadb',"-u$user","-p$password",'-N','-B','-e',$sql)
    $out=& $docker @args 2>&1
    if($LASTEXITCODE -ne 0){throw "SQL failed: $($out -join ' ')"}
    return (($out -join "`n").Trim())
}
function Wait-Http([string]$uri,[int]$seconds=$TimeoutSeconds){
    $until=(Get-Date).AddSeconds($seconds)
    do {
        try { $r=Invoke-WebRequest -UseBasicParsing -TimeoutSec 4 -Uri $uri; if($r.StatusCode -ge 200 -and $r.StatusCode -lt 500){return $r} } catch {}
        Start-Sleep -Milliseconds 700
    } while((Get-Date) -lt $until)
    throw "HTTP readiness timeout: $uri"
}
function Wait-Text([string]$path,[string]$text,[int]$seconds=$TimeoutSeconds){
    $until=(Get-Date).AddSeconds($seconds)
    do {
        if((Test-Path -LiteralPath $path) -and ((Get-Content -LiteralPath $path -Raw) -like "*$text*")){return}
        Start-Sleep -Milliseconds 100
    } while((Get-Date) -lt $until)
    throw "Timed out waiting for verifier proxy state '$text': $path"
}
function Start-ResponseLossProxy {
    $python=Get-Command python -ErrorAction SilentlyContinue
    if(-not $python){throw 'python command is required for the response-loss verifier proxy'}
    $proxyScript=Join-Path $root 'cpf-tools\runtime\tools\delay_http_response_proxy.py'
    if(-not (Test-Path -LiteralPath $proxyScript -PathType Leaf)){throw "Response-loss verifier proxy is missing: $proxyScript"}
    $proxyLog=Join-Path $ResultDir 'response-loss-proxy.log'
    $proxyErr=Join-Path $ResultDir 'response-loss-proxy.err.log'
    $arguments=@($proxyScript,'--listen-port',$ResponseLossProxyPort,'--upstream-port',$DomainPort,
        '--delay-seconds',[string]($LeaseSeconds+5),'--accept-timeout-seconds',[string]$TimeoutSeconds,
        '--client-read-timeout-seconds',[string]$ResponseLossProxyClientReadTimeoutSeconds,
        '--upstream-connect-timeout-seconds',[string]$ResponseLossProxyUpstreamConnectTimeoutSeconds,
        '--upstream-read-timeout-seconds',[string]$ResponseLossProxyUpstreamReadTimeoutSeconds)
    $proxy=Start-Process -FilePath $python.Source -ArgumentList $arguments -RedirectStandardOutput $proxyLog -RedirectStandardError $proxyErr -PassThru
    $script:processes += [pscustomobject]@{Name='response-loss-proxy';Process=$proxy;Log=$proxyLog;ErrorLog=$proxyErr}
    Wait-Text $proxyLog 'READY'
    return [pscustomobject]@{Process=$proxy;Log=$proxyLog;ErrorLog=$proxyErr}
}
$script:TargetSystemCode=''
# CpfNetworkEndpointPolicy 는 loopback 대상 호출을 무조건 차단한다(SSRF 가드, 플래그로 완화 불가).
# 검증 topology 가 정책을 따르도록 이 Host 의 비-loopback IPv4 를 Runtime Endpoint 주소로 쓴다.
# 값을 하드코딩하지 않고 실제 인터페이스에서 찾는다.
$script:RuntimeHostAddress = (
    [System.Net.Dns]::GetHostAddresses([System.Net.Dns]::GetHostName()) |
    Where-Object { $_.AddressFamily -eq 'InterNetwork' -and -not [System.Net.IPAddress]::IsLoopback($_) } |
    Select-Object -First 1 -ExpandProperty IPAddressToString
)
if ([string]::IsNullOrWhiteSpace($script:RuntimeHostAddress)) {
    throw 'CPF Network Policy 가 loopback 을 금지하므로 비-loopback IPv4 주소가 필요합니다.'
}
function Start-Role([string]$name,[string]$jar,[int]$port,[hashtable]$extra,[string]$domainDatasourceSystemCode=''){
    $log=Join-Path $ResultDir "$name.log"
    $err=Join-Path $ResultDir "$name.err.log"
    $instance="bat-$name-$runId"
    # instanceId는 JVM 시스템 속성으로만 읽히므로(-D), Spring 인자만으로는 적용되지 않는다.
    $args=@("-Dcpf.runtime.instance-id=$instance",'-jar',$jar,"--server.port=$port",'--spring.batch.job.enabled=false',
      '--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.enabled=true',
      "--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.url=jdbc:mariadb://127.0.0.1:3306/$DatabaseName",
      "--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.username=$DbUser",
      '--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.driver-class-name=org.mariadb.jdbc.Driver',
      "--cpf.runtime.instance-id=$instance","--cpf.was-id=$instance","--cpf.db.vendor=$DbVendor",
      "--cpf.db.resource-root=$script:DbResourceRootResolved",
      "--cpf.batch.control.base-url=http://127.0.0.1:$ControlPlanePort")
    # Domain Call 은 caller 가 binding 을 선언해야 한다. AUTO 는 LOCAL/REMOTE 선택이지
    # serviceId 발견이 아니어서, 선언이 없으면 CpfDomainClientRouter 가
    # CPF-DOMAIN-BINDING-MISSING 으로 거절한다(정본 선례: cpf-external application-*.yml).
    # Domain 이름을 하드코딩하지 않고 Discovery 로 찾은 systemCode 를 그대로 쓴다.
    if(-not [string]::IsNullOrWhiteSpace($script:TargetSystemCode)){
        $args += "--cpf.integration.domain-call.bindings.$($script:TargetSystemCode).mode=AUTO"
        $args += "--cpf.integration.domain-call.bindings.$($script:TargetSystemCode).service-id=$($script:TargetSystemCode)"
        # 검증 Runtime 은 loopback http Domain 을 호출한다. CpfNetworkEndpointPolicy 는 기본이
        # requireTls=true, allowedPorts=443/8443/9443 이라 명시 선언 없이는 거절된다.
        # 정본 선언 키(cpf.services.<id>)로 이 검증 topology 의 정책만 좁혀서 허용한다.
        $args += "--cpf.services.$($script:TargetSystemCode).base-url=http://$($script:RuntimeHostAddress):$DomainPort"
        $args += "--cpf.services.$($script:TargetSystemCode).require-tls=false"
        $args += "--cpf.services.$($script:TargetSystemCode).allow-private=true"
        $args += "--cpf.services.$($script:TargetSystemCode).allowed-ports[0]=$DomainPort"
        $args += "--cpf.services.$($script:TargetSystemCode).allowed-ports[1]=$ResponseLossProxyPort"
    }
    if($name -eq 'domain'){
        # /_cpf/domain/** is a machine-only boundary.  The generated Domain must trust the
        # verifier's actual Worker peer as BAT through the canonical, explicit peer mapping;
        # Header6 alone must never authenticate an internal caller.  Do not use a broad CIDR or
        # a loopback shortcut: RuntimeHostAddress is the concrete non-loopback source address
        # used by the Worker-to-Domain route above.
        $args += "--cpf.web.internal-peer-identities=$($script:RuntimeHostAddress)=BAT"
        # Runtime Agent 는 자기 instance 를 OPS_SERVICE_INSTANCE 에 등록하는데, server.address 가
        # wildcard 면 base_url 을 **hostname** 으로 만든다(CpfRuntimeControlAgentAutoConfiguration
        # .resolveRuntimeBaseUrl). 그러면 Service Call Engine 이 그 instance 로 라우팅할 때
        # 정본 네트워크 정책이 "Hostname은 설정된 allowDns 정책 상에 포함되지 않습니다" 로 거절하고,
        # segment 가 IllegalArgumentException/TECHNICAL_FAILURE 로 남는다. 실제 실행에서 두 번의
        # BAT→Domain attempt 가 이렇게 실패한 뒤 endpoint baseUrl 로 우회 성공해, 성공한 segment 에
        # selected_instance_id 가 비는 현상이 나왔다.
        # 정책을 느슨하게(allow-dns=true) 만들지 않고, 이 topology 가 실제로 쓰는 주소를 그대로
        # 등록하게 한다. server.address 는 건드리지 않으므로 loopback health probe 도 그대로 동작한다.
        $args += "--cpf.runtime.control.agent.runtime-base-url=http://$($script:RuntimeHostAddress):$DomainPort"
    }
    foreach($k in $extra.Keys){$args += "--$k=$($extra[$k])"}
    # JDBC passwords must never enter a Java command line, process listing, or harness log.
    # Spring's canonical Batch runtime contract reads the password from this child-only environment.
    $childEnvironment=@{
        # Batch executable shared runtime binding and the direct role-data-source binding are
        # both supported Consumer paths. Keep their secret value environment-only in either case.
        CPF_PLATFORM_DB_PASSWORD=$DbPassword
        CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_PASSWORD=$DbPassword
        # Center-Cut Parameter Snapshot 은 AES-256-GCM 으로 봉인되며 Key 는 외부 Secret 으로만
        # 공급된다. 없으면 실행 생성이 500 으로 실패한다. 검증용 Key 는 실행마다 새로 만들고
        # 저장소에 남기지 않으며, JDBC 비밀번호와 동일하게 자식 환경에만 전달한다.
        CPF_CENTER_CUT_PARAMETER_KEY=$script:CenterCutParameterKey
        # Keep verifier-produced structured transaction logs out of the repository's default
        # logs/ root.  This is the one canonical CPF_LOG_ROOT consumed by the File Log owner.
        CPF_LOG_ROOT=$script:TransactionFileLogRoot
    }
    if(-not [string]::IsNullOrWhiteSpace($domainDatasourceSystemCode)){
        # Every generated Online Domain owns a separate Spring datasource binding named from its
        # canonical system code. Keep the verifier credential environment-only and never rely on a
        # particular generated project being present.
        $domainDatasourcePrefix=$domainDatasourceSystemCode.Trim().ToUpperInvariant()
        $domainUrl="jdbc:mariadb://127.0.0.1:3306/$DatabaseName"
        $childEnvironment["${domainDatasourcePrefix}_DATASOURCE_URL"]=$domainUrl
        $childEnvironment["${domainDatasourcePrefix}_DATASOURCE_USERNAME"]=$DbUser
        $childEnvironment["${domainDatasourcePrefix}_DATASOURCE_PASSWORD"]=$DbPassword
        $childEnvironment["${domainDatasourcePrefix}_DATASOURCE_DRIVER"]='org.mariadb.jdbc.Driver'
    }
    if($name -eq 'agent'){
        # The Agent must receive a valid run-scoped integrity key even when no artifact install is
        # invoked. The key is intentionally environment-only; command-line exposure defeats it.
        $childEnvironment.CPF_AGENT_ARTIFACT_STATE_MAC_KEY_BASE64=$script:AgentArtifactStateMacKeyBase64
        $childEnvironment.CPF_AGENT_COMMAND_LEDGER_ROOT=$script:AgentCommandLedgerRoot
    }
    if($name -eq 'domain'){
        # The generated Domain owns its operation policy seed.  This verifier supplies the one
        # concrete upstream system that performs the real Batch→Domain call; it does not use ALL,
        # mutate the policy tables, or weaken the receiver's fail-closed authorization boundary.
        $childEnvironment.CPF_OPERATION_POLICY_SEED_ALLOWED_CALLERS='BAT'
    }
    $p=Start-Process -FilePath $script:Java -ArgumentList $args -Environment $childEnvironment -PassThru -RedirectStandardOutput $log -RedirectStandardError $err
    $script:processes += [pscustomobject]@{Name=$name;Process=$p;Log=$log;Err=$err;Port=$port;Instance=$instance}
    $result.roles[$name]=[ordered]@{pid=$p.Id;port=$port;instanceId=$instance;jar=$jar;log=$log;errorLog=$err}
    Wait-Http "http://127.0.0.1:$port/actuator/health" | Out-Null
    Step "$name health" 'PASS' "pid=$($p.Id) port=$port"
    return $p
}
# BAT 인가 계약: 일반 명령의 body actor 는 인증된 Operator 와 같아야 하고, 승인 명령은
# 요청자와 승인자가 서로 달라야 한다. 두 값을 한 곳에서 소유해 header/body 드리프트를 막는다.
$BatOperatorId='cpf-harness-approver'
$BatApprovalRequesterId='cpf-harness-requester'
# Canonical Center-Cut Job id. 등록과 실행 요청이 같은 값을 쓰도록 한 곳에서 소유한다.
$CenterCutJobId='CPF_BAT_CENTER_CUT_JOB'
# 실행 범위 Center-Cut Parameter Key(256-bit). 고정 Secret 을 저장소에 두지 않기 위해
# 매 실행마다 난수로 생성한다.
$script:CenterCutParameterKey=[Convert]::ToBase64String((
    [System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32)))
function BatHeaders([bool]$approved=$false,[string]$targetOperationId='cpf-bat-runtime-qualification'){
    # BAT 전용 헤더만 보내면 CPF 공통 Header Filter 가 EXTERNAL_HEADER_REQUIRED(ECPF900002)로
    # 거부한다. 다른 smoke harness 와 같은 CPF 표준 거래 헤더를 함께 실어야 한다.
    # transactionId 는 canonical 34자 계약(yyyyMMddHHmmssfff + systemCode + caller + sequence)이다.
    $sequence=Get-Random -Minimum 1 -Maximum 9999999
    $transactionId="{0:yyyyMMddHHmmssfff}BATbatwrkr{1:0000000}" -f (Get-Date), $sequence
    # Canonical 6(X-Transaction-Id + System lineage 5)은 외부 Channel 호출의 필수 계약이다.
    # X-Original-System-Code 는 X-Transaction-Id 에 인코딩된 발행자와 일치해야 하고(BAT),
    # X-System-Code / X-Target-System-Code 는 수신 Runtime 의 System Code 와 같아야 한다.
    $h=@{
        'X-Cpf-Bat-Caller-Service'='ADM'
        'X-Cpf-Bat-Caller-Instance-Id'="harness-$runId"
        'X-Cpf-Bat-Operator-Id'=$BatOperatorId
        'X-Request-Type'='INQUIRY'
        'X-Transaction-Id'=$transactionId
        'X-Original-System-Code'='BAT'
        'X-System-Code'='BAT'
        'X-Caller-System-Code'='ADM'
        'X-Target-System-Code'='BAT'
        'X-Target-Operation-Id'=$targetOperationId
        'X-Trace-Id'=$transactionId
        'X-User-Id'='runtime-smoke'
        'X-Client-Id'='cpf-smoke'
        'X-Client-Version'='1.0.0'
        'X-Caller-Service'='smoke-bat-two-worker-runtime'
    }
    if($approved){$h['X-Cpf-Bat-Approval-Request-Id']="APR-$runId";$h['X-Cpf-Bat-Approval-Requester-Id']=$BatApprovalRequesterId}
    return $h
}
function Invoke-Json([string]$method,[string]$uri,[object]$body=$null,[bool]$approved=$false){
    $params=@{Method=$method;Uri=$uri;Headers=(BatHeaders $approved);UseBasicParsing=$true;TimeoutSec=30}
    if($null -ne $body){$params.ContentType='application/json';$params.Body=(Json $body)}
    return Invoke-RestMethod @params
}
function Snapshot-Claims {
    $sql="SELECT CONCAT(center_cut_item_id,'|',runner_id,'|',claim_status,'|',fencing_token,'|',attempt_no,'|',takeover_count) FROM ${DatabaseName}.BAT_CENTER_CUT_CLAIM ORDER BY center_cut_item_id;"
    return @(Invoke-Sql $sql)
}
function RuntimeCount([string]$pattern){
    # Runtime Agent registration is authoritative. BAT_RUNTIME_INSTANCE is not the Runtime
    # Agent projection and is legitimately empty before a Batch job starts.
    $v=Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.OPS_RUNTIME_INSTANCE_STATE WHERE instance_id LIKE '$pattern' AND runtime_role='WORKER';"
    return [int]$v
}

try {
    $script:Java=Resolve-Java
    $script:Docker=Resolve-Docker
    $running=(& $script:Docker inspect --format '{{.State.Running}}' $MariaDbContainer 2>$null | Out-String).Trim()
    Require ($running -eq 'true') "MariaDB container is not running: $MariaDbContainer"
    Step 'prerequisite Java25/MariaDB' 'PASS'

    # Fail closed when the retired Kafka Remote Execution DB surface survived migration.
    $remoteTable=[int](Invoke-Sql "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DatabaseName' AND table_name='BAT_REMOTE_MESSAGE_LEDGER';")
    Require ($remoteTable -eq 0) 'BAT_REMOTE_MESSAGE_LEDGER still exists; V140/current schema removal is incomplete'
    Step 'retired remote ledger absent' 'PASS'

    $jars=[ordered]@{
      control=(Resolve-Jar @('cpf-batch/control-plane/build/libs/*.jar') 'control-plane')
      scheduler=(Resolve-Jar @('cpf-batch/scheduler/build/libs/*.jar') 'scheduler')
      worker=(Resolve-Jar @('cpf-batch/worker/build/libs/*.jar') 'worker')
      centercut=(Resolve-Jar @('cpf-batch/center-cut/build/libs/*.jar') 'center-cut')
      agent=(Resolve-Jar @('cpf-batch/agent/build/libs/*.jar') 'agent')
      domain=$null
    }
    $targetDomain=Resolve-GeneratedDomainTarget
    $script:TargetSystemCode=[string]$targetDomain.systemCode
    $runtimeOperation=Resolve-GeneratedDomainCreateOperation $targetDomain
    $jars.domain=Resolve-Jar @("$([string]$targetDomain.projectName)/online/build/libs/*.jar") ("generated Domain " + [string]$targetDomain.systemCode)
    # Runtime Agent defaults serviceId to its canonical systemCode and endpointCode to
    # <systemCode>_API. The verifier must seed those exact identities, not a run-scoped alias.
    $serviceId=[string]$targetDomain.systemCode
    # Register the selected Domain before its Runtime Agent starts. Runtime registration is
    # intentionally fail-closed, so reversing this order makes an otherwise healthy Domain fail.
    # Endpoint 는 두 계약이 각각 요구하므로 둘 다 등록해야 한다.
    #  - <SYS>_API : Domain Runtime Agent 등록 계약. 없으면 Domain 이
    #                "Runtime Agent endpoint가 중앙 Registry에 등록되어 있지 않습니다" 로 기동 실패한다.
    #  - operationId : CpfHttpDomainRemoteTransport 가 .endpointCode(operationId) 로 찾는 Domain Call
    #                  Endpoint. 없으면 CpfEndpointResolver 가 "CPF 서비스 endpoint가 없습니다" 로 거절한다.
    # Domain 이름이나 임시 ping route를 하드코딩하지 않고, Generated Manifest가 소유한 실제
    # 업무 Create operationId를 endpoint identity로 사용한다.
    $runtimeOperationId=[string]$runtimeOperation.operationId
    $runtimeAgentEndpointCode="$([string]$targetDomain.systemCode)_API"
    $runtimeEndpointCode=$runtimeOperationId
    Invoke-Sql "INSERT INTO ${DatabaseName}.OPS_SERVICE(service_id,service_name,service_type,owner_module_code,description,use_yn,created_by,updated_by) VALUES('$serviceId','$([string]$targetDomain.projectName) Harness','INTERNAL','$([string]$targetDomain.systemCode)','Batch runtime generated Domain target','Y','HARNESS','HARNESS') ON DUPLICATE KEY UPDATE service_name=VALUES(service_name),service_type=VALUES(service_type),owner_module_code=VALUES(owner_module_code),description=VALUES(description),use_yn='Y',updated_by='HARNESS';" | Out-Null
    Invoke-Sql "INSERT INTO ${DatabaseName}.OPS_SERVICE_ENDPOINT(endpoint_code,service_id,endpoint_name,endpoint_type,base_url,context_path,default_timeout_ms,default_retry_count,use_yn,created_by,updated_by) VALUES('$runtimeEndpointCode','$serviceId','$([string]$targetDomain.systemCode) Runtime API','HTTP','http://$($script:RuntimeHostAddress):$DomainPort','/',$(($LeaseSeconds+15)*1000),0,'Y','HARNESS','HARNESS') ON DUPLICATE KEY UPDATE service_id=VALUES(service_id),endpoint_name=VALUES(endpoint_name),endpoint_type=VALUES(endpoint_type),base_url=VALUES(base_url),context_path=VALUES(context_path),default_timeout_ms=VALUES(default_timeout_ms),default_retry_count=VALUES(default_retry_count),use_yn='Y',updated_by='HARNESS';" | Out-Null
    Invoke-Sql "INSERT INTO ${DatabaseName}.OPS_SERVICE_ENDPOINT(endpoint_code,service_id,endpoint_name,endpoint_type,base_url,context_path,default_timeout_ms,default_retry_count,use_yn,created_by,updated_by) VALUES('$runtimeAgentEndpointCode','$serviceId','$([string]$targetDomain.systemCode) Runtime API','HTTP','http://$($script:RuntimeHostAddress):$DomainPort','/',$(($LeaseSeconds+15)*1000),0,'Y','HARNESS','HARNESS') ON DUPLICATE KEY UPDATE service_id=VALUES(service_id),endpoint_name=VALUES(endpoint_name),endpoint_type=VALUES(endpoint_type),base_url=VALUES(base_url),context_path=VALUES(context_path),default_timeout_ms=VALUES(default_timeout_ms),default_retry_count=VALUES(default_retry_count),use_yn='Y',updated_by='HARNESS';" | Out-Null
    Step 'Generated Domain service registry pre-registration' 'PASS' "systemCode=$([string]$targetDomain.systemCode) serviceId=$serviceId operationId=$runtimeOperationId"

    # Center-Cut Job 정의는 Canonical Seed Model 에서 sample seed(58_runtime_sample_seed.sql)로
    # 분류되어 있고, Verifier 소유 Runtime DB 는 product seed 만 적재한다. Domain 서비스 등록과
    # 동일하게 이 시나리오의 전제를 Verifier 가 직접 만든다. Canonical 정의를 바꾸거나 sample
    # 행을 product seed 로 승격시키지 않는다.
    # BAT_CENTER_CUT_JOB.batch_job_id 는 BAT_JOB(job_id) 를 참조한다. Canonical sample seed 와
    # 같은 순서로 BAT_JOB 을 먼저 만든다. INSERT IGNORE 는 FK 위반까지 조용히 삼켜 원인을
    # 감추므로 쓰지 않는다.
    Invoke-Sql "INSERT INTO ${DatabaseName}.BAT_JOB(job_id,job_name,job_type,description,restartable_yn,use_yn,created_by,updated_by) VALUES('$CenterCutJobId','CPF BAT Center-Cut qualification Job','TASKLET','Runtime qualification prerequisite seeded by the verifier','Y','Y','SYSTEM','SYSTEM') ON DUPLICATE KEY UPDATE use_yn=VALUES(use_yn),updated_by=VALUES(updated_by);"
    Invoke-Sql "INSERT INTO ${DatabaseName}.BAT_CENTER_CUT_JOB(center_cut_job_id,batch_job_id,center_cut_job_name,provider_key,handler_key,chunk_size,retry_limit,use_yn,description,created_by,updated_by) VALUES('$CenterCutJobId','$CenterCutJobId','CPF Domain Invocation Center-Cut Job','cpfParameterSnapshotCenterCutTargetProvider','cpfDomainInvocationCenterCutHandler',10,3,'Y','Runtime qualification prerequisite seeded by the verifier','SYSTEM','SYSTEM') ON DUPLICATE KEY UPDATE use_yn=VALUES(use_yn),provider_key=VALUES(provider_key),handler_key=VALUES(handler_key),updated_by=VALUES(updated_by);"
    $centerCutJobRows=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_CENTER_CUT_JOB WHERE center_cut_job_id='$CenterCutJobId';")
    Require ($centerCutJobRows -eq 1) "Center-Cut Job prerequisite was not registered: $CenterCutJobId"
    Step 'Center-Cut Job pre-registration' 'PASS' "centerCutJobId=$CenterCutJobId rows=$centerCutJobRows"

    Start-Role 'control-plane' $jars.control $ControlPlanePort @{} | Out-Null
    Start-Role 'scheduler' $jars.scheduler $SchedulerPort @{} | Out-Null
    Start-Role 'worker-1' $jars.worker $Worker1Port @{'cpf.batch.worker.center-cut.lease-seconds'=$LeaseSeconds;'cpf.batch.worker.center-cut.heartbeat-ms'=1000} | Out-Null
    Start-Role 'center-cut' $jars.centercut $CenterCutPort @{} | Out-Null
    Start-Role 'agent' $jars.agent $AgentPort @{} | Out-Null
    Start-Role 'worker-2' $jars.worker $Worker2Port @{'cpf.batch.worker.center-cut.lease-seconds'=$LeaseSeconds;'cpf.batch.worker.center-cut.heartbeat-ms'=1000} | Out-Null
    Start-Role 'domain' $jars.domain $DomainPort @{} ([string]$targetDomain.systemCode) | Out-Null
    Step 'five Batch runtimes + second worker + Generated Domain' 'PASS' "systemCode=$([string]$targetDomain.systemCode)"
    # Domain startup synchronizes its generated business manifest and seeds the explicit BAT
    # caller policy.  Read the actual catalog/policy rows before the scenario; never insert a
    # verifier-owned policy row, because that would conceal a product registration defect.
    $operationRegistration=Invoke-Sql "SELECT CONCAT_WS('|',c.operation_id,c.discovery_status,p.enabled_yn,p.all_callers_yn,COALESCE(cp.caller_system_code,'')) FROM ${DatabaseName}.OPS_OPERATION_CATALOG c JOIN ${DatabaseName}.OPS_OPERATION_POLICY p ON p.operation_id=c.operation_id LEFT JOIN ${DatabaseName}.OPS_OPERATION_CALLER_POLICY cp ON cp.operation_id=c.operation_id AND cp.caller_system_code='BAT' WHERE c.operation_id='$runtimeOperationId';"
    Require ($operationRegistration -match ("^" + [regex]::Escape($runtimeOperationId) + "\\|ACTIVE\\|Y\\|N\\|BAT$")) "Generated Domain operation catalog/policy registration is not explicit for BAT: $operationRegistration"
    Step 'Generated Domain manifest catalog and BAT caller policy' 'PASS' "operationId=$runtimeOperationId registration=$operationRegistration"

    $workerRows=RuntimeCount "bat-worker-%-$runId"
    Require ($workerRows -ge 2) "Expected >=2 worker registry rows, actual=$workerRows"
    Step 'multi-instance worker registry' 'PASS' "workers=$workerRows"

    # Drain / resume is a general Worker lifecycle and must stay Kafka-independent.
    Invoke-WebRequest -UseBasicParsing -Method Post -Headers (BatHeaders) -Uri "http://127.0.0.1:$Worker1Port/internal/v1/worker/drain" | Out-Null
    Start-Sleep -Seconds 2
    $drainState=Invoke-RestMethod -Headers (BatHeaders) -Uri "http://127.0.0.1:$Worker1Port/actuator/health"
    Step 'worker drain' 'PASS'
    Invoke-WebRequest -UseBasicParsing -Method Post -Headers (BatHeaders) -Uri "http://127.0.0.1:$Worker1Port/internal/v1/worker/resume" | Out-Null
    Step 'worker resume' 'PASS'

    # Route one real Domain call through a verifier-owned response-loss proxy.  The proxy forwards
    # the request unchanged and delays only the already-produced response; it never writes CPF DB
    # rows and cannot manufacture UNKNOWN_RESULT.
    $responseLossProxy=Start-ResponseLossProxy
    Invoke-Sql "UPDATE ${DatabaseName}.OPS_SERVICE_ENDPOINT SET base_url='http://$($script:RuntimeHostAddress):$ResponseLossProxyPort',updated_by='HARNESS' WHERE endpoint_code='$runtimeEndpointCode' AND service_id='$serviceId';" | Out-Null
    # Service Call Engine 은 선택된 instance 의 baseUrl 을 endpoint baseUrl 보다 **먼저** 쓴다
    # (CpfEndpointResolver: firstText(instance.baseUrl, endpoint.baseUrl)). 그래서 endpoint 만
    # 프록시로 바꾸면 instance 로 라우팅되는 호출이 프록시를 우회해 곧바로 성공하고,
    # 응답유실 시나리오 자체가 성립하지 않는다(실제로 그렇게 COMPLETED 로 끝나 kill 대상이 없었다).
    # 라우팅에 실제로 쓰이는 두 출처를 함께 프록시로 돌린다.
    Invoke-Sql "UPDATE ${DatabaseName}.OPS_SERVICE_INSTANCE SET base_url='http://$($script:RuntimeHostAddress):$ResponseLossProxyPort',updated_by='HARNESS' WHERE service_id='$serviceId';" | Out-Null
    Step 'response-loss proxy armed' 'PASS' "upstreamDomainPort=$DomainPort proxyPort=$ResponseLossProxyPort"

    $businessKey="BK-$runId"
    # The standard generated Sample Create command is a real DB transaction with an immutable
    # idempotency key.  The same payload crosses the response-loss/reconcile boundary so the
    # verifier can prove one business effect rather than merely counting successful HTTP calls.
    $sampleKey="S-$runId"
    $domainIdempotencyKey="IDEM-$runId"
    $parameters=[ordered]@{systemCode=[string]$targetDomain.systemCode;operationId=$runtimeOperationId;targets=@([ordered]@{businessKey=$businessKey;request=[ordered]@{sampleKey=$sampleKey;itemName="CPF Batch Runtime $runId";idempotencyKey=$domainIdempotencyKey}})}
    $create=[ordered]@{centerCutJobId=$CenterCutJobId;idempotencyKey="CC-$runId";parameters=$parameters;parameterSchemaVersion=1;tpsLimit=10;concurrencyLimit=2;requestedBy=$BatOperatorId;reason='Kafka-free Center-Cut Domain Invocation qualification';transactionId=$null;parentSegmentId=$null}
    $execution=Invoke-Json 'POST' "http://127.0.0.1:$ControlPlanePort/api/v1/batch/center-cut/executions" $create $false
    $executionId=[string]($execution.center_cut_execution_id ?? $execution.centerCutExecutionId ?? $execution.executionId)
    Require (-not [string]::IsNullOrWhiteSpace($executionId)) "Center-Cut create response lacks execution id: $(Json $execution)"
    Step 'Center-Cut execution create' 'PASS' "executionId=$executionId"

    # Target 재료화는 생성 API 와 별개로 비동기 진행된다. CenterCutExecutionService.nextState 는
    # target_complete_yn='Y' 일 때만 START 를 RUNNING 으로 보내고, 그 전에는 STARTING 을 남긴다.
    # 대기 없이 START 를 부르면 제품이 정상인데도 검증기가 STARTING 을 실패로 읽는다.
    $targetDeadline=(Get-Date).AddSeconds($TimeoutSeconds); $targetComplete=''
    do {
        Start-Sleep -Milliseconds 100
        $targetComplete=Invoke-Sql "SELECT target_complete_yn FROM ${DatabaseName}.BAT_CENTER_CUT_EXECUTION WHERE center_cut_execution_id='$executionId';"
        if($targetComplete -eq 'Y'){break}
    } while((Get-Date) -lt $targetDeadline)
    Require ($targetComplete -eq 'Y') "Center-Cut target materialization did not complete: target_complete_yn=$targetComplete executionId=$executionId"
    Step 'Center-Cut target materialization' 'PASS' "executionId=$executionId"

    # Creation only materializes the target set.  Running a Center-Cut execution is a separate
    # high-impact state transition and the Owner API deliberately requires requester/approver
    # separation.  Do not treat TARGET_READY as runnable: invoke the real approved START route
    # before waiting for a Worker claim, so this verifier exercises the same Consumer path as ADM.
    $startApproved=[ordered]@{
        requestedBy=$BatApprovalRequesterId
        approvedBy=$BatOperatorId
        reason='Runtime qualification approved Center-Cut start after target materialization'
    }
    $startedExecution=Invoke-Json 'POST' "http://127.0.0.1:$ControlPlanePort/api/v1/batch/center-cut/executions/$executionId/start" $startApproved $true
    $startedState=[string]($startedExecution.execution_state ?? $startedExecution.executionState)
    Require ($startedState -eq 'RUNNING') "Approved Center-Cut START did not enter RUNNING: state=$startedState executionId=$executionId"
    Step 'Center-Cut execution approved start' 'PASS' "executionId=$executionId state=$startedState"

    # The previous harness waited for COMPLETED before stopping a Worker, so its process kill never
    # affected an in-flight transaction. Wait specifically for Worker-1's live DB claim and for the
    # proxy's upstream-response marker, then terminate that actual claim owner.
    $deadline=(Get-Date).AddSeconds($TimeoutSeconds); $status=''; $claimOwner=''
    do {
        Start-Sleep -Milliseconds 100
        $row=Invoke-Sql "SELECT CONCAT(execution_state,'|',processed_count,'|',success_count,'|',failure_count,'|',unknown_count) FROM ${DatabaseName}.BAT_CENTER_CUT_EXECUTION WHERE center_cut_execution_id='$executionId';"
        $status=$row
        $claimOwner=Invoke-Sql "SELECT COALESCE(c.runner_id,'') FROM ${DatabaseName}.BAT_CENTER_CUT_CLAIM c JOIN ${DatabaseName}.BAT_CENTER_CUT_ITEM i ON i.center_cut_item_id=c.center_cut_item_id WHERE i.center_cut_execution_id='$executionId' AND c.claim_status IN ('CLAIMED','RUNNING') AND i.item_status='RUNNING' LIMIT 1;"
        # 두 Worker 중 어느 쪽이 claim 할지는 비결정적이다. worker-1 만 기다리면 영원히 만나지
        # 못할 수 있다. 실제 claim 소유자를 잡아 그 프로세스를 죽여야 "살아 있는 claim 소유자
        # 강제 종료 -> lease 만료 -> UNKNOWN -> reconcile -> 다른 Worker 인수" 시나리오가 성립한다.
        if($claimOwner -match '^bat-worker-[12]-' -and (Test-Path -LiteralPath $responseLossProxy.Log) -and ((Get-Content -LiteralPath $responseLossProxy.Log -Raw) -like '*UPSTREAM_RESPONSE_DELAY_STARTED*')){break}
    } while((Get-Date) -lt $deadline)
    $itemCount=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_CENTER_CUT_ITEM WHERE center_cut_execution_id='$executionId';")
    Require ($itemCount -ge 1) "Center-Cut provider did not materialize DB work item: $executionId"
    # 이 단정이 깨졌을 때 "왜 Worker-1 이 claim 을 못 잡았는가"를 로그만으로 판정할 수 있어야 한다.
    # RUN37 에서는 Item 이 곧바로 FAILED 로 확정됐는데 실패 사유가 어디에도 남지 않아 재현 없이는
    # 원인을 좁힐 수 없었다. Harness 는 행을 만들지 않고 읽기만 한다.
    if($claimOwner -notmatch '^bat-worker-[12]-'){
        # 진단 자체가 실패해도 본래 실패 원인을 덮지 않아야 한다. RUN38 에서 컬럼명을 잘못 적은
        # 진단 SQL 이 먼저 던지는 바람에 정작 claim 실패 사유를 다시 놓쳤다.
        # 컬럼은 정본 스키마(BAT_CENTER_CUT_ITEM.item_status / last_error_message,
        # BAT_CENTER_CUT_CLAIM.claim_status / runner_id) 기준이다.
        $itemDiagnostics='<unavailable>'
        $endpointDiagnostics='<unavailable>'
        try {
            $itemDiagnostics=Invoke-Sql "SELECT CONCAT_WS('|',i.item_status,i.retry_count,COALESCE(LEFT(i.last_error_message,300),''),COALESCE(c.claim_status,''),COALESCE(c.runner_id,'')) FROM ${DatabaseName}.BAT_CENTER_CUT_ITEM i LEFT JOIN ${DatabaseName}.BAT_CENTER_CUT_CLAIM c ON c.center_cut_item_id=i.center_cut_item_id WHERE i.center_cut_execution_id='$executionId';"
        } catch { $itemDiagnostics="<item diagnostics failed: $($_.Exception.Message)>" }
        try {
            $endpointDiagnostics=Invoke-Sql "SELECT CONCAT_WS('|',endpoint_code,base_url,COALESCE(context_path,'')) FROM ${DatabaseName}.OPS_SERVICE_ENDPOINT WHERE service_id='$serviceId';"
        } catch { $endpointDiagnostics="<endpoint diagnostics failed: $($_.Exception.Message)>" }
        $proxyObserved=if(Test-Path -LiteralPath $responseLossProxy.Log){(Get-Content -LiteralPath $responseLossProxy.Log -Raw).Trim()}else{'<no proxy log>'}
        Step 'Center-Cut claim diagnostics' 'INFO' "item=$itemDiagnostics endpoint=$endpointDiagnostics proxy=$proxyObserved"
        Require $false "어떤 Worker 도 종료 시점까지 live Center-Cut claim 을 보유하지 않았다: owner=$claimOwner state=$status item=$itemDiagnostics endpoint=$endpointDiagnostics proxy=$proxyObserved"
    }
    Require ((Get-Content -LiteralPath $responseLossProxy.Log -Raw) -like '*UPSTREAM_RESPONSE_DELAY_STARTED*') 'Response-loss proxy did not observe a real Domain response before Worker termination'
    # PowerShell 은 원소가 0~1 개인 배열을 return 시 풀어버린다. Set-StrictMode Latest 에서는
    # 스칼라의 .Count 접근이 오류이므로 호출부에서 다시 배열로 고정한다.
    $claimRows=@(Snapshot-Claims)
    Step 'DB work item/claim/fencing path' 'PASS' "items=$itemCount claims=$($claimRows.Count) state=$status"

    # Kill the actual DB claim owner after its real Domain response has been observed but before it
    # reaches the Worker. Any in-flight loss must become UNKNOWN, never blind retry.
    # claim 을 실제로 보유한 Worker 를 종료 대상으로 삼는다.
    $claimOwnerRole=if($claimOwner -like 'bat-worker-2-*'){'worker-2'}else{'worker-1'}
    $takeoverRole=if($claimOwnerRole -eq 'worker-2'){'worker-1'}else{'worker-2'}
    Step 'Center-Cut claim owner 확정' 'PASS' "owner=$claimOwner killTarget=$claimOwnerRole takeover=$takeoverRole"
    $worker1=($processes | Where-Object Name -eq $claimOwnerRole).Process
    Stop-Process -Id $worker1.Id -Force
    Step 'worker process kill' 'PASS' "pid=$($worker1.Id)"
    # The replacement Worker must call the actual Domain directly after approved reconcile.  This
    # restores the registry endpoint only; it never edits a Claim/Item/Execution row.
    Invoke-Sql "UPDATE ${DatabaseName}.OPS_SERVICE_ENDPOINT SET base_url='http://$($script:RuntimeHostAddress):$DomainPort',updated_by='HARNESS' WHERE endpoint_code='$runtimeEndpointCode' AND service_id='$serviceId';" | Out-Null
    # instance 라우팅도 함께 되돌린다. 하나만 복구하면 reconcile 이후의 정상 호출이
    # 여전히 프록시를 지나 재현 불가능한 지연을 다시 받는다.
    Invoke-Sql "UPDATE ${DatabaseName}.OPS_SERVICE_INSTANCE SET base_url='http://$($script:RuntimeHostAddress):$DomainPort',updated_by='HARNESS' WHERE service_id='$serviceId';" | Out-Null
    # Do not assume a fixed sleep is enough for the surviving worker's lease-expiry scan.  Observe
    # the actual Item/Claim/Execution transition until the configured Work Unit deadline, retaining
    # the last read-only SQL snapshot as failure evidence. This preserves the required physical
    # UNKNOWN_RESULT proof; it does not convert a missing recovery into a skip.
    $unknownDeadline=(Get-Date).AddSeconds($TimeoutSeconds)
    $unknownDiagnostics='<not-observed>'
    $beforeUnknown=0
    do {
        $unknownDiagnostics=Invoke-Sql "SELECT CONCAT_WS('|',i.item_status,COALESCE(c.claim_status,''),COALESCE(DATE_FORMAT(c.lease_until,'%Y-%m-%dT%H:%i:%s.%fZ'),''),COALESCE(CAST(TIMESTAMPDIFF(MICROSECOND,c.lease_until,CURRENT_TIMESTAMP(6)) AS CHAR),''),e.execution_state,e.unknown_count) FROM ${DatabaseName}.BAT_CENTER_CUT_ITEM i JOIN ${DatabaseName}.BAT_CENTER_CUT_EXECUTION e ON e.center_cut_execution_id=i.center_cut_execution_id LEFT JOIN ${DatabaseName}.BAT_CENTER_CUT_CLAIM c ON c.center_cut_item_id=i.center_cut_item_id WHERE i.center_cut_execution_id='$executionId';"
        $beforeUnknown=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_CENTER_CUT_ITEM WHERE center_cut_execution_id='$executionId' AND item_status='UNKNOWN_RESULT';")
        if($beforeUnknown -gt 0){break}
        Start-Sleep -Milliseconds 250
    } while((Get-Date) -lt $unknownDeadline)
    Require ($beforeUnknown -gt 0) "Worker termination did not produce UNKNOWN_RESULT from an in-flight claim: executionId=$executionId diagnostics=$unknownDiagnostics"
    Step 'expired claim to UNKNOWN_RESULT observation' 'PASS' "unknown=$beforeUnknown diagnostics=$unknownDiagnostics"
    $beforeFencing=[long](Invoke-Sql "SELECT COALESCE(MAX(fencing_token),0) FROM ${DatabaseName}.BAT_CENTER_CUT_CLAIM c JOIN ${DatabaseName}.BAT_CENTER_CUT_ITEM i ON i.center_cut_item_id=c.center_cut_item_id WHERE i.center_cut_execution_id='$executionId';")
    Start-Sleep -Seconds 2
    $afterUnknown=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_CENTER_CUT_ITEM WHERE center_cut_execution_id='$executionId' AND item_status='UNKNOWN_RESULT';")
    Require ($afterUnknown -eq $beforeUnknown) 'UNKNOWN_RESULT was blindly retried without explicit reconciliation'
    Step 'no blind retry for UNKNOWN' 'PASS' "unknown=$afterUnknown"

    Require ($afterUnknown -gt 0) "UNKNOWN_RESULT was not retained during the required no-blind-retry observation: executionId=$executionId diagnostics=$unknownDiagnostics"
    if($afterUnknown -gt 0){
        $approved=[ordered]@{requestedBy=$BatApprovalRequesterId;approvedBy=$BatOperatorId;reason='Runtime qualification explicit UNKNOWN reconciliation'}
        [void](Invoke-Json 'POST' "http://127.0.0.1:$ControlPlanePort/api/v1/batch/center-cut/executions/$executionId/reconcile-unknown" $approved $true)
        Start-Sleep -Seconds 3
        $afterFencing=[long](Invoke-Sql "SELECT COALESCE(MAX(fencing_token),0) FROM ${DatabaseName}.BAT_CENTER_CUT_CLAIM c JOIN ${DatabaseName}.BAT_CENTER_CUT_ITEM i ON i.center_cut_item_id=c.center_cut_item_id WHERE i.center_cut_execution_id='$executionId';")
        Require ($afterFencing -gt $beforeFencing) "Reconciled takeover did not advance fencing token: before=$beforeFencing after=$afterFencing"
        $completionDeadline=(Get-Date).AddSeconds($TimeoutSeconds)
        do {
            Start-Sleep -Milliseconds 100
            $completedState=Invoke-Sql "SELECT execution_state FROM ${DatabaseName}.BAT_CENTER_CUT_EXECUTION WHERE center_cut_execution_id='$executionId';"
        } while($completedState -notmatch 'COMPLETED|SUCCESS' -and (Get-Date) -lt $completionDeadline)
        Require ($completedState -match 'COMPLETED|SUCCESS') "Reconciled takeover did not complete through Worker-2: state=$completedState"
        Step 'explicit UNKNOWN reconcile + fencing takeover' 'PASS' "before=$beforeFencing after=$afterFencing"
    } else {
        throw 'UNKNOWN_RESULT was not produced from the required physical response-loss scenario'
    }

    # The initial request reaches the Domain before its response is deliberately lost.  A later
    # reconciled takeover may invoke the same idempotency key once more, but must never create a
    # second business row or a second idempotency ledger entry.  These are direct queries against
    # the generated Domain's canonical table-prefix contract, not a test-only shadow table.
    $domainPrefix=([string]$targetDomain.tablePrefix).Trim().ToUpperInvariant()
    Require ($domainPrefix -match '^[A-Z][A-Z0-9_]{0,30}$') "Generated Domain table prefix is invalid: $domainPrefix"
    # Generated table suffixes are canonical lower-case tokens.  Linux MariaDB
    # keeps identifier case, so normalize only the prefix owned by the Domain
    # contract and preserve the rendered template spelling for the suffix.
    $sampleRows=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.${domainPrefix}_sample_item WHERE sample_key='$sampleKey' AND idempotency_key='$domainIdempotencyKey';")
    $idempotencyRows=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.${domainPrefix}_sample_item_idem WHERE idempotency_key='$domainIdempotencyKey' AND operation_code='CREATE';")
    Require ($sampleRows -eq 1 -and $idempotencyRows -eq 1) "Generated Domain retry produced duplicate or missing business effect: sampleRows=$sampleRows idempotencyRows=$idempotencyRows"
    $callHistoryRows=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.OPS_SERVICE_CALL_HISTORY WHERE transaction_id IN (SELECT transaction_id FROM ${DatabaseName}.BAT_CENTER_CUT_ITEM WHERE center_cut_execution_id='$executionId') AND service_id='$serviceId' AND endpoint_code='$runtimeOperationId' AND call_status='SUCCESS';")
    Require ($callHistoryRows -eq 1) "Reconciled Batch→Domain success history must contain exactly one completed caller-side record: actual=$callHistoryRows"
    Step 'Generated Domain business effect and retry idempotency' 'PASS' "sampleRows=$sampleRows idempotencyRows=$idempotencyRows serviceCallSuccess=$callHistoryRows operationId=$runtimeOperationId"

    # Select the actual business transaction created by this Center-Cut execution, then correlate
    # its structured file log, DB summary, DB segment and transaction-lineage rows.  This is not a
    # count-only check: every record must retain the same transaction/trace/segment lineage.
    $businessTransactionId=[string](Invoke-Sql "SELECT transaction_id FROM ${DatabaseName}.BAT_CENTER_CUT_ITEM WHERE center_cut_execution_id='$executionId' ORDER BY center_cut_item_id LIMIT 1;")
    Require ($businessTransactionId -match '^[0-9A-Za-z_-]{20,128}$') "Center-Cut business transaction id is missing or invalid: $businessTransactionId"
    $summaryRows=@(Get-SqlJsonRows "SELECT JSON_OBJECT('transactionId',transaction_id,'traceId',TRACE_ID,'instanceId',INSTANCE_ID,'sourceSystemCode',CALLER_SYSTEM_CODE,'targetSystemCode',TARGET_SYSTEM_CODE,'operationId',TARGET_OPERATION_ID,'status',HTTP_STATUS,'responseCode',RESPONSE_CODE,'messageCode',MESSAGE_CODE,'errorCode',ERROR_CODE) FROM ${DatabaseName}.CPF_TRANSACTION_LOG WHERE transaction_id='$businessTransactionId' ORDER BY LOG_DATE,LOG_IDX;")
    Require ($summaryRows.Count -gt 0) "DB transaction summary is missing for transactionId=$businessTransactionId"
    Require (@($summaryRows|Where-Object {[string]$_.transactionId -ne $businessTransactionId}).Count -eq 0) 'DB transaction summary changed transactionId'
    $traceIds=@($summaryRows|ForEach-Object {[string]$_.traceId}|Where-Object {-not [string]::IsNullOrWhiteSpace($_)}|Sort-Object -Unique)
    Require ($traceIds.Count -eq 1) "DB transaction summary must have one non-empty traceId: $($traceIds -join ',')"
    $businessTraceId=$traceIds[0]
    $expectedDomainInstance=[string]$result.roles['domain'].instanceId
    Require (-not [string]::IsNullOrWhiteSpace($expectedDomainInstance)) 'Generated Domain runtime instanceId is missing from harness role evidence'
    $successfulSummaryRows=@($summaryRows|Where-Object {
        [string]$_.sourceSystemCode -eq 'BAT' -and
        [string]$_.targetSystemCode -eq [string]$targetDomain.systemCode -and
        [string]$_.operationId -eq $runtimeOperationId -and
        [string]$_.instanceId -eq $expectedDomainInstance -and
        [string]$_.status -eq '200' -and
        -not [string]::IsNullOrWhiteSpace([string]$_.responseCode) -and
        -not [string]::IsNullOrWhiteSpace([string]$_.messageCode) -and
        [string]::IsNullOrWhiteSpace([string]$_.errorCode)
    })
    Require ($successfulSummaryRows.Count -gt 0) 'DB transaction summary does not retain the exact BAT→Domain success identity/result/instance'
    $successfulSummary=$successfulSummaryRows[-1]
    # segment 의 업무 식별자는 `transaction_segment_id`(VARCHAR(120), UNIQUE)다.
    # `segment_id` 는 BIGINT AUTO_INCREMENT 대리 PK 이므로 값이 '1' 같은 순번이고, 다른 저장소와
    # 이어지지 않는다. 실제로 그 컬럼을 쓰면 lineage 의 segmentId(...-SEG-0002-XXXX)와 대조되어
    # 'lineage references an orphan segment' 로 오판한다.
    # 정본 근거: `CpfTransactionLineageRecord.fromSegment` 가 lineage.segment_id 에
    # `TransactionSegmentRecord.getTransactionSegmentId()` 를 넣고, 제품의 정본 상관 조회인
    # `CpfTransactionTimelineQueryFacade` 도 `transaction_segment_id AS segmentId` 로 읽는다.
    # 파일 로그의 segmentId 도 같은 업무 식별자다. 되돌리려면 세 소비자가 모두 대리 PK 로
    # 바뀌었다는 근거를 먼저 제시하라.
    $segmentRows=@(Get-SqlJsonRows "SELECT JSON_OBJECT('transactionId',transaction_id,'segmentId',transaction_segment_id,'parentSegmentId',parent_segment_id,'attempt',attempt_no,'depth',call_depth,'instanceId',selected_instance_id,'sourceDomain',source_module_code,'targetDomain',target_module_code,'operation',target_operation_id,'status',status,'responseCode',downstream_http_status,'errorCode',failure_code,'errorMessage',failure_message_masked,'resultState',result_state,'sequenceNo',sequence_no,'role',transaction_role,'direction',direction,'unknownResultId',unknown_result_id) FROM ${DatabaseName}.CPF_TRANSACTION_SEGMENT WHERE transaction_id='$businessTransactionId' ORDER BY sequence_no,segment_id;")
    Require ($segmentRows.Count -gt 0) "DB transaction segment is missing for transactionId=$businessTransactionId"
    Require (@($segmentRows|Where-Object {[string]$_.transactionId -ne $businessTransactionId}).Count -eq 0) 'DB transaction segment changed transactionId'
    $segmentIds=@($segmentRows|ForEach-Object {[string]$_.segmentId}|Where-Object {-not [string]::IsNullOrWhiteSpace($_)})
    Require ($segmentIds.Count -eq @($segmentIds|Sort-Object -Unique).Count) 'Duplicate DB transaction segment detected'
    $orphanParents=@($segmentRows|Where-Object {-not [string]::IsNullOrWhiteSpace([string]$_.parentSegmentId) -and [string]$_.parentSegmentId -notin $segmentIds})
    Require ($orphanParents.Count -eq 0) "Orphan DB transaction segment detected: $(@($orphanParents|ForEach-Object {$_.segmentId}) -join ',')"
    $successfulOutboundSegments=@($segmentRows|Where-Object {
        [string]$_.sourceDomain -eq 'BAT' -and
        [string]$_.targetDomain -eq [string]$targetDomain.systemCode -and
        [string]$_.operation -eq $runtimeOperationId -and
        [string]$_.instanceId -eq $expectedDomainInstance -and
        [string]$_.status -eq 'SUCCESS' -and
        [string]$_.responseCode -eq '200' -and
        [int]$_.attempt -ge 1
    })
    # failure_message/role/direction/sequence_no 를 함께 남긴다. segment 의 failure_code 는
    # TransactionSegmentService 가 `scope.fail(ex.getClass().getSimpleName(), ex.getMessage())` 로
    # 기록하는데, 실제 실행에서 `IllegalArgumentException` 두 건이 남았는데도 Runtime 로그에는
    # 스택이 없어 원인을 알 수 없었다. 메시지가 없으면 다음 실행에서도 같은 자리에서 막힌다.
    # system_code는 callerDomain이 아니라 이 projection을 저장한 현재 처리 System이다. 이름을
    # sourceDomain으로 잘못 내보내면 Consumer가 실제 발신자와 처리자를 혼동한다.
    $lineageRows=@(Get-SqlJsonRows "SELECT JSON_OBJECT('transactionId',transaction_id,'segmentId',segment_id,'parentSegmentId',parent_segment_id,'attempt',attempt_no,'traceId',trace_id,'instanceId',instance_id,'workerId',worker_id,'systemDomain',system_code,'targetDomain',target_system_code,'operation',operation_id,'status',lifecycle_state,'unknownResult',unknown_yn,'reconcileState',reconcile_state) FROM ${DatabaseName}.CPF_TRANSACTION_LINEAGE WHERE transaction_id='$businessTransactionId' ORDER BY occurred_at,lineage_id;")
    Require ($lineageRows.Count -gt 0) "DB transaction lineage is missing for transactionId=$businessTransactionId"
    # Persist the raw, normalized projections before applying the cross-store assertions.  A
    # failed qualification must remain diagnosable after the verifier-owned database is cleaned.
    $fileLogRows=@(Get-TransactionFileLogRows $businessTransactionId)
    $observedFileLogRows=@($fileLogRows|ForEach-Object {(ConvertTo-FileLogEvidenceRow $_)})
    $result.transactionLineage=[ordered]@{
        status='OBSERVED';transactionId=$businessTransactionId;traceId=$businessTraceId;operationId=$runtimeOperationId;expectedDomainInstance=$expectedDomainInstance
        summary=@($summaryRows);segments=@($segmentRows);lineage=@($lineageRows);fileLog=@($observedFileLogRows)
    }
    # 아래 strict cross-store qualification이 실패해도 Raw Summary/Segment/Lineage/File projection을
    # Evidence에 남긴다. 실패 원인을 count나 재현 추측으로 덮지 않고 다음 Source/Consumer 판정에
    # 그대로 사용하기 위한 순서다.
    Require ($successfulOutboundSegments.Count -gt 0) 'DB transaction segment does not retain the exact successful BAT→Domain selected-instance/attempt/result'
    # Lineage 의 정본 상관관계 키는 transaction_id + segment_id 다. trace_id 는 TRACE source
    # (CPF_TRANSACTION_LOG) 에서만 보장된다 — 다음이 정본 근거다.
    #  - `CPF_TRANSACTION_LINEAGE.trace_id` 는 스키마상 NULL 허용이고
    #    `CpfTransactionLineageRecord` javadoc 이 "trace 식별자, 미수집 시 null 가능" 이라고 못박는다.
    #  - `CpfTransactionLineageRecord.fromSegment` 는 traceId 에 null 을 넣는다. 원본
    #    `TransactionSegmentRecord` 에 traceId 필드가 없고 `CPF_TRANSACTION_SEGMENT` 에도
    #    trace_id 컬럼이 없기 때문이다. 즉 SEGMENT projection 은 trace 를 알 수 없다.
    #  - 제품의 정본 상관 조회인 `CpfTransactionTimelineQueryFacade` 도 SEGMENT/OUTBOX/DLQ/FILE
    #    source 에 대해 `NULL AS traceId` 를 그대로 내보내고 TRACE source 만 실제 값을 준다.
    # 따라서 "모든 lineage 행의 traceId 가 업무 traceId 와 같아야 한다" 는 단정은 제품 계약이
    # 아니라 검증기의 과다 요구였다. 실제로 그 단정 때문에 SEGMENT lineage(trace_id=NULL)에서
    # 'transactionId/traceId mismatch' 로 실패했다. transactionId 는 그대로 엄격히 보고,
    # traceId 는 **있을 때만** 일치를 요구한다(오염된 trace 는 계속 잡힌다).
    # 되돌리려면: SEGMENT source 가 trace_id 를 싣도록 제품 계약이 바뀌었다는 근거를 먼저 제시하라.
    Require (@($lineageRows|Where-Object {[string]$_.transactionId -ne $businessTransactionId}).Count -eq 0) 'DB transaction lineage transactionId mismatch'
    $conflictingTraceRows=@($lineageRows|Where-Object {
        -not [string]::IsNullOrWhiteSpace([string]$_.traceId) -and [string]$_.traceId -ne $businessTraceId })
    Require ($conflictingTraceRows.Count -eq 0) "DB transaction lineage carries a conflicting traceId: $(@($conflictingTraceRows|ForEach-Object {[string]$_.traceId}) -join ',')"
    Require (@($lineageRows|Where-Object {-not [string]::IsNullOrWhiteSpace([string]$_.segmentId) -and [string]$_.segmentId -notin $segmentIds}).Count -eq 0) 'DB transaction lineage references an orphan segment'
    $successfulOutboundLineage=@($lineageRows|Where-Object {
        [string]$_.systemDomain -eq 'BAT' -and
        [string]$_.targetDomain -eq [string]$targetDomain.systemCode -and
        [string]$_.operation -eq $runtimeOperationId -and
        [string]$_.instanceId -eq $expectedDomainInstance -and
        [string]$_.status -eq 'SUCCESS' -and
        [int]$_.attempt -ge 1
    })
    Require ($successfulOutboundLineage.Count -gt 0) 'DB transaction lineage does not retain the exact successful BAT→Domain selected instance'
    Require ($fileLogRows.Count -gt 0) "Structured File Log is missing for transactionId=$businessTransactionId"
    # File Log는 logging aspect가 생성한 durable transaction segment를 기록해야 한다. telemetry
    # span이나 단순 non-empty check만으로 통과시키면 DB와 File 사이의 orphan/instance drift를 놓친다.
    # response/message/error code도 DB Summary의 같은 성공 row와 직접 대조한다.
    $correlatedFileRows=@($fileLogRows|Where-Object {
        $record=$_.record
        [string]$record.traceId -eq $businessTraceId -and
        [string]$record.segmentId -in $segmentIds -and
        [string]$record.callerSystemCode -eq 'BAT' -and
        [string]$record.systemCode -eq [string]$targetDomain.systemCode -and
        [string]$record.targetSystemCode -eq [string]$targetDomain.systemCode -and
        [string]$record.operationId -eq $runtimeOperationId -and
        [string]$record.instanceId -eq $expectedDomainInstance -and
        [string]$record.status -eq 'SUCCESS' -and
        [string]$record.httpStatus -eq [string]$successfulSummary.status -and
        [string]$record.responseCode -eq [string]$successfulSummary.responseCode -and
        [string]$record.messageCode -eq [string]$successfulSummary.messageCode -and
        [string]::IsNullOrWhiteSpace([string]$record.errorCode)
    })
    Require ($correlatedFileRows.Count -gt 0) 'Structured File Log does not exactly correlate to DB Summary/Segment transaction/trace/result/instance'
    $result.transactionLineage=[ordered]@{
        status='PASS';transactionId=$businessTransactionId;traceId=$businessTraceId;operationId=$runtimeOperationId;expectedDomainInstance=$expectedDomainInstance
        summary=@($summaryRows);segments=@($segmentRows);lineage=@($lineageRows)
        fileLog=@($correlatedFileRows|ForEach-Object {(ConvertTo-FileLogEvidenceRow $_)})
    }
    Step 'Batch→Domain File/DB transaction lineage' 'PASS' "transactionId=$businessTransactionId traceId=$businessTraceId summaries=$($summaryRows.Count) segments=$($segmentRows.Count) lineage=$($lineageRows.Count) fileRows=$($correlatedFileRows.Count)"

    # Strong static runtime assertion: no Batch Kafka provider or retired ledger has been recreated.
    $remoteTableAfter=[int](Invoke-Sql "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DatabaseName' AND table_name='BAT_REMOTE_MESSAGE_LEDGER';")
    Require ($remoteTableAfter -eq 0) 'Retired Batch Remote Kafka ledger reappeared during runtime'
    Step 'post-runtime remote Kafka surface absence' 'PASS'

    $result.status='PASS'
} catch {
    $result.status='FAIL'; $result.error=$_.Exception.Message
    Step 'runtime qualification' 'FAIL' $_.Exception.Message
} finally {
    foreach($entry in @($processes | Sort-Object {$_.Name -eq 'worker-1'})){
        try { if(-not $entry.Process.HasExited){Stop-Process -Id $entry.Process.Id -Force -ErrorAction SilentlyContinue} } catch {}
    }
    $result.completedAt=(Get-Date).ToString('o');$result.durationSeconds=[math]::Round(((Get-Date)-$started).TotalSeconds,2)
    $out=Join-Path $ResultDir 'batch-kafka-free-runtime-result.json'
    $result | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $out -Encoding utf8
    Write-Host "RESULT=$out"
    Write-Host "STATUS=$($result.status)"
}
if($result.status -ne 'PASS'){exit 1}
