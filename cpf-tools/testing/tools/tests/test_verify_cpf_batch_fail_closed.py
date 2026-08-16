from __future__ import annotations
import importlib.util
from pathlib import Path
import tempfile
import unittest

SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-batch-fail-closed.py"
spec = importlib.util.spec_from_file_location("batch_fail_closed", SCRIPT)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)


class BatchFailClosedTest(unittest.TestCase):
    def fixture(self) -> Path:
        root = Path(tempfile.mkdtemp())
        files = {
            "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java": '''ResponseEntity<Map<String, Object>> instances(long x){ try{} catch(Exception e){return ResponseEntity.status(503).body(Map.of("stale", true, "partial", true));}}''',
            "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlClient.java": '''void x(Object value){if (value == null) throw new E("BAT_OWNER_EMPTY_RESPONSE");}''',
            "cpf-admin/src/main/java/com/cpf/admin/opr/batch/RemoteCpfBatchOperationsAdapter.java": '''void x(){if (result.unknown()){} if (!result.success()){} if(v==null)throw new E("BAT Owner 목록 응답 본문이 없습니다."); if(v==null)throw new E("BAT Owner 상세 응답 본문이 없습니다.");}''',
            "cpf-batch/control-plane/src/main/java/com/cpf/batch/control/compat/BatInternalOperationsController.java": '''actorResolver.actor(request,textOrNull(payload,"requestUser"),"requestUser");''',
        }
        for rel, text in files.items():
            path = root / rel
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text, encoding="utf-8")
        return root

    def test_valid(self):
        module.verify(self.fixture())

    def test_http_200_empty_failure_rejected(self):
        root = self.fixture()
        p = root / "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java"
        p.write_text('Map<String, Object> instances(long x){return Map.of("stale", true, "partial", true);}', encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)

    def test_null_to_empty_row_rejected(self):
        root = self.fixture()
        p = root / "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlClient.java"
        p.write_text('Object row(Object value){return value == null ? new CpfDataRow() : value;}', encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)


if __name__ == "__main__":
    unittest.main()
