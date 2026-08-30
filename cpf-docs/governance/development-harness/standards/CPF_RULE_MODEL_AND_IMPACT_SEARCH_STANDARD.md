# CPF Rule Model · Impact Search Standard

> Development Harness 내부 통합 표준. 이 파일은 독립 정본이 아니며 `../CPF_DEVELOPMENT_HARNESS.md`의 통제를 받는다.

## 1. 목적과 해석 원칙

이 표준은 개발자·검수자·QA가 같은 Requirement를 서로 다르게 해석하지 않도록 **공통 규칙(Common Rule)** 과 **기능별 규칙(Feature Rule)** 을 분리하고, 하나의 Work Item에서 Source·Consumer·Test·Runtime·Evidence까지 빠르게 추적하는 방법을 정의한다.

규칙 우선순위는 다음과 같다.

`Current Product Contract / Architecture → Common Rule → Feature Rule → CURRENT_WORK_ITEM_REGISTRY.csv의 개별 Work Item → 실제 Test/Runtime Evidence`.

하위 규칙이나 개별 Work Item은 상위 규칙을 축소·면제·재정의할 수 없다. Feature Rule은 Common Rule에 **추가**되는 규칙이며 Common Rule을 대체하지 않는다.

## 2. 단일 작업대상 정본

사람이 직접 개발 대상을 선택·추가·분해·상태 확인하는 작업대상 정본은 다음 **한 파일뿐**이다.

`current/CURRENT_WORK_ITEM_REGISTRY.csv`

다른 Requirement/Role/Test/Status/Review/Evidence 파일을 별도의 작업대상 목록처럼 운영하지 않는다. 해당 파일들은 Product Contract, 역할 수행, Test 실행, 상세 리뷰, Evidence를 보존하는 **보조 원장 또는 파생 View**다.

- 새 Requirement·Steering·QA Finding·Defect·Runtime Failure·Side Effect는 반드시 `CURRENT_WORK_ITEM_REGISTRY.csv`의 기존 Root Cause에 병합하거나 새 Work Item으로 추가한다.
- 상위 제목만 추가하지 않는다. 실제로 독립 개발·검증 가능한 세부항목까지 동일 파일의 행으로 분해한다.
- 하나의 행에 서로 다른 Root Cause, 서로 독립적인 Consumer, 서로 다른 완료조건을 억지로 묶지 않는다.
- Parent/Umbrella Work Item은 Child 세부항목이 모두 충족되기 전에 CLOSED/PASS가 될 수 없다.
- 역할별 개인 TODO, 대화 중 목록, 별도 CSV/MD를 새 작업 정본으로 만들지 않는다.

## 2.1 단일 Registry 사람용 가독성 계약

`CURRENT_WORK_ITEM_REGISTRY.csv` 한 파일만 열어도 각 역할이 다음 행동을 결정할 수 있어야 한다. 따라서 최종 Current Registry Schema는 최소 다음 정보를 **직접 값 또는 명확한 Projection 열**로 노출해야 한다.

- Work Item ID / Parent-Child 또는 Root Cause 관계
- Requirement ID / Priority / 실행순서
- Canonical Owner / 개발 Owner
- 원 요구 / Root Cause / 현재 Observation
- 개발범위 / Source / Consumer / 호출경로
- Config / DB / Generator / API/OpenAPI / Frontend 영향
- Static Acceptance / Runtime Acceptance / Regression Scope
- DevGPT 상태
- Independent Reviewer(Codex=Claude) 상태
- QA 상태
- Runtime 상태
- 전체 상태
- current_action
- last sessionKey / last merged sessionKey 또는 해당 Evidence Reference
- 완료/미완료 사유 / 재실행 조건
- Current Source Identity

상세 명령·로그·SHA와 역할별 실행 이력은 Role/Test Ledger와 session Evidence에 둘 수 있지만, **현재 무엇을 해야 하는지 판단하려고 다른 작업 목록을 조립해서는 안 된다.** Registry에서 해당 Work Item의 현재 역할상태와 다음 행동을 확인한 뒤 필요한 상세 Evidence로 Drill-down한다.

현재 활성 개발자가 Registry를 동시에 수정 중인 경우 Schema 변경을 덮어쓰지 않는다. 이 표준이 적용된 후 첫 Merge Integrator가 최신 Registry 행을 보존한 상태에서 누락 Projection 열을 currentize하고, 행 수/ID/상태/Source Identity 손실 0을 검증한다.

## 3. Common Rule

다음 규칙은 기능 종류와 관계없이 모든 Work Item에 적용한다.

| Rule ID | 공통 규칙 | 왜 필요한가 / 왜곡 방지 기준 |
| --- | --- | --- |
| CR-01 | Current Source Identity | 과거 PASS를 현재 Source 성공으로 승계하지 않는다. 실행·Evidence·상태는 exact Source Identity와 연결한다. |
| CR-02 | Requirement 비축소 | 사용자 요구, Product Contract, Architecture, QA Requirement를 Interface/DTO/Sample/Test 존재 수준으로 축소하지 않는다. |
| CR-03 | Root Cause 단위 관리 | 첫 오류나 지적 파일만 고치지 않고 동일 Root Cause의 Repository 전체 잠복 결함을 함께 찾는다. |
| CR-04 | 변경 전·후 영향분석 | Owner, upstream/downstream Consumer, 호출경로, Config, DB, Generator, API/OpenAPI, Frontend, Runtime, Side Effect, Regression을 변경 전과 후에 같은 범위로 확인한다. |
| CR-05 | Full Implementation | Source → Consumer → 호출경로 → 오류/복구 → Test/Runtime → Evidence가 연결되지 않으면 구현 완료가 아니다. |
| CR-06 | Hardcoding/임시구현 금지 | PC 절대경로, 임시 상수, fake output, TODO성 구현, 검증을 피하기 위한 suppression/waiver를 완료 수단으로 사용하지 않는다. |
| CR-07 | UTF-8/NFC | Source/Text/YAML/SQL/JSON/JSONL/Markdown/PowerShell/Python/stdout/stderr/Tee/File Log/DB Log/Trace/Evidence는 UTF-8을 사용하고 한글 mojibake/control-char를 허용하지 않는다. 한글 파일명은 NFC를 기본으로 한다. |
| CR-08 | Profile 완전성 | 필요한 설정은 local/dev/stg/test/prod 전체 Profile과 실제 Consumer까지 확인한다. |
| CR-09 | 오류·경계·UNKNOWN | 정상 경로만으로 완료하지 않고 오류, 경계, 부분실패, UNKNOWN, Timeout을 검증한다. |
| CR-10 | Recovery | Retry/Probe/Recovery/Reconcile/Rollback/Compensation이 필요한 기능은 실제 실패 이후 복구까지 검증한다. |
| CR-11 | 동시성·멱등성 | 중복호출, 동시성, Multi-instance, Process Kill/Takeover 등 해당 기능의 실제 운영 실패모드를 검증한다. |
| CR-12 | Security/Audit | 권한, 승인, Data Scope, Masking, Secret, Audit, 실패 감사 흔적을 기능 영향범위에 포함한다. |
| CR-13 | 최대강도 Test/Runtime | 환경 부족을 이유로 smoke로 축소하지 않는다. 실행 불가 시 BLOCKED_EXTERNAL/NOT_EXECUTED와 정확한 재실행 조건을 남긴다. |
| CR-14 | Evidence 필수 | PASS/CLOSED는 command, environment, start/end, exit code, observed result, Source Identity, Evidence path/hash가 개별 Work Item에 연결될 때만 가능하다. |
| CR-15 | 일괄 상태변경 금지 | 여러 Work Item을 한 문장·한 Evidence로 `모두 완료`, `일괄 PASS`, `일괄 SKIP` 처리하지 않는다. 각 Work Item이 독립 근거를 가진다. |
| CR-16 | 역할 분리 | Developer, Independent Reviewer(Codex/Claude), QA의 수행·판정·Evidence를 분리한다. 한 역할이 다른 역할의 PASS를 대신 기록하지 않는다. |
| CR-17 | Current-only/Garbage | superseded 정본, stale Registry, 임시 산출물, 개발 의존 가비지를 남기지 않는다. 다만 현재 CLOSED 판정이 실제 참조하는 Evidence는 보존한다. |
| CR-18 | Git Write 제한 | 사용자 승인 없는 commit/push/branch/tag/PR/release/reset/restore/stash/clean/history rewrite를 금지한다. |
| CR-19 | Fresh Replay | 수정 후 동일 Source에서 Fresh 초기화/재실행으로 결과가 재현되어야 한다. |
| CR-20 | Final Self Review | 최종 완료 전 모든 Mandatory Work Item을 한 건씩 다시 읽고 요구→Source→Consumer→Test→Runtime→Evidence→Source Identity→상태를 검증한다. |
| CR-21 | Current Prerequisite Source-of-Truth | Java/Node/npm/Python/PowerShell/Docker/DB/Browser 등 실행 prerequisite는 과거 대화·이전 세션·사용자 PC의 현재 설치값을 기준으로 추정하지 않는다. Current Source의 canonical verifier/bootstrap/toolchain/package metadata에서 required 값을 재탐색하고 actual과 대조한 뒤 실행한다. |

`해당 없음(N/A)`은 기능과 정말 무관한 축에만 허용한다. N/A 사유가 없거나 단순 미구현/환경부족을 N/A로 바꾸면 False Green이다.

## 4. Feature Rule

아래 Feature Rule은 Common Rule 전체를 상속하고 각 기능의 **추가 영향범위와 최소 Acceptance**를 정의한다.

| Feature Rule ID | 기능 | 추가 영향범위 / 최소 Acceptance |
| --- | --- | --- |
| FR-BUILD | Java25/Gradle/Build | Current Source toolchain contract에서 prerequisite를 재탐색한 뒤 Fresh compile/test/build/publication, Warning=0, dependency graph, generated output, clean replay. |
| FR-IDE | VS Code/JDT/Buildship | Fresh import, 실제 Workspace Error=0/Warning=0, Gradle model과 JDT model 일치, clean 후 동일 세션 replay. IDE-only fake output/절대 `.classpath` 금지. |
| FR-CORE | Core/Common/Public Contract | Owner/Public API/SPI/Internal 경계, 모든 Starter/Consumer, binary compatibility, generator/sample/EDU 영향. |
| FR-CONFIG | Config/Profile | local/dev/stg/test/prod, default/override, env/property binding, secret, AutoConfiguration, 운영 주의사항. |
| FR-DB3 | DB/Schema/Migration | Canonical Schema → Oracle/PostgreSQL/MariaDB → Migration → Seed → Install/Upgrade → Rollback/Recovery → Runtime Query → Generator → Evidence. 한 Vendor PASS로 전체 PASS 금지. |
| FR-TX | Transaction | begin/commit/rollback, nested/remote boundary, idempotency, unknown outcome, audit/log survival, recovery. |
| FR-LOG | Logging/Trace | Structured/Business/Operation/Security/Audit/Error log, File/DB log, Context/Trace/Metric. 실제 동일 transactionId로 API→Service→Domain/Remote→File Log→DB Log→Trace/instanceId→Error/Recovery 상관검증. |
| FR-MSG | Messaging | producer/consumer, duplicate, ordering, timeout, poison/dead-letter, reconnect, retry, broker/provider failure, idempotency. |
| FR-INT | Integration/Webhook/Remote | timeout, protocol/error mapping, retry, circuit/recovery, partial/UNKNOWN, remote identity/header propagation. |
| FR-CACHE | Cache | get/put/evict/getOrLoad, TTL, invalidation, multi-instance refresh, stale/failure/reconnect/stampede/serialization/provider conflict. |
| FR-SEC | Security | authentication/authorization, approval, Data Scope, masking, secret, audit, deny path, token/session boundary. |
| FR-GW | Gateway | routing, header/context, target operation, timeout/error mapping, security, multi-instance, downstream recovery. |
| FR-ADM | Admin | 운영/관리 Application 경계, owner starter/capability 사용, 권한/승인/audit, DB/runtime 운영 기능. 생성형 업무 Domain 거래 규격을 임의 강제하지 않는다. |
| FR-MBW | Backoffice | Frontend→Channel Server→Gateway→Backoffice→Business Domain 경계, Channel Server DB-less/CPF dependency 0, Backoffice 데이터 ownership. |
| FR-BATCH | Batch | control-plane/scheduler/worker/center-cut/agent, multi-instance, process kill, takeover/fencing, restart/recovery, job/result/audit. |
| FR-GEN | Generator/Generated Domain | template/source contract, 재생성, deterministic output, generated consumer build/runtime, Sample/EDU, stale template 0. |
| FR-API | API/OpenAPI | 실제 Controller/Consumer, header/validation/error, OpenAPI parity, generated client/consumer, backward compatibility. |
| FR-FE | Frontend | build만이 아니라 실제 browser flow, API contract, error/empty/loading, accessibility, UTF-8, user-visible quality. |
| FR-CLI | CLI/Bootstrap/Runtime Tools | Windows/Linux parity, **Current Source에서 파생한 prerequisite required/actual 검증**, timeout, progress, exit code, stdout/stderr, idempotent stop/reset/recovery. |
| FR-OPEN | Open Git/Public Release | Current Release Source/Contract의 prerequisite와 classifier/native-build 조건을 먼저 재탐색한 뒤 Fresh Build/Generation → Binary/Sources/Javadoc/POM/BOM/SBOM/Checksum → Public API/SPI/CLI/Generator/Sample/EDU → Leakage 0 → Fresh Consumer. Master 완료와 별도 Gate. |
| FR-SUPPLY | Supply Chain | artifact catalog, dependency/license/security/SBOM/checksum/provenance, reproducible packaging, leakage. |
| FR-PERF | Performance/Operations | 운영 resource/timeout, load/concurrency, degradation/recovery, metrics/trace, capacity evidence. |
| FR-DOC | Harness/Documentation/Evidence | Current-only, UTF-8/NFC, 정확한 경로/명령, Source Identity, stale path 0, Requirement와 실제 Source 의미 일치. |

## 4.1 Current Prerequisite Source-of-Truth

개발자와 검수자는 실행 명령을 만들 때 다음 순서를 고정한다.

1. 현재 Work Item과 Feature Rule에서 필요한 실행 도구를 식별한다.
2. **Current Source에서** canonical bootstrap/verifier, Gradle toolchain, package metadata/lock, runtime script, DB/runtime contract를 검색해 required version/range/capability를 확정한다.
3. 실제 환경에서 같은 도구의 actual version/path/capability를 측정한다.
4. `required`, `actual`, `prerequisite_source`, 판정(`MATCH/MISMATCH/BLOCKED`)을 session Evidence에 남긴다.
5. MISMATCH이면 Framework 요구조건을 사용자 PC 버전에 맞춰 낮추거나 verifier의 expected 값을 바꿔 PASS시키지 않는다.
6. Canonical bootstrap이 환경 설치/격리를 소유하면 그 경로를 사용한다. 그렇지 않은 전역 Toolchain 변경은 사용자 환경에 side effect가 있으므로 자동 수행을 기본값으로 삼지 않고 정확한 교정 명령·영향·복구 방법을 제시한다.
7. 환경을 교정한 뒤에는 **동일 Current Source의 원래 canonical command**를 처음부터 재실행한다. prerequisite gate만 PASS했다고 Runtime/Test 전체 PASS로 승격하지 않는다.
8. 과거 세션의 required version, 모델의 기억, 대화에 복사된 숫자는 현재 Source 재탐색 없이 실행 기준으로 재사용하지 않는다.

명령을 사용자에게 제공할 때도 가능하면 현재 Source에서 required 값을 읽는 preflight를 명령 앞부분에 포함한다. 다만 Source Contract를 파싱하는 임시 정규식이 canonical toolchain API를 대신하도록 만들지 않는다. 이미 canonical verifier/bootstrap이 있으면 그것을 우선 사용한다.

## 5. Retired/Legacy 경로 해석

`refDB`, `referenceFixture`, retired module/route/DB 이름 등 과거 식별자가 Repository 또는 Harness에 존재한다고 해서 모두 삭제 대상이라고 해석하지 않는다.

허용되는 경우는 **현재 계약에서 retired임을 명시하는 금지 규칙, immutable migration provenance, negative fixture, Finding/Evidence 설명**뿐이다. 반대로 current runtime target, 생성 대상, seed/query target, active config, active consumer, 실행 경로로 해석되거나 다시 생성되면 Mandatory Finding이다.

따라서 검색 결과는 `ACTIVE_CURRENT / RETIRED_PROHIBITION / IMMUTABLE_PROVENANCE / NEGATIVE_TEST / FINDING_EVIDENCE`로 분류한 뒤 처리한다. 문자열이 있다는 이유만으로 released immutable provenance를 변조하지 않고, 빈 retired current 디렉터리·현재 생성 스크립트·현재 실행 설정처럼 의미 없는 active 흔적은 Source Owner가 exact-delete하고 재생성 0을 검증한다.

## 6. 검색과 영향도 추적 방법

작업자는 항상 `CURRENT_WORK_ITEM_REGISTRY.csv`의 Work Item ID에서 시작한다.

정방향 추적:

`Work Item → source_requirement_ids → Product Contract → Root Cause/work_package → development_scope → source_consumer_scope → Static/Runtime Acceptance → Role/Test Ledger → sessionKey Report/Evidence`.

역방향 추적:

`실패 로그/Source/Module/API/DB/Feature → Registry의 source_signal·work_package·development_scope·source_consumer_scope·root_cause_key 검색 → 관련 Parent/Child Work Item → Requirement → Test/Runtime/Evidence`.

검색 시 최소 키는 `work_item_id`, `source_requirement_ids`, `root_cause_key`, `work_package`, `source_consumer_scope`, `priority`, `item_role`, `execution_order`다. 동일 Root Cause가 여러 행에 중복되면 새 Work Item을 계속 추가하기보다 Parent/Child 관계와 동일 Root Cause를 정리한다.

## 7. 작업대상 세부항목 작성 기준

Work Item 한 행은 다음 질문에 독립적으로 답할 수 있어야 한다.

- 무엇을 요구하는가.
- 왜 필요한가 / Root Cause가 무엇인가.
- Canonical Owner는 어디인가.
- 어떤 Source를 수정하는가.
- 실제 Consumer와 호출경로는 무엇인가.
- Config/DB/Generator/API/Frontend 영향은 무엇인가.
- 정상/오류/경계/UNKNOWN/복구는 무엇을 확인하는가.
- 어떤 Test와 Physical Runtime이 Mandatory인가.
- Side Effect/Regression 범위는 어디까지인가.
- 어떤 Evidence로 완료를 증명하는가.
- Source가 바뀌면 무엇을 재실행해야 하는가.

하나의 행이 위 질문에 답할 수 없을 정도로 크면 세부 Child Work Item으로 분해한다. 반대로 같은 Root Cause와 동일 Acceptance를 단순 파일 수만큼 쪼개 개수를 부풀리지 않는다.

## 8. 완료 해석 금지사항

다음 문구는 독립 근거 없이 완료 사유로 사용할 수 없다.

- `전체 완료`, `일괄 완료`, `동일`, `기존과 동일`, `문제 없음`, `검증 완료`, `기존 실패`, `다른 작업자 문제`, `범위 외`, `환경 문제`, `추후 처리`, `skip`.

해당 표현이 필요하면 반드시 **어떤 Work Item에 대해, 어떤 명령·환경·실제 결과·Source Identity·Evidence로 그 판정을 했는지** 같은 행 또는 같은 세션 Report Block에서 명시한다.
