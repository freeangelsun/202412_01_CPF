# Package Replay — Documentation Harness 2.12.0

- Basis Source: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260828_191735.zip`
- Basis Source SHA-256: `34D692419F7701EBC58439B00F0A5111DBBE629BC8C25F46ED17DB875D4E3EA5`
- Exact Git SHA: `UNAVAILABLE_IN_SUPPLIED_ZIP` (supplied ZIP has no `.git`; no historical SHA substituted)
- Replay mode: exact `DELETE_MANIFEST.txt` cleanup followed by Root-relative documentation overlay
- Managed overlay files: **237**
- Overlay archive files including self metadata: **239**
- Fresh replay into an independent copy of the supplied Source: **PASS**
- Fresh-replayed `verify_documentation_delivery.py`: **PASS**
- Harness: **2.12.0 PASS**
- Negative fixtures: **100/100 PASS**
- README / DOCX 11 / PDF 11 / Reader Task 12 / Visual 9 / Final Acceptance / Manifest / SHA verification: **PASS**
- Product non-documentation Source drift: **0 changed / 0 missing / 0 added**

The final ZIP is additionally extracted and replayed again after Manifest/SHA locking. Its SHA-256 is reported outside the archive to avoid a self-referential package hash.

**PACKAGE_REPLAY_PASS = PASS**
