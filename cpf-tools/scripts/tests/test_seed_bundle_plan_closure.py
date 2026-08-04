from __future__ import annotations
import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SEED_MODEL = ROOT / "cpf-tools/db/canonical/seed-model.json"
SOURCE_PLAN = ROOT / "cpf-tools/config/database-source-plan.json"
OFFICIAL = ("mariadb", "postgresql", "oracle")
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

    def test_generated_product_bundles_contain_gateway_seed(self) -> None:
        for vendor in OFFICIAL:
            for area in ("source", "seed"):
                path = ROOT / f"cpf-tools/db/vendor/{vendor}/{area}/00_product_seed.sql"
                text = path.read_text(encoding="utf-8")
                self.assertIn("61_adm_gateway_seed.sql", text, str(path))
                self.assertIn("API_GATEWAY_READ", text, str(path))
                self.assertIn("GATEWAY_DASHBOARD", text, str(path))

if __name__ == "__main__":
    unittest.main()
