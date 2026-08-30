# CPF C 개발/QA 관리_2_6 — 다음 세션 상세 인수인계

## Current Authority
- Hotfix session: `20260830_devgpt_delete_runtime_hotfix` / merged / pending 0 / conflict 0
- 단일 실행 정본: `cpf-docs/governance/development-harness/`
- Current Registry: 410 = Tracking 394 + Root Cause Execution 16
- Product Source Identity: `1a39531bcd1f0b1c82bbc6f330ab7b8256fc9132f62676ed1c8887ae42040839` / 8,451 files

## 이번 Source 보완
- VS Code/Buildship source-empty profile class-folder 준비 및 fail-closed 검증 보완.
- Frontend npm lock 정합성 보완.
- Java/Node/npm/PowerShell capability-first host toolchain 공통정책을 Harness CR-22 및 Validator/Negative Mutation에 반영.
- Open Git/Release 한글 단계 UX와 상세 로그 분리 보완.
- Gateway/BAT StrictMode 결과 수집 및 Batch credential 경로 보완.
- Docker 전체설치/증분설치 공식 PowerShell lifecycle 진입점 복구.
- Current Registry/Status stale verifier를 단일 Current Harness 구조로 currentize.
- Legacy Delete Manifest intermediate replacement 직접 존재 검사를 제거하고 Migration Semantic Closure의 transitive terminal replacement SHA 검증에 위임.

## 검증 결과
- Verification suite: 126 PASS / 0 FAIL.
- Hotfix targeted regression: 23 PASS / 0 FAIL.
- Hotfix Delete Manifest contract: 2 PASS / 0 FAIL; generated-root cleanup only.
- 변경 영향 회귀: targeted 82 PASS / 1 SKIP / 0 FAIL 및 Open Git/Toolchain/Generator/Runtime 추가 묶음 PASS.
- Harness Negative: BASE 17/17 + AUTH_A 5/5 + AUTH_B 5/5 + STRENGTH 6/6 PASS.
- NXT3 Layout 87/87 PASS; Repository Garbage PASS; Toolchain Contract PASS; Batch no-remote-Kafka PASS.
- Session Merge: sessions=4 merged=4 pending=0 conflicts=0.
- Clean Source: garbage=0 / emptyDirs=0.
- Development Final Gate의 Evidence Semantics만 `verifiedRows=0 documents=0`으로 FAIL. 검증 완화 금지; 전체 CPF physical/reviewer/QA 미완결을 정확히 차단한 상태.

## 다음 작업 순서
1. Overlay 적용 후 교정된 Delivery Delete Manifest의 4개 `GENERATED_ROOT`만 안전 삭제. 이전 46-file SHA Manifest는 폐기한다.
2. Fresh Windows VS Code/Buildship Error=0 Warning=0.
3. Same Source 최대강도 Java/Gradle/Frontend/DB3/Batch/One-WAS/Browser/Performance/Open Git replay.
4. 각 Mandatory WP별 execution Evidence를 Current Source Identity에 묶어 Registry/Role/Test Ledger currentize.
5. Evidence Semantics 재실행.
6. Codex/Claude Independent Review.
7. QA Final Acceptance.

`SOURCE_FIXED`, `VERIFICATION_PENDING`, `BLOCKED_EXTERNAL`, `NOT_EXECUTED`, `UNKNOWN`은 PASS가 아니다.


## Delete Manifest 교정
- 이전 46-file SHA Manifest는 폐기한다. 실행 시마다 바이트가 바뀌는 Gradle/Python generated artifact에 immutable SHA를 강제해 정상 재생성 파일에서 DELETE SHA MISMATCH가 발생했다.
- `.editorconfig`, `.gitattributes`, `.github/**`, `.gitignore`는 삭제 대상이 아니며 경로 정규화 과정에서 선행 `.`을 잃은 이전 Manifest 오류를 재발시키지 않는다.
- 현재 Delivery cleanup은 `gradle-plugin/.gradle`, `gradle-plugin/build`, `platform-bom/.gradle`, `build/tools/__pycache__` 4개 exact generated root만 허용한다.

## 로컬 최고강도 Runtime
- Current canonical request: `current/CPF_REQUIRED_FULL_RUNTIME_REQUEST.md`.
- Entry point: `cpf-tools/verification/tools/run-cpf-required-full-runtime-validation.ps1`.
- Fresh VS Code Problems JSON을 Downloads에 `CPF_VSCODE_PROBLEMS_*.json`으로 export한 후 실행한다. PRIMARY → VS Code 0/0 → FRESH_REPLAY까지 동일 Source Identity로 강제한다.
