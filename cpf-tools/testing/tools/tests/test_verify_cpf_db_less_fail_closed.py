import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/db/verification/verify-cpf-db-less-fail-closed.py"
def load():s=importlib.util.spec_from_file_location('g',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def test_missing_context_test_fails(self):
  with tempfile.TemporaryDirectory() as d:self.assertRaises(Exception,load().verify,Path(d))
 def fixture(self):
  td=tempfile.TemporaryDirectory();r=Path(td.name);g=load()
  data={rel:' '.join(toks) for rel,toks in g.REQ.items()}
  data['cpf-admin/src/main/java/com/cpf/admin/config/AdmPersistencePolicy.java']+=' "DATABASE" Mode.MEMORY'
  for rel in ('cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/common/config/CmnDataSourceConfig.java','cpf-starters/data/persistence-mybatis/src/main/java/com/cpf/common/config/CmnMyBatisConfig.java'):
   data[rel]+=" == 'product'"
  for rel,text in data.items():p=r/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text,encoding='utf-8')
  return td,r
 def test_contract_fixture_passes(self):
  td,r=self.fixture();self.addCleanup(td.cleanup);self.assertEqual('PASS',load().verify(r)['status'])
 def test_invented_datasource_fallback_fails(self):
  td,r=self.fixture();self.addCleanup(td.cleanup);p=r/'cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/common/config/CmnDataSourceConfig.java';p.write_text(p.read_text()+' EmbeddedDatabase',encoding='utf-8');self.assertRaises(Exception,load().verify,r)
 def test_product_condition_is_required(self):
  td,r=self.fixture();self.addCleanup(td.cleanup);p=r/'cpf-starters/data/persistence-mybatis/src/main/java/com/cpf/common/config/CmnMyBatisConfig.java';p.write_text(p.read_text().replace("== 'product'",''),encoding='utf-8');self.assertRaises(Exception,load().verify,r)
if __name__=='__main__':unittest.main()
