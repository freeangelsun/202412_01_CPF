# Open Git Release Repair — DevGPT Current Evidence

## Source Identity

- Input ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260829_211247(1).zip`
- Input ZIP SHA-256: `E6E343947AB4D829996107833AD20CD056D35BAC340013F58E0B2068C9694B30`
- Input ZIP file count: `12468`
- Git SHA: supplied ZIP에서 `.git` provenance를 신뢰 근거로 사용하지 않음

## User Actual Failure

- Command path: `cpf release open-git build --profile binary`
- Result: FAIL
- Stage: `05/14 Framework Binary Publication`
- Direct failure: `:nxt3LayoutGate` → Python exit 2
- Gradle execution before failure: 861 actionable tasks, Fresh POM generation + Maven publication staging reached `CPF_PUBLIC_ARTIFACT_STAGING=...\cpf-release\work\binary-repository-raw`
- Commit/Push: NOT_EXECUTED
- Failed `cpf-release.zip`: only logs/reports/raw staging; final `open-git/` and `binary-repository/` absent, therefore not a publishable Release.

## Root Causes / Fix

1. NXT3 default Garbage Ledger miswired to `CURRENT_DEVELOPMENT_STATUS.csv`; changed to `CURRENT_GARBAGE_DECISIONS.csv` and current Repository Hygiene scope includes status + garbage + delete authorities.
2. Source-empty Java aggregate/profile projects used disposable `build/classes/java/main`; `clean` removed it and NO-SOURCE compile did not recreate it. Stable output is now discovery-driven under Gradle User Home only for source-empty Java projects.
3. Fresh raw Maven publication used native/private coordinates. Public-release-only Maven projection now maps publications and CPF POM dependencies to Final Artifact Catalog Public coordinates; duplicate starter/runtime aliases are explicit; native non-public build identity remains unchanged.
4. `cpf-batch-runtime` had no Maven Publication despite PUBLIC_RUNTIME contract; real publication added.
5. Public BOM contained stale `cpf-batch-contract`; it now derives all Public Java/Runtime coordinates from Final Artifact Catalog.

## Executed Verification in DevGPT Environment

- `python -m pytest ...` relevant IDE/NXT3/OpenGit/Public Repository/Public Consumer suites: **57 passed**.
- `python cpf-tools/verification/nxt3/verify_nxt3_layout.py --root .`: **PASS, 87 pass / 0 fail**.
- `python -m py_compile ...`: **PASS**.
- Failed user raw Maven staging inspection: `48 POM / 111 JAR / 96 maven-metadata.xml`; confirms actual Fresh Maven publication occurred before NXT3 task finalized the Gradle invocation.
- Static native-publication coverage against failed raw repository: `UNMAPPED_INTERNAL_PUBLICATION=0` for public-owner internal group publications after applying the Canonical project/public mapping model.

## Environment / Verification Pending

- DevGPT container Java: OpenJDK 21.0.11. CPF requires Java25 for completion.
- Gradle Wrapper distribution `gradle-9.1.0-bin.zip` was not cached; network access to `services.gradle.org` unavailable (`UnknownHostException`). Therefore actual Gradle configuration/publication was not falsely marked PASS.
- Required user-side physical replay: Java25 Fresh Release 14/14, Fresh VS Code/Buildship `Error=0 / Warning=0`, Public Binary verifier + isolated consumer, Windows/Linux Generator classifier, Fresh Open Git clone Build/Test/Runtime, leakage 0.

Overall: `SOURCE_FIXED / VERIFICATION_PENDING`.

## Final Overlay Packaging Gate

- Targeted/Open Git/Public Maven/VS Code regression suite: `57 passed`.
- Actual current NXT3 command: `python cpf-tools/verification/nxt3/cpf_nxt3_layout_gate.py --root .` → `PASS 87/87`.
- Package excludes `.pytest_cache`, `__pycache__`, compiled Python bytecode, `cpf-release/`, local Gradle build output, and the failed external `cpf-release.zip`.
- Package-specific `CHANGE_MANIFEST.csv`, `DELETE_MANIFEST.csv`, `PACKAGE_MANIFEST.sha256` are kept under this current Evidence directory; no new repository-root management file is introduced.
- Overall remains `SOURCE_FIXED / VERIFICATION_PENDING` until the user's Java25/Windows physical replay closes the Runtime/IDE/Public Consumer/Open Git gates.
