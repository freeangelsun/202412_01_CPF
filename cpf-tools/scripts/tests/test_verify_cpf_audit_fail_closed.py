from __future__ import annotations
import importlib.util, pathlib, tempfile, unittest
SCRIPT=pathlib.Path(__file__).parents[1]/"verify-cpf-audit-fail-closed.py"
spec=importlib.util.spec_from_file_location("audit_gate",SCRIPT); module=importlib.util.module_from_spec(spec); assert spec.loader; spec.loader.exec_module(module)
SERVICE='''class AdmAuditLogService { Object x(){ delivery.enrichReservation(mandatory,c); delivery.executeAudited(c,op,after); delivery.record(c,after,diff); throw new IllegalStateException("ADM 감사 로그 조회 실패."); throw ex; } }'''
DELIVERY='''class AdmAuditDeliveryService { PROPAGATION_REQUIRES_NEW; Object x(){ long id = reserve(command); T result = operation.get(); completeOperation(id, "FAILED", null, null); throw ex; } String sql="OPERATION_STATUS,DELIVERY_STATUS 'REQUESTED','PENDING' OPERATION_STATUS='UNKNOWN' DELIVERY_STATUS='RETRY' FOR UPDATE ATTEMPT_COUNT=ATTEMPT_COUNT+1 DELIVERY_STATUS='DELIVERED'"; void relayPending(){recoverStaleRequested();markRetry(id, ex);setMaxRows(RELAY_BATCH_SIZE);} @Scheduled void s(){} public List<Map<String, Object>> findDeliveries(){return jdbc.query();} public Map<String, Object> findDelivery(){return jdbc.queryForMap();} }'''
class AuditGateTest(unittest.TestCase):
 def fixture(self):
  self.tmp=tempfile.TemporaryDirectory(); root=pathlib.Path(self.tmp.name)
  for rel,text in {"cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditLogService.java":SERVICE,"cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditDeliveryService.java":DELIVERY}.items(): p=root/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text,encoding='utf-8')
  return root
 def tearDown(self):
  if hasattr(self,'tmp'): self.tmp.cleanup()
 def test_valid(self): self.assertEqual('PASS',module.verify(self.fixture())['status'])
 def test_missing_source_fails(self):
  root=self.fixture();(root/'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditDeliveryService.java').unlink()
  with self.assertRaises(module.GateError):module.verify(root)
 def test_reservation_after_operation_fails(self):
  root=self.fixture();p=root/'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditDeliveryService.java';p.write_text(DELIVERY.replace('long id = reserve(command); T result = operation.get();','T result = operation.get(); long id = reserve(command);'),encoding='utf-8')
  with self.assertRaises(module.GateError):module.verify(root)
 def test_missing_unknown_recovery_fails(self):
  root=self.fixture();p=root/'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditDeliveryService.java';p.write_text(DELIVERY.replace("OPERATION_STATUS='UNKNOWN'",'NO_UNKNOWN'),encoding='utf-8')
  with self.assertRaises(module.GateError):module.verify(root)
 def test_missing_lock_fails(self):
  root=self.fixture();p=root/'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditDeliveryService.java';p.write_text(DELIVERY.replace('FOR UPDATE','NO LOCK'),encoding='utf-8')
  with self.assertRaises(module.GateError):module.verify(root)
 def test_query_empty_swallow_fails(self):
  root=self.fixture();p=root/'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditLogService.java';p.write_text(SERVICE+' catch(DataAccessException ex){return List.of();}',encoding='utf-8')
  with self.assertRaises(module.GateError):module.verify(root)
if __name__=='__main__':unittest.main()
