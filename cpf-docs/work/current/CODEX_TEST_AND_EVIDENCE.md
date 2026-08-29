# CPF Codex Test and Evidence — Current Continuation

## Current Source Identity

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Start HEAD / origin/master: `5f2289d4171d5a42ce579d7c98ebf3e1bef19828`
- Start Working Tree: clean
- Current Product Source Identity: `fddfbcabe6f7bab187bd957178b5e9a06791886625c29e7362c4e0a4e38aea91`
- Source files / bytes: `8,362 / 45,383,854`
- Environment: Windows x64, Python 3.13.14, pytest 9.1.1, Java 25.0.3
- Git index/history writes: `0`

## P0-1 — Open Git/Public pytest and Windows Temp Isolation

Status: `CLOSED`

Initial command used a new non-reparse-point base directory outside the repository:

```text
python -m pytest -x -vv -p no:cacheprovider --basetemp <isolated> cpf-tools\release\open-git\tests cpf-tools\release\public\tests
```

Initial result:

```text
21 passed / 1 failed
exitCode=1
failingTest=cpf-tools/release/open-git/tests/test_cpf_open_git.py::test_cross_platform_cli_build_requires_java25
assertion=non-Java25 compiler must fail closed
expected=mocked javac 21.0.11 rejected before compilation
actual=real Windows javac.exe 25.0.3 executed and build succeeded
pytestCleanupException=0
WinError5=0
```

Root Cause:

- Product `build_cross_platform_cli` correctly selects and validates Java 25.
- Test fault injection matched `str(cmd[0]).endswith("javac")` and therefore missed Windows `javac.exe`.
- The negative branch was not injected; a real Java 25 compiler ran, producing the assertion failure.
- The same pattern also made adjacent Windows tests false-green by bypassing their intended compile adaptation.

Closure:

- Normalized command matching across POSIX `javac` and Windows `javac.exe` in both Open Git and independent verification tests.
- Strengthened the Java 21 negative so any command after the version probe fails immediately.
- Repository search after the fix found `endswith("javac")` / `endswith('javac')` occurrences: `0`.
- Product build owner remained unchanged because its fail-closed Java 25 behavior was correct.

Regression:

```text
targeted Open Git + independent CLI contract = 6 passed / 0 failed / 0 skipped
Open Git/Public full suite                 = 48 passed / 0 failed / 0 skipped
source-state + Open Git/Public combined    = 59 passed / 0 failed / 0 skipped
cleanup exception                          = 0
repository product-source drift from test  = 0
```

## Source Identity False-Drift Closure

Status: `CLOSED`

The first recomputation reported `c6f18d... / 8,415` because the Git-independent scanner treated ignored generated files as product source:

- canonical Batch `bin/` compiled `*.class`: 48 files
- repository-root runtime log files: 2 files
- generated `.github/modernize/` workspace: 3 files

The scanner now excludes those generated artifacts while retaining canonical Batch `run/stop` Shells and ADM frontend `features/logs` source. Focused and combined regression passed, and recomputation is stable at the Current Product Source Identity above.

## P0-2 — Unified CLI / Gradle / Generated Domain Mutation

Status: parent work package `IN_PROGRESS`; the following findings are individually `CLOSED` on the Current Product Source Identity.

- Active generator/verification consumers now enter through `cpf` (`cpf.cmd` on Windows) instead of the retired `cpf.bat` or the low-level Python engine. Java-to-Python child processes force UTF-8, and the unified CLI exposes the internal Domain and DB render routes without adding them to the Public command surface.
- The CLI builder requires exact Java 25 and compiles with `--release 25 -Xlint:all -Werror`. Rebuilt JAR SHA-256: `57b253feb743edb389903f952fe3ca5cbaf0b93787130f5d317bcd063c1f8ecc`; embedded Source Identity matches this document.
- Public/Internal BOM publication defaults now use the product-owned isolated repository under generated evidence. A focused BOM publication produced all 15 expected files there. The pre-fix diagnostic run wrote BOM artifacts to the user Maven cache; Codex did not delete that out-of-workspace residue without user approval.
- General Generated Domain `--apply` deletion remains fail-closed. Only the exact product-owned `lifecycle-<domain>/cpf-<domain>` disposable sandbox, exact colocated definition, unchanged ownership manifest, and explicit lifecycle confirmation can apply deletion. Unknown/user files fail before any partial deletion; exact Gradle artifact roots are discarded only in that disposable scope.

Physical mutation result:

```text
codexscratch dry-run                 = PASS
generate + first clean/test/assemble = PASS
Delete Manifest dry-run              = PASS
approved disposable source removal   = PASS
restore + second clean/test/assemble  = PASS
round-trip hash differenceCount      = 0
```

Impact / Problems regression:

```text
Java source syntax                    = 3,066 files / 0 errors
changed Python compile                = 19 files / 0 errors
changed PowerShell parse              = 10 files / 0 errors
VS Code known-diagnostic regression   = 5/5 PASS
Java25 CLI -Xlint:all -Werror         = PASS
unified CLI + cross-platform tests    = 10/10 PASS
cpfVerifyFast --warning-mode all      = 25/25 tasks PASS / compiler-deprecation warning 0
```

Evidence:

- `cpf-docs/work/evidence/generated/domain-generator/reports/generated-domain-lifecycle/codexscratch/generated-domain-lifecycle.sanitized.json`
- `C:\dev\projects\jck\_cpf_codex_runtime\generator-cross-pass.json`
- `C:\dev\projects\jck\_cpf_codex_runtime\source-state\stage4-eol\summary.json`

The remaining parent scope—zero/one/many Domain root projections, Backoffice-absent physical mutation, Open Git stale-reference mutation, and Linux physical CLI—is not promoted by this closure.

## Overall Status

- This document closes V2 P0-1, the identity false-drift prerequisite, and the individually listed P0-2 findings.
- Actual Open Git Fresh Release lifecycle, CLI/Gradle/Domain mutation, transaction logging runtime, DB3, Batch fault/recovery, Browser, Performance, Full Runtime, and Fresh Replay remain `IN_PROGRESS` or `NOT_EXECUTED` until their own physical evidence exists.
