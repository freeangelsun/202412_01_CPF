"""PowerShell `List[object]` 에 array subexpression `@()` 를 쓰는 패턴을 정적으로 차단한다.

증상 근거: PowerShell 7.6 에서 `System.Collections.Generic.List[object]` 에 `@($list)` 를
적용하면 `System.ArgumentException: Argument types do not match` 로 던진다.
(같은 코드가 `List[string]` 에서는 정상 동작하고, `.ToArray()` 도 정상이다.)

실제 사례: `smoke-integrated-log-correlation.ps1` 의 `Find-CorrelationInFiles` 가
`return @($correlated)` 에서 끊겨, 1-WAS 통합 로그 상관관계 검증이 업무 단정에 닿지도 못하고
`Argument types do not match` 로만 실패했다. 원인이 함수 반환 지점이라 로그만 보고는
"ADM 조회 API 가 비었다"로 오인하기 쉬웠다.

되돌리면 재발할 증상: 상관관계 검증이 ArgumentException 으로 실패하고, File/DB/ADM 3중
증적 대조가 한 건도 수행되지 않는다.
"""

from __future__ import annotations

import io
import re
import sys
import unittest
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

REPO_ROOT = Path(__file__).resolve().parents[3]

EXCLUDED_PARTS = ("/build/", "/node_modules/", "/bin/")

# $name = New-Object [System.]Collections.Generic.List[object]
LIST_OF_OBJECT = re.compile(
    r"\$(\w+)\s*=\s*New-Object\s+(?:System\.)?Collections\.Generic\.List\[object\]",
    re.IGNORECASE,
)


def _owned_powershell_files() -> list[Path]:
    files: list[Path] = []
    for path in REPO_ROOT.rglob("*.ps1"):
        normalized = "/" + path.relative_to(REPO_ROOT).as_posix()
        if any(part in normalized for part in EXCLUDED_PARTS):
            continue
        files.append(path)
    return files


class PowerShellGenericListProjectionTest(unittest.TestCase):
    def test_list_of_object_is_not_wrapped_in_array_subexpression(self) -> None:
        violations: list[str] = []
        for path in _owned_powershell_files():
            try:
                text = io.open(path, encoding="utf-8").read()
            except (OSError, UnicodeDecodeError):
                continue
            names = {match.group(1) for match in LIST_OF_OBJECT.finditer(text)}
            if not names:
                continue
            relative = path.relative_to(REPO_ROOT).as_posix()
            for name in sorted(names):
                pattern = re.compile(r"@\(\s*\$" + re.escape(name) + r"\s*\)")
                for match in pattern.finditer(text):
                    line = text.count("\n", 0, match.start()) + 1
                    violations.append(f"{relative}:{line}: @(${name})")
        self.assertEqual(
            [],
            sorted(violations),
            "List[object] 에는 @() 대신 .ToArray() 를 쓴다. PowerShell 7.6 에서 @() 는 "
            "System.ArgumentException: Argument types do not match 로 던진다: "
            f"{sorted(violations)}",
        )


if __name__ == "__main__":
    unittest.main()
