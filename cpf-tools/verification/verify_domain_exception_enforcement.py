#!/usr/bin/env python3
"""Verify the current Generated Domain manual-exception fail-closed contract.

The canonical policy lives in the Starter Catalog. The removed NXT2 generated-domain
contract and project-local generator metadata are intentionally not consulted.
"""
from __future__ import annotations

import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
failures: list[str] = []
catalog_path = ROOT / "cpf-tools/generator/contracts/cpf-starter-catalog.json"
schema_path = ROOT / "cpf-tools/generator/contracts/manual-configuration-exception.schema.json"
engine_path = ROOT / "cpf-tools/generator/engine/cpf_domain_generator.py"
runtime_path = (
    ROOT
    / "cpf-starters/base/runtime/src/main/java/com/cpf/starter/runtime/"
    "CpfGeneratedDomainPolicyRuntimeVerifier.java"
)
for path, label in (
    (catalog_path, "STARTER_CATALOG"),
    (schema_path, "EXCEPTION_SCHEMA"),
    (engine_path, "GENERATOR_ENGINE"),
    (runtime_path, "RUNTIME_VERIFIER"),
):
    if not path.is_file():
        failures.append(f"{label}_MISSING")

if catalog_path.is_file():
    catalog = json.loads(catalog_path.read_text(encoding="utf-8-sig"))
    policy = catalog.get("approvedExternalExceptionPolicy", {})
    inheritance_policy = catalog.get("standardInheritancePolicy", {})
    fail_closed_policy = {
        "versionDriftPolicy": "FAIL_CLOSED",
        "expiredPolicy": "FAIL_CLOSED",
        "unregisteredPolicy": "FAIL_CLOSED",
        "returnToStandardPathRequired": True,
    }
    if inheritance_policy.get("failClosed") is not True or any(
        policy.get(field) != expected for field, expected in fail_closed_policy.items()
    ):
        failures.append("CATALOG_EXCEPTION_POLICY_NOT_FAIL_CLOSED")
    if policy.get("registryPath") != "config/cpf-approved-exceptions.csv":
        failures.append("REGISTRY_PATH_DRIFT")
    if policy.get("runtimeResource") != "META-INF/cpf/cpf-approved-exceptions.csv":
        failures.append("RUNTIME_EXCEPTION_RESOURCE_DRIFT")
    if policy.get("approvedStatus") != "APPROVED":
        failures.append("APPROVED_STATUS_DRIFT")
    if policy.get("hashAlgorithm") != "SHA-256-METADATA-AND-CONFIG-CONTENT":
        failures.append("HASH_ALGORITHM_DRIFT")
    required = set(policy.get("requiredFields", []))
    for field in {
        "exception_id",
        "module",
        "capability",
        "artifact",
        "version",
        "approved_at",
        "expires_at",
        "status",
        "config_hash",
    }:
        if field not in required:
            failures.append(f"REQUIRED_FIELD_MISSING:{field}")

if engine_path.is_file():
    engine = engine_path.read_text(encoding="utf-8-sig", errors="strict")
    for token in (
        "com.cpf.core.api.error.CpfValidationException",
        "com.cpf.core.api.error.CpfBusinessException",
        "CpfErrorCode.NOT_FOUND",
    ):
        if token not in engine:
            failures.append(f"DOMAIN_NEUTRAL_EXCEPTION_MISSING:{token}")
    for token in (
        "org.springframework.web.server.ResponseStatusException",
        "org.springframework.http.HttpStatusException",
        "manifest/domain-manifest.json",
        "manifest/generator-ownership.json",
    ):
        if token in engine:
            failures.append(f"GENERATOR_FORBIDDEN_TOKEN:{token}")

if runtime_path.is_file():
    runtime = runtime_path.read_text(encoding="utf-8-sig", errors="strict")
    for token in (
        "META-INF/cpf/cpf-approved-exceptions.csv",
        "Duplicate approved exception id",
        "External exception is not approved",
        "External exception is expired",
        "config_hash(",
        "active_config_hash(",
        "Approved exception id drift",
        "Runtime attestation property is missing",
        "exceptionRegistrySha256",
    ):
        if token not in runtime:
            failures.append(f"RUNTIME_EXCEPTION_ENFORCEMENT_MISSING:{token}")

failures = sorted(set(failures))
print("CPF_DOMAIN_EXCEPTION_ENFORCEMENT=" + ("PASS" if not failures else "FAIL"))
print(f"failures={len(failures)}")
for failure in failures:
    print(failure)
sys.exit(0 if not failures else 1)
