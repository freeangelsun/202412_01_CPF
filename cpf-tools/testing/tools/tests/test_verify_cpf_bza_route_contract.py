from __future__ import annotations
import importlib.util,tempfile,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
SCRIPT=ROOT/'cpf-tools/verification/tools/verify-cpf-bza-route-contract.py'
spec=importlib.util.spec_from_file_location('bza_compat',SCRIPT);module=importlib.util.module_from_spec(spec);assert spec.loader;spec.loader.exec_module(module)
class BzaCompatibilityContractTest(unittest.TestCase):
 def test_real_tree_delegates_current_backoffice(self):
  result=module.validate(ROOT);self.assertEqual(96,result['routes']);self.assertEqual(96,result['operations'])
 def test_retired_root_is_rejected(self):
  with tempfile.TemporaryDirectory() as tmp:
   target=Path(tmp);(target/'cpf-biz-admin').mkdir();
   with self.assertRaises(module.ContractError):module.validate(target)
if __name__=='__main__':unittest.main()
