# CPF Developer GPT Next Work Instruction

최신 사용자 전체 Source ZIP과 최신 FullLocal 결과 ZIP을 유일한 실행 기준으로 잡는다. 과거 PASS/Evidence를 현재 성공으로 자동 승계하지 않는다.

우선순위는 `FullLocal FAIL root cause → SPECIAL 20 P0 → Developer/Adoption REWORK 영향 재검수 → P1 → Evidence/Fresh Apply`다. Source/SQL/API/Test/Config/Frontend/Generator/Consumer/Evidence를 완료 단위로 처리한다.

다음 입력에서 FullLocal의 PASS/FAIL/SKIP_ENV/NOT_EXECUTED를 먼저 전량 집계하고 공통 원인별로 수정한다. 필수 Runtime이 미실행이면 전체 완료가 아니다.

Developer/Adoption 검수는 `CPF_DEVELOPER_GOLDEN_PATH.md`, `CPF_STARTER_QUICK_SELECT.md`, `CPF_PUBLIC_FUNCTION_TOP_100.md`, `CPF_BATCH_DEVELOPER_TOP_50.md`와 실제 Catalog/Source/Consumer가 일치하는지 다시 확인한다. 기능을 줄이거나 Internal API를 Public으로 올려 사용성을 해결하지 않는다.

## 로컬 전달 직후 병행 선행개발 루틴

사용자가 Overlay를 로컬에 적용했거나 FullLocal을 실행 중이라고 전달하면 결과를 기다리며 멈추지 않는다. 그 시점의 적용 Source를 기준으로 다음 사이클 준비를 즉시 시작한다.

1. 현재 적용 Source를 내부 작업공간에 재구성하고 content identity를 기록한다.
2. Runtime 전용 항목을 제외한 Static/Function/Security/Contract/Generator/Frontend/OpenAPI/DB/Batch/Messaging/Cache/Evidence 검수를 최대 범위로 수행한다.
3. SPECIAL 20과 Developer/Adoption REWORK 10의 영향 항목을 다시 열고 잠복 결함을 찾는다.
4. FullLocal Orchestrator에서 필수 Runtime이 환경변수/경로/Stage 의존성 때문에 이유 없이 SKIP/NOT_EXECUTED될 가능성을 선검수한다.
5. 다음 로컬에서 실행할 Stage, 필요한 Environment/Evidence, PASS/FAIL 기준을 미리 준비한다.
6. 이 선행 작업은 로컬 결과가 오기 전 별도 ZIP으로 전달하지 않고 내부 작업본에 누적한다.
7. 로컬 결과 ZIP이 도착하면 준비된 분석과 결과를 병합해 공통 Root Cause 단위로 재개발하고, 그때 하나의 Overlay/Handover/Evidence로 전달한다.
8. 세션당 안전하게 수행 가능한 작업량을 최대화한다. 작은 수정 하나씩 왕복하지 않고 관련 Source/Test/Verifier/Generator/Frontend/SQL/Evidence를 한 사이클에 묶는다.

이 루틴은 세션 인수인계 시에도 반드시 승계하며, Runtime 결과 대기를 작업 중단 사유로 사용하지 않는다.

