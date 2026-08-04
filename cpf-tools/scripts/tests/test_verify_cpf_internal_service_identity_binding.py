import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).parents[1]/'verify-cpf-internal-service-identity-binding.py'
def load():
 s=importlib.util.spec_from_file_location('i',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def fixture(self,bound=True,implementation=False):
  td=tempfile.TemporaryDirectory();r=Path(td.name);p=r/'cpf-core/src/main/java/com/cpf/core/common/web/TransactionHeaderValidationInterceptor.java';p.parent.mkdir(parents=True)
  cert='private boolean hasClientCertificate(Request request,String callerServiceId,String callerInstanceId){return certificateSanMatches(request,callerServiceId,callerInstanceId);}' if bound else 'private boolean hasClientCertificate(Request request){return true;}'
  call='hasClientCertificate(request,callerServiceId,callerInstanceId)' if bound else 'hasClientCertificate(request)'
  p.write_text(f'''class X{{Verifier internalServiceIdentityVerifier; void shared(){{internalServiceIdentityVerifier.isTrusted(request, callerService, callerInstance);}} Verifier defaultIdentityVerifier(Env e){{boolean productionProfile=true;return (request,callerServiceId,callerInstanceId)->{{if({call})return true;if(productionProfile)return false;return true;}};}} {cert}}}''')
  if implementation:
   q=r/'cpf-starters/security/src/main/java/x/BoundVerifier.java';q.parent.mkdir(parents=True);q.write_text('class BoundVerifier implements CpfInternalServiceIdentityVerifier {}')
  return td,r
 def test_bound_certificate_passes(self):td,r=self.fixture();self.addCleanup(td.cleanup);self.assertEqual('PASS',load().verify(r)['status'])
 def test_unbound_certificate_fails(self):td,r=self.fixture(False);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
 def test_explicit_implementation_alone_does_not_make_unbound_default_safe(self):td,r=self.fixture(False,True);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
if __name__=='__main__':unittest.main()
