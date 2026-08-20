#!/usr/bin/env python3
"""Fail-closed CPF cache capability source/consumer closure verifier.

Validates the current canonical ownership model:
- provider-neutral API belongs to :cpf-data,
- Spring Data Redis protocol/durable invalidation runtime belongs to internal :cpf-cache-spring-data-redis,
- Redis and Valkey are separate public provider starters,
- ADM consumes only the shared runtime coordinator,
- the former Valkey-local com.cpf.data.cache implementation files contain no active duplicate classes.
"""
from __future__ import annotations

import argparse
import json
from pathlib import Path

PUBLIC_API = [
    "cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCache.java",
    "cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCacheKey.java",
    "cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCacheValue.java",
    "cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCacheMetricsSnapshot.java",
    "cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCacheHealth.java",
    "cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCacheInvalidationPort.java",
    "cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfDistributedLockPort.java",
]
SHARED_RUNTIME = [
    "cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/SpringDataRedisCpfCache.java",
    "cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/CpfCacheInvalidationCoordinator.java",
    "cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/JdbcCpfCacheInvalidationStore.java",
    "cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/CpfRedisProtocolProviderSelection.java",
]
REDIS_PROVIDER = [
    "cpf-starters/data/cache/redis/src/main/java/com/cpf/data/cache/redis/CpfRedisCacheAutoConfiguration.java",
    "cpf-starters/data/cache/redis/src/main/java/com/cpf/data/cache/redis/RedisCpfCache.java",
    "cpf-starters/data/cache/redis/src/main/java/com/cpf/data/cache/redis/CpfRedisCacheProperties.java",
]
VALKEY_PROVIDER = [
    "cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache/valkey/CpfValkeyAutoConfiguration.java",
    "cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache/valkey/ValkeyCpfCache.java",
    "cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache/valkey/CpfValkeyProperties.java",
]
ADM_CONSUMER = "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmCacheOperationService.java"
SHARED_TEST = "cpf-starters/data/cache/spring-data-redis/src/test/java/com/cpf/data/cache/rediscommon/CpfCacheInvalidationCoordinatorTest.java"
REDIS_TEST = "cpf-starters/data/cache/redis/src/test/java/com/cpf/data/cache/redis/RedisCpfCachePortHealthTest.java"
VALKEY_TEST = "cpf-starters/data/cache/valkey/src/test/java/com/cpf/data/cache/valkey/ValkeyCpfCachePortHealthTest.java"
LEGACY_VALKEY_PACKAGE = "cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache"


def read(repo: Path, relative: str) -> str:
    path = repo / relative
    return path.read_text(encoding="utf-8") if path.is_file() else ""


def run(repo_root: Path, report_json: Path | None = None) -> dict:
    required_paths = [*PUBLIC_API, *SHARED_RUNTIME, *REDIS_PROVIDER, *VALKEY_PROVIDER,
                      ADM_CONSUMER, SHARED_TEST, REDIS_TEST, VALKEY_TEST]
    missing_paths = [path for path in required_paths if not (repo_root / path).is_file()]

    shared_port = read(repo_root, SHARED_RUNTIME[0])
    coordinator = read(repo_root, SHARED_RUNTIME[1])
    store = read(repo_root, SHARED_RUNTIME[2])
    redis_auto = read(repo_root, REDIS_PROVIDER[0])
    redis_port = read(repo_root, REDIS_PROVIDER[1])
    valkey_auto = read(repo_root, VALKEY_PROVIDER[0])
    valkey_port = read(repo_root, VALKEY_PROVIDER[1])
    adm = read(repo_root, ADM_CONSUMER)

    findings: list[dict] = []
    def require(condition: bool, finding_id: str, message: str, paths: list[str]):
        if not condition:
            findings.append({"severity": "P0", "id": finding_id, "message": message, "paths": paths})

    require("implements CpfCache, CpfDistributedLockPort" in shared_port,
            "CACHE-SHARED-PORT-CONTRACT-MISSING",
            "Shared Redis-protocol runtime must own CpfCache and distributed-lock semantics.", [SHARED_RUNTIME[0]])
    require("durable.version(" in coordinator and "durable.advanceVersion(" in coordinator
            and "reconcileNow()" in coordinator,
            "CACHE-SHARED-INVALIDATION-FENCE-MISSING",
            "Shared invalidation coordinator must retain durable replay and monotonic version fencing.", [SHARED_RUNTIME[1]])
    require("long version(String consumerId" in store and "void advanceVersion(" in store and "String consumerId, String tenantId" in store,
            "CACHE-JDBC-VERSION-FENCE-MISSING",
            "Durable invalidation store must implement per-consumer version fencing.", [SHARED_RUNTIME[2]])
    require("new RedisCpfCache" in redis_auto and "CpfCacheInvalidationCoordinator" in redis_auto
            and "JdbcCpfCacheInvalidationStore" in redis_auto,
            "CACHE-REDIS-PROVIDER-WIRING-MISSING",
            "Redis provider must compose the canonical shared runtime rather than a provider-local duplicate.", [REDIS_PROVIDER[0]])
    require("extends SpringDataRedisCpfCache" in redis_port,
            "CACHE-REDIS-SHARED-RUNTIME-NOT-USED",
            "Redis adapter must delegate protocol behavior to the internal shared runtime.", [REDIS_PROVIDER[1]])
    require("new ValkeyCpfCache" in valkey_auto and "CpfCacheInvalidationCoordinator" in valkey_auto
            and "JdbcCpfCacheInvalidationStore" in valkey_auto,
            "CACHE-VALKEY-PROVIDER-WIRING-MISSING",
            "Valkey provider must compose the canonical shared runtime rather than a provider-local duplicate.", [VALKEY_PROVIDER[0]])
    require("extends SpringDataRedisCpfCache" in valkey_port,
            "CACHE-VALKEY-SHARED-RUNTIME-NOT-USED",
            "Valkey adapter must delegate protocol behavior to the internal shared runtime.", [VALKEY_PROVIDER[1]])
    require("import com.cpf.data.cache.rediscommon.CpfCacheInvalidationCoordinator;" in adm
            and "import com.cpf.data.cache.CpfCacheInvalidationCoordinator;" not in adm,
            "CACHE-ADM-STALE-OWNER-CONSUMER",
            "ADM must consume the canonical shared invalidation coordinator owner.", [ADM_CONSUMER])

    active_legacy = []
    legacy_root = repo_root / LEGACY_VALKEY_PACKAGE
    if legacy_root.is_dir():
        active_legacy = [
            path.relative_to(repo_root).as_posix()
            for path in legacy_root.glob("*.java")
            if path.is_file()
        ]
    require(not active_legacy,
            "CACHE-VALKEY-SHARED-RUNTIME-DUPLICATE",
            "Valkey-local legacy implementation sources must be absent after approved cleanup or remain inactive migration tombstones before deletion.", active_legacy)

    require((repo_root / SHARED_TEST).is_file() and "duplicateAndOutOfOrderVersionsDoNotReapply" in read(repo_root, SHARED_TEST)
            and "reconcileReplaysDurableEventsAfterCheckpoint" in read(repo_root, SHARED_TEST),
            "CACHE-SHARED-RECOVERY-TEST-MISSING",
            "Shared cache runtime requires duplicate/out-of-order and reconciliation regression tests.", [SHARED_TEST])

    result = {
        "status": "PASS" if not findings and not missing_paths else "FAIL",
        "ownership": {
            "api": ":framework:data",
            "shared_runtime": ":internal:data:cache:spring-data-redis",
            "redis_provider": ":starters:cache:redis",
            "valkey_provider": ":starters:cache:valkey",
        },
        "missing_paths": missing_paths,
        "active_legacy_duplicate_sources": active_legacy,
        "tests": [SHARED_TEST, REDIS_TEST, VALKEY_TEST],
        "findings": findings + ([{
            "severity": "P0", "id": "CACHE-REQUIRED-SOURCE-MISSING",
            "message": "Required current cache source/test path is missing.", "paths": missing_paths
        }] if missing_paths else []),
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
