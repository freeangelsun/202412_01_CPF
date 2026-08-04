# QA-12B R5 Cross Review Request

## Development GPT

Review every `QA_DIRECT_PATCH_CROSS_REVIEW_PENDING` file in `QA_PATCH_MANIFEST.csv`.
Confirm transaction behavior under Spring proxy, SQL portability, OpenAPI regeneration,
Orval type compatibility, frontend consumer behavior and regression.

## Codex

Independently inspect the original defect reproduction and patched code. Re-run module tests,
frontend generation/verify, 3-Vendor replay and Broker fault tests.

## QA

After user apply/commit/push, rebase on the new exact `origin/master` SHA and independently rerun.
No patched row may become QA pass before all three review stages complete.
