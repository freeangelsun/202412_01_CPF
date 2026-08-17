[CmdletBinding()]
param(
    [string]$RepositoryRoot = '.',
    [string]$ManifestPath = 'cpf-docs/work/current/DELETE_MANIFEST.txt'
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
$paths=@(
    Get-Content -LiteralPath $manifest -Encoding UTF8 |
        ForEach-Object {$_.Trim()} |
        Where-Object {$_ -and -not $_.StartsWith('#')}
)
$seen=@{}; $deleted=0; $alreadyAbsent=0
foreach($raw in $paths){
    $rel=([string]$raw).Replace('\\','/').TrimStart('./')
    if([string]::IsNullOrWhiteSpace($rel)){continue}
    if($seen.ContainsKey($rel)){throw "DELETE_MANIFEST_DUPLICATE_PATH: $rel"}
    $seen[$rel]=$true
    if([IO.Path]::IsPathRooted($rel) -or $rel -match '(^|/)\.\.(/|$)' -or $rel.IndexOfAny([char[]]'*?[]') -ge 0){throw "DELETE_MANIFEST_UNSAFE_PATH: $rel"}
    foreach($prefix in $protectedPrefixes){if($rel.StartsWith($prefix,[StringComparison]::OrdinalIgnoreCase)){throw "DELETE_MANIFEST_PROTECTED_PATH: $rel"}}
    foreach($exact in $protectedExact){if($rel.Equals($exact,[StringComparison]::OrdinalIgnoreCase)){throw "DELETE_MANIFEST_PROTECTED_PATH: $rel"}}
    $target=[IO.Path]::GetFullPath((Join-Path $root ($rel.Replace('/',[IO.Path]::DirectorySeparatorChar))))
    if(-not $target.StartsWith($rootPrefix,[StringComparison]::OrdinalIgnoreCase)){throw "DELETE_MANIFEST_PATH_ESCAPE: $rel"}
    if(Test-Path -LiteralPath $target -PathType Container){throw "DELETE_MANIFEST_DIRECTORY_DELETE_FORBIDDEN: $rel"}
    if(Test-Path -LiteralPath $target -PathType Leaf){Remove-Item -LiteralPath $target -Force; $deleted++}else{$alreadyAbsent++}
}

# 파일 삭제 후 생긴 빈 폴더만 bottom-up으로 제거한다. 보호 경로는 유지한다.
$emptyDeleted=0
Get-ChildItem -LiteralPath $root -Directory -Recurse -Force |
    Sort-Object {$_.FullName.Length} -Descending |
    ForEach-Object {
        $relDir=[IO.Path]::GetRelativePath($root,$_.FullName).Replace('\\','/')+'/'
        $protected=$false
        foreach($prefix in $protectedPrefixes){if($relDir.StartsWith($prefix,[StringComparison]::OrdinalIgnoreCase)){$protected=$true;break}}
        if(-not $protected -and -not(Get-ChildItem -LiteralPath $_.FullName -Force -ErrorAction SilentlyContinue | Select-Object -First 1)){
            Remove-Item -LiteralPath $_.FullName -Force -ErrorAction SilentlyContinue
            if(-not(Test-Path -LiteralPath $_.FullName)){$emptyDeleted++}
        }
    }

Write-Host ("CPF_DELETE_MANIFEST_APPLIED rows={0} deleted={1} alreadyAbsent={2} emptyDirs={3}" -f $paths.Count,$deleted,$alreadyAbsent,$emptyDeleted)
git status --short
