from __future__ import annotations
import importlib.util,shutil,tempfile,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4];SCRIPT=ROOT/"cpf-tools/verification/tools/verify-cpf-operator-trust-boundary.py"
def load():s=importlib.util.spec_from_file_location("g",SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
COPY=(
 "cpf-admin/frontend/src/shared/cpfApi.ts","cpf-backoffice-web/frontend/src/shared/api/channelHttpClient.ts","cpf-backoffice-web/src/main/java/com/cpf/backoffice/web/shared/protocol/CanonicalHeaderOwnershipFilter.java",
 "cpf-admin/src/main/java/com/cpf/admin/opr/filter/AdmApiAuthFilter.java",
 "cpf-admin/src/main/java/com/cpf/admin/opr/audit/AdmVerifiedActorRequestBodyAdvice.java",
 "cpf-admin/src/main/java/com/cpf/admin/opr/audit/AdmMandatoryAuditInterceptor.java",
 "cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/filter/BackofficeApiAuthFilter.java",
 "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java",
 "cpf-admin/src/test/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlControllerEndpointTest.java",
)
class T(unittest.TestCase):
 def test_pass(self):self.assertEqual("PASS",load().verify(ROOT)["status"])
 def test_frontend_actor_guard_fails(self):
  td=tempfile.TemporaryDirectory();self.addCleanup(td.cleanup);r=Path(td.name)
  for rel in COPY:
   q=r/rel;q.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(ROOT/rel,q)
  p=r/COPY[0];p.write_text(p.read_text(encoding="utf-8").replace("assertNoClientActorQuery","actorGuardRemoved"),encoding="utf-8")
  self.assertRaises(Exception,load().verify,r)
if __name__=="__main__":unittest.main()
