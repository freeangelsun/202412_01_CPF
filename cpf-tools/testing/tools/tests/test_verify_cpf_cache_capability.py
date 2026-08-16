from __future__ import annotations
import importlib.util
from pathlib import Path


def load_module():
    script = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-cache-capability.py"
    spec = importlib.util.spec_from_file_location("cache_capability", script)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


def write(repo: Path, relative: str, text: str = "class X {}") -> None:
    path = repo / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def complete_fixture(repo: Path, module) -> None:
    for path in module.PUBLIC_API:
        write(repo, path)
    write(repo, module.SHARED_RUNTIME[0], "class X implements CpfCachePort, CpfDistributedLockPort {}")
    write(repo, module.SHARED_RUNTIME[1], "durable.version( durable.advanceVersion( reconcileNow()")
    write(repo, module.SHARED_RUNTIME[2], "long version(String consumerId void advanceVersion( String consumerId, String tenantId")
    write(repo, module.SHARED_RUNTIME[3])
    write(repo, module.REDIS_PROVIDER[0], "new RedisCpfCachePort CpfCacheInvalidationCoordinator JdbcCpfCacheInvalidationStore")
    write(repo, module.REDIS_PROVIDER[1], "class X extends SpringDataRedisCpfCachePort {}")
    write(repo, module.REDIS_PROVIDER[2])
    write(repo, module.VALKEY_PROVIDER[0], "new ValkeyCpfCachePort CpfCacheInvalidationCoordinator JdbcCpfCacheInvalidationStore")
    write(repo, module.VALKEY_PROVIDER[1], "class X extends SpringDataRedisCpfCachePort {}")
    write(repo, module.VALKEY_PROVIDER[2])
    write(repo, module.ADM_CONSUMER, "import com.cpf.data.cache.rediscommon.CpfCacheInvalidationCoordinator;")
    write(repo, module.SHARED_TEST, "duplicateAndOutOfOrderVersionsDoNotReapply reconcileReplaysDurableEventsAfterCheckpoint")
    write(repo, module.REDIS_TEST)
    write(repo, module.VALKEY_TEST)


def test_complete_current_cache_contract_passes(tmp_path: Path):
    module = load_module()
    complete_fixture(tmp_path, module)
    result = module.run(tmp_path)
    assert result["status"] == "PASS", result
    assert result["active_legacy_duplicate_sources"] == []


def test_valkey_local_duplicate_runtime_fails_closed(tmp_path: Path):
    module = load_module()
    complete_fixture(tmp_path, module)
    duplicate = module.LEGACY_VALKEY_PACKAGE + "/CpfCacheInvalidationCoordinator.java"
    write(tmp_path, duplicate, "package com.cpf.data.cache; public class CpfCacheInvalidationCoordinator {}")
    result = module.run(tmp_path)
    assert result["status"] == "FAIL"
    assert duplicate in result["active_legacy_duplicate_sources"]
    assert any(f["id"] == "CACHE-VALKEY-SHARED-RUNTIME-DUPLICATE" for f in result["findings"])


def test_adm_stale_legacy_owner_fails_closed(tmp_path: Path):
    module = load_module()
    complete_fixture(tmp_path, module)
    write(tmp_path, module.ADM_CONSUMER, "import com.cpf.data.cache.CpfCacheInvalidationCoordinator;")
    result = module.run(tmp_path)
    assert result["status"] == "FAIL"
    assert any(f["id"] == "CACHE-ADM-STALE-OWNER-CONSUMER" for f in result["findings"])
