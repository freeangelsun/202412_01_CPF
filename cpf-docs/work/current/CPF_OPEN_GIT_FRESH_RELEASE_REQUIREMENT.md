# CPF Open Git Release — Fresh Generation / Fresh Build / EDU Validation / Canonical Currentization Steering

## 0. 목적과 강제성

이번 Steering은 Open Git Release의 생성 방식, 공개·비공개 경계, Generated Domain Fresh Generation, Framework Fresh Build, Backoffice 예외, EDU 검증, Developer-facing 명령 전수 검증, Fresh 개발자 시나리오, 그리고 **개발 정본 현행화**를 하나의 완료 단위로 강제한다.

이 작업은 단순 문서 작성이나 Release Package 생성만을 의미하지 않는다.

다음을 모두 수행해야 한다.

1. 실제 Source/Generator/Build/Publication/Shell/CLI 구현 확인
2. 결함이 있으면 정확한 Owner Source 직접 보완
3. Open Git Release Fresh 생성
4. Fresh 개발자 관점 실제 실행검증
5. EDU Example Compile/Test 및 가능한 Runtime 검증
6. Generated Domain Fresh Generate/Compile/Test
7. Binary/Public Surface/Secret/Leakage 검증
8. **개발 정본과 Requirement 원장을 실제 구현과 동일 의미로 현행화**
9. Evidence와 Source Identity 재산정

**개발 정본 현행화가 끝나지 않으면 이번 Open Git Work Package는 완료가 아니다.**

---

# 1. 최상위 Release 원칙

Open Git Release는 Private master의 과거 결과물을 단순 복사하는 패키지가 아니다.

매 Release마다 현재 최신 Private Source를 입력으로 하여 Clean Release Workspace에서 필요한 것은 Fresh Generate하고, Framework Binary는 Fresh Compile/Publish하고, 사람이 작성·유지하는 Canonical Source만 Projection하여 새로 구성해야 한다.

```text
Author-maintained canonical assets
    → latest Private Source에서 Projection

Generated Java Source
    → latest Generator + canonical input으로 Fresh Generation

Framework Binary
    → latest Framework Source에서 Fresh Compile / Test / Publication

Backoffice Java Source
    → canonical prebuilt Source Projection
    → Fresh Compile / Test

Backoffice UI
    → canonical UI Source Projection
    → Fresh dependency install / Build / Test

EDU Source
    → canonical Education Source Projection
    → Fresh Compile / Test / 가능한 Runtime 검증

```

다음 결과물은 Release 입력으로 재사용하지 않는다.

- 기존 `build/**`
- 기존 `build/libs/**`
- 과거 JAR/POM/BOM
- 과거 Publication 산출물
- 이전 Release staging
- 이전 Open Git package
- 기존 Generated Domain Java 결과물을 복사하여 Fresh Generation으로 간주하는 방식
- Local Maven cache의 과거 CPF Artifact에 의존한 False Green

---

# 2. Private master가 유일한 개발 정본

Private master는 다음의 유일한 개발 정본이다.

- CPF 전체 Product Source
- Generator
- Generated Domain 규칙
- Framework Publication
- Open Git Release Policy
- Open Git Packaging Tool
- Public/Private Artifact Policy
- Developer CLI
- EDU Canonical Source
- Backoffice Canonical Source
- Open Git Requirement

Open Git은 독립 개발 정본이 아니다.

정상 흐름:

```text
Private master 수정
→ 검증
→ Open Git Release Fresh Generation
→ Open Git 검증
→ 사용자 확인
→ 사용자 직접 commit/push

```

Open Git에서 발견한 결함은 Private master의 정확한 Owner Source/Generator/Policy/Template/CLI를 수정하고 Release를 다시 생성하여 닫는다.

---

# 3. Release Owner와 Local Generated Root

Open Git Release의 canonical Owner는 다음을 기준으로 한다.

```text
cpf-tools/release/open-git/**

```

이 영역이 다음을 소유한다.

- Surface Policy
- Artifact Policy
- Fresh Build/Publication
- Fresh Generated Domain generation
- Backoffice/EDU/UI projection
- Developer command packaging
- Secret/Leakage Gate
- Manifest/SHA
- Fresh developer validation
- VERIFIED + 사용자 Git 반영 상태 분리 Gate
- Release tests

실행 결과는 다음처럼 직관적으로 보이게 한다.

```text
cpf-release/
├─ open-git/
├─ binary-repository/
├─ reports/
└─ logs/

```

`cpf-release/**`는 local-generated 영역이며 Private Git에서 전체 ignore되어야 한다.

```gitignore
/cpf-release/

```

Release 정의/정책/Template/Tool의 정본을 `cpf-release/**`에 두지 않는다.

---

# 4. Release 재수행은 전체 Clean Regeneration

이전 결과를 부분 재사용하거나 stale 파일을 남기지 않는다.

매 실행:

```text
canonical cpf-release 경로 안전성 확인
→ tracked/protected path 오염 여부 확인
→ 기존 cpf-release 전체 제거
→ cpf-release 신규 생성
→ Open Git fresh workspace/fresh clone
→ Fresh Generation / Fresh Build / Projection
→ 전체 Gate

```

제품 Source나 보호경로를 광범위하게 삭제하지 않는다.

---

# 5. Open Git Source와 Binary Deliverable 분리

Open Git Source repository와 Maven-folder Binary Repository는 분리한다.

```text
cpf-release/
├─ open-git/             # 실제 Open Git Source repository
└─ binary-repository/    # Framework Maven-compatible binary deliverable

```

Framework JAR을 Open Git Git history에 Release마다 누적 commit하는 구조를 만들지 않는다.

---

# 6. Framework Binary는 매 Release Fresh Build

Framework Binary는 기존 JAR 복사가 아니라 현재 최신 Source에서 Fresh Build/Publication한다.

최소:

```text
latest Source
→ fresh compile
→ test
→ publication
→ POM/BOM/metadata
→ Maven-folder repository
→ checksum/hash

```

금지:

- 기존 `build/libs` 복사
- Local Maven 과거 artifact에 기대는 build
- 이전 Release binary 재사용
- Source와 artifact version/content 불일치

---

# 7. Generated Customer Domain Java Source는 반드시 Fresh Generation

Generated Customer Domain의 Java Source는 Private master의 기존 생성 결과를 단순 복사하지 않는다.

대상:

- `cpf-member`
- `cpf-external`
- Scratch/Public Generated Domain
- 향후 canonical Generated Customer Domain

Release 시:

```text
latest Generator
+
canonical Generator input
→ Fresh Generate
→ Directory/Java Package IA 검사
→ Fresh Compile
→ Fresh Test
→ Open Git Source 구성

```

Open Git의 Generated Domain은 실제 개발자가 `cpf domain new`로 만드는 결과와 동일한 Generator 계약을 가져야 한다.

---

# 8. Generated Domain에는 개발자에게 필요한 것만 생성

Generated Customer Domain은 Generator 내부 상태 저장소가 아니다.

개발자가 실제 업무 개발에 필요한 Developer-facing Source/Build 계약만 생성한다.

특히 다음 두 파일은 Generated Domain Root 및 Fresh Generate 결과에서 **존재하면 안 된다**.

```text
cpf-domain.yaml
cpf-generator.lock.json

```

이 파일에 있던 정보가 기술적으로 필요하다면:

- Generator-owned canonical input/state로 이동
- central generator metadata로 이동
- deterministic regeneration으로 대체

한다.

금지:

- `.cpf/` 같은 숨김 Root 폴더로 이동
- 이름만 바꾼 lock/state 파일 생성
- MBR/EXS에서만 수동 제거
- Open Git에서만 숨기고 Generator는 계속 생성
- 잘못된 정본을 이유로 그대로 유지

Generated Domain Root는 대략 다음 성격을 가져야 한다.

```text
cpf-member/
├─ online/
├─ batch/                  # 사용하는 경우만
├─ build.gradle
├─ settings.gradle
└─ gradle.properties

```

실제 Runtime/Build에 꼭 필요한 Developer-facing 파일이 있으면 유지할 수 있으나 Generator 내부 bookkeeping 파일은 생성하지 않는다.

---

# 9. Generated Domain Fresh Validation Matrix

한 개 Domain 생성 성공으로 끝내지 않는다.

최소:

```text
A. 최소 Online Domain
B. multi-feature Domain
C. dependency 포함 Domain
D. batch 선택 Domain
E. regenerate/sync
F. MBR clean regenerate
G. EXS clean regenerate

```

각 경우:

- generated file inventory
- Root cleanliness
- canonical directory IA
- canonical Java package IA
- domain name 중복 segment 0
- starter/dependency
- build registration
- 필요한 DB contract
- compile
- test
- 가능한 runtime
- legacy reference 0

을 확인한다.

---

# 10. Backoffice는 Generated Domain 표준을 따르지만 Fresh Generation 대상이 아니다

Backoffice는 Generated Domain과 같은 개발 IA/패키지 표준을 따르되 Generator로 매 Release 다시 만드는 영역은 아니다.

이미 선구현된 업무 Sample/Consumer/연동 Source가 의미 있는 canonical authored source이므로 다음과 같이 처리한다.

```text
latest canonical Backoffice Source
→ Open Git에 공개 가능한 고객 개발 Surface Projection
→ Fresh Compile
→ Fresh Test

```

Backoffice 예외를 이유로 Platform Internal Runtime/Private Source까지 공개하지 않는다.

Backoffice UI도 canonical Source를 Projection할 수 있으나 Release workspace에서 fresh dependency install/build/test를 수행한다.

---

# 11. EDU는 공식 Canonical Example Source

`cpf-education`은 개발자가 CPF 기능을 실제 코드로 보고 참고·학습하는 공식 Sample Source다.

EDU는 Generator가 다시 생성하는 Domain이 아니다.

Release에서는:

```text
latest cpf-education canonical Source
→ Open Git Projection
→ Fresh Compile
→ Fresh Test
→ 가능한 Representative Runtime Validation

```

을 수행한다.

---

# 12. EDU는 “존재 확인”이 아니라 실제 Example 검증

다음은 EDU PASS 근거가 아니다.

- 폴더 존재
- Java file 존재
- Interface/Sample skeleton 존재
- README 존재
- compile/test 미실행
- deprecated API를 사용하는 예제
- 실제 CPF Consumer가 없는 예제

각 Example은 실제 CPF 계약을 사용해야 하고 가능한 범위에서 테스트까지 성공해야 한다.

현재 Canonical EDU Inventory가 `Online 20 + Batch 15 = 35`로 유지된다면 실제 Source와 전수 대조한다.

숫자만 맞추지 말고 각 Example의 목적과 Consumer를 확인한다.

각 Example 최소 검증:

```text
Example ID / 목적
Source 존재
실제 CPF Public API/Starter Consumer
compile
test
필요 config
DB vendor neutrality
legacy reference 0
Open Git Projection

```

Runtime 환경이 있으면 대표 Example은 실제 Runtime까지 실행한다.

환경상 Runtime이 불가능하면 PASS가 아니라 `미검증`으로 기록하고 환경/명령/blocker/재실행 조건을 남긴다.

**예제 검증은 적어도 compile/test까지 실제 실행하는 것을 기본으로 한다.**

---

# 13. EDU가 사용하는 계약도 Current여야 한다

EDU Example은 현재 CPF Canonical 계약을 실제 사용해야 한다.

예:

- Canonical Header/Context
- operationId contract
- Public API/SPI
- current Starter
- Domain Invocation
- official DB Vendor Oracle/PostgreSQL/MariaDB
- Batch canonical contract
- External integration contract
- Generated Domain IA 기준

금지:

- 직접 Header 조립으로 Framework 우회
- Legacy API
- Deprecated package
- 구 Starter 명칭
- H2/MySQL/MSSQL 증적
- Consumer 없는 Sample
- 운영/Admin 구현 자체를 EDU로 복제

---

# 14. Open Git 공개 Source Policy

Default-Deny를 사용한다.

```text
default = DENY

```

Source 공개 대상:

- Fresh Generated Customer Domain Source
- 고객 Backoffice 개발 Surface
- `cpf-education`
- Developer-facing Generator/Setup/Build/Test/Domain CLI
- 필요한 Template/Config

Source 비공개:

- Framework Internal implementation
- ADM internal source
- Gateway internal source
- Batch runtime/worker/scheduler/agent internal source
- Internal provider source
- QA/Governance/Internal evidence
- Private release implementation source

---

# 15. Binary / Source Profile / sources.jar 정책

기본 Open Git Profile은 **Binary Distribution(`binary`)**이며 Framework 구현 Source와 Framework `sources.jar`/`javadoc.jar`는 공개하지 않는다. `sources.jar`는 Source 공개 수단으로 사용하지 않는다.

```text
binary (default)
  Public Framework/Starter Runtime Binary + BOM/POM/metadata/checksum/SBOM
  Customer Development Source Tree 포함
  Framework implementation Source = 0
  Framework sources.jar = 0
  javadoc.jar = 0

source (explicit optional)
  binary 전체
  + Canonical Public Source Allowlist의 승인 Public API/SPI/Annotation Source Tree만 framework-source/로 Projection
  sources.jar/javadoc.jar = 0
```

Customer Development Source(Generated Domain, Backoffice, Sample/EDU, 고객 Config/SQL/Frontend/Test/Build)는 기본 Binary Profile에서도 실제 Source Tree로 제공한다. Framework Source 비공개와 고객 개발 Source 제공을 혼동하지 않는다.

Internal/Generator Engine/Development·QA Harness/Evidence/Secret/Internal Release Tool은 모든 Public Profile에서 금지하며 미분류 Artifact/Source는 공개하지 않는다. Artifact별 정책은 Canonical Artifact Catalog와 Default-Deny Release Policy를 따른다.

---

# 16. Developer-facing 명령 전수 Inventory

미리 정한 몇 개 명령만 검사하지 않는다.

실제 Open Git Package에 노출된 모든 user-facing command와 option을 전수 Inventory한다.

대표 Open Git 고객 UX:

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

Private CPF Framework 개발자의 Release UX는 같은 Unified CLI의 INTERNAL Capability인 `cpf release open-git ...`이며 Open Git Binary Profile에는 Projection하지 않는다.

실제 공개 명령이 더 있으면 모두 검사한다.

긴 내부 script filename을 사용자가 외워야 하는 구조를 피한다.

---

# 17. Developer command UX 검증

각 명령은 실행 중 최소:

```text
현재 단계
전체 단계
현재 작업
진행/대기 상태

```

를 보여준다.

종료 시:

```text
PASS / FAIL
ExitCode
Start Time
End Time
Log File
실패 원인
다음 행동

```

을 보여준다.

장시간 작업 중 콘솔이 멈춘 것처럼 보이지 않게 한다.

로그는 Timestamp별 저장하고 console에도 실시간 출력한다.

---

# 18. `cpf bootstrap` 실제 Acceptance

`cpf bootstrap`은 Shell launch 성공으로 PASS가 아니다.

Fresh Open Git 개발자의 실제 개발환경을 준비해야 한다.

최소:

```text
Prerequisite
→ Java 25
→ Git
→ Container Runtime
→ 필요한 Node
→ CPF Version/Binary Repository
→ DB Container
→ Health wait
→ User/Schema
→ Migration
→ Seed
→ Middleware
→ Domain discovery
→ Build
→ Test
→ Runtime start
→ Health
→ Registry/기본 상태
→ CPF LOCAL DEVELOPMENT READY

```

최종 성공 기준:

```text
CPF LOCAL DEVELOPMENT READY

```

---

# 19. 모든 Developer command 실제 실행검증

Shell/CLI 존재나 `help`만으로 PASS 처리하지 않는다.

각 명령에서 실제 확인:

1. 명령 인식
2. help/options/default와 구현 일치
3. Open Git Package/Binary만으로 실행
4. Private CPF Source 참조 없음
5. Local Maven 과거 Artifact false-green 없음
6. Windows/Linux wrapper 계약
7. 진행상태 출력
8. ExitCode 정확성
9. 오류/다음 행동 안내
10. 재실행 멱등성/안전복구

---

# 20. Fresh Open Git Developer Acceptance Scenario

Release 생성 후 완전히 별도 Fresh Workspace에서 외부 개발자 관점으로 검증한다.

Private CPF Source를 사용할 수 없는 조건이어야 한다.

최소:

```text
Fresh Open Git workspace
→ 공개 Binary Repository 연결
→ cpf bootstrap

→ 기존 Generated Domain build/test
→ EDU compile/test
→ Backoffice 공개 Java compile/test
→ UI fresh build/test

→ cpf domain new
→ 신규 Domain Fresh Generation
→ generated file inventory
→ cpf domain sync 또는 bootstrap
→ 신규 Domain compile/test
→ 가능한 Runtime/Health
→ 대표 호출

→ 전체 workspace build
→ 전체 test
→ verify
→ status
→ stop

```

---

# 21. Domain Generator Acceptance

`cpf domain new` ExitCode 0으로 끝내지 않는다.

실제 Scratch Domain을 만들고:

- Directory IA
- Java package IA
- feature structure
- dependency
- optional batch
- build registration
- current Starter
- compile
- test
- regenerate/sync
- 가능한 runtime

을 확인한다.

Fresh result에서:

```text
cpf-domain.yaml
cpf-generator.lock.json

```

이 없어야 한다.

---

# 22. Failure Path 검증

최소 다음을 실제로 검사한다.

- Java version 오류
- Docker/Container runtime 미기동
- Binary Repository 접근 실패
- 잘못된 CPF Version
- 잘못된/중복 Domain name
- 필수 option 누락
- DB 연결 실패
- Migration 실패
- Build 실패
- Test 실패
- Bootstrap 중간 실패 후 재실행
- 존재하지 않는 명령
- 잘못된 option
- reset confirmation 누락

실패 시 개발자가 원인/단계/로그/다음 행동을 바로 알 수 있어야 한다.

---

# 23. stop/reset 안전

`stop`과 `reset` 역할을 분리한다.

`reset`은 destructive action이므로 명시 확인 없이 실행하지 않는다.

예:

```text
cpf reset --confirm

```

확인이 없으면 안전하게 거부하고 ExitCode/이유/다음 명령을 알려준다.

---

# 24. Open Git Release Gate

최소 의미:

```text
01 Release root safety / cpf-release Private master tracked=0
02 Previous cpf-release current-only clean regeneration
03 Repository Root / Branch / HEAD / Working Tree / Source Identity capture
04 Toolchain
05 Framework Fresh Compile/Test
06 Binary Publication
07 Generated Domain Fresh Generation
08 Generated Domain Compile/Test
09 Backoffice Fresh Compile/Test
10 UI Fresh Install/Build/Test
11 EDU Fresh Compile/Test
12 Surface/Artifact Policy
13 Secret/Private Source/sources.jar Leakage
14 Developer Command Validation
15 Fresh Developer Acceptance
16 Manifest/Checksum/SBOM/Evidence
17 Open Git Working Tree read-only git status / git diff --check
18 Git write-command negative (add/index/commit/push/reset/restore/stash/clean=0)
19 VERIFIED
20 User Review (Tool 자동 전이 금지)
```

실제 구현에 따라 단계 수는 조정 가능하나 의미는 줄이지 않는다.

---

# 25. VERIFIED / 사용자 Git 반영 상태 모델

Release 상태는 다음을 구분한다.

```text
GENERATED
→ VERIFIED
→ USER_REVIEWED
→ GIT_COMMITTED
→ GIT_PUSHED
```

Tool/CLI가 자동으로 도달할 수 있는 최종 상태는 `VERIFIED`다. `USER_REVIEWED`, `GIT_COMMITTED`, `GIT_PUSHED`는 사용자 검토/행위 없이 자동 전이하지 않는다.

`VERIFIED`는 Fresh package/binary/generation/build/test/runtime/fresh replay, 공개·비공개 정책, customer source completeness, Manifest/Checksum/SBOM, leakage 0, Git write 0이 모두 PASS한 상태다.

---

# 26. Git 반영 분리 / 자동 Git write 금지

`cpf-release/`는 Open Git 전달 전용이며 Private CPF master Commit/Push 대상이 아니다. Private `.gitignore`와 Source Identity에서 제외한다.

Release Tool/CLI/DevGPT/Codex는 사용자 승인 전 Private master와 Open Git fresh clone 모두에서 다음을 자동 실행하지 않는다.

```text
git add
git commit
git push
git reset
git restore
git stash
git clean
branch/tag/history write
```

검증 단계에서는 read-only `git status --short`, `git diff --check`, Branch/HEAD 조회만 허용한다. 필수 Release Gate가 모두 PASS한 뒤에만 Tool은 **사용자가 직접 실행할 Open Git Git 명령을 제시**할 수 있고 실행하지 않는다. CPF master용 Source Commit/Push와 Open Git Release Commit/Push는 명확히 분리한다.

---

# 27. ★ 개발 정본 현행화는 필수 완료조건

**이번 Steering에서 가장 중요한 추가 요구다.**

Open Git/Generated Domain/EDU/Developer CLI 구현을 Source에만 넣고 끝내지 않는다.

Codex는 구현이 끝난 뒤 반드시 현재 개발 정본을 직접 열어 이번 Requirement를 정확하고 상세하게 반영한다.

최소 현행화 대상:

```text
cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md
QA가 지정한 단일 Requirement 원장
Development / Closure Inventory
Generated Domain 관련 Canonical 문서
Generator 관련 Canonical 문서
Open Git / Release 관련 Canonical 문서
EDU 관련 Canonical 문서
Developer CLI / Bootstrap 관련 Canonical 문서

```

정본 위치가 최신 Source에서 달라졌다면 실제 canonical owner를 찾아 동일 의미로 현행화한다.

---

# 28. 개발 정본에는 최소 다음 의미가 반드시 남아야 한다

## Private master

- CPF 전체 Source와 Release 정의의 유일 정본

## Open Git Release Owner

```text
cpf-tools/release/open-git/**

```

## Local Release

```text
cpf-release/**

```

- local generated
- Private Git ignore
- 매 실행 clean regeneration

## Framework

- 기존 binary copy 금지
- 매 Release Fresh Compile/Test/Publication

## Generated Domain

- 기존 Generated Java Source copy 금지
- latest Generator Fresh Generation
- generated result compile/test
- `cpf-domain.yaml` 미생성
- `cpf-generator.lock.json` 미생성
- 개발자에게 필요한 Source/Build 계약만 생성

## Backoffice

- Generated Domain 개발 IA 표준 준수
- Generator 대상은 아님
- canonical prebuilt Java Source Projection
- fresh compile/test

## EDU

- canonical authored Example Source
- Open Git Source 공개
- fresh compile/test
- 가능한 representative runtime validation
- Canonical Example Inventory 관리

## Public/Private

- Generated Domain / Backoffice 개발 Surface / EDU / Developer CLI Source 공개
- ADM/Gateway/Batch/Internal Runtime Source 비공개
- Binary 기본 Profile sources.jar/javadoc.jar=0 + Optional Source Allowlist Tree 정책 적용

## Developer UX

- user-facing command 전수
- 짧고 직관적인 명령
- progress/error/ExitCode/log/next action
- Fresh developer scenario 실제 검증

## Final Gate

- Default-Deny
- Secret/Leakage
- Fresh Consumer
- Manifest/SHA
- VERIFIED + 사용자 Git 반영 상태 분리
- 자동 commit/push 금지

---

# 29. 잘못된 개발 정본은 Source와 함께 수정

현재 정본에 아래와 같은 오래되거나 잘못된 Requirement가 있다면 그대로 승계하지 않는다.

예:

- 기존 Generated Domain Source copy를 Release 방식으로 정의
- 기존 JAR 복사를 Publication으로 간주
- `cpf-domain.yaml` 필수
- `cpf-generator.lock.json` 필수
- EDU를 폴더 존재만으로 완료
- user command 실제 실행검증 누락
- `public`과 `open-git` 이중 정본 허용
- `cpf-release/**`를 Private master Git tracked/commit 대상 source로 취급
- Open Git 자동 commit/push 허용

이런 Requirement가 사용자 Steering/최종 제품 목표와 충돌하면 **개발 정본 자체를 명확하게 currentize**한다.

---

# 30. Requirement 원장/Closure Inventory 반영

이번 Open Git Release는 별도 정식 Requirement/Work Package로 추적되어야 한다.

예를 들어 현재 ID 체계와 충돌하지 않는 방식으로:

```text
Open Git Fresh Release
Generated Domain Fresh Generation
Open Git Artifact Exposure Policy
Developer CLI Fresh Acceptance
EDU Open Git Validation
Open Git Canonical Currentization

```

등의 acceptance가 누락되지 않도록 관리한다.

단, 역할별 컬럼 수정 권한은 기존 QA 규칙을 지킨다.

Codex는 Codex 소유 검수/보완 영역을 중심으로 수정하며, QA의 최종 상태를 임의로 완료 처리하지 않는다.

---

# 31. Source와 정본이 일치해야 완료

다음 상태는 허용하지 않는다.

```text
Source는 새 구조인데 Requirement는 옛 구조
Requirement는 Fresh Generate인데 Release Tool은 Source copy
EDU Requirement는 35개인데 실제 Example은 깨짐
CLI 문서에는 명령이 있는데 실제 Package에는 없음
sources.jar 정책은 비공개인데 Publication이 노출

```

최종 검수에서:

```text
Source
Generator
Release Tool
Artifact Policy
Developer CLI
EDU
Tests
Evidence
Canonical Requirement
Requirement Inventory

```

가 동일한 의미를 가져야 한다.

---

# 32. Finding 처리

검증 중 결함 발견 시:

```text
Finding OPEN
→ Root Cause
→ 정확한 Owner Source 수정
→ Targeted Test
→ Release 재생성
→ Fresh Workspace 재검증
→ broader gate
→ Finding CLOSED

```

임시 Release workaround로 Product defect를 숨기지 않는다.

---

# 33. False Green 금지

금지:

- 기존 build 결과 복사
- Local Maven cache 성공
- 기존 Generated Source copy
- MBR/EXS 수동정리만 수행
- help만 확인
- compile만 하고 test 생략
- EDU 폴더 존재만 확인
- 실행 불가 Runtime을 PASS 기록
- Private Source 참조
- 임의 Artifact 좌표 추가
- sources.jar leakage를 무시
- 정본 미현행화 상태에서 완료
- Requirement를 짧게 축약해서 사용자 의도 상실

---

# 34. Evidence

최소 기록:

- 기준 Source Identity
- 실제 실행 명령
- Start/End
- ExitCode
- PASS/FAIL/미검증
- Test count
- 오류 요약
- 로그 위치
- generated files/artifact count
- Manifest/SHA
- Finding ID
- 재실행 조건

미실행은 PASS가 아니다.

---

# 35. Delete/Garbage 안전

삭제 후보는 다음 모두 충족할 때만 Delete Manifest에 넣는다.

```text
Consumer 0
Current Requirement 불필요
Canonical replacement 존재
Fresh generate/release 미사용
회귀 PASS
100% stale/duplicate/garbage 확정

```

사용자 승인 없이 실제 제품 Source 삭제를 수행하지 않는다.

---

# 36. Git 안전

사용자 승인 없이 수행 금지:

- commit
- push
- branch
- tag
- reset
- restore
- stash
- clean
- history rewrite

Open Git Release Tool도 실제 commit/push하지 않는다.

---

# 37. 최종 Acceptance Criteria

다음 질문에 모두 YES여야 한다.

## Release 생성

- 최신 Source에서 Framework Binary Fresh Build했는가?
- 기존 JAR을 복사하지 않았는가?
- Generated Domain을 latest Generator로 Fresh Generate했는가?
- 기존 Generated Java Source copy를 사용하지 않았는가?
- Backoffice Source Projection 후 Fresh Compile/Test했는가?
- UI Fresh Build/Test했는가?
- EDU Fresh Compile/Test했는가?

## Generated Domain

- Developer-facing 파일만 생성되는가?
- `cpf-domain.yaml` 없는가?
- `cpf-generator.lock.json` 없는가?
- IA/package가 canonical인가?
- MBR/EXS/Scratch parity가 맞는가?

## Developer Usage

- 공개 명령 전수 Inventory했는가?
- 실제 실행했는가?
- bootstrap이 Fresh 환경을 준비하는가?
- Domain 생성 후 compile/test가 되는가?
- 전체 build/test 가능한가?
- 오류/로그/ExitCode/다음 행동이 명확한가?
- stop/reset이 안전한가?

## EDU

- Canonical Example Inventory와 실제 Source가 일치하는가?
- Example이 current CPF 계약을 사용하는가?
- Compile/Test 실제 실행했는가?
- 가능한 대표 Runtime 검증을 했는가?

## Security/Release

- Default-Deny인가?
- Private Source leakage 0인가?
- Secret leakage 0인가?
- 금지 sources.jar leakage 0인가?
- Manifest/SHA 일치하는가?
- Fresh developer validation PASS인가?
- staged diff check PASS인가?
- 자동 commit/push가 없는가?

## ★ Canonical Currentization

- `CPF_FINAL_TARGET_REQUIREMENTS.md`가 현행화됐는가?
- Requirement 원장/Closure Inventory가 현행화됐는가?
- Generated Domain/Generator/EDU/Open Git/CLI Requirement가 이번 Source와 같은 의미인가?
- 오래된 Requirement가 남아 있지 않은가?
- 중복 Canonical 문서가 서로 다른 내용을 말하지 않는가?

**Canonical Currentization 항목 중 하나라도 NO이면 전체 완료가 아니다.**

---

# 38. 최종 외부 개발자 판정

Codex는 최종적으로 실제 실행 근거를 가지고 다음 질문에 답해야 한다.

> Private CPF Source가 없는 Fresh 환경에서 Open Git Release만 받은 개발자가 공개된 Binary와 명령을 사용해 최초 설정 → Bootstrap → 기존 Domain Build/Test → 신규 Domain Fresh Generation → 신규 Domain Build/Test → EDU Example Test → Backoffice/UI Build/Test → 가능한 Runtime/Health까지 실제로 진행할 수 있는가?

YES가 아니면 Open Git Release는 READY가 아니다.

---

# 39. 최종 개발 정본 판정

또한 다음 질문에도 YES여야 한다.

> 현재 `CPF_FINAL_TARGET_REQUIREMENTS.md`, Requirement 원장/Closure Inventory 및 관련 Canonical 문서가 실제 Open Git Release Source/Generator/Artifact Policy/Developer CLI/EDU 구현과 동일한 최종 제품 규칙을 상세하고 왜곡 없이 설명하고 있는가?

NO이면 **정본을 현행화한 뒤 다시 검수**한다.

---

# 40. 최종 한 문장

> **Open Git Release는 최신 Private Source를 기준으로 매번 Clean하게 재구성하며, Framework Binary는 Fresh Build하고 Generated Customer Domain Java Source는 latest Generator로 Fresh Generate한다. Backoffice/EDU/UI처럼 사람이 유지하는 Canonical Source만 Projection한 뒤 Fresh Build/Test한다. 외부 개발자가 사용하는 모든 명령과 EDU Example을 Fresh 환경에서 실제 검증하고, Source·Release Tool·Generator·Artifact Policy·Developer UX·Evidence와 동일한 의미로 개발 정본 및 Requirement 원장까지 현행화된 경우에만 READY로 판정한다.**

**다시 강조한다: 개발 정본 현행화는 선택사항이 아니라 이번 Steering의 필수 Acceptance Criteria다.**