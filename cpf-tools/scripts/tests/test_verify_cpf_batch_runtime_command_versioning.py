import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).parents[1]/'verify-cpf-batch-runtime-command-versioning.py'
def load():
 s=importlib.util.spec_from_file_location('v',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def fixture(self,secure=True):
  td=tempfile.TemporaryDirectory();root=Path(td.name)
  c=root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java';c.parent.mkdir(parents=True)
  r=root/'cpf-batch/contract/src/main/java/com/cpf/batch/api/RuntimeCommand.java';r.parent.mkdir(parents=True)
  j=root/'cpf-batch/control-server/src/main/java/com/cpf/batch/control/internal/JdbcRuntimeRegistry.java';j.parent.mkdir(parents=True)
  c.write_text('class C{void command(){'+('requireExpectedVersion(request);' if secure else '')+'client.command(request);}}')
  r.write_text('record RuntimeCommand(long expectedVersion){RuntimeCommand{'+('if(expectedVersion<0)throw x;' if secure else '')+'}}')
  version='expectedVersion' if secure else 'expectedVersion>0?expectedVersion:current'
  j.write_text(f'class J{{void updateDesiredState(String id,Object desired,long expectedVersion){{jdbc.update(sql.required("runtime-desired-state-update"),desired,id,{version});}}}}')
  return td,root
 def test_secure_chain_passes(self):td,r=self.fixture();self.addCleanup(td.cleanup);self.assertEqual('PASS',load().verify(r)['status'])
 def test_current_version_fallback_fails(self):td,r=self.fixture(False);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
if __name__=='__main__':unittest.main()
