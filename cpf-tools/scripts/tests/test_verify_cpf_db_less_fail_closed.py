import importlib.util
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).parents[1] / "verify-cpf-db-less-fail-closed.py"
spec = importlib.util.spec_from_file_location("db_less", SCRIPT)
mod = importlib.util.module_from_spec(spec); spec.loader.exec_module(mod)

ADM = '''class X { private static final java.util.Set<String> MEMORY_ALLOWED_PROFILES = java.util.Set.of("edu", "test"); X(E e){e.getProperty("cpf.adm.persistence.mode", "DATABASE");}}'''
DS = '''@ConditionalOnExpression("'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'") class X {}'''
MB = '''@ConditionalOnExpression("'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'") class X { X(@Qualifier("cmnDataSource") DataSource cmnDataSource){} }'''
SAMPLE = '''@Profile({"edu", "test"}) @ConditionalOnProperty(prefix = "cpf.cmn.sample-db", name = "enabled", havingValue = "true") class X { Object cmnSampleDataSource; }'''

class GateTest(unittest.TestCase):
    def root(self, adm=ADM, ds=DS, mb=MB, sample=SAMPLE):
        td=tempfile.TemporaryDirectory(); root=Path(td.name); self.addCleanup(td.cleanup)
        files={
          "cpf-admin/src/main/java/com/cpf/admin/config/AdmPersistencePolicy.java":adm,
          "cpf-common/src/main/java/com/cpf/common/config/CmnDataSourceConfig.java":ds,
          "cpf-common/src/main/java/com/cpf/common/config/CmnMyBatisConfig.java":mb,
          "cpf-common/src/main/java/com/cpf/common/config/CmnSampleDataSourceConfig.java":sample,
        }
        for rel,text in files.items(): p=root/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text, encoding="utf-8")
        return root
    def test_pass(self): self.assertEqual("PASS", mod.verify(self.root())["status"])
    def test_rejects_demo_memory(self):
        with self.assertRaises(SystemExit): mod.verify(self.root(adm=ADM.replace('"edu", "test"','"test", "demo"')))
    def test_rejects_unprofiled_sample(self):
        with self.assertRaises(SystemExit): mod.verify(self.root(sample=SAMPLE.replace('@Profile({"edu", "test"}) ','')))
    def test_rejects_mybatis_without_product_condition(self):
        with self.assertRaises(SystemExit): mod.verify(self.root(mb=MB.replace('@ConditionalOnExpression("\'${cpf.common.runtime-mode:product}\'.toLowerCase() == \'product\'") ','')))

if __name__ == '__main__': unittest.main()
