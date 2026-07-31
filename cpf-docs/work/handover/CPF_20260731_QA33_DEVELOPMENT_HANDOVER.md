# CPF QA33 후속 개발 인수인계

- Base master SHA: `c1f273f1ea4fafac6fd5d23bd837adfc38a04497`
- Branch: `master`
- Deliverable: Root-relative QA33 Final Source Overlay ZIP
- README·Guide·Asset: 변경하지 않음
- Git write: 수행하지 않음

## 현재 상태

- Requirement 개발 완료: 135/138
- 부분 구현: `QA33-REQ-017`, `QA33-REQ-018`, `QA33-REQ-120`
- Result 검증 완료: 151/552
- Result 미검증: 401

## 적용 후 필수 순서

1. ZIP을 프로젝트 Root에 덮어쓴다.
2. `cpf-tools/scripts/cleanup-cpf-qa33-workspace.ps1`을 실행해 Delete Manifest와 가비지를 정리한다.
3. `git diff --check`, `git status --short`를 확인한다.
4. 승인 Registry가 연결된 환경에서 ADM/BZA `npm ci`, `generate:api`, `git diff --exit-code`, typecheck/test/build/Playwright 3종을 실행한다.
5. Java 25와 3DB/Kafka/Browser/다중 인스턴스 환경에서 `verify-cpf-qa33-all.ps1`을 실행한다.
6. exact-SHA Evidence가 모두 PASS한 뒤에만 QA33 완료로 판정한다.

## 정본 Evidence

- `cpf-docs/evidence/current/qa33-development/CPF_20260731_QA33_FINAL_SOURCE_VALIDATION.sanitized.json`
- SHA-256: `8b144b04c02a40dbd6b53f6326d6fbb22542f7ab46fdf764280c19e91fb87130`
