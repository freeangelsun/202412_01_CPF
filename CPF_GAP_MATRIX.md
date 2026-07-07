# CPF 남은 갭 매트릭스

## 목적

이 문서는 `CPF_FINAL_TARGET_REQUIREMENTS.md`의 최종 목표 대비 이번 작업에서 아직 완료로 닫지 못한 항목을 추적한다. 완료 증적이 없는 항목을 완료로 올리지 않기 위한 보조 문서다.

| 우선순위 | 영역 | 상태 | 남은 작업 | 완료 기준 |
| --- | --- | --- | --- | --- |
| 1 | report/matrix/evidence gate | 완료 | 리포트, 기능 매트릭스, 증적 인덱스 상태 일치 검사 추가 | `scripts/check-report-matrix-evidence-consistency.ps1`와 `qualityGate` 연결 |
| 1 | MariaDB full install | 완료 | 신규 DB 기준 `00_all_install_and_smoke.sql` 실행, FK/index/seed idempotent 검증 | `mariadb-full-install-result.sanitized.json` 생성 및 성공 |
| 1 | 표준 헤더 E2E | 완료 | `X-Cpf-*`, `X-Transaction-*`, 확장 헤더, 민감 헤더 마스킹, outbound 전파 확인 | `standard-header-e2e-result.sanitized.json` 생성 및 성공 |
| 1 | ADM runtime | 부분 구현 | ADM 전체 smoke, 권한 write API, OpenAPI 품질 gate 통과. 브라우저 클릭은 환경 부재로 skip | ADM/API smoke 결과 JSON 생성. browser click은 Node/Playwright 준비 후 완료 가능 |
| 1 | 복합 거래 추적 | 완료 | ACC/MBR/EXS 연계 거래, segment log, ADM 거래 그룹 timeline/detail 조회 | composite 및 adm transaction group smoke 결과 JSON 생성 |
| 2 | create-domain | 완료 | 신규 도메인 scaffold, 충돌 검사, patch 후보, smoke 검증 | `create-domain-result.sanitized.json` 생성 및 성공 |
| 2 | runtime smoke summary | 완료 | runtime smoke 결과 전체 재수집과 summary 재생성 | `runtime-smoke-summary.sanitized.json` 생성 및 성공 |
| 2 | EDU mapper DB slice | 완료 | MariaDB test datasource 기준 mapper slice 재실행 | `edu-mapper-db-slice.log` 통과 |
| 2 | Redis/Kafka/MQ broker | 미검증 | 실제 broker publish/subscribe, fallback, 장애/복구, DLQ/replay 검증 | broker smoke 결과 JSON 생성 |
| 2 | PFW Service Call Engine | 부분 구현 | service/endpoint/instance registry, Remote Facade Proxy, routing/health/timeout/retry 정책 고도화 | SQL, API, EDU, smoke, ADM 조회까지 연결 |
| 3 | 문서 mojibake 정리 | 재확인 필요 | README와 일부 Gradle 설명 문자열, 요청서 출력의 깨진 한글 정리 | 사람이 읽는 문서 기준 UTF-8/mojibake gate 강화 |
| 3 | evidence status 문자열 | 재확인 필요 | 과거 sanitized JSON의 status 문자열 깨짐 제거 | 신규 smoke 재실행 후 정상 한글 status 또는 영문 status 표준화 |

## 다음 작업 권장 순서

1. ADM browser click 검증 환경(Node/npm/npx 또는 Playwright/브라우저 드라이버)을 준비하고 실제 클릭 증적을 닫는다.
2. broker 실연동은 Redis/Kafka/MQ 실행 환경을 명확히 준비한 뒤 별도 증적으로 닫는다.
3. PFW Service Call Engine registry/Remote Facade Proxy를 SQL, ADM, EDU, smoke와 함께 확장한다.
4. README 외 상세 가이드 문서의 남은 mojibake와 과거 evidence status 문자열 깨짐을 정리한다.
