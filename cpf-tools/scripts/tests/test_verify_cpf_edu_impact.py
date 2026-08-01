import csv,importlib.util,tempfile,unittest
from pathlib import Path
S=Path(__file__).resolve().parents[1]/'verify-cpf-edu-impact.py';spec=importlib.util.spec_from_file_location('impact',S);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
class EduImpactTest(unittest.TestCase):
 def fixture(self):
  t=tempfile.TemporaryDirectory();r=Path(t.name);d=r/m.DECL;d.parent.mkdir(parents=True)
  fields=['impact_id','surface','changed_path_pattern','edu_feature_ids','coverage_artifacts','test_artifacts','owner','status']
  with d.open('w',newline='',encoding='utf-8') as f:w=csv.DictWriter(f,fieldnames=fields);w.writeheader();w.writerow({'impact_id':'I1','surface':'PUBLIC_CONTRACT','changed_path_pattern':'cpf-core/src/main/java/**','edu_feature_ids':'EDU-003','coverage_artifacts':'catalog','test_artifacts':'test','owner':'cpf-core','status':'완료'})
  good=['cpf-core/src/main/java/com/cpf/core/api/X.java',m.DECL,'cpf-tools/governance/cpf-edu-executable-catalog.json','cpf-tools/scripts/tests/test_x.py']
  return t,r,d,good
 def test_valid(self):
  t,r,d,c=self.fixture();self.addCleanup(t.cleanup);self.assertEqual([],m.validate(r,c,d))
 def test_missing_declaration_change(self):
  t,r,d,c=self.fixture();self.addCleanup(t.cleanup);c.remove(m.DECL);self.assertTrue(any('same commit' in x for x in m.validate(r,c,d)))
 def test_missing_coverage_change(self):
  t,r,d,c=self.fixture();self.addCleanup(t.cleanup);c=[x for x in c if x not in m.COVERAGE];self.assertTrue(any('coverage matrix' in x for x in m.validate(r,c,d)))
 def test_missing_test_change(self):
  t,r,d,c=self.fixture();self.addCleanup(t.cleanup);c=[x for x in c if 'test_' not in x];self.assertTrue(any('executable test' in x for x in m.validate(r,c,d)))
 def test_nonimpactful_docs_need_no_edu(self):
  t,r,d,c=self.fixture();self.addCleanup(t.cleanup);self.assertEqual([],m.validate(r,['cpf-docs/README.md'],d))
if __name__=='__main__':unittest.main()
