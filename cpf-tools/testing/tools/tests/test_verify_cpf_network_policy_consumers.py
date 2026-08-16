from __future__ import annotations
import importlib.util,pathlib,tempfile,unittest
SCRIPT=pathlib.Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-network-policy-consumers.py"
spec=importlib.util.spec_from_file_location('network_gate',SCRIPT);m=importlib.util.module_from_spec(spec);assert spec.loader;spec.loader.exec_module(m)
POLICY='class CpfNetworkEndpointPolicy { boolean allowPrivate,allowPublic,allowDns,requireTls,specialUseDenied; Object allowedCidrs,allowedPorts; void validateEndpoint(){} void validateResolvedAddresses(){} private void validateAddress(Address address){} }'
POLICY_TEST='supportsPrivateOnlyGatewayPolicy alwaysRejectsSpecialUseEvenWhenPrivateAllowed validatesIpv6AndDnsRebinding rejectsMalformedCorpus'
VALID={
 'cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfServiceEndpointRegistry.java':'validateEndpoint validateResolvedAddresses record ResolvedEndpoint pinnedAddress rejectMixedAddressClasses pinnedAddresses',
 'cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfPinnedHttpConnectorFactory.java':'remoteAddress endpoint.pinnedAddress() InetSocketAddress',
 'cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfWebClient.java':'resolvedEndpoint pinnedConnectorFactory.connector defaultHeader("Host"',
 'cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgTargetResolver.java':'CpfNetworkEndpointPolicy validateEndpoint validateResolvedAddresses pinned',
 'cpf-batch/agent/src/main/java/com/cpf/batch/agent/internal/PinnedArtifactHttpTransport.java':'CpfNetworkEndpointPolicy validateEndpoint validateResolvedAddresses openSocket InetSocketAddress(target.address()',
 'cpf-starters/integration/http/src/test/java/com/cpf/integration/http/internal/CpfServiceEndpointRegistryTest.java':'dnsAddressIsValidatedAndReturnedAsTheActualConnectionPin dnsRebindingToPrivateOrMixedAddressFailsBeforeConnectorCreation configuredPinMismatchFailsClosed',
 'cpf-starters/integration/http/src/test/java/com/cpf/integration/http/internal/CpfPinnedHttpConnectorFactoryContractTest.java':'createsConnectorFromValidatedPinnedAddress',
}
class T(unittest.TestCase):
 def fixture(self):
  self.tmp=tempfile.TemporaryDirectory();r=pathlib.Path(self.tmp.name)
  data={m.POLICY:POLICY,m.POLICY_TEST:POLICY_TEST,**VALID}
  for rel,text in data.items():p=r/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text,encoding='utf-8')
  return r
 def tearDown(self):
  if hasattr(self,'tmp'):self.tmp.cleanup()
 def test_valid(self):self.assertEqual('PASS',m.verify(self.fixture())['status'])
 def test_hostname_only_registry_fails(self):
  r=self.fixture();p=r/'cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfServiceEndpointRegistry.java';p.write_text(VALID[p.relative_to(r).as_posix()]+' return validatedBaseUrl(x);',encoding='utf-8')
  with self.assertRaises(m.GateError):m.verify(r)
 def test_connector_without_remote_pin_fails(self):
  r=self.fixture();p=r/'cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfPinnedHttpConnectorFactory.java';p.write_text('endpoint.pinnedAddress() InetSocketAddress',encoding='utf-8')
  with self.assertRaises(m.GateError):m.verify(r)
 def test_missing_rebinding_test_fails(self):
  r=self.fixture();p=r/m.POLICY_TEST;p.write_text(POLICY_TEST.replace('validatesIpv6AndDnsRebinding',''),encoding='utf-8')
  with self.assertRaises(m.GateError):m.verify(r)
if __name__=='__main__':unittest.main()
