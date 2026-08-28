# Documentation Harness Handover

## Current canonical Harness

- Version: **2.10.0**
- Path: `cpf-docs/governance/documentation-harness/`
- Source baseline: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260828_191735.zip`
- Source SHA-256: `34D692419F7701EBC58439B00F0A5111DBBE629BC8C25F46ED17DB875D4E3EA5`
- Git exact SHA: `UNAVAILABLE_IN_SUPPLIED_ZIP`

## What changed

2.10.0 preserves the useful 2.9.0 package/geometry/render rules and adds the missing reader-executable layer. A document no longer passes because an API name, comparison table, keyword, or geometry exists. Selection must lead to action; developer guidance must contain consumer/call path, working example/procedure, failure/UNKNOWN, recovery and verification; visual-density rules prevent flat-list, table-wall, code-stack and centered-hero overcrowding.

## Verified state

- Harness self validation PASS
- 76 false-green/negative fixtures PASS
- Latest source alignment PASS
- Fresh replay byte-for-byte PASS
- Existing artifacts correctly rejected with 20 readability/actionability findings

## Next phase

Do **not** rewrite documents from scratch. Patch the current good documents/visuals against Harness 2.10.0, starting with README and Framework Developer Guide, then Batch Developer Guide and the remaining artifact set. Every user finding must update both the artifact and the Harness regression gate when a new class of defect is discovered.

Windows PowerShell runtime remains external-environment `미검증`; do not record it as PASS until executed on Windows.
