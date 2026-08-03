import importlib.util, json, tempfile, unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[1]/'verify-cpf-db-vendor-semantic-parity.py'
def load():
 s=importlib.util.spec_from_file_location('gate',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class TestDbVendorSemanticParity(unittest.TestCase):
 def fixture(self,bad_dialect=False,missing_column=False,empty_rollback=False,bad_checksum=False):
  td=tempfile.TemporaryDirectory();root=Path(td.name)
  canonical={"schemaVersion":1,"tableCount":1,"tables":[{"name":"t","columns":[{"name":"id"},{"name":"payload"},{"name":"duplicate_key"},{"name":"checksum"}],"primaryKey":["id"],"uniqueKeys":[],"indexes":[{"name":"ix_t_duplicate","columns":["duplicate_key(20)","checksum"]},{"name":"ix_t_payload","columns":["payload"],"unique":True}],"foreignKeys":[]}]}
  c=root/'cpf-tools/db/canonical/platform-schema.json';c.parent.mkdir(parents=True);c.write_text(json.dumps(canonical),encoding='utf-8')
  for v in ('mariadb','postgresql','oracle'):
   for k in ('source','install','migration','rollback','verify'):(root/'cpf-tools/db/vendor'/v/k).mkdir(parents=True,exist_ok=True)
   typ={'mariadb':'BLOB','postgresql':'BYTEA','oracle':'BLOB'}[v]
   payload='' if missing_column and v=='oracle' else f'payload {typ},'
   if v=='mariadb':
    ddl=f'''CREATE TABLE t (id BIGINT, {payload} duplicate_key VARCHAR(100), checksum VARCHAR(64), CONSTRAINT pk_t PRIMARY KEY(id), INDEX ix_t_duplicate(duplicate_key(20), checksum), UNIQUE INDEX ix_t_payload(payload));'''
   else:
    ddl=f'''CREATE TABLE t (id BIGINT, {payload} duplicate_key VARCHAR(100), checksum VARCHAR(64), CONSTRAINT pk_t PRIMARY KEY(id));\nCREATE INDEX ix_t_duplicate ON t(duplicate_key, checksum);\nCREATE UNIQUE INDEX ix_t_payload ON t(payload);'''
   if bad_dialect and v=='postgresql':ddl=ddl.replace(typ,'LONGBLOB')
   (root/'cpf-tools/db/vendor'/v/'source/10_cpf_schema.sql').write_text(ddl,encoding='utf-8')
   (root/'cpf-tools/db/vendor'/v/'install/00_empty_install.sql').write_text(ddl,encoding='utf-8')
   (root/'cpf-tools/db/vendor'/v/'migration/V1__marker.sql').write_text('ALTER TABLE t ADD marker INTEGER;',encoding='utf-8')
   (root/'cpf-tools/db/vendor'/v/'rollback/R1.sql').write_text('' if empty_rollback and v=='oracle' else 'ALTER TABLE t DROP COLUMN marker;',encoding='utf-8')
   (root/'cpf-tools/db/vendor'/v/'verify/verify.sql').write_text('SELECT id FROM t;',encoding='utf-8')
   if bad_checksum and v=='oracle':(root/'cpf-tools/db/vendor'/v/'migration/V1__marker.sql.sha256').write_text('0'*64+'  V1__marker.sql\n',encoding='utf-8')
  g=root/'cpf-tools/scripts/generate-official-db-vendor-source.ps1';g.parent.mkdir(parents=True);g.write_text("'^LONGBLOB$','BYTEA' '^MEDIUMTEXT$','TEXT' $u -eq 'LONGBLOB' $u -eq 'MEDIUMTEXT'",encoding='utf-8')
  return td,root
 def test_positive_source_install_and_lifecycle(self):
  td,r=self.fixture();self.addCleanup(td.cleanup);result=load().verify(r);self.assertEqual('PASS',result['status']);self.assertEqual(1,result['canonicalTableCount'])
 def test_wrong_dialect_fails(self):
  td,r=self.fixture(bad_dialect=True);self.addCleanup(td.cleanup)
  with self.assertRaises(Exception):load().verify(r)
 def test_missing_column_fails(self):
  td,r=self.fixture(missing_column=True);self.addCleanup(td.cleanup)
  with self.assertRaises(Exception):load().verify(r)
 def test_empty_lifecycle_fails(self):
  td,r=self.fixture(empty_rollback=True);self.addCleanup(td.cleanup)
  with self.assertRaises(Exception):load().verify(r)

 def test_bad_migration_checksum_fails(self):
  td,r=self.fixture(bad_checksum=True);self.addCleanup(td.cleanup)
  with self.assertRaises(Exception):load().verify(r)
 def abandon_fixture(self,missing_verify=False,rollback_allows=False):
  td=tempfile.TemporaryDirectory();root=Path(td.name)
  raw={"tables":[{"name":"cpf_batch_execution_control","checks":[{"name":"ck_cpf_bat_control_status","expression":"control_status IN ('FAILED','UNKNOWN_RESULT','ABANDONING','ABANDONED')"}]}]}
  states="'FAILED', 'UNKNOWN_RESULT', 'ABANDONING', 'ABANDONED'"
  rolled="'FAILED', 'UNKNOWN_RESULT', 'ABANDONED'" if not rollback_allows else states
  for vendor in ('mariadb','postgresql','oracle'):
   base=root/'cpf-tools/db/vendor'/vendor
   for path in (base/'source',base/'install',base/'migration/flyway/batDB',base/'rollback',base/'verify'):path.mkdir(parents=True,exist_ok=True)
   schema=f"CREATE TABLE cpf_batch_execution_control (id BIGINT, control_status VARCHAR(40), CONSTRAINT ck_cpf_bat_control_status CHECK (control_status IN ({states})));"
   (base/'source/35_bat_schema.sql').write_text(schema,encoding='utf-8')
   (base/'install/00_empty_install.sql').write_text(schema,encoding='utf-8')
   migration=f"ALTER TABLE cpf_batch_execution_control DROP CONSTRAINT ck_cpf_bat_control_status; ALTER TABLE cpf_batch_execution_control ADD CONSTRAINT ck_cpf_bat_control_status CHECK (control_status IN ({states}));"
   (base/'migration/flyway/batDB/V99__bat_abandon_two_phase_state.sql').write_text(migration,encoding='utf-8')
   guard={'mariadb':'SIGNAL SQLSTATE','postgresql':'RAISE EXCEPTION','oracle':'RAISE_APPLICATION_ERROR'}[vendor]
   rollback=f"SELECT 'ABANDONING'; {guard}; ALTER TABLE cpf_batch_execution_control ADD CONSTRAINT ck_cpf_bat_control_status CHECK (control_status IN ({rolled}));"
   (base/'rollback/R99__bat_abandon_two_phase_state.sql').write_text(rollback,encoding='utf-8')
   verify={'mariadb':"SELECT 'CPF-BAT-V99-VERIFY-FAILED ABANDONING';",'postgresql':"DO $$ BEGIN RAISE EXCEPTION 'ABANDONING'; END $$;",'oracle':"BEGIN RAISE_APPLICATION_ERROR(-20997, 'ABANDONING'); END; /"}[vendor]
   if not (missing_verify and vendor=='oracle'):(base/'verify/V99__bat_abandon_two_phase_state.sql').write_text(verify,encoding='utf-8')
  return td,root,raw
 def test_abandon_lifecycle_positive(self):
  td,r,raw=self.abandon_fixture();self.addCleanup(td.cleanup);self.assertEqual([],load().verify_batch_abandon_lifecycle(r,raw))
 def test_abandon_lifecycle_missing_verify_fails(self):
  td,r,raw=self.abandon_fixture(missing_verify=True);self.addCleanup(td.cleanup);self.assertTrue(any('verification missing' in x for x in load().verify_batch_abandon_lifecycle(r,raw)))
 def test_abandon_lifecycle_rollback_still_allows_state_fails(self):
  td,r,raw=self.abandon_fixture(rollback_allows=True);self.addCleanup(td.cleanup);self.assertTrue(any('still allows ABANDONING' in x for x in load().verify_batch_abandon_lifecycle(r,raw)))

 def test_real_schema_fixture_covers_200_tables_when_available(self):
  real=Path('/mnt/data/cpf-r2-work/db-real');canonical=Path('/mnt/data/platform-schema.json')
  if not real.is_dir() or not canonical.is_file():self.skipTest('session-only real fixture unavailable')
  result=load().verify_schema_inventory(canonical,real);self.assertEqual('PASS',result['status']);self.assertEqual(200,result['canonicalTables'])
if __name__=='__main__':unittest.main()
