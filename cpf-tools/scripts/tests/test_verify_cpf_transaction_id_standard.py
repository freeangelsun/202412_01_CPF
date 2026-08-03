import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[1]/'verify-cpf-transaction-id-standard.py'
def load():s=importlib.util.spec_from_file_location('g',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def root(self,bad=False):
  td=tempfile.TemporaryDirectory();r=Path(td.name);p=r/'cpf-core/src/main/java/com/cpf/core/common/web';p.mkdir(parents=True);(p/'TransactionHeaderValidationInterceptor.java').write_text('transaction == null isInfrastructureEndpoint validateRequiredHeaders(request) /actuator/ /v3/api-docs /swagger-ui',encoding='utf-8');q=r/'cpf-admin/src/main/java/x';q.mkdir(parents=True);(q/'C.java').write_text('@RestController @GetMapping '+('String globalId;' if bad else ''),encoding='utf-8');return td,r
 def test_pass_with_fallback(self):td,r=self.root();self.addCleanup(td.cleanup);self.assertEqual('PASS',load().verify(r)['status'])
 def test_legacy_fails(self):td,r=self.root(True);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
if __name__=='__main__':unittest.main()
