# CPF 20260729_04 최종 인수인계

## 기준과 역할

- 기준 SHA: `b8941577b99535ff3e64a4fad99b74bafa544227`
- 최종 목표 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- ChatGPT: 개발자
- Codex: 검수자
- 사용자 승인 없는 Commit/Push/Branch/Tag/PR 금지

## Source 개발 상태

이번 Overlay에서 Known Source Gap과 Root Cause를 실제 Owner Module에 반영했다. `부분 구현`·`미구현` 상태를 Codex 개발 항목으로 넘기지 않는다.

- Redis/Cache Product Runtime
- Streaming Excel/CSV File Job
- BZA Recursive Tree와 Action Permission
- Runtime Control Typed Contract
- Notification Typed Contract와 위험 Action
- Generator Golden Template와 CREATE/UPDATE/DELETE Request Hash 멱등 원장
- Local Web/Batch Runtime Guard와 `cpf-tools/runtime/*` 물리 이관
- 3개 DB Vendor Migration/Seed/Checksum Parity
- CI/Semantic/Hygiene/Evidence Gate

## 검증 상태

이 환경에서 Static Closure 31개와 Frontend 구문 검사를 수행했다. Java 25·Gradle 9.1·PowerShell·3DB·Redis 실제 Topology·Browser·다중 Instance는 환경 부재로 `미검증`이다.

## 다음 세션이 할 일

신규 기능을 다시 설계하지 않는다. 최신 master에 Overlay를 적용하고 Codex 검수 결과를 받는다. 실패가 있으면 다음 순서로 ChatGPT가 수정한다.

1. 최초 Compile 오류와 Owner Module 확인
2. Public API/SPI/Internal 경계 보존
3. Consumer, Test, Config, SQL, Frontend, Guide 동시 수정
4. 3개 Vendor와 Generator 영향 확인
5. 정적 Gate 재실행
6. 수정 Root Overlay와 새 SHA Manifest 제공

## 보호할 기능

Transaction ID, 표준 Header/Error, Runtime Control Durable Delivery, Batch Runtime 분리, EXS Generated Domain 정책, 공식 DB 3종, Fail-closed 권한, Repository Root Hygiene를 회귀시키지 않는다.

## 주요 파일

- `cpf-docs/quality/CPF_FINAL_TARGET_162_TRACEABILITY_20260729_04.csv`
- `cpf-docs/quality/qa-20260729/CPF_ENTERPRISE_REQA_816_DEVELOPMENT_CLOSURE_20260729_04.csv`
- `cpf-docs/quality/qa-20260729/CPF_QA_387_FINAL_VALIDATION_MATRIX_20260729_04.csv`
- `cpf-docs/work/current/CPF_20260729_04_CODEX_FINAL_REVIEW_REQUEST.md`
- `cpf-docs/guides/CPF_20260729_04_FINAL_APPLY_AND_VALIDATION_GUIDE.md`


## Repository Root 정리 결정

- `cpf-local-runtime`, `cpf-local-batch-runtime`은 Gradle 논리 이름만 유지한다.
- 물리 Source는 `cpf-tools/runtime/cpf-local-runtime`, `cpf-tools/runtime/cpf-local-batch-runtime`이다.
- Root `deploy`는 배포 자산 Owner이므로 Java Source Module을 넣지 않는다.
- 적용 후 `cpf-tools/scripts/apply-20260729-final-overlay.ps1`로 Root 중복을 제거한다.
