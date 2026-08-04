# OPEN ISSUES

1. **QA12A-ENV-001 — Sandbox fresh clone blocked**
   - Cause: DNS resolution failure.
   - Alternative used: GitHub MCP exact-SHA file retrieval.
   - Risk: local build/runtime gates remain unexecuted.

2. **QA12A-EVD-001 — Current exact-SHA requirement evidence not closed**
   - Source coverage ledger contains historical/stale baseline references and is not sufficient for individual QA closure.
   - This is a common finding and is not used as blanket failure evidence.

3. **QA12A-SC-001 — Scenario extraction incomplete**
   - Only scenario master part 1 is included in this checkpoint.
   - Remaining parts must be filtered by `linked_requirement_id`.
