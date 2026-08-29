# VS Code Buildship/JDT Classpath Rework Evidence

- Source Identity: `5bac3ad5d773fbd8db1cc0e4f23dcd8e7c9d0813b44798aecd82d2f86089236b` / 8,351 files
- Trigger: user Fresh VS Code diagnostics still reported missing required libraries under `build/classes/java/main` after the prior task-only repair.
- Root cause: source-empty Java projects are `compileJava=NO-SOURCE`; Fresh Gradle Import returns a Buildship/JDT project model referencing the canonical compile output before any repair task runs.
- Fix: discovery-driven configuration-time materialization for source-empty Java projects only; no module hardcode, fake Java class, profile ownership change, or dependency workaround.
- Fail-closed gate: `cpfVerifyIdeClasspathModel` executes before explicit `cpfPrepareIdeClasspath`; post-build model gate repeats after clean/build.
- EntryPoint side effect: scanner-regenerated inventory = 978 entries; tooling inventory = 832 entries; duplicate=0; dead=0.
- Exact-source regression: Verification 104 PASS; Testing Tools 400 PASS / 23 SKIP; DB 142 PASS / 2 SKIP + DB Verification 87 PASS; Runtime 73 PASS / 2 SKIP; Security 8 PASS; Release 62 PASS; Generator 48 PASS / 10 SKIP / 6 subtests; Docker 8 PASS. FAIL=0 in executed suites.
- Physical status: **VERIFICATION_PENDING** until Windows Java25 Fresh Gradle Import produces entire Domain/Module VS Code Problems Error=0 / Warning=0.
