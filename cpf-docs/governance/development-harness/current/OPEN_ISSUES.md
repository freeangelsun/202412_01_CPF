# CPF OPEN ISSUES — Development Harness Current

## Product Conformance 미구현 — 11건
Harness가 현재 Source에서 탐지한 항목이며 `CURRENT_WORK_ITEM_REGISTRY.csv`에 정식 Work Item으로 승계했다. 구현·검증 전에는 완료가 아니다.

- `HARN-76022FDD9FB4` — CONFIG_PROFILE_SET —  —
- `HARN-6F8FFF3EB4CE` — CONFIG_PROFILE_SET —  —
- `HARN-0C1B84016176` — CONFIG_PROFILE_SET —  —
- `HARN-3F7FE74DC9CE` — CONFIG_PROFILE_SET —  —
- `HARN-DCAD325A91E3` — CONFIG_PROFILE_SET —  —
- `HARN-6BB833921EF5` — CONFIG_PROFILE_SET —  —
- `HARN-DA839DACE764` — CONFIG_PROFILE_SET —  —
- `HARN-4D5899DC9211` — CONFIG_PROFILE_SET —  —
- `HARN-2EDF146119AE` — CONFIG_PROFILE_SET —  —
- `HARN-BD4F408AAA33` — CONFIG_PROFILE_SET —  —
- `HARN-10CD5AF82B99` — CONTROL_CHAR —  —

## Mandatory Physical / Independent Verification 미완료
1. Java25 Root Build/Test/Publication/SBOM.
2. Fresh VS Code Java25/Gradle Import Error=0 / Warning=0.
3. Oracle/PostgreSQL/MariaDB actual DB3 Physical Full Lifecycle.
4. Windows/Linux Unified CLI actual lifecycle, UTF-8/path/prerequisite negative.
5. Batch 5-role + Worker×2 kill/takeover/fencing/UNKNOWN/reconcile.
6. One-WAS transaction/logging durability + Runtime OpenAPI.
7. ADM/Backoffice Frontend/Browser E2E/a11y/error-state.
8. Performance live/load/soak.
9. Actual Open Git Fresh Binary/Source Release + Public CLI lifecycle + leakage 0.
10. Full Runtime/Fresh Replay에서 mandatory skip/not-executed/unknown/fail/drift 0.
11. Codex/Claude current exact-source independent verification 및 새 Evidence.
12. QA final acceptance.

Source/Contract/Static PASS로 위 항목을 대체하지 않는다. 환경 부족 시 범위를 낮추지 않고 `BLOCKED_EXTERNAL/NOT_EXECUTED`와 최고강도 재실행 명령을 남긴다.
