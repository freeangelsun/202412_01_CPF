from __future__ import annotations

import importlib.util
import shutil
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
TOOL = ROOT / "cpf-tools/verification/verify_initial_operator_bootstrap_contract.py"
SPEC = importlib.util.spec_from_file_location("cpf_initial_operator_contract", TOOL)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)


def _copy_contract_surface(destination: Path) -> None:
    for relative in MODULE.REQUIRED_FILES:
        source = ROOT / relative
        target = destination / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)


def test_current_initial_operator_contract_is_complete() -> None:
    assert MODULE.validate(ROOT) == []


def test_negative_mutation_rejects_profile_specific_bootstrap_semantics(tmp_path: Path) -> None:
    _copy_contract_surface(tmp_path)
    target = tmp_path / "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmBootstrapInitializer.java"
    target.write_text(
        target.read_text(encoding="utf-8") + "\nif (profile.equals(\"prod\")) { }\n",
        encoding="utf-8")
    failures = MODULE.validate(tmp_path)
    assert "FORBIDDEN_PROFILE_SECURITY_BRANCH:adm-initial-runner" in failures


def test_negative_mutation_rejects_missing_canonical_bootstrap_environment(tmp_path: Path) -> None:
    _copy_contract_surface(tmp_path)
    target = tmp_path / "cpf-backoffice/online/src/main/resources/application.yml"
    target.write_text(
        target.read_text(encoding="utf-8").replace("    code: ${CPF_ENVIRONMENT_CODE:local}\n", ""),
        encoding="utf-8")
    failures = MODULE.validate(tmp_path)
    assert "MISSING_CONTRACT:mbw-initial-config:code: ${CPF_ENVIRONMENT_CODE:local}" in failures
