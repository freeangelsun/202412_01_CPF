import importlib.util, json, tempfile, unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
SCRIPT=ROOT/'cpf-tools/db/render_vendor_pack.py'
spec=importlib.util.spec_from_file_location('cpf_db_render',SCRIPT); R=importlib.util.module_from_spec(spec); spec.loader.exec_module(R)

class CanonicalVendorRenderTest(unittest.TestCase):
    def test_time_expression_conversion_is_portable_for_all_canonical_forms(self):
        source = (
            "DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), "
            "DATE_ADD(NOW(), INTERVAL 30 MINUTE), "
            "DATE_SUB(NOW(3), INTERVAL 9 MINUTE)"
        )
        mariadb = R.convert_time_expr('mariadb', source)
        postgresql = R.convert_time_expr('postgresql', source)
        oracle = R.convert_time_expr('oracle', source)

        self.assertEqual(source, mariadb)
        self.assertNotRegex(postgresql, r'(?i)DATE_(?:ADD|SUB)|NOW\(')
        self.assertIn("CURRENT_DATE + INTERVAL '1 day'", postgresql)
        self.assertIn("CURRENT_TIMESTAMP + INTERVAL '30 minute'", postgresql)
        self.assertIn("CURRENT_TIMESTAMP - INTERVAL '9 minute'", postgresql)
        self.assertNotRegex(oracle, r'(?i)DATE_(?:ADD|SUB)|NOW\(')
        self.assertIn("CURRENT_DATE + INTERVAL '1' DAY", oracle)
        self.assertIn("SYSTIMESTAMP + INTERVAL '30' MINUTE", oracle)
        self.assertIn("SYSTIMESTAMP - INTERVAL '9' MINUTE", oracle)

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

    def test_index_prefix_length_is_explicitly_mariadb_only(self):
        schema=json.loads((ROOT/'cpf-tools/db/canonical/platform-schema.json').read_text(encoding='utf-8'))
        table=next(t for t in schema['tables'] if t['targetTableName']=='CPF_FILE_TRANSFER_HISTORY')
        index=next(i for i in table['indexes'] if i['name']=='ix_cpf_file_transfer_duplicate')
        self.assertEqual(['endpoint_code','duplicate_key','checksum'],index['columns'])
        self.assertEqual(
            ['endpoint_code','duplicate_key(255)','checksum'],
            index['vendorColumns']['mariadb'],
        )
        self.assertEqual('endpoint_code, duplicate_key(255), checksum',R.render_index_columns('mariadb',index))
        self.assertEqual('endpoint_code, duplicate_key, checksum',R.render_index_columns('postgresql',index))
        self.assertEqual('endpoint_code, duplicate_key, checksum',R.render_index_columns('oracle',index))

    def test_gateway_binding_unique_key_uses_bounded_full_key_digest(self):
        schema=json.loads((ROOT/'cpf-tools/db/canonical/platform-schema.json').read_text(encoding='utf-8'))
        table=next(t for t in schema['tables'] if t['targetTableName']=='GW_BINDING')
        column=next(c for c in table['columns'] if c['name']=='binding_key_hash')
        unique=next(u for u in table['uniqueKeys'] if u['name']=='uk_cpf_gwy_binding_key')
        self.assertEqual('CHAR(64)',column['type'])
        self.assertFalse(column['nullable'])
        self.assertEqual(['binding_key_hash'],unique['columns'])
        for vendor in R.OFFICIAL:
            ddl=R.render_schema(vendor,schema,'CPF_PLATFORM_DB')
            self.assertIn('CONSTRAINT uk_cpf_gwy_binding_key UNIQUE (binding_key_hash)',ddl)

    def test_response_code_http_status_is_a_required_runtime_contract(self):
        schema=json.loads((ROOT/'cpf-tools/db/canonical/platform-schema.json').read_text(encoding='utf-8'))
        table=next(t for t in schema['tables'] if t['targetTableName']=='CMN_RESPONSE_CODE')
        column=next(c for c in table['columns'] if c['name']=='http_status')
        self.assertEqual('INT',column['type'])
        self.assertFalse(column['nullable'])
        for vendor in R.OFFICIAL:
            ddl=R.render_schema(vendor,schema,'CPF_PLATFORM_DB')
            self.assertRegex(ddl,r'(?i)http_status\s+(?:INT|INTEGER|NUMBER\(10\))\s+NOT NULL')

    def test_backoffice_permission_identity_is_environment_scoped_for_all_vendors(self):
        schema=json.loads((ROOT/'cpf-tools/db/canonical/platform-schema.json').read_text(encoding='utf-8'))
        table=next(t for t in schema['tables'] if t['targetTableName']=='MBW_PERMISSION')
        unique=next(u for u in table['uniqueKeys'] if u['name']=='uk_mbw_permission_scope')
        expected=['role_code','menu_code','button_code','permission_type','environment_code']
        self.assertEqual(expected,unique['columns'])
        for vendor in R.OFFICIAL:
            ddl=R.render_schema(vendor,schema,'CUSTOMER_BUSINESS_DB')
            self.assertIn(
                'CONSTRAINT uk_mbw_permission_scope UNIQUE '
                '(role_code, menu_code, button_code, permission_type, environment_code)',
                ddl,
            )
    def test_generated_pack_check(self):
        import subprocess,sys
        p=subprocess.run([sys.executable,str(SCRIPT),'--root',str(ROOT),'--check'],text=True,capture_output=True)
        self.assertEqual(0,p.returncode,p.stdout+p.stderr)

if __name__=='__main__': unittest.main()
