from __future__ import annotations

import os
import re
import unittest
from pathlib import Path

ROOT = Path(os.environ.get("CPF_REPO_ROOT", Path(__file__).resolve().parents[3])).resolve()
PRODUCT = ROOT / "cpf-common/src/main/java/com/cpf/common"
RUNTIME = ROOT / "cpf-starters/common/src/main/java/com/cpf/common"
AUTOCONFIG = RUNTIME / "runtime/CpfCommonJdbcAutoConfiguration.java"
CALENDAR = PRODUCT / "calendar/CmnCalendarService.java"
ADAPTER = PRODUCT / "calendar/CmnDurableCalendarChangePublisher.java"
EVENT = PRODUCT / "calendar/CmnCalendarChangeEvent.java"
PUBLISHER = RUNTIME / "runtime/cache/CpfCommonCacheRefreshPublisher.java"
LISTENER = RUNTIME / "runtime/cache/CpfCommonCacheRefreshListener.java"


class CmnCalendarDurableOutboxTest(unittest.TestCase):
    def test_product_mutations_use_cpf_common_transaction(self):
        source = CALENDAR.read_text(encoding="utf-8")
        annotations = re.findall(
            r'@Transactional\(transactionManager\s*=\s*"([^"]+)"',
            source,
        )
        self.assertGreaterEqual(annotations.count("cpfCommonTransactionManager"), 2)

    def test_product_publish_failure_is_not_swallowed(self):
        source = CALENDAR.read_text(encoding="utf-8")
        product_branch = source.split("if(productMode){", 1)[1].split("return;", 1)[0]
        self.assertIn("changePublisher.publish(event);", product_branch)
        self.assertNotIn("catch", product_branch)

    def test_adapter_uses_required_durable_outbox(self):
        source = ADAPTER.read_text(encoding="utf-8")
        self.assertIn("publishRequired", source)
        self.assertIn('CACHE_NAME = "businessCalendar"', source)
        wiring = AUTOCONFIG.read_text(encoding="utf-8")
        self.assertNotIn("@ConditionalOnMissingBean", source)
        self.assertIn("@ConditionalOnMissingBean(CmnCalendarChangePublisher.class)", wiring)
        self.assertIn("return new CmnDurableCalendarChangePublisher(publisher);", wiring)
        self.assertNotIn("publishAfterCommit", source)

    def test_shared_publisher_is_mandatory_and_invalidates_after_commit(self):
        source = PUBLISHER.read_text(encoding="utf-8")
        self.assertIn("Propagation.MANDATORY", source)
        self.assertIn("repository.insertEvent", source)
        self.assertIn("registerSynchronization", source)
        self.assertIn("afterCommit()", source)
        self.assertIn("refresher.refresh(cacheName)", source)

    def test_listener_refreshes_then_advances_durable_checkpoint(self):
        source = LISTENER.read_text(encoding="utf-8")
        refresh = source.index("refresher.refresh(cache);")
        checkpoint = source.index("repository.advanceCheckpoint")
        self.assertLess(refresh, checkpoint)
        self.assertIn("lastEventId = id;", source)
        self.assertIn("durable cache checkpoint initialization failed", source)

    def test_calendar_event_contract_fails_closed(self):
        source = EVENT.read_text(encoding="utf-8")
        self.assertIn('Set.of("UPSERT", "DELETE")', source)
        self.assertIn("businessDate is required", source)
        self.assertIn("version must be greater than zero", source)


if __name__ == "__main__":
    unittest.main()
