# CPF DEV20 FINAL TEST AND EVIDENCE

기준: DEV19 99.82% worked checkpoint를 DEV20에서 연속 검증·보정한 로컬 Source. Git commit/push/branch/tag는 수행하지 않았다.

## 실행 PASS

| 영역 | 결과 |
|---|---|
| Current final static verifier | PASS — deleteManifest=429, EDU online=20, batch=15, operationPairs=115, uniqueOperationIds=115 |
| Generator verification | PASS — 29 passed, 10 skipped, 6 subtests |
| DB tests | PASS — 82 + 75 = 157 passed |
| Public release/workspace tests | PASS — 28 passed |
| Runtime + Security | PASS — 73 passed, 2 skipped, 7 subtests |
| Verification + OpenAPI | PASS — 76 passed |
| Testing tools | PASS — 375 passed, 22 skipped, 2 subtests |
| Frontend API runtime harness | PASS — ADM + Backoffice, Canonical System6 protected, checks >= 38 |
| Frontend substitute validation | PASS — ADM TypeScript/Node/Chromium + Backoffice Web trust boundary |
| Controller permission | PASS — operations=425, mutations=196, warnings=0 |
| Backoffice boundary | PASS — backendOperations=96, webRoutes=96, dbLess=1, cpfJavaDependency=0, canonicalHeaders=6 |
| Batch execution fencing | PASS — sources=7, checks=22 |
| DB3 canonical render | PASS — tables=234, seeds=156, vendors=3, overrides=0 |
| Gradle logical tree | PASS — catalogModuleCount=64 |
| Public empty staging | PASS — 178 files, includeBackoffice=true |

## DEV20에서 발견·수정한 잠복 결함

1. `frontend-api-runtime`이 폐기된 BZA tsconfig/path와 Channel-based 6 Header를 요구하여 실행 불가/False Green 위험이 있던 문제를 현재 `cpf-backoffice-web` + Canonical System6 기준으로 현행화했다.
2. `verify-cpf-frontend-substitute-validation.py`가 `VITE_BZA_CHANNEL_BASE_URL`, `com.cpf.bzachannel`을 요구하던 문제를 `VITE_MBW_WEB_BASE_URL`, `com.cpf.backoffice.web` 계약으로 현행화했다.
3. Standard Header E2E/EDU raw guard에서 Canonical 6을 Channel Header로 취급한 잔재를 System6로 교정했다. Optional Channel context 자체는 제거하지 않았다.
4. `CPF_FINAL_TARGET_REQUIREMENTS.md`의 retired Backoffice root 표기 오타를 실제 retired `cpf-biz-admin/cpf-biz-channel/cpf-biz-frontend`로 교정했다.
5. ADM Transaction Group 검색 힌트를 `X-System-Code` 기준으로 currentize했다.

## 환경상 미검증

- Root Gradle configuration/build: 이 실행환경에서 Gradle wrapper가 `https://services.gradle.org/distributions/gradle-9.1.0-bin.zip`을 내려받으려 했으나 외부 네트워크 차단으로 `java.net.UnknownHostException: services.gradle.org` 발생. Source 실패로 판정하지 않음.
- 이 환경 Java는 21이므로 Java 25 최종 build/runtime 증명을 대신하지 않음.
- Oracle/PostgreSQL/MariaDB live lifecycle, Multi-WAS/process-kill, Browser→BFF→Gateway→Backoffice→Domain full E2E는 로컬/실환경 최종 통합검증 대상.

미실행 항목을 PASS로 기록하지 않았다.
