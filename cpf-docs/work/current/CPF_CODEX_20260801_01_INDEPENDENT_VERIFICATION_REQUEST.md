# CPF Codex 독립 최종 검수 요청서

## 1. 목적과 기준

이 문서는 이전 대화 없이 Overlay 적용·사용자 Commit 후 최신 `master`를 한 번의 통합 순서로 검수하기 위한 요청서다.

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 최초 개발 시작 SHA: `23babb9140b90e501d6ac715e7b77f55b66198a5`
- Overlay 적용 기준 SHA: `19dd72b5978f2a3c630943c0fff05bee2d2fed34`
- `20260801_04` 변경과 Overlay 경로 충돌: 0건
- 검수 SHA: 실제 `HEAD == origin/master`인 최종 40자리 SHA
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 개발 원장: SELF 30 + QA36 Active Gap 85
- Source 개발 상태: 완료 113, README/연결 Manual 별도 재확인 2

## 2. 반드시 지킬 보호 규칙

- README와 README에서 연결되는 Manual·Guide를 수정하지 않는다.
- README·Manual은 미래 완성 상태를 가정해 작성될 수 있으므로 완료 근거로 신뢰하지 않는다.
- 완료 판단은 Source → SQL/Migration → API/OpenAPI → 실제 Consumer → Test/Gate → exact-SHA Runtime Evidence 순으로 한다.
- README·Manual의 표현을 근거로 Source 결함을 면제하거나 Requirement를 완료 처리하지 않는다.
- 사용자 승인 없이 Commit, Push, Branch, Tag, PR, Reset, Restore, Stash, Clean, 추적 파일 삭제를 수행하지 않는다.

## 3. 변경 영향 영역

- `cpf-admin`: Permission·Operation ID·Operator Trust·Audit·Calendar·Notification/Incident·Batch Proxy
- `cpf-admin/frontend`: 59 Route, Operation Workbench, Generated Consumer, 운영 화면·위험조치·오류상태
- `cpf-biz-admin/frontend`: 26 Route, Public Operation Workbench, Generated Consumer Closure
- `cpf-batch`: Owner 장애 fail-closed, Ghost Lock/Execution·Fencing·Recovery
- `cpf-common`: Calendar CAS/Actor/Persistence, Product DB-less/Profile Boundary
- `cpf-core`·`cpf-gateway`·Host Agent: 공통 Network 정책과 실제 Consumer
- `cpf-tools/db`: Oracle·PostgreSQL·MariaDB V92, Ghost SQL, Lifecycle parity
- `cpf-tools/scripts`: Requirement·Route·OpenAPI·Permission·DB·EDU·Security·Evidence·Hygiene Gate
- `.github/workflows`: exact-SHA Required Gate와 OpenAPI Coverage 인자 수정

변경 전체 목록은 `cpf-docs/work/manifest/CPF_20260801_01_CHANGED_FILES.txt`와 SHA Manifest를 사용한다.

## 4. 이미 수행한 검증

아래 정적 검증은 동일 Source에서 반복하지 말고, Overlay 적용 후 저비용 Gate의 회귀 여부만 한 번 확인한다.

- Python Unit Test 144건
- Source/Contract Gate 47/47
- ADM 59 Route, BZA 26 Route Source/Interaction Contract
- Controller Permission·Operation ID·Operator Trust·Audit·Calendar·Batch·Incident Negative Gate
- Java Source 98개, Frontend Source 112개 구문 검사
- ADM/BZA Source OpenAPI Coverage 298/84
- ADM/BZA 인증 제외 Operation Consumer 297/76
- 3DB Lifecycle·Generator Lifecycle·Supply-chain Static Contract
- Legacy 3,679 ID Continuity, Requirement 115행 실제 파일 경로
- README/Manual 보호와 Overlay Hygiene·Secret Boundary

이 결과는 `CONTROLLER_SOURCE_PRE_RUNTIME` 정적 결과이며 Release Runtime 성공으로 승계하지 않는다.

## 5. 한 번만 실행할 통합 순서

### Stage 1 — 저비용 Read-only Gate

Repository Root에서 다음을 한 번 실행한다.

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-cpf-final-readonly.ps1 -Root . -ExpectedSha <FINAL_SHA>
```

실패 시 해당 묶음의 최소 Gate만 재실행한다. 전체 Gate를 반복하지 않는다.

### Stage 2 — Java 25

```powershell
.\gradlew.bat clean test assemble qualityGate --no-daemon --stacktrace
```

JDK 25가 아니거나 Wrapper/Dependency 접근이 차단되면 Source를 임의 재설계하지 말고 명령·Java Version·오류·종료 코드를 Environment Blocker로 기록한다.

### Stage 3 — ADM/BZA Frontend

각 Frontend에서 한 번씩 실행한다.

```powershell
npm ci --ignore-scripts; npm run verify
```

Runtime OpenAPI Export → Orval Generate → Marker/Consumer → lint → typecheck → unit → production build 순서를 유지한다. Source Snapshot을 Runtime Export로 위장하지 않는다.

### Stage 4 — 3DB

Oracle·PostgreSQL·MariaDB 각각 Fresh Install → Upgrade → Runtime Query → Drift → Reverse Rollback → Forward Reapply → Backup/Restore를 한 번 실행한다. 한 Vendor 결과를 다른 Vendor에 승계하지 않는다.

### Stage 5 — Browser·Distributed Runtime

환경이 있을 때만 Playwright 3 Browser, Kafka·Redis·Batch·Scheduler·Worker·Gateway·Agent, 다중 인스턴스·Process Kill·Response Loss·Unknown Result·Recovery를 실행한다.

### Stage 6 — Supply-chain

SBOM·Vulnerability·License·Artifact Catalog·Hash/Signature를 최종 산출물에서 한 번 실행한다.

## 6. 완료 처리 금지 조건

- `HEAD != origin/master`, Dirty Working Tree, Evidence SHA 불일치
- Runtime OpenAPI Origin이 `BACKEND_RUNTIME`이 아님
- Java/Frontend/DB/Runtime 미실행을 성공으로 기록
- Requirement 완료 행의 Source·Consumer·Test 파일 누락
- Mutation Permission·Operator Trust·Audit·CAS·Fencing·3DB parity 실패
- README·Manual을 완료 근거로 사용
- Delete Manifest에 없는 추적 파일 삭제
- 검증 Script가 실행 중 Source·Checksum·Evidence를 변경함

## 7. 수정 시 최소 재검증 단위

- Route/Frontend: Route Source + Interaction + OpenAPI Consumer + 해당 Frontend verify
- Controller/API: Permission + OpenAPI Runtime Export/Generate/Consumer + 관련 Java Test
- SQL/Migration: 3 Vendor Static + 변경 Vendor Lifecycle 전체
- Batch Recovery: Batch Unit + 3DB Query + Two-worker/Fault Scenario
- Matrix/Evidence: Requirement Trace + Evidence Semantics + exact-SHA Read-only Gate

Codex는 기능을 임의 재설계하지 말고, Source Defect와 Environment Blocker를 분리한다. 수정이 없으면 동일 대형 Build를 반복하지 않는다.
