from __future__ import annotations

import os
import unittest
from pathlib import Path

ROOT = Path(os.environ.get("CPF_REPO_ROOT", Path(__file__).resolve().parents[3])).resolve()
BASE = ROOT / "cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api/CpfBaseService.java"
COMMON = ROOT / "cpf-starters/common/src/main/java/com/cpf/common"
REFERENCE_SAMPLE = ROOT / "cpf-education/src/main/java/com/cpf/education/common/sample/CmnSampleItemService.java"


class CmnExtensionContractTest(unittest.TestCase):
    def test_base_service_is_owned_by_base_runtime(self):
        source = BASE.read_text(encoding="utf-8")
        self.assertIn("abstract class CpfBaseService", source)
        self.assertFalse((COMMON / "common/base/CmnBaseService.java").exists())

    def test_common_product_services_extend_base_without_reowning_engine(self):
        for relative in [
            "calendar/CmnCalendarService.java",
            "template/CmnTemplateManagementService.java",
        ]:
            source = (COMMON / relative).read_text(encoding="utf-8")
            self.assertIn("import com.cpf.foundation.api.CpfBaseService;", source, relative)
            self.assertIn("extends CpfBaseService", source, relative)
            self.assertNotIn("CmnBaseService", source, relative)

    def test_reference_sample_consumes_base_directly(self):
        source = REFERENCE_SAMPLE.read_text(encoding="utf-8")
        self.assertIn("import com.cpf.foundation.api.CpfBaseService;", source)
        self.assertIn("extends CpfBaseService", source)
        self.assertNotIn("com.cpf.common.common.base", source)

    def test_no_active_java_consumer_references_retired_cmn_base_service(self):
        offenders = []
        for root in [ROOT / "cpf-starters", ROOT / "cpf-education", ROOT / "cpf-admin",
                     ROOT / "cpf-biz-admin", ROOT / "cpf-batch", ROOT / "cpf-gateway"]:
            if not root.exists():
                continue
            for path in root.rglob("*.java"):
                if any(part in {"build", "out", "target", "node_modules"} for part in path.parts):
                    continue
                text = path.read_text(encoding="utf-8-sig", errors="replace")
                if "CmnBaseService" in text or "com.cpf.common.common.base" in text:
                    offenders.append(path.relative_to(ROOT).as_posix())
        self.assertEqual([], offenders)


if __name__ == "__main__":
    unittest.main()
