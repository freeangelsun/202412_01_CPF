import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[1]/'verify-cpf-operator-trust-boundary.py'
def load():s=importlib.util.spec_from_file_location('g',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def fixture(self,bad=False):
  td=tempfile.TemporaryDirectory();r=Path(td.name)
  for base in ('cpf-admin','cpf-biz-admin'):
   p=r/base/'frontend/src/shared/cpfApi.ts';p.parent.mkdir(parents=True);p.write_text('URLSearchParams FormData Blob JSON.parse assertNoClientActorQuery assertNoClientActor fetch(x)',encoding='utf-8');
   if bad:
    q=r/base/'frontend/src/features/unsafe.ts';q.parent.mkdir(parents=True,exist_ok=True);q.write_text('fetch(x); requestUser',encoding='utf-8')
  c=r/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java';c.parent.mkdir(parents=True);c.write_text('@RequestAttribute("adm.operatorId") withServerActor saveJobDefinition transitionJobDefinition command( plan(',encoding='utf-8')
  t=r/'cpf-admin/src/test/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlControllerEndpointTest.java';t.parent.mkdir(parents=True);t.write_text('everyPrivilegedEndpointUsesAuthenticatedActorAndStripsNestedAliases validationErrorsAreAlways400AndNeverUnknownResult typedOwnerErrorsUseOneEndpointIndependentStatusMatrix unexpectedTransportFailureIsOnlyCaseMappedToUnknownResult',encoding='utf-8');return td,r
 def test_pass(self):td,r=self.fixture();self.addCleanup(td.cleanup);self.assertEqual('PASS',load().verify(r)['status'])
 def test_frontend_bypass_fails(self):td,r=self.fixture(True);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
if __name__=='__main__':unittest.main()
