[CmdletBinding()]
param(
    [string]$RepositoryRoot = '.',
    [string]$ManifestPath = 'cpf-docs/deliverables/DELETE_MANIFEST.csv',
    [string]$ReasonTag = '',
    [string]$UserApprovalRef = ''
)

$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$root=(Resolve-Path -LiteralPath $RepositoryRoot).Path
$manifest=Join-Path $root $ManifestPath
if(-not(Test-Path -LiteralPath $manifest -PathType Leaf)){throw "DELETE_MANIFEST_NOT_FOUND: $manifest"}

$protectedPrefixes=@(
    'cpf-docs/deliverables/',
    'cpf-docs/guides/',
    'cpf-docs/assets/manuals/',
    'cpf-docs/assets/readme/',
    'cpf-docs/environment/docker/',
    'cpf-tools/environment/docker-development-test/'
)
$protectedExact=@('cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md')
$rootPrefix=$root.TrimEnd([IO.Path]::DirectorySeparatorChar)+[IO.Path]::DirectorySeparatorChar
$allRows=@(Import-Csv -LiteralPath $manifest -Encoding UTF8)
$rows=@()
$historicalEvidenceRows=0
foreach($candidate in $allRows){
    $approved=([string]$candidate.approved).Trim().ToLowerInvariant()
    $precondition=([string]$candidate.precondition).Trim()
    $lifecycle=([string]$candidate.lifecycle).Trim()

    # HISTORICAL_ALREADY_ABSENT rows are evidence only. They describe paths that were absent
    # in the development baseline and must never become executable deletion instructions.
    # Local build outputs may legitimately recreate some historical paths (for example bin/).
    if($lifecycle -eq 'HISTORICAL_ALREADY_ABSENT'){
        $historicalEvidenceRows++
        continue
    }
    if($lifecycle -ne 'PENDING_USER_EXECUTION'){
        throw "DELETE_MANIFEST_UNSUPPORTED_LIFECYCLE: $lifecycle ($($candidate.path))"
    }

    $userRequired=([string]$candidate.user_execution_required).Trim().ToLowerInvariant() -in @('true','1','yes','y')
    $tagOk=[string]::IsNullOrWhiteSpace($ReasonTag) -or ([string]$candidate.reason).StartsWith($ReasonTag,[StringComparison]::Ordinal)
    $eligible=($approved -in @('true','1','yes','y')) -and $precondition -eq 'SATISFIED' -and $tagOk
    if(-not $eligible){
        continue
    }
    if($userRequired -and [string]::IsNullOrWhiteSpace($UserApprovalRef)){
        throw "DELETE_MANIFEST_USER_APPROVAL_REQUIRED: $($candidate.path) (pass -UserApprovalRef with the user's current approval reference)"
    }
    $rows += $candidate
}
$seen=@{}; $deleted=0; $alreadyAbsent=0; $emptyManifestDirs=0
foreach($row in $rows){
    $rel=([string]$row.path).Replace('\','/').TrimStart('./')
    if([string]::IsNullOrWhiteSpace($rel)){continue}
    if($seen.ContainsKey($rel)){throw "DELETE_MANIFEST_DUPLICATE_PATH: $rel"}
    $seen[$rel]=$true
    if([IO.Path]::IsPathRooted($rel) -or $rel -match '(^|/)\.\.(/|$)' -or $rel.IndexOfAny([char[]]'*?[]') -ge 0){throw "DELETE_MANIFEST_UNSAFE_PATH: $rel"}
    $target=[IO.Path]::GetFullPath((Join-Path $root ($rel.Replace('/',[IO.Path]::DirectorySeparatorChar))))
    if(-not $target.StartsWith($rootPrefix,[StringComparison]::OrdinalIgnoreCase)){throw "DELETE_MANIFEST_PATH_ESCAPE: $rel"}
    $isProtected=$false
    foreach($prefix in $protectedPrefixes){if($rel.StartsWith($prefix,[StringComparison]::OrdinalIgnoreCase)){$isProtected=$true;break}}
    if(-not $isProtected){foreach($exact in $protectedExact){if($rel.Equals($exact,[StringComparison]::OrdinalIgnoreCase)){$isProtected=$true;break}}}
    # Historical protected-path evidence is allowed only when the path is already absent.
    # A currently existing protected path remains fail-closed and is never removed by this utility.
    if($isProtected -and -not(Test-Path -LiteralPath $target)){
        $alreadyAbsent++
        continue
    }
    if($isProtected){throw "DELETE_MANIFEST_PROTECTED_PATH: $rel"}
    if(Test-Path -LiteralPath $target -PathType Container){
        if(Get-ChildItem -LiteralPath $target -Force -ErrorAction SilentlyContinue | Select-Object -First 1){throw "DELETE_MANIFEST_NONEMPTY_DIRECTORY_FORBIDDEN: $rel"}
        Remove-Item -LiteralPath $target -Force
        $emptyManifestDirs++
    } elseif(Test-Path -LiteralPath $target -PathType Leaf){
        Remove-Item -LiteralPath $target -Force
        $deleted++
    } else {
        $alreadyAbsent++
    }
}

$emptyDeleted=0
Get-ChildItem -LiteralPath $root -Directory -Recurse -Force |
    Sort-Object {$_.FullName.Length} -Descending |
    ForEach-Object {
        $relDir=$_.FullName.Substring($rootPrefix.Length).Replace('\','/')+'/'
        $protected=$false
        foreach($prefix in $protectedPrefixes){if($relDir.StartsWith($prefix,[StringComparison]::OrdinalIgnoreCase)){$protected=$true;break}}
        if(-not $protected -and -not(Get-ChildItem -LiteralPath $_.FullName -Force -ErrorAction SilentlyContinue | Select-Object -First 1)){
            Remove-Item -LiteralPath $_.FullName -Force -ErrorAction SilentlyContinue
            if(-not(Test-Path -LiteralPath $_.FullName)){$emptyDeleted++}
        }
    }

Write-Host ("CPF_DELETE_MANIFEST_APPLIED pendingRows={0} historicalEvidenceRows={1} filesDeleted={2} manifestEmptyDirs={3} alreadyAbsent={4} cleanupEmptyDirs={5} reasonTag={6} userApprovalRef={7}" -f $rows.Count,$historicalEvidenceRows,$deleted,$emptyManifestDirs,$alreadyAbsent,$emptyDeleted,$ReasonTag,$UserApprovalRef)
git status --short
