"""Runtime Target Catalog 가 가상 SystemCode 를 주장하지 못하게 한다.

증상 근거: catalog 는 `admin=ADM`, `gateway=GWY`, `local-runtime=LOCAL`,
`batch-control=BAT`, `local-batch=BAT`, `backoffice-web=MBW` 를 `systemCode` 로 들고 있었다.
이 Runtime 들의 architectureRole 은 SystemCode 를 갖지 않는다(PLATFORM_CONTROL_PLANE / GATEWAY /
TOPOLOGY / BATCH_CONTROL_PLANE / FRAMEWORK_INTERNAL / CHANNEL_FRONT). Module Code 나 topology
이름을 System Identity 로 승격한 값이며, 아무 소비자도 읽지 않는 채 정본만 오염시키고 있었다.

되돌리면 재발할 증상: System 을 키로 하는 거래 계약이 실체 없는 대상에 걸리고, 다음 작업자가
이 catalog 를 근거로 "ADM 은 SystemCode ADM 을 가진다"는 잘못된 전제를 다시 세운다.

기대값은 cpf-product-surface-policy.json 에서 파생한다. 목록을 복제하면 정본이 바뀌어도
게이트가 조용히 통과한다.
"""

from __future__ import annotations

import io
import json
import sys
import unittest
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

REPO_ROOT = Path(__file__).resolve().parents[3]
CATALOG = REPO_ROOT / "cpf-tools/runtime/cpf-runtime-target-catalog.json"
POLICY = REPO_ROOT / "cpf-tools/governance/cpf-product-surface-policy.json"


def load(path: Path) -> dict:
    return json.loads(io.open(path, encoding="utf-8").read())


def role_for(owner: str, module_owners: list[dict]) -> str | None:
    """가장 긴 prefix 가 이긴다. path/package 추론이 아니라 정본 소유자 선언으로 판정한다."""
    best: tuple[str, str] | None = None
    for entry in module_owners:
        prefix = str(entry.get("prefix", "")).rstrip("/")
        role = entry.get("architectureRole")
        if not prefix or not role:
            continue
        if owner == prefix or owner.startswith(prefix + "/"):
            if best is None or len(prefix) > len(best[0]):
                best = (prefix, role)
    return best[1] if best else None


class RuntimeTargetIdentityContractTest(unittest.TestCase):
    def setUp(self) -> None:
        self.catalog = load(CATALOG)
        self.policy = load(POLICY)
        self.roles = self.policy["architectureRoles"]
        self.module_owners = self.policy["moduleOwners"]
        self.non_system = set(self.policy["nonSystemIdentityCodes"]["codes"])

    def test_every_runtime_owner_resolves_to_a_canonical_role(self) -> None:
        unresolved = [r["target"] for r in self.catalog["runtimes"]
                      if role_for(r["owner"], self.module_owners) is None]
        self.assertEqual([], unresolved,
                         f"Runtime owner 의 architectureRole 을 정본에서 찾지 못했다: {unresolved}")

    def test_system_code_exists_only_where_the_role_has_one(self) -> None:
        violations = []
        for runtime in self.catalog["runtimes"]:
            role = role_for(runtime["owner"], self.module_owners)
            has = bool(self.roles.get(role, {}).get("hasSystemCode"))
            declared = runtime.get("systemCode")
            if declared and not has:
                violations.append(f"{runtime['target']}={declared}(role={role})")
            if has and not declared:
                violations.append(f"{runtime['target']}=missing(role={role})")
        self.assertEqual([], violations,
                         "SystemCode 는 hasSystemCode=true 인 Role 의 Runtime 에만 존재한다: "
                         + ", ".join(violations))

    def test_no_non_system_identity_code_is_declared_as_a_system_code(self) -> None:
        violations = [f"{r['target']}={r.get('systemCode')}" for r in self.catalog["runtimes"]
                      if r.get("systemCode") in self.non_system]
        self.assertEqual([], violations,
                         "Module Code / DB Prefix / topology 이름을 SystemCode 로 승격하면 안 된다: "
                         + ", ".join(violations))


if __name__ == "__main__":
    unittest.main()
