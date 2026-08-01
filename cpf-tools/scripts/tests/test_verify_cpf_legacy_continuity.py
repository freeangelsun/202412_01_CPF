from __future__ import annotations
import csv,importlib.util,json,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).parents[1]/"verify-cpf-legacy-continuity.py";spec=importlib.util.spec_from_file_location("legacy",SCRIPT);module=importlib.util.module_from_spec(spec);assert spec and spec.loader;spec.loader.exec_module(module)
class LegacyContinuityTest(unittest.TestCase):
 def fixture(self):
  root=Path(tempfile.mkdtemp()); ids=[]; counts={}
  for name,rel,key in module.SOURCES:
   p=root/rel;p.parent.mkdir(parents=True,exist_ok=True);rid=f"{name}-1";ids.append(rid);counts[name]=1
   with p.open('w',encoding='utf-8-sig',newline='') as f:w=csv.DictWriter(f,fieldnames=[key]);w.writeheader();w.writerow({key:rid})
  canonical=root/'canonical.csv'
  with canonical.open('w',encoding='utf-8-sig',newline='') as f:w=csv.DictWriter(f,fieldnames=['requirement_id']);w.writeheader();[w.writerow({'requirement_id':f'C-{i:03d}'}) for i in range(162)]
  mapping=root/'mapping.csv'
  fields=['record_id','kind','source_sets','source_files','area','canonical_primary','canonical_mapping','relationship','semantic_score','status','notes']
  with mapping.open('w',encoding='utf-8-sig',newline='') as f:
   w=csv.DictWriter(f,fieldnames=fields);w.writeheader();[w.writerow({'record_id':rid,'kind':'REQUIREMENT','canonical_primary':'C-000','canonical_mapping':'C-000','status':'승계','notes':'README/Manual은 완료 판단 근거에서 제외'}) for rid in ids]
  reconciliation=root/'reconciliation.json';reconciliation.write_text(json.dumps({'uniqueRecordCount':len(ids),'sourceRawCounts':counts,'readmeManualPolicy':'README/Manual 제외'}),encoding='utf-8')
  return root,mapping,reconciliation,canonical
 def test_valid(self):
  module.verify(*self.fixture())
 def test_missing_id_rejected(self):
  root,m,r,c=self.fixture();lines=m.read_text(encoding='utf-8-sig').splitlines();m.write_text('\n'.join(lines[:-1])+'\n',encoding='utf-8-sig')
  with self.assertRaises(ValueError):module.verify(root,m,r,c)
 def test_unknown_canonical_rejected(self):
  root,m,r,c=self.fixture();m.write_text(m.read_text(encoding='utf-8-sig').replace('C-000','UNKNOWN'),encoding='utf-8-sig')
  with self.assertRaises(ValueError):module.verify(root,m,r,c)
if __name__=='__main__':unittest.main()
