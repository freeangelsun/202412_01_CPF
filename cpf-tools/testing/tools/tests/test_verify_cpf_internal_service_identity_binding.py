from __future__ import annotations
import importlib.util,shutil,tempfile,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
SCRIPT=ROOT/"cpf-tools/verification/tools/verify-cpf-internal-service-identity-binding.py"
def load():
 s=importlib.util.spec_from_file_location("identity",SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
FILES=(
 "cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfWebContextFilter.java",
 "cpf-starters/web/src/main/java/com/cpf/web/context/CpfHttpInboundContextAdapter.java",
 "cpf-starters/web/src/main/java/com/cpf/web/context/CpfConfiguredIngressTrustResolver.java",
 "cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfRestClientInterceptor.java",
 "cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfWebClientConfig.java",
 "cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/domaincall/CpfHttpDomainRemoteTransport.java",
 "cpf-starters/web/src/main/java/com/cpf/web/context/CpfHttpOutboundContextAdapter.java",
 "cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfLocalServiceIdentity.java",
)
class T(unittest.TestCase):
 def fixture(self):
  td=tempfile.TemporaryDirectory();r=Path(td.name)
  for rel in FILES:
   q=r/rel;q.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(ROOT/rel,q)
  return td,r
 def test_bound_certificate_passes(self):
  td,r=self.fixture();self.addCleanup(td.cleanup);self.assertEqual("PASS",load().verify(r)["status"])
 def test_external_ingress_promotion_fails(self):
  td,r=self.fixture();self.addCleanup(td.cleanup);p=r/FILES[2];p.write_text(p.read_text(encoding="utf-8").replace("return new Decision(CpfHttpIngressTrust.UNTRUSTED_EXTERNAL, null)","return new Decision(CpfHttpIngressTrust.TRUSTED_INTERNAL, \"FORGED\")"),encoding="utf-8");self.assertRaises(Exception,load().verify,r)
 def test_raw_caller_header_promotion_fails(self):
  td,r=self.fixture();self.addCleanup(td.cleanup);p=r/FILES[2];p.write_text(p.read_text(encoding="utf-8").replace("String remoteAddress = request.getRemoteAddr();","String forged = request.getHeader(\"X-Caller-Channel\");\n        String remoteAddress = request.getRemoteAddr();"),encoding="utf-8");self.assertRaises(Exception,load().verify,r)
if __name__=="__main__":unittest.main()
