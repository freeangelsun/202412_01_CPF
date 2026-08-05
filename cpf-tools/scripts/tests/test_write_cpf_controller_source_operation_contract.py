#!/usr/bin/env python3
from __future__ import annotations

import hashlib
import importlib.util
import json
import shutil
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "write-cpf-controller-source-operation-contract.py"
SPEC = importlib.util.spec_from_file_location("cpf_controller_contract", SCRIPT)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class ControllerSourceContractTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        source = self.root / "cpf-admin/src/main/java/com/cpf/admin/opr/controller"
        source.mkdir(parents=True)
        frontend = self.root / "cpf-admin/frontend"
        (frontend / "src/shared").mkdir(parents=True)
        (frontend / "src/shared/cpfApi.ts").write_text(
            "export async function cpfGeneratedRequest<T>(_v: unknown): Promise<T> { return {} as T; }\n",
            encoding="utf-8",
        )
        (frontend / "orval.config.ts").write_text("export default {};\n", encoding="utf-8")
        (frontend / "package-lock.json").write_text('{"lockfileVersion":3}\n', encoding="utf-8")
        (frontend / "package.json").write_text(
            json.dumps({"engines": {"node": "22.16.0", "npm": "10.9.2"}}) + "\n",
            encoding="utf-8",
        )
        self.source = source
        self.output = frontend / "src/generated"
        self.openapi = frontend / "openapi/cpf-openapi.json"

    def tearDown(self) -> None:
        self.temp.cleanup()

    def write_controller(self, body: str, name: str = "FixtureController.java") -> Path:
        target = self.source / name
        target.write_text(body, encoding="utf-8")
        return target

    def generate(self) -> dict:
        records, schemas = MODULE.discover(self.root, "ADM")
        MODULE.write(self.root, "ADM", self.output, records, schemas, self.openapi)
        return json.loads(self.openapi.read_text(encoding="utf-8"))

    def test_preserves_path_query_header_body_and_trust_boundary(self) -> None:
        self.write_controller(r'''
package com.cpf.admin.opr.controller;
import jakarta.validation.constraints.NotBlank;
@RestController
@RequestMapping("/adm/api/fixture")
public class FixtureController {
  @PostMapping(path = "/{itemId}/actions")
  @Operation(operationId = "admFixtureAction", summary = "fixture")
  public void action(
      @PathVariable("itemId") long itemId,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(required = false) String requestUser,
      @RequestHeader("X-CPF-Risk-Confirmed") String riskConfirmed,
      @RequestBody ActionRequest body,
      HttpServletRequest request) {}
  public record ActionRequest(
      @NotBlank String reason,
      java.util.List<com.example.Value> values,
      String operator,
      boolean force) {}
}
''')
        spec = self.generate()
        operation = spec["paths"]["/adm/api/fixture/{itemId}/actions"]["post"]
        parameters = {(item["in"], item["name"]): item for item in operation["parameters"]}
        self.assertEqual({("path", "itemId"), ("query", "limit"), ("header", "X-CPF-Risk-Confirmed")}, set(parameters))
        self.assertTrue(parameters[("path", "itemId")]["required"])
        self.assertFalse(parameters[("query", "limit")]["required"])
        self.assertEqual(50, parameters[("query", "limit")]["schema"]["default"])
        self.assertTrue(parameters[("header", "X-CPF-Risk-Confirmed")]["required"])
        self.assertEqual(
            {"$ref": "#/components/schemas/AdmFixtureActionRequest"},
            operation["requestBody"]["content"]["application/json"]["schema"],
        )
        schema = spec["components"]["schemas"]["AdmFixtureActionRequest"]
        self.assertEqual({"reason", "values", "force"}, set(schema["properties"]))
        self.assertEqual(["reason", "force"], schema["required"])
        self.assertEqual(
            {"type": "array", "items": {"$ref": "#/components/schemas/Value"}},
            schema["properties"]["values"],
        )
        self.assertNotIn("operator", schema["properties"])
        self.assertEqual("CONTROLLER_SOURCE_PRE_RUNTIME", spec["x-cpf-export-origin"])
        self.assertFalse(spec["x-cpf-release-eligible"])

    def test_is_deterministic_and_idempotent(self) -> None:
        self.write_controller(r'''
@RestController
@RequestMapping("/adm/api/idempotent")
class FixtureController {
  @GetMapping("/{id}") @Operation(operationId="admIdempotentFind")
  public String find(@PathVariable String id, @RequestParam(required=false) String query) { return id; }
}
''')
        self.generate()
        first = {p.relative_to(self.root).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest()
                 for p in sorted((self.root / "cpf-admin/frontend").rglob("*")) if p.is_file()}
        self.generate()
        second = {p.relative_to(self.root).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest()
                  for p in sorted((self.root / "cpf-admin/frontend").rglob("*")) if p.is_file()}
        self.assertEqual(first, second)
        marker = json.loads((self.output / ".cpf-openapi-source.json").read_text(encoding="utf-8"))
        self.assertEqual("CONTROLLER_SOURCE_PRE_RUNTIME", marker["origin"])
        self.assertFalse(marker["releaseEligible"])
        self.assertEqual(1, marker["openApiOperationCount"])

    def test_duplicate_operation_id_fails_closed(self) -> None:
        self.write_controller(r'''
@RestController @RequestMapping("/adm/api/duplicate")
class FixtureController {
  @GetMapping("/one") @Operation(operationId="admDuplicate") public String one(){return "";}
  @GetMapping("/two") @Operation(operationId="admDuplicate") public String two(){return "";}
}
''')
        with self.assertRaisesRegex(MODULE.ContractError, "duplicate operationId"):
            MODULE.discover(self.root, "ADM")

    def test_missing_operation_id_fails_closed(self) -> None:
        self.write_controller(r'''
@RestController @RequestMapping("/adm/api/missing")
class FixtureController {
  @GetMapping("/one") public String one(){return "";}
}
''')
        with self.assertRaisesRegex(MODULE.ContractError, "operationId missing"):
            MODULE.discover(self.root, "ADM")


    def test_controller_local_record_names_are_operation_scoped(self) -> None:
        self.write_controller(r'''
@RestController @RequestMapping("/adm/api/one")
class OneController {
  @PostMapping @Operation(operationId="admOneSave")
  public void save(@RequestBody ControlRequest body) {}
  public record ControlRequest(@NotBlank String reason) {}
}
''', "OneController.java")
        self.write_controller(r'''
@RestController @RequestMapping("/adm/api/two")
class TwoController {
  @PostMapping @Operation(operationId="admTwoSave")
  public void save(@RequestBody ControlRequest body) {}
  public record ControlRequest(long version, String approvalId) {}
}
''', "TwoController.java")
        spec = self.generate()
        one_ref = spec["paths"]["/adm/api/one"]["post"]["requestBody"]["content"]["application/json"]["schema"]["$ref"]
        two_ref = spec["paths"]["/adm/api/two"]["post"]["requestBody"]["content"]["application/json"]["schema"]["$ref"]
        self.assertEqual("#/components/schemas/AdmOneSaveRequest", one_ref)
        self.assertEqual("#/components/schemas/AdmTwoSaveRequest", two_ref)
        self.assertEqual({"reason"}, set(spec["components"]["schemas"]["AdmOneSaveRequest"]["properties"]))
        self.assertEqual({"version", "approvalId"}, set(spec["components"]["schemas"]["AdmTwoSaveRequest"]["properties"]))
        self.assertNotIn("ControlRequest", spec["components"]["schemas"])

    def test_nested_java_type_uses_legal_component_name(self) -> None:
        self.assertEqual(
            {"$ref": "#/components/schemas/PolicyCommand"},
            MODULE.java_schema("com.cpf.admin.service.AdmApprovalEngineService.PolicyCommand"),
        )
        self.assertEqual(
            {"type": "array", "items": {"$ref": "#/components/schemas/PolicyCommand"}},
            MODULE.java_schema("java.util.List<com.cpf.admin.service.AdmApprovalEngineService.PolicyCommand>"),
        )

    def test_optional_and_default_contracts(self) -> None:
        self.write_controller(r'''
@RestController @RequestMapping("/adm/api/options")
class FixtureController {
  @GetMapping @Operation(operationId="admOptions")
  public String options(@RequestParam(required=false) java.util.Optional<String> q,
                        @RequestHeader(name="X-Mode", required=false) String mode,
                        @RequestParam(defaultValue="true") boolean enabled){return "";}
}
''')
        spec = self.generate()
        params = {p["name"]: p for p in spec["paths"]["/adm/api/options"]["get"]["parameters"]}
        self.assertFalse(params["q"]["required"])
        self.assertEqual({"type": "string"}, params["q"]["schema"])
        self.assertFalse(params["X-Mode"]["required"])
        self.assertEqual(True, params["enabled"]["schema"]["default"])


if __name__ == "__main__":
    unittest.main(verbosity=2)
