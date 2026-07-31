# CPF Next Work Request

현재 다음 작업 정본은 아래 문서다.

- `cpf-docs/work/current/CPF_20260731_QA33_POST_PUSH_REMEDIATION_REQUEST.md`

최신 검수 기준 SHA는 `da491b3f5210e36efc63a7a627ad07c9481fac63`이다. 작업 시작 시 실제 `master` HEAD를 다시 확인한다.

우선순위는 신규 기능 추가가 아니라 다음 P0 결함의 완전 수리다.

1. Release Gate Self-Dirty와 Evidence Staging
2. Frontend Git SHA 자기참조 제거
3. Generated Client Validator Schema 통일
4. 실제 Orval·TanStack Query Consumer 이관
5. Result/Requirement/Scenario Evidence 계약 재구축
6. Post-Push exact-SHA 정본 갱신
7. Java25·ADM/BZA·3DB·Kafka·Multi-instance·Supply-chain 실행 검증

상세 결함과 완료 조건은 다음 리뷰를 따른다.

- `cpf-docs/work/review/CPF_20260731_QA33_POST_PUSH_FINAL_REVIEW.md`

위 검증과 결함 수리가 완료되기 전에는 QA33, Release 또는 GA 완료로 처리하지 않는다.
