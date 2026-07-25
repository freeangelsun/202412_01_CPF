param([string]$Root = (Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$review=Join-Path $Root 'cpf-docs/work/review/20260725_02/CPF_R8_REQUIREMENT_REVIEW.md'
if(-not(Test-Path -LiteralPath $review -PathType Leaf)){throw "R8 requirement review missing: $review"}
$text=Get-Content -LiteralPath $review -Raw
$matches=[regex]::Matches($text,'(?m)^\| `([^`]+)` \| ([^|]+) \| ([^|]+) \| ([^|]+) \| ([^|]+) \|$')
$ids=@($matches|ForEach-Object{$_.Groups[1].Value})
if($ids.Count -ne 162){throw "Canonical requirement row count mismatch: expected=162 actual=$($ids.Count)"}
$unique=@($ids|Sort-Object -Unique)
if($unique.Count -ne 162){throw "Canonical requirement IDs are duplicated: unique=$($unique.Count)"}
$allowed=@('완료','부분 구현','미구현','미검증','실패','재확인 필요')
foreach($m in $matches){
  $status=$m.Groups[4].Value.Trim()
  if($allowed -notcontains $status){throw "Invalid requirement status: id=$($m.Groups[1].Value) status=$status"}
}
$aliases=@('FACADE-LOCAL','FACADE-REMOTE','CMN-ID','CMN-FILE','CMN-FIXED','ADM-COMP','CENTER-ADV','API-GATEWAY')
foreach($alias in $aliases){if($ids -contains $alias){throw "Legacy alias must not be double-counted: $alias"}}
Write-Host 'R8 canonical requirement review PASS. canonical=162 unique=162 aliases=0.'
