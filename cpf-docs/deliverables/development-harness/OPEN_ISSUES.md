# CPF OPEN ISSUES — Development Harness Current

## Product Conformance OPEN — 11건
- `HARN-76022FDD9FB4` — **CONFIG_PROFILE_SET** — `cpf-education/src/main/resources` — 필수 profile 누락: test
- `HARN-6F8FFF3EB4CE` — **CONFIG_PROFILE_SET** — `cpf-admin/src/main/resources` — 필수 profile 누락: test
- `HARN-0C1B84016176` — **CONFIG_PROFILE_SET** — `cpf-backoffice-web/src/main/resources` — 필수 profile 누락: dev,stg,test,prod
- `HARN-3F7FE74DC9CE` — **CONFIG_PROFILE_SET** — `cpf-batch/control-plane/src/main/resources` — 필수 profile 누락: local,stg
- `HARN-DCAD325A91E3` — **CONFIG_PROFILE_SET** — `cpf-batch/worker/src/main/resources` — 필수 profile 누락: local,stg
- `HARN-6BB833921EF5` — **CONFIG_PROFILE_SET** — `cpf-batch/scheduler/src/main/resources` — 필수 profile 누락: local,stg
- `HARN-DA839DACE764` — **CONFIG_PROFILE_SET** — `cpf-batch/center-cut/src/main/resources` — 필수 profile 누락: local,stg
- `HARN-4D5899DC9211` — **CONFIG_PROFILE_SET** — `cpf-batch/agent/src/main/resources` — 필수 profile 누락: local,stg
- `HARN-2EDF146119AE` — **CONFIG_PROFILE_SET** — `cpf-tools/runtime/cpf-local-batch-runtime/src/main/resources` — 필수 profile 누락: dev,stg,test,prod
- `HARN-BD4F408AAA33` — **CONFIG_PROFILE_SET** — `cpf-tools/runtime/cpf-local-runtime/src/main/resources` — 필수 profile 누락: dev,stg,test,prod
- `HARN-10CD5AF82B99` — **CONTROL_CHAR** — `README.md` — NUL/BACKSPACE 제어문자 검출

## Mandatory Physical / Independent Verification
1. Java25 Root Build/Test/Publication/SBOM.
2. Fresh VS Code Java25/Gradle Import Error=0 / Warning=0.
3. Oracle/PostgreSQL/MariaDB DB3 Physical Full Lifecycle.
4. Windows/Linux Unified CLI 실제 lifecycle/UTF-8/path/prerequisite negative.
5. Batch 5-role + Worker×2 kill/takeover/fencing/UNKNOWN/reconcile.
6. One-WAS transaction/logging durability + Runtime OpenAPI.
7. ADM/Backoffice Frontend/Browser E2E/a11y/error-state.
8. Performance live/load/soak.
9. Actual Open Git Fresh Binary/Source Release + Public CLI + leakage 0.
10. Same Source Full Runtime/Fresh Replay mandatory fail/skip/not-executed/unknown/drift 0.
11. Codex/Claude current exact-source Independent Review.
12. QA final acceptance.

Static/Contract PASS는 위 Physical Gate를 대체하지 않는다.
