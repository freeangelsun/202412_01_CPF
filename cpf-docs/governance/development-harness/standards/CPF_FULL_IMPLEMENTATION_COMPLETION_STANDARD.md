> Development Harness 내부 통합 표준. 이 파일은 독립 정본이 아니며 `CPF_DEVELOPMENT_HARNESS.md`의 통제를 받는다.

# CPF 부분 구현 완료 금지 표준

> 제품 Requirement의 완료 판정은 `../product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md` §24를 따른다. 이 문서는 QA가 빠르게 확인할 수 있는 금지 규칙만 요약한다.

다음 중 하나라도 해당하면 완료가 아니다.

- Interface/DTO/Sample/Swagger/Table/화면/Script만 존재
- 실제 Consumer/호출경로 없음
- 정상 Happy Path만 있고 오류/경계/부분 실패/UNKNOWN/복구 없음
- retry/idempotency/concurrency/multi-instance/process-kill 검증 누락
- Security/permission/masking/audit/secret 처리 누락
- DB 변경인데 DB3/install/upgrade/rollback/runtime query 누락
- Framework 계약 변경인데 Generator/Generated Domain/EDU/OpenAPI/Frontend/Config 미반영
- Runtime 미실행을 PASS 처리
- verifier 대상 0건/stale path/old policy의 False Green
- READY/PLANNED/NOT_EXECUTED를 PASS로 기록
- 과거 Source SHA Evidence를 현재 PASS로 승계

N/A는 이유와 Owner 경계를 명시해야 하며 “이번 범위 아님”만으로 면제하지 않는다.
