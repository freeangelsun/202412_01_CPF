# CPF Standalone Windows / Linux Runtime 표준

Standalone로 배포·실행되는 CPF Runtime/Tool은 Windows PowerShell과 Linux shell에서 동일한 운영 Lifecycle을 제공해야 한다. Harness는 특정 파일명을 제품 계약으로 하드코딩하지 않고 실제 Standalone Catalog/Source에서 entrypoint를 발견하여 의미적 parity를 검증한다.

필수 의미 동작은 `prerequisite → prepare → start → status/readiness → verify/E2E → stop → cleanup → fresh-replay`다. 각 플랫폼은 동일한 입력 의미, exit-code, 로그/Evidence 위치, 실패 분류, idempotency를 가져야 한다. 한 플랫폼 구현 또는 실검증이 없으면 전체 Runtime PASS가 아니다.

Runtime 요청서를 생성할 때에는 Windows와 Linux 명령을 함께 제공하고, 필요한 Java/Docker/Node/DB/Browser/Secret prerequisite, 시작/종료 시각, 단계별 로그, 실제 ExitCode, PASS/FAIL 조건을 명시한다.
