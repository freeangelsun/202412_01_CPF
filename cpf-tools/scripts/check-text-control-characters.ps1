param([string]$Root = (Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
$extensions = @('.java','.gradle','.ps1','.yml','.yaml','.json','.md','.csv','.sql','.xml','.properties')
$bad = [System.Collections.Generic.List[string]]::new()
foreach ($file in Get-ChildItem -LiteralPath $Root -Recurse -File -Force) {
    if ($file.FullName -match '[\\/](build|node_modules|\.git|logs|tmp)[\\/]') { continue }
    if ($extensions -notcontains $file.Extension.ToLowerInvariant()) { continue }
    $stream = [IO.File]::OpenRead($file.FullName)
    $buffer = [byte[]]::new(8192)
    $offset = 0L
    $found = $false
    try {
        while (-not $found -and ($read = $stream.Read($buffer, 0, $buffer.Length)) -gt 0) {
            for ($index = 0; $index -lt $read; $index++) {
                $value = [int]$buffer[$index]
                if ($value -lt 32 -and $value -notin @(9,10,13)) {
                    $bad.Add("$($file.FullName): byte=$value offset=$($offset + $index)")
                    $found = $true
                    break
                }
            }
            $offset += $read
        }
    } finally {
        $stream.Dispose()
    }
}
if ($bad.Count -gt 0) { throw "text control character detected:`n$($bad -join [Environment]::NewLine)" }
Write-Host '[PASS] CPF text control character gate'
