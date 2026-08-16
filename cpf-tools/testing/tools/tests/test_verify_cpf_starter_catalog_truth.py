from __future__ import annotations
import importlib.util,json,subprocess,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[4]/"cpf-tools/verification/tools/verify-cpf-starter-catalog-truth.py"
def load():s=importlib.util.spec_from_file_location("g",SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def root(self,stale=False):
  td=tempfile.TemporaryDirectory();r=Path(td.name);subprocess.run(["git","init","-q",r]);subprocess.run(["git","-C",r,"config","user.email","a@b.c"]);subprocess.run(["git","-C",r,"config","user.name","t"])
  owner=r/"cpf-starters/profiles/x";owner.mkdir(parents=True);(owner/"build.gradle").write_text("",encoding="utf-8");(r/"cpf-starters/group-x").mkdir(parents=True)
  catalog={"catalogId":"TEST","baselinePolicy":"STATIC" if stale else "GIT_HEAD_RUNTIME","baselineSha":"0"*40 if stale else "RUNTIME_GIT_HEAD","publicProfiles":["x"],"capabilityGroups":[{"id":"x-cap","path":"cpf-starters/group-x"}],"modules":[{"artifactId":"cpf-starter-profile-x","projectPath":":starters:profiles:x","profileId":"x","ownerPath":"cpf-starters/profiles/x","packageBase":"com.cpf.profile.x","visibility":"public","role":"profile"}],"providerSlots":{"x":{"default":{"projectPath":":starters:profiles:x","coordinate":"com.cpf.starter:cpf-starter-profile-x"}}},"removedArtifactIds":[],"starterAdmissionPolicy":{"failClosed":True},"profileDefinitions":{"x":{"artifactId":"cpf-starter-profile-x","runtimeProjects":[":starters:profiles:x"],"requiredCapabilities":["x-cap"]}}}
  c=r/"cpf-tools/generator/contracts/cpf-starter-catalog.json";c.parent.mkdir(parents=True);c.write_text(json.dumps(catalog),encoding="utf-8");subprocess.run(["git","-C",r,"add","."]);subprocess.run(["git","-C",r,"commit","-qm","x"]);return td,r
 def test_pass(self):td,r=self.root();self.addCleanup(td.cleanup);self.assertEqual("PASS",load().verify(r)["status"])
 def test_stale_fails(self):td,r=self.root(True);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
if __name__=="__main__":unittest.main()
