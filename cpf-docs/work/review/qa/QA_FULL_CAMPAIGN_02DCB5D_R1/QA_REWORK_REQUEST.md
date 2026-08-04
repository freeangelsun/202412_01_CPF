# QA 재개발 요청서

- QA 회차: `QA_FULL_CAMPAIGN_02DCB5D_R1`
- 기준 SHA: `02dcb5d45646469f4950cf43c371706e00458616`
- 대상 역할: 개발GPT
- 원칙: 동일 Finding ID로 구현·자체검수 후 Codex 교차검토와 독립 QA 재검수

## QA-FULL-001 — P0 — ADM 승인 Controller 7개 중복 Handler Mapping

- 처리: **개발GPT 재개발**
- 근거: cpf-admin/.../approval/controller/AdmApprovalController.java; cpf-admin/.../opr/controller/AdmApprovalController.java
- 현재 동작: 두 @RestController가 동일 /adm/api/approvals 아래 GET 3개, POST 4개를 중복 선언한다.
- 기대 동작: 승인 API path/method/operationId마다 정확히 하나의 Handler와 하나의 Engine만 존재한다.
- 수정 대상: 승인 Engine 정본을 하나로 결정하고 중복 Controller/Service/Repository/SQL/Test를 통합한다.
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-spring-request-mapping-uniqueness.py --root . --json-output cpf-docs/evidence/qa/spring-mapping.json; ./gradlew :cpf-admin:test --no-daemon`
- 성공 기대 결과: 중복 mapping 0, Spring ApplicationContext PASS, OpenAPI operation 충돌 0
- 실패 판정 기준: 동일 method/path 2개 이상 또는 Context 기동 실패
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: ADM 기동 실패 및 비결정적 승인 API 라우팅

## QA-FULL-002 — P0 — Legacy 승인 실행 이중 CAS

- 처리: **개발GPT 재개발**
- 근거: AdmApprovalService.execute; AdmApprovalRepository.reserveExecution/startExecution/updateRequest
- 현재 동작: startExecution이 APPROVED→EXECUTING과 version 증가를 수행한 뒤 서비스가 이전 version으로 updateRequest(EXECUTING)를 다시 호출한다.
- 기대 동작: 실행 예약은 단일 원자 CAS이며 Owner 호출 전 한 번만 성공한다.
- 수정 대상: Legacy/New Engine 통합 과정에서 reserveExecution 1회와 finishExecutionAndRequest/markExecutionUnknown을 정본화한다.
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-approval-state-machine.py --root .; ./gradlew :cpf-admin:test --tests '*Approval*' --no-daemon`
- 성공 기대 결과: 정상 실행 1회, 경쟁 실행 1건만 성공, 원장/요청 최종 상태 일치
- 실패 판정 기준: 정상 경로 동시변경 오류 또는 Owner 중복 호출
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: 승인된 조치 실행 불가 또는 중복 실행

## QA-FULL-003 — P0 — 신규 승인 Engine 현재 단계 우회

- 처리: **개발GPT 재개발**
- 근거: AdmApprovalEngineService.decide
- 현재 동작: participant 조회가 requestId+operatorId만 사용하며 current_step_no를 조건으로 사용하지 않는다.
- 기대 동작: 현재 단계 WAITING 참여자만 결정하고 미래 단계/다중 단계 동일 운영자는 거부한다.
- 수정 대상: 조회·CAS·평가를 current_step_no와 participantId에 결합한다.
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-approval-state-machine.py --root .; ./gradlew :cpf-admin:test --tests '*ApprovalEngine*' --no-daemon`
- 성공 기대 결과: 미래 단계 결정 403/409, 현재 단계만 원자 갱신
- 실패 판정 기준: 미래 단계 승인 또는 다중 행 오류
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: 승인 통제 우회

## QA-FULL-004 — P0 — 신규 승인 Engine Owner 실행 예약·멱등·UNKNOWN 원장 부재

- 처리: **개발GPT 재개발**
- 근거: AdmApprovalEngineService.execute
- 현재 동작: APPROVED 확인 후 dispatcher를 직접 호출하며 실행 예약 CAS·멱등 execution ledger·UNKNOWN 최종화가 없다.
- 기대 동작: Owner 호출 전 원자 예약, 중복 호출 replay/차단, 응답손실 UNKNOWN 및 reconcile 기록
- 수정 대상: 승인 Engine 정본 통합과 실행 상태기계 구현
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-approval-state-machine.py --root .; ./gradlew :cpf-admin:test --tests '*Approval*' --no-daemon`
- 성공 기대 결과: Owner side effect 1회, replay/UNKNOWN/reconcile PASS
- 실패 판정 기준: 중복 Owner 호출 또는 결과불명 손실
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: 위험조치 중복·감사 손실

## QA-FULL-005 — P0 — BAT 위험명령 Browser 승인자·승인ID 신뢰

- 처리: **개발GPT 재개발**
- 근거: frontend batch api → BatchRuntimeControlController → RuntimeCommandExecutor → RuntimeLifecycleService
- 현재 동작: Frontend가 approvedBy를 공개하고 Controller가 요청 본문 값을 필수로 받아 전달하며 BAT는 비어있음/요청자 분리만 검사한다. 실제 ADM 승인 원장 검증이 없다.
- 기대 동작: 서버가 approvalRequestId로 완료된 승인 원장을 조회하고 action/target/policy/requester/approver/expiry를 검증한다.
- 수정 대상: ADM 승인 검증 Port 또는 서명된 Approval Grant를 BAT Owner가 검증하도록 구현하고 Browser approvedBy 제거
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-batch-approval-trust-boundary.py --root .; ./gradlew :cpf-admin:test :cpf-batch:control-server:test --no-daemon`
- 성공 기대 결과: 임의 approvalId/approvedBy 거부, 실제 승인 Grant만 실행
- 실패 판정 기준: Browser 임의 승인 정보로 명령 수락
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: 무승인 START/STOP/RESTART/ROLLBACK 권한 우회

## QA-FULL-006 — P0 — BAT expectedVersion 0/누락/음수 CAS 우회

- 처리: **개발GPT 재개발**
- 근거: BatchRuntimeControlController.command; RuntimeCommand; JdbcRuntimeRegistry.updateDesiredState
- 현재 동작: /commands는 expectedVersion을 검증하지 않고 Registry는 expectedVersion<=0을 현재 version으로 치환한다.
- 기대 동작: 위험 명령은 양의 exact expectedVersion이 필수이고 불일치 시 409로 거부한다.
- 수정 대상: Controller·record·repository 전 구간 양수 검증 및 0 fallback 제거
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-batch-runtime-command-versioning.py --root .; ./gradlew :cpf-batch:control-server:test --no-daemon`
- 성공 기대 결과: 누락/0/음수 400, stale version 409, exact version만 성공
- 실패 판정 기준: 현재 version 자동 대입 또는 stale writer 성공
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: 동시 변경 덮어쓰기

## QA-FULL-007 — P0 — BAT 부수효과 후 오류를 FAILED로 오분류

- 처리: **개발GPT·Codex 교차검토 후 독립 QA 재검수**
- 근거: CpfBatchRiskCommandCoordinator
- 현재 동작: Owner action 성공 후 serialization/ledger complete 실패가 일반 FAILED로 분류됐다.
- 기대 동작: 부수효과 시작 이후 최종화 실패는 UNKNOWN으로 보존하고 blind retry를 차단한다.
- 수정 대상: Overlay에서 action 완료 이후 실패를 unknown()으로 기록하고 UnknownResultException으로 표준화
- 재실행 명령: `python -m pytest -q cpf-tools/scripts/tests/test_batch_risk_command_post_action_unknown.py`
- 성공 기대 결과: Java21 Harness PASS, failed=0, unknown=1
- 실패 판정 기준: post-action failure가 FAILED 또는 원 예외로 노출
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: 위험조치 중복 수행

## QA-FULL-008 — P0 — V100 BAT Ledger Lifecycle Script 누락

- 처리: **개발GPT·Codex 교차검토 후 독립 QA 재검수**
- 근거: run-db-vendor-lifecycle.ps1
- 현재 동작: V100/R100/verify V100이 Fresh/Upgrade/RollbackReapply에서 실행되지 않았다.
- 기대 동작: 3 Vendor 모든 Mode에서 V100 적용·검증·Rollback/Reapply 순서가 닫힌다.
- 수정 대상: Overlay에서 V100/R100/verify100 선언과 실행 순서 추가
- 재실행 명령: `python -m pytest -q cpf-tools/scripts/tests/test_runtime_handoff_scripts.py cpf-tools/scripts/tests/test_verify_cpf_bat_operation_ledger_lifecycle.py`
- 성공 기대 결과: Gate PASS 후 3 Vendor×3 Mode 실 DB PASS
- 실패 판정 기준: V100 누락/순서 오류/실 DB 오류
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: Upgrade 환경 BAT 위험조치 SQL 실패

## QA-FULL-009 — P0 — R100 비어있지 않은 BAT Ledger 무조건 DROP

- 처리: **개발GPT·Codex 교차검토 후 독립 QA 재검수**
- 근거: mariadb/postgresql/oracle rollback/R100__bat_operation_request_ledger.sql
- 현재 동작: 세 Vendor 모두 DROP TABLE 한 줄로 감사·멱등·UNKNOWN 기록을 무조건 삭제했다.
- 기대 동작: Ledger가 비어 있을 때만 Rollback하고 데이터가 있으면 export/reconcile 전까지 fail-closed한다.
- 수정 대상: Overlay의 세 R100에 Vendor별 SIGNAL/RAISE guard 추가
- 재실행 명령: `python -m pytest -q cpf-tools/scripts/tests/test_verify_cpf_bat_operation_ledger_lifecycle.py`
- 성공 기대 결과: 비어있지 않은 fixture Rollback 거부, 빈 fixture Drop 허용
- 실패 판정 기준: 행이 있는 테이블 DROP
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: 감사·멱등·복구 Evidence 영구 유실

## QA-FULL-010 — P0 — Endpoint Snapshot 동일 Version 상충 덮어쓰기

- 처리: **개발GPT 재개발**
- 근거: CpfServiceEndpointRegistry.replaceRuntime
- 현재 동작: version<old만 거부하여 같은 version의 다른 Snapshot이 CAS로 덮어쓸 수 있다.
- 기대 동작: 같은 version+같은 normalized payload만 멱등 허용하고 같은 version+다른 payload는 충돌 거부
- 수정 대상: Version fence 구현 및 추가된 JUnit 회귀 Test 통과
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-runtime-snapshot-versioning.py --root .; ./gradlew :cpf-starters:integration:http-client:test --no-daemon`
- 성공 기대 결과: same-version identical idempotent, conflicting payload rejected
- 실패 판정 기준: same-version conflicting update succeeds
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: stale endpoint/credential/network policy 덮어쓰기

## QA-FULL-011 — P0 — 내부 서비스 인증서와 claimed service ID 미결합

- 처리: **개발GPT 재개발**
- 근거: TransactionHeaderValidationInterceptor.defaultIdentityVerifier; CpfInternalServiceIdentityVerifier
- 현재 동작: 운영 기본 verifier가 X509Certificate 배열 존재만 확인하고 callerServiceId/callerInstanceId와 SAN/Subject를 대조하지 않는다.
- 기대 동작: 운영은 명시적 verifier Bean 또는 인증서/mesh/token identity와 claimed service/instance 결합을 강제한다.
- 수정 대상: 서비스 신원 바인딩 구현 및 fail-closed Context Test
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-internal-service-identity-binding.py --root .; ./gradlew :cpf-core:test --no-daemon`
- 성공 기대 결과: 다른 서비스 인증서로 claimed service 위조 거부
- 실패 판정 기준: 인증서 존재만으로 임의 caller 허용
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: S형 공유 API 내부 신원 위조

## QA-FULL-012 — P0 — R4 Evidence SHA stale 및 최신 CI 부재

- 처리: **개발GPT 재개발**
- 근거: R4 sourceHead=cb305fc5363263c9607e990ba640233c28668f01; current master=02dcb5d45646469f4950cf43c371706e00458616; commit status/workflow run=0
- 현재 동작: R4 대체검증은 이전 SHA를 기록하고 현재 master required check가 없다.
- 기대 동작: clean exact HEAD에서 Java25/Frontend/DB/Browser/Multi-instance/71,321 product-pass 원본 로그와 status가 존재한다.
- 수정 대상: 새 전체 exact-head runner 실행 후 Evidence/Manifest 재생성 및 required check 운영
- 재실행 명령: `pwsh -NoProfile -File .\cpf-tools\scripts\run-cpf-full-qa-validation.ps1 -ExpectedHead 02dcb5d45646469f4950cf43c371706e00458616 -Root .`
- 성공 기대 결과: 모든 Step PASS, GitHub required status SUCCESS, Evidence SHA/hash 일치
- 실패 판정 기준: stale SHA/0 checks/step 실패/hash mismatch
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: 대체 Harness를 제품 완료로 오인

## QA-FULL-013 — P0 — 전체 Current Requirement/Scenario 상태 원장 부재

- 처리: **개발GPT 재개발**
- 근거: cpf-docs/work/current에는 split master index가 있으나 전체 REQUIREMENT_STATUS.csv/SCENARIO_STATUS.csv가 확인되지 않음
- 현재 동작: QA39 44건 Matrix와 R4 10,558건 별도 파일만 존재해 71,321개 Current 상태를 한 파일 체계로 관리하지 못한다.
- 기대 동작: Requirement 30,558행과 Scenario 40,763행의 단일 Current 원장, exact SHA, 개별 Evidence/판정
- 수정 대상: Overlay builder를 최신 clean checkout에서 1회 실행하고 동일 파일을 역할별 순차 갱신
- 재실행 명령: `python cpf-tools/scripts/build-cpf-full-qa-ledgers.py --root . --expected-sha 02dcb5d45646469f4950cf43c371706e00458616 --generated-at 2026-08-04T05:03:39+09:00 --updated-by 'QA GPT' --json-output cpf-docs/evidence/qa/full-ledger-build.json`
- 성공 기대 결과: 30,558/40,763 exact coverage, 중복/미연결/Hash mismatch 0
- 실패 판정 기준: 행 수/ID/Scenario/WP/Hash 불일치
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: 일부 범위를 전체 완료로 오판

## QA-FULL-014 — P1 — R4 대체검증 Wrapper 하드코딩 stale baseline

- 처리: **개발GPT·Codex 교차검토 후 독립 QA 재검수**
- 근거: run-cpf-r4-substitute-validation.py
- 현재 동작: 이전 baseline SHA가 기본값이고 실제 Source HEAD 비교 없이 Evidence를 생성했다.
- 기대 동작: baseline SHA 필수, source HEAD와 불일치하면 모든 Gate 전 즉시 실패
- 수정 대상: Overlay에서 --baseline-sha 필수 및 --source-head/git HEAD 비교 추가
- 재실행 명령: `python -m pytest -q cpf-tools/scripts/tests/test_run_cpf_r4_substitute_validation.py`
- 성공 기대 결과: stale/missing SHA fail, exact SHA pass
- 실패 판정 기준: 이전 SHA로 PASS Evidence 생성
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: Stale Evidence 재사용

## QA-FULL-015 — P1 — Owner Boundary Gate 역방향/Internal 참조 false-negative

- 처리: **개발GPT·Codex 교차검토 후 독립 QA 재검수**
- 근거: verify-cpf-owner-boundaries.py
- 현재 동작: core→common 및 bizadmin/gateway 등 cross-owner internal/완전수식 참조를 놓쳤다.
- 기대 동작: 공식 Module 의존 방향과 모든 owner internal 경계를 fail-closed 검사
- 수정 대상: Overlay Gate와 6개 회귀 Test로 강화
- 재실행 명령: `python -m pytest -q cpf-tools/scripts/tests/test_verify_cpf_owner_boundaries.py`
- 성공 기대 결과: 허용 경로 PASS, 역방향/Internal/unknown owner FAIL
- 실패 판정 기준: synthetic violation 통과
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: Architecture 부채를 전수 Gate가 놓침

## QA-FULL-016 — P1 — Operator Trust Gate 문자열 기반 false-negative 가능

- 처리: **개발GPT 재개발**
- 근거: verify-cpf-operator-trust-boundary.py
- 현재 동작: 주석/토큰 존재로 통과할 수 있고 별도 DTO, @RequestHeader, multiline parameter actor field를 완전 파싱하지 않는다.
- 기대 동작: AST/semantic 기반 Frontend body/query와 Controller/DTO 전 범위 actor alias 검출
- 수정 대상: Java/TS parser 기반 Gate 및 negative fixtures 추가
- 재실행 명령: `python cpf-tools/scripts/verify-cpf-operator-trust-boundary.py --root .; python -m pytest -q cpf-tools/scripts/tests/test_verify_cpf_operator_trust_boundary.py`
- 성공 기대 결과: DTO/Header/nested/multiline 위조 fixture 모두 FAIL
- 실패 판정 기준: actor field fixture 통과
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: 운영자 위조 경로 미탐지

## QA-FULL-017 — P1 — V100 Flyway authoritative 위치 미확정

- 처리: **개발GPT 재개발**
- 근거: V98/V99는 migration/flyway/batDB, V100은 migration/ 루트
- 현재 동작: 실제 Flyway location 소비 경로가 Connector 검색으로 확인되지 않아 운영 자동 Upgrade 포함 여부가 불명확하다.
- 기대 동작: V100이 BAT 운영 migration discovery에 정확히 1회 포함되고 중복 version이 없다.
- 수정 대상: 실제 Flyway config/packaging을 확인해 authoritative 경로 1개로 정리하고 checksum/upgrade Test 추가
- 재실행 명령: `./gradlew dependencies --no-daemon; pwsh -NoProfile -File .\cpf-tools\scripts\run-db-vendor-lifecycle.ps1 -Vendor postgresql -Mode Upgrade -Root .`
- 성공 기대 결과: V100 정확히 1회 적용, checksum/drift PASS
- 실패 판정 기준: 0회 또는 중복 V100
- 요구 Evidence: exact HEAD, clean Working Tree, 명령 원문, Exit Code, 전체 원본 로그, 변경 Manifest, Test/Runtime 결과, Hash Manifest
- 미조치 위험: 운영 Upgrade Ledger 미생성 또는 Flyway duplicate version

