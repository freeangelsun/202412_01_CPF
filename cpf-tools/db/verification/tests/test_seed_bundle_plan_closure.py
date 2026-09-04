from __future__ import annotations
import json
import re
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SEED_MODEL = ROOT / "cpf-tools/db/canonical/seed-model.json"
SOURCE_PLAN = ROOT / "cpf-tools/db/config/database-source-plan.json"
OFFICIAL = ("mariadb", "postgresql", "oracle")


# 기대값은 정본에서 파생시킨다. 이 파일에 SystemCode 목록을 복제하면 정본이 바뀌어도
# 게이트가 조용히 통과한다(Harness §30.24 하드코딩 금지).
_IDENTITY_TESTS = ROOT / "cpf-tools/verification/tests"
if str(_IDENTITY_TESTS) not in sys.path:
    sys.path.insert(0, str(_IDENTITY_TESTS))
from test_cpf_system_identity_contract import (  # noqa: E402
    _declared_system_codes_by_runtimes,
    _non_system_identity_codes,
)

SEED_SYSTEM_CODE = re.compile(r"\(\s*'(?P<code>[A-Z0-9_]+)'\s*,")


def canonical_system_codes() -> set[str]:
    """SystemCode 를 가지는 Role 의 Product Runtime 이 실제로 선언한 System Identity."""
    return _declared_system_codes_by_runtimes()


def canonical_non_system_codes() -> set[str]:
    """Module Code / DB Prefix / topology 이름. Business System Identity 가 아니다."""
    return _non_system_identity_codes()
BUNDLE_KEYS = ("productSeedFiles", "optionalSampleSeedFiles", "testSeedFiles")

class SeedBundlePlanClosureTest(unittest.TestCase):
    def setUp(self) -> None:
        self.seed = json.loads(SEED_MODEL.read_text(encoding="utf-8"))
        self.plan = json.loads(SOURCE_PLAN.read_text(encoding="utf-8"))

    def test_statement_count_matches_canonical_array(self) -> None:
        self.assertEqual(len(self.seed["statements"]), self.seed["statementCount"])

    def test_every_canonical_seed_source_has_one_bundle_owner(self) -> None:
        expected = list(self.seed["canonicalPolicy"]["sourceFiles"])
        for vendor in OFFICIAL:
            assigned = []
            for key in BUNDLE_KEYS:
                assigned.extend(self.plan[vendor][key])
            self.assertCountEqual(expected, assigned, vendor)
            self.assertEqual(len(assigned), len(set(assigned)), f"{vendor}: duplicate bundle source")

    def test_gateway_seed_is_product_seed_for_all_vendors(self) -> None:
        for vendor in OFFICIAL:
            self.assertIn("61_adm_gateway_seed.sql", self.plan[vendor]["productSeedFiles"])

    def test_product_seed_defines_complete_operation_system_registry_baseline(self) -> None:
        statements = [
            statement
            for statement in self.seed["statements"]
            if statement.get("tableName") == "OPS_SYSTEM_REGISTRY"
            and statement.get("productionDefault") is True
        ]
        self.assertEqual(1, len(statements), "canonical Product seed must have one registry owner")
        statement = statements[0]
        self.assertEqual(["system_code"], statement["conflictColumns"])

        # OPS_SYSTEM_REGISTRY 는 **SystemCode 를 보유한 Runtime** 만 담는다.
        # CPF/CMN/ADM/GWY 같은 non-System Identity Code 를 여기에 넣으면 가상 SystemCode 가 생겨
        # System 을 키로 하는 거래 계약이 실체 없는 대상에 걸린다(Harness §30.10/§30.16.1).
        # 기대값은 정본 정책에서 파생시킨다. 이 파일에 목록을 복제하면 정본이 바뀌어도
        # 게이트가 조용히 통과한다.
        declared = canonical_system_codes()
        self.assertTrue(declared, "정본에서 SystemCode 보유 Runtime 을 하나도 찾지 못했다.")
        seeded = {m.group("code") for m in SEED_SYSTEM_CODE.finditer(statement["source"])}
        self.assertTrue(seeded, "Product seed 의 OPS_SYSTEM_REGISTRY 행을 읽지 못했다.")

        unknown = sorted(seeded - declared)
        self.assertEqual(
            [], unknown,
            "OPS_SYSTEM_REGISTRY 에 실체 없는 SystemCode 가 있다. 어떤 Runtime 도 선언하지 않은 "
            f"코드는 가상 System 을 만든다: {unknown}")
        forbidden = sorted(seeded & canonical_non_system_codes())
        self.assertEqual(
            [], forbidden,
            "Module Code / DB Prefix / topology 이름은 SystemCode 가 아니다. "
            f"OPS_SYSTEM_REGISTRY 에 등재하면 System 계약이 실체 없는 대상에 걸린다: {forbidden}")

        for vendor in OFFICIAL:
            generated = ROOT / f"cpf-tools/db/generated/current/{vendor}/cpf-platform-seed.sql"
            text = generated.read_text(encoding="utf-8")
            self.assertIn("OPS_SYSTEM_REGISTRY", text, str(generated))
            for system_code in sorted(seeded):
                self.assertIn(f"'{system_code}'", text, str(generated))

    def test_generated_product_bundles_contain_gateway_seed(self) -> None:
        for vendor in OFFICIAL:
            for area in ("source", "seed"):
                path = ROOT / f"cpf-tools/db/vendor/{vendor}/{area}/00_product_seed.sql"
                text = path.read_text(encoding="utf-8")
                self.assertIn("61_adm_gateway_seed.sql", text, str(path))
                self.assertIn("API_GATEWAY_READ", text, str(path))
                self.assertIn("GATEWAY_DASHBOARD", text, str(path))

    def test_source_and_lifecycle_seed_bundles_are_exact_mirrors(self) -> None:
        for vendor in OFFICIAL:
            for name in (
                "00_product_seed.sql",
                "00_optional_sample_seed.sql",
                "00_test_seed.sql",
            ):
                source = ROOT / f"cpf-tools/db/vendor/{vendor}/source/{name}"
                lifecycle = ROOT / f"cpf-tools/db/vendor/{vendor}/seed/{name}"
                self.assertEqual(
                    source.read_bytes(),
                    lifecycle.read_bytes(),
                    f"{vendor}: generated source/lifecycle drift: {name}",
                )

if __name__ == "__main__":
    unittest.main()
