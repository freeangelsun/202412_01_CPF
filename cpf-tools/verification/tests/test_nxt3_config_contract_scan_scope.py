import importlib.util
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
MODULE_PATH = ROOT / "cpf-tools/verification/nxt3/verify_nxt3_config_contract.py"


def _load_module():
    spec = importlib.util.spec_from_file_location("verify_nxt3_config_contract", MODULE_PATH)
    assert spec and spec.loader
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def test_config_scan_excludes_cross_platform_python_toolchain_packages(tmp_path):
    module = _load_module()
    dependency = tmp_path / "python-env/Lib/site-packages/pip/_internal/example.py"
    dependency.parent.mkdir(parents=True)
    dependency.write_text('password = "third-party-example"\n', encoding="utf-8")
    assert not module.is_owned_text_file(dependency)
    assert module.is_owned_text_file(MODULE_PATH)


def test_config_scan_excludes_pytest_basetemp_but_keeps_evidence_payloads():
    module = _load_module()

    pytest_fixture = Path(
        "cpf-docs/governance/development-harness/evidence/platform/current/generated/"
        "pytest-basetemp/run/test_case/cpf-tools/example.py"
    )
    evidence_payload = Path(
        "cpf-docs/governance/development-harness/evidence/platform/current/"
        "generated/runtime/file-db-correlation.json"
    )
    product_source = Path("cpf-tools/example.py")

    assert not module.is_owned_text_path(pytest_fixture)
    assert module.is_owned_text_path(evidence_payload)
    assert module.is_owned_text_path(product_source)


def test_config_scan_allows_only_the_exact_git_lfs_pointer_specification_uri():
    """LFS pointer syntax is technical format metadata, never an operations endpoint."""
    module = _load_module()

    assert module.is_immutable_technical_spec_uri(
        "https://git-lfs.github.com/spec/v1"
    )
    assert not module.is_immutable_technical_spec_uri("https://github.com/cpf-team/cpf-framework")
    assert not module.is_immutable_technical_spec_uri("https://git-lfs.github.com/spec/v2")
