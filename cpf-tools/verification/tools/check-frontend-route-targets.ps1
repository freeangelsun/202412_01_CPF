param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..\..").Path)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
$python=Get-Command python -ErrorAction Stop
& $python.Source (Join-Path $Root 'cpf-tools/verification/tools/verify-cpf-frontend-consumer-closure.py') --root $Root
if($LASTEXITCODE -ne 0){throw 'Frontend source/import closure failed.'}
$errors=[System.Collections.Generic.List[string]]::new()
$external='(?i)(?:@import\s+(?:url\()?|url\s*\(|<link\b[^>]*\bhref\s*=|<(?:script|img|source)\b[^>]*\bsrc\s*=|(?:fetch|EventSource|WebSocket)\s*\()\s*["'']https?://(?!localhost|127\.0\.0\.1)'
foreach($app in @('cpf-admin/frontend','cpf-biz-frontend')){
  $src=Join-Path $Root "$app/src"; if(-not(Test-Path $src)){ $errors.Add("$app source missing"); continue }
  $found=@(Get-ChildItem $src -Recurse -File -Include *.vue,*.ts,*.css,*.html|Select-String -Pattern $external -ErrorAction SilentlyContinue)
  if($found.Count){$errors.Add("$app external runtime URL $($found.Count)")}
}
if($errors.Count){$errors|%{Write-Host " - $_"};throw "Frontend route/runtime asset gate FAIL: $($errors.Count)"}
Write-Host 'Frontend route/import/local-asset gate PASS.'
