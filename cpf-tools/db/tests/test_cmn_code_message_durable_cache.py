from __future__ import annotations

import os
import unittest
from pathlib import Path

ROOT = Path(os.environ.get("CPF_REPO_ROOT", Path(__file__).resolve().parents[3])).resolve()
JAVA = ROOT / "cpf-common/src/main/java/com/cpf/common"

class CmnCodeMessageDurableCacheContractTest(unittest.TestCase):
    def test_code_and_message_mutations_share_cmn_transaction_and_required_outbox(self):
        for relative in ["cde/service/CodeCacheService.java", "msg/service/MessageCacheService.java"]:
            source = (JAVA / relative).read_text(encoding="utf-8")
            self.assertIn('@Transactional(transactionManager = "cmnTransactionManager")', source)
            self.assertIn("publishRequired", source)
            self.assertIn("registerSynchronization", source)
            self.assertIn("afterCommit", source)

    def test_event_store_is_durable_and_fail_closed(self):
        publisher = (JAVA / "ref/service/CacheRefreshEventPublisher.java").read_text(encoding="utf-8")
        store = (JAVA / "ref/service/CacheRefreshEventStore.java").read_text(encoding="utf-8")
        listener = (JAVA / "ref/service/CacheRefreshEventListener.java").read_text(encoding="utf-8")
        self.assertIn("insertRequired", publisher)
        self.assertIn("Propagation.MANDATORY", store)
        self.assertIn("Propagation.REQUIRES_NEW", store)
        self.assertIn("durable checkpoint", listener.lower())
        for cache in ["codeCache", "messageCache", "businessCalendar", "commonTemplate"]:
            self.assertIn(cache, listener)

    def test_reload_failure_preserves_existing_snapshot_and_records_failure(self):
        code_source = (JAVA / "cde/service/CodeCacheService.java").read_text(encoding="utf-8")
        message_source = (JAVA / "msg/service/MessageCacheService.java").read_text(encoding="utf-8")
        self.assertIn("Existing cache is preserved", code_source)
        self.assertIn("Existing cache is preserved", message_source)
        self.assertIn("lastRefreshFailure", message_source)

if __name__ == "__main__":
    unittest.main()
