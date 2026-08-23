from __future__ import annotations

import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
DB = ROOT / "cpf-tools/db"


class OfficialVendorSourceTypeContractTest(unittest.TestCase):
    def sql_paths(self, vendor: str) -> list[Path]:
        root = DB / "vendor" / vendor
        paths = sorted((root / "source").glob("*.sql")) + sorted((root / "install").glob("*.sql"))
        self.assertTrue(paths, vendor)
        return paths

    def assert_pattern_absent(self, vendor: str, pattern: str) -> None:
        compiled = re.compile(pattern)
        for path in self.sql_paths(vendor):
            self.assertIsNone(
                compiled.search(path.read_text(encoding="utf-8-sig")),
                path.relative_to(ROOT).as_posix(),
            )

    def pattern_count(self, vendor: str, pattern: str) -> int:
        compiled = re.compile(pattern)
        return sum(
            len(compiled.findall(path.read_text(encoding="utf-8-sig")))
            for path in self.sql_paths(vendor)
        )

    def test_bounded_binary_is_rendered_for_each_official_vendor(self):
        self.assert_pattern_absent("postgresql", r"(?i)\bVARBINARY\b")
        self.assertGreaterEqual(
            self.pattern_count("postgresql", r"(?i)\b(?:access_iv|refresh_iv)\s+BYTEA\b"), 4
        )
        self.assert_pattern_absent("oracle", r"(?i)\b(?:VARBINARY|BYTEA)\b")
        self.assertGreaterEqual(
            self.pattern_count("oracle", r"(?i)\b(?:access_iv|refresh_iv)\s+RAW\(32\)(?=\s|,|$)"), 4
        )

    def test_retired_split_schema_projections_are_absent(self):
        for vendor in ("mariadb", "postgresql", "oracle"):
            for name in ("20_cmn_schema.sql", "30_adm_schema.sql"):
                self.assertFalse((DB / "vendor" / vendor / "source" / name).exists(), f"{vendor}/{name}")

    def test_official_generator_owns_both_vendor_conversions(self):
        generator = (DB / "generator/generate-official-db-vendor-source.ps1").read_text(
            encoding="utf-8-sig"
        )
        self.assertIn("if($u -match '^VARBINARY\\(\\d+\\)$'){return 'BYTEA'}", generator)
        self.assertIn('if($u -match \'^VARBINARY\\((\\d+)\\)$\'){return "RAW($($Matches[1]))"}', generator)

    def test_shared_schema_output_uses_explicit_canonical_order(self):
        generator = (DB / "generator/generate-official-db-vendor-source.ps1").read_text(
            encoding="utf-8-sig"
        )
        self.assertIn("foreach($db in $logicalDatabases)", generator)
        self.assertIn("foreach($sourceFile in $sourceSchemaFiles)", generator)
        self.assertNotIn("foreach($db in $fileByDb.Keys)", generator)


if __name__ == "__main__":
    unittest.main()
