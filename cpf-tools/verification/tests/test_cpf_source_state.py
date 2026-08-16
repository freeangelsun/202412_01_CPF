from __future__ import annotations
import importlib.util
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "tools" / "cpf-source-state.py"
spec = importlib.util.spec_from_file_location("cpf_source_state", SCRIPT)
assert spec and spec.loader
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


def test_source_identity_changes_with_product_bytes_but_not_work_evidence(tmp_path: Path):
    (tmp_path / "cpf-core").mkdir()
    product = tmp_path / "cpf-core" / "a.txt"
    product.write_text("one", encoding="utf-8")
    work = tmp_path / "cpf-docs" / "work"
    work.mkdir(parents=True)
    (work / "PACKAGE_MANIFEST.json").write_text("{}", encoding="utf-8")
    first = module.snapshot(tmp_path, "source")
    (work / "PACKAGE_MANIFEST.json").write_text('{"changed":true}', encoding="utf-8")
    second = module.snapshot(tmp_path, "source")
    assert first["contentSha256"] == second["contentSha256"]
    product.write_text("two", encoding="utf-8")
    third = module.snapshot(tmp_path, "source")
    assert second["contentSha256"] != third["contentSha256"]


def test_managed_identity_detects_review_mutation(tmp_path: Path):
    work = tmp_path / "cpf-docs" / "work"
    work.mkdir(parents=True)
    target = work / "TEST_AND_EVIDENCE.md"
    target.write_text("before", encoding="utf-8")
    before = module.snapshot(tmp_path, "managed")
    target.write_text("after", encoding="utf-8")
    after = module.snapshot(tmp_path, "managed")
    assert before["contentSha256"] != after["contentSha256"]


def test_generated_build_outputs_are_excluded_but_cpf_tools_build_is_product_source(tmp_path: Path):
    generated = tmp_path / "cpf-core" / "build"
    generated.mkdir(parents=True)
    (generated / "generated.bin").write_text("x", encoding="utf-8")
    product = tmp_path / "cpf-tools" / "build"
    product.mkdir(parents=True)
    (product / "cpf-root-conventions.gradle").write_text("source", encoding="utf-8")
    result = module.snapshot(tmp_path, "managed")
    paths = {row["path"] for row in result["files"]}
    assert "cpf-core/build/generated.bin" not in paths
    assert "cpf-tools/build/cpf-root-conventions.gradle" in paths
