param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/runtime-smoke")
)

$ErrorActionPreference = "Stop"

$allowed = New-Object System.Collections.Generic.List[object]
$review = New-Object System.Collections.Generic.List[object]
$forbidden = New-Object System.Collections.Generic.List[object]
$eventClasses = New-Object System.Collections.Generic.List[object]
$checkedFiles = 0

function Get-RelativePath {
    param([string] $Path)
    return $Path.Substring($Root.Length + 1).Replace("\", "/")
}

function Add-Finding {
    param(
        [System.Collections.Generic.List[object]] $Target,
        [string] $Rule,
        [string] $Path,
        [string] $Detail
    )

    $Target.Add([pscustomobject]@{
        rule = $Rule
        path = $Path
        detail = $Detail
    })
}

function Test-AllowedSpringEventUsage {
    param(
        [string] $RelativePath,
        [string] $Text
    )

    if ($RelativePath -match "^pfw/src/main/java/cpf/pfw/common/logging/") {
        return $true
    }
    if ($RelativePath -eq "pfw/src/main/java/cpf/pfw/config/CpfTransactionMetaAutoConfiguration.java") {
        return $true
    }
    if ($RelativePath -eq "acc/src/main/java/cpf/acc/common/exception/GlobalExceptionHandler.java" -and $Text -match "TransactionLogEvent") {
        return $true
    }
    return $false
}

function Test-ForbiddenEventPattern {
    param([string] $Text)

    return [System.Text.RegularExpressions.Regex]::IsMatch(
        $Text,
        "Saga|Compensation|UnknownResult|Reconciliation|DLQ|Replay|ServiceCallEngine|WebClient\s*\.\s*builder|RestTemplate|RestClient|KafkaTemplate|JmsTemplate|RabbitTemplate|SFTP|FTP|FileTransfer",
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
    )
}

$sourceFiles = @(Get-ChildItem -LiteralPath $Root -Recurse -File -Filter "*.java" |
    Where-Object { $_.FullName -match "\\src\\main\\java\\" -and $_.FullName -notmatch "\\build\\" })

foreach ($file in $sourceFiles) {
    $checkedFiles++
    $relativePath = Get-RelativePath $file.FullName
    $text = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)

    if ($text -match "\b(class|record)\s+\w*Event\b") {
        Add-Finding $eventClasses "EVENT_CLASS" $relativePath "Class or record name ends with Event."
    }

    $usesSpringEvent = $text -match "ApplicationEventPublisher|@EventListener|ApplicationListener"
    if (-not $usesSpringEvent) {
        continue
    }

    if (Test-AllowedSpringEventUsage $relativePath $text) {
        Add-Finding $allowed "SPRING_EVENT_ALLOWED_HOOK" $relativePath "Allowed hook/log/audit/meta support usage."
    } elseif (Test-ForbiddenEventPattern $text) {
        Add-Finding $forbidden "SPRING_EVENT_CORE_FLOW_FORBIDDEN" $relativePath "Spring Event is used around core flow or external/recovery candidate logic."
    } else {
        Add-Finding $review "SPRING_EVENT_REVIEW" $relativePath "Review whether Spring Event usage is only hook/telemetry/cache/audit support."
    }
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "spring-event-usage-scan.sanitized.json"
$status = "DONE"
$statusCode = "DONE"
if ($forbidden.Count -gt 0) {
    $status = "FAILED"
    $statusCode = "FAILED"
} elseif ($review.Count -gt 0) {
    $status = "NEEDS_REVIEW"
    $statusCode = "NEEDS_REVIEW"
}

$result = [pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $status
    checkedFiles = $checkedFiles
    allowedCount = $allowed.Count
    reviewCount = $review.Count
    forbiddenCount = $forbidden.Count
    allowed = @($allowed.ToArray())
    reviewCandidates = @($review.ToArray())
    forbidden = @($forbidden.ToArray())
    eventClasses = @($eventClasses.ToArray())
}
$json = $result | ConvertTo-Json -Depth 8
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($resultPath, $json, $utf8NoBom)

Write-Host "Spring Event usage scan statusCode=$statusCode allowed=$($allowed.Count) review=$($review.Count) forbidden=$($forbidden.Count) evidence=$resultPath"
if ($review.Count -gt 0) {
    $review | ForEach-Object { Write-Host "REVIEW [$($_.rule)] $($_.path)" }
}
if ($forbidden.Count -gt 0) {
    $forbidden | ForEach-Object { Write-Host "FAIL [$($_.rule)] $($_.path)" }
    exit 1
}
