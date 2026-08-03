# QA 재개발·재검수 요청

- QA 기준 SHA: `2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca`
- 회차: `QA-DEV-R1`
- QA 결과: `S4-001~S4-009 전부 미통과`
- 이 문서는 개발 GPT와 Codex가 같은 Requirement ID로 재개발·재검수하도록 작성한 단일 요청서다.
- QA 통과 전 `development_status=완료`, `verification_status=완료` 사용을 금지한다.

## QA-DEV-S4-001 — 개발 Evidence와 실행 원장이 실제 Push SHA가 아닌 이전 기준 SHA를 검증 대상으로 기록

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-001..009`
- 대상 역할: `Codex`
- 분류: `STALE_EVIDENCE`
- 근거: 최신 master는 2903de14...이나 JAVA21_SUBSTITUTE_VALIDATION.json, FRONTEND_SUBSTITUTE_VALIDATION.json, EXECUTION_LEDGER.csv 등은 verifiedAgainstSha=d2adc89f...를 기록한다.
- 영향: 최신 Git 실제 구현과 Evidence의 exact-SHA 정합성이 성립하지 않아 기존 PASS를 QA 통과 근거로 자동 승계할 수 없음
- 요청 작업: 최신 master SHA에서 각 Gate와 대체검증을 재실행하거나, 동일 파일 Hash를 검증하는 재현 가능한 Exact-SHA Evidence를 Codex 영역에 새로 작성
- 재실행 명령: `git rev-parse HEAD && git status --short; 각 Python/Java21/Node/Chromium Gate를 HEAD=2903de14...에서 재실행`
- 성공 기대 결과: 모든 Evidence의 verifiedAgainstSha가 실제 검수 후보 HEAD와 일치하고 Working Tree 및 명령 원문이 기록됨
- 실패 판정 기준: 이전 SHA 유지, 실행 미수행, Overlay 경로만 기록, 최신 Git 파일 Hash와 불일치
- 요구 Evidence: 명령 Transcript, exact HEAD/status, JSON/로그, 대상 파일 SHA-256

## QA-DEV-S4-002 — Codex Java25/Gradle 이관 명령의 ExpectedSourceSha가 Push 전 SHA로 고정

- 심각도: `HIGH`
- 대상 Requirement: `CPF-XFER-S4-JAVA25`
- 대상 역할: `Codex`
- 분류: `INVALID_RERUN_COMMAND`
- 근거: ENVIRONMENT_VALIDATION_HANDOFF.csv의 명령은 -ExpectedSourceSha d2adc89f...를 사용한다. verify-cpf-final-completion.ps1은 그 SHA 이후 Source 변경이 있으면 실패하도록 구현됐다.
- 영향: Codex가 지시된 명령을 그대로 실행하면 최신 2903de14...의 Source 변경을 감지하고 검증 시작 단계에서 실패
- 요청 작업: Codex 검수 후보 SHA를 조회한 뒤 그 exact SHA로 명령과 모든 Evidence 경로를 갱신
- 재실행 명령: `pwsh -NoProfile -File .\cpf-tools\scripts\verify-cpf-final-completion.ps1 -RepoRoot . -ExpectedSourceSha <CODEX_CANDIDATE_EXACT_SHA> ...`
- 성공 기대 결과: ExpectedSourceSha와 HEAD가 동일하고 Source-after-baseline 차단 오류 없이 Gate가 실행됨
- 실패 판정 기준: d2adc89f... 고정 유지 또는 ancestor SHA를 사용한 채 최신 Source를 검증 완료로 주장
- 요구 Evidence: 수정된 명령, exact HEAD/status, 실행 Transcript

## QA-DEV-S4-003 — Package Manifest와 원장이 포함됐다고 주장한 핵심 로그가 최신 Git에 존재하지 않음

- 심각도: `CRITICAL`
- 대상 Requirement: `CPF-SELF-DEV-S4-002`
- 대상 역할: `개발GPT/Codex`
- 분류: `MISSING_GIT_EVIDENCE`
- 근거: PACKAGE_MANIFEST.json과 CHANGE_MANIFEST.csv는 P00_P05_MERGED_REGRESSION_TESTS_R2.log, PYTHON_COMPILEALL_R2.log, pre-fix-split-master-failure.log 등을 포함 파일로 기록하지만 최신 master 조회 결과 404.
- 영향: 70건 회귀 PASS 및 사전 실패/복구 근거를 Git 정본에서 재현·감사할 수 없음
- 요청 작업: 누락 Evidence를 Git 추적 가능한 경로에 추가하거나 Codex가 최신 SHA에서 다시 실행하여 자신의 Evidence로 제출. Package/Change/File Hash Manifest도 실제 Git 파일 집합으로 재생성
- 재실행 명령: `python -m unittest discover -s cpf-tools/scripts/tests -p "test_*.py" > <codex-evidence>/P00_P05_REGRESSION.log 2>&1`
- 성공 기대 결과: 실제 Git에서 로그 조회 가능, 테스트 수·이름·결과·exit code·HEAD가 일치
- 실패 판정 기준: Manifest에만 존재, 404, 요약 문자열만 있고 원 실행 로그/테스트 목록 없음
- 요구 Evidence: Git 추적 로그, exit code, 테스트 목록, 파일 SHA-256, Package Manifest

## QA-DEV-S4-004 — P02 Owner Boundary Gate가 Requirement에서 요구하는 전체 경계를 전수검사하지 않음

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-003`
- 대상 역할: `개발GPT`
- 분류: `INCOMPLETE_OWNER_GATE`
- 근거: verify-cpf-owner-boundaries.py는 ADM의 특정 DB 문자열, BAT의 제한된 Core Runtime import, cpf-core build.gradle의 cpf-batch 의존 일부만 검사한다. 전체 Module dependency, Public API/SPI/Internal 참조, 순환·역방향 의존, ownerless common, 실제 Consumer는 검사하지 않는다.
- 영향: Gate PASS여도 Repository 전체 Architecture Ownership 위반이 남을 수 있음
- 요청 작업: settings/build dependency graph와 모든 main Source import를 대상으로 공식 Module·Package Ownership/Public/SPI/Internal/순환/역방향/Consumer 규칙을 전수검사하도록 Gate와 Negative Test 보강
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-owner-boundaries.py --root . --json-output <evidence>/owner-boundaries.json`
- 성공 기대 결과: 검사 파일 수·Module graph·모든 위반 유형·finding 0이 Evidence에 기록
- 실패 판정 기준: 현재 제한 패턴만 검사하거나 일부 Module/Package/Consumer를 제외
- 요구 Evidence: 전수 Scan 목록, dependency graph, JSON finding, Negative Test

## QA-DEV-S4-005 — P03 Transaction 전수 Gate와 이관 성공기준이 서로 불일치

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-005`
- 대상 역할: `개발GPT`
- 분류: `INCOMPLETE_TRANSACTION_GATE`
- 근거: 이관 원장은 legacy globalId/gid runtime use가 없어야 한다고 명시하지만 verify-cpf-transaction-id-standard.py는 구형 실행 ID 문자열 패턴과 지정 Annotation을 중심으로 검사하며 globalId/gid 식별자 전수검사 규칙이 없다.
- 영향: Legacy 거래 식별자나 중복 계약이 Gate PASS 뒤에도 남을 수 있음
- 요청 작업: Java/SQL/API/OpenAPI/Frontend/Config의 transactionId/globalId/gid 별칭과 Annotation 중복·오용을 전체 Repository에서 검사하고 허용 Migration/alias 구역만 명시적 Allowlist로 제외
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-transaction-id-standard.py --root . --json-output <evidence>/transaction-id-standard.json`
- 성공 기대 결과: 스캔 파일 목록, Annotation 수, legacy 별칭 finding 0, 허용 제외 근거가 기록
- 실패 판정 기준: 구형 ID 문자열만 검사하거나 globalId/gid 식별자 검사가 없음
- 요구 Evidence: 전수 Scan JSON, Allowlist, Negative Test, exact SHA

## QA-DEV-S4-006 — BZA 공용 API가 ADM 구현과 동일 파일로 치환되며 기존 bza* 공개 함수가 제거됨

- 심각도: `CRITICAL`
- 대상 Requirement: `CPF-SELF-DEV-S4-006`
- 대상 역할: `개발GPT`
- 분류: `BZA_PUBLIC_API_REGRESSION`
- 근거: Commit diff에서 bzaQuery/bzaMutation/bzaApi/bzaInvokeOperation이 admQuery/admMutation/admApi/admInvokeOperation으로 변경됐고 BZA 파일은 ADM 파일과 동일 Hash다. 같은 Commit에는 BZA Consumer 수정이 없다. 대체검증은 import를 제거하고 Stub Harness가 새 adm* 함수만 호출한다.
- 영향: 기존 BZA 화면·생성 Client import가 Compile 또는 Runtime에서 깨질 가능성이 높고 BZA/ADM 경계가 손상됨
- 요청 작업: 기존 BZA Public API를 유지하거나 명시적 호환 Alias를 제공하고 실제 BZA 전체 Consumer를 TypeScript로 Compile. ADM/BZA 메시지·Operation naming·Same-origin 정책도 각 제품 경계에 맞게 복원
- 재실행 명령: `BZA 실제 frontend project에서 typecheck/build/unit 실행; repository 전체에서 bzaQuery,bzaMutation,bzaApi,bzaInvokeOperation import/호출 전수 Scan`
- 성공 기대 결과: 기존 BZA Consumer가 수정 없이 호환되거나 모든 Consumer가 의도적으로 Migration되어 Build PASS
- 실패 판정 기준: Stub Harness만 PASS, 기존 export 제거, BZA 파일이 ADM 파일과 무의미하게 동일
- 요구 Evidence: Consumer 목록, TypeScript Build 로그, API export compatibility test

## QA-DEV-S4-007 — Frontend Actor Body Guard가 string/FormData/URLSearchParams/Blob 본문을 검사하지 않음

- 심각도: `MEDIUM`
- 대상 Requirement: `CPF-SELF-DEV-S4-006`
- 대상 역할: `개발GPT`
- 분류: `ACTOR_GUARD_BYPASS`
- 근거: assertNoClientActor는 object 속성만 검사하고 admRawResponse는 string/FormData/Blob/URLSearchParams를 그대로 전송한다. Node/Chromium Harness는 중첩 JSON 객체만 검증한다.
- 영향: Form 또는 Raw Body 기반 호출 경로가 존재하면 requestedBy 등 Actor alias 차단을 우회할 수 있음
- 요청 작업: 지원 Body 타입별 정책을 명시하고 FormData/URLSearchParams의 key를 검사. 문자열 JSON은 parse 후 검사하거나 raw body를 Privileged API에서 금지
- 재실행 명령: `Node 및 Chromium Harness에 5개 alias × Raw/FormData/URLSearchParams Negative Case 추가`
- 성공 기대 결과: 모든 지원 Body 형식에서 Actor alias가 Consumer 호출 전에 차단
- 실패 판정 기준: 객체 JSON만 검사하거나 Raw/Form body가 그대로 전송
- 요구 Evidence: Body type matrix, Node/Chromium Test 결과, 실제 Consumer capture

## QA-DEV-S4-008 — Batch Actor Test가 Controller endpoint/Consumer 전체 호출 경로를 검증하지 않음

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-006`
- 대상 역할: `개발GPT`
- 분류: `INSUFFICIENT_CONTROLLER_TEST`
- 근거: 단위 Test와 Java21 Harness는 private withServerActor를 reflection으로 직접 호출한다. save/transition/command/plan endpoint의 RequestAttribute, validation, status, client capture는 실행하지 않는다.
- 영향: 실제 Spring binding·예외 분류·모든 mutation 경로의 Sanitizer 적용 회귀를 검출하지 못함
- 요청 작업: Controller를 실제 Client spy/fake와 구성하여 4개 privileged endpoint별 정상·누락 actor·중첩 alias·client exception·invalid request 상태를 검증
- 재실행 명령: `Java21 독립 Harness 또는 실제 JUnit에서 Controller method와 BatchRuntimeControlClient spy 실행`
- 성공 기대 결과: 모든 mutation endpoint가 인증 actor만 Owner로 전달하고 기대 HTTP 상태를 반환
- 실패 판정 기준: private helper reflection만 테스트하거나 일부 endpoint 미검사
- 요구 Evidence: Endpoint별 Test Matrix, captured request, status/result log

## QA-DEV-S4-009 — Batch mutation endpoint의 IllegalArgumentException 처리 방식이 일관되지 않음

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-006`
- 대상 역할: `개발GPT`
- 분류: `ERROR_CLASSIFICATION`
- 근거: command는 IllegalArgumentException을 400으로 처리하지만 saveJobDefinition/transitionJobDefinition은 BatchControlClientException만 catch한다. plan은 모든 RuntimeException을 503 UNKNOWN_RESULT로 변환한다.
- 영향: 클라이언트 입력 오류가 500/503 또는 UNKNOWN_RESULT로 잘못 분류되어 운영자 재시도·감사·복구 판단을 왜곡
- 요청 작업: 모든 mutation에서 Validation/Conflict/Unknown/Unavailable을 일관된 Error Contract로 매핑하고 Test 추가
- 재실행 명령: `각 endpoint에 누락 필드·blank actor·invalid expectedVersion·client unavailable Test 실행`
- 성공 기대 결과: Validation=400, Permission=403, Conflict=409, Unknown/Unavailable만 정의된 5xx로 반환
- 실패 판정 기준: IllegalArgumentException이 500/503으로 누출되거나 입력 오류가 UNKNOWN_RESULT로 변환
- 요구 Evidence: Endpoint Error Matrix, Test 결과, OpenAPI error schema

## QA-DEV-S4-010 — QA 통과 전에 checkpoint Requirement의 development_status/verification_status를 완료로 기록

- 심각도: `CRITICAL`
- 대상 Requirement: `CPF-SELF-DEV-S4-001..009`
- 대상 역할: `개발GPT`
- 분류: `INVALID_STATUS_TRANSITION`
- 근거: REQUIREMENT_STATUS.csv의 여러 행에서 development_status=완료 및 verification_status=완료를 기록한다. CPF 지침은 QA 통과 전 두 전체 상태의 완료 사용을 금지한다.
- 영향: 개발 자체 PASS가 QA 완료로 오인되고 단일 Requirement 원장의 상태 통제가 무너짐
- 요청 작업: QA 통과 전 development_status는 부분 구현/재확인 필요, verification_status는 미검증/재확인 필요로 정정. 개발GPT 역할 상태와 전체 상태를 분리
- 재실행 명령: `상태 CSV Schema/Allowed-value Gate 실행`
- 성공 기대 결과: QA 통과 전 전체 완료 0건이며 역할별 상태만 개발 완료로 기록
- 실패 판정 기준: QA 결과 없이 development_status 또는 verification_status=완료
- 요구 Evidence: 수정 전후 Status Delta, Schema validation 결과

## QA-DEV-S4-011 — 실행 원장 다수 명령이 placeholder라 재현 가능한 exact command가 아님

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-001..009`
- 대상 역할: `개발GPT/Codex`
- 분류: `NON_REPRODUCIBLE_COMMAND`
- 근거: EXECUTION_LEDGER.csv의 P03/P04/P05 행은 <corresponding-gate>.py, <targeted-exact-sha-fixture>, <evidence> 같은 placeholder를 사용한다.
- 영향: Codex와 QA가 동일 검증을 그대로 재실행할 수 없고 Evidence 출처를 감사할 수 없음
- 요청 작업: 실제 스크립트 경로, root, output path, exact SHA, exit code를 포함한 완전한 명령으로 교체
- 재실행 명령: `각 행별 실제 명령을 실행 원문 그대로 기록`
- 성공 기대 결과: 복사 실행 가능한 명령이며 같은 SHA에서 동일 결과 재현
- 실패 판정 기준: placeholder, fixture 경로 미상, 출력 경로/HEAD 누락
- 요구 Evidence: 명령 Transcript, 환경 버전, exact SHA, output hash

## QA-DEV-S4-012 — Split Master Gate가 verified SHA를 과거 Commit으로 하드코딩

- 심각도: `CRITICAL`
- 대상 Requirement: `CPF-SELF-DEV-S4-001`
- 대상 역할: `개발GPT`
- 분류: `HARDCODED_EVIDENCE_SHA`
- 근거: verify-cpf-split-master-dataset.py의 VERIFIED_AGAINST_SHA가 a6856e7557f586875796172ac6ebae22bb87958e로 고정되어 있고 summary 및 canonical manifest 갱신에 그대로 사용된다. 반면 제출 Evidence는 d2adc89f...로 기록되어 committed script의 직접 산출물과 일치하지 않는다.
- 영향: 실행 대상 Git SHA를 검증하지 못하고 Evidence가 실행 후 수동 수정됐을 가능성을 배제할 수 없어 Provenance가 깨짐
- 요청 작업: 하드코딩을 제거하고 --expected-sha 또는 git rev-parse HEAD를 필수 입력으로 사용. Git working tree가 없으면 PASS가 아니라 미검증/실패로 처리하고 최신 SHA에서 Evidence 재생성
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-split-master-dataset.py --root . --expected-sha <EXACT_HEAD> --json-output <evidence>`
- 성공 기대 결과: Script가 exact HEAD를 직접 확인하고 JSON·지원 Manifest 모두 같은 SHA를 기록
- 실패 판정 기준: 상수 SHA 사용, Git 없는 Overlay에서 PASS, 산출 JSON을 사후 수동 수정
- 요구 Evidence: 수정 Script, exact HEAD/status, 원본 명령 Transcript, 재생성 JSON Hash

## QA-DEV-S4-013 — Split Gate가 execution_order의 형식·정렬·연속성을 검증하지 않음

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-001`
- 대상 역할: `개발GPT`
- 분류: `INCOMPLETE_SEQUENCE_VALIDATION`
- 근거: validate_canonical_id_continuity는 Requirement와 Scenario만 검사하며 execution에는 적용하지 않는다. Requirement/Scenario도 ID 하나가 형식에 맞지 않으면 전체 continuity 검사를 return으로 건너뛴다. build_scope는 현재 파일 순서의 앞 N행을 그대로 신뢰한다.
- 영향: 실행순서 누락·역전·비정상 ID가 있어도 Scope 1~10,027 및 다음 10,028~20,000 경계가 잘못 계산될 수 있음
- 요청 작업: execution_order canonical parser, phase/order 순서, 중복·누락·역전, Work Package 경계를 전수검증. Requirement/Scenario의 비정상 ID는 continuity 생략이 아니라 즉시 실패
- 재실행 명령: `Split Gate Negative Test에 malformed ID, missing execution, reversed rows, duplicate order 추가`
- 성공 기대 결과: 모든 ID 형식과 논리 순서가 엄격히 검증되고 Scope 경계가 Work Package와 함께 Evidence에 기록
- 실패 판정 기준: 비정상 ID가 continuity 검사 생략을 유발하거나 파일 행 순서를 무조건 신뢰
- 요구 Evidence: ID/sequence 검사 결과, Negative Test, first/last/work-package Evidence

## QA-DEV-S4-014 — P01 Traceability PASS가 Git HEAD를 확인하지 않은 Overlay 실행 결과

- 심각도: `CRITICAL`
- 대상 Requirement: `CPF-SELF-DEV-S4-002`
- 대상 역할: `Codex`
- 분류: `TRACEABILITY_WITHOUT_GIT`
- 근거: P01_REQUIREMENT_TRACEABILITY_R2.json은 status=PASS이지만 head=UNAVAILABLE, result_matrix rows=0/completed=0/verified=0을 기록한다. verifiedAgainstSha=d2adc89f...는 Gate의 실제 git check 결과가 아니다.
- 영향: 최신 Git exact SHA, Clean Working Tree, Requirement 결과 원장, 완료 Evidence를 전혀 검증하지 않은 상태에서 PASS로 오인
- 요청 작업: Codex가 최신 Repository working tree에서 --expected-sha <HEAD> --require-clean으로 실행하고 실제 단일 Result Matrix 경로를 지정해 행·완료·검증 Coverage를 확인
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-requirement-traceability.py --root . --expected-sha <CODEX_HEAD> --require-clean --result-matrix <ACTIVE_SINGLE_REQUIREMENT_LEDGER> --json-output <evidence>`
- 성공 기대 결과: head가 exact SHA이고 Result Matrix 행 수와 완료/검증 수가 실제 원장과 일치
- 실패 판정 기준: head=UNAVAILABLE, rows=0, Overlay fixture만 사용하거나 SHA 필드를 사후 추가
- 요구 Evidence: Git HEAD/status, 원장 경로·Hash, Traceability JSON, 명령 Transcript

## QA-DEV-S4-015 — Traceability main flow가 결과 원장 부재·0행을 정상 PASS로 허용

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-002`
- 대상 역할: `개발GPT`
- 분류: `OPTIONAL_SPARSE_RESULT_MATRIX`
- 근거: check_optional_result_matrix는 파일이 없으면 rows=0을 반환하고, main flow는 이를 PASS로 인정한다. 또한 sparse 행만 검사하므로 30,558 Requirement 전체 결과 Coverage를 강제하지 않는다.
- 영향: 단일 Requirement 원장이 없거나 비어 있어도 Traceability PASS가 가능해 완료·미완료·Evidence 상태를 검증하지 못함
- 요청 작업: 개발 Checkpoint와 Release 모드를 분리하되 활성 단일 Requirement 원장은 항상 필수로 읽고, Scope 내 Requirement Coverage와 필수 역할 컬럼을 강제. sparse 보조 원장은 별도 명칭으로 분리
- 재실행 명령: `Result Matrix 누락·0행·일부행 Negative Test와 Scope Coverage Test 실행`
- 성공 기대 결과: 활성 원장 누락/0행/Scope 누락 시 fail-closed
- 실패 판정 기준: rows=0을 PASS하거나 일부 완료 행만 검사하고 Canonical Scope Coverage 미검사
- 요구 Evidence: 원장 Schema/Coverage JSON, Negative Test, Scope별 누락 목록

## QA-DEV-S4-016 — Owner Gate가 공식 Settings/Surface Policy/전체 Build Graph를 사용하지 않음

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-003`
- 대상 역할: `개발GPT`
- 분류: `OWNER_GATE_GRAPH_GAP`
- 근거: Settings에는 Batch testkit 포함 9개 Module과 다수 제품/Starter Project가 있으나 Gate는 8개 Batch root와 제한 문자열만 검사한다.
- 영향: 역방향·순환·Internal 외부 참조·Ownerless 공통·Consumer 없는 Public Contract가 PASS 뒤에 남을 수 있음
- 요청 작업: Settings/모든 Build/모든 main Source를 읽는 dependency+package graph Gate로 재개발
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-owner-boundaries.py --root . --json-output <evidence>`
- 성공 기대 결과: 공식 Owner/Package/Public/SPI/Internal/순환/Consumer Finding 0
- 실패 판정 기준: 제한 문자열 패턴·일부 Module만 검사하거나 testkit/Starter/BZA 제외
- 요구 Evidence: Module graph, scanned files, violations JSON, Negative Test, exact SHA

## QA-DEV-S4-017 — DB-less Gate와 Test가 실제 Spring Context·Consumer·DB 실패 경로를 실행하지 않음

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-004`
- 대상 역할: `개발GPT`
- 분류: `STATIC_TOKEN_ONLY_VALIDATION`
- 근거: Gate는 4개 Source의 문자열/정규식만 확인하며 Test도 축약 문자열 Fixture만 사용한다.
- 영향: Bean wiring·Profile 충돌·URL/JNDI 실패·Memory fallback 회귀를 검출하지 못해 Product fail-closed를 증명할 수 없음
- 요청 작업: Resolver/Consumer 전수 확인 후 Java21 Context 또는 독립 Harness와 실제 Service fallback Test 작성
- 재실행 명령: `Java21에서 profile matrix와 missing datasource/JNDI failure harness 실행`
- 성공 기대 결과: Product DB 실패가 fail-closed이며 edu/test 외 Memory Bean이 생성되지 않음
- 실패 판정 기준: 문자열 Gate만 PASS하거나 실제 Consumer/Context 미검사
- 요구 Evidence: Profile matrix, bean graph, exception result, Consumer path, exact SHA

## QA-DEV-S4-018 — 표준 실행 Annotation 0건인데 Gate PASS하며 미부착 Endpoint는 거래 Header 검증을 우회

- 심각도: `CRITICAL`
- 대상 Requirement: `CPF-SELF-DEV-S4-005`
- 대상 역할: `개발GPT`
- 분류: `ZERO_ANNOTATION_PASS`
- 근거: P03_TRANSACTION_ID_R2_TARGETED.json은 executionAnnotationCount=0/status=PASS다. TransactionHeaderValidationInterceptor는 resolveTransactionAnnotation 결과가 null이면 true를 반환한다.
- 영향: 실제 업무 Controller에 Annotation이 없으면 X-Transaction-Id와 표준 실행 ID 검증이 전체적으로 적용되지 않아 추적·감사·호출계약이 무력화됨
- 요청 작업: 업무 Controller/Operation 전수 목록과 Annotation Coverage를 대조하고 미부착 업무 Endpoint를 실패시키며 Health/Swagger/Callback만 근거 있는 Allowlist로 제외
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-transaction-id-standard.py --root . --json-output <evidence>/transaction-id-standard.json`
- 성공 기대 결과: 업무 Endpoint Annotation 누락 0, 제외 Endpoint Allowlist 일치, Annotation/Operation Coverage가 Evidence에 기록
- 실패 판정 기준: Annotation 0건 또는 업무 Endpoint 미부착을 PASS하거나 Interceptor가 조용히 검증을 생략
- 요구 Evidence: Controller/Route/Operation 목록, Annotation Coverage CSV, Negative Test, exact SHA

## QA-DEV-S4-019 — Operator Trust PASS가 실제 전체 App이 아닌 Frontend 2개·Controller 1개와 임시 Stub Harness에 한정

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-006`
- 대상 역할: `개발GPT/Codex`
- 분류: `TARGETED_STUB_EVIDENCE`
- 근거: Targeted Evidence는 frontendSourceCount=2/controllerSourceCount=1이다. Java21은 Batch Controller/Test만 임시 Spring/JUnit Stub으로 Compile하고 Frontend는 공유 cpfApi.ts만 임시 Dependency Stub으로 실행한다.
- 영향: Auth Filter→adm.operatorId→모든 위험 Controller→Owner→Audit 전체 경로와 기존 BZA Consumer 호환성을 증명하지 못함
- 요청 작업: 실제 ADM/BZA Project 전체 Build와 위험 Mutation Endpoint별 인증 Actor/Audit Actor E2E Test를 실행
- 재실행 명령: `./gradlew :cpf-admin:test :cpf-biz-admin:test :cpf-admin:frontendVerify :cpf-biz-admin:frontendVerify`
- 성공 기대 결과: 실제 전체 Consumer Compile PASS, 인증되지 않은 Actor 호출 0, Owner/Audit Actor 일치
- 실패 판정 기준: 공유 파일·Private Helper·Stub Harness만 실행하거나 전체 App Consumer를 제외
- 요구 Evidence: 전체 Build/Test 로그, Endpoint Matrix, Actor/Audit 결과, exact SHA

## QA-DEV-S4-020 — HTTP Client Endpoint Registry가 DNS Address 검증·Pin 없이 Hostname URL을 반환

- 심각도: `CRITICAL`
- 대상 Requirement: `CPF-SELF-DEV-S4-007`
- 대상 역할: `개발GPT`
- 분류: `HTTP_CLIENT_DNS_REBINDING_GAP`
- 근거: CpfServiceEndpointRegistry.validatedBaseUrl은 policy.validateEndpoint(v)만 호출하고 URL을 반환한다. DNS resolve, validateResolvedAddresses, pinned connection이 없다. Gate는 이 Consumer에 해당 호출을 요구하지 않으면서 dnsRebinding=true를 기록한다.
- 영향: HTTP Client Consumer에서 검증 이후 DNS 응답 변경 또는 Rebinding을 통해 Private/Metadata 주소로 연결될 수 있고 공통 Network Policy 적용 완료 주장이 거짓 양성이 됨
- 요청 작업: 실제 HTTP Transport 연결 직전에 DNS Resolve·Address 검증·Pinned Connection을 구현하고 Gateway/Host Agent와 동등한 정책을 적용
- 재실행 명령: `./gradlew :cpf-starter-integration-http-client:test :cpf-gateway:test :cpf-batch:host-agent:test`
- 성공 기대 결과: DNS Rebinding/Mixed DNS/Address 변경/Metadata 주소 Test가 모두 차단되고 검증 Address에 실제 연결
- 실패 판정 기준: Hostname 문자열 검증만 수행하거나 검증한 Address와 실제 연결 Address가 다름
- 요구 Evidence: DNS Resolver Test, Socket target capture, Consumer별 policy path, exact SHA

## QA-DEV-S4-021 — Durable Audit PASS가 Source Token Fixture뿐이며 실제 DB·다중 인스턴스·Process Kill Evidence가 없음

- 심각도: `MEDIUM`
- 대상 Requirement: `CPF-SELF-DEV-S4-007`
- 대상 역할: `Codex/외부환경`
- 분류: `AUDIT_RUNTIME_EVIDENCE_MISSING`
- 근거: Gate는 AdmAuditLogService와 AdmAuditDeliveryService의 문자열 순서와 Token을 검사한다. 제출 Evidence도 exact-SHA source fixture라고 기록하며 실제 Vendor DB 실행 결과가 없다.
- 영향: REQUIRES_NEW reservation, UNKNOWN recovery, FOR UPDATE 중복 방지가 실제 3 Vendor와 부분 실패에서 동작하는지 QA 통과 판정 불가
- 요청 작업: MariaDB/PostgreSQL/Oracle에서 2개 ADM 인스턴스, Owner 실행 전후 Process Kill, Relay Retry/Exhaustion을 실행
- 재실행 명령: `./gradlew :cpf-admin:test --tests '*Audit*Runtime*' -Dcpf.qa.runtime.enabled=true`
- 성공 기대 결과: Reservation 유실 0, UNKNOWN 대사 가능, 중복 Audit 0, 실패와 정상 0건 구분
- 실패 판정 기준: Source Token Test만 제출하거나 실제 DB/다중 인스턴스/Process Kill 미실행
- 요구 Evidence: DB별 Transcript, row snapshot, process timeline, exact SHA

## QA-DEV-S4-022 — PostgreSQL과 Oracle Fresh Install SQL에 MariaDB 전용 LONGBLOB·MEDIUMTEXT 타입 포함

- 심각도: `CRITICAL`
- 대상 Requirement: `CPF-SELF-DEV-S4-008`
- 대상 역할: `개발GPT`
- 분류: `INVALID_VENDOR_SQL_TYPES`
- 근거: postgresql/install/00_empty_install.sql과 oracle/install/00_empty_install.sql의 cpf_broker_outbox에 payload LONGBLOB, header_json MEDIUMTEXT, attribute_json MEDIUMTEXT가 생성돼 있다.
- 영향: PostgreSQL/Oracle Fresh Install이 문법 오류로 실패하므로 3 Vendor Lifecycle 구현 완료가 아니며 환경 문제가 아닌 Source/Generator 결함
- 요청 작업: Canonical Type Mapping을 수정해 PostgreSQL은 BYTEA/TEXT 계열, Oracle은 BLOB/CLOB 계열로 재생성하고 모든 Lifecycle SQL을 재검사
- 재실행 명령: `pwsh -NoProfile -File cpf-tools/scripts/build-all-install-sql.ps1; 3 Vendor fresh install/verify/rollback 실행`
- 성공 기대 결과: 공식 Vendor SQL에 타 Vendor 전용 타입 0건, 3 Vendor Fresh Install 성공
- 실패 판정 기준: PostgreSQL/Oracle SQL에 LONGBLOB/MEDIUMTEXT 잔존 또는 실제 Install Syntax 오류
- 요구 Evidence: Generator diff, SQL token scan, DB별 install log, schema metadata

## QA-DEV-S4-023 — DB Vendor Gate가 Lifecycle SQL 내용·Dialect·Parity·Rollback을 검사하지 않고 경로 존재만 PASS

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-008`
- 대상 역할: `개발GPT`
- 분류: `DB_GATE_PATH_ONLY`
- 근거: verify-cpf-db-vendor-manifest.py는 exactly-three metadata와 36개 path/directory 존재만 확인한다. Test도 빈 SQL 파일을 materialize해 PASS한다.
- 영향: 실제 Vendor 문법 오류·DDL Drift·누락 Index/FK·잘못된 Rollback이 있어도 PASS
- 요청 작업: Vendor SQL Parse/Forbidden Token/Canonical Metadata Parity/Migration Checksum/Rollback 역연산 Gate를 추가
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-db-vendor-manifest.py --root . --json-output <evidence>/db-vendor.json`
- 성공 기대 결과: SQL file count/hash/object parity/dialect finding/rollback drift가 Vendor별 Evidence에 기록
- 실패 판정 기준: Path 존재와 Manifest 문자열만 검사하거나 실제 SQL 내용을 읽지 않음
- 요구 Evidence: SQL scan manifest, parser result, object parity, rollback result, exact SHA

## QA-DEV-S4-024 — Starter Catalog packageBase와 실제 Persistence Product Config Package가 불일치

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-009`
- 대상 역할: `개발GPT`
- 분류: `CATALOG_PACKAGEBASE_DRIFT`
- 근거: Catalog의 JDBC/MyBatis packageBase는 com.cpf.starter.data.persistence.*이나 CmnDataSourceConfig/CmnSampleDataSourceConfig/CmnMyBatisConfig 실제 package는 com.cpf.common.config다. Gate는 Source Package를 스캔하지 않는다.
- 영향: Catalog가 Package Ownership의 정본 역할을 하지 못하고 Generator/문서/Public-Internal 경계가 잘못 파생될 수 있음
- 요청 작업: Catalog PackageBase 의미를 명확히 하고 실제 Source를 이동하거나 Catalog에 Owner/Export Package를 정확히 분리해 전수 검증
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-starter-catalog-truth.py --root . --json-output <evidence>/starter-catalog.json`
- 성공 기대 결과: 38개 Module의 Catalog Package와 실제 main Source Package Drift 0
- 실패 판정 기준: Directory/build.gradle 존재만 검사하고 실제 Java Package Ownership 미검사
- 요구 Evidence: Module별 Source Package 목록, Catalog diff, Owner boundary result

## QA-DEV-S4-025 — Starter Catalog baselineSha가 최신 Git과 다르며 Gate가 exact HEAD 정합성을 강제하지 않음

- 심각도: `HIGH`
- 대상 Requirement: `CPF-SELF-DEV-S4-009`
- 대상 역할: `개발GPT/Codex`
- 분류: `STALE_CATALOG_BASELINE`
- 근거: Catalog baselineSha=4aea798c..., 제출 Evidence verifiedAgainstSha=d2adc89f344fa1f93a2f9291f6576ce69be05239, 실제 QA HEAD=2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca다. Gate는 catalog_baseline_sha를 출력만 한다.
- 영향: Catalog·Settings·BOM·38개 Module이 어느 Git 정본을 기준으로 생성·검증됐는지 증명할 수 없음
- 요청 작업: Catalog Revision/Source SHA를 최신 후보 SHA와 동기화하고 Gate에서 expected-sha/clean-tree를 필수 검증
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-starter-catalog-truth.py --root . --expected-sha <EXACT_HEAD> --json-output <evidence>/starter-catalog.json`
- 성공 기대 결과: Catalog baseline/revision, Git HEAD, Evidence SHA가 동일하고 38개 physical/publication contract가 일치
- 실패 판정 기준: 과거 baseline 유지, Gate가 SHA를 출력만 하거나 Overlay Fixture를 Git 검증으로 주장
- 요구 Evidence: exact HEAD/status, Catalog diff, Gate JSON, 38개 Artifact manifest
