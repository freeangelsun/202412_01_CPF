from __future__ import annotations

import importlib.util
import pathlib
import tempfile
import unittest

SCRIPT = pathlib.Path(__file__).parents[1] / "verify-cpf-operator-trust-boundary.py"
spec = importlib.util.spec_from_file_location("operator_gate", SCRIPT)
module = importlib.util.module_from_spec(spec)
assert spec.loader
spec.loader.exec_module(module)

VALID_API = '''const CLIENT_ACTOR_FIELDS = new Set(["requestedBy","requestUser","actorId","operatorId","operatorIdOverride"]);
function assertNoClientActor(value: unknown): void { if (Array.isArray(value)) value.forEach(assertNoClientActor); else if(value && typeof value === "object") Object.entries(value).forEach(([key, child])=>{if(CLIENT_ACTOR_FIELDS.has(key))throw new Error(key);assertNoClientActor(child);}); }
function assertNoClientActorQuery(target: URL){for(const key of target.searchParams.keys())if(CLIENT_ACTOR_FIELDS.has(key))throw new Error(key);}
function q(target:URL){assertNoClientActorQuery(target);assertNoClientActorQuery(target);assertNoClientActorQuery(target);assertNoClientActorQuery(target);assertNoClientActorQuery(target);}
export async function cpfGeneratedRequest(config:any){const target=new URL(config.url);Object.entries(config.params||{}).forEach(([key,value])=>{if(CLIENT_ACTOR_FIELDS.has(key))throw new Error(key);});}
export async function admApi(){}
'''
VALID_RUNTIME = '''class AdmRuntimeControlController { void x(HttpServletRequest request, RuntimeBody body){ Object x=request.getAttribute("adm.operatorId"); body.toCommand(operator); new CpfRuntimeChangeCommand(); new CpfRuntimeGroupCommand(); new CpfRuntimeGroupMemberCommand(); } }'''
VALID_BATCH = '''class BatchRuntimeControlController { static final Set<String> CLIENT_ACTOR_FIELDS=Set.of("requestedBy","requestUser","actorId","operatorId","operatorIdOverride"); Object x(Object value){ if(value instanceof Map<?, ?> nested)return sanitizeCommandMap(nested); if(value instanceof List<?> list)return sanitizeCommandValue(list); command.put("requestedBy", operatorId); client.saveJobDefinition(withServerActor(request, operatorId)); client.transitionJobDefinition(jobId, version, withServerActor(request, operatorId)); client.command(withServerActor(request, operatorId)); client.createPlan(withServerActor(request, operatorId)); return value;} Object sanitizeCommandMap(Object v){return v;} Object sanitizeCommandValue(Object v){return v;} }'''
VALID_GATEWAY = '''class AdmGatewayRegistryController { void x(HttpServletRequest request){String operator=operator(request); owner.update(command,operator);} }'''

class OperatorTrustBoundaryTest(unittest.TestCase):
    def fixture(self) -> pathlib.Path:
        self.tmp = tempfile.TemporaryDirectory()
        root = pathlib.Path(self.tmp.name)
        files = {
            "cpf-admin/frontend/src/shared/cpfApi.ts": VALID_API,
            "cpf-biz-admin/frontend/src/shared/cpfApi.ts": VALID_API,
            "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmRuntimeControlController.java": VALID_RUNTIME,
            "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java": VALID_BATCH,
            "cpf-admin/src/main/java/com/cpf/admin/opr/gateway/AdmGatewayRegistryController.java": VALID_GATEWAY,
        }
        for rel, text in files.items():
            path=root/rel; path.parent.mkdir(parents=True,exist_ok=True); path.write_text(text,encoding="utf-8")
        return root

    def tearDown(self) -> None:
        if hasattr(self,"tmp"): self.tmp.cleanup()

    def test_valid(self):
        self.assertEqual("PASS", module.verify(self.fixture())["status"])

    def test_missing_frontend_root_fails(self):
        root=self.fixture(); (root/"cpf-biz-admin/frontend/src/shared/cpfApi.ts").unlink(); (root/"cpf-biz-admin/frontend/src/shared").rmdir(); (root/"cpf-biz-admin/frontend/src").rmdir()
        with self.assertRaises(module.GateError): module.verify(root)

    def test_browser_actor_payload_fails(self):
        root=self.fixture(); p=root/"cpf-admin/frontend/src/feature.ts"; p.write_text("const body={operatorId:'browser'}",encoding="utf-8")
        with self.assertRaises(module.GateError): module.verify(root)

    def test_shared_guard_missing_operator_id_fails(self):
        root=self.fixture(); p=root/"cpf-admin/frontend/src/shared/cpfApi.ts"; p.write_text(VALID_API.replace(',"operatorId"',''),encoding="utf-8")
        with self.assertRaises(module.GateError): module.verify(root)


    def test_bza_shared_guard_missing_requested_by_fails(self):
        root=self.fixture(); p=root/"cpf-biz-admin/frontend/src/shared/cpfApi.ts"; p.write_text(VALID_API.replace('"requestedBy",',''),encoding="utf-8")
        with self.assertRaises(module.GateError): module.verify(root)


    def test_generated_client_query_guard_missing_fails(self):
        root=self.fixture(); p=root/"cpf-admin/frontend/src/shared/cpfApi.ts"; p.write_text(VALID_API.replace("Object.entries(config.params||{}).forEach(([key,value])=>{if(CLIENT_ACTOR_FIELDS.has(key))throw new Error(key);});", "Object.entries(config.params||{}).forEach(([key,value])=>{});"),encoding="utf-8")
        with self.assertRaises(module.GateError): module.verify(root)

    def test_batch_recursive_sanitizer_missing_fails(self):
        root=self.fixture(); p=root/"cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java"; p.write_text(VALID_BATCH.replace("sanitizeCommandValue", "noRecursiveValue"),encoding="utf-8")
        with self.assertRaises(module.GateError): module.verify(root)


    def test_batch_mutation_without_server_actor_fails(self):
        root=self.fixture(); p=root/"cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java"; p.write_text(VALID_BATCH.replace("client.createPlan(withServerActor(request, operatorId))", "client.createPlan(request)"),encoding="utf-8")
        with self.assertRaises(module.GateError): module.verify(root)

    def test_raw_runtime_command_fails(self):
        root=self.fixture(); p=root/"cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmRuntimeControlController.java"; p.write_text(VALID_RUNTIME+" @RequestBody CpfRuntimeChangeCommand raw;",encoding="utf-8")
        with self.assertRaises(module.GateError): module.verify(root)

    def test_gateway_without_server_actor_fails(self):
        root=self.fixture(); p=root/"cpf-admin/src/main/java/com/cpf/admin/opr/gateway/AdmGatewayRegistryController.java"; p.write_text("class AdmGatewayRegistryController {}",encoding="utf-8")
        with self.assertRaises(module.GateError): module.verify(root)

    def test_request_parameter_actor_fails(self):
        root=self.fixture(); p=root/"cpf-admin/src/main/java/com/cpf/admin/opr/controller/Other.java"; p.write_text("class Other {void x(@RequestParam String operatorId){}}",encoding="utf-8")
        with self.assertRaises(module.GateError): module.verify(root)

if __name__ == "__main__": unittest.main()
