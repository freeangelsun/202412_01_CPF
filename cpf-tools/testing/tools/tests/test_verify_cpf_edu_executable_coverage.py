from __future__ import annotations
import json,shutil,subprocess,sys,tempfile,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]; SCRIPT=ROOT/"cpf-tools/verification/tools/verify-cpf-edu-executable-coverage.py"; CAT="cpf-education/src/main/resources/education/cpf-education-canonical-35.json"
class EduCoverageTest(unittest.TestCase):
 def run_gate(self,root): return subprocess.run([sys.executable,str(SCRIPT),"--root",str(root)],capture_output=True,text=True,encoding="utf-8")
 def fixture(self): td=tempfile.TemporaryDirectory();r=Path(td.name);shutil.copytree(ROOT/"cpf-education",r/"cpf-education");return td,r,r/CAT
 def test_valid_development_contract(self): cp=self.run_gate(ROOT);self.assertEqual(0,cp.returncode,cp.stdout+cp.stderr);self.assertIn("features=35",cp.stdout)
 def test_feature_count_drift_fails(self):
  td,r,q=self.fixture();self.addCleanup(td.cleanup);d=json.loads(q.read_text());d["totalCount"]=34;q.write_text(json.dumps(d));cp=self.run_gate(r);self.assertNotEqual(0,cp.returncode);self.assertIn("must contain 20 online + 15 batch = 35",cp.stdout)
 def test_retired_reference_path_fails(self):
  td,r,q=self.fixture();self.addCleanup(td.cleanup);d=json.loads(q.read_text());d["examples"][0]["primaryClass"]="cpf-reference/Sample.java";q.write_text(json.dumps(d));cp=self.run_gate(r);self.assertNotEqual(0,cp.returncode);self.assertIn("retired path/id",cp.stdout)
 def test_duplicate_catalog_id_fails(self):
  td,r,q=self.fixture();self.addCleanup(td.cleanup);d=json.loads(q.read_text());d["examples"][1]["id"]=d["examples"][0]["id"];q.write_text(json.dumps(d));cp=self.run_gate(r);self.assertNotEqual(0,cp.returncode);self.assertIn("uniqueness/format",cp.stdout)
if __name__=="__main__":unittest.main()
