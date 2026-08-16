from __future__ import annotations
import hashlib,importlib.util,json,subprocess,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-approved-baseline-metadata.py"
spec=importlib.util.spec_from_file_location('baseline',SCRIPT);module=importlib.util.module_from_spec(spec);assert spec and spec.loader;spec.loader.exec_module(module)
class ApprovedBaselineTest(unittest.TestCase):
 def git(self,root,*args):subprocess.run(['git','-C',str(root),*args],check=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
 def fixture(self):
  temp=tempfile.TemporaryDirectory();root=Path(temp.name);self.git(root,'init');self.git(root,'config','user.email','test@example.com');self.git(root,'config','user.name','Test')
  migration_paths={}
  for vendor,logical in [('mariadb','global'),('postgresql','cpfDB'),('oracle','cpfDB')]:
   relative=Path(f'cpf-tools/db/vendor/{vendor}/migration/flyway/{logical}/V1__baseline.sql');path=root/relative;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(f'-- {vendor}\n',encoding='utf-8')
   migration_paths[f'{vendor}/{logical}']=relative.as_posix()
  self.git(root,'add','.');self.git(root,'commit','-m','baseline');sha=subprocess.check_output(['git','-C',str(root),'rev-parse','HEAD'],text=True).strip()
  packs={}
  for pack,relative in migration_paths.items():
   blob=subprocess.check_output(['git','-C',str(root),'show',f'{sha}:{relative}'])
   packs[pack]=[hashlib.sha256(blob).hexdigest()+' *V1__baseline.sql']
  metadata=root/'cpf-tools/db/metadata/CPF_BASELINE_MIGRATION_CHECKSUMS_B894157.json';metadata.parent.mkdir(parents=True,exist_ok=True);metadata.write_text(json.dumps({'schemaVersion':1,'baseCommit':sha,'packs':packs}),encoding='utf-8')
  self.git(root,'add','.');self.git(root,'commit','-m','metadata')
  return temp,root,metadata
 def test_valid_git_baseline(self):
  temp,root,_=self.fixture()
  try:self.assertEqual(3,module.validate(root)['migrationCount'])
  finally:temp.cleanup()
 def test_historical_mutation_rejected(self):
  temp,root,_=self.fixture()
  try:
   path=next((root/'cpf-tools/db/vendor/mariadb').rglob('V1__baseline.sql'));path.write_text('-- modified\n', encoding="utf-8");self.git(root,'add','.');self.git(root,'commit','-m','bad mutation')
   with self.assertRaises(module.BaselineError):module.validate(root)
  finally:temp.cleanup()
 def test_unsupported_vendor_rejected(self):
  temp,root,metadata=self.fixture()
  try:
   data=json.loads(metadata.read_text(encoding="utf-8"));data['packs']['h2/global']=['0'*64+' *V1__baseline.sql'];metadata.write_text(json.dumps(data), encoding="utf-8")
   with self.assertRaises(module.BaselineError):module.validate(root,False)
  finally:temp.cleanup()
 def test_invalid_checksum_entry_rejected(self):
  temp,root,metadata=self.fixture()
  try:
   data=json.loads(metadata.read_text(encoding="utf-8"));data['packs']['mariadb/global']=['bad'];metadata.write_text(json.dumps(data), encoding="utf-8")
   with self.assertRaises(module.BaselineError):module.validate(root,False)
  finally:temp.cleanup()
if __name__=='__main__':unittest.main()
