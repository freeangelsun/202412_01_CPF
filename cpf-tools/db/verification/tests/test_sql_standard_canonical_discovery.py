from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/db/verification/check-sql-standard.ps1"

class SqlStandardCanonicalDiscoveryTest(unittest.TestCase):
    def test_sql_standard_delegates_to_current_canonical_authorities(self):
        text = SCRIPT.read_text(encoding="utf-8-sig")
        self.assertIn("cpf-tools/db/canonical/platform-schema.json", text)
        self.assertIn("CANONICAL_JSON_RENDERER", text)
        self.assertIn("verify-cpf-db-schema-governance.py", text)
        self.assertIn("verify-cpf-db-vendor-semantic-parity.py", text)
        self.assertIn("render_vendor_pack.py", text)
        self.assertNotIn("vendor/mariadb/source/10_cpf_schema.sql", text)
        self.assertNotIn("cmnDB canonical table discovery is empty", text)

if __name__ == "__main__":
    unittest.main()
