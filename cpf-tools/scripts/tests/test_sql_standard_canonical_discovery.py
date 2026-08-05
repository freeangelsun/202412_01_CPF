from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / "cpf-tools/scripts/check-sql-standard.ps1"

class SqlStandardCanonicalDiscoveryTest(unittest.TestCase):
    def test_cmn_tables_are_discovered_from_canonical_schema(self):
        text = SCRIPT.read_text(encoding="utf-8-sig")
        self.assertIn('$_.logicalDatabase -ieq "cmnDB"', text)
        self.assertNotIn('$expectedCmnTables = @("cmn_business_calendar_day", "cmn_sample_item")', text)
        self.assertIn('cmnDB canonical table discovery is empty', text)

if __name__ == "__main__":
    unittest.main()
