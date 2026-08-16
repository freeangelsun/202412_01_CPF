param(
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [string]$DockerRoot = "C:\dev\Docker"
)

$ErrorActionPreference = "Stop"
$cpfRoot = Join-Path $DockerRoot "CPF"
$envFile = Join-Path $cpfRoot "tool-images.env"
if (-not (Test-Path -LiteralPath $RepoRoot -PathType Container)) { throw "Repository가 없습니다: $RepoRoot" }
if (-not (Test-Path -LiteralPath $envFile -PathType Leaf)) { throw "Tool Image 환경파일이 없습니다: $envFile" }

$imageLine = Get-Content $envFile | Where-Object { $_ -like "TRIVY_IMAGE=*" } | Select-Object -First 1
if (-not $imageLine) { throw "TRIVY_IMAGE가 없습니다." }
$image = $imageLine.Substring($imageLine.IndexOf("=") + 1)
$output = Join-Path $cpfRoot "output\trivy"
$cache = Join-Path $cpfRoot "cache\trivy"
New-Item -ItemType Directory -Force -Path $output, $cache | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$json = Join-Path $output "trivy-fs-$timestamp.json"
$sbom = Join-Path $output "trivy-sbom-$timestamp.cdx.json"

& docker run --rm `
    --mount "type=bind,source=$RepoRoot,target=/workspace,readonly" `
    --mount "type=bind,source=$cache,target=/root/.cache/trivy" `
    --mount "type=bind,source=$output,target=/output" `
    $image fs --scanners vuln,misconfig,secret --format json --output "/output/$(Split-Path -Leaf $json)" /workspace
if ($LASTEXITCODE -ne 0) { throw "Trivy File System 점검 실패(exit=$LASTEXITCODE)" }

& docker run --rm `
    --mount "type=bind,source=$RepoRoot,target=/workspace,readonly" `
    --mount "type=bind,source=$cache,target=/root/.cache/trivy" `
    --mount "type=bind,source=$output,target=/output" `
    $image fs --format cyclonedx --output "/output/$(Split-Path -Leaf $sbom)" /workspace
if ($LASTEXITCODE -ne 0) { throw "Trivy SBOM 생성 실패(exit=$LASTEXITCODE)" }

Write-Host "Trivy 결과: $json"
Write-Host "SBOM 결과: $sbom"
