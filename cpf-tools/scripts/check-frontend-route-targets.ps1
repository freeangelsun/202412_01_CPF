param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'
$Root=(Resolve-Path -LiteralPath $Root).Path
$errors=[System.Collections.Generic.List[string]]::new()
$externalRuntimePattern = '(?i)(?:@import\s+(?:url\()?|url\s*\(|<link\b[^>]*\bhref\s*=|<(?:script|img|source)\b[^>]*\bsrc\s*=|baseURL\s*:|(?:fetch|EventSource|WebSocket)\s*\()\s*["'']https?://(?!localhost|127\.0\.0\.1)'

foreach($app in @('cpf-admin/frontend','cpf-biz-admin/frontend')) {
    $routes=Join-Path $Root "$app/src/app/routes.ts"
    if(-not(Test-Path $routes)){ $errors.Add("$app routes.ts 누락"); continue }
    $text=Get-Content $routes -Raw
    $routeDir=Split-Path $routes -Parent
    $matches=[regex]::Matches($text, "import\(\s*['""]([^'""]+)['""]\s*\)")
    foreach($m in $matches) {
        $relative=$m.Groups[1].Value
        if($relative.StartsWith('.')) {
            $target=[IO.Path]::GetFullPath((Join-Path $routeDir $relative))
            if(-not(Test-Path $target)){ $errors.Add("$app lazy route target 누락: $relative") }
        }
    }

    $src=Join-Path $Root "$app/src"
    if(Test-Path $src) {
        $external=@(Get-ChildItem $src -Recurse -File -Include *.vue,*.ts,*.css,*.html |
            Select-String -Pattern $externalRuntimePattern -ErrorAction SilentlyContinue)
        if($external.Count){ $errors.Add("$app 외부 Runtime URL $($external.Count)건") }
    }
}
if($errors.Count){$errors|ForEach-Object{Write-Host " - $_"};throw "Frontend route/runtime asset gate FAIL: $($errors.Count)건"}
Write-Host 'Frontend lazy-route target/local-asset gate PASS.'
