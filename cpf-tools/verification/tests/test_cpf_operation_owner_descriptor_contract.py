"""Operation Owner descriptor 가 정본 Identity 계약과 어긋나지 못하게 한다.

Operation 의 소유자는 path/package/module 이름 추론이 아니라 명시적 component descriptor
(META-INF/cpf/runtime-component.properties, generated-domain.properties)로 판정한다.
descriptor 가 정본과 다른 주장을 하면 Runtime 은 잘못된 Owner 로 System 계약을 적용한다.

증상 근거: Identity 정정 이전에는 ADM/Gateway/topology 같은 SystemCode 없는 Component 에도
가상 SystemCode 가 붙어 있었고, 그 값이 System 을 키로 하는 거래 계약에 그대로 쓰였다.
descriptor 로 경계를 옮긴 뒤에도 같은 오염이 descriptor 안에서 재현될 수 있다.

되돌리면 재발할 증상: SystemCode 를 갖지 않는 Role 의 Component 가 Business System 계약의
대상이 되어, 실체 없는 System 으로 거래 검증이 통과하거나 반대로 정상 거래가 거절된다.

기대값은 cpf-product-surface-policy.json 에서 파생한다.
"""

from __future__ import annotations

import io
import json
import re
import sys
import unittest
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

REPO_ROOT = Path(__file__).resolve().parents[3]
POLICY = REPO_ROOT / "cpf-tools/governance/cpf-product-surface-policy.json"
DESCRIPTOR_NAME = "runtime-component.properties"
EXCLUDED_PARTS = ("/build/", "/bin/main/", "/node_modules/")


def descriptors() -> list[tuple[Path, dict[str, str]]]:
    found = []
    for path in REPO_ROOT.rglob(DESCRIPTOR_NAME):
        posix = path.as_posix()
        if any(part in posix for part in EXCLUDED_PARTS):
            continue
        values: dict[str, str] = {}
        for line in io.open(path, encoding="utf-8").read().splitlines():
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            values[key.strip()] = value.strip()
        found.append((path, values))
    return found


class OperationOwnerDescriptorContractTest(unittest.TestCase):
    def setUp(self) -> None:
        policy = json.loads(io.open(POLICY, encoding="utf-8").read())
        self.roles = policy["architectureRoles"]
        self.non_system = set(policy["nonSystemIdentityCodes"]["codes"])
        self.found = descriptors()

    def test_descriptors_exist(self) -> None:
        self.assertTrue(self.found, f"{DESCRIPTOR_NAME} 를 하나도 찾지 못했다.")

    def test_declared_role_is_canonical(self) -> None:
        unknown = [f"{path.relative_to(REPO_ROOT).as_posix()}={values.get('architectureRole')}"
                   for path, values in self.found
                   if values.get("architectureRole") not in self.roles]
        self.assertEqual([], unknown,
                         "descriptor 가 정본에 없는 architectureRole 을 선언했다: " + ", ".join(unknown))

    def test_system_code_presence_matches_the_declared_role(self) -> None:
        violations = []
        for path, values in self.found:
            role = values.get("architectureRole")
            has = bool(self.roles.get(role, {}).get("hasSystemCode"))
            declared = values.get("systemCode")
            name = path.relative_to(REPO_ROOT).as_posix()
            if declared and not has:
                violations.append(f"{name}: role={role} 인데 systemCode={declared}")
            if has and not declared:
                violations.append(f"{name}: role={role} 인데 systemCode 가 없다")
        self.assertEqual([], violations, "; ".join(violations))

    def test_system_code_is_never_a_non_system_identity_code(self) -> None:
        violations = [f"{path.relative_to(REPO_ROOT).as_posix()}={values.get('systemCode')}"
                      for path, values in self.found
                      if values.get("systemCode") in self.non_system]
        self.assertEqual([], violations,
                         "Module Code / DB Prefix / topology 이름은 SystemCode 가 아니다: "
                         + ", ".join(violations))

    def test_scan_package_is_declared_and_specific(self) -> None:
        # package 는 명시 선언들 중 하나를 고르는 선택자다. 비어 있으면 추론으로 되돌아간다.
        weak = [path.relative_to(REPO_ROOT).as_posix() for path, values in self.found
                if not re.fullmatch(r"[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*){2,}",
                                    values.get("scanPackage", ""))]
        self.assertEqual([], weak,
                         "descriptor 는 충분히 구체적인 scanPackage 를 선언해야 한다: " + ", ".join(weak))


if __name__ == "__main__":
    unittest.main()
