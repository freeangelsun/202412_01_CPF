from __future__ import annotations

import os
import unittest
from pathlib import Path

ROOT = Path(os.environ.get("CPF_REPO_ROOT", Path(__file__).resolve().parents[3])).resolve()
COMMON = ROOT / "cpf-starters/common/src/main/java/com/cpf/common"
CODE = COMMON / "code/service/JdbcCpfCodeService.java"
CATALOG = COMMON / "message/service/CmnErrorCatalogCache.java"
MANAGEMENT = COMMON / "message/service/CmnCommonCatalogManagementService.java"
PUBLISHER = COMMON / "runtime/cache/CpfCommonCacheRefreshPublisher.java"
REPOSITORY = COMMON / "runtime/cache/CpfCommonCacheRefreshEventRepository.java"
LISTENER = COMMON / "runtime/cache/CpfCommonCacheRefreshListener.java"


class CmnCodeMessageDurableCacheContractTest(unittest.TestCase):
    def test_code_service_is_read_through_and_never_synthesizes_cache_success(self):
        source = CODE.read_text(encoding="utf-8")
        self.assertIn('CACHE = "codeCache"', source)
        self.assertIn("jdbc.query(", source)
        self.assertIn('throw new IllegalStateException("CPF Common code cache is not configured")', source)
        self.assertIn("cache.put(", source)
        self.assertIn("refresh() { requireCache().clear(); }", source)

    def test_error_catalog_preserves_existing_hits_on_db_failure_and_uses_version_fence(self):
        source = CATALOG.read_text(encoding="utf-8")
        self.assertIn("return responses.get(key);", source)
        self.assertIn("return messages.get(key);", source)
        self.assertIn("reconcileFence()", source)
        self.assertIn("signals.catalogFallback", source)
        self.assertIn("responses.clear();", source)
        self.assertIn("messages.clear();", source)

    def test_catalog_mutations_use_cpf_common_transaction_and_after_commit_invalidation(self):
        source = MANAGEMENT.read_text(encoding="utf-8")
        self.assertIn('TX = "cpfCommonTransactionManager"', source)
        self.assertGreaterEqual(source.count("@Transactional(transactionManager = TX)"), 6)
        self.assertIn("registerSynchronization", source)
        self.assertIn("afterCommit()", source)
        self.assertIn("cache.invalidateAll()", source)
        self.assertIn("audit.record", source)

    def test_shared_cache_event_store_is_durable_and_fail_closed(self):
        publisher = PUBLISHER.read_text(encoding="utf-8")
        repository = REPOSITORY.read_text(encoding="utf-8")
        listener = LISTENER.read_text(encoding="utf-8")
        self.assertIn("Propagation.MANDATORY", publisher)
        self.assertIn("repository.insertEvent", publisher)
        self.assertIn("Common cache refresh event persistence failed", repository)
        self.assertIn("CMN_CACHE_REFRESH_CHECKPOINT", repository)
        self.assertIn("refresher.refresh(cache);", listener)
        self.assertIn("repository.advanceCheckpoint", listener)


if __name__ == "__main__":
    unittest.main()
