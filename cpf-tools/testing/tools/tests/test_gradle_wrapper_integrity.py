from __future__ import annotations

import importlib.util
from pathlib import Path
import shutil

SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/build/tools/verify-cpf-gradle-wrapper-integrity.py"
spec = importlib.util.spec_from_file_location("gradle_wrapper_integrity", SCRIPT)
assert spec and spec.loader
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


def fixture(tmp_path: Path) -> Path:
    (tmp_path / "gradle/wrapper").mkdir(parents=True)
    (tmp_path / "gradle").mkdir(exist_ok=True)
    (tmp_path / "gradle/wrapper/gradle-wrapper.properties").write_text(
        "distributionBase=GRADLE_USER_HOME\n"
        "distributionPath=wrapper/dists\n"
        "distributionUrl=https\\://services.gradle.org/distributions/gradle-9.1.0-bin.zip\n"
        f"distributionSha256Sum={module.EXPECTED_DISTRIBUTION_SHA256}\n"
        "networkTimeout=10000\n"
        "validateDistributionUrl=true\n"
        "zipStoreBase=GRADLE_USER_HOME\n"
        "zipStorePath=wrapper/dists\n",
        encoding="utf-8",
    )
    (tmp_path / "gradle/cpf-stack.properties").write_text(
        "javaVersion=25\ngradleVersion=9.1.0\n", encoding="utf-8"
    )
    jar = tmp_path / "gradle/wrapper/gradle-wrapper.jar"
    # Tests patch the expected digest to the deterministic fixture bytes. The verifier still
    # compares real repository bytes against the published official digest.
    jar.write_bytes(b"cpf-test-wrapper-jar")
    module.EXPECTED_WRAPPER_JAR_SHA256 = module.sha256(jar)
    (tmp_path / "gradlew").write_text("#!/bin/sh\n", encoding="utf-8")
    (tmp_path / "gradlew.bat").write_text("@echo off\r\n", encoding="utf-8")
    return tmp_path


def test_valid_wrapper_contract_passes(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    assert module.verify(root)["status"] == "PASS"


def test_missing_distribution_checksum_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    p = root / "gradle/wrapper/gradle-wrapper.properties"
    p.write_text(
        "\n".join(
            line for line in p.read_text(encoding="utf-8").splitlines()
            if not line.startswith("distributionSha256Sum=")
        ) + "\n",
        encoding="utf-8",
    )
    result = module.verify(root)
    assert result["status"] == "FAIL"
    assert any("distributionSha256Sum" in error for error in result["errors"])


def test_mutated_wrapper_jar_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    expected = module.EXPECTED_WRAPPER_JAR_SHA256
    (root / "gradle/wrapper/gradle-wrapper.jar").write_bytes(b"tampered")
    module.EXPECTED_WRAPPER_JAR_SHA256 = expected
    result = module.verify(root)
    assert result["status"] == "FAIL"
    assert any("wrapper jar SHA-256" in error for error in result["errors"])


def test_stack_version_drift_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    (root / "gradle/cpf-stack.properties").write_text(
        "javaVersion=21\ngradleVersion=8.14\n", encoding="utf-8"
    )
    result = module.verify(root)
    assert result["status"] == "FAIL"
    assert any("gradleVersion mismatch" in error for error in result["errors"])
    assert any("javaVersion mismatch" in error for error in result["errors"])
