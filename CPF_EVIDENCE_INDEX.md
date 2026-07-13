# CPF 증적 인덱스

작성 시각: 2026-07-13 16:36 KST

기준 증적 디렉터리: `specs/evidence/20260713_02`

| check id | 상태 | 증적 | 확인 기준 | 비고 |
| --- | --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log` | report/matrix/evidence 정합성 | 기존 EDU mapper DB slice 증적 유지 |
| mariadb-full-install | 미검증 | `specs/evidence/20260713_02/mariadb-preflight.sanitized.log` | report/matrix/evidence 정합성 | CLI는 확인했으나 현재 셸의 DB secret 부재로 접속·설치 SQL·Flyway 미실행 |
| adm-runtime | 완료 | `specs/evidence/20260707_02/adm-runtime-smoke-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM runtime smoke 증적 유지 |
| adm-permission-runtime | 완료 | `specs/evidence/20260707_02/adm-permission-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM 권한 runtime smoke 증적 유지 |
| openapi-runtime | 부분 구현 | `specs/evidence/20260713_02/openapi-source-coverage.sanitized.json`, `specs/evidence/20260713_02/openapi-runtime.sanitized.log` | report/matrix/evidence 정합성 | 59개 Controller·349개 mapping operationId 소스 검사는 통과, 현재 앱 `/v3/api-docs` 미실행 |
| adm-browser-click | 미검증 | `specs/evidence/20260713_02/adm-browser-click.sanitized.log` | report/matrix/evidence 정합성 | ADM 앱·DB를 기동한 브라우저 클릭 미실행 |
| standard-header-e2e | 완료 | `specs/evidence/20260707_02/standard-header-e2e-result.sanitized.json` | report/matrix/evidence 정합성 | scripts/smoke-standard-header-e2e.ps1 기준 기존 증적 |
| complex-transaction-trace | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 복합 거래 trace 증적 유지 |
| transaction-segment-log | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 segment log 증적 유지 |
| adm-transaction-group-list | 완료 | `specs/evidence/20260707_02/adm-operation-console-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM 거래 그룹 목록 증적 유지 |
| adm-transaction-timeline | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM timeline 증적 유지 |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260708_05/cmn-test.log` | report/matrix/evidence 정합성 | 기존 CMN fixed-length 테스트 증적 유지 |
| composite-runtime-smoke | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 composite runtime smoke 증적 유지 |
| adm-transaction-group-runtime | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM 거래 그룹 runtime 증적 유지 |
| redis-kafka-mq-broker | 미검증 | 없음 | report/matrix/evidence 정합성 | 실 Redis/Kafka/MQ broker runtime 미실행 |
| broker-real-integration | 미검증 | 없음 | report/matrix/evidence 정합성 | 실 broker adapter와 장애 fallback runtime 미실행 |
| file-log-standard | 부분 구현 | `specs/evidence/20260713_02/log-management-standard.sanitized.json`, `specs/evidence/20260713_02/reliability-log-tests.sanitized.log` | report/matrix/evidence 정합성 | 단위·정적 표준 검증 완료, 전체 앱·다중 인스턴스 runtime 미검증 |
| trace-boost-runtime | 완료 | `specs/evidence/20260707_02/trace-boost-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 trace boost runtime 증적 유지 |
| bat-trace-boost-runtime | 완료 | `specs/evidence/20260707_02/bat-trace-boost-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 BAT trace boost 증적 유지 |
| runtime-start-services | 완료 | `specs/evidence/20260707_02/runtime-start-services-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 runtime start services 증적 유지 |
| packaged-runtime-resources | 완료 | `specs/evidence/20260707_02/packaged-runtime-resource-check.sanitized.json` | report/matrix/evidence 정합성 | 기존 packaged resource 증적 유지 |
| runtime-status-diagnostics | 완료 | `specs/evidence/20260707_02/runtime-status-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 runtime status 증적 유지 |
| runtime-closure | 완료 | `specs/evidence/20260707_02/runtime-closure-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 runtime closure 증적 유지 |
| adm-operation-console-runtime | 완료 | `specs/evidence/20260707_02/adm-operation-console-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM operation console runtime 증적 유지 |
| adm-log-policy-ui-static | 완료 | `specs/evidence/20260707_02/adm-log-policy-ui-static-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM log policy UI static 증적 유지 |
| bat-log-bean-runtime | 완료 | `specs/evidence/20260707_02/bat-log-bean-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 BAT log bean runtime 증적 유지 |
| exs-timeout-retry-runtime | 완료 | `specs/evidence/20260708_05/exs-test.log` | report/matrix/evidence 정합성 | 기존 EXS timeout/retry 테스트 증적 유지 |
| cmn-fixed-length-advanced | 완료 | `specs/evidence/20260708_05/cmn-test.log` | report/matrix/evidence 정합성 | 기존 CMN advanced fixed-length 테스트 증적 유지 |
| create-domain-smoke | 완료 | `specs/evidence/20260713_02/create-domain-smoke.sanitized.log` | report/matrix/evidence 정합성 | 최신 로그 템플릿을 포함한 LNG 생성·test·bootJar 실제 통과 |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260713_02/reliability-log-tests.sanitized.log` | report/matrix/evidence 정합성 | 현재 PFW 테스트 통과, 다중 HTTP runtime 미검증 |
| adm-service-registry-runtime | 완료 | `specs/evidence/20260707_02/adm-service-registry-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM service registry runtime 증적 유지 |
| architecture-ownership-scan | 재확인 필요 | `specs/evidence/20260709_03/architecture-ownership-scan.sanitized.json` | report/matrix/evidence 정합성 | CMN 기술 실행 후보는 PFW migration 추적 필요 |
| spring-event-usage-scan | 완료 | `specs/evidence/20260709_03/spring-event-usage-scan.sanitized.json` | report/matrix/evidence 정합성 | Spring Event usage scan 통과 증적 유지 |
| pfw-broker-capability | 부분 구현 | `specs/evidence/20260713_02/reliability-log-tests.sanitized.log` | report/matrix/evidence 정합성 | worker와 결정적 adapter 테스트 통과, 실 broker·JDBC 재기동 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs/evidence/20260713_02/reliability-log-tests.sanitized.log` | report/matrix/evidence 정합성 | LOCAL base path·symlink·checksum·temp cleanup 통과, 외부 protocol·JDBC 재기동 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs/evidence/20260709_03/security-credential-contract-test.sanitized.json` | report/matrix/evidence 정합성 | Vault/KMS runtime 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs/evidence/20260709_03/runtime-worker-contract-test.sanitized.json` | report/matrix/evidence 정합성 | 다중 instance runtime 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs/evidence/20260713_02/reliability-log-tests.sanitized.log`, `specs/evidence/20260713_02/adm-ui-js-syntax.sanitized.log`, `specs/evidence/20260713_02/adm-browser-click.sanitized.log` | report/matrix/evidence 정합성 | API/UI 문법·서비스 테스트 통과, 실제 DB runtime과 브라우저 클릭 미검증 |
| profile-loading-standard | 완료 | `specs/evidence/20260708_03/profile-loading-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 profile loading 증적 유지 |
| packaged-dependencies-check | 완료 | `specs/evidence/20260708_05/packaged-dependencies-acc.sanitized.json` | report/matrix/evidence 정합성 | 기존 packaged dependencies 증적 유지 |
| deploy-dry-run-standard | 부분 구현 | `specs/evidence/20260708_05/remote-deploy-dry-run.sanitized.json` | report/matrix/evidence 정합성 | dry-run 계획 검증 |
| garbage-file-cleanup | 완료 | `specs/evidence/20260713_02/garbage-file-scan.sanitized.json` | report/matrix/evidence 정합성 | 현재 저장소 garbage scan 통과 |
| empty-directory-scan | 완료 | `specs/evidence/20260713_02/empty-directory-scan.sanitized.json` | report/matrix/evidence 정합성 | 현재 저장소 empty directory scan 통과 |
| deploy-env-standard | 완료 | `specs/evidence/20260708_05/deploy-env-acc-dev.sanitized.json` | report/matrix/evidence 정합성 | 기존 deploy env 증적 유지 |
| deploy-inventory-standard | 완료 | `specs/evidence/20260708_05/deploy-inventory-acc-dev.sanitized.json` | report/matrix/evidence 정합성 | 기존 deploy inventory 증적 유지 |
| gradle-deploy-task-standard | 완료 | `specs/evidence/20260708_05/gradle-remote-deploy-task-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 Gradle deploy task scan 증적 유지 |
| datasource-mode-standard | 완료 | `specs/evidence/20260708_05/datasource-mode-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 datasource mode scan 증적 유지 |
| local-port-duplicate-scan | 완료 | `specs/evidence/20260708_05/local-port-duplicate-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 local port duplicate scan 증적 유지 |
| edu-module-deploy-alias-scan | 완료 | `specs/evidence/20260708_05/edu-module-deploy-alias-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 EDU deploy alias scan 증적 유지 |
| bat-edu-package | 완료 | `specs/evidence/20260713_02/reliability-log-tests.sanitized.log` | report/matrix/evidence 정합성 | BAT 전체 테스트와 로그 EDU 통과 |
| bat-job-log-policy | 완료 | `specs/evidence/20260713_02/reliability-log-tests.sanitized.log` | report/matrix/evidence 정합성 | JobInstance 분리·restart·업무일자·JSON Lines 검증 통과 |
| sample-coverage-matrix | 완료 | `specs/evidence/20260713_02/sample-coverage-result.sanitized.json` | report/matrix/evidence 정합성 | 신규 XYZ/BAT 로그 EDU 포함 coverage 검사 통과 |
| sample-placeholder-scan | 완료 | `specs/evidence/20260713_02/sample-placeholder-scan.sanitized.json` | report/matrix/evidence 정합성 | sample placeholder scan 통과 |
| evidence-path-existence-check | 완료 | `specs/evidence/20260713_02/evidence-path-existence-check.sanitized.json` | report/matrix/evidence 정합성 | current evidence 추적·stale·ID·raw 로그 금지 검사 통과 |
| create-domain-profile-template | 완료 | `specs/evidence/20260708_05/garbage-file-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 create-domain profile template 증적 유지 |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_02/runtime-smoke-summary.sanitized.json` | report/matrix/evidence 정합성 | 기존 runtime smoke summary 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260713_02/report-matrix-evidence-consistency.sanitized.json` | report/matrix/evidence 정합성 | 61개 check ID 상태 일치 검사 통과 |
| quality-gate | 완료 | `specs/evidence/20260713_02/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | Java 21 전체 gate 통과, JUnit 301건·실패 0·오류 0·건너뜀 3 |
| check-html-docs | 완료 | `specs/evidence/20260713_02/static-quality-gates.sanitized.log` | report/matrix/evidence 정합성 | 현재 HTML docs gate 통과 |
| check-feature-evidence | 완료 | `specs/evidence/20260713_02/static-quality-gates.sanitized.log` | report/matrix/evidence 정합성 | 현재 feature evidence gate 통과 |
| check-utf8 | 완료 | `specs/evidence/20260713_02/static-quality-gates.sanitized.log` | report/matrix/evidence 정합성 | UTF-8 및 mojibake gate 통과 |

## 추가 작업 증적

| 증적 | 상태 | 내용 |
| --- | --- | --- |
| `specs/evidence/20260713_02/worktree-diff.sanitized.log` | 완료 | 시작 commit, 요청서 hash, raw 로그 추적 제거 수, 변경 경로 목록 |
| `specs/evidence/20260713_02/git-diff-check.sanitized.log` | 완료 | `git diff --check` 종료 코드와 정제 출력 |
| `specs/evidence/20260713_02/xyz-log-edu-tests.sanitized.log` | 완료 | XYZ 일반 로그·마스킹·ADM BAT 로그 조회 EDU 테스트 3건 |
| `specs/evidence/20260713_02/java25-pfw-test.sanitized.log` | 실패 | Java 25와 현재 Gradle/Spring Boot 조합의 구성 단계 실패 |
