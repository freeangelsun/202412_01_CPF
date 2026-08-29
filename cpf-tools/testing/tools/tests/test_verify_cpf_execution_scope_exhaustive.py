import csv
import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-execution-scope-exhaustive.py"
spec = importlib.util.spec_from_file_location("scope_gate", SCRIPT)
mod = importlib.util.module_from_spec(spec); spec.loader.exec_module(mod)

REQ_FIELDS = [
    "requirement_id","requirement","priority","owner_module","source_basis","change_target","actual_consumer","acceptance_criteria","verification_method","regression_protection","requirement_group","capability","feature","function_type","error_handling","concurrency_control","retry_policy","unknown_result_policy","recovery_policy","security_control","audit_requirement","db_vendor_impact","api_contract","frontend_contract","completion_prohibited_when","execution_phase_id","execution_order","work_package_id"
]
SC_FIELDS = ["scenario_id","linked_requirement_id","scenario_type","title","preconditions","steps","expected_result","failure_criteria","environment","topology","required_evidence","execution_phase_id","work_package_id"]
EXEC_FIELDS = ["execution_order","requirement_id","phase_id","phase_name","work_package_id","priority","owner_module","requirement_group","capability","feature","function_type"]

def write_csv(path, fields, rows):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w",encoding="utf-8-sig",newline="") as f:
        w=csv.DictWriter(f,fieldnames=fields);w.writeheader();w.writerows(rows)

def row(rid,order,group="ADM_UI",wp="P09-A"):
    return {"requirement_id":rid,"requirement":"r","priority":"P1","owner_module":"cpf-admin","source_basis":"s","change_target":"c","actual_consumer":"consumer","acceptance_criteria":"a","verification_method":"v","regression_protection":"g","requirement_group":group,"capability":"ONLINE","feature":"F","function_type":"TEST","error_handling":"e","concurrency_control":"c","retry_policy":"r","unknown_result_policy":"u","recovery_policy":"r","security_control":"s","audit_requirement":"a","db_vendor_impact":"d","api_contract":"api","frontend_contract":"f","completion_prohibited_when":"x","execution_phase_id":"P09","execution_order":order,"work_package_id":wp}

class GateTest(unittest.TestCase):
    def fixture(self):
        td=tempfile.TemporaryDirectory();root=Path(td.name)
        for stem in ("CPF_EXECUTION_SEQUENCE","CPF_REQUIREMENT_MASTER","CPF_SCENARIO_MASTER"):
            (root/"cpf-docs/governance/development-harness/current"/(stem+".parts")).mkdir(parents=True)
        req=row("CPF-FR-000001","09-00000001")
        ex={k:req.get(k,"") for k in EXEC_FIELDS};ex.update({"phase_id":"P09","phase_name":"x"})
        sc={"scenario_id":"CPF-SC-000001","linked_requirement_id":req["requirement_id"],"scenario_type":"POSITIVE","title":"t","preconditions":"p","steps":"s","expected_result":"e","failure_criteria":"f","environment":"java21","topology":"single","required_evidence":"log","execution_phase_id":"P09","work_package_id":"P09-A"}
        write_csv(root/"cpf-docs/governance/development-harness/current/CPF_EXECUTION_SEQUENCE.parts/p1.csv",EXEC_FIELDS,[ex])
        write_csv(root/"cpf-docs/governance/development-harness/current/CPF_REQUIREMENT_MASTER.parts/p1.csv",REQ_FIELDS,[req])
        write_csv(root/"cpf-docs/governance/development-harness/current/CPF_SCENARIO_MASTER.parts/p1.csv",SC_FIELDS,[sc])
        for p in ["cpf-admin/frontend/src/app","cpf-admin/frontend/src/generated","cpf-admin/frontend/src/features","cpf-admin/src/main/java/com/cpf/admin","cpf-admin/src/test"]:(root/p).mkdir(parents=True,exist_ok=True)
        (root/"cpf-admin/frontend/src/app/routes.ts").write_text('export const admCapabilityRegistry={"x":{routeId:"x", expectedOperationIds:["op"], component:import("../features/x/X.vue")}}',encoding="utf-8")
        (root/"cpf-admin/frontend/src/generated/cpf-operation-contract.ts").write_text('export type CpfOperationId = "op";',encoding="utf-8")
        for p in ["cpf-backoffice-web/frontend/src/router","cpf-backoffice-web/frontend/src/features/x","cpf-backoffice-web/frontend/scripts","cpf-backoffice/online/openapi","cpf-backoffice/online/src/main/java/com/cpf/backoffice/online","cpf-backoffice/online/src/test"]:(root/p).mkdir(parents=True,exist_ok=True)
        (root/"cpf-backoffice-web/frontend/src/router/index.ts").write_text('import X from "../features/x/X.vue"; const routes=[{ path:"/x", component:X }]',encoding="utf-8")
        (root/"cpf-backoffice-web/frontend/src/features/x/X.vue").write_text('<template><div>x</div></template>',encoding="utf-8")
        (root/"cpf-backoffice-web/frontend/scripts/generate-reference-client.mjs").write_text("const wanted=['bop']",encoding="utf-8")
        backoffice_openapi=json.dumps({"paths":{"/api/v1/backoffice/x":{"get":{"operationId":"bop"}}}})
        (root/"cpf-backoffice/online/openapi/cpf-openapi.json").write_text(backoffice_openapi,encoding="utf-8")
        (root/"cpf-backoffice-web/frontend/openapi").mkdir(parents=True,exist_ok=True)
        (root/"cpf-backoffice-web/frontend/openapi/cpf-openapi.json").write_text(backoffice_openapi,encoding="utf-8")
        args=type("A",(),dict(root=str(root),expected_sha="a"*40,source_head="a"*40,start_row=1,expected_total_execution=1,expected_scope=1,audit_csv="audit.csv",work_package_csv="wp.csv"))()
        return td,root,args
    def test_positive(self):
        td,root,args=self.fixture()
        try:
            result=mod.verify(args);self.assertEqual("PASS",result["status"]);self.assertEqual(1,result["scopeRows"])
        finally:td.cleanup()
    def test_backoffice_route_contract_uses_openapi_not_stale_generator_wanted_list(self):
        td,root,args=self.fixture()
        try:
            generator=root/"cpf-backoffice-web/frontend/scripts/generate-reference-client.mjs"
            generator.write_text("// current generator intentionally has no wanted list",encoding="utf-8")
            result=mod.verify(args)
            self.assertEqual("PASS",result["status"])
            self.assertEqual(1,result["routeContracts"]["bzaExpectedOperations"])
        finally:td.cleanup()

    def test_canonical_counts_can_be_derived_without_stale_fixed_baseline(self):
        td,root,args=self.fixture()
        try:
            args.expected_total_execution=None
            args.expected_scope=None
            result=mod.verify(args)
            self.assertEqual(1,result["totalExecutionRows"])
            self.assertEqual(1,result["scopeRows"])
        finally:td.cleanup()

    def test_source_sha256_identity_is_accepted(self):
        td,root,args=self.fixture()
        try:
            args.expected_sha="b"*64
            args.source_head="b"*64
            result=mod.verify(args)
            self.assertEqual("PASS",result["status"])
            self.assertEqual("b"*64,result["verifiedAgainstSha"])
        finally:td.cleanup()

    def test_direct_verification_title_drift_fails(self):
        td,root,args=self.fixture()
        try:
            p=root/"cpf-docs/governance/development-harness/current/CPF_SCENARIO_MASTER.parts/p1.csv"
            _,rows=mod.read_csv(p)
            rows[0]["scenario_type"]="DIRECT_VERIFICATION"
            rows[0]["title"]="[CPF-FR-999999] stale / TEST 직접 검증"
            write_csv(p,SC_FIELDS,rows)
            with self.assertRaises(mod.AuditError):mod.verify(args)
        finally:td.cleanup()

    def test_missing_scenario_fails(self):
        td,root,args=self.fixture()
        try:
            write_csv(root/"cpf-docs/governance/development-harness/current/CPF_SCENARIO_MASTER.parts/p1.csv",SC_FIELDS,[])
            with self.assertRaises(mod.AuditError):mod.verify(args)
        finally:td.cleanup()
    def test_placeholder_fails(self):
        td,root,args=self.fixture()
        try:
            p=root/"cpf-docs/governance/development-harness/current/CPF_REQUIREMENT_MASTER.parts/p1.csv";_,rows=mod.read_csv(p);rows[0]["acceptance_criteria"]="TODO";write_csv(p,REQ_FIELDS,rows)
            with self.assertRaises(mod.AuditError):mod.verify(args)
        finally:td.cleanup()
    def test_route_operation_drift_fails(self):
        td,root,args=self.fixture()
        try:
            (root/"cpf-admin/frontend/src/generated/cpf-operation-contract.ts").write_text('export type CpfOperationId = "different";',encoding="utf-8")
            with self.assertRaises(mod.AuditError):mod.verify(args)
        finally:td.cleanup()

if __name__=="__main__":unittest.main()


def test_current_modernization_requirement_groups_have_source_owners():
    required = {
        "DOCUMENTATION GOVERNANCE",
        "REPOSITORY HYGIENE",
        "EVIDENCE",
        "QA GOVERNANCE",
        "HANDOVER",
        "EXECUTION GOVERNANCE",
    }
    assert required <= set(mod.GROUP_SOURCE_MAP)
    root = Path(__file__).resolve().parents[4]
    for group in required:
        assert any((root / relative).exists() for relative in mod.GROUP_SOURCE_MAP[group]), group
