from __future__ import annotations
import json, subprocess, sys, tempfile, unittest
from pathlib import Path

SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/invoke-cpf-final-verification-plan.py"

class FinalPlanExecutorTest(unittest.TestCase):
    def fixture(self, mutates: bool=False, required_env: bool=False):
        base=Path(tempfile.mkdtemp()); root=base/'repo'; evidence=base/'evidence'; root.mkdir()
        subprocess.run(['git','init','-q',str(root)],check=True)
        subprocess.run(['git','-C',str(root),'config','user.email','test@example.test'],check=True)
        subprocess.run(['git','-C',str(root),'config','user.name','Test'],check=True)
        tool=root/'tool.py'; tool.write_text("from pathlib import Path\n"+("Path('dirty.txt').write_text('x')\n" if mutates else "print('ok')\n"),encoding='utf-8')
        plan={'schemaVersion':1,'planId':'test-plan','orderedStages':['source'],'commands':[{'id':'one','stage':'source','runner':'python','path':'tool.py','args':[],'required':True}]}
        if required_env: plan['commands'][0]['requiredEnvironment']=['CPF_TEST_ENV']
        (root/'plan.json').write_text(json.dumps(plan),encoding='utf-8')
        subprocess.run(['git','-C',str(root),'add','.'],check=True);subprocess.run(['git','-C',str(root),'commit','-qm','init'],check=True)
        sha=subprocess.check_output(['git','-C',str(root),'rev-parse','HEAD'],text=True).strip()
        return root,evidence,sha
    def execute(self,root,evidence,sha,*extra):
        return subprocess.run([sys.executable,str(SCRIPT),'--root',str(root),'--expected-sha',sha,'--evidence-dir',str(evidence),'--plan','plan.json',*extra],capture_output=True,text=True)
    def test_executes_once_and_stays_clean(self):
        root,evidence,sha=self.fixture();cp=self.execute(root,evidence,sha);self.assertEqual(0,cp.returncode,cp.stdout+cp.stderr)
        data=json.loads((evidence/'cpf-final-verification-result.sanitized.json').read_text(encoding="utf-8"));self.assertEqual(1,data['executedCommandCount']);self.assertEqual('PASS',data['steps'][0]['status'])
    def test_source_mutation_fails(self):
        root,evidence,sha=self.fixture(mutates=True);cp=self.execute(root,evidence,sha);self.assertNotEqual(0,cp.returncode)
        data=json.loads((evidence/'cpf-final-verification-result.sanitized.json').read_text(encoding="utf-8"));self.assertIn('dirty',json.dumps(data))
    def test_environment_blocker_fails_by_default(self):
        root,evidence,sha=self.fixture(required_env=True);cp=self.execute(root,evidence,sha);self.assertNotEqual(0,cp.returncode)
        data=json.loads((evidence/'cpf-final-verification-result.sanitized.json').read_text(encoding="utf-8"));self.assertEqual(1,data['environmentBlockerCount'])
    def test_environment_blocker_can_be_recorded_in_non_release_development_run(self):
        root,evidence,sha=self.fixture(required_env=True);cp=self.execute(root,evidence,sha,'--allow-environment-blockers');self.assertEqual(0,cp.returncode,cp.stdout+cp.stderr)

if __name__=='__main__':unittest.main()
