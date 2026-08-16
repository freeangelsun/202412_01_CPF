$ErrorActionPreference='Stop';$root=(Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path;$errors=@();$publicTypes=@{};$imports=@{}
Get-ChildItem $root -Recurse -File -Filter *.java|?{$_.FullName -notmatch '[\\/](build|out|target)[\\/]'}|%{$rel=$_.FullName.Substring($root.Length+1);$text=Get-Content $_.FullName -Raw -Encoding UTF8
 if($text -match '(?m)^package\s+([\w.]+);'){$pkg=$Matches[1]}else{return};if($text -match '(?m)^public\s+(?:final\s+)?(?:class|interface|record|enum)\s+(\w+)'){$publicTypes["$pkg.$($Matches[1])"]=$rel}
 $imports[$rel]=[regex]::Matches($text,'(?m)^import\s+(com\.cpf\.[\w.]+);')|%{$_.Groups[1].Value}
 if($rel -match 'src[\\/]main' -and $text -match 'public\s+[^\n]*(Map<String\s*,\s*Object>|ResponseEntity<Map)'){$errors+="Public raw Map: $rel"}
 if($rel -notmatch 'cpf-core[\\/]' -and $text -match 'import\s+com\.cpf\.core\.common\.'){$errors+="Core internal import: $rel"}
 if($rel -notmatch 'cpf-batch[\\/]' -and $text -match 'import\s+com\.cpf\.batch\.(?!api|spi)'){$errors+="Batch internal import: $rel"}}
foreach($type in $publicTypes.Keys){$consumer=($imports.Values|%{$_}|?{$_ -eq $type}).Count;if($type -match '\.spi\.' -and $consumer -eq 0){Write-Warning "SPI consumer 미발견: $type"}}
if($errors){$errors|%{Write-Error $_};exit 1};Write-Host "[PASS] Semantic graph publicTypes=$($publicTypes.Count)"
