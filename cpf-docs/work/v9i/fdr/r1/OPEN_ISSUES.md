# Open Issues

개발GPT가 직접 수행 가능한 구현·정적·대체검증은 완료했다. 아래는 허위 PASS로 처리하지 않은 target-runtime 검증이다.

1. **FDEV-004** — Java 25/Gradle 9.1 clean snapshot build/test/publication.
2. **FDEV-005** — Oracle/PostgreSQL/MariaDB real lifecycle; DBA credentials required.
3. **FDEV-006** — Broker/split-WAS/multi-process/process-kill and UNKNOWN reconcile.
4. **FDEV-017** — Chromium/Firefox/WebKit browser matrix.
5. `cpf-starters/openapi-webmvc/**`는 `web-api` 내부화 후 삭제 후보이나, 사용자 승인 전 삭제하지 않았다.
6. Git Working Tree는 GitHub connector로 조회할 수 없어 Overlay 적용 대상 로컬에서 `git status` 재확인이 필요하다.
