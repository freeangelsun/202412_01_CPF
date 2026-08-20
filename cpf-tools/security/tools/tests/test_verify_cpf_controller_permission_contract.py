from __future__ import annotations
import importlib.util,json,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[1]/'verify-cpf-controller-permission-contract.py'
spec=importlib.util.spec_from_file_location('controller_contract',SCRIPT);module=importlib.util.module_from_spec(spec);assert spec and spec.loader;spec.loader.exec_module(module)

class ControllerContractTest(unittest.TestCase):
 def write(self,root:Path,relative:str,text:str):
  path=root/relative;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(text,encoding='utf-8')
 def fixture(self,operation=True,permission=True,duplicate=False):
  directory=tempfile.TemporaryDirectory();root=Path(directory.name)
  op='@Operation(operationId="admThingCreate")' if operation else ''
  self.write(root,'cpf-admin/src/main/java/com/cpf/admin/opr/controller/ThingController.java',f'''package x; @RestController @RequestMapping("/adm/api/things") class ThingController {{ @PostMapping("/{{id}}") {op} public void create(){{}} }}''')
  if duplicate:self.write(root,'cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/ThingController.java',f'''package x; @RestController @RequestMapping("/api/v1/backoffice/things") class ThingController {{ @GetMapping {op} public void find(){{}} }}''')
  security='BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/things", "ADM:THING:WRITE");' if permission else 'BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/other", "ADM:OTHER:READ");'
  self.write(root,'cpf-admin/src/main/java/com/cpf/admin/opr/filter/AdmApiAuthFilter.java',f'class AdmApiAuthFilter{{String rule="{security}";}}')
  return directory,root
 def backoffice_fixture(self,manifest=True):
  directory=tempfile.TemporaryDirectory();root=Path(directory.name)
  self.write(root,'cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/directory/controller/BackofficeDirectoryController.java','''package x; @RestController @RequestMapping("/api/v1/backoffice/directory") class BackofficeDirectoryController { @PostMapping("/positions") @Operation(operationId="MBW_DIRECTORY_SAVE_POSITION") public void save(){} }''')
  if manifest:
   data={'schemaVersion':2,'apiResourceGroups':{'directory/positions':'EMPLOYEE'},'actionRules':[{'method':'POST','pathPattern':'*/**','actionCode':'WRITE'}]}
   self.write(root,'cpf-tools/db/metadata/backoffice-permission-manifest.json',json.dumps(data))
  return directory,root
 def test_real_overlay_stream_operation_id(self):
  root=Path(__file__).resolve().parents[4]
  records,errors,warnings=module.validate(root,True)
  self.assertEqual([],errors);self.assertEqual([],warnings)
  stream=next(record for record in records if record['path']=='/adm/api/gateway-registry/operations/stream')
  self.assertEqual('admGatewayOperationsStream',stream['operation_id'])
 def test_valid_operation_and_permission(self):
  directory,root=self.fixture()
  try:
   records,errors,warnings=module.validate(root,True);self.assertEqual(1,len(records));self.assertEqual([],errors);self.assertEqual([],warnings)
  finally:directory.cleanup()
 def test_missing_operation_id_rejected(self):
  directory,root=self.fixture(operation=False)
  try:self.assertTrue(module.validate(root,True)[1])
  finally:directory.cleanup()
 def test_missing_permission_rejected_in_strict_mode(self):
  directory,root=self.fixture(permission=False)
  try:self.assertTrue(any('permission' in error for error in module.validate(root,True)[1]))
  finally:directory.cleanup()
 def test_duplicate_operation_id_rejected(self):
  directory,root=self.fixture(duplicate=True)
  try:self.assertTrue(any('duplicate operationId' in error for error in module.validate(root,True)[1]))
  finally:directory.cleanup()
 def test_backoffice_real_path_is_discovered_and_manifest_authorizes(self):
  directory,root=self.backoffice_fixture()
  try:
   records,errors,warnings=module.validate(root,True);self.assertEqual('/api/v1/backoffice/directory/positions',records[0]['path']);self.assertEqual([],errors);self.assertEqual([],warnings)
  finally:directory.cleanup()
 def test_backoffice_missing_manifest_is_fail_closed(self):
  directory,root=self.backoffice_fixture(manifest=False)
  try:self.assertTrue(any('Backoffice permission manifest' in error for error in module.validate(root,True)[1]))
  finally:directory.cleanup()
if __name__=='__main__':unittest.main()
