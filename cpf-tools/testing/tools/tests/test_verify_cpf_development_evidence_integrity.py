import csv,hashlib,importlib.util,json,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-development-evidence-integrity.py"
spec=importlib.util.spec_from_file_location('gate',SCRIPT);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
SHA='a'*40
BASELINE='c'*40

def digest(path:Path)->str:return hashlib.sha256(path.read_bytes()).hexdigest()

class T(unittest.TestCase):
 def fixture(self,stale=False,missing=False):
  td=tempfile.TemporaryDirectory();root=Path(td.name);review=root/'review';ev=root/'evidence';review.mkdir();ev.mkdir();(ev/'F1.log').write_text('PASS\n')
  req_fields=['requirement_id','development_status','verification_status'];
  with (review/'REQUIREMENT_STATUS.csv').open('w',newline='',encoding='utf-8') as f:w=csv.DictWriter(f,fieldnames=req_fields);w.writeheader();w.writerow({'requirement_id':'R1','development_status':'완료','verification_status':'완료'})
  ff=['finding_id','개발GPT_상태','source_head','positive_exit_code','negative_exit_code','regression_exit_code','evidence_paths','execution_command','미완료사유']
  with (review/'QA_FINDING_REVALIDATION.csv').open('w',newline='',encoding='utf-8') as f:w=csv.DictWriter(f,fieldnames=ff);w.writeheader();w.writerow({'finding_id':'F1','개발GPT_상태':'완료','source_head':'b'*40 if stale else SHA,'positive_exit_code':'0','negative_exit_code':'0','regression_exit_code':'0','evidence_paths':'evidence/missing.log' if missing else 'evidence/F1.log','execution_command':'python gate.py','미완료사유':''})
  (review/'TEST_AND_EVIDENCE.md').write_text('clean\n')
  # CHANGE_MANIFEST must verify real payload bytes and is itself part of the package payload.
  with (review/'CHANGE_MANIFEST.csv').open('w',newline='',encoding='utf-8') as f:
   w=csv.DictWriter(f,fieldnames=['path','change_type','size_bytes','sha256','category']);w.writeheader();p=ev/'F1.log';w.writerow({'path':'evidence/F1.log','change_type':'MODIFIED','size_bytes':p.stat().st_size,'sha256':digest(p),'category':'EVIDENCE'})
  payload=[ev/'F1.log',review/'REQUIREMENT_STATUS.csv',review/'QA_FINDING_REVALIDATION.csv',review/'TEST_AND_EVIDENCE.md',review/'CHANGE_MANIFEST.csv']
  files=[{'path':p.relative_to(root).as_posix(),'sizeBytes':p.stat().st_size,'sha256':digest(p)} for p in payload]
  manifest=review/'PACKAGE_MANIFEST.json';manifest.write_text(json.dumps({'baselineSha':BASELINE,'resultContentSha1':SHA,'files':files}))
  sha_entries=payload+[manifest]
  (review/'SHA256SUMS.txt').write_text(''.join(f"{digest(p)}  {p.relative_to(root).as_posix()}\n" for p in sha_entries))
  return td,root
 def test_positive(self):
  td,root=self.fixture();self.addCleanup(td.cleanup);r=m.verify(root,Path('review'),SHA,SHA,1,1);self.assertEqual(r['status'],'PASS')

 def test_current_runtime_identity_may_differ_from_package_provenance(self):
  td,root=self.fixture();self.addCleanup(td.cleanup)
  runtime='d'*40
  r=m.verify(root,Path('review'),runtime,runtime,1,1)
  self.assertEqual(r['status'],'PASS')
  self.assertEqual(r['runtimeSourceSha'],runtime)
  self.assertEqual(r['packageResultSha'],SHA)
  self.assertFalse(r['runtimeMatchesPackage'])
 def test_distinct_baseline_provenance_is_allowed(self):
  td,root=self.fixture();self.addCleanup(td.cleanup)
  manifest=json.loads((root/'review/PACKAGE_MANIFEST.json').read_text())
  self.assertEqual(manifest['baselineSha'],BASELINE)
  self.assertNotEqual(manifest['baselineSha'],manifest['resultContentSha1'])
  r=m.verify(root,Path('review'),SHA,SHA,1,1);self.assertEqual(r['status'],'PASS')
 def test_baseline_cannot_substitute_for_result_identity(self):
  td,root=self.fixture();self.addCleanup(td.cleanup);path=root/'review/PACKAGE_MANIFEST.json'
  manifest=json.loads(path.read_text());manifest.pop('resultContentSha1');manifest['baselineSha']=SHA;path.write_text(json.dumps(manifest))
  self.refresh_package(root)
  with self.assertRaises(m.GateError) as ctx:m.verify(root,Path('review'),SHA,SHA,1,1)
  self.assertIn('result content SHA-1',str(ctx.exception))
 def test_stale_fails(self):
  td,root=self.fixture(stale=True);self.addCleanup(td.cleanup)
  with self.assertRaises(m.GateError):m.verify(root,Path('review'),SHA,SHA,1,1)
 def test_exact_id_projection_is_supported(self):
  td,root=self.fixture();self.addCleanup(td.cleanup);review=root/'review'
  with (review/'REQUIREMENT_STATUS.csv').open('w',newline='',encoding='utf-8') as f:
   w=csv.DictWriter(f,fieldnames=['exact_id','development_status','verification_status']);w.writeheader();w.writerow({'exact_id':'R1','development_status':'완료','verification_status':'완료'})
  self.refresh_package(root)
  r=m.verify(root,Path('review'),SHA,SHA,1,1);self.assertEqual(r['status'],'PASS')
 def test_missing_evidence_fails(self):
  td,root=self.fixture(missing=True);self.addCleanup(td.cleanup)
  with self.assertRaises(m.GateError):m.verify(root,Path('review'),SHA,SHA,1,1)
 def test_corrupted_sha256sums_fails_closed(self):
  td,root=self.fixture();self.addCleanup(td.cleanup);path=root/'review/SHA256SUMS.txt'
  lines=path.read_text().splitlines();lines[0]='0'*64+lines[0][64:];path.write_text('\n'.join(lines)+'\n')
  with self.assertRaises(m.GateError) as ctx:m.verify(root,Path('review'),SHA,SHA,1,1)
  self.assertIn('SHA256SUMS hash mismatch',str(ctx.exception))
 def test_stale_change_manifest_hash_fails_closed(self):
  td,root=self.fixture();self.addCleanup(td.cleanup);path=root/'review/CHANGE_MANIFEST.csv'
  rows=list(csv.DictReader(path.open(encoding='utf-8')));rows[0]['sha256']='0'*64
  with path.open('w',newline='',encoding='utf-8') as f:w=csv.DictWriter(f,fieldnames=rows[0].keys());w.writeheader();w.writerows(rows)
  self.refresh_package(root)
  with self.assertRaises(m.GateError) as ctx:m.verify(root,Path('review'),SHA,SHA,1,1)
  self.assertIn('change manifest hash mismatch',str(ctx.exception))

 def test_duplicate_completed_command_fails(self):
  td,root=self.fixture();self.addCleanup(td.cleanup);review=root/'review';ev=root/'evidence';(ev/'F2.log').write_text('PASS\n')
  path=review/'QA_FINDING_REVALIDATION.csv';rows=list(csv.DictReader(path.open(encoding='utf-8')));fieldnames=list(rows[0].keys());r=dict(rows[0]);r.update({'finding_id':'F2','evidence_paths':'evidence/F2.log'});rows.append(r)
  with path.open('w',newline='',encoding='utf-8') as f:w=csv.DictWriter(f,fieldnames=fieldnames);w.writeheader();w.writerows(rows)
  manifest=json.loads((review/'PACKAGE_MANIFEST.json').read_text());manifest['files'].append({'path':'evidence/F2.log','sizeBytes':(ev/'F2.log').stat().st_size,'sha256':digest(ev/'F2.log')});(review/'PACKAGE_MANIFEST.json').write_text(json.dumps(manifest));self.refresh_package(root)
  with self.assertRaises(m.GateError) as ctx:m.verify(root,Path('review'),SHA,SHA,1,2)
  self.assertIn('execution command duplicates',str(ctx.exception))
 def test_completed_finding_requires_dedicated_evidence(self):
  td,root=self.fixture();self.addCleanup(td.cleanup);path=root/'review/QA_FINDING_REVALIDATION.csv';rows=list(csv.DictReader(path.open(encoding='utf-8')));rows[0]['evidence_paths']='evidence/common.log';(root/'evidence/common.log').write_text('PASS\n')
  with path.open('w',newline='',encoding='utf-8') as f:w=csv.DictWriter(f,fieldnames=rows[0].keys());w.writeheader();w.writerows(rows)
  manifest=json.loads((root/'review/PACKAGE_MANIFEST.json').read_text());manifest['files'].append({'path':'evidence/common.log','sizeBytes':(root/'evidence/common.log').stat().st_size,'sha256':digest(root/'evidence/common.log')});(root/'review/PACKAGE_MANIFEST.json').write_text(json.dumps(manifest));self.refresh_package(root)
  with self.assertRaises(m.GateError) as ctx:m.verify(root,Path('review'),SHA,SHA,1,1)
  self.assertIn('dedicated evidence',str(ctx.exception))
 def refresh_package(self,root:Path):
  review=root/'review';manifest_path=review/'PACKAGE_MANIFEST.json';manifest=json.loads(manifest_path.read_text())
  for item in manifest['files']:
   target=root/item['path'];item['sizeBytes']=target.stat().st_size;item['sha256']=digest(target)
  manifest_path.write_text(json.dumps(manifest))
  payload=[root/item['path'] for item in manifest['files']]+[manifest_path]
  (review/'SHA256SUMS.txt').write_text(''.join(f"{digest(p)}  {p.relative_to(root).as_posix()}\n" for p in payload))
if __name__=='__main__':unittest.main()
