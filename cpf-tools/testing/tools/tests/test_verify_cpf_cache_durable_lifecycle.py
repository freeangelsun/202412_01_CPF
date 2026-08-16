from __future__ import annotations

import importlib.util
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-cache-durable-lifecycle.py"


def load():
    spec = importlib.util.spec_from_file_location("cache_durable_gate", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


def test_cache_durable_gate_passes_product_tree():
    module = load()
    result = module.verify(Path(__file__).resolve().parents[4])

    assert result["status"] == "PASS"
    assert set(result["vendors"]) == {"oracle", "postgresql", "mariadb"}
    assert result["sourceChecks"]["namespaceSqlNull"]
    assert result["sourceChecks"]["durableFirst"]
    assert result["sourceChecks"]["checkpointAfterApply"]
    assert result["sourceChecks"]["versionFence"]
    assert all(
        vendor["pack"]
        and vendor["dialect"]
        and all(vendor["tables"].values())
        for vendor in result["vendors"].values()
    )


def test_cache_durable_gate_requires_current_ledger_and_version_lifecycle_files():
    module = load()

    assert len(module.LEDGER) == 6
    assert len(module.VERSION) == 6
    assert "rollback/R101__cache_invalidation_ledger.sql" in module.LEDGER
    assert "rollback/R113__cache_invalidation_version_fence.sql" in module.VERSION
    assert "runtime/cache/cache_invalidation_queries.sql" in module.LEDGER
    assert "runtime/cache/cache_invalidation_version_queries.sql" in module.VERSION


def test_namespace_invalidation_uses_shared_store_sql_null_and_no_empty_string_default():
    repo = Path(__file__).resolve().parents[4]
    store = (
        repo
        / "cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/JdbcCpfCacheInvalidationStore.java"
    ).read_text(encoding="utf-8")

    assert "statement.setNull(4, java.sql.Types.VARCHAR)" in store
    for vendor in ("oracle", "postgresql", "mariadb"):
        ddl = (repo / f"cpf-tools/db/vendor/{vendor}/source/16_cache_invalidation_ledger.sql").read_text(
            encoding="utf-8"
        ).upper()
        line = next(line for line in ddl.splitlines() if "CACHE_KEY_VALUE" in line)
        assert "NOT NULL" not in line
        assert "DEFAULT ''" not in line
