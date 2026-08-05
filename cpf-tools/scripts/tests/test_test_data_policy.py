from __future__ import annotations
import json,subprocess,sys,tempfile,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
GEN=ROOT/'cpf-tools/scripts/generate-cpf-test-data-pack.py'
SCHEMA=json.loads((ROOT/'cpf-tools/db/canonical/platform-schema.json').read_text(encoding='utf-8-sig'))
WRAPPER=(ROOT/'cpf-tools/scripts/prepare-cpf-test-data.ps1').read_text(encoding='utf-8-sig')
class TestDataPolicyTest(unittest.TestCase):
 def run_gen(self,vendor,mode):
  td=tempfile.TemporaryDirectory();base=Path(td.name);sql=base/f'{vendor}-{mode}.sql';inv=base/'inventory.json';p=subprocess.run([sys.executable,str(GEN),'--root',str(ROOT),'--vendor',vendor,'--mode',mode,'--output',str(sql),'--inventory',str(inv)],text=True,capture_output=True);self.assertEqual(0,p.returncode,p.stderr);return td,sql.read_text(),json.loads(inv.read_text())
 def test_masking_generated_for_all_vendors_and_excludes_formal_keys(self):
  for vendor in ('mariadb','postgresql','oracle'):
   td,sql,inv=self.run_gen(vendor,'mask')
   try:
    self.assertGreater(inv['summary']['maskedColumnCount'],50)
    self.assertTrue(all(not c['formalKey'] for c in inv['columns']))
    self.assertIn('CPF_TEST_DATA_MASKING_V1',sql)
    self.assertIn('example.invalid',sql)
   finally: td.cleanup()
 def test_required_pii_classes_are_present(self):
  td,sql,inv=self.run_gen('mariadb','mask')
  try:
   classes={c['class'] for c in inv['columns']}
   self.assertTrue({'email','phone','ip','person_name','employee_number','secret','payload','message','file_name'}<=classes)
   pairs={(c['table'].lower(),c['column'].lower()) for c in inv['columns']}
   self.assertIn(('bza_employee','email'),pairs);self.assertIn(('adm_operator_profile','mobile_no'),pairs)
  finally:td.cleanup()
 def test_synthetic_pack_has_only_reserved_markers_and_ten_rows(self):
  for vendor in ('mariadb','postgresql','oracle'):
   td,sql,inv=self.run_gen(vendor,'synthetic')
   try:
    import re
    self.assertEqual({f'CPF-SYNTH-{i:04d}' for i in range(1,11)}, set(re.findall(r'CPF-SYNTH-\d{4}',sql)))
    self.assertIn('CPF_SYNTHETIC_DATA_V1',sql)
    self.assertNotIn('@gmail.com',sql);self.assertFalse(inv['summary']['productionDerived'])
   finally:td.cleanup()
 def test_wrapper_forbids_production_and_requires_confirmations_plan_hash_unknown(self):
  for token in ['productionEnvironmentNames','ConfirmNonProduction','ConfirmSourceDataAuthorized','ConfirmRawDataPurge','ExpectedSqlSha256',"$result.status='UNKNOWN'",'$result.reconcileRequired=$true']:
   self.assertIn(token,WRAPPER)
if __name__=='__main__':unittest.main()
