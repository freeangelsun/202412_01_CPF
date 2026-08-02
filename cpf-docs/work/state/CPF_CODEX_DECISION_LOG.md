# CPF Codex Decision Log

## QA38 Architecture Decisions

1. `cpf-core`는 topology-independent API/SPI/Model과 최소 순수 Java 계약만 소유한다.
2. 선택 Runtime은 `cpf-starters` Leaf Starter가 소유한다.
3. Aggregate/Profile은 전이 Dependency와 Version Lock만 제공하며 자체 업무 Bean을 소유하지 않는다.
4. 공식 DB Vendor는 Oracle, PostgreSQL, MariaDB만 사용한다.
5. Multi-provider Messaging은 Named Binding 필수, Default 최대 1개, 모호성 fail-closed다.
6. TCP write 후 response loss는 실패 재시도가 아니라 `UNKNOWN_RESULT`와 reconcile로 처리한다.
7. SFTP Provider 부재는 성공 계획 반환이 아니라 startup/runtime fail-closed다.
8. Legacy 삭제는 exact path Manifest로만 수행하고 보호 경로는 제외한다.
9. 개발 완료와 Runtime 검증 완료를 별도 상태로 관리한다.
10. Commit·Push는 사용자만 수행한다.

Base SHA: `dafe5c0e5260ea8149234e8ab2e75347e75338c1`
