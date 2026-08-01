import importlib.util,tempfile,unittest
from pathlib import Path
S=Path(__file__).resolve().parents[1]/'verify-cpf-network-policy-consumers.py';spec=importlib.util.spec_from_file_location('n',S);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
class NetworkConsumerTest(unittest.TestCase):
 def fixture(self):
  t=tempfile.TemporaryDirectory();r=Path(t.name)
  for rel,tokens in m.REQUIRED.items():
   p=r/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text('class X { '+ ' '.join(tokens)+' }', encoding="utf-8")
  p=r/'cpf-core/src/main/java/com/cpf/core/api/security/network/CpfNetworkEndpointPolicy.java';p.parent.mkdir(parents=True,exist_ok=True);p.write_text('allowPrivate allowPublic allowDns requireTls allowedCidrs allowedPorts specialUseDenied', encoding="utf-8")
  p=r/'cpf-core/src/test/java/com/cpf/core/api/security/network/CpfNetworkEndpointPolicyTest.java';p.parent.mkdir(parents=True,exist_ok=True);p.write_text('supportsPrivateOnlyGatewayPolicy alwaysRejectsSpecialUseEvenWhenPrivateAllowed validatesIpv6AndDnsRebinding', encoding="utf-8")
  return t,r
 def test_valid(self):
  t,r=self.fixture();self.addCleanup(t.cleanup);self.assertEqual([],m.validate(r))
 def test_missing_consumer(self):
  t,r=self.fixture();self.addCleanup(t.cleanup);(r/next(iter(m.REQUIRED))).unlink();self.assertTrue(m.validate(r))
 def test_duplicate_classifier(self):
  t,r=self.fixture();self.addCleanup(t.cleanup);p=r/list(m.REQUIRED)[1];p.write_text(p.read_text(encoding="utf-8")+' private boolean privateAddress(String x){return true;}', encoding="utf-8");self.assertTrue(any('duplicate' in x for x in m.validate(r)))
if __name__=='__main__':unittest.main()
