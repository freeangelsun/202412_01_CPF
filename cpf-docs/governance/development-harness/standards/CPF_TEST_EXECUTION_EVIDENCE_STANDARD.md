# CPF Test Execution / Evidence 원장 표준

모든 Work Item은 역할 상태와 별개로 `current/TEST_EXECUTION_LEDGER.csv`에 실제 Test 수행 상태를 기록한다. 최소 `STATIC_HIGH_INTENSITY`와 `RUNTIME_HIGH_INTENSITY`를 가진다. Canonical Acceptance가 명시적으로 `해당 없음`인 경우에만 `NOT_APPLICABLE`이 가능하다.

## PASS 필수 근거
`completion_reason, command, environment, prerequisite_source, required_environment, actual_environment, started_at, ended_at, exit_code, observed_result, evidence, evidence_sha256, source_identity, impact_scope, regression_scope`가 모두 있어야 한다. `prerequisite_source`는 Current Source의 canonical verifier/bootstrap/toolchain/package metadata 등 required 값을 확정한 위치를 가리켜야 한다. Evidence 파일은 실제 존재하고 SHA-256이 일치해야 한다.

## 금지
- Test를 작성했다는 이유로 실행 PASS 처리
- `READY/PLANNED/SKIP/NOT_EXECUTED/UNKNOWN/BLOCKED_EXTERNAL`을 PASS로 치환
- Target count 0, assertion 0, consumer 0 같은 vacuous pass
- 이전 Source Identity의 PASS 승계
- 과거 세션/대화의 tool version을 Current Source 재확인 없이 재사용
- 사용자 PC actual version에 맞춰 Framework prerequisite/expected를 낮춰 PASS 생성
- 실패 후 expected/waiver/suppression을 낮춰 PASS 생성

## 완료 연계
`verification_status=완료`는 해당 Work Item의 mandatory Test가 모두 PASS이고 역할별 검수 Evidence가 일치할 때만 가능하다. `overall_status=완료`는 QA PASS와 필수 Runtime/Fresh Replay까지 닫힌 뒤에만 가능하다.

## sessionKey와 Work Item 1:1 Evidence

모든 실제 Test/Runtime Evidence는 생성 sessionKey를 식별할 수 있어야 한다. 세션 Report는 Work Item별 독립 Evidence Block을 사용한다.

하나의 실행이 여러 Work Item을 검증할 수는 있으나, 각 Work Item에 `shared execution`이라는 이유만 적어서는 안 된다. 해당 실행에서 **그 Work Item의 Acceptance를 직접 증명한 관찰 결과**를 개별 기록한다.

다음은 PASS 근거가 아니다.

- 여러 Work Item에 동일 completion_reason을 복사한 일괄 완료
- 명령/Exit Code/Evidence 없이 `모두 PASS`
- 환경 부족을 여러 Work Item의 일괄 SKIP으로 변환
- 이전 sessionKey/Source Identity의 PASS를 현재 Source에 자동 승계

최종 QA는 Current Registry의 각 Mandatory Work Item과 session Evidence를 1:1 대조하고 미Merge Session/Conflict/근거 누락이 0인지 확인한다.

