import importlib.util,tempfile,unittest
from pathlib import Path
S=Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-edu-profile-isolation.py";spec=importlib.util.spec_from_file_location('iso',S);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
class EduIsolationTest(unittest.TestCase):
 def fixture(self):
  t=tempfile.TemporaryDirectory();r=Path(t.name);p=r/m.REQUIRED_PRODUCT;p.parent.mkdir(parents=True);p.write_text('import { createAdmEducationFixture } from "./createAdmEducationFixture"; const x=VITE_CPF_EDU_PROFILE === "true" ? createAdmEducationFixture() : undefined;', encoding="utf-8")
  f=r/m.REQUIRED_FIXTURE;f.write_text('CPF_EDU_TASKLET_JOB CPF_EDU_TASKLET_DAILY', encoding="utf-8")
  return t,r,p,f
 def test_valid(self):
  t,r,p,f=self.fixture();self.addCleanup(t.cleanup);self.assertEqual([],m.validate(r))
 def test_product_default_rejected(self):
  t,r,p,f=self.fixture();self.addCleanup(t.cleanup);p.write_text(p.read_text(encoding="utf-8")+' CPF_EDU_TASKLET_JOB', encoding="utf-8");self.assertTrue(m.validate(r))
 def test_identifier_outside_fixture_rejected(self):
  t,r,p,f=self.fixture();self.addCleanup(t.cleanup);x=r/'cpf-admin/frontend/src/other.ts';x.write_text('CPF_EDU_UNSCOPED_FIXTURE', encoding="utf-8");self.assertTrue(any('outside' in e for e in m.validate(r)))
 def test_missing_profile_guard_rejected(self):
  t,r,p,f=self.fixture();self.addCleanup(t.cleanup);p.write_text('createAdmEducationFixture()', encoding="utf-8");self.assertTrue(any('profile guard' in e for e in m.validate(r)))
if __name__=='__main__':unittest.main()
