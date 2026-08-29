# CPF Test Execution / Evidence 원장 표준

모든 Work Item은 역할 상태와 별개로 `current/TEST_EXECUTION_LEDGER.csv`에 실제 Test 수행 상태를 기록한다. 최소 `STATIC_HIGH_INTENSITY`와 `RUNTIME_HIGH_INTENSITY`를 가진다. Canonical Acceptance가 명시적으로 `해당 없음`인 경우에만 `NOT_APPLICABLE`이 가능하다.

## PASS 필수 근거
`completion_reason, command, environment, started_at, ended_at, exit_code, observed_result, evidence, evidence_sha256, source_identity, impact_scope, regression_scope`가 모두 있어야 한다. Evidence 파일은 실제 존재하고 SHA-256이 일치해야 한다.

## 금지
- Test를 작성했다는 이유로 실행 PASS 처리
- `READY/PLANNED/SKIP/NOT_EXECUTED/UNKNOWN/BLOCKED_EXTERNAL`을 PASS로 치환
- Target count 0, assertion 0, consumer 0 같은 vacuous pass
- 이전 Source Identity의 PASS 승계
- 실패 후 expected/waiver/suppression을 낮춰 PASS 생성

## 완료 연계
`verification_status=완료`는 해당 Work Item의 mandatory Test가 모두 PASS이고 역할별 검수 Evidence가 일치할 때만 가능하다. `overall_status=완료`는 QA PASS와 필수 Runtime/Fresh Replay까지 닫힌 뒤에만 가능하다.
