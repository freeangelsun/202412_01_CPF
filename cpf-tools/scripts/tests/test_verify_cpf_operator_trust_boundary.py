from __future__ import annotations

import importlib.util
import pathlib
import tempfile
import unittest

SCRIPT = pathlib.Path(__file__).parents[1] / "verify-cpf-operator-trust-boundary.py"
spec = importlib.util.spec_from_file_location("operator_trust", SCRIPT)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)


class OperatorTrustBoundaryTest(unittest.TestCase):
    def fixture(self) -> pathlib.Path:
        root = pathlib.Path(tempfile.mkdtemp())
        api = root / "cpf-admin/frontend/src/shared/cpfApi.ts"
        api.parent.mkdir(parents=True)
        api.write_text('const CLIENT_ACTOR_FIELDS = new Set(["requestUser","actorId","operatorIdOverride","requestedBy"]); function assertNoClientActor(){}', encoding="utf-8")
        runtime = root / "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmRuntimeControlController.java"
        runtime.parent.mkdir(parents=True)
        runtime.write_text('request.getAttribute("adm.operatorId"); body.toCommand(operator); new CpfRuntimeChangeCommand(); new CpfRuntimeGroupCommand(); new CpfRuntimeGroupMemberCommand();', encoding="utf-8")
        batch = root / "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java"
        batch.parent.mkdir(parents=True)
        batch.write_text('command.remove("requestedBy"); command.remove("requestUser"); command.put("requestedBy", operatorId);', encoding="utf-8")
        gateway = root / "cpf-admin/src/main/java/com/cpf/admin/opr/gateway/AdmGatewayRegistryController.java"
        gateway.parent.mkdir(parents=True)
        gateway.write_text('String operator=operator(request); new Command(a,operator);', encoding="utf-8")
        return root

    def test_valid(self):
        module.verify(self.fixture())

    def test_frontend_actor_field_rejected(self):
        root = self.fixture()
        page = root / "cpf-admin/frontend/src/Page.ts"
        page.write_text('const body={requestedBy: operatorId};', encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)

    def test_server_actor_response_dto_is_allowed(self):
        root = self.fixture()
        page = root / "cpf-admin/frontend/src/IncidentApi.ts"
        page.write_text("export interface Timeline { actorId: string; createdAt: string }", encoding="utf-8")
        module.verify(root)

    def test_actor_field_in_payload_still_rejected_with_dto_present(self):
        root = self.fixture()
        page = root / "cpf-admin/frontend/src/IncidentApi.ts"
        page.write_text("export interface Timeline { actorId: string }\nconst body={actorId: 'forged'};", encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)

    def test_controller_actor_fallback_rejected(self):
        root = self.fixture()
        controller = root / "cpf-admin/src/main/java/com/cpf/admin/opr/controller/FallbackController.java"
        controller.write_text(
            "private String requestUser(HttpServletRequest request, String fallback) { return fallback; }",
            encoding="utf-8",
        )
        with self.assertRaises(ValueError):
            module.verify(root)

    def test_raw_dynamic_log_actor_assignment_rejected(self):
        root = self.fixture()
        controller = root / "cpf-admin/src/main/java/com/cpf/admin/opr/controller/LogController.java"
        controller.write_text("request.setRequestUser(requestUser);", encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)

    def test_raw_runtime_command_rejected(self):
        root = self.fixture()
        controller = root / "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmRuntimeControlController.java"
        controller.write_text(controller.read_text(encoding="utf-8") + ' @RequestBody CpfRuntimeChangeCommand c', encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)


if __name__ == "__main__":
    unittest.main()
