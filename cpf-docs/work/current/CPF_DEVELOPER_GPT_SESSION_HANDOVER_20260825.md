# CPF Developer GPT Handover — 2026-08-25 Runtime Pending

## Current Source

- Baseline: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260825_121103.zip`
- Baseline ZIP SHA-256: `d2e89aba1841a4387a473610db905415f8565fcf09d06a56a8afa3a1b33a3a48`
- Current Product Source SHA-256: `81e35eeb7b528bd3f8812d9bc66f927eeed06279439690f561e94fc612a41653`
- Canonical Requirement: 208

## 개발 완료

Runtime 로그의 34 FAIL과 VSCode Problem에서 수집한 Root Cause를 Source/Harness/정본에 반영했다. Customer Shared Library Generator, Windows 200자 경로 Gate, Docker prerequisite auto-start/readiness/test-owned cleanup도 상위 정본 Requirement로 추가했다.

## 3차 Full Runtime(결함 탐색 전용) 이후 개발 라운드 — 2026-08-25

`CPF_REQUIRED_FULL_RUNTIME_20260825_213806.log` (132 Stage: PASS 108 / FAIL 15 / NOT_EXECUTED 7 / SKIP_ENV 2)는
실행 중 Source를 계속 수정했으므로 **Final Evidence가 아니라 결함 탐색 결과로만** 사용한다. 수집한 Root Cause를
아래와 같이 수정하고 각각 Targeted 검증까지 마쳤다.

### 수정 및 검증 완료

| 대상 Stage / 항목 | Root Cause | 수정 | Targeted 검증 |
|---|---|---|---|
| `[06] NXT3_22` | `DELETE_MANIFEST.csv` 11건이 `GARBAGE_SWEEP_DECISIONS.csv`에 미등재 | 원장 보완 등록 | `run_nxt3_final_all.py` 23/23 PASS (`failed=0 unverified=0`) |
| Data Safety Gate | 개발 Gate가 산출물 영역(`cpf-docs/guides/**`) 문구를 역참조 | Guide 의존 검사 제거, `CPF_ADMIN_DATA_SAFETY_GUIDE.md` 회수 | Source/Migration/Rollback/Query Contract 정본만으로 통과 재확인 |
| `[98] ADM_FRONTEND_VERIFY` | ADM Frontend Sandbox가 `platform-operations/observability` Source 미복사 | BFF Session과 동일 패턴으로 복사 대상 추가 | 다음 Full Runtime에서 재검증 |
| `[107] GENERATOR_LIFECYCLE` | `cpf-platform-bom`이 별도 Included Build라 `cpfPublishToIsolatedLocal` subprojects 집계에 구조적으로 미포함 | Staging과 동일하게 `gradle.includedBuild(...)`로 Local 게시 명시 연결 | Root Build 성공 |
| `[122]/[123]` Oracle DB3 `SKIP_ENV` | `docker compose up` 경로에서 Oracle 이미지 내장 HEALTHCHECK가 깨진 bare `healthcheck.sh`(exit 127)로 대체됨(실측) | Docker Health 대신 이미지 공식 준비 완료 로그 마커 확인으로 전환 | 컨테이너 로그에서 마커 확인 |
| `[133] BATCH_TWO_WORKER_CRASH_UNKNOWN` | 선행 Stage가 Kafka를 정지시킨 뒤 이 Stage가 Kafka를 재기동하지 않음(자원 생명주기 배선 누락) | 자체 기동/추적/자체 소유분만 정지 | 다음 Full Runtime에서 재검증 |
| `[140] LOCAL_ONE_WAS_START` | ADM/Backoffice가 동일 Bean 이름(`cpfOperationErrorContractCustomizer`)을 등록해 통합 Local WAS ApplicationContext 기동 실패 **(신규 실질 결함)** | 각 Module 전용 Bean 이름 명시 | `:apps:admin:compileJava` `:apps:backoffice:compileJava` BUILD SUCCESSFUL |
| `[134] GATEWAY_BATCH_RUNTIME` | DB Module fallback Secret Key(`CPF_DB_APP_PASSWORD` 등) provisioning 경로 부재 | `ensure-cpf-runtime-secrets.ps1` 신규(멱등, 값 미출력) + 최초 설치/Full Runtime 배선 | `core.runtime.password` 해석 성공 확인 |
| `[95] DEPLOYMENT_FULL_DISTRIBUTED_ARTIFACT_PACK` | (1) Batch `projectPath`→물리경로 오매핑 (2) EDU/GWY/ADM artifactName 불일치 (3) ADM은 `.war` 패키징인데 글롭이 `.jar`만 탐색 | 4개 env manifest에 `projectDir`/`artifactName` 보강, `.war` 탐색 및 `-plain.war` 제외 | 9건 누락 → 전 항목 해소, 4개 env 전부 재검증 |
| DB Lifecycle `currentVersion` | `run-db-vendor-lifecycle.ps1`이 선택 Module/logical DB 범위를 무시하고 Vendor 전역 최대 Version 선택 | 3 Vendor 공통 단일 범위 규칙 적용(MariaDB는 inline `USE` 라우팅, 그 외는 pack 기준) — V139 특례 없음 | `test_vendor_lifecycle_orchestrator.py` 9 passed / 9 subtests |
| MariaDB V138/R138 라우팅 누락 | `mariadb-historical-migration-routing.json` 미등록(위 수정 중 신규 발견) | 라우팅 등록 | 동일 테스트로 검증 |
| Windows Path `FORBIDDEN_VERSIONED_DIR` 64건 | 제품 정본 Logging 계약의 일일 Roll(`logs/**/transactions/<YYYYMMDD>/`)과 Gate 규칙이 정면 충돌 | Runtime 일일 Roll만 세그먼트 단위로 좁게 예외(손으로 만든 Version 폴더는 계속 FAIL) | 회귀 테스트 3종 추가, 11 passed. 위반 240 → 176 |
| Windows Path 길이 위반 | 장문 Evidence 경로 | 16개 폴더를 짧은 `CX-PATH-30~45-*` Alias로 Byte 동일성 검증 복사 후 `DELETE_MANIFEST.csv` 91행 등록 | 원본 삭제만 사용자 승인 대기 |
| Migration Immutable Baseline | git history squash로 `baselineCommit`이 소멸 | 사용자 승인 하에 현재 시점으로 재anchor (Pre-release 간주) | JSON 정합 완료 |
| `[126] QA39_RUNTIME_FAULT_SMOKE` | **Toxiproxy 2.9+ 가 브라우저형 User-Agent를 `403 User agent not allowed`로 거부**하는데 PowerShell `Invoke-WebRequest` 기본 UA가 `Mozilla/5.0 ... PowerShell/7.x`. 자원 경합이 아니라 100% 결정적 실패. 또한 `Wait-Http`의 `catch { }`가 403을 삼키고 "Timeout"으로 오보고해 진단을 방해했다 | Toxiproxy 호출 4개 전부 non-browser UA 명시(`start-qa39-runtime.ps1`, `verify-qa39-runtime.ps1`, `cpf-tooling.ps1`, `run-qa39-runtime-fault-smoke.ps1`) + 실패 원인 보존 | **QA39 단독 재실행 exit 0.** Toxiproxy `status=200`, 장애주입 5단계 전부 PASS (baseline / proxy-disabled / proxy-recovered / latency 305ms / final-recovery) |
| `[91] GRADLE_FULL_BUILD_QUALITY` | `CpfXlsxTabularAdapterTest`가 `Map.of`(JVM마다 반복순서 무작위) + `containsExactlyEntriesOf`(순서 요구) 조합 → 본질적 flaky. 제품은 `LinkedHashMap`으로 Schema 순서를 올바르게 보장 | 계약 약화 없이 기대값만 결정적(`LinkedHashMap`, Schema 순서)으로 수정 | `:internal:file:tabular:poi:test` BUILD SUCCESSFUL |
| Python 검증 Harness **Locale 의존 결함** (신규) | 한국어 Windows 기본 로케일이 cp949인데, 검증 Test가 제품 Source를 UTF-8로 읽고 **인코딩 지정 없이 되쓰기**(`write_text`)해 한글이 깨지고 다음 UTF-8 읽기에서 `UnicodeDecodeError` 발생. 또 `subprocess`/`Popen`이 한글 출력을 로케일로 디코딩 | Source를 왕복시키는 `read_text`/`write_text` 호출과 Open Git `subprocess`/`Popen`에만 `encoding="utf-8"` 명시(전역 일괄 변경은 위험 대비 편익이 없어 되돌리고 정밀 적용) | 실패 8건 → **0건**. `test_batch_agent_fail_closed_gate` 7/7, `test_cpf_open_git` 17/17, 대상 3개 파일 17/17 |

### Tier-1 인수 기준 실측 결과 (Root + MBR + EXS 3종)

Codex `CODEX_REVALIDATION_REQUEST.md` T1-01/T1-03이 요구하는 3종 Build를 모두 실제 실행했다.

```
# 1) Root (Generated Domain 포함)
gradlew clean cpfBuild qualityGate cpfTest qa34IntegrationTest --continue
  -PcpfIncludeGeneratedDomains=true -PcpfDbVendor=mariadb
=> BUILD SUCCESSFUL / 595 actionable tasks / FAILED 0

# 2) Standalone MBR (cpf-member)
gradlew -PcpfProductCompositeRoot=<repo> -PcpfDbVendor=mariadb :batch:build
=> BUILD SUCCESSFUL / online+batch bootJar 생성

# 3) Standalone EXS (cpf-external)
gradlew -PcpfProductCompositeRoot=<repo> -PcpfDbVendor=mariadb build
=> BUILD SUCCESSFUL / 93 tasks / cpf-external-online JAR 생성
```

`-PcpfDbVendor`는 Generated Domain의 batch Sub-module이 fail-closed로 요구하므로 함께 지정해야 하며,
이를 표준 Full Runtime 파이프라인 `$gradleBase`에 반영했다. 새 Domain을 만들지 않았다 —
cpf-member/batch는 cpf-member 단일 Domain의 선택 Batch Module이다.

### Codex 잔여 Job 승계 현황

`CODEX_FINDING_CLOSURE.csv` 307건 기준 — CLOSED 201 / VERIFICATION_PENDING 94 / SOURCE_FIXED 10 / IN_PROGRESS 2.
이번 라운드에서 위 3종 Build 실측 근거로 총 14건을 CLOSED 처리했다 — Root Build 근거 5건(`CX-F-002/003/005/007/100`), Root+MBR+EXS 3종 근거 9건(`CX-F-006/008/009/010/011/012/014/015/019`). `CX-F-018`은 실제 Worker Runtime을 요구하므로 열어 두었다.

잔여 106건을 실제 차단 요인별로 분류하면 대부분 DB/Docker 실측, Runtime 실측이 선행 조건이다. 즉 대부분이 **Frozen Source Full Runtime에서만 닫을 수 있는**
항목이며, 정적으로 닫을 수 있는 것은 이번 라운드에 모두 처리했다.

주의: Toxiproxy User-Agent 결함과 XLSX flaky 테스트 결함은 Codex Ledger에 없는 **이번 세션 신규 발견**이므로
기존 Codex 행을 임의로 닫지 않고 위 표에 별도 기록했다.

## 사용자 판단이 필요한 잔여 항목

1. **Git write** — `V133/V136/checksums.sha256`의 `USE mbwDB;` 라우팅 수정은 Working Tree에 반영되어 있으나,
   `verify_canonical_vendor_render.py`의 immutable history 계약은 commit 상태를 요구한다. Commit 없이는
   이 Gate만 통과 불가하며, Git write는 승인 없이 수행하지 않는다.
2. **보호 경로 삭제** — Windows 200자 초과 원본 Evidence 176건. Alias 사본은 생성/검증 완료이며
   `DELETE_MANIFEST.csv`에 `user_approved=false`로 등재되어 있다. 삭제는 승인 후 실행한다.
3. **실제 Secret 값** — 자동 provisioning 구조는 구현했다. 최초 설치 시 사용자만 제공 가능한
   관리자 공통 비밀번호 자체만 외부 입력으로 남는다.

## 다음 필수 단계

Windows Java25 + PowerShell 7 + Docker 환경에서 Full Runtime을 실행한다. Harness가 필요한 컨테이너를 자동 시작하고 readiness를 확인하며 검증기가 올린 컨테이너는 종료한다. 결과가 하나라도 FAIL/SKIP_ENV/NOT_EXECUTED/UNVERIFIED이면 QA로 넘기지 않고 같은 Requirement를 다시 개발한다.

절차: 저비용 Gate 전부 재검증 → Source Identity/정본/Evidence 현행화 → **Source Freeze** →
Full Runtime 처음부터 끝까지 실행(실행 중 Source 수정 금지) → 이 마지막 Frozen Source 실행만 Final Evidence로 인정.

## 성공 조건

`FAIL=0 / SKIP_ENV=0 / NOT_EXECUTED=0 / UNVERIFIED=0` + Java25 Root Build + DB3 3사 lifecycle + 2-worker kill/recovery + Browser E2E + Fresh Replay PASS.

## Overlay 전달 규칙

- Baseline 대비 실제 `ADD/MODIFY` 파일만 ZIP payload에 포함한다.
- unchanged 파일은 ZIP에 포함하지 않는다.
- 삭제 대상은 `DELETE_MANIFEST.csv`로만 적용하며 자동 삭제하지 않는다.
- 현재 Source의 Java25/Docker Full Runtime이 실제 PASS하기 전에는 `Runtime Pending` 상태를 유지한다.
- Currentization time: `2026-08-25T23:40:00+09:00`
