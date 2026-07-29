param(
    [string] $ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [Alias('ExpectedSha')]
    [string] $ExpectedBaseSha = 'b8941577b99535ff3e64a4fad99b74bafa544227',
    [switch] $AllowDifferentBaseSha,
    [switch] $SkipStaticVerification
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$ProjectRoot = (Resolve-Path -LiteralPath $ProjectRoot).Path

function Invoke-CheckedScript {
    param([string] $Path, [hashtable] $Arguments = @{})
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "Required script is missing: $Path"
    }
    & $Path @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Script failed: $Path (exit=$LASTEXITCODE)"
    }
}

Push-Location $ProjectRoot
try {
    if (-not (Test-Path -LiteralPath '.git' -PathType Container)) {
        throw "CPF Git worktree is required: $ProjectRoot"
    }

    $head = (& git rev-parse HEAD).Trim()
    if (-not $head) {
        throw 'Unable to resolve current Git HEAD.'
    }
    if ($ExpectedBaseSha -and $head -ne $ExpectedBaseSha -and -not $AllowDifferentBaseSha) {
        throw "Overlay base SHA mismatch. expected=$ExpectedBaseSha actual=$head. Rebase/review the overlay or rerun with -AllowDifferentBaseSha after manual impact review."
    }

    $dirtyBefore = @(& git status --short)
    Write-Host "[INFO] CPF overlay apply start: root=$ProjectRoot head=$head dirty=$($dirtyBefore.Count)"

    # Local runtime is a development/integration source module. Keep the Gradle logical
    # project names, but physically own the source below cpf-tools/runtime.
    Invoke-CheckedScript -Path (Join-Path $ProjectRoot 'cpf-tools\scripts\relocate-local-runtime-modules.ps1') -Arguments @{ ProjectRoot = $ProjectRoot }

    # Obsolete root build copies are removed only after a lossless merge. Different
    # content at the same relative path is a conflict and stops the apply.
    function Merge-CanonicalBuildDirectory([string] $LegacyName, [string] $CanonicalRelative) {
        $legacyPath = Join-Path $ProjectRoot $LegacyName
        $canonicalPath = Join-Path $ProjectRoot $CanonicalRelative
        if (-not (Test-Path -LiteralPath $legacyPath -PathType Container)) { return }
        if (-not (Test-Path -LiteralPath $canonicalPath -PathType Container)) {
            throw "Canonical build owner is missing: $CanonicalRelative"
        }
        $conflicts = [System.Collections.Generic.List[string]]::new()
        Get-ChildItem -LiteralPath $legacyPath -Recurse -File | ForEach-Object {
            $relative = [System.IO.Path]::GetRelativePath($legacyPath, $_.FullName)
            $destination = Join-Path $canonicalPath $relative
            if (-not (Test-Path -LiteralPath $destination -PathType Leaf)) {
                New-Item -ItemType Directory -Path (Split-Path -Parent $destination) -Force | Out-Null
                Copy-Item -LiteralPath $_.FullName -Destination $destination -Force
            } elseif ((Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash -ne
                      (Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash) {
                $conflicts.Add($relative)
            }
        }
        if ($conflicts.Count -gt 0) {
            throw "Root build relocation conflict ($LegacyName):`n$($conflicts -join [Environment]::NewLine)"
        }
        Remove-Item -LiteralPath $legacyPath -Recurse -Force
        Write-Host "[MOVED] $LegacyName -> $CanonicalRelative"
    }
    Merge-CanonicalBuildDirectory 'cpf-gradle-plugins' 'cpf-tools\build\gradle-plugin'
    Merge-CanonicalBuildDirectory 'cpf-platform-bom' 'cpf-tools\build\platform-bom'

    # Remove only obsolete paths produced by the earlier non-canonical checkpoint.
    # PostgreSQL/Oracle lifecycle ownership is migration/flyway/{logicalDB} and rollback/{logicalDB};
    # source/migration is MariaDB-only authoring ownership.
    foreach ($vendor in @('postgresql', 'oracle')) {
        $obsoleteRoot = Join-Path $ProjectRoot "cpf-tools\db\vendor\$vendor\source\migration"
        if (Test-Path -LiteralPath $obsoleteRoot -PathType Container) {
            $knownNames = @(
                'V69__enterprise_cache_file_job.sql',
                'V70__bza_action_permission_hardening.sql',
                'V71__adm_notification_action_permissions.sql',
                'V72__adm_file_job_control_audit.sql',
                'R69__enterprise_cache_file_job.sql',
                'R70__bza_action_permission_hardening.sql',
                'R71__adm_notification_action_permissions.sql',
                'R72__adm_file_job_control_audit.sql',
                'checksums.sha256'
            )
            Get-ChildItem -LiteralPath $obsoleteRoot -Recurse -File | ForEach-Object {
                if ($_.Name -in $knownNames) { Remove-Item -LiteralPath $_.FullName -Force }
            }
            Get-ChildItem -LiteralPath $obsoleteRoot -Recurse -Directory |
                Sort-Object FullName -Descending |
                Where-Object { @(Get-ChildItem -LiteralPath $_.FullName -Force).Count -eq 0 } |
                Remove-Item -Force
            if (@(Get-ChildItem -LiteralPath $obsoleteRoot -Force -ErrorAction SilentlyContinue).Count -eq 0) {
                Remove-Item -LiteralPath $obsoleteRoot -Force
            }
            Write-Host "[CLEAN] removed obsolete non-canonical lifecycle paths: $vendor/source/migration"
        }
    }

    # Canonical schema/seed -> official vendor source -> install/migration/rollback/checksum packs.
    # This is intentionally part of apply so Source and generated lifecycle artifacts cannot drift.
    Invoke-CheckedScript -Path (Join-Path $ProjectRoot 'cpf-tools\scripts\sync-database-artifacts.ps1') -Arguments @{ Root = $ProjectRoot }

    foreach ($forbidden in @('cpf-local-runtime', 'cpf-local-batch-runtime', 'cpf-gradle-plugins', 'cpf-platform-bom')) {
        if (Test-Path -LiteralPath (Join-Path $ProjectRoot $forbidden)) {
            throw "Repository root ownership violation remains after apply: $forbidden"
        }
    }

    if (-not $SkipStaticVerification) {
        $python = Get-Command python -ErrorAction Stop
        & $python.Source (Join-Path $ProjectRoot 'cpf-tools\verification\20260729_04\check_final_source_closure.py') $ProjectRoot
        if ($LASTEXITCODE -ne 0) { throw "Final source closure failed (exit=$LASTEXITCODE)" }

        & $python.Source (Join-Path $ProjectRoot 'cpf-tools\verification\20260729_04\check_generator_idempotency_templates.py') $ProjectRoot
        if ($LASTEXITCODE -ne 0) { throw "Generator idempotency template failed (exit=$LASTEXITCODE)" }

        & $python.Source (Join-Path $ProjectRoot 'cpf-tools\verification\20260729_04\check_generator_java_template_compile.py') $ProjectRoot
        if ($LASTEXITCODE -ne 0) { throw "Generator Java template compile failed (exit=$LASTEXITCODE)" }

        $node = Get-Command node -ErrorAction Stop
        & $node.Source (Join-Path $ProjectRoot 'cpf-tools\verification\20260729_04\check_frontend_syntax.cjs') $ProjectRoot
        if ($LASTEXITCODE -ne 0) { throw "Frontend syntax gate failed (exit=$LASTEXITCODE)" }

        Invoke-CheckedScript -Path (Join-Path $ProjectRoot 'cpf-tools\scripts\check-local-runtime-topology.ps1') -Arguments @{ Root = $ProjectRoot }
    }

    $dirtyAfter = @(& git status --short)
    Write-Host "[PASS] CPF 20260729 final overlay applied. changedEntries=$($dirtyAfter.Count)"
    Write-Host '[NEXT] Review git diff, then run Java 25/Gradle 9.1, frontend, DB, Redis, browser and multi-instance validation.'
}
finally {
    Pop-Location
}
