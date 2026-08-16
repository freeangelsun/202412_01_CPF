from __future__ import annotations
import json,shutil,subprocess,sys,tempfile,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
SCRIPT=ROOT/"cpf-tools/verification/tools/verify-cpf-edu-executable-coverage.py"

class EduCoverageTest(unittest.TestCase):
    def run_gate(self,root:Path):
        return subprocess.run([sys.executable,str(SCRIPT),"--root",str(root)],capture_output=True,text=True,encoding="utf-8")
    def fixture(self):
        td=tempfile.TemporaryDirectory();r=Path(td.name)
        q=r/"cpf-tools/governance/cpf-edu-executable-catalog.json";q.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(ROOT/"cpf-tools/governance/cpf-edu-executable-catalog.json",q)
        shutil.copytree(ROOT/"cpf-education",r/"cpf-education")
        return td,r,q
    def test_valid_development_contract(self):
        cp=self.run_gate(ROOT);self.assertEqual(0,cp.returncode,cp.stdout+cp.stderr);self.assertIn("features=135",cp.stdout)
    def test_runtime_pending_is_reported_not_promoted(self):
        cp=self.run_gate(ROOT);self.assertEqual(0,cp.returncode);self.assertIn("runtimePending=135",cp.stdout)
    def test_feature_count_drift_fails(self):
        td,r,q=self.fixture();self.addCleanup(td.cleanup);d=json.loads(q.read_text(encoding="utf-8"));d["featureCount"]=134;q.write_text(json.dumps(d),encoding="utf-8");cp=self.run_gate(r);self.assertNotEqual(0,cp.returncode);self.assertIn("must contain 135",cp.stdout)
    def test_retired_reference_path_fails(self):
        td,r,q=self.fixture();self.addCleanup(td.cleanup);d=json.loads(q.read_text(encoding="utf-8"));d["features"][0]["sourcePath"]="cpf-reference/src/main/java/X.java";q.write_text(json.dumps(d),encoding="utf-8");cp=self.run_gate(r);self.assertNotEqual(0,cp.returncode);self.assertIn("retired path",cp.stdout)
    def test_duplicate_requirement_id_fails(self):
        td,r,q=self.fixture();self.addCleanup(td.cleanup);d=json.loads(q.read_text(encoding="utf-8"));d["features"][1]["requirementId"]=d["features"][0]["requirementId"];q.write_text(json.dumps(d),encoding="utf-8");cp=self.run_gate(r);self.assertNotEqual(0,cp.returncode);self.assertIn("uniqueness/format",cp.stdout)

if __name__=="__main__":unittest.main()
