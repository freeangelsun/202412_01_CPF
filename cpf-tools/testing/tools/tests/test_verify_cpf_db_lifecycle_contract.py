from __future__ import annotations
import importlib.util,json,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/db/verification/verify-cpf-db-lifecycle-contract.py";spec=importlib.util.spec_from_file_location('db_contract',SCRIPT);module=importlib.util.module_from_spec(spec);assert spec and spec.loader;spec.loader.exec_module(module)
class DbLifecycleTest(unittest.TestCase):
 def fixture(self):
  temp=tempfile.TemporaryDirectory();root=Path(temp.name);(root/'cpf-tools/db').mkdir(parents=True)
  vendors={}
  contracts={}
  for vendor in module.OFFICIAL:
   pack=f'cpf-tools/db/vendor/{vendor}/pack.json';p=root/pack;p.parent.mkdir(parents=True)
   migration=f'cpf-tools/db/vendor/{vendor}/migration/flyway'+('' if vendor=='mariadb' else '/{logicalDatabase}')
   rollback=f'cpf-tools/db/vendor/{vendor}/rollback'+('' if vendor=='mariadb' else '/{logicalDatabase}')
   p.write_text(json.dumps({'vendor':vendor,'status':'완료','runtimeVerification':'미검증','historicalMigrationRoot':f'cpf-tools/db/vendor/{vendor}/migration','historicalRollbackRoot':f'cpf-tools/db/vendor/{vendor}/rollback','migrationLocationPattern':migration,'rollbackLocationPattern':rollback}), encoding="utf-8")
   lifecycle={key:f'cpf-tools/db/vendor/{vendor}/{key}' for key in module.LIFECYCLE_KEYS};lifecycle['migration']=migration;lifecycle['rollback']=rollback
   vendors[vendor]={'pack':pack,'lifecycle':lifecycle}
   contracts[vendor]={'migrationRoot':migration,'rollbackRoot':rollback}
  manifest={'supportedVendors':list(module.OFFICIAL),'officialVendors':list(module.OFFICIAL),'candidateVendors':[],'vendors':vendors}
  contract={'officialVendors':list(module.OFFICIAL),'orderedStages':module.STAGES,'statusModel':{'development':'완료','runtimeVerification':'미검증'},'requiredStaticGates':[],'runtimeExecutor':'x.ps1','environmentManifestVariable':'CPF_DB_RUNTIME_MANIFEST','runtimeEnvironmentManifest':{'schemaVersion':1,'vendorOrder':list(module.OFFICIAL),'secretValuePolicy':'ENVIRONMENT_REFERENCE_ONLY','requiredEnvironmentKeys':{v:['X'] for v in module.OFFICIAL}},'backupContract':'backup.json','pitrContract':'pitr.json','dataRetentionContract':'retention.json','testDataPolicy':'testdata.json','performanceContract':'performance.json','vendorContracts':contracts}
  (root/'cpf-tools/db/vendor-pack-manifest.json').write_text(json.dumps(manifest), encoding="utf-8");(root/'cpf-tools/db/cpf-db-lifecycle-contract.json').write_text(json.dumps(contract), encoding="utf-8")
  return temp,root,manifest,contract
 def test_valid_contract(self):
  temp,root,_,_=self.fixture()
  try:self.assertEqual((3,9),module.validate(root,False))
  finally:temp.cleanup()
 def test_unsupported_vendor_rejected(self):
  temp,root,manifest,_=self.fixture();manifest['supportedVendors'].append('h2');(root/'cpf-tools/db/vendor-pack-manifest.json').write_text(json.dumps(manifest), encoding="utf-8")
  try:
   with self.assertRaises(module.DbContractError):module.validate(root,False)
  finally:temp.cleanup()
 def test_rollback_drift_rejected(self):
  temp,root,_,_=self.fixture();p=root/'cpf-tools/db/vendor/oracle/pack.json';d=json.loads(p.read_text(encoding="utf-8"));d['historicalRollbackRoot']='cpf-tools/db/vendor/oracle/migration/rollback';p.write_text(json.dumps(d), encoding="utf-8")
  try:
   with self.assertRaises(module.DbContractError):module.validate(root,False)
  finally:temp.cleanup()
 def test_stage_order_rejected(self):
  temp,root,_,contract=self.fixture();contract['orderedStages']=list(reversed(module.STAGES));(root/'cpf-tools/db/cpf-db-lifecycle-contract.json').write_text(json.dumps(contract), encoding="utf-8")
  try:
   with self.assertRaises(module.DbContractError):module.validate(root,False)
  finally:temp.cleanup()
if __name__=='__main__':unittest.main()
