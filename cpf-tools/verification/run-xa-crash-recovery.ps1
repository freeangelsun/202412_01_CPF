param([Parameter(Mandatory=$true)][string]$RepositoryRoot)
$ErrorActionPreference='Stop'
Set-Location $RepositoryRoot
$required=@('CPF_XA_VENDOR1','CPF_XA_URL1','CPF_XA_USER1','CPF_XA_PASSWORD1','CPF_XA_VENDOR2','CPF_XA_URL2','CPF_XA_USER2','CPF_XA_PASSWORD2','CPF_XA_TRANSACTION_ID','CPF_XA_INSERT_SQL','CPF_XA_COUNT_SQL')
foreach($name in $required){ if([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))){ throw "Missing environment variable: $name" } }
$props=@(
 "-Dcpf.xa.harness.vendor1=$env:CPF_XA_VENDOR1", "-Dcpf.xa.harness.url1=$env:CPF_XA_URL1", "-Dcpf.xa.harness.user1=$env:CPF_XA_USER1", "-Dcpf.xa.harness.password1=$env:CPF_XA_PASSWORD1",
 "-Dcpf.xa.harness.vendor2=$env:CPF_XA_VENDOR2", "-Dcpf.xa.harness.url2=$env:CPF_XA_URL2", "-Dcpf.xa.harness.user2=$env:CPF_XA_USER2", "-Dcpf.xa.harness.password2=$env:CPF_XA_PASSWORD2",
 "-Dcpf.xa.harness.transaction-id=$env:CPF_XA_TRANSACTION_ID", "-Dcpf.xa.harness.insert-sql=$env:CPF_XA_INSERT_SQL", "-Dcpf.xa.harness.count-sql=$env:CPF_XA_COUNT_SQL"
)
& .\gradlew @props :cpf-starter-data-transaction-jta:runXaCrashHarness -PcpfXaHarnessMode=prepare-kill
if($LASTEXITCODE -ne 73){ throw "prepare-kill expected process exit 73 but was $LASTEXITCODE" }
& .\gradlew @props :cpf-starter-data-transaction-jta:runXaCrashHarness -PcpfXaHarnessMode=recover
if($LASTEXITCODE -ne 0){ throw "recovery harness failed: $LASTEXITCODE" }
