import copy
import hashlib
import importlib.util
import json
import pathlib
import subprocess
import sys
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[3]
DB = ROOT / "cpf-tools" / "db"
MODEL_PATH = DB / "canonical" / "generated-domain-schema.json"
VENDORS = ("mariadb", "postgresql", "oracle")


def load_renderer():
    spec = importlib.util.spec_from_file_location(
        "render_generated_domain_template",
        DB / "render_generated_domain_template.py",
    )
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


class GeneratedDomainVerifyRendererTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.model = json.loads(MODEL_PATH.read_text(encoding="utf-8-sig"))
        cls.renderer = load_renderer()

    def test_canonical_model_has_exact_minimal_transaction_shape(self):
        contract = self.renderer._domain_contract(self.model)
        self.assertEqual(2, len(contract["tables"]))
        self.assertEqual([14, 8], contract["columnCounts"])
        self.assertEqual(22, len(contract["columns"]))
        self.assertEqual(5, len(contract["indexes"]))
        self.assertEqual(8, len(contract["constraints"]))

    def test_renderer_fails_when_canonical_cardinality_drifts(self):
        drifted = copy.deepcopy(self.model)
        drifted["tables"][0]["columns"].pop()
        with self.assertRaisesRegex(ValueError, "canonical cardinality drift"):
            self.renderer.render_domain_verify("mariadb", drifted)

    def test_generated_verify_is_fail_closed_and_covers_every_named_object(self):
        fail_closed = {
            "mariadb": "SIGNAL SQLSTATE '45000'",
            "postgresql": "RAISE EXCEPTION",
            "oracle": "RAISE_APPLICATION_ERROR",
        }
        catalog = {
            "mariadb": "information_schema.table_constraints",
            "postgresql": "pg_constraint",
            "oracle": "user_constraints",
        }
        contract = self.renderer._domain_contract(self.model)
        canonical_names = [
            *contract["tableNames"],
            *(identity.split(".", 1)[1] for identity in contract["columns"]),
            *(identity.split(".", 1)[1] for identity in contract["indexes"]),
        ]
        for vendor in VENDORS:
            path = DB / "generated" / "domain-template" / vendor / "verify" / "90_verify.sql.template"
            sql = path.read_text(encoding="utf-8-sig")
            self.assertIn(fail_closed[vendor], sql, vendor)
            self.assertIn(catalog[vendor], sql, vendor)
            self.assertNotIn("@CPF_SCHEMA_NAME@", sql, vendor)
            self.assertIn("v_actual <> 2", sql, vendor)
            self.assertIn("v_actual <> 22", sql, vendor)
            self.assertIn("v_actual <> 5", sql, vendor)
            self.assertIn("v_actual <> 8", sql, vendor)
            self.assertIn("character_maximum_length = 34" if vendor != "oracle" else "char_length = 34", sql, vendor)
            self.assertIn("generated_domain_sample_verify", sql, vendor)
            self.assertIn("generated_domain_idempotency_verify", sql, vendor)
            for name in canonical_names:
                self.assertIn(self.renderer.tokenise(name), sql, f"{vendor}: {name}")
            for _, name, constraint_type in contract["constraints"]:
                physical_name = "PRIMARY" if vendor == "mariadb" and constraint_type == "PRIMARY KEY" else name
                self.assertIn(self.renderer.tokenise(physical_name), sql, f"{vendor}: {physical_name}")

    def test_manifest_hashes_cover_exact_generated_resource_set(self):
        expected_resources = {
            "install/10_empty_install.sql.template",
            "migration/V1____DOMAIN___domain.sql.template",
            "seed/20_product_seed.sql.template",
            "rollback/R1__remove___DOMAIN___domain.sql.template",
            "verify/90_verify.sql.template",
        }
        for vendor in VENDORS:
            root = DB / "generated" / "domain-template" / vendor
            manifest = json.loads((root / "manifest.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(expected_resources, set(manifest["artifacts"]), vendor)
            for relative, expected_hash in manifest["artifacts"].items():
                actual_hash = hashlib.sha256((root / relative).read_bytes()).hexdigest()
                self.assertEqual(expected_hash, actual_hash, f"{vendor}: {relative}")

    def test_checked_in_templates_match_deterministic_renderer(self):
        completed = subprocess.run(
            [
                sys.executable,
                str(DB / "render_generated_domain_template.py"),
                "--root",
                str(ROOT),
                "--check",
            ],
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, completed.returncode, completed.stdout + completed.stderr)


if __name__ == "__main__":
    unittest.main()
