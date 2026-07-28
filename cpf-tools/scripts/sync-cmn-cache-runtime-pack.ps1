param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path,[switch]$Check)
$ErrorActionPreference='Stop';Set-StrictMode -Version Latest
$utf8=[System.Text.UTF8Encoding]::new($false)
$template=Join-Path $Root 'cpf-tools\db\runtime-template\cmn\mybatis\ref\CacheRefreshEventMapper.xml.template'
if(-not(Test-Path -LiteralPath $template -PathType Leaf)){throw "Missing CMN cache mapper canonical template: $template"}
$source=[IO.File]::ReadAllText($template,[Text.Encoding]::UTF8).Replace("`r`n","`n").Replace("`r","`n")
$tokens=@{mariadb='CURRENT_TIMESTAMP(3)';postgresql='CURRENT_TIMESTAMP(3)';oracle='CURRENT_TIMESTAMP(3)'}
foreach($vendor in @('mariadb','postgresql','oracle')){
  $expected=$source.Replace('@NOW3@',$tokens[$vendor]).Trim()+"`n"
  $target=Join-Path $Root "cpf-tools\db\vendor\$vendor\runtime\cmn\mybatis\ref\CacheRefreshEventMapper.xml"
  if($Check){if(-not(Test-Path -LiteralPath $target)){throw "Missing generated CMN cache mapper: $target"};$actual=[IO.File]::ReadAllText($target,[Text.Encoding]::UTF8).Replace("`r`n","`n").Replace("`r","`n");if($actual -cne $expected){throw "CMN cache mapper drift: vendor=$vendor"}}
  else{[IO.Directory]::CreateDirectory((Split-Path -Parent $target))|Out-Null;[IO.File]::WriteAllText($target,$expected,$utf8)}
}
Write-Host "CMN cache runtime pack $([string](if($Check){'check'}else{'sync'})) PASS"
