import csv,hashlib,importlib.util,subprocess,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-split-master-dataset.py"
def load():s=importlib.util.spec_from_file_location('g',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
def wcsv(p,fields,rows):p.parent.mkdir(parents=True,exist_ok=True);f=p.open('w',encoding='utf-8',newline='');w=csv.DictWriter(f,fieldnames=fields);w.writeheader();w.writerows(rows);f.close()
class T(unittest.TestCase):
 def root(self,malformed=False,reversed_order=False):
  td=tempfile.TemporaryDirectory();r=Path(td.name);subprocess.run(['git','init','-q',r],check=True);subprocess.run(['git','-C',r,'config','user.email','a@b.c']);subprocess.run(['git','-C',r,'config','user.name','t'])
  d=r/'cpf-docs/work/current';d.mkdir(parents=True)
  specs=[('requirement','requirement_id',['CPF-FR-000001']),('scenario','scenario_id',['CPF-SC-000001']),('execution','execution_order',['00-00000002' if reversed_order else '00-00000001'])]
  for kind,idc,ids in specs:
   part=d/f'{kind}.part.csv';fields=[idc]
   row={idc:('bad' if malformed and kind=='requirement' else ids[0])}
   if kind=='execution':fields += ['requirement_id','scenario_id','phase_id','work_package_id'];row.update(requirement_id='CPF-FR-000001',scenario_id='CPF-SC-000001',phase_id='P00',work_package_id='WP1')
   wcsv(part,fields,[row]);b=part.read_bytes();idx=d/{'requirement':'CPF_REQUIREMENT_MASTER.csv','scenario':'CPF_SCENARIO_MASTER.csv','execution':'CPF_EXECUTION_SEQUENCE.csv'}[kind]
   cols=['part_sequence','part_path','part_record_count','first_record_id','last_record_id','size_bytes','sha256','logical_record_count']
   wcsv(idx,cols,[{'part_sequence':1,'part_path':part.relative_to(r).as_posix(),'part_record_count':1,'first_record_id':row[idc],'last_record_id':row[idc],'size_bytes':len(b),'sha256':hashlib.sha256(b).hexdigest(),'logical_record_count':1}])
  subprocess.run(['git','-C',r,'add','.'],check=True);subprocess.run(['git','-C',r,'commit','-qm','x'],check=True);return td,r
 def test_pass_and_head(self):td,r=self.root();self.addCleanup(td.cleanup);x=load().verify(r);self.assertEqual('PASS',x['status']);self.assertEqual(40,len(x['verifiedAgainstSha']))
 def test_malformed_fails(self):td,r=self.root(True);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
 def test_head_mismatch_fails(self):td,r=self.root();self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r,'0'*40)
 def test_dirty_fails_when_required(self):td,r=self.root();self.addCleanup(td.cleanup);(r/'x').write_text('x');self.assertRaises(Exception,load().verify,r,None,True)
if __name__=='__main__':unittest.main()
