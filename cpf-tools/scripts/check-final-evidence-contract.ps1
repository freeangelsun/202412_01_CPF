param(
 [string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path,
 [string]$EvidenceDir='cpf-docs/evidence/final-closing',
 [switch]$RequireAll
)
$ErrorActionPreference='Stop';Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
$dir=if([IO.Path]::IsPathRooted($EvidenceDir)){$EvidenceDir}else{Join-Path $Root $EvidenceDir}
if(-not(Test-Path $dir -PathType Container)){if($RequireAll){throw "final evidence directory missing: $dir"};Write-Host '[INFO] final evidence not generated yet';return}
$required=@('exactSha','command','profile','environment','startedAt','endedAt','exitCode','outputFile','outputSha256','redactionChecked','requirementIds','status')
$allowed=@('완료','부분 구현','미구현','미검증','실패','재확인 필요')
$files=@(Get-ChildItem $dir -Filter '*.evidence.json' -File)
if($RequireAll -and $files.Count -eq 0){throw 'final evidence JSON is empty'}
foreach($f in $files){
 $o=Get-Content -Raw -Encoding UTF8 $f.FullName|ConvertFrom-Json -Depth 30
 foreach($k in $required){if($null -eq $o.$k -or ([string]$o.$k).Trim().Length -eq 0){throw "evidence field missing: $($f.Name) :: $k"}}
 if($allowed -notcontains [string]$o.status){throw "invalid evidence status: $($f.Name) :: $($o.status)"}
 if([int]$o.exitCode -ne 0 -and [string]$o.status -eq '완료'){throw "failed command marked complete: $($f.Name)"}
 if(-not [bool]$o.redactionChecked){throw "redaction not checked: $($f.Name)"}
 $out=Join-Path $dir ([string]$o.outputFile)
 if(-not(Test-Path $out -PathType Leaf)){throw "evidence output missing: $out"}
 $hash=(Get-FileHash $out -Algorithm SHA256).Hash.ToLowerInvariant()
 if($hash -ne ([string]$o.outputSha256).ToLowerInvariant()){throw "evidence output hash mismatch: $($f.Name)"}
 if((Get-Content -Raw -Encoding UTF8 $out).Trim() -in @('PASS','SUCCESS','완료')){throw "one-line evidence rejected: $($f.Name)"}
}
Write-Host "[PASS] final evidence contract files=$($files.Count)"
