import json
import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
RUNTIME_CONSUMER = (
    ROOT
    / "cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/sql"
    / "CpfVendorResourceRoot.java"
)


class VendorPackSchemaRuntimeContractTest(unittest.TestCase):
    def test_runtime_consumer_accepts_the_exact_current_official_pack_schema(self):
        """Official DB pack producers and the JVM runtime have one exact schema contract."""
        source = RUNTIME_CONSUMER.read_text(encoding="utf-8")
        match = re.search(
            r"SUPPORTED_PACK_SCHEMA_VERSION\s*=\s*(\d+)\s*;",
            source,
        )
        self.assertIsNotNone(match, "runtime schema-version consumer is required")
        runtime_version = int(match.group(1))

        for vendor in ("mariadb", "postgresql", "oracle"):
            with self.subTest(vendor=vendor):
                manifest = json.loads(
                    (ROOT / f"cpf-tools/db/vendor/{vendor}/pack.json").read_text(encoding="utf-8")
                )
                self.assertEqual(vendor, manifest["vendor"])
                self.assertEqual(runtime_version, manifest["schemaVersion"])


if __name__ == "__main__":
    unittest.main()
