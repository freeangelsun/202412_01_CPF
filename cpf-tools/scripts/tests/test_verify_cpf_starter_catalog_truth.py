import importlib.util,json,subprocess,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[1]/'verify-cpf-starter-catalog-truth.py'
def load():s=importlib.util.spec_from_file_location('g',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def root(self,stale=False):
  td=tempfile.TemporaryDirectory();r=Path(td.name);subprocess.run(['git','init','-q',r]);subprocess.run(['git','-C',r,'config','user.email','a@b.c']);subprocess.run(['git','-C',r,'config','user.name','t']);m=r/'cpf-starters/x';(m/'src/main/java/a').mkdir(parents=True);(m/'build.gradle').write_text('');(m/'src/main/java/a/X.java').write_text('package com.cpf.x;');c=r/'cpf-tools/config/cpf-starter-catalog.json';c.parent.mkdir(parents=True);c.write_text(json.dumps({'baselinePolicy':'STATIC' if stale else 'GIT_HEAD_RUNTIME','baselineSha':'0'*40 if stale else 'RUNTIME_GIT_HEAD','modules':[{'ownerPath':'cpf-starters/x','packageBase':'com.cpf.x'}]}));subprocess.run(['git','-C',r,'add','.']);subprocess.run(['git','-C',r,'commit','-qm','x']);return td,r
 def test_pass(self):td,r=self.root();self.addCleanup(td.cleanup);self.assertEqual('PASS',load().verify(r)['status'])
 def test_stale_fails(self):td,r=self.root(True);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
if __name__=='__main__':unittest.main()
