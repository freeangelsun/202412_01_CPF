#!/usr/bin/env python3
"""Fail-closed CPF Gradle wrapper and stack integrity verifier."""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import re
import sys

EXPECTED_GRADLE_VERSION = "9.1.0"
EXPECTED_JAVA_VERSION = "25"
EXPECTED_DISTRIBUTION_URL = (
    "https://services.gradle.org/distributions/gradle-9.1.0-bin.zip"
)
EXPECTED_DISTRIBUTION_SHA256 = (
    "a17ddd85a26b6a7f5ddb71ff8b05fc5104c0202c6e64782429790c933686c806"
)
EXPECTED_WRAPPER_JAR_SHA256 = (
    "76805e32c009c0cf0dd5d206bddc9fb22ea42e84db904b764f3047de095493f3"
)


def read_properties(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            raise ValueError(f"invalid property line: {path}:{raw}")
        key, value = line.split("=", 1)
        key = key.strip()
        if not key or key in values:
            raise ValueError(f"duplicate/blank property key: {path}:{key}")
        values[key] = value.strip().replace("\\:", ":")
    return values


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def verify(root: Path) -> dict[str, object]:
    errors: list[str] = []
    wrapper_properties = root / "gradle/wrapper/gradle-wrapper.properties"
    wrapper_jar = root / "gradle/wrapper/gradle-wrapper.jar"
    stack_properties = root / "gradle/cpf-stack.properties"
    gradlew = root / "gradlew"
    gradlew_bat = root / "gradlew.bat"

    required = [wrapper_properties, wrapper_jar, stack_properties, gradlew, gradlew_bat]
    missing = [str(path.relative_to(root)) for path in required if not path.is_file()]
    if missing:
        errors.append("missing required files: " + ", ".join(missing))

    wrapper: dict[str, str] = {}
    stack: dict[str, str] = {}
    if wrapper_properties.is_file():
        try:
            wrapper = read_properties(wrapper_properties)
        except (OSError, ValueError) as exc:
            errors.append(str(exc))
    if stack_properties.is_file():
        try:
            stack = read_properties(stack_properties)
        except (OSError, ValueError) as exc:
            errors.append(str(exc))

    if stack.get("gradleVersion") != EXPECTED_GRADLE_VERSION:
        errors.append(
            f"gradleVersion mismatch expected={EXPECTED_GRADLE_VERSION} "
            f"actual={stack.get('gradleVersion', '<missing>')}"
        )
    if stack.get("javaVersion") != EXPECTED_JAVA_VERSION:
        errors.append(
            f"javaVersion mismatch expected={EXPECTED_JAVA_VERSION} "
            f"actual={stack.get('javaVersion', '<missing>')}"
        )
    if wrapper.get("distributionUrl") != EXPECTED_DISTRIBUTION_URL:
        errors.append(
            f"distributionUrl mismatch expected={EXPECTED_DISTRIBUTION_URL} "
            f"actual={wrapper.get('distributionUrl', '<missing>')}"
        )
    if wrapper.get("distributionSha256Sum", "").lower() != EXPECTED_DISTRIBUTION_SHA256:
        errors.append(
            "distributionSha256Sum mismatch/missing "
            f"expected={EXPECTED_DISTRIBUTION_SHA256} "
            f"actual={wrapper.get('distributionSha256Sum', '<missing>')}"
        )
    if wrapper.get("validateDistributionUrl", "").lower() != "true":
        errors.append("validateDistributionUrl must be true")

    timeout = wrapper.get("networkTimeout", "")
    if not re.fullmatch(r"[1-9][0-9]*", timeout):
        errors.append(f"networkTimeout must be a positive integer actual={timeout or '<missing>'}")

    wrapper_jar_sha = sha256(wrapper_jar) if wrapper_jar.is_file() else None
    if wrapper_jar_sha != EXPECTED_WRAPPER_JAR_SHA256:
        errors.append(
            f"wrapper jar SHA-256 mismatch expected={EXPECTED_WRAPPER_JAR_SHA256} "
            f"actual={wrapper_jar_sha or '<missing>'}"
        )

    result: dict[str, object] = {
        "status": "PASS" if not errors else "FAIL",
        "gradleVersion": stack.get("gradleVersion"),
        "javaVersion": stack.get("javaVersion"),
        "distributionUrl": wrapper.get("distributionUrl"),
        "distributionSha256Sum": wrapper.get("distributionSha256Sum"),
        "wrapperJarSha256": wrapper_jar_sha,
        "errors": errors,
    }
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path("."))
    parser.add_argument("--json-output", type=Path)
    args = parser.parse_args()

    root = args.root.resolve()
    result = verify(root)
    rendered = json.dumps(result, ensure_ascii=False, indent=2, sort_keys=True)
    if args.json_output:
        args.json_output.parent.mkdir(parents=True, exist_ok=True)
        args.json_output.write_text(rendered + "\n", encoding="utf-8")
    print(rendered)
    return 0 if result["status"] == "PASS" else 1


if __name__ == "__main__":
    sys.exit(main())
