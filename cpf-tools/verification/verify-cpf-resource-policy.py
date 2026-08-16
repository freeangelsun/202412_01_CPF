#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
from pathlib import Path
import re
import sys

PROFILES = ("common", "local", "dev", "test", "stg", "prod")
REQUIRED_COMMON = {
    "heap.step.mb",
    "gradle.maxWorkers",
    "gradle.parallel",
    "gradle.jvm.xms",
    "gradle.jvm.xmx",
    "gradle.jvm.maxMetaspace",
    "test.xms",
    "test.xmx",
    "test.maxParallelForks",
    "frontend.node.maxOldSpace.mb",
    "runtime.web.xms",
    "runtime.web.xmx",
    "runtime.batch.xms",
    "runtime.batch.xmx",
    "runtime.jvm.maxMetaspace",
    "runtime.jvm.maxDirectMemory",
    "runtime.jvm.reservedCodeCache",
    "runtime.jvm.threadStack",
    "runtime.batch.enabledByDefault",
    "runtime.local.singleWebDefault",
    "runtime.memory.enforceCeiling",
}
HEAP_KEYS = (
    "test.xms",
    "test.xmx",
    "runtime.web.xms",
    "runtime.web.xmx",
    "runtime.batch.xms",
    "runtime.batch.xmx",
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
        key, value = key.strip(), value.strip()
        if not key or key in values:
            raise ValueError(f"duplicate/blank property key: {path}:{key}")
        values[key] = value
    return values


def memory_mb(value: str) -> int:
    match = re.fullmatch(r"([0-9]+)([mMgG])", value.strip())
    if not match:
        raise ValueError(f"invalid memory value: {value}")
    amount = int(match.group(1))
    return amount * 1024 if match.group(2).lower() == "g" else amount


def verify(root: Path) -> dict[str, object]:
    errors: list[str] = []
    warnings: list[str] = []
    policy_root = root / "gradle/cpf-runtime"
    parsed: dict[str, dict[str, str]] = {}
    for profile in PROFILES:
        source = policy_root / f"{profile}.properties"
        if not source.is_file():
            errors.append(f"missing profile: {source.relative_to(root)}")
            continue
        try:
            parsed[profile] = read_properties(source)
        except (OSError, ValueError) as exc:
            errors.append(str(exc))

    common = parsed.get("common", {})
    missing_common = sorted(REQUIRED_COMMON - common.keys())
    if missing_common:
        errors.append("common missing keys: " + ", ".join(missing_common))

    resolved_profiles: dict[str, dict[str, str]] = {}
    try:
        step = int(common.get("heap.step.mb", "250"))
        ceiling = int(common.get("runtime.memory.ceiling.mb", "1000"))
        if step != 250:
            errors.append(f"heap step must be 250MB actual={step}")
        if ceiling != 1000:
            errors.append(f"heap ceiling must be 1000MB actual={ceiling}")
        for profile in PROFILES[1:]:
            merged = dict(common)
            merged.update(parsed.get(profile, {}))
            resolved_profiles[profile] = merged
            if merged.get("runtime.memory.enforceCeiling", "").lower() != "true":
                errors.append(f"{profile} memory ceiling must be enforced")
            for key in HEAP_KEYS:
                if key not in merged:
                    errors.append(f"{profile} missing heap key={key}")
                    continue
                value = memory_mb(merged[key])
                if value < step or value > ceiling or value % step != 0:
                    errors.append(f"{profile} {key}={merged[key]} violates {step}MB step/{ceiling}MB ceiling")
            for xms_key, xmx_key in (("gradle.jvm.xms","gradle.jvm.xmx"),("test.xms","test.xmx"),("runtime.web.xms","runtime.web.xmx"),("runtime.batch.xms","runtime.batch.xmx")):
                if memory_mb(merged[xms_key]) > memory_mb(merged[xmx_key]):
                    errors.append(f"{profile} {xms_key} must be <= {xmx_key}")
    except (ValueError, TypeError) as exc:
        errors.append(str(exc))

    local = resolved_profiles.get("local", {})
    if local:
        if local.get("runtime.batch.enabledByDefault", "").lower() != "false":
            errors.append("local batch must be disabled by default")
        if local.get("runtime.local.singleWebDefault", "").lower() != "true":
            errors.append("local single Web WAS must be default")

    root_convention = root / "cpf-tools/build/cpf-root-conventions.gradle"
    start_local = root / "cpf-tools/runtime/tools/start-cpf-local.ps1"
    helper = root / "cpf-tools/runtime/tools/cpf-resource-policy.ps1"
    gradlew = root / "gradlew"
    gradlew_bat = root / "gradlew.bat"
    required_files = (root_convention, start_local, helper, gradlew, gradlew_bat)
    for source in required_files:
        if not source.is_file():
            errors.append(f"missing integration source: {source.relative_to(root)}")

    if root_convention.is_file():
        text = root_convention.read_text(encoding="utf-8")
        for token in (
            "cpf-resource.properties",
            "cpfResourceProfile",
            "cpfResource.${key}",
            "maxParallelForks",
            "maxHeapSize",
            "module cpf-resource.properties > -PcpfResource.<key> > environment profile > common",
        ):
            if token not in text:
                errors.append(f"root convention missing resource contract token={token}")

    if start_local.is_file():
        text = start_local.read_text(encoding="utf-8")
        for token in (
            "cpf-resource-policy.ps1",
            "Resolve-CpfResourcePolicy",
            "runtime.web.xmx",
            "runtime.batch.xmx",
            "MaxMetaspaceSize",
            "MaxDirectMemorySize",
            "ReservedCodeCacheSize",
        ):
            if token not in text:
                errors.append(f"start-cpf-local missing resource contract token={token}")

    for wrapper in (gradlew, gradlew_bat):
        if wrapper.is_file():
            text = wrapper.read_text(encoding="utf-8")
            for token in ("gradle/cpf-runtime", "CPF_RESOURCE_PROFILE", "gradle.jvm.xmx", "org.gradle.workers.max"):
                normalized = text.replace("\\", "/")
                if token not in normalized:
                    errors.append(f"{wrapper.name} missing project-local resource token={token}")

    result = {
        "status": "PASS" if not errors else "FAIL",
        "profiles": list(PROFILES),
        "policyRoot": str(policy_root.relative_to(root)),
        "local": {
            "heapStepMb": local.get("heap.step.mb"),
            "heapCeilingMb": local.get("runtime.memory.ceiling.mb"),
            "webXms": local.get("runtime.web.xms"),
            "webXmx": local.get("runtime.web.xmx"),
            "batchXms": local.get("runtime.batch.xms"),
            "batchXmx": local.get("runtime.batch.xmx"),
            "batchDefault": local.get("runtime.batch.enabledByDefault"),
            "singleWebDefault": local.get("runtime.local.singleWebDefault"),
        },
        "errors": errors,
        "warnings": warnings,
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
