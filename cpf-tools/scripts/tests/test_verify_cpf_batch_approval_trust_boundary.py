import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).parents[1]/'verify-cpf-batch-approval-trust-boundary.py'
def load():
 s=importlib.util.spec_from_file_location('a',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def fixture(self,secure=True):
  td=tempfile.TemporaryDirectory();r=Path(td.name)
  f=r/'cpf-admin/frontend/src/features/batch-runtime-control/api.ts';f.parent.mkdir(parents=True)
  c=r/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java';c.parent.mkdir(parents=True)
  e=r/'cpf-batch/control-server/src/main/java/com/cpf/batch/control/RuntimeCommandExecutor.java';e.parent.mkdir(parents=True)
  if secure:
   f.write_text('interface BatchRuntimeCommandRequest { approvalRequestId:string }')
   c.write_text('class C{Set CLIENT_ACTOR_FIELDS=Set.of("approvedBy");void command(){approvedCommandDispatcher.resolve(request);}}')
   e.write_text('class E{void validate(){approvalValidationPort.verify(command);if(command.approvedBy().equals(command.requestedBy()))throw x;}}')
  else:
   f.write_text('interface BatchRuntimeCommandRequest { approvedBy:string; approvalRequestId:string }')
   c.write_text('class C{void command(){requireCommandField(request,"approvedBy");client.command(request);}}')
   e.write_text('class E{void validate(){if(command.approvedBy().equals(command.requestedBy()))throw x;}}')
  return td,r
 def test_secure_server_resolution_passes(self):td,r=self.fixture();self.addCleanup(td.cleanup);self.assertEqual('PASS',load().verify(r)['status'])
 def test_browser_approval_and_separation_only_fails(self):td,r=self.fixture(False);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
if __name__=='__main__':unittest.main()
