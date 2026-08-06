# Test and Evidence

- 기준 SHA: `2929163b3bb40159e22e1f57e79b6cd070abf7ad`
- QA 통과 주장: 없음

## 개발GPT 환경 실행 결과

| Gate | Command | Exit | Result |
|---|---|---:|---|
| Remote exact SHA | GitHub `master` commit 조회 | 0 | `2929163b3bb40159e22e1f57e79b6cd070abf7ad` |
| Core/Common Java contract | `javac --release 21 -Xlint:all -Werror` | 0 | Data Quality SPI와 reference implementation compile PASS |
| ADM changed-source synthetic compile | `bash run_synthetic_javac.sh` | 0 | Spring/Jackson/API signature compile PASS |
| Python Gate syntax | `python -m py_compile ...` | 0 | PASS |
| Integration Closure contract | `python cpf-tools/verification/verify_integration_closure_contract.py --root <overlay>` | 0 | 8 operation IDs, server approval only, actual route consumer PASS |
| Starter Catalog/BOM | `python cpf-tools/verification/verify_starter_catalog.py --root <overlay> --overlay-only` | 0 | 39 modules = 6 public + 33 internal; no hard-coded count PASS |
| Generated TypeScript client | `tsc --noEmit --target ES2022 --module ES2022 --lib ES2022,DOM ...` | 0 | PASS |
| Canonical ledger integrity | bundled canonical verifier | 0 | 47,745 exact IDs, 4 Parts, 32 request IDs, duplicate/orphan/hash mismatch 0 |
| Overlay package validation | `python validate_s10_package.py` | 0 | JSON/CSV/YAML/path/role-column/secret checks PASS |
| ZIP extraction and hashes | final package verifier | 0 | all entries extracted and SHA-256 matched |

## 실환경에서만 수행 가능한 미검증

- FDEV-004: Java 25 / Gradle 9.1 clean snapshot full build, test, publication
- FDEV-005: Oracle/PostgreSQL/MariaDB real install/upgrade/rollback/runtime query
- FDEV-006: Broker, split-WAS, multi-process, process-kill, UNKNOWN/reconcile
- FDEV-017: Chromium/Firefox/WebKit Playwright matrix

정확한 Preflight, 명령, 성공·실패 기준과 Evidence 경로는 `cpf-tools/verification/final-dev/*.ps1` 및 `evidence/templates`에 포함했다.
