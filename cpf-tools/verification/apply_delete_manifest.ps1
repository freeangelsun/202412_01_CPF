[CmdletBinding()]
param(
    [string]$RepositoryRoot = '.',
    [string]$ManifestPath = 'cpf-docs/deliverables/DELETE_MANIFEST.csv',
    [string]$ReasonTag = ''
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
$rows=@(Import-Csv -LiteralPath $manifest -Encoding UTF8 | Where-Object {
    $approved=([string]$_.approved).Trim().ToLowerInvariant()
    $tagOk=[string]::IsNullOrWhiteSpace($ReasonTag) -or ([string]$_.reason).StartsWith($ReasonTag,[StringComparison]::Ordinal)
    $approved -in @('true','1','yes','y') -and $tagOk
})
$seen=@{}; $deleted=0; $alreadyAbsent=0; $emptyManifestDirs=0
foreach($row in $rows){
    $rel=([string]$row.path).Replace('\','/').TrimStart('./')
    if([string]::IsNullOrWhiteSpace($rel)){continue}
    if($seen.ContainsKey($rel)){throw "DELETE_MANIFEST_DUPLICATE_PATH: $rel"}
    $seen[$rel]=$true
    if([IO.Path]::IsPathRooted($rel) -or $rel -match '(^|/)\.\.(/|$)' -or $rel.IndexOfAny([char[]]'*?[]') -ge 0){throw "DELETE_MANIFEST_UNSAFE_PATH: $rel"}
    foreach($prefix in $protectedPrefixes){if($rel.StartsWith($prefix,[StringComparison]::OrdinalIgnoreCase)){throw "DELETE_MANIFEST_PROTECTED_PATH: $rel"}}
    foreach($exact in $protectedExact){if($rel.Equals($exact,[StringComparison]::OrdinalIgnoreCase)){throw "DELETE_MANIFEST_PROTECTED_PATH: $rel"}}
    $target=[IO.Path]::GetFullPath((Join-Path $root ($rel.Replace('/',[IO.Path]::DirectorySeparatorChar))))
    if(-not $target.StartsWith($rootPrefix,[StringComparison]::OrdinalIgnoreCase)){throw "DELETE_MANIFEST_PATH_ESCAPE: $rel"}
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

Write-Host ("CPF_DELETE_MANIFEST_APPLIED rows={0} filesDeleted={1} manifestEmptyDirs={2} alreadyAbsent={3} cleanupEmptyDirs={4} reasonTag={5}" -f $rows.Count,$deleted,$emptyManifestDirs,$alreadyAbsent,$emptyDeleted,$ReasonTag)
git status --short
