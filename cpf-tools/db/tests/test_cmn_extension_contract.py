from __future__ import annotations

import os
import unittest
from pathlib import Path

ROOT = Path(os.environ.get("CPF_REPO_ROOT", Path(__file__).resolve().parents[3])).resolve()
JAVA = ROOT / "cpf-common/src/main/java/com/cpf/common"

class CmnExtensionContractTest(unittest.TestCase):
    def test_header_validation_rejects_blank_required_values(self):
        source = (JAVA / "validation/HeaderValidator.java").read_text(encoding="utf-8")
        self.assertEqual(3, source.count(".isBlank()"))
        self.assertNotIn(".isEmpty()", source)
        self.assertIn("header == null", source)
        self.assertIn("getTimestamp() == null", source)

    def test_common_services_extend_core_without_owning_engine(self):
        source = (JAVA / "common/base/CmnBaseService.java").read_text(encoding="utf-8")
        self.assertIn("extends CpfBaseService", source)
        self.assertNotIn("class CpfBaseService", source)

    def test_concrete_product_consumers_use_common_extension_boundary(self):
        sample = (JAVA / "sample/CmnSampleItemService.java").read_text(encoding="utf-8")
        code = (JAVA / "cde/service/CodeCacheService.java").read_text(encoding="utf-8")
        message = (JAVA / "msg/service/MessageCacheService.java").read_text(encoding="utf-8")
        self.assertIn("extends CmnBaseService", sample)
        self.assertIn("cmnTransactionManager", code)
        self.assertIn("cmnTransactionManager", message)

if __name__ == "__main__":
    unittest.main()
