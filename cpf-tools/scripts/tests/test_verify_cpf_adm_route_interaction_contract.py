from __future__ import annotations
import csv,importlib.util,tempfile,unittest
from pathlib import Path
MODULE=Path(__file__).parents[1]/'verify-cpf-adm-route-interaction-contract.py'
spec=importlib.util.spec_from_file_location('gate',MODULE);gate=importlib.util.module_from_spec(spec);spec.loader.exec_module(gate)
REAL_ROOT=Path(__file__).parents[3]
REAL_MATRIX=REAL_ROOT/'cpf-docs/quality/CPF_20260801_ADM_ROUTE_INTERACTION_MATRIX.csv'
REAL_CATALOG=REAL_ROOT/'cpf-tools/verification/20260801_01/adm-public-operation-catalog.csv'
class RouteContractTest(unittest.TestCase):
 def test_real_overlay_contract(self):self.assertEqual([],gate.validate(REAL_ROOT,REAL_MATRIX,REAL_CATALOG))
 def mutate(self,field,value):
  with tempfile.TemporaryDirectory() as temp:
   path=Path(temp)/'matrix.csv'
   with REAL_MATRIX.open(encoding='utf-8-sig',newline='') as f:rows=list(csv.DictReader(f));fields=list(rows[0])
   rows[0][field]=value
   with path.open('w',encoding='utf-8-sig',newline='') as f:w=csv.DictWriter(f,fieldnames=fields);w.writeheader();w.writerows(rows)
   return gate.validate(REAL_ROOT,path,REAL_CATALOG)
 def test_missing_operation_rejected(self):self.assertTrue(any('operation drift' in x for x in self.mutate('query_operation_ids','')))
 def test_error_status_drift_rejected(self):self.assertTrue(any('error statuses' in x for x in self.mutate('required_error_statuses','401;403;500')))
 def test_high_risk_cas_rejected(self):
  with REAL_MATRIX.open(encoding='utf-8-sig',newline='') as f:rows=list(csv.DictReader(f))
  target=next(r for r in rows if r['risk_level']=='HIGH' and r['mutation_operation_ids'])
  with tempfile.TemporaryDirectory() as temp:
   p=Path(temp)/'m.csv';fields=list(rows[0]);target['requires_cas']='false'
   with p.open('w',encoding='utf-8-sig',newline='') as h:w=csv.DictWriter(h,fieldnames=fields);w.writeheader();w.writerows(rows)
   self.assertTrue(any('approval and CAS' in x for x in gate.validate(REAL_ROOT,p,REAL_CATALOG)))
 def test_unresolved_placeholder_rejected(self):self.assertTrue(any('placeholder' in x for x in self.mutate('owner_module','미확정')))
if __name__=='__main__':unittest.main()
