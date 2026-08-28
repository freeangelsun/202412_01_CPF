# Harness 2.9.0 Recurrence Fix

User findings were promoted from artifact-only corrections into Harness rules and executable gates.

- PATCH_FIRST baseline preservation; no unexplained fresh rewrite.
- User/manual visual finding overrides automated false-green.
- README major-section breathing and 900/1200/1440 viewer checks.
- README wide table/header-wrap avoidance.
- Dark DOCX table header requires explicit >=4.5:1 text contrast; AUTO/black on navy fails.
- Low-density/orphan final pages fail.
- Visual safe-area/crop review at README viewer widths.
- Windows VS Code built-in Markdown Preview failure is an environment-specific runtime finding and may not be fabricated as PASS.
- Negative fixtures increased from 58 to **64**.

**Harness self-validation PASS / 64 fixtures PASS.**
