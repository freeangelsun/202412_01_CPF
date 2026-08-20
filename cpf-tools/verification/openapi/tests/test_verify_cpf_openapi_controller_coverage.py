from __future__ import annotations
import importlib.util,json,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[1]/'verify-cpf-openapi-controller-coverage.py'
spec=importlib.util.spec_from_file_location('coverage',SCRIPT);module=importlib.util.module_from_spec(spec);assert spec and spec.loader;spec.loader.exec_module(module)
class CoverageTest(unittest.TestCase):
 def fixture(self,product='cpf-backoffice',source_path='/api/v1/backoffice/things',openapi_path=None,operation=True):
  temp=tempfile.TemporaryDirectory();root=Path(temp.name)
  if product=='cpf-backoffice':
   source_root=root/'cpf-backoffice/online/src/main/java';package='com/cpf/backoffice/online/things/controller';base='/api/v1/backoffice'
  else:
   source_root=root/'cpf-admin/src/main/java';package='com/cpf/admin/opr';base='/adm/api'
  p=source_root/package/'ThingController.java';p.parent.mkdir(parents=True)
  op='@Operation(operationId="thingFind")' if operation else ''
  p.write_text(f'''@RestController @RequestMapping("{base}") class ThingController {{ @GetMapping("/things") {op} public void find(){{}} }}''', encoding='utf-8')
  path=openapi_path or source_path;spec={'openapi':'3.1.0','paths':{path:{'get':{'operationId':'thingFind','responses':{'200':{'description':'ok'}}}}}}
  o=root/'openapi.json';o.write_text(json.dumps(spec), encoding='utf-8');return temp,root,o
 def test_backoffice_real_path_passes(self):
  temp,root,o=self.fixture()
  try:self.assertEqual(1,len(module.validate(root,'cpf-backoffice',o)[0]))
  finally:temp.cleanup()
 def test_cpf_controller_meta_annotation_passes(self):
  temp,root,o=self.fixture()
  try:
   source=next((root/'cpf-backoffice/online/src/main/java').rglob('ThingController.java'))
   source.write_text(source.read_text(encoding='utf-8').replace('@RestController','@CpfController'),encoding='utf-8')
   self.assertEqual(1,len(module.validate(root,'cpf-backoffice',o)[0]))
  finally:temp.cleanup()
 def test_reversed_or_legacy_backoffice_path_rejected(self):
  temp,root,o=self.fixture(openapi_path='/api/bza/things')
  try:
   with self.assertRaises(module.CoverageError):module.validate(root,'cpf-backoffice',o)
  finally:temp.cleanup()
 def test_method_path_drift_rejected(self):
  temp,root,o=self.fixture(openapi_path='/api/v1/backoffice/other')
  try:
   with self.assertRaises(module.CoverageError):module.validate(root,'cpf-backoffice',o)
  finally:temp.cleanup()
 def test_missing_operation_rejected(self):
  temp,root,o=self.fixture(operation=False)
  try:
   with self.assertRaises(module.CoverageError):module.validate(root,'cpf-backoffice',o)
  finally:temp.cleanup()
if __name__=='__main__':unittest.main()
