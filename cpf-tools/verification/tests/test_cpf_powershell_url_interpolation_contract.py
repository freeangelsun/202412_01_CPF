"""PowerShell 문자열 보간이 query string 구분자를 삼키는 것을 정적으로 차단한다.

증상 근거: `"$BaseUrl/adm/api/observability/transactions/$transactionId?limit=50"` 는
PowerShell 에서 `${transactionId?limit}` 이라는 **변수 이름**으로 해석된다(PowerShell 변수명은
`?` 를 포함할 수 있다). 그 변수는 존재하지 않으므로 빈 문자열이 되고, 실제 요청 URL 은
`/adm/api/observability/transactions/=50` 이 된다. 오류 없이 200 이 돌아오고 결과만 비어 있어
"Runtime 이 상관관계를 못 만든다"는 잘못된 결론으로 이어졌다.

되돌리면 재발할 증상: 검증기가 엉뚱한 경로를 호출하고도 성공으로 보이거나, 원인을 알 수 없는
빈 결과로 실패한다.

같은 함정: `$var:`(scope 구분자), `$var-`(`-` 는 변수명에 못 들어가므로 안전), `$var.`(속성 접근).
여기서는 실제로 URL 을 망가뜨리는 `?` 와 `:` 만 검사한다.
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
SCAN_ROOTS = ("cpf-tools", "bin")
EXCLUDED_PARTS = ("/build/", "/bin/main/", "/node_modules/", "/.gradle/")

# 주석은 검사 대상이 아니다(설명문에 예시를 적을 수 있어야 한다).
COMMENT = re.compile(r"^\s*#")

# ${name} 으로 감싸지 않은 변수 뒤에 ? 또는 : 가 곧바로 오는 경우를 잡는다.
SWALLOWED = re.compile(r"\$([A-Za-z_][A-Za-z0-9_]*)([?:])[A-Za-z0-9_]")

# $env:VAR / $script:VAR 같은 scope 한정자는 정상 문법이다. ':' 쪽만 예외로 둔다.
POWERSHELL_SCOPES = frozenset(
    {"env", "script", "global", "local", "private", "using", "variable", "function", "workflow"}
)


class PowerShellUrlInterpolationContractTest(unittest.TestCase):
    def test_no_variable_swallows_a_url_separator(self) -> None:
        offenders: list[str] = []
        scanned = 0
        for root in SCAN_ROOTS:
            base = REPO_ROOT / root
            if not base.is_dir():
                continue
            for path in base.rglob("*.ps1"):
                if any(part in path.as_posix() for part in EXCLUDED_PARTS):
                    continue
                scanned += 1
                for number, line in enumerate(io.open(path, encoding="utf-8").read().splitlines(), 1):
                    if COMMENT.match(line):
                        continue
                    for match in SWALLOWED.finditer(line):
                        name, separator = match.group(1), match.group(2)
                        if separator == ":" and name.lower() in POWERSHELL_SCOPES:
                            continue
                        offenders.append(
                            f"{path.relative_to(REPO_ROOT).as_posix()}:{number}: {match.group(0)}"
                        )

        self.assertGreater(scanned, 0, "검사한 PowerShell 파일이 없다.")
        self.assertEqual(
            [], offenders,
            "PowerShell 변수명은 ? 와 : 를 포함할 수 있어 URL 구분자를 삼킨다. ${name} 으로 감싸라: "
            + "; ".join(offenders),
        )


if __name__ == "__main__":
    unittest.main()
