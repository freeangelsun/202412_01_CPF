from __future__ import annotations
import importlib.util
from pathlib import Path
import tempfile
import unittest
SCRIPT=Path(__file__).parents[1]/"verify-cpf-audit-fail-closed.py"
spec=importlib.util.spec_from_file_location("audit_gate",SCRIPT); module=importlib.util.module_from_spec(spec); assert spec and spec.loader; spec.loader.exec_module(module)
class AuditFailClosedTest(unittest.TestCase):
    def fixture(self):
        root=Path(tempfile.mkdtemp())
        files={
          "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditLogService.java": 'delivery.enrichReservation(mandatory,c); if(mandatory==null){delivery.record(c,after,diff);return;} throw new IllegalStateException("ADM 감사 로그 조회 실패.");',
          "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditDeliveryService.java": "long id = reserve(command); recoverStaleRequested OPERATION_STATUS='UNKNOWN' markRetry(id, ex)",
        }
        for rel,text in files.items(): p=root/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text,encoding='utf-8')
        return root
    def test_valid(self): module.verify(self.fixture())
    def test_swallowed_enrichment_rejected(self):
        root=self.fixture();p=root/"cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditLogService.java";p.write_text(p.read_text(encoding="utf-8")+" catch(RuntimeException ex){log.warn(\"ADM mandatory audit 상세 보강 실패\");}",encoding='utf-8')
        with self.assertRaises(ValueError): module.verify(root)
if __name__=='__main__': unittest.main()
