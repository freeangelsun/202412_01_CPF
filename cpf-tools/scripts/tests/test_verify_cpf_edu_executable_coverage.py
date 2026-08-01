import copy,csv,importlib.util,json,tempfile,unittest
from pathlib import Path
S=Path(__file__).resolve().parents[1]/'verify-cpf-edu-executable-coverage.py';spec=importlib.util.spec_from_file_location('edu',S);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
class EduCoverageTest(unittest.TestCase):
 def fixture(self):
  t=tempfile.TemporaryDirectory();r=Path(t.name);cat=r/'catalog.json';mapping=r/'mapping.csv';canonical=r/'canonical.csv'
  features=[]
  for i in range(1,33):
   fid=f'EDU-{i:03d}';src=f'src/{fid}.java';test=f'test/{fid}Test.java';contract=f'api/{fid}.java'
   for rel,text in [(src,'package x; class X {}'),(test,'class T {}'),(contract,'package api; public interface A {}')]:p=r/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text, encoding="utf-8")
   features.append({'featureId':fid,'area':fid,'coverageScope':'scope','ownerModule':'cpf-reference','referenceSources':[src],'tests':[test],'publicContracts':[contract],'scenarioAxes':{'normal':fid+'-N','error':fid+'-E','recovery':fid+'-R'},'admObservationPaths':['/dashboard'],'runtimeCommands':['python run_'+fid+'.py'],'developmentStatus':'완료','verificationStatus':'미검증'})
  cat.write_text(json.dumps({'featureCount':32,'canonicalRequirementCount':162,'features':features}),encoding='utf-8')
  cfields=['requirement_id','section'];mfields=['requirement_id','section','edu_feature_ids','public_contract','reference_source','normal_scenario','error_fault_recovery_scenario','adm_observation_path','runtime_command','coverage_development_status','runtime_verification_status','evidence']
  with canonical.open('w',encoding='utf-8',newline='') as f:
   w=csv.DictWriter(f,fieldnames=cfields);w.writeheader();[w.writerow({'requirement_id':f'REQ-{i:03d}','section':'S'}) for i in range(1,163)]
  with mapping.open('w',encoding='utf-8',newline='') as f:
   w=csv.DictWriter(f,fieldnames=mfields);w.writeheader()
   for i in range(1,163):
    fid=f'EDU-{((i-1)%32)+1:03d}'
    w.writerow({'requirement_id':f'REQ-{i:03d}','section':'S','edu_feature_ids':fid,'public_contract':'api/'+fid+'.java','reference_source':'src/'+fid+'.java','normal_scenario':fid+'-N','error_fault_recovery_scenario':fid+'-E | '+fid+'-R','adm_observation_path':'/dashboard','runtime_command':'python run_'+fid+'.py','coverage_development_status':'완료','runtime_verification_status':'미검증','evidence':f'evidence/{i}.json'})
  return t,r,cat,mapping,canonical
 def test_valid_development_contract(self):
  t,r,c,mx,ca=self.fixture();self.addCleanup(t.cleanup);self.assertEqual([],m.validate(r,c,mx,ca,False))
 def test_missing_continuous_feature(self):
  t,r,c,mx,ca=self.fixture();self.addCleanup(t.cleanup);d=json.loads(c.read_text(encoding="utf-8"));d['features'].pop();c.write_text(json.dumps(d), encoding="utf-8");self.assertTrue(any('continuous' in x for x in m.validate(r,c,mx,ca,False)))
 def test_duplicate_runtime_command(self):
  t,r,c,mx,ca=self.fixture();self.addCleanup(t.cleanup);d=json.loads(c.read_text(encoding="utf-8"));d['features'][1]['runtimeCommands']=d['features'][0]['runtimeCommands'];c.write_text(json.dumps(d), encoding="utf-8");self.assertTrue(any('duplicate runtime command' in x for x in m.validate(r,c,mx,ca,False)))
 def test_unknown_feature_mapping(self):
  t,r,c,mx,ca=self.fixture();self.addCleanup(t.cleanup);text=mx.read_text(encoding="utf-8");mx.write_text(text.replace('EDU-001','EDU-999',1), encoding="utf-8");self.assertTrue(any('unknown EDU feature' in x for x in m.validate(r,c,mx,ca,False)))
 def test_missing_canonical_requirement(self):
  t,r,c,mx,ca=self.fixture();self.addCleanup(t.cleanup);lines=mx.read_text(encoding="utf-8").splitlines();mx.write_text('\n'.join(lines[:-1])+'\n', encoding="utf-8");self.assertTrue(any('must contain 162' in x or 'missing from EDU' in x for x in m.validate(r,c,mx,ca,False)))
 def test_release_requires_runtime_evidence(self):
  t,r,c,mx,ca=self.fixture();self.addCleanup(t.cleanup);self.assertTrue(any('verificationStatus must be 완료' in x or 'runtime_verification_status must be 완료' in x for x in m.validate(r,c,mx,ca,True)))
 def test_internal_import_rejected(self):
  t,r,c,mx,ca=self.fixture();self.addCleanup(t.cleanup);d=json.loads(c.read_text(encoding="utf-8"));d['features'][0]['referenceSources']=['cpf-reference/src/main/java/X.java'];c.write_text(json.dumps(d), encoding="utf-8");p=r/'cpf-reference/src/main/java/X.java';p.parent.mkdir(parents=True);p.write_text('import com.cpf.core.internal.Bad; class X {}', encoding="utf-8");errs=m.validate(r,c,mx,ca,True);self.assertTrue(any('Internal package' in x for x in errs))
if __name__=='__main__':unittest.main()
