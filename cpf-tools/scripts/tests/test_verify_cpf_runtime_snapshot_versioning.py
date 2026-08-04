import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).parents[1]/'verify-cpf-runtime-snapshot-versioning.py'
def load():
 s=importlib.util.spec_from_file_location('v',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def fixture(self,fixed=True):
  td=tempfile.TemporaryDirectory();r=Path(td.name);p=r/'cpf-starters/integration/http-client/src/main/java/com/cpf/core/common/http/CpfServiceEndpointRegistry.java';p.parent.mkdir(parents=True)
  equal="""if(version==old.version()){if(normalized.equals(old.endpoints()))return old;throw new IllegalArgumentException(\"version collision\");}""" if fixed else ''
  p.write_text(f'''class X{{public Snapshot replaceRuntime(long version,Map endpoints){{while(true){{Snapshot old=runtime.get();if(version<old.version())throw new IllegalArgumentException();{equal}Snapshot next=new Snapshot(version,normalized);if(runtime.compareAndSet(old, next))return next;}}}}}}''')
  return td,r
 def test_fixed_passes(self):td,r=self.fixture();self.addCleanup(td.cleanup);self.assertEqual('PASS',load().verify(r)['status'])
 def test_same_version_overwrite_fails(self):td,r=self.fixture(False);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
 def test_test_contract_exists(self):
  t=(SCRIPT.parents[2]/'cpf-starters/integration/http-client/src/test/java/com/cpf/core/common/http/CpfServiceEndpointRegistryVersionFenceTest.java')
  self.assertTrue(t.is_file());self.assertIn('sameVersionIsIdempotentOnlyForTheSameNormalizedSnapshot',t.read_text())
if __name__=='__main__':unittest.main()
