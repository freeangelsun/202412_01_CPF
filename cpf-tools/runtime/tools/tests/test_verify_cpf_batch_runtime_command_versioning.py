import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).parents[1]/'verify-cpf-batch-runtime-command-versioning.py'
def load():
 s=importlib.util.spec_from_file_location('v',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def fixture(self,secure=True):
  td=tempfile.TemporaryDirectory();root=Path(td.name)
  c=root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java';c.parent.mkdir(parents=True)
  q=root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeCommandRequest.java'
  a=root/'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java';a.parent.mkdir(parents=True)
  r=root/'cpf-batch/api/src/main/java/com/cpf/batch/api/RuntimeCommand.java';r.parent.mkdir(parents=True)
  j=root/'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/internal/JdbcRuntimeRegistry.java';j.parent.mkdir(parents=True)
  c.write_text('class C{void command(BatchRuntimeCommandRequest body){if(body.approvalRequestId==null)throw x;approvalService.execute(1L,body.reason,operatorId);}}')
  q.write_text('class BatchRuntimeCommandRequest{String approvalRequestId;String reason;}')
  if secure:
   a.write_text('class A{void executeRuntimeCommand(C c,R risk){if(risk.expectedVersion() == null)throw x;Map request=m();request.put("expectedVersion", risk.expectedVersion());}}')
   r.write_text('record RuntimeCommand(long expectedVersion){RuntimeCommand{if(expectedVersion<0)throw x;}}')
   j.write_text('class J{void updateDesiredState(String id,Object desired,long expectedVersion){if(expectedVersion<0)throw x;long current=read();if(current != expectedVersion)throw conflict;jdbc.update(sql.required("runtime-desired-state-update"),desired,id,expectedVersion);}}')
  else:
   a.write_text('class A{void executeRuntimeCommand(C c,R risk){Map request=m();request.put("expectedVersion", 0L);}}')
   r.write_text('record RuntimeCommand(long expectedVersion){}')
   j.write_text('class J{void updateDesiredState(String id,Object desired,long expectedVersion){long current=read();jdbc.update(sql.required("runtime-desired-state-update"),desired,id,expectedVersion>0?expectedVersion:current);}}')
  return td,root
 def test_secure_chain_passes(self):td,r=self.fixture();self.addCleanup(td.cleanup);self.assertEqual('PASS',load().verify(r)['status'])
 def test_current_version_fallback_fails(self):td,r=self.fixture(False);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
if __name__=='__main__':unittest.main()
