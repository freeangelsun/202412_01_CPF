"""공개 배포본의 Build/Run 권한을 정본에서만 파생하도록 고정한다.

Open Git 공개 배포본에서 Build/Test Capability 는 Public Source Development Surface 에만
허용한다. ADM/Gateway/Framework Internal/Batch Internal 처럼 Product Contract 상 Binary-only 인
Platform Component 는 공개 Consumer 가 실행·구성·검증할 수 있으나, 그 Component 의
Source/Module Build/Test/Publication Task 는 Public Release 에 존재해서는 안 된다.

증상 근거: 공개 트리에 ADM/Gateway Source 가 없다는 사실은 "cpf-tools/ 가 금지 경로라서"라는
부수효과였을 뿐, 명시적 계약으로 검증되지 않았다. 새 Component 가 추가되면 분류 없이
공개 Build Surface 로 새어 들어갈 수 있었다.

되돌리면 재발할 증상: 공개 사용자에게 내부 Component 의 build/test/publication 진입점이
노출되어 Public Product Boundary 가 깨진다. 내부 그룹으로 숨기는 것만으로는 충족되지 않는다.
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
POLICY = REPO_ROOT / "cpf-tools/governance/cpf-product-surface-policy.json"
CATALOG = REPO_ROOT / "cpf-tools/runtime/cpf-runtime-target-catalog.json"

# Build/Test 는 Source 를 공개하는 Surface 에만 허용된다.
SOURCE_ONLY_CAPABILITIES = {"BUILD", "TEST"}


def load(path: Path) -> dict:
    return json.loads(io.open(path, encoding="utf-8").read())


def surface_for(owner: str, module_owners: list[dict]) -> str | None:
    """가장 긴 prefix 가 이긴다. 경로 추론이 아니라 정본 소유자 선언으로 판정한다."""
    best: tuple[str, str] | None = None
    for entry in module_owners:
        prefix = str(entry.get("prefix", "")).rstrip("/")
        surface = entry.get("publicDistributionSurface")
        if not prefix or not surface:
            continue
        if owner == prefix or owner.startswith(prefix + "/"):
            if best is None or len(prefix) > len(best[0]):
                best = (prefix, surface)
    return best[1] if best else None


class PublicDistributionSurfaceContractTest(unittest.TestCase):
    def setUp(self) -> None:
        self.policy = load(POLICY)
        self.surfaces = self.policy["publicDistributionSurfaces"]
        self.module_owners = self.policy["moduleOwners"]

    def test_every_module_owner_declares_a_public_distribution_surface(self) -> None:
        # 분류가 없으면 Release 가 그 Component 를 어느 쪽으로 다뤄야 할지 알 수 없다.
        missing = [entry.get("prefix") for entry in self.module_owners
                   if not entry.get("publicDistributionSurface")]
        self.assertEqual([], missing,
                         f"publicDistributionSurface 미선언 Component 가 있다(fail-closed): {missing}")

    def test_declared_surfaces_exist_in_the_canonical_catalog(self) -> None:
        unknown = sorted({str(entry.get("publicDistributionSurface")) for entry in self.module_owners}
                         - set(self.surfaces))
        self.assertEqual([], unknown, f"정본에 없는 Public Distribution Surface: {unknown}")

    def test_build_and_test_are_only_granted_where_source_is_published(self) -> None:
        violations = []
        for name, spec in self.surfaces.items():
            capabilities = set(spec.get("capabilities", []))
            if capabilities & SOURCE_ONLY_CAPABILITIES and not spec.get("publishesSource"):
                violations.append(f"{name}: publishesSource=false 인데 {sorted(capabilities & SOURCE_ONLY_CAPABILITIES)}")
        self.assertEqual([], violations,
                         "Source 를 공개하지 않는 Surface 에 Build/Test 를 부여하면 안 된다: " + "; ".join(violations))

    def test_binary_runtime_surface_never_grants_build_or_test(self) -> None:
        spec = self.surfaces["PUBLIC_BINARY_RUNTIME"]
        granted = sorted(set(spec.get("capabilities", [])) & SOURCE_ONLY_CAPABILITIES)
        self.assertEqual([], granted,
                         f"Binary-only Component 에 Build/Test 를 부여하면 안 된다: {granted}")
        self.assertFalse(spec.get("publishesSource"),
                         "Binary-only Component 는 Source 를 공개하지 않는다.")

    def test_private_internal_surface_grants_nothing(self) -> None:
        spec = self.surfaces["PRIVATE_INTERNAL"]
        self.assertEqual([], list(spec.get("capabilities", [])),
                         "내부 Component 는 공개 Capability 를 갖지 않는다.")

    def test_every_runtime_target_owner_is_classified(self) -> None:
        catalog = load(CATALOG)
        unclassified = [entry.get("target") for entry in catalog.get("runtimes", [])
                        if surface_for(str(entry.get("owner") or ""), self.module_owners) is None]
        self.assertEqual([], unclassified,
                         f"Runtime Target 의 Public Distribution Surface 를 정본에서 찾지 못했다: {unclassified}")

    def test_runtime_provision_matches_the_declared_surface(self) -> None:
        # provision=source 는 Source 를 공개하는 Surface 에서만, binary 는 Source 를 공개하지 않는
        # Surface 에서만 성립한다. 두 정본이 어긋나면 Release projection 이 모순된다.
        catalog = load(CATALOG)
        violations = []
        for entry in catalog.get("runtimes", []):
            owner = str(entry.get("owner") or "")
            surface = surface_for(owner, self.module_owners)
            publishes_source = bool(self.surfaces.get(surface, {}).get("publishesSource"))
            provision = str(entry.get("provision") or "")
            if provision == "source" and not publishes_source:
                violations.append(f"{entry.get('target')}: provision=source 인데 surface={surface}")
            if provision == "binary" and publishes_source:
                violations.append(f"{entry.get('target')}: provision=binary 인데 surface={surface}")
        self.assertEqual([], violations, "; ".join(violations))


if __name__ == "__main__":
    unittest.main()
