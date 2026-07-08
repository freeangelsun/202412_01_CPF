# CPF 안정화 리포트

## 기준 정보

- 작업 요청서: `CPF_NEW_REQUEST.md` 요청서 04
- 최종 목표 기준: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 증적 디렉터리: `specs/evidence/20260708_01`
- Git commit/push/branch: 미수행
- 요청서 파일: 확인용으로만 사용, 수정하지 않음

## 작업 요약

- PFW 공통 capability skeleton을 추가했다. 범위는 broker, file transfer, security/credential, runtime control, ADM 관제 query DTO다.
- `scripts/check-architecture-ownership.ps1`를 추가하고 Gradle `qualityGate`에 연결했다.
- `scripts/check-spring-event-usage.ps1`를 추가하고 Gradle `qualityGate`에 연결했다.
- `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`에 PFW/CMN/업무 ownership, EXS adapter 기준, Spring Event 남용 금지 기준을 보강했다.
- `README.md`, `CPF_EVIDENCE_INDEX.md`, `CPF_GAP_MATRIX.md`, `specs/기능_구현_매트릭스.html`을 새 check id와 상태 기준으로 현행화했다.

## PFW Capability 추가 목록

- Broker: `CpfBrokerPublisher`, `CpfBrokerConsumer`, `CpfBrokerMessage`, `CpfBrokerEnvelope`, `CpfBrokerResult`, `CpfBrokerDlqPort`, `CpfBrokerReplayPort`, `CpfBrokerHealthPort`, `CpfBrokerStatus`
- File Transfer: `CpfFileTransferClient`, `CpfFileTransferPort`, `CpfFileTransferRequest`, `CpfFileTransferResult`, `CpfFileTransferEndpoint`, `CpfFileTransferHistoryPort`, `CpfFileTransferHealthPort`, `CpfFileTransferStatus`
- Security/Credential: `CpfCredentialRef`, `CpfSecretProviderPort`, `CpfKeyProviderPort`, `CpfCertificateProviderPort`, `CpfSignaturePort`, `CpfEncryptionPort`, `CpfCredentialStatus`
- Runtime Control: `CpfDistributedLockPort`, `CpfHeartbeatPort`, `CpfHealthCheckPort`, `CpfGhostDetectorPort`, `CpfLockHandle`, `CpfRuntimeHealthStatus`, `CpfRuntimeGhostCandidate`
- Admin query DTO: `CpfBrokerStatusQuery`, `CpfFileTransferStatusQuery`, `CpfCredentialStatusQuery`, `CpfRuntimeHealthStatusQuery`

## 실행 검증

| 명령 | 결과 | 증적 |
| --- | --- | --- |
| `.\gradlew.bat :pfw:test --offline --no-daemon --console=plain --tests cpf.pfw.common.capability.CpfCapabilityContractTest` | 통과 | `specs/evidence/20260708_01/pfw-capability-contract-test.log` |
| `.\gradlew.bat :pfw:test --offline --no-daemon --console=plain --tests cpf.pfw.common.servicecall.*` | 통과 | `specs/evidence/20260708_01/pfw-service-call-engine-test.log` |
| `.\gradlew.bat :adm:test --offline --no-daemon --console=plain --tests cpf.adm.opr.controller.AdmServiceRegistryControllerTest` | 통과 | `specs/evidence/20260708_01/adm-service-registry-api-test.log` |
| `.\gradlew.bat :pfw:test --offline --no-daemon --console=plain` | 통과 | `specs/evidence/20260708_01/pfw-test.log` |
| `scripts/check-architecture-ownership.ps1` | 재확인 필요 | `specs/evidence/20260708_01/architecture-ownership-scan.sanitized.json` |
| `scripts/check-spring-event-usage.ps1` | 통과 | `specs/evidence/20260708_01/spring-event-usage-scan.sanitized.json` |
| `scripts/check-service-call-boundary.ps1` | 통과 | `specs/evidence/20260708_01/forbidden-direct-call-scan.log` |
| service-call source smoke 4종 | 통과 | `specs/evidence/20260708_01/service-call-engine-runtime-success.sanitized.json` 외 |
| ADM service registry static/runtime-prep smoke | 정적 통과, runtime 미검증 | `specs/evidence/20260708_01/adm-service-registry-runtime-result.json` |

## Architecture Ownership Scan 결과

`architecture-ownership-scan`은 실패 0건, 재확인 후보 6건이다. 즉시 금지 의존성은 발견되지 않았지만 아래 항목은 후속 정리가 필요하다.

- CMN 기술 engine 후보: `cmn/fle` 파일 전송 protocol/request/service, `cmn/mqe/CmnMessageBridgeService`
- EDU URL literal 후보: `xyz/src/main/java/cpf/xyz/edu/controller/XyzServiceCallEducationController.java`

이 항목은 완료가 아니라 `재확인 필요`로 유지한다.

## Spring Event Scan 결과

`spring-event-usage-scan`은 금지 후보 0건이다. 허용된 사용처는 로그/감사/메타 초기화 보조 용도 5건이다. Spring Event를 핵심 거래 흐름, 외부 송신, DLQ/replay, unknown result, reconciliation 완료 근거로 사용한 곳은 이번 scan 기준 발견되지 않았다.

## Source-only와 Runtime 구분

- PFW Service Call Engine은 source/unit smoke와 contract test 기준 `부분 구현`이다.
- ACC/MBR/EXS/ADM 다중 서비스 실기동 HTTP runtime은 이번에 실행하지 않아 `미검증`이다.
- ADM Service Registry는 controller test와 정적 UI smoke만 통과했다. 실제 ADM 서버 API 호출과 browser click은 `미검증`이다.
- MariaDB 신규 빈 DB full install은 이번 작업에서 새로 실행하지 않았다. 기존 증적은 유지하되 이번 작업 신규 완료로 올리지 않는다.
- 실제 Kafka/MQ/Redis Stream broker와 SFTP/FTP/SSH 서버 연동은 실행하지 않아 `미검증`이다.

## EDU/DB 기준 참고

- EDU mapper DB slice 검증 기준 파일: `xyz_edu_query_fixture.sql`
- EDU DB 환경변수 기준: `CPF_XYZ_EDU_MAPPER_DB_USER`, `CPF_XYZ_EDU_MAPPER_DB_USERNAME`
- MariaDB full install 검증 스크립트: `scripts/smoke-mariadb-full-install.ps1`
- 표준 헤더 E2E 검증 스크립트: `scripts/smoke-standard-header-e2e.ps1`

## Check ID 상태

| check id | 상태 | 비고 |
| --- | --- | --- |
| edu-mapper-db-slice | 완료 | 이전 검증 증적 유지 |
| mariadb-full-install | 완료 | 이전 검증 증적 유지, 이번 작업 신규 실행은 아님 |
| adm-runtime | 완료 | 이전 검증 증적 유지 |
| adm-permission-runtime | 완료 | 이전 검증 증적 유지 |
| openapi-runtime | 완료 | 이전 검증 증적 유지 |
| adm-browser-click | 미검증 | browser click 미수행 |
| standard-header-e2e | 완료 | 이전 검증 증적 유지 |
| complex-transaction-trace | 완료 | 이전 검증 증적 유지 |
| transaction-segment-log | 완료 | 이전 검증 증적 유지 |
| adm-transaction-group-list | 완료 | 이전 검증 증적 유지 |
| adm-transaction-timeline | 완료 | 이전 검증 증적 유지 |
| cmn-fixed-length-engine | 완료 | 이전 검증 증적 유지 |
| composite-runtime-smoke | 완료 | 이전 검증 증적 유지 |
| adm-transaction-group-runtime | 완료 | 이전 검증 증적 유지 |
| redis-kafka-mq-broker | 미검증 | 실제 broker 미실행 |
| broker-real-integration | 미검증 | 실제 broker 장애/fallback 미실행 |
| file-log-standard | 완료 | 이전 검증 증적 유지 |
| trace-boost-runtime | 완료 | 이전 검증 증적 유지 |
| bat-trace-boost-runtime | 완료 | 이전 검증 증적 유지 |
| runtime-start-services | 완료 | 이전 검증 증적 유지 |
| packaged-runtime-resources | 완료 | 이전 검증 증적 유지 |
| runtime-status-diagnostics | 완료 | 이전 검증 증적 유지 |
| runtime-closure | 완료 | 이전 검증 증적 유지 |
| adm-operation-console-runtime | 완료 | 이전 검증 증적 유지 |
| adm-log-policy-ui-static | 완료 | 이전 검증 증적 유지 |
| bat-log-bean-runtime | 완료 | 이전 검증 증적 유지 |
| exs-timeout-retry-runtime | 완료 | 이전 검증 증적 유지, EXS 기술 소유 의미 아님 |
| cmn-fixed-length-advanced | 완료 | 이전 검증 증적 유지 |
| create-domain-smoke | 완료 | 이전 검증 증적 유지 |
| pfw-service-call-engine | 부분 구현 | source/unit smoke 통과, 다중 서비스 runtime 미검증 |
| adm-service-registry-runtime | 미검증 | API 단위/정적 UI 통과, 실제 ADM API 미호출 |
| architecture-ownership-scan | 재확인 필요 | 실패 0건, CMN/EDU 재확인 후보 6건 |
| spring-event-usage-scan | 완료 | 금지 후보 0건 |
| pfw-broker-capability | 부분 구현 | skeleton/test 통과, 실제 broker runtime 미검증 |
| pfw-file-transfer-capability | 부분 구현 | skeleton/test 통과, 실제 SFTP/FTP/SSH runtime 미검증 |
| pfw-security-credential-capability | 부분 구현 | skeleton/test 통과, Vault/KMS/HSM 미연동 |
| pfw-runtime-control-capability | 부분 구현 | skeleton/test 통과, 다중 인스턴스 runtime 미검증 |
| pfw-admin-status-capability | 부분 구현 | query DTO skeleton/test 통과 |
| runtime-smoke-summary | 완료 | 이전 검증 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | 마지막 정합성 검증 후 갱신 |
| quality-gate | 완료 | 마지막 qualityGate 실행 후 갱신 |
| check-html-docs | 완료 | 마지막 HTML docs 검증 후 갱신 |
| check-feature-evidence | 완료 | 마지막 feature evidence 검증 후 갱신 |
| check-utf8 | 완료 | 마지막 UTF-8/mojibake 검증 후 갱신 |

## 후속 Gap

- CMN `CmnMessageBridgeService`, `cmn/fle` 파일 전송 후보가 프로젝트 공통 helper인지 기술 engine인지 재분류해야 한다.
- PFW broker/file transfer/security/runtime port의 실제 adapter와 DB 이력/ADM 관제 API/UI는 후속 구현 대상이다.
- Service Call Engine 다중 서비스 runtime, ADM Service Registry runtime/browser click, MariaDB 신규 빈 DB full install, real broker/file-transfer runtime은 아직 미검증이다.
