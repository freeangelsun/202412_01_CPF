"""Runtime 검증기가 존재하지 않는 operationId 를 실어 보내지 못하게 한다.

증상 근거: `smoke-integrated-log-correlation.ps1` 이 `admLogSearch`,
`admObservabilityTransaction`, `admTransactionGroupTimeline`,
`admObservabilityFileLogRecovery` 를 `X-Target-Operation-Id` 로 보냈다. 네 값 모두 Source 에
존재하지 않는 이름이었다. `smoke-log-policy-runtime.ps1` 은 모든 ADM 호출에 기본값
`getAdmReadiness` 를 그대로 실어 보냈다. SystemCode 를 갖지 않는 ADM 은 Operation Access
Policy 대상이 아니어서 Runtime 이 이 값을 거절하지 않았고, 그 결과 검증기는 "Header 계약을
지키는 것처럼 보이지만 실제로는 아무 값이나 통과하는" False Green 을 만들었다.

되돌리면 재발할 증상: Header 계약 위반이 Runtime 검증을 통과하고, 나중에 Operation 단위
접근통제를 켠 순간 원인을 알 수 없는 대량 409 로 드러난다.
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
SMOKE_DIR = REPO_ROOT / "cpf-tools" / "runtime" / "tools"

# Source 정본: @Operation / @CpfOnlineTransaction 어느 쪽이든 operationId 선언 형태는 같다.
JAVA_OPERATION_ID = re.compile(r'operationId\s*=\s*"([^"]+)"')

# 검증기가 실제로 Header 에 싣는 값만 모은다.
PS_HEADER_LITERAL = re.compile(r"""X-Target-Operation-Id["']?\s*=\s*["']([^"']+)["']""")
PS_HEADER_BUILDER = re.compile(
    r"""(?:New-CpfAdmHeaders|New-AdmAuthHeaders|Merge-OperationHeaders(?:\s+\$\w+)?)\s+'([^']+)'"""
)

# 값이 호출 인자로 주입되는 자리는 이 게이트가 아니라 호출자 쪽에서 검사된다.
PS_VARIABLE = re.compile(r"^\$")

JAVA_ROOTS = ("cpf-admin", "cpf-backoffice", "cpf-education", "cpf-member", "cpf-external",
              "cpf-batch", "cpf-gateway", "cpf-starters", "cpf-common", "cpf-backoffice-web")
EXCLUDED_PARTS = ("/build/", "/bin/", "/node_modules/", "/generated/")


def declared_operation_ids() -> set[str]:
    declared: set[str] = set()
    for root in JAVA_ROOTS:
        base = REPO_ROOT / root
        if not base.is_dir():
            continue
        for path in base.rglob("*.java"):
            posix = path.as_posix()
            if any(part in posix for part in EXCLUDED_PARTS):
                continue
            declared.update(JAVA_OPERATION_ID.findall(io.open(path, encoding="utf-8").read()))
    return declared


class SmokeOperationIdContractTest(unittest.TestCase):
    def test_every_smoke_operation_id_exists_in_source(self) -> None:
        declared = declared_operation_ids()
        self.assertTrue(declared, "Source 에서 operationId 선언을 하나도 찾지 못했다.")

        unknown: list[str] = []
        checked = 0
        for path in sorted(SMOKE_DIR.glob("smoke-*.ps1")):
            text = io.open(path, encoding="utf-8").read()
            used = set(PS_HEADER_LITERAL.findall(text)) | set(PS_HEADER_BUILDER.findall(text))
            for operation_id in sorted(used):
                if PS_VARIABLE.match(operation_id):
                    continue
                checked += 1
                if operation_id not in declared:
                    unknown.append(f"{path.name}: {operation_id}")

        self.assertGreater(checked, 0, "검증기에서 operationId 사용처를 찾지 못했다.")
        self.assertEqual(
            [], unknown,
            "Runtime 검증기가 Source 에 없는 operationId 를 X-Target-Operation-Id 로 보낸다: "
            + ", ".join(unknown),
        )


if __name__ == "__main__":
    unittest.main()
