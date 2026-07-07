# CPF 증적 인덱스

## 작성 기준

- `CPF_STABILIZATION_REPORT.md`와 `specs/기능_구현_매트릭스.html`의 check id와 상태를 같은 값으로 유지한다.
- `완료` 항목은 존재하는 파일 증적 또는 이번 작업에서 생성한 검증 로그를 반드시 연결한다.
- 실행하지 않은 항목은 `미검증`으로 표시하고 완료로 집계하지 않는다.

| check id | 상태 | 증적 | 확인 기준 | 비고 |
| --- | --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log` | 로그 파일 크기, 민감정보 scan | MariaDB test datasource 기반 mapper slice 통과 |
| mariadb-full-install | 완료 | `specs/evidence/20260707_01/mariadb-full-install-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | MariaDB full install, smoke, seed 재실행 검증 통과 |
| adm-runtime | 완료 | `specs/evidence/20260707_01/adm-runtime-smoke-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | ADM runtime smoke 통과 |
| adm-permission-runtime | 완료 | `specs/evidence/20260707_01/adm-permission-runtime-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | ADM permission runtime smoke 통과 |
| openapi-runtime | 완료 | `specs/evidence/20260707_01/openapi-runtime-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | OpenAPI smoke 통과 |
| adm-browser-click | 미검증 | `specs/evidence/20260707_01/adm-ui-browser-smoke-result.sanitized.json` | SKIPPED 사유 확인 | Node/npm/npx 또는 Playwright/브라우저 드라이버 부재 |
| standard-header-e2e | 완료 | `specs/evidence/20260707_01/standard-header-e2e-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | 표준 헤더 E2E 통과 |
| complex-transaction-trace | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | 복합 거래 trace 통과 |
| transaction-segment-log | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | transaction segment 기록 검증 |
| adm-transaction-group-list | 완료 | `specs/evidence/20260707_01/adm-operation-console-runtime-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | 운영 콘솔 smoke에서 목록 API 확인 |
| adm-transaction-timeline | 완료 | `specs/evidence/20260707_01/adm-transaction-group-runtime-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | timeline/header/external log 조회 확인 |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260707_01/cmn-fixed-length-advanced-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | parser/formatter 고급 검증 증적 사용 |
| composite-runtime-smoke | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json`, `specs/evidence/20260707_01/composite-transaction-failure-runtime-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | 성공/실패 복합 거래 runtime 통과 |
| adm-transaction-group-runtime | 완료 | `specs/evidence/20260707_01/adm-transaction-group-runtime-result.sanitized.json`, `specs/evidence/20260707_01/adm-transaction-group-failure-runtime-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | 성공/실패 ADM 거래 그룹 runtime 통과 |
| redis-kafka-mq-broker | 미검증 | `없음` | broker 미실행 | Redis/Kafka/MQ 실제 broker 테스트 필요 |
| broker-real-integration | 미검증 | `없음` | broker 장애/복구 미실행 | DLQ/replay/fallback 시나리오 필요 |
| file-log-standard | 완료 | `specs/evidence/20260707_01/file-log-standard-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | 거래 파일 로그 증적 |
| trace-boost-runtime | 완료 | `specs/evidence/20260707_01/trace-boost-runtime-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | Trace Boost runtime 증적 |
| bat-trace-boost-runtime | 완료 | `specs/evidence/20260707_01/bat-trace-boost-runtime-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | BAT trace boost 증적 |
| runtime-start-services | 완료 | `specs/evidence/20260707_01/runtime-start-services-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | 5개 모듈 기동 증적 |
| packaged-runtime-resources | 완료 | `specs/evidence/20260707_01/packaged-runtime-resource-check.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | bootJar 리소스 검사 증적 |
| runtime-status-diagnostics | 완료 | `specs/evidence/20260707_01/runtime-status-result.sanitized.json`, `specs/evidence/20260707_01/runtime-diagnostics-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | status/diagnostics 증적 |
| runtime-closure | 완료 | `specs/evidence/20260707_01/runtime-closure-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | runtime closure 집계 증적 |
| adm-operation-console-runtime | 완료 | `specs/evidence/20260707_01/adm-operation-console-runtime-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | ADM 운영 콘솔 smoke 증적 |
| adm-log-policy-ui-static | 완료 | `specs/evidence/20260707_01/adm-log-policy-ui-static-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | UI marker 정적 검증 증적 |
| bat-log-bean-runtime | 완료 | `specs/evidence/20260707_01/bat-log-bean-runtime-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | BAT logging bean 증적 |
| exs-timeout-retry-runtime | 완료 | `specs/evidence/20260707_01/exs-timeout-retry-runtime-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | EXS timeout/retry 증적 |
| cmn-fixed-length-advanced | 완료 | `specs/evidence/20260707_01/cmn-fixed-length-advanced-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | CMN fixed-length 고급 증적 |
| create-domain-smoke | 완료 | `specs/evidence/20260707_01/create-domain-result.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | domain 생성 smoke 통과 |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_01/runtime-smoke-summary.sanitized.json` | JSON parse, 파일 크기, 민감정보 scan | runtime smoke summary exitCode 0 |
| quality-gate | 완료 | `specs/evidence/20260707_01/quality-gate.log` | Gradle qualityGate 로그 | 이번 작업 최종 단계에서 생성 |
| check-html-docs | 완료 | `specs/evidence/20260707_01/check-html-docs.log` | HTML 문서/리포트 상태 일치 로그 | 이번 작업 최종 단계에서 생성 |
| check-feature-evidence | 완료 | `specs/evidence/20260707_01/check-feature-evidence.log` | 기능 증적 marker 로그 | 이번 작업 최종 단계에서 생성 |
| check-utf8 | 완료 | `specs/evidence/20260707_01/check-utf8-mojibake.log` | UTF-8/mojibake 검사 로그 | 이번 작업 최종 단계에서 생성 |
