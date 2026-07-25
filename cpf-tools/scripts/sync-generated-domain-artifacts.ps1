param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [ValidateSet("Database","AllGeneratorOwned")]
    [string] $Scope = "Database",
    [string[]] $DomainNames = @(),
    [switch] $Apply,
    [switch] $AllowModifiedGeneratorFiles
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path -LiteralPath $Root).Path
$Utf8 = [Text.UTF8Encoding]::new($false)
$generator = Join-Path $Root "cpf-tools/generator/create-domain.ps1"
if (-not (Test-Path $generator)) { throw "Domain generator가 없습니다: $generator" }

function Get-Hash([string] $Path) {
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) { return "" }
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}
function Is-InScope([string] $Path) {
    $p = $Path.Replace('\','/')
    if ($Scope -eq "AllGeneratorOwned") { return $true }
    return $p.StartsWith("src/main/resources/db/vendor/") -or
           $p.StartsWith("src/main/resources/mybatis/vendor/") -or
           $p.StartsWith("src/main/resources/sql/vendor/") -or
           $p.StartsWith("deploy/database/") -or
           $p -eq "manifest/domain-manifest.json"
}
function YN([bool] $Value) { if ($Value) { "Y" } else { "N" } }

$ownershipFiles = @(Get-ChildItem -LiteralPath $Root -Filter "generator-ownership.json" -Recurse -File -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -match "[\\/]cpf-[^\\/]+[\\/]manifest[\\/]generator-ownership\.json$" })

$domainManifests = @(Get-ChildItem -LiteralPath $Root -Filter "domain-manifest.json" -Recurse -File -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -match "[\\/]cpf-[^\\/]+[\\/]manifest[\\/]domain-manifest\.json$" })
$generatedDomainManifests = @($domainManifests | Where-Object {
    try {
        $m = Get-Content $_.FullName -Raw | ConvertFrom-Json
        [string]$m.domainType -eq "GENERATED_DOMAIN"
    } catch { $false }
})

# Generator ownership이 없는 Generated Domain을 조용히 건너뛰면 SQL/Template/API 변경이 해당 Domain에
# 반영되지 않은 채 parity PASS가 날 수 있습니다. 오래된 Domain은 먼저 현재 Generator로 재생성/비교해
# ownership을 정본화해야 하며, 그 전에는 fail-closed 합니다.
foreach ($domainManifest in $generatedDomainManifests) {
    $manifestDir = Split-Path $domainManifest.FullName -Parent
    $ownershipPath = Join-Path $manifestDir "generator-ownership.json"
    if (-not (Test-Path -LiteralPath $ownershipPath -PathType Leaf)) {
        $m = Get-Content $domainManifest.FullName -Raw | ConvertFrom-Json
        throw "Generated Domain generator ownership manifest 누락. 자동 동기화를 생략할 수 없습니다. domain=$($m.domainName) path=$ownershipPath"
    }
}

if ($DomainNames.Count -gt 0) {
    $normalized = @($DomainNames | ForEach-Object { $_.Trim().ToLowerInvariant() })
    $ownershipFiles = @($ownershipFiles | Where-Object {
        $manifest = Get-Content $_.FullName -Raw | ConvertFrom-Json
        $manifest.domainName.ToString().ToLowerInvariant() -in $normalized
    })
}
if ($ownershipFiles.Count -eq 0) {
    Write-Host "Generated Domain sync: target domain 없음."
    exit 0
}

$drifts = New-Object System.Collections.Generic.List[object]
foreach ($ownershipFile in $ownershipFiles) {
    $moduleDir = Split-Path (Split-Path $ownershipFile.FullName -Parent) -Parent
    $ownership = Get-Content $ownershipFile.FullName -Raw | ConvertFrom-Json
    $domainManifestPath = Join-Path $moduleDir "manifest/domain-manifest.json"
    if (-not (Test-Path $domainManifestPath)) { throw "domain-manifest.json 누락: $moduleDir" }
    $domain = Get-Content $domainManifestPath -Raw | ConvertFrom-Json
    $caps = $ownership.capabilities

    $tempRoot = Join-Path $Root ("build/generated-domain-sync/" + $ownership.projectName + "-" + [guid]::NewGuid().ToString("N"))
    New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null
    try {
        Set-Content -LiteralPath (Join-Path $tempRoot "settings.gradle") -Value "// isolated generator sync" -Encoding utf8
        $tempVendor = Join-Path $tempRoot "cpf-tools/db/vendor"
        New-Item -ItemType Directory -Force -Path (Split-Path $tempVendor -Parent) | Out-Null
        Copy-Item -LiteralPath (Join-Path $Root "cpf-tools/db/vendor") -Destination $tempVendor -Recurse -Force

        $tempOutput = Join-Path $tempRoot ("build/domain-generator/" + $ownership.projectName)
        $arguments = @(
            "-NoProfile","-ExecutionPolicy","Bypass","-File",$generator,
            "-Root",$tempRoot,
            "-OutputDir",$tempOutput,
            "-DomainName",$domain.domainName,
            "-SystemCode",$domain.systemCode,
            "-ModuleName",$domain.moduleName,
            "-DomainIdCode",$domain.domainIdCode,
            "-PackageName",$domain.packageName,
            "-BasePackage",$domain.basePackage,
            "-SchemaName",$domain.schemaName,
            "-TablePrefix",$domain.tablePrefix,
            "-Port",[string]$domain.port,
            "-DatabaseVendor",$domain.databaseVendor,
            "-Online",(YN([bool]$caps.online)),
            "-Database",(YN([bool]$caps.database)),
            "-Batch",(YN([bool]$caps.batch)),
            "-External",(YN([bool]$caps.external)),
            "-Messaging",(YN([bool]$caps.messaging)),
            "-File",(YN([bool]$caps.file)),
            "-SecurityAudit",(YN([bool]$caps.securityAudit)),
            "-Ui",(YN([bool]$caps.ui)),
            "-ProductionProfile",(YN([bool]$caps.productionProfile)),
            "-AllowReserved"
        )
        & pwsh @arguments
        if ($LASTEXITCODE -ne 0) { throw "Generator 재생성 실패: $($domain.domainName)" }

        $generatedOwnershipPath = Join-Path $tempOutput "manifest/generator-ownership.json"
        $generatedOwnership = Get-Content $generatedOwnershipPath -Raw | ConvertFrom-Json
        $oldHashByPath = @{}
        foreach ($file in @($ownership.createdFiles)) {
            $key = ([string]$file.path).Replace('\','/')
            $oldHashByPath[$key] = [string]$file.sha256
        }
        $freshByPath = @{}
        foreach ($file in @($generatedOwnership.createdFiles)) {
            $key = ([string]$file.path).Replace('\','/')
            $freshByPath[$key] = $file
        }

        # 새 Generator가 더 이상 소유하지 않는 파일도 drift로 취급합니다.
        # 이전 Generator hash와 동일할 때만 안전하게 삭제하고, 개발자가 수정한 파일은 절대 자동 삭제하지 않습니다.
        foreach ($oldPath in @($oldHashByPath.Keys)) {
            if (-not (Is-InScope $oldPath) -or $freshByPath.ContainsKey($oldPath)) { continue }
            $target = Join-Path $moduleDir $oldPath
            if (-not (Test-Path -LiteralPath $target -PathType Leaf)) { continue }
            $targetHash = Get-Hash $target
            $oldOwnedHash = [string]$oldHashByPath[$oldPath]
            $userModified = $oldOwnedHash -and ($targetHash -ne $oldOwnedHash)
            if ($userModified -and -not $AllowModifiedGeneratorFiles) {
                throw "Generator에서 제거된 파일에 사용자 변경이 있어 자동 삭제하지 않습니다. domain=$($domain.domainName) path=$oldPath"
            }
            $drifts.Add([ordered]@{
                domain = $domain.domainName
                systemCode = $domain.systemCode
                action = 'DELETE'
                path = $oldPath
                currentHash = $targetHash
                generatedHash = ''
                userModified = [bool]$userModified
            })
            if ($Apply) { Remove-Item -LiteralPath $target -Force }
        }

        foreach ($file in @($generatedOwnership.createdFiles)) {
            $relative = ([string]$file.path).Replace('\','/')
            if (-not (Is-InScope $relative)) { continue }
            $fresh = Join-Path $tempOutput $relative
            $target = Join-Path $moduleDir $relative
            $freshHash = Get-Hash $fresh
            $targetHash = Get-Hash $target
            if ($freshHash -eq $targetHash) { continue }

            $oldOwnedHash = [string]$oldHashByPath[$relative]
            # 기존 ownership에 없던 경로가 이미 존재하면 사용자 파일 충돌로 간주합니다.
            $unexpectedExisting = [bool]($targetHash -and -not $oldOwnedHash)
            $userModified = [bool]($targetHash -and $oldOwnedHash -and ($targetHash -ne $oldOwnedHash))
            if (($unexpectedExisting -or $userModified) -and -not $AllowModifiedGeneratorFiles) {
                throw "Generator-owned 경로 충돌/사용자 변경이 있어 자동 덮어쓰지 않습니다. domain=$($domain.domainName) path=$relative"
            }

            $drifts.Add([ordered]@{
                domain = $domain.domainName
                systemCode = $domain.systemCode
                action = $(if ($targetHash) { 'UPDATE' } else { 'CREATE' })
                path = $relative
                currentHash = $targetHash
                generatedHash = $freshHash
                userModified = [bool]($unexpectedExisting -or $userModified)
            })
            if ($Apply) {
                New-Item -ItemType Directory -Force -Path (Split-Path $target -Parent) | Out-Null
                Copy-Item -LiteralPath $fresh -Destination $target -Force
            }
        }

        if ($Apply) {
            # ownership manifest 자체도 최신 Generator의 정본으로 교체한 뒤 현재 hash를 다시 기록합니다.
            $currentOwnership = $generatedOwnership
            foreach ($owned in @($currentOwnership.createdFiles)) {
                $target = Join-Path $moduleDir ([string]$owned.path)
                if (Test-Path -LiteralPath $target -PathType Leaf) { $owned.sha256 = Get-Hash $target }
            }
            $currentOwnership.generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
            [IO.File]::WriteAllText(
                $ownershipFile.FullName,
                ($currentOwnership | ConvertTo-Json -Depth 30),
                $Utf8)
        }
    } finally {
        if (Test-Path $tempRoot) { Remove-Item $tempRoot -Recurse -Force }
    }
}

if ($drifts.Count -gt 0 -and -not $Apply) {
    $drifts | Format-Table domain,systemCode,action,path,userModified -AutoSize
    throw "Generated Domain artifact drift $($drifts.Count)건. 검토 후 -Apply를 실행하십시오."
}

if ($Apply) {
    Write-Host "Generated Domain artifact sync PASS. updated=$($drifts.Count) scope=$Scope"
} else {
    Write-Host "Generated Domain artifact parity PASS. scope=$Scope"
}
