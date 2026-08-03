import csv,hashlib,importlib.util,json,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).parents[1]/'verify-cpf-development-evidence-integrity.py'
spec=importlib.util.spec_from_file_location('gate',SCRIPT);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
SHA='a'*40
class T(unittest.TestCase):
 def fixture(self,stale=False,missing=False):
  td=tempfile.TemporaryDirectory();root=Path(td.name);review=root/'review';ev=root/'evidence';review.mkdir();ev.mkdir();(ev/'ok.log').write_text('PASS\n')
  req_fields=['requirement_id','development_status','verification_status'];
  with (review/'REQUIREMENT_STATUS.csv').open('w',newline='',encoding='utf-8') as f:w=csv.DictWriter(f,fieldnames=req_fields);w.writeheader();w.writerow({'requirement_id':'R1','development_status':'완료','verification_status':'완료'})
  ff=['finding_id','개발GPT_상태','source_head','positive_exit_code','negative_exit_code','regression_exit_code','evidence_paths','execution_command','미완료사유']
  with (review/'QA_FINDING_REVALIDATION.csv').open('w',newline='',encoding='utf-8') as f:w=csv.DictWriter(f,fieldnames=ff);w.writeheader();w.writerow({'finding_id':'F1','개발GPT_상태':'완료','source_head':'b'*40 if stale else SHA,'positive_exit_code':'0','negative_exit_code':'0','regression_exit_code':'0','evidence_paths':'evidence/missing.log' if missing else 'evidence/ok.log','execution_command':'python gate.py','미완료사유':''})
  for n in ['TEST_AND_EVIDENCE.md','CHANGE_MANIFEST.csv']:(review/n).write_text('clean\n')
  files=[]
  for p in [ev/'ok.log',review/'REQUIREMENT_STATUS.csv',review/'QA_FINDING_REVALIDATION.csv',review/'TEST_AND_EVIDENCE.md',review/'CHANGE_MANIFEST.csv']:
   files.append({'path':p.relative_to(root).as_posix(),'sizeBytes':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()})
  (review/'PACKAGE_MANIFEST.json').write_text(json.dumps({'sourceHead':SHA,'files':files}))
  # package manifest cannot hash itself; verifier does not require self-entry
  return td,root
 def test_positive(self):
  td,root=self.fixture();self.addCleanup(td.cleanup);r=m.verify(root,Path('review'),SHA,SHA,1,1);self.assertEqual(r['status'],'PASS')
 def test_stale_fails(self):
  td,root=self.fixture(stale=True);self.addCleanup(td.cleanup)
  with self.assertRaises(m.GateError):m.verify(root,Path('review'),SHA,SHA,1,1)
 def test_missing_evidence_fails(self):
  td,root=self.fixture(missing=True);self.addCleanup(td.cleanup)
  with self.assertRaises(m.GateError):m.verify(root,Path('review'),SHA,SHA,1,1)
if __name__=='__main__':unittest.main()
