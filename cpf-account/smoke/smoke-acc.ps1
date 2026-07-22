param(
    [string] $BaseUrl = "http://localhost:8082",
    [int] $TimeoutSec = 20
)

$ErrorActionPreference = "Stop"
$uri = "$BaseUrl/api/v1/=sample&page=0&size=20&sortBy=created_at&sortDirection=DESC"
$response = Invoke-WebRequest -Method Get -Uri $uri -TimeoutSec $TimeoutSec -UseBasicParsing
if ([int] $response.StatusCode -lt 200 -or [int] $response.StatusCode -ge 300) {
    throw "Account smoke failed. status=$($response.StatusCode)"
}
Write-Host "Account smoke passed. uri=$uri"