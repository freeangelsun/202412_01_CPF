from __future__ import annotations

import importlib.util
from pathlib import Path


def load_module():
    script = Path(__file__).resolve().parents[1] / "verify-cpf-cache-capability.py"
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
    write(repo, module.CAFFEINE, " ".join(module.REQUIRED_CAFFEINE_TOKENS))
    imported = ["CpfCacheAsideService", "CpfCacheInvalidationCoordinator", "CpfRedisCacheProvider"]
    imports = "\n".join(f"import com.cpf.common.cache.{name};" for name in imported)
    write(repo, module.VALKEY_COMPAT,
          imports + "\n" + " ".join(module.REQUIRED_VALKEY_TOKENS) +
          " CpfRedisCacheProvider cpfRedisCacheProvider")
    for name in imported:
        write(repo, f"cpf-common/src/main/java/com/cpf/common/cache/{name}.java")
    write(repo, module.VALKEY_NATIVE, "final class CpfValkeyCache {}")
    write(repo, module.ADM_CONSUMER, " ".join(module.REQUIRED_ADM_TOKENS))
    write(repo, module.LOCAL_TEST)
    write(repo, module.INVALIDATION_TEST)


def test_complete_cache_contract_passes(tmp_path: Path):
    module = load_module()
    complete_fixture(tmp_path, module)
    result = module.run(tmp_path)
    assert result["status"] == "PASS", result
    assert result["assertions"]["valkey_port_lock_secret_durable_invalidation"]


def test_missing_imported_valkey_provider_fails_closed(tmp_path: Path):
    module = load_module()
    complete_fixture(tmp_path, module)
    missing = tmp_path / "cpf-common/src/main/java/com/cpf/common/cache/CpfRedisCacheProvider.java"
    missing.unlink()
    result = module.run(tmp_path)
    assert result["status"] == "FAIL"
    assert str(missing.relative_to(tmp_path)).replace("\\", "/") in result["missing_imported_classes"]
    assert any(finding["id"] == "CACHE-VALKEY-MISSING-IMPLEMENTATION-CLASSES" for finding in result["findings"])
