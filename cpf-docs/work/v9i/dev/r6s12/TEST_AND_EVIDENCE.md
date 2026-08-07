# CPF R6S12 Test and Evidence

## Baseline

- instruction basis SHA: `28f823a18eca859cebdbceb382029f595cdf490c`
- QA product basis SHA: `e7cc9ada86c871214a20862779f2433bc46fea1b`
- master 확인: GitHub Connector, `06_11`
- local fresh clone: DNS `github.com` 해석 실패, Exit 128
- result commit: 생성하지 않음(사용자 적용·검증·Commit 대상)
- evidence source: basis SHA + Root Overlay path/hash manifest

## PASS

| Gate | Exit | 실제 결과 | Evidence |
|---|---:|---|---|
| R6 approval/source contract | 0 | Backend/API/SPI/Config/Frontend/DB layout contract PASS | `evidence/R6_APPROVAL_CONTRACT.txt` |
| R6 behavior/mutation | 0 | 18 source behavior checks, 9 regression mutations PASS | `evidence/R6_BEHAVIOR_MUTATION.txt` |
| DB3 runner contract | 0 | JSON stdin/no URL argv/environment clear/timeout/kill/redaction 8/8 PASS | `evidence/R6_DB3_RUNNER_CONTRACT.txt` |
| DB3 SQL parity | 0 | Oracle/PostgreSQL/MariaDB 3 vendor × 6 lifecycle roles PASS | `evidence/R6_SQL_PARITY.txt` |
| Overlay hygiene | 0 | empty/temp/long path/secret/trailing whitespace 0 | `evidence/R6_HYGIENE.txt` |
| Java quality compile | 0 | Core API/SPI + InMemory implementation Java 21 compile PASS | `evidence/JAVA_QUALITY_COMPILE.txt` |
| Java quality runtime harness | 0 | null violation, forged proof rejection, correction CAS, concurrent replay convergence, stale CAS rejection, operationId collision rejection PASS | `evidence/JAVA_QUALITY_RUNTIME.txt` |
| Frontend runtime harness | 0 | duplicate/precision/null, A→B→A key lifecycle PASS | `evidence/NODE_FRONTEND_RUNTIME.txt` |
| Node script syntax | 0 | modified `.mjs` syntax PASS | `evidence/NODE_SYNTAX.txt` |
| JSON/YAML parse | 0 | modified JSON/YAML parse PASS | `evidence/YAML_JSON_VALIDATION.txt` |

## NOT_EXECUTED / 미검증

| Required runtime | 상태 | 정확한 사유 | 재실행 조건/명령 |
|---|---|---|---|
| Java 25 / Gradle 9.1 full build/test/publication | NOT_EXECUTED | Java 21만 설치, Gradle/complete checkout 없음 | `pwsh -File cpf-tools/verification/final-dev/run-r6-release-gates.ps1 -ExpectedHead <RESULT_SHA>` |
| npm full verify | NOT_EXECUTED | Node 22.16.0이며 package 최소 22.18, complete frontend dependencies 없음 | 동일 runner 또는 workflow |
| Playwright/accessibility/responsive | NOT_EXECUTED | Browser/runtime/dependencies 없음 | runner `-RunBrowser` |
| Oracle/PostgreSQL/MariaDB live lifecycle/CAS | NOT_EXECUTED | Docker/DB/Secret/pwsh 없음 | runner `-RunDb3` + 9개 CPF_RUNTIME_* 환경변수 |
| Broker/multi-instance/split-WAS/process-kill | NOT_EXECUTED | Broker/process runtime/complete checkout 없음 | runner `-RunMultiprocess` |
| QA38/QA39/REV004 complete repository | NOT_EXECUTED | Root Overlay에는 정본 Catalog/BOM/generated artifacts 전체를 의도적으로 포함하지 않음 | Overlay 적용 후 Repository Root에서 실행 |
| Codex independent review | NOT_EXECUTED | Codex 검수 주체/결과가 이 세션에 없음 | `CODEX_REVIEW_REQUEST.md` 범위 실행 |

실행하지 않은 검증은 PASS로 기록하지 않았다. Exit 1인 QA38/QA39/REV004 Overlay 단독 실행은 제품 실패 판정이 아니라 complete checkout precondition 미충족으로 분류했다.
