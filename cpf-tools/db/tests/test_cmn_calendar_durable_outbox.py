from __future__ import annotations

import os
import re
import unittest
from pathlib import Path

ROOT = Path(os.environ.get("CPF_REPO_ROOT", Path(__file__).resolve().parents[3]))
CALENDAR = ROOT / "cpf-common/src/main/java/com/cpf/common/calendar/CmnCalendarService.java"
ADAPTER = ROOT / "cpf-common/src/main/java/com/cpf/common/calendar/CmnDurableCalendarChangePublisher.java"
EVENT = ROOT / "cpf-common/src/main/java/com/cpf/common/calendar/CmnCalendarChangeEvent.java"
LISTENER = ROOT / "cpf-common/src/main/java/com/cpf/common/ref/service/CacheRefreshEventListener.java"


class CmnCalendarDurableOutboxTest(unittest.TestCase):
    def test_product_mutations_use_cmn_transaction(self):
        source = CALENDAR.read_text(encoding="utf-8")
        annotations = re.findall(r'@Transactional\(transactionManager="([^"]+)"\)', source)
        self.assertGreaterEqual(annotations.count("cmnTransactionManager"), 2)

    def test_product_publish_failure_is_not_swallowed(self):
        source = CALENDAR.read_text(encoding="utf-8")
        product_branch = source.split("if(productMode){", 1)[1].split("return;", 1)[0]
        self.assertIn("changePublisher.publish(event);", product_branch)
        self.assertNotIn("catch", product_branch)

    def test_adapter_uses_required_durable_outbox(self):
        source = ADAPTER.read_text(encoding="utf-8")
        self.assertIn("publisher.publishRequired", source)
        self.assertIn('CACHE_NAME = "businessCalendar"', source)
        self.assertNotIn("publishAfterCommit", source)
        self.assertNotIn("noop", source)

    def test_listener_consumes_calendar_events_without_stalling_checkpoint(self):
        source = LISTENER.read_text(encoding="utf-8")
        self.assertIn('case "businessCalendar"', source)
        self.assertIn('"commonTemplate"', source)

    def test_calendar_event_contract_fails_closed(self):
        source = EVENT.read_text(encoding="utf-8")
        self.assertIn('Set.of("UPSERT", "DELETE")', source)
        self.assertIn("businessDate is required", source)
        self.assertIn("version must be greater than zero", source)

    def test_default_adapter_allows_customer_override(self):
        source = ADAPTER.read_text(encoding="utf-8")
        self.assertIn("@ConditionalOnMissingBean(CmnCalendarChangePublisher.class)", source)


if __name__ == "__main__":
    unittest.main()
