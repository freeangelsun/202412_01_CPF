import importlib.util, json, tempfile, unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
SCRIPT=ROOT/'cpf-tools/db/render_vendor_pack.py'
spec=importlib.util.spec_from_file_location('cpf_db_render',SCRIPT); R=importlib.util.module_from_spec(spec); spec.loader.exec_module(R)

class CanonicalVendorRenderTest(unittest.TestCase):
    def test_all_logical_types_render_for_vendor3(self):
        schema=json.loads((ROOT/'cpf-tools/db/canonical/platform-schema.json').read_text(encoding='utf-8'))
        for vendor in R.OFFICIAL:
            for t in schema['tables']:
                for c in t['columns']:
                    self.assertTrue(R.render_type(vendor,c['type']))
    def test_render_is_deterministic_and_complete(self):
        schema=json.loads((ROOT/'cpf-tools/db/canonical/platform-schema.json').read_text(encoding='utf-8'))
        for vendor in R.OFFICIAL:
            a=R.render_schema(vendor,schema,'CPF_PLATFORM_DB'); b=R.render_schema(vendor,schema,'CPF_PLATFORM_DB')
            self.assertEqual(a,b)
            self.assertEqual(sum(1 for t in schema['tables'] if t.get('targetDatabaseRole')=='CPF_PLATFORM_DB'),a.count('CREATE TABLE '))
            self.assertNotRegex(a,r'(?im)^\s*USE\s+')
            self.assertNotRegex(a,r'(?i)\b(?:cmnDB|admDB|batDB|refDB)\.')
    def test_oracle_is_not_mariadb_ddl(self):
        schema=json.loads((ROOT/'cpf-tools/db/canonical/platform-schema.json').read_text(encoding='utf-8'))
        o=R.render_schema('oracle',schema,'CPF_PLATFORM_DB'); m=R.render_schema('mariadb',schema,'CPF_PLATFORM_DB')
        self.assertIn('VARCHAR2(',o); self.assertIn('NUMBER(19)',o)
        self.assertNotIn('AUTO_INCREMENT',o); self.assertIn('AUTO_INCREMENT',m)
    def test_generated_pack_check(self):
        import subprocess,sys
        p=subprocess.run([sys.executable,str(SCRIPT),'--root',str(ROOT),'--check'],text=True,capture_output=True)
        self.assertEqual(0,p.returncode,p.stdout+p.stderr)

if __name__=='__main__': unittest.main()
