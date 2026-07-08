# CPF 안정화 작업 리포트

## 기준 정보

- 요청서: `CPF_NEW_REQUEST.md`
- 최종 목표 기준: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 이번 증적 기준 디렉터리: `specs/evidence/20260708_02`
- 원칙: 실행하지 않은 검증은 완료로 쓰지 않는다. dry-run 배포는 실제 원격 배포 완료가 아니다.

## 이번 작업 요약

- PFW broker/file transfer/security/runtime capability 2차 skeleton을 보강하고 contract test 대상으로 연결했다.
- PFW/CMN profile과 업무 서비스별 `local/dev/stg/prod` profile 파일 및 import 표준을 추가했다.
- 공통 배포 dry-run 스크립트, env/inventory, 패키징 의존성 점검 스크립트를 추가했다.
- `qualityGate`에 architecture ownership, Spring Event, profile loading, packaged dependency, deploy dry-run, report/matrix/evidence 정합성 검사를 연결했다.
- `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`에 PFW/CMN/EXS ownership, Spring Event 제한, 증적 기준을 다시 명시했다.

## 검증 상태 매트릭스

| check id | 상태 | 근거 요약 |
| --- | --- | --- |
| edu-mapper-db-slice | 완료 | 기존 DB slice 증적 유지 |
| mariadb-full-install | 완료 | 기존 MariaDB full install 증적 유지 |
| adm-runtime | 완료 | 기존 ADM runtime smoke 증적 유지 |
| adm-permission-runtime | 완료 | 기존 ADM permission runtime 증적 유지 |
| openapi-runtime | 완료 | 기존 OpenAPI runtime 증적 유지 |
| adm-browser-click | 미검증 | static smoke만 있으며 실제 브라우저 클릭 증적 없음 |
| standard-header-e2e | 완료 | 기존 표준 헤더 E2E 증적 유지 |
| complex-transaction-trace | 완료 | 기존 복합 거래 trace 증적 유지 |
| transaction-segment-log | 완료 | 기존 segment log 증적 유지 |
| adm-transaction-group-list | 완료 | 기존 ADM 거래 그룹 목록 증적 유지 |
| adm-transaction-timeline | 완료 | 기존 ADM timeline 증적 유지 |
| cmn-fixed-length-engine | 완료 | 기존 CMN fixed-length engine 증적 유지 |
| composite-runtime-smoke | 완료 | 기존 composite runtime smoke 증적 유지 |
| adm-transaction-group-runtime | 완료 | 기존 ADM transaction group runtime 증적 유지 |
| redis-kafka-mq-broker | 미검증 | 실제 broker runtime 미수행 |
| broker-real-integration | 미검증 | Redis/Kafka/MQ 실연동 미수행 |
| file-log-standard | 완료 | 기존 file log standard 증적 유지 |
| trace-boost-runtime | 완료 | 기존 trace boost runtime 증적 유지 |
| bat-trace-boost-runtime | 완료 | 기존 BAT trace boost runtime 증적 유지 |
| runtime-start-services | 완료 | 기존 runtime start 증적 유지 |
| packaged-runtime-resources | 완료 | 기존 packaged runtime resource 증적 유지 |
| runtime-status-diagnostics | 완료 | 기존 runtime status/diagnostics 증적 유지 |
| runtime-closure | 완료 | 기존 runtime closure 증적 유지 |
| adm-operation-console-runtime | 완료 | 기존 ADM operation console 증적 유지 |
| adm-log-policy-ui-static | 완료 | 기존 ADM log policy static 증적 유지 |
| bat-log-bean-runtime | 완료 | 기존 BAT log bean runtime 증적 유지 |
| exs-timeout-retry-runtime | 완료 | 기존 EXS timeout/retry runtime 증적 유지 |
| cmn-fixed-length-advanced | 완료 | 기존 fixed-length advanced 증적 유지 |
| create-domain-smoke | 완료 | 기존 create-domain smoke 증적 유지 |
| pfw-service-call-engine | 부분 구현 | source/unit smoke와 boundary scan은 수행, 다중 서비스 실제 HTTP runtime은 후속 |
| adm-service-registry-runtime | 미검증 | API 단위/정적 UI smoke 대상, 실제 `-RunRuntime` ADM 서버 호출은 후속 |
| architecture-ownership-scan | 재확인 필요 | failure 0 목표, CMN PFW port migration 후보는 warning으로 분류 |
| spring-event-usage-scan | 완료 | Spring Event forbidden usage scan 대상 |
| pfw-broker-capability | 부분 구현 | port/interface/DTO/contract test까지, 실제 broker runtime은 후속 |
| pfw-file-transfer-capability | 부분 구현 | protocol/policy/checksum/history DTO까지, 실제 SFTP/FTP/SSH runtime은 후속 |
| pfw-security-credential-capability | 부분 구현 | credential/token port와 secret 출력 방지 contract까지, 실제 Vault/KMS/HSM은 후속 |
| pfw-runtime-control-capability | 부분 구현 | lock/heartbeat/worker/health DTO까지, 다중 instance runtime은 후속 |
| pfw-admin-status-capability | 부분 구현 | ADM 관제 연결 후보 DTO까지, 운영 화면 실사용은 후속 |
| profile-loading-standard | 완료 | PFW/CMN/업무 profile loading smoke 대상 |
| packaged-dependencies-check | 완료 | ACC bootJar 기준 PFW/CMN runtime dependency 포함 여부 점검 대상 |
| deploy-dry-run-standard | 부분 구현 | env/inventory/checksum/evidence dry-run까지, 실제 remote 전송/기동은 미검증 |
| runtime-smoke-summary | 완료 | 기존 runtime smoke summary 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | report/matrix/evidence index 상태 정합성 gate 대상 |
| quality-gate | 완료 | 이번 작업 최종 qualityGate 대상 |
| check-html-docs | 완료 | HTML 문서 구조와 matrix 상태 marker gate 대상 |
| check-feature-evidence | 완료 | 소스/문서/스크립트 증적 존재 gate 대상 |
| check-utf8 | 완료 | UTF-8/mojibake gate 대상 |

## 남은 리스크

- 실제 원격 배포, 원격 서버 stop/start/health/rollback은 접속 대상과 승인 정보가 없어 미검증이다.
- Redis/Kafka/MQ, SFTP/FTP/SSH, Vault/KMS/HSM 실연동은 이번 범위에서 skeleton/port 수준이며 runtime adapter 검증은 후속이다.
- ADM Service Registry 실제 서버 호출은 `scripts/smoke-adm-service-registry-runtime.ps1 -RunRuntime` 증적이 아직 필요하다.
- architecture ownership scan에서 CMN 파일 전송/메시지 bridge 일부는 PFW port migration 후보로 남긴다.

## 실행 검증 결과

| 명령 | 결과 | 증적 |
| --- | --- | --- |
| `.\gradlew.bat :pfw:test --offline --no-daemon --console=plain` | 성공 | `specs/evidence/20260708_02/pfw-test.log` |
| `.\gradlew.bat :cmn:test --offline --no-daemon --console=plain` | 성공 | `specs/evidence/20260708_02/cmn-test.log` |
| `.\gradlew.bat :adm:test --offline --no-daemon --console=plain --tests cpf.adm.opr.controller.AdmServiceRegistryControllerTest` | 성공 | `specs/evidence/20260708_02/adm-service-registry-api-test.log` |
| `.\gradlew.bat :pfw:test --offline --no-daemon --console=plain --tests cpf.pfw.common.servicecall.*` | 성공 | `specs/evidence/20260708_02/pfw-service-call-engine-test.log` |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-profile-loading.ps1 -ResultDir specs/evidence/20260708_02` | 성공 | `specs/evidence/20260708_02/profile-loading-result.sanitized.json` |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/deploy/deploy-module.ps1 -Module ACC -Env dev -DryRun -BuildBeforeDeploy -ResultDir specs/evidence/20260708_02` | 성공 | `specs/evidence/20260708_02/deploy-acc-dev-dry-run.sanitized.json` |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-report-matrix-evidence-consistency.ps1` | 성공 | `specs/evidence/20260708_02/check-report-matrix-evidence-consistency.log` |
| `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | 성공 | `specs/evidence/20260708_02/quality-gate.log` |

미실행 검증:

- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1`은 이번 요청의 새 대상이 아니므로 재실행하지 않았다.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-standard-header-e2e.ps1`은 이번 요청의 새 대상이 아니므로 재실행하지 않았다.
- 실제 원격 배포, ADM `-RunRuntime`, 실제 broker/SFTP/Vault 연동은 미검증이다.

## EDU/DB fixture marker

- EDU mapper fixture 표준 파일명: `xyz_edu_query_fixture.sql`
- EDU mapper DB 사용자 환경변수: `CPF_XYZ_EDU_MAPPER_DB_USERNAME`
- 호환 확인용 legacy 환경변수명: `CPF_XYZ_EDU_MAPPER_DB_USER`
