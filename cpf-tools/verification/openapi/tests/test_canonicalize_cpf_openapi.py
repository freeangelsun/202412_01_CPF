from __future__ import annotations
import importlib.util,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/contracts/openapi/canonicalize-cpf-openapi.py"
spec=importlib.util.spec_from_file_location('canonical_openapi',SCRIPT);module=importlib.util.module_from_spec(spec);assert spec and spec.loader;spec.loader.exec_module(module)

def base(schema=None,path='/adm/api/things'):
 return {'openapi':'3.1.0','x-cpf-source-sha':'a'*40,'servers':[{'url':'x'}],'paths':{path:{'get':{'operationId':'admThingFind' if path.startswith('/adm/') else 'mbwThingFind','responses':{'200':{'description':'ok','content':{'application/json':{'schema':{'type':'array','items':schema or {'type':'string'}}}}}}}}}}
class CanonicalOpenApiTest(unittest.TestCase):
 def test_removes_sha_and_adds_security_errors(self):
  result,warnings=module.canonicalize(base(),'ADM')
  self.assertNotIn('x-cpf-source-sha',result);self.assertNotIn('servers',result)
  operation=result['paths']['/adm/api/things']['get'];self.assertEqual([{'cpfSession':[]}],operation['security'])
  for code in module.ERROR_CODES:self.assertIn(code,operation['responses'])
  self.assertEqual([],warnings)
  self.assertEqual(5,result['x-cpf-canonical-schema-version']);self.assertFalse(result['x-cpf-release-eligible'])
 def test_release_metadata_is_runtime_eligible_v5(self):
  result,warnings=module.canonicalize(base(),'ADM',True)
  self.assertEqual(5,result['x-cpf-canonical-schema-version']);self.assertTrue(result['x-cpf-release-eligible']);self.assertEqual([],warnings)
 def test_mbw_real_public_prefix(self):
  result,warnings=module.canonicalize(base(path='/api/v1/backoffice/things'),'MBW')
  self.assertEqual(1,result['x-cpf-public-operation-count']);self.assertEqual([],warnings)
 def test_mbw_legacy_bza_prefix_rejected(self):
  with self.assertRaises(module.ContractError):module.canonicalize(base(path='/api/bza/things'),'MBW')
 def test_duplicate_operation_rejected(self):
  value=base();value['paths']['/adm/api/other']=value['paths']['/adm/api/things']
  with self.assertRaises(module.ContractError):module.canonicalize(value,'ADM')
 def test_empty_success_schema_blocks_release(self):
  value=base();value['paths']['/adm/api/things']['get']['responses']['200']['content']['application/json']['schema']={}
  self.assertTrue(module.canonicalize(value,'ADM',False)[1])
  with self.assertRaises(module.ContractError):module.canonicalize(value,'ADM',True)
 def test_wrong_module_prefix_rejected(self):
  with self.assertRaises(module.ContractError):module.canonicalize(base(),'MBW')
if __name__=='__main__':unittest.main()
