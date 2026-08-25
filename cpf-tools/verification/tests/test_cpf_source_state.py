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


def test_ide_and_module_bin_outputs_are_excluded(tmp_path: Path):
    product = tmp_path / "cpf-core" / "src"
    product.mkdir(parents=True)
    (product / "Core.java").write_text("class Core {}", encoding="utf-8")
    module_bin = tmp_path / "cpf-core" / "bin"
    module_bin.mkdir(parents=True)
    (module_bin / "Core.class").write_bytes(b"generated")
    vscode = tmp_path / ".vscode"
    vscode.mkdir()
    (vscode / "settings.json").write_text("{}", encoding="utf-8")
    result = module.snapshot(tmp_path, "source")
    paths = {row["path"] for row in result["files"]}
    assert "cpf-core/src/Core.java" in paths
    assert "cpf-core/bin/Core.class" not in paths
    assert ".vscode/settings.json" not in paths


def test_release_template_bin_scripts_are_product_source(tmp_path: Path):
    # cpf-tools/release/*/templates/bin/**은 컴파일 산출물이 아니라 고객이 자신의 bin/에
    # 그대로 설치하는 CLI Template Source다. 일반 module-root bin/(컴파일 산출물)과 혼동해
    # 제외하면 안 된다.
    template_bin = tmp_path / "cpf-tools" / "release" / "public" / "templates" / "bin"
    template_bin.mkdir(parents=True)
    (template_bin / "cpf-bootstrap.sh").write_text("#!/bin/sh\necho bootstrap\n", encoding="utf-8")
    result = module.snapshot(tmp_path, "source")
    paths = {row["path"] for row in result["files"]}
    assert "cpf-tools/release/public/templates/bin/cpf-bootstrap.sh" in paths


def test_jvm_crash_and_heap_dump_artifacts_do_not_change_any_identity_scope(tmp_path: Path):
    product = tmp_path / "cpf-core" / "src"
    product.mkdir(parents=True)
    (product / "Core.java").write_text("class Core {}", encoding="utf-8")
    before = {scope: module.snapshot(tmp_path, scope) for scope in ("source", "managed")}

    for name in (
        "hs_err_pid123.log",
        "replay_pid123.log",
        "java_pid123.hprof",
        "manual.hprof",
        "native.stackdump",
    ):
        (tmp_path / name).write_bytes(b"automatic-jvm-garbage")
    nested_cache = tmp_path / "cpf-common" / "cpf-docs" / "work" / "evidence" / "generated" / "gradle"
    nested_cache.mkdir(parents=True)
    (nested_cache / "cache.bin").write_bytes(b"ide-tooling-api-cache")

    after = {scope: module.snapshot(tmp_path, scope) for scope in ("source", "managed")}
    for scope in ("source", "managed"):
        assert before[scope]["contentSha256"] == after[scope]["contentSha256"]
        assert before[scope]["fileCount"] == after[scope]["fileCount"]
        assert before[scope]["totalBytes"] == after[scope]["totalBytes"]
