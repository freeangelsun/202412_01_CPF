# CPF Developer GPT Next Work Instruction

최신 사용자 전체 Source ZIP과 최신 FullLocal 결과 ZIP을 유일한 실행 기준으로 잡는다. 과거 PASS/Evidence를 현재 성공으로 자동 승계하지 않는다.

우선순위는 `FullLocal FAIL root cause → SPECIAL 20 P0 → Developer/Adoption REWORK 영향 재검수 → P1 → Evidence/Fresh Apply`다. Source/SQL/API/Test/Config/Frontend/Generator/Consumer/Evidence를 완료 단위로 처리한다.

다음 입력에서 FullLocal의 PASS/FAIL/SKIP_ENV/NOT_EXECUTED를 먼저 전량 집계하고 공통 원인별로 수정한다. 필수 Runtime이 미실행이면 전체 완료가 아니다.

Developer/Adoption 검수는 `CPF_DEVELOPER_GOLDEN_PATH.md`, `CPF_STARTER_QUICK_SELECT.md`, `CPF_PUBLIC_FUNCTION_TOP_100.md`, `CPF_BATCH_DEVELOPER_TOP_50.md`와 실제 Catalog/Source/Consumer가 일치하는지 다시 확인한다. 기능을 줄이거나 Internal API를 Public으로 올려 사용성을 해결하지 않는다.
