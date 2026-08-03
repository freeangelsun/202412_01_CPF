import csv,importlib.util,subprocess,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[1]/'verify-cpf-requirement-traceability.py'
def load():s=importlib.util.spec_from_file_location('g',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def root(self,bad=False):
  td=tempfile.TemporaryDirectory();r=Path(td.name);subprocess.run(['git','init','-q',r]);subprocess.run(['git','-C',r,'config','user.email','a@b.c']);subprocess.run(['git','-C',r,'config','user.name','t']);(r/'x').write_text('x');subprocess.run(['git','-C',r,'add','.']);subprocess.run(['git','-C',r,'commit','-qm','x']);head=subprocess.check_output(['git','-C',r,'rev-parse','HEAD'],text=True).strip()
  f=r/'status.csv';cols=list(load().MANDATORY)+['exact_sha','QA_상태'];row={c:'' for c in cols};row.update(requirement_id='CPF-FR-1',development_status='재확인 필요',verification_status='미검증',개발GPT_수행여부='Y',개발GPT_상태='완료',개발GPT_수행내용='fixed',개발GPT_실행및검증='python gate.py --root .',개발GPT_evidence='e.json',exact_sha=('0'*40 if bad else head))
  with f.open('w',encoding='utf-8',newline='') as h:w=csv.DictWriter(h,fieldnames=cols);w.writeheader();w.writerow(row)
  return td,r,f
 def test_pass(self):td,r,f=self.root();self.addCleanup(td.cleanup);self.assertEqual('PASS',load().verify(r,f)['status'])
 def test_stale_sha_fails(self):td,r,f=self.root(True);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r,f)
 def test_missing_matrix_fails(self):td,r,f=self.root();self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r,r/'none.csv')
if __name__=='__main__':unittest.main()
