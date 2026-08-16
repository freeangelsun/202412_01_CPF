from pathlib import Path


def test_frontend_workflow_harness_uses_supplied_build_root_once():
    root = Path(__file__).resolve().parents[4]
    harness = root / "cpf-tools/verification/frontend-workflow-runtime/harness.cjs"
    text = harness.read_text(encoding="utf-8")
    assert "path.join(root,rel)" in text
    assert "frontend-workflow-runtime/build',rel" not in text


def test_runtime_launcher_passes_compiled_build_root():
    root = Path(__file__).resolve().parents[4]
    launcher = root / "cpf-tools/verification/frontend-workflow-runtime/run-frontend-workflow-runtime-harness.py"
    text = launcher.read_text(encoding="utf-8")
    assert "str(base/'build')" in text
    assert "str(root)],cwd=root,check=True" not in text
