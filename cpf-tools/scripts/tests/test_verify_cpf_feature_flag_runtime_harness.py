from __future__ import annotations

import importlib.util
import shutil
from pathlib import Path


def load_module():
    script = Path(__file__).resolve().parents[1] / "verify-cpf-feature-flag-runtime-harness.py"
    spec = importlib.util.spec_from_file_location("feature_flag_runtime_harness", script)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


def test_harness_declares_all_security_and_lifecycle_assertions():
    module = load_module()
    source = module.HARNESS_SOURCE
    for token in [
        "context-sanitization",
        "cache-revision-audit",
        "provider-fallback",
        "controlled-state-precedence",
        "override-lifecycle",
        "kill-switch-validation",
        "raw targeting key not audited",
    ]:
        assert token in source


def test_runtime_harness_executes_against_repository_sources(tmp_path: Path):
    module = load_module()
    repo = Path(__file__).resolve().parents[3]
    if not all((repo / path).is_file() for path in module.SOURCE_PATHS):
        # Minimal overlay-only test environments do not contain the complete repository.
        return
    report = tmp_path / "feature-flag-runtime.json"
    result = module.run(repo, report)
    assert result["status"] == "PASS", result
    assert result["assertion_count"] == 6
    assert result["javac_exit_code"] == 0
    assert result["java_exit_code"] == 0
    assert report.is_file()
