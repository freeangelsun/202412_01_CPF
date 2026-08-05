# QA Rework Request

- Baseline: `faedf43a7baffdad456bf40f8e46d622db9cfc76`
- Scope: DEVGPT-6B
- QA final completion is not requested for cross-session or target-runtime rows.
- Review the 6B-owned implementation, 63 unique product PASS markers, 65 mapped Gate rows and all 1,109/1,698 row-level decisions.
- Keep DEVGPT-6A, DEVGPT-6D, DEVGPT-BATCH, DEVGPT-6E and DEVGPT-6F rows open until their implementation and target-runtime Evidence are merged.
- Do not convert Java 21 substitute validation into Java 25 PASS.
