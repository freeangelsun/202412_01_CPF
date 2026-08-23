# CPF Open Git Release — Test and Evidence

## 1. 기준

- Work Package: `WP-14 — Open Git Release Projection / Packaging / Developer DX`
- 개발 기준 ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260822_102905.zip`
- 기준 ZIP SHA-256: `3cfa4c25eff8381526f66f9865178e22bdff72f0c14cf40813ca943c77bd528d`
- 기준 Canonical Source Identity: `4572fd3659d076f230cbe2aa0284a5835a4f914e1f0a0cb4823b20c53b724886`
- 본 별도 작업본 Source Identity: `56764a8f39c38a1b76f0f967524fd40bfb3703798c40ae79b1ca066e12b2ad3a`
- 본 작업은 Codex가 사용하는 VS Code Working Tree에 적용하지 않고 별도 작업본에서 수행했다.

## 2. 구현 범위

이번 Overlay는 기존 Product/Gradle/Generator/Runtime 파일을 덮어쓰지 않는다. 신규 `cpf-tools/release/open-git/**`와 본 Work Package/Evidence 파일만 추가한다.

구현된 기능:

- `cpf-release/` 정확 경로 안전성 검사 및 전체 재생성 Lifecycle
- `cpf-release/open-git`, `binary-repository`, `reports`, `logs` 가시성 중심 IA
- Open Git Source Default-Deny Surface Policy
- Generated Domain / Backoffice / Backoffice Web / EDU / Developer Command 공개 Projection
- Core/Common/Admin/Gateway/Batch/Starter 등 Framework 내부 Source Tree 차단
- Maven-compatible Binary Repository 생성/검증 계층
- `sources.jar` / `javadoc.jar` Source disclosure Default-Deny
- Common/Public Starter 계열 source/javadoc 허용, Core/ADM/Gateway/Batch/Internal Runtime binary-only 정책
- Secret / Private Source / JAR-WAR Leakage 차단
- Open Git fresh clone + sync + staged diff 검증
- 자동 commit/push 금지, `READY_TO_COMMIT`까지만 수행
- Release 담당자 짧은 Canonical UX: `cpf open-git`, `cpf open-git check`, `cpf open-git status`
- Open Git 개발자 짧은 Canonical UX: `cpf bootstrap/build/test/verify/domain/status/stop/reset`
- 개발자 단일 Dispatcher + 기존 개별 Script 호환 Wrapper
- 장시간 명령 진행 단계/실시간 콘솔 + Timestamp Log + PASS/FAIL/ExitCode/시각/로그 경로/다음 행동
- `cpf bootstrap` 기본 Runtime Start/Health + `CPF LOCAL DEVELOPMENT READY`, `cpf reset` 명시 확인 Gate
- Codex 종료 후 기존 파일을 좁은 Anchor로 통합하는 idempotent `setup`

## 3. 수행 검증

### 3.1 신규 + 기존 Public Release 회귀

```text
python -m pytest -q \
  cpf-tools/release/open-git/tests/test_cpf_open_git.py \
  cpf-tools/release/public/tests/test_prepare_cpf_public_workspace.py \
  cpf-tools/release/public/tests/test_publish_cpf_public_repository.py \
  cpf-tools/release/public/tests/test_verify_cpf_public_binary_repository.py \
  cpf-tools/release/public/tests/test_verify_cpf_public_binary_consumer.py
```

결과: `28 passed`, fail 0.

신규 Open Git 단독 Test: `11 passed`, fail 0.

### 3.2 구문/정책

- `python -m py_compile cpf-tools/release/open-git/cpf_open_git.py` → PASS
- `bash -n cpf-tools/release/open-git/cpf-open-git.sh` → PASS
- `bash -n cpf-tools/release/open-git/templates/tools/verify-open-git-workspace.sh` → PASS
- 두 JSON Policy `python -m json.tool` → PASS
- PowerShell 실행 Runtime은 현재 Linux 환경에 `pwsh/powershell`이 없어 미실행. Script는 단순 Wrapper/Verifier이며 Windows 검증은 후속 Runtime Gate 대상이다.

### 3.3 실제 Open Git Source Projection

현재 전체 Source에서 신규 Surface Policy로 실제 staging 생성:

- Result: PASS
- Files: 439
- `cpf-member`: 존재
- `cpf-external`: 존재
- `cpf-backoffice`: 존재
- `cpf-backoffice-web`: 존재
- `cpf-education`: 존재
- `bin`, `tools`, MBR/EXS/MBW Domain Definition: 존재
- Forbidden Framework root leakage: 0
- Gradle Wrapper 이외 JAR/WAR: 0
- `build/`: 0
- `node_modules/`: 0
- `dist/`: 0

`cpf-backoffice`는 내부 Test Fixture까지 통째로 공개하지 않고 고객 개발 Main Source + Build/Domain/OpenAPI 계약만 공개한다. 실제 Fixture의 인증성 긴 문자열이 Secret Gate에 걸린 결과를 반영했으며 Gate를 약화하지 않았다.


### 3.4 Developer Command UX 실제 실행

신규 Open Git Projection에서 실제 Linux Shell 실행:

- `cpf help` → PASS, 짧은 Canonical command 목록/성공 기준 표시
- `cpf status` → PASS, Java/Git/Docker/CPF Version/Repository/Runtime/Latest Log 표시
- 환경변수 없는 `cpf build` → ExitCode `2`로 fail-fast, 실행 단계/원인/로그/다음 행동 표시
- `cpf reset` without `--confirm` → ExitCode `2`, destructive action 시작 전 거부
- 기존 `cpf-bootstrap/build/test/domain-*` Script는 Canonical `cpf` Dispatcher를 호출하는 호환 Wrapper로 교체
- 기존 Public Wrapper와 Bootstrap Engine 사이의 `--workspace`, `--timeout-seconds`, `--start-runtime`, unsupported `build/test` 호출 불일치를 Open Git Template 계층에서 제거

실패 출력 공통 항목:

```text
Step / Reason / Status / ExitCode / Started / Completed / Log / Next
```

### 3.5 `cpf open-git` 재실행/오류 UX 실제 검증

통합 Sandbox에 `cpf-release/open-git/stale-before-rebuild.txt`를 생성한 뒤 Release Build를 재실행했다.

- `01/14 Release Root 안전 확인`에서 이전 `cpf-release/` 전체 제거 → `STALE_REMOVED_PASS`
- `03/14 Artifact 공개 정책 확인`에서 현재 Baseline 계약 불일치 5건으로 의도된 fail-closed
- 실패 화면: Stage, 실제 Blocker, ExitCode 1, Log 경로, 다음 행동, `Commit/Push NOT_EXECUTED` 표시
- 실패 후 과거 READY package는 남지 않고 `reports/` + `logs/`만 존재

### 3.6 Setup 실제 정본 복제 통합

전체 Source 복제본에서 `setup` 실행 결과:

```json
{"status":"PASS","changed":[".gitignore","cpf-tools/verification/tools/cpf-source-state.py","cpf-tools/runtime/cli/cpf.py","cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md"],"commitExecuted":false,"pushExecuted":false}
```

확인:

- `/cpf-release/` Private Git ignore 추가 → PASS
- `cpf-release` Source Identity 제외 → PASS
- `CPF_FINAL_TARGET_REQUIREMENTS.md` 21.3 Open Git Requirement 삽입 → PASS
- `cpf open-git` CLI 등록 → PASS
- `cpf open-git` 인자 없는 기본 build parsing → PASS
- 두 번째 `setup` → `changed=[]`, idempotent PASS
- 기존 21.3 Open Git Section이 구버전이면 해당 Owner Section만 currentize하고 다음 EDU Section은 보존 → PASS
- commit/push 실행 → false

### 3.7 Baseline 충돌 검사

기준 ZIP 전체 파일과 별도 작업본을 SHA-256 비교했다.

- 기존 Baseline 파일 변경: **0**
- 기존 Baseline 파일 누락: **0**
- 이번 작업 신규 파일만 존재

따라서 이 Overlay 자체는 Codex가 수정한 기존 파일을 덮어쓰지 않는다. 단, Overlay 파일을 추가하면 Source Identity 분모는 바뀌므로 Codex Final Gate가 끝난 뒤 적용하는 것이 기준이다.

### 3.8 Exact Baseline Fresh Replay

`CPF_FULL_SOURCE_FOR_NEXT_QA_20260822_102905.zip`을 새 디렉터리에 다시 풀고 본 Overlay만 적용한 뒤 동일 저비용 Gate를 재실행했다.

- Overlay Manifest: payload 39건 hash/size mismatch 0
- Baseline 기존 파일 변경: 0
- Baseline 기존 파일 누락: 0
- 신규 + 기존 Public Release 회귀: `28 passed`, fail 0
- Open Git Source Projection: 439 files PASS
- Forbidden Framework root leakage: 0
- Gradle Wrapper 외 JAR/WAR leakage: 0

따라서 본 Overlay는 기준 Source의 기존 파일을 덮어쓰지 않고 신규 파일만 추가하는 Fresh Replay를 통과했다. Codex Final Source에 적용할 때도 먼저 Codex Final Gate/Source Identity를 끝낸 뒤 적용하고 `setup`의 Anchor Gate를 통과해야 한다.

## 4. Production Release 미검증 / Blocker

현재 개발 기준 Artifact Catalog를 사용자 Steering과 대조하면 다음 5건이 Release Blocker다.

```text
binary-only artifact is cataloged with publishSources=true: cpf-core
required binary artifact is not publicly publishable: cpf-admin publicationClass=MISSING
required binary artifact has no Public Maven group: cpf-admin
required binary artifact is not publicly publishable: cpf-gateway publicationClass=MISSING
required binary artifact has no Public Maven group: cpf-gateway
```

이 상태에서 임의 Maven 좌표를 생성하거나 ADM/Gateway Source를 공개하는 것은 Requirement 위반이므로 Tool은 **fail-closed**한다. Codex가 현재 Gradle/Catalog/Runtime을 수정하는 동안 본 Overlay가 해당 파일을 변경하지 않도록 의도적으로 남긴 통합 Blocker다.

현재 실행 환경 Java는 21이며 CPF 최종 Gate 요구 Java 25가 아니므로 실제 Java25 Root Build/Publication도 성공 처리하지 않았다. 실제 Open Git Remote push는 정책상 자동 수행하지 않는다.

## 5. 자체검수 판정

- Open Git Packaging 전용 Source 구현: **PASS**
- 기존 Public Release 회귀: **PASS**
- Source Projection / Leakage: **PASS**
- Codex 기존 파일 무덮어쓰기 Overlay: **PASS**
- Canonical integration setup: **PASS**
- Production Binary Contract: **BLOCKED_INTEGRATION**
- Java25 실제 Release Build/Publication: **NOT_EXECUTED**
- 전체 WP-14 최종 상태: **미완료 — Codex 종료 후 Artifact Catalog 통합 및 Java25 Final Release Gate 필요**

미실행/차단 항목을 PASS로 기록하지 않는다.
