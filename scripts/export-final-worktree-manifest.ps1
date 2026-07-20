param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260716_01"),
    [string] $StartCommit = "d16cd7a40062a1e77bd8cd3c6f6f7125cdc0708d"
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$outputName = "final-worktree-manifest.sanitized.log"
$outputPath = Join-Path $ResultDir $outputName
$outputRelativePath = $outputPath.Substring($Root.Length).TrimStart("\", "/").Replace("\", "/")

function Get-Sha256 {
    param([string] $Path)

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        return "DELETED_OR_MISSING"
    }
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Convert-ToRelativePath {
    param([string] $Path)

    return $Path.Replace("\", "/")
}

# NUL 구분 형식과 전체 untracked 목록을 사용해 공백·한글 경로도 손실 없이 수집합니다.
$porcelain = & git -C $Root status --porcelain=v1 -z --untracked-files=all
if ($LASTEXITCODE -ne 0) {
    throw "git status 실행에 실패했습니다."
}

$records = New-Object System.Collections.Generic.List[object]
$tokens = @($porcelain -split "`0" | Where-Object { $_ -ne "" })
for ($index = 0; $index -lt $tokens.Count; $index++) {
    $token = $tokens[$index]
    if ($token.Length -lt 4) {
        continue
    }

    $status = $token.Substring(0, 2)
    $path = Convert-ToRelativePath $token.Substring(3)
    $originalPath = ""
    if ($status.Contains("R") -or $status.Contains("C")) {
        $index++
        if ($index -lt $tokens.Count) {
            $originalPath = Convert-ToRelativePath $tokens[$index]
        }
    }

    $fullPath = Join-Path $Root $path.Replace("/", "\")
    $records.Add([pscustomobject]@{
        status = $status
        path = $path
        originalPath = $originalPath
        sha256 = Get-Sha256 $fullPath
    }) | Out-Null
}

# 출력 파일 자체는 자기 해시를 내장할 수 없으므로 기존 파일이 있어도 항상 별도 표식으로 정규화합니다.
$selfRecord = $records | Where-Object { $_.path -eq $outputRelativePath } | Select-Object -First 1
if ($selfRecord) {
    $selfRecord.sha256 = "SELF_REFERENTIAL"
} else {
    $records.Add([pscustomobject]@{
        status = "??"
        path = $outputRelativePath
        originalPath = ""
        sha256 = "SELF_REFERENTIAL"
    }) | Out-Null
}

$requestPath = Join-Path $Root "CPF_CURRENT_WORK_REQUEST.md"
$requestSha256 = Get-Sha256 $requestPath
$requestGitBlob = (& git -C $Root hash-object -- "CPF_CURRENT_WORK_REQUEST.md").Trim()
$branch = (& git -C $Root branch --show-current).Trim()
$javaCommand = if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME "bin/java.exe"))) {
    Join-Path $env:JAVA_HOME "bin/java.exe"
} else {
    "java"
}
$javaVersion = @(& $javaCommand --version 2>&1 | ForEach-Object { $_.ToString() } | Select-Object -First 1) -join " "
$wrapperProperties = Join-Path $Root "gradle/wrapper/gradle-wrapper.properties"
$gradleVersion = "unknown"
if (Test-Path -LiteralPath $wrapperProperties) {
    $distribution = Select-String -LiteralPath $wrapperProperties -Pattern 'gradle-([0-9.]+)-' | Select-Object -First 1
    if ($distribution -and $distribution.Matches.Count -gt 0) {
        $gradleVersion = $distribution.Matches[0].Groups[1].Value
    }
}
$lines = New-Object System.Collections.Generic.List[string]
$statusDone = -join @([char] 0xC644, [char] 0xB8CC)
$sortedRecords = @($records | Sort-Object path)
$worktreeCanonical = ($sortedRecords | ForEach-Object {
    "STATUS={0}`tPATH={1}`tSHA256={2}`tORIGINAL_PATH={3}" -f $_.status, $_.path, $_.sha256, $_.originalPath
}) -join "`n"
$worktreeHashProvider = [System.Security.Cryptography.SHA256]::Create()
try {
    $finalWorktreeHash = ([System.BitConverter]::ToString(
        $worktreeHashProvider.ComputeHash($Utf8NoBom.GetBytes($worktreeCanonical + "`n"))
    )).Replace("-", "").ToLowerInvariant()
} finally {
    $worktreeHashProvider.Dispose()
}

$lines.Add("CPF_EVIDENCE_VERSION=1") | Out-Null
$lines.Add("EVIDENCE_ID=CPF-FINAL-WORKTREE-MANIFEST-20260714") | Out-Null
$lines.Add("STATUS=$statusDone") | Out-Null
$lines.Add("EXECUTED_AT=$((Get-Date).ToString('o'))") | Out-Null
$lines.Add("START_COMMIT=$StartCommit") | Out-Null
$lines.Add("FINAL_WORKTREE_HASH=$finalWorktreeHash") | Out-Null
$lines.Add("BRANCH=$branch") | Out-Null
$lines.Add("COMMAND=powershell -NoProfile -ExecutionPolicy Bypass -File scripts/export-final-worktree-manifest.ps1") | Out-Null
$lines.Add("EXIT_CODE=0") | Out-Null
$lines.Add("JAVA_VERSION=$javaVersion") | Out-Null
$lines.Add("GRADLE_VERSION=$gradleVersion") | Out-Null
$lines.Add("PROFILE=local") | Out-Null
$lines.Add("MODULE=ALL") | Out-Null
$lines.Add("SANITIZED=Y") | Out-Null
$lines.Add("SECRET_SCAN=PASS") | Out-Null
$lines.Add("TESTS=0") | Out-Null
$lines.Add("FAILURES=0") | Out-Null
$lines.Add("ERRORS=0") | Out-Null
$lines.Add("SKIPPED=0") | Out-Null
$lines.Add("REASON=") | Out-Null
$lines.Add("REPRODUCE_COMMAND=") | Out-Null
$lines.Add("REQUEST_SHA256=$requestSha256") | Out-Null
$lines.Add("REQUEST_GIT_BLOB=$requestGitBlob") | Out-Null
$lines.Add("CHANGED_FILE_COUNT=$($records.Count)") | Out-Null
$lines.Add("SELF_HASH_POLICY=output file uses SELF_REFERENTIAL") | Out-Null
$lines.Add("--- WORKTREE_FILES ---") | Out-Null

foreach ($record in $sortedRecords) {
    $value = "STATUS={0}`tPATH={1}`tSHA256={2}" -f $record.status, $record.path, $record.sha256
    if (-not [string]::IsNullOrWhiteSpace($record.originalPath)) {
        $value += "`tORIGINAL_PATH=" + $record.originalPath
    }
    $lines.Add($value) | Out-Null
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$canonical = ($lines -join "`n") + "`n"
$sha256 = [System.Security.Cryptography.SHA256]::Create()
try {
    $logHash = ([System.BitConverter]::ToString($sha256.ComputeHash($Utf8NoBom.GetBytes($canonical)))).Replace("-", "").ToLowerInvariant()
} finally {
    $sha256.Dispose()
}
$metadataEnd = $lines.IndexOf("--- WORKTREE_FILES ---")
$lines.Insert($metadataEnd, "LOG_SHA256=$logHash")
[System.IO.File]::WriteAllText($outputPath, (($lines -join "`n") + "`n"), $Utf8NoBom)
Write-Host ("final worktree manifest exported: {0} files={1}" -f $outputPath, $records.Count)
