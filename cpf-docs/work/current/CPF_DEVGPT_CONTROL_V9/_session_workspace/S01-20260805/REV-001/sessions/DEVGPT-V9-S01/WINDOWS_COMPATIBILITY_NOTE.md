# Windows-compatible checkpoint package

- Original campaign directory: `CPF-V9-INITIAL-FULL-REVIEW-20260805-2101`
- Package campaign directory: `S01-20260805`
- Reason: the original archive contained paths up to 327 characters. With a Windows repository destination path, PowerShell `Expand-Archive` can exceed the effective path limit.
- Product overlay paths are unchanged.
- Evidence source trees are preserved in `EVIDENCE_BUNDLE.zip`.
- Integration proposal source/diff trees are preserved in each request folder as `PROPOSED_OVERLAY.zip` and `DIFFS.zip`.
- Nested bundles are evidence/transfer artifacts and are not automatically expanded into product paths.
