# Codex Revalidation Request — Documentation Harness 2.10.0

Perform an independent read-only revalidation of the current Harness. Do not weaken requirements and do not mark existing artifacts PASS.

Validate in order:

1. `validate_harness.py`
2. `validate_source_alignment.py`
3. `validate_quality_fixtures.py`
4. Inspect `readability-actionability.json`, Framework/Batch/README profiles and Final Acceptance evidence requirements.
5. Run `validate_readability_actionability.py` against the current source and confirm it rejects the known 20 current-artifact defects.
6. Confirm lock/package hashes and current-only policy.

Source identity: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260828_191735.zip` / SHA-256 `34D692419F7701EBC58439B00F0A5111DBBE629BC8C25F46ED17DB875D4E3EA5`.
Git exact SHA is unavailable in the supplied ZIP.
