param(
    [Parameter(Mandatory=$true)][string]$RequirementId,
    [Parameter(Mandatory=$true)][string]$PayloadJson,
    [string]$BaseUrl = $env:CPF_EDUCATION_BASE_URL,
    [int]$TimeoutSeconds = 600
)
$ErrorActionPreference = 'Stop'
# 교육 예제는 Endpoint를 Source에 고정하지 않고 환경 설정으로만 주입받습니다.
if ([string]::IsNullOrWhiteSpace($BaseUrl)) { throw 'CPF_EDUCATION_BASE_URL is required.' }
if ($RequirementId -notmatch '^EDU-(DEV|BAT|ADM|OPS|BZA|GWY)-\d{2}$') { throw "Unsupported Education requirement id: $RequirementId" }
try { $payload = $PayloadJson | ConvertFrom-Json } catch { throw 'PayloadJson must be valid JSON.' }
$uri = $BaseUrl.TrimEnd('/') + '/api/education/edu-capabilities/' + $RequirementId + '/executions'
# 실제 Education Controller의 실행 API를 호출하므로 문서/Mock 전용 EntryPoint가 아닙니다.
$response = Invoke-RestMethod -Method Post -Uri $uri -ContentType 'application/json' -Body ($payload | ConvertTo-Json -Depth 20 -Compress) -TimeoutSec $TimeoutSeconds
$response | ConvertTo-Json -Depth 20
