param(
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [string]$DockerRoot = "C:\dev\Docker",
    [ValidateSet("requirements", "analyze")]
    [string]$Action = "requirements"
)

$ErrorActionPreference = "Stop"
$cpfRoot = Join-Path $DockerRoot "CPF"
$envFile = Join-Path $cpfRoot "tool-images.env"
if (-not (Test-Path -LiteralPath $RepoRoot -PathType Container)) { throw "Repository가 없습니다: $RepoRoot" }
if (-not (Test-Path -LiteralPath $envFile -PathType Leaf)) { throw "Tool Image 환경파일이 없습니다: $envFile" }

$imageLine = Get-Content $envFile | Where-Object { $_ -like "ORT_IMAGE=*" } | Select-Object -First 1
if (-not $imageLine) { throw "ORT_IMAGE가 없습니다." }
$image = $imageLine.Substring($imageLine.IndexOf("=") + 1)
$output = Join-Path $cpfRoot "output\ort"
New-Item -ItemType Directory -Force -Path $output | Out-Null

if ($Action -eq "requirements") {
    & docker run --rm $image requirements
    if ($LASTEXITCODE -ne 0) { throw "ORT requirements 실패(exit=$LASTEXITCODE)" }
    return
}

$tempRoot = Join-Path $env:TEMP ("cpf-ort-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null
try {
    $null = robocopy $RepoRoot $tempRoot /E /NFL /NDL /NJH /NJS /NP `
        /XD .git .gradle build node_modules dist coverage test-results playwright-report `
        /XF *.log *.tmp *.bak *.orig *.rej *.pyc
    if ($LASTEXITCODE -gt 7) { throw "ORT 임시 Source 복사 실패(exit=$LASTEXITCODE)" }

    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    & docker run --rm `
        --mount "type=bind,source=$tempRoot,target=/project" `
        --mount "type=bind,source=$output,target=/results" `
        $image analyze -i /project -o "/results/analyzer-result-$timestamp.yml"
    if ($LASTEXITCODE -ne 0) { throw "ORT analyze 실패(exit=$LASTEXITCODE)" }
} finally {
    Remove-Item -LiteralPath $tempRoot -Recurse -Force -ErrorAction SilentlyContinue
}
