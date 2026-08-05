#!/usr/bin/env python3
"""Fail-closed CPF cache capability source/consumer closure verifier.

The gate validates the public CpfCachePort boundary, Caffeine L1 semantics, Valkey/Redis L2
compatibility beans, durable invalidation consumer wiring, and baseline source completeness.
It intentionally fails when an imported product class is absent instead of treating an
autoconfiguration declaration as a real implementation.
"""
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path

PUBLIC_API = [
    "cpf-core/src/main/java/com/cpf/core/api/cache/CpfCachePort.java",
    "cpf-core/src/main/java/com/cpf/core/api/cache/CpfCacheKey.java",
    "cpf-core/src/main/java/com/cpf/core/api/cache/CpfCacheValue.java",
    "cpf-core/src/main/java/com/cpf/core/api/cache/CpfCacheMetricsSnapshot.java",
    "cpf-core/src/main/java/com/cpf/core/api/cache/CpfCacheHealth.java",
    "cpf-core/src/main/java/com/cpf/core/api/cache/CpfDistributedLockPort.java",
]
CAFFEINE = "cpf-starters/data/cache-caffeine/src/main/java/com/cpf/starter/data/cache/caffeine/CaffeineCpfCachePort.java"
VALKEY_COMPAT = "cpf-starters/data/cache-valkey/src/main/java/com/cpf/starter/data/cache/valkey/CpfRedisCompatibilityAutoConfiguration.java"
VALKEY_NATIVE = "cpf-starters/data/cache-valkey/src/main/java/com/cpf/starter/data/cache/valkey/CpfValkeyCache.java"
ADM_CONSUMER = "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmCacheOperationService.java"
LOCAL_TEST = "cpf-starters/data/cache-caffeine/src/test/java/com/cpf/common/cache/CpfLocalCacheProviderTest.java"
INVALIDATION_TEST = "cpf-starters/data/cache-valkey/src/test/java/com/cpf/common/cache/CpfCacheInvalidationCoordinatorTest.java"

REQUIRED_CAFFEINE_TOKENS = [
    "implements CpfCachePort",
    "ttl.isNegative()",
    "ttl.isZero()",
    "maximumPayloadBytes",
    "expiresAt().isBefore",
    "CpfCacheMetricsSnapshot",
    "CpfCacheHealth",
]
REQUIRED_VALKEY_TOKENS = [
    "CpfCachePort",
    "CpfDistributedLockPort",
    "CpfCacheAsideService",
    "CpfCacheInvalidationPort",
    "CpfCacheInvalidationCoordinator",
    "CpfRedisCacheProvider",
    "properties.validate",
    "CpfSecretProvider",
]
REQUIRED_ADM_TOKENS = [
    "CpfCachePort",
    "CpfCacheInvalidationCoordinator",
    "active.request(",
    "requestNamespace",
    "reconcileNow",
]

IMPORT_PATTERN = re.compile(r"^import\s+(com\.cpf\.common\.cache\.([A-Za-z0-9_]+));", re.MULTILINE)


def read(repo: Path, relative: str) -> str:
    path = repo / relative
    return path.read_text(encoding="utf-8") if path.is_file() else ""


def run(repo_root: Path, report_json: Path | None = None) -> dict:
    required_paths = [*PUBLIC_API, CAFFEINE, VALKEY_COMPAT, VALKEY_NATIVE, ADM_CONSUMER, LOCAL_TEST, INVALIDATION_TEST]
    missing_paths = [path for path in required_paths if not (repo_root / path).is_file()]
    caffeine = read(repo_root, CAFFEINE)
    valkey = read(repo_root, VALKEY_COMPAT)
    native = read(repo_root, VALKEY_NATIVE)
    adm = read(repo_root, ADM_CONSUMER)

    missing_caffeine_tokens = [token for token in REQUIRED_CAFFEINE_TOKENS if token not in caffeine]
    missing_valkey_tokens = [token for token in REQUIRED_VALKEY_TOKENS if token not in valkey]
    missing_adm_tokens = [token for token in REQUIRED_ADM_TOKENS if token not in adm]

    imported_common_cache_classes = sorted({match.group(2) for match in IMPORT_PATTERN.finditer(valkey)})
    imported_common_cache_paths = {
        name: f"cpf-common/src/main/java/com/cpf/common/cache/{name}.java"
        for name in imported_common_cache_classes
    }
    missing_imported_classes = [
        relative for relative in imported_common_cache_paths.values()
        if not (repo_root / relative).is_file()
    ]

    native_unified_port = "implements CpfCachePort" in native
    compatibility_provider_declared = "CpfRedisCacheProvider cpfRedisCacheProvider" in valkey
    findings: list[dict] = []
    if missing_imported_classes:
        findings.append({
            "severity": "P0",
            "id": "CACHE-VALKEY-MISSING-IMPLEMENTATION-CLASSES",
            "message": "Valkey compatibility auto-configuration imports product classes absent from the repository.",
            "paths": missing_imported_classes,
        })
    if not native_unified_port and not compatibility_provider_declared:
        findings.append({
            "severity": "P0",
            "id": "CACHE-VALKEY-NO-CPF-CACHE-PORT",
            "message": "Valkey starter exposes neither a native CpfCachePort nor a compatibility provider bean.",
            "paths": [VALKEY_NATIVE, VALKEY_COMPAT],
        })
    if missing_paths or missing_caffeine_tokens or missing_valkey_tokens or missing_adm_tokens:
        findings.append({
            "severity": "P1",
            "id": "CACHE-CAPABILITY-CONTRACT-INCOMPLETE",
            "message": "Required cache source, consumer, or assertion tokens are incomplete.",
            "missing_paths": missing_paths,
            "missing_caffeine_tokens": missing_caffeine_tokens,
            "missing_valkey_tokens": missing_valkey_tokens,
            "missing_adm_tokens": missing_adm_tokens,
        })

    result = {
        "status": "PASS" if not findings else "FAIL",
        "public_api_paths": PUBLIC_API,
        "caffeine_adapter": CAFFEINE,
        "valkey_compatibility": VALKEY_COMPAT,
        "valkey_native": VALKEY_NATIVE,
        "adm_consumer": ADM_CONSUMER,
        "tests": [LOCAL_TEST, INVALIDATION_TEST],
        "missing_paths": missing_paths,
        "imported_common_cache_classes": imported_common_cache_classes,
        "missing_imported_classes": missing_imported_classes,
        "native_valkey_implements_cpf_cache_port": native_unified_port,
        "compatibility_provider_declared": compatibility_provider_declared,
        "assertions": {
            "caffeine_ttl_payload_expiry_metrics_health": not missing_caffeine_tokens,
            "valkey_port_lock_secret_durable_invalidation": not missing_valkey_tokens and not missing_imported_classes,
            "adm_key_namespace_reconcile_consumer": not missing_adm_tokens,
            "positive_and_recovery_tests_present": (repo_root / LOCAL_TEST).is_file() and (repo_root / INVALIDATION_TEST).is_file(),
        },
        "findings": findings,
    }
    if report_json:
        report_json.parent.mkdir(parents=True, exist_ok=True)
        report_json.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo-root", type=Path, default=Path("."))
    parser.add_argument("--report-json", type=Path)
    args = parser.parse_args()
    result = run(args.repo_root.resolve(), args.report_json)
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0 if result["status"] == "PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())
