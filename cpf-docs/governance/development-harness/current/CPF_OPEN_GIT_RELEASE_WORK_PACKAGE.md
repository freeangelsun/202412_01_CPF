# CPF Open Git Release Work Package

## 1. Work Package

- ID: `WP-14`
- Name: `Open Git Release Projection / Packaging / Developer DX`
- Owner: Development GPT
- Source Owner: `cpf-tools/release/open-git/**`
- Generated Root: `cpf-release/**`
- Target Canonical Requirement: `CPF_FINAL_TARGET_REQUIREMENTS.md` §21.3
- Codex/QA 상태 컬럼: 본 Work Package에서 변경하지 않음

## 2. 목표

Private CPF Source를 유일한 개발 정본으로 유지하면서 개발자에게 공개할 Source, 개발 명령, Binary Repository를 Default-Deny 방식으로 분리해 Open Git Release를 생성한다.

Open Git Repository는 별도 개발 정본이 아니다. 모든 변경은 Private Source에서 수행하고 Release Projection을 다시 생성한다.

## 3. 로컬 Release 구조

```text
cpf-release/
├─ open-git/             # 실제 Open Git fresh clone + Source Projection
├─ binary-repository/    # Maven-compatible CPF Binary Repository
├─ reports/              # Manifest / SHA / Release Status
└─ logs/                 # 실행 로그
```

`cpf-release/`는 Private Git과 Git-independent Source Identity에서 제외한다. 재실행 시 정확히 이 디렉터리만 안전성 확인 후 전체 제거하고 신규 생성한다.

## 4. Source 공개 정책

공개:
- Generated Customer Domain (`cpf-member`, `cpf-external` 및 Generator 생성 Domain)
- MBW Backoffice 업무 개발 Main Source와 OpenAPI/Build 계약
- Backoffice Web Channel/BFF Source
- `cpf-education` 실행·학습 Source
- Setup/Bootstrap/Build/Test/Domain 생성·동기화 등 Developer Command
- 공개 문서/설정/Domain Definition

비공개:
- `cpf-core`, `cpf-common`, `cpf-admin`, `cpf-biz-admin`, `cpf-batch`, `cpf-gateway`, `cpf-starters` 내부 Source Tree
- Internal Provider/Runtime Source
- Governance/QA/Evidence/Internal Release Source
- Secret/Credential

## 5. Binary 공개 정책

Framework 기능은 Maven-compatible Binary Repository로 사용한다.

- 기본 `binary`: Public Framework/Starter Binary + BOM/POM/Checksum/SBOM, Framework `sources.jar`/`javadoc.jar` 0건
- Optional `source`: 동일 Binary + Canonical Public Source Allowlist의 승인 Source Tree만 `framework-source/`로 Projection
- Core/Common/Admin/Gateway/Batch/Internal Runtime 구현 Source와 Internal Artifact는 기본 Binary에서 비공개
- Public BOM/Generator/공개 Runtime Artifact 제공
- Open Git Source Workspace 자체에는 누적 CPF JAR/WAR를 넣지 않음

현재 Baseline Artifact Catalog에서 `cpf-admin`, `cpf-gateway`의 Public Maven 좌표/Publication Class 및 `cpf-core` source 공개 정책이 사용자 Steering과 일치하지 않는 경우 Release Blocker로 검출하며 임의 좌표를 만들지 않는다.

## 6. 사용자 UX

Release 담당자 Canonical 명령:

```text
cpf release open-git
cpf release open-git check --profile binary
cpf release open-git status
cpf release open-git build --profile source
```

Open Git 개발자 Canonical 명령:

```text
cpf bootstrap
cpf domain-new <domain>
cpf domain-sync [domain]
cpf build
cpf test
cpf run
cpf status
cpf stop
cpf reset
```

공식 Tooling Interface는 `cpf-tools` 소유의 exactly-one Java `cpf` CLI다. Linux `cpf`, Windows `cpf.cmd`/`cpf.ps1`은 Thin Wrapper이며 기존 개별 Script는 Canonical Engine 또는 동일 Java CLI를 호출하는 호환 alias로만 남긴다. 장시간 작업은 진행 단계/전체 단계와 하위 명령 출력을 콘솔에 실시간 표시하고 Timestamp 로그를 동시에 저장한다. 종료 시 PASS/FAIL, ExitCode, 시작/완료 시각, 로그 전체 경로, 실패 원인과 다음 행동을 표시한다. `cpf bootstrap`의 기본 성공 기준은 `CPF LOCAL DEVELOPMENT READY`이며 Runtime 시작/Health 확인까지 포함한다. `cpf reset`은 명시 확인 전 destructive action을 수행하지 않는다.

`cpf-tools/release/open-git/cpf_open_git.py`와 OS별 release script는 Unified CLI가 호출하는 Canonical Release Engine/저수준 자동화 Consumer다. 별도 Open Git CLI 제품군을 만들지 않는다.

## 7. Lifecycle

```text
현재 Private Source
→ Source Identity / Clean Source 확인
→ 기존 cpf-release 안전 전체 제거
→ cpf-release 신규 생성
→ Private Release Gate
→ Raw Binary Publication
→ Open Git Artifact Policy 적용
→ Final Binary Repository 검증
→ Default-Deny Open Git Source Projection
→ Secret / Private Source / JAR Leakage 검사
→ Open Git Remote Fresh Clone
→ Projection Sync
→ Fresh Workspace Build/Test
→ Manifest / SHA / Status
→ Open Git `git status --short` / `git diff --check` read-only 검증
→ Git write-command negative
→ VERIFIED
→ 사용자 검토
→ 사용자 직접 Open Git commit/push
```

Release Tool은 `git add`/index staging/commit/push를 수행하지 않는다. `cpf-release/`는 Private master에 Commit/Push하지 않으며, 모든 Release Gate PASS 후 사용자가 Open Git에서 직접 Git 반영한다.

## 8. Acceptance Criteria

1. `cpf-release/` 외 경로를 재생성 삭제하지 않는다.
2. `cpf-release/` symlink 또는 Private tracked 파일이 있으면 fail-closed한다.
3. 이전 Release stale 파일이 재사용되지 않는다.
4. Open Git Source에 금지된 Framework Source가 0건이다.
5. Open Git Source에 Gradle Wrapper 이외 CPF/Application JAR/WAR가 0건이다.
6. EDU/Generated Domain/Backoffice/Developer Command가 실제 Projection에 존재한다.
7. Common/Starter 외 금지 Owner의 `sources.jar`/`javadoc.jar`가 0건이다.
8. Public Binary Artifact 좌표가 불명확하면 임의 생성하지 않고 Release Blocker로 판정한다.
9. Fresh Open Git Clone과 isolated Binary Repository 기반 Build/Test가 성공한다.
10. Manifest/SHA/SBOM/Secret/Leakage/Git read-only Diff/Status Gate가 성공한다.
11. Release Tool은 Private/Open Git 어디에서도 사용자 승인 전 `git add`/index staging/commit/push를 실행하지 않는다. `cpf-release/` Private master tracked=0을 fail-closed 검증한다.
12. Codex 작업 중인 기존 Product/Gradle/Generator/Runtime Source를 이번 Overlay가 덮어쓰지 않는다.
13. 개발자 Canonical 명령은 짧은 단일 Dispatcher로 제공되고 호환 Wrapper는 동일 계약을 재사용한다.
14. 모든 장시간 개발 명령은 진행 단계/로그를 실시간 표시하고 PASS/FAIL, ExitCode, 시각, 로그 경로와 다음 행동을 출력한다.
15. `cpf reset`은 명시 확인 전 Local Data 삭제를 시작하지 않는다.
16. Package Help/Dispatcher/Wrapper가 공개하는 모든 사용자 명령과 옵션을 전수 Inventory하고 실제 목적 완료까지 실행한다.
17. Fresh checkout은 Private Source, Private 경로, `mavenLocal()` 및 기존 CPF cache 없이 별도 Gradle/Maven cache에서 검증한다.
18. Fresh checkout에서 setup → bootstrap → 기존 Domain build/test → 신규 Domain 생성/sync/bootstrap → compile/test → Runtime/Health/대표 호출 → stop을 끝까지 실행한다.
19. Java/Docker/Binary Repository/CPF Version/Domain identity/필수 옵션/DB/Build/Test/bootstrap 재실행/reset 승인/unknown command 실패경로를 실제 실행한다.
20. EDU/Backoffice/Generated Domain 대표 Consumer는 공개 Binary만으로 compile/run한다.
21. 필수 명령·Runtime·Health·실패경로 중 하나라도 미검증 또는 실패면 전체 Release는 READY가 아니다.

## 9. 재개발 방지 실행 순서

```text
Requirement·공개 경계 확정
→ Gradle/Publication/Generator 기반 계약 확정
→ 현재 Generated Domain 1회 currentize
→ Binary Repository 실제 생성·검증
→ Open Git Release clean regeneration
→ Fresh checkout 명령/실패경로 전수 실행
→ Runtime/Health/stop
→ 최종 Evidence/Source Identity/READY 판정
```

Steering의 문단 순서는 구현 우선순위가 아니다. Downstream Source를 먼저 임시 수정하지 않고 Owner 기반을 먼저 고정해 Generator/Projection을 한 번만 재생성한다.

## 10. 명령 전수 실행 원장

실제 Release 생성 후 `help`, Dispatcher, PowerShell/POSIX Wrapper에서 명령·옵션·기본값을 추출해 하나의 실행 원장을 만든다. 각 행은 인식, 구현 일치, Package 입력 파일, Private/Maven Local 독립성, Windows/POSIX parity, 진행 UX, 종료 UX, 재실행 안전성, 성공 실행, 실패 실행, Evidence를 가져야 한다. Help 또는 파일 존재만으로 완료 처리하지 않는다.
## 11. 2026-08-29 Actual Fresh Release 재개방 Finding

Source 입력은 `CPF_FULL_SOURCE_FOR_NEXT_QA_20260829_211247(1).zip` SHA-256 `E6E343947AB4D829996107833AD20CD056D35BAC340013F58E0B2068C9694B30`이다. 사용자가 실행한 Binary Profile Release는 Stage `05/14 Framework Binary Publication`에서 `:nxt3LayoutGate` 실패로 종료되었으며 Commit/Push는 실행되지 않았다. 실패 중간 `cpf-release.zip`은 최종 Release Evidence나 배포본으로 사용하지 않는다.

이번 재개방의 Root Cause와 Source 보정은 다음과 같다.

- NXT3 Garbage/Delete authority가 `CURRENT_DEVELOPMENT_STATUS.csv`로 잘못 연결된 경로를 `CURRENT_GARBAGE_DECISIONS.csv`로 교정한다. `DELETE_MANIFEST.csv`와 함께 Current authority를 유지하며 Gate를 완화하지 않는다.
- `gradlew clean` 뒤 `compileJava=NO-SOURCE`인 Java aggregate/profile project의 Buildship output이 사라지는 문제는 실제 Source가 0인 Java project를 자동 탐색해 Gradle User Home의 stable output으로만 materialize한다. 특정 cpf-admin/profile 하드코딩, fake class, API 추가로 우회하지 않는다.
- Framework Binary는 이전 `build/libs`, 사용자 `~/.m2`, 과거 `cpf-release`를 복사하지 않는다. Release 시작 시 exact `<CPF_ROOT>/cpf-release`를 안전 확인 후 전체 삭제하고, 현재 Source에서 `clean cpfBuild qualityGate cpfTest publicationGate publishCpfVerifiedLocalPlatformArtifacts`를 실행해 Fresh Maven staging을 생성한다.
- Public Release mode에서만 Private/native project identity를 유지한 채 Maven Publication 좌표를 `cpf-final-artifact-catalog.json`의 `publicGroupId/artifactId`로 projection한다. Public POM의 CPF project dependency 역시 Canonical Public 좌표로 rewrite하며 `com.cpf.internal` dependency/publication은 최종 verifier에서 0건이어야 한다.
- `cpf-batch-runtime`은 Catalog에 PUBLIC_RUNTIME으로 존재하므로 실제 `maven-publish` publication을 제공한다. Public BOM은 별도 hardcoded artifact 목록 대신 Final Artifact Catalog의 Public Java/Runtime 좌표 전체를 사용하고 stale `cpf-batch-contract`를 사용하지 않는다.
- 동일 Source owner가 Public Starter와 Public Runtime alias를 모두 제공하면 별도 Maven Publication을 생성한다. Runtime alias에는 sources/javadoc을 공개하지 않으며 Open Git Binary profile 최종 Repository의 sources/javadoc은 정책대로 0건이어야 한다.

재실행 시작 시 Release Engine이 `<CPF_ROOT>/cpf-release`를 자체 Fresh-clean하므로 사용자가 해당 디렉터리를 사전 수동 삭제할 필요는 없다. 반면 사용자가 별도로 만든 `cpf-release.zip`은 Release Engine 소유가 아니므로 자동 삭제 대상이 아니며, 실패본 확인 후 exact path로 삭제한다.

Source 수정 완료만으로 본 Finding을 CLOSED하지 않는다. Java25 Windows에서 실제 Release 14/14, Fresh VS Code/Gradle Import `Error=0 / Warning=0`, Public Binary Repository verifier, isolated Maven consumer, Fresh Open Git clone Build/Test/Runtime, Generator Windows/Linux classifier, Leakage 0와 Evidence/Source Identity 일치까지 실제 PASS해야 한다.

