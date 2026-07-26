param(
 [Parameter(Mandatory=$true)][string]$ReleaseDir,
 [Parameter(Mandatory=$true)][string]$PrivateKey,
 [string]$PublicKey = $env:CPF_RELEASE_PUBLIC_KEY
)
$ErrorActionPreference='Stop'
if(-not(Test-Path $PrivateKey)){throw 'Release private key file not found'}
$repo=(Resolve-Path "$PSScriptRoot\..\..").Path
$classes=Join-Path $repo 'build\release-signer-classes'
New-Item -ItemType Directory -Force -Path $classes|Out-Null
& javac --release 25 -d $classes (Join-Path $PSScriptRoot 'CpfReleaseSigner.java')
if($LASTEXITCODE-ne0){throw 'Release signer compile failed'}
$files=Get-ChildItem $ReleaseDir -File|Where-Object{$_.Extension -in '.json','.jar','.war' -and $_.Name -ne 'cpf-release-signatures.json'}|Sort-Object Name
$rows=@()
foreach($f in $files){$sig="$($f.FullName).sig";& java -cp $classes com.cpf.tools.release.CpfReleaseSigner sign $PrivateKey $f.FullName $sig;if($LASTEXITCODE-ne0){throw "Sign failed: $($f.Name)"};$rows+=[ordered]@{file=$f.Name;signature=(Split-Path $sig -Leaf);algorithm='Ed25519';sha256=(Get-FileHash $f.FullName -Algorithm SHA256).Hash.ToLowerInvariant()}}
[ordered]@{schema='cpf.release-signatures';algorithm='Ed25519';createdAt=(Get-Date).ToUniversalTime().ToString('o');publicKeyRef=$PublicKey;files=$rows}|ConvertTo-Json -Depth 10|Set-Content -Encoding UTF8 (Join-Path $ReleaseDir 'cpf-release-signatures.json')
