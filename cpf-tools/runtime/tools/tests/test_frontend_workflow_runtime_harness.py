from pathlib import Path

def test_frontend_workflow_harness_targets_adm_and_backoffice_reference_sources():
    root = Path(__file__).resolve().parents[4]
    harness = root / "cpf-tools/verification/frontend-workflow-runtime/harness.cjs"
    text = harness.read_text(encoding="utf-8")
    assert "cpf-backoffice-web/frontend/src/features/employees/model/employeeModel.js" in text
    assert "cpf-biz-admin/frontend" not in text

def test_runtime_launcher_uses_current_reference_harness():
    root = Path(__file__).resolve().parents[4]
    launcher = root / "cpf-tools/verification/frontend-workflow-runtime/run-frontend-workflow-runtime-harness.py"
    text = launcher.read_text(encoding="utf-8")
    assert "run-frontend-workflow-harness.py" in text
    assert "cpf-backoffice-web" in text
