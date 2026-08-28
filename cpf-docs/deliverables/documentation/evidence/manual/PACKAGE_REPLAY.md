# Package Replay — Harness 2.9.0

- Pre-final candidate archive: `CPF_DOCS_V29_CANDIDATE.zip`
- Candidate SHA-256: `2648960028EEB9B40066203F47944316522B55C1A711BD0DB0296A81794408C3`
- Candidate entries: **215**
- ZIP NFC duplicate check: **PASS**
- ZIP traversal check: **PASS**
- Maximum Root-relative path length: **101** characters
- Fresh extraction into an empty directory: **PASS**
- Fresh-extracted `verify_documentation_delivery.py`: **PASS**
- Harness 2.9.0 / 64 negative fixtures / README / DOCX 11 / Reader 12 / Visual 9 / Final Acceptance / Manifest / SHA verification: **PASS**

After this evidence is recorded, Manifest/SHA metadata is regenerated and the final archive is built and fresh-replayed once more. The final archive SHA-256 is intentionally recorded in the delivery response rather than embedded inside the archive, which would create a self-referential hash.

**PACKAGE_REPLAY_PASS = PASS**
