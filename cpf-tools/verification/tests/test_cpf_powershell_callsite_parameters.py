"""하네스가 다른 PowerShell 스크립트에 **선언되지 않은 파라미터**를 넘기지 않는지 검증한다.

`[CmdletBinding()]` 이 없는 `param()` 블록은 알 수 없는 이름을 오류 없이 `$args` 로 흘려보낸다.
그래서 호출부가 존재하지 않는 옵션을 넘겨도 **아무 증상 없이 무시**된다.

실제 사례: Full Runtime 의 `LOCAL_ONE_WAS_START` 가
`start-cpf-local.ps1 -RepoRoot <root> -Mode integrated -ResourceProfile local -WebOnly` 로 호출했는데
`-RepoRoot` 와 `-WebOnly` 는 그 스크립트(및 저장소 전체)에 **선언된 적이 없다**.
`git log --all -S WebOnly` 에도 이 호출부 외에는 나오지 않는다. 즉 "웹 계층만 기동" 이라는 의도가
한 번도 적용되지 않은 채 통과하고 있었다.

`test_cpf_powershell_script_references_exist.py` 는 **경로**만 검증한다. 이 게이트는 **파라미터**를
검증한다. 둘 다 있어야 호출 계약이 닫힌다.
"""
from __future__ import annotations

import json
import subprocess
import tempfile
import sys
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
# pwsh 호스트 자체의 옵션이라 대상 스크립트의 param() 에는 없는 것이 정상이다.
POWERSHELL_HOST_OPTIONS = {
    "-NoProfile", "-File", "-Command", "-NoLogo", "-NonInteractive", "-ExecutionPolicy",
    "-EncodedCommand", "-WorkingDirectory", "-InputFormat", "-OutputFormat", "-Version",
    "-WindowStyle", "-MTA", "-STA", "-NoExit",
}
EXCLUDED_PATH_PARTS = {"build", "node_modules", ".git", "cpf-release", "bin", "out"}

# 이 게이트는 PowerShell AST 로 판정한다. 정규식으로 param() 블록과 배열 리터럴을 읽으면
# 여러 줄 attribute / 중첩 배열 / 문자열 보간에서 곧바로 틀린다.
SCANNER = r"""
param([string] $Root)
$ErrorActionPreference = 'Stop'
$Root = (Resolve-Path -LiteralPath $Root).Path
$hostOptions = @('-NoProfile','-File','-Command','-NoLogo','-NonInteractive','-ExecutionPolicy',
    '-EncodedCommand','-WorkingDirectory','-InputFormat','-OutputFormat','-Version','-WindowStyle',
    '-MTA','-STA','-NoExit')

function Get-DeclaredParameters([string] $Path) {
    $ast = [System.Management.Automation.Language.Parser]::ParseFile($Path, [ref]$null, [ref]$null)
    if ($null -eq $ast -or $null -eq $ast.ParamBlock) { return @() }
    $names = @()
    foreach ($p in $ast.ParamBlock.Parameters) {
        $names += $p.Name.VariablePath.UserPath
        foreach ($attr in $p.Attributes) {
            if ($attr -is [System.Management.Automation.Language.AttributeAst] -and $attr.TypeName.Name -eq 'Alias') {
                foreach ($pos in $attr.PositionalArguments) {
                    if ($pos -is [System.Management.Automation.Language.StringConstantExpressionAst]) { $names += $pos.Value }
                }
            }
        }
    }
    return $names
}

$declaredCache = @{}
$findings = @()
$scripts = Get-ChildItem -LiteralPath $Root -Recurse -File -Filter '*.ps1' -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -notmatch '[\\/](build|node_modules|\.git|cpf-release|bin|out)[\\/]' }
foreach ($script in $scripts) {
    $ast = [System.Management.Automation.Language.Parser]::ParseFile($script.FullName, [ref]$null, [ref]$null)
    if ($null -eq $ast) { continue }
    $arrays = $ast.FindAll({ param($n) $n -is [System.Management.Automation.Language.ArrayLiteralAst] }, $true)
    foreach ($array in $arrays) {
        $elements = @($array.Elements)
        for ($i = 0; $i -lt $elements.Count; $i++) {
            $element = $elements[$i]
            if ($element -isnot [System.Management.Automation.Language.StringConstantExpressionAst]) { continue }
            if ($element.Value -ne '-File') { continue }
            if ($i + 1 -ge $elements.Count) { continue }
            $targetNode = $elements[$i + 1]
            if ($targetNode -isnot [System.Management.Automation.Language.StringConstantExpressionAst]) { continue }
            $targetText = $targetNode.Value
            if ($targetText -notmatch '\.ps1$') { continue }
            $targetPath = Join-Path $Root ($targetText -replace '^\.[\\/]', '')
            if (-not (Test-Path -LiteralPath $targetPath -PathType Leaf)) { continue }
            if (-not $declaredCache.ContainsKey($targetPath)) { $declaredCache[$targetPath] = @(Get-DeclaredParameters $targetPath) }
            $declared = $declaredCache[$targetPath]
            for ($j = $i + 2; $j -lt $elements.Count; $j++) {
                $arg = $elements[$j]
                if ($arg -isnot [System.Management.Automation.Language.StringConstantExpressionAst]) { continue }
                $value = $arg.Value
                if ($value -notmatch '^-[A-Za-z]') { continue }
                if ($hostOptions -contains $value) { continue }
                if ($declared -notcontains $value.TrimStart('-')) {
                    $findings += [ordered]@{
                        caller = $script.FullName.Substring($Root.Length + 1).Replace('\', '/')
                        line = $arg.Extent.StartLineNumber
                        target = $targetText.Replace('\', '/')
                        parameter = $value
                    }
                }
            }
        }
    }
}
@($findings) | ConvertTo-Json -Depth 5 -Compress
"""


def _scan(root: Path) -> list[dict]:
    """스캐너를 임시 위치에서 실행한다.

    저장소 안에 작업용 임시 파일을 만들지 않는다. `build/` 아래에 두면 스캐너 자신의 제외
    규칙에 걸려 음성 변이 검증이 불가능해지기도 한다.
    """
    with tempfile.TemporaryDirectory(prefix="cpf-callsite-scan-") as workspace:
        scanner = Path(workspace) / "scan-callsite-parameters.ps1"
        scanner.write_text(SCANNER, encoding="utf-8")
        try:
            completed = subprocess.run(
                ["pwsh", "-NoProfile", "-File", str(scanner), "-Root", str(root)],
                capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=600)
        except (FileNotFoundError, subprocess.TimeoutExpired) as unavailable:  # pragma: no cover
            raise AssertionError(f"pwsh is required for this contract: {unavailable}") from unavailable
    assert completed.returncode == 0, completed.stdout + completed.stderr
    payload = (completed.stdout or "").strip()
    if not payload:
        return []
    parsed = json.loads(payload)
    return parsed if isinstance(parsed, list) else [parsed]


@lru_cache(maxsize=1)
def _findings() -> tuple[dict, ...]:
    return tuple(_scan(ROOT))


def test_harness_never_passes_an_undeclared_powershell_parameter() -> None:
    violations = [
        f"{item['caller']}:{item['line']} -> {item['target']} {item['parameter']}"
        for item in _findings()
    ]
    assert violations == [], (
        "CmdletBinding 없는 param() 블록은 알 수 없는 파라미터를 조용히 무시한다. "
        "호출부가 넘긴 옵션이 대상 스크립트에 선언되어 있어야 의도가 실제로 적용된다: "
        f"{violations}")


def test_scanner_actually_detects_an_undeclared_parameter() -> None:
    """게이트가 0건을 검사하는 빈 규칙이 아님을 음성 변이로 고정한다.

    저장소를 건드리지 않도록 임시 루트에 최소 재현 케이스를 만들어 검출을 확인한다.
    """
    with tempfile.TemporaryDirectory(prefix="cpf-callsite-probe-") as workspace:
        root = Path(workspace)
        (root / "target.ps1").write_text("param([string] $Declared)\n", encoding="utf-8")
        (root / "caller.ps1").write_text(
            "$a = @('-NoProfile','-File','./target.ps1','-Declared','x','-NotDeclared','y')\n",
            encoding="utf-8")
        found = _scan(root)
    assert [item["parameter"] for item in found] == ["-NotDeclared"], found
    assert found[0]["caller"] == "caller.ps1", found


def test_scanner_accepts_a_declared_parameter_and_host_options() -> None:
    with tempfile.TemporaryDirectory(prefix="cpf-callsite-ok-") as workspace:
        root = Path(workspace)
        (root / "target.ps1").write_text(
            "param([string] $Declared, [switch] $Flag)\n", encoding="utf-8")
        (root / "caller.ps1").write_text(
            "$a = @('-NoProfile','-File','./target.ps1','-Declared','x','-Flag')\n",
            encoding="utf-8")
        assert _scan(root) == []


def test_scanner_honours_parameter_aliases() -> None:
    with tempfile.TemporaryDirectory(prefix="cpf-callsite-alias-") as workspace:
        root = Path(workspace)
        (root / "target.ps1").write_text(
            "param([Alias('Repo')][string] $RepositoryRoot)\n", encoding="utf-8")
        (root / "caller.ps1").write_text(
            "$a = @('-NoProfile','-File','./target.ps1','-Repo','x')\n", encoding="utf-8")
        assert _scan(root) == []


def test_powershell_host_options_are_not_reported_as_violations() -> None:
    assert "-NoProfile" in POWERSHELL_HOST_OPTIONS
    assert "-File" in POWERSHELL_HOST_OPTIONS
    assert sys.version_info >= (3, 10)
    assert EXCLUDED_PATH_PARTS
