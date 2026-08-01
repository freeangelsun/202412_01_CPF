param(
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [string]$DockerRoot = "C:\dev\Docker"
)

$ErrorActionPreference = "Stop"
$cpfRoot = Join-Path $DockerRoot "CPF"
$envFile = Join-Path $cpfRoot "tool-images.env"
if (-not (Test-Path -LiteralPath $RepoRoot -PathType Container)) { throw "Repository가 없습니다: $RepoRoot" }
if (-not (Test-Path -LiteralPath $envFile -PathType Leaf)) { throw "Tool Image 환경파일이 없습니다: $envFile" }

$imageLine = Get-Content $envFile | Where-Object { $_ -like "FULL_TOOLCHAIN_IMAGE=*" } | Select-Object -First 1
if (-not $imageLine) { throw "FULL_TOOLCHAIN_IMAGE가 없습니다." }
$image = $imageLine.Substring($imageLine.IndexOf("=") + 1)

& docker run --rm -it `
    --mount "type=bind,source=$RepoRoot,target=/workspace/cpf" `
    --mount "type=bind,source=//var/run/docker.sock,target=/var/run/docker.sock" `
    --workdir /workspace/cpf `
    $image
if ($LASTEXITCODE -ne 0) { throw "통합 Toolchain 실행 실패(exit=$LASTEXITCODE)" }
