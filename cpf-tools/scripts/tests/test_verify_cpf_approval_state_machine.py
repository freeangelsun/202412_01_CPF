import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).parents[1]/'verify-cpf-approval-state-machine.py'
def load():
 s=importlib.util.spec_from_file_location('a',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def fixture(self,secure=True):
  td=tempfile.TemporaryDirectory();r=Path(td.name)
  l=r/'cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java';l.parent.mkdir(parents=True)
  e=r/'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmApprovalEngineService.java';e.parent.mkdir(parents=True)
  if secure:
   l.write_text('class L{public Map<String,Object> execute(long id){repository.reserveExecution(id);return x;}}')
   e.write_text('class E{public Map<String, Object> decide(long id){int currentStep=1;participant=jdbc.queryForMap("SELECT * FROM p WHERE request_id=? AND operator_id=? AND step_no=?",id,actor,currentStep);return x;} public Object execute(long id){String idempotencyKey="k";reserveExecution(id);try{Object r=dispatcher.execute(req);completeExecution(id,r);return r;}catch(Exception x){markExecutionUnknown(id,x);throw x;}}}')
  else:
   l.write_text('class L{public Map<String,Object> execute(long id){repository.startExecution(id);repository.updateRequest(id,"EXECUTING");return x;}}')
   e.write_text('class E{public Map<String, Object> decide(long id){participant=jdbc.queryForMap("SELECT * FROM p WHERE request_id=? AND operator_id=?",id,actor);return x;} public Object execute(long id){return dispatcher.execute(req);}}')
  return td,r
 def test_secure_state_machine_passes(self):td,r=self.fixture();self.addCleanup(td.cleanup);self.assertEqual('PASS',load().verify(r)['status'])
 def test_double_cas_step_bypass_and_raw_execute_fail(self):td,r=self.fixture(False);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
if __name__=='__main__':unittest.main()
