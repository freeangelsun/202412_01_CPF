from __future__ import annotations
import importlib.util,pathlib,tempfile,unittest
SCRIPT=pathlib.Path(__file__).parents[1]/'verify-cpf-network-policy-consumers.py'
spec=importlib.util.spec_from_file_location('network_gate',SCRIPT);m=importlib.util.module_from_spec(spec);assert spec.loader;spec.loader.exec_module(m)
CONSUMER='class X { CpfNetworkEndpointPolicy p; void x(){p.validateEndpoint("x");p.validateResolvedAddresses("x",List.of());} }'
POLICY='class CpfNetworkEndpointPolicy { boolean allowPrivate,allowPublic,allowDns,requireTls,specialUseDenied; Object allowedCidrs,allowedPorts; void validateEndpoint(){} void validateResolvedAddresses(){} private void validateAddress(Address address){} }'
TEST='supportsPrivateOnlyGatewayPolicy alwaysRejectsSpecialUseEvenWhenPrivateAllowed validatesIpv6AndDnsRebinding rejectsMalformedCorpus'
class T(unittest.TestCase):
 def fixture(self):
  self.tmp=tempfile.TemporaryDirectory();r=pathlib.Path(self.tmp.name)
  data={**{rel:CONSUMER for rel in m.REQUIRED},m.POLICY:POLICY,m.TEST:TEST}
  for rel,text in data.items():p=r/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text,encoding='utf-8')
  return r
 def tearDown(self):
  if hasattr(self,'tmp'):self.tmp.cleanup()
 def test_valid(self):self.assertEqual('PASS',m.verify(self.fixture())['status'])
 def test_missing_consumer_fails(self):
  r=self.fixture();(r/next(iter(m.REQUIRED))).unlink()
  with self.assertRaises(m.GateError):m.verify(r)
 def test_stale_core_http_path_is_not_accepted(self):
  r=self.fixture();rel=next(iter(m.REQUIRED));p=r/rel;p.unlink();q=r/'cpf-core/src/main/java/com/cpf/core/common/http/CpfServiceEndpointRegistry.java';q.parent.mkdir(parents=True,exist_ok=True);q.write_text(CONSUMER,encoding='utf-8')
  with self.assertRaises(m.GateError):m.verify(r)
 def test_duplicate_classifier_fails(self):
  r=self.fixture();p=r/next(iter(m.REQUIRED));p.write_text(CONSUMER+' private boolean privateAddress(){return true;}',encoding='utf-8')
  with self.assertRaises(m.GateError):m.verify(r)
 def test_missing_rebinding_test_fails(self):
  r=self.fixture();p=r/m.TEST;p.write_text(TEST.replace('validatesIpv6AndDnsRebinding',''),encoding='utf-8')
  with self.assertRaises(m.GateError):m.verify(r)
if __name__=='__main__':unittest.main()
