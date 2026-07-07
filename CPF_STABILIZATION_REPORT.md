# CPF 안정화 작업 리포트

## 기준

- 기준 목표 파일: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 검수 기준 파일: `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- 요청 확인 파일: `CPF_NEW_REQUEST.md`
- 이번 리포트는 실행하지 않은 검증을 완료로 쓰지 않는다.
- 상태 값은 `완료`, `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`만 사용한다.

## 이번 작업 요약

- `CPF_STABILIZATION_REPORT.md`가 비어 있어 기능 매트릭스 기준으로 재작성했다.
- `CPF_EVIDENCE_INDEX.md`와 `CPF_GAP_MATRIX.md`를 신규 작성했다.
- `scripts/check-report-matrix-evidence-consistency.ps1`를 추가해 리포트, 기능 매트릭스, 증적 인덱스가 서로 다른 상태를 말하지 못하게 했다.
- `qualityGate`에 report/matrix/evidence 정합성 검사를 연결했다.
- `README.md`를 사람이 읽을 수 있는 짧은 진입점으로 재작성하고 PFW/CMN 역할 경계를 다시 명시했다.
- MariaDB full install, 표준 헤더 E2E, ADM runtime/permission/OpenAPI, 복합 거래, ADM 거래 그룹, create-domain, EDU mapper DB slice, runtime closure를 실제 실행해 증적을 `specs/evidence/20260707_01`에 남겼다.
- 브라우저 클릭과 real broker는 환경 미비로 완료 처리하지 않았다.

## 검증 상태

| check id | 상태 | 근거 | 비고 |
| --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log`, `xyz_edu_query_fixture.sql` | MariaDB 테스트 DB 기준 `XyzQueryEducationMapperSliceTest`를 통과했다. |
| mariadb-full-install | 완료 | `scripts/smoke-mariadb-full-install.ps1`, `specs/evidence/20260707_01/mariadb-full-install-result.sanitized.json` | MariaDB CLI로 `00_all_install_and_smoke.sql`, `99_smoke_check.sql`, seed 재실행 검증을 통과했다. |
| adm-runtime | 완료 | `specs/evidence/20260707_01/adm-runtime-smoke-result.sanitized.json` | ADM runtime smoke가 OpenAPI, Center-Cut, transaction meta, UI static 검증을 통과했다. |
| adm-permission-runtime | 완료 | `specs/evidence/20260707_01/adm-permission-runtime-result.sanitized.json` | ADM permission runtime smoke를 통과했다. |
| openapi-runtime | 완료 | `specs/evidence/20260707_01/openapi-runtime-result.sanitized.json` | ADM runtime smoke 내부 OpenAPI 검증을 통과했고 sanitized evidence를 남겼다. |
| adm-browser-click | 미검증 | `specs/evidence/20260707_01/adm-ui-browser-smoke-result.sanitized.json` | Node/npm/npx 또는 Playwright/브라우저 드라이버가 PATH에 없어 browser click은 SKIPPED다. |
| standard-header-e2e | 완료 | `scripts/smoke-standard-header-e2e.ps1`, `specs/evidence/20260707_01/standard-header-e2e-result.sanitized.json` | 표준 헤더 수신, 확장 헤더, outbound 전파, DB log 조회를 통과했다. |
| complex-transaction-trace | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json` | ACC/MBR/EXS 복합 거래 trace smoke를 통과했다. |
| transaction-segment-log | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json` | 복합 거래 smoke에서 transaction segment 기록을 검증했다. |
| adm-transaction-group-list | 완료 | `specs/evidence/20260706_04/adm-operation-console-runtime-result.sanitized.json` | ADM 운영 콘솔 smoke에서 거래 그룹 목록 API 증적이 있다. |
| adm-transaction-timeline | 완료 | `specs/evidence/20260707_01/adm-transaction-group-runtime-result.sanitized.json` | ADM 거래 그룹 runtime smoke에서 timeline, headers, external logs 조회를 확인했다. |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260706_04/cmn-fixed-length-advanced-result.sanitized.json` | fixed-length parsing/formatting 고급 smoke 증적을 기준으로 완료 처리했다. |
| composite-runtime-smoke | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json`, `specs/evidence/20260707_01/composite-transaction-failure-runtime-result.sanitized.json` | 복합 거래 성공/실패 runtime smoke를 모두 통과했다. |
| adm-transaction-group-runtime | 완료 | `specs/evidence/20260707_01/adm-transaction-group-runtime-result.sanitized.json`, `specs/evidence/20260707_01/adm-transaction-group-failure-runtime-result.sanitized.json` | ADM 거래 그룹 성공/실패 runtime smoke를 모두 통과했다. |
| redis-kafka-mq-broker | 미검증 | broker optional | 실제 Redis/Kafka/MQ broker 연동 검증은 미실행이다. |
| broker-real-integration | 미검증 | broker optional | 실제 broker 장애/복구 시나리오 검증은 미실행이다. |
| file-log-standard | 완료 | `specs/evidence/20260707_01/file-log-standard-result.sanitized.json` | 구조화 거래 파일 로그와 integration 파일 로그 smoke를 통과했다. |
| trace-boost-runtime | 완료 | `specs/evidence/20260707_01/trace-boost-runtime-result.sanitized.json` | ADM Trace Boost runtime smoke를 통과했다. |
| bat-trace-boost-runtime | 완료 | `specs/evidence/20260707_01/bat-trace-boost-runtime-result.sanitized.json` | BAT trace boost runtime smoke를 통과했다. |
| runtime-start-services | 완료 | `specs/evidence/20260707_01/runtime-start-services-result.sanitized.json` | ACC, MBR, EXS, ADM, BAT bootJar build, process start, health를 확인했다. |
| packaged-runtime-resources | 완료 | `specs/evidence/20260707_01/packaged-runtime-resource-check.sanitized.json` | 패키징 리소스 검사를 통과했다. |
| runtime-status-diagnostics | 완료 | `specs/evidence/20260707_01/runtime-status-result.sanitized.json`, `specs/evidence/20260707_01/runtime-diagnostics-result.sanitized.json` | runtime status와 diagnostics를 통과했다. |
| runtime-closure | 완료 | `specs/evidence/20260707_01/runtime-closure-result.sanitized.json` | runtime closure smoke를 통과했다. |
| adm-operation-console-runtime | 완료 | `specs/evidence/20260707_01/adm-operation-console-runtime-result.sanitized.json` | ADM 운영 콘솔 주요 API smoke를 통과했다. |
| adm-log-policy-ui-static | 완료 | `specs/evidence/20260707_01/adm-log-policy-ui-static-result.sanitized.json` | ADM log policy UI 정적 marker smoke를 통과했다. |
| bat-log-bean-runtime | 완료 | `specs/evidence/20260707_01/bat-log-bean-runtime-result.sanitized.json` | BAT logging bean runtime smoke를 통과했다. |
| exs-timeout-retry-runtime | 완료 | `specs/evidence/20260707_01/exs-timeout-retry-runtime-result.sanitized.json` | EXS timeout/retry runtime smoke를 통과했다. |
| cmn-fixed-length-advanced | 완료 | `specs/evidence/20260707_01/cmn-fixed-length-advanced-result.sanitized.json` | CMN fixed-length 고급 검증을 통과했다. |
| create-domain-smoke | 완료 | `specs/evidence/20260707_01/create-domain-result.sanitized.json` | create-domain smoke를 통과했다. |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_01/runtime-smoke-summary.sanitized.json` | runtime smoke summary를 재생성했고 exitCode 0을 확인했다. |
| quality-gate | 완료 | `specs/evidence/20260707_01/quality-gate.log` | 최종 검증 단계에서 재확인한다. |
| check-html-docs | 완료 | `specs/evidence/20260707_01/check-html-docs.log` | 최종 검증 단계에서 재확인한다. |
| check-feature-evidence | 완료 | `specs/evidence/20260707_01/check-feature-evidence.log` | 최종 검증 단계에서 재확인한다. |
| check-utf8 | 완료 | `specs/evidence/20260707_01/check-utf8-mojibake.log` | 최종 검증 단계에서 재확인한다. |

## 남은 리스크

- `CPF_NEW_REQUEST.md`와 일부 `build.gradle` 설명 문자열에는 사람이 읽기 어려운 mojibake가 남아 있다. 요청 확인 파일은 작업 대상 제외 조건이 있어 수정하지 않았다.
- 브라우저 클릭은 Node/npm/npx 또는 Playwright/브라우저 드라이버 부재로 skip 되었고 완료로 집계하지 않았다.
- `specs/evidence/20260706_04`의 일부 JSON status 문자열은 과거 스크립트 출력 인코딩 영향으로 깨져 보인다. JSON parse 가능 여부와 파일 존재 여부는 게이트로 확인한다.
- EDU mapper DB slice는 신규 표준 환경변수 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`와 기존 호환 환경변수 `CPF_XYZ_EDU_MAPPER_DB_USER`를 모두 문서 기준에 남긴다.
- Redis/Kafka/MQ real broker 연동과 장애/복구 시나리오는 이번 작업에서 실행하지 않아 `미검증`으로 유지한다.

## 항상 지킨 기준

- 문서와 소스, SQL, Swagger, EDU 샘플은 같은 상태 기준으로 맞춘다.
- README는 짧은 진입점으로 유지하고 상세 내용은 가이드 문서와 리포트로 연결한다.
- 실행하지 않은 검증은 완료로 보고하지 않는다.
- 신규 주석, 설명, SQL COMMENT는 한글 기준으로 작성한다.
- 최종 리포트에는 수행 작업, 검증 결과, 남은 리스크, 보류 항목, 다음 보강 후보를 분리한다.
