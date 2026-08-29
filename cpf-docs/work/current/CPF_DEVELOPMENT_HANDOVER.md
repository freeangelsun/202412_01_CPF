# CPF Development Handover — Current

## Authority

- Input Working Tree ZIP SHA-256: `52d807563afb9268ef176d89dff8b4721e1a1c5b6475fb8ae0f84e269c6d3886`
- Current Product Source Identity: `b162d358b4f127f7c4e3a816f89dcc8ab5fc2f66423ec7c37cbbefd6414fde9e` / 8,351 files
- Developer Requirement Ledger: 218 rows
- QA24 Closure Inventory: 222 rows
- Git writes: 0

## Source Rework Closure

Runtime UTF-8 fail-closed, VS Code Buildship/JDT source-empty project classpath model(configuration-time discovery + pre-repair `cpfVerifyIdeClasspathModel`), Observability failure envelope, Unified CLI/Generator UTF-8 launcher, Tool EntryPoint evidence exclusion, DB4 verifier currentization, Local Runtime canonical project path, Batch runtime account/secret, Gateway/BAT failure evidence, Frontend toolchain detail, Managed-state snapshot ordering을 보정했다.

보호 경로 `cpf-tools/environment/docker-development-test/**`에서 최신 ZIP에 누락된 3개 설치 Script는 과거 Source Closure ZIP에서 가져와 `CHANGE_MANIFEST.csv` 기록 SHA와 정확히 일치할 때만 복원했다.

## Current 검증

현재 환경 실행 가능 static/Python 회귀 FAIL 0. Java25/Windows/Docker/Browser Physical Acceptance는 미검증이며 전체 완료가 아니다. 상세는 `TEST_AND_EVIDENCE.md`, `OPEN_ISSUES.md`, `CPF_QA24_DEVELOPMENT_CLOSURE_INVENTORY.csv`를 따른다.

## 다음 순서

1. Overlay 적용 및 SHA 검증.
2. 승인 Delete Manifest 적용.
3. Low-cost Gate.
4. Java25 Root Build/Test/Publication.
5. Fresh Gradle Import + 전체 Domain/Module VS Code Problems **Error 0 / Warning 0**. 하나라도 있으면 즉시 같은 WP에서 수정 후 재검증.
6. DB3 → Unified CLI → Batch → One-WAS → Frontend/Browser → Performance → Actual Open Git.
7. Required Full Runtime + Same Source Fresh Replay.
8. Codex/Claude current-source 독립 검수.

Codex/Claude는 Source 수정 시 전체 VS Code 0/0과 Requirement별 개발/검증/Runtime/Evidence 근거를 반드시 남긴다.

## VS Code 재개방 인수인계

- 기존 task-only directory 보정은 Fresh Import에 적용되지 않아 사용자 화면의 missing-library marker가 그대로 남았다. 이 방식은 폐기했다.
- Current 방식은 Source 0개 Java project를 동적으로 discovery하고 Gradle configuration/Tooling API model 반환 전에 canonical `compileJava` output을 materialize한다. 모듈명 하드코딩, fake class, dependency 변경은 없다.
- `cpfVerifyIdeClasspathModel`이 explicit repair task보다 먼저 PASS해야 하며, 최종 Acceptance는 사용자 Windows Java25 Fresh Gradle Import 후 **전체 Domain/Module Error 0 / Warning 0**이다.
- Codex/Claude가 후속 수정할 때 이 구조를 다른 방식으로 되돌려 핑퐁하지 않는다. 상위 QA Requirement와 충돌하는 실제 근거가 있을 때만 같은 Root Cause 내에서 교정한다.
