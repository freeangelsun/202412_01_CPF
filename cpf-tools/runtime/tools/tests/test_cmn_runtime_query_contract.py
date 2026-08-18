from __future__ import annotations

import importlib.util
import json
import shutil
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/runtime/tools/verify-cpf-cmn-runtime-query-contract.py"

spec = importlib.util.spec_from_file_location("cmn_query_contract", SCRIPT)
if spec is None or spec.loader is None:
    raise RuntimeError("cannot load CMN query verifier")
MOD = importlib.util.module_from_spec(spec)
sys.modules["cmn_query_contract"] = MOD
spec.loader.exec_module(MOD)


class CmnRuntimeQueryContractTest(unittest.TestCase):
    def test_current_contract_passes(self):
        result = MOD.evaluate(ROOT)
        self.assertEqual("PASS", result["status"], result["findings"])
        self.assertEqual(14, result["statementCount"])

    def copy_fixture(self) -> Path:
        tmp = Path(tempfile.mkdtemp())
        for relative in [
            "cpf-tools/db/metadata/platform-runtime-query-contract.json",
            "cpf-tools/db/runtime-template/cmn",
            "cpf-starters/common/src/main/resources/cpf-sql/cmn",
            "cpf-starters/common/src/main/java/com/cpf/common/calendar/CmnJdbcCalendarStore.java",
            "cpf-starters/common/src/main/java/com/cpf/common/template/CmnJdbcTemplateStore.java",
        ]:
            src = ROOT / relative
            dst = tmp / relative
            dst.parent.mkdir(parents=True, exist_ok=True)
            if src.is_dir(): shutil.copytree(src, dst)
            else: shutil.copy2(src, dst)
        self.addCleanup(shutil.rmtree, tmp, ignore_errors=True)
        return tmp

    def test_hash_drift_fails(self):
        root = self.copy_fixture()
        path = root / "cpf-starters/common/src/main/resources/cpf-sql/cmn/calendar/find.sql"
        path.write_text(path.read_text() + "\n-- drift\n")
        self.assertEqual("FAIL", MOD.evaluate(root)["status"])

    def test_missing_declared_runtime_resource_fails(self):
        root = self.copy_fixture()
        path = root / "cpf-tools/db/runtime-template/cmn/calendar/find.sql"
        path.unlink()
        self.assertEqual("FAIL", MOD.evaluate(root)["status"])

    def test_parameter_count_mismatch_fails(self):
        root = self.copy_fixture()
        path = root / "cpf-tools/db/metadata/platform-runtime-query-contract.json"
        data = json.loads(path.read_text())
        cmn = next(m for m in data["modules"] if m["module"] == "cmn")
        cmn["statements"][0]["parameterCount"] += 1
        path.write_text(json.dumps(data))
        self.assertEqual("FAIL", MOD.evaluate(root)["status"])

    def test_consumer_reference_is_required(self):
        root = self.copy_fixture()
        path = root / "cpf-starters/common/src/main/java/com/cpf/common/calendar/CmnJdbcCalendarStore.java"
        path.write_text(path.read_text().replace("calendar/find.sql", "calendar/not-declared.sql"))
        self.assertEqual("FAIL", MOD.evaluate(root)["status"])

    def test_inline_sql_fails(self):
        root = self.copy_fixture()
        path = root / "cpf-starters/common/src/main/java/com/cpf/common/calendar/CmnJdbcCalendarStore.java"
        path.write_text(path.read_text() + '\nclass BadSql { String sql = "SELECT * FROM forbidden"; }\n')
        self.assertEqual("FAIL", MOD.evaluate(root)["status"])


if __name__ == "__main__":
    unittest.main()
